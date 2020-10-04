import kotlinx.coroutines.*

fun main() = runBlocking {
    val neuron = Neuron(0.5)
    simulate(neuron)
}

suspend fun simulate(neuron: Neuron) = coroutineScope {
    val input = launch {
        while (true) {
            delay(100)
            neuron.receive(0.05)
        }
    }
    launch {
        neuron.run()
    }
    repeat(50) {
        delay(200L)
        println(neuron.voltage)
    }
    neuron.stop()
    input.cancel()
}
