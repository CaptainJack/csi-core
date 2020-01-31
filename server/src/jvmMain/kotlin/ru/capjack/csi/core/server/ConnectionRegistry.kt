package ru.capjack.csi.core.server

interface ConnectionRegistry<I : Any> {
	fun put(identity: I, handler: () -> Unit)
	
	fun remove(identity: I)
}