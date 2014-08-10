package labyrinth.engine.method;
import static tbm.util.statics.*;
import static labyrinth.engine.method.Value.*;
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
			Point p = pa.point();
			char symbol = pa.get().Char();
			pa.finish();
			Tile target = Script.tile;
			if (p != null)
				target = TileMap.get(p);
			Type type = Type.t(symbol);
			target.setType(type);
			if (type.method)
				target.method = charToString(symbol);
			else
				target.method = null;
			return Void;
		});

		/**run the method of another tile*/
		new Method("trigger", array(VPoint.class), param->{
			pa.start(param);
			Point pos = pa.point();
			pa.finish();
			//i think changing tile and mob for the rest of the function is OK
			if (pos != null)
				Script.tile = TileMap.get(pos);
			if (Script.tile.mob() != null)
				Script.mob = Script.tile.mob();
			if (Script.tile.method != null)
				Script.root.get('['+Script.tile.method+']').call(new LinkedList<Value>());
			return Void;
		});

		/**teleport*/
		new Method("move", array(VPoint.class), param->{
			pa.start(param);
			Point pos = pa.point();
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

		new Method("!", array(VBool.class), param->{
			return VBool.v(!param[0].Bool());
		});

		new Method("&", array(VBool.class, VBool.class, null), param->{
			for (Value v : param)
				if (v.Bool()==false)
					return False;
			return True;
		});

		new Method("|", array(VBool.class, VBool.class, null), param->{
			for (Value v : param)
				if (v.Bool()==true)
					return True;
			return False;
		});

		new Method("^", array(VBool.class, VBool.class), param->{
			if (param[0].Bool() == param[1].Bool())
				return Value.False;
			return Value.True;
		});


		new Method("+", array(VInt.class, VInt.class, null), param->{
			int sum=0;
			for (Value v : param)
				sum += v.Int();
			return new Value.VInt(sum);
		});

		new Method("-", array(VInt.class, null), param->{
			if (param.length == 1)
				return new Value.VInt(-param[0].Int());
			if (param.length == 2)
				return new Value.VInt(param[0].Int()-param[1].Int());
			throw Script.error("-(): one or two parameters");
		});

		new Method("*", array(VInt.class, null), param->{
			int sum = 1;
			for (Value v : param)
				sum *= v.Int();
			return new Value.VInt(sum);
		});

		new Method("/", array(VInt.class, VInt.class), param->{
			return new Value.VInt( param[0].Int() / param[1].Int() );
		});


		new Method("p", array(VPoint.class), param->{
			pa.start(param);
			Point p = pa.point();
			pa.finish();
			return new Value.VPoint(p);
		});

		new Method("p+", array(VPoint.class, VPoint.class, null), param->{
			int x=0, y=0;
			for (Value v : param) {
				Point p = v.Point();
				x += p.x;
				y += p.y;
			}
			return new Value.VPoint(x, y);
		});


		new Method("cat", array(VString.class, VString.class, null), param->{
			StringBuilder str = new StringBuilder();
			for (Value v : param)
				str.append(v.String());
			return new VString(str.toString());
		});

		new Method("[]", array(VString.class, VInt.class), param->{
			return new VChar( param[0].String() .charAt( param[1].Int() ) );
		});
	}


	/**A class for iterating over parameters, and allowing two integers as a point*/
	private static class ParameterWalker {
		private Value[] param = null;
		private int i;
		public ParameterWalker(Value[] param) {
			start(param);
		}
		public void start(Value[] param) {
			if (this.param != null)
				throw new RuntimeException("ParameterWalker: previous use not finish()ed.");
			this.param = param;
			i=0;
		}
		public Point point() {
			int i_bak = i;
			if (i+1<param.length && get() instanceof VInt && get() instanceof VInt)
				return new Point(param[this.i-2].Int(), param[this.i-1].Int());
			else i = i_bak;
			return get().Point();
		}
		public Value get() {
			if (i >= param.length)
				throw Script.error("too few parameters");
			Value v = param[i];
			i++;
			return v;
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
