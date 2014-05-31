package labyrinth;
import static java.awt.Color.*;
import static java.awt.event.KeyEvent.*;
import java.io.File;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Consumer;
import tbm.util.awtKeyListen;
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
		TileMap.all("enemy", new Consumer<Tile>(){public void accept(Tile t) {
				t.setType("floor");
				new Enemy.Normal(t, l+"enemy.png", 600, 0, 600);
			}});
		
		TileMap.all("exit", (t)->t.visible());//Vis alle utganger fra start

		Player p = new Player(l+"player.png", player->{
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
		}
		);

		Window.display();
		findStart(p);
		p.start();
		Mob.pauseAll(false);
	}

	private static void findStart(Player player) {
		ArrayList<Tile> start = new ArrayList<Tile>();
		TileMap.all("start", (t)-> {
			if (t.mob()==null)
				start.add(t);
			TileMap.removeShroud(t.pos());
		});
		if (start.isEmpty())
			throw Window.error("No start tile.");
		player.move(start.get(0));
		if (start.size() == 1)
			return;

		LinkedTransferQueue<KeyEvent> queue = new LinkedTransferQueue<>();
		awtKeyListen.Pressed klp = event->queue.add(event);
		Window.window.addKeyListener(klp);

		int index = 0;
		boolean finished = false;
		while (!finished)
			try {switch (queue.take().getKeyCode()) {
				case VK_LEFT :
					if (index == 0)
						index = start.size();
					index--;
					player.move(start.get(index));						
					break;
				case VK_RIGHT:
					index++;
					if (index == start.size())
						index = 0;
					player.move(start.get(index));
					break;
				case VK_ENTER:
					finished = true;
			}} catch (InterruptedException e1) {
				finished = true;
			}
		Window.window.removeKeyListener(klp);
	}
}
