package by.bsu.fpmi.cube.core.models.filters

import by.bsu.fpmi.cube.core.models.data.TableEntry
import by.bsu.fpmi.cube.core.models.types.TableType

case class DiscreteFilter[T <: TableType, A <: TableEntry[T]](fixedValues: Seq[A])

object DiscreteFilter {

  def singleValue[T <: TableType, A <: TableEntry[T]](value: A) = DiscreteFilter[T, A](Seq(value))

}
