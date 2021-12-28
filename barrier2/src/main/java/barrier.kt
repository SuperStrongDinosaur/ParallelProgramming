import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class Barrier(private var size: Int) {
    private var count: AtomicInteger = AtomicInteger(size)
    private var sum: AtomicInteger = AtomicInteger(0)
    private var sum2: AtomicInteger = AtomicInteger(0)
    private var sense = false

    private var threadSense: ThreadLocal<Boolean> = object : ThreadLocal<Boolean>() {
        override fun initialValue(): Boolean {
            return !sense
        }
    }

    fun waitBarrier(value: Int): Int {
        val mySense = threadSense.get()
        sum.addAndGet(value)
        val position = count.getAndDecrement()
        if (position == 1) {
            sum2.set(sum.getAndSet(0))
            count.set(size)
            sense = mySense
        } else {
            while (sense != mySense){ Thread.sleep(0) }
        }
        threadSense.set(!mySense)
        return sum2.get()
    }
}

class test(var barrier: Barrier, var i: Int): Callable<Int> {
    override fun call(): Int {
        return barrier.waitBarrier(i)
    }
}

class test2(var barrier: Barrier, val barrierSize: Int): Runnable {
    override fun run() {
        for(i in 0..1000000) {
            val res = barrier.waitBarrier(i)
            if(res != i * barrierSize)
                println("Error!")
        }
    }
}

fun main() {
    val barrierSize = 4
    val barrier = Barrier(barrierSize)
    for(i in 0 until barrierSize) {
        val thread = Thread(test2(barrier, barrierSize))
        thread.start()
    }
}