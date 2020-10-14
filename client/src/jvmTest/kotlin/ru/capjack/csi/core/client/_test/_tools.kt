package ru.capjack.csi.core.client._test

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer

val GLOBAL_ASSISTANT = assistant(8, "global")

inline fun gate(crossinline code: ChannelAcceptor.() -> Unit): ChannelGate {
	return object : ChannelGate {
		override fun openChannel(acceptor: ChannelAcceptor) {
			GLOBAL_ASSISTANT.execute {
				acceptor.code()
			}
		}
	}
}

open class FnChannel(
	private val send: InputByteBuffer.() -> Unit = { skipRead() },
	private val close: () -> Unit = {},
	override val id: Any = 1
) : Channel {
	
	override fun send(data: Byte) {
		send(ArrayByteBuffer { writeByte(data) })
	}
	
	override fun send(data: ByteArray) {
		send(ArrayByteBuffer { writeArray(data) })
	}
	
	override fun send(data: InputByteBuffer) {
		send.invoke(data)
	}
	
	override fun close() {
		close.invoke()
	}
	
}

open class FnInternalChannel(
	send: InputByteBuffer.() -> Unit = { skipRead() },
	close: () -> Unit = {},
	id: Any = 1
) : FnChannel(send, close, id), InternalChannel {
	
	private var processor: ChannelProcessor? = null
	
	override fun useProcessor(processor: ChannelProcessor) {
		this.processor = processor
	}
	
	override fun useProcessor(processor: ChannelProcessor, activityTimeoutSeconds: Int) {
		this.processor = processor
	}
	
	override fun closeWithMarker(marker: Byte) {
		send(marker)
		close()
	}
	
	override fun close() {
		super.close()
		processor?.processChannelClose(this, false)
	}
}