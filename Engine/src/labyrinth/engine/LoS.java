package labyrinth.engine;

import tbm.util.geom.Direction;
import tbm.util.geom.Point;

public class LoS {
	public static int line(Point p, Direction d, int max, Action a) {
		int len=0; Tile t;
		do {
			len++;
			t = TileMap.get(p);
			a.action(t);
			p=p.add(d.point);
		} while (!t.getType().solid && len<max);
		return len-1;
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
	public static void riangle(Point start, final Direction d, int behind, final Action a) {
		int max = 1+line2(start, d, behind, a);
		Point[] side = {d.left().point, d.right().point};
		for (int i=1; i<=max; i++)
			for (Point s : side) {
				Tile t;
				Point p = start;
				int ii=1;
				do {
					p=p.move(d);
					t = TileMap.get(p);
					if (i%ii==0) {
						a.action(t);
						p=p.add(s);
					}
					ii++;
				} while (!t.getType().solid);
			}
	}
	public static void side(Point p, Direction forward, Direction side, int max, Action a) {
		p=p.move(side);
		int opening=0;
		for (int i=0;  i<max||opening>0;  i++) {
			Tile t = TileMap.get(p);
			a.action(t);
			if (t.getType().solid) {
				if (opening>0)
					side(p.move(forward.back(), opening-1), forward, side, opening, a);
				opening = 0;
			} else
				opening++;
			p=p.move(forward);
		}
	}
	public static void triangle(Point start, final Direction d, int behind, final Action a) {
		int max = line(start, d, Integer.MAX_VALUE, a);
		side(start, d, d.left(),  max, a);
		side(start, d, d.right(), max, a);
	}



	public static interface Action {
		boolean action(Tile t);
	}
}
