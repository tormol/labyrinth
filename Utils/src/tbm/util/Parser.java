package tbm.util;
import static tbm.util.statics.*;
import tbm.util.parseNum;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;

import tbm.util.statics.InvalidHexException;

public class Parser implements Closeable, AutoCloseable, CharSupplier<IOException>, Cloneable {
	/**End of file, will always be -1*///because internal code depends on it;
	public static final int END = -1;
	public static char checkEnd(int c) throws EOFException {
		if (c==END)
			throw new EOFException(eof);
		return (char)c;
	}
	protected static String eof = "Unexpected end of stream.";

	protected final Base base;
	private int line, col;
	/**Is newline a whitespace character?*/
	public final boolean newline_whitespace;
	/**Should the int parsing methods detect other number systems?*/

	private Parser(Base base, boolean newline_whitespace, int line, int col) {
		this.base = base; 
		this.newline_whitespace = newline_whitespace;
		this.line = line;
		this.col = col;
	}
	protected Parser(Parser p, boolean newline_whitespace, int line, int col) {
		this(p.base, newline_whitespace, line, col);
	}

	/**TODO: replace with interface, but then I need length(), get(line) and get(line, col)*/
	private static class Base extends ArrayList<String> implements Closeable {
		public Base() {
			add(null);//so the first line starts at one, null to fail fast.
		}
		/**read a line from src*/
		public boolean read_line() throws IOException {
			return false;
		}
		@Override//Closeable
		public void close() throws IOException {
			do_nothing();
		}
		public String toString() {
			return "";
		}
		private static final long serialVersionUID = 1L;
	}
	public Parser(String str, boolean newline_whitespace) {
		this(new Base(), newline_whitespace, 0, 0);
		for (String line : str.split("\n"))
			base.add(line);
	}
	private static class FileBase extends Base {
		public final File file;
		private BufferedReader src;
		public FileBase(File file) throws FileNotFoundException {
			this.file = file;
			this.src = new BufferedReader(new FileReader(file));
		}
		public synchronized boolean read_line() throws IOException {
			if (src == null)
				return false;
			String line = src.readLine();
			if (line == null) {
				close();
				return false;
			}
			/**Might change this, String charAt() is faster than asArray()*/
			add(line);
			return true;
		}
		public String toString() {
			return file.getPath() + ' ';
		}
		/**Close the file.*/@Override//Closeable
		public synchronized void close() throws IOException {
			if (src != null) {
				src.close();
				src = null;
			}
		}
		private static final long serialVersionUID = 1L;
	}
	public Parser(File file, boolean newline_whitespace) throws FileNotFoundException {
		this(new FileBase(file), newline_whitespace, 0, 0);
	}

	private static class SupplierBase extends Base {
		public SupplierBase(Supplier<String> get) {
			this.get = get;
		}
		public final Supplier<String> get;
		@Override public boolean read_line() throws IOException {
			String line = get.get();
			if (line == null)
				return false;
			add(line);
			return true;
		}
		private static final long serialVersionUID = 1L;
	}
	public Parser(Supplier<String> get, boolean newline_whitespace) {
		this(new SupplierBase(get), newline_whitespace, 0, 0);
	}
	@Override//Cloneable
	public Parser clone() {
		return new Parser(base, newline_whitespace, line, col);
	}
	public Parser with_newline_whitespace(boolean newline_whitespace) {
		return new Parser(base, newline_whitespace, line, col);
	}
	

	@Override//Closeable, AutoCloseable
	/**If this Parser is backed by a file or stream, it is closed, if not nothing happens*/
	public void close() throws IOException {
		base.close();
	}

	/**Read everything from the source and close it.*/
	public void read_all() throws IOException {
		while (base.read_line())
			do_nothing();
		base.close();
	}

	/**@return wheter the source is empty*/
	public boolean empty() throws IOException {
		if (line == base.size())
			return !base.read_line();
		return false;
	}
	/**Skip to the next char
	 *@return this*/
	public Parser skip() throws IOException {
		if (!empty()) {
			col++;
			if (col > base.get(line).length()) {
				col = 0;
				line++;
			}
		}
		return this;
	}
	/**Move to the previous char
	 *@return this*/
	public Parser back() {
		if (col == 0) {
			if (line == 1)
				throw new RuntimeException("Cannot back() from the start of a file.");
			line--;
			col = base.get(line).length();
		} else
			col--;
		return this;
	}

	/**get the char at the current positon without incrementing the position. returns -1 if there is nothing to read.*/
	public int ipeek() throws IOException {
		if (empty())
			return END;
		if (col == base.get(line).length())
			return '\n';
		return base.get(line).charAt(col);
	}
	/**get the char at the current positon without incrementing the position. returns -1 if there is nothing to read.*/
	public char peek() throws IOException, EOFException {
		int c = ipeek();
		if (c == END)
			throw new EOFException(eof);
		return (char)c;
	}
	public int inext() throws IOException {
		int c = ipeek();
		skip();
		return c;
	}
	public char next() throws IOException, EOFException {
		int c = ipeek();
		if (c == END)
			throw new EOFException(eof);
		skip();
		return (char)c;
	}


	/**@return the current line number, starts at one*/
	public int getLine() {
		return line;
	}
	/**@return the position in the current line, starts at zero*/
	public int getCol() {
		return col;
	}
	/**@return the length of the specified line*/
	public int length(int line) throws IndexOutOfBoundsException {
		try {
			return base.get(line).length();
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Invalid line number: "+line+", the line might not have been read yet.");
		}
	}

	/**Check that the parameter is valid, and go to the start of the specified line.
	 *@return this*/
	public Parser setLine(int line) throws IndexOutOfBoundsException {
		if (line<=0 || line>=base.size())
			throw new IndexOutOfBoundsException("Line out of range");
		this.line = line;
		this.col = 0;
		return this;
	}

	/**check that the*/
	public Parser setCol(int col) throws IndexOutOfBoundsException {
		int length = base.get(line).length();
		if (col < 0)
			this.col = length - col;
		else if (col >= length)
			this.col = length - 1;
		else
			this.col = col;
		return this;
	}

	public Parser setPos(int line, int col) {
		return setLine(line).setCol(col);
	}

	/**Set the line and col of to that of p.
	 **Only works if this and p share the same base;
	 *@return this*/
	public Parser setPos(Parser p) throws RuntimeException {
		if (p.base != this.base)
			throw new RuntimeException("The parser belong tho another source.");
		this.line = p.line;
		this.col  = p.col;
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
	public final Parser skip_whitespace() throws IOException {
		return skip_whitespace(newline_whitespace);
	}
	public final Parser sw() throws IOException {
		return skip_whitespace(newline_whitespace);
	}

	public String subString(Parser start) {
		if (start.line == line)
			if (start.col <= col)
				return base.get(line).substring( start.col, col-start.col);
			else
				throw new IllegalArgumentException("start collumn is after current collumn");
		if (start.line > line)
			throw new IllegalArgumentException("start line is after current line");
		String startLine = base.get(start.line);
		StringBuilder sb = new StringBuilder();
		sb.append(startLine, start.col, startLine.length()-start.col);
		sb.append('\n');
		for (int l=start.line+1; l<line; l++)
			sb.append(base.get(l)).append('\n');
		if (col > 0)
			sb.append(base.get(line), 0, col);
		return sb.toString();
	}

	public int _uint(boolean negative, boolean other_systems) throws IOException, EOFException, NumberFormatException {
		int num = parseNum.unsigned(this, negative, other_systems);
		back();
		return num;
	}
	public int _int(boolean other_systems) throws IOException, EOFException, NumberFormatException {
		int num = parseNum.signed(this, other_systems);
		back();
		return num;
	}

	public static interface CaptureChar {
		boolean capture(char c);
	}

	public String next(CaptureChar c) throws IOException, EOFException {
		Parser start = clone();
		while (c.capture(peek()))
			skip();
		return subString(start);
	}
	public String line() throws IOException, EOFException {
		if (empty())
			throw new EOFException(eof);
		String str = base.get(line).substring(col);
		line++;
		col=0;
		return str;
	}
	

	public char escapeChar(boolean require_end) throws EOFException, IOException, ParseException {
		char c = next();
		if (c == '\\')
			try {
				c = char_escape(next(), this);
			} catch (InvalidHexException e) {
				throw new ParseException("Invalid hex escape sequence");
			}
		else if (c == '\n')
			throw new ParseException("line breaks are not allowed in chars");
		if (ipeek() == '\'')
			skip();
		else if (require_end)
			throw new ParseException("missing '");
		return c;
	}

	public String escapeString(char end) throws IOException, EOFException, ParseException {
		if (peek() == end) {
			skip();
			if (ipeek() != end)
				return "";
			skip();
			return textBody(end);
		}
		int start = col;
		char c;
		StringBuilder sb = new StringBuilder();
		while ((c=next())!=end)
			if (c=='\\') {
				c = next();
				if (c == '\n') {
					sb.append(c);
					do {//ignore equal indentation
						c = next();
					} while (col <= start  &&  (c=='\t' || c==' '));
				}
				try {
					sb.append(char_escape(c, this));
				} catch (InvalidHexException e) {
					throw new ParseException("Invalid hex escape sequence");
				}
			} else if (c=='\n')
				throw new ParseException("line breaks must be escaped");
			else
				sb.append(c);
		return sb.toString();
	}
	private String textBody(char end) throws EOFException, IOException {
		String stop = String.valueOf(new char[]{end, end, end});
		int startCol = col;
		String rest = line();
		int endIndex = rest.indexOf(stop);
		if (endIndex != -1) {
			col = startCol + endIndex + stop.length();
			line--;
			return rest.substring(0, endIndex);
		}
		if (!rest.trim().isEmpty())
			stop = rest.trim();
		StringBuilder sb = new StringBuilder();
		for (String line = line();  !line.startsWith(stop);  line = line())
			sb.append(line).append('\n');
		if (base.get(this.line-1).length() != stop.length()) {
			this.line--;
			col = stop.length();
		}
		return StringBuilder_removeLast(sb).toString();
	}



	/***/
	public class ParseException extends Exception {
		public ParseException(String f, Object... a) {
			super(base.toString() + "Line " + (line+1) + ':' + col + ' ' + String.format(f, a));
		}
		private static final long serialVersionUID = 1L;
	}

	/**An easy way to throw a exception, prepends message with line number and column.*/
	public ParseException error(String f, Object... a) {
		return new ParseException(f, a);
	}

	@Override
	public String toString() {
		return base.toString() + "Line: " + (line+1) + ", col: " + col + ", is newline whitespace? " + newline_whitespace;
	}

	@Deprecated @Override//CharSupplier
	/**For implementing CharSupplier, which is used internally.
	 *Equals next(), but the EOFExcetion is thrown on the second END.*/
	public int get() throws IOException {
		if (empty())
			throw new EOFException(eof);
		return inext();
	}
}
