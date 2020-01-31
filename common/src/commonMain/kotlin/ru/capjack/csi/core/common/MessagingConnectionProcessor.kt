package ru.capjack.csi.core.common

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.InputByteBufferFrame
import ru.capjack.tool.lang.alsoIf
import ru.capjack.tool.lang.lefIf

abstract class MessagingConnectionProcessor<H : ConnectionHandler>(
	handler: H,
	protected val messages: Messages
) : ConnectionProcessor {
	
	private enum class InputState {
		MARKER,
		MESSAGE_ID,
		MESSAGE_BODY,
		MESSAGE_RECEIVED
	}
	
	protected var handler = handler
		private set
	
	private var inputState = InputState.MARKER
	private var inputMessageId = 0
	
	final override fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	final override fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		inputState = InputState.MARKER
		inputMessageId = 0
		return doProcessConnectionRecovery(channel, lastSentMessageId)
	}
	
	final override fun processConnectionClose() {
		handler.handleConnectionClose()
		handler = doProcessConnectionClose()
	}
	
	final override fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean {
		return when (inputState) {
			InputState.MARKER           -> {
				val marker = buffer.readByte()
				processChannelInputMarker(channel, buffer, marker)
			}
			InputState.MESSAGE_ID       -> processChannelInputMessageId(buffer)
			InputState.MESSAGE_BODY     -> processChannelInputMessageBody(buffer.frame)
			InputState.MESSAGE_RECEIVED -> processChannelInputMessageReceived(buffer)
		}
	}
	
	protected abstract fun doProcessConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor
	
	protected abstract fun doProcessConnectionClose(): H
	
	protected open fun processChannelInputMarker(channel: Channel, buffer: FramedInputByteBuffer, marker: Byte): Boolean {
		return when (marker) {
			ProtocolMarker.MESSAGING_NEW      -> {
				inputState = InputState.MESSAGE_ID
				processChannelInputMessageId(buffer)
			}
			ProtocolMarker.MESSAGING_RECEIVED -> {
				inputState = InputState.MESSAGE_RECEIVED
				processChannelInputMessageReceived(buffer)
			}
			ProtocolMarker.CLOSE_DEFINITELY,
			ProtocolMarker.CLOSE_PROTOCOL_BROKEN,
			ProtocolMarker.CLOSE_ERROR        -> {
				channel.close()
				false
			}
			else                                                                            ->
				throw ProtocolBrokenException("Unknown marker ${ProtocolMarker.toString(marker)}")
		}
	}
	
	private fun processChannelInputMessageId(buffer: FramedInputByteBuffer): Boolean {
		return buffer.isReadable(4).lefIf {
			inputMessageId = buffer.readInt()
			inputState = InputState.MESSAGE_BODY
			processChannelInputMessageBody(buffer.frame)
		}
	}
	
	private fun processChannelInputMessageBody(frame: InputByteBufferFrame): Boolean {
		return alsoIf(frame.fill()) {
			acceptReceivedMessageId(inputMessageId)
			inputState = InputState.MARKER
			
			handler.handleConnectionMessage(frame)
			
			check(!frame.readable) { "Message must be read in full" }
		}
	}
	
	private fun processChannelInputMessageReceived(buffer: InputByteBuffer): Boolean {
		return alsoIf(buffer.isReadable(4)) {
			val messageId = buffer.readInt()
			inputState = InputState.MARKER
			acceptDeliveredMessageId(messageId)
		}
	}
	
	private fun acceptReceivedMessageId(id: Int) {
		messages.incoming.update(id)
	}
	
	private fun acceptDeliveredMessageId(id: Int) {
		messages.outgoing.clearTo(id)
	}
}