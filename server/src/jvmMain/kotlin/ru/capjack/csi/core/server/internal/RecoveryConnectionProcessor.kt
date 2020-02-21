package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.common.ConnectionProcessor
import ru.capjack.csi.core.common.InternalConnection
import ru.capjack.csi.core.common.NothingConnectionProcessor
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant

internal class RecoveryConnectionProcessor(
	private var messagingProcessor: ConnectionProcessor,
	connection: Connection,
	assistant: DelayableAssistant,
	activityTimeoutSeconds: Int
) : ConnectionProcessor {
	
	private var timeout = assistant.schedule(activityTimeoutSeconds * 2 * 1000) {
		connection.close()
	}
	
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel): ConnectionProcessor {
		return with(messagingProcessor) {
			free()
			processConnectionRecovery(channel)
		}
	}
	
	override fun processConnectionClose() {
		with(messagingProcessor) {
			free()
			processConnectionClose()
		}
	}
	
	override fun processChannelInput(channel: Channel, buffer: InputByteBuffer): Boolean {
		throw UnsupportedOperationException()
	}
	
	override fun processChannelInterrupt(connection: InternalConnection): ConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	private fun free() {
		messagingProcessor = NothingConnectionProcessor
		timeout.cancel()
		timeout = Cancelable.DUMMY
	}
}