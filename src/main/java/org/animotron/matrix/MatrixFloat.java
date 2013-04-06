/*
 *  Copyright (C) 2012-2013 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animi.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.animotron.matrix;

import java.text.DecimalFormat;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MatrixFloat implements Matrix<Float> {
	
	int[] dimensions;
	
	float[] data;
	BitSet isSet;
	
	public MatrixFloat(final int ... dims) {
		dimensions = new int[dims.length];
		System.arraycopy(dims, 0, dimensions, 0, dims.length);
		
		int length = 1;
		for (int i = 0; i < dims.length; i++) {
			length *= dims[i];
		}
		
		data = new float[length];
		isSet = new BitSet(length);
	}
	
	public MatrixFloat(final MatrixFloat source) {
		dimensions = new int[source.dimensions.length];
		System.arraycopy(source.dimensions, 0, dimensions, 0, source.dimensions.length);
		
		data = new float[source.data.length];
		System.arraycopy(source.data, 0, data, 0, source.data.length);

		isSet = new BitSet(source.data.length);
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#init(org.animotron.animi.cortex.Matrix.Value)
	 */
	@Override
	public void init(final Value<Float> value) {
		for (int i = 0; i < data.length; i++) {
			data[i] = value.get();
		}
		isSet.clear(0, isSet.size() - 1);
	}
	
	@Override
	public void step() {
		isSet.clear(0, isSet.size() - 1);
		Arrays.fill(data, 0f);
	}

	protected int index(final int ... dims) {
		if (dims.length != dimensions.length) {
			throw new IndexOutOfBoundsException("Matrix have "+dimensions.length+" dimensions, but get "+dims.length+".");
		}
		
		for (int i = 0; i < dims.length; i++) {
			if (dims[i] >= dimensions[i]) {
				throw new IndexOutOfBoundsException("Matrix's "+(i+1)+" dimension have "+dimensions[i]+" elements, but requested "+dims[i]+" element.");
			}
		}
		
		int index = 0;
		for (int i = dims.length - 1; i > 0 ; i--) {
			index = (index + dims[i]) * dimensions[i-1];
		}
		return index + dims[0];
	}
	
	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#length()
	 */
	@Override
	public int length() {
		return data.length;
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#dimensions()
	 */
	@Override
	public int dimensions() {
		return dimensions.length;
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#dimension(int)
	 */
	@Override
	public int dimension(final int index) {
		return dimensions[index];
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#getByIndex(int)
	 */
	@Override
	public Float getByIndex(final int index) {
		return data[index];
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#setByIndex(float, int)
	 */
	@Override
	public void setByIndex(final Float value, final int index) {
		data[index] = value;
		isSet.set(index);
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#get(int)
	 */
	@Override
	public Float get(final int ... dims) {
		return data[index(dims)];
	}

	public boolean isSet(final int ... dims) {
		return isSet(index(dims));
	}
	
	public boolean isSet(final int index) {
		return isSet.get(index);
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#set(float, int)
	 */
	@Override
	public void set(final Float value, final int ... dims) {
		this.setByIndex(value, index(dims));
	}
	
	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#fill(float)
	 */
	@Override
	public void fill(final Float value) {
		Arrays.fill(data, value);
	}
	
	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#max()
	 */
	@Override
	public int[] max() {
    	int maxPos = -1;
    	float max = 0;
    	for (int pos = 0; pos < data.length; pos++) {
    		if (data[pos] > max) {
    			maxPos = pos;
    			max = data[pos];
    		}
    	}
    	
    	if (maxPos == -1) {
    		return null;
//    		throw new IllegalArgumentException("Maximum value can't be found.");
    	}
    	
    	int[] dims = new int[dimensions.length];
    	
//		System.out.print("Max "+maxPos+" ");

    	int prev = 0, factor = 1;
    	for (int i = dimensions.length - 1; i > 0; i--) {
    		factor = 1;
    		for (int z = 0; z < i; z++) {
    			factor *= dimensions[z];
    		}

			dims[i] = prev = (int)Math.floor(maxPos / factor);
			maxPos -= (prev * factor);
    	}
    	dims[0] = maxPos;
    	//maxPos must be zero
    	
//    	System.out.println(Arrays.toString(dims));;
    	return dims;
	}
	
	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#copy()
	 */
	@Override
	public Matrix<Float> copy() {
		return new MatrixFloat(this);
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#sub(int)
	 */
	@Override
	public MatrixProxy<Float> sub(int ... dims) {
		return new MatrixProxy<Float>(this, dims);
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#debug(java.lang.String)
	 */
	@Override
	public void debug(String comment) {
		System.out.println(comment);
		
		debug(new Floats(data), false);
	}
	
	protected void debug(List<?> array, boolean direct) {
		int[] pos = new int[dimensions.length];
		Arrays.fill(pos, 0);
		
		int value;
		
		DecimalFormat df;
		if (array instanceof Integers) {
			df = new DecimalFormat("0");
		} else {
			df = new DecimalFormat("0.00000");
		}
		
		System.out.print(Arrays.toString(pos));
		System.out.print(" ");
		System.out.print(df.format(direct ? array.get(0) : get(pos)));
		System.out.print(" ");
		
		boolean print = false;

    	for (int index = 1; index < array.size(); index++) {
    		for (int dim = dimensions.length - 1; dim >= 0; dim--) {
        		value = ++pos[dim];
        		
        		if (value >= dimensions[dim]) {
        			pos[dim] = 0;
        			
        			if (dim == dimensions.length - 1) {
        				print = true;
        			}
        			continue;
        		}
        		break;
    		}
    		
    		if (print) {
    			print = false;
				System.out.println();
				System.out.print(Arrays.toString(pos));
				System.out.print(" ");
    		}

			System.out.print(df.format(direct ? array.get(index) : get(pos)));
			System.out.print(" ");
		}
    	System.out.println();
	}
	
    protected class Floats extends AList<Float>  {

	    private final float[] a;
	
	    Floats(float[] data) {
	        if (data==null)
	            throw new NullPointerException();
	        a = data;
	    }
	
	    public int size() {
	        return a.length;
	    }
	
	    public Float get(int index) {
	        return Float.valueOf(a[index]);
	    }
	}

    protected class Integers extends AList<Integer>  {

	    private final int[] a;
	
	    Integers(int[] data) {
	        if (data==null)
	            throw new NullPointerException();
	        a = data;
	    }
	
	    public int size() {
	        return a.length;
	    }
	
	    public Integer get(int index) {
	        return Integer.valueOf(a[index]);
	    }
	}

    private abstract class AList<T> extends AbstractList<T>  {

	    public Object[] toArray() {
	    	throw new IllegalArgumentException();
	    }
	
	    @SuppressWarnings("hiding")
		public <T> T[] toArray(T[] a) {
	    	throw new IllegalArgumentException();
	    }
	
	    public T set(int index, T element) {
	    	throw new IllegalArgumentException();
	    }
	
	    public int indexOf(Object o) {
	    	throw new IllegalArgumentException();
	    }
	
	    public boolean contains(Object o) {
	    	throw new IllegalArgumentException();
	    }
	}
}
