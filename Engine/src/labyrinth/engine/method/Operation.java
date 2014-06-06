package labyrinth.engine.method;
import labyrinth.engine.Action;
import labyrinth.engine.Mob;
import labyrinth.engine.Tile;

//interface action in code called from code void(TIle, Mob)
/**interface operation in code called from compiled file Value(Tile, Mob) implements action*/
public interface Operation extends Action {
	Value perform(Tile t, Mob m);
	/**interface compiler in code called from file operation(Value[])*/
	//class procedure in file called from map and file implements operation with name
	//class method accepts compiler
	public static interface Compiler {
		Operation instance(Value[] params);
	}
}
