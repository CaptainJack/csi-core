package ru.capjack.tool.csi.core.client.internal

internal interface ConnectionProcessor : InputProcessor {
	fun processLoss(delegate: ConnectionDelegate)
}

