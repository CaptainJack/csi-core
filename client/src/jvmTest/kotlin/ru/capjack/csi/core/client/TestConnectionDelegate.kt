package ru.capjack.csi.core.client

import ru.capjack.csi.core.client.internal.ConnectionDelegate
import ru.capjack.csi.core.client.internal.ConnectionDelegateImpl
import ru.capjack.csi.core.client.internal.ConnectionProcessor
import ru.capjack.csi.core.client.internal.NothingConnectionProcessor
import ru.capjack.csi.core.client.stubs.DummyConnection
import ru.capjack.csi.core.client.stubs.DummyScheduledExecutor
import ru.capjack.tool.io.ArrayByteBuffer
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
			object : DummyConnection() {
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
			delegate.handleInput(ArrayByteBuffer(byteArrayOf(0)))
		}
		
		sleep(10)
		
		delegate.handleInput(ArrayByteBuffer(byteArrayOf(1)))
		
		delegate.send(0)
		delegate.send(byteArrayOf(0))
		delegate.send(ArrayByteBuffer(byteArrayOf(0)))
		
		delegate.close()
		delegate.handleClose()
		delegate.terminate()
		
		delegate.handleInput(ArrayByteBuffer(byteArrayOf(1)))
		
		delegate.send(0)
		delegate.send(byteArrayOf(0))
		delegate.send(ArrayByteBuffer(byteArrayOf(0)))
		
		delegate.close()
		delegate.handleClose()
		delegate.terminate()
		
		sleep(100)
		
		delegate.handleInput(ArrayByteBuffer(byteArrayOf(1)))
		
		delegate.send(0)
		delegate.send(byteArrayOf(0))
		delegate.send(ArrayByteBuffer(byteArrayOf(0)))
		
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