package tbm.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

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
	/***/
	private SortedSet<ParsedOpt> opts;
	/***/
	private ArrayList<OptCore> validOpts = new ArrayList<OptCore>();
	/**All arguments that are not an option.
	 * Sort before open.*/
	public SortedSet<Argument> arg;
	private boolean unused_arg = true;
	//a log
	private StringBuilder errors = new StringBuilder();

	/**Parse args with default parameters.
	 * @param args the String[] passed to main().
	 */
	public ArgsParser(String... args) {
		this(null, args);
	}
	/**Parse args with parameters set in ArgsParser.Builder.
	 * @param args the String[] passed to main().
	 */
	public ArgsParser(Builder b, String... args) {
		if (b==null)
			b = new Builder();
		final boolean windows = System.getProperty("os.name").startsWith("Windows");
		final String helpStr = "(\\?|h|help)";
		if (args.length==1
		 && (args[0].matches("-"+helpStr)
		  || (windows && args[0].matches("/"+helpStr)) ) )
			args[0] = "--help";

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
			else if ((args[i].startsWith("-")  || (windows && args[i].startsWith("/")))  &&  args[i].substring(1).matches(b.shortopt_regex) ) {
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
			//if the previous option doesn't have an argument, add this.
			else if (!opts.isEmpty()  &&  opts.last().argument.index == -1)
				opts.last().argument = new Argument(i, args[i]);
			else {
				arg.add(new Argument(i, args[i]));
				if (b.nonopt_stops_opts)
					stopopt = true;
			}
	}




	/**An interface for creating an option
	 *
	 */
	public static interface Opt_interface {
		/**A multi-character option; --option*/
		public String getLong();
		/**A single-character option; -o*/
		public char getShort();
		/**TODO*/
		public String getHelp();
		/**Is called when the option was set.*/
		public String got(ParsedOpt o);
		/**Is called when the option was given an argument, should it be a non-opt argument?*/
		public boolean dropArg();
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



	/**@return An array of all  non-opt arguments.
	 */
	public String[] getArgs() {
		unused_arg = false;
		//sort: during init all argument after options are considered argument to that option.
		//when arguments to flag options are added back, they appear at the end of the list. sorting fixes that. 

		//return String[] to allow args = .getArgs();
		String[] args = new String[arg.size()];
		int i=0;
		for (Argument a : arg) {
			args[i] = a.str;
			i++;
		}		
		return args;
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
		if (unused_arg  &&  getArgs().length > 0)
			errors.append("Unused arguments: " + getArgs().toString());
		return errors.toString();
	}



	/*classes that are used everywhere*/
	/**TODO
	 * Is used to store set options
	 * 
	 *
	 */
	public static class ParsedOpt implements Comparable<ParsedOpt> {
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
			if (!Long.isEmpty())
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


	/**A class to store the information about an argument*/
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


	public static class Builder {
		public String shortopt_regex = "[A-Za-z].*";
		public boolean nonopt_stops_opts = false;
		public boolean h_help = true;
		public Builder()
			{}
		public ArgsParser parse(String... args) {
			return new ArgsParser(this,  args);
		}
		public Builder valid_shortopts(String regex) {
			shortopt_regex = regex;
			return this;
		}
		public Builder integer_shortopts() {
			shortopt_regex = "[A-Za-Z0-9].*";
			return this;
		}
		public Builder h_not_help() {
			h_help = false;
			return this;
		}
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
