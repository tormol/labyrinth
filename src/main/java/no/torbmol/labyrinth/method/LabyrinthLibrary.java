package no.torbmol.labyrinth.method;
import static no.torbmol.util.statics.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;
import no.torbmol.labyrinth.*;
import no.torbmol.labyrinth.method.StandardLibrary.ParameterWalker;
import no.torbmol.util.geom.Point;

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
			if (p != null) {
				target = TileMap.get(p);
			}
			char one = target.getType().getSymbols()[0];
			return VChar.v(one);
		});

		new LabyrinthLibrary("mobAt", array(VPoint.class), param->{
			pa.start(param);
			Point p = pa.get(VPoint.class).Point();
			pa.finish();
			for (Mob mob : Mob.mobs) {
				if (mob.tile().pos().equals(p)) {
					return VString.v(mob.name());
				}
			}
			return Void;
		});

		new LabyrinthLibrary("addEnemy", array(VPoint.class, VString.class, VString.class, VInt.class, VInt.class, VInt.class), param->{
			pa.start(param);
			Point pos = pa.get(VPoint.class).Point();
			String path = pa.get(VString.class).String();
			String type = pa.get(VString.class).String();
			int waitStart = pa.get(VInt.class).Int();
			int waitShorter = pa.get(VInt.class).Int();
			int waitMin = pa.get(VInt.class).Int();
			pa.finish();
			Tile start = TileMap.get(pos);
			if (type == null || type.equals("normal")) {
				new Enemy.Normal(start, path, waitStart, waitShorter, waitMin).pause(false);
			} else if (type.equals("Ghost")) {
				new Enemy.Ghost(start, path, waitStart, waitShorter, waitMin).pause(false);
			} else if (type.equals("Targeting")) {
				new Enemy.Targeting(start, path, waitStart, waitShorter, waitMin).pause(false);
			} else {
				throw Script.error("Unknown enemy type "+type);
			}
			return Void;
		});

		new LabyrinthLibrary("enemyPos", array(VInt.class), param->{
			int want = param[0].Int();
			try {
				return VPoint.v(Enemy.getAll().get(want).tile().pos());
			} catch (IndexOutOfBoundsException oob) {
				throw Script.error("Enemy index %d out of range (current enemies: %d)", want, Enemy.getAll().size());
			}
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
		new LabyrinthLibrary("move", array(VPoint.class, null), param-> {
			pa.start(param);
			Point pos = pa.get(VPoint.class).Point();
			Value enemy = pa.get(VInt.class, false);
			pa.finish();
			Tile to = TileMap.get(pos);
			if (enemy != null) {
				int want = enemy.Int();
				try {
					Enemy.getAll().get(want).moveTo(to);
				} catch (IndexOutOfBoundsException oob) {
					throw Script.error("Enemy index %d out of range (current enemies: %d)", want, Enemy.getAll().size());
				}
			} else if (Script.scr.mob != null) {
				//if the target is also a teleporter, you could end up teleporting infinitely.
				//using Mob.move() prevents that because it doesn't trigger tiles.
				Script.scr.mob.moveTo(to);
			} else {
				Player.get().moveTo(to);
			}
			TileMap.panel.repaint();
			return Void;
		});

		new LabyrinthLibrary("playerPos", array(), param->{
			pa.start(param).finish();
			for (Mob mob : Mob.mobs) {
				if (mob instanceof Player) {
					return VPoint.v(mob.tile().pos());
				}
			}
			throw Script.error("playerPos(): Player not found in Mob.mobs");
		});

		new LabyrinthLibrary("text", array(VString.class, null), param->{
			switch (param.length) {
				case 0:
					Window.hideText();
					break;
				case 1:
					Window.setText(param[0].String());
					break;
				case 2:
					Window.setText(param[0].String(), param[1].String());
					break;
				default:
					throw Script.error("Too many arguments to text()");
			}
			return Void;
		});

		new LabyrinthLibrary("end", array(VString.class), param->{
			Window.end(param[0].String());
			return Void;
		});

		new LabyrinthLibrary("error", array(Value.class, null), param->{
			throw Script.error(Script.scr.root.value("cat").call(Arrays.asList(param)).toString());
		});
	}
}
