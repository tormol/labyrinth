package labyrinth.engine;
import tbm.util.geom.Point;
import tbm.util.geom.Direction;

public abstract class Enemy extends Mob implements Runnable {
	static final ThreadGroup threadGroup = new ThreadGroup("Fiender");


	private int vent;
	public final int ventRaskere;
	public final int ventMin;
	private boolean stopp = false;
	private boolean pause = false;
	private final Thread tråd;

	/**called from run()
	 *@return where the unit should move to*/
	abstract protected Point finnRute();

	protected Enemy(Tile start, String fil, int ventStart, int ventRaskere, int ventMin) {
		super("Fiende", fil);
		vent = ventStart;
		this.ventRaskere = ventRaskere;
		this.ventMin = ventMin + ventRaskere;
		setRute(start);
		rute().flyttTil(this, false);
		tråd = new Thread(threadGroup, this, "Fiende");
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

	/**To avoid try/catch around every Thread.sleep().
	 *Ends pause if it is interrupted.*/
	private void sov(long millisekunder) {
		try {
			Thread.sleep(millisekunder);
		} catch (InterruptedException e) {
			pause = false;
		}
	}

	public void truffet(Mob enhet) {
		if (enhet instanceof Enemy)
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

	/**Tells the thread to stop, and waits for it.*/
	public void fjern() {
		stopp = true;
		try {
			tråd.interrupt();
			tråd.join();
		} catch (InterruptedException e) {
			System.err.println("Error interrupting Fiende: " + navn);
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
	public static class Vanlig extends Enemy {
		public Vanlig(Tile start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		protected Point finnRute() {
			retning = Direction.d( (int)(Math.random()*4),  0, 1, 2, 3);
			final Tile til = TileMap.get( rute().pos().move(retning) );
			if (til.kanFlytteTil(this, false)  &&  !(til.enhet() instanceof Enemy))
				return til.pos();
			return null;
		}
	}

	/**Pro/ver aa flytte i tilfeldig retning, kan flytte til solide ruter.*/
	public static class Spøkelse extends Enemy {
		public Spøkelse(Tile start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		protected Point finnRute() {
			retning = Direction.d( (int)(Math.random()*4),  0, 1, 2, 3);
			final Tile til = TileMap.get( rute().pos().move(retning) );
			if (!til.isType("utenfor")  &&  !(til.enhet() instanceof Enemy))
				return til.pos();
			return null;
		}
	}

	/**Flytter mot spilleren, en akse om gangen.*/
	public static class Målrettet extends Enemy {
		public Målrettet(Tile start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Fiende
		/**finner spilleren,  finner avstanden, snur seg etter den lengste avstanden til spilleren, og gaar mot den minste avstanden som ikke er 0.
		 * */
		protected Point finnRute() {
			Point avstand = new Point();
			Point pos = rute().pos();
			for (Mob e : Mob.enheter)
				if (e instanceof Player) {
					avstand = rute().pos().diff(pos);
					break;
				}
			Point ret = new Point( (int)Math.signum(avstand.x), (int)Math.signum(avstand.y));
			Point[] alternativ;
			if (Math.abs(avstand.x) > Math.abs(avstand.y)) {
				retning = Direction.d( ret.x,  0, 0, -1, 1);
				alternativ = new Point[]{ new Point(0, ret.y),  new Point(ret.x, 0)};
			} else {
				retning = Direction.d( ret.y,  -1, 1, 0, 0);
				alternativ = new Point[]{ new Point(ret.x, 0),  new Point(0, ret.y)};
			}

			for (Point p : alternativ) {
				if (p.equals(0, 0))
					continue;
				Tile rute = TileMap.get(pos.x+p.x, pos.y+p.y);
				if (rute.kanFlytteTil(this, false)
				 && !(rute.enhet() instanceof Enemy))
					return rute.pos();
			}
			return null;
		}
	}
}
