package tbm.util.collections;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.*;

public class Dat200ListTests {
	public static Test suite() {
		TestSuite s = new TestSuite("Dat200 collections");
		s.addTest(ListTestSuiteBuilder
				.using(new TestStringListGenerator() {@Override protected List<String> create(String[] elements) {
					return new ArrayList<String>(MinimalCollection.of(elements));}})
				.named("ArrayList")
				.withFeatures(
						ListFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						//CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		s.addTest(ListTestSuiteBuilder
				.using(new TestStringListGenerator() {@Override protected List<String> create(String[] elements) {
					return new LinkedList<String>(MinimalCollection.of(elements));}})
				.named("LinkedList")
				.withFeatures(
						ListFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						//CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		return s;
	}
}
