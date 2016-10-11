package freedoobie

import java.sql.Connection

import cats.arrow.FunctionK
import cats.data.Kleisli
import doobie.free.connection.ConnectionIO
import fs2.util.{ Catchable, Suspendable }

/**
 * Lift an interpreter G ~> F into G ~> ConIOK[R, ?]
 */
trait LiftInterpreterConIOK[F[_], R[_]] {
  def apply[G[_]](fk: FunctionK[G, F]): FunctionK[G, ConIOK[R, ?]] 
}

object LiftInterpreterConIOK extends LiftInterpreterConIOK0 {
  def apply[F[_], R[_]](implicit ev: LiftInterpreterConIOK[F, R]): LiftInterpreterConIOK[F, R] = ev

  def create[K[_], L[_], F[_]](fk: FunctionK[K, L])(implicit liftInterp: LiftInterpreterConIOK[L, F]): FunctionK[K, ConIOK[F, ?]] = liftInterp(fk)

  def create2[F[_]]: PartiallyAppliedCreate[F] = new PartiallyAppliedCreate[F]
  class PartiallyAppliedCreate[F[_]] {
    def apply[K[_], L[_]](fk: FunctionK[K, L])(implicit liftInterp: LiftInterpreterConIOK[L, F]): FunctionK[K, ConIOK[F, ?]] = liftInterp(fk)
  }

}

trait LiftInterpreterConIOK0 extends LiftInterpreterConIOK1 {
  // from G ~> ConnectionIO to G ~> ConIOK[F, ?]
  implicit def connectionIOtoLiftInterpreterConIOK[F[_]: Catchable: Suspendable]: LiftInterpreterConIOK[ConnectionIO, F] =
    new LiftInterpreterConIOK[ConnectionIO, F] {
      def apply[G[_]](fk: FunctionK[G, ConnectionIO]): FunctionK[G, ConIOK[F, ?]] =
        fk andThen[ConIOK[F, ?]] λ[FunctionK[ConnectionIO, ConIOK[F, ?]]](_.transK[F])
    }
}

trait LiftInterpreterConIOK1 {
  // from G ~> F to G ~> ConIOK[F, ?]
  // implicit def fToConIOKInterpreter[F[_]: Catchable: Suspendable]: LiftInterpreterConIOK[F, F] =
  implicit def fToLiftInterpreterConIOK[F[_]: Catchable: Suspendable](implicit 
    neq: shapeless.=:!=[F[A], ConnectionIO[A]] forSome {type A}
  ): LiftInterpreterConIOK[F, F] =
    new LiftInterpreterConIOK[F, F] {
      def apply[G[_]](fk: FunctionK[G, F]): FunctionK[G, ConIOK[F, ?]] = {
        val f2coniok = λ[FunctionK[F, ConIOK[F, ?]]]( f => Kleisli( (_: Connection) => f) )
        fk andThen[ConIOK[F, ?]] f2coniok
      }
    }
}