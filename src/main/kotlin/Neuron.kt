import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object ActionPotential

class Synapse(private val neuron: Firing, var weight: Double) {
    val vesicles: Flow<Double> = neuron.potentials.map { weight }
}

interface Firing {
    val potentials: Flow<ActionPotential>
    suspend fun fire()
}

class SimpleInput(private val _delay: Long) : Firing {
    override val potentials: Flow<ActionPotential> = flow {
        while (true) {
            emit(ActionPotential)
            delay(_delay)
        }
    }
    override suspend fun fire() {}
}

@FlowPreview
class Neuron(private val threshold: Double, private val synapses: List<Synapse>, private val refractoryPeriod: Long = 1,
private val name: String = "neuron") : Firing {
    var voltage: Double = 0.0
    private val _potentials = MutableSharedFlow<ActionPotential>(replay=0)
    override val potentials: SharedFlow<ActionPotential> = _potentials

    private suspend fun onSynapticInput(input: Double) {
        voltage += input
        println(voltage)
        if (voltage > threshold) {
            voltage = 0.0
            _potentials.emit(ActionPotential)
            delay(refractoryPeriod)
        }
    }

    override suspend fun fire() {
        synapses.asFlow().flatMapMerge { it.vesicles }.buffer().collect { onSynapticInput(it) }
    }
}

@FlowPreview
suspend fun basicTest() {
    val weights = listOf(1.0, 2.0, 3.0)
    val neurons = listOf(SimpleInput(20), SimpleInput(30), SimpleInput(25))
    val synapses: List<Synapse> = neurons.zip(weights) { n, w -> Synapse(n, w) }
    val neuron = Neuron(79.0, synapses)
    withTimeoutOrNull(5000) {
        launch { neuron.potentials.take(2).collect { println("Fired!") } }
        launch { neuron.fire() }
    }
    println("Done!")
}

@FlowPreview
suspend fun twoNeurons() {
    val weights = listOf(1.0, 2.0, 3.0)
    val neurons = listOf(SimpleInput(20), SimpleInput(30), SimpleInput(25))
    val synapses: List<Synapse> = neurons.zip(weights) { n, w -> Synapse(n, w) }
    val neuron1 = Neuron(79.0, synapses, name = "neuron1")
    val neuron2 = Neuron(63.0, synapses, name = "neuron2")
    coroutineScope {
        launch {
            neuron1.potentials.take(2).collect { println("Neuron1 fired!") }
        }
        launch {
            neuron2.potentials.take(2).collect { println("Neuron2 fired!") }
        }
        launch {
            neuron1.fire()
            neuron2.fire()
        }
    }
    println("Done!")
}

@FlowPreview
suspend fun twoOutputNeurons() {
        //L1
        val weights = listOf(1.0, 2.0, 3.0)
        val inputs = listOf(SimpleInput(20), SimpleInput(30), SimpleInput(25))
        val synapses: List<Synapse> = inputs.zip(weights) { n, w -> Synapse(n, w) }
        val neuronL1 = Neuron(5.0, synapses, name = "neuron1L1")
        //L2
        val neuron1L2 = Neuron(1.0, listOf(Synapse(neuronL1, 1.0)), name = "neuron1L2")
        val neuron2L2 = Neuron(2.0, listOf(Synapse(neuronL1, 1.0)), name = "neuron2L2")
        val neurons = listOf(neuronL1, neuron1L2, neuron2L2)
    withTimeoutOrNull(5000) {
        launch {
            neuronL1.potentials.collect { println("Neuron1L1 fired!") }
        }
        launch {
            neuron1L2.potentials.collect { println("Neuron1L2 fired!") }
        }
        launch {
            neuron2L2.potentials.collect { println("Neuron2L2 fired!") }
        }
        for (neuron in neurons) {
            launch { neuron.fire() }
        }
    }
    println("Done!")
}

@FlowPreview
fun main() = runBlocking {
    basicTest()
    twoNeurons()
    twoOutputNeurons()
    println("Test finished!")
}