package tbm.util;

/**Parsing numbers from a stream, with the option of accepting other number systems, or ignoring spaces, tabs or any characters you want.*/
public class parseNum {
	  //*******//
	 // flags //
	//*******//
	//private values are only partially implemented, 49152 combinations!

	//number systems
	/**numbers that start with a zero are octal, use with caution as it's annoying and unexpected.*/
	private static final int ZERO_OCT        = 0b0000_0000_0000_0001;
	/**if the number starts with 0b it's binary.*/
	public static final int  OPT_BIN        = 0b0000_0000_0000_0010;
	/**if the number starts with 0d it's decimal.*/
	public static final int  OPT_DEC        = 0b0000_0000_0000_0100;
	/**if the number starts with 0o it's octal.*/
	private static final int  OPT_OCT        = 0b0000_0000_0000_1000;
	/**if the number starts with 0b it's hexadecimal.*/
	public static final int  OPT_HEX        = 0b0000_0000_0001_0000;
	/**set the default radix, must be 2, 8, 10 or 16.*/
	private static final int DEFAULT_RADIX(int radix) throws IllegalArgumentException {switch (radix) {
		case  2: return                       0b0000_0000_0010_0000;
		case  8: return                       0b0000_0000_0100_0000;
		case 10: return                       0b0000_0000_0000_0000;
		case 16: return                       0b0000_0000_0110_0000;
		default: throw new IllegalArgumentException(String.valueOf(radix).concat(" is not a supported radix, must be 2, 8, 10 or 16."));
	}}

	//grouping characters
	/**Spaces can be used to divide digits into groups.*/
	public static final int SKIP_SPACE      = 0b0000_0000_1000_0000;
	/**_ can be used to divide digits into groups.*/
	public static final int SKIP_UNDERSCORE = 0b0000_0001_0000_0000;
	/**- can be used to divide digits into groups.*/
	public static final int SKIP_HYPEN      = 0b0000_0010_0000_0000;
	/**. can be used to divide digits into groups.*/
	public static final int SKIP_DOT        = 0b0000_0100_0000_0000;
	/**, can be used to divide digits into groups.*/
	public static final int SKIP_COMMA      = 0b0000_1000_0000_0000;
	//cannot force a skip interval as those are counted from the end.


	//ways to enter negative numbers
	/**use a minus sign to identify negative numbers*/
	public static final int MINUS          = 0b1100_0000_0000_0000;
	/**use a minus sign to identify negative decimal numbers*/
	public static final int MINUS_DEC      = 0b1000_0000_0000_0000;
	/**use a minus sign to identify negative non-decimal numbers*/
	public static final int MINUS_OTHER    = 0b0100_0000_0000_0000;
	/**Allow numbers to overflow into negative numbers.*/
	public static final int OVERFLOW       = 0b0011_0000_0000_0000;
	/**Allow decimal numbers to overflow into negative numbers.*/
	public static final int OVERFLOW_DEC   = 0b0010_0000_0000_0000;
	/**Allow non-decimalnumbers to overflow into negative numbers.*/
	public static final int OVERFLOW_OTHER = 0b0001_0000_0000_0000;

	////latter cases, hard to implement
	///**Hexadecimal letters and radix identifier must be uppercase.*/
	//public static final int  ONLY_UPPERCASE = 0b0001 << 16;
	///**Hexadecimal letters and radix identifier must be lowercase.*/
	//public static final int  ONLY_LOWERCASE = 0b0010 << 16;
	///**Hexadecimal letters and radix identifier cannot be bost uppercase and lowercase.*/
	//public static final int CONSISTENT_CASE = 0b0011 << 16;


	  //***********************//
	 // public static methods //
	//***********************//

	/**A good default: DEFAULT_radix(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW_OTHER*/
	public static final int ANY_FLAGS = DEFAULT_RADIX(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW_OTHER; 
	/***/
	public static <EX extends Throwable> long any_long(int flags, CharSupplier<EX> cs) throws EX, NumberFormatException, IllegalArgumentException {
		return (long)bits(Long.SIZE, flags, cs);
	}
	public static <EX extends Throwable> long any_long(int flags, char first_char, CharSupplier<EX> cs) throws EX, NumberFormatException, IllegalArgumentException {
		return (long)bits(Long.SIZE, flags, first_char, cs);
	}


	/**A good default: DEFAULT_radix(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW*/
	public static final int BITS_FLAGS = DEFAULT_RADIX(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW; 
	public static <EX extends Throwable> long bits(int bits, int flags, CharSupplier<EX> cs) throws EX, NumberFormatException {
		return bits(bits, flags, cs.fetch(), cs);
	}
	public static <EX extends Throwable> long bits(int bits, int flags, int first_char, CharSupplier<EX> cs) throws EX, NumberFormatException {
		if ( !isset(flags, OVERFLOW | MINUS))
			throw new IllegalArgumentException("Must set either OVERFLOW or MINUS, else there is no way to enter a signed negative number.");
		if ( !isset(flags, OVERFLOW_DEC | MINUS_DEC)  &&  (isset(flags, OPT_DEC) || start_radix(flags) == 10))
			throw new IllegalArgumentException("Must set either OVERFLOW_DEC or MINUS_DEC, else there is no way to enter a signed negative decimal number.");
		if ( !isset(flags, OVERFLOW_OTHER | MINUS_OTHER)  &&  (isset(flags, OPT_BIN|OPT_OCT|OPT_HEX|ZERO_OCT) || start_radix(flags) != 10))
			throw new IllegalArgumentException("Must set either OVERFLOW_OTHER or MINUS_OTHER, else there is no way to enter a signed negative non-decimal number.");

		short digits=0;
		byte radix = start_radix(flags);
		short max_digits = radix == 10 ? -1 : (short)(bits/radix + bits%radix);
		
		if (radix == 16  &&  isset(flags, OPT_BIN | OPT_DEC))
			throw new IllegalArgumentException("OPT_BIN and OPT_DEC is incompatible with DEFAULT_radix(16) since it's impossible to know if a b or d is a digit or means binary or decimal.");

		int c = next(flags, first_char, cs);
		if (c == '0') {
			c = next(flags, cs.fetch(), cs);
			byte new_radix = zero_first(radix, flags, c);
			if (new_radix != -1) {
				radix = new_radix;
				c = next(flags, cs.fetch(), cs);
			} else
				digits++;
		}

		if (bits < 1)
			throw new IllegalArgumentException(bits+" bits doesn't make sense, bits must be between 1 and "+Long.SIZE);
		if (bits > Long.SIZE)
			throw new IllegalArgumentException("Cannot store more than "+Long.SIZE+" bits.");
		long max_value = (1L << (bits-1)) -1;
		if ((radix != 10  &&  isset(flags, OVERFLOW_OTHER))
		 || (radix == 10  &&  isset(flags, OVERFLOW_DEC)))
			max_value = (max_value<<1)+1;

		if (radix != 10) {
			int radix_bits = Integer.highestOneBit(radix)-1;
			//round up http://stackoverflow.com/questions/7446710/how-to-round-up-integer-division-and-have-int-result-in-java
			max_digits = (byte) ((bits-1+radix_bits)/radix_bits);
		}

		return parse(bits, flags, first_char, cs);
	}

	/**A good default: DEFAULT_radix(10) MINUS*/
	public static final int RANGE_FLAGS = DEFAULT_RADIX(10) | MINUS; 
	public static <EX extends Throwable> long range_long(long min, long max, int flags, CharSupplier<EX> cs) throws EX, NumberFormatException, IllegalArgumentException {
		long n = min < 0
			? signed_long(ch, other_systems, spaces)
			: unsigned_long(ch, false, other_systems, spaces);
		if (n < min  ||  n > max)
			throw new NumberFormatException(n +" is outside the accepted range of ["+min+", "+max);
		return n;
	}
	
	public static <EX extends Throwable> long range_long(long min, long max, int flags, int first_char, CharSupplier<EX> cs) throws EX, NumberFormatException, IllegalArgumentException {
		byte radix = start_radix(flags);
		int c = next(flags, first_char, cs);

		if (Long.compareUnsigned(min, max) > 0)
			throw new IllegalArgumentException("min must be smaller or equal to max, but min="+min+" and max="+max);
		if (max<0 && min<0  && c != '-')
			throw new NumberFormatException("Number must start with a minus sign.");
		if (n < min  ||  n > max)
			throw new NumberFormatException(n +" is outside the accepted range of ["+min+", "+max);

		short digits=0;
		
		if (radix == 16  &&  isset(flags, OPT_BIN | OPT_DEC))
			throw new IllegalArgumentException("OPT_BIN and OPT_DEC is incompatible with DEFAULT_radix(16) since it's impossible to know if a b or d is a digit or means binary or decimal.");

		if (c == '0') {
			c = next(flags, cs.fetch(), cs);
			byte new_radix = zero_first(radix, flags, c);
			if (new_radix != -1) {
				radix = new_radix;
				c = next(flags, cs.fetch(), cs);
			} else
				digits++;
		}
		return n;
	}


	  //************************//
	 // private static methods //
	//************************//

	/**Is flag set in flags?
	 *@return (flags & flag) != 0*/
	private static boolean isset(int flags, int flag) {
		return (flags & flag) != 0;
	}

	/**Get next non-whitespace character.*/
	private static <EX extends Throwable> int next(int flags, int c, CharSupplier<EX> cs) throws EX {
		while ((c==' ' && isset(flags, SKIP_SPACE))
		    || (c=='_' && isset(flags, SKIP_UNDERSCORE))
		    || (c=='-' && isset(flags, SKIP_HYPEN))
		    || (c==',' && isset(flags, SKIP_COMMA))
		    || (c=='.' && isset(flags, SKIP_DOT)))
			c = cs.fetch();
		return c;
	}
	/**convert char to int
	 *@return -1 if not a digit*/
	private static int toNum(byte radix, int digit) {
		if (digit >= '0'  &&  digit <= '0'+radix-1  &&  digit<='9')
			return digit-'0';
		digit |= 0x20;//convert to lowercase
		if (radix == 16  &&  digit >= 'a'  &&  digit <= 'f')
			return digit+0xa-'a';
		return -1;
	}


	private static byte start_radix(int flags) {
		flags &= DEFAULT_RADIX(16); // both bits set
		if (flags == DEFAULT_RADIX(16)) return 16;
		if (flags == DEFAULT_RADIX(10)) return 10;
		if (flags == DEFAULT_RADIX( 8)) return  8;
		if (flags == DEFAULT_RADIX( 2)) return  2;
		throw new RuntimeException("BUG: DEFAULT_radix( is inconsistent.");
	}
	private static byte zero_first(byte radix, int flags, int next) {
		if (isset(flags,  OPT_HEX) && next=='x')             	return 16;
		if (isset(flags,  OPT_DEC) && next=='d')             	return 10;
		if (isset(flags,  OPT_OCT) && next=='o')             	return  8;
		if (isset(flags,  OPT_BIN) && next=='b')             	return  2;
		if (isset(flags, ZERO_OCT) && next>='0' && next<='7')	return  8;
		else                                                 	return -1;
	}
	/**@return false if minus sign is not supported, true if it is accepted and throws if not for the number system*/
	private static boolean handle_minus_sign(int flags, int radix) throws NumberFormatException {
		if ( !isset(flags, MINUS))
			return false;
		if (radix != 10  &&  isset(flags, MINUS_DEC))
			throw new NumberFormatException("cannot use minus sign with non-decimal numbers.");
		if (radix == 10  &&  isset(flags, MINUS_OTHER))
			throw new NumberFormatException("cannot use minus sign with decimal numbers.");
		return true;
	}


	/**detects radix, compute max value and max digits
	 *@param ch input stream
	 *@param c holds the first digit
	 *@param bits number of bits in the number, int because <Boxed>.SIZE is int
	 *@param can_overflow can negative numbers be entered as positive
	 *@param negative is this number negative
	 *@param other_systems are binary(0b), hexadecimal(0x) or octal(0o) numbers allowed?
	 *@param spaces a list of characters that won't stop parsing, but will be ignored. Cannot be null.
	 *@exception EX thrown by CharSupplier
	 *@throws NumberFormatException NaN/no digits, too big/small/long 
	 *@throws IllegalArgumentException if bits is not between 1 and Long.SIZE
	 */

	/**Do the actual parsing.
	 *@param cs character source
	 *@param c the first char, can't rewind CharSupplier
	 *@param radix which characters are digits?
	 *@param flags only used as parameter to next()
	 *@param minus is the number negative, doesn't affect parsing as parse() checked for minus sign.
	 *@param max_digits maximum number of digits, including leading zeroes, multiplying a number by more than two and make it hard to detect.
	 *@param digits number of digits already read.
	 *@param max_value max allowed value, should be negative.
	 *@param spaces a list of characters that won't stop parsing, but will be ignored. Cannot be null.
	 *@exception EX thrown by CharSupplier
	 *@throws NumberFormatException NaN/no digits, too big/small/long 
	 * @throws InternalException 
	 *///too many parameters, but I need them to make good error messages.
	private static <EX extends Throwable> long do_parse (
				byte radix, int flags, byte max_digits, long max_value,
				byte digits, int c, CharSupplier<EX> cs
			) throws EX, NumberFormatException, InternalException {
		long num=0;
		while (true) {
			int n = next(flags, c, cs);
			if (n == -1)
				break;
			digits++;
			long newnum = num*radix - n;
			c = cs.fetch();
			if (digits > max_digits)
				throw new InternalException(num, newnum, digits, false);
			if (Long.compareUnsigned(num, max_value) > 0  ||  Long.compareUnsigned(num, newnum) > 0)
				throw new InternalException(num, newnum, digits,  true);
			num = newnum;
		}
		if (digits == 0)
			throw new NumberFormatException("No digits");
		return num;
	}



	/**so exception messages use the correct radix and prepend 0[xob], and does unsigned correctly.*/
	private static String numToString(long num, byte radix, boolean negative) {
		StringBuilder out = new StringBuilder(18);
		if (negative)
			out.append('-');
		if (radix != 10) {
			out.append('0');
			switch (radix) {//gave up finding a formula, the switch is probably faster anyway
				case  2: out.append('b'); break;//02=0b00010->b=098=0b01100010	+00=0b0000->+00=0b00000
				case  8: out.append('o'); break;//16=0b10000->x=120=0b01111000	+14=0b1110->+22=0b10110
				case 16: out.append('x'); break;//08=0b01000->o=111=0b01101111	+06=0b0110->+13=0b01101
			}
		}
		out.append(Long.toUnsignedString(num, radix));
		return out.toString();
	}


	/**A way to pass state where the error occurred */
	private static class InternalException extends Exception {
		/**value when the error was detected*/protected final long oldnum, newnum;
		/***/protected final byte digits;
		protected final boolean tooBig;
		protected InternalException(long oldnum, long newnum, byte digits, boolean tooBig) {
			this.oldnum = oldnum;
			this.newnum = newnum;
			this.digits = digits;
			this.tooBig = tooBig;
		}
	}
}
