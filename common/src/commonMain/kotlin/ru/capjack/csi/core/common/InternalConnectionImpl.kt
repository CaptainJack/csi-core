package ru.capjack.csi.core.common

import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.EMPTY_FUNCTION_0
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.debug
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.warn
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.LivingWorker
import ru.capjack.tool.utils.concurrency.accessOrDefer
import ru.capjack.tool.utils.concurrency.accessOrDeferOnLive
import ru.capjack.tool.utils.concurrency.executeOnLive
import ru.capjack.tool.utils.concurrency.withCapture

abstract class InternalConnectionImpl(
	override val id: Long,
	private var channel: InternalChannel,
	private var processor: ConnectionProcessor,
	assistant: DelayableAssistant,
	loggingName: String
) : InternalConnection, ChannelProcessor {
	
	private val logger: Logger = ownLogger.wrap("[$loggingName] ")
	private val messages = Messages()
	private val worker = LivingWorker(assistant, ::syncHandleError)
	
	override fun accept() {
		logger.trace { "Schedule accept" }
		worker.defer {
			if (worker.alive) {
				logger.debug { "Accept successful on channel ${channel.id}" }
				processor = processor.processConnectionAccept(channel, this, messages)
			}
			else {
				logger.debug { "Accept failed on closed connection" }
			}
		}
	}
	
	override fun recovery(channel: InternalChannel, lastSentMessageId: Int) {
		logger.trace { "Try recovery" }
		
		if (!tryRecovery(channel, lastSentMessageId)) {
			logger.warn { "Recovery failed by not captured connection control" }
			channel.closeWithMarker(ProtocolMarker.CLOSE_DEFINITELY)
		}
	}
	
	protected open fun tryRecovery(channel: InternalChannel, lastSentMessageId: Int): Boolean {
		return worker.withCapture {
			if (worker.alive) {
				logger.debug { "Recovery successful on channel ${channel.id}" }
				
				val prevChannel = this.channel
				this.channel = channel
				
				prevChannel.closeWithMarker(ProtocolMarker.CLOSE_DEFINITELY)
				
				channel.useProcessor(this)
				processor = processor.processConnectionRecovery(channel, lastSentMessageId)
				
				messages.outgoing.apply {
					clearTo(lastSentMessageId)
					forEach(::syncSendMessage)
				}
			}
			else {
				logger.trace { "Recovery failed on closed connection" }
				channel.closeWithMarker(ProtocolMarker.CLOSE_DEFINITELY)
			}
		}
	}
	
	override fun sendMessage(data: Byte) {
		logger.trace { "Schedule send message of 1B" }
		
		worker.accessOrDeferOnLive {
			syncSendMessage(messages.outgoing.add(data))
		}
	}
	
	override fun sendMessage(data: ByteArray) {
		logger.trace { "Schedule send message of ${data.size}B" }
		
		worker.accessOrDeferOnLive {
			syncSendMessage(messages.outgoing.add(data))
		}
	}
	
	override fun sendMessage(data: InputByteBuffer) {
		logger.trace { "Schedule send message of ${data.readableSize}B" }
		
		if (worker.alive) {
			if (worker.accessible) {
				syncSendMessage(messages.outgoing.add(data))
			}
			else {
				data.readToArray().also { bytes ->
					worker.defer {
						if (worker.alive) {
							syncSendMessage(messages.outgoing.add(bytes))
						}
					}
				}
			}
		}
		else {
			data.skipRead()
		}
	}
	
	override fun close() {
		closeWithMarker(ProtocolMarker.CLOSE_DEFINITELY)
	}
	
	override fun close(handler: () -> Unit) {
		closeWithMarker(ProtocolMarker.CLOSE_DEFINITELY, handler)
	}
	
	override fun closeWithMarker(marker: Byte) {
		closeWithMarker(marker, EMPTY_FUNCTION_0)
	}
	
	override fun closeWithMarker(marker: Byte, handler: () -> Unit) {
		logger.trace { "Schedule close with marker ${ProtocolMarker.toString(marker)}" }
		
		worker.accessOrDefer {
			if (worker.alive) {
				syncCloseWithMarker(marker)
			}
			handler.invoke()
		}
	}
	
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
		logger.trace { "Try process input ${buffer.readableSize}B from channel ${channel.id}" }
		
		worker.withCapture {
			if (this.channel != channel) {
				logger.trace { "Process input skipped with wrong channel" }
				return ChannelProcessorInputResult.BREAK
			}
			
			var success = true
			
			while (success && worker.alive && buffer.readable) {
				try {
					success = processor.processChannelInput(channel, buffer)
				}
				catch (e: Throwable) {
					syncHandleError(e)
					return ChannelProcessorInputResult.BREAK
				}
			}
			
			val alive = worker.alive
			
			if (alive) {
				syncSendLastReceivedMessageIfNeeded()
			}
			
			return if (success && alive)
				ChannelProcessorInputResult.CONTINUE
			else
				ChannelProcessorInputResult.BREAK
		}
		
		logger.trace { "Process input failed by not captured connection control" }
		
		return ChannelProcessorInputResult.DEFER
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {
		logger.trace { "Process channel ${channel.id} close ${interrupted.make("interrupted", "definitely")}" }
		
		worker.executeOnLive {
			if (worker.alive) {
				if (this.channel == channel) {
					if (interrupted) {
						processor = processor.processChannelClose(this)
					}
					else {
						logger.debug("Close definitely")
						syncTerminate()
					}
				}
				else {
					logger.trace { "Process close skipped with wrong channel" }
				}
			}
		}
	}
	
	///
	
	protected abstract fun syncProcessClose()
	
	private fun syncHandleError(e: Throwable) {
		val marker = if (e is ProtocolBrokenException) {
			logger.warn("Protocol broken", e)
			ProtocolMarker.CLOSE_PROTOCOL_BROKEN
		}
		else {
			logger.error("Uncaught exception", e)
			ProtocolMarker.CLOSE_ERROR
		}
		if (worker.alive) {
			syncCloseWithMarker(marker)
		}
	}
	
	private fun syncSendMessage(message: OutgoingMessage) {
		logger.debug { "Send message id ${message.id} of ${message.size}B" }
		channel.send(message.data)
	}
	
	private fun syncSendLastReceivedMessageIfNeeded() {
		if (messages.incoming.changed) {
			logger.debug { "Send received message id ${messages.incoming.id}" }
			channel.send(messages.incoming.makeMessage())
		}
	}
	
	private fun syncCloseWithMarker(marker: Byte) {
		syncSendLastReceivedMessageIfNeeded()
		
		logger.debug { "Close with marker ${ProtocolMarker.toString(marker)}" }
		
		val c = channel
		syncTerminate()
		c.closeWithMarker(marker)
	}
	
	private fun syncTerminate() {
		val p = processor
		
		worker.die()
		processor = NothingConnectionProcessor
		channel = NothingInternalChannel
		messages.outgoing.clear()
		
		p.processConnectionClose()
		
		syncProcessClose()
	}
}



