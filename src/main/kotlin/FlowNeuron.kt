import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

object ActionPotential

interface Firing {
    val potentials: Flow<ActionPotential>
}

class SimpleInput(private val _delay: Long) : Firing {
    override val potentials: Flow<ActionPotential> = flow {
        while (true) {
            emit(ActionPotential)
            delay(_delay)
        }
    }
}

class FlowNeuron(private val input: Flow<Double>, private val threshold: Double) : Firing {
    override val potentials: Flow<ActionPotential> = firing()
    var voltage: Double = 0.0
    fun firing(): Flow<ActionPotential> = flow {
        input.buffer().collect {
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
fun List<Firing>.toInput(weights: List<Double>) : Flow<Double> {
    return this.zip(weights) {
        n: Firing, w: Double -> n.potentials.map { w }
    }.asFlow().flattenMerge()
}

@FlowPreview
suspend fun basicTest() {
    val weights = listOf(1.0, 2.0, 3.0)
    val neurons = listOf(SimpleInput(200), SimpleInput(300), SimpleInput(250))
    val input = neurons.toInput(weights)
    val nOut = FlowNeuron(input, 79.0)
    nOut.potentials.collect { println("Fired!") }
}

@FlowPreview
fun main() = runBlocking {
    basicTest()
}