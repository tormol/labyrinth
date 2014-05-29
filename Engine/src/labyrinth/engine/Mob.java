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
	public static final List<Mob> mobs = new LinkedList<Mob>();
	public static void pauseAll(boolean pause) {
		for (Mob enhet : Mob.mobs)
			enhet.pause(pause);
	}


	private Tile tile;
	protected String name="Unknown mob";
	public final BufferedImage image;
	protected Direction direction = Direction.NORTH;

	/**Is called from Rute if another unit tries to move to this units rute.
	 *@param mob the unit that is trying to move.*/
	public abstract void hit(Mob mob);
	/**Make the unit stop/start.*/
	public abstract void pause(boolean pause);

	protected Mob(String name, String imagePath) {
		this.name = name;
		try {
			this.image = ImageIO.read(new File(imagePath));
		} catch (IOException e) {
			throw Window.error("Feil under lasting av bilde til %s: %s", name, imagePath);
		}
		mobs.add(this);
	}

	/**Move from current tile to tile with pos.*/
	public void move(final Point pos) {
		final Mob _this = this;
		SwingUtilities.invokeLater(() -> {
			if (tile != null)
				tile.moveFrom(false);
			if (pos != null)
				tile = TileMap.get(pos).moveTo(_this, false);
		});
	}

	public String name() {
		return name;
	}

	public Tile tile() {
		return tile;
	}

	/**Draw the unit's image.*/
	public void draw(Graphics g, int bredde, int høyde) {
		//Roterer figur etter retning
		AffineTransform transform = new AffineTransform();
		transform.rotate(direction.theta, bredde/2, høyde/2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		g.drawImage(op.filter(image, null), 0, 0, bredde, høyde, null);
	}

	/**Flytter fra rute, fjerner fra Enhet.enheter.*/
	public void remove() {
		if (tile != null)
			tile.moveFrom(false);
		mobs.remove(this);
	}


	/**It's really simple*/
	protected void setTile(Tile tile) {
		this.tile = tile;
	}
}
