package tbm.util.collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import junit.framework.TestSuite;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.*;

/**Can only test features from existing interfaces*/
@RunWith(Suite.class)
@Suite.SuiteClasses({
	LeanHashTests.GuavaTests.class,
	LeanHashTests.NonMapFeaturesTests.class
})public class LeanHashTests {

public static class GuavaTests {
	public static junit.framework.Test suite() {
		TestSuite s = new TestSuite("tbm.util.LeanHash");
		s.addTest(SetTestSuiteBuilder
				.using(new TestStringSetGenerator() {@Override protected Set<String> create(String[] elements) {
					return new LeanHashSet<String>(MinimalCollection.of(elements));}})
				.named("LeanHashSet")
				.withFeatures(
						SetFeature.GENERAL_PURPOSE,
						CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		s.addTest(MapTestSuiteBuilder
				.using(new TestStringMapGenerator() {@Override protected Map<String, String> create(Entry<String, String>[] entries) {
						Map<String,String> map = new LeanHashMap<String, String>();
						for (Entry<String,String> e : entries)
							map.put(e.getKey(), e.getValue());
						return map;
					}})
				.named("LeanHashMap")
				.withFeatures(
						MapFeature.GENERAL_PURPOSE,
						MapFeature.ALLOWS_NULL_VALUES,
						MapFeature.ALLOWS_NULL_ENTRY_QUERIES,
						CollectionFeature.REMOVE_OPERATIONS,
						CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		return s;
	}
}


public static class NonMapFeaturesTests {
	//TODO test LeanHashSets SetWithGet implementation
	//TODO test LeanHashMap.iterator();
}
}
