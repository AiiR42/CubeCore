package by.bsu.fpmi.cube.core.services

import by.bsu.fpmi.cube.core.models.data.{FactEntry, DimensionEntry}
import by.bsu.fpmi.cube.core.models.filters.DiscreteFilter
import by.bsu.fpmi.cube.core.models.types.{TableType, FactType, DimensionType}
import by.bsu.fpmi.cube.core.sources.SqlSource

import scala.util.Try

object CubeService {

  def getData(filters: Seq[(DimensionType, DiscreteFilter[DimensionType, DimensionEntry])]): Map[Seq[DimensionEntry], FactEntry] = {
    Try {
      SqlSource.readFacts(filters)
    }.getOrElse(Map[Seq[DimensionEntry], FactEntry]())
  }

  def getDimensionValues(tableType: DimensionType): Seq[DimensionEntry] = {
    SqlSource.readAll(tableType)
  }

  def getDimensionTypes: Seq[DimensionType] = {
    SqlSource.configuration.dimensions.map(DimensionType)
  }

  def getFactType: FactType = {
    FactType(SqlSource.configuration.factsTableName)
  }

}
