package ru.capjack.tool.csi.server.internal

import ru.capjack.tool.csi.common.Connection
import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.ConnectionHandler
import ru.capjack.tool.csi.common.formatLoggerMessageBytes
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.alsoElse
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.trace
import ru.capjack.tool.logging.wrap
import ru.capjack.tool.utils.concurrency.LivingWorker
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.concurrency.accessOrDeferOnLive
import ru.capjack.tool.utils.concurrency.executeOnLive
import ru.capjack.tool.utils.concurrency.withCaptureOnLive
import java.util.concurrent.atomic.AtomicBoolean

internal class ConnectionDelegateImpl(
	private val connection: Connection,
	private val releaser: ConnectionReleaser,
	private var processor: ConnectionProcessor,
	executor: ScheduledExecutor,
	activityTimeoutMillis: Int
) : ConnectionDelegate, ConnectionHandler {
	
	private val logger: Logger = ownLogger.wrap { "[${connection.id}${worker.alive.make("", "-dead")}] $it" }
	private val worker = LivingWorker(executor, ::syncHandleError)
	
	private val inputBuffer = FramedByteBuffer(64)
	private val outputBuffer = ByteBuffer(64)
	
	private var active = AtomicBoolean(true)
	private val activeChecker = executor.repeat(activityTimeoutMillis, ::checkActivity)
	
	override val connectionId: Any
		get() = connection.id
	
	override fun setProcessor(processor: ConnectionProcessor) {
		if (worker.accessible) {
			syncSetProcessor(processor)
		}
		else {
			worker.execute {
				if (worker.alive) {
					syncSetProcessor(processor)
				}
				else {
					processor.processClose(this, false)
				}
			}
		}
	}
	
	override fun send(data: Byte) {
		logger.trace { "Schedule send 1B" }
		
		worker.accessOrDeferOnLive(
			{ outputBuffer.writeByte(data) },
			{
				logger.trace { formatLoggerMessageBytes("Send ", data) }
				connection.send(data)
			}
		)
	}
	
	override fun send(data: ByteArray) {
		logger.trace { "Schedule send ${data.size}B" }
		
		worker.accessOrDeferOnLive(
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
	
	override fun close(reason: ConnectionCloseReason) {
		logger.trace { "Schedule close by $reason" }
		
		worker.accessOrDeferOnLive { syncClose(reason) }
	}
	
	override fun close() {
		logger.trace { "Schedule close" }
		
		worker.accessOrDeferOnLive(::syncClose)
	}
	
	override fun handleInput(data: InputByteBuffer) {
		logger.trace { "Handle input ${data.readableSize}B" }
		
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
			syncTerminate(true)
		}
	}
	
	private fun checkActivity() {
		if (!active.compareAndSet(true, false)) {
			logger.trace { "Activity timeout expired" }
			activeChecker.cancel()
			worker.executeOnLive {
				syncClose(ConnectionCloseReason.ACTIVITY_TIMEOUT_EXPIRED)
			}
		}
	}
	
	private fun syncSetProcessor(processor: ConnectionProcessor) {
		logger.trace { "Use processor ${processor.javaClass.simpleName}" }
		this.processor = processor
	}
	
	private fun syncProcessInput() {
		logger.trace { formatLoggerMessageBytes("Process input ", inputBuffer) }
		
		while (worker.alive && inputBuffer.readable && processor.processInput(this, inputBuffer)) {
		}
		
		if (worker.alive) {
			syncFlushOutput()
		}
	}
	
	private fun syncFlushOutput() {
		if (outputBuffer.readable) {
			logger.trace { formatLoggerMessageBytes("Send ", outputBuffer) }
			
			do {
				connection.send(outputBuffer)
			}
			while (outputBuffer.readable)
		}
	}
	
	private fun syncClose(reason: ConnectionCloseReason) {
		logger.trace { "Close by $reason" }
		
		send(reason.flag)
		syncFlushOutput()
		syncTerminate(false)
	}
	
	private fun syncClose() {
		logger.trace { "Close" }
		syncFlushOutput()
		syncTerminate(false)
	}
	
	private fun syncTerminate(loss: Boolean) {
		val p = processor
		
		inputBuffer.clear()
		worker.die()
		setProcessor(NothingConnectionProcessor)
		activeChecker.cancel()
		connection.close()
		releaser.releaseConnection(this)
		
		p.processClose(this, loss)
	}
	
	private fun syncHandleError(t: Throwable) {
		logger.error("Uncaught exception", t)
		connection.close()
	}
}