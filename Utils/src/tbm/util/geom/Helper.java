package tbm.util.geom;
import java.awt.geom.Point2D;
import tbm.util.Bracket;
/***/
public class Helper {
	/***/
	public static Point parsePoint(String str) throws PointFormatException
	{
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
			throw new PointFormatException("too many or to few numbers." + str);
	
		try {
			return new Point(
					Integer.parseInt( val[0] ),
					Integer.parseInt( val[1] )
				);
		} catch (NumberFormatException e) {
			throw new PointFormatException("Invalid number. " + e.getMessage());
		}
	}

	/***/
	public static String toString(char o, Point2D p, char c) {
		Point pp  =  p==null  ?  null  :  new Point(p);
		return Helper.toString(o, pp, c);
	}

	/***/
	public static String toString(char o, Object p, char c) {
		String str = (p==null ? "null" : p.toString());
		if (o=='\0' && c=='\0')
			return str;
		return o+str+c;
	}

	/***/
	public static String toString(Point2D p) {
		if (p == null)
			return "null";
		return new Point(p).toString();
	}

	@Deprecated/**use p.toString()*/
	public static String toString(Object p) {
		return toString('\0', p, '\0');
	}
}
