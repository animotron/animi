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

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class FloatsMapped implements Floats {
	
	final Floats source;
	final Integers mapper;
	
	public FloatsMapped(final Floats source, final Integers mapper) {
		this.source = source;
		this.mapper = mapper;
		
		//XXX: checks dimensions
	}

	@Override
	public void init(final Value value) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void step() {
		throw new IllegalAccessError();
	}

	@Override
	public int length() {
		return mapper.dimension(0);
	}

	@Override
	public int dimensions() {
		throw new IllegalArgumentException();
	}

	@Override
	public int dimension(final int index) {
		throw new IllegalArgumentException();
	}

	@Override
	public float getByIndex(final int index) {
		final int length = mapper.dimension(1);
		int[] dims = new int[length];
		for (int i = 0; i < length; i++) {
			dims[i] = mapper.get(new int[] {index, i}); //XXX: try to remove 'new int[]'
		}
		return source.get(dims);
	}

	@Override
	public void setByIndex(final float value, final int index) {
		throw new IllegalArgumentException();
	}

	@Override
	public float get(final int... dims) {
		throw new IllegalArgumentException();
	}

	@Override
	public void set(final float value, final int... dims) {
		throw new IllegalArgumentException();
	}


	@Override
	public boolean isSet(int... dims) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isSet(int index) {
		throw new IllegalArgumentException();
	}

	@Override
	public void fill(final float value) {
		throw new IllegalArgumentException();
	}

	@Override
	public int[] max() {
		throw new IllegalArgumentException();
	}

	@Override
	public float maximum() {
		throw new IllegalArgumentException();
	}

	@Override
	public FloatsMapped copy() {
		throw new IllegalArgumentException();
	}

	@Override
	public FloatsProxy sub(final int... dims) {
		throw new IllegalArgumentException();
	}

	@Override
	public void debug(final String comment) {
		throw new IllegalArgumentException();
	}
}
