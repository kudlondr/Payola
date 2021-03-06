package cz.payola.web.client.views.bootstrap

import s2js.adapters.browser._
import cz.payola.web.client.views._
import cz.payola.web.client.views.elements._
import cz.payola.common.ValidationException
import cz.payola.web.client.events.SimpleUnitEvent
import cz.payola.web.client.views.elements.form._

class InputControl[A <: Field[_]](val label: String, val field: A, labelClass: Option[String])
    extends ComposedView
{
    val delayedChanged = new SimpleUnitEvent[this.type]

    private var delayedChangedTimeout: Option[Int] = None

    private val infoText = new Text("")

    val controlGroup = new Div(List(
        new Label(label, field.formHtmlElement, labelClass.getOrElse("")),
        new Div(List(
            field,
            new Span(List(infoText), "help-inline")),
            "controls"
        )),
        "control-group "
    )

    field.changed += { _ =>
        delayedChangedTimeout.foreach(window.clearTimeout(_))
        delayedChangedTimeout = Some(window.setTimeout({ () =>
            delayedChanged.triggerDirectly(this)
        }, 500))
        true
    }

    def createSubViews = List(controlGroup)

    def setState(exception: ValidationException, fieldName: String) {
        if (fieldName == exception.fieldName) {
            setError(exception.message)
        } else {
            setOk()
        }
    }

    def setError(errorMessage: String) {
        infoText.text = errorMessage
        controlGroup.removeCssClass("success")
        controlGroup.addCssClass("error")
    }

    def setOk() {
        infoText.text = ""
        controlGroup.removeCssClass("error")
        controlGroup.addCssClass("success")
    }

    def isActive: Boolean = field.isActive

    def isActive_=(newValue: Boolean) {
        field.isActive = newValue
    }
}
