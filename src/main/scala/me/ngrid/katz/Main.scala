package me.ngrid.katz

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import cats.{Applicative, Monad}
import cats.effect._
import doobie.util.transactor.Transactor
import cats.implicits._
import cats.data.Kleisli
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
    } <* dataRepository.delete <* (for {
      _ <- dataRepository.create
    } yield ())
  }
}

trait RepositorySettings {
  self: DatabaseSettings =>


  lazy val dataRepository: SqlRepository[IO, InboundFile] = new SqlRepository[IO, InboundFile](self.sqlite)

  def fileRepository[F[_]: Monad]: Kleisli[F, Transactor[F], SqlRepository[F, InboundFile]] = Kleisli {
    t => new SqlRepository(t).pure
  }
}

trait DatabaseSettings {
  //  implicit val
  implicit val cs = cats.effect.IO.contextShift(ExecutionContext.global)

  def sqlite[F[_]: Monad: Async: ContextShift]: Kleisli[F, Unit, Transactor[F]] = Kleisli.pure {
    Transactor.fromDriverManager[F](
      "org.sqlite.JDBC", "jdbc:sqlite:sample.db", "", ""
    )
  }
}


