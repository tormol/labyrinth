package tbm.util.geom;

public enum Axis {
	X('x'),Y('y');
	/**lowercase letter of axis*/
	public final char letter;

	Axis(char c) {
		this.letter = c;
	}

	/**get the other axis: X->Y, Y->X*/
	public Axis flip() {
		switch (this) {
		  case X: return Y;
		  case Y: return X;
		  default: throw new AssertionError("Unhandled Axis "+this.toString());
		}
	}
}
