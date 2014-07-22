package labyrinth.engine.method;
import static labyrinth.engine.method.VType.*;
import static tbm.util.statics.*;
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
	public Value call(Value[] param) {
		return internal.apply(param);
	}




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
				target.method = String.valueOf(symbol);
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
				Script.root.get(Script.tile.method).call(new Value[0]);
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
					;
				else
					;
			else
				;
			return Value.Void;
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
