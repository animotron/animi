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
package org.animotron.animi.acts;

import org.animotron.animi.cortex.*;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Restructorization implements Act<CortexZoneComplex> {
	
	public double ny = 0.1;

	public Restructorization() {}

    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
    	
		NeuronComplex cn = layer.col[x][y];
		
		double delta = 0;
		double sumDelta = 0;
		for (LinkQ link : cn.Qs.values()) {
			delta = cn.activity * link.synapse.activity * ny;
			
			sumDelta += delta;
			
			link.q += delta;
		}
		
		for (LinkQ link : cn.Qs.values()) {
			link.q = link.q * cn.sumQ / (cn.sumQ + sumDelta);
		}
    }
}