package ru.capjack.csi.core.server.internal

internal interface ReceptionConnectionProcessorHeir {
	fun proceedAuthorization(): ConnectionProcessor
	
	fun proceedRecovery(): ConnectionProcessor
}