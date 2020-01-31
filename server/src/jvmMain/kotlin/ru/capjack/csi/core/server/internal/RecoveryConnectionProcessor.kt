package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.Messages
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.tool.io.FramedInputByteBuffer
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class RecoveryConnectionProcessor(
	private val connection: Connection,
	private var messagingProcessor: ConnectionProcessor,
	assistant: DelayableAssistant,
	activityTimeoutSeconds: Int
) : ConnectionProcessor {
	
	private var timeout = assistant.schedule(activityTimeoutSeconds * 2 * 1000, ::processTimeoutExpired)
	
	override fun processConnectionAccept(channel: Channel, connection: Connection, messages: Messages): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel, lastSentMessageId: Int): ConnectionProcessor {
		val p = messagingProcessor
		free()
		return p.processConnectionRecovery(channel, lastSentMessageId)
	}
	
	override fun processConnectionClose() {
		messagingProcessor.processConnectionClose()
		free()
	}
	
	override fun processChannelInput(channel: Channel, buffer: FramedInputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelClose(connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	private fun processTimeoutExpired() {
		connection.close()
	}
	
	private fun free() {
		messagingProcessor = NothingConnectionProcessor
		timeout.cancel()
		timeout = Cancelable.DUMMY
	}
}