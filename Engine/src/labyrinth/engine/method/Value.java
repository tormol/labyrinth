package labyrinth.engine.method;
import java.util.List;
import tbm.util.geom.Point;

public interface Value {
	/** a point has members x and y, to support structs, etc*/
	default Value member(String name) {throw Script.error("this type have no members");}
	default int Int() {throw Script.error("not an Integer");}
	default Point Point() {throw Script.error("not a Point");}
	default char Char() {throw Script.error("not a characther");}
	default String String() {throw Script.error("not a string");}
	default boolean Bool() {throw Script.error("not a boolean");}
	default Value call(List<Value> param) {throw Script.error("not a function");}
	default void setRef(Value v) {throw Script.error("not a reference");}
	default Value getRef() {throw Script.error("not a reference");}
	default boolean equals(Value v) {
		//if (!v.class.getName().equals(this.class.getName()))
			return false;
	}
	boolean eq(Value v);


	public static class VVoid implements Value {@Override
		public boolean eq(Value v) {
			return v==Void;
		}
	}
	public static VVoid Void = new VVoid();

	public static class VBool implements Value {
		public final boolean bool;
		private VBool(boolean bool) {
			this.bool = bool;
		}@Override
		public boolean Bool() {
			return bool;
		}@Override
		public boolean eq(Value v) {
			return bool==v.Bool();
		}
		public static VBool v(boolean b) {
			return b?True:False;
		}
	}
	public static VBool True  = new VBool(true );
	public static VBool False = new VBool(false);


	public static class VChar implements Value {
		public final char c;
		public VChar(char c) {
			this.c=c;
		}@Override
		public char Char() {
			return c;
		}@Override
		public boolean eq(Value v) {
			return c==v.Char();
		}
	}
	public static class VString implements Value {
		public final String str;
		public VString(String str) {
			this.str=str;
		}@Override
		public String String() {
			return str;
		}@Override
		public boolean eq(Value v) {
			return str.equals(v.String());
		}
	}
	
	public static class VInt implements Value {
		public final int n;
		public VInt(int n) {
			this.n=n;
		}@Override
		public int Int() {
			return n;
		}@Override
		public boolean eq(Value v) {
			return n==v.Int();
		}
	}

	public static class VPoint extends Point implements Value {
		public VPoint(Point p) {
			super(p);
		}
		public VPoint(int x, int y) {
			super(x,y);
		}@Override
		public Point Point() {
			return this;
		}@Override
		public Value member(String m) {switch (m) {
			case"x":	return new Value.VInt(x);
			case"y":	return new Value.VInt(y);
			default:	throw Script.error("Points has no member %s", m);
		}}@Override
		public boolean eq(Value v) {
			return super.equals(v.Point());
		}
		private static final long serialVersionUID = 1L;
	}

	public static interface VFunc extends Value {
		@Override
		Value call(List<Value> param);
		@Override
		default boolean eq(Value v) {
			return v==this;
		}
	}

	public static interface VRef extends Value {
		@Override void setRef(Value v);
		@Override Value getRef();
		@Override default boolean eq(Value v) {
			return this.getRef() == v.getRef();
		}
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

	public static boolean equalType(Value a, Value b) {
		return true;
	}
}
