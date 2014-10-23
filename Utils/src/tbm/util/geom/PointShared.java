package tbm.util.geom;
//import static tbm.util.geom.Direction.*;
//import static tbm.util.statics.*;
//import static java.lang.Math.*;
//import java.awt.geom.Point2D;
//import tbm.util.Bracket;

/**An attemt to make Point and MPoint almost interchangeable.
 * Most likely a bad idea.*/
 interface PointShared {
	int x();
	int y();
//
//	/**= |x|+|y| = Number of horizontal and vertical moves needed to reach origin.*/
//	public default int tileDistance() {return tileDistance(0, 0);}
//	/**= |this.x-x|+|this.y-y| = Number of horizontal and vertical moves needed to reach this coordinate.*/
//	public default int tileDistance(int x, int y) {return Math.abs(x()-x) + Math.abs(y()-y);}
//	/**= |x-p.x|+|y-p.y| = Number of horizontal and vertical moves needed to reach this point.*/
//	public default int tileDistance(Point p) {return tileDistance(p.x(), p.y());}
//	/**= |this| = sqrt(x^2, y^2)*/
//	public default double length() {return sqrt(x()*x() + y()*y());}
//	/**= |this-[x,y]| = sqrt((this.x-x)^2, (this.y-y)^2)*/
//	public default double length(int x, int y) {return sqrt((x()-x)*(x()-x) + (y()-y)*(y()-y));}
//	/**= |this-p| = sqrt((x-p.x)^2, (y-p.y)^2)*/
//	public default double length(Point p) {return length(p.x(), p.y());}
//	/**X->x Y->y*/
//	public default int length(Axis a) {return IPoint.p(a).x()*x() + IPoint.p(a).y()*y();}
//	/**NORTH->-y SOUTH->y WEST->-x EAST->x NONE->0,but you shouldn't pass that*/
//	public default int length(Direction d) {return IPoint.p(d).x()*x() + IPoint.p(d).y()*y();}
//
//	/**The angle relative to positive x, 
//	 *@see Math.atan2(double, double)*/
//	public default double arg() {return angle(0);}
//	/**=this.arg()-angle, in (-pi, pi]*/
//	public default double angle(double angle) {return normalize(atan2(y(), x())-angle, nextUp(-PI), PI);}
//	/**=this.arg()-p.arg(), in (-pi, pi]*/
//	public default double angle(Point p) {return angle(p.arg());}
//	/**=this.arg()-d.angle, in (-pi, pi], NONE->NaN*/
//	public default double angle(Direction d) {return angle(d.angle);}
//	//angle(int x, int y) doesn't make much sense
//
//	/**this.x*x+this.y*y = dot product*/
//	public default int dot(int x, int y) {return x()*x + y()*y;}
//	/**x*p.x+y*p.y = dot product*/
//	public default int dot(Point p) {return dot(p.x(), p.y());}
//
//	/**Return the direction of the axis with the highest absolute value; NONE if both are zero;*/
//	public default Direction direction() {
//		if (abs(x()) > abs(y()))
//			return x()>0 ? EAST : WEST;
//		if (abs(x()) < abs(y()))
//			return y()>0 ? SOUTH : NORTH;
//		return NONE;
//	}
//	/**Return the directions with the highest and lowest absolute value; NONE if zero.
//	 * (1, -2) -> [NORTH, EAST*, (-5, 5)->[WEST, SOUTH], (0, 1)->[NORTH, NONE]*/
//	public default Direction[] directions() {
//		Direction X = x()>0 ? EAST  : x()<0 ? WEST  : NONE;
//		Direction Y = y()>0 ? SOUTH : y()<0 ? NORTH : NONE;
//		if (abs(x()) >= abs(y()))
//			return new Direction[]{X, Y};
//		else
//			return new Direction[]{Y, X};
//	}
//
//	/**returns false if null*/
//	public default boolean equals(java.awt.Point p) {return p==null ? false : (p.x==x() && p.y==y());}
//	/**returns false if null*/
//	public default boolean equals(Point2D p) {return p==null ? false : (p.getX()==x() && p.getY()==y());}
//	/**returns false if null*/
//	public default boolean equals(Point p) {return p==null ? false : (p.x()==x() && p.y()==y());}
//	public default boolean equals(int x, int y) {return (x()==x && y()==y);}
//
//	/***/
//	public default String toString(char o, char c) {return Point.toString(o, this, c);}
//
//
//	/**new Point = this+[x, y] = [this.x+x, this.y+y]*/
//	public default Point plus(int x, int y) {return IPoint.p(x()+x, y()+y);}
//	/**new Point = this+p = [this.x+p.x, this.y+p.y]*/
//	public default Point plus(Point p) {return plus(p.x(), p.y());}
//	/**new Point = X->[x+v, y] Y->[x, y+v]*/
//	public default Point plus(Axis a, int v) {return plus(IPoint.p(a).x()*v, IPoint.p(a).y()*v);}
//	/**new Point = North->[x, y-1] South->[x, y+1] West->[x-1, y] East->[x+1, y]*/
//	public default Point plus(Direction d) {return plus(IPoint.p(d));}
//	/**new Point = North->[x, y-n] South->[x, y+n] West->[x-n, y] East->[x+n, y]*/
//	public default Point plus(Direction d, int n) {return plus(IPoint.p(d).x()*n, IPoint.p(d).y()*n);}
//	/**new Point = this-[x,y] = [this.x-x, this.y-y]*/
//	public default Point minus(int x, int y) {return plus(-x, -y);}
//	/**new Point = this-p = [this.x-p.x, this.y-p.y]*/
//	public default Point minus(Point p) {return minus(p.x(), p.y());}
//	/**new Point = [-x, -y]*/
//	public default Point minus() {return IPoint.p(-x(), -y());}
//	/**new Point = n*this = [x*n, y*n]*/
//	public default Point times(int n) {return IPoint.p(x()*n, y()*n);}
//	/**new Point = [x*this.x, y*this.y]*/
//	public default Point times(int x, int y) {return IPoint.p(x()*x, y()*y);}
//	/**new Point = [p.x*x, p.y*y]*/
//	public default Point times(Point p) {return times(p.x(), p.y());}
//
//	/**new Point = [sign(x), sign(y)]*/
//	public default Point sign() {return IPoint.p(Integer.signum(x()), Integer.signum(y()));}
//	/**new Point = [y, x]*/
//	public default Point inverse() {return IPoint.p(y(), x());}
//
//	/**new Point = [|this|*cos arg(this),  |this|*sin arg(this)
//	 * Exact for multiples of pi/2*/
//	public default Point rotate(Double d) {
//		if (Double.isNaN(d))
//			return this;
//		if (d%(PI/2) != 0)//non-exact is complicated, d will usually be exact
//			return IPoint.p(length(), arg()+d);
//		d = normalize(d, nextUp(-PI), PI);//-pi would be an extra case to test for.
//		return d==0?this : d==PI?IPoint.p(-x(), -y()) : d==PI/2?IPoint.p(y(), -x()) : IPoint.p(-y(), x());}
//	//rotate(Direction) would be non-obivious, just use rotate(direction.angle)
//
//	/**new Point = [x, this.y]*/
//	public default Point withX(int x) {return IPoint.p(x, y());}
//	/**new Point = [this.x, y]*/
//	public default Point withY(int y) {return IPoint.p(x(), y);}
//	/**new Point = North->[x, -v] South->[x, v] West->[-v, y] East->[v, y]*/
//	public default Point with(Direction d, int v) {return IPoint.p(x()*IPoint.p(d.axis.flip()).x()+IPoint.p(d).x()*v, y()*IPoint.p(d.axis.flip()).y()+IPoint.p(d).y()*v);}
//	/**new Point = X->[v, y] Y->[x, v]*/
//	public default Point with(Axis a, int v) {return IPoint.p(x()*IPoint.p(a.flip()).x()+IPoint.p(a).x()*v, y()*IPoint.p(a.flip()).y()+IPoint.p(a).y()*v);}
//
//	public static String toString(int x, int y) {
//		return x+", "+y;
//	}
//
//	public static boolean equals(Point a, Object b) {
//		//Cannot compare with awt.Point or Point2D, because equals(Object) should be symmetric;
//		//a.equals(b)==b.equals(a). And I cannot modify those to compare with my class. 
//		if (b == null || !(b instanceof Point))
//			return false;
//		return a.x() == ((Point)b).x()  &&  a.y() == ((Point)b).y();
//	}
//
//	public static int hashCode(int x, int y) {
//		//the same as java.awt.Point so MPoint can .equal to both
//		//http://stackoverflow.com/questions/9251961/java-is-the-point-classs-hashcode-method-any-good-or-should-i-override-it
//		long bits = Double.doubleToLongBits(x)
//	              ^ Double.doubleToLongBits(y) * 31;
//	    return (((int) bits) ^ ((int) (bits >> 32)));
//	}
//
//
//	public static IPoint parsePoint(String str) throws PointFormatException
//	{
//		str = str.replace("\t", "").replace(" ", "");
//		Bracket b = Bracket.get(str.charAt(0));
//		if (b != null)
//			if (str.indexOf(b.close) == -1) 
//				throw new PointFormatException("Point \""+str+"\": missing '"+b.close+"'.");
//			else
//				str = str.substring(1, str.indexOf(b.close)-1);
//	
//		if (str.toLowerCase().equals("null"))
//			return null;
//		String val[] = str.split(",");
//		if (val.length != 2)
//			throw new PointFormatException("too many or to few numbers." + str);
//	
//		try {
//			return IPoint.p(
//					Integer.parseInt( val[0] ),
//					Integer.parseInt( val[1] )
//				);
//		} catch (NumberFormatException e) {
//			throw new PointFormatException("Invalid number. " + e.getMessage());
//		}
//	}
//
//	/***/
//	public static String toString(char o, Point2D p, char c) {
//		Point pp  =  p==null  ?  null  :  IPoint.p(p);
//		return Point.toString(o, pp, c);
//	}
//
//	/***/
//	public static String toString(char o, Object p, char c) {
//		String str = (p==null ? "null" : p.toString());
//		if (o=='\0' && c=='\0')
//			return str;
//		return o+str+c;
//	}
//
//	/***/
//	public static String toString(Point2D p) {
//		if (p == null)
//			return "null";
//		return Point.toString((int)p.getX(), (int)p.getY());
//	}
}
