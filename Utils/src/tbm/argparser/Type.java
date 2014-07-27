package tbm.argparser;

import java.util.regex.Pattern;

import tbm.argparser.Core.ArgException;

public interface Type<T> {
	T parse(String arg) throws ArgException;
	String help();


	public static class Int implements Type<Integer> {
		public final int min, max;
		public Int(int min, int max) {
			this.min=min;
			this.max=max;
		}
		@Override
		public Integer parse(String arg) throws ArgException {
			try {
				int n = Integer.parseInt(arg.trim());
				if (n>max || n<min)
					throw new ArgException(help());
				return n;
			} catch (NumberFormatException e) {
				throw new ArgException(help());
			}
		}
		public String help() {
			if (min==0 && max==Integer.MAX_VALUE)
				return "must be a positive integer";
			if (min==Integer.MIN_VALUE && max==Integer.MAX_VALUE)
				return "must be an integer";
			return String.format("must be an integer between %d and %d", min, max);
		}
		public static Int any = new Int(Integer.MIN_VALUE, Integer.MAX_VALUE);
		public static Int positive = new Int(0, Integer.MAX_VALUE);
		public static Int positive(int max) {
			return new Int(0, max);
		}
		public static Int range(int min, int max) {
			return new Int(min, max);
		}
	}

	public static class Str implements Type<String> {
		public final String regex;
		public Str(String regex) {
			this.regex = regex;
		}
		public String parse(String arg) throws ArgException {
			if (regex==null || arg.matches(regex))
				return arg;
			throw new ArgException("is invalid");
		}
		/**@return "" because you cant give much help from a regex*/
		public String help() {
			return "";
		}
		public static Str any = new Str(null);
		public static Str regex(String regex) {
			return new Str(regex);
		}
		public static Str list(String... valid) {
			StringBuilder regex = new StringBuilder();
			for (String option : valid)
				regex.append(Pattern.quote(option)).append('|');
			regex.deleteCharAt(/*regex.length()*/-1);
			return new Str(regex.toString());
		}
	}
}
