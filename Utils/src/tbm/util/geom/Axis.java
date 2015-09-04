package tbm.util.geom;

public enum Axis {
	X('x') {@Override public Axis flip() {return Y;}},
	Y('y') {@Override public Axis flip() {return X;}};
	Axis(char c) {
		this.letter = c;
	}

	/**lowercase letter of axis*/
	public final char letter;
	/**get the other axis: X->Y, Y->X*/
	public abstract Axis flip();
}
