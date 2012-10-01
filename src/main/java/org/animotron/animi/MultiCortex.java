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

import java.awt.Color;
import java.awt.image.BufferedImage;

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
    
    // create a grayscale image the same size
    // Neuron link on the surfarce
    class Link2d {
        int x, y;
        boolean cond;
    }

    // Neuron link on the surfarce with a cortex reference
    class Link2dZone extends Link2d {
        SCortexZone zone;
    }

    // Neuron link in the cortex space
    class Link3d {
        int x, y, z;
    }

    // Simple neuron
    class SNeuron {
        boolean occupy, active;
        int n_on;               // Number of active cycles after activation
        int n_act;              // Number of cycles after activation
        double p_on;            // Average number of active neighbors at activation moment
        double p_off_m;         // Average number of active neighbors when calm and activity of neighbors more p_on
        int n_off_m;            // Number of passive cycles after activation when activity of neighbors more p_on
        Link2dZone[] s_links;   // Links of synapses connects cortex neurons with projecting nerve bundle
        Link2d[] a_links;       // Axonal connections with nearest cortical columns
        int n1;                 // Counter for links of synapses
        int n2;                 // Counter for axonal connections
    }

    // Complex neuron
    class CNeuron {
        boolean active;
        int sum;                // Number of active neurons
        Link3d[] s_links;       // Links of synapses connects cortex neurons with neurons of cortical columns
    }

    // Projection description of the one zone to another
    class Mapping {
        SCortexZone zone;       // Projecting zone
        int ns_links;           // Number of synaptic connections for the zone
        double disp_links;      // Grouping parameter. Describe a size of sensor field

        public Mapping(SCortexZone zone, int ns_links, double disp_links) {
            this.zone = zone;
            this.ns_links = ns_links;
            this.disp_links = disp_links;
        }

        public String toString() {
        	return "mapping "+zone.toString();
        }
    }

    // Simple cortex zone
    public class SCortexZone implements Layer {

        String name;
        CNeuron[][] col;        // State of complex neurons (outputs cortical columns)
        int width;              //
        int height;             //

        public SCortexZone(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.col = new CNeuron[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    CNeuron cn = new CNeuron();
                    cn.active = false;
                    this.col[x][y] = cn;
                }
            }
        }

        public BufferedImage getColImage() {
        	int c;
        	
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    c = col[x][y].active ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                    image.setRGB(x, y, ImgP.create_rgb(255, c, c, c));
                }
            }
            return image;
        }
        
        public String toString() {
        	return name;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

		@Override
		public void process() {
			// TODO Auto-generated method stub
		}

		@Override
		public void set(int x, int y, boolean b) {
			col[x][y].active = b;
		}
    }
    
    // Complex cortex zone
    public class CCortexZone extends SCortexZone {

        Mapping[] in_zones;
        int deep;
        int ns_links;           // Number of synaptic connections of the all simple neurons
        int nas_links;          // Number of axonal connections of the all simple neurons
        int nsc_links;          // Number of synaptic connections of the complex neuron
        double k_active;        // Excitation threshold of cortical column
        int k_mem;              // Min number of active synapses to remember
        double k_det1, k_det2;  // Matching percent for the active/passive elements required for recognition
        int n_act_min;          // Number of cycles to turn on the possibility of forgetting
        double k_non;           // Ratio threshold of forgetting
        SNeuron[][][] s;        // Memory

        CCortexZone(String name, int width, int height, int deep,
                   int nas_links,
                   double k_active, double k_mem, double k_det1, double k_det2,
                   int n_act_min, double k_non,
                   Mapping[] in_zones) {

            super(name, width, height);
            this.deep = deep;
            this.s = new SNeuron[width][height][deep];
            this.in_zones = in_zones;

            ns_links = 0;
            for (Mapping i : in_zones) {
                ns_links += i.ns_links;
            }

            this.nas_links = nas_links;
            this.nsc_links = nas_links * deep;
            this.k_active = k_active;
            this.k_mem = (int) Math.round(ns_links * k_mem);
            this.k_det1 = k_det1;
            this.k_det2 = k_det2;
            this.n_act_min = n_act_min;
            this.k_non = k_non;
            
            //Инициализация синаптических связей простых нейронов
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < deep; z++) {
                        
                    	SNeuron sn = s[x][y][z] = new SNeuron();
                        sn.s_links = new Link2dZone[ns_links];
                        for (int i = 0; i < ns_links; i++)
                        	sn.s_links[i] = new Link2dZone();
                        
                        sn.a_links = new Link2d[nas_links];
                        for (int i = 0; i < nas_links; i++)
                        	sn.a_links[i] = new Link2d();
                        
                        sn.occupy = sn.active = false;
                        sn.n1 = sn.n2 = 0;
                    }
                }
            }

            //Создание синаптических связей симпл нейронов.
            //Связи распределяются случайным образом.
            //Плотность связей убывает экспоненциально с удалением от колонки.
            double X, S, Y, dX, dY;
        	double x_in_nerv, y_in_nerv;
            int lx, ly;
            for (Mapping m : in_zones) {
//                if (!(m.zone instanceof CCortexZone))
//                	continue;

//            	System.out.println("!"+width+" "+m);
                boolean[][] nerv_links = new boolean[m.zone.width][m.zone.height];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {

                    	//Определение координат текущего нейрона в масштабе проецируемой зоны
                    	x_in_nerv = x * m.zone.width / (double)width;
                    	y_in_nerv = x * m.zone.height / (double)height;
                        
                    	for (int z = 0; z < deep; z++) {
                    		//Обнуление массива занятости связей
                            for (int n1 = 0; n1 < m.zone.width; n1++) {
                                for (int n2 = 0; n2 < m.zone.height; n2++) {
                                    nerv_links[n1][n2] = false;
                                }
                            }
//                            System.out.println("  "+x+" "+y+" "+z);
                            //преобразование Бокса — Мюллера для получения нормально распределенной величины
                            //DispLink - дисперсия связей
                            for (int i = 0; i < m.ns_links; i++) {
                                do {
                                    do {
                                        do {
                                            X = 2.0 * Math.random() - 1;
                                            Y = 2.0 * Math.random() - 1;
                                            S =  X * X + Y * Y;
                                        } while (!(S < 1 && S > 0));

                                        S = Math.sqrt(-2 * Math.log(S) / S);
                                        dX = X * S * m.zone.width * m.disp_links;
                                        dY = Y * S * m.zone.height * m.disp_links;
                                        lx = (int) Math.round(x_in_nerv + dX);
                                        ly = (int) Math.round(y_in_nerv + dY);
                                        
                                        if (lx <= 0)
                                        	lx = 1;
                                        
                                        if (ly <= 0)
                                        	ly = 1;

                                        if (lx >= m.zone.width)
                                        	lx = m.zone.width - 1;
                                        
                                        if (ly >= m.zone.height)
                                        	ly = m.zone.height - 1;
                                        
                                    //определяем, что не вышли за границы поля колонок
                                    //колонки по периметру не задействованы
                                    } while (!(lx >= 1 && ly >= 1 && lx < m.zone.width && ly < m.zone.height));
                                //Проверка на повтор связи
                                } while (nerv_links[lx][ly]);
                                
                                nerv_links[lx][ly] = true;
                                
                                //Создаем синаптическую связь
                                SNeuron n = s[x][y][z];
                                n.s_links[n.n1].x = lx;
                                n.s_links[n.n1].y = ly;
                                n.s_links[n.n1].zone = m.zone;
                                n.n1++;
                                //System.out.println(""+n.n1+" "+n.n2);
                            }
                        }
                    }
                }
            }
            
            //Инициализация аксонных связей простых нейронов
            //и, соответственно, синаптических сложных нейронов.
            //В простейшем случае каждый простой нейрон сязан с девятью колонками, образующими 
            //квадрат с центров в этом нейроне.
//            int simLinks = 3*3*deep;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    CNeuron sn = col[x][y];// = new CNeuron();
                    
                    sn.s_links = new Link3d[nsc_links];
                    for (int i = 0; i < nsc_links; i++)
                    	sn.s_links[i] = new Link3d();
                    
                    sn.active = false;
                }
            }
            
            int n;
            //колонки по периметру не задействованы
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    
                	n = 0;
                    
                	for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            for (int k = 0; k < deep ; k++) {

                                CNeuron cn = col[x][y];

                                cn.s_links[n].x = i;
                                cn.s_links[n].y = j;
                                cn.s_links[n].z = k;
                                n++;

                                SNeuron sn = s[i][j][k];
                                sn.a_links[sn.n2].x = x;
                                sn.a_links[sn.n2].y = y;
                                sn.n2++;
                            }
                        }
                    }
                }
            }
        }

        //Картинка активных нейронов по колонкам
        public BufferedImage [] getSImage() {
            BufferedImage [] a = new BufferedImage[deep];
            for (int z = 0; z < deep; z++) {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int c = s[x][y][z].active ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                        image.setRGB(x, y, ImgP.create_rgb(255, c, c, c));
                    }
                }
                a[z] = image;
            }
            return a;
        }

        //Картинка суммы занятых нейронов в колонке
        public BufferedImage [] getOccupyImage() {
            BufferedImage [] a = new BufferedImage[deep];
            for (int z = 0; z < deep; z++) {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int c = s[x][y][z].occupy ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                        image.setRGB(x, y, ImgP.create_rgb(255, c, c, c));
                    }
                }
                a[z] = image;
            }
            return a;
        }
    }

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
                        SNeuron sn = zone.s[x][y][z];
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
                    CNeuron cn = zone.col[x][y];
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
    	SNeuron s = null;
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
