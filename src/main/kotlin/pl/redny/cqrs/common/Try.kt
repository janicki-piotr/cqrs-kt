package pl.redny.cqrs.common

sealed class Try<out T> {
    companion object {
        operator fun <T> invoke(body: () -> T): Try<T> {
            return try {
                Success(body())
            } catch (e: Exception) {
                Failure(e)
            }
        }
    }

    abstract fun isSuccess(): Boolean
    abstract fun isFailure(): Boolean
    abstract fun get(): T
    abstract fun getOrElse(default: @UnsafeVariance T): T
    abstract fun getOrElseThrow(default: Throwable): T
    abstract fun orElse(default: Try<@UnsafeVariance T>): Try<T>

    fun <U> map(f: (T) -> U): Try<U> {
        return when (this) {
            is Success -> Try { f(this.value) }
            is Failure -> this as Failure<U>
        }
    }

    fun <U> flatMap(f: (T) -> Try<U>): Try<U> {
        return when (this) {
            is Success -> f(this.value)
            is Failure -> this as Failure<U>
        }
    }

    data class Success<out T>(val value: T) : Try<T>() {
        override fun isSuccess(): Boolean = true
        override fun isFailure(): Boolean = false
        override fun get(): T = value
        override fun getOrElse(default: @UnsafeVariance T): T = value
        override fun getOrElseThrow(default: Throwable): T = value
        override fun orElse(default: Try<@UnsafeVariance T>): Try<T> = this
    }

    data class Failure<out T>(val e: Throwable) : Try<T>() {
        override fun isSuccess(): Boolean = false
        override fun isFailure(): Boolean = true
        override fun get(): T = throw e
        override fun getOrElse(default: @UnsafeVariance T): T = default
        override fun getOrElseThrow(default: Throwable): T = throw default
        override fun orElse(default: Try<@UnsafeVariance T>): Try<T> = default
    }
}