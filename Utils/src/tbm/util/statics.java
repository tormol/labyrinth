package tbm.util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class statics {
	public static boolean char_letter(char c) {
		return ((c>='a' && c<='z')  ||  (c>='A' && c<='Z')  ||  c=='_');
	}public static boolean char_num(char c) {
		return (c>='0' && c<='9');
	}public static boolean char_word(char c) {
		return (char_num(c) || char_letter(c));
	}public static boolean char_whitespace(char c) {
		return (c==' ' || c=='\t' ||  c=='\n');
	}public static boolean char_mathches(char c, String regex) {
		return charToString(c).matches(regex);
	}public static char[] char_array(char... a) {
		return a;
	}public static boolean char_anyof(char c, String list) {
		return list.indexOf(c) == -1;
	}public static boolean char_anyof(char c, char... list) {
		for (char e : list)
			if (c==e)
				return true;
		return false;
	}public static boolean char_hex(char c) {
		return char_num(c) || (c>='a' && c<='f') || (c>='A' && c<='F');
	}

	public static class InvalidHexException extends Exception {
		public InvalidHexException(char c) {
			super("'"+c+"' is not a hexadecimal characther.");
		}
		private static final long serialVersionUID = 1L;
	}public static int char_asHex(int c) {
		if (c>='0' && c<='9')
			return c-'0';
		c |= 0x20;//makes upper-case letters lower-case.
		if (c>='a' && c<='f')
			return c-'a'+10;
		//cannot write if (c<16)return c; because '\t' and '\n' are less than 16
		return -1;
	}public static char char_asHex(char c) throws InvalidHexException {
		c = char_asHex(c);
		if (c==(char)-1)
			throw new InvalidHexException(c);
		return c;
	}public static char char_asHex(char c1, char c2) throws InvalidHexException {
		return(char) (16*char_asHex(c1) + char_asHex(c2));
	}public static int char_toInt(char base, char c) {
		if (c >= '0'  &&  c  <= '9'  &&  c-'0' < base)
			return c - '0';
		if (base == 16) {
			c |= 0x20; 
			if (c >= 'a'  && c <= 'f')
				return 10+c-'a';
		} else if (base < 2  ||  base > 10)
			throw new RuntimeException("tbm.util.statics.char_toInt(): invalid base: "+base+"\nValid bases are 2-10, 16 and 256.");
		return -1;
	}
	public static <EX extends Throwable> char char_escape(char first, CharSupplier<EX> get) throws EX, InvalidHexException {
		switch (first) {
			case 't':return'\t';
			case 's':return ' ';
			case 'n':return'\n';
			case 'x':return char_asHex((char)get.get(), (char)get.get());
			case '0':return'\0';
			case 'e':return 0x1b;//escape
			case 'b':return'\b';
			case 'r':return'\r';
			default :return first;
		}
	}

	@Deprecated/**replace with char2str*/
	public static String charToString(char c) {
		return String.valueOf(c);
	}/**six letters shorter than String.valueOf*/
	public static String char2str(char c) {
		return String.valueOf(c);
	}/**Fill a String with n characters of c*/
	public static String String_nchars(int n, char c) {
		char[] arr = new char[n];
		Arrays.fill(arr, c);
		return String.valueOf(arr);
	}/**Compare two Strings without throwing NullPointerException.
	  * if both are null the result is false.*/
	public static boolean String_equals(String a, String b) {
		return a==null ? false : a.equals(b);
	}/**Create a String from arr starting at the nth character.*/
	public static String String_valueOf(char[] arr, int offset) {
		return String.valueOf(arr, offset, arr.length-offset);
	}
	/**Returns an array with all matches of regex i str.*/
	public static String[] String_findAll(String str, String regex) {
		LinkedList<String> found = new LinkedList<String>();
		Matcher m = Pattern.compile(regex).matcher(str);
		while (m.find())
			found.add(m.group());
		return found.toArray(new String[found.size()]);
	}

	/**Remove the last character from a StringBuilder
	 * Useful when you're creating a list and want to remove the last comma.
	 *@return the StringBuilder*/
	public static StringBuilder StringBuilder_removeLast(StringBuilder sb) {
		return sb.deleteCharAt(sb.length()-1);
	}


	@SafeVarargs//I'm not certain http://docs.oracle.com/javase/specs/jls/se7/html/jls-9.html#jls-9.6.3.7
	//javas lack of syntactic sugar is ... WHAT! HOW CAN I FORGET THIS?!
	/**a shorter initialization*/
	public static <T> T[] array(T... a) {
		return a;
	}
	/**A shorter alias of Arrays.asList()*/@SafeVarargs
	public static <T> List<T> list(T... a) {
		return Arrays.asList(a);
	}

	/**Create a HashMap and fill it with entries*/@SafeVarargs
	public static <K,V> HashMap<K,V> map_init(K[] keys, V... values) {
		if (keys.length != values.length)
			throw new IllegalArgumentException("keys.length != values.length");
		HashMap<K,V> map = new HashMap<>();
		for (int i=0; i<keys.length; i++)
			map.put(keys[i], values[i]);
		return map;
	}

	/**Get the first key which value == value parameter. else return null*/
	public static <K,V> K map_firstKey(Map<K,V> map, V value) {
		for (Map.Entry<K,V> entry : map.entrySet())
			if (value == entry.getValue())
				return entry.getKey();
		return null;
	}

	/**Intentionally do nothing.*/
	public static void do_nothing()
		{}

	/**Convert elements in a collection from one type to another and put them into a List.*/
	public static <F, T> List<T> convert(Collection<F> from, Function<F, T> convert) {
		List<T> to = new ArrayList<>(from.size());
		for (F e : from)
			to.add(convert.apply(e));
		return to;
	}

	/**@return value1==null ? value2 : value1*/
	public static <T> T nonNull(T value1, T value2) {
		return value1==null ? value2 : value1;
	}/**return value1 if it's not null then the same for value2. Throws an exception if both ar null.*/
	public static <T> T nonNull_throw(T value1, T value2) throws NullPointerException {
		if (value1 != null)
			return value1;
		if (value2 != null)
			return value2;
		throw new NullPointerException("Both values are null");
	}/**Won't call value2 if value1 returns null. Will only call once.*/
	public static <T> T nonNull(Supplier<T> value1, Supplier<T> value2) {
		T value = value1.get();
		if (value != null)
			return value;
		return value2.get();
	}/**Won't call value2 if value1 returns null. Will only call once. throws an exception if both return null.*/
	public static <T> T nonNull_throw(Supplier<T> value1, Supplier<T> value2) throws NullPointerException {
		T value = nonNull(value1, value2);
		if (value != null)
			return value;
		throw new NullPointerException("Both values are null");
	}


	/**Check that int -1 == char 255*/
	static {
		int i = -1;	//if i is final I get a warning: comparing identical expression, so this is probably guaranteed.
		if ((char)i != (char)-1  ||  (char)i != 0xffff)
			throw new RuntimeException("(char)((int)-1) != 0xffff");
	}
}
