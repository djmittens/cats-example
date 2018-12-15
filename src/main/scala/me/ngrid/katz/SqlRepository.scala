package me.ngrid.katz

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
         |date DATE NOT NULL,
         |filename VARCHAR (255) NOT NULL,
         |content TEXT NOT NULL,
         |)"""
      .stripMargin.update.run.attempt.transact(db).map {
      case Left(e) => Some(FileRepository.UnexpectedException(e))
      case _ => None
    }

  override def fileArrived(date: LocalDate, file: File): F[Option[FileRepository.Error]] = {
    ???
  }

  override def findFilesThatArrivedOn(date: LocalDate): F[Either[FileRepository.Error, List[File]]] = {
    sql"select filename, content, date from `files`".
      query[FileEntity].
      to[List].
      attempt.
      transact(db).map {
      case Left(e) => Some(FileRepository.UnexpectedException(e))
//      case Right(l) => l.convert
    }
//      ???
  }
}

object SqlRepository {
  case class FileEntity(filename: String, content: String, date: LocalDate)
}

