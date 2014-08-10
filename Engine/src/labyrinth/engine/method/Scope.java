package labyrinth.engine.method;
import static tbm.util.statics.map_firstKey;
import java.util.HashMap;
import java.util.Map;

//can get map.getentry() with reflection, but thats dirty.
public class Scope {
	public final Scope parent;
	private final Map<String, Variable> vars = new HashMap<>();
	public Scope(Scope parent) {
		this.parent = parent;
	}
	public Variable declare(String name) {
		if (vars.containsKey(name))
			throw Script.error("The variable \"%s\" is already declared.", name);
		return vars.put(name, new Variable(true, Value.Void));
	}
	/**for inbuilt variables*/
	public void define(String name, Value inbuilt) {
		vars.put(name, new Variable(true, inbuilt));
	}
	public Variable search(String name) {
		for (Scope s=this; s!=null; s=s.parent) {
			Variable v = s.vars.get(name);
			if (v != null)
				return v;
		}
		return null;
	}
	public Value get(String name) {
		Variable v = search(name);
		if (v==null)
			throw Script.error("The variable \"%s\" is not defined.", name);
		return v.value;
	}
	Scope outerClass() {
		return this;
	}

	public class Variable implements Value.VRef {
		private Value value;
		private boolean _final = false;
		public final boolean _static;
		Variable(boolean _static, Value initial) {
			this._static = _static;
			this.value = initial;
		}
		public boolean isFinal() {
			return _final;
		}
		public boolean isStatic() {
			return _static;
		}
		public void finalize() {
			_final = true;
		}
		public Value get() {
			return value;
		}
		public void set(Value v) {
			if (_final)
				throw Script.error("The variable has been finalized.");
			if (v == null)
				throw Script.error("tried to set a variable to null");
			value = v;
		}
		public String findName() {
			return map_firstKey(vars, this);
		}
		public Scope getScope() {
			return outerClass();
		}

		@Override//Value
		public void setRef(Value v) {
			this.value = v;
		}
		@Override//Value
		public Value getRef() {
			return this.value;
		}
	}
}
