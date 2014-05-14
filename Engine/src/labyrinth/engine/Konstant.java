package labyrinth.engine;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Map;

/**Kjekt å ha, linjer som starter med $ leses som konstante variabler*/
public class Konstant {
	private static final Map<String, String> konstanter = new HashMap<String, String>();
	public static void add(String navn, String verdi) {
		if (!navn.matches("^\\w+$"))
			throw Vindu.feil("Ugyldig konstant-navn \"%s\"", navn);
		konstanter.put(navn, verdi);
	}
	public static void add(String linje) {
		Konstant ny = new Konstant(linje);
		konstanter.put(ny.navn, ny.verdi);
	}
	public static String get(String navn) {
		if (!navn.matches("\\w+"))
			throw Fil.feil("Ugyldig konstant-navn \"%s\"", navn);
		return konstanter.get(navn);
	}
	public static String fyllInn(String str) {
		StringBuilder b = new StringBuilder();
		String[] del = str.split("(?<!')\\$(?!')");
		for (int i=0; i<del.length; i++)
			if (i%2==0)
				b.append(del[i]);//ikke vaiabel
			else {
				String verdi = Konstant.get(del[i]);
				if (verdi==null)
					throw Fil.feil("Konstanten $%s er ikke definert.", del[i]);
				b.append(verdi);
			}
		return b.toString();
	}



	public final String navn;
	public final String verdi;
	Konstant(String linje) {
		Matcher matcher = Pattern.compile("^\\s*\\$\\s*(\\w+)\\s*=(.*?)$").matcher(linje);
		if (!matcher.matches())
			throw Vindu.feil("Ugyldig konstant \"%s\"", linje);
		navn = matcher.group(1);
		verdi = Konstant.fyllInn( matcher.group(2) );
	}
}
