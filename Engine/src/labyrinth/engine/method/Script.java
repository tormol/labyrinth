package labyrinth.engine.method;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import labyrinth.engine.Mob;
import labyrinth.engine.Parser;
import labyrinth.engine.Tile;
import labyrinth.engine.Window;

public class Script {
	public static Scope current = new Scope(null);
	public static Scope root = current;

	//TODO: use singleton
	static Mob mob = null;
	static Tile tile = null;
	//the result of the last operation performed in a function
	static Value last = Value.Void;
	public static Value call(String name, Tile t, Mob m) {
		last = Value.Void;
		Value v = current.get(name);
		if (v.type != VType.FUNC)
			Window.error("The variable %s is not a function.", name);
		mob = m;
		tile = t;
		return v.call(new Value[0]);
	}
	static Window.ErrorDialog error(String f, Object... a) {
		return Window.error(f, a);
	}


	public static enum Mode {
		STATIC, FUNCTION, PARAMETER; 
	}

	private static List<Object> parse(Parser p, Mode m) throws IOException {
		ArrayList<Object> ops = new ArrayList<>(10);
		boolean param_mode = false;
		while (p.sw().peek() != ';') {
			if (p.peek() == '.') {
				p.next();
				if (!isStartVar(p.peek()))
					throw p.error("Variable name required after a dot.");
				ops.add(new Operation.Declare(p.next(c->!isContVar(c))));
			} else if (p.peek() == '(') {
				Object toCall = ops.remove(ops.size()-1);
				
				ops.add(new Operation.Call(toCall, parse(p, Mode.PARAMETER)));
			}
			String name = p.next( c->"#.(:; \t\n".indexOf(c)==-1 );
			if (name.isEmpty())
				throw p.error("operation expected");
			p.skip_whitespace();
			if (p.peek()=='(') {
				if (f==null)
					throw p.error("Unknown operation %s.", name);
				if (p.sw().next() != '(')
					p.error("'(' expected after \"%s\".", name);
				Value[] params = new Value[f.parameters.length];
				for (int i=0; i<params.length; i++)
					if (p.peek()==')')
						throw p.error("method %s requres %d paramaters, but got %d", name, params.length, i+1);
					else
						params[i] = f.parameters[i].parse(p); 
				if (p.sw().next() != ')')
					throw p.error("method %s requres %d paramaters, too many.", name, params.length);
				ops.add(f.instance(params));
			}
		}
		return ops;
	}

	private static boolean isContVar(char c) {
		return "#.():; \t\n".indexOf(c)==-1;
	}
	private static boolean isStartVar(char c) {
		return isContVar(c) && "\"1234567890".indexOf(c)==-1;
	}
}
