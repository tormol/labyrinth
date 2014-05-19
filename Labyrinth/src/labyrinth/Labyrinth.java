package labyrinth;
import static java.awt.Color.*;
import java.io.File;
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
		for (Tile enemy : TileMap.all("enemy")) {
			enemy.setType("floor");
			new Enemy.Normal(enemy, l+"enemy.png", 600, 0, 600); 
		}

		new Player(l+"player.png", new Player.MoveTo(){public void moveTo(Player player) {
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
				if (TileMap.all("dot").isEmpty())
					Window.won();
			}
		}});
		Window.display();
	}
}
