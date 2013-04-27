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

import org.animotron.animi.Imageable;
import org.animotron.matrix.Matrix;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public interface Mapping {

	public LayerSimple frZone();
	
	public LayerWLearning toZone();

	public Matrix<Integer[]> senapses();

	public Matrix<Integer> _senapses();

	public Matrix<Float> senapseWeight();

	public Matrix<Float> senapsesCode();

//	public Matrix<Float> lateralWeight();

	public Matrix<Float> lateralWeight();
	
	public Matrix<Integer[]> lateralSenapse();

	public boolean haveInhibitoryWeight();
	
	public Matrix<Float> inhibitoryWeight();

	public boolean isDirectLearning();

	public void map(LayerWLearning cortexZoneComplex);

	public int toZoneCenterX();

	public int toZoneCenterY();

	public double fX();

	public double fY();

	public int ns_links();

	public double disp();

	public Imageable getImageable();
}