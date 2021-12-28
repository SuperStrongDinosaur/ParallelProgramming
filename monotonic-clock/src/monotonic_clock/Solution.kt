package monotonic_clock

/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 */
class Solution : MonotonicClock {
    private var c11 by RegularInt(0)
    private var c12 by RegularInt(0)
    private var c3 by RegularInt(0)
    private var c21 by RegularInt(0)
    private var c22 by RegularInt(0)

    override fun write(time: Time) {
        c21 = time.d1
        c22 = time.d2
        c3 = time.d3
        c12 = c22
        c11 = c21
    }

    override fun read(): Time {
        val v1 = c11
        val u1 = c12
        val w = c3
        val u2 = c22
        val v2 = c21

        if(v1 == v2 && u1 == u2)
            return Time(v1, u1, w)
        else if(v1 == v2)
            return Time(v2, u2, 0)
        else return Time(v2, 0, 0)
    }
}