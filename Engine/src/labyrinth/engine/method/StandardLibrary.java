package labyrinth.engine.method;
import static tbm.util.statics.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;
import labyrinth.engine.method.StandardLibrary;
import tbm.util.geom.Point;

public class StandardLibrary extends VFunc.Method {
	private static HashMap<String, Value> lib = new HashMap<>();
	public static HashMap<String, Value> get() {
		return lib;
	}

	private StandardLibrary(String name, Class<? extends Value>[] parameters, Function<Value[], Value> function) {
		super(parameters, function);
		lib.put(name, this);
	}



	//methods add themselves to Moethod.map in the constructor.
	static {
		ParameterWalker pa = new ParameterWalker(null);
		lib.put("true", Value.True);
		lib.put("false", Value.False);
		lib.put("null", Value.Void);

		/*new StandardLibrary("", array(), param->{
			
			return Value.Void;
		});//*/
		new StandardLibrary("=", array(Value.class, null), param->{
			if (param.length == 2)
				if (param[0].eq(param[1]))
					return Value.True;
				else
					return Value.False;
			else if (param.length != 1)
				throw Script.error("=() takes oner or two parameters");
			else if (Script.scr.last instanceof VRef)
				Script.scr.last.setRef(param[0]);
			else
				throw Script.error("last is not a reference", param[0]);
			return Void;
		});

		new StandardLibrary("write", array(Value.class, null), param->{
			for (Value v : param)
				System.out.print(v.toString());
			return Void;
		});

		new StandardLibrary("error", array(Value.class, null), param->{
			for (Value v : param)
				System.err.print(v.toString());
			return Void;
		});

		new StandardLibrary("?", array(VBool.class, Value.class, Value.class), param->{
			pa.start(param);
			boolean _if = pa.get().Bool();
			Value _then = pa.get();
			Value _else = pa.get();
			pa.finish();
			return _if ? _then : _else;
		});

		new StandardLibrary("if", array(VBool.class, VFunc.class, VFunc.class), param->{
			pa.start(param);
			boolean _if = pa.get().Bool();
			Value _then = pa.get();
			if (!(_then instanceof VFunc))
				throw Script.error("Second parameter to if() not a function.");
			Value _else = null;
			if (param.length == 3) {
				_else = pa.get();
				if (!(_else instanceof VFunc))
					throw Script.error("Third parameter to if() not a function.");
			}
			pa.finish();
			if (_if)
				return _then.call(new LinkedList<>());
			if (_else != null)
				return _else.call(new LinkedList<>());
			return Void;
		});

		new StandardLibrary("!", array(VBool.class), param->{
			return VBool.v(!param[0].Bool());
		});

		new StandardLibrary("&", array(VBool.class, VBool.class, null), param->{
			for (Value v : param) {
				if (v.Bool()==false)
					return False;
			}
			return True;
		});

		new StandardLibrary("|", array(VBool.class, VBool.class, null), param->{
			for (Value v : param) {
				if (v.Bool()==true)
					return True;
			}
			return False;
		});

		new StandardLibrary("^", array(VBool.class, VBool.class), param->{
			if (param[0].Bool() == param[1].Bool())
				return Value.False;
			return Value.True;
		});


		new StandardLibrary("+", array(VInt.class, VInt.class, vararg), param->{
			int sum=0;
			for (Value v : param)
				sum += v.Int();
			return VInt.v(sum);
		});

		//more than two parameters would be confusing because (a - (b - c)) != -((a - b) - c),
		//altough the latter is most obivious. you can use +(a -b -c)anyway
		new StandardLibrary("-", array(VInt.class, null), param->{
			if (param.length == 1)
				return VInt.v(-param[0].Int());
			if (param.length == 2)
				return VInt.v(param[0].Int()-param[1].Int());
			throw Script.error("-(): one or two parameters");
		});

		new StandardLibrary("*", array(VInt.class, null), param->{
			int sum = 1;
			for (Value v : param)
				sum *= v.Int();
			return VInt.v(sum);
		});

		new StandardLibrary("/", array(VInt.class, VInt.class), param->{
			return VInt.v( param[0].Int() / param[1].Int() );
		});


		new StandardLibrary(">", array(VInt.class, VInt.class), param->{
			return VBool.v(param[0].Int() > param[1].Int());
		});

		new StandardLibrary(">=", array(VInt.class, VInt.class), param->{
			return VBool.v(param[0].Int() >= param[1].Int());
		});

		new StandardLibrary("<=", array(VInt.class, VInt.class), param->{
			return VBool.v(param[0].Int() <= param[1].Int());
		});

		new StandardLibrary("<", array(VInt.class, VInt.class), param->{
			return VBool.v(param[0].Int() < param[1].Int());
		});


		new StandardLibrary("p", array(VPoint.class), param->{
			pa.start(param);
			Point p = pa.get(VPoint.class).Point();
			pa.finish();
			return VPoint.v(p);
		});

		new StandardLibrary("p+", array(VPoint.class, VPoint.class, null), param->{
			int x=0, y=0;
			for (Value v : param) {
				Point p = v.Point();
				x += p.x;
				y += p.y;
			}
			return VPoint.v(x, y);
		});

		new StandardLibrary("x", array(VPoint.class), param->{
			return VInt.v(param[0].Point().x);
		});

		new StandardLibrary("y", array(VPoint.class), param->{
			return VInt.v(param[0].Point().y);
		});


		new StandardLibrary("cat", array(VString.class, Value.class, null), param->{
			StringBuilder str = new StringBuilder();
			for (Value v : param) {
				str.append(v.toString());
			}
			return VString.v(str.toString());
		});

		new StandardLibrary("[]", array(VString.class, VInt.class), param->{
			return VChar.v( param[0].String().charAt( param[1].Int() ) );
		});

		new StandardLibrary("rand", array(VInt.class, null), param->{
			switch (param.length) {
				case 0:
					// all values can be returned, as Math.random() returns a double,
					// whose mantissa has more bits than an int
					return VInt.v((int)(Math.random()*((double)Integer.MAX_VALUE)+1));
				case 1:
					return VInt.v((int)(Math.random()*param[0].Int()));
				case 2:
					int min = param[0].Int();
					int max = param[1].Int();
					return VInt.v((int)(Math.random()*(max-min))+min);
				default:
					throw Script.error("rand() does not accept more than two parameters.");
			}
		});

		lib.put("length", new OneP(VString.v("st"), vstr->VInt.v(vstr.String().length())));
	}


	/**A class for iterating over parameters, and allowing two integers as a point*/
	static class ParameterWalker {
		private Value[] param = null;
		private int i;
		public ParameterWalker(Value[] param) {
			start(param);
		}
		public ParameterWalker start(Value[] param) {
			if (this.param != null)
				throw new RuntimeException("ParameterWalker: previous use not finish()ed.");
			this.param = param;
			i = 0;
			return this;
		}

		public Value get(Class<? extends Value> type, boolean required) {
			Value v = get(required);
			if (type.isInstance(v))
				return v;
			if (type == VPoint.class  &&  v instanceof VInt) {
				Value y = get(VInt.class, false);
				if (y != null)
					return VPoint.v(v.Int(), y.Int());
			}
			if (required) {
				String t = map_firstKey(Value.types, type, Object::equals);
				throw Script.error("%d. parameter should be of type %s.", i, t);
			}
			return null;
		}
		public Value get(boolean required) {
			Value v = null;
			if (i < param.length) {
				v = param[i];
				i++;
			} else if (required)
				throw Script.error("too few parameters");
			return v;
		}
		public Value get(Class<? extends Value> type) {
			return get(type, true);
		}
		public Value get() {
			return get(true);
		}

		public void finish() {
			if (param == null)
				;//an extra call is harmless, allows early stop  
				//throw new RuntimeException("ParameterWalker: allready finished()");
			else if (i < param.length)
				throw Script.error("too many parameters");
			param = null;
		}
	}
}
