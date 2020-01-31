package ru.capjack.csi.core.server

class DummyConnectionRegistry<I : Any> : ConnectionRegistry<I> {
	override fun put(identity: I, handler: () -> Unit) {
		handler()
	}
	
	override fun remove(identity: I) {
	}
}
