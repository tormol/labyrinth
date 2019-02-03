package no.torbmol.labyrinth.method;
import static no.torbmol.util.statics.map_firstKey;
import java.util.HashMap;
import java.util.Map;

// Could get map.getEntry() with reflection, but that's dirty.
public class Scope {
	public final Scope parent;
	private final Map<String, Variable> vars = new HashMap<>();
	public final String description;
	public Scope(Scope parent, String description) {
		if (description == null) {
			throw new IllegalArgumentException("description is null");
		}
		this.parent = parent;
		this.description = description;
	}
	/**Add new variable, but don't initialize it
	 * (is set to void but allows one assignment if final).
	 * The new variable is returned, and can be initialized by calling .set()*/
	Variable declare(String name, boolean _final, Class<? extends Value> type) {
		if (vars.containsKey(name)) {
			throw Script.error("The variable \"%s\" is already declared.", name);
		}
		Variable var = new Variable(_final, Value.Void, type);
		vars.put(name, var);//put() returns the previous value, which is null.
		return var;
	}
	/**Add inbuilt, final variables*/
	public void define(String name, Value inbuilt) {
		vars.put(name, new Variable(true, inbuilt, null));
	}
	/**remove the variable if it's declared in this scope*/
	public void remove(String name) {
		if (vars.remove(name) == null) {
			throw Script.error("The variable \"%s\" is not defined.", name);
		}
	}
	/**Is the variable defined in this scope?*/
	public boolean defined_here(String name) {
		return vars.get(name) != null;
	}


	/**search this and parents for the variable
	 *@return the reference or null if it's not found*/
	public Variable get_variable(String name) {
		for (Scope s=this; s!=null; s=s.parent) {
			Variable v = s.vars.get(name);
			if (v != null) {
				return v;
			}
		}
		return null;
	}
	/**search this and parents for the variable, and create an error if not declared
	 *@return the value*/
	public Value get_value(String name) {
		Variable v = get_variable(name);
		return v==null ? null : v.value;
	}
	/**search this and parents for the variable, and create an error if not declared
	 *@throws Script.error() if */
	public Value value(String name) {
		Variable v = get_variable(name);
		if (v == null) {
			throw Script.error("The variable \"%s\" is not defined.", name);
		}
		return v.value;
	}



	private Scope outerClass() {
		return this;
	}
	public class Variable implements Value.VRef {
		private Value value;
		public final boolean _final;
		public final Class<? extends Value> type;
		Variable(boolean _final, Value initial, Class<? extends Value> type) {
			this._final = _final;
			this.value = initial;
			if (type == null) {
				type = Value.class;
			}
			this.type = type;
		}
		public boolean isFinal() {
			return _final;
		}
		public Value get() {
			return value;
		}
		public void set(Value v) {
			if (_final  &&  value != Value.Void) {
				throw Script.error("The variable is final.");
			} else if (v == null) {
				throw Script.error("tried to set a variable to null");
			} else if (!type.isInstance(v)) {
				throw Script.error("%s is not a subtype of %s", Value.types.get(v.getClass()), Value.types.get(type));
			} else {
				value = v;
			}
		}
		public String findName() {
			return map_firstKey(vars, this, Object::equals);
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
		public String toString() {
			return ""+(type == Value.class ? "any" : Value.types.get(type)) +':'+value.toString();
		}
	}
}
