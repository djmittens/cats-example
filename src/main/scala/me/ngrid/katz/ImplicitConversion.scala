package me.ngrid.katz

import cats.Functor
import cats.implicits._

trait ImplicitConversion[-In, +Out] {
  def apply(x: In): Out
}

trait ImplicitConversionSyntax {
  type >:>[In, Out] = ImplicitConversion[In, Out]

  implicit class Convertable[T](v: T) {
    def convert[U](implicit conversion: T >:> U): U = conversion(v)
  }

  implicit def funConverter[F[_] : Functor, T, U](implicit con: T >:> U): F[T] >:> F[U] = v => v.map(con.apply)

}
