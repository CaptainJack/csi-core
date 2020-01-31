package ru.capjack.csi.core.server

import ru.capjack.csi.core.ProtocolBrokenException
import ru.capjack.csi.core.common.DummyChannelHandler
import ru.capjack.csi.core.common.NothingChannelProcessor
import ru.capjack.csi.core.common.NothingInternalChannel
import ru.capjack.csi.core.common.TransitionChannelProcessor
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.FramedArrayByteBuffer
import kotlin.test.Test

class Coverage {
	@Test
	fun `DummyChannelHandler full`() {
		DummyChannelHandler.handleChannelClose()
		DummyChannelHandler.handleChannelInput(ArrayByteBuffer())
	}
	
	@Test(ProtocolBrokenException::class)
	fun `TransitionChannelProcessor processChannelInput`() {
		TransitionChannelProcessor.processChannelInput(NothingInternalChannel, FramedArrayByteBuffer())
	}
	
	@Test(UnsupportedOperationException::class)
	fun `NothingChannelProcessor processChannelInput`() {
		NothingChannelProcessor.processChannelInput(NothingInternalChannel, FramedArrayByteBuffer())
	}
	
	@Test(UnsupportedOperationException::class)
	fun `NothingChannelProcessor processChannelClose`() {
		NothingChannelProcessor.processChannelClose(NothingInternalChannel, false)
	}
}