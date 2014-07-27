package tbm.util;
import static tbm.util.statics.*;

public class StringStream {
	public final String str;
	public final char[] arr;
	public int pos;
	public StringStream(String str) {
		this.str = str;
		this.arr = str.toCharArray();
		this.pos = 0;
	}
	public int length() {
		return arr.length;
	}
	public boolean empty() {
		return (pos == arr.length);
	}
	public char peek() {
		return arr[pos];
	}
	public char peek_nw() {
		whitespace();
		return peek();
	}
	public char next() {
		char c = arr[pos];
		pos++;
		return c;
	}
	public char next_nw() {
		whitespace();
		return next();
	}
	public void back() {
		pos--;
	}
	public void whitespace() {
		while (pos<arr.length && char_whitespace(arr[pos]))
			pos++;
	}


	public int _uint() {
		int num=0;
		char c=this.next_nw();
		if (!char_num(c))
			throw new NumberFormatException("no numer at all");
		do {
			num = num*10 + c-'0';
			c = this.next();
		} while (char_num(c));
		back();
		return num;
	}
	public int _int() {
		int sign=1;
		if (peek_nw()=='-') {
			sign = -1;
			next();
		}
		return _uint()*sign;
	}
	public String next(Matcher m) {
		int start=pos;
		while (m.capture(arr[pos]))
			pos++;
		return str.substring(start, pos);
	}

	public static interface Matcher {
		boolean capture(char c);
	}
}
