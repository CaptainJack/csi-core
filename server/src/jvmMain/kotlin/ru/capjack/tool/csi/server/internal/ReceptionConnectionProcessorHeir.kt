package ru.capjack.tool.csi.server.internal

internal interface ReceptionConnectionProcessorHeir {
	fun acceptAuthorization(): ConnectionProcessor
	
	fun acceptRecovery(): ConnectionProcessor
}