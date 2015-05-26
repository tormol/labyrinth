package tbm.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**This unused code was written for a codereview.stackexchange post (that I ofc never replied to) about the fastest way to display a number as three-digit groups.*/
public class format {
	public static final int max_Long_length = 26;//-9 223 372 036 854 775 808
	public static final int max_Int_length = 14;//-2 147 483 648
	public static final int max_Short_length = 7;//-65 536
	public static final int max_Byte_length = 4;//-256

	public static int spaceLong(char[] arr, long num, char delimiter) throws ArrayIndexOutOfBoundsException {
		int pos = arr.length;
		int digits = 0;
		boolean negative = num < 0;
		if (! negative)
			num = -num;
		try {
			while (num != 0) {
				arr[--pos] = (char) ('0' - num % 10);
				num /= 10;
				digits++;
				if (digits % 3  ==  0)
					arr[--pos] = delimiter;
			}
			if (negative)
				arr[--pos] = '-';
		} catch (ArrayIndexOutOfBoundsException e) {
			//num is too long for arr / arr is too small.
			//try to recover by removing spaces
			String without_spaces = String.valueOf(num);
			int len = without_spaces.length();
			if (len > arr.length)//still too long / recovery failed
				throw new ArrayIndexOutOfBoundsException(spaceLong(num, delimiter) + " exceeds the maximum " +arr.length+ " characters.");
			pos = arr.length - len;
			System.arraycopy(without_spaces.toCharArray(), 0, arr, pos, len);
		}
		return pos; 
	}


	/**1234->1 234, 2->2, -9876543->-9 876 543*/
	public static String spaceLong(long num, char delimiter) {
		char arr[] = new char[max_Long_length];
		int start = spaceLong(arr, num, delimiter);
		return String.valueOf(arr, start, arr.length-start);
	}

	/**1234->1 234, 2->2, -9876543->-9 876 543*/
	public static String spaceLong(long num, char delimiter, int pad_to_length, char pad_with) throws ArrayIndexOutOfBoundsException {
		char arr[] = new char[pad_to_length];
		int start = spaceLong(arr, num, delimiter);
		Arrays.fill(arr, 0, start, pad_with);
		return String.valueOf(arr);
	}


	public static String spaceLong(long num)
		{return spaceLong(num, ' ');}

	public static void main(String[] arg) throws IOException {
		System.out.println(spaceLong(Integer.MIN_VALUE));
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			while (true)
				System.out.println(spaceLong(Long.parseLong(br.readLine())));
		} catch (NumberFormatException nf)
			{}
	}
}
