package tbm.util.collections;
import java.util.Iterator;

@SuppressWarnings("unchecked")//(E)Stackable<E>
/**Kan ikke implementere Iterator fordi next(){return next;} 1: ville utelatt seg selv og 2: ikke oppdaterer next, saa next() aldri slutter aa returnere element nr. 2*/
public abstract class Stackable<E extends Stackable<E>> extends Stack.Stackable<E> implements Iterable<E> {
	public static <E extends Stackable<E>> E push(E stack, E value) {
		value.next = stack;
		return value;
	}


	protected E next;
	protected Stackable() {
		next = null;
	}
	protected Stackable(E stack) {
		next = stack;
	}
	
	public int size() {
		int size = 1;
		for (E e = next; e!= null;  e=e.next)
			size++;
		return size;
	}

	public E reverse() {
		E prev = (E)this;
		E e = next;
		while (e != null) {
			E tmp = e.next;
			e.next = prev;
			prev = e;
			e = tmp;
		}
		next = null;
		return prev;
	}

	public E get() {
		return next;
	}

	public E get(int index) {
		if (index<0) {
			index = size() + index;
			if (index < 0)
				throw new ArrayIndexOutOfBoundsException();
		}
		//v1: telle ned fra index, ungaa brackets: for(e=this; index>0; index--, e=e.next)if(null) 
		//v2: optimalisere ved aa fjerne if(null): try{e=this;for(;index>0;e=e.next)index--;return e}catch Null{throw OutOfBounds}
		//innsaa til slutt at optimaliseringen gjorde koden saa kompleks at innsparte linjer ble oppveid av kommentarer.
		E e=(E)this;
		for (int i=0; i<index; i++) {
			e=e.next;
			//hvis denne var fo/r e=e.next ville get(size()) returnert null. 
			if (e==null)
				throw new ArrayIndexOutOfBoundsException();
		}
		return e;
	}

	public Stack<E> toStack() {
		return new Stack<E>((E) this);
	}


	public boolean hasNext() {
		return (next != null);
	}


	@Override//Iterable
	public Iterator<E> iterator() {
		return toStack();
	}
}
