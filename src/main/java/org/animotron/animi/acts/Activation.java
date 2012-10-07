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

import org.animotron.animi.cortex.CortexZoneComplex;
import org.animotron.animi.cortex.Link2dZone;
import org.animotron.animi.cortex.NeuronComplex;
import org.animotron.animi.cortex.NeuronSimple;

/**
 * Активация простых нейронов при узнавании запомненной картины
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Activation implements Act<CortexZoneComplex> {

    /** Matching percent for the active/passive elements required for recognition **/
    private double k_det1, k_det2;

    public Activation (double k_det1, double k_det2) {
        this.k_det1 = k_det1;
        this.k_det2 = k_det2;
    }

    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
        int sum = 0;
        int sum_on_on = 0, sum_on_off = 0, sum_off_on = 0, sum_off_off = 0;
        double k1 = 0, k2 = 0;

        final NeuronComplex cn = layer.col[x][y];
        for (int z = 0; z < layer.deep; z++) {
        	final NeuronSimple sn = layer.s[x][y][z];
            if (sn.occupy) {
                sum_on_on = 0; sum_on_off = 0; sum_off_on = 0; sum_off_off = 0;
                for (int i = 0; i < layer.ns_links; i++) {
                	final Link2dZone link = sn.s_links[i];
                    if (link.zone.col[link.x][link.y].active) {
                        if (link.cond)
                            sum_on_on++;
                        else
                            sum_on_off++;

                    } else {
                        if (link.cond)
                            sum_off_on++;
                        else
                            sum_off_off++;
                    }
                }
                k1 = 0;
                if (sum_on_on != 0)
                    k1 = (double) sum_on_on / (sum_on_on + sum_off_on);

                k2 = 0;
                if (sum_off_off != 0)
                    k2 = (double) sum_off_off / (sum_on_off + sum_off_off);

                sn.active = k1 > k_det1 && k2 > k_det2;
                
                if (sn.active) sum++;
            }
        }
        cn.sum = sum;
    }
}
