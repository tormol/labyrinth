package labyrinth.engine;
import static java.awt.event.KeyEvent.*; 

import javax.swing.SwingUtilities;

import tbm.util.geom.Direction;
import tbm.util.geom.Point;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

public class Player extends Mob implements KeyListener {
	private static Tile finnStart() {
		LinkedList<Tile> startpunkt = new LinkedList<Tile>();
		for (Tile rute : TileMap.alle("start")) {
			if (rute.enhet() == null)
				startpunkt.add(rute);
			//gjør feltene rundt synlige
			TileMap.fjernDis(rute.pos());
		}
		if (startpunkt.size() > 1)
			throw Window.feil("Brettet har mer enn ett startpunkt.");
		if (startpunkt.size() == 0)
			throw Window.feil("Brettet mangler startpunkt.");
		return startpunkt.getFirst();
	}


	public final FlyttTil flyttTil;
	private boolean pause = false;
	/**Med en hammer er det fiendene som taper*/
	protected boolean hammer = false;

	public Player(String fil, Tile start, FlyttTil flyttTil) {
		super("Spiller", fil);
		setRute(start);
		this.flyttTil = flyttTil;
		if (start!=null)
			rute().flyttTil(this, false);
		Window.vindu.addKeyListener(this);
	}
	public Player(String fil, FlyttTil flyttTil) {
		this(fil, Player.finnStart(), flyttTil);
	}


	@Override//KeyListener
	public void keyPressed(KeyEvent e) {
		Direction retning = Direction.d(e.getKeyCode(), VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT);
		if (retning==null || pause) {
			if (e.getKeyCode() == VK_ESCAPE)
				Mob.pauseAlle(!pause);
			return;
		}
		this.retning = retning;
		final Point nyPos = rute().pos().move(retning);
		
		final Tile til = TileMap.get(nyPos);
		if (til.kanFlytteTil(this, true)) {
			//is called from the eventqueue
			rute().flyttFra(true);
			setRute(til);
			rute().flyttTil(this, true);
			if (flyttTil != null)
				flyttTil.flyttTil(this);
		} else
			rute().repaint();
		LoS.triangle(rute().pos(), retning, new LoS.Action() {public void action(Tile t) {
			t.vis();
		}});
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
			throw Window.feil("Spilleren har allerede en hammer.");
		hammer = true;
		Thread t = new Thread(new Runnable(){public void run(){
			try {
				Thread.sleep(millisekunder);
			} catch (InterruptedException e) {
			}
			hammer = false;
		}});
		t.setName("hammer");
		t.setDaemon(true);
		t.start();
	}


	@Override
	public void flytt(final Point pos) {
		super.flytt(pos);
		if (pos != null)
			SwingUtilities.invokeLater(new Runnable(){public void run() {
				LoS.triangle(rute().pos(), retning, new LoS.Action() {public void action(Tile t) {
					t.vis();
				}});
			}});
	}


	@Override
	public void truffet(Mob enhet) {
		if (hammer)
			enhet.fjern();
		else
			Window.tapte();
	}
	@Override
	/**@super*/
	public void pause(boolean pause) {
		this.pause = pause;
	}

	@Override
	/**Fjerner KeyListener
	 * @super*/
	public void fjern() {
		Window.vindu.removeKeyListener(this);
		super.fjern();
	}

	/**Lar programmer kjo/re egen kode naar spilleren flytter til et felt.*/
	public static interface FlyttTil {
		void flyttTil(Player spiller);
	}
}
