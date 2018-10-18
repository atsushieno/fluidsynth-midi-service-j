package fluidsynth

import com.sun.jna.ptr.PointerByReference

class Settings : FluidsynthObject
{
    companion object {
        var library: FluidsynthLibrary = FluidsynthLibrary.INSTANCE
    }

    constructor()
        : super (library.new_fluid_settings (), true)

    constructor(handle : PointerByReference)
        : super (handle, false)

    override fun onClose()
    {
        library.delete_fluid_settings (getHandle())
    }

    fun getEntry (name : String) : SettingEntry
    {
        return Settings.SettingEntry(getHandle(), name)
    }

    class SettingEntry
    {
        internal constructor(handle : PointerByReference, name : String)
        {
            this.handle = handle
            this.name_ = name
        }

        private val handle : PointerByReference
        private val name_ : String

        private fun getName () : String {
            return name_
        }

        fun getType () : Int {
            return library.fluid_settings_get_type(handle, getName())
        }

        //ERROR
        //fun getHints () : Int/*FluidHint*/ {
        //    return library.fluid_settings_get_hints(handle, getName());
        //}

        /*ERROR
        fun getStringDefalut () : String {
            return library.fluid_settings_getstr_default(handle, getName());
        }
        */

        /*ERROR
        fun getStringValue () {

            val v : PointerByReference? = null;
            library.fluid_settings_getstr(handle, getName(), v);
            return v;
        }
        */
        fun setStringValue (v : String) {
            library.fluid_settings_setstr (handle, getName(), v)
        }

        class ValueRange<T> (min : T, max : T)
        {
        }

        /*ERROR
        fun getIntDefault () : Int
        {
            return library.fluid_settings_getint_default(handle, getName());
        }
        */

        /*ERROR
        fun getIntRange () : ValueRange<Int>
        {
            var min : IntByReference? = null;
            var max : IntByReference? = null;
            library.fluid_settings_getint_range (handle, getName(), min, max);
            return ValueRange(min!!.value, max!!.value);
        }
        */

        /*ERROR
        fun getIntValue () : Int {

            var v : IntByReference? = null;
            library.fluid_settings_getint (handle, getName(), v);
            return if (v == null) getIntDefault() else v.value;
        }
        */
        fun setIntValue (v : Int) {
            library.fluid_settings_setint (handle, getName(), v)
        }

        /*ERROR
        fun getDoubleDefault() : Double {
            return library.fluid_settings_getnum_default(handle, getName());
        }
        */

        /*ERROR
        fun getDoubleRange () : ValueRange<Double>
        {
            var min : DoubleByReference? = null;
            var max : DoubleByReference? = null;
            library.fluid_settings_getnum_range (handle, getName(), min, max);
            return ValueRange(min!!.value, max!!.value);
        }
        */

        /*ERROR
        fun getDoubleValue () : Double {

            var v : DoubleByReference? = null;
            library.fluid_settings_getnum (handle, getName(), v);
            return if (v == null) getDoubleDefault() else v.value;
        }
        */
        fun setDoubleValue (v : Double) {
            library.fluid_settings_setnum (handle, getName(), v)
        }

        /*
        fun ForeachOption (func : (String,String)->Unit)
        {
            val f : FluidsynthLibrary.fluid_settings_foreach_option_t = fun (d : Pointer, nm : Pointer, opt : Pointer) { func (nm, opt); return Pointer.NULL; };
            library.fluid_settings_foreach_option (handle, getName(), Pointer.NULL, f);
        }

        fun ForeachOption (Func<IntPtr,string,string,IntPtr> func, IntPtr data = default (IntPtr))
        {
            library.fluid_settings_foreach_option_t f = (d, nm, opt) => func (d, nm, opt);
            library.fluid_settings_foreach_option (handle, getName(), data, f);
        }

        fun Foreach (Action<string,FluidTypes> func)
        {
            library.fluid_settings_foreach_t f = (d, nm, t) => { func (nm, t); return IntPtr.Zero; };
            library.fluid_settings_foreach (handle, getName(), IntPtr.Zero, f);
        }

        fun Foreach (Func<IntPtr,string,FluidTypes,IntPtr> func, IntPtr data = default (IntPtr))
        {
            library.fluid_settings_foreach_t f = (d, nm, t) => func (d, nm, t);
            library.fluid_settings_foreach (handle, getName(), data, f);
        }
        */
    }
}

