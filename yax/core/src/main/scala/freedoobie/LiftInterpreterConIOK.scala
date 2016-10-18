package doobie.freedoobie

import doobie.free.connection.ConnectionIO

#+cats
import cats.~>
import cats.data.Kleisli
#-cats
#+fs2
import fs2.util.{ Catchable, Suspendable }
#-fs2
#+scalaz
import scalaz.{ ~>, Catchable, Kleisli, Monad }
import doobie.util.capture._
#-scalaz

import java.sql.Connection

/**
 * Lift an interpreter G ~> F into G ~> ConIOK[R, ?]
 */
trait LiftInterpreterConIOK[F[_], R[_]] {
  def apply[G[_]](fk: G ~> F): G ~> ConIOK[R, ?] 
}

object LiftInterpreterConIOK extends LiftInterpreterConIOK0 {
  def apply[F[_], R[_]](implicit ev: LiftInterpreterConIOK[F, R]): LiftInterpreterConIOK[F, R] = ev

  def create[K[_], L[_], F[_]](fk: K ~> L)(implicit liftInterp: LiftInterpreterConIOK[L, F]): K ~> ConIOK[F, ?] = liftInterp(fk)

  def create2[F[_]]: PartiallyAppliedCreate[F] = new PartiallyAppliedCreate[F]
  class PartiallyAppliedCreate[F[_]] {
    def apply[K[_], L[_]](fk: K ~> L)(implicit liftInterp: LiftInterpreterConIOK[L, F]): K ~> ConIOK[F, ?] = liftInterp(fk)
  }

}

trait LiftInterpreterConIOK0 extends LiftInterpreterConIOK1 {
  // from G ~> ConnectionIO to G ~> ConIOK[F, ?]
#+fs2
  implicit def connectionIOtoLiftInterpreterConIOK[F[_]: Catchable: Suspendable]: LiftInterpreterConIOK[ConnectionIO, F] =
#-fs2
#+scalaz
  implicit def connectionIOtoLiftInterpreterConIOK[F[_]: Monad: Catchable: Capture]: LiftInterpreterConIOK[ConnectionIO, F] =
#-scalaz
    new LiftInterpreterConIOK[ConnectionIO, F] {
      def apply[G[_]](fk: G ~> ConnectionIO): G ~> ConIOK[F, ?] =
        fk andThen[ConIOK[F, ?]] λ[(ConnectionIO ~> ConIOK[F, ?])](_.transK[F])
    }
}

trait LiftInterpreterConIOK1 {
  // from G ~> F to G ~> ConIOK[F, ?]
#+fs2  
  implicit def fToLiftInterpreterConIOK[F[_]: Catchable: Suspendable](implicit 
#-fs2
#+scalaz
  implicit def fToLiftInterpreterConIOK[F[_]: Monad: Catchable: Capture](implicit 
#-scalaz
    neq: shapeless.=:!=[F[A], ConnectionIO[A]] forSome { type A }
  ): LiftInterpreterConIOK[F, F] =
    new LiftInterpreterConIOK[F, F] {
      def apply[G[_]](fk: G ~> F): G ~> ConIOK[F, ?] =
        fk andThen[ConIOK[F, ?]] λ[(F ~> ConIOK[F, ?])](ConIOK.lift(_))
    }
}