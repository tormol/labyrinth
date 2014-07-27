package tbm.argparser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import tbm.util.ArgsParser;

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
public class Core {
	/***/
	private SortedSet<ParsedOpt> opts;
	/***/
	private final ArrayList<Opt_interface> validOpts = new ArrayList<Opt_interface>();
	/**All arguments that are not an option.
	 * Sort before open.*/
	private final SortedSet<Argument> arg;
	private final Consumer<String> errors;

	/**Parse args with default parameters.
	 * @param args the String[] passed to main().
	 */
	public Core(String... args) {
		this(null, args);
	}
	/**Parse args with parameters set in ArgsParser.Builder.
	 * @param args the String[] passed to main().
	 */
	public Core(Builder b, String... args) {
		errors = b.errors;
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
	public static interface Opt_interface<AT> extends Consumer<AT> {
		/**A multi-character option; --option*/
		String Long();
		/**A single-character option; -o*/
		char Short();
		/**TODO*/
		String help();
	}


	/**TODO
	 * Adds the option to a list in case help() is called.
	 * Searches a list of set options, and calls opt.got() when an option matches.
	 * @param opt a class implementing Opt_interface
	 * @return the parameter
	 */
	public <OI extends Opt_interface> OI opt(OI opt) {
		Iterator<ParsedOpt> itr = opts.iterator();
		ParsedOpt po;
		while ((po=itr.next()) != null)
			if (po.matches(opt.Short(), opt.Short())) {
				String error = opt.got(po);
				if (error != null)
					errors.append(error);
				else if (po.argSeparate() && opt.dropArg()) {
					arg.add(po.argument);
					po.argument = null;
				}
				itr.remove();
			}
		validOpts.add(opt);
		return opt;
	}
	public <AT, H extends Consumer<AT>> H opt(char Short, String Long, String help, Type<AT> argType, H got) {
		Iterator<ParsedOpt> itr = opts.iterator();
		ParsedOpt po;
		while ((po=itr.next()) != null)
			if (po.matches(Short, Long)) {
				try {
					if (po.argument==null)
						got.accept(null);
					else if (argType != null)
						got.accept(argType.parse(po.argument.str));
					else if (po.argSeparate())
						arg.add(po.argument);
					else
						errors.accept(String.format("%s does not take an argument\n", po.trigger()));
				} catch (ArgException e) {
					
				}
				itr.remove();
			}
		validOpts.add(o);
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
	public <AT> AT[] getArgs(Type<AT> parser) {
		//return String[] to allow args = .getArgs();
		@SuppressWarnings("unchecked")
		AT[] args = (AT[])new Object[arg.size()];
		int i=0;
		for (Argument a : arg)
			try {
				args[i] = parser.parse(a.str);
				i++;
			} catch (ArgException e) {
				
			}
		
		return args;
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
		/**Returns true if short or long is not null and equals this options short/long.*/
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

	@SuppressWarnings("serial")
	public static class ArgException extends Exception {
		public ArgException(String str) {
			super(str);
		}
		public ArgException(String f, Object... a) {
			super(String.format(f, a));
		}
	}
	public static class Opt<AT> implements Opt_interface<String> {
		public AT arg = null;
		
		public Opt(Type<AT> type) {
			
		}
		@Override
		public void accept(String t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String Long() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public char Short() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String help() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}

