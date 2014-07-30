package labyrinth.engine.method;
import static labyrinth.engine.method.Value.VType.*;
import static tbm.util.statics.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import labyrinth.engine.*;
import labyrinth.engine.method.Method;
import tbm.util.geom.Point;

public class Method extends Value {
	public final VType[] parameters;
	private final Function<Value[], Value> internal;
	public Method(String name, VType[] parameters, Function<Value[], Value> function) {
		super(VType.FUNC);
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
		new Method("set", array(POINT, CHAR), param->{
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
			return Value.Void;
		});

		/**run the method of another tile*/
		new Method("trigger", array(POINT), param->{
			pa.start(param);
			Point pos = pa.point();
			pa.finish();
			//i think changing tile and mob for the rest of the function is OK
			if (pos != null)
				Script.tile = TileMap.get(pos);
			if (Script.tile.mob() != null)
				Script.mob = Script.tile.mob();
			if (Script.tile.method != null)
				Script.root.get(Script.tile.method).call(new LinkedList<Value>());
			return Value.Void;
		});

		/**teleport*/
		new Method("move", array(POINT), param->{
			pa.start(param);
			Point pos = pa.point();
			pa.finish();
			Tile to = TileMap.get(pos);
			if (Script.mob==null)
				throw Window.error("Method move: mob==null");
			//if the target is also a teleporter, you could end up teleporting infinitely.
			//using Mob.move() prevents that because it doesn't trigger tiles.
			Script.mob.moveTo(to);
			TileMap.panel.repaint();
			return Value.Void;
		});

		/*new Method("", array(), param->{
			
			return Value.Void;
		});//*/
		new Method("=", null, param->{
			if (param.length == 2)
				if (param[0].equals(param[1]))
					return Value.True;
				else
					return Value.False;
			else if (param.length != 1)
				throw Script.error("=() takes oner or two parameters");
			else if (Script.last.type == VType.REF)
				Script.last.setRef(param[0]);
			else
				throw Script.error("last is not a reference", param[0]);
			return Value.Void;
		});

		new Method("!", array(BOOL), param->{
			if (param[0] == Value.True)
				return Value.False;
			if (param[0] == Value.False)
				return Value.True;
			throw Script.error("Unknown boolean: %s", param[0].toString());
		});

		new Method("&", array(BOOL), param->{
			for (Value v : param)
				if (v.Bool()==false)
					return Value.False;
			return Value.True;
		});

		new Method("|", array(BOOL), param->{
			for (Value v : param)
				if (v.Bool()==true)
					return Value.True;
			return Value.False;
		});

		new Method("^", array(BOOL, BOOL), param->{
			if (param[0].Bool() == param[1].Bool())
				return Value.False;
			return Value.True;
		});


		new Method("+", null, param->{
			int sum=0;
			for (Value v : param)
				sum += v.Int();
			return new Value.VInt(sum);
		});

		new Method("-", null, param->{
			if (param.length == 1)
				return new Value.VInt(-param[0].Int());
			if (param.length == 2)
				return new Value.VInt(param[0].Int()-param[1].Int());
			throw Script.error("-(): one or two parameters");
		});

		new Method("*", null, param->{
			int sum = 1;
			for (Value v : param)
				sum *= v.Int();
			return new Value.VInt(sum);
		});

		new Method("/", array(INT, INT), param->{
			return new Value.VInt( param[0].Int() / param[1].Int() );
		});


		new Method("p", array(POINT), param->{
			pa.start(param);
			Point p = pa.point();
			pa.finish();
			return new Value.VPoint(p);
		});

		new Method("p+", null, param->{
			int x=0, y=0;
			for (Value v : param) {
				Point p = v.Point();
				x += p.x;
				y += p.y;
			}
			return new Value.VPoint(x, y);
		});


		new Method("cat", null, param->{
			StringBuilder str = new StringBuilder();
			for (Value v : param)
				str.append(v.String());
			return new Value.VString(str.toString());
		});

		new Method("[]", array(STRING, INT), param->{
			return new Value.VChar( param[0].String() .charAt( param[1].Int() ) );
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
			if (get().type==INT && get().type==INT)
				return new Point(param[i-2].Int(), param[i-1].Int());
			i -= 2;
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
