package ru.capjack.csi.core.server._test

import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.DelayableAssistant

object FakeDelayableAssistant : DelayableAssistant {
	override fun execute(code: () -> Unit) {}
	
	override fun repeat(delayMillis: Int, code: () -> Unit) = Cancelable.DUMMY
	
	override fun schedule(delayMillis: Int, code: () -> Unit) = Cancelable.DUMMY
	
	override fun charge(code: () -> Unit) = Cancelable.DUMMY
}