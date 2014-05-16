package tbm.util;

public class calc { 
	public calc() {
		
	}


	public void solve(String str) {
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
