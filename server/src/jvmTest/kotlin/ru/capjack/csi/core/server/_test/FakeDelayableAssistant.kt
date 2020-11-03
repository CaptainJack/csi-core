package ru.capjack.csi.core.server._test

import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.TemporalAssistant
import java.util.concurrent.TimeUnit

object FakeTemporalAssistant : TemporalAssistant {
	override fun execute(code: () -> Unit) {}
	
	override fun charge(code: () -> Unit) = Cancelable.DUMMY
	
	override fun repeat(delayMillis: Int, code: () -> Unit) = Cancelable.DUMMY
	
	override fun schedule(delayMillis: Int, code: () -> Unit) = Cancelable.DUMMY
	
	override fun repeat(delay: Long, unit: TimeUnit, code: () -> Unit) = Cancelable.DUMMY
	
	override fun schedule(delay: Long, unit: TimeUnit, code: () -> Unit) = Cancelable.DUMMY
}