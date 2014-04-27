package Spis;

import static java.awt.Color.*;
import java.awt.Point;

import javax.swing.SwingUtilities;

import motor.Brett;
import motor.Enhet;
import motor.Fiende;
import motor.Spiller;
import motor.Type;
import motor.Vindu;


public class Spis {
	public static final int GODBITER = 5;
	public static final int MAKS_FIENDER = 10;
	static Spiller spiller;

	public static void main(String[] args) {
		Vindu.start("Labyrint");
		Type.add("gang",   false, false, null, BLACK,  " ");
		Type.add("godbit", false, false, "spis/godbit.png", BLACK, ".");

		Brett.start(10, 10);
		Brett.synsvidde(0);
		spiller = new Spiller("spis/spiller.png", null, new Spiller.FlyttTil(){
			int fiender = 1;
			public void flyttTil(Spiller spiller) {
				if (spiller.rute.isType("godbit")) {
					spiller.rute.setType("gang");
					if (Brett.alle("godbit").isEmpty())
						if (fiender == MAKS_FIENDER)
							Vindu.vant();
						else {
							fiender++;
							Spis.start(fiender);
						}
				}
			}
		});
		start(1);
		Vindu.vis();
	}

	static void start(final int fiender) {
		SwingUtilities.invokeLater(new Runnable() {public void run() {
			Vindu.setTekst("Nivå "+fiender);
			Enhet.pauseAlle(true);
			Point p;
			for (int i=0; i<GODBITER; i++) {
				do {
					p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
				} while (Brett.get(p).isType("godbit"));
				Brett.get(p).setType("godbit");
			}
			int i=0;
			for (Enhet e : Enhet.enheter)
				if (e instanceof Fiende) {
					p = e.rute().pos();
					while ((p.x<5 && p.y<5) || Brett.get(p).enhet != null)
						p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
					e.flytt( p );
					((Fiende)e).vent = 1000 - i*100;
					i++;
				}
			spiller.flytt(new Point(1, 1));
			do {
				p = new Point((int)(Math.random()*10), (int)(Math.random()*10));
			} while ((p.x<5 && p.y<5) || Brett.get(p).enhet != null);
			new Fiende.Vanlig(Brett.get(p), "spis/fiende.png", 1000-100*(fiender-1), 5, 0);
			Enhet.pauseAlle(false);
		}});
	}
}
