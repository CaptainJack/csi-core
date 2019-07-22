package ru.capjack.tool.csi.client

import ru.capjack.tool.csi.client.internal.ConnectionDelegate
import ru.capjack.tool.csi.client.internal.ConnectionDelegateImpl
import ru.capjack.tool.csi.client.internal.ConnectionProcessor
import ru.capjack.tool.csi.client.internal.NothingConnectionProcessor
import ru.capjack.tool.csi.client.stubs.DummyConnection
import ru.capjack.tool.csi.client.stubs.DummyConnectionProducer
import ru.capjack.tool.csi.client.stubs.DummyScheduledExecutor
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.test.Test

class TestConnectionDelegate {
	@Test(expected = IllegalStateException::class)
	fun `POF setProcessor`() {
		val delegate = ConnectionDelegateImpl(
			DummyScheduledExecutor,
			DummyConnection(),
			NothingConnectionProcessor()
		)
		
		delegate.setProcessor(NothingConnectionProcessor())
	}
	
	@Test()
	fun `POC handleInput, handleClose, send, close, terminate`() {
		val delegate = ConnectionDelegateImpl(
			DummyScheduledExecutor,
			object: DummyConnection() {
				override fun send(data: InputByteBuffer) {
					data.readByte()
				}
			},
			object : ConnectionProcessor {
				override fun processLoss(delegate: ConnectionDelegate) {
				}
				
				override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
					if (buffer.readByte() == 0.toByte()) {
						delegate.send(byteArrayOf(0, 1))
						sleep(100)
					}
					return false
				}
			}
		)
		
		thread {
			delegate.handleInput(ByteBuffer(byteArrayOf(0)))
		}
		
		sleep(10)
		
		delegate.handleInput(ByteBuffer(byteArrayOf(1)))
		
		delegate.send(0)
		delegate.send(byteArrayOf(0))
		delegate.send(ByteBuffer(byteArrayOf(0)))
		
		delegate.close()
		delegate.handleClose()
		delegate.terminate()
		
		delegate.handleInput(ByteBuffer(byteArrayOf(1)))
		
		delegate.send(0)
		delegate.send(byteArrayOf(0))
		delegate.send(ByteBuffer(byteArrayOf(0)))
		
		delegate.close()
		delegate.handleClose()
		delegate.terminate()
		
		sleep(100)
		
		delegate.handleInput(ByteBuffer(byteArrayOf(1)))
		
		delegate.send(0)
		delegate.send(byteArrayOf(0))
		delegate.send(ByteBuffer(byteArrayOf(0)))
		
		delegate.close()
		delegate.handleClose()
		delegate.terminate()
	}
	
	@Test()
	fun `POC error`() {
		val delegate = ConnectionDelegateImpl(
			DummyScheduledExecutor,
			object : DummyConnection() {
				override fun send(data: Byte) {
					throw RuntimeException()
				}
			},
			NothingConnectionProcessor()
		)
		
		delegate.send(0)
	}
}