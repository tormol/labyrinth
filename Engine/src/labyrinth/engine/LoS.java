package labyrinth.engine;

import tbm.util.geom.Direction;
import tbm.util.geom.Point;

public class LoS {
	public static int line(Point p, Direction d, int max, Action a) {
		int i=0;
		for (; i<max; i++) {
			p=p.add(d.point);
			Tile t = TileMap.get(p);
			a.action(t);
			if (t.getType().solid)
				break;
		}
		return i;
	}
	public static int line2(Point p, Direction d, int behind, Action a) {
		a.action(TileMap.get(p));
		line(p, d.back(), behind, a);
		return line(p, d, Integer.MAX_VALUE, a);
	}
	public static int tunnel(Point p, final Direction d, int behind, final Action a) {
		int max = line2(p, d, behind, new Action(){public boolean action(Tile t) {
			a.action(t);
			a.action(TileMap.get(t.pos().move(d.left())));
			a.action(TileMap.get(t.pos().move(d.right())));
			return false;
		}});
		//line2(p.move(d.left()), d, behind-1, a);
		//line2(p.move(d.right()), d, behind-1, a);
		return max;
	}

	public static interface Action {
		boolean action(Tile t);
	}
}
