package tbm.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.List;
import java.util.regex.Pattern;
//see git commit message for TODOs and documentation.
/**
 * If you get positional arguments before all flags, some arguments might be missing as they are still attached to flags.
 *
 *Example:
 * ArgsParser a = new ArgsParser(args);
 * File file = a.optArg('f', "file", null/*hide from --help* /, null, str->new File(str);
 * boolean quiet = a.optFlag('-q', "quiet", "say less");
 * a.handle_version('v', quiet?"0.9.1":""simpleProgram version 0.9.1\nCopyright someone 2015-2016");
 * boolean help = a.optFlag('h', "help", "Display this help and quit.");
 * Integer x = a.optionalArgument("x", null, new IntRange().min(-5).max(5));
 * Integer y = a.optionalArgument("y", null, new IntRange().min(-5).max(5));
 * args = a.allArgumentss("all remaining non-integer arguments", false);//zero arguments is okay
 * if (help) {
 *     System.out.print(a.getHelp(quiet, "simpleProgram - do something"));
 *     System.exit(0);
 * }
 * a.handle_errors(-1);
 *
 *@author tbm
 * License: Apache v2
 */
public class ArgsParser {
	/**Is used by ArgType.*/@SuppressWarnings("serial")
	public static class ArgException extends Exception {
		public ArgException(String f, Object... a) {
			super(String.format(f, a));
		}
	}


	/**Convert an argument to an option or positional argument to the desired type, for example an integer or IP address.
	 *@param T type/class the argument is converted to*///@FunctionalInterface
	public interface ArgType<T> {
		 /**Convert an argument to an option or positional argument to the desired type, for example an integer or IP address.
		 *@param arg string to be converted
		 *@return the converted type 
		 *@throws ArgException if the string cannot be converted.
		 * the message will come after "must be ".
		 */
		T parse(String arg) throws ArgException;

		/**If name sent to *argument() is null, use argtype lowercase classname.*/
		static String argName(String suppliedName, ArgType<?> argType) {
			return suppliedName == null
				? argType.getClass().getName().toLowerCase()
				: suppliedName;
		}
	}



	/**Represents an args option that have not been matched yet.*/
	public class FoundOpt {
		/**A multi-character option; --option*/
		protected String longOpt = null;
		/**A single-character option; -o*/
		protected char shortOpt = '\0';
		/**The options index in args[]*/
		protected final int index;
		/**The arguments index in args[]. possible values are -1, index and index+1.*/
		protected int argument_index = -1;
		/**the argument, null==not set*/
		protected String argument = null;

		protected FoundOpt(int index) {
			this.index = index;
		}

		/**Get the string that set this option.*/
		public String setBy() {
			if (! longOpt.isEmpty())
				return "--" + longOpt;
			else if (shortOpt != '\0')
				return "-" + String.valueOf(shortOpt);
			else
				return "";
		}
		/**@return this is a single-character option; -o
		 *Returns <tt>'\0'</tt> if this is a long/multi-character option*/
		public char getShortOpt() {return shortOpt;}
		/**@return this is a multi-character option; --option
		 *Returns null if this is a short/single-character option*/
		public String getLongOpt() {return longOpt;}
		public int getIndex() {return index;}
		public String getArgument() {return argument;}
		/**Returns true if this option has an argument.*/
		public boolean hasArgument() {
			return argument != null;
		}
		/**Returns true if argument was not a part of the option
		 * -o3->false, --size 8->true, --help->false*/
		public boolean canDisownArgument() {
			return hasArgument() &&  index != argument_index;
		}
		/**Remove argument and put it back into args[]
		 *@throws if there is no argument or it's not removable.*/
		public void disownArgument() throws IllegalStateException {
			if (! canDisownArgument())
				throw new IllegalStateException(toString() + ": Cannot disown argument.");
			arguments[argument_index] = argument;
			argument = null;
			argument_index = -1;
		}

		/**Get debug information. see .setBy()*/
		public String toString() {
			String str = String.format("[%d]=%s", index, setBy());
			if (hasArgument())
				str += String.format("->[%d]=%s", argument_index, argument);
			return str;
		}
	}


	/**Registers found options and decides what should be done with it's present or missing argument.*///@FunctionalInterface
	public interface OptType {
		/**Registers found options and decides what should be done with it's present or missing argument.
		 *@throws if there is something wrong with the option, return an error message.*/
		void accept(FoundOpt option) throws ArgException;
	}



	/**for aligning descriptions in generate_help() */
	protected static class ValidOption {
		public final char shortOpt;
		public final String longOpt;
		public final String description;
		protected ValidOption(char shortOpt, String longOpt, String description) {
			this.shortOpt = shortOpt;
			this.longOpt = longOpt;
			this.description = description;
		}
	}




	public static class Builder {
		public Builder()
			{}
		public ArgsParser parse(String... args) {
			return new ArgsParser(this,  args);
		}

		protected String shortOpt_regex = "[A-Za-z]";
		/**Only characters that match regex can be short options.*/
		public Builder is_shortOpt(String shortOpt_regex) {
			this.shortOpt_regex = shortOpt_regex;
			return this;
		}
		/**Digits can be options, this makes it impossible to enter negative numbers as positional arguments.*/
		public Builder integer_shortOpts() {
			return is_shortOpt("[A-Za-Z0-9]");
		}

		protected boolean nonopt_stops_opts = false;
		/**Turns the --option in "-q command --option" into an argument.
		 * Note that you now must enter options requiring an argument as --file=path;
		 * ArgsParser doesn't know -q is a flag until it's added, and then optFlag("option") would already have returned true.*/
		public Builder stop_at_first_nonOpt() {
			nonopt_stops_opts = true;
			return this;
		}
	
		protected String name = null;
		/**for the Usage: line.*/
		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}
	}


	/**All arguments that are not an option.*/
	protected final String[] arguments;
	/**Options parsed from args[]*///LinkedList because frequent removals
	protected final LinkedList<FoundOpt> options = new LinkedList<FoundOpt>();

	/**For generating help, Can't use a StringBuilder because you need to know the longest long option to line up descriptions.*/
	protected final List<ValidOption> validOptions = new ArrayList<ValidOption>();
	/**Store help information about positional arguments.*/
	protected final StringBuilder positionalUsage = new StringBuilder();

	/**While quitting on the first error is simpler, I think collecting them and printing them at the end is more user friendly, altougt some errors might cause other errors*/
	protected final StringBuilder errors = new StringBuilder();

	/**Name of the program, for the Usage: string*/
	public final String name;

	/**@param args the String[] passed to main().
	 * @param b settings that affects argument parsing. @see Builder
	 */
	public ArgsParser(String... args) {
		this(new Builder(), args);
	}
	/**@param args the String[] passed to main().
	 * @param b settings that affects argument parsing. @see Builder
	 */
	public ArgsParser(Builder b, String... args) {
		//Builder parameters that are used later
		this.arguments = new String[args.length];

		final boolean windows = System.getProperty("os.name").startsWith("Windows");
		if (args.length==1
	   	   &&(( windows && (args[0].equals("/help") || args[0].equals("/h") || args[0].equals("/?") ))
	   	           ||      (args[0].equals("-help") || args[0].equals("-h") || args[0].equals("-?") )) )
	   	   args[0] = "--help";

		if (b.name == null) {
			b.name = System.getProperty("sun.java.command");
			if (b.name == null) {//use class name of main method, assumes being on the main thread.
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				b.name = stack[stack.length-1].getFileName().toLowerCase();
				if (b.name == null)
					b.name = "";
			}
		}
		this.name = b.name;
	
		boolean stopopt = false;
		for (int i=0; i<args.length; i++)
			if (stopopt)
				arguments[i] = args[i];
			else if (args[i].equals("--"))
				stopopt = true;
			else if (args[i].startsWith("--")) {
				int t = args[i].indexOf("=");
				FoundOpt p = new FoundOpt(i);
				if (t==-1)
					p.longOpt = args[i].substring(2);
				else {
					p.longOpt = args[i].substring(2, t);
					p.argument = args[i].substring(t+1);
					p.argument_index = i;
				}
				options.add(p);
			}
			else if ((args[i].startsWith("-")  ||  (windows && args[i].startsWith("/")))   &&   args[i].substring(1, 2).matches(b.shortOpt_regex) ) {
				FoundOpt o = null;
				for (int ii=1; ii<args[i].length(); ii++)
					if (args[i].substring(ii, ii+1).matches(b.shortOpt_regex)) {
						o = new FoundOpt(i);
						o.shortOpt = args[i].charAt(ii);
						options.add(o);
					}
					else {
						if (args[i].charAt(ii) == '=')
							ii++;
						o.argument = args[i].substring(ii);
						o.argument_index = i;
						break;
					}
			}
			//if the last option didn't have an argument, add this.
			//see Builder.stop_at_first_nonopt() for why nonopt_stops_opts forces option arguments to be 
			else if (!options.isEmpty()  &&  !options.getLast().hasArgument()  &&  !b.nonopt_stops_opts) {
				options.getLast().argument = args[i];
				options.getLast().argument_index = i;
			}
			else {
				arguments[i] = args[i];
				if (b.nonopt_stops_opts)
					stopopt = true;
			}
	}



	/**Check for an option and store it for help messages.
	 *@param shortOpt single-character version, -o
	 *@param longOpt multi-character version, --option
	 *@param desc if not null option is stored and will be printed as a part of --help output
	 *@param handler converts string to wanted type.
	 *@return {@code handler}*/
	public <OT extends OptType> OT option(char shortOpt, String longOpt, String desc, OT handler) {
		//cannot remove in a foreach
		Iterator<FoundOpt> itr = options.iterator();
		while (itr.hasNext()) {
			FoundOpt opt = itr.next();
			if (longOpt != null  &&  longOpt.equals(opt.longOpt)
			 || shortOpt !='\0'  &&  shortOpt == opt.shortOpt)
				try {
					itr.remove();
					handler.accept(opt);
				} catch (ArgException ae) {
					errors.append(opt.setBy()).append(' ').append(ae.getMessage()).append('\n');
				}
		}
		if (desc != null)
			validOptions.add(new ValidOption(shortOpt, longOpt, desc));
		return handler;
	}





  ////////////////////////////////////
 //error, help and version handling//
////////////////////////////////////
	/**Are there any errors?*/
	public boolean hasErrors() {
		return getErrors().length() > 0;
	}


	/**Return a String with all errors.*/
	public String getErrors() {
		//use a new StringBuilder so getErrors() can be called multiple times
		StringBuilder errors = new StringBuilder(this.errors);
		//invalid options
		for (FoundOpt o : options)
			errors.append("Invalid option ").append(o.setBy()).append('.').append('\n');
		//unused arguments
		if (argumentsLeft() > 0)
			errors.append("Unused arguments: " + Arrays.toString(allArguments(null, false)));
		return errors.toString();
	}


	/**If there are any errors, print them and exit.
	 * @param errorCode is passed to System.exit()*/
	public void handle_errors(int errorCode) {
		String errors = getErrors();
		if ( !errors.isEmpty()) {
			System.err.println(errors);
			System.err.println(getUsage());
			System.exit(errorCode);
		}
	}



	/**Returns a Usage: string.
	 * Ends with a newline.*/
	public String getUsage() {//I could rename positionalUsage and add name in the constructor, but that might change. 
		return "Usage " + name + " [ options ... ] " + positionalUsage.toString() + '\n';
	}


	/**Return a String with a list of all options and their description.
	 * Ends with a newline if not empty.
	 * You probably want to prepend getUsage()\n*/
	public String getOptionHelp() {
		int longest_longOpt = 0;
		for (ValidOption o : validOptions)
			if (o.description != null  &&  o.longOpt != null  &&  o.longOpt.length() > longest_longOpt)
				longest_longOpt = o.longOpt.length();

		StringBuilder help = new StringBuilder();
		for (ValidOption o : validOptions)
			if (o.description != null) {
				//short
				help.append('\t');
				if (o.shortOpt != '\0')
					help.append('-').append(o.shortOpt);
				else
					help.append(' ').append(' ');

				//long
				int spaces_after = longest_longOpt;
				help.append('\t');
				if (o.longOpt != null) {
					help.append("--").append(o.longOpt);
					spaces_after -= o.longOpt.length();
				} else
					spaces_after += 2;
				while (spaces_after-- > 0)
					help.append('\n');

				//description
				String[] descLines = o.description.split("\n");
				help.append('\t').append(descLines[0]).append('\n');
				for (int l=1; l<descLines.length; l++) {
					help.append("\t  \t  ");
					for (int s=0; s<longest_longOpt; s++)
						help.append('\n');
					help.append(descLines[l]).append('\n');
				}
			}

		return help.toString();
	}


	/**generate --help output as "$help\n${getUsage()}"Options:\n${getOptionHelp()}"
	 *Ends with a newline.
	 *Should be called after checking for positional arguments!
	 *@param only_usage is --quiet set?
	 *@param help text to be displayed before usage and option help, should end with a newline.*/
	public String getHelp(boolean only_usage, String help) {
		if (only_usage)
			return getUsage();
		return help + '\n' + getUsage() + "Options:\n" + getOptionHelp();
	}

	//handle_help() would encourage not waiting for positional arguments to get added to usage. 



	/**If --version is set, print version_info and exit(0).
	 *@param shortOpt should probably be 'v' or '\0'
	 *If there is a -q--quiet flag, you should only show the version number if it was set.*/
	public void handle_version(char shortOpt, String version_info) {
		if (optFlag(shortOpt, "version", "Display version information and quit.")) {
			System.out.println(version_info);
			System.exit(0);
		}
	}


	/**Add options -v--verbose -q--quiet and -s--silent.
	 * if silent is last -2 is returned,
	 * else volume = times verbose - 1 if quiet or silent was set.*/
	public int volume(String verbose_description, String quiet_description, String silent_description) {
		Flag verbose = option('v', "verbose", verbose_description, new Flag());
		Flag quiet = option('q', "quiet", quiet_description, new Flag());
		Flag silent = option('s', "silent", silent_description, new Flag());
		if (silent.last_index > verbose.last_index
		&&  silent.last_index > quiet.last_index)
			return -2;
		return verbose.times
				- (quiet.isSet() || silent.isSet() ? 1 : 0);
	}





  ////////////////////////
 //positional arguments//
////////////////////////
	public <T> T optionalArgument(String name, T missing, ArgType<T> type) {
		try {
			positionalUsage.append(' ').append('[').append(ArgType.argName(name,  type)).append(']');
			for (int i=0; i<arguments.length; i++)
				if (arguments[i] != null) {
					T arg = type.parse(arguments[i]);
					arguments[i] = null;//only remove if valid
					return arg;
				}
		} catch (ArgException ae)
			{}//string has wrong type and is for a later positional argument
		return missing;
	}


	/**A required positional argument
	 *@param name for the Usage: line.*/
	public <T> T argument(String name, ArgType<T> type) {
		positionalUsage.append(' ').append(ArgType.argName(name,  type));
		for (int i=0; i<arguments.length; i++)
			if (arguments[i] != null)
				try {
					return type.parse(arguments[i]);
				} catch (ArgException ae) {
					if (name == null)
						name = type.getClass().getName().toLowerCase();
					errors.append(String.format("%s is not a valid %s, must be %s\n", arguments[i], name, ae.getMessage()));
					return null;
				} finally {
					arguments[i] = null;
				}
		errors.append(name).append(" is missing\n");
		return null;
	}


	/**Get a list of all remaining non-opt arguments.
	 *@param name for the Usage: string.
	 *@param minimum_one require at least one argument.*/
	public <T> List<T> allArguments(String name, boolean minimum_one, ArgType<T> type) {
		name = ArgType.argName(name, type);
		positionalUsage.append(' ');
		if (minimum_one)
			positionalUsage.append(name).append(" [");
		else
			positionalUsage.append('[').append(name);
		positionalUsage.append("...]");

		ArrayList<T> list = new ArrayList<T>(arguments.length);
		for (int i=0; i<arguments.length; i++)
			if (arguments[i] != null)
				try {
					list.add(type.parse(arguments[i]));
					arguments[i] = null;
				} catch (ArgException ae) {
					errors.append(arguments[i]).append(" is not a valid ").append(name).append(", must be ").append(ae.getMessage()).append('\n');
				}
		if (list.isEmpty() && minimum_one)
			errors.append("must at least have one ").append(name).append('\n');
		return list;
	}


	/**Get a list of all remaining non-opt arguments.
	 *@param name for the Usage: string.
	 *@param minimum_one require at least one argument.
	 *@return array so you can do {@code args = allArguments();}*/
	public String[] allArguments(String name, boolean minimum_one) {
		List<String> list = allArguments(name, minimum_one, any);
		return list.toArray(new String[list.size()]);
	}


	protected int argumentsLeft() {
		int n = 0;
		for (String arg : arguments)
			if (arg != null)
				n++;
		return n;
	}





  ///////////////////////////////////////
 //OptType implementations and methods//
///////////////////////////////////////
	/**An option type that forgets the previous occurrence when it gets a new one.*/
	public static class Single<V> implements OptType {
		public V value;
		public final V noArg_value;
		public final ArgType<V> type;
		public Single(V notSet, ArgType<V> type) {
			value = notSet;
			noArg_value = null;
			this.type = type;
		}
		public Single(V notSet, V noArg, ArgType<V> type) {
			value = notSet;
			noArg_value = Objects.requireNonNull(noArg);
			this.type = type;
		}
		public void accept(FoundOpt o) throws ArgException {
			if (o.hasArgument())
				if (noArg_value != null  &&  o.canDisownArgument()) {
					o.disownArgument();
					value = noArg_value;
				} else
					try {
						value = type.parse(o.argument);
					} catch (ArgException ae) {
						throw new ArgException("%s has invalid argument \"%s\"; must %s.\n", o.setBy(), o.argument, ae.getMessage());
					}
			else if (noArg_value == null)
				throw new ArgException("require an argument");
			value = noArg_value;
		}
	}

	public <T> T optArg(char shortOpt, String longOpt, String description, T notSet, ArgType<T> type) {
		return option(shortOpt, longOpt, description, new Single<T>(notSet, type)).value;
	}

	public <T> T optArg(char shortOpt, String longOpt, String description, T notSet, T noArg, ArgType<T> type) {
		return option(shortOpt, longOpt, description, new Single<T>(notSet, noArg, type)).value;
	}



	/**An option type that stores every argument and require one.*/
	public static class MultiArg<V> implements OptType {
		public final List<V> values = new ArrayList<V>(3);
		public final ArgType<V> type;
		public MultiArg(ArgType<V> type) {
			this.type = Objects.requireNonNull(type);
		}
		public void accept(FoundOpt o) throws ArgException {
			if ( !o.hasArgument())
				throw new ArgException("%s (argument nr %d) require an argument.\n", o.setBy(), o.index+1);
			try {
				values.add(type.parse(o.argument));
			} catch (ArgException ae) {
				throw new ArgException("%d. %s has invalid argument \"%s\"; must %s.\n", values.size()+1, o.setBy(), o.argument, ae.getMessage());
			}
		}
	}

	public <T> List<T> optMultiArg(char shortOpt, String longOpt, String description, ArgType<T> type) {
		return option(shortOpt, longOpt, description, new MultiArg<T>(type)).values;
	}



	/**An option type that takes no arguments and gives an error if it cannot be removed.*/
	public static class Flag implements OptType {
		public int times = 0;
		public int last_index = -1;
		public Flag()
			{}
		public void accept(FoundOpt o) throws ArgException {
			if (o.hasArgument())
				if (o.canDisownArgument())
					o.disownArgument();
				else
					throw new ArgException("is a flag and cannot have an argument.");
			times++;
			last_index = o.index;
		}
		public boolean isSet() {
			return times > 0;
		}
	}

	
	/**A shared object since flags are pretty reusable.*/
	protected final Flag optFlag = new Flag();
	public int optFlagN(char shortOpt, String longOpt, String description) {
		optFlag.times = 0;
		return option(shortOpt, longOpt, description, optFlag).times;
	}

	public boolean optFlag(char shortOpt, String longOpt, String description) {
		optFlag.times = 0;
		return option(shortOpt, longOpt, description, optFlag).isSet();
	}





  ///////////////////////////////////////
 //ArgType implementations and methods//
///////////////////////////////////////
	//not static to not stay in memory permanently
	//don't require java 8
	/**An ArgType that passes the argument through.*/
	public final ArgType<String> any = new ArgType<String>() {
		/**@return argument as-is.*/
		public String parse(String arg) {return arg;}
	};

	/**@return null if not set.*/
	public String optStr(char shortOpt, String longOpt, String description) {
		return optArg(shortOpt, longOpt, description, null, any);
	}


	/**argument must match a regex.*/
	public static class Regex implements ArgType<String> {
		public final Pattern regex;
		public Regex(Pattern regex) {
			this.regex = Objects.requireNonNull(regex);
		}
		public Regex(String regex) {
			this.regex = Pattern.compile(regex);
		}
		public Regex(String regex, int flags) {
			this.regex = Pattern.compile(regex, flags);
		}
		public String parse(String string) throws ArgException {
			 if (! regex.matcher(string).matches())
				 throw new ArgException("doesn't match the regular expression "+regex);
			 return string;
		}
	}


	/**For arguments that must be an integer*/
	public static class IntRange implements ArgType<Long> {
		public final long min, max;
		/**@throws IllegalArgumentException if min >= max*/
		public IntRange(long min, long max) throws IllegalArgumentException {
			if (min >= max)
				throw new IllegalArgumentException("min >= max");
			this.min = min;
			this.max = max;
		}
		public Long parse(String number) throws ArgException {
			try {
				long num = Long.parseLong(number.replace(" ", "").replace("_", ""));
				if (num >= min  &&  num <= max)
					return num;
				if (num < 0  &&  min >= 0)
					throw new ArgException("must be positive");
			} catch (NumberFormatException nfe)
				{}//continue from here
			StringBuilder sb = new StringBuilder("must be an integer");
			if (min != Long.MIN_VALUE)
				sb.append(" bigger than ").append(min-1);
			if (max != Long.MAX_VALUE) {
				if (min != Long.MIN_VALUE)
					sb.append(" and");
				sb.append(" smaller than ").append(max+1);
			}
			throw new ArgException(sb.toString());
		}
	}

	public int optInt(char shortOpt, String longOpt, String description, int notSet, int min, int max) {
		return optArg(shortOpt, longOpt, description, Long.valueOf(notSet), new IntRange(min, max)).intValue();
	}


	/**argument must be one of the given values.*/
	public static class Set<T> implements ArgType<T> {
		/**Converts arguments to the desired type.*/
		public final ArgType<T> converter; 
		/**An argument must be equal to one of theese.*/
		protected final Object[] values;

		/**@param converter Converts arguments to the desired type.
		  *@param values if it's not in this list it's an invalid argument.
		  *@throws IllegalArgumentException if values.length < 2
		  */@SafeVarargs
		public Set(ArgType<T> converter, T... values) throws IllegalArgumentException {
			this.converter = Objects.requireNonNull(converter);
			this.values = Objects.requireNonNull(values);
			if (values.length < 2)
				throw new IllegalArgumentException("A set with less than two values doesn't make sense.");
		}

		@SuppressWarnings("unchecked")//completely safe
		public T parse(String option) throws ArgException {
			T converted = converter.parse(option);
			for (Object v : values)
				if (converted.equals(v))
					return (T)v;
			throw new ArgException("must be one of: "+values("or"));
		}

		/**Build a String with all the vallid values separated by ", ", except the last one which is separated by "$lastGlue ".
		 * Similar to Arrays.toString(array) but without the [ and ].
		 *@param lastGlue used to separate the next last element from the last one, words should start with a space but not end with one.
		 *@return "a, b, c$lastGlue d"*/
		public String values(String lastGlue) {
			StringBuilder list = new StringBuilder();
			list.append(values[0]);
			for (int i=1; i<values.length-1; i++)
				list.append(',').append(' ').append(values[i]);
			list.append(lastGlue).append(' ').append(values[values.length-1]);
			return list.toString();
		}

		public String toString() {
			return values(" and");
		}
	}

	/**Create a set where all values and arguments are trimmed and converted to lowercase before being compared*/
	public static Set<String> StringSetIgnoreCase(String... values) {
		for (int i=0; i<values.length; i++)
			values[i] = values[i].toLowerCase();
		return new Set<String>(new ArgType<String>(){public String parse(String arg) {
			return arg.trim().toLowerCase();
		}}, values);
	}



	public final ArgType<InputStream> inputFile  = new ArgType<InputStream>() {public InputStream parse(String path) throws ArgException {
		if (path.equals("-"))
			return System.in;
		try {
			return new FileInputStream(path);
		} catch (IOException ioe) {
			File file = new File(path);
			if ( !file.exists())
				throw new ArgException("doesn't exist");
			if ( !file.isFile())
				throw new ArgException("is not a file");
			if ( !file.canRead())
				throw new ArgException("is unreadable");
			throw new ArgException(ioe.getMessage());
		}
	}};
	


	public static class OutputFile implements ArgType<OutputStream> {
		protected boolean append = false;
		public OutputFile append() {
			return append(true);
		}
		public OutputFile append(boolean append) {
			this.append = append;
			return this;
		}
		public OutputStream parse(String path) throws ArgException {
			if (path.equals("-"))
				return System.out;
			try {
				return new FileOutputStream(path, append);
			} catch (FileNotFoundException fnfe) {
				String error = fnfe.getMessage();
				File file = new File(path);
				if ( !file.exists())
					error = "cannot create file";
				else if ( !file.isFile())
					error = "not a file";
				else if ( !file.canWrite())
					error = "cannot write to file";
				throw new ArgException(error);
			}
		}
	}
	/**Add an option that require a path argument and open it for writing.
	 *@return null if not set or System.out if "-".
	 *@param append append to file instead of overwriting.*/
	public OutputStream optOutputFile(char shortOpt, String longOpt, String description, boolean append) {
		return optArg(shortOpt, longOpt, description, null, new OutputFile().append(append));
	}
}
