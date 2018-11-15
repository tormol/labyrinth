package labyrinth.engine.method;
import java.util.List;
//import static tbm.util.statics.*;

/**script-created functions*/
public class Procedure implements Operation {
	private Iterable<Object> operations;
	private List<String> param_names;
	public final String description;

	public Procedure(Iterable<Object> operations, List<String> param_names, String description) {
		this.description = description;
		this.param_names = param_names;
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

	/**A function defined inside one block and assigned to an outer variable might be used from another block.
	 * Each time the code the function is declared in is run, the code is the same but with different values in scopes*/
	public class Instance implements VFunc {
		public final Scope parent;
		public Instance(Scope parent) {
			this.parent = parent;
		}

		@Override//Value
		public Value call(List<Value> param_values) {
			Scope s = new Scope(Script.scr.current, description);
			// ignore extra parameters if the function doesn't have a parameter list
			if (param_names != null) {
				if (param_values.size() != param_names.size()) {
					throw Script.error(
							"function (%s) takes %i parameters but %i were passed",
							description,
							param_names.size(),
							param_values.size()
					);
				}
				for (int i = 0; i < param_values.size(); i++) {
					s.declare(param_names.get(i), false, null).set(param_values.get(i));
				}
			}
			Script.scr.current = s;
			Script.run(operations);
			Script.scr.current = Script.scr.current.parent;
			return Script.scr.last;
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
