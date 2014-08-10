package labyrinth.engine.method;
import static labyrinth.engine.method.Value.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tbm.util.Parser.InvalidEscapeException;
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
	static String name = null;
	//the result of the last operation performed in a function
	static Value last = Void;
	public static Value call(String name, Tile t, Mob m) {
		last = Void;
		Value v = current.get(name);
		if (!(v instanceof VFunc))
			Window.error("The variable %s is not a function.", name);
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
		for (Object o : ops) {
			Value ret = Value.get(o);
			if (ret != Value.Void)
				Script.last = ret;
		}
	}

	public static enum Mode {
		STATIC, FUNCTION, PARAMETER; 
	}

	public static void parseFile(Parser p) throws IOException {
		Method.start();
		Script.current = Script.root = new Scope(Script.root);
		Script.run(Script.parse(p, Mode.STATIC));
	}

	private static List<Object> parse(Parser p, Mode m) throws IOException {
		ArrayList<Object> ops = new ArrayList<>();
		boolean stop = false;
		while (!stop && !(p.sw().empty() && m==Mode.STATIC)) switch (p.next()) {
			case'.':
				if (!isStartVar(p.peek()))
					throw p.error("Variable name required after a dot.");
				String name = p.next(c->isContVar(c));
				if (name.isEmpty())
					ops.add(Operation.getLast);
				//else if (m==Mode.PARAMETER)
				else
					ops.add(new Operation.Declare(name));
				break;
			case'(':
				if (ops.isEmpty())
					throw p.error("cannot start a body with a '('");
				Object toCall = ops.remove(ops.size()-1);
				//TODO: description
				//TODO: parameter validation
				//ops.add(f.instance(params));
				ops.add(new Operation.Call(toCall, parse(p, Mode.PARAMETER)));
				break;
			case')':
				if (m != Mode.PARAMETER)
					throw p.error("Unexpected closing parenthesis");
				stop = true;
				break;
			case';':
				if (m != Mode.FUNCTION)
					throw p.error("';' inside a call or file");
				stop = true;
				break;
			case':':
				ops.add(new Procedure(parse(p, Mode.FUNCTION), null));
				break;
			case'"':
				try {
					ops.add(new Value.VString(p.escapeString()));
				} catch (InvalidEscapeException e) {
					throw p.error(e.getMessage());
				} break;
			case'\'':
				try {
					ops.add(new Value.VChar(p.escapeChar()));
				} catch (InvalidEscapeException e) {
					throw p.error(e.getMessage());
				} break;
			case'0':case'1':case'2':case'3':case'4':case'5':case'6':case'7':case'8':case'9':
				p.back();
				ops.add(new Value.VInt(p._uint()));
				break;
			default: //name
				p.back();
				ops.add(p.next( c->isContVar(c) ));
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
