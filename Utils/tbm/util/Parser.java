package tbm.util;

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
	private int line=0, col=0;
	private BufferedReader src;
	private final ArrayList<char[]> lines = new ArrayList<>();
	public boolean newline_whitespace = false;
	private boolean end = false;
	public Parser(File file) throws FileNotFoundException {
		this.file = file;
		this.src = new BufferedReader(new FileReader(file));
	}
	private void read_line() throws IOException {
		if (end)
			return;
		String line = src.readLine();
		if (line==null)
			end = true;
		else
			lines.add(line.toCharArray());
	}
	public boolean empty() {
		return end;
	}
	private int peek(boolean throw_EOS) throws IOException {
		if (line == lines.size()) {
			read_line();
			if (end)
				if (throw_EOS)
					throw new EOS();
				else
					return -1;
		}
		if (col == lines.get(line).length)
			return '\n';
		return lines.get(line)[col];
	}
	public char peek() throws IOException {
		return (char)peek(true);
	}
	public char next() throws IOException {
		char c = peek();
		skip();
		return c;
	}
	public Parser skip() throws IOException {
		if (col == 0)
			peek();
		col++;
		if (col > lines.get(line).length) {
			col = 0;
			line++;
		}
		return this;
	}
	/**skip whitespace*/
	public Parser sw() throws IOException {
		int ch = peek(false);
		while (ch==' ' || ch=='\t' || (ch=='\n' && newline_whitespace)) {
			skip();
			ch = peek(false);
		}
		return this;
	}
	public Pos getPos() {
		return new Pos(line, col);
	}
	public Parser setPos(int line, int col) {
		if (line<0 || line>=lines.size())
			throw new IndexOutOfBoundsException("Line out of range");
		if (col<0 || col>=lines.get(line).length)
			throw new IndexOutOfBoundsException("col out of range");
		this.line = line;
		this.col = col;
		return this;
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
		char c=sw().peek();
		if (!char_num(c))
			throw new NumberFormatException("no numer at all");
		do {
			skip();
			num = num*10 + c-'0';
			c = peek();
		} while (char_num(c));
		return num;
	}
	public int _int() throws IOException {
		int sign=1;
		if (sw().peek()=='-') {
			sign = -1;
			skip();
		}
		return sign*_uint();
	}
	public String next(CaptureChar c) throws IOException {
		Pos start = getPos();
		while (c.capture(peek()))
			skip();
		return subString(start);
	}
	public String line() throws IOException {
		peek();
		char[] arr = lines.get(line);
		String str = new String(arr, col, arr.length-col);
		line++;
		col=0;
		return str;
	}
	public String escapeString(char stop) throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean escaped = false;
		char c;
		while ((c=next())!=stop || escaped)
			if (escaped) {
				if (c==stop)
					sb.append(stop);
				else
					switch (c) {
						case 't': sb.append('\t'); break;
						case 's': sb.append(' '); break;
						case '\n': sw();//a simple way to allow indentation
						case 'n': sb.append('\n'); break;
						case '\\': sb.append('\\'); break;
						//case '': sb.append('\'); break;
						default: sb.append('\\').append(c);
					}
				escaped = false;
			}
			else if (c=='\\')
				escaped = true;
			else if (c=='\n')
				throw new RuntimeException("line breaks must be escaped");
			else
				sb.append(c);
		return sb.toString();
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
