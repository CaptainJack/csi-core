package ru.capjack.tool.csi.client.internal

internal interface ConnectionProcessor : InputProcessor {
	fun processLoss(delegate: ConnectionDelegate)
}

