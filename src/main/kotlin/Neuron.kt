import kotlinx.coroutines.delay
import java.util.concurrent.atomic.DoubleAdder
import kotlin.random.Random

private const val DELAY = 400L
        
class Neuron(private val threshold: Double) {
    private val soma: DoubleAdder = DoubleAdder()
    var voltage: Double = init()
    private val synapses: MutableList<Synapse> = mutableListOf()
    private var stopped = false
    
    fun receive(activation: Double) {
        soma.add(activation)
    }
    
    fun connectTo(other: Neuron) {
        synapses.add(Synapse(this, other))
    }
    suspend fun run() {
        while(!stopped) {
            update()
            delay(DELAY)
        }
    }

    fun stop() {
        stopped = true
    }

    private fun init() = Random.nextDouble()
    private fun baseline() = -0.5
    private fun decay(value: Double): Double = value * 0.9
    
    private fun update() {
        val sum = soma.sum()
        soma.add(-sum)
        voltage = decay(voltage) + sum
        if (voltage > threshold) {
            voltage = baseline()
            fire()
        }
    }
    
    private fun fire() {
        for (synapse in synapses) {
            synapse.activate()
        }
    }
}





