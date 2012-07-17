package cz.payola.web.client.views.graph.visual.graph

import collection.mutable.ListBuffer
import cz.payola.common.rdf.{LiteralVertex, IdentifiedVertex, Vertex}
import s2js.adapters.js.dom.CanvasRenderingContext2D
import cz.payola.web.client.views.graph.visual.settings._
import cz.payola.web.client.views.graph.visual.Color
import cz.payola.web.client.views.algebra._

/**
  * Graphical representation of Vertex object in the drawn graph.
  * @param vertexModel the vertex object from the model, that is visualised
  * @param position of this graphical representation in drawing space
  */
class VertexView(val vertexModel: IdentifiedVertex, var position: Point2D, var settings: VertexSettingsModel,
    settingsText: TextSettingsModel, var rdfType: Option[String]) extends View[CanvasRenderingContext2D] {

    var literalVertices = ListBuffer[LiteralVertex]()

    private var age = 0

    /*private val image = prepareImage(//TODO This has to be called after color or path change event was fired
        vertexModel match {
            case i: LiteralVertex => new Color(180, 50, 50, 1)
            case i: IdentifiedVertex => new Color(50, 180, 50, 1)
            case _ => new Color(0, 0, 0, 1)
        }, vertexModel match {
            case i: LiteralVertex => "/assets/images/book-icon.png"
            case i: IdentifiedVertex => "/assets/images/view-eye-icon.png"
            case _ => "/assets/images/question-mark-icon.png"
        })*/

    /**
      * Indicator of isSelected attribute. Does not effect inner mechanics.
      */
    var selected = false

    /**
      * List of edges that this vertex representation has. Allows to Iterate through the graphical representation
      * of the graph.
      */
    var edges = ListBuffer[EdgeView]()

    /**
      * Textual data that should be visualised with this vertex ("over this vertex").
      */
    val information: Option[InformationView] = vertexModel match {
        case i: LiteralVertex => Some(new InformationView(i, settingsText))
        case i: IdentifiedVertex => Some(new InformationView(i, settingsText))
        case _ => None
    }

    def isSelected: Boolean = {
        selected
    }

    def getCurrentAge: Int = {
        age
    }

    def resetCurrentAge() {
        age = 0
    }

    def increaseCurrentAge() {
        age += 1
    }

    def setCurrentAge(newAge: Int) {
        age = newAge
    }

    def isPointInside(point: Point2D): Boolean = {
        isPointInRect(point, position + (new Vector2D(settings.radius, settings.radius) / -2),
            position + (new Vector2D(settings.radius, settings.radius) / 2))
    }

    def draw(context: CanvasRenderingContext2D, color: Option[Color], positionCorrection: Vector2D) {
        drawQuick(context, color, positionCorrection)
        //drawImage(context, image, position + Vector2D(-10, -10), Vector2D(20, 20))

        if(information.isDefined) {
            information.get.draw(context, Some(settingsText.color), positionCorrection)
        }
    }

    def drawQuick(context: CanvasRenderingContext2D, color: Option[Color], positionCorrection: Vector2D) {
        val colorToUseOnBox = color.getOrElse(settings.color)
        val correctedPosition = this.position + positionCorrection

        drawCircle(context, correctedPosition, settings.radius / 2, 2, Color.Black)
        fillCurrentSpace(context, colorToUseOnBox)
    }

    def drawInformation(context: CanvasRenderingContext2D, color: Option[Color], positionCorrection: Vector2D) {
        if (information.isDefined) {
            vertexModel match {
                case i: IdentifiedVertex => information.get.draw(context, color, positionCorrection)
                case _ => if (selected) {
                    information.get.draw(context, color, positionCorrection)
                }
            }
        }
    }

    override def toString: String = {
        this.position.toString
        //"["+vertexModel.toString+"]"
    }

    /**
      * Compares this to another vertexView. Returns true if vertexModels.toString are equal.
      * @param vertexView
      * @return
      */
    def isEqual(vertexView: Any): Boolean = {
        if (vertexView == null) {
            false
        }
        vertexView match {
            case vv: VertexView =>
                vv.vertexModel.toString eq vertexModel.toString
            case _ => false
        }
    }
}
