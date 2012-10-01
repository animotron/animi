/*
 *  Copyright (C) 2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
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
package org.animotron.animi;


/**
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MultiCortex {

	//Сетчатка
    public static final int RETINA_WIDTH = 640;
	public static final int RETINA_HEIGHT = 480;

	public static final int VISUAL_FIELD_WIDTH = 96 * 2;
	public static final int VISUAL_FIELD_HEIGHT = 72 * 2;

    public boolean active = false;
    
    public Retina retina;

    public SCortexZone z_video, z_viscor, z_asscor, z_good, z_bad;

    public SCortexZone [] zones;

    public MultiCortex() {

        System.out.println("z_video");
        z_video = new SCortexZone("Input visual layer", VISUAL_FIELD_WIDTH, VISUAL_FIELD_HEIGHT);

        System.out.println("z_viscor");
        z_viscor = new CCortexZone("Prime visual cortex", VISUAL_FIELD_WIDTH, VISUAL_FIELD_HEIGHT, 2,
                9, 0, 0.3, 0.6, 0.6, 10, 2,
                new Mapping[]{
                        new Mapping(z_video, 15, 0.02)
                }
        );

//        System.out.println("z_good");
//        z_good = new SCortexZone("Zone good", 20, 20);
//        System.out.println("z_bad");
//        z_bad = new SCortexZone("Zone bad", 20, 20);
//
//        System.out.println("z_asscor");
//        z_asscor = new CCortexZone("Associative cortex", 48, 48, 10,
//                9, 0, 0.3, 0.6, 0.6, 10, 2,
//                new Mapping[]{
//                        new Mapping(z_viscor, 20, 0.1),
//                        new Mapping(z_good, 10, 0.01),
//                        new Mapping(z_bad, 10, 0.01)
//                }
//        );
//
//        zones = new SCortexZone[]{z_video, z_viscor, z_asscor, z_good, z_bad};
        zones = new SCortexZone[]{z_video, z_viscor};
        
        retina = new Retina(RETINA_WIDTH, RETINA_HEIGHT);
        retina.setNextLayer(z_video);

        System.out.println("done.");

    }

    //Такт 1. Активация колонок (узнавание)
    public void cycle1() {
        int sum_on_on, sum_on_off, sum_off_on, sum_off_off;
        double k1, k2 = 0;

        //Последовательность активации зон коры определяется их номером
        for (SCortexZone cortex : zones) {
            if (!(cortex instanceof CCortexZone))
            	continue;
            CCortexZone zone = (CCortexZone) cortex;
            
            //Активация простых нейронов при узнавании запомненной картины
            //Граничные нейроны не задействованы.
            
            for (int x = 1; x < zone.width - 1; x++) {
                for (int y = 1; y < zone.height - 1; y++) {
                    for (int z = 0; z < zone.deep; z++) {
                        NeuronSimple sn = zone.s[x][y][z];
                        if (sn.occupy) {
                            sum_on_on = sum_on_off = sum_off_on = sum_off_off = 0;
                            for (int i = 0; i < zone.ns_links; i++) {
                                Link2dZone link = sn.s_links[i];
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
                            	k1 = sum_on_on / (double)(sum_on_on + sum_off_on);
                            
                            k2 = 0;
                            if (sum_off_off != 0)
                            	k2 = sum_off_off / (double)(sum_on_off + sum_off_off);
                            
                            sn.active = k1 > zone.k_det1 && k2 > zone.k_det2;
                        }
                    }
                }
            }
            int sum = 0;

            //активация колонок если набралась критическая масса активности нейронов обвязки
            for (int x = 1; x < zone.width - 1; x++) {
                for (int y = 1; y < zone.height - 1; y++) {
                    sum = 0;
                    for (int z = 0; z < zone.deep; z++) {
                        if (zone.s[x][y][z].active) {
                            sum++;
                        }
                    }
                    NeuronComplex cn = zone.col[x][y];
                    cn.sum = sum;
                    
                    sum = 0;
                    for (int i = 0; i < zone.nsc_links; i++) {
                        Link3d link = cn.s_links[i];
                        if (zone.s[link.x][link.y][link.z].active) {
                            sum++;
                        }
                    }
                    cn.active = sum / (double)zone.nsc_links > zone.k_active;
                }
            }
        }
    }

    //Такт 2. Запоминание  и переоценка параметров стабильности нейрона
    public void cycle2() {
    	NeuronSimple s = null;
        int sumact = 0;
        int sum = 0;
    	
        for (SCortexZone cortex : zones) {
            if (!(cortex instanceof CCortexZone))
            	continue;
            CCortexZone zone = (CCortexZone) cortex;
            
            //Граничные нейроны не задействованы. 
            //Это дает возможность всем используемым нейронам иметь восемь соседних колонок.
            
            for (int x = 1; x < zone.width - 1; x++) {
                for (int y = 1; y < zone.height - 1; y++) {
                    for (int z = 0; z < zone.deep; z++) {
                        
                    	s = zone.s[x][y][z];
                        
                        //Вычисляем кол-во активных соседей
                        sumact = 0;
                        for (int i = x - 1; i <= x + 1; i++)
                            for (int j = y - 1; j <= y + 1; j++)
                                sumact += zone.col[i][j].sum;

                        if (s.occupy) {
                        	//Нейрон занят. Изменяем информацию об активности.
                            if (s.active) {
                            	//изменяем среднее кол-во активных соседей в состоянии активности
                                s.p_on = (s.p_on * s.n_on + sumact) / (s.n_on + 1);
                                s.n_on++;
                            } else {
                                if (sumact > s.p_on) {
                                	//изменяем среднее кол-во активных соседей в состоянии покоя в случаях, 
                                	//когда их больше чем при собственной активности нейрона
                                    s.p_off_m = (s.p_off_m * s.n_off_m + sumact) / (s.n_off_m + 1);
                                    s.n_off_m++;
                                }
                            }
                            s.n_act++;
                            //проверяем условие забывания и обнуляем нейрон если оно выполняется
                            if (s.n_act > zone.n_act_min && s.n_off_m > s.n_on * zone.k_non) {
                                s.occupy = false;
                            }
                        } else {
                        	//Нейрон свободен. Проверяем основание для записи и записываем если выполняется.
                            sum = 0;
                            for (int i = 0; i < zone.ns_links; i++) {
                                Link2dZone link = s.s_links[i];
                                if (link.zone != null)
                                	if (link.zone.col[link.x][link.y].active)
                                		sum++;
                            }
                            if (sum > zone.k_mem) {
                            	//запоминаем состояние
                                s.occupy = true;
                                s.n_on = 1;
                                s.n_act = 0;
                                s.p_on = sumact;
                                s.p_off_m = 0;
                                s.n_off_m = 0;
                                for (int i = 0; i < zone.ns_links; i++) {
                                    Link2dZone link = s.s_links[i];
                                    link.cond = link.zone.col[link.x][link.y].active;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
