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
	/**Brukes av feil()
	 *-1 betyr at det ikke leses noen fil, og Fil.feil() vil ikke vise noe linjenummer.*/
	public static int line = -1;

	
	public static File choose() {
		return choose("");
	}
	/**Viser en */
	public static File choose(String sti) {
		FileDialog fd = new FileDialog(Window.window, Window.window.getTitle()+" - Velg fil", FileDialog.LOAD);
		fd.setDirectory(new File("./"+sti).getAbsolutePath());
		fd.setFile("*.txt");
		fd.setVisible(true);
		String file = fd.getFile();
		if (file == null)
			throw Window.error("Ingen fil ble valgt");
		return new File(fd.getDirectory(), file);
	}


	/**leser en fil og setter opp Brett, konstanter og metoder
	 * TODO: UTF-8*/
	public static void read(File path) {
		//Holder linjer i filen.
		LinkedList<String> file = new LinkedList<String>();
		try (BufferedReader fileReader= new BufferedReader(new FileReader(path))) {
			String linje;
			while ((linje = fileReader.readLine()) != null)
				file.add(linje);
		} catch (FileNotFoundException e) {
			throw Window.error("Filen \"%s\" finnes ikke.", path);
		} catch (IOException e) {
			throw Window.error("Feil under lesing av fil \"%s\"", path);
		}
		MapFile.line = 1;

		Queue<char[]> map = new LinkedList<char[]>();
		while (!file.isEmpty() && !file.getFirst().isEmpty())
			map.add(file.removeFirst().toCharArray());
		TileMap.start(map);
		MapFile.line++;

		while (!file.isEmpty()) {
			String line = file.removeFirst().trim();
			if (line.trim().isEmpty()  ||  line.startsWith("#"))
				continue;
			while (line.endsWith("\\") && !file.isEmpty()) {
				line = line.substring(0, -1) + file.removeFirst().trim();
				MapFile.line++;
			}
			if (line.startsWith("$"))
				Constant.add(line);
			else
				Method.add( Constant.fillIn(line) );
			MapFile.line++;
		}

		String synsvidde = Constant.get("synsvidde");
		if (synsvidde != null) {
			synsvidde = synsvidde.trim();
			if (synsvidde.equals("av"))
				synsvidde = "0";
			try {
				TileMap.synsvidde( Integer.parseInt(synsvidde) );
			} catch (NumberFormatException e) {
				throw Window.error("Konstanten $synsvidde er ikke et tall\n%s", synsvidde);
			}
		}
		
		TileMap.findMethods();
		MapFile.line=-1;
	}
}
