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
package animi.matrix;

import java.util.Arrays;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class Proxy implements Matrix {
	
	interface Function {
		public void call(int[] pos);
	}
	
	interface ObjectHolderFunction extends Function {
		public Object getPossition();
	}

	Matrix matrix;
	
	int[] fixeDimensions;
	
	int length;
	
	protected Proxy(Matrix other, int ... dims) {
		
		if (other instanceof Proxy) {
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
	
	protected int[] dims(final int ... dims) {
		if (dims.length == matrix.dimensions()) {
			return dims;
		}

		int[] ds = new int[matrix.dimensions()];
		System.arraycopy(fixeDimensions, 0, ds, 0, fixeDimensions.length);
		System.arraycopy(dims, 0, ds, fixeDimensions.length, dims.length);
		
		return ds;
	}

	protected void iterate(Function function) {
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
	public void step() {
		throw new IllegalAccessError();
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

	protected int[] pos(final int index) {
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