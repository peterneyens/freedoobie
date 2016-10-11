package freedoobie

import java.sql.Connection
import javax.sql.DataSource

import cats.data.Kleisli
import cats.syntax.cartesian._
import cats.syntax.flatMap._
import doobie.free.connection.{ setAutoCommit, commit, rollback, close }
import doobie.util.transactor.Transactor
import doobie.syntax.catchable.ToDoobieCatchableOps._
import fs2.util.{ Catchable, Suspendable }
import fs2.interop.cats._  // Catchable for Kleisli

object ConIOK {
  def lift[F[_], A](fa: F[A]): ConIOK[F, A] = Kleisli.lift(fa)

  /**
   * Execute a ConIOK using a Transactor to get a connection
   * Based on Transactor.trans
   */
  // def execute[F[_]: Catchable: Suspendable, A](ciok: ConIOK[F, A], xa: Transactor[F]): F[A] = {
  def execute[F[_]: Catchable: Suspendable, A, D <: DataSource](ciok: ConIOK[F, A], xa: MyDataSourceTransactor[F, D]): F[A] = {
    def safe(ma: ConIOK[F, A]): ConIOK[F, A] = {
      // we should also reuse these from the Transactor
      val before = setAutoCommit(false).transK[F]
      val after  = commit.transK[F]
      val oops   = rollback.transK[F]
      val always = close.transK[F]

      (before *> ma <* after) onException oops ensuring always
    }

    // connectRefl(xa) flatMap safe(ciok).run
    xa.connectHack flatMap safe(ciok).run
  }

  // ooops - reflection ??
  // Transactor.connect is protected
  // def connectRefl[F[_]](xa: Transactor[F]): F[Connection] =
  //   xa.getClass.getDeclaredMethod("connect").invoke(xa).asInstanceOf[F[Connection]]

}