package labyrinth.engine;
import tbm.util.geom.Point;
import tbm.util.geom.Direction;

public abstract class Enemy extends Mob implements Runnable {
	static final ThreadGroup threadGroup = new ThreadGroup("Enemies");


	private int wait;
	public final int waitShorter;
	public final int waitMin;
	private boolean stop = false;
	private final Thread thread;

	/**called from run()
	 *@return where the unit should move to*/
	abstract protected Tile findTile();

	protected Enemy(Tile start, String imagePath, int waitStart, int waitShorter, int waitMin) {
		super("Enemy", imagePath);
		wait = waitStart;
		this.waitShorter = waitShorter;
		this.waitMin = waitMin + waitShorter;
		setTile(start);
		tile().enter(this, false);
		thread = new Thread(threadGroup, this, "Enemy");
		thread.setName("Enemy");
		thread.setDaemon(true);
		thread.start();
	}

	@Override//Runnable (Thread)
	public void run() {
		sleep(wait);
		while (!stop) {
			if (pause) {
				sleep(Long.MAX_VALUE);
				continue;//might have been stopped
			}
			//moves
			Tile to = findTile();
			if (to != null)
				move(to);
			//wait shorter and shorter until limit.
			if (wait > waitMin)
				wait -= waitShorter;
			sleep(wait);
		}
	}

	/**To avoid try/catch around every Thread.sleep().
	 *Ends pause if it is interrupted.*/
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			pause = false;
		}
	}

	public void hit(Mob mob) {
		if (mob instanceof Enemy)
			remove();
		else
			mob.hit(this);
	}

	/**Sets pause*/
	public void pause(boolean pause) {
		if (!pause)
			thread.interrupt();
		this.pause = pause;
	}

	/**Tells the thread to stop, and waits for it.*/
	public void remove() {
		stop = true;
		try {
			thread.interrupt();
			thread.join();
		} catch (InterruptedException e) {
			System.err.println("Error interrupting Enemy: " + name);
		}
		super.remove();
	}


	/**@return this.wait*/
	public int getWait() {
		return wait;
	}

	/**@param wait >= 0*/
	public void setWait(int wait) {
		if (wait < 0)
			throw new IllegalArgumentException("wait is negative.");
		this.wait = wait;
	}



	/**Moves in random direction.*/
	public static class Normal extends Enemy {
		public Normal(Tile start, String imagePath, int waitStart, int waitShorter, int waitMin) {
			super(start, imagePath, waitStart, waitShorter, waitMin);
		}

		@Override//Fiende
		protected Tile findTile() {
			direction = Direction.d( (int)(Math.random()*4),  0, 1, 2, 3);
			final Tile to = TileMap.get( tile().pos().move(direction) );
			if (to.canEnter(this, false)  &&  !(to.mob() instanceof Enemy))
				return to;
			return null;
		}
	}

	/**Moves in random direction, and can move trough wall.*/
	public static class Ghost extends Enemy {
		public Ghost(Tile start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Enemy
		protected Tile findTile() {
			direction = Direction.d( (int)(Math.random()*4),  0, 1, 2, 3);
			final Tile to = TileMap.get( tile().pos().move(direction) );
			if (!to.isType("outside")  &&  !(to.mob() instanceof Enemy))
				return to;
			return null;
		}
	}

	/**Moves towards player, one axis at a time.*/
	public static class Targeting extends Enemy {
		public Targeting(Tile start, String fil, int ventStart, int ventRaskere, int ventMin) {
			super(start, fil, ventStart, ventRaskere, ventMin);
		}

		@Override//Enemy
		/**Finds player, find the distance, turn toward the axis with longest distance,
		 *  and moves to reduce the smallest nonzero distance.*/
		protected Tile findTile() {
			Point distance = new Point();
			Point pos = tile().pos();
			for (Mob e : Mob.mobs)
				if (e instanceof Player) {
					distance = tile().pos().diff(pos);
					break;
				}
			Point dir = new Point( (int)Math.signum(distance.x), (int)Math.signum(distance.y));
			Point[] alt;
			if (Math.abs(distance.x) > Math.abs(distance.y)) {
				direction = Direction.d( dir.x,  0, 0, -1, 1);
				alt = new Point[]{ new Point(0, dir.y),  new Point(dir.x, 0)};
			} else {
				direction = Direction.d( dir.y,  -1, 1, 0, 0);
				alt = new Point[]{ new Point(dir.x, 0),  new Point(0, dir.y)};
			}

			for (Point p : alt) {
				if (p.equals(0, 0))
					continue;
				Tile tile = TileMap.get(pos.x+p.x, pos.y+p.y);
				if (tile.canEnter(this, false)
				 && !(tile.mob() instanceof Enemy))
					return tile;
			}
			return null;
		}
	}
}
