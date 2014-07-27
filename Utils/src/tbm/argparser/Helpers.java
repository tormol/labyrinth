package tbm.argparser;



public class Helpers extends Core {
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
