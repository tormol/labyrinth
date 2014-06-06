package labyrinth.engine;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Queue;
import java.util.function.Consumer;

import labyrinth.engine.method.Procedure;
import static tbm.util.statics.*;
import tbm.util.Wrapper;
import tbm.util.geom.Point;


/**Originalt var denne ikke statisk, men siden jeg aldri vil trenge mer enn en instans, og den brukes overalt ble det tungvindt å sende den ene instansen rundt til alle metoder.
 * Holder alle rutene på skjermen.*/
public class TileMap {
	private static Tile[][] map;
	/**Panelet som inneholder rutene*/
	public static final JPanel panel = new JPanel(); 
	

	/**Empty map*/
	public static void start(int width, int height) {
		char[][] map = new char[height][width];
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
				map[y][x] = ' ';
		start(map);
	}
	/***/
	public static void start(String[] lines) {
		char[][] symbol = new char[lines.length][];
		for (int i=0; i<symbol.length; i++)
			symbol[i] = lines[i].toCharArray();
		start(symbol);
	}
	public static void start(Queue<char[]> symbol) {
		start(symbol.toArray(new char[symbol.size()][]));
	}
	public static void start(char[][] tegn) {
		Type.add("outside", true, false, null, Color.CYAN, "");
		int collumns = tegn[0].length;
		map = new Tile[tegn.length][collumns];
		panel.setLayout(new GridLayout( map.length, map[0].length));

		//lager Ruter
		for (int y=0;  y<map.length;  y++, MapFile.line++) {
			if (tegn[y].length != collumns)
				throw Window.error("lengden passer ikke med resten.");
			for (int x=0; x<map[0].length; x++) {
				Type type = Type.get(tegn[y][x]);
				if (type == null)
					throw Window.error("Kolonne %d: Ugyldig tegn '%c'", x, tegn[y][x]);
				map[y][x] = new Tile(type, new Point(x, y));
				if (type.method)
					map[y][x].method = Procedure.get( charToString(tegn[y][x]) );
				panel.add(map[y][x]);
			}
		}
	}

	
	/**returnerer ruten i rad y, kolonner x
	 * hvis koordinatene er utenfor brettet returneres en ny rute av type vegg*/
	public static Tile get(Point p) {
		return get(p.x, p.y);
	}
	/**returnerer ruten i rad y, kolonner x
	 * hvis koordinatene er utenfor brettet returneres en ny rute av type vegg*/
	public static Tile get(int x, int y) {
		if (y < 0  ||  y >= map.length  ||  x < 0  ||  x >= map[0].length)
			//På denne måten slipper jeg å sjekke om jeg er utenfor brettet andre steder.
			return new Tile(Type.t("outside"), null);
		return map[y][x];
	}

	public static Dimension dimesions() {
		return new Dimension(map[0].length, map.length);
	}
	/**@return map width*height*/
	public static int numberOfTiles() {
		return map[0].length * map.length;
	}

	/**Are there any tiles of this type?*/
	public static boolean anyTiles(String type) {
		Wrapper<Boolean> any = new Wrapper<>(false);
		all(type, t->any.v=true);
		return any.v;
	}

	/**Returnerer alle rutene på brettet*/
	public static Tile[] all() {
		Tile[] all = new Tile[numberOfTiles()];
		int pos=0;
		for (Tile[] row : map) {
			System.arraycopy(row, 0, all, pos, row.length);
			pos += row.length;
		}
		return all;
	}

	public static void all(String type, Consumer<Tile> c) {
		all(Type.t(type), c);
	}
	/**Returnerer alle ruter av typen*/
	public static void all(Type type, Consumer<Tile> consumer) {
		for (Tile[] row : map)
			for (Tile t : row)
				if (t.getType() == type)
					consumer.accept(t);
	}

	/**make tiles visible*/
	public static void visible(Iterable<Tile> tiles) {
		for (Tile t : tiles)
			t.visible();
	}



	@Deprecated/**Hvor mange felt spilleren ser i hver retning. 0=ser alle*/
	private static int synsvidde = 2;
	@Deprecated
	/**Gjør alle ruter i et kvadrat med radius Brett.synsvidde sentrert på p synlige
	 * Må kalles fra SwingUtilities.invokeLater()*/
	public static void removeShroud(Point p) {
		for (int x=p.x-synsvidde; x<=p.x+synsvidde; x++)
			for (int y=p.y-synsvidde; y<=p.y+synsvidde; y++)
				get(x, y).visible();
	}
	@Deprecated
	public static void synsvidde(int synsvidde) {
		if (synsvidde<0)
			throw Window.error("Brett.synsvidde kan ikke være negativ");
		TileMap.synsvidde = synsvidde;
		if (synsvidde==0)
			for (Tile rute : all())
				rute.visible();
	}
}
