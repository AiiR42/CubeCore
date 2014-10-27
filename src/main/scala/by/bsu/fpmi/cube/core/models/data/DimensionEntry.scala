package by.bsu.fpmi.cube.core.models.data

import by.bsu.fpmi.cube.core.models.types.DimensionType

case class DimensionEntry(override val tableType: DimensionType, override val entry: Entry) extends TableEntry[DimensionType](tableType, entry)

object DimensionEntry {
  implicit def fromTableEntry(tableEntry: TableEntry[DimensionType]): DimensionEntry = DimensionEntry(tableEntry.tableType, tableEntry.entry)
}
