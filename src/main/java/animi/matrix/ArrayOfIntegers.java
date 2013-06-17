package animi.matrix;

public interface ArrayOfIntegers extends Matrix {

	public interface Value {
		public int[] get(int... dims);
	}

	public void init(Value value);
	
	public int[] getByIndex(int index);

	public void setByIndex(int[] value, int index);

	public int[] get(int... dims);

	public void set(int[] value, int... dims);

	public void fill(int[] value);

	public int[] maximum();
	public int[] max();

	public ArrayOfIntegers copy();

	public ArrayOfIntegersProxy sub(int... dims);
}