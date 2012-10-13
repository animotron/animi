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

import org.animotron.animi.InitParam;
import org.animotron.animi.cortex.*;

/**
 * Активация колонок (узнавание)
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Recognition implements Act<CortexZoneComplex> {

    /** Excitation threshold of cortical column **/
	@InitParam(name="excitation threshold")
	public double k_active;

    public Recognition () {}

	public Recognition (double k_active) {
        this.k_active = k_active;
    }

    @Override
    public void process(final CortexZoneComplex layer, final int x, final int y) {
        double k_active = this.k_active * layer.nsc_links;
        int sum = 0;
        final NeuronComplex cn = layer.col[x][y];
        for (int i = 0; i < layer.nsc_links; i++) {
            final Link3d link = cn.s_links[i];
            if (layer.s[link.x][link.y][link.z].active) {
                sum++;
            }
        }
        cn.active = sum > k_active;
    }

}
