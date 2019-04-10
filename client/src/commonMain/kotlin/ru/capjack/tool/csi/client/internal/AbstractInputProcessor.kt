package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.logging.warn

internal abstract class AbstractInputProcessor : InputProcessor {
	
	private var waitFlag = true
	
	override fun processInput(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return if (waitFlag)
			processInputFlag(delegate, buffer.readByte())
		else
			processInputBody(delegate, buffer)
	}
	
	protected open fun processInputFlag(delegate: ConnectionDelegate, flag: Byte): Boolean {
		val reason = when (flag) {
			ProtocolFlag.CLOSE                          -> ConnectionCloseReason.CONSCIOUS
			ProtocolFlag.CLOSE_CONCURRENT               -> ConnectionCloseReason.CONCURRENT
			ProtocolFlag.CLOSE_SERVER_SHUTDOWN          -> ConnectionCloseReason.SERVER_SHUTDOWN
			ProtocolFlag.CLOSE_ACTIVITY_TIMEOUT_EXPIRED -> ConnectionCloseReason.ACTIVITY_TIMEOUT_EXPIRED
			ProtocolFlag.CLOSE_AUTHORIZATION_REJECT     -> ConnectionCloseReason.AUTHORIZATION_REJECT
			ProtocolFlag.CLOSE_PROTOCOL_BROKEN          -> ConnectionCloseReason.PROTOCOL_BROKEN
			ProtocolFlag.CLOSE_RECOVERY_REJECT          -> ConnectionCloseReason.RECOVERY_REJECT
			ProtocolFlag.CLOSE_SERVER_ERROR             -> ConnectionCloseReason.SERVER_ERROR
			else                                        -> {
				ownLogger.warn { "Invalid flag 0x${flag.toHexString()}" }
				ConnectionCloseReason.PROTOCOL_BROKEN
			}
		}
		delegate.terminate()
		processInputClose(reason)
		return false
	}
	
	protected fun switchToFlag() {
		waitFlag = true
	}
	
	protected fun switchToBody() {
		waitFlag = false
	}
	
	protected abstract fun processInputBody(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean
	
	protected abstract fun processInputClose(reason: ConnectionCloseReason)
}