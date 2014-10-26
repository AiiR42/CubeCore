package by.bsu.fpmi.cube.core.models.data

import by.bsu.fpmi.cube.core.models.types.TableType

case class Entry(data: Map[String, String])

class TableEntry[T <: TableType](val tableType: T, val entry: Entry)
