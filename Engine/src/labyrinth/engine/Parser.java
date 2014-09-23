package labyrinth.engine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Parser extends tbm.util.Parser {
	public Parser(File file) throws FileNotFoundException {
		super(file, true);
	}
	public Parser(tbm.util.Parser p) {
		super(p, true, p.getLine(), p.getCol());
	}

	@Override/**@super Additionally skips comments.*/
	public Parser skip_whitespace(boolean newline) throws IOException {
		super.skip_whitespace(newline);
		if (ipeek() == '#')
			if (!newline)
				setPos(getLine(), length(getLine()));
			else
				do {
					line();
					super.skip_whitespace(newline);
				} while (ipeek() == '#');
		return this;
	}

	@Override
	public Parser clone() {
		return new Parser(this);
	}
}
