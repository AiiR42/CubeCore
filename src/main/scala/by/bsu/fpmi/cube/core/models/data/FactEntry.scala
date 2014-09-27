package by.bsu.fpmi.cube.core.models.data

import by.bsu.fpmi.cube.core.models.types.FactType

case class FactEntry(override val tableType: FactType, override val entry: Entry) extends TableEntry[FactType](tableType, entry)

object FactEntry {
  implicit def fromTableEntry(tableEntry: TableEntry[FactType]) = FactEntry(tableEntry.tableType, tableEntry.entry)
}
