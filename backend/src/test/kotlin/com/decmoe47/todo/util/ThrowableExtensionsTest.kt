package com.decmoe47.todo.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ThrowableExtensionsTest : FunSpec({
    test("rootCause returns deepest exception") {
        val root = IllegalStateException("root")
        val wrapped = RuntimeException("wrapped", root)

        wrapped.rootCause() shouldBe root
    }
})
