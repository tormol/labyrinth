package tbm.util;

/**Parsing numbers from a stream*/
public class parseNum {
	private String spaces = null;
	private boolean negative = false;
	private boolean other_systems = false;
	public parseNum()
		{}
	public parseNum allow_negative_numbers(boolean allow) {
		this.negative = allow;
		return this;
	}
	public parseNum allow_other_number_systems(boolean allow) {
		this.other_systems = allow;
		return this;
	}
	public parseNum allow_spaces(String spaces) {
		if (spaces != null  &&  spaces.isEmpty())
			spaces = null;
		this.spaces = spaces;
		return this;
	}
	public parseNum no_spaces() {
		this.spaces = null;
		return this;
	}

	public <EX extends Throwable> long parse_long(CharSupplier<EX> ch) throws NumberFormatException, EX {
		if (negative)
			return parseNum.signed_long(ch, other_systems, spaces);
		else
			return parseNum.unsigned_long(ch, false, other_systems, spaces);
	}

	public <EX extends Throwable> int parse_int(CharSupplier<EX> ch) throws NumberFormatException, EX {
		if (negative)
			return parseNum.signed_int(ch, other_systems, spaces);
		else
			return parseNum.unsigned_int(ch, false, other_systems, spaces);
	}

	public <EX extends Throwable> short parse_short(CharSupplier<EX> ch) throws NumberFormatException, EX {
		if (negative)
			return parseNum.signed_short(ch, other_systems, spaces);
		else
			return parseNum.unsigned_short(ch, false, other_systems, spaces);
	}

	public <EX extends Throwable> byte parse_byte(CharSupplier<EX> ch) throws NumberFormatException, EX {
		if (negative)
			return parseNum.signed_byte(ch, other_systems, spaces);
		else
			return parseNum.unsigned_byte(ch, false, other_systems, spaces);
	}


	public static <EX extends Throwable> long signed_long(CharSupplier<EX> ch, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return parse_signed(ch, Long.SIZE, other_systems, spaces);
	}
	public static <EX extends Throwable> long unsigned_long(CharSupplier<EX> ch, boolean negative, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return parse(ch, ch.get(), Long.SIZE, false, negative, other_systems, spaces);
	}

	public static <EX extends Throwable> int signed_int(CharSupplier<EX> ch, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return (int)parse_signed(ch, Integer.SIZE, other_systems, spaces);
	}
	public static <EX extends Throwable> int unsigned_int(CharSupplier<EX> ch, boolean negative, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return (int)parse_unsigned(ch, Integer.SIZE, negative, other_systems, spaces);
	}

	public static <EX extends Throwable> short signed_short(CharSupplier<EX> ch, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return (short)parse_signed(ch, Short.SIZE, other_systems, spaces);
	}
	public static <EX extends Throwable> short unsigned_short(CharSupplier<EX> ch, boolean negative, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return (short)parse_unsigned(ch, Short.SIZE, negative, other_systems, spaces);
	}


	public static <EX extends Throwable> byte signed_byte(CharSupplier<EX> ch, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return (byte)parse_signed(ch, Byte.SIZE, other_systems, spaces);
	}
	public static <EX extends Throwable> byte unsigned_byte(CharSupplier<EX> ch, boolean negative, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return (byte)parse_unsigned(ch, Byte.SIZE, negative, other_systems, spaces);
	}


	private static <EX extends Throwable> int next(CharSupplier<EX> ch, String skip) throws EX {
		int c;
		do c = ch.get();
			while (skip.indexOf(c) != -1);
		return c;
	}

	private static <EX extends Throwable> long parse_signed(CharSupplier<EX> ch, int bits, boolean other_systems, String spaces) throws EX, NumberFormatException {
		int first = next(ch, spaces);
		if (first == '-')
			return parse(ch, ch.get(), bits, true, true, other_systems, spaces);
		return parse(ch, first, bits, true, false, other_systems, spaces);
	}
	private static <EX extends Throwable> long parse_unsigned(CharSupplier<EX> ch, int bits, boolean negative, boolean other_systems, String spaces) throws EX, NumberFormatException {
		return parse(ch, ch.get(), bits, false, negative, other_systems, spaces);
	}
	

	/**@param ch input stream
	 * @param c holds the first digit
	 * @param bits number of bits in the number, int because <Boxed>.SIZE is int
	 * @param signed should the first bit in bits be used for sign
	 * @param negative is this number negative
	 * @param other_systems are binary(0b), hexadecimal(0x) or octal(0o) numbers allowed?
	 * @param spaces a list of characters that wont stop parsing, but will be ignored. Cannot be null.
	 * detects base, compute max value and max digits
	 */
	private static <EX extends Throwable> long parse (
				CharSupplier<EX> ch, int c, int bits, boolean signed,
				boolean negative, boolean other_systems, String spaces
			) throws EX {
		byte base=10;
		byte digits=0;
		byte max_digits = -1;//not used with base 10;
		if (spaces.indexOf(c) != -1)
			c = next(ch, spaces);
		if (other_systems && c=='0')
			switch (c = next(ch, spaces)) {
				case'x':base =16;  break;
				case'b':base = 2;  break;
				case'o':base = 8;  break;
				default:digits=1;//a zero
			}

		long max_value;
		if (bits < 1)
			throw new IllegalArgumentException(bits+" bits doesn't make sense, bits must be between 2 and "+Long.SIZE);
		else if (bits < Long.SIZE)
			max_value = (1L << bits) -1;
		else if (bits == Long.SIZE)
			max_value = -1;//<<64==<<0
		else//if (bits > Long.SIZE)
			throw new IllegalArgumentException("Cannot store more than "+Long.SIZE+" bits.");

		if (base != 10) {
			c = ch.get();
			int base_bits = Integer.highestOneBit(base)-1;
			//round up http://stackoverflow.com/questions/7446710/how-to-round-up-integer-division-and-have-int-result-in-java
			max_digits = (byte) ((bits-1+base_bits)/base_bits);
		} else if (signed) {
			max_value >>= 1;
			if (negative)
				max_value++;
		}
		return do_parse(ch, c, base, negative, max_digits, digits, max_value, spaces);
	}


	private static <EX extends Throwable> long do_parse (
				CharSupplier<EX> ch, int c, byte base, boolean negative,
				byte max_digits, int digits, long max_value, String spaces
			) throws EX {
		long num=0;
		//when reading a unsigned long a too big value would be less than ~0, so comparing doesn't work
		//my workaround is to set this to true when num overflows and become negative, and check if num becomes positive again.
		boolean overflowed = false;
		while (true) {
			int n = digit(base, c);
			if (n == -1)
				if (spaces.indexOf(c) != -1) {
					c = ch.get();
					continue;
				} else
					break;
			digits++;
			num = num*base + n;
			c = ch.get();
			if (base != 10  &&  digits > max_digits)
				throw new NumberFormatException("Too many digits, "+max_digits+" is max.");
			if (num < 0)
				overflowed = true;
			//wrapped around from negative to positive, aka 64bit unsigned overflow
			else if (overflowed  ||  Long.compareUnsigned(num, max_value) > 0) {
				int bits = (int)Math.ceil( Math.log(max_value) / Math.log(2) );//ln n/ln 2=log2 n
				if (negative)
					throw new NumberFormatException(-num+" is too small for "+bits+" bits.");
				else
					throw new NumberFormatException(num+" is too big for "+bits+" bits.");
			}
		}
		if (digits == 0)
			if (base == 10)//no digits
				throw new NumberFormatException("Not a number");
			else//0xbo and then nothing
				throw new NumberFormatException("No digits");
		if (negative)
			num = -num;
		return num;
	}


	/**convert char to int
	 *@return -1 if not a digit*/
	private static int digit(int base, int c) {
		if (c >= '0'  &&  c <= '0'+base-1  &&  c<='9')
			return c-'0';
		c |= 0x20;//convert to lowercase
		if (base == 16  &&  c >= 'a'  &&  c <= 'f')
			return c+0xa-'a';
		return -1;
	}
}
