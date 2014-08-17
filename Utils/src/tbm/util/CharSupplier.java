package tbm.util;
/**For methods that read an unknow number of chars*/
public interface CharSupplier<EX extends Throwable> {
	/**int to allow -1 errors*/
	int get() throws EX;
}
