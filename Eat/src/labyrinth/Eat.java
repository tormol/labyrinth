package labyrinth;
import static java.awt.Color.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import tbm.util.geom.Point;
import labyrinth.engine.*;


public class Eat {
	public static final int GODBITER = 5;
	public static final int MAKS_FIENDER = 10;
	static Player player;

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		Window.start("Labyrint");
		Type.add("gang",   false, false, null, BLACK,  " ");
		Type.add("godbit", false, false, "res/dot.png", BLACK, ".");

		TileMap.start(10, 10);
		TileMap.visible(Arrays.asList(TileMap.alle()));
		player = new Player("res/player.png", null, new Player.FlyttTil(){
			int enemies = 1;
			public void flyttTil(Player player) {
				if (player.rute().isType("godbit")) {
					player.rute().setType("gang");
					if (TileMap.alle("godbit").isEmpty())
						if (enemies == MAKS_FIENDER)
							Window.vant();
						else {
							enemies++;
							Eat.start(enemies);
						}
				}
			}
		});
		start(1);
		Window.vis();
	}

	static void start(final int enemies) {
		SwingUtilities.invokeLater(new Runnable() {public void run() {
			Window.setTekst("Nivå "+enemies);
			Mob.pauseAlle(true);
			Point p;
			for (int i=0; i<GODBITER; i++) {
				do {
					p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
				} while (TileMap.get(p).isType("godbit"));
				TileMap.get(p).setType("godbit");
			}
			int i=0;
			for (Mob e : Mob.enheter)
				if (e instanceof Enemy) {
					p = e.rute().pos();
					while ((p.x<5 && p.y<5) || TileMap.get(p).enhet() != null)
						p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
					e.flytt( p );
					((Enemy)e).setVent(1000 - i*100);
					i++;
				}
			player.flytt(new Point(1, 1));
			do {
				p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
			} while ((p.x<5 && p.y<5) || TileMap.get(p).enhet() != null);
			new Enemy.Vanlig(TileMap.get(p), "res/enemy.png", 1000-100*(enemies-1), 5, 0);
			Mob.pauseAlle(false);
		}});
	}
}
