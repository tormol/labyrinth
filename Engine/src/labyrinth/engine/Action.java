package labyrinth.engine;

/**interface action in code called from code void(TIle, Mob)*/
//interface operation in code called from compiled file Value(Tile, Mob) implements action
//interface compiler in code called from file operation(Value[])
//class procedure in file called from map and file implements operation with name
//class method accepts compiler
public interface Action {
	/**@return null (this way subinterfaces can return something).*/
	Object perform(Tile t, Mob m);
}
