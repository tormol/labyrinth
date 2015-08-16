package tbm.util;
import static tbm.util.ParseNum.*;
import static org.junit.Assert.*;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

public class ParseNumTest {
	public long parse(String num, int bits, int flags) throws NumberFormatException, IllegalArgumentException {
		try {
			return new ParseNum(flags, new StringReader(num)).bits(bits);
		} catch (IOException e) {//should never happen
			throw new RuntimeException("A StringBuilder somehow threw an IOException.", e);
		}
	}

	/**fail if a NumberFormatException is not thrown*/
	public void expectNFE(String desc, String num, int bits, int flags) {
		try {
			parse(num, bits, flags);
			fail(desc);
		} catch (NumberFormatException e)
			{}
	}

	/**fail if an IllegalArgumentException is not thrown*/
	public void expectIAE(String desc, String num, int bits, int flags) {
		try {
			parse(num, bits, flags);
			fail(desc);
		} catch (NumberFormatException e)
			{}
	}




	@Test
	public void testFlagCombinations() {
		fail("Not yet implemented");
	}

	@Test//the basics
	public void testRadixDigits() {
		assertEquals("dec",   19,  parse( "19a", Long.SIZE, OVERFLOW|RADIX(10)));
		assertEquals("oct",   15,  parse( "178", Long.SIZE, OVERFLOW|RADIX( 8)));
		assertEquals("bin",    2,  parse( "103", Long.SIZE, OVERFLOW|RADIX( 2)));
		assertEquals("hex",  506,  parse("1fAg", Long.SIZE, OVERFLOW|RADIX(16)));
	}

	@Test
	public void testChangeRadix() {
		assertEquals("allowing changing radix doesn't break anything",
				parse("010", Long.SIZE, OVERFLOW),
				parse("010", Long.SIZE, OVERFLOW|OPT_DEC) );
		assertEquals("without ZERO_OCT, leading zeroes are ignored",  10,  parse("010", Long.SIZE, OVERFLOW) );
		assertEquals("with ZERO_OCT, leading zeroes causes octal",  10,  parse("012", Long.SIZE, OVERFLOW|ZERO_OCT) );
		
		assertEquals("OPT_HEX",  10,  parse("0xa",    Long.SIZE, OVERFLOW|OPT_HEX) );
		assertEquals("OPT_DEC",  10,  parse("0d10",   Long.SIZE, OVERFLOW|OPT_DEC|RADIX(2)) );
		assertEquals("OPT_OCT",  10,  parse("0o12",   Long.SIZE, OVERFLOW|OPT_OCT) );
		assertEquals("OPT_BIN",  10,  parse("0b1010", Long.SIZE, OVERFLOW|OPT_BIN) );
		assertEquals("other bases can overflow to negative without minus sign",
				-1,
				parse("11", 2, RADIX(2)|OVERFLOW_OTHER) );
	}

	@Test
	public void testNoDigits() {
		expectNFE("empty string throw", "", Long.SIZE, OVERFLOW);
		expectNFE("no digits didn't throw", "_", Long.SIZE, OVERFLOW);
		expectNFE("0x didn't throw", "0x", Long.SIZE, OVERFLOW|OPT_HEX);
	}


	@Test
	public void testSKIP_() {
		assertEquals("accepting unused characthers is ok",
				parse("10", Long.SIZE, OVERFLOW),
				parse("10", Long.SIZE, OVERFLOW|SKIP_SPACE) );
		assertEquals("space characters are ignored",
				parse("10", Long.SIZE, OVERFLOW|SKIP_SPACE|SKIP_UNDERSCORE),
				parse("_1 0", Long.SIZE, OVERFLOW|SKIP_SPACE|SKIP_UNDERSCORE) );
		expectNFE("No digits when number starts with whitespace", " 0", Long.SIZE, OVERFLOW|SKIP_UNDERSCORE);
	}


	@Test
	public void testGeneralMinusOrOverflow() {
		fail("Not yet implemented");
	}

	@Test
	public void testRadixSpecificOverflowAndMinus() {
		fail("Not yet implemented");
	}

	@Test
	public void testLimitedBits() {
		fail("Not yet implemented");
	}

	@Test//the hardest case
	public void testMaxBits() {
		assertEquals("Long.MIN_VALUE+1 is too small",
				Long.MIN_VALUE+1,
				parse(Long.toString(Long.MIN_VALUE+1), Long.SIZE, MINUS));
		assertEquals("Long.MIN_VALUE is too small",
				Long.MIN_VALUE,
				parse(Long.toString(Long.MIN_VALUE), Long.SIZE, MINUS));
		expectNFE("a number one too small for long doesn't throw",
				"-"+Long.toUnsignedString(Long.MAX_VALUE+2),
				Long.SIZE, MINUS);
	}



/*
	@Test
	public void testSigned_short() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnsigned_short() {
		fail("Not yet implemented");
	}

	@Test
	public void testParse_char() {
		fail("Not yet implemented");
	}

	@Test
	public void testSigned_byte() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnsigned_byte() {
		fail("Not yet implemented");
	}
*/
}
