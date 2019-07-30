package ru.capjack.tool.csi.core.server.internal

internal interface ReceptionConnectionProcessorHeir {
	fun acceptAuthorization(): ConnectionProcessor
	
	fun acceptRecovery(): ConnectionProcessor
}