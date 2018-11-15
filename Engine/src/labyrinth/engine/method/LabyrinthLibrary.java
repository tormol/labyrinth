package labyrinth.engine.method;
import static tbm.util.statics.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;
import labyrinth.engine.*;
import labyrinth.engine.method.LabyrinthLibrary;
import labyrinth.engine.method.StandardLibrary.ParameterWalker;
import tbm.util.geom.Point;

public class LabyrinthLibrary extends VFunc.Method {
	private static HashMap<String, Value> lib = new HashMap<>();
	public static HashMap<String, Value> get() {
		return lib;
	}

	private LabyrinthLibrary(String name, Class<? extends Value>[] parameters, Function<Value[], Value> function) {
		super(parameters, function);
		lib.put(name, this);
	}


	//methods add themselves to Method.map in the constructor.
	static {
		ParameterWalker pa = new ParameterWalker(null);

		/**change the type and method of a tile*/
		new LabyrinthLibrary("set", array(VPoint.class, VChar.class), param->{
			pa.start(param);
			Point p = pa.get(VPoint.class).Point();
			char symbol = pa.get().Char();
			pa.finish();
			Tile target = Script.scr.tile;
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


		new LabyrinthLibrary("get", array(VPoint.class), param->{
			pa.start(param);
			Point p = pa.get(VPoint.class).Point();
			pa.finish();
			Tile target = Script.scr.tile;
			if (p != null)
				target = TileMap.get(p);
			char one = target.getType().getSymbols()[0];
			return VChar.v(one);
		});

		/**run the method of another tile*/
		new LabyrinthLibrary("trigger", array(VPoint.class), param->{
			pa.start(param);
			Point pos = pa.get(VPoint.class).Point();
			pa.finish();
			// I think changing tile and mob for the rest of the function is OK
			if (pos != null)
				Script.scr.tile = TileMap.get(pos);
			if (Script.scr.tile.mob() != null)
				Script.scr.mob = Script.scr.tile.mob();
			if (Script.scr.tile.method != null)
				Script.scr.root.value('['+Script.scr.tile.method+']').call(new LinkedList<Value>());
			return Void;
		});

		/**teleport*/
		new LabyrinthLibrary("move", array(VPoint.class), param->{
			pa.start(param);
			Point pos = pa.get(VPoint.class).Point();
			pa.finish();
			Tile to = TileMap.get(pos);
			if (Script.scr.mob==null)
				throw Script.error("LabyrinthLibrary move: mob==null");
			//if the target is also a teleporter, you could end up teleporting infinitely.
			//using Mob.move() prevents that because it doesn't trigger tiles.
			Script.scr.mob.moveTo(to);
			TileMap.panel.repaint();
			return Void;
		});

		new LabyrinthLibrary("error", array(Value.class, null), param->{
			throw Script.error(Script.scr.root.value("cat").call(Arrays.asList(param)).toString());
		});
	}
}
