package no.torbmol.util;
import no.torbmol.util.geom.Point;
import static no.torbmol.util.ParseNum.*;
import java.io.IOException;
import java.io.StringReader;
import static no.torbmol.util.DryOpts.*;

/**Argument types that depend on other classes and would make DryOpts not self-contained.
 *
 * @author tbm
 * License Apache v3
 */
public class DryOptsExtensions {
	public static class ExtendedInteger implements DryOpts.ArgType<Long> {
		public static final int default_flags = OPT_DEC|OPT_HEX|OPT_BIN|SKIP_SPACE|SKIP_UNDERSCORE;

		public long min,max;
		public int flags;
		public ExtendedInteger(long min, long max) {
			this(min, max, default_flags);
		}
		public ExtendedInteger(long min, long max, int flags) {
			this.min = min;
			this.max = max;
			this.flags = flags;
		}
		@Override
		public Long parse(String arg) throws ArgException {
			try {
				return new ParseNum(flags, new StringReader(arg)).range(min, max);
			} catch (IOException e) {
				throw new IllegalStateException("BUG: internal StringReader is somehow closed.", e);
			}
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
