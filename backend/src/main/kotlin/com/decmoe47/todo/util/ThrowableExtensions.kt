package com.decmoe47.todo.util

/**
 * Returns the deepest non-null cause of this throwable, or this if none.
 */
fun Throwable.rootCause(): Throwable = this.cause?.rootCause() ?: this

