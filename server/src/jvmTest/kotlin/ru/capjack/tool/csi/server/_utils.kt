package ru.capjack.tool.csi.server

import ru.capjack.tool.lang.toHexString
import kotlin.test.assertEquals

fun now(): Long {
	return System.currentTimeMillis()
}

fun Long.passedLess(ms: Long): Boolean {
	return now() - this <= ms
}


fun assertEqualsBytes(expected: String, actual: ByteArray) {
	assertEquals(expected, actual.toHexString(' '))
}