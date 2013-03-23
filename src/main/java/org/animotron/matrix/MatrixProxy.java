/*
 *  Copyright (C) 2012 The Animo Project
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

import org.animotron.matrix.MatrixFloat.Integers;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MatrixProxy<T extends Number> implements Matrix<T> {
	
	interface Function {
		public void call(int[] pos);
	}
	
	interface PossitionHolderFunction extends Function {
		public int[] getPossition();
	}

	Matrix<T> matrix;
	
	int[] fixeDimensions;
	
	int length;
	
	protected MatrixProxy(Matrix<T> other, int ... dims) {
		
		if (other instanceof MatrixProxy) {
			throw new IllegalArgumentException();
		}
		
		fixeDimensions = new int[dims.length];
		System.arraycopy(dims, 0, fixeDimensions, 0, dims.length);
		
		length = 1;
		for (int i = fixeDimensions.length; i < other.dimensions(); i++) {
			length *= other.dimension(i);
		}
		
		matrix = other;
	}
	
	public void init(Value<T> value) {
		throw new IllegalArgumentException();
		
//		for (int i = 0; i < length; i++) {
//			matrix.data[offset + i] = value.get();
//		}
	}

	protected int[] dims(final int ... dims) {
		if (dims.length == matrix.dimensions()) {
			return dims;
		}

		int[] ds = new int[matrix.dimensions()];
		System.arraycopy(fixeDimensions, 0, ds, 0, fixeDimensions.length);
		System.arraycopy(dims, 0, ds, fixeDimensions.length, dims.length);
		
		return ds;
	}

	public T get(final int ... dims) {
		return matrix.get(dims(dims));
	}

	public void set(final T value, int ... dims) {
		matrix.set(value, dims(dims));
	}
	
	public void fill(final T value) {
		iterate(new Function() {
			@Override
			public void call(int[] pos) {
				matrix.set(value, pos);
			}
		});
	}
	
	public int[] max() {
		PossitionHolderFunction function = new PossitionHolderFunction() {
	     	
			int[] maxPos = new int[matrix.dimensions()];
	    	Number max = 0f;
	    	
	    	public void call(int[] pos) {
	    		if (compare(matrix.get(pos), max) == 1) {
	    			System.arraycopy(pos, 0, maxPos, 0, pos.length);
	    			max = matrix.get(pos);
	    		}
	    	}

			private int compare(Number n1, Number n2) {
				if (n1 instanceof Float) {
					return ((Float)n1).compareTo((Float)n2);
				}
				throw new IllegalArgumentException();
			}

			@Override
			public int[] getPossition() {
				return maxPos;
			}
		};
    	
		iterate(function);
    	
    	return function.getPossition();
	}
	
	public MatrixProxy<T> copy() {
		return new MatrixProxy<T>(matrix.copy(), fixeDimensions);
	}

	public MatrixProxy<T> sub(int ... dims) {
		throw new IllegalArgumentException();
	}
	
	private void iterate(Function function) {
    	int[] pos = new int[matrix.dimensions()];
    	Arrays.fill(pos, 0);
    	System.arraycopy(fixeDimensions, 0, pos, 0, fixeDimensions.length);

    	function.call(pos);

		float value = 0;
    	for (int i = 1; i < length; i++) {
    		for (int dim = matrix.dimensions() - 1; dim >= fixeDimensions.length; dim--) {
        		value = ++pos[dim];
        		
        		if (value >= matrix.dimension(dim)) {
        			pos[dim] = 0;
        			continue;
        		}
        		break;
    		}

        	function.call(pos);
    	}
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public int dimensions() {
		return matrix.dimensions() - fixeDimensions.length;
	}

	@Override
	public int dimension(int index) {
		return matrix.dimension(fixeDimensions.length + index);
	}

	@Override
	public T getByIndex(int index) {
		return matrix.get(pos(index));
	}

	@Override
	public void setByIndex(T value, int index) {
		matrix.set(value, pos(index));
	}

	@Override
	public void debug(String comment) {
		DecimalFormat df = new DecimalFormat("0.00000");

		System.out.println(comment);
		for (int index = 0; index < length(); index++) {
			System.out.print(df.format(getByIndex(index)));
			System.out.print(" ");
		}
		System.out.println();
	}
	
	private int[] pos(final int index) {
		int tmp = index;
		
		int offset = fixeDimensions.length;

    	int[] dims = new int[matrix.dimensions()];
		System.arraycopy(fixeDimensions, 0, dims, 0, offset);
		
    	int prev = 0, factor = 1;
    	for (int i = dimensions() - 1; i > 0; i--) {
    		factor = 1;
    		for (int z = 0; z < i; z++) {
    			factor *= dimension(z);
    		}

			dims[offset + i] = prev = (int)Math.floor(tmp / factor);
			tmp -= (prev * factor);
    	}
    	dims[offset] = tmp;
    	//maxPos must be zero
    	
//    	System.out.println(Arrays.toString(dims));;
    	return dims;
	}
}