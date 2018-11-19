package no.torbmol.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.LinkedTransferQueue;

public class awtKeyListen {
	public static interface Pressed extends KeyListener {
		@Override default void keyReleased(KeyEvent e) {}
		@Override default void keyTyped(KeyEvent e) {}
	}
	public static interface Released extends KeyListener {
		@Override default void keyPressed(KeyEvent e) {}
		@Override default void keyTyped(KeyEvent e) {}
	}
	public static interface Typed extends KeyListener {
		@Override default void keyPressed(KeyEvent e) {}
		@Override default void keyReleased(KeyEvent e) {}
	}

	public static class PressListener {
		LinkedTransferQueue<KeyEvent> queue = new LinkedTransferQueue<>();
	}
}
