package ru.capjack.csi.core.server

import org.junit.Test
import ru.capjack.csi.core.server._test.server
import kotlin.test.Ignore

class BehaviorStops {
	@Test
	fun `Fast stop on empty server`() {
		server {}
	}
	
	@Test()
	fun `Stop on silent channel with shutdown timeout`() {
		server {
			channel {
				receiveData("40  00 00 00 01  54")
				receiveClose()
			}
			server.stop()
		}
	}
	
	@Test()
	fun `Stop on silent channel with shutdown timeout when they close independently`() {
		server {
			channel {
				receiveData("40  00 00 00 01")
				sendClose()
				receiveClose()
			}
			server.stop()
		}
	}
	
	
	@Test()
	fun `Stop on silent channel without shutdown timeout`() {
		server(shutdownTimeout = 0) {
			channel {
				receiveData("54")
				receiveClose()
			}
			server.stop()
		}
	}
	
	@Test
	fun `Refuse channel on shutdown`() {
		server(shutdownTimeout = 0) {
			server.stop()
			
			channel {
				receiveData("54")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Stop on busy connection`() {
		server(channelActivityTimeout = 5, shutdownTimeout = 0) {
			channel {
				authorization()
				sendMessageSleep(1, 1500)
				server.stop()
				receiveMessageReceived(1)
				receiveData("54")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Stop on very busy connection`() {
		server(channelActivityTimeout = 5, shutdownTimeout = 0) {
			channel {
				authorization()
				sendMessageSleep(1, 2500)
				server.stop()
				receiveMessageReceived(1)
				receiveData("54")
				receiveClose()
			}
		}
	}
}
