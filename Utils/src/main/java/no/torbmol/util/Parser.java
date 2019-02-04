/* Copyright 2019 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package no.torbmol.util;
import static no.torbmol.util.statics.*;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import no.torbmol.util.statics.InvalidHexException;

/**A Scanner that is'nt based on tokens and is geared towards parsing programming languages.*/
public class Parser extends Reader implements Closeable, AutoCloseable, Cloneable {
	public static char checkEnd(int c) throws EOFException {
		if (c == -1)
			throw new EOFException(eof);
		return (char)c;
	}
	protected static final String eof = "Unexpected end of stream.";

	public static abstract class Source implements Closeable {
		public static final int DEFAULT_EXPECTED_LINES = 10;
		//More descriptive alternatives to passing true or false.
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
		/**read a line from src
		 *@return whether more lines were read.*/
		public boolean read_line() throws IOException {return false;}
		/**Read everything from the source and close it.*/
		public void read_all() throws IOException {
			while (read_line())
				do_nothing();
			close();
		}

		@Override//Closeable
		public void close() throws IOException {do_nothing();}
		public String sourceName() {return "";}
		/**get the number of lines read so far,*/
		public int currentLines() {return lines.size();}
		protected void addLine(String line) {lines.add(line);}
		/**get a line that has been read.
		 *@param lineNr is 1-indexed
		 *@throws IndexOutOfBoundException if lineNr < 0 || lineNr >= currentLines()*/
		protected String get(int lineNr) throws IndexOutOfBoundsException {
			if (lineNr == 0)
				throw new IndexOutOfBoundsException("Parser line numbers start at 1");
			return lines.get(lineNr);
		}
		/**get the nth char from a line that has been read.
		 *@param lineNr is 1-indexed
		 *@param col is 0-indexed
		 *@throws IndexOutOfBoundException if lineNr < 0 || lineNr >= currentLines() || col < 0 || col >= line length*/
		protected char get(int lineNr, int col) throws IndexOutOfBoundsException {return get(lineNr).charAt(col-1);}
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


	/**Is private as it might be removed, use getSource()*/
	private final Source source;
	protected int line=1, col=0;


	public Parser(Source s) {
		//Considered removing source field and use lock:
		//+saves memory
		//-class cast on every access (maybe optimized away by the JVM?)
		//-mutable (constructor-assigned final fields were added in 1.1, Reader is from 1.0 (en.wikipedia.org/wiki/Final_(Java)#Blank_final))
		//         If it's possible to modify javadoc of inherited fields, then I don't know how.
		//-less intuitive
		this.lock = this.source = Objects.requireNonNull(s);
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


	public final Source getSource() {
		return source;
	}

	public Parser clone() {
		try {
			return (Parser)super.clone();
		} catch (CloneNotSupportedException cnse) {
			throw new RuntimeException("CloneNotSupportedException: ".concat(cnse.getMessage()), cnse);
		}
	}
	

	/**If this Parser is backed by a file or stream, it is closed, if not nothing happens.
	 * You can continue using this parser, but no more lines can be read.
	 *@Deprecated use {@code getSource().close()} is more correct.
	 */@Deprecated @Override//Closeable, AutoCloseable
	public void close() throws IOException {
		source.close();
	}

	/**@return whether the source is empty*/
	public boolean isEmpty() throws IOException {
		if (line == source.currentLines())
			return !source.read_line();
		return false;
	}
	/**Skip to the next character
	 *@return {@code this}*/
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
			return -1;
		if (col == source.get(line).length())
			return '\n';
		return source.get(line).charAt(col);
	}
	/**Get the char at the current position without incrementing the position. returns -1 if there is nothing to read.*/
	public char peek() throws IOException, EOFException {
		int c = ipeek();
		if (c == -1)
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
		if (c == -1)
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
	 *@return {@code this}*/
	public Parser setLine(int line) throws IndexOutOfBoundsException {
		if (line<=0 || line>=source.currentLines())
			throw new IndexOutOfBoundsException("Line "+line+" is < 1 or > "+source.currentLines());
		this.line = line;
		this.col = 0;
		return this;
	}

	/**Move to (zero-indexed) column {@code col} of the current line.
	 *@return {@code this}*/
	public Parser setCol(int col) throws IndexOutOfBoundsException {
		int length = source.get(line).length();
		if (col < 0)
			this.col = length - col;
		else if (col >= length)
			throw new IndexOutOfBoundsException("col >= getLength(getLine()");
		else
			this.col = col;
		return this;
	}

	public Parser setPos(int line, int col) {
		return setLine(line).setCol(col);
	}

	/**Set line and col of to that of p.
	 *@throws IllegalArgumentException if p has a different source.
	 *@return {@code this}*/
	public Parser setPos(Parser p) throws IllegalArgumentException {
		if (p.source != this.source)
			throw new IllegalArgumentException("The parser belong tho another source.");
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

	/**Find the end of a regular expression and compile a Pattern.
	 * @throws ParseException if there is an error in the regex or an unknown flag.*/
	public Pattern regex() throws EOFException, IOException, ParseException {
		Parser start = clone();
		boolean inClass=false;//inside [] / doesn't need to be escaped
		while (true) {
			char c = next();
			if (c == '\\')
				skip();
			else if (c == '[')
				inClass = true;
			else if (c == ']')
				inClass = false;
			else if (c == '/'  && !inClass)
				break;
		}
		String body = back().subString(start);

		int flags = 0;
		while (true) switch (inext()) {
			  case'i': flags |= Pattern.CASE_INSENSITIVE; break;
			  case'm': flags |= Pattern.MULTILINE; break;
			  case'x': flags |= Pattern.COMMENTS; break;
			  case's': flags |= Pattern.DOTALL; break;
			  case'd': flags |= Pattern.UNIX_LINES; break;
			  case'u': flags |= Pattern.UNICODE_CASE; break;
			  default:
				try {
					int c = back().ipeek();
					if (Character.isLetter(c))
						throw error("%c is not a valid flag.", c);
					return Pattern.compile(body, flags);
				} catch (PatternSyntaxException pse) {
					col = start.col + pse.getIndex();
					throw error(pse.getDescription());
				}
			}
	}



	/**Syntax error.*/
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

	/**{@inheritDoc}
	 *@Deprecated use {@code inext()} as it's more in line with other methods.
	 *@return {@code inext()}
	 */@Deprecated @Override//Reader
	public int read() throws IOException {
		return inext();
	}
	@Override//Reader
	public int read(char[] buf, int offset, int length) throws IOException {
		for (int read = 0;  read < length;  read++) {
			int c = inext();
			if (c == -1)
				return read;
			buf[offset + read]  = (char)c;
		}
		return length;
	}
	@Override//Reader
	public long skip(long n) throws IOException, IllegalArgumentException {
		if (n < 0)
			throw new IllegalArgumentException("parameter is negative.");
		long toSkip = n;
		try {
			while (toSkip-- > 0)//slow but short
				skip();
		} catch (EOFException ee) {
			n = n-toSkip-1;
		}
		return n;
	}
	@Override//Reader
	public boolean ready() {
		return col != 0  ||  line < source.currentLines();
	}
}
