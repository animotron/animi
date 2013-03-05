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
package org.animotron.animi.cortex;

import java.util.Arrays;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MatrixDelay extends MatrixFloat {
	
	int delay;
	
	int[] delays;
	
	public MatrixDelay(int delay, int ... dims) {
		super(dims);
		
		this.delay = delay;

		delays = new int[data.length];
	}
	
	public MatrixDelay(MatrixDelay source) {
		super(source);
		
		delay = source.delay;
		
		delays = new int[source.delays.length];
		System.arraycopy(source.delays, 0, delays, 0, source.delays.length);
	}

	public void init(Value<Float> value) {
		for (int i = 0; i < data.length; i++) {
			data[i] = value.get();
		}

		//zero delays
		for (int i = 0; i < delays.length; i++) {
			delays[i] = 0;
		}
	}

	public void set(Float value, int ... dims) {
		final int index = index(dims);
		super.setByIndex(1f, index);
		if (value == 0) {
			delays[index] = 0;
		} else {
			delays[index] = delay;
		}
	}
	
	public void setByIndex(Float value, int index) {
		super.setByIndex(1f, index);
		if (value == 0f) {
			delays[index] = 0;
		} else {
			delays[index] = delay;
		}
	}

	public void fill(Float value) {
		super.fill(value);
		Arrays.fill(delays, 0);
	}
	
	public int[] max() {
		throw new IllegalArgumentException();
	}
	
	public MatrixDelay copy() {
		return new MatrixDelay(this);
	}

	public MatrixProxy<Float> sub(int ... dims) {
		return new MatrixProxy<Float>(this, dims);
	}

	public void debug(String comment) {
		throw new IllegalArgumentException();
	}
	
	public void step() {
		
//		super.debug("before step");
//		System.out.println("before delays");
//		debug(new Integers(delays), true);
		
		for (int i = 0; i < delays.length; i++) {
			if (delays[i] > 1) {
				delays[i]--;
			} else if (delays[i] == 1) {
				delays[i]--;
				super.setByIndex(0f, i);
			}
		}

//		super.debug("after step");
//		System.out.println("after delays");
//		debug(new Integers(delays), true);
	}
}
