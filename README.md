# k-retry
k-retry is a naive retry implementation in Kotlin. Sleep strategy, sleep times and logging are customizable. You'd need a `Retry` instance to work. Here's how to build one:

```kotlin
import dev.akif.kretry.Retry
import org.slf4j.event.Level

// A retry instance with default logging levels
val retry = Retry()

// A retry instance custom logging levels
val customRetry = Retry(retryLogLevel = Level.INFO, sleepLogLevel = Level.DEBUG)
```

## 1. Retrying on an Exception

You might want to retry when an exception is thrown. You can customize the behavior.

```kotlin
import dev.akif.kretry.Retry
import dev.akif.kretry.SleepStrategy

fun doSomething(): String { /* Do something possibly throwing exceptions */ }

// Will sleep 500 ms and retry up to 3 times
// if exception message contains "later"
val something1 = Retry().onException(
  tag                  = "doing-something",
  sleepStrategy        = SleepStrategy.Constant,
  maxRetryCount        = 3,
  initialSleepInMillis = 500L,
  shouldRetry          = { e -> e.message?.contains("later") ?: false }
) {
  doSomething()
}

// Will sleep 500 ms and retry up to 10 times
// each time increasing sleep time by 100 ms linearly
// up to maximum 1000 ms
// if exception message contains "later"
val something2 = Retry().onException(
  tag                  = "doing-something",
  sleepStrategy        = SleepStrategy.Linear(100L, 1000L),
  maxRetryCount        = 10,
  initialSleepInMillis = 500L,
  shouldRetry          = { e -> e.message?.contains("later") ?: false }
) {
  doSomething()
}

// Will sleep 500 ms and retry up to 5 times
// each time multiplying sleep time by 2
// up to maximum 4000 ms
// if exception message contains "later"
val something3 = Retry().onException(
  tag                  = "doing-something",
  sleepStrategy        = SleepStrategy.ExponentialBackOff(2, 4000L),
  maxRetryCount        = 4,
  initialSleepInMillis = 500L,
  shouldRetry          = { e -> e.message?.contains("later") ?: false }
) {
  doSomething()
}

// You can have your own strategy
// This example behaves the same as `Constant`
val customStrategy = object : SleepStrategy.Custom<String>() {
    override fun nextSleepTime(result: String?, previousSleepTime: Long): Long =
        previousSleepTime
}

// Behaves the same for `something1`
val something4 = Retry().onException(
  tag                  = "doing-something",
  sleepStrategy        = customStrategy,
  maxRetryCount        = 3,
  initialSleepInMillis = 500L,
  shouldRetry          = { e -> e.message?.contains("later") ?: false }
) {
  doSomething()
}
```

## 2. Retrying on a Condition

You might want to retry based on a condition. You can customize the behavior.

```kotlin
import dev.akif.kretry.Retry
import dev.akif.kretry.SleepStrategy

fun getSomething(): String { /* Get something, not throwing exceptions */ }

// Will sleep 500 ms and retry up to 3 times
// if result is empty or it contains "failed"
val something1 = Retry().onCondition(
  tag                  = "doing-something",
  sleepStrategy        = SleepStrategy.Constant,
  maxRetryCount        = 3,
  initialSleepInMillis = 500L,
  shouldRetry          = { it.isEmpty() || it.contains("failed") }
) {
  getSomething()
}

// Will sleep 500 ms and retry up to 10 times
// each time increasing sleep time by 100 ms linearly
// up to maximum 1000 ms
// if result is empty or it contains "failed"
val something2 = Retry().onCondition(
  tag                  = "doing-something",
  sleepStrategy        = SleepStrategy.Linear(100L, 1000L),
  maxRetryCount        = 10,
  initialSleepInMillis = 500L,
  shouldRetry          = { it.isEmpty() || it.contains("failed") }
) {
  getSomething()
}

// Will sleep 500 ms and retry up to 5 times
// each time multiplying sleep time by 2
// up to maximum 4000 ms
// if result is empty or it contains "failed"
val something3 = Retry().onCondition(
  tag                  = "doing-something",
  sleepStrategy        = SleepStrategy.ExponentialBackOff(2, 4000L),
  maxRetryCount        = 4,
  initialSleepInMillis = 500L,
  shouldRetry          = { it.isEmpty() || it.contains("failed") }
) {
  getSomething()
}

// You can have your own strategy
// This example behaves the same as `Constant`
val customStrategy = object : SleepStrategy.Custom<String>() {
    override fun nextSleepTime(result: String?, previousSleepTime: Long): Long =
        previousSleepTime
}

// Behaves the same for `something1`
val something4 = Retry().onCondition(
  tag                  = "doing-something",
  sleepStrategy        = customStrategy,
  maxRetryCount        = 3,
  initialSleepInMillis = 500L,
  shouldRetry          = { it.isEmpty() || it.contains("failed") }
) {
  getSomething()
}
```

## Contributing

All contributions are welcome. Please feel free to send a pull request. Thank you.

## License

This content is licensed with MIT License. See [LICENSE.md](LICENSE.md) for details.
