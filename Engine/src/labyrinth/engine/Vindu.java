package labyrinth.engine;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Vindu {
	public static JFrame vindu = null;
	private static JLabel tekst = new JLabel();
	public static void start(String vinduTittel) {
		vindu = new JFrame(vinduTittel);
	}

	/**@param args første argument er et filnavn som lastes i stedet for å spørre.*/
	public static void vis() {
		vindu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		vindu.getContentPane().setLayout(new BoxLayout(
				vindu.getContentPane(), BoxLayout.PAGE_AXIS
			));
		tekst.setEnabled(false);
		vindu.getContentPane().add(tekst);
		vindu.getContentPane().add(Brett.panel);
		vindu.setFocusable(true);
		vindu.requestFocus();
		vindu.pack();
		vindu.setVisible(true);
	}


	public static void setTekst(String str) {
		setTekst(str, null);
	}
	public static void setTekst(String str, String tooltip) {
		tekst.setEnabled(true);
		tekst.setText(str);
		tekst.setToolTipText(tooltip);
	}
	public static void skjulTekst() {
		tekst.setEnabled(false);
	}


	public static void vant() {
		//TODO: last et nytt brett fra en konstant
		slutt("Du vant!");
	}
	public static void tapte() {
		slutt("du tapte");
	}

	public static void slutt(String tekst) {
		for (Enhet e :Enhet.enheter)
			e.pause(true);
		JOptionPane.showMessageDialog(vindu,
				tekst, vindu.getTitle(), JOptionPane.PLAIN_MESSAGE
			);
		vindu.dispose();
	}


	/**Gjør det enkelt å gi en feilmelding*/
	public static FeilMelding feil(String f, Object... a) {
		return new FeilMelding(String.format(f, a));
	}
	@SuppressWarnings("serial")
	public static class FeilMelding extends RuntimeException {
		private FeilMelding(String melding) {
			JOptionPane.showMessageDialog(vindu,
					melding, "Labyrint - feil", JOptionPane.ERROR_MESSAGE
				);
			//Siden det er flere tråder blir ikke programmet
			//avsluttet av et unntak i én tråd.
			if (vindu != null)
				vindu.dispose();
		}
	}
}
