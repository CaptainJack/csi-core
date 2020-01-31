package ru.capjack.csi.core.client

interface ChannelGate {
	fun openChannel(acceptor: ChannelAcceptor)
}