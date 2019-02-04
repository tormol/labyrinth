/* Copyright 2019 Torbjørn Birch Moltu
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or
 * http://apache.org/licenses/LICENSE-2.0> or the MIT license <LICENSE-MIT or
 * http://opensource.org/licenses/MIT>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package no.torbmol.util.geom;
import static no.torbmol.util.statics.*;
import static no.torbmol.util.geom.Direction.*;
import static java.lang.Math.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import no.torbmol.util.Bracket;

/**An immutable alternative to java.awt.Point.
 * Also has many more functions.
 * No methods accept null
 * Use static constructor wrappers, I might add object caching
 * The class isn't final, but don't make it mutable*/
public class Point implements Serializable {
	/**the horizontal axis, positive is right*/
	public final int x;
	/**the vertical  axis, positive is down*/
	public final int y;

	protected Point(int x, int y) {this.x=x; this.y=y;}

	/**new Point = this+[x, y] = [this.x+x, this.y+y]*/
	public Point plus(int x, int y) {return p(this.x+x, this.y+y);}
	/**new Point = this+p = [this.x+p.x, this.y+p.y]*/
	final public Point plus(Point p) {return plus(p.x, p.y);}
	/**new Point = X->[x+v, y] Y->[x, y+v]*/
	public Point plus(Axis a, int v) {return plus(p(a).x*v, p(a).y*v);}
	/**new Point = North->[x, y-1] South->[x, y+1] West->[x-1, y] East->[x+1, y]*/
	public Point plus(Direction d) {return plus(p(d));}
	/**new Point = North->[x, y-n] South->[x, y+n] West->[x-n, y] East->[x+n, y]*/
	public Point plus(Direction d, int n) {return plus(p(d).x*n, p(d).y*n);}
	/**new Point = this-[x,y] = [this.x-x, this.y-y]*/
	final public Point minus(int x, int y) {return plus(-x, -y);}
	/**new Point = this-p = [this.x-p.x, this.y-p.y]*/
	final public Point minus(Point p) {return minus(p.x, p.y);}
	/**new Point = [-x, -y]*/
	public Point minus() {return p(-x, -y);}
	/**new Point = n*this = [x*n, y*n]*/
	public Point times(int n) {return p(x*n, y*n);}
	/**new Point = [x*this.x, y*this.y]*/
	public Point times(int x, int y) {return p(this.x*x, this.y*y);}
	/**new Point = [p.x*x, p.y*y]*/
	final public Point times(Point p) {return times(p.x, p.y);}

	/**new Point = [sign(x), sign(y)]*/
	public Point sign() {return p(Integer.signum(x), Integer.signum(y));}
	/**new Point = [y, x]*/
	public Point inverse() {return p(y, x);}

	/**= |x|+|y| = Number of horizontal and vertical moves needed to reach origin.*/
	final public int tileDistance() {return tileDistance(0, 0);}
	/**= |this.x-x|+|this.y-y| = Number of horizontal and vertical moves needed to reach this coordinate.*/
	public int tileDistance(int x, int y) {return Math.abs(this.x-x) + Math.abs(this.y-y);}
	/**= |x-p.x|+|y-p.y| = Number of horizontal and vertical moves needed to reach this point.*/
	final public int tileDistance(Point p) {return tileDistance(p.x, p.y);}
	/**= |this| = sqrt(x^2, y^2)*/
	public double length() {return sqrt(x*x + y*y);}
	/**= |this-[x,y]| = sqrt((this.x-x)^2, (this.y-y)^2)*/
	public double length(int x, int y) {return sqrt((this.x-x)*(this.x-x) + (this.y-y)*(this.y-y));}
	/**= |this-p| = sqrt((x-p.x)^2, (y-p.y)^2)*/
	public double length(Point p) {return length(p.x, p.y);}
	/**X->x Y->y*/
	public int length(Axis a) {return p(a).x*x + p(a).y*y;}
	/**NORTH->-y SOUTH->y WEST->-x EAST->x NONE->0,but you shouldn't pass that*/
	public int length(Direction d) {return p(d).x*x + p(d).y*y;}

	/**The angle of the point, relative to positive x
	 *@see Math.atan2(double, double)*/
	public double arg() {return atan2(y, x);}
	/**=this.arg()-angle, in (-pi, pi]*/
	public double angle(double angle) {return normalize(atan2(y, x)-angle, nextUp(-PI), PI);}
	/**=this.arg()-p.arg(), in (-pi, pi]*/
	final public double angle(Point p) {return angle(p.arg());}
	/**=this.arg()-d.angle, in (-pi, pi], NONE->NaN*/
	public double angle(Direction d) {return angle(d.angle);}
	//angle(int x, int y) doesn't make much sense

	/**new Point = [|this|*cos arg(this),  |this|*sin arg(this)
	 * Exact for multiples of pi/2*/
	public Point rotate(Double d) {
		if (Double.isNaN(d))
			return this;
		if (d%(PI/2) != 0)//non-exact is complicated, d will usually be exact
			return p(length(), arg()+d);
		d = normalize(d, nextUp(-PI), PI);//-pi would be an extra case to test for.
		return d==0?this : d==PI?p(-x, -y) : d==PI/2?p(y, -x) : p(-y, x);}
	//rotate(Direction) would be non-obivious, just use rotate(direction.angle)

	/**this.x*x+this.y*y = dot product*/
	public int dot(int x, int y) {return this.x*x + this.y*y;}
	/**x*p.x+y*p.y = dot product*/
	public int dot(Point p) {return dot(p.x, p.y);}

	//If you want to write a little more.
	public int getX() {return x;}
	public int getY() {return y;}
	/**Return the direction of the axis with the highest absolute value; NONE if both are zero;*/
	public Direction direction() {
		if (abs(x) > abs(y))
			return x>0 ? EAST : WEST;
		if (abs(x) < abs(y))
			return y>0 ? SOUTH : NORTH;
		return NONE;
	}
	/**Return the directions with the highest and lowest absolute value; NONE if zero.
	 * (1, -2) -> [NORTH, EAST*, (-5, 5)->[WEST, SOUTH], (0, 1)->[NORTH, NONE]*/
	public Direction[] directions() {
		Direction X = x>0 ? EAST  : x<0 ? WEST  : NONE;
		Direction Y = y>0 ? SOUTH : y<0 ? NORTH : NONE;
		if (abs(x) >= abs(y))
			return new Direction[]{X, Y};
		else
			return new Direction[]{Y, X};
	}

	/**new Point = [x, this.y]*/
	public Point withX(int x) {return p(x, this.y);}
	/**new Point = [this.x, y]*/
	public Point withY(int y) {return p(this.x, y);}
	/**new Point = North->[x, -v] South->[x, v] West->[-v, y] East->[v, y]*/
	public Point with(Direction d, int v) {return p(x*p(d.axis.flip()).x+p(d).x*v, y*p(d.axis.flip()).y+p(d).y*v);}
	/**new Point = X->[v, y] Y->[x, v]*/
	public Point with(Axis a, int v) {return p(x*p(a.flip()).x+p(a).x*v, y*p(a.flip()).y+p(a).y*v);}
	/**new Point = sqrt(x^2+y^2)[cos(arg), sin(arg)]*/
	public Point with_arg(double arg) {return rotate(arg-arg());}
	/**new Point = length[cos(arg), sin(arg)] = length/length() [x, y]*/
	public Point with_length(double length) {return p(round(x*length/length()), round(y*length/length()));}

	/**returns false if null*/
	public boolean equals(java.awt.Point p) {return p==null ? false : (p.x==x && p.y==y);}
	/**returns false if null*/
	public boolean equals(Point2D p) {return p==null ? false : (p.getX()==x && p.getY()==y);}
	/**returns false if null*/
	public boolean equals(Point p) {return p==null ? false : (p.x==x && p.y==y);}
	public boolean equals(int x, int y) {return (this.x==x && this.y==y);}
	/**returns false if null or not instance of this class*/@Override
	public boolean equals(Object p) {
		//Cannot compare with awt.Point or Point2D, because equals(Object) should be symmetric;
		//a.equals(b)==b.equals(a). And I cannot modify those to compare with my class.
		if (!(p instanceof Point))
			return false;
		return (((Point)p).x==x && ((Point)p).y==y);
	}
	/**@return y*/
	public int hashCode() {return y;}
	
	/**=x+", "+y*/
	public String toString() {return x+", "+y;}
	/**=before+x+", "+y+after*/
	public String toString(char before, char after) {return before+x+", "+y+after;}
	/**={x, y}*/
	public int[] toArray() {return new int[]{x, y};}

	/**Create a new Point, or return a cached one.*/
	public static Point p(int x, int y) {
		if (x==0) switch (y) {
			case  0: return origin;
			case +1: return south;
			case -1: return north;
		} else if (y==0) switch (x) {
			case -1: return west;
			case +1: return east;
		}
		return new Point(x, y);
	}
	/**@throws NullPointerException if null
	 **@return the parameter if instance of Point, else new*/
	public static Point p(Point p) {
		if (p==null)
			throw new NullPointerException();
		if (p instanceof Point)
			return (Point)p;
		return p(p.x, p.y);
	}
	/**Create a new Point
	 *@throws NullPointerException if null*/
	public static Point p(Point2D p) {return p((int)p.getX(), (int)p.getY());}
	/**Create a new Point
	 *@throws NullPointerException if null*/
	public static Point p(java.awt.Point p) {return p(p.x, p.y);}
	/**Create a point from the string
	 *@throws NullPointerException if null
	 *@return null if the body is "null"*/
	public static Point p(String str) throws PointFormatException {
		str = str.replace("\t", "").replace(" ", "");
		Bracket b = Bracket.get(str.charAt(0));
		if (b != null)
			if (str.indexOf(b.close) == -1)
				throw new PointFormatException("Point \""+str+"\": missing '"+b.close+"'.");
			else
				str = str.substring(1, str.indexOf(b.close)-1);

		if (str.toLowerCase().equals("null"))
			return null;
		String val[] = str.split(",");
		if (val.length != 2)
			throw new PointFormatException("too "+ (val.length>2 ?"many":"few") +" numbers in " + str);
		int x, y;
		try {x=Integer.parseInt( val[0] );
		} catch (NumberFormatException e) {
			throw new PointFormatException("Invalid number for x; " + e.getMessage());}
		try {y = Integer.parseInt( val[1] );
		} catch (NumberFormatException e) {
			throw new PointFormatException("Invalid number for y; " + e.getMessage());}
		return p(x, y);
	}
	/**Return the unit vector of the axis.
	 *@throws NullPointerException if null*/
	public static Point p(Axis a) {switch (a) {
		case  X:	return south;
		case  Y:	return  west;
		default:	throw new AssertionError("Unhandled Axis "+a.toString());
	}}
	/**Return the unit vector of the direction.
	 *@throws NullPointerException if null*/
	public static Point p(Direction d) {switch (d) {
		case NORTH: return  north;
		case SOUTH:	return  south;
		case  WEST:	return   west;
		case  EAST:	return   east;
		case  NONE: return origin;
		default:	throw new AssertionError("Unhandled Direction "+d.toString());
	}}
	/**Round numbers to the nearest integer*/
	public static Point p(double length, double argument) {
		double x = length*cos(argument);
		double y = length*sin(argument);
		return p(iround(x), iround(y));
	}

	static final Point origin= new Point( 0,  0);
	static final Point north = new Point( 0, -1);
	static final Point south = new Point( 0, +1);
	static final Point east  = new Point(+1,  0);
	static final Point west  = new Point(-1,  0);
	private static final long serialVersionUID = 1L;
}
