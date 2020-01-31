package ru.capjack.csi.core.server._test

import ru.capjack.csi.core.server.internal.ServerChannel
import ru.capjack.csi.core.server.internal.ServerChannelReleaser

object EmptyServerChannelReleaser : ServerChannelReleaser {
	override fun releaseServerChannel(channel: ServerChannel) {}
}