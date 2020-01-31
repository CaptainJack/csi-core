package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.csi.core.client.ChannelGate

internal class NothingChannelGate : ChannelGate {
	override fun openChannel(acceptor: ChannelAcceptor) {
		throw UnsupportedOperationException()
	}
}