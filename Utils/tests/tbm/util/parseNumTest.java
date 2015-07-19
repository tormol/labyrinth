package tbm.util;
import static tbm.util.parseNum.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class parseNumTest {
	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testLong_MIN_VALUE() {
		String minLong = "-9223372036854775808";
		assertEquals("wrong minLong", String.valueOf(Long.MIN_VALUE), minLong);
		assertEquals("Long.MIN_VALUE", Long.MIN_VALUE, with(MINUS, CharSupplier.fromString(minLong)).bits(Long.SIZE));
		thrown.expect(NumberFormatException.class);
		with(MINUS, CharSupplier.fromString("-9223372036854775809")).bits(Long.SIZE);
	}

	@Test
	public void testAllow_other_number_systems() {
		assertEquals("allowing other number systems doesn't break anything",
				with(OVERFLOW, CharSupplier.fromString("010")).bits(Long.SIZE),
				with(OVERFLOW|OPT_DEC, CharSupplier.fromString("010")).bits(Long.SIZE) );
		assertEquals("hexadecimal",
				with(OPT_HEX|OVERFLOW, CharSupplier.fromString("10")).bits(Long.SIZE),
				with(OPT_HEX|OVERFLOW, CharSupplier.fromString("0xa")).bits(Long.SIZE) );
		assertEquals("octal",
				with(OPT_OCT|OVERFLOW, CharSupplier.fromString("10")).bits(Long.SIZE),
				with(OPT_OCT|OVERFLOW, CharSupplier.fromString("0o12")).bits(Long.SIZE) );
		assertEquals("binary",
				with(OPT_BIN|OVERFLOW, CharSupplier.fromString("10")).bits(Long.SIZE),
				with(OPT_BIN|OVERFLOW, CharSupplier.fromString("0b1010")).bits(Long.SIZE) );
		assertEquals("other bases can overflow to negative without minus sign",
				-1,
				with(RADIX(2)|OVERFLOW_OTHER, CharSupplier.fromString("11")).bits(2) );
		thrown.expect(NumberFormatException.class);//no digits
		with(OPT_HEX, CharSupplier.fromString("0x")).bits(2);
	}

	@Test
	public void testWithSpaces() {
		assertEquals("accepting unused characthers is ok",
				with(OVERFLOW, CharSupplier.fromString("10")).bits(Long.SIZE),
				with(OVERFLOW|SKIP_SPACE, CharSupplier.fromString("10")).bits(Long.SIZE));
		assertEquals("space characters are ignored",
				with(OVERFLOW|SKIP_SPACE|SKIP_UNDERSCORE, CharSupplier.fromString("10")).bits(Long.SIZE),
				with(OVERFLOW|SKIP_SPACE|SKIP_UNDERSCORE, CharSupplier.fromString("_1 0")).bits(Long.SIZE));
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
