//Generated from Point.java
package tbm.util.geom;
import static java.lang.Math.*;
import java.awt.geom.Point2D;
import java.io.Serializable;


/***/
public class FPoint implements Serializable, Cloneable {
	public final float x,y;

	/***/
	public FPoint() {this(0, 0);}
	/***/
	public FPoint(float x, float y) {this.x=x; this.y=y;}
	/***/
	public FPoint(FPoint p) {this(p.x, p.y);}
	/***/
	public FPoint(Point2D p) {this((float)p.getX(), (float)p.getY());}
	/***/
	public FPoint(float[] a) {this(a[0], a[1]);}
	/***/
	public FPoint(String str) {this(Helper.parsePoint(str));}

	/***/
	public FPoint add(float x, float y) {return new FPoint(this.x+x, this.y+y);}
	/***/
	public FPoint add(FPoint p) {return add(p.x, p.y);}
	public FPoint add(Axis a, float value) {switch (a) {
		case X: return add(x+value, y);
		case Y: return add(x, value+y);
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}

	/***/
	public FPoint negate() {return new FPoint(-x, -y);}

	/***/
	public FPoint diff(float x, float y) {return add(-x, -y);}
	/***/
	public FPoint diff(FPoint p) {return diff(p.x, p.y);}

	/***/
	public float flyttAvstand(float x, float y) {
		return Math.abs(this.x-x) + Math.abs(this.y-y);
	}
	/***/
	public float flyttAvstand(FPoint p) {return flyttAvstand(p.x, p.y);}
	/***/
	public double abs() {return sqrt(x*x + y*y);}
	/***/
	public FPoint enhet() {return new FPoint((float)signum(x), (float)signum(y));}

	/***/
	public <T> FPoint move(T direction, T xa, T xs, T ya, T ys) {
		return move(direction, xa, xs, ya, ys, 1);
	}
	/***/
	public <T> FPoint move(T direction, T xa, T xs, T ya, T ys, float n) {
		if (xa.equals(direction))  return add(+n, 0);
		if (xs.equals(direction))  return add(-n, 0);
		if (ya.equals(direction))  return add(0, +n);
		if (ys.equals(direction))  return add(0, -n);
		return this;
	}

	public float getX() {return x;}
	public float getY() {return y;}
	public float get(Axis a) {switch (a) {
		case X: return x;
		case Y: return y;
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}

	public FPoint withX(float x) {return new FPoint(x, this.y);}
	public FPoint withY(float y) {return new FPoint(this.x, y);}
	public FPoint with(Axis a, float v) {switch (a) {
		case X: return new FPoint(v, y);
		case Y: return new FPoint(x, v);
		default: throw new AssertionError("Unhandled Axis "+a.toString());
	}}

	/***/
	public boolean equals(float x, float y) {return (this.x==x && this.y==y);}
	/***/
	public String toString() {return x+", "+y;}
	/***/
	public String toString(char o, char c) {return Helper.toString(o, this, c);}
	/***/
	public FPoint toPoint() {return new FPoint(x, y);}
	/***/
	public float[] toArray() {return new float[]{x, y};}

	private static final long serialVersionUID = 1L;
}
