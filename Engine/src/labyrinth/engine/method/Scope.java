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
	public Variable declare(String name, boolean _final) {
		if (vars.containsKey(name))
			throw Script.error("The variable \"%s\" is already declared.", name);
		Variable var = new Variable(_final, Value.Void);
		vars.put(name, var);//put() return the previous value, which is null.
		return var;
	}
	/**for inbuilt variables*/
	public void define(String name, Value inbuilt) {
		vars.put(name, new Variable(true, inbuilt));
	}
	/**search this and parents for the variable
	 *@return the reference or null if it's not found*/
	public Variable search(String name) {
		for (Scope s=this; s!=null; s=s.parent) {
			Variable v = s.vars.get(name);
			if (v != null)
				return v;
		}
		return null;
	}
	/**Is the valiable declared in this scope?*/
	public boolean has(String name) {
		return vars.get(name) != null;
	}
	/**search this and parents for the variable, and create an error if not declared
	 *@return the value*/
	public Value get(String name) {
		Variable v = search(name);
		if (v==null)
			throw Script.error("The variable \"%s\" is not defined.", name);
		return v.value;
	}
	/**remove the variable if it's declared in this scope*/
	public void remove(String name) {
		if (vars.remove(name) == null)
			throw Script.error("The variable \"%s\" is not defined.", name);
	}
	Scope outerClass() {
		return this;
	}

	public class Variable implements Value.VRef {
		private Value value;
		public final boolean _final;
		Variable(boolean _final, Value initial) {
			this._final = _final;
			this.value = initial;
		}
		public boolean isFinal() {
			return _final;
		}
		public Value get() {
			return value;
		}
		public void set(Value v) {
			if (_final  &&  value != Value.Void)
				throw Script.error("The variable is final.");
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
			set(v);
		}
		@Override//Value
		public Value getRef() {
			return this.value;
		}
	}
}
