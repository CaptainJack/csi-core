package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.internal.InternalConnectionProcessor
import ru.capjack.csi.core.internal.InternalConnection
import ru.capjack.csi.core.internal.NothingConnectionProcessor
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.TemporalAssistant

internal class RecoveryConnectionProcessor(
	private var messagingProcessor: InternalConnectionProcessor,
	connection: Connection,
	assistant: TemporalAssistant,
	activityTimeoutSeconds: Int
) : InternalConnectionProcessor {
	
	private var timeout = assistant.schedule(activityTimeoutSeconds * 2 * 1000) {
		connection.close()
	}
	
	override fun processConnectionAccept(channel: Channel, connection: InternalConnection): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	override fun processConnectionRecovery(channel: Channel): InternalConnectionProcessor {
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
	
	override fun processChannelInterrupt(connection: InternalConnection): InternalConnectionProcessor {
		throw UnsupportedOperationException()
	}
	
	private fun free() {
		messagingProcessor = NothingConnectionProcessor
		timeout.cancel()
		timeout = Cancelable.DUMMY
	}
}