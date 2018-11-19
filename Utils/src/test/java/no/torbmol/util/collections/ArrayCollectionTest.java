package no.torbmol.util.collections;
import java.util.Collection;
import com.google.common.collect.testing.CollectionTestSuiteBuilder;
import com.google.common.collect.testing.TestStringCollectionGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import org.junit.Test;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ArrayCollectionTest.GuavaTests.class,
	ArrayCollectionTest.NonListFeaturesTests.class
})public class ArrayCollectionTest {

public static class GuavaTests {
	public static junit.framework.Test suite() {
		junit.framework.TestSuite s = new junit.framework.TestSuite("guava");
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
		return s;
	}
}

public static class NonListFeaturesTests {
	@Test public void toArrayClassTest() {
		assertEquals("correct class",
				String[].class,
				new ArrayCollection<Object>("e","d").toArray(String[].class).getClass()
			);
	}
	@Test public void constructorTest() {
		assertArrayEquals("vararg", new Object[]{"a","b"}, new ArrayCollection<String>("a", "b").toArray());
		assertArrayEquals("no-arg", new Object[]{}, new ArrayCollection<String>().toArray());
		//collection is used by guava
	}
}

}
