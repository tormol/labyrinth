package labyrinth.engine;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayDeque;
import java.util.Queue;
import tbm.util.geom.Point;


/**Originalt var denne ikke statisk, men siden jeg aldri vil trenge mer enn en instans, og den brukes overalt ble det tungvindt å sende den ene instansen rundt til alle metoder.
 * Holder alle rutene på skjermen.*/
public class TileMap {
	private static Tile[][] brett;
	/**Panelet som inneholder rutene*/
	public static final JPanel panel = new JPanel(); 
	/**Hvor mange felt spilleren ser i hver retning. 0=ser alle*/
	private static int synsvidde = 2;
	/**Er en linket liste*/
	private static FinnMetode finnMetode = null;
	

	/**Lager et tomt brett*/
	public static void start(int _x, int _y) {
		char[][] brett = new char[_y][_x];
		for (int y=0; y<_y; y++)
			for (int x=0; x<_x; x++)
				brett[y][x] = ' ';
		start(brett);
	}
	/***/
	public static void start(String[] linjer) {
		char[][] tegn = new char[linjer.length][];
		for (int i=0; i<tegn.length; i++)
			tegn[i] = linjer[i].toCharArray();
		start(tegn);
	}
	public static void start(Queue<char[]> tegn) {
		start(tegn.toArray(new char[tegn.size()][]));
	}
	public static void start(char[][] tegn) {
		Type.add("utenfor", true, false, null, Color.CYAN, "");
		int kolonner = tegn[0].length;
		brett = new Tile[tegn.length][kolonner];
		panel.setLayout(new GridLayout( brett.length, brett[0].length));

		//lager Ruter
		for (int y=0;  y<brett.length;  y++, MapFile.linje++) {
			if (tegn[y].length != kolonner)
				throw MapFile.feil("lengden passynsvidde ikke med resten.");
			for (int x=0; x<brett[0].length; x++) {
				Type type = Type.get(tegn[y][x]);
				if (type == null)
					throw MapFile.feil("Kolonne %d: Ugyldig tegn '%c'", x, tegn[y][x]);
				brett[y][x] = new Tile(type, new Point(x, y));
				if (type.metode)
					finnMetode = new FinnMetode(tegn[y][x], brett[y][x], finnMetode);
				panel.add(brett[y][x]);
			}
		}
	}

	
	/**returnerer ruten i rad y, kolonner x
	 * hvis koordinatene er utenfor brettet returneres en ny rute av type vegg*/
	public static Tile get(Point p) {
		return get(p.x, p.y);
	}
	/**returnerer ruten i rad y, kolonner x
	 * hvis koordinatene er utenfor brettet returneres en ny rute av type vegg*/
	public static Tile get(int x, int y) {
		if (y < 0  ||  y >= brett.length  ||  x < 0  ||  x >= brett[0].length)
			//På denne måten slipper jeg å sjekke om jeg er utenfor brettet andre steder.
			return new Tile(Type.t("utenfor"), null);
		return brett[y][x];
	}

	/**Gjør alle ruter i et kvadrat med radius Brett.synsvidde sentrert på p synlige
	 * Må kalles fra SwingUtilities.invokeLater()*/
	public static void fjernDis(Point p) {
		for (int x=p.x-synsvidde; x<=p.x+synsvidde; x++)
			for (int y=p.y-synsvidde; y<=p.y+synsvidde; y++)
				get(x, y).vis();
	}

	public static Dimension dimensjoner() {
		return new Dimension(brett[0].length, brett.length);
	}
	/**Returnerer hvor mange ruter det er på brettet.*/
	public static int antallRuter() {
		return brett[0].length * brett.length;
	}

	/**Returnerer alle rutene på brettet*/
	public static Tile[] alle() {
		Tile[] alle = new Tile[antallRuter()];
		int pos=0;
		for (Tile[] rad : brett) {
			System.arraycopy(rad, 0, alle, pos, rad.length);
			pos += rad.length;
		}
		return alle;
	}

	public static Queue<Tile> alle(String type) {
		return alle(Type.t(type));
	}
	/**Returnerer alle ruter av typen*/
	public static Queue<Tile> alle(Type type) {
		ArrayDeque<Tile> ruter = new ArrayDeque<Tile>(antallRuter());
		for (Tile[] rad : brett)
			for (Tile rute : rad)
				if (rute.getType() == type)
					ruter.add(rute);
		//TODO: ruter.trim();
		return ruter;
	}

	public static void synsvidde(int synsvidde) {
		if (synsvidde<0)
			throw MapFile.feil("Brett.synsvidde kan ikke være negativ");
		TileMap.synsvidde = synsvidde;
		if (synsvidde==0)
			for (Tile rute : alle())
				rute.vis();
	}

	/**Metode trenger å vite størrelsen på brettet for å sjekke at koordinater er gyldige, Brett trenger Metode for å lage ruter med metorer
	 * Løsning: start brett, les inn metoder, legg metoder irutene med finnMetoder()*/
	public static void finnMetoder() {
		for (; finnMetode != null;  finnMetode = finnMetode.neste)
			finnMetode.rute.metode = Method.get( String.valueOf(finnMetode.metode) );
	}
}

/**For å legge metoder til ruter i labyrinten før metodene er lest inn.*/
class FinnMetode {
	/**Linket liste*/
	public final FinnMetode neste;
	public final char metode;
	public final Tile rute;
	public FinnMetode(char metode, Tile rute, FinnMetode neste) {
		this.metode = metode;	this.rute = rute;	this.neste = neste;
	}
}
