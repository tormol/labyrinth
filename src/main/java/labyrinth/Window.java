package labyrinth;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Window {
	public static JFrame window = null;
	private static JLabel text = new JLabel();
	public static void start(String windowTitle) {
		window = new JFrame(windowTitle);
	}

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
		end("You won!");
	}
	public static void lost() {
		end("You lost");
	}

	public static void end(String text) {
		for (Mob unit : Mob.mobs) {
			unit.pause(true);
		}
		JOptionPane.showMessageDialog(window,
				text, window.getTitle(), JOptionPane.PLAIN_MESSAGE
			);
		window.dispose();
	}

	/**An easy way to give an error message and exit.*/
	public static ErrorDialog error(String f, Object... a) {
		return new ErrorDialog(f, a);
	}


	public static class ErrorDialog extends RuntimeException {
		private ErrorDialog(String f, Object... a) {
			this(String.format(f, a));
		}
		private ErrorDialog(String message) {
			JOptionPane.showMessageDialog(window,
					message, "Labyrinth - error", JOptionPane.ERROR_MESSAGE
				);
			//this exception won't stop other threads,
			//Don't know whether this code does that, or I got stuck.
			if (window != null) {
				window.dispose();
			}
		}
		private static final long serialVersionUID = 1L;
	}
}
