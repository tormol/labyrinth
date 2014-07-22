package labyrinth.engine.method;
import static labyrinth.engine.method.VType.*;
import labyrinth.engine.Window;
import tbm.util.geom.Point;

public abstract class Value implements Operation {
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
	public Value call(Value[] param) {throw Script.error("not a function");}
	public void setRef(Value v) {throw Script.error("not a reference");}
	public Value getRef() {throw Script.error("not a reference");}
	@Override
	public Value perform() {
		return this;
	}



	public static Value Void = new Value(VOID)
		{};


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
		@Override
		public Point Point() {
			return p;
		}
		public Point value() {return p;}
	}

	public static Value get(Object o) {
		if (o instanceof String)
			return Script.current.get((String)o);
		if (o instanceof Value)
			return (Value)o;
		if (o instanceof Operation)
			return ((Operation)o).perform();
		throw Script.error("Value.get(): Unrecognized object.");
	}
}
