package tbm.util.collections;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;

public class CollectionTests {
	public static Test suite() {
		TestSuite s = new TestSuite("tbm.util.collections");
		s.addTest(SetTestSuiteBuilder
		        .using(new TestStringSetGenerator() {
		            @Override public Set<String> create(String[] elements) {
		              return new LeanHashSet<String>(MinimalCollection.of(elements));
		            }
		          })
		        .named("LeanHashSet")
		        .withFeatures(
		            SetFeature.GENERAL_PURPOSE,
		            CollectionFeature.SERIALIZABLE,
		            CollectionSize.ANY)
		        .createTestSuite());
		return s;
	}
}
