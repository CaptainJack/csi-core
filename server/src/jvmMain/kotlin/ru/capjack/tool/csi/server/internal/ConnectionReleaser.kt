package ru.capjack.tool.csi.server.internal

internal interface ConnectionReleaser {
	fun releaseConnection(delegate: ConnectionDelegate)
}