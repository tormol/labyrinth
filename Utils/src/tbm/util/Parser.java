package tbm.util;
import static tbm.util.statics.*;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import tbm.util.statics.InvalidHexException;

public class Parser implements Closeable, AutoCloseable, CharSupplier<IOException>, Cloneable {
	/**End of file, will always be -1*///because some methods depend on it;
	public static final int END = -1;
	public static char checkEnd(int c) throws EOFException {
		if (c==END)
			throw new EOFException(eof);
		return (char)c;
	}
	protected static String eof = "Unexpected end of stream.";

	public static abstract class Source implements Closeable {
		public static final int DEFAULT_EXPECTED_LINES = 10;
		public static final boolean NEWLINE_IS_WHITESPACE = true;
		public static final boolean NEWLINE_NOT_WHITESPACE = false;
		public static final boolean HASH_STARTS_COMMENT = true;
		public static final boolean HASH_NOT_COMMENT = false;

		protected final List<String> lines;
		/**Is newline a whitespace character?*/
		public final boolean newline_whitespace;
		public final boolean hash_comment_line;
		protected Source(int expectedLines, boolean newline_whitespace, boolean hash_comment_line) {
			lines = new ArrayList<>(expectedLines);
			lines.add(null);//first line starts at one, null to fail fast.
			this.newline_whitespace = newline_whitespace;
			this.hash_comment_line = hash_comment_line;
		}
		/**read a line from src*/
		public boolean read_line() throws IOException {return false;}
		@Override//Closeable
		public void close() throws IOException {do_nothing();}
		public String sourceName() {return "";}
		protected int currentLines() {return lines.size();}
		public void addLine(String line) {lines.add(line);}
		protected String get(int lineNr) {
			if (lineNr < 1)
				throw new IllegalArgumentException("1 > lineNr=".concat(String.valueOf(lineNr)));
			return lines.get(lineNr);
		}
		protected char get(int lineNr, int col) {return get(lineNr).charAt(col-1);}
	}

	public static class SourceString extends Source {
		public SourceString(String str, boolean newline_whitespace, boolean hash_comment) {
			this(str.split("\n"), newline_whitespace, hash_comment);
		}
		public SourceString(String[] lines, boolean newline_whitespace, boolean hash_comment) {
			super(lines.length, newline_whitespace, hash_comment);
			for (String line : lines)
				this.lines.add(line);
		}
	}
	public static class SourceFile extends Source {
		public final File file;
		protected BufferedReader src;
		public SourceFile(File file, int expectedLines, boolean newline_whitespace, boolean hash_comment) throws FileNotFoundException {
			super(expectedLines, newline_whitespace, hash_comment);
			this.file = Objects.requireNonNull(file);
			this.src = new BufferedReader(new FileReader(file));
		}
		public SourceFile(File file, boolean newline_whitespace, boolean hash_comment) throws FileNotFoundException {
			this(file, DEFAULT_EXPECTED_LINES, newline_whitespace, hash_comment);
		}
		public synchronized boolean read_line() throws IOException {
			if (src == null)//closed
				return false;
			String line = src.readLine();
			if (line == null) {
				close();
				return false;
			}
			addLine(line);
			return true;
		}
		/**Close the file.*/@Override//Closeable
		public synchronized void close() throws IOException {
			if (src != null) {
				src.close();
				src = null;
			}
		}
		public String sourceName() {
			return file.getPath();
		}
	}
	public static class SourceSupplier extends Source {
		public final Supplier<String> get;
		public SourceSupplier(Supplier<String> get, boolean newline_whitespace, boolean hash_comment) {
			super(DEFAULT_EXPECTED_LINES, newline_whitespace, hash_comment);
			this.get = Objects.requireNonNull(get);
		}
		@Override public boolean read_line() throws IOException {
			String line = get.get();
			if (line == null)
				return false;
			lines.add(line);
			return true;
		}
	}


	protected final Source source;
	protected int line=1, col=0;
	/**Should the int parsing methods detect other number systems?*/

	private Parser(Source base) {
		this.source = Objects.requireNonNull(base);
	}
	public Parser(Supplier<String> get, boolean newline_whitespace, boolean hash_comment) {
		this(new SourceSupplier(get, newline_whitespace, hash_comment));
	}
	public Parser(File file, int expectedLines, boolean newline_whitespace, boolean hash_comment) throws FileNotFoundException {
		this(new SourceFile(file, expectedLines, newline_whitespace, hash_comment));
	}
	public Parser(File file, boolean newline_whitespace, boolean hash_comment) throws FileNotFoundException {
		this(new SourceFile(file, newline_whitespace, hash_comment));
	}


	public Parser clone() {
		try {
			return (Parser)super.clone();
		} catch (CloneNotSupportedException cnse) {
			throw new RuntimeException("CloneNotSupportedException: ".concat(cnse.getMessage()), cnse);
		}
	}
	

	@Override//Closeable, AutoCloseable
	/**If this Parser is backed by a file or stream, it is closed, if not nothing happens*/
	public void close() throws IOException {
		source.close();
	}

	/**Read everything from the source and close it.*/
	public void read_all() throws IOException {
		while (source.read_line())
			do_nothing();
	}

	/**@return whether the source is empty*/
	public boolean isEmpty() throws IOException {
		if (line == source.currentLines())
			return !source.read_line();
		return false;
	}
	/**Skip to the next character
	 *@return this*/
	public Parser skip() throws IOException {
		if (! isEmpty()) {
			col++;
			if (col > source.get(line).length()) {
				col = 0;
				line++;
			}
		}
		return this;
	}
	/**Move to the previous character
	 *@return this*/
	public Parser back() {
		if (col == 0) {
			if (line == 1)
				throw new RuntimeException("Cannot back() from the start of a file.");
			line--;
			col = source.get(line).length();
		} else
			col--;
		return this;
	}

	/**Get the char at the current position without incrementing the position. returns -1 if there is nothing to read.*/
	public int ipeek() throws IOException {
		if (isEmpty())
			return END;
		if (col == source.get(line).length())
			return '\n';
		return source.get(line).charAt(col);
	}
	/**Get the char at the current position without incrementing the position. returns -1 if there is nothing to read.*/
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
			return source.get(line).length();
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Invalid line number: "+line+", the line might not have been read yet.");
		}
	}

	/**Check that the parameter is valid, and go to the start of the specified line.
	 *@return this*/
	public Parser setLine(int line) throws IndexOutOfBoundsException {
		if (line<=0 || line>=source.currentLines())
			throw new IndexOutOfBoundsException("Line "+line+" is < 1 or > "+source.currentLines());
		this.line = line;
		this.col = 0;
		return this;
	}

	/**check that the*/
	public Parser setCol(int col) throws IndexOutOfBoundsException {
		int length = source.get(line).length();
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
		if (p.source != this.source)
			throw new RuntimeException("The parser belong tho another source.");
		this.line = p.line;
		this.col  = p.col;
		return this;
	}


	/**skip whitespace*/
	public Parser skip_whitespace(boolean newline_whitespace, boolean hash_comment) throws IOException {
		while (true) {
			int ch = ipeek();
			while (ch==' ' || ch=='\t' || (ch=='\n' && newline_whitespace)) {
				skip();
				ch = ipeek();
			}
			if (ch == '#'  &&  hash_comment) {
				line();
				if (newline_whitespace)
					continue;
				back();
			}
			return this;
		}
	}
	public final Parser skip_whitespace() throws IOException {
		return skip_whitespace(source.newline_whitespace, source.hash_comment_line);
	}
	public final Parser sw() throws IOException {
		return skip_whitespace();
	}

	public String subString(Parser start) {
		if (start.line == line)
			if (start.col <= col)
				return source.get(line).substring( start.col, col);
			else
				throw new IllegalArgumentException("start collumn is after current collumn");
		if (start.line > line)
			throw new IllegalArgumentException("start line is after current line");
		String startLine = source.get(start.line);
		StringBuilder sb = new StringBuilder();
		sb.append(startLine, start.col, startLine.length()-start.col);
		sb.append('\n');
		for (int l=start.line+1; l<line; l++)
			sb.append(source.get(l)).append('\n');
		if (col > 0)
			sb.append(source.get(line), 0, col);
		return sb.toString();
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
		if (isEmpty())
			throw new EOFException(eof);
		String str = source.get(line).substring(col);
		line++;
		col=0;
		return str;
	}
	
	/**escape a single character and skip or require a following '*/
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

	/**read a string with escape sequences stopping at an unescaped end.
	 *  if the two first chars==end, a special mode similar to bash << input redirect is activated:
	 *  	(here stop is a string with three end chars)
	 *  	Escape sequences are ignored.
	 *  	If another stop is on the same line, stop after it.
	 *  	else if there is any more text on the line, that becomes the text terminator.
	 *  	if all lines start with the same spaces and tabs those are skipped.
	 *  	*/
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
	/**{@see escapeString()}*/
	protected String textBody(char defaultEnd) throws EOFException, IOException {
		String endx3 = String.valueOf(new char[]{defaultEnd, defaultEnd, defaultEnd});
		int startCol = col;
		String line = line();
		int endIndex = line.indexOf(endx3);
		if (endIndex != -1) {//only one line
			this.col = startCol + endIndex + endx3.length();//col is 0 after line()
			this.line--;//undo line()
			return line.substring(0, endIndex);
		}
		String stop = line.trim();
		if (stop.isEmpty())
			stop = endx3;

		line = line();
		//get the starting whitespace
		String indent = line.substring(0, String_start(line));
		boolean indented = !indent.isEmpty();

		StringBuilder sb = new StringBuilder();
		while (line.indexOf(stop) == -1) {
			sb.append(line).append('\n');
			line = line();
			if (indented && !line.startsWith(indent))
				indented = false;
		}
		sb.append(line.substring(0, line.indexOf(stop)));
		if (indented)//remove indent after each newline
			for (int i=sb.indexOf("\n");  i != -1;  i=sb.indexOf("\n", i))
				sb.delete(i+1, i+1+indent.length());

		this.col = line.indexOf(stop) + stop.length();
		this.line--;
		return sb.toString();
	}



	/***/
	public class ParseException extends Exception {
		protected ParseException(String f, Object... a) {this(String.format(f, a));}
		protected ParseException(String str) {super(str);}
		public ParseException removeLastCall() {
			return removeInnerStacks(1);
		}
		public ParseException removeInnerStacks(int remove) {
			StackTraceElement[] trace = getStackTrace();
			remove = Integer.min(remove, trace.length);
			trace = Arrays.copyOfRange(trace, remove, trace.length);
			setStackTrace(trace);
			return this;
		}
		private static final long serialVersionUID = 1L;
	}

	/**An easy way to throw a exception, prepends message with line number and column.*/
	public ParseException error(String f, Object... a) {
		return new ParseException("%s Line %d:%d: %s", source.sourceName(), line, col, String.format(f, a)).removeLastCall();
	}

	/**@return "$sourcename Line: $line, col: $col"*/@Override
	public String toString() {
		return source.sourceName() + " Line: " + line + ", col: " + col;
	}

	@Deprecated @Override//CharSupplier
	/**Similar to next(), but EOFExcetion is only thrown after EOF has been returned once.
	 *@Deprecated For implementing CharSupplier, which is used internally.*/
	public int fetch() throws EOFException, IOException {
		if (isEmpty())
			throw new EOFException(eof);
		return inext();
	}
}
