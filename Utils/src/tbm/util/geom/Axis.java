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
}
