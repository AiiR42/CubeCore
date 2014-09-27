package by.bsu.fpmi.cube.core.models.filters

import by.bsu.fpmi.cube.core.models.data.TableEntry

case class DiscreteFilter[A <: TableEntry](fixedValues: Seq[A])

object DiscreteFilter {

  def singleValue[A <: TableEntry](value: A) = DiscreteFilter(Seq(value))

}
