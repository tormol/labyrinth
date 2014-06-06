package labyrinth.engine.method;
//import static tbm.util.statics.*;
import java.util.HashMap;
import java.util.Map;
import labyrinth.engine.Mob;
import labyrinth.engine.Tile;
import labyrinth.engine.Window;

public class Procedure implements Operation {
	static final Map<String,Procedure> procedures = new HashMap<String,Procedure>();

	public static Procedure get(String name) {
		Procedure p = procedures.get(name);
		if (p==null) {
			//declare it
			p = new Procedure(name, null);
			procedures.put(name, p);
		}
		return p;
	}

	public static void perform(String method, Tile tile, Mob mob) {
		Procedure.get(method).perform(tile, mob);
	}

	public static void define(String name, Iterable<Operation> ops) {
		Procedure p = procedures.get(name);
		if (p==null)
			procedures.put(name, new Procedure(name, ops));
		else
			p.operations = ops;
	}

	public static void checkUndefined() {
		for (Procedure p : procedures.values())
			if (p.operations == null)
				throw Window.error("The procedure %s is undefined.", p.name);
	}



	public final String name;
	private Iterable<Operation> operations;
	public Procedure(String name, Iterable<Operation> operations) {
		this.name = name;
		this.operations = operations;
		//make callable from other procedures
		new Method(name, new VType[0], VType.VOID, params->this);
	}

	@Override//Operation
	public Value perform(Tile tile, Mob mob) {
		for (Operation op : operations)
			op.perform(tile, mob);
		return Value.Void;
	}
}
