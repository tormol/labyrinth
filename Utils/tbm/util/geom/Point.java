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
	public Point add(int x, int y) {return new Point(this.x+x, this.y+y);}
	/***/
	public Point add(Point p) {return add(p.x, p.y);}
	public Point add(Axis a, int value) {switch (a) {
		case X: return add(x+value, y);
		case Y: return add(x, value+y);
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}

	/***/
	public Point negate() {return new Point(-x, -y);}

	/***/
	public Point diff(int x, int y) {return add(-x, -y);}
	/***/
	public Point diff(Point p) {return diff(p.x, p.y);}

	/***/
	public int flyttAvstand(int x, int y) {
		return Math.abs(this.x-x) + Math.abs(this.y-y);
	}
	/***/
	public int flyttAvstand(Point p) {return flyttAvstand(p.x, p.y);}
	/***/
	public double abs() {return sqrt(x*x + y*y);}
	/***/
	public Point enhet() {return new Point((int)signum(x), (int)signum(y));}

	/***/
	public <T> Point move(T direction, T xa, T xs, T ya, T ys) {
		return move(direction, xa, xs, ya, ys, 1);
	}
	/***/
	public <T> Point move(T direction, T xa, T xs, T ya, T ys, int n) {
		if (xa.equals(direction))  return add(+n, 0);
		if (xs.equals(direction))  return add(-n, 0);
		if (ya.equals(direction))  return add(0, +n);
		if (ys.equals(direction))  return add(0, -n);
		return this;
	}

	public int getX() {return x;}
	public int getY() {return y;}
	public int get(Axis a) {switch (a) {
		case X: return x;
		case Y: return y;
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}

	public Point withX(int x) {return new Point(x, this.y);}
	public Point withY(int y) {return new Point(this.x, y);}
	public Point with(Axis a, int v) {switch (a) {
		case X: return new Point(v, y);
		case Y: return new Point(x, v);
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}

	/***/
	public String toString() {return x+", "+y;}
	/***/
	public String toString(char o, char c) {return Helper.toString(o, this, c);}
	/***/
	public Point toPoint() {return new Point(x, y);}
	/***/
	public int[] toArray() {return new int[]{x, y};}

	private static final long serialVersionUID = 1L;
}
