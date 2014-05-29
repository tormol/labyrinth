package labyrinth.engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Entity {
	//private static ThreadGroup durationcounter; 
	public final String name;
	public final BufferedImage image;
	//public final long duration;
	
	public Entity(String name, String imagePath) {
		this.name = name;
		try {
			this.image = ImageIO.read(new File(imagePath));
		} catch (IOException e) {
			throw Window.error("Error while loading image for Entity(%s,%s):\n%s", name, imagePath, e.getMessage());
		}
	}
}
