package tbm.util;
import static tbm.util.parseNum.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class parseNumTest {
	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testLong_MIN_VALUE() throws IOException {
		String minLong = "-9223372036854775808";
		assertEquals("wrong minLong", String.valueOf(Long.MIN_VALUE), minLong);
		assertEquals("Long.MIN_VALUE", Long.MIN_VALUE, with(MINUS, new StringReader(minLong)).bits(Long.SIZE));
		thrown.expect(NumberFormatException.class);
		with(MINUS, new StringReader("-9223372036854775809")).bits(Long.SIZE);
	}

	@Test
	public void testAllow_other_number_systems() throws IOException {
		assertEquals("allowing other number systems doesn't break anything",
				with(OVERFLOW, new StringReader("010")).bits(Long.SIZE),
				with(OVERFLOW|OPT_DEC, new StringReader("010")).bits(Long.SIZE) );
		assertEquals("hexadecimal",
				with(OPT_HEX|OVERFLOW, new StringReader("10")).bits(Long.SIZE),
				with(OPT_HEX|OVERFLOW, new StringReader("0xa")).bits(Long.SIZE) );
		assertEquals("octal",
				with(OPT_OCT|OVERFLOW, new StringReader("10")).bits(Long.SIZE),
				with(OPT_OCT|OVERFLOW, new StringReader("0o12")).bits(Long.SIZE) );
		assertEquals("binary",
				with(OPT_BIN|OVERFLOW, new StringReader("10")).bits(Long.SIZE),
				with(OPT_BIN|OVERFLOW, new StringReader("0b1010")).bits(Long.SIZE) );
		assertEquals("other bases can overflow to negative without minus sign",
				-1,
				with(RADIX(2)|OVERFLOW_OTHER, new StringReader("11")).bits(2) );
		thrown.expect(NumberFormatException.class);//no digits
		with(OPT_HEX, new StringReader("0x")).bits(2);
	}

	@Test
	public void testWithSpaces() throws IOException {
		assertEquals("accepting unused characthers is ok",
				with(OVERFLOW, new StringReader("10")).bits(Long.SIZE),
				with(OVERFLOW|SKIP_SPACE, new StringReader("10")).bits(Long.SIZE));
		assertEquals("space characters are ignored",
				with(OVERFLOW|SKIP_SPACE|SKIP_UNDERSCORE, new StringReader("10")).bits(Long.SIZE),
				with(OVERFLOW|SKIP_SPACE|SKIP_UNDERSCORE, new StringReader("_1 0")).bits(Long.SIZE));
	}


/*	@Test
	public void testParse_long() {
		fail("Not yet implemented");
	}

	@Test
	public void testSigned_long() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnsigned_long() {
		fail("Not yet implemented");
	}

	@Test
	public void testSigned_int() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnsigned_int() {
		fail("Not yet implemented");
	}

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
