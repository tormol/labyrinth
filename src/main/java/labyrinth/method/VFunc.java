package labyrinth.method;
import java.util.List;
import java.util.function.Function;

import labyrinth.Window;
import labyrinth.Window.ErrorDialog;

public interface VFunc extends Value {
	@Override
	Value call(List<Value> param);
	@Override
	default boolean eq(Value v) {
		return v==this;
	}
	/**throw if invalid*/
	void validateCall(List<Class<? extends Value>> param, Class<? extends Value> last) throws Window.ErrorDialog;
	/***/
	default Class<? extends Value> returnType() {
		return Value.class;
	}


	public class Method implements VFunc {
		public final Class<?extends Value>[] parameters;
		private final Function<Value[], Value> internal;
		public Method(Class<? extends Value>[] parameters, Function<Value[], Value> function) {
			this.parameters = parameters;
			this.internal = function;
		}
		@Override
		public Value call(List<Value> param) {
			return internal.apply(param.toArray(new Value[param.size()]));
		}
		@Override
		public void validateCall(List<Class<? extends Value>> param,
				Class<? extends Value> last) {
			//TODO, not used yet
		}
	}


	//new("length", VString.class, VInt.class, vstr->VInt.v(vstr.string.length()) );
	/**single-parameter functions without side effects, argument can be parameter or Script.last*/
	static class OneP implements Operation, VFunc {
		public final Class<? extends Value> in, out;
		private final Function<Value, Value> internal;
		//TODO: make static creator using generics to simplify.
		/**in and out are determined from the example value and the result from calling f on the example value*/
		@SuppressWarnings("unchecked")//Is checked when used
		//altough value.Type() would work, this allows more specific error messages and slightly shorter code
		public <IN extends Value> OneP(IN example, Function<IN, Value> f) {
			this(example.getClass(), f.apply(example).getClass(), (Function<Value, Value>)f);
		}
		public OneP(Class<? extends Value> in, Class<? extends Value> out, Function<Value, Value> f) {
			this.in = in;
			this.out = out;
			this.internal = f;
		}

		@Override
		public Value call(List<Value> param) {
			Value arg;
			if (param == null  ||  !param.isEmpty())
				arg = Script.scr.last;
			else if (param.size() == 1)
				arg = param.get(0);
			else
				throw Script.error("more than one argument");
			if (!in.isInstance(arg))
				throw Script.error("Wrong type, must be %s", types.get(in));
			return internal.apply(arg);
		}
		@Override
		public Value perform() {
			return call(null);
		}
		@Override
		public void validateCall(List<Class<? extends Value>> param,  Class<? extends Value> last) throws ErrorDialog {
			
		}
	}
}