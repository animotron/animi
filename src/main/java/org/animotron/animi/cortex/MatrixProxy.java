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
	
	int offset;
	int length;
	
	protected MatrixProxy(Matrix other, int ... dims) {
		
		if (other instanceof MatrixProxy) {
			throw new IllegalArgumentException();
		}
		
		length = 1;
		
		offset = 0;
		for (int i = dims.length - 1; i >= 0; i--) {
			offset = (offset + dims[i]) * other.dimensions[i];
		}
		
		dimensions = new int[other.dimensions.length - dims.length];
		for (int i = dims.length; i < other.dimensions.length; i++) {
			length *= other.dimensions[i];

			dimensions[dims.length - i] = other.dimensions[i];
		}

		matrix = other;
	}
	
	public void init(Value value) {
		for (int i = 0; i < length; i++) {
			matrix.data[offset + i] = value.get();
		}
	}

	protected int index(int ... dims) {
		return offset + super.index(dims);
	}

	public float get(int ... dims) {
		return matrix.data[index(dims)];
	}

	public void set(float value, int ... dims) {
		matrix.data[index(dims)] = value;
	}
	
	public void fill(float value) {
		for (int i = 0; i < length; i++) {
			matrix.data[offset + i] = value;
		}
	}
	
	public int[] max() {
    	int maxPos = -1;
    	float max = 0;
    	for (int pos = 0; pos < length; pos++) {
    		if (matrix.data[offset + pos] > max) {
    			maxPos = pos;
    			max = data[offset + pos];
    		}
    	}
    	
    	if (maxPos == -1) {
    		return null;
//    		throw new IllegalArgumentException("Maximum value can't be found.");
    	}
    	
    	int[] dims = new int[dimensions.length];
    	
		System.out.print("Max "+maxPos+" ");

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
    	
    	System.out.println(Arrays.toString(dims));
    	return dims;
	}
	
	public void copy(Matrix source) {
		if (source.dimensions.length != dimensions.length) {
			throw new IndexOutOfBoundsException("Matrix have "+dimensions.length+" dimensions, but get "+source.dimensions.length+".");
		}
		
		for (int i = 0; i < dimensions.length; i++) {
			if (source.dimensions[i] != dimensions[i]) {
				throw new IndexOutOfBoundsException("Matrix's "+i+" dimension have "+dimensions[i]+" elements, but source dimension have "+source.dimensions[i]+" element.");
			}
		}

		System.arraycopy(source.data, 0, matrix.data, offset, length);
	}

	public void copy(Matrix source, int ... dims) {
		//XXX: checks?
		
		int _offset = 0;
		for (int i = dims.length - 1; i >= 0; i--) {
			_offset = (_offset + dims[i]) * dimensions[i];
		}
		
		int length = 1;
		for (int i = dims.length; i < dimensions.length; i++) {
			length *= dimensions[i];
		}

		System.arraycopy(source.data, 0, data, offset + _offset, length);
	}

	public MatrixProxy sub(int ... dims) {
		return new MatrixProxy(this, dims);
	}
}
