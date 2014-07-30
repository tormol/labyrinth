package labyrinth.engine.method;
import java.util.List;
//import static tbm.util.statics.*;

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
		return new Instance(Script.current);
	}

	public class Instance extends Value implements Operation {
		public final Scope parent;
		public Instance(Scope parent) {
			super(VType.FUNC);
			this.parent = parent;
		}

		@Override//Operation, Value
		public Value perform() {
			Script.current = new Scope(Script.current);
			Script.run(operations);
			Script.current = Script.current.parent;
			return Script.last;
		}

		@Override//Value
		public Value call(List<Value> param) {
			if (param.size() != 0)
				throw Script.error("this function takes no parameters");
			return perform();
		}
	}
}
