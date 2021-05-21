package ru.capjack.csi.core.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler
import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.DummyByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readArray
import ru.capjack.tool.lang.alsoFalse
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.debug
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.TemporalAssistant
import ru.capjack.tool.utils.pool.ObjectPool
import ru.capjack.tool.utils.worker.LivingWorker
import ru.capjack.tool.utils.worker.accessOrDeferOnLive
import ru.capjack.tool.utils.worker.executeOnLive
import ru.capjack.tool.utils.worker.withCaptureOnLive
import kotlin.jvm.Volatile

abstract class InternalChannelImpl(
	private var channel: Channel,
	private var processor: InternalChannelProcessor,
	private var byteBuffers: ObjectPool<ByteBuffer>,
	private var assistant: TemporalAssistant,
	activityTimeoutSeconds: Int
) : InternalChannel, ChannelHandler {
	
	private val logger: Logger = ownLogger.wrap("[${channel.id}] ")
	
	private val worker = LivingWorker(assistant, ::syncHandleError)
	
	private var inputBuffer = byteBuffers.take()
	private var outputBuffer = byteBuffers.take()
	
	@Volatile
	private var activity = true
	private var activeChecker = assistant.repeat(activityTimeoutSeconds * 1000, ::checkActivity)
	
	override val id: Any
		get() = channel.id
	
	override fun send(data: Byte) {
		logger.trace { "Schedule send 1B" }
		
		worker.accessOrDeferOnLive(
			{ outputBuffer.writeByte(data) },
			{
				logger.debug { formatLoggerMessageBytes("Send ", data) }
				channel.send(data)
			}
		)
	}
	
	override fun send(data: ByteArray) {
		logger.trace { "Schedule send ${data.size}B" }
		
		worker.accessOrDeferOnLive(
			{ outputBuffer.writeArray(data) },
			{
				logger.debug { formatLoggerMessageBytes("Send ", data) }
				channel.send(data)
			}
		)
	}
	
	override fun send(data: InputByteBuffer) {
		logger.trace { "Schedule send ${data.readableSize}B" }
		
		if (worker.alive) {
			if (worker.accessible) {
				outputBuffer.writeBuffer(data)
			}
			else {
				data.readArray().also { bytes ->
					worker.defer {
						if (worker.alive) {
							logger.debug { formatLoggerMessageBytes("Send ", bytes) }
							channel.send(bytes)
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
		logger.trace { "Schedule close" }
		
		worker.accessOrDeferOnLive {
			syncClose(false)
		}
	}
	
	override fun useProcessor(processor: InternalChannelProcessor) {
		if (worker.accessible && worker.alive) {
			syncUseProcessor(processor)
		}
		else {
			throw IllegalStateException()
		}
	}
	
	override fun useProcessor(processor: InternalChannelProcessor, activityTimeoutSeconds: Int) {
		if (worker.accessible && worker.alive) {
			syncUseProcessor(processor)
			activeChecker.cancel()
			activeChecker = assistant.repeat(activityTimeoutSeconds * 1000, ::checkActivity)
		}
		else {
			throw IllegalStateException()
		}
	}
	
	override fun closeWithMarker(marker: Byte) {
		logger.trace { "Schedule close with marker ${ProtocolMarker.toString(marker)}" }
		
		worker.accessOrDeferOnLive {
			send(marker)
			syncClose(false)
		}
	}
	
	override fun handleChannelInput(data: InputByteBuffer) {
		logger.trace { "Handle channel input ${data.readableSize}B" }
		
		activity = true
		
		worker.withCaptureOnLive {
			inputBuffer.writeBuffer(data)
			syncProcessInput()
		} alsoFalse {
			val array = data.readArray()
			
			worker.executeOnLive {
				inputBuffer.writeArray(array)
				syncProcessInput()
			}
		}
	}
	
	override fun handleChannelClose() {
		logger.trace { "Handle channel close" }
		
		worker.executeOnLive {
			syncClose(true)
		}
	}
	
	protected abstract fun processClose()
	
	private fun checkActivity() {
		worker.executeOnLive {
			if (activity) {
				activity = false
			}
			else {
				logger.debug { "Activity timeout expired" }
				activeChecker.cancel()
				send(ProtocolMarker.CLOSE_ACTIVITY_TIMEOUT)
				syncClose(true)
			}
		}
	}
	
	private fun syncUseProcessor(processor: InternalChannelProcessor) {
		this.processor = processor
	}
	
	private fun syncProcessInput() {
		logger.debug { formatLoggerMessageBytes("Process input ", inputBuffer) }
		
		loop@ while (worker.alive && inputBuffer.readable) {
			when (processor.processChannelInput(this, inputBuffer)) {
				ChannelProcessorInputResult.CONTINUE ->
					continue@loop
				ChannelProcessorInputResult.BREAK -> {
					inputBuffer.flush()
					break@loop
				}
				ChannelProcessorInputResult.DEFER -> {
					inputBuffer.flush()
					worker.defer(::syncProcessInput)
					break@loop
				}
			}
		}
		
		if (worker.alive && outputBuffer.readable) {
			if (worker.relaxed) {
				syncSendOutput()
			}
			else {
				worker.executeOnLive(::syncSendOutputIfNeeded)
			}
		}
	}
	
	private fun syncSendOutputIfNeeded() {
		if (outputBuffer.readable) {
			syncSendOutput()
		}
	}
	
	private fun syncSendOutput() {
		logger.debug { formatLoggerMessageBytes("Send ", outputBuffer) }
		channel.send(outputBuffer)
		
		if (outputBuffer.readable) {
			throw IllegalStateException("Output buffer must be read in full")
		}
		outputBuffer.clear()
	}
	
	private fun syncClose(interrupted: Boolean) {
		syncSendOutputIfNeeded()
		
		logger.debug { "Close ${interrupted.make("interrupted", "definitely")}" }
		
		worker.die()
		
		val p = processor
		syncUseProcessor(NothingChannelProcessor)
		
		activeChecker.cancel()
		channel.close()
		
		activeChecker = Cancelable.DUMMY
		
		p.processChannelClose(this, interrupted)
		
		processClose()
		
		byteBuffers.back(inputBuffer)
		byteBuffers.back(outputBuffer)
		
		channel = NothingChannel
		inputBuffer = DummyByteBuffer
		outputBuffer = DummyByteBuffer
		
		byteBuffers = NothingByteBufferPool
		assistant = NothingTemporalAssistant
	}
	
	private fun syncHandleError(e: Throwable) {
		if (e is ProtocolBrokenException) {
			logger.warn("Protocol broken", e)
			closeWithMarker(ProtocolMarker.CLOSE_PROTOCOL_BROKEN)
		}
		else {
			logger.error("Uncaught exception", e)
			closeWithMarker(ProtocolMarker.CLOSE_ERROR)
		}
	}
}