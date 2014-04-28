package motor;
import static java.lang.Math.*;

import java.awt.Point;

/***/
public class Punkt {
	public final int x,y;

	/***/
	public Punkt() {this(0, 0);}
	/***/
	public Punkt(int x, int y) {this.x=x; this.y=y;}
	/***/
	public Punkt(Point p) {this(p.x, p.y);}
	/***/
	public Punkt(Punkt p) {this(p.x, p.y);}

	/***/
	public Punkt pluss(int x, int y) {return new Punkt(this.x+x, this.y+y);}
	/***/
	public Punkt pluss(Punkt p) {return pluss(p.x, p.y);}
	/***/
	public Punkt minus(int x, int y) {return new Punkt(this.x-x, this.y-y);}
	/***/
	public Punkt minus(Punkt p) {return minus(p.x, p.y);}
	/***/
	public Punkt neg() {return new Punkt(-x, -y);}
	/***/
	public Punkt avstand(Punkt p) {return avstand(p.x, p.y);}
	/***/
	public Punkt avstand(int x, int y) {return minus(x, y);}

	/***/
	public int flyttAvstand(Punkt p) {return flyttAvstand(p.x, p.y);}
	/***/
	public int flyttAvstand(int x, int y) {
		return Math.abs(this.x-x) + Math.abs(this.y-y);
	}
	/***/
	public double abs() {return sqrt(x*x + y*y);}
	/***/
	public Punkt enhet() {return new Punkt((int)signum(x), (int)signum(y));}

	/***/
	public String toString() {return String.format("%d, %d", x, y);}
	/***/
	public Point toPoint() {return new Point(x, y);}
	/***/
	public int[] toArray() {return new int[]{x, y};}
}
	