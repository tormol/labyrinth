package tbm.util.collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.*;

/**Can only test features from existing interfaces*/
public class LeanHashTests {
	public static Test suite() {
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
						Map<String,String> map = new HashMap<String, String>();
						for (Entry<String,String> e : entries)
							map.put(e.getKey(), e.getValue());
						return map;
					}})
				.named("java.util.HashMap for comparison")
				.withFeatures(
						MapFeature.GENERAL_PURPOSE,
						MapFeature.ALLOWS_NULL_VALUES,
						MapFeature.ALLOWS_NULL_KEYS,
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
						MapFeature.REJECTS_DUPLICATES_AT_CREATION,
						MapFeature.ALLOWS_NULL_VALUES,
						MapFeature.ALLOWS_NULL_KEY_QUERIES,
						MapFeature.ALLOWS_NULL_ENTRY_QUERIES,
						//CollectionFeature.SERIALIZABLE,
						CollectionSize.ANY)
				.createTestSuite());
		return s;
	}
}
