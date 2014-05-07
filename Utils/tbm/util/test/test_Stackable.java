package tbm.util.test;
import tbm.util.Stackable;
public class test_Stackable {
	static class Test extends Stackable<Test> {
		public final String word;
		public Test(String word) {
			this.word = word;
		}
	}

	public static void main(String[] args) {
		String line = "The small red fox jumped over the lazy brown dog.";
		Test t = null;
		for (String word : line.split(" "))
			t = Stackable.push(t, new Test(word));
		t = t.reverse();
		for (Test e : t)
			System.out.println(e.word);
	}
}
