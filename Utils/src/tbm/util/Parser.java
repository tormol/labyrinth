package tbm.util;
import static tbm.util.statics.*;
import tbm.util.parseNum;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import tbm.util.statics.InvalidHexException;

public class Parser implements Closeable, AutoCloseable {
	/**End of file, will always be -1*///because internal code depends on it;
	public static final int END = -1;
	public static char checkEnd(int c) {
		if (c==END)
			throw new EOS();
		return (char)c;
	}

	public final File file;
	private int line=0, col=0;
	private BufferedReader src;
	private final ArrayList<char[]> lines = new ArrayList<>();
	/**Is newline a whitespace character?*/
	public boolean newline_whitespace = false;
	/**Should the int parsing methods detect other number systems?*/
	public boolean int_other_systems = true;
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
	public boolean empty() throws IOException {
		peek();
		return end;
	}
	public int ipeek() throws IOException {
		if (line == lines.size()) {
			read_line();
			if (end)
				return END;
		}
		if (col == lines.get(line).length)
			return '\n';
		return lines.get(line)[col];
	}
	public char peek() throws IOException {
		return checkEnd(ipeek());
	}
	public int inext() throws IOException {
		int c = ipeek();
		skip();
		return c;
	}
	public char next() throws IOException {
		char c = checkEnd(ipeek());
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
	public Parser back() {
		if (col == 0) {
			if (line == 0)
				throw new RuntimeException("Cannot back() from the start of a file.");
			line--;
			col = lines.get(line).length;
		} else
			col--;
		return this;
	}
	/**skip whitespace*/
	public Parser skip_whitespace(boolean newline_whitespace) throws IOException {
		int ch = ipeek();
		while (ch==' ' || ch=='\t' || (ch=='\n' && newline_whitespace)) {
			skip();
			ch = ipeek();
		}
		return this;
	}
	public Parser skip_whitespace() throws IOException {
		return skip_whitespace(newline_whitespace);
	}
	public Parser sw() throws IOException {
		return skip_whitespace(newline_whitespace);
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

	private class PNSupply implements parseNum.CharSupplier<IOException> {
		@Override public char get() throws IOException {
			if (end)
				throw new EOS();
			return (char)inext();
		}
	}	
	public int _uint(boolean negative, boolean other_systems) throws IOException, NumberFormatException {
		int num = parseNum.unsigned(new PNSupply(), negative, other_systems);
		back();
		return num;
	}
	public int _int(boolean other_systems) throws IOException, NumberFormatException {
		int num = parseNum.signed(new PNSupply(), other_systems);
		back();
		return num;
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
	public String escapeString() throws IOException, InvalidEscapeException {
		StringBuilder sb = new StringBuilder();
		char c;
		while ((c=next())!='"')
			if (c=='\\')
				sb.append(escaped());
			else if (c=='\n')
				throw new InvalidEscapeException("line breaks must be escaped");
			else
				sb.append(c);
		return sb.toString();
	}

	public char escapeChar() throws IOException, InvalidEscapeException {
		char c = next();
		if (c=='\n')
			throw new InvalidEscapeException("line breaks must be escaped");
		if (c=='\'')
			return '\0';
		if (c=='\\')
			c = escaped();
		if (peek() == '\'')
			next();
		return c;
	}

	private char escaped() throws IOException, InvalidEscapeException {
		char c = next();
		try{switch (c) {
			case 't': return'\t';
			case 's': return ' ';
			case'\n': skip_whitespace();//a simple way to allow indentation
			case 'n': return'\n';
			case 'x': return char_asHex(next(), next());
			case'a':case'b':case'c':case'd':case'e':case'f':
				c -= 'a'-'A';
			case'A':case'B':case'C':case'D':case'E':case'F':
				c -= 'A'-'0'+10;
			case'0':case'1':case'2':case'3':case'4':case'5':case'6':case'7':case'8':case'9':
				return (char) (16*(c-'0') + char_asHex(next()));
			//case '': return'\';
			default: return c;
		}} catch (InvalidHexException e) {
			throw new InvalidEscapeException(e.getMessage());
		}
	}

	public static class InvalidEscapeException extends Exception {
		private InvalidEscapeException(String s, Object... o) {
			super(String.format(s, o));
		}
		private static final long serialVersionUID = 1L;
	}




	public static interface CaptureChar {
		boolean capture(char c);
	}

	public static class EOS extends RuntimeException {
		public EOS() {
			super("Unexpected end of stream.");
		}
		private static final long serialVersionUID = 1L;
	}
	public static class IORuntimeException extends RuntimeException {
		private IORuntimeException(IOException e) {
			super(e);
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
