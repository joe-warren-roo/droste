package qq.droste
package data

import cats.~>
import cats.Functor
import cats.Id
import cats.syntax.functor._

/** Mu is the least fixed point of a functor `F`. It is a
  * computation that can consume a inductive noninfinite
  * structure in one go.
  *
  * In Haskell this can more aptly be expressed as:
  * `data Mu f = Mu (forall x . (f x -> x) -> x)`
  */
sealed abstract class Mu[F[_]] extends (Algebra[F, ?] ~> Id) with Serializable

object Mu {
  def algebra[F[_]: Functor]: Algebra[F, Mu[F]] =
    fmf => Default(fmf)

  def coalgebra[F[_]: Functor]: Coalgebra[F, Mu[F]] =
    mf => mf[F[Mu[F]]](_ map algebra)

  def embed  [F[_]: Functor](fmf: F[Mu[F]]):   Mu[F]  = algebra  [F].apply(fmf)
  def project[F[_]: Functor](mf :   Mu[F] ): F[Mu[F]] = coalgebra[F].apply(mf)

  private final case class Default[F[_]: Functor](fmf: F[Mu[F]]) extends Mu[F] {
    def apply[A](fold: Algebra[F, A]): Id[A] =
      fold(fmf map (mf => mf(fold)))

    override def toString: String = s"Mu($fmf)"
  }

  implicit def drosteBasisForMu[F[_]: Functor]: Basis[F, Mu[F]] =
    Basis.Default[F, Mu[F]](Mu.algebra, Mu.coalgebra)

  implicit val drosteBasisSolveForMu: Basis.Solve.Aux[Mu, λ[(F[_], α) => F[α]]] = null
}
