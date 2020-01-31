package ru.capjack.csi.core.server

import ru.capjack.tool.utils.Closeable

interface ChannelGate {
	fun openGate(acceptor: ChannelAcceptor): Closeable
}

