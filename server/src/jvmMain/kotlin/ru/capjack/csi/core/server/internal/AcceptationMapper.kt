package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.ProtocolMarker
import java.util.function.BiFunction

internal class AcceptationMapper<I : Any>(
	private val connection: ServerConnection<I>
) : BiFunction<I, ServerConnection<I>?, ServerConnection<I>> {
	
	override fun apply(clientId: I, previous: ServerConnection<I>?): ServerConnection<I> {
		if (previous == null) {
			complete()
		}
		else {
			previous.closeWithMarker(ProtocolMarker.SERVER_CLOSE_CONCURRENT, ::complete)
		}
		return connection
	}
	
	private fun complete() {
		connection.accept()
	}
}