package motor;
import java.awt.Point;


public abstract class Fiende extends Enhet implements Runnable {
	static final ThreadGroup trådGruppe = new ThreadGroup("Fiender");


	private int vent;
	public final int ventRaskere;
	public final int ventMin;
	private boolean stopp = false;
	private boolean pause = false;
	private final Thread tråd;

	protected Fiende(String navn, String fil, Rute start, int ventStart, int ventRaskere, int ventMin) {
		super(navn, fil);
		vent = ventStart;
		this.ventRaskere = ventRaskere;
		this.ventMin = ventMin + ventRaskere;
		flyttTil(start);
		rute().flyttTil(this, false);
		tråd = new Thread(trådGruppe, this, "Fiende");
		tråd.setName("Fiende");
		tråd.setDaemon(true);
		tråd.start();
	}

	@Override//Runnable (Thread)
	public void run() {
		sov(vent);
		while (!stopp) {
			if (pause) {
				sov(Long.MAX_VALUE);
				continue;
			}
			//flytter
			final Point til = finnRute();
			if (til != null)
				flytt(til);
			//flytt raskere og raskere intill grensse
			if (vent > ventMin)
				vent -= ventRaskere;
			sov(vent);
		}
	}

	abstract protected Point finnRute();

	/**Unngaar try/catch rundt hver Thread.sleep(), avbryter pause hvis den blir avsluttet.*/
	private void sov(long millisekunder) {
		try {
			Thread.sleep(millisekunder);
		} catch (InterruptedException e) {
			pause = false;
		}
	}

	/**Blir kallt fra Rute hvis en annen enhet pro/ver aa flytte til dennes rute.*/
	public void truffet(Enhet enhet) {
		if (enhet instanceof Fiende)
			fjern();
		else
			enhet.truffet(this);
	}

	/**setter pause*/
	public void pause(boolean pause) {
		if (!pause)
			tråd.interrupt();
		this.pause = pause;
	}

	/**stopper traaden, og veenter til den avsluttes*/
	public void fjern() {
		stopp = true;
		try {
			tråd.interrupt();
			tråd.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.fjern();
	}


	/**@return this.vent*/
	public int getVent() {
		return vent;
	}

	/**@param vent kan ikke vaere negativ.*/
	public void setVent(int vent) {
		if (vent < 0)
			throw new IllegalArgumentException("vent er negativ.");
		this.vent = vent;
	}



	/**Pro/ver aa flytte i tilfeldig retnig*/
	public static class Vanlig extends Fiende {
		public Vanlig(Rute start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super("fiende vanlig", fil, start, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		protected Point finnRute() {
			retning = Retning.retning( (int)(Math.random()*4),  0, 1, 2, 3);
			final Rute til = Brett.get( retning.flytt( rute().pos() ) );
			if (til.kanFlytteTil(this, false)  &&  !(til.enhet() instanceof Fiende))
				return til.pos();
			return null;
		}
	}

	/**Pro/ver aa flytte i tilfeldig retning, kan flytte til solide ruter.*/
	public static class Spøkelse extends Fiende {
		public Spøkelse(Rute start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super("fil spøkelse", fil, start, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		protected Point finnRute() {
			retning = Retning.retning( (int)(Math.random()*4),  0, 1, 2, 3);
			final Rute til = Brett.get( retning.flytt( rute().pos() ) );
			if (!til.isType("utenfor")  &&  !(til.enhet() instanceof Fiende))
				return til.pos();
			return null;
		}
	}

	/**Flytter mot spilleren, en akse om gangen.*/
	public static class Målrettet extends Fiende {
		public Målrettet(Rute start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super("fil målrettet", fil, start, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		/**finner spilleren,  finner avstanden, snur seg etter den lengste avstanden til spilleren, og gaar mot den minste avstanden som ikke er 0.
		 * */
		protected Point finnRute() {
			Point avstand = new Point();
			Point pos = rute().pos();
			for (Enhet e : Enhet.enheter)
				if (e instanceof Spiller) {
					avstand.x = e.rute().pos().x - pos.x;
					avstand.y = e.rute().pos().y - pos.y;
					break;
				}
			Point ret = new Point( (int)Math.signum(avstand.x), (int)Math.signum(avstand.y));
			Point[] alternativ;
			if (Math.abs(avstand.x) > Math.abs(avstand.y)) {
				retning = Retning.retning( ret.x,  0, 1, 0, -1);
				alternativ = new Point[]{ new Point(0, ret.y),  new Point(ret.x, 0)};
			} else {
				retning = Retning.retning( ret.y,  -1, 0, 1, 0);
				alternativ = new Point[]{ new Point(ret.x, 0),  new Point(0, ret.y)};
			}

			for (Point p : alternativ) {
				if (p.distance(0, 0) == 0)
					continue;
				Rute rute = Brett.get(pos.x+p.x, pos.y+p.y);
				if (rute.kanFlytteTil(this, false)
				 && !(rute.enhet() instanceof Fiende))
					return rute.pos();
			}
			return null;
		}
	}
}
