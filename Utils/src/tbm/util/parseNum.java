package tbm.util;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**Parsing numbers from a stream, with the option of accepting other number systems, or ignoring spaces, tabs or any characters you want.*/
public class parseNum {
  //*******//
 // flags //
//*******//
	//private values are only partially implemented, 49152 combinations!

	//number systems
	/**numbers that start with a zero are octal, use with caution as it's annoying and unexpected.*/
	public static final int ZERO_OCT        = 0b0000_0000_0000_0001;
	/**if the number starts with 0b it's binary.*/
	public static final int  OPT_BIN        = 0b0000_0000_0000_0010;
	/**if the number starts with 0d it's decimal.*/
	public static final int  OPT_DEC        = 0b0000_0000_0000_0100;
	/**if the number starts with 0o it's octal.*/
	public static final int  OPT_OCT        = 0b0000_0000_0000_1000;
	/**if the number starts with 0b it's hexadecimal.*/
	public static final int  OPT_HEX        = 0b0000_0000_0001_0000;
	/**set the default radix, must be 2, 8, 10 or 16.*/
	public static final int RADIX(int radix) throws IllegalArgumentException {switch (radix) {
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

	//usage defaults
	/**RADIX(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW*/
	public static final int BITS_FLAGS = RADIX(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW; 
	/**RADIX(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW_OTHER*/
	public static final int ANY_FLAGS = RADIX(10) | OPT_HEX | OPT_DEC | OPT_OCT | OPT_BIN | MINUS_DEC | OVERFLOW_OTHER; 
	/**A good default: RADIX(10) MINUS*/
	public static final int RANGE_FLAGS = RADIX(10) | MINUS; 


//static utility methods

	/**Get the name of a certain radix, eg "hexadecimal" for 16. or "base "+radix if unknown.*/
	public static String radixToString(int radix) {switch (radix) {
		case 16: return "hexadecimal";
		case 10: return "decimal";
		case  8: return "octal";
		case  3: return "ternary";//why not?
		case  2: return "binary";
		case  1: return "unary";//why not?
		default: return "base "+radix; 
	}}

	/**16->'x' 10->'d' 8->'o' 2->'b' else -1*/
	public static int radixToChar(int radix) {switch (radix) {
		case 16: return 'x'; //16=0b10000->x=120=0b01111000	+14=0b1110->+22=0b10110
		case 10: return 'd'; //10=0b01010->d=100=0b01100100 +02=0b0010    what?
		case  8: return 'o'; //08=0b01000->o=111=0b01101111	+06=0b0110->+13=0b01101
		case  2: return 'b'; //02=0b00010->b=098=0b01100010	+00=0b0000->+00=0b00000
		default: return -1 ; //gave up finding a formula, switch might be faster anyway.
	}}



//constructors

	/**If you are only using the object once, this is shorter than new NumParser<SomeException>(flags, cs)
	 *@return {@code new NumParser(flags, cs);}
	 *@param reader cannot be null.*/
	public static parseNum with(int flags, Reader reader) {
		return new parseNum(flags, reader);
	}

	/**@throws IllegalArgumentException if start radix is 16 and OPT_BIN or OPT_DEC is set*/
	public parseNum(int flags, Reader reader) throws IllegalArgumentException {
		this.reader = Objects.requireNonNull(reader);
		this.c = NO_CHAR;
		newFlags(flags);
	}



	//fields are not final to allow reuse
	/**need to fetch a new one*/
	protected static final int NO_CHAR = -2;
	protected static final int END_CHAR = -1;
	protected int flags;
	/**a one-element pushback buffer, use next()*/
	protected int c;
	/**digit supplier*/
	private Reader reader;

	//those fields must be initialized before parse() is called
	/**radix of the last parsed number.*/
	public byte radix;
	/**digits of the last parsed number.*/
	public byte digits;
	/**was the last number negative?*/
	public boolean negative;


  //****************//
 // public methods //
//****************//

	/**Set the first char
	 *@return {@code this}*/
	public parseNum first_char(char first) {
		c = first;
		return this;
	}

	/**update {@code this.flags} and validate the combinations.
	 * (field is updated even if there is errors).
	 *@return {@code this}*///avoiding "set" as that is also the opposite of clear
	public parseNum newFlags(int flags) throws IllegalArgumentException {
		this.flags = flags;
		int radix = start_radix();
		if (radix == 16  &&  isset(OPT_BIN | OPT_DEC))
			throw new IllegalArgumentException("OPT_BIN and OPT_DEC is incompatible with DEFAULT_radix(16) since it's impossible to know if a b or d is a digit or means binary or decimal.");
		if ( !isset(OVERFLOW | MINUS))
			throw new IllegalArgumentException("Must set either OVERFLOW or MINUS, else there is no way to enter a signed negative number.");
		if ( !isset(OVERFLOW_DEC | MINUS_DEC)  &&  (isset(OPT_DEC) || radix == 10))
			throw new IllegalArgumentException("Must set either OVERFLOW_DEC or MINUS_DEC, else there is no way to enter a signed negative decimal number.");
		if ( !isset(OVERFLOW_OTHER | MINUS_OTHER)  &&  (isset(OPT_BIN|OPT_OCT|OPT_HEX|ZERO_OCT) || radix != 10))
			throw new IllegalArgumentException("Must set either OVERFLOW_OTHER or MINUS_OTHER, else there is no way to enter a signed negative non-decimal number.");
		return this;
	}

	/**is a flag (or any of a combination) set?
	 *@return {@code (flags & flag) != 0}*/
	public final boolean isset(int flag) {
		return (flags & flag) != 0;
	}

	/**parse a number with the given number of bits*/
	public long bits(int bits) throws IOException, NumberFormatException, IllegalArgumentException {
		radix = start_radix();
		negative = false;
		digits = 0;

		if (bits < 1)
			throw new IllegalArgumentException(bits+" bits doesn't make sense, bits must be between 1 and "+Long.SIZE);
		if (bits > Long.SIZE)
			throw new IllegalArgumentException("Cannot store more than "+Long.SIZE+" bits.");

		if ((c = next())  ==  '-') {
			negative = true;
			c = NO_CHAR;
		}
		if ((c = next())  ==  '0') {
			radix = zero_first(radix, c);
			c = NO_CHAR;
		}

		byte bits_per_digit = bits_per_digit();
		byte max_digits = (byte)(bits/bits_per_digit + bits%bits_per_digit);
		long max_value = (1L << (bits-1)) -1;
		if ((radix != 10  &&  isset(OVERFLOW_OTHER))
		 || (radix == 10  &&  isset(OVERFLOW_DEC)))
			max_value = (max_value<<1) | 1;

		if (negative) {//handle minus sign now that I know the radix.
			if ( !isset(MINUS))
				throw new NumberFormatException("cannot use minus sign.");
			if (radix != 10  &&  !isset(MINUS_OTHER))
				throw new NumberFormatException("cannot use minus sign with non-decimal numbers.");
			if (radix == 10  &&  !isset(MINUS_DEC))
				throw new NumberFormatException("cannot use minus sign with decimal numbers.");
			max_value++;
		}

		return parse(max_digits, max_value);
	}


	public final int range(int min, int max) throws IOException, NumberFormatException, IllegalArgumentException {
		return (int)range((long)min, (long)max);
	}
	public long range(long min, long max) throws IOException, NumberFormatException, IllegalArgumentException {
		radix = start_radix();
		negative = false;
		int c = next();

		if (min > max)
			throw new IllegalArgumentException("min must be smaller or equal to max, but min="+min+" and max="+max);
		if (max<0  && c != '-')
			
		if (c == '-') {
			if (min >= 0)
				throw new NumberFormatException("Number cannot be negative.");
			negative = true;
			max = min;
		} else if (max < 0)
			throw new NumberFormatException("Number must start with a minus sign.");

		return parse(Integer.MAX_VALUE, max);//infinite digits
	}



  //**************************//
 // private instance methods //
//**************************//

	/**Get next non-whitespace character and set this.c to NO_CHAR.*/
	protected int next() throws IOException {
		int c = this.c;
		while ( c==NO_CHAR
		    || (c==' ' && isset(SKIP_SPACE))
		    || (c=='_' && isset(SKIP_UNDERSCORE))
		    || (c=='-' && isset(SKIP_HYPEN))
		    || (c==',' && isset(SKIP_COMMA))
		    || (c=='.' && isset(SKIP_DOT)))
			c = reader.read();
		this.c = NO_CHAR;
		return c;
	}

	/**convert char to int
	 *@return -1 if not a digit*/
	protected int toNum(int digit) {
		if (digit >= '0'  &&  digit <= '0'+radix-1  &&  digit<='9')
			return digit-'0';
		digit |= 0x20;//convert to lowercase
		if (radix == 16  &&  digit >= 'a'  &&  digit <= 'f')
			return digit+0xa-'a';
		return -1;
	}

	/**get initial radix from flags*/
	protected final byte start_radix() {
		int flags = this.flags & RADIX(16); // both bits set
		if (flags == RADIX(16)) return 16;
		if (flags == RADIX(10)) return 10;
		if (flags == RADIX( 8)) return  8;
		if (flags == RADIX( 2)) return  2;
		throw new RuntimeException("Internal bug: RADIX() and start_radix() are inconsistent.");
	}

	/**get the radix associated with the next char, else return default_radix.*/
	protected byte zero_first(byte default_radix, int next) {
		if (isset( OPT_BIN) && next=='b')	return  2;
		if (isset( OPT_HEX) && next=='x')	return 16;
		if (isset( OPT_DEC) && next=='d')	return 10;
		if (isset( OPT_OCT) && next=='o')	return  8;
		if (isset(ZERO_OCT))             	return  8;
		digits++;                        	return default_radix;
	}

	/**16->4 10->3 8->3 2->1*/
	protected final byte bits_per_digit() {
		// radix                       	              	leadingz	31-
		//0b0000_0000__0000_0000__0000_0000__0001_0000	27      	4
		//0b0000_0000__0000_0000__0000_0000__0000_1000	28      	3
		//0b0000_0000__0000_0000__0000_0000__0000_0010	30      	1
		//0b0000_0000__0000_0000__0000_0000__0000_1010	28      	3
		return (byte)(31 - Integer.numberOfLeadingZeros(radix));
	}



	/**Do the actual parsing.
	 *@param max_digits maximum number of digits, including leading zeroes, multiplying a number by more than two and make it hard to detect.
	 *@param max_value max allowed value, should be negative.
	 *@throws NumberFormatException NaN/no digits, too big/small/long 
	 *///too many parameters, but I need them to make good error messages.
	private long parse(int max_digits, long max_value) throws IOException, NumberFormatException {
		long num=0;
		int n;
		while ((n = toNum(next())) != -1) {
			digits++;
			long newnum = num*radix + n;//need to check for overflow
			c = reader.read();//always read one past
			if (digits > max_digits)
				throw new NumberFormatException("max "+digits+" digits");
			if (Long.compareUnsigned(num, newnum) > 0)
				throw new NumberFormatException("number is too " + (negative ? "small" : "big"));
			num = newnum;
		}
		if (Long.compareUnsigned(num, max_value) > 0)//read the whole number first
			throw new NumberFormatException(numToString(num) + " is too " + (negative ? "small" : "big"));
		if (digits == 0)
			throw new NumberFormatException("No digits");
		if (negative)
			num = -num;
		return num;
	}



	/**try to recreate number so exception messages use the correct radix and prepend 0[xob], and does unsigned correctly.*/
	protected String numToString(long num) {
		StringBuilder out = new StringBuilder(18);
		if (negative)
			out.append('-');
		if (radix != start_radix()) {
			out.append('0');
			if (!isset(ZERO_OCT)  ||  isset(OPT_OCT)  ||  radix != 8)
				out.append(radixToChar(radix));
		}
		out.append(Long.toUnsignedString(num, radix));
		return out.toString();
	}
}
