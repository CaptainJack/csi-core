package ru.capjack.tool.csi.core.server.stubs

import ru.capjack.tool.csi.core.ConnectionCloseReason
import ru.capjack.tool.csi.core.server.internal.ConnectionDelegate
import ru.capjack.tool.csi.core.server.internal.ConnectionProcessor
import ru.capjack.tool.io.InputByteBuffer

internal class SlowlyConnectionDelegate(
	override val connectionId: Int = 1,
	private val sleepOnSend: Long = 0,
	private val sleepOnClose: Long = 0
) : ConnectionDelegate {
	
	override fun setProcessor(processor: ConnectionProcessor) {}
	
	override fun send(data: Byte) {
		onSend()
	}
	
	override fun send(data: ByteArray) {
		onSend()
	}
	
	override fun send(data: InputByteBuffer) {
		onSend()
	}
	
	private fun onSend() {
		if (sleepOnSend != 0L) Thread.sleep(sleepOnSend)
	}
	
	override fun close(reason: ConnectionCloseReason) {
		close()
	}
	
	override fun close() {
		if (sleepOnClose != 0L) Thread.sleep(sleepOnClose)
	}
}
