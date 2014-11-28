package labyrinth.engine.method;
import static labyrinth.engine.method.Value.*;
import java.util.LinkedList;
import labyrinth.engine.Mob;
import labyrinth.engine.Tile;
import labyrinth.engine.Window;

public class Script {
	public static Scope current = new Scope(null, "Initial");
	public static Scope root = current;
	static {
		root.define("true", Value.True);
		root.define("false", Value.False);
		root.define("null", Value.Void);
	}

	//TODO: use singleton
	static Mob mob = null;
	static Tile tile = null;
	static String name = null;
	//the result of the last operation performed in a function
	static Value last = Void;
	public static Value call(String name, Tile t, Mob m) {
		last = Void;
		Value v = current.value(name);
		if (!(v instanceof VFunc))
			Script.error("The variable %s is not a function.", name);
		mob = m;
		tile = t;
		return v.call(new LinkedList<Value>());
	}
	static Window.ErrorDialog error(String f, Object... a) {
		if (name == null)
			return Window.error(f, a);
		return Window.error("%s\n(Last variable: %s)", String.format(f, a), name);
	}


	public static void run(Iterable<Object> ops) {
		Value ret;
		for (Object o : ops) {
			if (o instanceof String)//get ref.
				//To avoid this if, parse_call, who use the standard behaviour, would have to override the default: case.
				ret = Script.current.get_variable(o.toString());
			else
				ret = Value.get(o);
			if (ret != Value.Void)
				Script.last = ret;
		}
	}
}
