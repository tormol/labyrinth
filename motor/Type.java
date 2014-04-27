package motor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**Siden ruter kan skifte type, og det ikke er mulig å erstatte komponenter i GridLayout*/
public class Type {
	private static List<Type> typer = new ArrayList<Type>(20);

	public static Type get(char tegn) {
		for (Type type : typer)
			if (type.tegn(tegn))
				return type;
			return null;
	}
	public static Type get(String navn) {
		for (Type type : typer)
			if (navn.equalsIgnoreCase(type.navn))
				return type;
		return null;
	}

	/**Returnerer den første typen som har tegnet tegn
	 * Gir en feilmelding hvis den ikke finner noen.*/
	public static Type _(char tegn) {
		Type type = get(tegn);
		if (type == null)
			throw Vindu.feil("Ukjent type '%c'", tegn);
		return type;
	}
	/**Returnerer den første typen med navn nanv
	 * Gir en feilmelding hvis den ikke finner noen.*/
	public static Type _(String navn) {
		Type type = get(navn);
		if (type == null)
			throw Vindu.feil("Ukjent type \"%s\"", navn);
		return type;
	}

	/**Lager en ny type, og legger den i listen.*/
	public static Type add(String navn, boolean solid, boolean metode, String bildefil, Color farge, String tegn) {
		Type type = new Type(navn, solid, metode, bildefil, farge, tegn);
		typer.add(type);
		return type;
	}




	/***/
	public final String navn;
	/**Bakgrunnsfarge*/
	public final Color farge;
	public final BufferedImage bilde;
	/**Enheter kan ikke flytte til solide ruter*/
	public final boolean solid;
	/**Kan felt av denne typen ha en metode*/
	public final boolean metode;
	/**Hvilke tegn angir denne typen*/
	private final char[] tegn;
	protected Type(String navn, boolean solid, boolean metode, String bildefil, Color farge, String tegn) {
		this.navn=navn;
		this.tegn=tegn.toCharArray();
		this.farge=farge;
		this.solid=solid;
		this.metode=metode;
		BufferedImage bilde = null;
		if (bildefil != null)
			try {
				bilde = ImageIO.read(new File(bildefil));
			} catch (IOException e) {
				throw Vindu.feil(
						"Feil under lasting av bildefil \"%s\" til RuteType %s:\n"
						+"Det kan skyldes at working directory ikke er satt til pakkeNavnet\n"+"%s",
						bildefil, navn, e.getMessage()
					);
			}
		this.bilde=bilde;
	}

	public char[] getTegn() {
		return tegn.clone();
	}

	/**Er en rute angitt med tegn denne typen?*/
	public boolean tegn(char tegn) {
		for (char c : this.tegn)
			if (tegn==c)
				return true;
		return false;
	}

	/**Starter navnet til denne typen med navn*/
	public boolean type(String navn) {
		return this.navn.startsWith(navn);
	}
}
