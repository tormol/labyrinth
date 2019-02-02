package no.torbmol.util;
import static org.junit.Assert.*;

import org.junit.Test;
import no.torbmol.util.DryOpts.Builder;

public class DryOptsTest {
	public DryOpts parse(String str) {
		return new DryOpts(str.split(" "));
	}
	public DryOpts parse(String str, Builder apb) {
		return apb.parse(str.split(" "));
	}

// 	/**Test method for {@link DryOpts#getHelp()}.*/
// 	@Test
// 	public void testGetHelp() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#getArgs()}.*/
// 	@Test
// 	public void testGetArgs() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#hasErrors()}.*/
// 	@Test
// 	public void testHasErrors() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#getErrors()}.*/
// 	@Test
// 	public void testGetErrors() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optFlagN(char, java.lang.String, java.lang.String)}.*/
// 	@Test
// 	public void testOptFlagN() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optFlag(char, java.lang.String, java.lang.String)}.*/
// 	@Test
// 	public void testOptFlag() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optStr(char, java.lang.String, java.lang.String)}.*/
// 	@Test
// 	public void testOptStrCharStringString() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optStr(char, java.lang.String, java.lang.String, java.lang.String)}.*/
// 	@Test
// 	public void testOptStrCharStringStringString() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optStr(char, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.*/
// 	@Test
// 	public void testOptStrCharStringStringStringString() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optUInt(char, java.lang.String, java.lang.String)}.*/
// 	@Test
// 	public void testOptUInt() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optInt(char, java.lang.String, java.lang.String, int)}.*/
// 	@Test
// 	public void testOptIntCharStringStringInt() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#optInt(char, java.lang.String, java.lang.String, int, int, int)}.*/
// 	@Test
// 	public void testOptIntCharStringStringIntIntInt() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#handle_help(java.lang.String)}.*/
// 	@Test
// 	public void testHandle_help() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#help()}.*/
// 	@Test
// 	public void testHelp() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#handle_version(java.lang.String)}.*/
// 	@Test
// 	public void testHandle_version() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#version()}.*/
// 	@Test
// 	public void testVersion() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#verbosity()}.*/
// 	@Test
// 	public void testVerbosity() {
// 		fail("Not yet implemented");
// 	}

// 	/**Test method for {@link DryOpts#handle_errors(int)}.*/
// 	@Test
// 	public void testHandle_errors() {
// 		fail("Not yet implemented");
// 	}


	/*Feature tests*/

	@Test
	public void doubleDashStopsOptParsing() {
		assertEquals(parse("-f").optFlagN('f', null, null), 1);
		assertEquals(parse("-f -- -f").optFlagN('f', null, null), 1);
	}

	@Test
	public void stopAtFirstNonOpt() {
		Builder b = new Builder();
		b.nonopt_stops_opts = false;
		assertTrue("=false",  parse("aa -f", b).optFlagN('f', null, null) == 1);
		b.nonopt_stops_opts = true;
		assertTrue("=true",   parse("aa -f", b).optFlagN('f', null, null) == 0);
		assertTrue("=true should handle a OptArg that is returned", 
		                      parse("-f aa -f", b).optFlagN('f', null, null) == 1);
	}

	@Test
	public void negativeNumbersAreNotOptions() {
		Builder b = new Builder();
		assertFalse(parse("-6", b).optFlag('6', "ipv6", null));
		b.integer_shortOpts();
		assertTrue (parse("-6", b).optFlag('6', "ipv6", null));
	}
	
	@Test
	public void onlyFirstDoubleDashShouldBeHandled() {
		DryOpts ap = parse("-- --");
		assertArrayEquals(new String[]{"--"}, ap.allArgs("", false));
	}
}
