package no.torbmol.util;
@SuppressWarnings("serial")
/**This exception cannot be created and therefore never be raised.
 *Useful when you're extending/implementing something that allow generic checked exceptions, but you don't need it.*/
public final class WillNeverThrowException extends RuntimeException	{
	private WillNeverThrowException()
		{}
}
