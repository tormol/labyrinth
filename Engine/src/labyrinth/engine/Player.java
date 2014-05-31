package labyrinth.engine;
import static java.awt.event.KeyEvent.*; 

import javax.swing.SwingUtilities;

import tbm.util.geom.Direction;
import tbm.util.geom.Point;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

public class Player extends Mob implements KeyListener {
	public final Consumer<Player> moveTo;
	private boolean pause = false;
	/**Med en hammer er det fiendene som taper*/
	protected boolean hammer = false;

	public Player(String imagePath, Tile start, Consumer<Player> moveTo) {
		super("Player", imagePath);
		setTile(start);
		this.moveTo = moveTo;
		if (start!=null)
			tile().moveTo(this, false);
		Window.window.addKeyListener(this);
	}


	@Override//KeyListener
	public void keyPressed(KeyEvent e) {
		Direction direction = Direction.d(e.getKeyCode(), VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT);
		if (direction==null || pause) {
			if (e.getKeyCode() == VK_ESCAPE)
				Mob.pauseAll(!pause);
			return;
		}
		this.direction = direction;
		final Point newPos = tile().pos().move(direction);
		
		final Tile to = TileMap.get(newPos);
		if (to.canMoveTo(this, true)) {
			//is called from the eventqueue
			tile().moveFrom(true);
			setTile(to);
			tile().moveTo(this, true);
			if (moveTo != null)
				moveTo.accept(this);
		} else
			tile().repaint();
		LoS.triangle(tile().pos(), direction, (tile) -> tile.visible());
		TileMap.panel.repaint();
	}


	@Override//KeyListener
	public void keyReleased(KeyEvent e)
		{}//brukes ikke
	@Override//KeyListener
	public void keyTyped(KeyEvent e)
		{}//brukes ikke


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


	@Override
	public void move(final Point pos) {
		super.move(pos);
		if (pos != null)
			SwingUtilities.invokeLater(() -> LoS.triangle(tile().pos(), direction, (tile) -> tile.visible()));
	}


	@Override
	public void hit(Mob mob) {
		if (hammer)
			mob.remove();
		else
			Window.lost();
	}
	@Override
	/**@super*/
	public void pause(boolean pause) {
		this.pause = pause;
	}

	@Override
	/**removes KeyListener
	 * @super*/
	public void remove() {
		Window.window.removeKeyListener(this);
		super.remove();
	}
}
