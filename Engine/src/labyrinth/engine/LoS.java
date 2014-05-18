package labyrinth.engine;

import java.util.ArrayDeque;
import java.util.Queue;

import tbm.util.geom.Direction;
import tbm.util.geom.Point;

public class LoS {
	public static int line(Point p, Direction d, int max, Action a) {
		if (a==null)
			a=new Action(){public void action(Tile t){}};
		int len=0; Tile t;
		do {
			len++;
			t = TileMap.get(p);
			a.action(t);
			p=p.move(d);
		} while (!t.getType().solid && len<max);
		return len-1;
	}
	public static void triangle(Point start, Direction forward, Action a) {
		int max = line(start, forward, Integer.MAX_VALUE, a);
		Direction back = forward.back();
		Queue<Line> lines = new ArrayDeque<>();
		lines.add(new Line(start, forward.left() , max));
		lines.add(new Line(start, forward.right(), max));
		while (!lines.isEmpty()) {
			Line l = lines.remove();
			Point p = l.start.move(l.side);
			int i, opening=0;
			for (i=0;  i<l.max||opening>0;  i++) {
				Tile t = TileMap.get(p);
				a.action(t);
				if (t.getType().solid) {
					if (opening>0)
						lines.add(new Line(p.move(back, opening-1), l.side, opening-1));
					opening = 0;
				} else
					opening++;
				p=p.move(forward);
			}
			if (i==l.max)
				a.action(TileMap.get(p));
		}

		//Look behind
		a.action(TileMap.get(start.move(back)));
		a.action(TileMap.get(start.move(back).move(forward.left())));
		a.action(TileMap.get(start.move(back).move(forward.right())));
	}



	private static class Line {
		final Point start;
		final Direction side;
		final int max;
		Line(Point start, Direction side, int max) {
			this.start=start;
			this.side=side;
			this.max=max;
		}
	}
	public static interface Action {
		void action(Tile t);
	}
}
