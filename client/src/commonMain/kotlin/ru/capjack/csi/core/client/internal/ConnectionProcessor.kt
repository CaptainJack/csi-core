package ru.capjack.csi.core.client.internal

internal interface ConnectionProcessor : InputProcessor {
	fun processLoss(delegate: ConnectionDelegate)
}

