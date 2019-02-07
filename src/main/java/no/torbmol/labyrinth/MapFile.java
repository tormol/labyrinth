package no.torbmol.labyrinth;

import no.torbmol.labyrinth.TileMap.InvalidMapException;
import no.torbmol.labyrinth.method.*;
import no.torbmol.util.Parser.ParseException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.WindowConstants;
import java.awt.FileDialog;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedTransferQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MapFile {
	/**file name without directories or file extension*/
	private static String nameOfMap(String path) {
		int start = Integer.max(path.lastIndexOf('/')+1, path.lastIndexOf('\\')+1);
		int end = path.lastIndexOf('.');
		if (end < start) {
			end = path.length();
		}
		return path.substring(start, end);
	}

	/**Get list of path to bundled maps.
	 * Has to manually open the jar when run from one*/
	private static List<Path> bundledMaps() {
		URL dir = Labyrinth.class.getResource("/maps/");
		System.out.println(dir);
		Path dirPath = null;
		if (dir.getProtocol().equals("jar")) {
			int sep = dir.toString().lastIndexOf('!');
			URI toJar = URI.create(dir.toString().substring(0, sep));
			String inJar = dir.toString().substring(sep+1);
			try {
				// leak the file system - must not be closed before Parser is done reading the map file
				FileSystem jar = FileSystems.newFileSystem(toJar, new HashMap<>());
				dirPath = jar.getPath(inJar);
			} catch (IOException e) {
				throw Window.error("Internal error: Cannot open jar: " + e.getMessage());
			}
		} else {
			dirPath = Path.of(URI.create(dir.toString()));
		}
		List<Path> files = new ArrayList<>();
		try (DirectoryStream<Path> opened = Files.newDirectoryStream(dirPath, "*.txt")) {
			for (Path p : opened) {
				files.add(p);
			}
		} catch (IOException e) {
			throw Window.error("Internal error: cannot get list of maps: " + e.getMessage());
		}
		return files;
	}

	/**Displays the window with a clickable list and a button*/
	private static String chooseString(String title, String other, String... alternatives) {
		LinkedTransferQueue<String> queue = new LinkedTransferQueue<>();
		JList<String> list = new JList(alternatives);
		list.setLayoutOrientation(JList.VERTICAL);
		list.addListSelectionListener(e -> queue.add(list.getSelectedValue()));
		JButton button = new JButton(other);
		button.addActionListener(a -> queue.add(""));
		JFrame window = new JFrame(title);
		window.getContentPane().setLayout(
			new BoxLayout(window.getContentPane(), BoxLayout.PAGE_AXIS)
		);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.add(list);
		window.add(button);
		window.pack();
		window.setVisible(true);
		String result = null;
		while (result == null) {
			try {result = queue.take();}
			catch (InterruptedException e) {}
		}
		window.dispose();
		return result;
	}

	/**Display a window with a list of the included maps and a button to open a file*/
	public static Parser selectBundled() {
		List<Path> paths = bundledMaps();
		HashMap<String,Path> maps = new HashMap<>();
		String[] names = new String[paths.size()];
		for (int i=0; i<paths.size(); i++) {
			names[i] = nameOfMap(paths.get(i).toString());
			maps.put(names[i], paths.get(i));
		}
		String map = chooseString("Choose map", "browse", names);
		if (map.isEmpty()) {
			return browse("");// current directory
		}
		try {
			return new Parser(Files.newInputStream(maps.get(map)), map+".txt");
		} catch (IOException e) {
			throw Window.error("%s: Error reading file\n%s", map, e.getMessage());
		}
	}

	/**Open one of the included maps*/
	public static Parser openBundled(String name) {
		InputStream in = Labyrinth.class.getResourceAsStream("/maps/"+name+".txt");
		if (in == null) {
			System.err.println("Unknown map.");
			System.exit(1);
		}
		return new Parser(in, name+".txt");
	}


	/**Display a file dialog and return the chosen file.
	 *@param dir directory to view in the file dialog*/
	public static Parser browse(String dir) {
		FileDialog fd = new FileDialog(Window.window, Window.window.getTitle()+" - Choose map", FileDialog.LOAD);
		fd.setDirectory(new File("./"+dir).getAbsolutePath());
		fd.setFile("*.txt");
		fd.setVisible(true);
		String file = fd.getFile();
		if (file == null) {
			throw Window.error("No file selected.");
		}
		return fromFile(new File(fd.getDirectory(), file));
	}

	/**Read a map file*/
	public static Parser fromFile(File file) {
		try {
			return new Parser(file);
		} catch (FileNotFoundException e) {
			throw Window.error("%s: File not Found.", file);
		}
	}
	
	/**Sets up TileMap, Constant and Method, and starts*/
	public static void start(Parser p) {
		String name = nameOfMap(p.getSource().sourceName());
		try (p) {
			Queue<char[]> map = new LinkedList<char[]>();
			while (!p.isEmpty() && (p.peek() != '\n' || map.isEmpty())) {
				map.add(p.line().toCharArray());
			}
			TileMap.start(map);
			new Script(p, name, StandardLibrary.get(), LabyrinthLibrary.get());
		} catch (EOFException e) {
			e.printStackTrace();
			throw Window.error("Unexpected end of file %s", p.getSource().sourceName());
		} catch (ParseException e) {
			e.printStackTrace();
			throw Window.error(e.getMessage());
		} catch (IOException e) {
			throw Window.error("%s: Error reading file\n%s", p.getSource().sourceName(), e.getMessage());
		} catch (InvalidMapException e) {
			throw Window.error(e.getOffsetMessage(1));
		}
	}
}
