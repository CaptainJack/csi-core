package ru.capjack.csi.core.client.utils

import ru.capjack.csi.core.client.Client
import ru.capjack.csi.core.client.ClientAcceptor
import ru.capjack.csi.core.client.ClientConnector
import ru.capjack.csi.core.client.ClientHandler
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionProducer
import ru.capjack.csi.core.client.stubs.StubClientHandler
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.EMPTY_FUNCTION_1
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

fun threadPoolFactory(name: String): ThreadFactory {
	return object : ThreadFactory {
		private val counter = AtomicInteger()
		
		override fun newThread(r: Runnable): Thread {
			return Thread(r, "$name-${counter.incrementAndGet()}")
		}
	}
}

fun clientConnector(connectionProducer: ConnectionProducer): ClientConnector {
	return ClientConnector(
		ScheduledExecutorImpl(Executors.newScheduledThreadPool(4, threadPoolFactory("connector"))),
		connectionProducer
	)
}

fun clientAcceptor(
	onSuccess: (Client) -> ClientHandler = { StubClientHandler(it) },
	onFail: (ConnectFailReason) -> Unit = EMPTY_FUNCTION_1
): ClientAcceptor {
	return object : ClientAcceptor {
		override fun acceptSuccess(client: Client): ClientHandler {
			return onSuccess(client)
		}
		
		override fun acceptFail(reason: ConnectFailReason) {
			onFail(reason)
		}
	}
}

fun byteBufferOf(s: String): ArrayByteBuffer {
	val data = ArrayByteBuffer()
	var i = 0
	val l = s.length
	var b = ""
	var q = 0
	while (i < l) {
		val c = s[i++]
		if (c != ' ') {
			++q
			if (q == 2 || c != '0') {
				b += c
				if (q == 2) {
					data.writeByte(b.toInt(16).toByte())
					q = 0
					b = ""
				}
			}
		}
	}
	return data
}

fun byteArrayOf(s: String): ByteArray {
	return byteBufferOf(s).readToArray()
}