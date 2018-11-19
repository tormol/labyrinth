package no.torbmol.util.collections;
import com.google.common.collect.testing.CollectionTestSuiteBuilder;
import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringCollectionGenerator;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.TestSuite;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import static no.torbmol.util.collections.arrayIterators.*;
//import static org.junit.Assert.*;

//cannot test randomAccessIterators since they are abstract
public class ArrayIteratorsTest {
	public static junit.framework.Test suite() {
		TestSuite s = new TestSuite("arrayIterators");
		//cannot test iterators directly

		s.addTest(CollectionTestSuiteBuilder
				.using(new TestStringCollectionGenerator() {@Override protected Collection<String> create(String[] elements) {
					return new AbstractCollection<String>() {
						@Override public int size() {
							return elements.length;
						}
						@Override public Iterator<String> iterator() {
							return new UnmodifiableArrayIterator<String>(elements);
						}						
					};}})
				.named("UnmodifiableArrayIterator")
				.withFeatures(
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionSize.ANY)
				.createTestSuite());

		s.addTest(ListTestSuiteBuilder
				.using(new TestStringListGenerator() {@Override protected List<String> create(String[] elements) {
					return new AbstractList<String>() {
						@Override public String get(int index) {
							return elements[index];
						}
						@Override public String set(int index, String e) {
							String old = get(index);
							elements[index] = e;
							return old;
						}
						@Override public int size() {
							return elements.length;
						}
						@Override public ListIterator<String> listIterator(int start) {
							return new ArrayListIterator<String>(elements, start);
						}
					};}})
				.named("ArrayListIterator")
				.withFeatures(
						ListFeature.SUPPORTS_SET,
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionSize.ANY)
				.createTestSuite());

		return s;
	}
}
