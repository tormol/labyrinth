package labyrinth.engine;

import static tbm.util.statics.char_word;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import tbm.util.StringStream;
import labyrinth.engine.method.Constant;
import labyrinth.engine.method.Method;
import labyrinth.engine.method.Operation;
import labyrinth.engine.method.Procedure;
import labyrinth.engine.method.Value;


public class MapFile {
	/**Used by Window.error()
	 *-1 == not reading a file, so don't show a line number in error messages.*/
	public static int line = -1;

	
	public static File choose() {
		return choose("");
	}
	/**Display a file dialog and return the chosen file.
	 *@param dir directory to view in the file dialog*/
	public static File choose(String dir) {
		FileDialog fd = new FileDialog(Window.window, Window.window.getTitle()+" - Velg fil", FileDialog.LOAD);
		fd.setDirectory(new File("./"+dir).getAbsolutePath());
		fd.setFile("*.txt");
		fd.setVisible(true);
		String file = fd.getFile();
		if (file == null)
			throw Window.error("No file selected.");
		return new File(fd.getDirectory(), file);
	}


	/**Read a map file and set up TileMap, Constant and Method
	 *TODO: UTF-8*/
	public static void read(File path) {
		LinkedList<String> lines = new LinkedList<String>();
		try (BufferedReader fileReader= new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = fileReader.readLine()) != null)
				lines.add(line);
		} catch (FileNotFoundException e) {
			throw Window.error("%s: File not Found.", path);
		} catch (IOException e) {
			throw Window.error("%s: Error reading file\n%s", path, e.getMessage());
		}
		MapFile.line = 1;

		Queue<char[]> map = new LinkedList<char[]>();
		while (!lines.isEmpty() && !lines.getFirst().isEmpty())
			map.add(lines.removeFirst().toCharArray());
		TileMap.start(map);

		while (!lines.isEmpty()) {
			MapFile.line++;
			StringStream l = new StringStream(lines.removeFirst());
			l.whitespace();
			if (l.empty())
				continue;
			
			String name = l.next(c -> char_word(c));
			if (name.isEmpty())
				Window.error("not a method");
			if (l.next_nw() == ':')
				Window.error("Method %s: not a method", name);

			while (line.endsWith("\\") && !lines.isEmpty()) {
				line = line.substring(0, -1) + lines.removeFirst().trim();
				MapFile.line++;
			}
			if (line.startsWith("$"))
				Constant.add(line);
			else
				Procedure.add( Constant.fillIn(line) );
		}

		String wiewDistance = Constant.get("synsvidde");
		if (wiewDistance != null) {
			wiewDistance = wiewDistance.trim();
			if (wiewDistance.equals("av"))
				wiewDistance = "0";
			try {
				TileMap.synsvidde( Integer.parseInt(wiewDistance) );
			} catch (NumberFormatException e) {
				throw Window.error("The Constant $synsvidde is not a number.\n%s", wiewDistance);
			}
		}
		
		MapFile.line = -1;
		Procedure.checkUndefined();
	}


	private static void parse_Procedure(String name, StringStream l) {
		l.whitespace();

		ArrayList<Operation> ops = new ArrayList<>(10);
		while (!l.empty()) {
			if (l.peek()=='#')
				break;
			try {
				String op_name = l.next( c->char_word(c) );
				if (op_name.isEmpty())
					throw Window.error("Method %s: operation expected", name);
				Method f = Method.get(op_name);
				if (f==null)
					throw Window.error("Method %s: Unknown operation %s.", name, op_name);
				if (l.next_nw() != '(')
					Window.error("Method %s: '(' expected after \"%s\".", name, op_name);
				Value[] params = new Value[f.parameters.length];
				int i=0;
				while (i<params.length) {
					params[i] = f.parameters[i].parse(l); 
					i++;
					if (i < params.length  &&  l.next_nw() != ',')
						throw Window.error("Method %s: ',' expected after %i. argument.", name, i);
				}
				if (l.next_nw() != ')')
					throw Window.error("Method %s: ')' expected after %i. argument.", name, i);
				if (l.next_nw() != ';')
					throw Window.error("Method %s: ';' expected after a method (%s(", name, op_name);
				ops.add(f.instance(params));
			} catch (ArrayIndexOutOfBoundsException e) {
				throw Window.error("", "Method %s: unexpected end of line.", name);
			}
			l.whitespace();
			//TODO: hvis koordinater mangler vil metoden kjøres på feltet som startet funksjonen.
		}
		Procedure.define(name, ops);
	}
}
