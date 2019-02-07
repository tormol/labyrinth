package no.torbmol.labyrinth;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Entity {
	//private static ThreadGroup durationCounter;
	public final String name;
	public final BufferedImage image;
	//public final long duration;

	public Entity(String name, String imagePath) {
		this.name = name;
		try {
			this.image = ImageIO.read(this.getClass().getResourceAsStream("/images/" + imagePath));
		} catch (IOException e) {
			throw Window.error("Error while loading image for Entity(%s,%s):\n%s", name, imagePath, e.getMessage());
		}
	}
}
