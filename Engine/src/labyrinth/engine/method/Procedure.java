package labyrinth.engine.method;
import java.util.List;
//import static tbm.util.statics.*;

public class Procedure extends Value implements Operation {
	private Iterable<Operation> operations;
	public final String description;
	public final Scope s;
	public Procedure(Iterable<Operation> operations, String description) {
		super(VType.FUNC);
		this.description = description;
		this.operations = operations;
	}

	@Override//Operation, Value
	public Value perform() {
		for (Operation op : operations) {
			Value ret = op.perform();
			if (ret != Value.Void)
				Script.last = ret;
		}
		return Script.last;
	}

	@Override//Value
	public Value call(List<Value> param) {
		if (param.size() != 0)
			throw Script.error("this function takes no parameters");
		return perform();
	}
	
}
