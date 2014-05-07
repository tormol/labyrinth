package tbm.util;
import java.io.Serializable;

//A simple wrapper for passing an immutable object by reference.
public class Reference<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	public T value;
	public Reference(T value)	{this.value = value;}
	public T get()				{return this.value;}
	public void set(T value)	{this.value = value;}
	public String toString()	{return String.valueOf(value);}

	/*
	public static class T implements Serializable{
		public T(T value)	{this.value = value;}
		private static final long serialVersionUID = 1L;
		public T value;
		public T get()				{return this.value;}
		public void set(T value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}*/

	public static class Boolean implements Serializable{
		private static final long serialVersionUID = 1L;
		public boolean value;
		public Boolean(boolean value)	{this.value = value;}
		public boolean get()				{return this.value;}
		public void set(boolean value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
	public static class Char implements Serializable{
		private static final long serialVersionUID = 1L;
		public char value;
		public Char(char value)	{this.value = value;}
		public char get()				{return this.value;}
		public void set(char value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
	public static class Byte implements Serializable{
		private static final long serialVersionUID = 1L;
		public byte value;
		public Byte(byte value)	{this.value = value;}
		public byte get()				{return this.value;}
		public void set(byte value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
	public static class Short implements Serializable{
		private static final long serialVersionUID = 1L;
		public short value;
		public Short(short value)	{this.value = value;}
		public short get()				{return this.value;}
		public void set(short value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
	public static class Int implements Serializable{
		private static final long serialVersionUID = 1L;
		public int value;
		public Int(int value)	{this.value = value;}
		public int get()				{return this.value;}
		public void set(int value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
	public static class Long implements Serializable{
		private static final long serialVersionUID = 1L;
		public long value;
		public Long(long value)	{this.value = value;}
		public long get()				{return this.value;}
		public void set(long value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
	public static class Float implements Serializable{
		private static final long serialVersionUID = 1L;
		public float value;
		public Float(float value)	{this.value = value;}
		public float get()				{return this.value;}
		public void set(float value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
	public static class Double implements Serializable{
		private static final long serialVersionUID = 1L;
		public double value;
		public Double(double value)	{this.value = value;}
		public double get()				{return this.value;}
		public void set(double value)	{this.value = value;}
		public String toString()	{return String.valueOf(value);}
	}
}
