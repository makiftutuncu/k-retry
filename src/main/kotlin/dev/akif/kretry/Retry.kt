package dev.akif.kretry

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.lang.IllegalArgumentException

class Retry(private val retryLogLevel: Level, private val sleepLogLevel: Level) {
    constructor(): this(Level.WARN, Level.INFO)

    fun <A> onException(tag: String,
                        sleepStrategy: SleepStrategy<A>,
                        maxRetryCount: Int,
                        initialSleepInMillis: Long,
                        shouldRetry: (Exception) -> Boolean,
                        f: () -> A): A {
        fun go(trial: Int, previousSleepTime: Long): A =
            try {
                f()
            } catch (e: Exception) {
                val time = sleepTime(
                    { shouldRetry(e) },
                    tag,
                    trial,
                    maxRetryCount,
                    sleepStrategy,
                    initialSleepInMillis,
                    previousSleepTime,
                    null
                )

                if (time == null) {
                    throw e
                } else {
                    if (time <= 0) {
                        log(sleepLogLevel, "Retrying '$tag' immediately")
                    } else {
                        log(sleepLogLevel, "Sleeping $time ms before retrying '$tag'")
                        Thread.sleep(time)
                    }

                    go(trial + 1, time)
                }
            }

        validateArgument("Maximum retry count must be non-negative!") { maxRetryCount >= 0 }
        validateArgument("Initial sleep time must be non-negative!")  { initialSleepInMillis >= 0 }

        return go(1, 0L)
    }

    fun <A> onCondition(tag: String,
                        sleepStrategy: SleepStrategy<A>,
                        maxRetryCount: Int,
                        initialSleepInMillis: Long,
                        shouldRetry: (A) -> Boolean,
                        f: () -> A): A {
        tailrec fun go(trial: Int, previousSleepTime: Long): A {
            val a = f()

            val time = sleepTime(
                { shouldRetry(a) },
                tag,
                trial,
                maxRetryCount,
                sleepStrategy,
                initialSleepInMillis,
                previousSleepTime,
                a
            )

            return if (time == null) {
                a
            } else {
                if (time <= 0) {
                    log(sleepLogLevel, "Retrying '$tag' immediately")
                } else {
                    log(sleepLogLevel, "Sleeping $time ms before retrying '$tag'")
                    Thread.sleep(time)
                }

                go(trial + 1, time)
            }
        }

        validateArgument("Maximum retry count must be non-negative!") { maxRetryCount >= 0 }
        validateArgument("Initial sleep time must be non-negative!")  { initialSleepInMillis >= 0 }

        return go(1, 0L)
    }

    internal fun <A> sleepTime(shouldRetry: () -> Boolean,
                               tag: String,
                               trial: Int,
                               maxRetryCount: Int,
                               sleepStrategy: SleepStrategy<A>,
                               initialSleepInMillis: Long,
                               previousSleepTime: Long,
                               value: A?): Long? =
        when {
            !shouldRetry() -> {
                log(retryLogLevel, "Will not retry '$tag' because condition is not satisfied")
                null
            }

            trial > maxRetryCount -> {
                log(retryLogLevel, "Giving up '$tag' after retrying $maxRetryCount times")
                null
            }

            else -> {
                if (previousSleepTime == 0L) {
                    initialSleepInMillis
                } else {
                    sleepStrategy.nextSleepTime(value, previousSleepTime)
                }
            }
        }

    private fun log(level: Level, message: String) {
        when (level) {
            Level.ERROR -> if (logger.isErrorEnabled) { logger.error(message) }
            Level.WARN  -> if (logger.isWarnEnabled)  { logger.warn(message) }
            Level.INFO  -> if (logger.isInfoEnabled)  { logger.info(message) }
            Level.DEBUG -> if (logger.isDebugEnabled) { logger.debug(message) }
            Level.TRACE -> if (logger.isTraceEnabled) { logger.trace(message) }
        }
    }

    private inline fun validateArgument(message: String, condition: () -> Boolean) {
        if (!condition()) {
            throw IllegalArgumentException(message)
        }
    }

    companion object {
        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(Retry::class.java)
    }
}
