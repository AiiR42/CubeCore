package by.bsu.fpmi.cube.core.sources

import scala.xml._

class Configuration(val metaInfoPath: String) {

  val metaContent = XML.loadFile(metaInfoPath)

  val factsTableName = (metaContent \ "facts" \ "@name").text.trim
  val factsDataFields = (metaContent \ "facts" \ "fields" \ "data" \ "name").toSeq.map(_.text.trim)
  def factsFkField(tableName: String) = (metaContent \ "facts" \ "fields" \ "field").filter { field =>
    field.attribute("fk").flatMap(_.find(_.toString() == tableName)).isDefined
  }.toSeq.map(_.text).head.trim

  val dimensions = (metaContent \ "dimension").map(_ \ "@name").toSeq.map(_.text)
  def dimensionKey(dimension: String) = {
    val dimensionNode = (metaContent \ "dimension").filter { node =>
      node.attribute("name").flatMap(_.find(_.toString() == dimension)).isDefined
    }
    (dimensionNode \ "fields" \ "@key").text.trim
  }

  def dimensionDataFields(dimension: String) = {
    val dimensionNode = (metaContent \ "dimension").filter { node =>
      node.attribute("name").flatMap(_.find(_.toString() == dimension)).isDefined
    }
    (dimensionNode \ "fields" \ "field" \ "name").toSeq.map(_.text.trim)
  }

}
