package tbm.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class statics {
	public static boolean char_letter(char c) {
		return ((c>='a' && c<='z')  ||  (c>='A' && c<='Z')  ||  c=='_');
	}
	public static boolean char_num(char c) {
		return (c>='0' && c<='9');
	}
	public static boolean char_word(char c) {
		return (char_num(c) || char_letter(c));
	}
	public static boolean char_whitespace(char c) {
		return (c==' ' || c=='\t' ||  c=='\n');
	}
	public static boolean char_mathches(char c, String regex) {
		return charToString(c).matches(regex);
	}

	public static String charToString(char c) {
		return String.valueOf(c);
	}
	public static String String_nchars(int n, char c) {
		char[] arr = new char[n];
		Arrays.fill(arr, c);
		return new String(arr);
	}
	public static boolean String_equal(String a, String b) {
		if (a==null || b==null)
			return false;
		return a.equals(b);
	}

	/**Returns an array with all matches of regex i str.*/
	public static String[] String_findAll(String str, String regex) {
		LinkedList<String> found = new LinkedList<String>();
		Matcher m = Pattern.compile(regex).matcher(str);
		while (m.find())
			found.add(m.group());
		return found.toArray(new String[found.size()]);
	}

	@SafeVarargs//I'm not certain http://docs.oracle.com/javase/specs/jls/se7/html/jls-9.html#jls-9.6.3.7
	//javas lack of syntactic sugar is ... WHAT! HOW CAN I FORGET THIS?!
	/**a shorter initialization*/
	public static <T> T[] array(T... e) {
		return e;
	}

	@SafeVarargs
	public static <K,V> HashMap<K,V> map_init(K[] keys, V... values) {
		if (keys.length != values.length)
			throw new IllegalArgumentException("keys.length != values.length");
		HashMap<K,V> map = new HashMap<>();
		for (int i=0; i<keys.length; i++)
			map.put(keys[i], values[i]);
		return map;
	}

	/**Intentionally do nothing*/
	public static void do_nothing()
		{}
}
