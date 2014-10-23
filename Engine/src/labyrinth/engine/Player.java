package labyrinth.engine;
import static java.awt.event.KeyEvent.*; 

import javax.swing.SwingUtilities;

import tbm.util.geom.Direction;
import tbm.util.geom.Point;
import tbm.util.awtKeyListen;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class Player extends Mob implements awtKeyListen.Pressed {
	public final Consumer<Player> onMove;
	protected boolean hammer = false;

	public Player(String imagePath, Consumer<Player> moveTo) {
		super("Player", imagePath);
		this.onMove = moveTo;

		String viewDistance = Constant.get("synsvidde");
		if (viewDistance != null)
			if (viewDistance.trim().equals("av"))
				for (Tile tile : TileMap.all())
					tile.visible();
			else
				throw Window.error("Limited viewDistance is not supported yet.");
	}

	public void start() {
		Window.window.addKeyListener(this);
	}


	@Override//KeyListener
	public void keyPressed(KeyEvent e) {
		Direction direction = Direction.find(e.getKeyCode(), VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT);
		if (direction==Direction.NONE || pause) {
			if (e.getKeyCode() == VK_ESCAPE)
				Mob.pauseAll(!pause);
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
			if (onMove != null)
				onMove.accept(this);
		} else
			tile().repaint();
		LoS.triangle(tile().pos(), direction, (tile) -> tile.visible());
		TileMap.panel.repaint();
	}


	/**With an hammer, its the enemies who lose.
	 * Note that the hammer is invisible (TODO)
	 *@param millisekunder how long the hammer last*/
	public synchronized void hammer(final int millisekunder) {
		if (hammer)
			throw Window.error("Spilleren har allerede en hammer.");
		hammer = true;
		Thread t = new Thread(() -> {
			try {
				Thread.sleep(millisekunder);
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
		if (t != null)
			SwingUtilities.invokeLater(()->LoS.triangle(
					tile().pos(), direction,
					tile->tile.visible()));
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
