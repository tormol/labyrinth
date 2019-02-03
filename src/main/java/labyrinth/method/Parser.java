package labyrinth.method;
import static labyrinth.method.Operation.GetLast;
import static no.torbmol.util.statics.char_anyof;
import static no.torbmol.util.statics.char_num;
import static no.torbmol.util.statics.char_whitespace;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import labyrinth.method.Operation.Call;
import labyrinth.method.Operation.Declare;
import labyrinth.method.Operation.GetRef;
import labyrinth.method.Operation.UnDeclare;
import labyrinth.method.Value.VChar;
import labyrinth.method.Value.VInt;
import labyrinth.method.Value.VString;
import no.torbmol.util.ParseNum;

public class Parser extends no.torbmol.util.Parser {
	public Parser(File file) throws FileNotFoundException {
		super(file,
			  no.torbmol.util.Parser.Source.NEWLINE_IS_WHITESPACE,
			  no.torbmol.util.Parser.Source.HASH_STARTS_COMMENT);
	}
	public Parser(no.torbmol.util.Parser p) {
		super(p.getSource());
		this.setPos(p);
	}

	@Override
	public Parser clone() {
		return new Parser(this);
	}




	static Deque<Object> parse_static(Parser p, Script scr) throws EOFException, IOException, ParseException {
		int c = p.sw().inext();
		if (c == '(')
			//might be used for program arguments
			throw p.error("A file cannot start with a '('.");
		ArrayDeque<Object> ops = new ArrayDeque<>();
		while (c != ';'  &&  c != -1) {
			statement((char)c, p, ops, scr);
			c = p.sw().inext();
		}
		return ops;
	}

	private static Procedure parse_method(Parser p, Script scr) throws IOException, EOFException, ParseException {
		Parser start = p.clone();
		scr.current = new Scope(scr.current, "parse_method()");
		List<String> param_names = null;
		if (p.sw().peek() == '(') {
			param_names = parse_param(p);
			// make the variables known
			for (String name : param_names) {
				scr.current.declare(name, false, null);
			}
		}
		ArrayDeque<Object> ops = new ArrayDeque<>();
		char c;
		while ((c = p.sw().next()) != ';')
			statement(c, p, ops, scr);
		scr.current = scr.current.parent;
		String desc = String.format("start %d:%d, end %d:%d", start.getLine(), start.getCol(), p.getLine(), p.getCol());
		return new Procedure(ops, param_names, desc);
	}

	private static Deque<Object> parse_call(Parser p, Script scr) throws EOFException, IOException, ParseException {
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
				if (scr.current.get_variable(name) == null)
					p.error("%s is not declared.", name);
				params.add(new GetRef(name));
			case')': break;
			default: statement(c, p, params, scr);
		}
		return params;
	}

	private static List<String> parse_param(Parser p) throws IOException, ParseException {
		p.skip();//'('
		char c;
		List<String> param_names = new ArrayList<>();
		while ((c = p.sw().peek()) != ')') {
			if (!isStartVar(c)) {
				throw p.error("parameter list can only contain variable names");
			}
			String name = p.next(ch->isContVar(ch));
			param_names.add(name);
		}
		p.skip();//')'
		return param_names;
	}

	static void statement(char c, Parser p, Deque<Object> ops, Script scr) throws EOFException, IOException, ParseException {
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
			if (!scr.current.defined_here(name)) {//if another declared outside this scope has the same name shadow it.
				scr.current.declare(name, _final, type);
				ops.add(new Declare(name, _final, type));
				break;
			} else if (_final)
				throw p.error("Cannot re-declare a variable as final. (%s)", name);
			else if (scr.current.get_variable(name).isFinal())
				throw p.error("Cannot remove the final variable %s", name);
			scr.current.remove(name);
			ops.add(new UnDeclare(name));
			break;
		  case'(':
			Parser start = p.clone();
			if (ops.isEmpty())
				throw p.error("Cannot start with a parenthesis.");
			Object toCall = ops.removeLast();
			Deque<Object> params = parse_call(p, scr);
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
			ops.add(parse_method(p, scr));
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
			ops.add(VInt.v(parseInt(p)));
			break;
		  case'-'://negative int if directly followed by a number
			if (char_num((char)p.ipeek())) {
				p.back();
				ops.add(VInt.v(parseInt(p)));
				break;
			}	
		  default: //name
			p.back();
			String var = p.next( ch->isContVar(ch) );
			if (scr.current.get_variable(var) == null)
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
	private static int parseInt(Parser p) throws EOFException, IOException, ParseException {
		int num;
		try {
			int flags = ParseNum.SKIP_UNDERSCORE | ParseNum.ANY_FLAGS;
			num = (int)new ParseNum(flags, p).bits(Integer.SIZE);
			// called _uint(negative, other_systems=true, spaces="_")
			// called parseNum.unsigned_int() and then this.back()
			// called parse_unsigned(ch, Integer.SIZE, negative, other_systems, spaces)
			// called parse(ch, ch.fetch(), bits, false, negative, other_systems, spaces);
			p.back();
		} catch (NumberFormatException e) {
			throw p.error(e.getMessage());
		} if (isContVar((char)p.ipeek()))
			throw p.error("Invalid number.");
		return num;
	}
}
