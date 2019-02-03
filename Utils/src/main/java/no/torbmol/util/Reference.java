package no.torbmol.util;
import java.io.Serializable;

/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
public class Reference<T> implements Serializable {
	/**The referenced value.*/              public T value;
	/**@param value the initial value.*/    public Reference(T value)    {set(value);}
	/**@return the referenced value.*/      public T get()               {return this.value;}
	/**@return the new value.*/             public T set(T value)        {this.value = value; return get();}
	private static final long serialVersionUID = 1L;

	/**@return a new reference to the value.*/public static <T> Reference<T> to(T value)           {return new Reference<T>(value);}
	/**@return a new reference to the value.*/public static Reference_boolean to(boolean value)    {return new Reference_boolean(value);}
	/**@return a new reference to the value.*/public static Reference_char to(char value)          {return new Reference_char(value);}
	/**@return a new reference to the value.*/public static Reference_byte to(byte value)          {return new Reference_byte(value);}
	/**@return a new reference to the value.*/public static Reference_short to(short value)        {return new Reference_short(value);}
	/**@return a new reference to the value.*/public static Reference_int to(int value)            {return new Reference_int(value);}
	/**@return a new reference to the value.*/public static Reference_long to(long value)          {return new Reference_long(value);}
	/**@return a new reference to the value.*/public static Reference_float to(float value)        {return new Reference_float(value);}
	/**@return a new reference to the value.*/public static Reference_double to(double value)      {return new Reference_double(value);}
	///**@return a new reference to the value.*/public static Reference_T to(T value)            {return new Reference_T(value);}


	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_boolean implements Serializable {
		/**The referenced value.*/              public boolean value;
		/**@param value the initial value.*/    public Reference_boolean(boolean value)    {set(value);}
		/**@return the referenced value.*/      public boolean get()                       {return this.value;}
		/**@return the new value.*/             public boolean set(boolean value)          {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}
	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_char implements Serializable {
		/**The referenced value.*/              public char value;
		/**@param value the initial value.*/    public Reference_char(char value)          {set(value);}
		/**@return the referenced value.*/      public char get()                          {return this.value;}
		/**@return the new value.*/             public char set(char value)                {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}

	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_byte implements Serializable {
		/**The referenced value.*/              public byte value;
		/**@param value the initial value.*/    public Reference_byte(byte value)          {set(value);}
		/**@return the referenced value.*/      public byte get()                          {return this.value;}
		/**@return the new value.*/             public byte set(byte value)                {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}
	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_short implements Serializable {
		/**The referenced value.*/              public short value;
		/**@param value the initial value.*/    public Reference_short(short value)        {set(value);}
		/**@return the referenced value.*/      public short get()                         {return this.value;}
		/**@return the new value.*/             public short set(short value)              {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}
	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_int implements Serializable {
		/**The referenced value.*/              public int value;
		/**@param value the initial value.*/    public Reference_int(int value)            {set(value);}
		/**@return the referenced value.*/      public int get()                           {return this.value;}
		/**@return the new value.*/             public int set(int value)                  {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}
	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_long implements Serializable {
		/**The referenced value.*/              public long value;
		/**@param value the initial value.*/    public Reference_long(long value)          {set(value);}
		/**@return the referenced value.*/      public long get()                          {return this.value;}
		/**@return the new value.*/             public long set(long value)                {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}

	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_float implements Serializable {
		/**The referenced value.*/              public float value;
		/**@param value the initial value.*/    public Reference_float(float value)        {set(value);}
		/**@return the referenced value.*/      public float get()                         {return this.value;}
		/**@return the new value.*/             public float set(float value)              {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}
	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*/
	public static class Reference_double implements Serializable {
		/**The referenced value.*/              public double value;
		/**@param value the initial value.*/    public Reference_double(double value)      {set(value);}
		/**@return the referenced value.*/      public double get()                        {return this.value;}
		/**@return the new value.*/             public double set(double value)            {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}

	/*Quicker to copy/paste and search/replace than doing every change in eight places
	/**A simple wrapper for passing a value from a lambda to an outer scope or updating a returned immutable value.*
	public static class Reference_T implements Serializable {
		/**The referenced value.*               public T value;
		/**@param value the initial value.*     public Reference_T(T value)                {set(value);}
		/**@return the referenced value.*       public T get()                             {return this.value;}
		/**@return the new value.*              public T set(T value)                      {this.value = value; return get();}
		private static final long serialVersionUID = 1L;
	}*/
}
