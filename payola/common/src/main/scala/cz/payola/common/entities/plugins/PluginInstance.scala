package cz.payola.common.entities.plugins

import scala.collection.immutable
import cz.payola.common.entities._
import cz.payola.common.Entity

/**
  * An instance of an analytical plugin. That is a valuation of the plugin parameters.
  */
trait PluginInstance extends Entity with DescribedEntity
{
    /** Type of the plugin the current object is instance of. */
    type PluginType <: Plugin

    protected var _plugin: PluginType

    protected var _parameterValues: immutable.Seq[PluginType#ParameterValueType]

    /** The corresponding analytical plugin. */
    def plugin = _plugin

    /** The plugin parameter values. */
    def parameterValues: immutable.Seq[PluginType#ParameterValueType] = _parameterValues
}
