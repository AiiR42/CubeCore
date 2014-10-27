package by.bsu.fpmi.cube.ui

import by.bsu.fpmi.cube.core.models.data.{Entry, FactEntry, DimensionEntry}
import by.bsu.fpmi.cube.core.models.filters.DiscreteFilter
import by.bsu.fpmi.cube.core.models.types.DimensionType
import by.bsu.fpmi.cube.core.services.CubeService

object Console {

  private def printTable(xValues: Seq[DimensionEntry], yValues: Seq[DimensionEntry], result: Map[Seq[DimensionEntry], FactEntry]): Unit = {
    val printableXValues = Seq(None) ++ xValues.map(Some(_))
    val printableYValues = Seq(None) ++ yValues.map(Some(_))

    val table = for {
      prY <- printableYValues
      prX <- printableXValues
    } yield (prX, prY)

    table.foreach{ case (prX, prY) =>
      prX match {
        case None =>
          prY match {
            case None =>
              print("\t")
            case Some(y) =>
              print("\n")
              print(y.entry.data.head._2)
              print("\t")
          }
        case Some(x) =>
          prY match {
            case None =>
              print(x.entry.data.head._2)
              print("\t")
            case Some(y) =>
              val res = result.find { case (dims, entry) =>
                dims.exists(_.entry.data.last._2 == y.entry.data.last._2) && dims.exists(_.entry.data.last._2 == x.entry.data.last._2)
              }
              print(res.fold(""){case (_, entry) => entry.entry.data.head._2})
              print("\t")
          }
      }
    }
    println()
  }

  private def createEntries(typeName: String, values: List[String]) = {
    values.map { value =>
      new DimensionEntry(DimensionType(typeName), new Entry(Map("name" -> value)))
    }.toSeq
  }

  private def createEntry(typeName: String, value: String) = {
    new DimensionEntry(DimensionType(typeName), new Entry(Map("name" -> value)))
  }

  def main(args: Array[String]) {

//    System.setProperty("sqlite4java.debug", "true")

//    com.almworks.sqlite4java.SQLite.main(Seq("-d").toArray)

    var yValues = createEntries("shops", List("Shop1", "Shop2"))

    var xValues = createEntries("apples", List("Apple1"))

    var fixValue = createEntry("countries", "Country1")

//    val filters = Seq(
//      DimensionType("shops") -> DiscreteFilter[DimensionType, DimensionEntry](yValues),
//      DimensionType("apples") -> DiscreteFilter[DimensionType, DimensionEntry](xValues),
//      DimensionType("countries") -> DiscreteFilter.singleValue[DimensionType, DimensionEntry](fixValue)
//    )

//    val result = CubeService.getData(filters)//.map { case (entry, value) => entry.map(e => e.entry.data.head._1 + "=" + e.entry.data.head._2) -> value.entry.data.head._2 }.mkString("\n")

//    printTable(xValues, yValues, result)
//
//    println(result)

    for (line <- io.Source.stdin.getLines()) {
      line.split(" ").toList match {
        case command :: subcommand :: params =>

          command match {
            case "set" =>

              subcommand match {
                case "yValues" =>
                  yValues = createEntries(params.head, params.tail)
                case "xValues" =>
                  xValues = createEntries(params.head, params.tail)
                case "fixValue" =>
                  fixValue = createEntry(params.head, params.tail.head)
              }

            case _ =>
          }

        case command :: _ =>

          command match {
            case "print" =>
              val filters = Seq(
                yValues.head.tableType -> DiscreteFilter[DimensionType, DimensionEntry](yValues),
                xValues.head.tableType -> DiscreteFilter[DimensionType, DimensionEntry](xValues),
                fixValue.tableType -> DiscreteFilter.singleValue[DimensionType, DimensionEntry](fixValue)
              )
              val result = CubeService.getData(filters)
              printTable(xValues, yValues, result)
          }

        case _ =>
      }
    }
  }

}
