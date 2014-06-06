package labyrinth.engine.method;
import static tbm.util.statics.*;
import java.awt.Dimension;
import labyrinth.engine.TileMap;
import labyrinth.engine.Window;
import tbm.util.StringStream;
import tbm.util.geom.Point;

public enum VType {
	VOID("void"), INT("int"), POINT("point"), CHAR("char"), STRING("string");


	private VType(String str)
		{}


	public Value parse(StringStream ss) {switch (this) {
		case CHAR:
			char open = ss.next_nw();
			char c1 = ss.next();
			if (open!='\'' || ss.next()!='\'')
				throw Window.error("not a char");
			return new Value.VChar(c1);
		case STRING:
			if (ss.next_nw() != '"')
				throw Window.error("not a string");
			String str = ss.next(c2 -> c2!='"');
			ss.next();//past the the '"'
			return new Value.VString(str);
		case INT:
			int num = ss._int();
			if (char_letter(ss.peek_nw()))
				throw Window.error("not an integer");
			return new Value.VInt(num);
		case POINT:
			ss.whitespace();
			java.awt.Point p = new java.awt.Point();
			boolean parenthes = false;
			if (ss.peek() == '(') {//optional to lighten the syntax.
				ss.next();
				parenthes = true;
			}
			try {
				p.x = ss._int();
				if (ss.next_nw() != ',')
					throw Window.error("invalid point");
				p.y = ss._int();
			} catch (NumberFormatException e) {
				throw Window.error("not a number point");
			}
			if (parenthes  &&  ss.next_nw() != ')')
				throw Window.error("Poit missing closeing parenthese");
			Dimension d = TileMap.dimesions();
			if (p.x<0 || p.y<0 || p.x>=d.width || p.y>=d.height)
				throw Window.error("(%d,%d) is outside the map", p.x, p.y);
			return new Value.VPoint(new Point(p));
		case VOID:
			throw new RuntimeException("cannot parse a VOID");
		default:
			throw new AssertionError("Unhandled type");
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