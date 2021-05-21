package ru.capjack.csi.core.internal

import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.TemporalAssistant
import java.util.concurrent.TimeUnit

actual object NothingTemporalAssistant : TemporalAssistant {
	override fun charge(code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
	
	override fun execute(code: () -> Unit) {
		throw UnsupportedOperationException()
	}
	
	override fun repeat(delayMillis: Int, code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
	
	override fun repeat(delay: Long, unit: TimeUnit, code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
	
	override fun schedule(delayMillis: Int, code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
	
	override fun schedule(delay: Long, unit: TimeUnit, code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
	
}