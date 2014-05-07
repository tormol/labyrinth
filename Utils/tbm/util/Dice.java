package tbm.util;
public class Dice {
	public static int SIDES=6;
	public final int sides;
	public int roll() {
		return (int)(1 + sides*Math.random());
	}

	public Dice() {
		this(Dice.SIDES);
	}
	public Dice(int sides) {
		if (sides < 1)
			throw new IllegalArgumentException("sides<1");
		this.sides = sides;
	}


	public static class MultiDice {
		private final Dice[] dices; 
		public int roll() {
			int sum=0;
			for (Dice d : dices)
				sum += d.roll();
			return sum;
		}

		public MultiDice(int dices, Dice type) {
			this.dices = new Dice[dices];
			for (int i=0; i<this.dices.length; i++)
				this.dices[i] = type;
		}
		public MultiDice(Dice[] dices) {
			this.dices = dices;
		}

		public Dice[] getDices() {
			return dices.clone();
		}
		public int num() {
			return dices.length;
		}

		public int max() {
			int max=0;
			for (Dice d : dices)
				max += d.sides;
			return max;
		}
		public float average() {
			return (max()+min()) / num();
		}
		public int min() {
			return num();
		}
	}
}
