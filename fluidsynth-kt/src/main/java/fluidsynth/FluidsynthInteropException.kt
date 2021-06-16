package fluidsynth

class FluidsynthInteropException : Exception
{
    constructor(message : String) : super (message)
    constructor(message : String, innerException : Exception) : super (message, innerException)
}