package tbm.util.test;

import tbm.util.ArgsParser;

@SuppressWarnings("unused")
public class TestArgsParser {
	public static void main(String[] args) {
		test(new String[]{"-", "b"});
		test(new String[]{"-b2", "a", "--", "--help"});
		test(new String[]{"-b2", "a", "--", "--help"});
	}

	public static void test(String[] args) {
		ArgsParser ap = new ArgsParser(args);
		boolean version = ap.version();
		int verbosity = ap.optFlagN('V', "verbose", "Tell me more.");
		boolean old = ap.optFlag('x', null, "deprecated");
		System.out.println(ap.help());

		System.out.println(ap.toString());
	}
}
