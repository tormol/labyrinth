package tbm.util.collections;
import java.util.Collection;
import java.util.Objects;

public class UnmodifiableLeanHashSet<E> extends UnmodifiableHashSet<E> {
	@SuppressWarnings("unchecked")
	public static <E> UnmodifiableLeanHashSet<E> from(Collection<E> col) {
		Objects.requireNonNull(col);
		int size = col.size();
		int power = Integer.highestOneBit(size);
		if (power < size)
			power *= 2;
		E[] elements = col.toArray((E[])new Object[power]);
		int hashes[] = new int[power];
		int shift=0;
		//check for nulls and duplicates, and hash everything
		for (int i=0; i<size; i++) {
			Objects.requireNonNull(elements[i]);
			hashes[i] = elements[i].hashCode();
			for (int ii=0; ii<i; ii++)
				if (hashes[i] == hashes[ii])
					if (elements[i].equals(elements[ii]))
						throw new RuntimeException("Duplicate elements \""+elements[ii]+"\" in collection.");
					else
						;//TODO the difficult part
				else
					while (hash(hashes, i, shift)  ==  hash(hashes, ii, shift)
					 &&    shift  <  Integer.SIZE / (hashes.length-1))//wrong
						shift++;
		}
	}

	protected static boolean colission(int[] hashes, int size, int shift) {
		for (int i=1; i<size; i++)
			for (int ii=0; ii<i; ii++)
				if (((hashes[i ]>>shift) & (hashes.length-1))
				 == ((hashes[ii]>>shift) & (hashes.length-1)))
					return true;
		return false; 
	}



	public final long[] buckets;
	public final int size, shift;

	protected UnmodifiableLeanHashSet(E[] elements, long[] buckets, int size, int shift) {
		super(elements);
		this.buckets = buckets;
		this.size = size;
		this.shift = shift;
	}
	public UnmodifiableLeanHashSet(LeanHashSet<E> lhs) {
		this(lhs.elements, lhs.buckets, lhs.size(), 0);
	}
	

	protected static <T> int hash(int[] hashes, int index, int shift) {
		return (hashes[index]>>shift) & (hashes.length-1);//FIXME wrong
	}
	@Override protected int hash(Object o) {
		return (o.hashCode()>>shift) & (elements.length-1);//FIXME wrong
	}

	@Override protected int indexOf(Object o) {
		long bucket = buckets[hash(o)];
		int bucket_start = (int) (bucket >> 32);
		int bucket_size = (int) (bucket & 0xffffffff);
		for (int i=0; i<bucket_size; i++)
			if (elements[bucket_start + i].equals(o))
				return bucket_start + i; 
		return -1;
	}

	@Override public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	@Override public arrayIterators.Unmodifiable<E> iterator() {
		return new arrayIterators.UnmodifiableSkipEmpty<E, Object>((E[])elements, ArrayCollection.empty);
	}
}
