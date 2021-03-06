package cz.payola.model.components

import cz.payola.data._
import cz.payola.domain.entities._
import cz.payola.domain.entities.plugins.PluginInstance
import cz.payola.model._
import cz.payola.domain.entities.plugins.parameters._
import cz.payola.domain.entities.plugins.parameters.StringParameterValue
import scala.collection.mutable.HashMap
import cz.payola.domain.entities.analyses.evaluation._
import cz.payola.domain.IDGenerator
import cz.payola.common._
import scala.Some
import cz.payola.common.EvaluationError
import cz.payola.domain.entities.analyses.evaluation.Success
import cz.payola.domain.entities.analyses.evaluation.Error

trait AnalysisModelComponent extends EntityModelComponent
{
    self: DataContextComponent with PrivilegeModelComponent =>
    val runningEvaluations: HashMap[String, (Option[User], AnalysisEvaluation, Long)] = new
            HashMap[String, (Option[User], AnalysisEvaluation, Long)]

    lazy val analysisModel = new ShareableEntityModel(analysisRepository, classOf[Analysis])
    {
        def addBinding(analysisId: String, sourceId: String, targetId: String, inputIndex: Int) {
            getById(analysisId).map {
                a =>
                    val source = a.pluginInstances.find(_.id == sourceId)
                    val target = a.pluginInstances.find(_.id == targetId)

                    if (!source.isDefined || !target.isDefined) {
                        throw new Exception("Invalid source or target.")
                    }

                    a.addBinding(source.get, target.get, inputIndex)
            }.getOrElse {
                throw new Exception("Unknown analysis.")
            }
        }

        def create(owner: User, name: String): Analysis = {
            val analysis = new Analysis(name, Some(owner))
            persist(analysis)
            analysis
        }

        def createPluginInstance(pluginId: String, analysisId: String): PluginInstance = {
            val analysis = analysisRepository.getById(analysisId).getOrElse {
                throw new ModelException("Unknown analysis ID.")
            }

            val instance = pluginRepository.getById(pluginId).map(_.createInstance()).getOrElse {
                throw new ModelException("Unknown plugin ID.")
            }

            analysis.addPluginInstance(instance)
            instance
        }

        def setParameterValue(user: User, analysisId: String, pluginInstanceId: String, parameterName: String,
            value: String) {
            val analysis = user.ownedAnalyses
                .find(_.id == analysisId)
                .get

            val pluginInstance = analysis.pluginInstances.find(_.id == pluginInstanceId)

            pluginInstance.map {
                i =>

                    if (!i.isEditable) {
                        throw new ModelException("The plugin instance is not editable.")
                    }

                    val option = i.getParameterValue(parameterName)

                    if (!option.isDefined) {
                        throw new Exception("Unknown parameter name: " + parameterName + ".")
                    }

                    val parameterValue = option.get

                    parameterValue match {
                        case v: BooleanParameterValue => v.value = value.toBoolean
                        case v: FloatParameterValue => v.value = value.toFloat
                        case v: IntParameterValue => v.value = value.toInt
                        case v: StringParameterValue => v.value = value
                        case _ => throw new Exception("Unknown parameter type.")
                    }

                    analysisRepository.persistParameterValue(parameterValue)
            }.getOrElse {
                throw new ModelException("Unknown plugin instance ID.")
            }
        }

        def removePluginInstanceById(analysisId: String, pluginInstanceId: String): Boolean = {
            val analysis = analysisRepository.getById(analysisId).getOrElse {
                throw new ModelException("Unknown analysis ID.")
            }

            val instance = analysis.pluginInstances.find(_.id == pluginInstanceId).getOrElse {
                throw new ModelException("Unknown plugin instance ID.")
            }

            analysis.removePluginInstance(instance)
            analysis.pluginInstances.contains(instance)
        }

        def removePluginInstanceBindingById(analysisId: String, pluginInstanceBindingId: String): Boolean = {
            val analysis = analysisRepository.getById(analysisId).getOrElse {
                throw new ModelException("Unknown analysis ID.")
            }

            val binding = analysis.pluginInstanceBindings.find(_.id == pluginInstanceBindingId).getOrElse {
                throw new ModelException("Unknown plugin instance ID.")
            }

            analysis.removeBinding(binding)
            analysis.pluginInstanceBindings.contains(binding)
        }

        def run(analysis: Analysis, timeoutSeconds: Long, oldEvaluationId: String, user: Option[User] = None) = {
            if (runningEvaluations.isDefinedAt(oldEvaluationId)) {
                if (!runningEvaluations.get(oldEvaluationId).filter(_._2.analysis.id == analysis.id).isEmpty) {
                    runningEvaluations.remove(oldEvaluationId)
                }
            }

            val evaluationId = IDGenerator.newId
            val timeout = scala.math.min(1800, timeoutSeconds)
            runningEvaluations
                .put(evaluationId, (user, analysis.evaluate(Some(timeout * 1000)), (new java.util.Date).getTime))

            evaluationId
        }

        private def getEvaluationTupleForID(id: String) = {
            val date = new java.util.Date
            runningEvaluations.foreach { tuple =>
                if (tuple._2._3 + (20 * 60 * 1000) < date.getTime) {
                    runningEvaluations.remove(tuple._1)
                }
            }

            runningEvaluations.get(id).getOrElse {
                throw new ModelException("The evaluation is not running.")
            }
        }

        def getEvaluationState(evaluationId: String, user: Option[User] = None) = {
            val evaluationTuple = getEvaluationTupleForIDAndPerformSecurityChecks(evaluationId, user)

            runningEvaluations.put(evaluationId, (evaluationTuple._1, evaluationTuple._2, (new java.util.Date).getTime))

            val evaluation = evaluationTuple._2

            evaluation.getResult.map {
                case r: Error => EvaluationError(transformException(r.error),
                    r.instanceErrors.toList.map { e => (e._1, transformException(e._2))})
                case r: Success => EvaluationSuccess(r.outputGraph,
                    r.instanceErrors.toList.map { e => (e._1, transformException(e._2))})
                case Timeout => new EvaluationTimeout
                case _ => throw new Exception("Unhandled evaluation state")
            }.getOrElse {
                val progress = evaluation.getProgress
                EvaluationInProgress(progress.value, progress.evaluatedInstances, progress.runningInstances.toList,
                    progress.errors.toList.map { e => (e._1, transformException(e._2))})
            }
        }

        private def transformException(t: Throwable): String = {
            t match {
                case e: Exception => e.getMessage
                case _ => "Unknown error."
            }
        }

        def getEvaluationTupleForIDAndPerformSecurityChecks(id: String, user: Option[User]) = {
            val evaluationTuple = getEvaluationTupleForID(id)
            if (!evaluationTuple._1.isDefined || evaluationTuple._1 == user) {
                evaluationTuple
            } else {
                throw new ModelException("Forbidden evaluation.")
            }
        }
    }
}
