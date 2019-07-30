package ru.capjack.tool.csi.core.client.internal

import ru.capjack.tool.csi.core.client.Client
import ru.capjack.tool.csi.core.client.ClientAcceptor

internal interface InternalClient : Client {
	fun accept(acceptor: ClientAcceptor)
}
