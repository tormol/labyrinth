package no.torbmol.util.defaultCollections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class DefaultCollections {
	 /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    static final int collection_MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Reallocates the array being used within toArray when the iterator
     * returned more elements than expected, and finishes filling it from
     * the iterator.
     *
     * @param r the array, replete with previously stored elements
     * @param it the in-progress iterator over this collection
     * @return array containing the elements in the given array, plus any
     *         further elements returned by the iterator, trimmed to size
     */
    @SuppressWarnings("unchecked")
    static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        int i = r.length;
        while (it.hasNext()) {
            int cap = r.length;
            if (i == cap) {
                int newCap = cap + (cap >> 1) + 1;
                // overflow-conscious code
                if (newCap - collection_MAX_ARRAY_SIZE > 0)
                    newCap = hugeCapacity(cap + 1);
                r = Arrays.copyOf(r, newCap);
            }
            r[i++] = (T)it.next();
        }
        // trim if overallocated
        return (i == r.length) ? r : Arrays.copyOf(r, i);
    }

    static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError
                ("Required array size too large");
        return (minCapacity > collection_MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            collection_MAX_ARRAY_SIZE;
    }




    //  String conversion
    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
     public static <E> String toString(Collection<E> iter) {
        Iterator<E> it = iter.iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == iter ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }


     // Comparison and hashing of Maps

     /**
      * Compares the specified object with this map for equality.  Returns
      * <tt>true</tt> if the given object is also a map and the two maps
      * represent the same mappings.  More formally, two maps <tt>m1</tt> and
      * <tt>m2</tt> represent the same mappings if
      * <tt>m1.entrySet().equals(m2.entrySet())</tt>.  This ensures that the
      * <tt>equals</tt> method works properly across different implementations
      * of the <tt>Map</tt> interface.
      *
      * @implSpec
      * This implementation first checks if the specified object is this map;
      * if so it returns <tt>true</tt>.  Then, it checks if the specified
      * object is a map whose size is identical to the size of this map; if
      * not, it returns <tt>false</tt>.  If so, it iterates over this map's
      * <tt>entrySet</tt> collection, and checks that the specified map
      * contains each mapping that this map contains.  If the specified map
      * fails to contain such a mapping, <tt>false</tt> is returned.  If the
      * iteration completes, <tt>true</tt> is returned.
      *
      * @param o object to be compared for equality with this map
      * @return <tt>true</tt> if the specified object is equal to this map
      */
     public static <K,V> boolean map_equals(Map<K,V> map, Object o) {
         if (o == map)
             return true;

         if (!(o instanceof Map))
             return false;
         Map<?,?> m = (Map<?,?>) o;
         if (m.size() != map.size())
             return false;

         try {
             Iterator<Entry<K,V>> i = map.entrySet().iterator();
             while (i.hasNext()) {
                 Entry<K,V> e = i.next();
                 K key = e.getKey();
                 V value = e.getValue();
                 if (value == null) {
                     if (!(m.get(key)==null && m.containsKey(key)))
                         return false;
                 } else {
                     if (!value.equals(m.get(key)))
                         return false;
                 }
             }
         } catch (ClassCastException unused) {
             return false;
         } catch (NullPointerException unused) {
             return false;
         }

         return true;
     }

     /**
      * Returns the hash code value for this map.  The hash code of a map is
      * defined to be the sum of the hash codes of each entry in the map's
      * <tt>entrySet()</tt> view.  This ensures that <tt>m1.equals(m2)</tt>
      * implies that <tt>m1.hashCode()==m2.hashCode()</tt> for any two maps
      * <tt>m1</tt> and <tt>m2</tt>, as required by the general contract of
      * {@link Object#hashCode}.
      *
      * @implSpec
      * This implementation iterates over <tt>entrySet()</tt>, calling
      * {@link Map.Entry#hashCode hashCode()} on each element (entry) in the
      * set, and adding up the results.
      *
      * @return the hash code value for this map
      * @see Map.Entry#hashCode()
      * @see Object#equals(Object)
      * @see Set#equals(Object)
      */
     public static <K,V> int map_hashCode(Map<K,V> map) {
         int h = 0;
         Iterator<Entry<K,V>> i = map.entrySet().iterator();
         while (i.hasNext())
             h += i.next().hashCode();
         return h;
     }

     /**
	 * Removes from this list all of the elements whose index is between
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
	 * Shifts any succeeding elements to the left (reduces their index).
	 * This call shortens the list by {@code (toIndex - fromIndex)} elements.
	 * (If {@code toIndex==fromIndex}, this operation has no effect.)
	 *
	 * <p>This method is called by the {@code clear} operation on this list
	 * and its subLists.  Overriding this method to take advantage of
	 * the internals of the list implementation can <i>substantially</i>
	 * improve the performance of the {@code clear} operation on this list
	 * and its subLists.
	 *
	 * <p>This implementation gets a list iterator positioned before
	 * {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
	 * followed by {@code ListIterator.remove} until the entire range has
	 * been removed.  <b>Note: if {@code ListIterator.remove} requires linear
	 * time, this implementation requires quadratic time.</b>
	 *
	 * @param fromIndex index of first element to be removed
	 * @param toIndex index after last element to be removed
	 */
	public static <E> void list_removeRange(List<E> list, int fromIndex, int toIndex) {
	    ListIterator<E> it = list.listIterator(fromIndex);
	    for (int i=0, n=toIndex-fromIndex; i<n; i++) {
	        it.next();
	        it.remove();
	    }
	}

	/**
      * Returns a string representation of this map.  The string representation
      * consists of a list of key-value mappings in the order returned by the
      * map's <tt>entrySet</tt> view's iterator, enclosed in braces
      * (<tt>"{}"</tt>).  Adjacent mappings are separated by the characters
      * <tt>", "</tt> (comma and space).  Each key-value mapping is rendered as
      * the key followed by an equals sign (<tt>"="</tt>) followed by the
      * associated value.  Keys and values are converted to strings as by
      * {@link String#valueOf(Object)}.
      *
      * @return a string representation of this map
      */
     static public <K,V> String map_toString(Map<K,V> map) {
         Iterator<Entry<K,V>> i = map.entrySet().iterator();
         if (! i.hasNext())
             return "{}";

         StringBuilder sb = new StringBuilder();
         sb.append('{');
         for (;;) {
             Entry<K,V> e = i.next();
             K key = e.getKey();
             V value = e.getValue();
             sb.append(key   == map ? "(this Map)" : key);
             sb.append('=');
             sb.append(value == map ? "(this Map)" : value);
             if (! i.hasNext())
                 return sb.append('}').toString();
             sb.append(',').append(' ');
         }
     }

	static void list_rangeCheckForAdd(int index, int size) {
	    if (index < 0 || index > size)
	        throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
	}

	static String outOfBoundsMsg(int index, int size) {
        return "Index: "+index+", Size: "+size;
    }

    // Comparison and hashing

    /**
     * Compares the specified object with this list for equality.  Returns
     * {@code true} if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.  (Two elements {@code e1} and
     * {@code e2} are <i>equal</i> if {@code (e1==null ? e2==null :
     * e1.equals(e2))}.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.<p>
     *
     * This implementation first checks if the specified object is this
     * list. If so, it returns {@code true}; if not, it checks if the
     * specified object is a list. If not, it returns {@code false}; if so,
     * it iterates over both lists, comparing corresponding pairs of elements.
     * If any comparison returns {@code false}, this method returns
     * {@code false}.  If either iterator runs out of elements before the
     * other it returns {@code false} (as the lists are of unequal length);
     * otherwise it returns {@code true} when the iterations complete.
     *
     * @param o the object to be compared for equality with this list
     * @return {@code true} if the specified object is equal to this list
     */
    public static <E> boolean equals(List<E> list, Object o) {
        if (o == list)
            return true;
        if (!(o instanceof List))
            return false;

        ListIterator<E> e1 = list.listIterator();
        ListIterator<?> e2 = ((List<?>) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    /**
     * Returns the hash code value for this list.
     *
     * <p>This implementation uses exactly the code that is used to define the
     * list hash function in the documentation for the {@link List#hashCode}
     * method.
     *
     * @return the hash code value for this list
     */
    public static <E> int list_hashCode(List<E> list) {
        int hashCode = 1;
        for (E e : list)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }



    //set methods ovveriding Object's
    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the given object is also a set, the two sets have
     * the same size, and every member of the given set is contained in
     * this set.  This ensures that the <tt>equals</tt> method works
     * properly across different implementations of the <tt>Set</tt>
     * interface.<p>
     *
     * This implementation first checks if the specified object is this
     * set; if so it returns <tt>true</tt>.  Then, it checks if the
     * specified object is a set whose size is identical to the size of
     * this set; if not, it returns false.  If so, it returns
     * <tt>containsAll((Collection) o)</tt>.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    public static <E> boolean set_equals(Set<E> set, Object o) {
        if (o == set)
            return true;

        if (!(o instanceof Set))
            return false;
        Collection<?> c = (Collection<?>) o;
        if (c.size() != set.size())
            return false;
        try {
            return set.containsAll(c);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set,
     * where the hash code of a <tt>null</tt> element is defined to be zero.
     * This ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
     * and <tt>s2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     *
     * <p>This implementation iterates over the set, calling the
     * <tt>hashCode</tt> method on each element in the set, and adding up
     * the results.
     *
     * @return the hash code value for this set
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    public static <E> int set_hashCode(Set<E> set) {
        int h = 0;
        Iterator<E> i = set.iterator();
        while (i.hasNext()) {
            E obj = i.next();
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }
}