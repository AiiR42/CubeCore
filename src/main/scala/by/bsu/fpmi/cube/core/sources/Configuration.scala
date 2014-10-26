package by.bsu.fpmi.cube.core.sources

import scala.xml._

class Configuration(val metaInfoPath: String) {

  val metaContent = XML.loadFile(metaInfoPath)

  val factsTableName = (metaContent \ "meta" \ "facts" \ "@name").text
  val factsDataFields = (metaContent \ "meta" \ "facts" \ "fields" \ "data" \ "name").toSeq.map(_.text)
  def factsFkField(tableName: String) = (metaContent \ "meta" \ "facts" \ "fields" \ "field").filter { field =>
    field.attribute("fk").contains(tableName)
  }.toSeq.map(_.text)

  val dimensions = (metaContent \ "meta" \ "dimension" \ "@name").toSeq.map(_.text)
  def dimensionKey(dimension: String) = {
    val dimensionNode = (metaContent \ "meta" \ "dimension").filter { node =>
      node.attribute("name").contains(dimension)
    }
    (dimensionNode \ "fields" \ "@key").text
  }

  def dimensionDataFields(dimension: String) = {
    val dimensionNode = (metaContent \ "meta" \ "dimension").filter { node =>
      node.attribute("name").contains(dimension)
    }
    (dimensionNode \ "fields" \ "name").toSeq.map(_.text)
  }

}
