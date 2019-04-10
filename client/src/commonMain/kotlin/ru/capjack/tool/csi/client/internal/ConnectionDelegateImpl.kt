package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.Connection
import ru.capjack.tool.csi.client.ConnectionHandler
import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.csi.common.formatLoggerMessageBytes
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.alsoElse
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.concurrency.Executor
import ru.capjack.tool.utils.concurrency.LivingWorker
import ru.capjack.tool.utils.concurrency.accessOnLive
import ru.capjack.tool.utils.concurrency.deferOnLive
import ru.capjack.tool.utils.concurrency.executeOnLive
import ru.capjack.tool.utils.concurrency.withCaptureOnLive

internal class ConnectionDelegateImpl(
	executor: Executor,
	private val connection: Connection,
	private var processor: ConnectionProcessor
) : ConnectionDelegate, ConnectionHandler {
	
	private val logger = ownLogger.wrap { "[${worker.alive.make("live", "dead")}] $it" }
	private val worker = LivingWorker(executor)
	
	private val inputBuffer = FramedByteBuffer(64)
	private val outputBuffer = ByteBuffer(64)
	
	init {
		syncSetProcessor(processor)
	}
	
	override fun setProcessor(processor: ConnectionProcessor) {
		if (worker.accessible) {
			syncSetProcessor(processor)
		}
		else {
			throw IllegalStateException()
		}
	}
	
	override fun send(data: Byte) {
		logger.trace { formatLoggerMessageBytes("Schedule send ", data) }
		
		worker.accessOnLive {
			logger.trace { "Fill output 1B" }
			outputBuffer.writeByte(data)
		} alsoElse {
			worker.deferOnLive {
				logger.trace { "Send 1B" }
				connection.send(data)
			}
		}
	}
	
	override fun send(data: ByteArray) {
		logger.trace { formatLoggerMessageBytes("Schedule send ", data) }
		
		worker.accessOnLive {
			logger.trace { "Fill output ${data.size}B" }
			outputBuffer.writeArray(data)
		} alsoElse {
			worker.deferOnLive {
				logger.trace { "Send ${data.size}B" }
				connection.send(data)
			}
		}
	}
	
	override fun send(data: InputByteBuffer) {
		logger.trace { formatLoggerMessageBytes("Schedule send ", data) }
		
		worker.accessOnLive {
			logger.trace { "Fill output ${data.readableSize}B" }
			outputBuffer.writeBuffer(data)
		} alsoElse {
			data.readToArray().also {
				worker.deferOnLive {
					logger.trace { "Send ${it.size}B" }
					connection.send(it)
				}
			}
		}
	}
	
	override fun close() {
		logger.trace { "Schedule close" }
		
		worker.accessOnLive(::syncClose) alsoElse {
			worker.deferOnLive(::syncClose)
		}
	}
	
	override fun terminate() {
		logger.trace { "Schedule terminate" }
		
		worker.accessOnLive(::syncTerminate) alsoElse {
			worker.deferOnLive(::syncTerminate)
		}
	}
	
	override fun handleInput(data: InputByteBuffer) {
		logger.trace { formatLoggerMessageBytes("Handle input ", data) }
		
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
	
	private fun syncSetProcessor(processor: ConnectionProcessor) {
		logger.trace { "Use processor ${processor::class.simpleName}" }
		this.processor = processor
	}
	
	private fun syncProcessInput() {
		logger.trace { "Process input ${inputBuffer.readableSize}B" }
		
		while (inputBuffer.readable && processor.processInput(this, inputBuffer)) {
		}
		
		if (worker.alive) {
			syncFlushOutput()
		}
	}
	
	private fun syncFlushOutput() {
		if (outputBuffer.readable) {
			logger.trace { "Flush output ${outputBuffer.readableSize}B" }
			
			do {
				connection.send(outputBuffer)
			}
			while (outputBuffer.readable)
		}
	}
	
	private fun syncClose() {
		logger.trace { "Close" }
		
		send(ProtocolFlag.CLOSE)
		syncFlushOutput()
		syncTerminate()
	}
	
	private fun syncTerminate() {
		logger.trace { "Terminate" }
		
		setProcessor(FakeConnectionProcessor())
		worker.die()
		connection.close()
	}
}