package ru.capjack.csi.core.client.stubs

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.ConnectionHandler
import ru.capjack.csi.core.client.utils.byteBufferOf
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.lang.toHexString
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.Executor
import ru.capjack.tool.utils.concurrency.Worker
import java.lang.Thread.sleep
import java.util.*

class PdConnection(
	override val id: Int,
	private val executor: Executor,
	actions: List<Action>
) : Connection {
	
	@Volatile
	var completed = false
	var failMessage: String? = null
	
	@Volatile
	private var running = false
	private var step = 0
	private val actions: Queue<Action> = LinkedList(actions)
	private val worker = Worker(executor) { Logging.getLogger(PdConnection::class).error("Uncaught exception", it) }
	private val inputData = ArrayByteBuffer()
	private var expectedInputAction: Action.Input = Action.Input.None
	
	private lateinit var handler: ConnectionHandler
	
	init {
		worker.defer {
			while (!running) {
				sleep(10)
			}
			nextAction()
		}
	}
	
	fun run(handler: ConnectionHandler) {
		this.handler = handler
		running = true
	}
	
	override fun send(data: Byte) {
		worker.defer {
			inputData.writeByte(data)
			processInputData()
		}
	}
	
	override fun send(data: ByteArray) {
		worker.defer {
			inputData.writeArray(data)
			processInputData()
		}
	}
	
	override fun send(data: InputByteBuffer) {
		send(data.readToArray())
	}
	
	override fun close() {
		worker.defer {
			processInputClose()
		}
	}
	
	private fun nextAction() {
		if (completed) {
			return
		}
		
		++step
		
		expectedInputAction = Action.Input.None
		val action = actions.poll()
		
		if (action == null) {
			completed = true
		}
		else {

//			ownLogger.trace("[$id-$step] $action")
			
			when (action) {
				is Action.Input  -> {
					expectedInputAction = action
				}
				is Action.Output -> {
					when (action) {
						is Action.Output.Close -> handler.handleClose()
						is Action.Output.Data  -> {
							if (action.concurrent) {
								executor.execute { handler.handleInput(action.data) }
							}
							else {
								handler.handleInput(action.data)
							}
						}
						is Action.Output.Sleep -> sleep(action.time)
						else                   -> throw IllegalArgumentException()
					}
					nextAction()
				}
			}
			
		}
		
	}
	
	private fun processInputData() {
		if (completed) {
			return
		}
		
		val action = expectedInputAction
		if (action is Action.Input.DataReceiver) {
			if (inputData.isReadable(action.size)) {
				val data = ArrayByteBuffer(action.size)
				inputData.readBuffer(data, action.size)
				action.receiver(data)
				nextAction()
				if (inputData.readable) {
					processInputData()
				}
			}
		}
		else if (action is Action.Input.Data) {
			var i = 0
			while (inputData.readable) {
				++i
				val inputByte = inputData.readByte()
				val expectedByte = action.data.readByte()
				
				if (inputByte != expectedByte) {
					inputData.backRead(i)
					action.data.backRead(i)
					fail(
						"Bad input data at $i\n" +
							"  expected: ${action.data.readToArray().toHexString(' ')}\n" +
							"  input:    ${inputData.readToArray().toHexString(' ')}\n"
					)
					break
				}
				else {
					if (!action.data.readable) {
						nextAction()
						if (inputData.readable) {
							processInputData()
						}
						break
					}
				}
			}
		}
		else {
			fail("Input data not expected")
		}
	}
	
	private fun processInputClose() {
		if (completed) {
			return
		}
		
		if (expectedInputAction == Action.Input.Close) {
			nextAction()
		}
		else {
			fail("Input close not expected")
		}
	}
	
	private fun fail(message: String) {
		completed = true
		failMessage = "[$id-$step] $message"
	}
	
	sealed class Action {
		sealed class Input : Action() {
			object None : Input()
			object Close : Input()
			class Data(val data: ArrayByteBuffer) : Input()
			class DataReceiver(val size: Int, val receiver: (InputByteBuffer) -> Unit) : Input()
		}
		
		sealed class Output : Action() {
			object Close : Output()
			class Data(val data: ArrayByteBuffer, val concurrent: Boolean = false) : Output()
			class Sleep(val time: Long) : Output()
		}
	}
	
	class Actions() {
		val list = mutableListOf<Action>()
		
		fun auth(activityTimeout: Int) {
			iData("01 00 00 00 00")
			oData(ArrayByteBuffer(1 + 8 + 8 + 4) {
				writeByte(0x01)
				writeLong(1234605616436508552)
				writeLong(1234605616436508552)
				writeInt(activityTimeout)
			})
		}
		
		fun iData(data: String) {
			list.add(Action.Input.Data(byteBufferOf(data)))
		}
		
		fun iData(data: ArrayByteBuffer) {
			list.add(Action.Input.Data(data))
		}
		
		fun iData(size: Int, receiver: (InputByteBuffer) -> Unit) {
			list.add(Action.Input.DataReceiver(size, receiver))
		}
		
		fun oData(data: String, concurrent: Boolean = false) {
			oData(byteBufferOf(data), concurrent)
		}
		
		fun oData(data: ArrayByteBuffer, concurrent: Boolean = false) {
			list.add(Action.Output.Data(data, concurrent))
		}
		
		fun iClose() {
			list.add(Action.Input.Close)
		}
		
		fun oClose() {
			list.add(Action.Output.Close)
		}
		
		fun sleep(time: Long) {
			list.add(Action.Output.Sleep(time))
		}
	}
}