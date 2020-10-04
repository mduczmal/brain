import kotlin.random.Random

class Synapse(val inputNeuron: Neuron, val outputNeuron: Neuron) {
    val weight: Double = weightInit()
    fun activate() {
        outputNeuron.receive(weight)
    }
    private fun weightInit(): Double {
        return Random.nextDouble()
    }
}