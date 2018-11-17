package labyrinth.engine;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Queue;
import java.util.function.Consumer;
import static tbm.util.statics.*;
import tbm.util.Reference;
import tbm.util.geom.Point;


/**Contains all the tiles of the map*/
public class TileMap {
	private static Tile[][] map;
	/**Swing panel that displays the tiles*/
	public static final JPanel panel = new JPanel(); 
	

	/**Empty map
	 * @throws InvalidMapException */
	public static void start(int width, int height) throws InvalidMapException {
		char[][] map = new char[height][width];
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
				map[y][x] = ' ';
		start(map);
	}
	/**@throws InvalidMapException */
	public static void start(String[] lines) throws InvalidMapException {
		char[][] symbol = new char[lines.length][];
		for (int i=0; i<symbol.length; i++) {
			symbol[i] = lines[i].toCharArray();
		}
		start(symbol);
	}
	public static void start(Queue<char[]> symbol) throws InvalidMapException {
		start(symbol.toArray(new char[symbol.size()][]));
	}
	public static void start(char[][] symbol) throws InvalidMapException {
		Type.add("outside", true, false, null, Color.CYAN, "");
		int columns = symbol[0].length;
		map = new Tile[symbol.length][columns];
		panel.setLayout(new GridLayout( map.length, map[0].length));

		// create tiles
		for (int y=0;  y<map.length;  y++) {
			if (symbol[y].length != columns) {
				throw new InvalidMapException(y, "Length doesn't match the previous rows");
			}
			for (int x=0; x<map[0].length; x++) {
				Type type = Type.get(symbol[y][x]);
				if (type == null) {
					throw new InvalidMapException(y, "column %d: Unknown symbol '%c'", x, symbol[y][x]);
				}
				map[y][x] = new Tile(type, Point.p(x, y));
				if (type.method) {
					map[y][x].method = char2str(symbol[y][x]);
				}
				panel.add(map[y][x]);
			}
		}
	}


	/**@return the tile in row y, column x
	 * If the coordinate is outside the map, a new tile which acts like a wall is returned*/
	public static Tile get(Point p) {
		return get(p.x, p.y);
	}
	/**@return the tile in row y, column x
	 * If the coordinate is outside the map, a new tile which acts like a wall is returned,
	 * this way I don't need to check for edge cases other places*/
	public static Tile get(int x, int y) {
		if (y < 0  ||  y >= map.length  ||  x < 0  ||  x >= map[0].length) {
			return new Tile(Type.t("outside"), null);
		}
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
		Reference<Boolean> any = new Reference<>(false);
		all(type, t->any.value=true);
		return any.value;
	}

	/**@return all tiles on the map*/
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
	/**Calls consumer for each tile of type*/
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

	public static class InvalidMapException extends Exception {
		public final int row;
		private InvalidMapException(int row, String f, Object... a) {
			super(String.format(f, a));
			this.row = row;
		}
		@Override//Exception
		public String getMessage() {
			return String.format("Row %d %s", row, super.getMessage());
		}
		public String getOffsetMessage(int lineOffset) {
			return String.format("Line %d %s", lineOffset+row, super.getMessage());
		}
		public int getRow() {
			return row;
		}
		private static final long serialVersionUID = 1L;
	}
}
