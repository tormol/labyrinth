package tbm.util;
import java.util.function.Supplier;
import java.util.ListIterator;
import java.util.Iterator;
//array[index>>1] and odd numbers are between doesnt work with set()
public abstract class ArrayIterator<E> implements Iterator<E> {
	@SafeVarargs
	public static <E> ArrayIterator<E> iter(E... a) {
		return new ArrayIterator<E>(){protected E[] getArray(){
			return a;
		}};
	}
	public static <E> ArrayIterator<E> iter(Supplier<E[]> prod) {
		return new ArrayIterator<E>(){protected E[] getArray(){
			return prod.get();
		}};
	}
	protected abstract E[] getArray();
	int index=-1;//last

	@Override//Iterator
	public boolean hasNext() {
		return (index+1)>>1 < getArray().length;
	}@Override//Iterator
	public E next() {
		index++;
		if (index == getArray().length) {
			index--;
			return null;
		}
		return getArray()[index];
	}
	public E peek() {
		E e = next();
		index--;
		return e;
	}
	public void skip() {
		index++;
	}@Override//Iterator
	public void remove() {
		throw new UnsupportedOperationException();		
	}//ListIterator
	public void set(E e) {
		if (index == -1)
			throw new IllegalStateException();
		getArray()[index>>1] = e;
	}//ListIterator
	public boolean hasPrevious() {
		return index != -1;
	}//ListIterator
	public int nextIndex() {
		return index+1;
	}//ListIterator
	public E previous() {
		if (index==-1)
			return null;
		return getArray()[index];
	}//ListIterator
	public int previousIndex() {
		return index;
	}


	public static abstract class List<E> extends ArrayIterator<E> {
		@Override//Iterator
		public boolean hasNext() {
			return (index+1)>>1 < getArray().length;
		}@Override//Iterator
		public E next() {
			index++;
			if (index>>1 == getArray().length) {
				index--;
				return null;
			}
			E e = getArray()[index>>1];
			index++;
			return e;
		}
		public E peek() {
			E e = next();
			index-=2;
			return e;
		}
		public void skip() {
			index+=2;
		}//ListIterator
		public void set(E e) {
			if (index == -1)
				throw new IllegalStateException();
			getArray()[(index-1)>>1] = e;
		}//ListIterator
		public void add(E e) {
			throw new UnsupportedOperationException();
		}//ListIterator
		public boolean hasPrevious() {
			return index != -1;
		}//ListIterator
		public int nextIndex() {
			return index+1;
		}//ListIterator
		public E previous() {
			if (index==-1)
				return null;
			index--;
			E e = getArray()[index];
			index--;
			return e;
		}//ListIterator
		public int previousIndex() {
			return index-1;
		}
	}

	//array[index>>1] and odd numbers are between doesnt work with set()
	public static abstract class notsetable<E> extends List<E> implements ListIterator<E> {
		@Override//ListIterator
		public void set(E e) {
			throw new UnsupportedOperationException();
		}
	}



	public static abstract class Full<E> extends ArrayIterator<E> implements ListIterator<E> {
		private int next = 0;
		@Override//Iterator
		public E next() {
			if (next==getArray().length)
				return null;
			if (next>index)
				index++;
			next=index+1;
			return getArray()[index];
		}@Override//ListIterator
		public void set(E e) {
			if (index == -1)
				throw new IllegalStateException();
			getArray()[index] = e;
		}@Override//ListIterator
		public int nextIndex() {
			if (next>index)
				return next;
			return index;
		}@Override//ListIterator
		public E previous() {
			if (next==-1)
				return null;
			if (next<index)
				index--;
			next=index-1;
			return getArray()[index];
		}@Override//ListIterator
		public int previousIndex() {
			if (next<index)
				return next;
			return index;
		}
	}
}
