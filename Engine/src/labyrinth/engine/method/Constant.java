package labyrinth.engine.method;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Map;

import labyrinth.engine.Window;

/**Kjekt å ha, linjer som starter med $ leses som konstante variabler*/
public class Constant {
	private static final Map<String, String> constants = new HashMap<String, String>();
	public static void add(String name, String value) {
		if (!name.matches("^\\w+$"))
			throw Window.error("Ugyldig konstant-navn \"%s\"", name);
		constants.put(name, value);
	}
	public static void add(String line) {
		Constant ny = new Constant(line);
		constants.put(ny.name, ny.value);
	}
	public static String get(String name) {
		if (!name.matches("\\w+"))
			throw Window.error("Ugyldig konstant-navn \"%s\"", name);
		return constants.get(name);
	}
	public static String fillIn(String str) {
		StringBuilder b = new StringBuilder();
		String[] part = str.split("(?<!')\\$(?!')");
		for (int i=0; i<part.length; i++)
			if (i%2==0)
				b.append(part[i]);//ikke vaiabel
			else {
				String value = Constant.get(part[i]);
				if (value==null)
					throw Window.error("Konstanten $%s er ikke definert.", part[i]);
				b.append(value);
			}
		return b.toString();
	}



	public final String name;
	public final String value;
	Constant(String line) {
		Matcher matcher = Pattern.compile("^\\s*\\$\\s*(\\w+)\\s*=(.*?)$").matcher(line);
		if (!matcher.matches())
			throw Window.error("Ugyldig konstant \"%s\"", line);
		name = matcher.group(1);
		value = Constant.fillIn( matcher.group(2) );
	}
}
