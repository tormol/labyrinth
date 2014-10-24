package labyrinth.engine.method;
import java.util.List;
import java.util.Map;
import tbm.util.geom.Point;
import static tbm.util.statics.*;

public interface Value {
	/**for support structures etc. f.ex Point has x and y, String.length*/
	default Value getMember(String name) {throw Script.error("this type have no members");}
	default void setMember(String name, Value v) {throw Script.error("this type have no members");}
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
	public static class VString implements Imutable {
		public final String str;
		public VString(String str) {
			this.str=str;
		}@Override
		public String String() {
			return str;
		}@Override
		public boolean eq(Value v) {
			return str.equals(v.String());
		}@Override
		public Value getMember(String member) {switch (member) {
			case"length": return new VInt(str.length());
			default: throw Script.error("Strings have no member \"%s\"", member);
		}}
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

	public static class VPoint extends Point implements Imutable {
		public VPoint(Point p) {
			//TODO: make all values use static constructor.
			super(p.x, p.y);
		}
		public VPoint(int x, int y) {
			super(x,y);
		}@Override
		public Point Point() {
			return this;
		}@Override
		public Value getMember(String m) {switch (m) {
			case"x":	return new Value.VInt(x);
			case"y":	return new Value.VInt(y);
			default:	throw Script.error("Points have no member \"%s\"", m);
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

	static interface Imutable extends Value {
		@Override Value getMember(String member);
		@Override default void setMember(String name, Value v) {
			getMember(name);//throw if it doesn't even exist.
			throw Script.error("%ss are immutable.", map_firstKey(types, this.getClass()));
		}	//             plural s, eg strings not string
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

	public static Map<String, Class<? extends Value>> types = map_init(
			"void,boolean,char,string,integer,point,reference".split(","),
			array(VVoid.class, VBool.class, VChar.class, VString.class, VInt.class, VPoint.class, VRef.class)
			);
}
