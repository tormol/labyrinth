package labyrinth;
import static java.awt.Color.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import tbm.util.geom.Point;
import labyrinth.engine.*;


public class Eat {
	public static final int GODBITER = 5;
	public static final int MAKS_FIENDER = 10;
	static Player spiller;

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		Window.start("Labyrint");
		Type.add("gang",   false, false, null, BLACK,  " ");
		Type.add("godbit", false, false, "res/dot.png", BLACK, ".");

		TileMap.start(10, 10);
		TileMap.synsvidde(0);
		spiller = new Player("res/player.png", null, new Player.FlyttTil(){
			int fiender = 1;
			public void flyttTil(Player spiller) {
				if (spiller.rute().isType("godbit")) {
					spiller.rute().setType("gang");
					if (TileMap.alle("godbit").isEmpty())
						if (fiender == MAKS_FIENDER)
							Window.vant();
						else {
							fiender++;
							Eat.start(fiender);
						}
				}
			}
		});
		start(1);
		Window.vis();
	}

	static void start(final int fiender) {
		SwingUtilities.invokeLater(new Runnable() {public void run() {
			Window.setTekst("Nivå "+fiender);
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
			spiller.flytt(new Point(1, 1));
			do {
				p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
			} while ((p.x<5 && p.y<5) || TileMap.get(p).enhet() != null);
			new Enemy.Vanlig(TileMap.get(p), "res/enemy.png", 1000-100*(fiender-1), 5, 0);
			Mob.pauseAlle(false);
		}});
	}
}
