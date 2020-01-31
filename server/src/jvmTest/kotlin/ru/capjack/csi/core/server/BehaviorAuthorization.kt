package ru.capjack.csi.core.server

import ru.capjack.csi.core.server._test.server
import ru.capjack.tool.lang.waitIf
import kotlin.test.Test

class BehaviorAuthorization {
	@Test
	fun `Invalid version with split message`() {
		server {
			channel {
				sendData {
					writeByte(0x10)
					writeByte(0x00)
					writeByte(0x00)
				}
				sendData {
					writeByte(0x00)
					writeByte(0x02)
					writeInt(0)
				}
				receiveData("50")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Authorization rejected on bad auth key with split message`() {
		server {
			channel {
				sendData {
					writeByte(0x10)
					writeInt(1)
					writeInt(4)
				}
				sendData {
					writeInt(0)
				}
				receiveData("51")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Error on authorization`() {
		server {
			channel {
				sendData {
					writeByte(0x10)
					writeInt(1)
					writeInt(1)
					writeByte(0x42)
				}
				receiveData("32")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Concurrent connections`() {
		server {
			channel {
				authorization(1)
				receiveData("53")
				receiveClose()
			}
			waitIf(1000) { server.connectionsAmount == 0 }
			channel {
				authorization(1)
				sendClose()
				receiveClose()
			}
		}
	}
	
	
	@Test
	fun `Input data on acceptation`() {
		var w = true
		server {
			channel {
				authorization(1)
				sendMessageSleep(1, 500)
				w = false
				receiveMessageReceived(1)
				receiveData("53")
				receiveClose()
			}
			
			waitIf(1000) { w }
			
			channel {
				sendData {
					writeByte(0x10)
					writeInt(1)
					writeInt(4)
					writeInt(1)
					writeByte(0x77)
				}
				receiveData("31")
				receiveClose()
			}
		}
	}
	
	@Test
	fun `Channel closed on acceptation`() {
		var w = true
		server {
			channel {
				authorization(1)
				sendMessageSleep(1, 500)
				w = false
				receiveMessageReceived(1)
				receiveData("53")
				receiveClose()
			}
			
			waitIf(1000) { w }
			
			channel {
				sendData {
					writeByte(0x10)
					writeInt(1)
					writeInt(4)
					writeInt(1)
				}
				sendClose()
				receiveClose()
			}
		}
	}
}