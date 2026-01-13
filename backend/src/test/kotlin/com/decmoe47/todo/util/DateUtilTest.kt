package com.decmoe47.todo.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class DateUtilTest : FunSpec({
    test("LocalDateTime.now returns time within current instant window") {
        val zone = TimeZone.currentSystemDefault()
        val before = Clock.System.now().toEpochMilliseconds()
        val value = LocalDateTime.now
        val after = Clock.System.now().toEpochMilliseconds()

        val epochMillis = value.toInstant(zone).toEpochMilliseconds()
        epochMillis.shouldBeGreaterThanOrEqual(before)
        epochMillis.shouldBeLessThanOrEqual(after)
    }
})
