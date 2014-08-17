package tbm.util;
import java.io.IOException;

public class parseNum<EX extends Throwable> {
	public static void main(String[] args) throws IOException {
		while (true) {
			try {
				System.out.println(signed(()->System.in.read(), true));
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
				if (e.getMessage().startsWith("Not"))
					return;
			}
			//stops at \r, skip the \n
			System.in.read();
		}
	}

	public static <EX extends Throwable> int signed(CharSupplier<EX> ch, boolean other_systems) throws EX, NumberFormatException {
		int first = ch.get();
		if (first == '-')
			return parse(ch, ch.get(), true, other_systems);
		return parse(ch, first, false, other_systems);
	}

	public static <EX extends Throwable> int unsigned(CharSupplier<EX> ch, boolean negative, boolean other_systems) throws EX, NumberFormatException {
		return parse(ch, ch.get(), negative, other_systems);
	}

	//c hold the first digit
	private static <EX extends Throwable> int parse(CharSupplier<EX> ch, int c, boolean negative, boolean other_systems) throws EX {
		long num=0;
		byte base=10;//plus one
		boolean no_digits = true;
		if (other_systems && c=='0')
			switch (c = ch.get()) {
			  case'x':base+=8+6;//10+14-6-2==16
			  case'b':base-=6;//10-6-2==2
			  case'o':base-=2;//10-2==8
			          c=ch.get();
			          if (negative)
			        	  throw new NumberFormatException("Negative numbers cannot use other bases");
			          break;
			  default:no_digits=false;
			}
		while (true) {
			if (c >= '0'  &&  c <= '9'  &&  c <= '0'+base)
				c -= '0';
			else {
				c |= 0x20; //make upper-case letters lower-case
				if (base == 16  &&  c >= 'a'  &&  c <= 'f')
					c = 10+c-'a';
				//invalid
				else if (no_digits)
					throw new NumberFormatException("Not a number.");
				else
					break;
			}
			num = num*base + c;
			no_digits = false;
			c = ch.get();
			if (num >= 0x80000000L)
				if (negative) {
					if (num != 0x80000000L)
						throw new NumberFormatException(-num+" is too small for an int");
				} else if (base == 10  ||  num > 0xffffffffL)
					throw new NumberFormatException(num+" is too big for an int");
		}
		if (negative)
			num = -num;
		return (int)num;
	}

	/*with goto
	private static <EX extends Throwable> int parse(CharSupplier<EX> ch, char first, boolean negative, boolean other_systems) throws EX {
		long num=0;
		char c = first;
		byte base=10;
		boolean hex = false;
		boolean no_digits = true;
		if negative loop
		if !other_systems loop
		if c != '0' loop
		c = ch.get()
		switch c 'x'->x 'b'->b 'o'->0
		no_digits = false;
		goto dig
	x   hex=true;
		base+=8;//10+8-6-2==10
	b   base-=6;//10-6-2==2
	o   base-=2;//10-2==8
		c=ch.get();
	lop	if !hex dig
		if c<'A' dig
		if c>'f' stp
		if c<'a' big
		c += 10-'a'+'0';
		goto dig
	big if c>'F' dig 
		c += 10-'A'+'0';
	dig	c -= '0';
	chk	if c<base add
	stp	if !no_digits ret
		throw new NumberFormatException("Not a number.");
	add no_digits = false;
		num = num*base + c;
		c = ch.get();
		if num<0x80000000L lop
		if negative neg
		if num>0xffffffffL err
		if hex lop
		if base != 10 lop
	err throw new NumberFormatException(num+" is too big for an int");
	neg if num==0x80000000L lop
		throw new NumberFormatException(-num+" is too small for an int");
	end	if !negative ret
		num = -num;
	ret	return (int)num;
	}*/

	static {
		int assumed_integer_size = 4;
		if (Integer.BYTES != assumed_integer_size)
			throw new RuntimeException("tbm.util.parseNum assumes Integer.BYTES==4");
	}
}
