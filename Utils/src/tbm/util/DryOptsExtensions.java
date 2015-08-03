package tbm.util;
import tbm.util.geom.Point;
import tbm.util.parseNum;
import static tbm.util.parseNum.*;
import static tbm.util.ArgsParser.*;
/**Argument types that depend on other classes and would make ArgsParser not self-contained.
 * 
 * @author tbm
 * License Apache v3
 */
public class ArgsParserExtensions {
	public static class ExtendedInteger extends parseNum implements ArgsParser.ArgType<Long> {
		public static final int default_flags = DEC|OCT|HEX|BIN|DEFAULT(10)|SKIP_SPACE|SKIP_UNDERSCORE;

		public long min,max;
		public ExtendedInteger(long min, long max) {
			this(min, max, default_flags);
		}
		public ExtendedInteger(long min, long max, int flags) {
			super(flags);
			this.min = min;
			this.max = max;
		}
		@Override
		public Long parse(String arg) throws ArgException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}



	public static class PointRectangle implements ArgType<Point> {
		public final IntRange x_range, y_range;

		public PointRectangle(int x_start, int y_start, int x_end, int y_end) throws IllegalArgumentException {
			x_range = new IntRange(x_start, x_end);
			y_range = new IntRange(y_start, y_end);
		}

		public Point parse(String point) throws ArgException {
			if ((point.startsWith("(") && point.endsWith(")"))
			||  (point.startsWith("[") && point.endsWith("]")))
				point = point.substring(1, point.length()-1);
			String[] scalars = point.split(",");
			if (scalars.length != 2)
				throw new ArgException(" have two numbers separated by a comma");
			return Point.p(x_range.parse(scalars[0]), y_range.parse(scalars[1]));
		}
	}
}
