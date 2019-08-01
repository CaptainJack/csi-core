package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.ProtocolFlag
import ru.capjack.csi.core.formatLoggerMessageBytes
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.FramedArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.alsoElse
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.concurrency.Executor
import ru.capjack.tool.utils.concurrency.LivingWorker
import ru.capjack.tool.utils.concurrency.accessOrExecuteOnLive
import ru.capjack.tool.utils.concurrency.executeOnLive
import ru.capjack.tool.utils.concurrency.withCaptureOnLive
import kotlin.jvm.Volatile

internal class ConnectionDelegateImpl(
	executor: Executor,
	private val connection: Connection,
	private var processor: ConnectionProcessor
) : ConnectionDelegate, ConnectionHandler {
	
	private val logger: Logger = ownLogger.wrap { "[${connection.id}${worker.alive.make("", "-dead")}] $it" }
	private val worker = LivingWorker(executor, ::syncHandleError)
	
	private val inputBuffer = FramedArrayByteBuffer(64)
	private val outputBuffer = ArrayByteBuffer(64)
	
	override val connectionId: Any
		get() = connection.id
	
	override fun setProcessor(processor: ConnectionProcessor) {
		if (worker.accessible) {
			logger.trace { "Use processor ${processor::class.simpleName}" }
			this.processor = processor
		}
		else {
			throw IllegalStateException()
		}
	}
	
	override fun send(data: Byte) {
		logger.trace { "Schedule send 1B" }
		
		worker.accessOrExecuteOnLive(
			{ outputBuffer.writeByte(data) },
			{
				logger.trace { formatLoggerMessageBytes("Send ", data) }
				connection.send(data)
			}
		)
	}
	
	override fun send(data: ByteArray) {
		logger.trace { "Schedule send ${data.size}B" }
		
		worker.accessOrExecuteOnLive(
			{ outputBuffer.writeArray(data) },
			{
				logger.trace { formatLoggerMessageBytes("Send ", data) }
				connection.send(data)
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
				val array = data.readToArray()
				worker.defer {
					if (worker.alive) {
						logger.trace { formatLoggerMessageBytes("Send ", array) }
						connection.send(array)
					}
				}
			}
		}
	}
	
	override fun close() {
		closed = true
		logger.trace { "Schedule close" }
		
		worker.accessOrExecuteOnLive(::syncClose)
	}
	
	@Volatile
	private var terminated = false
	
	@Volatile
	private var closed = false
	
	override fun terminate() {
		terminated = true
		logger.trace { "Schedule terminate" }
		
		worker.accessOrExecuteOnLive(::syncTerminate)
	}
	
	override fun deferInput() {
		worker.defer(::syncProcessInput)
	}
	
	override fun handleInput(data: InputByteBuffer) {
		logger.trace { "Handle input ${data.readableSize}B" }
		
		worker.withCaptureOnLive {
			inputBuffer.writeBuffer(data)
			syncProcessInput()
		} alsoElse {
			data.readToArray().also {
				worker.executeOnLive {
					inputBuffer.writeArray(it)
					syncProcessInput()
				}
			}
		}
	}
	
	override fun handleClose() {
		logger.trace { "Handle close" }
		
		worker.executeOnLive {
			processor.processLoss(this)
			syncTerminate()
		}
	}
	
	private fun syncProcessInput() {
		logger.trace { formatLoggerMessageBytes("Process input ", inputBuffer) }
		
		while (worker.alive && inputBuffer.readable) {
			if (!processor.processInput(this, inputBuffer)) {
				break
			}
		}
		
		if (outputBuffer.readable && worker.alive) {
			if (worker.relaxed) {
				syncFlushOutputDo()
			}
			else {
				worker.executeOnLive(::syncFlushOutput)
			}
		}
	}
	
	private fun syncFlushOutput() {
		if (outputBuffer.readable) {
			syncFlushOutputDo()
		}
	}
	
	private fun syncFlushOutputDo() {
		logger.trace { formatLoggerMessageBytes("Send ", outputBuffer) }
		do {
			connection.send(outputBuffer)
		}
		while (outputBuffer.readable)
	}
	
	private fun syncClose() {
		logger.trace { "Close" }
		
		send(ProtocolFlag.CLOSE)
		syncFlushOutput()
		syncTerminate()
	}
	
	private fun syncTerminate() {
		logger.trace { "Terminate" }
		
		setProcessor(NothingConnectionProcessor())
		inputBuffer.clear()
		worker.die()
		connection.close()
	}
	
	private fun syncHandleError(t: Throwable) {
		logger.error("Uncaught exception", t)
		connection.close()
	}
}