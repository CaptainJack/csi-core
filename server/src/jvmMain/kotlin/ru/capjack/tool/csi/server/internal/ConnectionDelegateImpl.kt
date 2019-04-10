package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.formatLoggerMessageBytes
import ru.capjack.tool.csi.server.Connection
import ru.capjack.tool.csi.server.ConnectionHandler
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.InputByteBufferFramedView
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.alsoElse
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.concurrency.LivingWorker
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.concurrency.accessOnLive
import ru.capjack.tool.utils.concurrency.deferOnLive
import ru.capjack.tool.utils.concurrency.executeOnLive
import ru.capjack.tool.utils.concurrency.withCaptureOnLive
import java.util.concurrent.atomic.AtomicBoolean

internal class ConnectionDelegateImpl(
	private val connection: Connection,
	private val releaser: ConnectionReleaser,
	private var processor: ConnectionProcessor,
	executor: ScheduledExecutor,
	activityTimeoutMilliseconds: Int
) : ConnectionDelegate, ConnectionHandler {
	
	private val logger = ownLogger.wrap { "[${connection.id} ${worker.alive.make("live", "dead")}] $it" }
	private val worker = LivingWorker(executor)
	
	private val inputBuffer = FramedByteBuffer(64)
	private val outputBuffer = ByteBuffer(64)
	
	private var active = AtomicBoolean(true)
	private val activeChecker = executor.repeat(activityTimeoutMilliseconds, ::checkActivity)
	
	override val connectionId: Any
		get() = connection.id
	
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
	
	override fun close(reason: ConnectionCloseReason) {
		logger.trace { "Schedule close by $reason" }
		
		worker.accessOnLive {
			syncClose(reason)
		} alsoElse {
			worker.deferOnLive {
				syncClose(reason)
			}
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
		
		active.set(true)
		
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
			processor.processClose(this, true)
			syncTerminate()
		}
	}
	
	private fun checkActivity() {
		if (!active.compareAndSet(true, false)) {
			logger.trace { "Activity timeout expired" }
			worker.executeOnLive {
				syncClose(ConnectionCloseReason.ACTIVITY_TIMEOUT_EXPIRED)
			}
		}
	}
	
	private fun syncSetProcessor(processor: ConnectionProcessor) {
		logger.trace { "Use processor ${processor::class.simpleName}" }
		this.processor = processor
	}
	
	private fun syncProcessInput() {
		logger.trace { "Process input ${inputBuffer.readableSize}B" }
		
		try {
			while (inputBuffer.readable && processor.processInput(this, inputBuffer)) {
			}
		}
		catch (e: Throwable) {
			logger.error("Error on process input data", e)
			inputBuffer.clear()
			syncClose(ConnectionCloseReason.SERVER_ERROR)
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
	
	private fun syncClose(reason: ConnectionCloseReason) {
		logger.trace { "Close by $reason" }
		
		processor.processClose(this, false)
		
		send(reason.flag)
		syncFlushOutput()
		syncTerminate()
	}
	
	private fun syncTerminate() {
		logger.trace { "Terminate" }
		
		setProcessor(FakeConnectionProcessor)
		activeChecker.cancel()
		worker.die()
		connection.close()
		releaser.releaseConnection(this)
	}
}