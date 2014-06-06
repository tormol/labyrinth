package labyrinth.engine.method;

import static tbm.util.statics.*;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser implements Closeable, AutoCloseable {
	public static void main(String[] args) {
		char[] arr = "Hello world".toCharArray();
		//System.out.println(new String(arr, 2, -1));//out of range
		System.out.println(new String(arr, -1, 2));//out of range
	}
	public final File file;
	private int line=0, col=-1;
	private BufferedReader src;
	private final ArrayList<char[]> lines = new ArrayList<>();
	public Parser(File file) throws FileNotFoundException {
		this.file = file;
		this.src = new BufferedReader(new FileReader(file));
	}
	private boolean read() throws IOException {
		String line = src.readLine();
		if (line==null)
			return false;
		lines.add(line.toCharArray());
		return true;
	}
	public boolean empty() {
		if (col==lines.get(line).length);
		return false;
	}
	private int _peek() throws IOException {
		if (col == -1) {
			if (!read())
				return -1;
			col = 0;
		}
		//in the first call lines is empty
		else if (col == lines.get(line).length)
			return '\n';
		return lines.get(line)[col];
	}
	public char peek() throws IOException {
		int c = _peek();
		if (c==-1)
			throw new EOS();
		return (char)c;
	}
	public char peek_nw() throws IOException {
		whitespace();
		return peek();
	}
	public char next() throws IOException {
		char c = peek();
		skip();
		return c;
	}
	public char next_nw() throws IOException {
		whitespace();
		return next();
	}
	private void skip() {
		col++;
		if (col > lines.get(line).length) {
			col = -1;
			line++;
		}
	}
	public int whitespace() throws IOException {
		int skipped = 0;
		int ch = _peek();
		while (ch != -1 && char_whitespace((char)ch)) {
			skip();
			ch = _peek();
			skipped++;
		}
		return skipped;
	}
	public Pos getPos() {
		return new Pos(line, col);
	}
	public boolean setPos(int line, int col) {
		return false;
	}
	public String subString(Pos start) {
		if (start.line == line)
			if (start.col <= col)
				return new String( lines.get(line), start.col, col-start.col);
			else
				throw new IllegalArgumentException("start collumn is after current collumn");
		if (start.line > line)
			throw new IllegalArgumentException("start line is after current line");
		char[] startLine = lines.get(start.line);
		if (startLine == null)
			throw new RuntimeException("the start has been freed");
		StringBuilder sb = new StringBuilder();
		sb.append(startLine, start.col, startLine.length-start.col);
		sb.append('\n');
		for (int l=start.line+1; l<line; l++)
			sb.append(lines.get(l)).append('\n');
		if (col > 0)
			sb.append(lines.get(line), 0, col);
		return sb.toString();
	}


	public int _uint() throws IOException {
		int num=0;
		char c=this.peek_nw();
		if (!char_num(c))
			throw new NumberFormatException("no numer at all");
		do {
			this.skip();
			num = num*10 + c-'0';
			c = this.peek();
		} while (char_num(c));
		return num;
	}
	public int _int() throws IOException {
		int sign=1;
		if (peek_nw()=='-') {
			sign = -1;
			next();
		}
		return _uint()*sign;
	}
	public String next(CaptureChar c) throws IOException {
		Pos start = getPos();
		while (c.capture(next()))
			;
		return subString(start);
	}


	public static interface CaptureChar {
		boolean capture(char c);
	}

	public static class EOS extends RuntimeException {
		private EOS() {
			super();
		}
		private static final long serialVersionUID = 1L;
	}

	public static class Pos {
		public final int line, col;
		private Pos(int line, int col) {
			this.line = line;
			this.col = col;
		}
	}

	@Override//Closeable
	public void close() throws IOException {
		if (src != null) {
			src.close();
			src = null;
		}
	}
}
