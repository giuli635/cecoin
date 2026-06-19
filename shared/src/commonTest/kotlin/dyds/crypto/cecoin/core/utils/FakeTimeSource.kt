package dyds.crypto.cecoin.core.utils

class FakeTimeProvider(private var currentMillis: Long = 0L) : () -> Long {

    override fun invoke(): Long = currentMillis

    fun advanceBy(millis: Long) {
        currentMillis += millis
    }
}
