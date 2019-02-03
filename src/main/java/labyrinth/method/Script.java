package labyrinth.method;
import static labyrinth.method.Value.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import no.torbmol.util.Parser.ParseException;
import labyrinth.Mob;
import labyrinth.Tile;
import labyrinth.Window;

public class Script {
	public static Script scr;
	public Scope current;
	public final Scope root;
	public Mob mob = null;
	public Tile tile = null;
	/**name of the last variable accessed*/
	public String name = null;
	//the result of the last operation performed in a function
	public Value last = Void;

	//no idea if this is safe
	public Script(Parser p, String name, Map<String, Value>... libraries) throws EOFException, IOException, ParseException {
		if (Script.scr != null)
			throw new RuntimeException("Script is a singleton");
		Script.scr = this;
		Scope parent = new Scope(null, "included methods");
		for (Map<String, Value> lib : libraries)
			for (Map.Entry<String, Value> var : lib.entrySet())
				if (var.getValue() == null)
					throw new RuntimeException("Value in library is null");
				else
					parent.define(var.getKey(), var.getValue());
		current = new Scope(parent, "parse_root");
		Deque<Object> ops = Parser.parse_static(p, this);
		current = root = new Scope(parent, "script"+name+" root");//remove declared variables
		//if I keep the Scope and run everything except declares and undeclares, removed variables would give errors
		Script.run(ops);
	}

	
	public static Value call(String name, Tile t, Mob m) {
		scr.last = Void;
		Value v = scr.current.value(name);
		if (!(v instanceof VFunc))
			throw error("The variable %s is not a function.", scr.name);
		scr.mob = m;
		scr.tile = t;
		return v.call(new LinkedList<Value>());
	}


	public static Window.ErrorDialog error(String f, Object... a) {
		StringBuilder msg = new StringBuilder( String.format(f, a) );
		if (scr.name != null)
			msg.append("\n(Last variable: ").append(scr.name).append(')');
		Scope scope = scr.current;
		do {
			msg.append("\n\tIn ").append(scope.description);
			scope = scope.parent;
		} while (scope != null  &&  scope.description != scr.root.description);
		return Window.error(msg.toString());
	}


	static void run(Iterable<Object> ops) {
		Value ret;
		for (Object o : ops) {
			if (o instanceof String)//get ref.
				//To avoid this if, parse_call, who use the standard behaviour, would have to override the default: case.
				ret = scr.current.get_variable(o.toString());
			else
				ret = Value.get(o);
			if (ret != Value.Void)
				scr.last = ret;
		}
	}
}
