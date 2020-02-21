package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.server.Server
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.OutputByteBuffer
import ru.capjack.tool.lang.waitIf
import ru.capjack.tool.logging.Logging
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.fail

inline fun server(
	version: Int = 1,
	shutdownTimeout: Int = 1,
	channelActivityTimeout: Int = 1,
	channelStopTimeout: Int = 1,
	connectionStopTimeout: Int = 1,
	noinline connectionIdGenerator: () -> Long = Random.Default::nextLong,
	crossinline actions: ServerActions.() -> Unit
) {
	val errors = Errors()
	
	val gateway = TestChannelGate(errors)
	val server = Server(
		assistant(2, "server"),
		GLOBAL_BYTE_BUFFER_POOL,
		TestConnectionAuthorizer(),
		TestConnectionAcceptor(),
		gateway,
		shutdownTimeout,
		version,
		channelActivityTimeout,
		channelStopTimeout,
		connectionStopTimeout,
		connectionIdGenerator
	)
	
	errors.observe {
		ServerActions(server, gateway).apply(actions)
		
		waitIf(10_000) { !gateway.allChannelsClosed }
		
		server.stop()
		
		check(gateway.allChannelsClosed) {
			"Not all channels closed\n" + gateway.channels.filterNot(TestChannel::completed).joinToString("\n", transform = TestChannel::state)
		}
		
		assertEquals(0, server.channels)
		assertEquals(0, server.connections)
		assertFalse(server.running)
	}
	
	if (errors.isNotEmpty()) {
		val logger = Logging.getLogger("test")
		
		errors.forEach { logger.error("Error", it) }
		
		throw fail("Fail")
	}
}


class Errors : ConcurrentLinkedQueue<Throwable>() {
	inline fun observe(fn: () -> Unit) {
		try {
			fn()
		}
		catch (e: Throwable) {
			add(e)
		}
	}
}


class ServerActions(val server: Server<*>, private val gateway: TestChannelGate) {
	
	fun channel(actions: ChannelActions.() -> Unit): Channel {
		return gateway.openChannel(actions)
	}
}


class ChannelActions(private val channel: TestChannel) {
	
	fun receiveData(data: String) {
		channel.receiveData(stringToByteBuffer(data))
	}
	
	fun receiveData(data: OutputByteBuffer.() -> Unit) {
		channel.receiveData(ArrayByteBuffer(data))
	}
	
	fun receiveDataRead(size: Int, reader: InputByteBuffer.() -> Unit) {
		channel.receiveDataRead(size, reader)
	}
	
	fun receiveClose() {
		channel.receiveClose()
	}
	
	fun sendData(data: String) {
		channel.sendData(stringToByteBuffer(data))
	}
	
	fun sendData(data: OutputByteBuffer.() -> Unit) {
		channel.sendData(ArrayByteBuffer(data))
	}
	
	fun sendClose() {
		channel.sendClose()
	}
	
	fun closeDefinitely() {
		sendData("30")
		receiveClose()
	}
	
	fun authorization(clientId: Int = Random.nextInt(0, Int.MAX_VALUE), clientVersion: Int = 1): Long {
		var connectionId = 0L
		
		sendData {
			writeByte(0x10)
			writeInt(clientVersion)
			writeInt(4)
			writeInt(clientId)
		}
		receiveData("10")
		receiveDataRead(8) {
			connectionId = readLong()
		}
		
		assertNotEquals(0, connectionId)
		
		return connectionId
	}
	
	fun sendMessage(messageId: Int, writer: OutputByteBuffer.() -> Unit) {
		sendData {
			writeByte(0x21)
			writeInt(messageId)
			ArrayByteBuffer(writer).also {
				writeInt(it.readableSize)
				writeBuffer(it)
			}
		}
	}
	
	fun sendMessageEcho(messageId: Int, message: Int) {
		sendMessage(messageId) {
			writeByte(TestApiMarker.ECHO)
			writeInt(message)
		}
	}
	
	fun sendMessageSleep(messageId: Int, ms: Int) {
		sendMessage(messageId) {
			writeByte(TestApiMarker.SLEEP)
			writeInt(ms)
		}
	}
	
	fun sendMessageClose(messageId: Int) {
		sendMessage(messageId) {
			writeByte(TestApiMarker.CLOSE)
		}
	}
	
	fun sendMessageCloseSleep(messageId: Int, ms: Int) {
		sendMessage(messageId) {
			writeByte(TestApiMarker.CLOSE_SLEEP)
			writeInt(ms)
		}
	}
	
	fun sendMessageSleepClose(messageId: Int, ms: Int) {
		sendMessage(messageId) {
			writeByte(TestApiMarker.SLEEP_CLOSE)
			writeInt(ms)
		}
	}
	
	fun receiveMessageReceived(messageId: Int) {
		receiveData {
			writeByte(0x22)
			writeInt(messageId)
		}
	}
	
	fun receiveMessageInt(messageId: Int, message: Int) {
		receiveData {
			writeByte(0x21)
			writeInt(messageId)
			writeInt(4)
			writeInt(message)
		}
	}
}