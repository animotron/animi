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
package org.animotron.animi.cortex;

import java.util.Arrays;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MatrixProxy extends Matrix {
	
	Matrix matrix;
	
	int[] fixeDimensions;
	
	int length;
	
	protected MatrixProxy(Matrix other, int ... dims) {
		
		if (other instanceof MatrixProxy) {
			throw new IllegalArgumentException();
		}
		
		fixeDimensions = new int[dims.length];
		System.arraycopy(dims, 0, fixeDimensions, 0, dims.length);
		
		length = 1;
		for (int i = fixeDimensions.length; i < other.dimensions.length; i++) {
			length *= other.dimensions[i];
		}
		
		matrix = other;
	}
	
	public void init(Value value) {
		throw new IllegalArgumentException();
		
//		for (int i = 0; i < length; i++) {
//			matrix.data[offset + i] = value.get();
//		}
	}

	protected int index(int ... dims) {
		if (dims.length == matrix.dimensions.length) {
			return matrix.index(dims);
		}

		int[] ds = new int[matrix.dimensions.length];
		System.arraycopy(fixeDimensions, 0, ds, 0, fixeDimensions.length);
		System.arraycopy(dims, 0, ds, fixeDimensions.length, dims.length);
		
		return matrix.index(ds);
	}

	public float get(int ... dims) {
		return matrix.data[index(dims)];
	}

	public void set(float value, int ... dims) {
		matrix.data[index(dims)] = value;
	}
	
	public void fill(final float value) {
		iterate(new Function() {
			@Override
			public void call(int[] pos) {
				matrix.set(value, pos);
			}
		});
	}
	
	public int[] max() {
		PossitionHolder function = new PossitionHolder() {
	     	
			int[] maxPos = new int[matrix.dimensions.length];
	    	float max = 0;
	    	
	    	public void call(int[] pos) {
	    		if (matrix.get(pos) > max) {
	    			System.arraycopy(pos, 0, maxPos, 0, pos.length);
	    			max = matrix.get(pos);
	    		}
	    	}

			@Override
			public int[] getPossition() {
				return maxPos;
			}
		};
    	
		iterate(function);
    	
    	return function.getPossition();
	}
	
	public MatrixProxy copy() {
		return new MatrixProxy(matrix.copy(), fixeDimensions);
	}

	public MatrixProxy sub(int ... dims) {
		throw new IllegalArgumentException();
	}
	
	private void iterate(Function function) {
    	int[] pos = new int[matrix.dimensions.length];
    	Arrays.fill(pos, 0);
    	System.arraycopy(fixeDimensions, 0, pos, 0, fixeDimensions.length);

    	function.call(pos);

		float value = 0;
    	for (int i = 1; i < length; i++) {
    		for (int dim = matrix.dimensions.length - 1; dim >= fixeDimensions.length; dim--) {
        		value = ++pos[dim];
        		
        		if (value >= matrix.dimensions[dim]) {
        			pos[dim] = 0;
        			continue;
        		}
        		break;
    		}

        	function.call(pos);
    	}
	}
	
	interface Function {
		public void call(int[] pos);
	}
	
	interface PossitionHolder extends Function {
		public int [] getPossition();
	}

}
