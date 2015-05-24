package tbm.util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.regex.Pattern;
//see git commit message for TODOs and documentation.
/**
 * new parser(args)
 * String file = parser.getopt("file", "blah").argument();
 * boolean help = parser.getopt("help", 'h', "this").flag();
 * if (help)
 *     System.out.println(parser.getHelp());
 * List<String> args = parser.getargs();
 * List<String> wrongops = 
 * parser.wrongopts
 * 
 */
public class ArgsParser {

	@SuppressWarnings("serial")
	public static class ArgException extends Exception {
		public ArgException(String str) {
			super(str);
		}
		public ArgException(String f, Object... a) {
			super(String.format(f, a));
		}
	}


	/****created from args[]**/
	/**Represents an args option that have not been matched yet.*/
	public class FoundOpt implements Comparable<FoundOpt> {
		/**A multi-character option; --option*/
		protected String Long = null;
		/**A single-character option; -o*/
		protected char Short = '\0';
		/**The options index in args[]*/
		protected final int index;
		/***/
		protected int argument_index = -1;
		/**the argument, null==not set*/
		protected String argument = null;

		protected FoundOpt(int index) {
			this.index = index;
		}

		/**Return the string that set this option*/
		public String setBy() {
			if (! Long.isEmpty())
				return "--" + Long;
			else if (Short != '\0')
				return "-" + String.valueOf(Short);
			else
				return null;
		}
		/**@return this is a single-character option; -o
		 *Returns <tt>'\0'</tt> if this is a long/multi-character option*/
		public char getShort() {return Short;}
		/**@return this is a multi-character option; --option
		 *Returns null if this is a short/single-character option*/
		public String getLong() {return Long;}
		public int getIndex() {return index;}
		public String getArgument() {return argument;}
		/**Returns true if this option has an argument.*/
		public boolean hasArgument() {
			return argument != null;
		}
		public void disownArgument() throws UnsupportedOperationException {
			if (! canDisownArgument())
				throw new UnsupportedOperationException(toString() + ": Cannot disown argument.");
			arguments[argument_index] = argument;
			argument = null;
			argument_index = -1;
		}
		/**Returns true if argument was a part of the option
		 * eg "-o3"->true, "--size 8"->false, "--help"->false 
		 */
		public boolean canDisownArgument() {
			return argument != null  &&  index != argument_index;
		}
		
		/**Return debug information. see .setBy()*/
		public String toString() {
			String str = String.format("[%d]=%s", index, setBy());
			if (hasArgument())
				str += String.format("->[%d]=%s", argument_index, argument);
			return str;
		}
		//@Override Comparable (1.5 doesn't allow @Override on interfaces)
		public int compareTo(FoundOpt fo) {
			return this.index-fo.index;
		}
	}


	/**@param T type/class the argument is converted to*/
	public interface ArgType<T> {
		T parse(String arg) throws ArgException;
		String type_help();
	}


	/**for aligning descriptions in generate_help() */
	protected static class ValidOption {
		public final char Short;
		public final String Long;
		public final String description;
		protected ValidOption(char Short, String Long, String description) {
			this.Short = Short;
			this.Long = Long;
			this.description = description;
		}
	}




	public static class Builder {
		public Builder()
			{}
		public ArgsParser parse(String... args) {
			return new ArgsParser(this,  args);
		}
		public String shortopt_regex = "[A-Za-z].*";
		public Builder valid_shortopts(String regex) {
			shortopt_regex = regex;
			return this;
		}
		public Builder integer_shortopts() {
			shortopt_regex = "[A-Za-Z0-9].*";
			return this;
		}
		public boolean h_help = true;
		public boolean v_version = false;
		public Builder h_not_help() {
			h_help = false;
			return this;
		}
		public Builder v_for_version() {
			v_version = true;
			return this;
		}
		public boolean nonopt_stops_opts = false;
		public Builder stop_at_first_nonOpt() {
			nonopt_stops_opts = true;
			return this;
		}
		public boolean can_exit_anywhere = true;
		public Builder dont_exit() {
			can_exit_anywhere = true;
			return this;
		}
	}


	/**Options parsed from args[]*/
	//TreeSet because sorted by index, which might not be the order they are added
	protected final SortedSet<FoundOpt> options = new TreeSet<FoundOpt>();;

	/**All arguments that are not an option.
	 * Sort before open.*/
	protected final String[] arguments;
	/**to allow <tt>get_args()</tt> to be run multiple times, it doesn't empty {@link arg}, so arg.length is unlikely to reach zero.
	 * that means we need another way to check for too many arguments.*/
	protected boolean unused_arg = true;

	/**For generating help, Can't use a StringBuilder because you need to know the longest long option to line up descriptions.*/
	//FIXME: use StringBuilder and max_lon_length, and add_help(char Short, String Long, String desc, ArgumentType<T> type)
	protected final List<ValidOption> validOptions = new ArrayList<ValidOption>();

	/**While quitting on the first error is simpler, I think collecting them and printing them at the end is simpler, altougt some errors might cause other errors*/
	protected final StringBuilder errors = new StringBuilder();

	protected final boolean dont_exit;
	protected final boolean h_help;
	protected final boolean v_version;
	

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
		Objects.requireNonNull(b);
		Objects.requireNonNull(args);

		//Builder parameters that are used later
		this.arguments = new String[args.length];
		this.dont_exit = b.can_exit_anywhere;
		this.h_help = b.h_help;
		this.v_version = b.v_version;

		//
		final boolean windows = System.getProperty("os.name").startsWith("Windows");
		final String helpStr = "(\\?|h|help)";
		if (args.length==1
		 && (args[0].matches("-"+helpStr)
		  || (windows && args[0].matches("/"+helpStr)) ) )
			args[0] = "--help";

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
					p.Long = args[i].substring(2);
				else {
					p.Long = args[i].substring(2, t);
					p.argument = args[i].substring(t+1);
					p.argument_index = i;
				}
				options.add(p);
			}
			else if ((args[i].startsWith("-")  ||  (windows && args[i].startsWith("/")))   &&   args[i].substring(1).matches(b.shortopt_regex) ) {
				FoundOpt o = null;
				for (int ii=1; ii<args[i].length(); ii++)
					if (args[i].substring(ii).matches(b.shortopt_regex)) {
						o = new FoundOpt(i);
						o.Short = args[i].charAt(ii);
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
			else if (!options.isEmpty()  &&  options.last().argument == null  &&  !b.nonopt_stops_opts) {
				options.last().argument = args[i];
				options.last().argument_index = i;
			}
			else {
				arguments[i] = args[i];
				if (b.nonopt_stops_opts)
					stopopt = true;
			}
	}



	/**
	 * 
	 * @param Short single-character version, -o
	 * @param Long multi-character version, --option
	 * @param help
	 * @return
	 */
	public List<FoundOpt> getOpts(char Short, String Long, String help) {
		//cannot remove in a foreach
		ArrayList<FoundOpt> hits = new ArrayList<FoundOpt>(2);
		Iterator<FoundOpt> itr = options.iterator();
		while (itr.hasNext()) {
			FoundOpt opt = itr.next();
			if (opt.Long != null  &&  opt.Long.equals(Long)
			 || opt.Short !='\0'  &&  opt.Short == Short) {
				hits.add(opt);
				itr.remove();
			}
		}
		if (help != null)
			validOptions.add(new ValidOption(Short, Long, help));
		return hits;
	}

	public int optFlagN(char Short, String Long, String description) {
		int times = 0;
		for (FoundOpt o : getOpts(Short, Long, description))
			if (o.hasArgument())
				if (o.canDisownArgument()) {
					o.disownArgument();
					times++;
				} else
					errors.append(o.setBy() + ": invalid");
			else
				times++;
		return times;
	}

	public boolean optFlag(char Short, String Long, String description) {
		return optFlagN(Short, Long, description) > 0;
	}

	public <T> List<T> optMultiArg(char Short, String Long, String description, ArgType<T> type) {
		ArrayList<T> list = new ArrayList<T>(2);
		for (FoundOpt o : getOpts(Short, Long, description))
			if (o.hasArgument())
				try {
					list.add(type.parse(o.argument));
				} catch (ArgException ae) {
					errors.append(String.format("invalid argument to option %s (argument %d) \"%s\" is not a valid %s.\n", o.setBy(), o.index, o.argument, type));
				}
			else
				errors.append(String.format("%s (argument %d) requires an argument.\n", o.setBy(), o.index));
		return list;
	}

	public <T> T optArg(char Short, String Long, String description, T notSet, ArgType<T> type) {
		T argument = notSet;
		for (FoundOpt o : getOpts(Short, Long, description))
			if (o.hasArgument())
				try {
					argument = type.parse(o.argument);
				} catch (ArgException ae) {
					errors.append(String.format("invalid argument to option %s: \"%s\" is not a valid %s.\n", o.setBy(), o.argument, type));
				}
			else
				errors.append(o.setBy() + " requires an argument.\n");
		return argument;
	}

	public <T> T optArg(char Short, String Long, String description, T notSet, T noArg, ArgType<T> type) {
		T argument = notSet;
		for (FoundOpt o : getOpts(Short, Long, description))
			if (o.hasArgument())
				if (! o.canDisownArgument())
					try {
						argument = type.parse(o.argument);
					} catch (ArgException ae) {
						errors.append(String.format("invalid argument to option %s: \"%s\" is not a valid %s.\n", o.setBy(), o.argument, type));
					}
				else {
					o.disownArgument();
					argument = noArg;
				}
			else
				argument = noArg;
		return argument;
	}



	/**Return a String with a list of all options and their description.*/
	public String getHelp() {
		StringBuilder help = new StringBuilder();
		int longest_Long = 0;
		for (ValidOption o : validOptions)
			if (o.description != null  &&  o.Long != null  &&  o.Long.length() > longest_Long)
				longest_Long = o.Long.length();
		for (ValidOption o : validOptions)
			if (o.description != null) {
				help.append('\t');
				if (o.Short != '\0')
					help.append('-').append(o.Short);
				else
					help.append(' ').append(' ');
				if (o.Long != null)
					help.append(String.format("\t--%1$-" + longest_Long + "s", o.Long));
				else
					help.append(String.format("\t  %1$" + longest_Long + "s", ""));
				help.append('\t').append(o.description).append('\n');
			}
		return help.toString();
	}


	public <T> T arg(String name, T missing, ArgType<T> type) {
		String arg = null;
		for (int i=0; i<arguments.length; i++)
			if (arguments[i] != null) {
				arg = arguments[i];
				arguments[i] = null;
			}
		if (arg == null) {
			if (missing == null)//required
				errors.append("missing positional argument ").append(name).append('\n');
			return missing;
		}
		try {
			return type.parse(arg);
		} catch (ArgException ae) {
			errors.append(ae.getMessage()).append('\n');
			return missing;
		}
	}

	public <T> T arg(String name, ArgType<T> type) {
		return arg(name, null, type);
	}

	protected int argumentsLeft() {
		int n = 0;
		for (String arg : arguments)
			if (arg != null)
				n++;
		return n;
	}

	/**@return An array of all non-opt arguments.
	 */
	public <T> List<T> allArgs(String name, boolean minimum_one, ArgType<T> type) {
		unused_arg = false;
		ArrayList<T> list = new ArrayList<T>(arguments.length);
		for (String arg : arguments)
			if (arg != null)
				try {
					list.add(type.parse(arg));
				} catch (ArgException ae) {
					errors.append(ae.getMessage()).append('\n');
				}
		if (list.isEmpty() && minimum_one)
			errors.append("must at least have one ").append(name).append('\n');
		return list;
	}

	/**@return An array of all  non-opt arguments.
	 */
	public String[] allArgs(String name, boolean minimum_one) {
		List<String> list = allArgs(name, minimum_one, any);
		return list.toArray(new String[list.size()]);
	}



	/**Returns true if there are any errors.*/
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
		if (unused_arg  &&  argumentsLeft() > 0)
			errors.append("Unused arguments: " + Arrays.toString(allArgs(null, false)));
		return errors.toString();
	}













/************************************************
 * implementations that should cover most cases *
 ************************************************/
	//https://weblogs.java.net/blog/emcmanus/archive/2010/10/25/using-builder-pattern-subclasses
	public static class Regex implements ArgType<String> {
		public final Pattern regex;
		public Regex(String regex) {
			this.regex = Pattern.compile(regex);
		}
		public Regex(String regex, int flags) {
			this.regex = Pattern.compile(regex, flags);
		}
		public String parse(String argument) throws ArgException {
			 if (! regex.matcher(argument).matches())
				 throw new ArgException("");
			 return argument;
		}
		public String type_help() {
			return "must match the regular expression " + regex.toString();
		}
	}

	public static class Set implements ArgType<String> {
		public final String[] values;
		public boolean ignoreCase = false; 
		public Set(String... values) {
			this.values = Objects.requireNonNull(values);
		}
		public Set ignoreCase() {
			ignoreCase = true;
			return this;
		}
		public String type_help() {
			return "";
		}
		public String parse(String argument) throws ArgException {
			argument = argument.trim();
			//if b==1 and 
			for (String v : values)
				if ((ignoreCase && argument.equalsIgnoreCase(v))
				|| (!ignoreCase && argument.equals(v)))
					return v;
			throw new ArgException("valid values are: " + Arrays.toString(values));
		}
	}

	//not static to not stay in memory permanently
	public final ArgType<String> any = new ArgType<String>() {
		public String parse(String arg) {return arg;}
		public String type_help() {return "";}
	};

	public static class IntRange implements ArgType<Long> {
		public final long min, max;
		public IntRange(long min, long max) {
			if (min >= max)
				throw new IllegalArgumentException("min >= max");
			this.min = min;
			this.max = max;
		}
		public Long parse(String argument) throws ArgException {
			long num = Long.parseLong(argument.replace(" ", "").replace("_", ""));
			if (num >= min  &&  num <= max)
				return num;
			if (num < 0  &&  min >= 0)
				throw new ArgException("cannot be negative");
			throw new ArgException("%s is too %s: must be %s",
                    argument,  num>max ? "big" : "small",  type_help());
			//throw new ArgException(argument + " is too " + (num>max?"big":"small") + ": must be " + type_help());
		}
		public String type_help() {
			return String.format("an integer bigger than %i and smaller than %i", min-1, max+1);
		}
	}


/*********************
 * shorthand methods *
 *********************/
	public String optStr(char Short, String Long, String description) {
		return optArg(Short, Long, description, null, any);
	}

	public int optInt(char shortOpt, String longOpt, String description, int notSet, int min, int max) {
		return optArg(shortOpt, longOpt, description, Long.valueOf(notSet), new IntRange(min, max)).intValue();
	}


	/**Adds the normal help options, if set, prints help+parameter help and call System.exit(0).
	 *@param help the text to be displaayed before option help.*/
	public void handle_help(String help) {
		String helpStr = help();
		if (helpStr == null)
			return;
		if (help == null)
			help = "";
		System.out.print(help);
		if (!help.isEmpty() && help.charAt(help.length()-1) != '\n')
			System.out.println();
		System.out.print(helpStr);
		System.exit(0);
	}

	/**@return option help if an help option was set. returns null otherwise.*/
	public String help() {
		if (optFlag('h', "help", "Print this help.")
		 || optFlag('?', null, null))
			return getHelp();
		return null;
	}

	/**If -v or --version is set, print version info and exit(0).*/
	public void handle_version(String version_info) {
		if (version()) {
			System.out.println(version_info);
			System.exit(0);
		}
	}
	/**@return true if -v or --version was set.*/
	public boolean version() {
		return optFlag('v', "version", "Display version information.");
	}


	/**If there are any errors, print them and exit.
	 * @param errorCode is passed to System.exit()*/
	public void handle_errors(int errorCode) {
		String errors = getErrors();
		if (errors.isEmpty())
			return;
		System.err.println(errors);
		System.exit(errorCode);
	}
}
