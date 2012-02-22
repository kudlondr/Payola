package cz.payola.data.model.graph

import cz.payola.scala2json.annotations.JSONPoseableClass

@JSONPoseableClass(otherClassName = "cz.payola.common.rdf.IdentifiedVertex")
class RDFIdentifiedNode(override val uri: String) extends RDFNode with cz.payola.common.rdf.IdentifiedVertex {

}
