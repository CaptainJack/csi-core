package ru.capjack.csi.core.server

import ru.capjack.csi.core.server._test.server
import ru.capjack.tool.lang.waitIf
import java.lang.Thread.sleep
import kotlin.test.Test

class BehaviorRecovery {
	@Test
	fun `Recovery success`() {
		server {
			var sid = 0L
			var w = true
			
			channel {
				sid = authorization(1)
				sendMessageEcho(1, 5)
				sendMessageEcho(2, 6)
				sendMessageEcho(3, 7)
				receiveData("21 00 00 00 01 00 00 00 04 00 00 00 05 22 00 00 00 01")
				receiveData("21 00 00 00 02 00 00 00 04 00 00 00 06 22 00 00 00 02")
				receiveData("21 00 00 00 03 00 00 00 04 00 00 00 07 22 00 00 00 03")
				sendClose()
				receiveClose()
				w = false
			}
			
			waitIf(1000) { w }
			
			channel {
				sendData {
					writeByte(0x11)
					writeLong(sid)
					writeInt(1)
				}
				receiveData("11  00 00 00 03")
				receiveData("21  00 00 00 02  00 00 00 04  00 00 00 06")
				receiveData("21  00 00 00 03  00 00 00 04  00 00 00 07")
				sendClose()
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Concurrent recovery success`() {
		server {
			var cid = 0L
			
			channel {
				cid = authorization()
				receiveData("30")
				receiveClose()
			}
			
			waitIf(1000) { cid == 0L }
			
			channel {
				sendData {
					writeByte(0x11)
					writeLong(cid)
				}
				sendData("00 00 00 00")
				receiveData("11  00 00 00 00")
				sendClose()
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Recovery fail`() {
		server {
			channel {
				sendData {
					writeByte(0x11)
					writeLong(1)
					writeInt(0)
				}
				receiveData("52")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Recovery timeout`() {
		server {
			channel {
				authorization(1)
				sendClose()
				receiveClose()
			}
			sleep(2100)
		}
	}
	
	@Test
	fun `Recovery fail on long busy connection`() {
		server(channelActivityTimeout = 5) {
			var cid = 0L
			var w = true
			
			channel {
				cid = authorization()
				sendMessageSleep(1, 1100)
				w = false
				receiveMessageReceived(1)
				closeDefinitely()
			}
			
			waitIf(1000) { w }
			
			channel {
				sendData {
					writeByte(0x11)
					writeLong(cid)
					writeInt(0)
				}
				receiveData("30")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Recovery fail on busy connection on closing`() {
		server(channelActivityTimeout = 5) {
			var cid = 0L
			var w = true
			
			channel {
				cid = authorization()
				sendMessageSleepClose(1, 500)
				w = false
				receiveMessageReceived(1)
				receiveData("30")
				receiveClose()
			}
			
			waitIf(1000) { w }
			
			channel {
				sendData {
					writeByte(0x11)
					writeLong(cid)
					writeInt(0)
				}
				receiveData("30")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Recovery success on busy connection`() {
		server(channelActivityTimeout = 5) {
			var cid = 0L
			var w = true
			
			channel {
				cid = authorization()
				sendMessageEcho(1, 5)
				sendMessageEcho(2, 6)
				sendMessageEcho(3, 7)
				receiveData("21 00 00 00 01 00 00 00 04 00 00 00 05 22 00 00 00 01")
				receiveData("21 00 00 00 02 00 00 00 04 00 00 00 06 22 00 00 00 02")
				receiveData("21 00 00 00 03 00 00 00 04 00 00 00 07 22 00 00 00 03")
				sendMessageSleep(4, 50)
				w = false
				receiveMessageReceived(4)
				receiveData("30")
				receiveClose()
			}
			
			waitIf(1000) { w }
			
			channel {
				sendData {
					writeByte(0x11)
					writeLong(cid)
					writeInt(2)
				}
				receiveData("11  00 00 00 04")
				receiveData("21  00 00 00 03  00 00 00 04  00 00 00 07")
				closeDefinitely()
			}
		}
	}
}