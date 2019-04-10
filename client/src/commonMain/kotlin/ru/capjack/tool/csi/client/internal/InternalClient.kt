package ru.capjack.tool.csi.client.internal

import ru.capjack.tool.csi.client.Client
import ru.capjack.tool.csi.client.ClientAcceptor

internal interface InternalClient : Client {
	fun accept(acceptor: ClientAcceptor)
}
