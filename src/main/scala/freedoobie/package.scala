import java.sql.Connection

import cats.data.Kleisli

package object freedoobie {
  type ConIOK[F[_], A] = Kleisli[F, Connection, A]
}