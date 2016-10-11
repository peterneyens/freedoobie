package freedoobie

import java.sql.Connection
import javax.sql.DataSource

import doobie.util.transactor.Transactor
import fs2.util.{ Catchable, Suspendable }

/** 
 * Subclass workaround to access protected connect method
 * Copy of doobie.util.transactor.DataSourceTransactor with extra hacky method
 */
abstract class MyDataSourceTransactor[M[_]: Catchable: Suspendable, D <: DataSource] extends Transactor[M] {//extends DataSourceTransactor[M, D] {
  def configure[A](f: D => M[A]): M[A]
  def connect: M[Connection]
  def connectHack: M[Connection] = connect
}

object MyDataSourceTransactor {
  class MyDataSourceTransactorCtor[M[_]] {
    def apply[D <: DataSource](ds: D)(implicit e0: Catchable[M], e1: Suspendable[M]): MyDataSourceTransactor[M ,D] =
      new MyDataSourceTransactor[M, D] {
        def configure[A](f: D => M[A]): M[A] = f(ds)
        val connect = e1.delay(ds.getConnection)
      }
  }

  def apply[M[_]]: MyDataSourceTransactorCtor[M] =
    new MyDataSourceTransactorCtor[M]
}