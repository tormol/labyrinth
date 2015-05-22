package tbm.util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;

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

	/**A class to store the index along with an non-option argument.*/
	protected static class Argument implements Comparable<Argument> {
		/**The arguments index in args[].*/
		public final int index;
		/**The argument*/
		public final String str;
		protected Argument(int index, String str) {
			this.index = index;
			this.str = str;
		}
		@Override//Comparable
		public int compareTo(Argument arg) {
			return this.index-arg.index;
		}
	}


	/**An interface for creating an option
	 *
	 */
	public static interface Opt_interface {
		/**A multi-character option; --option*/
		String getLong();
		/**A single-character option; -o*/
		char getShort();
		/**A sentence to explain the option, is used by generate_help().*/
		String getHelp();
		/**Is called from ArgsParserwhen the option was set.*/
		String got(ParsedOpt o) throws ArgException;
		/**Is called when the option was given an argument, should it be a non-opt argument?*/
		boolean dropArg();
	}

	/**Represents an args option that have not been matched yet.*/
	protected static class ParsedOpt implements Comparable<ParsedOpt> {
		/**A multi-character option; --option*/
		public String Long = "";
		/**A single-character option; -o*/
		public char Short = '\0';
		/**The options index in args[]*/
		public final int index;
		/**the argument, -1=not set*/
		protected Argument argument = new Argument(-1, null);

		protected ParsedOpt(int index) {
			this.index = index;
		}

		/**Return the argument*/
		public Argument getArg() {
			return argument;
		}
		/**Returns true if this option has an argument.*/
		public boolean hasArg() {
			return (argument.index!=-1);
		}
		/**Returns true if argument was a part of the option
		 * eg "-o3"->true, "--size 8"->false, "--help"->false 
		 */
		public boolean argIncluded() {
			return (hasArg() && argument.index==this.index);
		}
		/**Returns true if the option has an argument, and it was the next args[] element.
		 * eg "-o3"->false, "--size 8"->true, "--help"->false
		 */
		public boolean argSeparate() {
			return (hasArg() && argument.index==1+this.index);
		}
		/**Return the string that set this option*/
		public String trigger() {
			if (! Long.isEmpty())
				return "--" + Long;
			else if (Short != '\0')
				return "-" + String.valueOf(Short);
			else
				return null;
		}
		/**Returns true if short or long is not null and equals this options short/llong.*/
		public boolean matches(char Short, String Long) {
			if (this.Long != null  &&  this.Long.equals(Long)
			 || this.Short !='\0'  &&  this.Short == Short)
				return true;
			return false;
		}
		/**Return debug information. see .trigger()*/
		public String toString() {
			String str = String.format("[%d]=%s", index, trigger());
			if (hasArg())
				str += String.format("->[%d]=%s", argument.index, argument.str);
			return str;
		}

		@Override//Comparable
		public int compareTo(ParsedOpt po) {
			return this.index-po.index;
		}
	}



	/****Input classes****/

	/**@param T type/class the argument is converted to*/
	public interface ArgType<T> {
		T parse(String arg) throws ArgException;
		String type_help();
	}


	/**A class to store the information about an option*/
	protected static class OptCore {
		/**A multi-character option; --option*/
		public String _Long=null;
		/**A single-character option; -o*/
		public char _Short='\0';
		public String _help=null; 
		protected OptCore(char Short, String Long, String help) {
			_Short = Short;
			_Long = Long;
			_help = help;
		}
		public OptCore()
			{}

		public String getLong() {
			return _Long;
		}
		public char getShort() {
			return _Short;
		}
		public String getHelp() {
			return _help;
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
	protected SortedSet<ParsedOpt> opts;

	/**options supplied by the program, stored to generate --help output*/
	protected ArrayList<OptCore> validOpts = new ArrayList<OptCore>();

	/**All arguments that are not an option.
	 * Sort before open.*/
	public SortedSet<Argument> arg;

	/**While quitting on the first error is simpler, I think collecting them and printing them at the end is simpler, altougt some errors might cause other errors*/
	protected StringBuilder errors = new StringBuilder();

	/**to allow <tt>get_args()</tt> to be run multiple times, it doesn't empty {@link arg}, so arg.length is unlikely to reach zero.
	 * that means we need another way to check for too many arguments.*/
	protected boolean unused_arg = true;


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
		final boolean windows = System.getProperty("os.name").startsWith("Windows");
		final String helpStr = "(\\?|h|help)";
		if (args.length==1
		 && (args[0].matches("-"+helpStr)
		  || (windows && args[0].matches("/"+helpStr)) ) )
			args[0] = "--help";

		//TreeSet because sorted by index, which might not be the order they are added
		arg = new TreeSet<Argument>();
		opts = new TreeSet<ParsedOpt>();
		boolean stopopt = false;
		for (int i=0; i<args.length; i++)
			if (stopopt)
				arg.add(new Argument(i, args[i]));
			else if (args[i].equals("--"))
				stopopt = true;
			else if (args[i].startsWith("--")) {
				int t = args[i].indexOf("=");
				ParsedOpt p = new ParsedOpt(i);
				if (t==-1)
					p.Long = args[i].substring(2);
				else {
					p.Long = args[i].substring(2, t);
					p.argument = new Argument(i, args[i].substring(t+1));
				}
				opts.add(p);
			}
			else if ((args[i].startsWith("-")  ||  (windows && args[i].startsWith("/")))   &&   args[i].substring(1).matches(b.shortopt_regex) ) {
				ParsedOpt o = null;
				for (int ii=1; ii<args[i].length(); ii++)
					if (args[i].substring(ii).matches(b.shortopt_regex)) {
						o = new ParsedOpt(i);
						o.Short = args[i].charAt(ii);
						opts.add(o);
					}
					else {
						if (args[i].charAt(ii) == '=')
							ii++;
						o.argument  =  new Argument(i, args[i].substring(ii) );
						break;
					}
			}
			//if the last option didn't have an argument, add this.
			//see Builder.stop_at_first_nonopt() for why nonopt_stops_opts forces option arguments to be 
			else if (!opts.isEmpty()  &&  opts.last().argument.index == -1  &&  !b.nonopt_stops_opts)
				opts.last().argument = new Argument(i, args[i]);
			else {
				arg.add(new Argument(i, args[i]));
				if (b.nonopt_stops_opts)
					stopopt = true;
			}
	}


	public static interface OptType<V, R> {
		/**
		 *@param value is null if no argument
		 *@param included -o=path == true,  -o path == false
		 *@return true-> parameters ok, false->(value!=null&&
		 *@throws ArgException
		 */
		boolean accepts_separate_arg();
		void consume(V value) throws ArgException;
		void no_argument() throws ArgException;
		R produce();

		public static class Single<V> implements OptType<V,V> {
			protected V value = null;
			public final V noArg_value;
			public Single() {
				this.noArg_value = null;
			}
			public Single(V noArg_value) {
				this.noArg_value = Objects.requireNonNull(noArg_value);
			}
			@Override public boolean accepts_separate_arg() {return noArg_value == null;}
			@Override public void consume(V value) {this.value = value;}
			@Override public void no_argument() throws ArgException {
				if (noArg_value == null)
					throw new ArgException("require an argument");
				value = noArg_value;
			}
			@Override public V produce() {
				return this.value;
			}
		}

		public static class Array<V> extends ArrayList<V> implements OptType<V,List<V>> {
			public Array(int initialCapacity) {super(initialCapacity);}
			public Array() {super();}
			@Override public boolean accepts_separate_arg() {return true;}
			@Override public void consume(V value) throws ArgException {add(value);}
			@Override public void no_argument() throws ArgException {throw new ArgException("require an argument");}
			@Override public List<V> produce() {return this;}
			private static final long serialVersionUID = 1L;
		}

		public static class Flag implements OptType<Object, Integer> {
			protected int times = 0;
			public Flag()
				{}
			@Override public boolean accepts_separate_arg() {return false;}
			@Override public void consume(Object arg) throws ArgException {throw new ArgException("does not accept an argument");}
			@Override public void no_argument() {times++;}
			@Override public Integer produce() {return Integer.valueOf(times);}
		}
	}

	public <T, R> R opt(char _short, String _long, String help, OptType<T, R> optType, ArgType<T> argType) {
		//cannot remove in a foreach
		Iterator<ParsedOpt> itr = opts.iterator();
		while (itr.hasNext()) {
			ParsedOpt po = itr.next();
			if (po.matches(_short, _long))
				try {
					if (po.argSeparate()  &&  optType.accepts_separate_arg()) {
						arg.add(po.argument);
						po.argument = null;
					}
					if (po.hasArg())
						 optType.consume( argType.parse(po.argument.str) );
					else
						optType.no_argument();
				} catch (ArgException ae) {
					errors.append(po.trigger() + " " + ae.getMessage());
				} finally {
					itr.remove();
				}
		}
		validOpts.add(new OptCore(_short, _long, help));
		return optType.produce();
	}

	public int optflagN(char _short, String _long, String help) {
		int times = 0;
		//cannot remove in a foreach
		Iterator<ParsedOpt> itr = opts.iterator();
		while (itr.hasNext()) {
			ParsedOpt po = itr.next();
			if (po.matches(_short, _long)) {
				if (po.hasArg())
					if (po.argSeparate()) {
						arg.add(po.argument);
						po.argument = null;
						times++;
					} else
						errors.append(po.trigger() + "");
				else
					times++;
				itr.remove();
			}
		}
		validOpts.add(new OptCore(_short, _long, help));
		return times;
	}
	public boolean optflag(char _short, String _long, String help) {
		return 0 < optflagN(_short, _long, help);
	}


	/**TODO
	 * Adds the option to a list in case help() is called.
	 * Searches a list of set options, and calls opt.got() when an option matches.
	 * @param opt a class implementing Opt_interface
	 * @return opt, the parameter
	 */
	public <T extends Opt_interface> T opt(T opt) {
		OptCore inf = new OptCore(opt.getShort(), opt.getLong(), opt.getHelp());
		//cannot remove in a foreach
		Iterator<ParsedOpt> itr = opts.iterator();
		while (itr.hasNext()) {
			ParsedOpt po = itr.next();
			if (po.matches(inf._Short, inf._Long)) {
				String error = opt.got(po);
				if (error != null)
					errors.append(error);
				else if (po.argSeparate() && opt.dropArg()) {
					arg.add(po.argument);
					po.argument = null;
				}
				itr.remove();
			}
		}
		validOpts.add(inf);
		return opt;
	}


	/**Return a String with a list of all options and their description.*/
	public String getHelp() {
		StringBuilder help = new StringBuilder();
		int longest_Long = 0;
		for (OptCore o : this.validOpts)
			if (o._help != null  &&  o._Long != null  &&  o._Long.length() > longest_Long)
				longest_Long = o._Long.length();
		for (OptCore o : this.validOpts)
			if (o._help != null) {
				if (o._Short != '\0')
					help.append("\t-" + o._Short);
				else
					help.append("\t  ");
				if (o._Long != null)
					help.append(String.format("\t--%1$-" + longest_Long + "s", o._Long));
				else
					help.append(String.format("\t  %1$" + longest_Long + "s", ""));
				help.append("\t" + o._help + '\n');
			}
		return help.toString();
	}


	public <T> T arg( String name, boolean required, ArgType<T> at) {
		if (arg.isEmpty())
			if (required) {
				error.add("")
			}
		try {return at.parse(arg);}
		catch (ArgException ae) {
			errors.append()
		}
	}

	/**@return An array of all  non-opt arguments.
	 */
	public <T> List<T> allArgs(String name, boolean minimum_one, ArgType<T> at) {
		String[] args = allArgs(name, minimum_one);
		List<T> list = new ArrayList<T>(args.length);
		try {
			for (String arg: args)
				list.add(at.parse(arg));
		} catch (ArgException ae) {
			errors.append(ae.getMessage());
		}
		return list;
	}

	/**@return An array of all  non-opt arguments.
	 */
	public String[] allArgs(String name, boolean minimum_one) {
		unused_arg = false;
		if (arg.isEmpty() && minimum_one)
			errors.append("must at least have one "+name);
		return arg.toArray(new String[arg.size()]);
	}



	/**Returns true if there are any errors.*/
	public boolean hasErrors() {
		return (getErrors().length() > 0);
	}
	/**Return a String with all errors.*/
	public String getErrors() {
		StringBuilder errors = new StringBuilder(this.errors);
		//invalid options
		for (ParsedOpt o : this.opts)
			errors.append("Invalid option " + o.trigger() + ".\n");
		//unused arguments
		if (unused_arg  &&  !arg.isEmpty())
			errors.append("Unused arguments: " + Arrays.toString(arg.toArray()));
		return errors.toString();
	}













/************************************************
 * implementations that should cover most cases *
 ************************************************/
	//https://weblogs.java.net/blog/emcmanus/archive/2010/10/25/using-builder-pattern-subclasses
	public static abstract class Opt<T extends Opt<T>> extends OptCore implements Opt_interface {
		protected abstract T self();
		public Opt()
			{}
		public Opt(char Short, String Long, String help) {
			_Short = Short;
			_Long = Long;
			_help = help;
		}

		public T Long(String Long) {
			_Long = Long;
			return self();
		}
		public T Short(char Short) {
			_Short = Short;
			return self();
		}
		public T help(String help) {
			_help = help;
			return self();
		}
	}


	public static abstract class OptFlag_abstract<T extends OptFlag_abstract<T>> extends Opt<T> {
		public boolean set=false;
		public int times=0;
		public String got(ParsedOpt o) {
			set=true;
			times++;
			if (o.argIncluded())
				return o.trigger() + " does not accept an argument.\n";
			return null;
		}
		public boolean dropArg() {
			return true;
		}
	}
	public static final class OptFlag extends OptFlag_abstract<OptFlag> {
		protected OptFlag self() {
			return this;
		}
	}


	public static abstract class OptStr_abstract<T extends OptStr_abstract<T>> extends Opt<T> {
		public String arg=null;
		public String regex=null;
		public String noArg=null;
		public boolean arg_required=true;
		public T notSet(String arg) {
			this.arg=arg;
			return self();
		}
		public T match(String regex) {
			this.regex = regex;
			return self();
		}
		public T optional(String noArg) {
			this.noArg = noArg;
			arg_required = false;
			return self();
		}
		public String got(ParsedOpt o) {
			if (o.hasArg())
				if (regex==null || o.argument.str.matches(regex))
					arg=o.argument.str;
				else
					return o.trigger()+" has an invalid argument.\n";
			else if (!arg_required)
				arg=noArg;
			else
				return o.trigger()+" require an argument.\n";
			return null;
		}
		public boolean dropArg() {
			return !arg_required;
		}
	}
	public static final class OptStr extends OptStr_abstract<OptStr> {
		protected OptStr self() {
			return this;
		}
	}


	public static abstract class OptInt_abstract<T extends OptInt_abstract<T>> extends Opt<T> {
		public int value=0,
		           min=Integer.MIN_VALUE,
		           max=Integer.MAX_VALUE;

		public T notSet(int value) {
			this.value = value;
			return self();
		}
		public T min(int min) {
			this.min = min;
			return self();
		}
		public T max(int max) {
			this.max = max;
			return self();
		}
		
		@Override
		public String got(ParsedOpt o) {
			if (o.argument.index == -1)
				return String.format("%s require an integer argument in [%d, %d].\n", o.trigger(), min, max);
			try {
				value = Integer.parseInt(o.argument.str);
				if (value < min  ||  value > max)
					return String.format("The argument to %s must be an integer in [%d, %d].\n", o.trigger(), min, max);
			}
			catch (NumberFormatException e) {
				return String.format("The argument to %s must be an integer in [%d, %d].\n", o.trigger(), min, max);
			}
		return null;
		}
		@Override
		public boolean dropArg() {
			return false;
		}
	}
	public static final class OptInt extends OptInt_abstract<OptInt> {
		protected OptInt self() {
			return this;
		}
	}




/*********************
 * shorthand methods *
 *********************/
	public int optFlagN(char Short, String Long, String help) {
		return opt(new OptFlag().Short(Short).Long(Long).help(help)).times;
	}
	public boolean optFlag(char Short, String Long, String help) {
		return opt(new OptFlag().Short(Short).Long(Long).help(help)).set;
	}

	public String optStr(char Short, String Long, String help) {
		return opt(new OptStr().Short(Short).Long(Long).help(help)).arg;
	}
	public String optStr(char Short, String Long, String help, String notSet) {
		return opt(new OptStr().Short(Short).Long(Long).help(help).notSet(notSet)).arg;
	}
	public String optStr(char Short, String Long, String help, String notSet, String default_arg) {
		return opt(new OptStr().Short(Short).Long(Long).help(help).notSet(notSet).optional(default_arg)).arg;
	}

	public int optUInt(char Short, String Long, String help) {
		return opt(new OptInt().Short(Short).Long(Long).help(help)).value;
	}
	public int optInt(char Short, String Long, String help, int notSet) {
		return opt(new OptInt().Short(Short).Long(Long).help(help).notSet(notSet)).value;
	}
	public int optInt(char Short, String Long, String help, int notSet, int min, int max) {
		return opt(new OptInt().Short(Short).Long(Long).help(help).notSet(notSet).min(min).max(max)).value; 
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
		if (opt(new OptFlag().Long("help").Short('h').help("Print this help.")).set
		 || opt(new OptFlag().Short('?')).set)
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
		return opt(new OptFlag().Short('v').Long("version").help("Display version information.")).set;
	}

	/**This method is a shorthand.
	 * @return times verbose - times quiet - 2*times silent
	 */
	public int verbosity() {
		int v=0;
		v+=opt(new OptFlag().Short('v').Long("verbose").help("")).times;
		v-=opt(new OptFlag().Short('q').Long("quiet").help("")).times;
		v-=2*opt(new OptFlag().Short('s').Long("silent").help("")).times;
		return v;
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
