package cz.payola.web.client.presenters.components

import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.elements.Div
import cz.payola.web.client.views.elements.Anchor
import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.elements.form.fields.NumericInput

class AnalysisControls(timeoutSeconds: Int) extends ComposedView
{

    val runBtnCaption = new Text("Run Analysis")
    private val runBtnIcon = new Icon(Icon.play, true)
    val runBtn = new Button(runBtnCaption, "btn btn-success span2", runBtnIcon)

    val progressBar = new ProgressBar()
    val stopButton = new Button(new Text("Stop"), "btn-danger disabled span2", new Icon(Icon.stop, true))
    stopButton.setIsEnabled(false)
    val timeoutControl = new InputControl(
        "Set timeout [sec.]:",
        new NumericInput("timeout", timeoutSeconds, "Set timeout", "input-mini timeout-control"),
        None
    )
    timeoutControl.controlGroup.addCssClass("span3")

    private val timeoutInfoCaptionPre = new Text("The evaluation will timeout in [sec.]: ")
    val timeoutInfo = new Text(timeoutSeconds.toString)
    val timeoutInfoBar = new Span(List(timeoutInfoCaptionPre, timeoutInfo), "none span3")
    private val wrap = new Div(List(runBtn, stopButton, progressBar, timeoutControl, timeoutInfoBar), "well analysis-controls")

    def createSubViews = List(wrap)
}
