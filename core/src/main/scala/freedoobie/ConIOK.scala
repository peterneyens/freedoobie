package doobie.freedoobie

import java.sql.Connection

import cats.data.Kleisli
import cats.syntax.cartesian._
import cats.syntax.flatMap._
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
  def execute[F[_]: Catchable: Suspendable, A](ciok: ConIOK[F, A], xa: Transactor[F]): F[A] = {
    def safe(ma: ConIOK[F, A]): ConIOK[F, A] = {
      val before = xa.before.transK[F]
      val after  = xa.after .transK[F]
      val oops   = xa.oops  .transK[F]
      val always = xa.always.transK[F]

      (before *> ma <* after) onException oops ensuring always
    }

    xa.connect flatMap safe(ciok).run
  }

}