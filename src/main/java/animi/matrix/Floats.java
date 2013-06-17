package animi.matrix;

public interface Floats extends Matrix {

	public interface Value {
		public float get(int... dims);
	}

	public void init(Value value);
	
	public float getByIndex(int index);

	public void setByIndex(float value, int index);

	public float get(int... dims);

	public void set(float value, int... dims);

	public boolean isSet(int ... dims);
	public boolean isSet(int index);

	public void fill(float value);

	public float maximum();
	public int[] max();

	public Floats copy();

	public FloatsProxy sub(int... dims);
}