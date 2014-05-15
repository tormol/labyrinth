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

import tbm.util.geom.Direction;
import tbm.util.geom.Point;


public abstract class Mob {
	public static final List<Mob> enheter = new LinkedList<Mob>();
	public static void pauseAlle(boolean pause) {
		for (Mob enhet : Mob.enheter)
			enhet.pause(pause);
	}


	private Tile rute;
	protected String navn="Ukjent mob";
	public final BufferedImage figur;
	protected Direction retning = Direction.NORTH;

	/**Is called from Rute if another unit tries to move to this units rute.
	 *@param enhet the unit that is trying to move.*/
	public abstract void truffet(Mob enhet);
	/**Make the unit stop/start.*/
	public abstract void pause(boolean pause);

	protected Mob(String navn, String figur) {
		this.navn = navn;
		try {
			this.figur = ImageIO.read(new File(figur));
		} catch (IOException e) {
			throw Window.feil("Feil under lasting av bilde til %s: %s", navn, figur);
		}
		enheter.add(this);
	}

	/**Move from current tile to tile with pos.*/
	public void flytt(final Point pos) {
		final Mob denne = this;
		SwingUtilities.invokeLater(new Runnable(){public void run() {
			if (rute != null)
				rute.flyttFra(false);
			if (pos != null)
				rute = TileMap.get(pos).flyttTil(denne, false);
		}});
	}

	public String navn() {
		return navn;
	}

	public Tile rute() {
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
	protected void setRute(Tile rute) {
		this.rute = rute;
	}
}
