package tbm.util;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class parseNumTest {
	@Rule
    public ExpectedException thrown= ExpectedException.none();

	@Test
	public void testLong_MIN_VALUE() {
		String minLong = "-9223372036854775808";
		assertEquals("wrong minLong", String.valueOf(Long.MIN_VALUE), minLong);
		assertEquals(Long.MIN_VALUE, parseNum.signed_long(CharSupplier.fromString(minLong), false, ""));
		assertEquals(Long.MIN_VALUE, parseNum.unsigned_long(CharSupplier.fromString(minLong.substring(1)), true, false, ""));
		thrown.expect(NumberFormatException.class);
		parseNum.signed_long(CharSupplier.fromString("-9223372036854775809"), false, "");
	}

	@Test
	public void testAllow_other_number_systems() {
		assertEquals("allowing other number systems doesn't break anything",
				parseNum.signed_long(CharSupplier.fromString("010"), false, ""),
				parseNum.signed_long(CharSupplier.fromString("010"), true, ""));
		assertEquals("hexadecimal",
				parseNum.signed_long(CharSupplier.fromString("10"), true, ""),
				parseNum.signed_long(CharSupplier.fromString("0xa"), true, ""));
		assertEquals("octal",
				parseNum.signed_long(CharSupplier.fromString("10"), true, ""),
				parseNum.signed_long(CharSupplier.fromString("0o12"), true, ""));
		assertEquals("binary",
				parseNum.signed_long(CharSupplier.fromString("10"), true, ""),
				parseNum.signed_long(CharSupplier.fromString("0b1010"), true, ""));
		assertEquals("other bases can overflow to negative without minus sign",
				parseNum.signed_byte(CharSupplier.fromString("-1"), true, ""),
				parseNum.signed_byte(CharSupplier.fromString("0xff"), true, ""));
		thrown.expect(NumberFormatException.class);//no digits
		parseNum.signed_long(CharSupplier.fromString("0x"), true, "");
	}

	@Test
	public void testWithSpaces() {
		assertEquals("accepting unused characthers is ok",
				parseNum.signed_long(CharSupplier.fromString("10"), false, ""),
				parseNum.signed_long(CharSupplier.fromString("10"), false, " _"));
		assertEquals("space characters are ignored",
				parseNum.signed_long(CharSupplier.fromString("10"), false, " _"),
				parseNum.signed_long(CharSupplier.fromString("1_ 0"), false, " _"));
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
