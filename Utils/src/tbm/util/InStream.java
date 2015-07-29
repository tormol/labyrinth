package tbm.util;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**Looks like a bunch of functions for working with raw InputStreams*/
public class InStream {
	public static int skip_cr(InputStream is) throws IOException {
		int c = is.read();
		return c=='\r' ? is.read() : c;
	}

	public static String line(InputStream is) throws IOException {
		int c = skip_cr(is);
		if (c==-1)
			return null;
		if (c=='\n')
			return "";
		StringBuilder str = new StringBuilder();
		do {
			str.append(c);
			c = skip_cr(is);
		} while (c != -1 &&  c != '\n');
		return str.toString();
	}

	public static class LineIterator implements Iterator<String>, Iterable<String> {
		public final InputStream is;
		public LineIterator(InputStream is) {
			this.is = is;
		}
		private String next = null;
		@Override//Iterable
		public Iterator<String> iterator() {
			return this;
		}

		@Override//Iterator
		public boolean hasNext() {
			if (next != null)
				return true;
			next = read();
			return (next != null);
		}

		@Override
		public String next() {
			if (next == null)
				return read();
			String tmp = next;
			next = null;
			return tmp;
		}

		private String read() {
			try {
				return InStream.line(is);
			} catch (IOException e) {
				throw new IORuntimeException(e);
			}
		}
	}
}
