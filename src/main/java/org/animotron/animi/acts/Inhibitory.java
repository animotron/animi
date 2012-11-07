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
public class Inhibitory implements Act<CortexZoneComplex> {

	public Inhibitory() {}

    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
    	NeuronComplex cn = layer.col[x][y];
    	
    	double delta = 0;
    	for (Link link : cn.s_inhibitoryLinks) {
    		delta += link.w * link.synapse.activity;
    	}
    	
    	cn.activity = cn.activity - delta;
    	if (cn.activity < 0 || Double.isNaN(cn.activity)) 
    		cn.activity = 0;
    }
}
