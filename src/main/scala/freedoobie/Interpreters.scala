package freedoobie

import cats.~>
import cats.data.Coproduct
import fs2.util.{ Catchable, Suspendable }
import shapeless.{ DepFn1, HList, HNil, :: }


/**
 * Type class to combine multiple interpreters and lift them to return ConIOK[M, ?]
 *
 * Based on http://stackoverflow.com/questions/33971404/folding-a-list-of-different-types-using-shapeless-in-scala
 */
trait Interpreters[L <: HList, M[_]] extends DepFn1[L]

object Interpreters {
  type Aux[L <: HList, M[_], Out0] = Interpreters[L, M] { type Out = Out0 }

  // def combine[L <: HList, M[_]](l: L)(implicit is: Interpreters[L, M]): is.Out = is(l)
  def combine[M[_]]: PartiallyAppliedCombine[M] = new PartiallyAppliedCombine[M]

  class PartiallyAppliedCombine[M[_]] {
    def apply[L <: HList](l: L)(implicit is: Interpreters[L, M]): is.Out = is(l)
  }

  implicit def interpreters0[F[_], H[_], M[_]](implicit 
    lift: LiftInterpreterConIOK[H, M]
  ): Aux[(F ~> H) :: HNil, M, F ~> ConIOK[M, ?]] =
    new Interpreters[(F ~> H) :: HNil, M] {
      type Out = F ~> ConIOK[M, ?]
      def apply(in: (F ~> H) :: HNil): F ~> ConIOK[M, ?] = lift(in.head)
    }

  implicit def interpreters1[F[_], G[_], H[_], M[_], T <: HList](implicit
    ti: Aux[T, M, G ~> ConIOK[M, ?]],
    lift: LiftInterpreterConIOK[H, M]
  ): Aux[(F ~> H) :: T, M, Coproduct[F, G, ?] ~> ConIOK[M, ?]] =
    new Interpreters[(F ~> H) :: T, M] {
      type Out = Coproduct[F, G, ?] ~> ConIOK[M, ?]
      def apply(in: (F ~> H) :: T): Coproduct[F, G, ?] ~> ConIOK[M, ?] = lift(in.head) or ti(in.tail)
    }
}