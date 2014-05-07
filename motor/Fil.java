package motor;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;


public class Fil {
	/**Brukes av feil()
	 *-1 betyr at det ikke leses noen fil, og Fil.feil() vil ikke vise noe linjenummer.*/
	public static int linje = -1;

	
	public static String velg() {
		return velg("");
	}
	/**Viser en */
	public static String velg(String sti) {
		FileDialog fd = new FileDialog(Vindu.vindu, Vindu.vindu.getTitle()+" - Velg fil", FileDialog.LOAD);
		fd.setDirectory(new File("./"+sti).getAbsolutePath());
		fd.setFile("*.txt");
		fd.setVisible(true);
		sti = new File(fd.getDirectory(), fd.getFile()).getAbsolutePath();
		if (sti == null)
			throw feil("Ingen fil ble valgt");
		return sti;
	}


	/**leser en fil og setter opp Brett, konstanter og metoder
	 * TODO: UTF-8*/
	public static void lesInn(String sti) {
		//Holder linjer i filen.
		LinkedList<String> fil = new LinkedList<String>();
		try (BufferedReader filLeser= new BufferedReader(new FileReader(sti))) {
			String linje;
			while ((linje = filLeser.readLine()) != null)
				fil.add(linje);
		} catch (FileNotFoundException e) {
			throw feil("Filen \"%s\" finnes ikke.", sti);
		} catch (IOException e) {
			throw feil("Feil under lesing av fil \"%s\"", sti);
		}
		Fil.linje = 1;

		Queue<char[]> brett = new LinkedList<char[]>();
		while (!fil.isEmpty() && !fil.getFirst().isEmpty())
			brett.add(fil.removeFirst().toCharArray());
		Brett.start(brett);
		Fil.linje++;

		while (!fil.isEmpty()) {
			String linje = fil.removeFirst().trim();
			if (linje.trim().isEmpty()  ||  linje.startsWith("#"))
				continue;
			while (linje.endsWith("\\") && !fil.isEmpty()) {
				linje = linje.substring(0, -1) + fil.removeFirst().trim();
				Fil.linje++;
			}
			if (linje.startsWith("$"))
				Konstant.add(linje);
			else
				Metode.add( Konstant.fyllInn(linje) );
			Fil.linje++;
		}

		String synsvidde = Konstant.get("synsvidde");
		if (synsvidde != null) {
			synsvidde = synsvidde.trim();
			if (synsvidde.equals("av"))
				synsvidde = "0";
			try {
				Brett.synsvidde( Integer.parseInt(synsvidde) );
			} catch (NumberFormatException e) {
				throw feil("Konstanten $synsvidde er ikke et tall\n%s", synsvidde);
			}
		}
		
		Brett.finnMetoder();
	}

	/**Legger til linjenummer før feilmeldingen.*/
	public static Vindu.FeilMelding feil(String f, Object... a) {
		if (linje == -1)
			return Vindu.feil( String.format(f, a) );
		return Vindu.feil("Linje %d: %s", linje, String.format(f, a));
	}
}
