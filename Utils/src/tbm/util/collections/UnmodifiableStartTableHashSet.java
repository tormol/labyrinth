package tbm.util.collections;
import java.util.Arrays;

/**name is way to long
 * Cannot store null
 * Can have hash collisions
 * Constructor is slow, and calls .hashcode() at least two times on every element
 */
public class UnmodifiableStartTableHashSet<E> extends UnmodifiableHashSet<E> {
	/**must be between 0.5 and 1*/
	public static final float default_roundDown_treshold = 0.75f;
	protected final int[] startsAt;

	protected UnmodifiableStartTableHashSet(Object[] elements, boolean fromSet) {
		super(elements, true);//need to fill startsAt[] first
		int highest = Integer.highestOneBit(elements.length);
		int roundUp = (highest << 1) - 1;
		int roundDown = highest - 1;
		int hashes = elements.length*default_roundDown_treshold < roundDown
				? roundDown
				: roundUp;
		hashes = Integer.max(1, hashes);//else hashes is -1 and hash uses startsAt.length-2
		this.startsAt = new int[hashes+1];
		Arrays.sort(elements, (a,b)->hash(a)-hash(b) );
		int startsAt = 0;
		for (int hash=0; hash<hashes; hash++) {
			this.startsAt[hash] = startsAt;
			while (startsAt < elements.length  &&  hash == hash(elements[startsAt]))
				startsAt++;
		}
		this.startsAt[hashes] = startsAt;//the extra one

		if ( !fromSet)
			checkForDuplicates();
	}

	@Override protected void checkForDuplicates() throws IllegalArgumentException {
		for (int hash = 0;  hash+1 < startsAt.length;  hash++)//for each hash value
			for (int a = startsAt[hash]+1;  a < startsAt[hash+1];  a++)//for each element with hash except the first
				for (int b = startsAt[hash];  b < a;  b++)//for each element with hash before a
					if (elements[a].equals(elements[b]))
						throw new IllegalArgumentException("multiple "+elements[a]+'s');
	}

	//UnmodifiableHashSet methods
	@Override protected int hash(Object o) {
		return o.hashCode() & (startsAt.length-2);
	}

	@Override protected int indexOf(Object o) {
		if (o != null)
			for (int i=startsAt[hash(o)];  i<startsAt[hash(o)+1];  i++)
				if (elements[i].equals(o))
					return i;
		return -1;
	}

	private static final long serialVersionUID = 1L;
}
