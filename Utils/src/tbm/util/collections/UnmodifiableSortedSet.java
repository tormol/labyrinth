package tbm.util.collections;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

public class UnmodifiableSortedSet<E extends Comparable<E>> extends UnmodifiableSet<E> implements SortedSet<E> {
	/**Create an UnmodifiableSortedSet with these elements.*/@SafeVarargs//constructor accepts Object[]
	public static <E extends Comparable<E>> UnmodifiableSortedSet<E> with(E... elements) {
		return new UnmodifiableSortedSet<E>(elements);
	}

	protected UnmodifiableSortedSet(Object[] elements, boolean sorted, boolean fromSet) {
		super(elements);
		if ( !sorted)
			Arrays.sort(elements);
		if ( !fromSet)
			for (int i=1; i<elements.length; i++)
				if (Arrays.binarySearch(elements, 0, i, elements[i])  <  0)
					throw new IllegalArgumentException("multiple "+elements[i]+'s');
	}
	@Override protected int indexOf(Object o) {
		return Arrays.binarySearch(elements, o);
	}


	public UnmodifiableSortedSet(E[] elements) throws IllegalArgumentException {
		this(elements, false, false);
	}
	public UnmodifiableSortedSet(Collection<? extends E> col) throws IllegalArgumentException {
		this(col.toArray(), false, false);
	}
	public UnmodifiableSortedSet(Set<? extends E> set) throws IllegalArgumentException {
		this(set.toArray(), false, true);
	}
	public UnmodifiableSortedSet(SortedSet<? extends E> set) {
		this(set.toArray(), true, true);
	}



	  /////////////////////
	 //SortedSet methods//
	/////////////////////

	/**{@inheritDoc}
	 *Always returns {@code null}*/@Override
	public Comparator<? super E> comparator() {
		return null;
	}

	@SuppressWarnings("unchecked")
	protected E elementAt(int index) {
		try {
			return (E)elements[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new NoSuchElementException();
		}
	}
	@Override public E first() {
		return elementAt(0);
	}
	@Override public E last() {
		return elementAt(elements.length-1);
	}

	protected final int wouldBeIndexOf(Object o) {
		int startIndex = indexOf(o);
		if (startIndex < 0)
			startIndex = ~startIndex;//"clever"
		return startIndex;
	}
	protected UnmodifiableSortedSet<E> subSet(int from, int to) {//make public when needed.
		return new UnmodifiableSortedSet<E>(Arrays.copyOfRange(elements, from, to), true, true);
	}
	@Override public UnmodifiableSortedSet<E> subSet(E from, E to) {
		return subSet(wouldBeIndexOf(from), wouldBeIndexOf(to));
	}
	@Override public SortedSet<E> headSet(E endsWith) {
		return subSet(0, wouldBeIndexOf(endsWith));
	}
	@Override public SortedSet<E> tailSet(E startsWith) {
		return subSet(wouldBeIndexOf(startsWith), elements.length);
	}

	private static final long serialVersionUID = 1L;
}
