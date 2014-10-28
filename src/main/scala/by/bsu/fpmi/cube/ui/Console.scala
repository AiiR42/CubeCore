package by.bsu.fpmi.cube.ui

import java.io.PrintWriter

import by.bsu.fpmi.cube.core.models.data.{TableEntry, Entry, FactEntry, DimensionEntry}
import by.bsu.fpmi.cube.core.models.filters.DiscreteFilter
import by.bsu.fpmi.cube.core.models.types.DimensionType
import by.bsu.fpmi.cube.core.services.CubeService

import scala.xml.XML

object Console {

  private def dimensionValue(e: TableEntry[_]) = e.entry.data.last._2
  private def factValue(e: TableEntry[_]) = e.entry.data.head._2

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
              print("\t\t")
            case Some(y) =>
              print("\n")
              val label = dimensionValue(y)
              print(label)
              if (label.length < 8) print("\t")
              print("\t")
          }
        case Some(x) =>
          prY match {
            case None =>
              val label = dimensionValue(x)
              print(label)
              if (label.length < 8) print("\t")
              print("\t")
            case Some(y) =>
              val res = result.find { case (dims, entry) =>
                dims.exists(dimensionValue(_) == dimensionValue(y)) && dims.exists(dimensionValue(_) == dimensionValue(x))
              }
              print(res.fold(""){case (_, entry) => factValue(entry) })
              print("\t\t")
          }
      }
    }
    println()
  }

  private def printFilter(xValues: Seq[DimensionEntry], yValues: Seq[DimensionEntry], fixValue: DimensionEntry): Unit = {
    println(s"""     X axis type: ${yValues.head.tableType.name}""")
    println(s"""        X values: ${yValues.map(dimensionValue).mkString(", ")}""")
    println(s"""     Y axis type: ${xValues.head.tableType.name}""")
    println(s"""        Y values: ${xValues.map(dimensionValue).mkString(", ")}""")
    println(s"""Fixed value type: ${fixValue.tableType.name}""")
    println(s"""     Fixed value: ${dimensionValue(fixValue)}""")
  }

  private def createEntries(typeName: String, values: List[String]) = {
    values.map { value =>
      new DimensionEntry(DimensionType(typeName), new Entry(Map("name" -> value)))
    }.toSeq
  }

  private def createEntry(typeName: String, value: String) = {
    new DimensionEntry(DimensionType(typeName), new Entry(Map("name" -> value)))
  }

  private def save(filename: String, xValues: Seq[DimensionEntry], yValues: Seq[DimensionEntry], fixValue: DimensionEntry) = {
    val xml = <save>
      <filters>
        <xfilter type={xValues.head.tableType.name}>
          <values>
            {xValues.map(x => <value>{dimensionValue(x)}</value>)}
          </values>
        </xfilter>
        <yfilter type={yValues.head.tableType.name}>
          <values>
            {yValues.map(x => <value>{dimensionValue(x)}</value>)}
          </values>
        </yfilter>
        <fixfilter type={fixValue.tableType.name}>
          <value>{dimensionValue(fixValue)}</value>
        </fixfilter>
      </filters>
    </save>

    val out = new PrintWriter(filename, "UTF-8")
    out.write(xml.toString())
    out.close()
  }

  private def load(filename: String): (scala.collection.immutable.Seq[DimensionEntry], scala.collection.immutable.Seq[DimensionEntry], DimensionEntry) = {
    val content = XML.loadFile(filename)

    val yType = (content \ "filters" \ "yfilter" \ "@type").text.trim
    val xType = (content \ "filters" \ "xfilter" \ "@type").text.trim
    val fixType = (content \ "filters" \ "fixfilter" \ "@type").text.trim

    val yFixValues = (content \ "filters" \ "yfilter" \ "values" \ "value").map(_.text.trim).toList
    val xFixValues = (content \ "filters" \ "xfilter" \ "values" \ "value").map(_.text.trim).toList
    val fixedValue = (content \ "filters" \ "fixfilter" \ "value").text.trim

    val yValues = createEntries(yType, yFixValues)
    val xValues = createEntries(xType, xFixValues)
    val fixValue = createEntry(fixType, fixedValue)

    (xValues, yValues, fixValue)
  }

  private def printError(message: String) = {
    println("Error: " + message)
  }

  private def printCommandError(command: String) = {
    printError(s"""Command "$command" is incorrect.""")
  }


  def main(args: Array[String]) {
//    System.setProperty("sqlite4java.debug", "true")

//    com.almworks.sqlite4java.SQLite.main(Seq("-d").toArray)
    java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.OFF)

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

    var prevLine = ""
    print(">> ")

    for (line <- io.Source.stdin.getLines()) {
      prevLine = line
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
                case _ =>
                  printCommandError(line)
              }

            case "save" =>
              save(subcommand, xValues, yValues, fixValue)
            case "load" =>
              val savedInfo = load(subcommand)
              xValues = savedInfo._1
              yValues = savedInfo._2
              fixValue = savedInfo._3
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
            case "showFilter" =>
              printFilter(xValues, yValues, fixValue)
            case "" =>
              // no error message
            case _ =>
              printCommandError(line)
          }

        case _ =>
          printCommandError(line)
      }

      print(">> ")
    }
  }

}
