package labyrinth.engine;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import tbm.util.geom.Point;


public abstract class Enhet {
	public static final List<Enhet> enheter = new LinkedList<Enhet>();
	public static void pauseAlle(boolean pause) {
		for (Enhet enhet : Enhet.enheter)
			enhet.pause(pause);
	}


	private Rute rute;
	protected String navn="Ukjent mob";
	public final BufferedImage figur;
	protected Retning retning = Retning.NORD;

	/**Is called from Rute if another unit tries to move to this units rute.
	 *@param enhet the unit that is trying to move.*/
	public abstract void truffet(Enhet enhet);
	/**Make the unit stop/start.*/
	public abstract void pause(boolean pause);

	protected Enhet(String navn, String figur) {
		this.navn = navn;
		try {
			this.figur = ImageIO.read(new File(figur));
		} catch (IOException e) {
			throw Vindu.feil("Feil under lasting av bilde til %s: %s", navn, figur);
		}
		enheter.add(this);
	}

	/**Move from current tile to tile with pos.*/
	public void flytt(final Point pos) {
		final Enhet denne = this;
		SwingUtilities.invokeLater(new Runnable(){public void run() {
			if (rute != null)
				rute.flyttFra(false);
			if (pos != null)
				rute = Brett.get(pos).flyttTil(denne, false);
		}});
	}

	public String navn() {
		return navn;
	}

	public Rute rute() {
		return rute;
	}

	/**Draw the unit's image.*/
	public void tegn(Graphics g, int bredde, int høyde) {
		//Roterer figur etter retning
		AffineTransform transform = new AffineTransform();
		transform.rotate(retning.theta, bredde/2, høyde/2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		g.drawImage(op.filter(figur, null), 0, 0, bredde, høyde, null);
	}

	/**Flytter fra rute, fjerner fra Enhet.enheter.*/
	public void fjern() {
		if (rute != null)
			rute.flyttFra(false);
		enheter.remove(this);
	}


	/**It's really simple*/
	protected void setRute(Rute rute) {
		this.rute = rute;
	}


	/**Retningen enheten ser.*/
	public static enum Retning {
		NORD(0), ØST(Math.PI/2), SØR(Math.PI), VEST(-Math.PI/2);
		/**hvor mye bildet må roteres.*/
		public final double theta;
		private Retning(double theta) {
			this.theta = theta;
		}
		/**Flytt p en rute i retning*/
		public Point flytt(Point p) {
			return p.move(this, ØST, VEST, SØR, NORD);
		}
		/**Returner den retningen verdi er lik*/
		public static Retning retning(int verdi, int nord, int øst, int sør, int vest) {
			if (verdi==nord)  return NORD;
			if (verdi==øst)   return ØST;
			if (verdi==sør)   return SØR;
			if (verdi==vest)  return VEST;
			return null;
		}
	}
}