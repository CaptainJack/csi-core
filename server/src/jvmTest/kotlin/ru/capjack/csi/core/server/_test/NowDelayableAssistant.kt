package ru.capjack.csi.core.server._test

import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant

object NowDelayableAssistant : DelayableAssistant {
	override fun execute(code: () -> Unit) {
		code.invoke()
	}
	
	override fun repeat(delayMillis: Int, code: () -> Unit): Cancelable {
		code.invoke()
		return Cancelable.DUMMY
	}
	
	override fun schedule(delayMillis: Int, code: () -> Unit): Cancelable {
		code.invoke()
		return Cancelable.DUMMY
	}
	
	override fun charge(code: () -> Unit): Cancelable {
		code.invoke()
		return Cancelable.DUMMY
	}
}