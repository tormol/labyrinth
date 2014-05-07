package tbm.util.geom;

public enum Axis {
	X('x'),Y('y');
	public final char c;
	Axis(char c) {
		this.c = c;
	}

	public Axis flip() {
		switch (this) {
		  case X: return Y;
		  case Y: return X;
		  default: throw new AssertionError("Unhandled Axis "+this.toString());
		}
	}

	public Point setPoint(Point p, int value) {
		switch (this) {
		  case X: return new Point(value, p.y);
		  case Y: return new Point(p.x, value);
		  default: throw new AssertionError("Unhandled Axis "+this.toString());
		}
	}

	public Point addPoint(Point p, int value) {
		switch (this) {
		  case X: return new Point(p.x+value, p.y);
		  case Y: return new Point(p.x, value+p.y);
		  default: throw new AssertionError("Unhandled Axis "+this.toString());
		}
	}

	public int getAxis(Point p) {
		switch (this) {
		  case X: return p.x;
		  case Y: return p.y;
		  default: throw new AssertionError("Unhandled Axis "+this.toString());
		}
	}
}