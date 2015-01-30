package tbm.util.collections;
import java.util.AbstractSet;
import java.util.Iterator;

public class NotArrayHashSet<E> extends AbstractSet<E> {
	protected Object[] elements;
	
	@Override
	public boolean contains(Object o) {
		if (o == null  ||  elements[hash(o)] == null)
			return false;
		int hash = hash(o);
		if (elements[hash] == null)
			return false;
		return o.equals(elements[hash]); 
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>(){
			protected int elindex=0, bindex=0;
			@Override public boolean hasNext() {
				while (elindex < elements.length)
					if (elements[elindex] == null)
						index++;
					else
						return true;
				return false;
			}

			@Override public E next() {
				if (! hasNext())
					return null;
				E e = elements[index];
				index++;
				return e;
			}
		};
	}

	@Override
	public int size() {
		return size;
	}
}
