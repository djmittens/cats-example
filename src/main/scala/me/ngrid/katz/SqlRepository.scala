package me.ngrid.katz

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.LocalDate

import cats.Monad
import doobie.util.transactor.Transactor
import doobie._
import cats.implicits._
import doobie.implicits._
import me.ngrid.katz.SqlRepository.FileEntity

trait FileRepository[F[_], File] {
  def create: F[Option[FileRepository.Error]]

  def delete: F[Option[FileRepository.Error]]

  def fileArrived(date: LocalDate, file: File): F[Option[FileRepository.Error]]

  def findFilesThatArrivedOn(date: LocalDate): F[Either[FileRepository.Error, List[File]]]
}

object FileRepository {

  sealed trait Error

  case class UnexpectedException(e: Throwable) extends Error

}

class SqlRepository[F[_] : Monad, File](db: Transactor[F])(implicit to: File >:> FileEntity, from: FileEntity >:> File)
  extends FileRepository[F, File] {

  val delete: F[Option[FileRepository.Error]] =
    sql"""DROP TABLE IF EXISTS files""".
      update.run.attempt.transact(db).map {
      case Left(e) => Some(FileRepository.UnexpectedException(e))
      case _ => None
    }

  val create: F[Option[FileRepository.Error]] =
    sql"""CREATE TABLE files (
         |filetype INT NOT NULL,
         |date DATE NOT NULL,
         |filename VARCHAR (255) NOT NULL,
         |content TEXT NOT NULL,
         |)""".stripMargin.
      update.
      run.
      attempt.
      transact(db).
      map {
        case Left(e) => FileRepository.UnexpectedException(e).some
        case _ => None
      }

  override def fileArrived(date: LocalDate, file: File): F[Option[FileRepository.Error]] = {
    val entity: FileEntity = file.convert

    sql"""INSERT INTO files (
         |date, filetype, filename, content
         |)
         |VALUES (
         |$date, ${entity.fileType}, ${entity.fileName}, ${entity.content}
         |)""".stripMargin.
      update.
      run.
      attempt.
      transact(db).
      map {
        case Left(e) => FileRepository.UnexpectedException(e).some
        case Right(_) => None
      }
  }

  override def findFilesThatArrivedOn(date: LocalDate): F[Either[FileRepository.Error, List[File]]] = {
    sql"SELECT filename, fileType, content, date FROM `files`".
      query[FileEntity].
      to[List].
      attempt.
      transact(db).
      map {
        case Left(e) => FileRepository.UnexpectedException(e).asLeft
        case Right(l) => l.convert.asRight
      }
  }
}

object SqlRepository {

  case class FileEntity(fileName: String, fileType: Int, content: String, date: LocalDate)

  implicit val fromDetails: InboundFile.FileDetails >:> SqlRepository.FileEntity = { d =>

    d.content.mark()
    try {
      val c = StandardCharsets.UTF_8.decode(d.content).toString
      SqlRepository.FileEntity(d.fileName, 0, c, d.date)
    } finally {
      d.content.rewind()
    }
  }

  implicit val toDetails: SqlRepository.FileEntity >:> InboundFile = {
    case SqlRepository.FileEntity(fileName, 1, content, date) =>
      InboundFile.A(InboundFile.FileDetails(fileName, ByteBuffer.wrap(content.getBytes()), date))
    case SqlRepository.FileEntity(fileName, 2, content, date) =>
      InboundFile.B(InboundFile.FileDetails(fileName, ByteBuffer.wrap(content.getBytes()), date))
    case SqlRepository.FileEntity(fileName, 3, content, date) =>
      InboundFile.C(InboundFile.FileDetails(fileName, ByteBuffer.wrap(content.getBytes()), date))
  }

  implicit val toEntity: InboundFile >:> SqlRepository.FileEntity = {
    case InboundFile.A(d) => d.convert.copy(fileType = 1)
    case InboundFile.B(d) => d.convert.copy(fileType = 2)
    case InboundFile.C(d) => d.convert.copy(fileType = 3)
  }

}

