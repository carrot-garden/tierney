package tierney

import cats.Functor
import cats.~>
import cats.syntax.functor._

package object core {
  type IdKK[F[_], A] = F[A]
  
  final implicit class Fix[F[_]](val unfix: F[Fix[F]]) {
    def cata[A](f: F[A] ⇒ A)(implicit functor: Functor[F]): A =
      f(unfix map { _.cata(f) })
  }
  def unfix_[F[_]]: Fix[F] => F[Fix[F]] = {_.unfix}

  def unfixK[S[_[_], _]] = Lambda[FixK[S, ?] ~> S[FixK[S, ?], ?]](_.unfix)
  def cataK[S[_[_], _], F[_]](f: S[F, ?] ~> F)(implicit functor: FunctorK[S]): FixK[S, ?] ~> F =
    unfixK[S] andThen[S[F, ?]] functor.map[FixK[S, ?], F](new LazyFunctionK[FixK[S, ?], F](cataK(f))) andThen f
  final implicit class FixK[F[_[_], _], A](val unfix: F[FixK[F, ?], A]) {
    def cata[G[_]](f: F[G, ?] ~> G)(implicit functor: FunctorK[F]): G[A] = cataK(f).apply(this) 
  }
  
  def fixKK[W[_[_[_], _], _[_], _], F[_]]: W[Lambda[(G[_], B) => FixKK[W, G, B]], F, ?] ~> FixKK[W, F, ?] =
    Lambda[W[Lambda[(G[_], B) => FixKK[W, G, B]], F, ?] ~> FixKK[W, F, ?]](FixKK(_))
  /** If the definition of this class is moved below the next two methods, we get a compile failure.
   * This makes no sense, and I suspect a bug in kind-projector
   */
  final implicit class FixKK[W[_[_[_], _], _[_], _], F[_], A](val unfix: W[Lambda[(G[_], B) => FixKK[W, G, B]], F, A]) {
    def cata[S[_[_], _]](f: W[S, F, ?] ~> S[F, ?])(implicit functor: FunctorKK[W]): S[F, A] = cataKK(f).apply(this)
  }
  def unfixKK[W[_[_[_], _], _[_], _], F[_]]: (FixKK[W, F, ?] ~> W[Lambda[(G[_], B) => FixKK[W, G, B]], F, ?]) =
    Lambda[FixKK[W, F, ?] ~> W[Lambda[(G[_], B) => FixKK[W, G, B]], F, ?]](_.unfix)
  def cataKK[W[_[_[_], _], _[_], _], S[_[_], _], F[_]](f: W[S, F, ?] ~> S[F, ?])(implicit functor: FunctorKK[W]): FixKK[W, F, ?] ~> S[F, ?] =
    unfixKK[W, F] andThen[W[S, F, ?]] functor.map[Lambda[(G[_], A) => FixKK[W, G, A]], S, F](new LazyFunctionKK[Lambda[(G[_], A) => FixKK[W, G, A]], S, F](cataKK(f))) andThen[S[F, ?]] f
}
