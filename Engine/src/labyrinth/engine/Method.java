package labyrinth.engine;
import tbm.util.geom.Point;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
	private final Operation[] operations;
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


	protected static interface Operation {
		public void perform(Tile tile, Mob mob);
	}

	
	class Set implements Operation {
		public final Point pos;
		public final Type type;
		public final String method;
		public Set(Point pos, char symbol) {
			this.pos = pos;
			type = Type.t(symbol);
			if (type.method)
				method = String.valueOf(symbol);
			else
				method = null;
		}

		@Override
		public void perform(Tile rute, Mob enhet) {
			if (pos != null)
				rute = TileMap.get(pos);
			rute.setType(type);
			rute.method = Method.get(method);
		}
	}


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

	enum parameter {
		POINT, STRING, ITEGER;
	}
}
