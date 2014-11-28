package labyrinth.engine.method;
import static tbm.util.statics.*;
import static labyrinth.engine.method.Value.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import labyrinth.engine.*;
import labyrinth.engine.method.Method;
import tbm.util.geom.Point;

public class Method implements VFunc {
	public final Class<?extends Value>[] parameters;
	private final Function<Value[], Value> internal;
	public Method(String name, Class<? extends Value>[] parameters, Function<Value[], Value> function) {
		this.parameters = parameters;
		this.internal = function;
		Script.root.define(name, this);
	}
	@Override
	public Value call(List<Value> param) {
		return internal.apply(param.toArray(new Value[param.size()]));
	}


	/**Runs the static code that adds the methods*/
	public static void start()
		{}
	//methods add themselves to Moethod.map in the constructor.
	static {
		ParameterWalker pa = new ParameterWalker(null);
		/**change the type and method of a tile*/
		new Method("set", array(VPoint.class, VChar.class), param->{
			pa.start(param);
			Point p = pa.get(VPoint.class).Point();
			char symbol = pa.get().Char();
			pa.finish();
			Tile target = Script.tile;
			if (p != null)
				target = TileMap.get(p);
			Type type = Type.t(symbol);
			target.setType(type);
			if (type.method)
				target.method = char2str(symbol);
			else
				target.method = null;
			return Void;
		});

		/**run the method of another tile*/
		new Method("trigger", array(VPoint.class), param->{
			pa.start(param);
			Point pos = pa.get(VPoint.class).Point();
			pa.finish();
			//i think changing tile and mob for the rest of the function is OK
			if (pos != null)
				Script.tile = TileMap.get(pos);
			if (Script.tile.mob() != null)
				Script.mob = Script.tile.mob();
			if (Script.tile.method != null)
				Script.root.value('['+Script.tile.method+']').call(new LinkedList<Value>());
			return Void;
		});

		/**teleport*/
		new Method("move", array(VPoint.class), param->{
			pa.start(param);
			Point pos = pa.get(VPoint.class).Point();
			pa.finish();
			Tile to = TileMap.get(pos);
			if (Script.mob==null)
				throw Script.error("Method move: mob==null");
			//if the target is also a teleporter, you could end up teleporting infinitely.
			//using Mob.move() prevents that because it doesn't trigger tiles.
			Script.mob.moveTo(to);
			TileMap.panel.repaint();
			return Void;
		});

		/*new Method("", array(), param->{
			
			return Value.Void;
		});//*/
		new Method("=", array(Value.class, null), param->{
			if (param.length == 2)
				if (param[0].equals(param[1]))
					return Value.True;
				else
					return Value.False;
			else if (param.length != 1)
				throw Script.error("=() takes oner or two parameters");
			else if (Script.last instanceof VRef)
				Script.last.setRef(param[0]);
			else
				throw Script.error("last is not a reference", param[0]);
			return Void;
		});

		new Method("write", array(Value.class, null), param->{
			for (Value v : param)
				System.out.print(v.toString());
			System.out.println();
			return Void;
		});

		new Method("error", array(Value.class, null), param->{
			throw Script.error(Script.root.value("cat").call(Arrays.asList(param)).toString());
		});

		new Method("?", array(VBool.class, Value.class, Value.class), param->{
			pa.start(param);
			boolean _if = pa.get().Bool();
			Value _then = pa.get();
			Value _else = pa.get();
			pa.finish();
			return _if ? _then : _else;
		});

		new Method("if", array(VBool.class, VFunc.class, VFunc.class), param->{
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

		new Method("!", array(VBool.class), param->{
			return VBool.v(!param[0].Bool());
		});

		new Method("&", array(VBool.class, VBool.class, null), param->{
			for (Value v : param) {
				if (v.Bool()==false)
					return False;
			}
			return True;
		});

		new Method("|", array(VBool.class, VBool.class, null), param->{
			for (Value v : param) {
				if (v.Bool()==true)
					return True;
			}
			return False;
		});

		new Method("^", array(VBool.class, VBool.class), param->{
			if (param[0].Bool() == param[1].Bool())
				return Value.False;
			return Value.True;
		});


		new Method("+", array(VInt.class, VInt.class, vararg), param->{
			int sum=0;
			for (Value v : param)
				sum += v.Int();
			return VInt.v(sum);
		});

		//more than two parameters would be confusing because (a - (b - c)) != -((a - b) - c),
		//altough the latter is most obivious. you can use +(a -b -c)anyway
		new Method("-", array(VInt.class, null), param->{
			if (param.length == 1)
				return VInt.v(-param[0].Int());
			if (param.length == 2)
				return VInt.v(param[0].Int()-param[1].Int());
			throw Script.error("-(): one or two parameters");
		});

		new Method("*", array(VInt.class, null), param->{
			int sum = 1;
			for (Value v : param)
				sum *= v.Int();
			return VInt.v(sum);
		});

		new Method("/", array(VInt.class, VInt.class), param->{
			return VInt.v( param[0].Int() / param[1].Int() );
		});


		new Method("p", array(VPoint.class), param->{
			pa.start(param);
			Point p = pa.get(VPoint.class).Point();
			pa.finish();
			return VPoint.v(p);
		});

		new Method("p+", array(VPoint.class, VPoint.class, null), param->{
			int x=0, y=0;
			for (Value v : param) {
				Point p = v.Point();
				x += p.x;
				y += p.y;
			}
			return VPoint.v(x, y);
		});


		new Method("cat", array(VString.class, VString.class, null), param->{
			StringBuilder str = new StringBuilder();
			for (Value v : param)
				str.append(v.String());
			return VString.v(str.toString());
		});

		new Method("[]", array(VString.class, VInt.class), param->{
			return VChar.v( param[0].String() .charAt( param[1].Int() ) );
		});
	}


	/**A class for iterating over parameters, and allowing two integers as a point*/
	private static class ParameterWalker {
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
			if (required)
				throw Script.error("%d. parameter should be of type %s.", i, map_firstKey(Value.types, type));
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


	//new("length", VString.class, VInt.class, vstr->VInt.v(vstr.string.length()) );
	/**singe-parameter functions without side effects, argument can be parameter or Script.last*/
	static class OneP implements Operation, VFunc {
		public final Class<? extends Value> in, out;
		private final Function<Value, Value> internal;
		//TODO: make static creator using generics to simplify.
		/**in and out are determined from the example value and the result from calling f on the example value*/
		@SuppressWarnings("unchecked")//Is checked when used
		//altough value.Type() would work, this allows more specific error messages and slightly shorter code
		public <IN extends Value> OneP(String name, IN example, Function<IN, Value> f) {
			this(name, example.getClass(), f.apply(example).getClass(), (Function<Value, Value>)f);
		}
		public OneP(String name, Class<? extends Value> in, Class<? extends Value> out, Function<Value, Value> f) {
			this.in = in;
			this.out = out;
			this.internal = f;
			Script.root.define(name, this);
		}

		@Override
		public boolean eq(Value v) {
			return v==this;
		}
		@Override
		public Value call(List<Value> param) {
			Value arg;
			if (param == null  ||  !param.isEmpty())
				arg = Script.last;
			else if (param.size() == 1)
				arg = param.get(0);
			else
				throw Script.error("more than one argument");
			if (!in.isInstance(arg))
				throw Script.error("Wrong type, must be %s", types.get(in));
			return internal.apply(arg);
		}
		@Override
		public Value perform() {
			return call(null);
		}
	}

	static {//meh, doesnt look too nice
		new OneP("length", VString.v("st"), vstr->VInt.v(vstr.String().length()));
	}
}
