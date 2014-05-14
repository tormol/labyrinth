package labyrinth;
import static java.awt.Color.*;
import java.io.File;
import labyrinth.engine.*;

public class Labyrinth {
	public static void main(String[] args) {
		Window.start("Labyrint");
		String l = "res/";
		Type.add("vegg",   true,  false, l+"wall.png",   BLACK,  "#");
		Type.add("gang",   false, false, l+"floor.png",  WHITE,  " ");
		Type.add("utgang", false, false, l+"exit.png", MAGENTA,  "-");
		Type.add("start",  false, false, l+"start.png",  GREEN,  "*");
		Type.add("knapp",  true,  true,  l+"button.png",   RED,  "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Type.add("plate",  false, true,  l+"plate.png", ORANGE,  "abcdefghijklmnopqrstuvwxyz");
		Type.add("portal", false, true,  l+"portal.png",  BLUE,  "0123456789");
		Type.add("godbit", false, false, l+"dot.png",   YELLOW,  ".");
		Type.add("fiende", false, false, null,           WHITE,  "!");
		Type.add("hammer", false, false, l+"hammer.png", WHITE,  "^");

		if (args.length == 1)
			MapFile.lesInn(new File(args[0]));
		else //Vis en filåpner
			MapFile.lesInn(MapFile.velg("maps"));
		for (Tile fiende : TileMap.alle("fiende")) {
			fiende.setType("gang");
			new Enemy.Vanlig(fiende, l+"enemy.png", 600, 0, 600); 
		}

		new Player(l+"player.png", new Player.FlyttTil(){public void flyttTil(Player spiller) {
			if (spiller.rute().isType("utgang")) {
				spiller.rute().flyttFra(true);
				Window.vant();
			}
			else if (spiller.rute().isType("hammer")) {
				spiller.rute().setType("gang");
				spiller.hammer(5000);
			}
			else if (spiller.rute().isType("godbit")) {
				spiller.rute().setType("gang");
				if (TileMap.alle("godbit").isEmpty())
					Window.vant();
			}
		}});
		Window.vis();
	}
}
