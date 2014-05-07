package tbm.util;

import java.util.Arrays;

public class strings {
	public static String nchars(int n, char c) {
		char[] arr = new char[n];
		Arrays.fill(arr, c);
		return new String(arr);
	}

	public static boolean equal(String a, String b) {
		if (a==null || b==null)
			return false;
		return a.equals(b);
	}
}
