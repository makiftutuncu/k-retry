package dev.akif.kretry

sealed class SleepStrategy<A> {
    abstract fun nextSleepTime(result: A?, previousSleepTime: Long): Long

    class Constant<A>: SleepStrategy<A>() {
        override fun nextSleepTime(result: A?, previousSleepTime: Long): Long =
            previousSleepTime
    }

    data class Linear<A>(val milliseconds: Long, val maxMilliseconds: Long): SleepStrategy<A>() {
        override fun nextSleepTime(result: A?, previousSleepTime: Long): Long =
            (previousSleepTime + milliseconds).coerceAtMost(maxMilliseconds)
    }

    data class ExponentialBackOff<A>(val factor: Int, val maxMilliseconds: Long): SleepStrategy<A>() {
        override fun nextSleepTime(result: A?, previousSleepTime: Long): Long =
            (previousSleepTime * factor).coerceAtMost(maxMilliseconds)
    }

    abstract class Custom<A>: SleepStrategy<A>()
}
