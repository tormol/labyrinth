package tbm.util.collections;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.google.common.collect.testing.CollectionTestSuiteBuilder;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.SortedSetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringCollectionGenerator;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.TestStringSortedSetGenerator;
import com.google.common.collect.testing.features.*;

/**Can only test features from existing interfaces*/
public class CollectionTests {
	public static Test suite() {
		TestSuite s = new TestSuite("tbm.util.collections");
		//test the unmodifiables first as they have less tests
		s.addTest(SetTestSuiteBuilder
				.using(new TestStringSetGenerator() {@Override protected Set<String> create(String[] elements) {
					return UnmodifiableSet.small(elements);}})
				.named("UnmodifiableSmallSet")
				.withFeatures(
						CollectionFeature.REJECTS_DUPLICATES_AT_CREATION,
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionFeature.SERIALIZABLE,
						CollectionFeature.KNOWN_ORDER,
						CollectionSize.ANY)
				.createTestSuite());
		s.addTest(SortedSetTestSuiteBuilder
				.using(new TestStringSortedSetGenerator() {@Override protected SortedSet<String> create(String[] elements) {
					return new UnmodifiableSortedSet<String>(elements);}})
				.named("UnmodifiableSortedSet")
				.withFeatures(
						CollectionFeature.REJECTS_DUPLICATES_AT_CREATION,
						CollectionFeature.ALLOWS_NULL_QUERIES,
						CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		s.addTest(SetTestSuiteBuilder
				.using(new TestStringSetGenerator() {@Override protected Set<String> create(String[] elements) {
					return new UnmodifiableStartTableHashSet<String>(elements, false);}})
				.named("UnmodifiableStartTableHashSet")
				.withFeatures(
						CollectionFeature.REJECTS_DUPLICATES_AT_CREATION,
						CollectionFeature.ALLOWS_NULL_QUERIES,
						CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());

		//modifiables
		s.addTest(SetTestSuiteBuilder
				.using(new TestStringSetGenerator() {@Override protected Set<String> create(String[] elements) {
					return SetWithGet.from(new java.util.HashSet<String>(MinimalCollection.of(elements)));}})
				.named("WrapSetToAddGet")
				.withFeatures(
						SetFeature.GENERAL_PURPOSE,
						CollectionFeature.SERIALIZABLE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionSize.ANY)
				.createTestSuite());
		s.addTest(CollectionTestSuiteBuilder
				.using(new TestStringCollectionGenerator() {@Override protected Collection<String> create(String[] elements) {
					return new ArrayCollection<String>(elements);}})
				.named("ArrayCollection")
				.withFeatures(
						CollectionFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		s.addTest(SetTestSuiteBuilder
				.using(new TestStringSetGenerator() {@Override protected Set<String> create(String[] elements) {
					return new LeanHashSet<String>(MinimalCollection.of(elements));}})
				.named("LeanHashSet")
				.withFeatures(
						SetFeature.GENERAL_PURPOSE,
						CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		return s;
	}
}
