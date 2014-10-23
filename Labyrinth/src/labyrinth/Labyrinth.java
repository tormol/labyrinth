package labyrinth;
import static java.awt.Color.*;
import static java.awt.event.KeyEvent.*;
import static tbm.util.geom.Direction.*;
import java.io.File;
import java.awt.event.KeyEvent;
import java.util.concurrent.LinkedTransferQueue;
import tbm.util.awtKeyListen;
import java.util.LinkedList;
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

		Player p = new Player(l+"player.png", player->{
			if (player.tile().isType("exit")) {
				player.tile().leave(true);
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
		Tile start = findStart(p);
		p.moveTo( start );
		//see in all directions, except NONE
		for (Direction d : new Direction[]{NORTH, SOUTH, EAST, WEST})
			LoS.triangle(p.tile().pos(), d, t -> t.visible());
		p.start();
		Mob.pauseAll(false);
	}



	private static Tile findStart(Player player) {
		LinkedList<Tile> start = new LinkedList<>();
		TileMap.all("start", tile-> { 
			if (tile.mob()==null) {
				start.add(tile);
				tile.visible();
			}
		});
		if (start.isEmpty())
			throw Window.error("No start tile.");
		if (start.size() == 1)
			return start.get(0);

		LinkedTransferQueue<KeyEvent> queue = new LinkedTransferQueue<>();
		awtKeyListen.Pressed klp = event->queue.add(event);
		Window.window.addKeyListener(klp);
		start.get(0).enter(player, false);

		int index = 0;
		boolean finished = false;
		while (!finished)
			try {switch (queue.take().getKeyCode()) {
				case VK_LEFT :
					start.get(index).leave(false);
					if (index == 0)
						index = start.size();
					index--;
					start.get(index).enter(player, false);						
					break;
				case VK_RIGHT:
					start.get(index).leave(false);
					index++;
					if (index == start.size())
						index = 0;
					start.get(index).enter(player, false);
					break;
				case VK_ENTER:
					finished = true;
			}} catch (InterruptedException e1) {
				finished = true;
			}
		Window.window.removeKeyListener(klp);
		start.get(index).leave(false);
		return start.get(index);
	}
}
