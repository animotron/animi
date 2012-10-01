package org.animotron.animi;

import java.awt.Color;
import java.awt.image.BufferedImage;

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