package me.ngrid.katz

import java.nio.ByteBuffer
import java.time.LocalDate

sealed trait InboundFile { }

object InboundFile {
  case class A(details: FileDetails) extends InboundFile
  case class B(details: FileDetails) extends InboundFile
  case class C(details: FileDetails) extends InboundFile

  case class FileDetails(fileName: String, content: ByteBuffer, date: LocalDate)
}
