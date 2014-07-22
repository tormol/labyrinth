package labyrinth.engine;
import static tbm.util.statics.*;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import labyrinth.engine.TileMap.InvalidMapException;
import labyrinth.engine.method.*;


public class MapFile {
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
		try (Parser p = new Parser(path)) {
			Queue<char[]> map = new LinkedList<char[]>();
			while ((map.isEmpty() || !p.empty()) && p.peek() != '\n')
				map.add(p.line().toCharArray());
			TileMap.start(map);
			parse_Procedure(p, null);
		} catch (FileNotFoundException e) {
			throw Window.error("%s: File not Found.", path);
		} catch (IOException e) {
			throw Window.error("%s: Error reading file\n%s", path, e.getMessage());
		} catch (InvalidMapException e) {
			throw Window.error(e.getOffsetMessage(1));
		}

/*
		while (!lines.isEmpty()) {
			MapFile.line++;
			StringStream l = new StringStream(lines.removeFirst());
			l.whitespace();
			if (l.empty())
				continue;
			
			String name = l.next(c -> char_word(c));
			if (name.isEmpty())
				Window.error("not a method");
			if (l.next_nw() != ':')
				Window.error("Method %s: not a method", name);

			while (line.endsWith("\\") && !lines.isEmpty()) {
				line = line.substring(0, -1) + lines.removeFirst().trim();
				MapFile.line++;
			}
			if (line.startsWith("$"))
				Constant.add(line);
			else
				Procedure.add( Constant.fillIn(line) );
		}*/

		/*TODO: uncumment when new system is finnished.*/
		Variable wiewDistance = Script.getVar("viewDistaance");
		if (wiewDistance != null) {
			wiewDistance. = wiewDistance.trim();
			if (wiewDistance.equals("av"))
				wiewDistance = "0";
			try {
				TileMap.synsvidde( Integer.parseInt(wiewDistance) );
			} catch (NumberFormatException e) {
				throw Window.error("The Constant $synsvidde is not a number.\n%s", wiewDistance);
			}
		}
	}
}
