package name.atsushieno.fluidsynthjna

import com.sun.jna.Pointer
import com.sun.jna.ptr.DoubleByReference
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import name.atsushieno.fluidsynth.FluidsynthLibrary

public class Settings : FluidsynthObject
{
    companion object {
        var library: FluidsynthLibrary = FluidsynthLibrary.INSTANCE;
    }
    
    public constructor()
        : super (library.new_fluid_settings (), true)
    {
    }

    constructor(handle : PointerByReference)
        : super (handle, false)
    {
    }

    override fun onClose()
    {
        library.delete_fluid_settings (getHandle());
    }

    public fun getEntry (name : String) : SettingEntry
    {
        return SettingEntry (getHandle(), name);
    }

    public class SettingEntry
    {
        internal constructor(handle : PointerByReference, name : String)
        {
            this.handle = handle;
            this.name_ = name;
        }

        val handle : PointerByReference;
        val name_ : String;

        public fun getName () : String {
            return name_;
        }

        public fun getType () : Int {
            return library.fluid_settings_get_type(handle, getName());
        }

        //ERROR
        //public fun getHints () : Int/*FluidHint*/ {
        //    return library.fluid_settings_get_hints(handle, getName());
        //}

        /*ERROR
        public fun getStringDefalut () : String {
            return library.fluid_settings_getstr_default(handle, getName());
        }
        */

        /*ERROR
        public fun getStringValue () {

            val v : PointerByReference? = null;
            library.fluid_settings_getstr(handle, getName(), v);
            return v;
        }
        */
        public fun setStringValue (v : String) {
            library.fluid_settings_setstr (handle, getName(), v);
        }

        public class ValueRange<T> (min : T, max : T)
        {
        }

        /*ERROR
        public fun getIntDefault () : Int
        {
            return library.fluid_settings_getint_default(handle, getName());
        }
        */

        /*ERROR
        public fun getIntRange () : ValueRange<Int>
        {
            var min : IntByReference? = null;
            var max : IntByReference? = null;
            library.fluid_settings_getint_range (handle, getName(), min, max);
            return ValueRange(min!!.value, max!!.value);
        }
        */

        /*ERROR
        public fun getIntValue () : Int {

            var v : IntByReference? = null;
            library.fluid_settings_getint (handle, getName(), v);
            return if (v == null) getIntDefault() else v.value;
        }
        */
        public fun setIntValue (v : Int) {
            library.fluid_settings_setint (handle, getName(), v);
        }

        /*ERROR
        public fun getDoubleDefault() : Double {
            return library.fluid_settings_getnum_default(handle, getName());
        }
        */

        /*ERROR
        public fun getDoubleRange () : ValueRange<Double>
        {
            var min : DoubleByReference? = null;
            var max : DoubleByReference? = null;
            library.fluid_settings_getnum_range (handle, getName(), min, max);
            return ValueRange(min!!.value, max!!.value);
        }
        */

        /*ERROR
        public fun getDoubleValue () : Double {

            var v : DoubleByReference? = null;
            library.fluid_settings_getnum (handle, getName(), v);
            return if (v == null) getDoubleDefault() else v.value;
        }
        */
        public fun setDoubleValue (v : Double) {
            library.fluid_settings_setnum (handle, getName(), v);
        }

        /*
        public fun ForeachOption (func : (String,String)->Unit)
        {
            val f : FluidsynthLibrary.fluid_settings_foreach_option_t = fun (d : Pointer, nm : Pointer, opt : Pointer) { func (nm, opt); return Pointer.NULL; };
            library.fluid_settings_foreach_option (handle, getName(), Pointer.NULL, f);
        }

        public fun ForeachOption (Func<IntPtr,string,string,IntPtr> func, IntPtr data = default (IntPtr))
        {
            library.fluid_settings_foreach_option_t f = (d, nm, opt) => func (d, nm, opt);
            library.fluid_settings_foreach_option (handle, getName(), data, f);
        }

        public fun Foreach (Action<string,FluidTypes> func)
        {
            library.fluid_settings_foreach_t f = (d, nm, t) => { func (nm, t); return IntPtr.Zero; };
            library.fluid_settings_foreach (handle, getName(), IntPtr.Zero, f);
        }

        public fun Foreach (Func<IntPtr,string,FluidTypes,IntPtr> func, IntPtr data = default (IntPtr))
        {
            library.fluid_settings_foreach_t f = (d, nm, t) => func (d, nm, t);
            library.fluid_settings_foreach (handle, getName(), data, f);
        }
        */
    }
}

