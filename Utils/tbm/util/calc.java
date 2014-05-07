package tbm.util;

import java.util.LinkedList;

public class calc { 
	public calc() {
		
	}


	public void solve(String str) {
		LinkedList<Character> pharanteses = new LinkedList<Character>();
		for (int i=0; i<str.length(); i++) {
			String number = "";
			while (str.substring(i, i).matches("[0-9]")) {
				number.concat(str.substring(i, i));
				i++;
			}
		}
	}


	public static void main() {
		
	}
}
