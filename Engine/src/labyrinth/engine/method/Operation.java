package labyrinth.engine.method;
import java.util.ArrayList;
import labyrinth.engine.Action;

//interface action in code called from code void(TIle, Mob)
/**interface operation in code called from compiled file Value(Tile, Mob) implements action*/
public interface Operation extends Action {
	Value perform();
	/**interface compiler in code called from file operation(Value[])*/
	//class procedure in file called from map and file implements operation with name
	//class method accepts compiler
	public static interface Compiler {
		Operation instance(Value[] params);
	}

	public static class Call implements Operation {
		public final Object toCall;
		public final Iterable<Object> param;
		public final String description;
		public Call(Object toCall, Iterable<Object> params, String description) {
			this.toCall = toCall;
			this.param = params;
			this.description = description;
		}
		@Override
		public Value perform() {
			//must do it in order
			Value call = Value.get(toCall);
			ArrayList<Value> p = new ArrayList<>();
			param.forEach(e->p.add(Value.get(e))); 
			return call.call(p);
		}
	}

	/**Declare a variable*/
	public static class Declare implements Operation {
		public final String name;
		public final boolean _final;
		public final Class<? extends Value> type;
		//Would it be possible to use Scope.Variable as a Declare Operation?
		public Declare(String name, boolean _final, Class<? extends Value> type) {
			this.name = name;
			this._final = _final;
			this.type = type;
		}
		@Override
		public Value perform() {
			return Script.current.declare(name, _final, type);
		}
	}

	public static class UnDeclare implements Operation {
		public final String name;
		public UnDeclare(String name) {
			this.name = name;
		}
		@Override
		public Value perform() {
			Script.current.remove(name);
			return Value.Void;
		}
	}

	public static Operation GetLast = ()->Script.last;

	public static class GetRef implements Operation {
		public final String name;
		public GetRef(String name) {
			this.name = name;
		}
		@Override
		public Value perform() {
			return Script.current.get_variable(name);
		}
	}
}
