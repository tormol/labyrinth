//&Point = FPoint
//&int = float
package tbm.util.geom;
import static java.lang.Math.*;

import java.awt.geom.Point2D;
import java.io.Serializable;


/***/
public class Point implements Serializable, Cloneable {
	public final int x,y;

	/***/
	public Point() {this(0, 0);}
	/***/
	public Point(int x, int y) {this.x=x; this.y=y;}
	/***/
	public Point(Point p) {this(p.x, p.y);}
	/***/
	public Point(Point2D p) {this((int)p.getX(), (int)p.getY());}
	/***/
	public Point(int[] a) {this(a[0], a[1]);}
	/***/
	public Point(String str) {this(Helper.parsePoint(str));}
	/***/
	public Point(Axis a) {
		if (a==null) {
			x=0; y=0;
		} else switch (a) {
			case  X:	x=1; y=0; break;
			case  Y:	x=0; y=1; break;
			default:	throw new AssertionError("Unhandled Axis "+a.toString());
		}
	}
	/***/
	public Point(Direction d) {
		if (d==null) {
			x=0; y=0;
		} else switch (d) {
			case NORTH: x= 0; y=-1; break;
			case SOUTH:	x= 0; y=+1; break;
			case  WEST:	x=-1; y= 0; break;
			case  EAST:	x=+1; y= 0; break;
			default:	throw new AssertionError("Unhandled Direction "+d.toString());
		}
	}
	/***/
	public Point move(int x, int y) {return new Point(this.x+x, this.y+y);}
	/***/
	public Point move(Point p) {return move(p.x, p.y);}
	/***/
	public Point move(Axis a, int value) {switch (a) {
		case X: return move(value, 0);
		case Y: return move(0, value);
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}
	/***/
	public Point move(Direction d) {return move(d, 1);}
	/***/
	public Point move(Direction d, int n) {return move(new Point(d).times(n));}
	/***/
	public <T> Point move(T direction, T xa, T xs, T ya, T ys) {return move(direction, xa, xs, ya, ys, 1);}
	/***/
	public <T> Point move(T direction, T xa, T xs, T ya, T ys, int n) {
		Direction d = Direction.d(direction, xa, xs, ya, ys);
		if (d==null)
			return this;
		return move(d, n);
	}

	/***/
	public Point negate() {return new Point(-x, -y);}
	/***/
	public Point times(int n) {return new Point(x*n, y*n);}

	/***/
	public Point diff(int x, int y) {return move(-x, -y);}
	/***/
	public Point diff(Point p) {return diff(p.x, p.y);}

	/**Number of horisontal and vertical moves needed.*/
	public int movesTo(int x, int y) {
		return Math.abs(this.x-x) + Math.abs(this.y-y);
	}
	/***/
	public int movesTo(Point p) {return movesTo(p.x, p.y);}
	/***/
	public double abs() {return sqrt(x*x + y*y);}
	/***/
	public Point unit() {return new Point((int)signum(x), (int)signum(y));}


	public int getX() {return x;}
	public int getY() {return y;}
	public int get(Axis a) {switch (a) {
		case X: return x;
		case Y: return y;
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}
	public int get(Direction d) {switch (d) {
		case NORTH:	return -y;
		case SOUTH:	return +y;
		case  WEST:	return -x;
		case  EAST:	return -x;
		default:	throw new AssertionError("Unhandled Direction "+d.toString());
	}}
	

	public Point withX(int x) {return new Point(x, this.y);}
	public Point withY(int y) {return new Point(this.x, y);}
	public Point with(Axis a, int v) {switch (a) {
		case X: return new Point(v, y);
		case Y: return new Point(x, v);
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}

	/***/
	public boolean equals(int x, int y) {return (this.x==x && this.y==y);}
	/***/
	public String toString() {return x+", "+y;}
	/***/
	public String toString(char o, char c) {return Helper.toString(o, this, c);}
	/***/
	public Point toPoint() {return new Point(x, y);}
	/***/
	public int[] toArray() {return new int[]{x, y};}

	@Deprecated	public Point add(int x, int y) {return move(x, y);}
	@Deprecated	public Point add(Point p) {return move(p);}
	@Deprecated	public Point add(Axis a, int value) {return move(a, value);}
	@Deprecated	public int flyttAvstand(int x, int y) {return movesTo(x, y);}
	@Deprecated	public int flyttAvstand(Point p) {return movesTo(p);}
	@Deprecated	public Point enhet() {return unit();}
	private static final long serialVersionUID = 1L;
}
