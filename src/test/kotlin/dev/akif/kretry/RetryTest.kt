package dev.akif.kretry

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import java.lang.Exception

class RetryTest {
    @Test fun `should not retry on exception when condition is not met`() {
        val start: Long = System.currentTimeMillis()
        var end: Long?  = null

        try {
            Retry().onException(
                "test",
                SleepStrategy.Constant(),
                2,
                1000L,
                { e -> e.message?.startsWith("test") ?: false }
            ) {
                throw Exception("not a test")
            }
        } catch (e: Exception) {
            end = System.currentTimeMillis()
        }

        assertNotNull(end, "Excepted to throw an exception at least once")
        assertTrue(end!! - start < 1000L, "Excepted to not to sleep")
    }

    @Test fun `should not retry anymore on exception when max retry count is reached`() {
        val start: Long = System.currentTimeMillis()
        var end: Long?  = null

        try {
            Retry().onException(
                "test",
                SleepStrategy.Constant(),
                2,
                1000L,
                { e -> e.message?.startsWith("test") ?: false }
            ) {
                throw Exception("test")
            }
        } catch (e: Exception) {
            end = System.currentTimeMillis()
        }

        assertNotNull(end, "Excepted to throw an exception at least once")
        assertTrue(end!! - start >= 2000L, "Excepted to sleep at least 2 seconds")
        assertTrue(end - start < 3000L, "Excepted run to take less than 3 seconds")
    }

    @Test fun `should retry on exception and then get result`() {
        val start: Long = System.currentTimeMillis()
        var thrown = false

        val result =
            Retry().onException(
                "test",
                SleepStrategy.Constant(),
                2,
                1000L,
                { e -> e.message?.startsWith("test") ?: false }
            ) {
                if (!thrown) {
                    // Throw the first time
                    thrown = true
                    throw Exception("test")
                } else {
                    // Return the second time
                    42
                }
            }

        val end = System.currentTimeMillis()

        assertTrue(thrown, "Excepted to throw an exception at least once")
        assertTrue(end - start >= 1000L, "Excepted to sleep at least 1 second")
        assertTrue(end - start < 2000L, "Excepted run to take less than 2 seconds")
        assertEquals(42, result)
    }

    @Test fun `should not retry on condition when condition is not met`() {
        val start: Long = System.currentTimeMillis()

        Retry().onCondition(
            "test",
            SleepStrategy.Constant(),
            2,
            1000L,
            { it > 0 }
        ) {
            -1
        }

        val end = System.currentTimeMillis()

        assertTrue(end - start < 1000L, "Excepted to not to sleep")
    }

    @Test fun `should not retry anymore on condition when max retry count is reached`() {
        val start: Long = System.currentTimeMillis()

        Retry().onCondition(
            "test",
            SleepStrategy.Constant(),
            2,
            1000L,
            { it < 0 }
        ) {
            -1
        }

        val end = System.currentTimeMillis()

        assertTrue(end - start >= 2000L, "Excepted to sleep at least 2 seconds")
        assertTrue(end - start < 3000L, "Excepted run to take less than 3 seconds")
    }

    @Test fun `should retry on condition and then get result`() {
        val start: Long = System.currentTimeMillis()
        var retried = false

        val result =
            Retry().onCondition(
                "test",
                SleepStrategy.Constant(),
                2,
                1000L,
                { it < 0 }
            ) {
                if (!retried) {
                    // Fail the first time
                    retried = true
                    -1
                } else {
                    // Return the second time
                    42
                }
            }

        val end = System.currentTimeMillis()

        assertTrue(retried, "Excepted to have retried at least once")
        assertTrue(end - start >= 1000L, "Excepted to sleep at least 1 second")
        assertTrue(end - start < 2000L, "Excepted run to take less than 2 seconds")
        assertEquals(42, result)
    }
}
