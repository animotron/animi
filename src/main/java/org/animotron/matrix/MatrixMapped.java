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

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MatrixMapped<T extends Number> implements Matrix<T> {
	
	Matrix<T> source;
	Matrix<Integer> mapper;
	
	public MatrixMapped(Matrix<T> source, Matrix<Integer> mapper) {
		this.source = source;
		this.mapper = mapper;
		
		//XXX: checks dimensions
	}

	@Override
	public void init(Value<T> value) {
		throw new IllegalArgumentException();
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
	public int dimension(int index) {
		throw new IllegalArgumentException();
	}

	@Override
	public T getByIndex(int index) {
		final int length = mapper.dimension(1);
		int[] dims = new int[length];
		for (int i = 0; i < length; i++) {
			dims[i] = mapper.get(new int[] {index, i});
		}
		return source.get(dims);
	}

	@Override
	public void setByIndex(T value, int index) {
		throw new IllegalArgumentException();
	}

	@Override
	public T get(int... dims) {
		throw new IllegalArgumentException();
	}

	@Override
	public void set(T value, int... dims) {
		throw new IllegalArgumentException();
	}

	@Override
	public void fill(T value) {
		throw new IllegalArgumentException();
	}

	@Override
	public int[] max() {
		throw new IllegalArgumentException();
	}

	@Override
	public MatrixMapped<T> copy() {
		throw new IllegalArgumentException();
	}

	@Override
	public MatrixProxy<T> sub(int... dims) {
		throw new IllegalArgumentException();
	}

	@Override
	public void debug(String comment) {
		throw new IllegalArgumentException();
	}
}
