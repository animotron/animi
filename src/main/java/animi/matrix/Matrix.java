package animi.matrix;

public interface Matrix { //XXX: <T> {

	//XXX: public interface Value<K> {
	//	public K get(int... dims);
	//}

	//XXX: public void init(Value<T> value);
	
	public void step();

	public int length();

	public int dimensions();

	public int dimension(int index);

	//XXX: public T getByIndex(int index);

	//XXX: public void setByIndex(T value, int index);

	//XXX: public T get(int... dims);

	//XXX: public void set(T value, int... dims);

	//XXX: public void fill(T value);

	//XXX: public int[] max();
	//XXX: public T maximum();

	//XXX: public Matrix<T> copy();

	//XXX: public MatrixProxy<T> sub(int... dims);

	public void debug(String comment);
}