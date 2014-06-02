package tbm.util.geom;
import java.awt.geom.Point2D;

public class Helper {
	public static java.awt.Point parsePoint(String str) throws PointFormatException
	{
		str = str.replace("\t", "").replace(" ", "");
		char close;
		switch (str.charAt(0)) {
			case'[': close=']'; break;
			case'(': close=')'; break;
			default: throw new PointFormatException("Not a point.");
		}
		if (str.indexOf(close) == -1) 
			throw new PointFormatException("Not a point.");
		str = str.substring(1).split( String.valueOf(close) )[0];
	
		if (str.toLowerCase().equals("null"))
			return null;
		String val[] = str.split(",");
		if (val.length != 2)
			throw new PointFormatException("too many or to few numbers." + str);
	
		java.awt.Point p = new java.awt.Point();
		try {
			p.x = Integer.parseInt(val[0]);
			p.y = Integer.parseInt(val[1]);
		} catch (NumberFormatException e) {
			throw new PointFormatException("Invalid number. " + e.getMessage());
		}
		return p;
	}

	public static String toString(char o, Point2D p, char c) {
		return Helper.toString(o, new Point(p), c);
	}
	public static String toString(char o, Object p, char c) {
		String str = (p==null ? "null" : p.toString());
		if (o=='\0' && c=='\0')
			return str;
		return o+str+c;
	}
	public static String toString(Point2D p) {
		return Helper.toString(new Point(p));
	}
	public static String toString(Object p) {
		return toString('\0', p, '\0');
	}
}
