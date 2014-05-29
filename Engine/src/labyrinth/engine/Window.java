package labyrinth.engine;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Window {
	public static JFrame window = null;
	private static JLabel text = new JLabel();
	public static void start(String vinduTittel) {
		window = new JFrame(vinduTittel);
	}

	/**@param args første argument er et filnavn som lastes i stedet for å spørre.*/
	public static void display() {
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new BoxLayout(
				window.getContentPane(), BoxLayout.PAGE_AXIS
			));
		text.setEnabled(false);
		window.getContentPane().add(text);
		window.getContentPane().add(TileMap.panel);
		window.setFocusable(true);
		window.requestFocus();
		window.pack();
		window.setVisible(true);
	}


	public static void setText(String str) {
		setText(str, null);
	}
	public static void setText(String str, String tooltip) {
		text.setEnabled(true);
		text.setText(str);
		text.setToolTipText(tooltip);
	}
	public static void hideText() {
		text.setEnabled(false);
	}


	public static void won() {
		//TODO: last et nytt brett fra en konstant
		end("Du vant!");
	}
	public static void lost() {
		end("du tapte");
	}

	public static void end(String text) {
		for (Mob e : Mob.mobs)
			e.pause(true);
		JOptionPane.showMessageDialog(window,
				text, window.getTitle(), JOptionPane.PLAIN_MESSAGE
			);
		window.dispose();
	}

	/**Gjør det enkelt å gi en feilmelding
	 *Legger til linjenummer før feilmeldingen.*/
	public static ErrorDialog error(String f, Object... a) {
		if (MapFile.line == -1)
			return new ErrorDialog(f, a);
		return new ErrorDialog("Line %d: %s", MapFile.line, String.format(f, a));
	}


	@SuppressWarnings("serial")
	public static class ErrorDialog extends RuntimeException {
		private ErrorDialog(String f, Object... a) {
			this(String.format(f, a));
		}
		private ErrorDialog(String message) {
			JOptionPane.showMessageDialog(window,
					message, "Labyrint - feil", JOptionPane.ERROR_MESSAGE
				);
			//Siden det er flere tråder blir ikke programmet
			//avsluttet av et unntak i én tråd.
			if (window != null)
				window.dispose();
		}
	}
}
