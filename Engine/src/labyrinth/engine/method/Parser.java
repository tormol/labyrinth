package labyrinth.engine.method;
import static labyrinth.engine.method.Operation.GetLast;
import static tbm.util.statics.char_anyof;
import static tbm.util.statics.char_num;
import static tbm.util.statics.char_whitespace;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import labyrinth.engine.method.Operation.Call;
import labyrinth.engine.method.Operation.Declare;
import labyrinth.engine.method.Operation.GetRef;
import labyrinth.engine.method.Operation.UnDeclare;
import labyrinth.engine.method.Value.VChar;
import labyrinth.engine.method.Value.VInt;
import labyrinth.engine.method.Value.VString;

public class Parser extends tbm.util.Parser {
	public Parser(File file) throws FileNotFoundException {
		super(file, true);
	}
	public Parser(tbm.util.Parser p) {
		super(p, true, p.getLine(), p.getCol());
	}

	@Override/**@super Additionally skips comments.*/
	public Parser skip_whitespace(boolean newline) throws IOException {
		super.skip_whitespace(newline);
		if (ipeek() == '#')
			if (!newline)
				setPos(getLine(), length(getLine()));
			else
				do {
					line();
					super.skip_whitespace(newline);
				} while (ipeek() == '#');
		return this;
	}

	@Override
	public Parser clone() {
		return new Parser(this);
	}




	public static Scope parse_static(Parser p) throws EOFException, IOException, ParseException {
		Method.start();
		Script.current = Script.root = new Scope(Script.root, "parse_static()");
		int c = p.sw().inext();
		if (c == '(')
			//might be used for program arguments
			throw p.error("A file cannot start with a '('.");
		ArrayDeque<Object> ops = new ArrayDeque<>();
		while (c != ';'  &&  c != Parser.END) {
			statement((char)c, p, ops);
			c = p.sw().inext();
		}
		Script.current = Script.root = new Scope(Script.root.parent, "script root");//remove declared variables
		//if I keep the Scope and run everything except declares and undeclares, removed variables would give errors
		Script.run(ops);
		return Script.current;
	}

	private static Procedure parse_method(Parser p) throws IOException, EOFException, ParseException {
		Parser start = p.clone();
		Script.current = new Scope(Script.current, "parse_method()");
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
						p.error("Expected variable name");
				String name = p.next(ch->isContVar(ch));
				if (Script.current.get_variable(name) == null)
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

	static void statement(char c, Parser p, Deque<Object> ops) throws EOFException, IOException, ParseException {
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
			Class<? extends Value> type = null;
			if (p.ipeek() == '.') {//Has type
				String typeName = p.next(ch->isContVar(ch));
				if (typeName.isEmpty())
					throw p.error("Empty type in declaration of variable " + name);
				type = Value.types.get(typeName);
				if (type == null)
					throw p.error("Unknown type %s for variable %s", typeName, name);
			}
			if (!Script.current.defined_here(name)) {//if another declared outside this scope has the same name shadow it.
				Script.current.declare(name, _final, type);
				ops.add(new Declare(name, _final, type));
				break;
			} else if (_final)
				throw p.error("Cannot re-declare a variable as final. (%s)", name);
			else if (Script.current.get_variable(name).isFinal())
				throw p.error("Cannot remove the final variable %s", name);
			Script.current.remove(name);
			ops.add(new UnDeclare(name));
			break;
		  case'(':
			Parser start = p.clone();
			if (ops.isEmpty())
				throw p.error("Cannot start with a parenthesis.");
			Object toCall = ops.removeLast();
			Deque<Object> params = parse_call(p);
			String desc = toCall instanceof String ? (String)toCall : "<unknown>";//name was already used
			desc = String.format("%s() from start %d:%d, end %d:%d", desc,
			                     start.getLine(), start.getCol(), p.getLine(), p.getCol());
			ops.add(new Call(toCall, params, desc));
			
			//ops.add(new Call(toCall, parse_call(p),
			//                 String.format("%s() from start %d:%d, end %d:%d",
			//                               toCall instanceof String ? (String)toCall : "<unknown>",
			//                               start.getLine(), start.getCol(), p.getLine(), p.getCol())));
			break;
		  case')':
			throw p.error("Unexpected closing parenthesis");
		  case';'://end method
			throw p.error("Unexpected semicolon");
		  case':'://method
			ops.add(parse_method(p));
			break;
		  case'"'://string
			ops.add(VString.v(p.escapeString('"')));
			break;
		  case'\''://char
			ops.add(VChar.v(p.escapeChar(false)));
			break;
		  case'1':case'2':case'3':case'4':case'5':
		  case'6':case'7':case'8':case'9':case'0':
			p.back();
			ops.add(VInt.v(parseInt(p, false)));
			break;
		  case'-'://negative int if directly followed by a number
			if (char_num((char)p.ipeek())) {
				ops.add(VInt.v(parseInt(p, true)));
				break;
			}	
		  default: //name
			p.back();
			String var = p.next( ch->isContVar(ch) );
			if (Script.current.get_variable(var) == null)
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
			num = p._uint(negative, true, "_");
		} catch (NumberFormatException e) {
			throw p.error(e.getMessage());
		} if (isContVar((char)p.ipeek()))
			throw p.error("Invalid number.");
		return num;
	}
}
