package labyrinth.engine;
import tbm.util.chars;
import tbm.util.geom.Point;

import static labyrinth.engine.Method.VType.*;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Method {
	static final Map<String,Method> methods = new HashMap<String,Method>();
	public static Method get(String method) {
		if (method==null)
			return null;
		Method m = methods.get(method);
		if (m==null)
			throw Window.error("Finner ikke metoden \"%s\"", method);
		return m;
	}
	public static void call(String method, Tile tile, Mob mob) {
		Method.get(method).call(tile, mob);
	}
	public static void add(String line) {
		Method ny = new Method(line);
		methods.put(ny.name, ny);
	}


	public final String name;
	private final Func[] operations;
	/**
	 * 
	 */
	public Method(String line) {
		final String mName = "\\w+";
		final String parameters = "(?:[\\w\\s,]|'.')*";
		//finne navn
		Matcher method = Pattern.compile("^\\s*("+mName+")\\s*:((?:\\s*"+mName+"\\("+parameters+"\\);)*)\\s*$").matcher(line);
		if (!method.matches())
			Window.error("Uforsttaælig metode: \"%s\"", line);
		name = method.group(1);

		//http://stackoverflow.com/questions/6835970/regular-expression-capturing-all-repeating-groups
		String[] name = findAll(method.group(2), "("+mName+")(?=\\("+parameters+"\\);)"/*""+mNavn+"(?=\\("+parametre+"\\);)"*/);
		String[] parameter = findAll(method.group(2), "(?<="+mName+"\\()("+parameters+")(?=\\);)");
		operations = new Operation[parameter.length];
		for (int i=0; i<name.length; i++) {
			Operation o = null;
			//TODO: hvis koordinater mangler vil metoden kjøres på feltet som startet funksjonen.
			switch (name[i]) {
			  case "sett":
				String[] param = param(parameter[i], "(\\d+)", "(\\d+)", "'(.)'");
				o = new Set(point(param), param[2].charAt(0));
				break;
			  case "trigger": o=new Trigger(point(param(parameter[i], "(\\d+)", "(\\d+)")));  break;
			  case "kall": o=new Call(param(parameter[i], "(\\w+)")[0]);  break;
			  case "flytt": o=new Move(point(param(parameter[i], "(\\d+)", "(\\d+)")));  break;
			  default:
				throw Window.error("Metode %s: Ukjent operasjon %s.", this.name, name[i]); 
			}
			operations[i] = o;
		}
	}



	//hjelper-funksjoner til Metode()
	/**Returnerer en array med alle treff av regex i str.*/
	public String[] findAll(String str, String regex) {
		LinkedList<String> found = new LinkedList<String>();
		Matcher m = Pattern.compile(regex).matcher(str);

		while (m.find())
			found.add(m.group());
		return found.toArray(new String[found.size()]);
	}
	/**bygger en regex av par..., matcher den mot str og returnerer svarene*/
	private String[] param(String str, String... par) {
		String regex="";
		for (String p : par)
			regex += ",\\s*"+p+"\\s*";
		regex = regex.substring(1);
		if (par.length==0)
			regex = "^\\s*$";
		Matcher m = Pattern.compile(regex).matcher(str);
		if (!m.matches())
			//finner ingen god måte å fortelle funksjon-navn eller kolonne.
			throw Window.error("Metode %s: En funksjon har feil parametre \"(%s)\"", name, str);
		String[] found = new String[par.length];
		for (int i=0; i<par.length; i++)
			found[i] = m.group(i+1);
		return found;
	}
	private Point point(String[] found) {
		Point p = new Point(
				Integer.valueOf(found[0]),
				Integer.valueOf(found[1])
			);
		Dimension d = TileMap.dimesions();
		if (p.x<0 || p.y<0 || p.x>=d.width || p.y>=d.height)
			throw Window.error("Metode %s: koordinatene (%d,%d) er utenfor labyrinten.", name, p.x, p.y);
		return p;
	}


	public void call(Tile tile, Mob mob) {
		for (Operation op : operations)
			op.perform(tile, mob);
	}




	static class Source {
		public final String str;
		public final char[] arr;
		public int pos;
		public Source(String str) {
			this.str = str;
			this.arr = str.toCharArray();
			this.pos = 0;
		}
		public char peek() {
			return arr[pos];
		}
		public char next() {
			char c = arr[pos];
			pos++;
			return c;
		}
		public void back() {
			pos--;
		}
		public void whitespace() {
			while (chars.whitespace(arr[pos]))
				pos++;
		}
	}

	/*static class VType {
		public final String name;
		public final String start;
		private Function<Source, Object> parser;
		public VType(String name, String start, Function<Source, Object> parser) {
			this.name = name;
			this.start = start;
			this.parser = parser;
		}
		public Variable parse(Source src) {
			return new Variable(this, parser.apply(src));
		}
	}*/

	public static enum VType {
		VOID("void"), INT("int"), POINT("point"), CHAR("char");
		private VType(String str)
			{}
		public static VType[] l(VType... types) {
			return types;
		}
	}
	static abstract class Var {
		final String name;
		final VType type;
		protected Var(String n, VType t) {
			name=n;
			type=t;
		}
		public VType basicType() {return type;}
		public int Int() {throw Window.error("\"%s\" is not an Integer", name);}
		public Point Point() {throw Window.error("\"%s\" is not a Point", name);}
		public char Char() {throw Window.error("\"%s\" is not a characther", name);}

		public static Var parse(Source src) {
			src.whitespace();
			char c = src.peek();
			if (c=='(') {
				
			}
			else if (c=='"') {
				src.pos++;
				int start = src.pos;
			}
			return null;
		}

		public static class VVoid extends Var {
			public VVoid() {
				super(null, Var.VType.VOID);
			}
		}
		public static class VChar extends Var {
			public char c;
			public VChar(char c) {
				super(null, Var.VType.CHAR);
				this.c=c;
			}
			@Override
			public char Char() {
				return c;
			}
		}

		public static class VInt extends Var {
			public int n;
			public VInt(int n) {
				super(null, Var.VType.INT);
				this.n=n;
			}
			@Override
			public int Int() {
				return n;
			}
		}

		public static class VPoint extends Var {
			public Point p;
			public VPoint(Point p) {
				super(null, Var.VType.POINT);
				this.p=p;
			}
			@Override
			public Point Point() {
				return p;
			}
		}
	}


	static class Func {
		public final String name;
		public final VType[] parameters;
		public final VType ret;
		private final Function<Var[], BiFunction<Tile, Mob, Var>> init;
		private Func(String name, VType[] parameters, VType ret, Function<Var[], BiFunction<Tile, Mob, Var>> init) {
			this.name = name;
			this.parameters = parameters;
			this.ret = ret;
			this.init = init;
		}
		public BiFunction<Tile, Mob, Var> instance(Var... arg) {
			return init.apply(arg);
		}
	}

	static Func[] methodks = new Func[] {
		new Func("set", VType.l(POINT, CHAR), VOID, (param)->{
				Point p = param[0].Point();
				char symbol = param[1].Char();
				Type type = Type.t(symbol);
				
					m = String.valueOf(symbol);
				
			public void perform(Tile rute, Mob enhet) {
				if (pos != null)
					rute = TileMap.get(pos);
				rute.setType(type);
				if (type.method)
					rute.method = Method.get(symbol);
			}
		});
	};
	
	
		/**Kjør metoden til et annet felt*/
		class Trigger implements Operation {
			public final Point pos;
			public Trigger(Point pos) {
				this.pos = pos;
			}
	
			@Override
			public void perform(Tile rute, Mob enhet) {
				if (pos != null)
					rute = TileMap.get(pos);
				if (rute.mob() != null)
					enhet = rute.mob();
				if (rute.method != null)
					rute.method.call(rute, enhet);
			}
		}
	
	
		/**kall en annen metode*/
		class Call implements Operation {
			public final String metode;
			public Call(String metode) {
				this.metode = metode;
			}
	
			@Override
			public void perform(Tile rute, Mob enhet) {
				Method.call(metode, rute, enhet);
			}
		}
	
	
		/**teleporter*/
		class Move implements Operation {
			public final Point pos;
			public Move(Point pos) {
				this.pos = pos;
			}
			@Override
			public void perform(Tile rute, final Mob enhet) {
				if (pos != null)
					rute = TileMap.get(pos);
				if (enhet==null)
					throw Window.error("Metode.utfoor(): enhet==null");
				//Unngår å trigge felter, for hvis to felter teleporterer til hverandre ville det skapt en uendelig løkke.
				enhet.move(pos);
			}
		}

}
