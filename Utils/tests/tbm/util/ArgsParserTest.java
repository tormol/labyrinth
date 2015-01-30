package tbm.util;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class ArgsParserTest {
	public ArgsParser parse(String str) {
		return new ArgsParser(str.split(" "));
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#ArgsParser(tbm.util.ArgsParser.Builder, java.lang.String[])}.
	 */
	@Test
	public void testArgsParserBuilderStringArray() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#opt(tbm.util.ArgsParser.Opt_interface)}.
	 */
	@Test
	public void testOpt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#getHelp()}.
	 */
	@Test
	public void testGetHelp() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#getArgs()}.
	 */
	@Test
	public void testGetArgs() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#hasErrors()}.
	 */
	@Test
	public void testHasErrors() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#getErrors()}.
	 */
	@Test
	public void testGetErrors() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optFlagN(char, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testOptFlagN() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optFlag(char, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testOptFlag() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optStr(char, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testOptStrCharStringString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optStr(char, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testOptStrCharStringStringString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optStr(char, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testOptStrCharStringStringStringString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optUInt(char, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testOptUInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optInt(char, java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testOptIntCharStringStringInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#optInt(char, java.lang.String, java.lang.String, int, int, int)}.
	 */
	@Test
	public void testOptIntCharStringStringIntIntInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#handle_help(java.lang.String)}.
	 */
	@Test
	public void testHandle_help() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#help()}.
	 */
	@Test
	public void testHelp() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#handle_version(java.lang.String)}.
	 */
	@Test
	public void testHandle_version() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#version()}.
	 */
	@Test
	public void testVersion() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#verbosity()}.
	 */
	@Test
	public void testVerbosity() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link tbm.util.ArgsParser#handle_errors(int)}.
	 */
	@Test
	public void testHandle_errors() {
		fail("Not yet implemented");
	}

	@Test
	public void doubleDashStopsOptParsing() {
		assertTrue(parse("-f -- -f").optFlagN('f', null, null) == 1);
	}
	@Test
	public void onlyFirstDoubleDashShouldBeHandled() {
		ArgsParser ap = parse("-- --");
		//assertArrayEquals(new String[]{"--"}, ap.getArgs());
		if (ap.getArgs().length != 1)
			fail();
	}
}
