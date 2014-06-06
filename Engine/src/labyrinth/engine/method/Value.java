package labyrinth.engine.method;
import static labyrinth.engine.method.VType.*;
import labyrinth.engine.Window;

import tbm.util.geom.Point;

public abstract class Value {
	public final VType type;
	protected Value(VType t) {
		type=t;
	}
	public Object value() {return null;}
	//public VType basicType() {return type;}
	public int Int() {throw Window.error("not an Integer");}
	public Point Point() {throw Window.error("not a Point");}
	public char Char() {throw Window.error("not a characther");}
	public String String() {throw Window.error("not a string");}



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

	public static class Var extends Value {
		public Value value;
		public final String name;
		protected Var(VType t, String name) {
			super(t);
			this.name = name;
		}
		public void set(Value v) {
			if 
			if (this.type != VOID  &&  this.type != v.type)
				;
		}

		public int Int() {throw Window.error("\"%s\" is not an Integer", name);}
		public Point Point() {throw Window.error("\"%s\" is not a Point", name);}
		public char Char() {throw Window.error("\"%s\" is not a characther", name);}
		public String String() {throw Window.error("\"%s\" is not a string", name);}
	}
}
