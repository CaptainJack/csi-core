package ru.capjack.tool.csi.client.stubs

import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.ScheduledExecutor

object DummyScheduledExecutor : ScheduledExecutor {
	override fun execute(fn: () -> Unit) {}
	
	override fun execute(command: Runnable) {}
	
	override fun repeat(delayMillis: Int, fn: () -> Unit): Cancelable {
		return Cancelable.DUMMY
	}
	
	override fun schedule(delayMillis: Int, fn: () -> Unit): Cancelable {
		return Cancelable.DUMMY
	}
	
	override fun submit(fn: () -> Unit): Cancelable {
		return Cancelable.DUMMY
	}
}