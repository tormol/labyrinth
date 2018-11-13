package tbm.util.geom;
import static tbm.util.geom.Axis.*;
import tbm.util.Random;

/***/
public enum Direction {
	/**axis: Y  angle: pi/2*/
	NORTH(Y, +Math.PI/2) {
		@Override public Direction     left() {return  WEST;}
		@Override public Direction    right() {return  EAST;}
		@Override public Direction opposite() {return SOUTH;}
	},
	/**axis: Y  angle: -pi/2*/
	SOUTH(Y, -Math.PI/2) {
		@Override public Direction     left() {return  EAST;}
		@Override public Direction    right() {return  WEST;}
		@Override public Direction opposite() {return NORTH;}
	},
	 /**axis: X  angle: pi*/
	 WEST(X, Math.PI) {
		@Override public Direction     left() {return NORTH;}
		@Override public Direction    right() {return SOUTH;}
		@Override public Direction opposite() {return  EAST;}
	},
	 /**axis: X, angle: 0*/
	 EAST(X, 0) {
		@Override public Direction     left() {return SOUTH;}
		@Override public Direction    right() {return NORTH;}
		@Override public Direction opposite() {return  WEST;}
	},
	 /**axis: null,  angle: NaN
	  *If you don't want this member, create a simple wrapper*/
	 NONE(null, Double.NaN) {
		@Override public Direction     left() {return  NONE;}
		@Override public Direction    right() {return  NONE;}
		@Override public Direction opposite() {return  NONE;}
	};

	/**the axis of this direction. WEST,EAST->X  NORTH,SOUTH->Y*/
	public final Axis axis;
	/**angle in radians, relative to EAST*/
	public final double angle;
	private Direction(Axis axis, double angle) {
		this.axis  =  axis;
		this.angle = angle;
	}

	/**if you face this direction and look left, you look...
	 * NORTH->WEST WEST->SOUTH SOUTH->EAST EAST->NORTH*/
	public abstract Direction left();

	/**if you face this direction and look right, you look...
	 * NORTH->EAST EAST->SOUTH SOUTH->WEST WEST->NORTH*/
	public abstract Direction right();

	/**gives the opposite direction
	 * NORTH->SOUTH SOUTH->NORTH WEST->EAST EAST->WEST*/
	public abstract Direction opposite();


	/**Return a random direction except NONE*/
	public static Direction random() {
		return values()[Random.rand.nextInt(4)];
	}

	/**value.equals(): north->NORTH south->SOUTH west->WEST east->EAST other->NONE*/
	public static <T> Direction find(T value, T north, T south, T west, T east) {
		if (value.equals(north))  return NORTH;
		if (value.equals(south))  return SOUTH;
		if (value.equals( west))  return  WEST;
		if (value.equals( east))  return  EAST;
		return NONE;
	}

	/**value.equals(): north->NORTH south->SOUTH west->WEST east->EAST other->throw Exception
	 *@throws UnknownDirectionException if value in not equal to any direction*/
	public static <T> Direction get(T value, T north, T south, T west, T east) throws UnknownDirectionException {
		Direction d = find(value, north, south, west, east);
		if (d != NONE)
			return d;
		throw new UnknownDirectionException(value+" does not match any direction value.");
	}

	/**Is thrown by Direction.get() if input doesn't match a direction*/
	//nested because I don't think it will be used much
	public static class UnknownDirectionException extends Exception {
		public UnknownDirectionException(String str) {
			super(str);
		}
		private static final long serialVersionUID = 1;
	}
}
