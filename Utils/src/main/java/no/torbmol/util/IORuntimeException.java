package no.torbmol.util;
import java.io.IOException;
/**Useful when you need to throw from a method thatt cannot throw a checked exception.
 *Sets message, cause and stack trace to that of the parameter*/
public class IORuntimeException extends RuntimeException {
	public final IOException original;
	/**@param e the original exception.*/
	public IORuntimeException(IOException e) {
		super(e.getMessage(), e.getCause());
		setStackTrace(e.getStackTrace());
		original = e;
	}
	public void rethrow() throws IOException {
		throw original;
	}
	private static final long serialVersionUID = 1L;
}
