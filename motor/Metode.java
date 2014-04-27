package motor;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Metode {
	static final Map<String,Metode> metoder = new HashMap<String,Metode>();
	public static Metode get(String metode) {
		if (metode==null)
			return null;
		Metode m = metoder.get(metode);
		if (m==null)
			throw Vindu.feil("Finner ikke metoden \"%s\"", metode);
		return m;
	}
	public static void kall(String metode, Rute rute, Enhet enhet) {
		Metode.get(metode).kall(rute, enhet);
	}
	public static void add(String linje) {
		Metode ny = new Metode(linje);
		metoder.put(ny.navn, ny);
	}


	public final String navn;
	private final Operasjon[] operasjoner;
	/**
	 * @param navn
	 * 
	 */
	public Metode(String linje) {
		final String mNavn = "\\w+";
		final String parametre = "(?:[\\w\\s,]|'.')*";
		//finne navn
		Matcher metode = Pattern.compile("^\\s*("+mNavn+")\\s*:((?:\\s*"+mNavn+"\\("+parametre+"\\);)*)\\s*$").matcher(linje);
		if (!metode.matches())
			Fil.feil("Uforsttaælig metode: \"%s\"", linje);
		navn = metode.group(1);

		//http://stackoverflow.com/questions/6835970/regular-expression-capturing-all-repeating-groups
		String[] navn = finnAlle(metode.group(2), "("+mNavn+")(?=\\("+parametre+"\\);)"/*""+mNavn+"(?=\\("+parametre+"\\);)"*/);
		String[] parameter = finnAlle(metode.group(2), "(?<="+mNavn+"\\()("+parametre+")(?=\\);)");
		operasjoner = new Operasjon[parameter.length];
		for (int i=0; i<navn.length; i++) {
			Operasjon o = null;
			//TODO: hvis koordinater mangler vil metoden kjøres på feltet som startet funksjonen.
			switch (navn[i]) {
			  case "sett":
				String[] param = param(parameter[i], "(\\d+)", "(\\d+)", "'(.)'");
				o = new Sett(point(param), param[2].charAt(0));
				break;
			  case "trigger": o=new Trigger(point(param(parameter[i], "(\\d+)", "(\\d+)")));  break;
			  case "kall": o=new Kall(param(parameter[i], "(\\w+)")[0]);  break;
			  case "flytt": o=new Flytt(point(param(parameter[i], "(\\d+)", "(\\d+)")));  break;
			  default:
				throw Fil.feil("Metode %s: Ukjent operasjon %s.", this.navn, navn[i]); 
			}
			operasjoner[i] = o;
		}
	}



	//hjelper-funksjoner til Metode()
	/**Returnerer en array med alle treff av regex i str.*/
	public String[] finnAlle(String str, String regex) {
		LinkedList<String> treff = new LinkedList<String>();
		Matcher m = Pattern.compile(regex).matcher(str);

		while (m.find())
			treff.add(m.group());
		return treff.toArray(new String[treff.size()]);
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
			throw Fil.feil("Metode %s: En funksjon har feil parametre \"(%s)\"", navn, str);
		String[] treff = new String[par.length];
		for (int i=0; i<par.length; i++)
			treff[i] = m.group(i+1);
		return treff;
	}
	private Point point(String[] treff) {
		Point p = new Point(
				Integer.valueOf(treff[0]),
				Integer.valueOf(treff[1])
			);
		Dimension d = Brett.dimensjoner();
		if (p.x<0 || p.y<0 || p.x>=d.width || p.y>=d.height)
			throw Fil.feil("Metode %s: koordinatene (%d,%d) er utenfor labyrinten.", navn, p.x, p.y);
		return p;
	}


	public void kall(Rute rute, Enhet enhet) {
		for (Operasjon op : operasjoner)
			op.utfør(rute, enhet);
	}


	protected static interface Operasjon {
		public void utfør(Rute rute, Enhet Enhet);
	}

	
	class Sett implements Operasjon {
		public final Point pos;
		public final Type type;
		public final String metode;
		public Sett(Point pos, char tegn) {
			this.pos = pos;
			type = Type._(tegn);
			if (type.metode)
				metode = String.valueOf(tegn);
			else
				metode = null;
		}

		@Override
		public void utfør(Rute rute, Enhet enhet) {
			if (pos != null)
				rute = Brett.get(pos);
			rute.setType(type);
			rute.metode = Metode.get(metode);
		}
	}


	/**Kjør metoden til et annet felt*/
	class Trigger implements Operasjon {
		public final Point pos;
		public Trigger(Point pos) {
			this.pos = pos;
		}

		@Override
		public void utfør(Rute rute, Enhet enhet) {
			if (pos != null)
				rute = Brett.get(pos);
			if (rute.enhet != null)
				enhet = rute.enhet;
			if (rute.metode != null)
				rute.metode.kall(rute, enhet);
		}
	}


	/**kall en annen metode*/
	class Kall implements Operasjon {
		public final String metode;
		public Kall(String metode) {
			this.metode = metode;
		}

		@Override
		public void utfør(Rute rute, Enhet enhet) {
			Metode.kall(metode, rute, enhet);
		}
	}


	/**teleporter*/
	class Flytt implements Operasjon {
		public final Point pos;
		public Flytt(Point pos) {
			this.pos = pos;
		}
		@Override
		public void utfør(Rute rute, final Enhet enhet) {
			if (pos != null)
				rute = Brett.get(pos);
			if (enhet==null)
				throw Vindu.feil("Metode.utfoor(): enhet==null");
			//Unngår å trigge felter, for hvis to felter teleporterer til hverandre ville det skapt en uendelig løkke.
			enhet.flytt(pos);
		}
	}
}
