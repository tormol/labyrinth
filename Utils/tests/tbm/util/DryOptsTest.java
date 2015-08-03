package tbm.util;
import static org.junit.Assert.*;
import org.junit.Assert;
import org.junit.Test;
import tbm.util.ArgsParser;
import tbm.util.ArgsParser.Builder;

public class ArgsParserTest {
	public ArgsParser parse(String str) {
		return new ArgsParser(str.split(" "));
	}
	public ArgsParser parse(String str, Builder apb) {
		return apb.parse(str.split(" "));
	}

	/**Test method for {@link tbm.util.ArgsParser#getHelp()}.*/
	@Test
	public void testGetHelp() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#getArgs()}.*/
	@Test
	public void testGetArgs() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#hasErrors()}.*/
	@Test
	public void testHasErrors() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#getErrors()}.*/
	@Test
	public void testGetErrors() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optFlagN(char, java.lang.String, java.lang.String)}.*/
	@Test
	public void testOptFlagN() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optFlag(char, java.lang.String, java.lang.String)}.*/
	@Test
	public void testOptFlag() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optStr(char, java.lang.String, java.lang.String)}.*/
	@Test
	public void testOptStrCharStringString() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optStr(char, java.lang.String, java.lang.String, java.lang.String)}.*/
	@Test
	public void testOptStrCharStringStringString() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optStr(char, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.*/
	@Test
	public void testOptStrCharStringStringStringString() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optUInt(char, java.lang.String, java.lang.String)}.*/
	@Test
	public void testOptUInt() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optInt(char, java.lang.String, java.lang.String, int)}.*/
	@Test
	public void testOptIntCharStringStringInt() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#optInt(char, java.lang.String, java.lang.String, int, int, int)}.*/
	@Test
	public void testOptIntCharStringStringIntIntInt() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#handle_help(java.lang.String)}.*/
	@Test
	public void testHandle_help() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#help()}.*/
	@Test
	public void testHelp() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#handle_version(java.lang.String)}.*/
	@Test
	public void testHandle_version() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#version()}.*/
	@Test
	public void testVersion() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#verbosity()}.*/
	@Test
	public void testVerbosity() {
		fail("Not yet implemented");
	}

	/**Test method for {@link tbm.util.ArgsParser#handle_errors(int)}.*/
	@Test
	public void testHandle_errors() {
		fail("Not yet implemented");
	}


	/*Feature tests*/

	@Test
	public void doubleDashStopsOptParsing() {
		assert(parse("").optFlagN('f', null, null) == 1);
		assertTrue(parse("-f -- -f").optFlagN('f', null, null) == 1);
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
		ArgsParser ap = parse("-- --");
		assertArrayEquals(new String[]{"--"}, ap.allArguments("", false));
	}
}
