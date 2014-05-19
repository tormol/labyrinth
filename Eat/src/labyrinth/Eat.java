package labyrinth;
import static java.awt.Color.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import tbm.util.geom.Point;
import labyrinth.engine.*;


public class Eat {
	public static final int DOTS = 5;
	public static final int MAX_ENEMIES = 10;
	static Player player;

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		Window.start("Eat");
		Type.add("floor", false, false, null,          BLACK, " ");
		Type.add("dot",   false, false, "res/dot.png", BLACK, ".");

		TileMap.start(10, 10);
		TileMap.visible(Arrays.asList(TileMap.all()));
		player = new Player("res/player.png", null, new Player.MoveTo(){
			int enemies = 1;
			public void moveTo(Player player) {
				if (player.tile().isType("dot")) {
					player.tile().setType("floor");
					if (TileMap.all("dot").isEmpty())
						if (enemies == MAX_ENEMIES)
							Window.won();
						else {
							enemies++;
							Eat.start(enemies);
						}
				}
			}
		});
		start(1);
		Window.display();
	}

	static void start(final int enemies) {
		SwingUtilities.invokeLater(new Runnable() {public void run() {
			Window.setText("Level "+enemies);
			Mob.pauseAll(true);
			Point p;
			for (int i=0; i<DOTS; i++) {
				do {
					p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
				} while (TileMap.get(p).isType("dot"));
				TileMap.get(p).setType("dot");
			}
			int i=0;
			for (Mob e : Mob.mobs)
				if (e instanceof Enemy) {
					p = e.tile().pos();
					while ((p.x<5 && p.y<5) || TileMap.get(p).mob() != null)
						p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
					e.move( p );
					((Enemy)e).setWait(1000 - i*100);
					i++;
				}
			player.move(new Point(1, 1));
			do {
				p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
			} while ((p.x<5 && p.y<5) || TileMap.get(p).mob() != null);
			new Enemy.Normal(TileMap.get(p), "res/enemy.png", 1000-100*(enemies-1), 5, 0);
			Mob.pauseAll(false);
		}});
	}
}
