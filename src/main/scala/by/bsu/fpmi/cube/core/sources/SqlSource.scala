package by.bsu.fpmi.cube.core.sources

import by.bsu.fpmi.cube.core.models.data.TableEntry
import by.bsu.fpmi.cube.core.models.filters.DiscreteFilter
import by.bsu.fpmi.cube.core.models.types.TableType

class SqlSource[T <: TableType] {

  def read(tableType: T, filters: Seq[DiscreteFilter]): Seq[TableEntry[T]] = ???

  private def remoteRead(sql: String): Seq[TableEntry[T]] = ???

//  => { sql: "select * from apples where id in (1234, 2345);", version: 1 }
//  <= [ { id: 12345, [ { name: "asd" } ] }, { id: 23456, [ { name: "qwe" } ] } ]

}
