package labyrinth.engine.method;
import static labyrinth.engine.method.Value.VType.*;
import java.util.List;
import labyrinth.engine.Window;
import tbm.util.geom.Point;

public abstract class Value {
	public enum VType {
		VOID("void"), INT("int"), POINT("point"), CHAR("char"), STRING("string"), REF("ref"), FUNC("Func"), BOOL("bool"), STRUCT("struct");
		private VType(String str)
			{}
	}

	public final VType type;
	protected Value(VType t) {
		type=t;
	}
	public Object value() {return null;}
	//public VType basicType() {return type;}
	/** a point has members x and y, to support structs, etc*/
	public Value member(String name) {throw Window.error("%s have no members", type);}
	public int Int() {throw Script.error("not an Integer");}
	public Point Point() {throw Script.error("not a Point");}
	public char Char() {throw Script.error("not a characther");}
	public String String() {throw Script.error("not a string");}
	public boolean Bool() {throw Script.error("not a boolean");}
	public Value call(List<Value> param) {throw Script.error("not a function");}
	public void setRef(Value v) {throw Script.error("not a reference");}
	public Value getRef() {throw Script.error("not a reference");}



	public static Value Void = new Value(VOID)
		{};
	public static Value True = new Value(BOOL) {@Override public boolean Bool() {
		return true;
	}};
	public static Value False = new Value(BOOL) {@Override public boolean Bool() {
		return false;
	}};


	public static class VChar extends Value {
		public final char c;
		public VChar(char c) {
			super(CHAR);
			this.c=c;
		}
		@Override
		public char Char() {
			return c;
		}
	}
	public static class VString extends Value {
		public final String str;
		public VString(String str) {
			super(STRING);
			this.str=str;
		}
		@Override
		public String String() {
			return str;
		}
	}
	

	public static class VInt extends Value {
		public final int n;
		public VInt(int n) {
			super(INT);
			this.n=n;
		}
		@Override
		public int Int() {
			return n;
		}
	}

	public static class VPoint extends Value {
		public Point p;
		public VPoint(Point p) {
			super(POINT);
			this.p=p;
		}
		public VPoint(int x, int y) {
			this(new Point(x, y));
		}
		@Override
		public Point Point() {
			return p;
		}
		@Override
		public Value member(String m) {switch (m) {
			case "x":
				return new Value.VInt(p.x);
			case "y":
				return new Value.VInt(p.y);
			default:
				throw Script.error("Points has no member %s", m);
		}}
		public Point value() {return p;}
	}


	public static Value get(Object o) {
		if (o instanceof String) {
			Script.name = (String)o;
			return Script.current.get((String)o);
		} if (o instanceof Value)
			return (Value)o;
		if (o instanceof Operation)
			return ((Operation)o).perform();
		throw Script.error("Value.get(): Unrecognized object.");
	}


	//test code to see if a subinterface can remove a default implementation
	//it can, so VType can be removed
	static interface a {
		public default int Int() {
			throw new RuntimeException();
		};
		public default char Char() {
			throw new RuntimeException();
		}
	}
	static interface Integer extends a {
		public int Int();
		static a create(Integer v) {return v;}
	}
	static class b implements Integer {
		b(){}
		@Override
		public int Int() {
			return 5;
		}
	}
	static{
		Integer.create(()->5);
		new Integer(){@Override	public int Int() {
			return 0;
		}};
	}
}
