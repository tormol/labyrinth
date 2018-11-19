package no.torbmol.util.collections;
import java.util.Set;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.Test;
import junit.framework.TestSuite;

public class SetWithGetTests {
	public static Test suite() {
		TestSuite s = new TestSuite("no.torbmol.util.collections.SetWithGet");
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
		return s;
	}

	//TODO test WrapSetToAddGet
	//TODO test ImmutableSet
	//TODO test LeanHashSet
}
