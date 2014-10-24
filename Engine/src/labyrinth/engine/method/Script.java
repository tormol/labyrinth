package labyrinth.engine.method;
import static labyrinth.engine.method.Value.*;
import static tbm.util.statics.*;
import static labyrinth.engine.method.Operation.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import tbm.util.Parser.ParseException;
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
		for (Object o : ops) {
			Value ret = Value.get(o);
			if (ret != Value.Void)
				Script.last = ret;
		}
	}


	public static Scope parse_static(Parser p) throws EOFException, IOException, ParseException {
		Method.start();
		Script.current = Script.root = new Scope(Script.root);
		int c = p.sw().inext();
		if (c == '(')
			//might be used for program arguments
			throw p.error("A file cannot start with a '('.");
		ArrayDeque<Object> ops = new ArrayDeque<>();
		while (c != ';'  &&  c != Parser.END) {
			statement((char)c, p, ops);
			c = p.sw().inext();
		}
		Script.current = Script.root = new Scope(Script.root);//remove declared variables
		//if I keep the Scope and run everything except declares and undeclares, removed variables wold give errors
		run(ops);
		return current;
	}

	private static Procedure parse_method(Parser p) throws IOException, EOFException, ParseException {
		Parser start = p.clone();
		Script.current = new Scope(Script.current);
		if (p.sw().peek() == '(') {
			parse_param(p);
		}
		ArrayDeque<Object> ops = new ArrayDeque<>();
		char c;
		while ((c = p.sw().next()) != ';')
			statement(c, p, ops);
		Script.current = Script.current.parent;
		String desc = String.format("start %d:%d, end %d:%d", start.getLine(), start.getCol(), p.getLine(), p.getCol());
		return new Procedure(ops, desc);
	}

	private static Deque<Object> parse_call(Parser p) throws EOFException, IOException, ParseException {
		ArrayDeque<Object> params = new ArrayDeque<>();
		char c='\0';
		while (c != ')') switch (c = p.sw().next()) {
			case'.':
				c = p.peek();
				if (!isStartVar(c))
					if (char_whitespace(c))
						params.add(GetLast);
					else
						p.error("Expected ariable name");
				String name = p.next(ch->isContVar(ch));
				if (!Script.current.has(name))
					p.error("%s is not declared.", name);
				params.add(new GetRef(name));
			case')': break;
			default: statement(c, p, params);
		}
		return params;
	}

	private static List<Object> parse_param(Parser p) {
		p.error("Parameters are not supported yet");
		return null;
	}

	private static void statement(char c, Parser p, Deque<Object> ops) throws EOFException, IOException, ParseException {
		switch (c) {
		  case'.':
			boolean _final = false; 
			if (p.peek() == '.') {
				_final = true;
				p.skip();
			}
			if (!isStartVar(p.peek()))
				throw p.error("Variable name required after declaration.");
			String name = p.next(ch->isContVar(ch));
			Class<Value> type = null;
			if (p.ipeek() == '.') {//Has type
				String typename = p.next(ch->isContVar(ch));
				if (typename.isEmpty())
					throw p.error("Empty type in declaration of variable %s", name);
			}
			if (!current.has(name)) {
				current.declare(name, _final);
				ops.add(new Declare(name, _final));
				break;
			} else if (_final)
				throw p.error("Cannot re-declare a variable as final. (%s)", name);
			else if (current.search(name).isFinal())
				throw p.error("Cannot remove the final variable %s", name);
			current.remove(name);
			ops.add(new UnDeclare(name));
			break;
		  case'(':
			Parser start = p.clone();
			if (ops.isEmpty())
				p.error("Cannot start with a parenthesis.");
			Object toCall = ops.removeLast();
			Deque<Object> params = parse_call(p);
			String desc = String.format("start %d:%d, end %d:%d", start.getLine(), start.getCol(), p.getLine(), p.getCol());
			ops.add(new Call(toCall, params));
			break;
		  case')':
			throw p.error("Unexpected closing parenthesis");
		  case';'://end method
			throw p.error("Unexpected semicolon");
		  case':'://method
			ops.add(parse_method(p));
			break;
		  case'"'://string
			ops.add(new VString(p.escapeString('"')));
			break;
		  case'\''://char
			ops.add(new VChar(p.escapeChar(false)));
			break;
		  case'1':case'2':case'3':case'4':case'5':
		  case'6':case'7':case'8':case'9':case'0':
			p.back();
			ops.add(new VInt(parseInt(p, false)));
			break;
		  case'-'://negative int if directly followed by a number
			if (char_num((char)p.ipeek())) {
				ops.add(new VInt(parseInt(p, true)));
				break;
			}	
		  default: //name
			p.back();
			String var = p.next( ch->isContVar(ch) );
			if (Script.current.has(var))
				throw p.error("%s is not defined", var);
			ops.add(var);
		}
	}


	private static boolean isContVar(char c) {
		return !char_anyof(c, '#','.','(',')',':',';',' ','\t','\n');
	}
	private static boolean isStartVar(char c) {
		return isContVar(c) && c!='"' && c!='\'' && !char_num(c);
	}
	private static int parseInt(Parser p, boolean negative) throws EOFException, IOException, ParseException {
		int num;
		try {
			num = p._uint(negative, true);
		} catch (NumberFormatException e) {
			throw p.error(e.getMessage());
		} if (isContVar((char)p.ipeek()))
			throw p.error("Invalid number.");
		return num;
	}
}
