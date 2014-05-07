import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tbm.util.ArgsParser;


public class PrimitivePreProcessor {
	//Modes
	static boolean
		clean = false,//remove generated files instead of writing them
		append_new = true;//only write new files

	static String help =
	"Generate java files based on other files.\n" +
	"Syntax:\n"+
	"//&class = classname1,classname2,...\t\t//the first var is used for filenames\n" +
	"//&name = file1str,file2str,...\t\t(all lists must have equal size)\n" +
	"//comment that will not appear in generated files\n" +
	"//&name = *1, *2, ...\t\t(glob: name1, name2, ...)\n" +
	"import something\t\t(this line and everything after is written to the generated files)\n";
	static String version =
	"PrimitivePreProcessor v1\n" +
	"Copyright Torbjørn Birch Moltu\n" +
	"GPL version 2";
	public static void main(String[] args) {
		//init types, create files, open javap 
		ArgsParser ap = new ArgsParser(args);
		clean = ap.optFlag('c', "clean", "Remove the files that would normally been created.");
		append_new = ap.optFlag('n', "--append_new", "Only print new files");
		ap.handle_version(version);
		ap.handle_help(help);
		args = ap.getArgs();
		ap.handle_errors(1);

		try {
			for (String path : args) {
				File f = new File(path);
				if (f.isDirectory())
					System.err.format("%s is a directory.", f);
				else if (f.getName().endsWith(".java"))
					replaceFile(f);
				else
					System.err.format("%s unnsupported format", f);
			}
		} catch (AnError e) {
			System.err.println(e.getMessage());
		}
	}


	static void replaceFile(File f) throws AnError {
		ArrayList<Variable> words = new ArrayList<Variable>(10);
		try (
				BufferedReader src = new BufferedReader(new FileReader(f));
				FileList to = new FileList()
			) {
			String line;
			while ((line = src.readLine()) != null  &&  line.startsWith("//"))
				parse_variable_line( words, line.substring(2) );
			while (line != null  &&  line.isEmpty())
				line = src.readLine();

			if (words.isEmpty())
				System.err.println(f+": no variables, skipping.");
			else if (clean) {
				for (String className : words.get(0)) {
					File file = new File(f.getParentFile(), className + ".java");
					System.out.println(file);
					if (file.exists())
						if (!file.delete())
							System.err.format("Unable to delete file %s", file);
				}
			}
			else if (line==null)
				System.err.println(f+": no body, skipping.");
			else {
				Variable classNames = words.get(0);
				for (int i=0; i<classNames.size(); i++)
					to.add( new JavaFile(f.getParent(), i, classNames) )
							.open().writeln("//Generated from " + f.getName());

				do {//the last lire read from  
					SortedSet<Part> parts = parse_body(line, words);
					for (JavaFile jf : to)
						jf.writeln(line, parts);
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


	static void parse_variable_line(List<Variable> variables, String line) throws AnError {
		if (line.startsWith("&")) {
			Matcher m = Pattern.compile("^&\\s*(\\w[\\w\\.\\d]*)\\s*=\\s*(.*)$").matcher(line);
			if (!m.matches())
				throw new AnError(false, "Invalid variable line: %s", line);
			String name = m.group(1);
			String[] v = m.group(2).split(",");
			if (variables.isEmpty()) {
				for (int i=0; i<v.length; i++) {
					v[i] = v[i].trim();
					if (!v[i].matches("\\w+"))
						throw new AnError(false, "%s is not a valid className.", v[i]);
				}
			} else {
				int size = variables.get(0).size();
				if (v.length == size)
					for (int i=0; i<v.length; i++)
						v[i] = v[i].trim().replaceAll("\\*", name);
				else 
					throw new AnError(false, "The variable %s does not have %d values.", name, size);
			}
			variables.add(new Variable(name, v));
		}
	}


	/**Fills in variables*/
	static SortedSet<Part> parse_body(String text, List<Variable> variables) {
		SortedSet<Part> parts = new TreeSet<>();
		for (Variable v : variables) {
			int start, end = 0;
			while ((start = text.indexOf(v.name, end))  !=  -1) {
				end = start + v.name.length();
				if (isWord(text, start-1)  ||  isWord(text, end))
					continue;
				parts.add(new Part(start, v, end)); //automatically sorted
			}
		}
		return parts;
	}


	/**Is index a valid index of str, and does charAt(index) match the regex "[A-Z_a-z]"?*/
	static boolean isWord(String str, int index) {
		if (index<0 || index>=str.length())
			return false;
		char c = str.charAt(index);
		return ((c>='0' && c<='9')  ||  (c>='a' && c<='z')  ||  (c>='A' && c<='Z')  ||  c=='_');
		//ascii table: 0-9<A-Z<_<a-z
		//return (c>='A' && c<='z'  && (c<='Z' || c=='_' || c>='A'));
	}


	static class Variable implements Iterable<String> {
		public final String name;
		private final String[] value;
		public Variable(String name, String[] values) {
			this.name=name;
			value=values;
		}
		public String get(int index) {
			return value[index];
		}
		public int size() {
			return value.length;
		}
		@Override//Iterable
		public Iterator<String> iterator() {
			return Arrays.asList(value).iterator();
		}
	}


	static class Part implements Comparable<Part> {
		public final int start, end;
		public final Variable var;
		public Part(int start, Variable v, int end) {
			this.start = start;
			this.var = v;
			this.end = end;
		}
		@Override//Comparable
		public int compareTo(Part p) {
			return start-p.start;
		}
	}


	static class JavaFile {
		public BufferedWriter writer = null;
		public final File file;
		public final int index;
		JavaFile(String path, int index, Variable classNames) {
			this.index = index;
			file = new File(path, classNames.get(index) + ".java");
			if (!(append_new && file.exists()))
				System.out.println(file);
		}
		JavaFile open() throws IOException {
			writer = new BufferedWriter(new FileWriter(file));
			return this;
		}
		JavaFile writeln(String s) throws IOException {
			writer.write(s);
			writer.newLine();
			return this;
		}
		JavaFile writeln(String line, SortedSet<Part> parts) throws IOException {
			int prev = 0;
			for (Part p : parts) {
				writer.write(line.substring(prev, p.start) + p.var.get(index));
				prev = p.end;
			}
			writeln(line.substring(prev));
			return this;
		}
	}


	static class FileList implements AutoCloseable, Iterable<JavaFile> {
		ArrayList<JavaFile> files = new ArrayList<>(10);
		public FileList()
			{}
		public JavaFile add(JavaFile jf) {
			files.add(jf);
			return jf;
		}
		@Override//Iterable
		public Iterator<JavaFile> iterator() {
			return files.iterator();
		}
		@Override//AutoCloseable
		public void close() throws AnError {
			LinkedList<IOException> ioe = new LinkedList<>();
			for (JavaFile jf : files)
				if (jf.writer != null)
					try {
						jf.writer.close();
						jf.writer = null;
					} catch (IOException e) {
						System.err.format("Closing file \"%s\" failed.\n", jf.file);
						ioe.add(e);
					}
			if (!ioe.isEmpty()) {
				AnError master = new AnError(false, "Error closing some files");
				for (IOException e : ioe)
					master.addSuppressed(e);
				throw master;
			}
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
