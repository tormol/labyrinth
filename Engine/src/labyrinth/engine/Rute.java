package motor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import tbm.util.geom.Point;


public class Rute extends javax.swing.JPanel {
	private static final long serialVersionUID = 1L;
	/**Fargen til felter man ikke har sett.*/
	public static final Color dis = Color.LIGHT_GRAY;
	//type og metode skulle egentlig vært final, men jeg vet ikke hvordan jeg erstatter en rute i GridLayout
	/**Hva slags type rute denne ruten er*/
	private Type type;
	/***/
	private final Point posisjon;
	/**For knapper, plater og teleportører: Hva som skjer når knappen trykkes.*/
	public Metode metode;
	/**Spiller i ruten.*/
	private Enhet enhet = null;
	/**Om spilleren kan se dette feltet.*/
	private boolean synlig = false;

	public Rute(Type t, Point p) {
		type = t;
		posisjon = p;
		setBackground(dis);
		//Størrelse til ruten, i pixler.
		setPreferredSize(new Dimension(32, 32));
		setMinimumSize(new Dimension(32, 32));
		if (type.type("utgang"))
			vis();//Vis alle utganger fra start
	}

	/**Sjekker om enheten er solid, hvis trigger==true kan den kalle en Metode*/
	public boolean kanFlytteTil(Enhet enhet, boolean trigger) {
		if (trigger && type.type("knapp"))
			metode.kall(this, enhet);
		return !type.solid;
	}

	public Rute flyttTil(Enhet enhet, boolean trigger) {
		if (type.metode && trigger)
			metode.kall(this, enhet);
		//den som flytter til kan sjekke hva som er her før den flytter,
		//men det kan ikke den som var her fra før av.
		if (this.enhet != null)
			this.enhet.truffet(enhet);
		this.enhet = enhet;
		repaint();
		return this;
	}

	public Enhet flyttFra(boolean trigger) {
		Enhet enhet = this.enhet;
		this.enhet = null;
		repaint();
		return enhet;
	}

	public void vis() {
		if (!synlig) {
			synlig = true;
			setBackground(type.farge);
			repaint();
		}
	}

	/**For å vise bilde.*/
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (synlig) {
			if (type.bilde != null)
				g.drawImage(type.bilde, 0, 0, getWidth(), getHeight(), type.farge, null);
			if (enhet != null)
				enhet.tegn(g, getWidth(), getHeight());
		}
	}


	public Type getType() {
		return type;
	}
	public boolean isType(String type) {
		return this.type.type(type);
	}
	public void setType(String type) {
		setType(Type._(type));
	}
	public synchronized void setType(final Type type) {
		final Rute denne = this;
		denne.type = type;
		if (synlig)
			denne.setBackground(denne.type.farge);
		denne.repaint();
	}


	public Point pos() {
		if (posisjon==null)
			new Point();//no-op
		return new Point(posisjon);
	}

	public Enhet enhet() {
		return enhet;
	}
}
