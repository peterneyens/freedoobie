package doobie.freedoobie

import cats.data.Coproduct
import shapeless.{ ::, HNil, HList }

trait ToHList[F[_]] { type Out[_] <: HList }

object ToHList {
  type Aux[F[_], Out0[_]] = ToHList[F] { type Out[x] = Out0[x] }

  def apply[F[_]](implicit ev: ToHList[F]): ToHList[F] = ev

  implicit def toHList[F[_], G[_]](implicit 
    notNested: shapeless.=:!=[
      G[T], 
      Coproduct[H forSome { type H[_] }, J forSome { type J[_] }, T]
    ] forSome { type T }
  ): ToHList.Aux[Coproduct[F, G, ?], λ[α => F[α] :: G[α] :: HNil]] =
    new ToHList[Coproduct[F, G, ?]] {
      type Out[x] = F[x] :: G[x] :: HNil
    }

  implicit def toHListNested[
    F[_], G[_], H[_], J[_], TL[_] <: HList
  ](implicit
    ev: G[x] =:= Coproduct[H, J, x] forSome { type x },
    nested: ToHList.Aux[H, TL]
  ): ToHList.Aux[Coproduct[F, G, ?], λ[α => F[α] :: TL[α]]] =
    new ToHList[Coproduct[F, G, ?]] {
      type Out[x] = F[x] :: TL[x]
    }
}