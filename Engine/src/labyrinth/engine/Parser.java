package labyrinth.engine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import labyrinth.engine.Window.ErrorDialog;

public class Parser extends tbm.util.Parser {
	public Parser(File file) throws FileNotFoundException {
		super(file);
	}

	@Override/**@super Additionally skips comments.*/
	public Parser sw() throws IOException {
		super.sw();
		while (ipeek(false) == '#') {
			line();
			super.sw();
		}
		return this;
	}

	public ErrorDialog error(String f, Object... a) {
		return Window.error("Line %d:%d: %s", getPos().line+1, getPos().col, String.format(f, a));
	}

	public int lineNumber() {
		return getPos().line;
	}
}
