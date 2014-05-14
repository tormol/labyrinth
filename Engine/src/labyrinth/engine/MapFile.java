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
	public static int linje = -1;

	
	public static File velg() {
		return velg("");
	}
	/**Viser en */
	public static File velg(String sti) {
		FileDialog fd = new FileDialog(Window.vindu, Window.vindu.getTitle()+" - Velg fil", FileDialog.LOAD);
		fd.setDirectory(new File("./"+sti).getAbsolutePath());
		fd.setFile("*.txt");
		fd.setVisible(true);
		System.out.format("%s %s\n", fd.getDirectory(), fd.getFile());
		String file = fd.getFile();
		if (file == null)
			throw feil("Ingen fil ble valgt");
		return new File(fd.getDirectory(), file);
	}


	/**leser en fil og setter opp Brett, konstanter og metoder
	 * TODO: UTF-8*/
	public static void lesInn(File sti) {
		//Holder linjer i filen.
		LinkedList<String> fil = new LinkedList<String>();
		try (BufferedReader filLesynsvidde= new BufferedReader(new FileReader(sti))) {
			String linje;
			while ((linje = filLesynsvidde.readLine()) != null)
				fil.add(linje);
		} catch (FileNotFoundException e) {
			throw feil("Filen \"%s\" finnes ikke.", sti);
		} catch (IOException e) {
			throw feil("Feil under lesing av fil \"%s\"", sti);
		}
		MapFile.linje = 1;

		Queue<char[]> brett = new LinkedList<char[]>();
		while (!fil.isEmpty() && !fil.getFirst().isEmpty())
			brett.add(fil.removeFirst().toCharArray());
		TileMap.start(brett);
		MapFile.linje++;

		while (!fil.isEmpty()) {
			String linje = fil.removeFirst().trim();
			if (linje.trim().isEmpty()  ||  linje.startsWith("#"))
				continue;
			while (linje.endsWith("\\") && !fil.isEmpty()) {
				linje = linje.substring(0, -1) + fil.removeFirst().trim();
				MapFile.linje++;
			}
			if (linje.startsWith("$"))
				Constant.add(linje);
			else
				Method.add( Constant.fyllInn(linje) );
			MapFile.linje++;
		}

		String synsvidde = Constant.get("synsvidde");
		if (synsvidde != null) {
			synsvidde = synsvidde.trim();
			if (synsvidde.equals("av"))
				synsvidde = "0";
			try {
				TileMap.synsvidde( Integer.parseInt(synsvidde) );
			} catch (NumberFormatException e) {
				throw feil("Konstanten $synsvidde er ikke et tall\n%s", synsvidde);
			}
		}
		
		TileMap.finnMetoder();
	}

	/**Legger til linjenummer før feilmeldingen.*/
	public static Window.FeilMelding feil(String f, Object... a) {
		if (linje == -1)
			return Window.feil( String.format(f, a) );
		return Window.feil("Linje %d: %s", linje, String.format(f, a));
	}
}
