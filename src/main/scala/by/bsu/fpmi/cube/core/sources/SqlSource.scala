package by.bsu.fpmi.cube.core.sources

import java.io.File

import by.bsu.fpmi.cube.core.models.data.{Entry, FactEntry, DimensionEntry}
import by.bsu.fpmi.cube.core.models.filters.DiscreteFilter
import by.bsu.fpmi.cube.core.models.types.{FactType, DimensionType}
import com.almworks.sqlite4java.SQLiteConnection

object SqlSource {

  lazy val configuration = new Configuration("/Users/andrew/University/Projects/CubeBase/meta.xml")

  def readAll(tableType: DimensionType): Seq[DimensionEntry] = {
    val tableName = tableType.name
    val selectFields = configuration.dimensions.filter(_ == tableType.name).map { tableName =>
      configuration.dimensionDataFields(tableName).map { dimensionDataField =>
        s"$tableName.$dimensionDataField"
      }
    }.flatten
    val statement = s"select ${selectFields.mkString(", ")} from $tableName"

    var result = scala.collection.mutable.Buffer[DimensionEntry]()

    val db = new SQLiteConnection(new File("/Users/andrew/University/Projects/CubeBase/apples"))
    db.open(true)
    val st = db.prepare(statement)
    while (st.step()) {
      val allValues = selectFields.zipWithIndex.map { case (field, index) =>
        val value = st.columnValue(index).toString
        field.split("\\.").last -> value
      }

      result += DimensionEntry(tableType, Entry(Map(allValues: _*)))
    }

    result.toSeq
  }

  def readFacts(filters: Seq[(DimensionType, DiscreteFilter[DimensionType, DimensionEntry])]): Map[Seq[DimensionEntry], FactEntry] = {
    // select * from *facts_table_name* left join *dim1* on *dim1*.*dim1key* = *facts_table_name*.*fk* ... where

    val factsTableName = configuration.factsTableName
    val factsDataFields = configuration.factsDataFields.map(s => s"$factsTableName.$s")

    val selectFields = configuration.dimensions.map { tableName =>
      configuration.dimensionDataFields(tableName).map { dimensionDataField =>
        s"$tableName.$dimensionDataField"
      }
    }.flatten ++ factsDataFields

    val selectFieldsString = selectFields.mkString(", ")

    val joins = configuration.dimensions.map { tableName =>
      val dimensionKey = configuration.dimensionKey(tableName)
      val factsTableFk = configuration.factsFkField(tableName)
      s"left join $tableName on $tableName.$dimensionKey = $factsTableName.$factsTableFk"
    }.mkString(" ")

    val whereConditions = filters.map { case (dimensionType, filter) =>
      val tableName = dimensionType.name
      val dimensionKey = filter.fixedValues.head.entry.data.head._1  //configuration.dimensionKey(tableName)
      val setString = filter.fixedValues.map { dEntry => "\"" + dEntry.entry.data.head._2 + "\"" }.mkString(", ")
      if (setString.isEmpty) {
        ""
      } else {
        s"$tableName.$dimensionKey in ($setString)"
      }
    }.mkString(" and ")

    val whereStatement = if (whereConditions.isEmpty) {
      ""
    } else {
      s"where $whereConditions"
    }

    val statement = s"select $selectFieldsString from $factsTableName $joins $whereStatement;"

    val factType = FactType(factsTableName)

    var result = scala.collection.mutable.Map[Seq[DimensionEntry], FactEntry]()

    val db = new SQLiteConnection(new File("/Users/andrew/University/Projects/CubeBase/apples"))
    db.open(true)
    val st = db.prepare(statement)
    while (st.step()) {
      val allValues = selectFields.zipWithIndex.map { case (field, index) =>
        val value = st.columnValue(index).toString
        field.split("\\.") -> value
      }.groupBy(_._1.head)

      val allValuesByTables = allValues.map { case (tableName, seq) =>
        val values = seq.map { case (arr, value) =>
          arr.tail.head -> value
        }
        val valuesMap = Map(values: _*)
        tableName -> valuesMap
      }

//      println(allValuesByTables)

      val dimensionEntries = allValuesByTables.filter { case (tableName, _) =>
        tableName != factsTableName
      }.map { case (tableName, values) =>
        DimensionEntry(DimensionType(tableName), Entry(values))
      }

      val factEntry = FactEntry(FactType(factsTableName), Entry(allValuesByTables.get(factsTableName).get))

      result += (dimensionEntries.toSeq -> factEntry)
    }
    db.dispose()

    result.toMap
  }

}
