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

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class IntegersProxy extends Proxy implements Integers {
	
	interface Function {
		public void call(int[] pos);
		public int[] getPossition();
		public int getValue();
	}
	
	Integers matrix;
	
	protected IntegersProxy(Integers other, int ... dims) {
		super(other, dims);
		
		matrix = other;
	}
	
	@Override
	public void init(Value value) {
		throw new IllegalArgumentException();
		
//		for (int i = 0; i < length; i++) {
//			matrix.data[offset + i] = value.get();
//		}
	}

	@Override
	protected int[] dims(final int ... dims) {
		if (dims.length == matrix.dimensions()) {
			return dims;
		}

		int[] ds = new int[matrix.dimensions()];
		System.arraycopy(fixeDimensions, 0, ds, 0, fixeDimensions.length);
		System.arraycopy(dims, 0, ds, fixeDimensions.length, dims.length);
		
		return ds;
	}

	@Override
	public int get(final int ... dims) {
		return matrix.get(dims(dims));
	}
	
	public boolean isSet(int ... dims) {
		throw new IllegalArgumentException();
	}
	
	public boolean isSet(int index) {
		throw new IllegalArgumentException();
	}


	@Override
	public void set(final int value, int ... dims) {
		matrix.set(value, dims(dims));
	}
	
	public void fill(final int value) {
		iterate(new Function() {
			@Override
			public void call(int[] pos) {
				matrix.set(value, pos);
			}

			@Override
			public int[] getPossition() {
				throw new IllegalAccessError();
			}

			@Override
			public int getValue() {
				throw new IllegalAccessError();
			}
		});
	}
	
	public int[] max() {
		Function function = new Function() {
	     	
			int[] maxPos = new int[matrix.dimensions()];
	    	Number max = 0f;
	    	
	    	public void call(int[] pos) {
	    		if (compare((Number)matrix.get(pos), max) == 1) {
	    			System.arraycopy(pos, 0, maxPos, 0, pos.length);
	    			max = (Number)matrix.get(pos);
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

			@Override
			public int getValue() {
				throw new IllegalAccessError();
			}
		};
    	
		iterate(function);
    	
    	return function.getPossition();
	}
	
	public int maximum() {
		Function function = new Function() {
	     	
			int max = 0;
	    	
	    	public void call(int[] pos) {
	    		if (matrix.get(pos) > max) {
	    			max = matrix.get(pos);
	    		}
	    	}
	    	
			@Override
			public int[] getPossition() {
				throw new IllegalAccessError();
			}
			
			@Override
			public int getValue() {
				return max;
			}
		};
    	
		iterate(function);
    	
    	return function.getValue();
	}

	public IntegersProxy copy() {
		return new IntegersProxy(matrix.copy(), fixeDimensions);
	}

	public IntegersProxy sub(int ... dims) {
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
	public int getByIndex(int index) {
		return matrix.get(pos(index));
	}

	@Override
	public void setByIndex(int value, int index) {
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
}