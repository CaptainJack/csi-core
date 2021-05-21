package ru.capjack.csi.core.server.internal

import ru.capjack.csi.core.internal.InternalChannelProcessor
import ru.capjack.csi.core.internal.ChannelProcessorInputResult
import ru.capjack.csi.core.internal.InternalChannel
import ru.capjack.csi.core.internal.ProtocolMarker
import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readArray

internal class AuthorizationChannelProcessor<I : Any>(
	private val authorizer: ConnectionAuthorizer<I>,
	private val acceptor: ConnectionAuthorizationAcceptor<I>
) : InternalChannelProcessor {
	
	override fun processChannelInput(channel: InternalChannel, buffer: InputByteBuffer): ChannelProcessorInputResult {
		if (buffer.isReadable(4)) {
			val size = buffer.readInt()
			if (buffer.isReadable(size)) {
				val clientId = authorizer.authorizeConnection(buffer.readArray(size))
				
				if (clientId == null) {
					channel.closeWithMarker(ProtocolMarker.SERVER_CLOSE_AUTHORIZATION)
					return ChannelProcessorInputResult.BREAK
				}
				
				val processor = acceptor.acceptAuthorization(channel, clientId)
				
				channel.useProcessor(processor)
				return ChannelProcessorInputResult.CONTINUE
			}
			else {
				buffer.backRead(4)
			}
		}
		
		return ChannelProcessorInputResult.BREAK
	}
	
	override fun processChannelClose(channel: InternalChannel, interrupted: Boolean) {}
}

