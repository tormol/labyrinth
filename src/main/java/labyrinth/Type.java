package labyrinth;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**Separate from Tile Because tiles can change but components in GridLayout cannot be replaced*/
public class Type {
	private static List<Type> types = new ArrayList<Type>(20);

	public static Type get(char symbol) {
		for (Type type : types) {
			if (type.symbol(symbol)) {
				return type;
			}
		}
		return null;
	}
	public static Type get(String name) {
		for (Type type : types) {
			if (name.equalsIgnoreCase(type.name)) {
				return type;
			}
		}
		return null;
	}

	/**@return the first type associated to a symbol.
	 * Creates an error if no type matches.*/
	public static Type t(char symbol) {
		Type type = get(symbol);
		if (type == null) {
			throw Window.error("Unknown type '%c'", symbol);
		}
		return type;
	}
	/**@return the type with name.
	 * Creates an error if the type doesn't exist.*/
	public static Type t(String name) {
		Type type = get(name);
		if (type == null) {
			throw Window.error("Unknown type \"%s\"", name);
		}
		return type;
	}

	/**Creates a new tile type add adds it to the (global) list*/
	public static Type add(String name, boolean solid, boolean method, String imagePath, Color color, String symbol) {
		Type type = new Type(name, solid, method, imagePath, color, symbol);
		types.add(type);
		return type;
	}




	/***/
	public final String name;
	/**background color*/
	public final Color color;
	public final BufferedImage image;
	/**Can units enter tiles of this type?*/
	public final boolean solid;
	/**Can tiles of this type trigger functions?*/
	public final boolean method;
	/**Characters that become this type*/
	private final char[] symbols;
	protected Type(String name, boolean solid, boolean method, String imagePath, Color color, String symbol) {
		this.name = name;
		this.symbols = symbol.toCharArray();
		this.color = color;
		this.solid = solid;
		this.method = method;
		BufferedImage image = null;
		if (imagePath != null) {
			try {
				image = ImageIO.read(new File("src/main/resources/images/"+imagePath));
			} catch (IOException e) {
				throw Window.error(
						"Could not load image \"%s\" for tile type %s:\n%s",
						imagePath, name, e.getMessage()
				);
			}
		}
		this.image=image;
	}

	public char[] getSymbols() {
		return symbols.clone();
	}

	/**Is this type used for tiles defined with character symbol? */
	public boolean symbol(char symbol) {
		for (char c : this.symbols) {
			if (symbol == c) {
				return true;
			}
		}
		return false;
	}

	/** Does the name of this type start with name? */
	public boolean type(String name) {
		return this.name.startsWith(name);
	}
}
