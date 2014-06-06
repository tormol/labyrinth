package labyrinth.engine.method;

import static labyrinth.engine.method.VType.*;
import static tbm.util.statics.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import labyrinth.engine.Mob;
import labyrinth.engine.Tile;
import labyrinth.engine.TileMap;
import labyrinth.engine.Type;
import labyrinth.engine.Window;
import labyrinth.engine.method.Method;
import tbm.util.geom.Point;

class Method {
	public final String name;
	public final VType[] parameters;
	public final VType ret;
	private final Function<Value[], Operation> init;
	public Method(String name, VType[] parameters, VType ret, Function<Value[], Operation> init) {
		this.name = name;
		this.parameters = parameters;
		this.ret = ret;
		this.init = init;
		map.put(name, this);
	}
	public Operation instance(Value... arg) {
		return init.apply(arg);
	}



	public static Method get(String name) {
		return map.get(name);
	}
	protected static Map<String, Method> map = new HashMap<>();


	static {
		/**change the type and method of a tile*/
		new Method("set", array(POINT, CHAR), VOID, params->{
			Point p = params[0].Point();
			char symbol = params[1].Char();
			Tile target;
			if (p==null)
				target = null;
			else
				target = TileMap.get(p);
			Type type = Type.t(symbol);
			return (tile, mob) -> {
				if (target != null)
					tile = target;
				tile.setType(type);
				if (type.method)
					tile.method = Procedure.get(String.valueOf(symbol));
				else
					tile.method = null;
				return Value.Void;
			};
		});

		/**run the method of another tile*/
		new Method("trigger", array(POINT), VOID, params->{
			Point pos = params[0].Point();
			return (Tile tile, Mob mob) -> {
				if (pos != null)
					tile = TileMap.get(pos);
				if (tile.mob() != null)
					mob = tile.mob();
				if (tile.method != null)
					tile.method.perform(tile, mob);
				return Value.Void;
			};
		});

		/**teleport*/
		new Method("move", array(POINT), VOID, params->{
			Point pos = params[0].Point();
			Tile to = TileMap.get(pos); 
			return (Tile tile, Mob mob) -> {
				if (pos != null)
					tile = TileMap.get(pos);
				if (mob==null)
					throw Window.error("Method move: mob==null");
				//if the target is also a teleporter, you could end up teleporting infinitely.
				//using Mob.move() prevents that because it doesn't trigger tiles.
				mob.moveTo(to);
				TileMap.panel.repaint();
				return Value.Void;
			};
		});
	}
}