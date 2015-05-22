package tbm.argparser;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.function.Function;

import tbm.argparser.Core.ArgException;

public interface Type<T> {
	T parse(String arg) throws ArgException;
	String type_help();


	public static class Int implements Type<Long> {
		static public Function<String, Long> parser = str->Long.parseLong(str.trim());
		public final long min, max;
		public Int(long min, long max) {
			this.min=min;
			this.max=max;
		}
		
		@Override
		public Long parse(String arg) throws ArgException {
			try {
				Long n = parser.apply(arg);
				if (n>max || n<min)
					throw new ArgException(type_help());
				return n;
			} catch (NumberFormatException e) {
				throw new ArgException(type_help());
			}
		}
		@Override
		public String type_help() {
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
		public String type_help() {
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

	static public class List<T> implements Type<T> {
		protected final T[] values;
		@SafeVarargs
		public List(T... values) {
			this.values = Objects.requireNonNull(values);
		}
		@Override
		public T parse(String arg) throws ArgException {
			for (T valid : values)
				if (arg.equals(valid.toString()))
					return valid;
			throw new ArgException("Must be one of " + type_help());
		}
		@Override
		public String type_help() {
			return Arrays.toString(values);
		}
	}
	public static class Enums<E extends Enum> implements Type<E> {
		public final Class<E> _enum;
		public Enums(Class<E> _enum) {
			this._enum = _enum;
		}
		@Override
		public E parse(String arg) throws ArgException {
			try {
				return Enum.valueOf(c, string.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ArgException("Must be one of " + type_help());
			}
		}

		@Override
		public String type_help() {
			E[] consts = _enum.getEnumConstants();
			StringBuilder sb = new StringBuilder();
			return sb.toString();
		}
	}
}
