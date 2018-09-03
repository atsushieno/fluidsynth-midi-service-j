package name.atsushieno.fluidsynthjna

class FluidsynthInteropException : Exception
{
    public constructor(message : String) : super (message) {}
    public constructor(message : String, innerException : Exception) : super (message, innerException) {}
}