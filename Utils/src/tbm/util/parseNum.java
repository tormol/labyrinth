package tbm.util;
public class parseNum<EX extends Throwable> {
	public static interface CharSupplier<EX extends Throwable> {
		char get() throws EX;
	}

	public static <EX extends Throwable> int signed(CharSupplier<EX> ch, boolean other_systems) throws EX, NumberFormatException {
		char first = ch.get();
		if (first == '-')
			return parse(ch, ch.get(), true, other_systems);
		return parse(ch, first, false, other_systems);
	}

	public static <EX extends Throwable> int unsigned(CharSupplier<EX> ch, boolean negative, boolean other_systems) throws EX, NumberFormatException {
		return parse(ch, ch.get(), negative, other_systems);
	}

	private static <EX extends Throwable> int parse(CharSupplier<EX> ch, char first, boolean negative, boolean other_systems) throws EX {
		long num=0;
		char c = first;
		byte base=10;
		boolean hex = false;
		if (!negative && other_systems && c=='0')
			switch (c = ch.get()) {
			  case'x':hex=true;
				      base+=8;//10+8-6-2==10
			  case'b':base-=6;//10-6-2==2
			  case'o':base-=2;//10-2==8
			          c=ch.get();
			  case'1':case'2':case'3':case'4':case'5':
			  case'6':case'7':case'8':case'9':case'0':
			          break;
			  default:return 0;
			}
		while (true) {
			if (hex)
				if (c >= 'a'  && c <= 'f')
					c += 10-'a'+'0';
				else if (c >= 'A'  && c <= 'F')
					c += 10-'A'+'0';
			c -= '0';
			if (c >= base) {
				if (num == 0)//not a single valid digit
					throw new NumberFormatException("Not a number.");
				break;
			}
			num = num*base + c;
			if (num <  0x80000000L
			 ||(num <= 0xffffffffL  &&  base != 10)
			 ||(num == 0x80000000L  &&  negative)) {
				c = ch.get();
				continue;
			}
			if (negative)
				throw new NumberFormatException(num+" is too small for an int");
			throw new NumberFormatException(num+" is too big for an int");
		}
		if (negative)
			num = -num;
		return (int)num;
	}

	static {
		int assumed_integer_size = 4;
		if (Integer.BYTES != assumed_integer_size)
			throw new RuntimeException("tbm.util.parseNum assumes Integer.BYTES==4");
	}
}
