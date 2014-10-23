package tbm.util.geom;
import static tbm.util.geom.Axis.*;
import tbm.util.Random;

/***/
public enum Direction {
	/**axis: Y  angle: pi/2*/
	NORTH(Y, +Math.PI/2),
	/**axis: Y  angle: -pi/2*/
	SOUTH(Y, -Math.PI/2),
	 /**axis: X  angle: pi*/
	 WEST(X, Math.PI),
	 /**axis: X, angle: 0*/
	 EAST(X, 0),
	 /**axis: null,  angle: NaN
	  *If you don't want this member, create a simple wrapper*/
	 NONE(null, Double.NaN);

	/**the axis of this direction. WEST,EAST->X  NORTH,SOUTH->Y*/
	public final Axis axis;
	/**angle in radians, relative to NORTH*/
	public final double angle;
	private Direction(Axis axis, double theta) {
		this.axis  =  axis;
		this.angle = theta;
	}

	/**if you face this direction and look left, you look...
	 * NORTH->WEST WEST->SOUTH SOUTH->EAST EAST->NORTH*/
	public Direction left() {switch (this) {
		case NORTH:	return WEST;
		case SOUTH:	return EAST;
		case WEST:	return SOUTH;
		case EAST:	return NORTH;
		case NONE:  return NONE;
		default:	throw new RuntimeException();
	}}
	/**if you face this direction and look right, you look...
	 * NORTH->EAST EAST->SOUTH SOUTH->WEST WEST->NORTH*/
	public Direction right() {switch (this) {
		case NORTH:	return EAST;
		case SOUTH:	return WEST;
		case WEST:	return NORTH;
		case EAST:	return SOUTH;
		case NONE:  return NONE;
		default:	throw new AssertionError("Unhandled Direction "+toString());
	}}
	/**gives the opposite direction
	 * NORTH->SOUTH SOUTH->NORTH WEST->EAST EAST->WEST*/
	public Direction opposite() {switch (this) {
		case NORTH:	return SOUTH;
		case SOUTH:	return NORTH;
		case WEST:	return EAST;
		case EAST:	return WEST;
		case NONE:  return NONE;
		default:	throw new AssertionError("Unhandled Direction "+toString());
	}}

	/**Return a random direction except NONE*/
	public static Direction random() {switch (Random.rand.nextInt(4)) {
		case  0:	return EAST;
		case  1:	return NORTH;
		case  2:	return WEST;
		case  3:	return SOUTH;
		default:	throw new AssertionError("value from tbm.util.Random out of range.");
	}}

	/**value.equals(): north->NORTH south->SOUTH west->WEST east->EAST other->NONE*/
	public static <T> Direction find(T value, T north, T south, T west, T east) {
		if (value.equals(north))  return NORTH;
		if (value.equals(south))  return SOUTH;
		if (value.equals(west ))  return WEST;
		if (value.equals(east ))  return EAST;
		return NONE;
	}
	/**value.equals(): north->NORTH south->SOUTH west->WEST east->EAST other->throw Exception*/
	public static <T> Direction get(T value, T north, T south, T west, T east) throws Exception {
		Direction d = find(value, north, south, west, east);
		if (d != NONE)
			return d;
		throw new Exception('"'+value.toString()+"' does not match any direction value.");
	}
}
