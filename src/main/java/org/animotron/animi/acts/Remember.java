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
import org.animotron.animi.cortex.NeuronSimple;

/**
 * Запоминание  и переоценка параметров стабильности нейрона
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Remember implements Act<CortexZoneComplex> {

    @Override
    public void process(CortexZoneComplex layer, final int x, final int y) {
        int sumact = 0;

        for (int z = 0; z < layer.deep; z++) {
            final NeuronSimple sn = layer.s[x][y][z];
            //Вычисляем кол-во активных соседей
            sumact = 0;
            for (int i = x - 1; i <= x + 1; i++)
                for (int j = y - 1; j <= y + 1; j++)
                    sumact += layer.col[i][j].sum;
            if (sn.occupy) {
                //Нейрон занят. Изменяем информацию об активности.
                if (sn.active) {
                    //изменяем среднее кол-во активных соседей в состоянии активности
                    sn.p_on = (sn.p_on * sn.n_on + sumact) / (sn.n_on + 1);
                    sn.n_on++;
                } else {
                    if (sumact > sn.p_on) {
                        //изменяем среднее кол-во активных соседей в состоянии покоя в случаях, 
                        //когда их больше чем при собственной активности нейрона
                        sn.p_off_m = (sn.p_off_m * sn.n_off_m + sumact) / (sn.n_off_m + 1);
                        sn.n_off_m++;
                    }
                }
                sn.n_act++;
                //проверяем условие забывания и обнуляем нейрон если оно выполняется
                if (sn.n_act > layer.n_act_min && sn.n_off_m > sn.n_on * layer.k_non) {
                    sn.occupy = false;
                }
            } else {
                //Нейрон свободен. Проверяем основание для записи и записываем если выполняется.
                int sum = 0;
                for (int i = 0; i < layer.ns_links; i++) {
                    final Link2dZone link = sn.s_links[i];
                    if (link.zone != null)
                        if (link.zone.col[link.x][link.y].active)
                            sum++;
                }
                if (sum > layer.k_mem) {
                    //запоминаем состояние
                    sn.occupy = true;
                    sn.n_on = 1;
                    sn.n_act = 0;
                    sn.p_on = sumact;
                    sn.p_off_m = 0;
                    sn.n_off_m = 0;
                    for (int i = 0; i < layer.ns_links; i++) {
                        final Link2dZone link = sn.s_links[i];
                        link.cond = link.zone.col[link.x][link.y].active;
                    }
                }
            }
        }
    }
}
