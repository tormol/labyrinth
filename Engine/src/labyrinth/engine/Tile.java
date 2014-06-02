package labyrinth.engine;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import tbm.util.geom.Point;


public class Tile extends javax.swing.JPanel {
	/**The color of unexplored tiles.*/
	public static final Color shroud = Color.LIGHT_GRAY;
	/**What kind of tile this tile is, see Type*/
	private Type type;
	/**coordinate*/
	private final Point pos;
	/**method to be called when a mob enters the tile with trigger=true*/
	public Method method;
	/**Mob in this tile*/
	private Mob mob = null;
	
	/**Om spilleren kan se dette feltet.*/
	private boolean visible = false;

	public Tile(Type t, Point p) {
		type = t;
		pos = p;
		setBackground(shroud);
		//Size of tile in pixels.
		setPreferredSize(new Dimension(32, 32));
		setMinimumSize(new Dimension(32, 32));
	}

	/**Most mobs cannot enter solid tiles, and if trigger=true a method might be called*/
	public boolean canEnter(Mob mob, boolean trigger) {
		if (trigger && type.type("button"))
			method.call(this, mob);
		return !type.solid;
	}

	public Tile enter(Mob mob, boolean trigger) {
		//if there is a mob in the tile, let it know it has been removed.
		//the mob entering the tile can check if its occupied before moving.
		if (this.mob != null)
			this.mob.hit(mob);
		this.mob = mob;
		if (type.method && trigger)
			method.call(this, mob);
		repaint();
		return this;
	}

	public void moveFrom(boolean trigger) {
		this.mob = null;
		repaint();
	}

	public void visible() {
		if (!visible) {
			visible = true;
			setBackground(type.color);
			repaint();
		}
	}

	/**draw images for tile and mob.*/@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (visible) {
			if (type.image != null)
				g.drawImage(type.image, 0, 0, getWidth(), getHeight(), type.color, null);
			if (mob != null)
				mob.draw(g, getWidth(), getHeight());
		}
	}


	public Type getType() {
		return type;
	}
	public boolean isType(String type) {
		return this.type.type(type);
	}
	public void setType(String type) {
		setType(Type.t(type));
	}
	public synchronized void setType(final Type type) {
		this.type = type;
		if (visible) {
			setBackground(this.type.color);
			repaint();
		}
	}


	public Point pos() {
		if (pos==null)
			new Point();//no-op TODO: Why?
		return new Point(pos);
	}

	public Mob mob() {
		return mob;
	}

	private static final long serialVersionUID = 1L;
}
