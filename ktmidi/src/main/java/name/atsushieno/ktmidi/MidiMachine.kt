package name.atsushieno.ktmidi

class MidiMachine
{
    private val event_received_handlers = arrayListOf<OnMidiEventListener>()

    fun addOnEventReceivedListener(listener: OnMidiEventListener)
    {
        event_received_handlers.add(listener)
    }

    fun removeOnEventReceivedListener(listener: OnMidiEventListener)
    {
        event_received_handlers.remove(listener)
    }

    var channels = Array<MidiMachineChannel> (16, { i -> MidiMachineChannel() })

    fun processEvent (evt: MidiEvent)
    {
        when (evt.eventType) {
            MidiEvent.NOTE_ON->
            channels [evt.channel.toInt()].noteVelocity [evt.msb.toInt()] = evt.lsb

            MidiEvent.NOTE_OFF->
            channels [evt.channel.toInt()].noteVelocity [evt.msb.toInt()] = 0
            MidiEvent.PAF->
            channels [evt.channel.toInt()].pafVelocity [evt.msb.toInt()] = evt.lsb
            MidiEvent.CC-> {
                // FIXME: handle RPNs and NRPNs by DTE
                when (evt.msb) {
                    MidiCC.NRPN_MSB,
                    MidiCC.NRPN_LSB ->
                        channels[evt.channel.toInt()].dteTarget = DteTarget.NRPN
                    MidiCC.RPN_MSB,
                    MidiCC.RPN_LSB ->
                        channels[evt.channel.toInt()].dteTarget = DteTarget.RPN

                    MidiCC.DTE_MSB ->
                        channels[evt.channel.toInt()].processDte(evt.lsb, true)
                    MidiCC.DTE_LSB ->
                        channels[evt.channel.toInt()].processDte(evt.lsb, false)
                    MidiCC.DTE_INCREMENT ->
                        channels[evt.channel.toInt()].processDteIncrement()
                    MidiCC.DTE_DECREMENT ->
                        channels[evt.channel.toInt()].processDteDecrement()
                }
                channels[evt.channel.toInt()].controls[evt.msb.toInt()] = evt.lsb
            }
            MidiEvent.PROGRAM->
            channels [evt.channel.toInt()].program = evt.msb
            MidiEvent.CAF->
            channels [evt.channel.toInt()].caf = evt.msb
            MidiEvent.PITCH ->
            channels [evt.channel.toInt()].pitchbend = ((evt.msb.toInt() shl 7) + evt.lsb).toShort()
        }
        for (receiver in event_received_handlers)
            receiver.onEvent (evt)
    }
}

class MidiMachineChannel
{
    val noteVelocity = ByteArray(128)
    val pafVelocity = ByteArray(128)
    val controls = ByteArray(128)
    val rpns = ShortArray(128) // only 5 should be used though
    val nrpns = ShortArray(128)
    var program : Byte = 0
    var caf : Byte = 0
    var pitchbend : Short = 8192
    var dteTarget : DteTarget = DteTarget.RPN
    private var dte_target_value : Byte = 0

    val rpnTarget : Short
        get () = ((controls [MidiCC.RPN_MSB.toInt()].toInt() shl 7) + controls [MidiCC.RPN_LSB.toInt()]).toShort()


    fun processDte (value: Byte, isMsb: Boolean)
    {
        var arr : ShortArray
        when (dteTarget) {
            DteTarget.RPN-> {
                dte_target_value = controls[(if (isMsb) MidiCC.RPN_MSB else MidiCC.RPN_LSB).toInt()]
                arr = rpns
            }
            DteTarget.NRPN-> {
                dte_target_value = controls[(if (isMsb) MidiCC.NRPN_MSB else MidiCC.NRPN_LSB).toInt()]
                arr = nrpns
            }
        }
        var cur = arr [dte_target_value.toInt()].toInt()
        if (isMsb)
            arr [dte_target_value.toInt()] = (cur and 0x007F + ((value.toInt() and 0x7F) shl 7)).toShort()
        else
        arr [dte_target_value.toInt()] = (cur and 0x3FF0 + (value.toInt() and 0x7F)).toShort()
    }

    fun processDteIncrement ()
    {
        when (dteTarget) {
            DteTarget.RPN -> rpns [dte_target_value.toInt()]++
            DteTarget.NRPN -> nrpns [dte_target_value.toInt()]++
        }
    }

    fun processDteDecrement ()
    {
        when (dteTarget) {
            DteTarget.RPN -> rpns [dte_target_value.toInt()]--
            DteTarget.NRPN -> nrpns [dte_target_value.toInt()]--
        }
    }
}

enum class DteTarget
{
    RPN,
    NRPN
}
