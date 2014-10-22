package labyrinth.engine.method;
import static labyrinth.engine.method.Value.*;
import static tbm.util.statics.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import tbm.util.Parser.ParseException;
import labyrinth.engine.Mob;
import labyrinth.engine.Parser;
import labyrinth.engine.Tile;
import labyrinth.engine.Window;
import labyrinth.engine.method.Scope.Variable;

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
		if (p.sw().ipeek() == '(')
			throw p.error("A file cannot start with a '('.");
		int c;
		while (true) switch (c = p.sw().inext()) {
		  case';':
		  case Parser.END:
			return current;
		  default:
			Object st = statement((char)c, p);
			if (!(st instanceof Operation.Declare)) {//is already declared
				Value ret = Value.get(st);
				if (ret != Value.Void)
					Script.last = ret;
			}
		}
	}

	private static Procedure parse_method(Parser p) throws IOException, EOFException, ParseException {
		Parser start = p.clone();
		Script.current = new Scope(Script.current);
		if (p.sw().peek() == '(') {
			parse_param(p);
		}
		ArrayList<Object> ops = new ArrayList<>();
		char c;
		while (true) switch(c = p.sw().next()){
		  case';':
			Script.current = Script.current.parent;
			String desc = String.format("start %i:%i, end %i:%i", start.getLine(), start.getCol(), p.getLine(), p.getCol());
			return new Procedure(ops, desc);
		  default:
			ops.add(statement(c, p));
		}
	}

	private static List<Object> parse_call(Parser p) {
		return null;
	}

	private static List<Object> parse_param(Parser p) {
		p.error("Parameters are not supported yet");
		return null;
	}

	private static Object statement(char c, Parser p) throws EOFException, IOException, ParseException {switch (c) {
	  case'.':
		boolean _final = false; 
		if (p.peek() == '.') {
			_final = true;
			p.skip();
		}
		if (!isStartVar(p.peek()))
			throw p.error("Variable name required after declaration.");
		String name = p.next(ch->isContVar(ch));
		Class type = null;
		if (p.ipeek() == '.') {//Has type
			String typename = p.next(ch->isContVar(ch));
			if (typename.isEmpty())
				throw p.error("Empty type in declaration of variable %s", name);
			
		}
		if (!current.has(name)) {
			current.declare(name, _final);
			return new Operation.Declare(name, _final);
		} else if (_final)
			throw p.error("Cannot re-declare a variable as final. (%s)", name);
		else if (current.search(name).isFinal())
			throw p.error("Cannot remove the final variable %s", name);
		current.remove(name);
		return new Operation.UnDeclare(name);
	  case'(':
		return parse_call(p);
	  case')':
		throw p.error("Unexpected closing parenthesis");
	  case';'://end method
		throw p.error("Unexpected semicolon");
	  case':'://method
		return parse_method(p);
	  case'"'://string
		return new Value.VString(p.escapeString('"'));
	  case'\''://char
		return new VChar(p.escapeChar(false));
	  case'1':case'2':case'3':case'4':case'5':
	  case'6':case'7':case'8':case'9':case'0':
		p.back();
		return new VInt(parseInt(p, false));
	  case'-'://negative int if directly followed by a number
		if (char_num((char)p.ipeek()))
			return new VInt(parseInt(p, true));
	  default: //name
		p.back();
		String var = p.next( ch->isContVar(ch) );
		if (Script.current.search(var) == null)
			throw p.error("%s is not defined", var);
		return var;
	}}


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
