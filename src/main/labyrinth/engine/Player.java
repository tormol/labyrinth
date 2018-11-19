package labyrinth.engine;
import static java.awt.event.KeyEvent.*; 
import javax.swing.SwingUtilities;
import tbm.util.geom.Direction;
import tbm.util.geom.Point;
import tbm.util.awtKeyListen;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import labyrinth.engine.method.Script;
import labyrinth.engine.method.Value;
import labyrinth.engine.method.Value.*;

public class Player extends Mob implements awtKeyListen.Pressed {
	/**Get the (first) Player object from Mob.mobs
	 *@return null if no player is found.*/
	public static Player get() {
		for (Mob m : Mob.mobs) {
			if (m instanceof Player) {
				return (Player)m;
			}
		}
		return null;
	}

	public final Consumer<Player> onMove;
	protected boolean hammer = false;

	public Player(String imagePath, Consumer<Player> moveTo) {
		super("Player", imagePath);
		this.onMove = moveTo;

		//Make all tiles visible if the variable "viewDistance" is false or "disabled"
		//or (TODO) limit line of sight to n tiles if it's an integer.
		VRef vd_var = Script.scr.root.get_variable("viewDistance");
		if (vd_var != null) {
			Value vd = vd_var.getRef();
			if (vd == Value.False  ||  (vd instanceof VString && vd.String().equals("disabled")))
				for (Tile tile : TileMap.all())
					tile.visible();
			else if (vd instanceof VInt)
				throw Window.error("Limited viewDistance is not supported yet.");
			else
				throw Window.error("viewDistance has wrong type.");
		}
	}

	public void start() {
		Window.window.addKeyListener(this);
	}


	@Override//KeyListener
	public void keyPressed(KeyEvent e) {
		Direction direction = Direction.find(e.getKeyCode(), VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT);
		if (direction==Direction.NONE || pause) {
			if (e.getKeyCode() == VK_ESCAPE) {
				Mob.pauseAll(!pause);
			}
			return;
		}
		this.direction = direction;
		final Point newPos = tile().pos().plus(direction);
		
		final Tile to = TileMap.get(newPos);
		if (to.canEnter(this, true)) {
			//is called from the eventqueue
			tile().leave(true);
			setTile(to);
			tile().enter(this, true);
			if (onMove != null) {
				onMove.accept(this);
			}
		} else {
			tile().repaint();
		}
		LoS.triangle(tile().pos(), direction, (tile) -> tile.visible());
		TileMap.panel.repaint();
	}


	/**With an hammer, its the enemies who lose.
	 * Note that the hammer is invisible (TODO)
	 *@param millis how long the hammer last*/
	public synchronized void hammer(final int millis) {
		if (hammer) {
			throw Window.error("Player alreadyhas a hammer.");
		}
		hammer = true;
		Thread t = new Thread(() -> {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e)
				{}
			hammer = false;
		});
		t.setName("hammer");
		t.setDaemon(true);
		t.start();
	}


	@Override//Mob
	public void moveTo(Tile t) {
		super.moveTo(t);
		if (t != null) {
			SwingUtilities.invokeLater(() -> LoS.triangle(
					tile().pos(),
					direction,
					tile -> tile.visible()
			));
		}
	}


	@Override//Mob
	public void hit(Mob mob) {
		if (hammer)
			mob.remove();
		else
			Window.lost();
	}
	@Override//Mob
	/**@super*/
	public void pause(boolean pause) {
		this.pause = pause;
	}

	@Override//Mob
	/**removes KeyListener
	 * @super*/
	public void remove() {
		Window.window.removeKeyListener(this);
		super.remove();
	}
}
