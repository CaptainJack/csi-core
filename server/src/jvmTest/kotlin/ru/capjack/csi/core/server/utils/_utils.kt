package ru.capjack.csi.core.server.utils

import ru.capjack.csi.core.server.ConnectionGateway
import ru.capjack.csi.core.server.Server
import ru.capjack.csi.core.server.stubs.StubClientAcceptor
import ru.capjack.csi.core.server.stubs.StubClientAuthorizer
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

fun waitThreads(vararg commands: () -> Unit) {
	val counter = CountDownLatch(commands.size)
	
	for (command in commands) {
		thread {
			command()
			counter.countDown()
		}
	}
	
	counter.await()
}

fun waitThreads(repeat: Int, command: () -> Unit) {
	waitThreads(*Array(repeat) { command })
}

fun threadPoolFactory(name: String): ThreadFactory {
	return object : ThreadFactory {
		private val counter = AtomicInteger()
		
		override fun newThread(r: Runnable): Thread {
			return Thread(r, "$name-${counter.incrementAndGet()}")
		}
	}
}

fun createServer(activityTimeout: Int, shutdownTimeout: Int, gateway: ConnectionGateway): Server {
	return Server(
		ScheduledExecutorImpl(Executors.newScheduledThreadPool(4, threadPoolFactory("server"))),
		StubClientAuthorizer(),
		StubClientAcceptor(),
		gateway,
		activityTimeout,
		shutdownTimeout
	)
}
