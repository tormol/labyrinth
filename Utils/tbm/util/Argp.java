package tbm.util;
class Argp {
	
}
/* usage
options = new parser()...settings...parse(args);
new opt().longs().short().help(str).Optionalarg().multiple.in(optionlist, flag/int.range/
variants of in for each type
cannot parse without knowing wether an option is a flag or requires an option.
Actually you can, SimpleParser shows its possible, and if you have a regex saying what characters are allowed as shorts, you can support stuff like"-O3"
 */
/* 2
options = new Argument()...settings...addType(<T implements opt>);
<T implements opt> opt = o.add(<implements argopt> Argument.opt<T implements opt>()..Long..Short..<T>-specifics..);
opt.set();
opt.value
 */
/*
the parser must know:
long, short, argument(no, optional, yes, everything after), a way tell the option that it was set, somewhere to put arguments 
akstract:
t:argopt
arg.t
number:.arg.t
interfaces:


default types
flag.t	
shorthand arg.
(int|float|...)	t.arg.number
t.flag:repeatable
t.
*/
/*
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.LinkedList;

public class Option {
	private LinkedList<String> longs = new LinkedList<String>();
	private LinkedList<Character> shorts = new LinkedList<Character>();
	public final String id;
	public String help = "";
	public boolean optArg = false;
	public boolean multiple = false;
	
	public Option(String id) {
		this.id = id;
	}


	public boolean at(ArgParser ap) {
		if (optArg)
			ap.log.add("Warning: optarg set on flag --" + longs.getFirst());
		if (multiple)
			ap.log.add("Warning: multiple set on flag --" + longs.getFirst());
		ArgOptResult res = ap.find(this);
		
	}


	public Option optArg(boolean b) {
		this.optArg = b;
		return this;
	}
	public boolean optArg(boolean b) {
		return true;
	}
	public Option multiple(boolean b) {
		this.multiple = b;
		return this;
	}
	public Option help(String help) {
		this.help += help;
		return this;
	}
	public Option (boolean b) {
		this. = b;
		return this;
	}


	public Option Long(String str) {
		longs.add(str);
		return this;
	}
	public Option Long(String[] arr) {
		for (String str : arr)
			longs.add(str);
		return this;
	}
	public Option Long(Collection<String> list) {
		longs.addAll(list);
		return this;
	}

	public Option Short(char c) {
		shorts.add(new Character(c));
		return this;
	}
	public Option Short(char[] arr) {
		for (char c : arr)
			shorts.add(new Character(c));
		return this;
	}
	public Option Short(Character[] arr) {
		for (Character c : arr)
			shorts.add(c);
		return this;
	}
	public Option Short(Collection<Character> list) {
		shorts.addAll(list);
		return this;
	}
}*/
