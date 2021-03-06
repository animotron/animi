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
package animi.matrix;

import java.util.Arrays;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class FloatsZeroAvg extends FloatsDelay {
	
	float[] avgs;
	int count = 0;
	
	public FloatsZeroAvg(Attenuation attenuation, int ... dims) {
		super(attenuation, dims);
		
		avgs = new float[data.length];
	}
	
	public FloatsZeroAvg(Attenuation attenuation, FloatsImpl source) {
		this(attenuation, source.dimensions.clone());
	}
	
	public FloatsZeroAvg(FloatsZeroAvg source) {
		super(source);
		
		avgs = new float[source.avgs.length];
		System.arraycopy(source.avgs, 0, avgs, 0, source.avgs.length);
	}

	public void init(Value value) {
		super.init(value);

		//zero steps
		for (int i = 0; i < avgs.length; i++) {
			avgs[i] = 0;
		}
	}

	public void set(final float value, final int ... dims) {
		final int index = index(dims);
		super.setByIndex(value, index);
		stepsFromLastSet[index] = 0;
	}
	
	public void setByIndex(final float value, final int index) {
		super.setByIndex(value, index);
		stepsFromLastSet[index] = 0;
	}

	public void fill(float value) {
		super.fill(value);
		Arrays.fill(stepsFromLastSet, 0);
		Arrays.fill(avgs, 0);
		isSet.clear(0, isSet.size() - 1);
	}
	
	@Override
	public float getByIndex(final int index) {
		return attenuation.next(stepsFromLastSet[index], data[index]) - avgs[index];
	}

	public float pureByIndex(final int index) {
		return attenuation.next(stepsFromLastSet[index], data[index]);
	}

	@Override
	public float get(final int ... dims) {
		return getByIndex(index(dims));
	}

	public int[] max() {
		return super.max();
	}
	
	public FloatsZeroAvg copy() {
		return new FloatsZeroAvg(this);
	}

	public FloatsProxy sub(int ... dims) {
		throw new IllegalArgumentException();
//		return new MatrixProxy<Float>(this, dims);
	}

//	public void debug(String comment) {
//		System.out.println(comment);
//		
//		debug(new Floats(data), false);
//	}
	
	public void step(FloatsImpl matrix) {
		float avg = 0f;
		
		for (int index = 0; index < length(); index++) {

			if (matrix.isSet(index) && matrix.getByIndex(index) != 0f) {//WORKAROUND: != 0f
				setByIndex(matrix.getByIndex(index), index);
			}
			//calculate average
			avg = avgs[index];
			avg = (avg * count + pureByIndex(index)) / (count + 1) ;
			avgs[index] = avg;

			stepsFromLastSet[index]++;
		}
		isSet.clear(0, isSet.size() - 1);
		
		count++;
	}

	@Override
	public void step() {
		throw new IllegalAccessError();
	}
}
