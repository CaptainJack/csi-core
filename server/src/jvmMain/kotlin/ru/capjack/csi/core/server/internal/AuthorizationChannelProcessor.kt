package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.common.ChannelProcessor
import ru.capjack.csi.core.common.ChannelProcessorInputResult
import ru.capjack.csi.core.common.InternalChannel
import ru.capjack.csi.core.common.ProtocolMarker
import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.tool.io.FramedInputByteBuffer

internal class AuthorizationChannelProcessor<I : Any>(
	private val authorizer: ConnectionAuthorizer<I>,
	private val acceptor: ConnectionAuthorizationAcceptor<I>
) : ChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: FramedInputByteBuffer): ChannelProcessorInputResult {
		if (buffer.frame.fill()) {
			val clientId = authorizer.authorizeConnection(buffer.frame)
			
			if (clientId == null) {
				channel.closeWithMarker(ProtocolMarker.SERVER_CLOSE_AUTHORIZATION)
				return ChannelProcessorInputResult.BREAK
			}
			
			val processor = acceptor.acceptAuthorization(channel, clientId)
			
			channel.useProcessor(processor)
			return ChannelProcessorInputResult.CONTINUE
		}
		
		return ChannelProcessorInputResult.BREAK
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}

