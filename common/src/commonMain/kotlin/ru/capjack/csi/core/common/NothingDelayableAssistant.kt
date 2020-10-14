package ru.capjack.csi.core.common

import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.DelayableAssistant

object NothingDelayableAssistant : DelayableAssistant {
	override fun charge(code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
	
	override fun execute(code: () -> Unit) {
		throw UnsupportedOperationException()
	}
	
	override fun repeat(delayMillis: Int, code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
	
	override fun schedule(delayMillis: Int, code: () -> Unit): Cancelable {
		throw UnsupportedOperationException()
	}
}