package motor;
import java.awt.Point;


public abstract class Fiende extends Enhet implements Runnable {
	static final ThreadGroup trådGruppe = new ThreadGroup("Fiender");


	public int vent;
	final int ventRaskere;
	final int ventMin;
	private boolean stopp = false;
	private boolean pause = false;
	private final Thread tråd;

	protected Fiende(Rute start, String fil, int ventStart, int ventRaskere, int ventMin) {
		super("Fiende", fil);
		vent = ventStart;
		this.ventRaskere = ventRaskere;
		this.ventMin = ventMin + ventRaskere;
		rute = start;
		rute.flyttTil(this, false);
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

	private void sov(long millisekunder) {
		try {
			Thread.sleep(millisekunder);
		} catch (InterruptedException e) {
			pause = false;
		}
	}

	
	public void truffet(Enhet enhet) {
		if (enhet instanceof Fiende)
			fjern();
		else
			enhet.truffet(this);
	}

	public void pause(boolean pause) {
		if (pause)
			this.pause = true;
		else
			tråd.interrupt();
	}
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


	public static class Vanlig extends Fiende {
		public Vanlig(Rute start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		protected Point finnRute() {
			retning = Retning.retning( (int)(Math.random()*4),  0, 1, 2, 3);
			final Rute til = Brett.get( retning.flytt( rute.pos() ) );
			if (til.kanFlytteTil(this, false)  &&  !(til.enhet instanceof Fiende))
				return til.pos();
			return null;
		}
	}

	public static class Spøkelse extends Fiende {
		public Spøkelse(Rute start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		protected Point finnRute() {
			retning = Retning.retning( (int)(Math.random()*4),  0, 1, 2, 3);
			final Rute til = Brett.get( retning.flytt( rute.pos() ) );
			if (!til.isType("utenfor")  &&  !(til.enhet instanceof Fiende))
				return til.pos();
			return null;
		}
	}

	public static class Målrettet extends Fiende {
		public Målrettet(Rute start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		/**finner spilleren,  finner avstanden, snur seg etter den lengste avstanden til spilleren, og gaar mot den minste avstanden som ikke er 0.
		 * */
		protected Point finnRute() {
			Point avstand = new Point();
			Point pos = rute.pos();
			for (Enhet e : Enhet.enheter)
				if (e instanceof Spiller) {
					avstand.x = e.rute.pos().x - pos.x;
					avstand.y = e.rute.pos().y - pos.y;
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
				 && !(rute.enhet instanceof Fiende))
					return rute.pos();
			}
			return null;
		}
	}
}
