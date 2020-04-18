package io.horizontalsystems.bankwallet

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.common.base.Stopwatch
import io.horizontalsystems.groestlcoinkit.GroestlHasher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        //assertEquals("io.horizontalsystems.bankwallet", appContext.packageName)
    }

    @Test
    fun determineSpeed() {
        val bytes = ByteArray(32)

        val watch = Stopwatch.createStarted()

        for(i in 0..10000) {
            GroestlHasher().groestldNative(bytes, 0, bytes.size)
        }
        watch.stop()
        println("stopwatch, native: $watch")

        watch.reset()
        watch.start()
        for(i in 0..10000) {
            GroestlHasher().groestldKotlin(bytes)
        }
        watch.stop()
                println("stopwatch, native: $watch")

    }
}
