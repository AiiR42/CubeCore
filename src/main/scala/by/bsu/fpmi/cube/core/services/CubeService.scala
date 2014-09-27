package by.bsu.fpmi.cube.core.services

import by.bsu.fpmi.cube.core.models.data.{FactEntry, DimensionEntry}
import by.bsu.fpmi.cube.core.models.filters.DiscreteFilter
import by.bsu.fpmi.cube.core.models.types.{FactType, DimensionType}

object CubeService {

  def getData(filters: Seq[DiscreteFilter]): Map[Seq[DimensionEntry], FactEntry] = ???

  def getDimensionValues(tableType: DimensionType): Seq[DimensionEntry] = ???

  def getDimensionTypes: Seq[DimensionType] = ???
  def getFactType: FactType = ???

}
