package tbm.util.test;
import java.util.ArrayDeque;
/**Find the rigth methods to remove undo last add iterate same order as add, This works*/
public class IterDeque {
	public static void main(String[] args) {
		ArrayDeque<String> deque = new ArrayDeque<>();
		deque.add("a");
		deque.add("b");
		deque.add("c");
		deque.removeLast();
		for (String str : deque)
			System.out.print(str);
		System.out.println("\ntoString: "+deque);
	}
}
