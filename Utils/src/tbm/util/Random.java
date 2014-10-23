package tbm.util;

//this will do for now
//TODO: redo the untrackend version git deleted.
public class Random extends java.util.Random {
	public Random() {
		super();
	}
	public Random(long seed) {
		super(seed);
	}

	/**@return a random number min<=result<max
	 * @throws IllegalArgumentException if min >= max*/
	public int nextInt(int min, int max_plus_one) {
		if (min >= max_plus_one)
			throw new IllegalArgumentException("min ("+min+") must be less than max ("+max_plus_one+").");
		return nextInt(max_plus_one-min)+min;
	}

	public static final Random rand = new Random();

	private static final long serialVersionUID = 1L;
}
