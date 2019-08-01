package ru.capjack.csi.core.client.internal

import ru.capjack.csi.core.client.Client
import ru.capjack.csi.core.client.ClientAcceptor

internal interface InternalClient : Client {
	fun accept(acceptor: ClientAcceptor)
}
