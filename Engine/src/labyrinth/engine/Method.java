package labyrinth.engine;
import tbm.util.StringStream;
import tbm.util.geom.Point;
import static labyrinth.engine.Method.VType.*;
import static tbm.util.statics.*;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Method {
	static final Map<String,Method> methods = new HashMap<String,Method>();
	public static Method get(String method) {
		if (method==null)
			return null;
		Method m = methods.get(method);
		if (m==null)
			throw Window.error("No method \"%s\"", method);
		return m;
	}
	public static void call(String method, Tile tile, Mob mob) {
		Method.get(method).call(tile, mob);
	}
	public static void add(String line) {
		Method toAdd = new Method(line);
		methods.put(toAdd.name, toAdd);
	}


	public final String name;
	private final Func.Operation[] operations;
	/**
	 * 
	 */
	public Method(String line) {
		StringStream l = new StringStream(line);
		l.whitespace();
		if (l.empty())
			;
		
		name = l.next(c -> char_word(c));
		if (name.isEmpty())
			Window.error("not a method");
		if (l.next_nw() != ':')
			Window.error("Method %s: not a method", name);
		l.whitespace();

		ArrayList<Func.Operation> ops = new ArrayList<>(10);
		while (!l.empty()) {
			if (l.peek()=='#')
				break;
			try {
				String op_name = l.next( c->char_word(c) );
				if (op_name.isEmpty())
					throw Window.error("Method %s: operation expected", name);
				Func f = Func.get(op_name);
				if (f==null)
					throw Window.error("Method %s: Unknown operation %s.", name, op_name);
				if (l.next_nw() != '(')
					Window.error("Method %s: '(' expected after \"%s\".", name, op_name);
				Var[] params = new Var[f.parameters.length];
				int i=0;
				while (i<params.length) {
					params[i] = f.parameters[i].parse(l); 
					i++;
					if (i < params.length  &&  l.next_nw() != ',')
						throw Window.error("Method %s: ',' expected after %i. argument.", name, i);
				}
				if (l.next_nw() != ')')
					throw Window.error("Method %s: ')' expected after %i. argument.", name, i);
				if (l.next_nw() != ';')
					throw Window.error("Method %s: ';' expected after a method (%s(", name, op_name);
				ops.add(f.instance(params));
			} catch (ArrayIndexOutOfBoundsException e) {
				throw Window.error("", "Method %s: unexpected end of line.", name);
			}
			l.whitespace();
			//TODO: hvis koordinater mangler vil metoden kjøres på feltet som startet funksjonen.
		}
		operations = ops.toArray(new Func.Operation[ops.size()]);
	}

	public void call(Tile tile, Mob mob) {
		for (Func.Operation op : operations)
			op.perform(tile, mob);
	}



	public static enum VType {
		VOID("void"), INT("int"), POINT("point"), CHAR("char"), STRING("string");
		private VType(String str)
			{}
		public Var parse(StringStream ss) {switch (this) {
			case CHAR:
				char open = ss.next_nw();
				char c1 = ss.next();
				if (open!='\'' || ss.next()!='\'')
					throw Window.error("not a char");
				return new Var.VChar(c1);
			case STRING:
				if (ss.next_nw() != '"')
					throw Window.error("not a string");
				String str = ss.next(c2 -> c2!='"');
				ss.next();//past the the '"'
				return new Var.VString(str);
			case INT:
				int num = ss._int();
				if (char_letter(ss.peek_nw()))
					throw Window.error("not an integer");
				return new Var.VInt(num);
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
				return new Var.VPoint(new Point(p));
			case VOID:
				throw new RuntimeException("cannot parse a VOID");
			default:
				throw new AssertionError("Unhandled type");
		}}
	}

	static abstract class Var {
		final String name;
		final VType type;
		protected Var(String n, VType t) {
			name=n;
			type=t;
		}
		//public VType basicType() {return type;}
		public int Int() {throw Window.error("\"%s\" is not an Integer", name);}
		public Point Point() {throw Window.error("\"%s\" is not a Point", name);}
		public char Char() {throw Window.error("\"%s\" is not a characther", name);}
		public String String() {throw Window.error("\"%s\" is not a string", name);}

		public static class VVoid extends Var {
			public VVoid() {
				super(null, VOID);
			}
		}
		public static class VChar extends Var {
			public char c;
			public VChar(char c) {
				super(null, CHAR);
				this.c=c;
			}
			@Override
			public char Char() {
				return c;
			}
		}
		public static class VString extends Var {
			public String str;
			public VString(String str) {
				super(null, STRING);
				this.str=str;
			}
			@Override
			public String String() {
				return str;
			}
		}
		

		public static class VInt extends Var {
			public int n;
			public VInt(int n) {
				super(null, INT);
				this.n=n;
			}
			@Override
			public int Int() {
				return n;
			}
		}

		public static class VPoint extends Var {
			public Point p;
			public VPoint(Point p) {
				super(null, POINT);
				this.p=p;
			}
			@Override
			public Point Point() {
				return p;
			}
		}
	}


	static class Func {
		public final String name;
		public final VType[] parameters;
		public final VType ret;
		private final Function<Var[], Operation> init;
		private Func(String name, VType[] parameters, VType ret, Function<Var[], Operation> init) {
			this.name = name;
			this.parameters = parameters;
			this.ret = ret;
			this.init = init;
		}
		public Operation instance(Var... arg) {
			return init.apply(arg);
		}
		interface Operation {
			Var perform(Tile t, Mob m);
		}


		public static Func get(String name) {
			return map.get(name);
		}
		protected static Map<String, Func> map = Collections.unmodifiableMap(map_init(
			array("set","trigger","call","move"),
			/**change the type and method of a tile*/
			new Func("set", array(POINT, CHAR), VOID, (params)->{
				Point p = params[0].Point();
				char symbol = params[1].Char();
				Tile target;
				if (p==null)
					target = null;
				else
					target = TileMap.get(p);
				Type type = Type.t(symbol);
				return (tile, mob) -> {
					if (target != null)
						tile = target;
					tile.setType(type);
					if (type.method)
						tile.method = Method.get(String.valueOf(symbol));
					else
						tile.method = null;
					return new Var.VVoid();
				};
			}),

			/**run the method of another tile*/
			new Func("trigger", array(POINT), VOID, (params) -> {
				Point pos = params[0].Point();
				return (Tile tile, Mob mob) -> {
					if (pos != null)
						tile = TileMap.get(pos);
					if (tile.mob() != null)
						mob = tile.mob();
					if (tile.method != null)
						tile.method.call(tile, mob);
					return new Var.VVoid();
				};
			}),

			/**run a method*/
			new Func("call", array(STRING), VOID, (params) -> {
				String method = params[0].String();
				return (Tile tile, Mob mob) -> {
					Method.call(method, tile, mob);
					return new Var.VVoid();
				};
			}),

			/**teleport*/
			new Func("move", array(POINT), VOID, (params) -> {
				Point pos = params[0].Point();
				Tile to = TileMap.get(pos); 
				return (Tile tile, Mob mob) -> {
					if (pos != null)
						tile = TileMap.get(pos);
					if (mob==null)
						throw Window.error("Method move: mob==null");
					//if the target is also a teleporter, you could end up teleporting infinitely.
					//using Mob.move() prevents that because it doesn't trigger tiles.
					mob.move(to);
					TileMap.panel.repaint();
					return new Var.VVoid();
				};
			})
		));
	}
}
