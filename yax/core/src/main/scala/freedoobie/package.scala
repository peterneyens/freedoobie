package doobie

#+cats
import cats.data.Kleisli
#-cats
#+scalaz
import scalaz.Kleisli
#-scalaz

import java.sql.Connection

package object freedoobie {
  type ConIOK[F[_], A] = Kleisli[F, Connection, A]
}