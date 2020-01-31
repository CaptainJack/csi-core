package ru.capjack.csi.core.server.internal

interface ServerChannelReleaser {
	fun releaseServerChannel(channel: ServerChannel)
}