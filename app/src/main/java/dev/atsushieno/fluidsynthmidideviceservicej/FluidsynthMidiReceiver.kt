package dev.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.media.midi.MidiReceiver
import android.util.Log
import dev.atsushieno.ktmidi.*
import dev.atsushieno.fluidsynth.androidextensions.AndroidLogger
import dev.atsushieno.fluidsynth.*
import dev.atsushieno.fluidsynth.androidextensions.AndroidNativeAssetSoundFontLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.experimental.and
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

internal fun Byte.toUnsigned() = if (this < 0) 256 + this else this.toInt()

class FluidsynthMidiReceiver (val service: Context) : MidiReceiver()
{
    private val settings: Settings
    private val syn: Synth
    private val adriver: AudioDriver
    private val asset_sfloader: SoundFontLoader

    private var midiProtocol = 1
    private var is_disposed = false

    init {
        System.setProperty ("jna.nosys", "false") // https://github.com/java-native-access/jna/issues/384#issuecomment-441405266
        AndroidLogger.installAndroidLogger()

        val am = ApplicationModel(service)
        settings = Settings ()
        settings.getEntry (ConfigurationKeys.SynthThreadSafeApi).setIntValue (0)
        settings.getEntry (ConfigurationKeys.SynthGain).setDoubleValue (am.audioGainPercentage / 100.0) // See https://github.com/atsushieno/fluidsynth-midi-service-j/issues/7
        //settings.getEntry (ConfigurationKeys.AudioDriver).setStringValue ("opensles")
        //settings.getEntry (ConfigurationKeys.AudioSampleFormat).setStringValue ("float")
        settings.getEntry ("audio.oboe.sharing-mode").setStringValue(if (am.audioExclusiveUse) "Exclusive" else "Shared")
        settings.getEntry ("audio.oboe.performance-mode").setStringValue(am.performanceMode)
        settings.getEntry (ConfigurationKeys.SynthSampleRate).setDoubleValue (am.sampleRate.toDouble())
        settings.getEntry (ConfigurationKeys.AudioPeriodSize).setIntValue (am.framesPerBuffer)
        //settings.getEntry (ConfigurationKeys.AudioOboeErrorRecoveryMode).setStringValue ("Stop")

        // We should be able to use this alternatively, but it still has some issue that callbacks are reset in the middle, more GC pinning is likely required.
        //asset_sfloader = AndroidAssetSoundFontLoader(settings, context.assets)
        asset_sfloader = AndroidNativeAssetSoundFontLoader(settings, service.assets)
        syn = Synth (settings)
        syn.handleError = fun (errorCode: Int, wrapperError: String, nativeError: String) : Boolean {
            Log.d("FluidsynthMidiService", "$wrapperError (error code $errorCode: $nativeError)")
            when (wrapperError) {
                "noteoff operation failed" -> return true
                else -> return false
            }
        }
        syn.addSoundFontLoader (asset_sfloader)

        for (sf in am.soundFonts)
            syn.loadSoundFont (sf, false)

        adriver = AudioDriver (syn.getSettings(), syn)
        syn.systemReset()
    }

    fun isDisposed() : Boolean{
        return is_disposed
    }

    fun dispose()
    {
        //asset_sfloader.close ()
        adriver.close ()
        syn.close ()
        settings.close ()
        is_disposed = true
    }

    @OptIn(ExperimentalTime::class)
    override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
        if (msg == null)
            throw IllegalArgumentException ("null msg")
        if (timestamp == 0L)
            sendImmediate(msg, offset, count)
        else {
            val time = TimeSource.Monotonic.markNow()
            runBlocking {
                delay(timestamp / 1000 - time.elapsedNow().inWholeMicroseconds)
                sendImmediate(msg, offset, count)
            }
        }
    }

    private fun sendImmediate(msg: ByteArray, offset: Int, count: Int) {
        if (midiProtocol == 2)
            sendMidi2Immediate(msg, offset, count)
        else
            sendMidi1Immediate(0, msg, offset, count)
    }

    private fun sendMidi1Immediate(group: Byte, msg: ByteArray, offset: Int, count: Int) {
        var off = offset
        var c = count
        var runningStatus = 0
        while (c > 0) {
            var stat = msg[off].toUnsigned()
            if (stat < 0x80) {
                stat = runningStatus
            } else {
                off++
                c--
            }
            runningStatus = stat
            val ch = stat and 0x0F
            when (stat and 0xF0) {
                0x80 -> syn.noteOff(ch, msg[off].toUnsigned())
                0x90 -> {
                    if (msg[off + 1].toInt() == 0)
                        syn.noteOff(ch, msg[off].toUnsigned())
                    else
                        syn.noteOn(ch, msg[off].toUnsigned(), msg[off + 1].toUnsigned())
                }
                0xA0 -> {
                    // No PAf in fluidsynth?
                }
                0xB0 -> syn.cc(ch, msg[off].toUnsigned(), msg[off + 1].toUnsigned())
                0xC0 -> syn.programChange(ch, msg[off].toUnsigned())
                0xD0 -> syn.channelPressure(ch, msg[off].toUnsigned())
                0xE0 -> syn.pitchBend(ch, msg[off].toUnsigned() + msg[off + 1].toUnsigned() * 0x80)
                0xF0 -> {
                    if (stat == 0xF0) { // sysex
                        val idx = msg.drop(off).indexOf(0xF7.toByte())
                        val sysex = msg.copyOfRange(off, off + idx)
                        syn.sysex(sysex, null)
                        if (sysex[0] == 0x7E.toByte() && sysex[1] == 0x7F.toByte() && sysex[2] == 0x0D.toByte() &&
                            sysex[3] == 0x12.toByte() && sysex[4] == 1.toByte()) {
                            // we don't check the rest (Source MUID / Destination MUID / Authority Level)
                            // as it is obvious that this MIDI receiver is the ultimate destination.
                            midiProtocol = (sysex[14] and 3).toUnsigned()
                        }
                    }
                }
            }
            when (stat and 0xF0) {
                0xC0,0xD0 -> {
                    off++
                    c--
                }
                0xF0 -> {
                    off += c - 1
                    c = 0
                }
                else -> {
                    off += 2
                    c -= 2
                }
            }
        }
    }

    private fun sendMidi2Immediate(msg: ByteArray, offset: Int, count: Int) {
        for (ump in Ump.fromBytes(msg, offset, count)) {
            when (ump.messageType) {
                MidiMessageType.MIDI1 -> {
                    val channel = ump.group * 16 + ump.channelInGroup
                    when (ump.eventType) {
                        MidiChannelStatus.NOTE_OFF -> syn.noteOff(channel, ump.midi1Note)
                        MidiChannelStatus.NOTE_ON -> syn.noteOn(channel, ump.midi1Note, ump.midi1Velocity)
                        MidiChannelStatus.PAF -> {} // no PAf in Fluidsynth?
                        MidiChannelStatus.CC -> syn.cc(channel, ump.midi1CCIndex, ump.midi1CCData)
                        MidiChannelStatus.PROGRAM -> syn.programChange(channel, ump.midi1Program)
                        MidiChannelStatus.CAF -> syn.channelPressure(channel, ump.midi1CAf)
                        MidiChannelStatus.PITCH_BEND -> syn.pitchBend(channel, ump.midi1PitchBendData)
                    }
                }
                MidiMessageType.SYSEX7 -> {
                    // FIXME: send sysex7
                }
                MidiMessageType.MIDI2 -> {
                    val channel = ump.group * 16 + ump.channelInGroup
                    when (ump.eventType) {
                        MidiChannelStatus.NOTE_OFF -> syn.noteOff(channel, ump.midi2Note)
                        MidiChannelStatus.NOTE_ON -> syn.noteOn2(channel, ump.midi2Note, ump.midi2Velocity16)
                        MidiChannelStatus.PAF -> {} // no PAf in Fluidsynth?
                        MidiChannelStatus.CC -> syn.cc(channel, ump.midi2CCIndex, ump.midi2CCData.toInt())
                        MidiChannelStatus.PROGRAM -> {
                            syn.cc(channel, MidiCC.BANK_SELECT, ump.midi2ProgramBankMsb)
                            syn.cc(channel, MidiCC.BANK_SELECT_LSB, ump.midi2ProgramBankLsb)
                            syn.programChange(channel, ump.midi2ProgramProgram)
                        }
                        MidiChannelStatus.CAF -> syn.channelPressure(channel, ump.midi2CAf.toInt())
                        MidiChannelStatus.PITCH_BEND -> syn.pitchBend(channel, ump.midi2PitchBendData.toInt())
                        MidiChannelStatus.RPN -> {
                            syn.cc(channel, MidiCC.RPN_MSB, ump.midi2RpnMsb)
                            syn.cc(channel, MidiCC.RPN_LSB, ump.midi2RpnLsb)
                            syn.cc(channel, MidiCC.DTE_MSB, (ump.midi2RpnData shr 25).toInt())
                            syn.cc(channel, MidiCC.DTE_LSB, (ump.midi2RpnData shr 18).toInt() and 0x7F)
                        }
                        MidiChannelStatus.NRPN -> {
                            syn.cc(channel, MidiCC.NRPN_MSB, ump.midi2RpnMsb)
                            syn.cc(channel, MidiCC.NRPN_LSB, ump.midi2RpnLsb)
                            syn.cc(channel, MidiCC.DTE_MSB, (ump.midi2RpnData shr 25).toInt())
                            syn.cc(channel, MidiCC.DTE_LSB, (ump.midi2RpnData shr 18).toInt() and 0x7F)
                        }
                        MidiChannelStatus.RELATIVE_RPN, MidiChannelStatus.RELATIVE_NRPN -> {} // FIXME: implement
                        MidiChannelStatus.PER_NOTE_ACC,
                        MidiChannelStatus.PER_NOTE_RCC,
                        MidiChannelStatus.PER_NOTE_MANAGEMENT,
                        MidiChannelStatus.PER_NOTE_PITCH_BEND -> {} // not supported
                    }
                }
                MidiMessageType.SYSEX8_MDS -> {
                    // FIXME: send sysex8/MDS ?
                }
            }
        }
    }
}