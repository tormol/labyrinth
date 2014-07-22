package labyrinth.engine.method;
import static tbm.util.statics.*;

import java.awt.Dimension;
import java.io.IOException;

import labyrinth.engine.TileMap;
import labyrinth.engine.Window;
import tbm.util.Parser;
import tbm.util.geom.Point;

public enum VType {
	VOID("void"), INT("int"), POINT("point"), CHAR("char"), STRING("string"), REF("ref"), FUNC("Func"), BOOL("bool"), STRUCT("struct");


	private VType(String str)
		{}


	public Value parse(Parser ss) throws IOException {
		ss.skip_whitespace();
		if (char_letter(ss.peek())) {
			//variable or 
			
		}
			
		switch (this) {
		  case CHAR:
			char open = ss.next();
			char c1 = ss.next();
			if (open!='\'' || ss.next()!='\'')
				throw Window.error("not a char");
			return new Value.VChar(c1);
		  case STRING:
			if (ss.next() != '"')
				throw Window.error("not a string");
			String str = ss.next(c2 -> c2!='"');
			ss.next();//past the the '"'
			return new Value.VString(str);
		  case INT:
			int num = ss._int();
			if (char_letter(ss.sw().peek()))
				throw Window.error("not an integer");
			return new Value.VInt(num);
		  case POINT:
			java.awt.Point p = new java.awt.Point();
			boolean parenthes = false;
			if (ss.peek() == '(') {//optional to lighten the syntax.
				ss.next();
				parenthes = true;
			}
			try {
				p.x = ss._int();
				if (ss.sw().next() != ',')
					throw Window.error("invalid point");
				p.y = ss._int();
			} catch (NumberFormatException e) {
				throw Window.error("not a number point");
			}
			if (parenthes  &&  ss.sw().next() != ')')
				throw Window.error("Point missing closeing parenthese");
			Dimension d = TileMap.dimesions();
			if (p.x<0 || p.y<0 || p.x>=d.width || p.y>=d.height)
				throw Window.error("(%d,%d) is outside the map", p.x, p.y);
			return new Value.VPoint(new Point(p));
		  case VOID:
			throw new RuntimeException("cannot parse a VOID");
		  default:
			throw new AssertionError("Unhandled type");
		}
	}

	public static VType find(char first) {switch (first) {
		case'1': case'2': case'3': case'4': case'5': case'6': case'7': case'8': case'9':
		case'0': return INT;
		case'"': return STRING;
		case'\'':return CHAR;
		case'(': return POINT;
		case':': return FUNC;
		case'.': return REF;
		default: return null;
	}}


	/*public Var var() {switch (this) {
	case CHAR:
		break;
	case INT:
		break;
	case POINT:
		break;
	case STRING:
		break;
	case VOID:
		break;
	default:
		break;
	
	}}*/
}