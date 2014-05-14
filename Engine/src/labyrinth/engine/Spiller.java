package labyrinth.engine;
import static java.awt.event.KeyEvent.*; 
import javax.swing.SwingUtilities;
import tbm.util.geom.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

public class Spiller extends Enhet implements KeyListener {
	private static Rute finnStart() {
		LinkedList<Rute> startpunkt = new LinkedList<Rute>();
		for (Rute rute : Brett.alle("start")) {
			if (rute.enhet() == null)
				startpunkt.add(rute);
			//gjør feltene rundt synlige
			Brett.fjernDis(rute.pos());
		}
		if (startpunkt.size() > 1)
			throw Vindu.feil("Brettet har mer enn ett startpunkt.");
		if (startpunkt.size() == 0)
			throw Vindu.feil("Brettet mangler startpunkt.");
		return startpunkt.getFirst();
	}


	public final FlyttTil flyttTil;
	private boolean pause = false;
	/**Med en hammer er det fiendene som taper*/
	protected boolean hammer = false;

	public Spiller(String fil, Rute start, FlyttTil flyttTil) {
		super("Spiller", fil);
		setRute(start);
		this.flyttTil = flyttTil;
		if (start!=null)
			rute().flyttTil(this, false);
		Vindu.vindu.addKeyListener(this);
	}
	public Spiller(String fil, FlyttTil flyttTil) {
		this(fil, Spiller.finnStart(), flyttTil);
	}


	@Override//KeyListener
	public void keyPressed(KeyEvent e) {
		Retning retning = Retning.retning(e.getKeyCode(), VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		if (retning==null) {
			if (e.getKeyCode() == VK_ESCAPE)
				Enhet.pauseAlle(!pause);
			return;
		}
		if (pause)
			return;
		this.retning = retning;
		final Point nyPos = retning.flytt(rute().pos());
		
		final Rute til = Brett.get(nyPos);
		final Spiller denne = this;
		if (til.kanFlytteTil(denne, true))
			SwingUtilities.invokeLater(new Runnable(){public void run() {
				rute().flyttFra(true);
				setRute(til);
				rute().flyttTil(denne, true);
				Brett.fjernDis(nyPos);
				if (flyttTil != null)
					flyttTil.flyttTil(denne);
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
			throw Vindu.feil("Spilleren har allerede en hammer.");
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
				Brett.fjernDis(pos);
			}});
	}


	@Override
	public void truffet(Enhet enhet) {
		if (hammer)
			enhet.fjern();
		else
			Vindu.tapte();
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
		Vindu.vindu.removeKeyListener(this);
		super.fjern();
	}

	/**Lar programmer kjo/re egen kode naar spilleren flytter til et felt.*/
	public static interface FlyttTil {
		void flyttTil(Spiller spiller);
	}
}
