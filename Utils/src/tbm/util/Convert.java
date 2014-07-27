package tbm.util;
import java.util.ArrayList;
import java.util.List;

//I accidentally a symbolic interpreter
public class Convert {
	public static class Measurement {
		public final String name;
		public final char letter;
		public Unit baseUnit = null;
		public Measurement(String name, char letter, Unit Default) {
			if (name==null)
				throw new IllegalArgumentException("name is null");
			this.name = name;
			this.letter = letter;
		}
	}
	public static abstract class Unit {
		public final String name;
		public final String postfix;
		public final Measurement type;
		abstract public double to(double in);
		abstract public double from(double out);
		protected Unit(Measurement type, String name, String postfix) {
			this.type = type;
			this.name = name;
			this.postfix = postfix;
		}
		public String getName() {
			return name;
		}
		public char getLetter() {
			return name.charAt(0);
		}
		public String getType() {
			return type.name;
		}
	}
	public static interface Function {
		public double f(double[] params); 
	}
	public static class Relationship {
		
	}
	public static class Operator {
		public final String name;
		public final String operator;
		public final int parameters;
		protected final Function f;
		public Operator(String name, String operator, int parameters, Function f) {
			this.name = name;
			this.operator = operator;
			this.parameters = parameters;
			this.f = f;
		}
		//param can be double, or expression
		public void/*float*/ compute(float params) {
			
		}
	}
	public static class Expr {
		
	}
	
	public static List<Unit> units = new ArrayList<>();

	public static Unit get_unit(Unit u) {
		return u;
	}
	public static Unit get_unit(String name) {
		for (Unit u : units)
			if (u.getName()==name)
				return u;
		return null;
	}
	public static Unit get_unit(char letter) {
		for (Unit u : units)
			if (u.getLetter()==letter)
				return u;
		return null;
	}

	public static Unit[] get_units(String type) {
		List<Unit> l = new ArrayList<>();
		for (Unit u : units)
			if (u.getType().compareTo(type) == 0)
				l.add(u);
		return (Unit[])l.toArray();
	}
	public static Unit[] get_alternatives(Unit exclude) {
		List<Unit> l = new ArrayList<>();
		for (Unit u : units)
			if (u.getType().compareTo(exclude.getType()) == 0  &&  !u.equals(exclude))
				l.add(u);
		return (Unit[])l.toArray();
	}

	static double convert(double value, Unit from, Unit to) {
		if (from.getType().compareToIgnoreCase(to.getType()) != 0)
			return Double.NaN;//converting from length to temperature
		return to.to(from.from(value));
	}
	static double convert(double v, Unit from, String to) {
		return convert(v, from, get_unit(to));
	}
	static double convert(double v, Unit from, char to) {
		return convert(v, from, get_unit(to));
	}
	static double convert(double v, String from, Unit to) {
		return convert(v, get_unit(from), to);
	}
	static double convert(double v, String from, String to) {
		return convert(v, get_unit(from), get_unit(to));
	}
	static double convert(double v, String from, char to) {
		return convert(v, get_unit(from), get_unit(to));
	}
	static double convert(double v, char from, Unit to) {
		return convert(v, get_unit(from), to);
	}
	static double convert(double v, char from, String to) {
		return convert(v, get_unit(from), get_unit(to));
	}
	static double convert(double v, char from, char to) {
		return convert(v, get_unit(from), get_unit(to));
	}
}