import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import tbm.util.DryOpts;

public class cat {
	static String prepend = null;
	static boolean append = false;
	static PrintStream out = System.out;
	static String outFile = null;
	public static void main(String[] args) {
		DryOpts ap = new DryOpts(args);
		prepend = ap.optStr('p', "header", "Add this before every file.");
		outFile = ap.optStr('o', "out", "Write to this file.");
		append = ap.optFlag('a', "append", "Append to the file specified in out.");
		ap.handle_version('v', "javaCat version 1");
		boolean help = ap.optFlag('h', "help", "display help and exit");
		args = ap.allArgs("files to read", false);
		if (help) {
			System.out.println(ap.getHelp(false, "Concatenate files"));
			return;
		}
		ap.handle_errors(1);

		try {
			if (outFile != null  &&  !outFile.equals("-"))
				new PrintStream(outFile);
			for (String file : args)
				readFile(file);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			out.close();
		}
	}

	static boolean readFile(String file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			if (prepend != null)
				out.println( prepend.replaceAll("\\$\\$", file) );
			String line;
			while ((line = reader.readLine()) != null)
				out.println(line);
		} catch (FileNotFoundException e) {
			System.err.format("%s, skipping\n", e.getMessage());
			return false;
		}
		return true;
	}
}
