package labyrinth.engine.method;
import java.util.List;
//import static tbm.util.statics.*;

/**script-created functions*/
public class Procedure implements Operation {
	private Iterable<Object> operations;
	public final String description;
	public Procedure(Iterable<Object> operations, String description) {
		this.description = description;
		this.operations = operations;
	}

	@Override//Operation
	/**Creates a new Instance*/
	public Value perform() {
		return new Instance(Script.scr.current);
	}

	public String toString() {
		return description;
	}

	/**A function defined inside one block and assigned to an outer variable might be used from anothe block.
	 * Each time the code the function is declared in is run, the code is the same but with different values in scopes*/
	public class Instance implements VFunc, Operation {
		public final Scope parent;
		public Instance(Scope parent) {
			this.parent = parent;
		}

		@Override//Operation, Value
		public Value perform() {
			Script.scr.current = new Scope(Script.scr.current, description);
			Script.run(operations);
			Script.scr.current = Script.scr.current.parent;
			return Script.scr.last;
		}

		@Override//Value
		public Value call(List<Value> param) {
			if (param.size() != 0)
				throw Script.error("this function takes no parameters");
			return perform();
		}

		public String toString() {
			return description + " - " + parent.toString();
		}

		@Override//VFunc
		public void validateCall(List<Class<? extends Value>> param,  Class<? extends Value> last) {
			if (!param.isEmpty())
				throw Script.error("This function take no parameters");
		}
	}
}
