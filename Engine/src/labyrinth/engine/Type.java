package labyrinth.engine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**Siden ruter kan skifte type, og det ikke er mulig å erstatte komponenter i GridLayout*/
public class Type {
	private static List<Type> types = new ArrayList<Type>(20);

	public static Type get(char symbol) {
		for (Type type : types)
			if (type.symbol(symbol))
				return type;
			return null;
	}
	public static Type get(String name) {
		for (Type type : types)
			if (name.equalsIgnoreCase(type.name))
				return type;
		return null;
	}

	/**Returnerer den første typen som har tegnet tegn
	 * Gir en feilmelding hvis den ikke finner noen.*/
	public static Type t(char symbol) {
		Type type = get(symbol);
		if (type == null)
			throw Window.error("Ukjent type '%c'", symbol);
		return type;
	}
	/**Returnerer den første typen med navn nanv
	 * Gir en feilmelding hvis den ikke finner noen.*/
	public static Type t(String name) {
		Type type = get(name);
		if (type == null)
			throw Window.error("Ukjent type \"%s\"", name);
		return type;
	}

	/**Lager en ny type, og legger den i listen.*/
	public static Type add(String name, boolean solid, boolean method, String imagePath, Color color, String symbol) {
		Type type = new Type(name, solid, method, imagePath, color, symbol);
		types.add(type);
		return type;
	}




	/***/
	public final String name;
	/**Bakgrunnsfarge*/
	public final Color color;
	public final BufferedImage image;
	/**Enheter kan ikke flytte til solide ruter*/
	public final boolean solid;
	/**Kan felt av denne typen ha en metode*/
	public final boolean method;
	/**Hvilke tegn angir denne typen*/
	private final char[] symbols;
	protected Type(String name, boolean solid, boolean method, String imagePath, Color color, String symbol) {
		this.name=name;
		this.symbols=symbol.toCharArray();
		this.color=color;
		this.solid=solid;
		this.method=method;
		BufferedImage image = null;
		if (imagePath != null)
			try {
				image = ImageIO.read(new File(imagePath));
			} catch (IOException e) {
				throw Window.error(
						"Feil under lasting av bildefil \"%s\" til RuteType %s:\n"
						+"Det kan skyldes at working directory ikke er satt til pakkeNavnet\n"+"%s",
						imagePath, name, e.getMessage()
					);
			}
		this.image=image;
	}

	public char[] getSymbols() {
		return symbols.clone();
	}

	/**Er en rute angitt med tegn denne typen?*/
	public boolean symbol(char symbol) {
		for (char c : this.symbols)
			if (symbol==c)
				return true;
		return false;
	}

	/**Starter navnet til denne typen med navn*/
	public boolean type(String name) {
		return this.name.startsWith(name);
	}
}
