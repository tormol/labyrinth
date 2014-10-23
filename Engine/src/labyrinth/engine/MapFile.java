package labyrinth.engine;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;


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
			throw Window.error("Ingen fil ble valgt");
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
			String line = lines.removeFirst().trim();
			if (line.trim().isEmpty()  ||  line.startsWith("#"))
				continue;
			while (line.endsWith("\\") && !lines.isEmpty()) {
				line = line.substring(0, -1) + lines.removeFirst().trim();
				MapFile.line++;
			}
			if (line.startsWith("$"))
				Constant.add(line);
			else
				Method.add( Constant.fillIn(line) );
		}
		
		TileMap.findMethods();
		MapFile.line=-1;
	}
}
