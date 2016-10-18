package doobie.freedoobie

import doobie.util.transactor.Transactor
import doobie.syntax.catchable.ToDoobieCatchableOps._

#+cats
import cats.data.Kleisli
import cats.syntax.cartesian._
import cats.syntax.flatMap._
#-cats
#+fs2
import fs2.util.{ Catchable, Suspendable }
import fs2.interop.cats._  // Catchable for Kleisli
#-fs2
#+scalaz
import scalaz.{ Catchable, Kleisli, Monad }
import scalaz.syntax.monad._
import doobie.util.capture._
#-scalaz

import java.sql.Connection


object ConIOK {
  def lift[M[_], A](ma: M[A]): ConIOK[M, A] = 
#+cats  
    Kleisli.lift(ma)
#-cats
#+scalaz
    Kleisli[M, Connection, A](_ => ma)
#-scalaz

  /**
   * Execute a ConIOK using a Transactor to get a connection
   * Based on Transactor.trans
   */
#+fs2
  def execute[M[_]: Catchable: Suspendable, A](ciok: ConIOK[M, A], xa: Transactor[M]): M[A] = {
#-fs2
#+scalaz
  def execute[M[_]: Monad: Catchable: Capture, A](ciok: ConIOK[M, A], xa: Transactor[M]): M[A] = {
#-scalaz
    def safe(ma: ConIOK[M, A]): ConIOK[M, A] = {
      val before = xa.before.transK[M]
      val after  = xa.after .transK[M]
      val oops   = xa.oops  .transK[M]
      val always = xa.always.transK[M]

      (before *> ma <* after) onException oops ensuring always
    }

    xa.connect flatMap safe(ciok).run
  }

}