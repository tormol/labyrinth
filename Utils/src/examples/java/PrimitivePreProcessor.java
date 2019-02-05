/* Copyright 2019 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.torbmol.util.DryOpts;

public class PrimitivePreProcessor {
	//Modes
	static boolean
		clean = false,//remove generated files instead of writing them
		append_new = true;//only write new files

	static final int ARRAYLIST_LENGTHT = 10;
	static final String helpStr =
	"Generate java files based on other files.\n" +
	"Syntax:\n"+
	"//&class = classname1,classname2,...\t\t//the first var is used for filenames\n" +
	"//&name = file1str,file2str,...\t\t(all lists must have equal size)\n" +
	"//comment that will not appear in generated files\n" +
	"//&name = *1, *2, ...\t\t(glob: name1, name2, ...)\n" +
	"import something\t\t(this line and everything after is written to the generated files)\n";
	static final String versionStr =
	"PrimitivePreProcessor v1\n" +
	"Copyright Torbjørn Birch Moltu\n" +
	"Licensed under the Apache version 2.0 license or MIT license";
	public static void main(String[] args) {
		DryOpts ap = new DryOpts(args);
		clean = ap.optFlag('c', "clean", "Remove the files that would normally been created.");
		append_new = ap.optFlag('n', "append_new", "Only print new files");
		ap.handle_version('v', versionStr);
		boolean help = ap.optFlag('h', "help", "Display help and exit");
		args = ap.allArgs("source files", true);
		if (help) {
			System.out.println(ap.getHelp(false, helpStr));
			return;
		}
		ap.handle_errors(1);

		try {
			for (String path : args) {
				File f = new File(path);
				if (f.isDirectory())
					System.err.format("%s is a directory.", f);
				else if (f.getName().endsWith(".java"))
					replaceFile(f);
				else
					System.err.format("%s: unnsupported format", f);
			}
		} catch (AnError e) {
			System.err.println(e.getMessage());
		}
	}


	static void replaceFile(File f) throws AnError {
		WordList words = new WordList();
		try (
				BufferedReader src = new BufferedReader(new FileReader(f));
				FileList to = new FileList()
			) {
			String line;
			while ((line = src.readLine()) != null  &&  line.startsWith("//"))
				words.parse( line.substring(2) );
			while (line != null  &&  line.isEmpty())
				line = src.readLine();

			if (words.isEmpty())
				System.err.println(f+": no variables, skipping.");
			else if (clean) {
				for (String className : words.classNames) {
					File file = javaFile(f, className);
					System.out.println(file);
					if (file.exists())
						if (!file.delete())
							System.err.format("Unable to delete file %s", file);
				}
			}
			else if (line==null)
				System.err.println(f+": no body, skipping.");
			else {
				for (String cn : words.classNames)
					to.add( new Writer( javaFile(f, cn) )
						.writeln("//Generated from " + f.getName()) );

				do {//the last line read from
					Replacer r = words.replacer(line);
					for (int i=0; i<to.size(); i++)
						to.get(i).writeln(r.replace(i));
				} while ((line = src.readLine()) != null);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Source file "+f+" not found, skipping.\n"+e.getMessage());
		} catch (AnError e) {
			String msg = f + ": " + e.getMessage();
			if (e.exit)
				throw new AnError(e, msg);
			System.err.println(msg);
		} catch (IOException e) {
			throw new AnError(true, e.getMessage());
		}
	}


	@SuppressWarnings("serial")
	static class WordList extends HashMap<String, String[]> {
		public String[] classNames = null;
		public WordList() {
			super(ARRAYLIST_LENGTHT);
		}

		public void parse(String line) throws AnError {
			if (line.startsWith("&")) {
				final String _name = "\\w[\\w\\d]*";
				Matcher m = Pattern.compile("^&\\s*("+_name+"(\\."+_name+")?)\\s*=").matcher(line);
				if (!m.matches())
					throw new AnError(false, "Invalid variable line: %s", line);
				String name = m.group(1);
				String[] v = m.group(2).split(",");
				if (isEmpty()) {
					for (int i=0; i<v.length; i++) {
						v[i] = v[i].trim();
						if (!v[i].matches("\\w+"))
							throw new AnError(false, "%s is not a valid className.", v[i]);
					}
					classNames = v;
				} else {
					int size = classNames.length;
					if (v.length == size)
						for (int i=0; i<v.length; i++)
							v[i] = v[i].trim().replaceAll("\\*", name);
					else
						throw new AnError(false, "The variable %s does not have %d values.", name, size);
				}
				put(name, v);
			}
		}

		Replacer replacer(String text) {
			return new Replacer(this, text);
		}
	}


	static class Replacer {
		public final char[] text;
		private SortedSet<Part> parts = new TreeSet<>();
		/**Fills in variables*/
		public Replacer(WordList words, String text) {
			this.text = text.toCharArray();
			for (Entry<String, String[]> v : words.entrySet()) {
				int start, end = 0;
				while ((start = text.indexOf(v.getKey(), end))  !=  -1) {
					end = start + v.getKey().length();
					if (isWord(start-1, text)  ||  isWord(end, text))
						continue;
					parts.add(new Part(start, v.getValue(), v.getKey().length())); //automatically sorted
				}
			}
		}

		/**Is index a valid index of str, and does charAt(index) match the regex "[A-Z_a-z]"?*/
		private boolean isWord(int index, String str) {
			if (index<0 || index>=str.length())
				return false;
			final char c = str.charAt(index);
			return ((c>='0' && c<='9')  ||  (c>='a' && c<='z')  ||  (c>='A' && c<='Z')  ||  c=='_');
		}

		public String replace(int index) {
			StringBuilder str = new StringBuilder(text.length);
			int prev = 0;
			for (Part p : parts) {
				str.append(text, prev, p.start-prev);
				str.append(p.var[index]);
				prev = p.start + p.length;
			}
			str.append(text, prev, text.length-prev);
			return str.toString();
		}

		private static class Part implements Comparable<Part> {
			public final int start, length;
			public final String[] var;
			public Part(int start, String[] v, int length) {
				this.start = start;
				this.var = v;
				this.length = length;
			}
			@Override//Comparable
			public int compareTo(Part p) {
				return start - p.start;
			}
		}
	}


	public static File javaFile(File path, String className) {
		if (path.isFile())
			path = path.getParentFile();
		return new File(path, className + ".java");
	}


	static class Writer extends BufferedWriter {
		public final File file;
		Writer(File file) throws IOException {
			super(new FileWriter(file));
			this.file = file;
			if (!(append_new && file.exists()))
				System.out.println(file);
		}
		Writer writeln(String s) throws IOException {
			write(s);
			newLine();
			return this;
		}
	}


	@SuppressWarnings("serial")
	static class FileList extends ArrayList<Writer> implements AutoCloseable {
		public FileList() {
			super(ARRAYLIST_LENGTHT);
		}
		@Override//AutoCloseable
		public void close() throws AnError {
			AnError err = new AnError(false, "");
			for (Writer w : this)
				try {
					w.close();
				} catch (IOException e) {
					System.err.format("Closing file \"%s\" failed.\n", w.file);
					err.addSuppressed(e);
				}
			if (err.getSuppressed().length > 0)
				throw new AnError(err, "Error closing some files");
		}
	}


	@SuppressWarnings("serial")
	static class AnError extends Exception {
		public final boolean exit;
		public AnError(boolean exit, String f, Object... p) {
			super(String.format(f, p));
			this.exit = exit;
		}
		public AnError(AnError t, String f, Object... p) {
			super(String.format(f, p), t);
			exit = t.exit;
		}
	}
}
