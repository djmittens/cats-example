package me.ngrid.katz

import cats.Monad
import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import cats.implicits._
import doobie.implicits._
import doobie._

import scala.concurrent.ExecutionContext

object Main extends IOApp
  with RepositorySettings
  with DatabaseSettings {

  override def run(args: List[String]): IO[ExitCode] = {
    IO {
      println("Hello World !!!!!")
      ExitCode.Success
    } <* dataRepository.drop <* (for {
      _ <- dataRepository.create
    } yield ())
  }
}

trait RepositorySettings {
  self: DatabaseSettings =>
  lazy val dataRepository: SqlRepository[IO] = new SqlRepository[IO](self.sqlite)
}

trait DatabaseSettings {
  //  implicit val
  implicit val cs = cats.effect.IO.contextShift(ExecutionContext.global)

  lazy val sqlite = Transactor.fromDriverManager[IO](
    "org.sqlite.JDBC", "jdbc:sqlite:sample.db", "", ""
  )
}


