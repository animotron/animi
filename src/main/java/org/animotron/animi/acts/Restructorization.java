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

import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.*;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Restructorization implements Act<CortexZoneSimple> {
	
	@RuntimeParam(name = "count")
	public int count = 10000;

	@RuntimeParam(name = "ny")
	public double ny = 0.1 / 5;
	@RuntimeParam(name = "inhibitoryNy")
	public double inhibitoryNy = ny / 5;

	public Restructorization() {}

    @Override
    public void process(final CortexZoneSimple layer, final int x, final int y) {
    	
		final NeuronComplex cn = layer.col[x][y];
		
		if (cn.activity == 0)
			return;
		
//		double sumA = 0;
//		for (int dx = x - 1; dx <= x + 1; dx++) {
//			for (int dy = y - 1; dy <= y + 1; dy++) {
//				if (x == dx && y == dy) continue;
//				
//				sumA += layer.col[dx][dy].activity;
//			}
//		}
//		sumA /= 8;
		
		double factor = ny / Math.pow(2, layer.count / count);
		double sumQ2 = 0;
		for (LinkQ link : cn.Qs.values()) {
			
//			link.q += (sumA + cn.activity) * link.synapse.activity * ny;
			link.q += cn.activity * link.synapse.activity * factor;

			sumQ2 += link.q * link.q;
		}
		
		double norm = Math.sqrt(sumQ2);
		for (LinkQ link : cn.Qs.values()) {
			link.q = link.q / norm;
		}
		
		//inhibitory restructorization & normlization
		sumQ2 = 0;
		for (Link link : cn.s_inhibitoryLinks) {
//			link.w += cn.activity * (link.synapse.activity * inhibitoryNy - cn.activity * link.w);
			link.w += cn.activity * link.synapse.activity * inhibitoryNy;

			sumQ2 += link.w * link.w;
		}

//		sumQ2 *= k;
		norm = Math.sqrt(sumQ2);
		for (Link link : cn.s_inhibitoryLinks) {
			link.w = link.w / norm;
		}
    }
}