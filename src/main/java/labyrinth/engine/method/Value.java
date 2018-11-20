package labyrinth.engine.method;
import java.util.List;
import java.util.Map;

import no.torbmol.util.geom.Point;
import static no.torbmol.util.statics.*;

/**Variable types base class*/
public interface Value {
	/**for maps, not directly exposed to script code*/
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
	default boolean sameType(Value v) {
		return v.getClass().getName().equals(this.getClass().getName());
	}
	boolean eq(Value v);

	//Must be a class to use instanceof, static.class and .getClass to get name from the types map.
	public static class VVoid implements Value {
		private VVoid() {;}
		@Override
		public boolean eq(Value v) {
			//return false would make it impossible to check if a variable is void in a script.
			return v==Void;
		}
		public String toString() {
			return "void";
		}
	}
	public static final VVoid Void = new VVoid();

	//for Method definiton paramater lists
	/**The previous argument is optional and can be repeated.
	 *Must be the last element in the array.*/
	static final Class<Value> vararg = null;

	/**Boolean, private constructor; use VBool.v()*/
	public static class VBool implements Value {
		public final boolean bool;
		private VBool(boolean bool) {
			this.bool = bool;
		}@Override
		public boolean Bool() {
			return bool;
		}@Override
		public boolean eq(Value v) {
			//return v==this is a premature optimization that would bite in the ass if another type ovverride .Bool()
			return bool==v.Bool();
		}
		public String toString() {
			return bool ? "true" : "false";
		}
		public static VBool v(boolean b) {
			return b?True:False;
		}
	}
	public static final VBool True  = new VBool(true );
	public static final VBool False = new VBool(false);


	public static class VChar implements Value {
		public final char c;
		private VChar(char c) {
			this.c=c;
		}@Override
		public char Char() {
			return c;
		}@Override
		public boolean eq(Value v) {
			return c==v.Char();
		}
		public String toString() {
			return Character.toString(c);
		}
		public static VChar v(char ch) {
			if (ch < 0  ||  ch > 128)
				return new VChar(ch);
			if (flyweight[ch] == null)//Avoid creating objects until they're needed.
				flyweight[ch] = new VChar(ch);
			return flyweight[ch];
		}
		private static final VChar[] flyweight = new VChar[128];
	}
	public static class VString implements Immutable, VList {
		public final String str;
		private VString(String str) {
			this.str=str;
		}@Override
		public String String() {
			return str;
		}@Override
		public boolean eq(Value v) {
			return str.equals(v.String());
		}
		public String toString() {
			return str;
		}
		public static VString v(String str) {
			return new VString(str);
		}
		@Override
		public int length() {
			return str.length();
		}
		public VChar getN(int index) {
			return VChar.v(str.charAt(index));
		}
		@Override
		public void setN(int index, Value v)
			{;}//will never be called since Immutable is implemented
		@Override
		public Class<? extends Value> elementType() {
			return VChar.class;
		}
	}


	public static class VInt implements Value {
		public final int n;
		private VInt(int n) {
			this.n=n;
		}@Override
		public int Int() {
			return n;
		}@Override
		public boolean eq(Value v) {
			return n==v.Int();
		}
		public String toString() {
			return Integer.toString(n);
		}
		public static VInt v(int n) {
			if (n < start  ||  n > flyweight.length+start)
				return new VInt(n);
			if (flyweight[n-start] == null)//Avoid creating objects until they're needed.
				flyweight[n-start] = new VInt(n);
			return flyweight[n-start];
		}
		//premature but I want to.
		private static final VInt[] flyweight = new VInt[256];
		private static final int start = -32;//asymmetric because I think positive values are used more frequently than negative ones
	}


	public static class VPoint extends Point implements Value {
		private VPoint(int x, int y) {
			super(x,y);
		}@Override
		public Point Point() {
			return this;
		}public boolean eq(Value v) {
			return super.equals(v.Point());
		}
		public static VPoint v(Point p) {
			if (p instanceof VPoint)
				return (VPoint)p;
			return v(p.x, p.y);
		}
		public static VPoint v(int x, int y) {
			return new VPoint(x, y);
		}
		private static final long serialVersionUID = 1L;
	}

	public static interface VRef extends Value {
		@Override void setRef(Value v);
		@Override Value getRef();
		@Override default boolean eq(Value v) {
			return this.getRef() == v.getRef();
		}
	}

	/**Implement setMember to throw if key exist*/
	static interface Immutable extends Value {
		/***/
		@Override default void setMember(String name, Value v) {
			getMember(name);//throws if name doesn't even exist or not a map
			throw Script.error("%ss are immutable.", map_firstKey(types, this.getClass(), (k,c) -> k==c ));
		}	//             plural s, eg strings not string
		//a default setN here would be nice, but subclasses implementing this and VList must implement it anyway.
	}

	/***///TODO: resizeable
	static interface VList extends VFunc {//TODO: extend java.util.List<Value>
		@Override default Value call(List<Value> param) {
			if (param == null  ||  param.isEmpty()  ||  param.size() > 2)
				Script.error("Lists must have one or two parameters.");
			int index = param.get(0).Int();
			if (index < 0)
				index = length()-index;
			if (index >= length()  ||  index < 0)
				throw Script.error("This List only have %d elements", length());
			if (param.size() == 2) {
				if (this instanceof Immutable)
					throw Script.error("This list is Immutable");
				Value _new = param.get(1);
				if (!elementType().isInstance(_new))
					throw Script.error("Element %s is not a subtype of %s", _new, types.get(elementType()));
				setN(index, _new);
			}
			return getN(index);
		}
		default VList slice(int start, int end) {
			throw Script.error("Slices are not implementted yet");
		}

		@Override
		default void validateCall(List<Class<? extends Value>> param,  Class<? extends Value> last) {
			//TODO, not used yet, make call() use this method
		}

		//Lists are comparable
		/**Slow, special implementations should override*/@Override
		default boolean eq(Value v) {
			if (v.getClass() != this.getClass()
			 || ((VList)v).elementType() != elementType()
			 || ((VList)v).length() != length())
				return false;
			for (int i=length(); i>=0; i--)
				if (((VList)v).getN(i).eq(getN(i)))
					return false;
			return true;
		}

		static String toString_helper(VList l) {
			int len = l.length();
			if (len == 0) {
				return "[]";
			}
			StringBuilder sb = new StringBuilder("[");
			sb.append(l.getN(0));
			for (int i=1; i<len; i++) {
				sb.append(' ');
				sb.append(l.getN(i));
			}
			sb.append(']');
			return sb.toString();
		}

		int length();
		Value getN(int index);
		void setN(int index, Value v);
		Class<? extends Value> elementType();
	}


	public static Value get(Object o) {
		if (o instanceof String) {
			Script.scr.name = (String)o;
			return Script.scr.current.value((String)o);
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
