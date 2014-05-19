package labyrinth.engine;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import tbm.util.geom.Point;


public class Tile extends javax.swing.JPanel {
	/**Fargen til felter man ikke har sett.*/
	public static final Color shroud = Color.LIGHT_GRAY;
	//type og metode skulle egentlig vært final, men jeg vet ikke hvordan jeg erstatter en rute i GridLayout
	/**Hva slags type rute denne ruten er*/
	private Type type;
	/***/
	private final Point pos;
	/**For knapper, plater og teleportører: Hva som skjer når knappen trykkes.*/
	public Method method;
	/**Spiller i ruten.*/
	private Mob mob = null;
	/**Om spilleren kan se dette feltet.*/
	private boolean visible = false;

	public Tile(Type t, Point p) {
		type = t;
		pos = p;
		setBackground(shroud);
		//Størrelse til ruten, i pixler.
		setPreferredSize(new Dimension(32, 32));
		setMinimumSize(new Dimension(32, 32));
		if (type.type("exit"))
			visible();//Vis alle utganger fra start
	}

	/**Sjekker om enheten er solid, hvis trigger==true kan den kalle en Metode*/
	public boolean canMoveTo(Mob enhet, boolean trigger) {
		if (trigger && type.type("button"))
			method.call(this, enhet);
		return !type.solid;
	}

	public Tile moveTo(Mob mob, boolean trigger) {
		if (type.method && trigger)
			method.call(this, mob);
		//den som flytter til kan sjekke hva som er her før den flytter,
		//men det kan ikke den som var her fra før av.
		if (this.mob != null)
			this.mob.hit(mob);
		this.mob = mob;
		repaint();
		return this;
	}

	public Mob moveFrom(boolean trigger) {
		Mob enhet = this.mob;
		this.mob = null;
		repaint();
		return enhet;
	}

	public void visible() {
		if (!visible) {
			visible = true;
			setBackground(type.color);
			repaint();
		}
	}

	/**For å vise bilde.*/
	@Override
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
		if (visible)
			setBackground(this.type.color);
		repaint();
	}


	public Point pos() {
		if (pos==null)
			new Point();//no-op
		return new Point(pos);
	}

	public Mob mob() {
		return mob;
	}

	private static final long serialVersionUID = 1L;
}
