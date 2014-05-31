package labyrinth;
import static java.awt.Color.*;
import static java.awt.event.KeyEvent.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedTransferQueue;

import tbm.util.awtKeyListen;
import tbm.util.geom.Direction;
import labyrinth.engine.*;

public class Labyrinth {
	public static void main(String[] args) {
		Window.start("Labyrinth");
		String l = "res/";
		Type.add("wall",   true , false, l+"wall.png",   BLACK,  "#");
		Type.add("floor",  false, false, l+"floor.png",  WHITE,  " ");
		Type.add("exit",   false, false, l+"exit.png", MAGENTA,  "-");
		Type.add("start",  false, false, l+"start.png",  GREEN,  "*");
		Type.add("button", true , true , l+"button.png",   RED,  "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Type.add("plate",  false, true , l+"plate.png", ORANGE,  "abcdefghijklmnopqrstuvwxyz");
		Type.add("portal", false, true , l+"portal.png",  BLUE,  "0123456789");
		Type.add("dot",    false, false, l+"dot.png",   YELLOW,  ".");
		Type.add("enemy",  false, false, null,           WHITE,  "!");
		Type.add("hammer", false, false, l+"hammer.png", WHITE,  "^");

		if (args.length == 1)
			MapFile.read(new File(args[0]));
		else //show a fileChooser
			MapFile.read(MapFile.choose("maps/"));
		TileMap.all("enemy", tile -> {
			tile.setType("floor");
			new Enemy.Normal(tile, l+"enemy.png", 600, 0, 600);
		});
		
		TileMap.all("exit", t->t.visible());
		TileMap.panel.repaint();

		new Player(l+"player.png", findStart(), player -> {
			if (player.tile().isType("exit")) {
				player.tile().moveFrom(true);
				Window.won();
			}
			else if (player.tile().isType("hammer")) {
				player.tile().setType("floor");
				player.hammer(5000);
			}
			else if (player.tile().isType("dot")) {
				player.tile().setType("floor");
				if (!TileMap.anyTiles("dot"))
					Window.won();
			}
		});
		Window.display();
	}



	private static Tile findStart() {
		LinkedList<Tile> start = new LinkedList<>();
		TileMap.all("start", tile-> { 
			if (tile.mob()==null) {
				start.add(tile);
				for (Direction d : Direction.values())
					LoS.triangle(tile.pos(), d, t -> t.visible());
			}
		});
		if (start.size() > 1)
			throw Window.error("Brettet har mer enn ett startpunkt.");
		if (start.size() == 0)
			throw Window.error("Brettet mangler startpunkt.");
		return start.getFirst();
	}
}
