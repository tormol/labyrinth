package no.torbmol.util;

/**{@inheritDoc}
 *Adds <tt>nextInt(min, max)</tt>*/
public class Random extends java.util.Random {
	public Random() {
		super();
	}
	public Random(long seed) {
		super(seed);
	}

	/**get a random number less than <tt>maxA</tt> and greater or equal to <tt>min</tt>
	 *@param min inclusive
	 *@param max exclusive
	 *@return {@code nextInt(max-min)+min}
	 *@throws IllegalArgumentException if {@code min >= max}*/
	public int nextInt(int min, int max) throws IllegalArgumentException {
		if (min >= max)
			throw new IllegalArgumentException("min ("+min+") must be less than max ("+max+").");
		return nextInt(max-min)+min;
	}

	/**When you don't need a known order, you get slightly better randomness by sharing the instance*/
	public static final Random rand = new Random();

	private static final long serialVersionUID = 1L;
}
