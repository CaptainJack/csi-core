package ru.capjack.csi.core.internal

import ru.capjack.csi.core.BaseConnectionHandler
import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.SubInputByteBuffer
import ru.capjack.tool.lang.alsoTrue
import ru.capjack.tool.lang.letTrue
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.trace

abstract class MessagingConnectionProcessor<H : BaseConnectionHandler>(
	handler: H,
	private val messages: Messages,
	protected val logger: Logger
) : InternalConnectionProcessor {
	
	private enum class InputState {
		MARKER,
		MESSAGE_ID,
		MESSAGE_BODY,
		MESSAGE_RECEIVED
	}
	
	protected var handler = handler
		private set
	
	protected val lastIncomingMessageId
		get() = messages.incoming.id
	
	private var inputState = InputState.MARKER
	private var inputMessageId = 0
	private val inputMessageBuffer = SubInputByteBuffer()
	
	final override fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	final override fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor {
		inputState = InputState.MARKER
		inputMessageId = 0
		return doProcessConnectionRecovery(channel)
	}
	
	final override fun processConnectionClose() {
		handler.handleConnectionClose()
		handler = doProcessConnectionClose()
	}
	
	final override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		return when (inputState) {
			InputState.MARKER           -> {
				val marker = buffer.readByte()
				logger.trace { "Process marker ${ProtocolMarker.toString(marker)}" }
				processChannelInputMarker(channel, buffer, marker)
			}
			InputState.MESSAGE_ID       -> processChannelInputMessageId(buffer)
			InputState.MESSAGE_BODY     -> processChannelInputMessageBody(buffer)
			InputState.MESSAGE_RECEIVED -> processChannelInputMessageReceived(buffer)
		}
	}
	
	protected abstract fun doProcessConnectionClose(): H
	
	protected abstract fun doProcessConnectionRecovery(channel: Channel): InternalConnectionProcessor
	
	protected open fun processChannelInputMarker(channel: Channel, buffer: InputByteBuffer, marker: Byte): Boolean {
		return when (marker) {
			ProtocolMarker.MESSAGING_NEW         -> {
				inputState = InputState.MESSAGE_ID
				processChannelInputMessageId(buffer)
			}
			ProtocolMarker.MESSAGING_RECEIVED    -> {
				inputState = InputState.MESSAGE_RECEIVED
				processChannelInputMessageReceived(buffer)
			}
			ProtocolMarker.CLOSE_DEFINITELY      -> {
				channel.close()
				false
			}
			ProtocolMarker.CLOSE_ACTIVITY_TIMEOUT,
			ProtocolMarker.CLOSE_ERROR,
			ProtocolMarker.CLOSE_PROTOCOL_BROKEN -> {
				logger.error("Closing because of the received marker ${ProtocolMarker.toString(marker)}")
				channel.close()
				false
			}
			else                                 ->
				throw ProtocolBrokenException("Unknown marker ${ProtocolMarker.toString(marker)}")
		}
	}
	
	private fun processChannelInputMessageId(buffer: InputByteBuffer): Boolean {
		return buffer.isReadable(4).letTrue {
			inputMessageId = buffer.readInt()
			inputState = InputState.MESSAGE_BODY
			processChannelInputMessageBody(buffer)
		}
	}
	
	private fun processChannelInputMessageBody(buffer: InputByteBuffer): Boolean {
		if (buffer.isReadable(4)) {
			val size = buffer.readInt()
			
			if (buffer.isReadable(size)) {
				logger.trace { "Receive message id $inputMessageId of ${size}B" }
				
				acceptReceivedMessageId(inputMessageId)
				inputState = InputState.MARKER
				
				inputMessageBuffer.bindSource(buffer, size)
				try {
					handler.handleConnectionMessage(inputMessageBuffer)
					check(!inputMessageBuffer.readable) { "Message must be read in full" }
				}
				finally {
					inputMessageBuffer.unbindSource()
				}
				return true
			}
			else {
				buffer.backRead(4)
				logger.trace { "Wait message id $inputMessageId body" }
			}
		}
		else {
			logger.trace { "Wait message id $inputMessageId body size" }
		}
		return false
	}
	
	private fun processChannelInputMessageReceived(buffer: InputByteBuffer): Boolean {
		return alsoTrue(buffer.isReadable(4)) {
			val messageId = buffer.readInt()
			
			logger.trace { "Outgoing message $messageId is delivered" }
			
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