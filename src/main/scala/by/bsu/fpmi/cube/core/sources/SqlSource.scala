package by.bsu.fpmi.cube.core.sources

import java.io.File

import by.bsu.fpmi.cube.core.models.data.{Entry, FactEntry, DimensionEntry, TableEntry}
import by.bsu.fpmi.cube.core.models.filters.DiscreteFilter
import by.bsu.fpmi.cube.core.models.types.{FactType, DimensionType, TableType}
import com.almworks.sqlite4java.SQLiteConnection

object SqlSource {

  lazy val configuration = new Configuration("meta.xml")

//  def readAll[T <: TableType](tableType: T): Seq[TableEntry[T]] = {
//    val tableName = tableType.name
//
//
//
//  }

  def readFacts(filters: Seq[(DimensionType, DiscreteFilter)]): Map[Seq[DimensionEntry], FactEntry] = {
    // select * from *facts_table_name* left join *dim1* on *dim1*.*dim1key* = *facts_table_name*.*fk* ... where
    //

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
      val dimensionKey = configuration.dimensionKey(tableName)
      val setString = filter.fixedValues.map { _.toString }.mkString(", ")
      if (setString.isEmpty) {
        ""
      } else {
        s"$tableName.$dimensionKey in ($setString)"
      }
    }.mkString(" or ")

    val whereStatement = if (whereConditions.isEmpty) {
      ""
    } else {
      s"where $whereConditions"
    }

    val statement = s"select $selectFieldsString $joins $whereStatement;"

    val factType = FactType(factsTableName)

    var result = scala.collection.mutable.Map[Seq[DimensionEntry], FactEntry]()

    val db = new SQLiteConnection(new File("/tmp/database"))
    db.open(true)
    val st = db.prepare(statement)
    while (st.step()) {
      val allValues = selectFields.zipWithIndex.map { case (field, index) =>
        val value = st.columnValue(index).toString
        field.split(".") -> value
      }.groupBy(_._1.head)

      val allValuesByTables = allValues.map { case (tableName, seq) =>
        val values = seq.map { case (arr, value) =>
          arr.tail.head -> value
        }
        val valuesMap = Map(values: _*)
        tableName -> valuesMap
      }

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
