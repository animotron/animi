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
import java.util.Arrays;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MatrixArrayInteger implements Matrix<Integer[]> {
	
	int[] dimensions;
	
	Integer[][] data;
	
	public MatrixArrayInteger(int arraysize, int ... dims) {
		dimensions = new int[dims.length];
		System.arraycopy(dims, 0, dimensions, 0, dims.length);
		
		int length = 1;
		for (int i = 0; i < dims.length; i++) {
			length *= dims[i];
		}
		
		data = new Integer[length][arraysize];
	}
	
	public MatrixArrayInteger(MatrixArrayInteger source) {
		throw new IllegalArgumentException();
//		dimensions = new int[source.dimensions.length];
//		System.arraycopy(source.dimensions, 0, dimensions, 0, source.dimensions.length);
//		
//		data = new int[source.data.length];
//		System.arraycopy(source.data, 0, data, 0, source.data.length);
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#init(org.animotron.animi.cortex.Matrix.Value)
	 */
	@Override
	public void init(Value<Integer[]> value) {
		for (int i = 0; i < data.length; i++) {
			Integer[] v = value.get();
			for (int j = 0; j < v.length; j++)
			data[i][j] = v[j];
		}
	}
	
	@Override
	public void step() {
	}

	protected int index(int ... dims) {
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
	public int dimension(int index) {
		return dimensions[index];
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#getByIndex(int)
	 */
	@Override
	public Integer[] getByIndex(int index) {
		return data[index];
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#setByIndex(float, int)
	 */
	@Override
	public void setByIndex(Integer[] value, int index) {
		for (int i = 0; i < value.length; i++) {
			data[index][i] = value[i];
		}
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#get(int)
	 */
	@Override
	public Integer[] get(int ... dims) {
		return data[index(dims)];
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#set(float, int)
	 */
	@Override
	public void set(Integer[] value, int ... dims) {
		data[index(dims)] = value;
	}
	
	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#fill(float)
	 */
	@Override
	public void fill(Integer[] value) {
		Arrays.fill(data, value);
	}
	
	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#max()
	 */
	@Override
	public int[] max() {
		throw new IllegalArgumentException();
	}
	
	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#maximum()
	 */
	@Override
	public Integer[] maximum() {
		throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#copy()
	 */
	@Override
	public MatrixArrayInteger copy() {
		throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#sub(int)
	 */
	@Override
	public MatrixProxy<Integer[]> sub(int ... dims) {
		return new MatrixProxy<Integer[]>(this, dims);
	}

	/* (non-Javadoc)
	 * @see org.animotron.animi.cortex.Matrix#debug(java.lang.String)
	 */
	@Override
	public void debug(String comment) {
		System.out.println(comment);
		
		int[] pos = new int[dimensions.length];
		Arrays.fill(pos, 0);
		
		int value;
		
		DecimalFormat df = new DecimalFormat("0.00000");
		
		System.out.print(Arrays.toString(pos));
		System.out.print(" ");
		debugValue( df, get(pos) );
		System.out.print(" ");
		
		boolean print = false;

    	for (int i = 1; i < data.length; i++) {
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

    		debugValue( df, get(pos) );
			System.out.print(" ");
		}
    	System.out.println();
	}
	
	private void debugValue(DecimalFormat df, Object val) {
		if (val.getClass().isArray()) {
			for (Object obj : (Object[])val) {
				System.out.print(obj);
				System.out.print(" ");
			}
		} else {
			System.out.print(df.format(val));
		}
	}
}
