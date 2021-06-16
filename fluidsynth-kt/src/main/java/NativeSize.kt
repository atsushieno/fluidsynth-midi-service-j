// workaround for missing type found in JNAerated code.
package com.ochafik.lang.jnaerator.runtime;

import com.sun.jna.IntegerType
import com.sun.jna.Native

class NativeSize : IntegerType {
    override fun toByte(): Byte {
        return SIZE.toByte()
    }

    override fun toChar(): Char {
        return SIZE.toChar()
    }

    override fun toShort(): Short {
        return SIZE.toShort()
    }

    companion object {
        var SIZE = Native.SIZE_T_SIZE
    }

    public constructor() : this (0){
    }

    public constructor(value: Long = 0) : super (SIZE, value) {
    }
}
