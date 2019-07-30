package ru.capjack.tool.csi.core.server.internal

internal interface ConnectionReleaser {
	fun releaseConnection(delegate: ConnectionDelegate)
}