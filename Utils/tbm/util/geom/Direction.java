package tbm.util.geom;
import static tbm.util.geom.Axis.*;

/**Retningen enheten ser.*/
public enum Direction {
	NORTH(Y, 0),
	SOUTH(Y, Math.PI),
	 WEST(X, Math.PI/2),
	 EAST(X, Math.PI*3/2);

	public final Axis axis;
	public final double theta;
	/**hvor mye bildet må roteres.*/
	private Direction(Axis axis, double theta) {
		this.axis  =  axis;
		this.theta = theta;
	}

	/***/
	public Direction left() {switch (this) {
		case NORTH:	return WEST;
		case SOUTH:	return EAST;
		case WEST:	return SOUTH;
		case EAST:	return NORTH;
		default:	throw new RuntimeException();
	}}
	/***/
	public Direction right() {switch (this) {
		case NORTH:	return EAST;
		case SOUTH:	return WEST;
		case WEST:	return NORTH;
		case EAST:	return SOUTH;
		default:	throw new AssertionError("Unhandled Direction "+toString());
	}}
	/***/
	public Direction back() {switch (this) {
		case NORTH:	return SOUTH;
		case SOUTH:	return NORTH;
		case WEST:	return EAST;
		case EAST:	return WEST;
		default:	throw new AssertionError("Unhandled Direction "+toString());
	}}
	/**Returner den retningen verdi er lik*/
	public static <T> Direction d(T v, T n, T s, T w, T e) {
		if (v.equals(n))  return NORTH;
		if (v.equals(s))  return SOUTH;
		if (v.equals(w))  return WEST;
		if (v.equals(e))  return EAST;
		return null;
	}
}
