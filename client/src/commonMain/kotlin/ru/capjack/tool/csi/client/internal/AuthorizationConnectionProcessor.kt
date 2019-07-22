package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.ClientAcceptor
import ru.capjack.tool.csi.client.ConnectFailReason
import ru.capjack.tool.csi.client.ConnectionProducer
import ru.capjack.tool.csi.common.ConnectionCloseReason
import ru.capjack.tool.csi.common.ProtocolFlag
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.alsoIf
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.concurrency.ScheduledExecutor

internal class AuthorizationConnectionProcessor(
	private val executor: ScheduledExecutor,
	private val connectionProducer: ConnectionProducer,
	private val acceptor: ClientAcceptor
) : AbstractInputProcessor(), ConnectionProcessor {
	
	override fun processInputFlag(delegate: ConnectionDelegate, flag: Byte): Boolean {
		return if (flag == ProtocolFlag.AUTHORIZATION) {
			switchToBody()
			true
		}
		else super.processInputFlag(delegate, flag)
	}
	
	override fun processInputBody(delegate: ConnectionDelegate, buffer: FramedInputByteBuffer): Boolean {
		return buffer.isReadable(16 + 4).alsoIf {
			val client = InternalClientImpl(
				executor,
				connectionProducer,
				delegate,
				buffer.readToArray(16),
				buffer.readInt()
			)
			
			delegate.setProcessor(client)
			client.accept(acceptor)
			
			switchToFlag()
		}
	}
	
	override fun processInputClose(reason: ConnectionCloseReason) {
		val failReason = when (reason) {
			ConnectionCloseReason.AUTHORIZATION_REJECT     -> ConnectFailReason.AUTHORIZATION_REJECTED
			ConnectionCloseReason.PROTOCOL_BROKEN          -> ConnectFailReason.PROTOCOL_BROKEN
			ConnectionCloseReason.SERVER_ERROR             -> ConnectFailReason.SERVER_ERROR
			ConnectionCloseReason.CLOSE                    -> ConnectFailReason.CONNECTION_REFUSED
			ConnectionCloseReason.SERVER_SHUTDOWN          -> ConnectFailReason.CONNECTION_REFUSED
			ConnectionCloseReason.ACTIVITY_TIMEOUT_EXPIRED -> ConnectFailReason.CONNECTION_REFUSED
			ConnectionCloseReason.CONCURRENT               -> ConnectFailReason.CONNECTION_REFUSED
			else                                           -> {
				ownLogger.error("Unexpected close reason $reason")
				ConnectFailReason.PROTOCOL_BROKEN
			}
		}
		
		fail(failReason)
	}
	
	override fun processLoss(delegate: ConnectionDelegate) {
		fail(ConnectFailReason.CONNECTION_REFUSED)
	}
	
	private fun fail(reason: ConnectFailReason) {
		acceptor.acceptFail(reason)
	}
}
