package labyrinth.engine;
//import static statics.*;
import java.awt.FileDialog;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import no.torbmol.util.Parser.ParseException;
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
		if (file == null) {
			throw Window.error("No file selected.");
		}
		return new File(fd.getDirectory(), file);
	}


	/**Read a map file and set up TileMap, Constant and Method
	 *TODO: UTF-8*/
	public static void read(File path) {
		try (Parser p = new Parser(path)) {
			Queue<char[]> map = new LinkedList<char[]>();
			while (!p.isEmpty() && (p.peek() != '\n' || map.isEmpty())) {
				map.add(p.line().toCharArray());
			}
			TileMap.start(map);
			new Script(p, path.getName(), StandardLibrary.get(), LabyrinthLibrary.get());
		} catch (FileNotFoundException e) {
			throw Window.error("%s: File not Found.", path);
		} catch (EOFException e) {
			e.printStackTrace();
			throw Window.error("Unexpected end of file %s", path.toString());
		} catch (ParseException e) {
			e.printStackTrace();
			throw Window.error(e.getMessage());
		} catch (IOException e) {
			throw Window.error("%s: Error reading file\n%s", path, e.getMessage());
		} catch (InvalidMapException e) {
			throw Window.error(e.getOffsetMessage(1));
		}
	}
}
