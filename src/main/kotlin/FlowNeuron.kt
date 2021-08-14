import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

object ActionPotential
typealias Synapse = Flow<Double>

interface Firing {
    fun firing(): Flow<ActionPotential>
    fun toSynapse(weight: Double): Synapse = firing().map { weight }
}

class SimpleInput(private val _delay: Long) : Firing {
    override fun firing() = flow {
        while (true) {
            emit(ActionPotential)
            delay(_delay)
        }
    }
}

@FlowPreview
class FlowNeuron(private val threshold: Double, private val synapses: List<Synapse>) : Firing {
    var voltage: Double = 0.0
    override fun firing() = flow {
        synapses.asFlow().flattenMerge().buffer().collect {
            voltage += it
            println(voltage)
            if (voltage > threshold) {
                voltage = 0.0
                emit(ActionPotential)
            }
        }
    }
}

@FlowPreview
suspend fun basicTest() {
    val weights = listOf(1.0, 2.0, 3.0)
    val neurons = listOf(SimpleInput(20), SimpleInput(30), SimpleInput(25))
    val synapses: List<Synapse> = neurons.zip(weights) { n, w -> n.toSynapse(w) }
    val nOut = FlowNeuron(79.0, synapses)
    nOut.firing().take(2).collect { println("Fired!") }
    println("Done!")
}

@FlowPreview
fun main() = runBlocking {
    basicTest()
}