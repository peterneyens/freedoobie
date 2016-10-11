package app

import cats.~>
import cats.data.Coproduct
import cats.free.{ Free, Inject }
import cats.instances.int._
import cats.instances.option._
import cats.syntax.applicative._
import cats.syntax.semigroup._
import doobie.imports._
import fs2.interop.cats._ // Monad[IOLite]
import shapeless.{ ::, HNil }

import freedoobie._

object AppUsingFree {

  // adts

  sealed trait DataOp[A]
  case class Put(key: String, value: Int) extends DataOp[Unit]
  case class Get(key: String) extends DataOp[Option[Int]]
  case class Delete(key: String) extends DataOp[Unit]

  sealed trait LogOp[A]
  case class Error(msg: String) extends LogOp[Unit]
  case class Warning(msg: String) extends LogOp[Unit]


  // ops

  class DataOps[F[_]](implicit I: Inject[DataOp, F]) {
    def put(key: String, value: Int): Free[F, Unit] =
      Free.inject[DataOp, F](Put(key, value))
    def get(key: String): Free[F, Option[Int]] =
      Free.inject[DataOp, F](Get(key))
    def delete(key: String): Free[F, Unit] =
      Free.inject[DataOp, F](Delete(key))
  }

  object DataOps {
    implicit def DataOps[F[_]](implicit I: Inject[DataOp, F]): DataOps[F] = new DataOps[F]
  }

  class LogOps[F[_]](implicit I: Inject[LogOp, F]) {
    def error(msg: String): Free[F, Unit] =
      Free.inject[LogOp, F](Error(msg))
    def warn(msg: String): Free[F, Unit] =
      Free.inject[LogOp, F](Warning(msg))
  }

  object LogOps {
    implicit def LogOps[F[_]](implicit I: Inject[LogOp, F]): LogOps[F] = new LogOps[F]
  }


  // combine algebras

  type App[A] = Coproduct[DataOp, LogOp, A]


  // interpreters

  val log2iolite = λ[(LogOp ~> IOLite)] {
    case Error(msg)   => IOLite.primitive(println(msg))
    case Warning(msg) => IOLite.primitive(println(msg))
  }

  val data2connectionio = λ[(DataOp ~> ConnectionIO)] {
    case Get(key)        => sql"select random() * 100".query[Int].option
    case Put(key, value) => ().pure[ConnectionIO]
    case Delete(key)     => ().pure[ConnectionIO]
  }


  // lift interpreters to return a ConnIOK[IOLite, ?]

  val interpreter: App ~> ConIOK[IOLite, ?] =
    Interpreters.combine[IOLite](data2connectionio :: log2iolite :: HNil)


  // a program

  def prgrm1(implicit Log: LogOps[App], Data: DataOps[App]): Free[App, Option[Int]] =
    for {
      _ <- Data.put("key", 10)
      a <- Data.get("key")
      _ <- Log.warn(s"key a = $a")
      b <- Data.get("key2")
      _ <- Log.warn(s"key b = $b")
      _ <- Data.delete("key")
    } yield a |+| b


  implicit val RecursiveTailRecMConIOK = cats.RecursiveTailRecM.create[ConIOK[IOLite, ?]]  // ??

  val interpreted = prgrm1.foldMap(interpreter)


  // execute the ConnIOK[IOLite, Option[Int]]

  val ds = new org.postgresql.ds.PGSimpleDataSource()
  ds.setDatabaseName("world")
  ds.setUser("postgres")
  ds.setPassword("")

  val xa = MyDataSourceTransactor[IOLite](ds)

  val appResult = ConIOK.execute(interpreted, xa)
 
}