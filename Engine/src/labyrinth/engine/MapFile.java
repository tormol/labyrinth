package labyrinth.engine;
//import static tbm.util.statics.*;
import static labyrinth.engine.method.Value.*;
import java.awt.FileDialog;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import tbm.util.Parser.ParseException;
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
			while (!p.empty() && (p.peek() != '\n' || map.isEmpty()))
				map.add(p.line().toCharArray());
			TileMap.start(map);
			Script.parse_static(p);
		} catch (FileNotFoundException e) {
			throw Window.error("%s: File not Found.", path);
		} catch (EOFException e) {
			e.printStackTrace();
			throw Window.error("Unexpected end of file %s", path.toString());
		} catch (ParseException e) {
			e.printStackTrace();
			throw Window.error("Unexpected end of file %s", path.toString());
		} catch (IOException e) {
			throw Window.error("%s: Error reading file\n%s", path, e.getMessage());
		} catch (InvalidMapException e) {
			throw Window.error(e.getOffsetMessage(1));
		}


		Scope.Variable viewDistance = Script.root.search("viewDistance");
		if (viewDistance != null) {
			Value vd = viewDistance.get();
			if (vd instanceof VString  &&  vd.String().equals("disabled")  ||  vd == Value.False)
				vd = new Value.VInt(0);
			if (!(vd instanceof VInt))
				throw Window.error("The variable viewDistance is not an integer.\n");
			if (vd.Int() < 0)
				throw Window.error("viewDistance cannot be negative");
			TileMap.synsvidde( vd.Int() );
		}
	}
}
