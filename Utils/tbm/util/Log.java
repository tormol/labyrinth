package tbm.util;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.regex.*;

public class Log {
	private LinkedList<Entry> messages = new LinkedList<Entry>();
	private PrintStream stream = null;
	public Log() {
		this(null);
	}
	public Log(PrintStream stream) {
		this.stream = stream;
	}


	public Log add(Entry e) {
		if (this.stream != null)
			this.stream.println(e.toString());
		this.messages.addLast(e);
		return this;
	}
	public Log add(type level, String message) {
		return add(new Entry(level, message));
	}
	public Log add(String level, String message) {
		type logLevel = type.set(level);
		if (logLevel == null)
			throw new IllegalArgumentException("\"" + level + "\" is not a valid loglevel.");
		return add(new Entry(logLevel, message));
	}
	public Log add(String message) {
		//http://www.rubular.com/
		Matcher matcher = Pattern.compile("\\A[ \\t]*([A-Za-z]+)[: \\t]+(\\S.*)").matcher(message);
		type level = type.set(matcher.group(1));
		if (level != null)
			message = matcher.group(2);
		return add(new Entry(level, message));
	}


	public String toString() {
		String str = "";
		for (Entry e : this.messages)
			str += "\n" + e.toString();
		return str.substring(1);
	}


	public static class Entry {
		public final type level;
		public final String message;

		public Entry(type level, String message) {
			if (message == null)
				throw new IllegalArgumentException("message cannot be null.");
			this.level = level;
			this.message = message.trim();
		}

		public String toString() {
			String level = (this.level==null ? "null" : this.level.toString()) + ":\t";
			String[] line = this.message.split("\n");
			String str = level + line[0];
			String spaces = tbm.util.strings.nchars(level.length()-1, ' ');
			for (int i=1; i<line.length; i++)
				str += "\n" + spaces + "\t" + line[i];
			return str;
		}
	}


	public static enum type {
		ERROR("Error"), WARN("Warning"), INFO("Info"), DEBUG("Debug");

		type(String str)
			{}

		public static type set(String str) {
			switch (str.trim().toLowerCase()) {
				case "error": return ERROR;
				case "warn": return WARN;
				case "info": return INFO;
				case "debug": return DEBUG;
				default: return null;
			}
		}
	}
}
