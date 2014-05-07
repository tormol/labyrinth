package tbm.util;
public class Convert {
	/* i accidentaly a symbolic intepreter
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
		public float compute(float params) {
			
		}
	}
	public static class Expr {
		public final 
	}
	
	public static LinkedList<Unit> units = new LinkedList<Unit>();

	public static unit get_unit(unit u) {
		return u;
	}
	public static unit get_unit(String name) {
		for (unit u : units)
			if (u.get_name()==name)
				return u;
		return null;
	}
	public static unit get_unit(char letter) {
		for (unit u : units)
			if (u.get_letter()==letter)
				return u;
		return null;
	}

	public static unit[] get_units(String type) {
		ArrayList<unit> l = new ArrayList<unit>();
		for (unit u : units)
			if (u.get_type().compareTo(type) == 0)
				l.add(u);
		return (unit[])l.toArray();
	}
	public static unit[] get_alternatives(unit exclude) {
		ArrayList<unit> l = new ArrayList<unit>();
		for (unit u : units)
			if (u.get_type().compareTo(exclude.get_type()) == 0  &&  !u.equals(exclude))
				l.add(u);
		return (unit[])l.toArray();
	}

	static double convert(double value, unit from, unit to) {
		if (from.get_type().compareToIgnoreCase(to.get_type()) != 0)
			return Double.NaN;//converting from length to temperature
		return to.to(from.from(value));
	}
	static double convert(double v, unit from, String to) {
		return convert(v, from, get_unit(to));
	}
	static double convert(double v, unit from, char to) {
		return convert(v, from, get_unit(to));
	}
	static double convert(double v, String from, unit to) {
		return convert(v, get_unit(from), to);
	}
	static double convert(double v, String from, String to) {
		return convert(v, get_unit(from), get_unit(to));
	}
	static double convert(double v, String from, char to) {
		return convert(v, get_unit(from), get_unit(to));
	}
	static double convert(double v, char from, unit to) {
		return convert(v, get_unit(from), to);
	}
	static double convert(double v, char from, String to) {
		return convert(v, get_unit(from), get_unit(to));
	}
	static double convert(double v, char from, char to) {
		return convert(v, get_unit(from), get_unit(to));
	}*/
}