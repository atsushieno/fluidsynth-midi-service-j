package name.atsushieno.ktmidi

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

@kotlin.ExperimentalUnsignedTypes
class MidiMusicUnitTest {
    @Test
    fun getBpm() {
        Assert.assertEquals("120", 120, Math.round(MidiMetaType.getBpm(byteArrayOf(7, 0xA1.toByte(), 0x20))))
        Assert.assertEquals("140", 140, Math.round(MidiMetaType.getBpm(byteArrayOf(6, 0x8A.toByte(), 0xB1.toByte()))))
    }
}