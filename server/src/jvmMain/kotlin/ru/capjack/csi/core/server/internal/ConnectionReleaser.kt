package ru.capjack.csi.core.server.internal

internal interface ConnectionReleaser {
	fun releaseConnection(delegate: ConnectionDelegate)
}