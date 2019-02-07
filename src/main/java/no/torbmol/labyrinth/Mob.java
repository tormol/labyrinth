package no.torbmol.labyrinth;
import javax.imageio.ImageIO;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.torbmol.util.geom.Direction;


public abstract class Mob {
	public static final List<Mob> mobs = new ArrayList<Mob>();
	public static void pauseAll(boolean pause) {
		for (Mob unit : Mob.mobs) {
			unit.pause(pause);
		}
	}


	private Tile tile;
	protected String name="Unknown mob";
	public final BufferedImage image;
	protected Direction direction = Direction.NORTH;
	protected boolean pause = true;

	/**Is called from Tile if another unit tries to move to this units rute.
	 *@param mob the unit that is trying to move.*/
	public abstract void hit(Mob mob);
	/**Make the unit stop/start.*/
	public abstract void pause(boolean pause);

	protected Mob(String name, String imagePath) {
		this.name = name;
		try {
			this.image = ImageIO.read(this.getClass().getResourceAsStream("/images/"+imagePath));
		} catch (IOException e) {
			throw Window.error("Error loading image to %s: (%s)\n%s", name, imagePath, e.getMessage());
		}
		mobs.add(this);
	}

	/**Move from current tile to tile with pos.*/
	public void moveTo(Tile tile) {
		//might be buggy, change later.
		Mob _this = this;
		if (_this.tile != null)
			_this.tile.leave(false);
		if (tile != null)
			_this.tile = tile.enter(_this, false);
	}

	public String name() {
		return name;
	}

	public Tile tile() {
		return tile;
	}

	/**Draw the unit's image.*/
	public void draw(Graphics g, int width, int height) {
		//Rotate image to direction
		AffineTransform transform = new AffineTransform();
		//Rotation is done before scaling, so rotate around center of the image, not around the center of the tile.
		double angle = Math.PI/2 - direction.angle;//unrotated face north, but NORTH.angle==pi/2, so subtract to cancel out.
		transform.rotate(angle, image.getWidth()/2, image.getHeight()/2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		g.drawImage(op.filter(image, null), 0, 0, width, height, null);
	}

	/**Move from tile, and remove from Mob.mobs.*/
	public void remove() {
		if (tile != null) {
			tile.leave(false);
		}
		mobs.remove(this);
	}

	/**Set this.tile to tile*/
	protected void setTile(Tile tile) {
		this.tile = tile;
	}
}
