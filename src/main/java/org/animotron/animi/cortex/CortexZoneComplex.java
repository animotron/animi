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
package org.animotron.animi.cortex;

import org.animotron.animi.Utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Complex cortex zone
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CortexZoneComplex extends CortexZoneSimple {

	Mapping[] in_zones;
	int deep;
	
	/** Number of synaptic connections of the all simple neurons **/
	int ns_links;
	/** Number of axonal connections of the all simple neurons **/
	int nas_links;
	/** Number of synaptic connections of the complex neuron **/
	int nsc_links;
	/** Excitation threshold of cortical column **/
	double k_active;
	/** Min number of active synapses to remember **/
	int k_mem;
	/** Matching percent for the active/passive elements required for recognition **/
	double k_det1, k_det2;
	/** Number of cycles to turn on the possibility of forgetting **/
	int n_act_min;
	/** Ratio threshold of forgetting **/
	double k_non;
	/** Memory **/
	NeuronSimple[][][] s;

	CortexZoneComplex(String name, int width, int height, int deep,
			int nas_links, double k_active, double k_mem, double k_det1,
			double k_det2, int n_act_min, double k_non, Mapping[] in_zones) {

		super(name, width, height);
		this.deep = deep;
		this.s = new NeuronSimple[width][height][deep];
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

		// Инициализация синаптических связей простых нейронов
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < deep; z++) {

					NeuronSimple sn = s[x][y][z] = new NeuronSimple();
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

		// Создание синаптических связей симпл нейронов.
		// Связи распределяются случайным образом.
		// Плотность связей убывает экспоненциально с удалением от колонки.
		double X, S, Y, dX, dY;
		double x_in_nerv, y_in_nerv;
		int lx, ly;
		for (Mapping m : in_zones) {

			boolean[][] nerv_links = new boolean[m.zone.width][m.zone.height];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {

					// Определение координат текущего нейрона в масштабе
					// проецируемой зоны
					x_in_nerv = x * m.zone.width / (double) width;
					y_in_nerv = y * m.zone.height / (double) height;

					for (int z = 0; z < deep; z++) {
						// Обнуление массива занятости связей
						for (int n1 = 0; n1 < m.zone.width; n1++) {
							for (int n2 = 0; n2 < m.zone.height; n2++) {
								nerv_links[n1][n2] = false;
							}
						}

						// преобразование Бокса — Мюллера для получения
						// нормально распределенной величины
						// DispLink - дисперсия связей
						for (int i = 0; i < m.ns_links; i++) {
							do {
                                do {
                                    X = 2.0 * Math.random() - 1;
                                    Y = 2.0 * Math.random() - 1;
                                    S = X * X + Y * Y;
                                } while (!(S < 1 && S > 0));

                                S = Math.sqrt(-2 * Math.log(S) / S);
                                dX = X * S * m.zone.width * m.disp_links;
                                dY = Y * S * m.zone.height * m.disp_links;
                                lx = (int) Math.round(x_in_nerv + dX);
                                ly = (int) Math.round(y_in_nerv + dY);

                                // колонки по периметру не задействованы

                                if (lx < 1)
                                    lx = 1;

                                if (ly < 1)
                                    ly = 1;

                                if (lx > m.zone.width - 1)
                                    lx = m.zone.width - 2;

                                if (ly > m.zone.height - 1)
                                    ly = m.zone.height - 2;

                            // Проверка на повтор связи
							} while (nerv_links[lx][ly]);

							nerv_links[lx][ly] = true;

							// Создаем синаптическую связь
							NeuronSimple n = s[x][y][z];
							n.s_links[n.n1].x = lx;
							n.s_links[n.n1].y = ly;
							n.s_links[n.n1].zone = m.zone;
							n.n1++;
						}
					}
				}
			}
		}

		// Инициализация аксонных связей простых нейронов
		// и, соответственно, синаптических сложных нейронов.
		// В простейшем случае каждый простой нейрон сязан с девятью колонками,
		// образующими квадрат с центров в этом нейроне.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				NeuronComplex sn = col[x][y];

				sn.s_links = new Link3d[nsc_links];
				for (int i = 0; i < nsc_links; i++)
					sn.s_links[i] = new Link3d();

				sn.active = false;
			}
		}

		int n;
		// колонки по периметру не задействованы
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {

				n = 0;

				for (int i = x - 1; i <= x + 1; i++) {
					for (int j = y - 1; j <= y + 1; j++) {
						for (int k = 0; k < deep; k++) {

							NeuronComplex cn = col[x][y];

							cn.s_links[n].x = i;
							cn.s_links[n].y = j;
							cn.s_links[n].z = k;
							n++;

							NeuronSimple sn = s[i][j][k];
							sn.a_links[sn.n2].x = x;
							sn.a_links[sn.n2].y = y;
							sn.n2++;
						}
					}
				}
			}
		}
	}

	// Картинка активных нейронов по колонкам
	public BufferedImage[] getSImage() {
		BufferedImage[] a = new BufferedImage[deep];
		for (int z = 0; z < deep; z++) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int c = s[x][y][z].active ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
					image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
				}
			}
			a[z] = image;
		}
		return a;
	}

	
	int boxSize = 10;
	int maxX = width * boxSize;
	int maxY = height * boxSize;
	BufferedImage image = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);

	public BufferedImage getColumnRFimage() {
		Graphics g = image.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, maxX, maxY);

		int pX, pY = 0;

//		g.setColor(Color.YELLOW);

		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				
//				g.drawLine(x*boxSize, 0, x*boxSize, maxY);
//				g.drawLine(0, y*boxSize, maxX, y*boxSize);

				final NeuronComplex cn = col[x][y];

                for (int i = 0; i < nsc_links; i++) {
                	final Link3d cl = cn.s_links[i];
                    if (s[cl.x][cl.y][cl.z].occupy) {
                    	
                    	final NeuronSimple sn = s[cl.x][cl.y][cl.z];
                    	if (sn.occupy) {
                    		
                    		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
                            for (int j = 0; j < ns_links; j++) {
                                final Link2dZone sl = sn.s_links[j];
                                if (sl.cond) {
                                	minX = Math.min(minX, sl.x);
                                	minY = Math.min(minY, sl.y);
                                }
                            }
                            for (int j = 0; j < ns_links; j++) {
                                final Link2dZone sl = sn.s_links[j];
                                if (sl.cond) {
                                	if (sl.x - minX < boxSize && sl.y - minY < boxSize) {
										pX = x*boxSize + (sl.x - minX);
										pY = y*boxSize + (sl.y - minY);
				                    	
				                    	int c = Utils.calcGrey(image, pX, pY);
										c += 50;
										image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
                                	}
                                }
                            }
                    	}
                    }
                }
			}
		}
		return image;
	}

	// Картинка суммы занятых нейронов в колонке
	public BufferedImage[] getOccupyImage() {
		BufferedImage[] a = new BufferedImage[deep];
		for (int z = 0; z < deep; z++) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int c = s[x][y][z].occupy ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
					image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
				}
			}
			a[z] = image;
		}
		return a;
	}
    
    public void cycle (int x1, int y1, int x2, int y2, Action action) {
        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                action.process(x, y);
            }
        }
    }

    //Граничные нейроны не задействованы.
    public void cycle1() {
        cycle(1, 1, width - 1, height - 1, activateNeurone);
        cycle(1, 1, width - 1, height - 1, activateColumn);
    }

    //Граничные нейроны не задействованы.
    public void cycle2() {
        cycle(1, 1, width - 1, height - 1, remember);
    }

    private interface Action {
        public abstract void process(int x, int y);
    }
    
    private Action activateNeurone = new Action() {
        @Override
        //Активация простых нейронов при узнавании запомненной картины
        public void process (final int x, final int y) {
            int sum = 0;
            int sum_on_on = 0, sum_on_off = 0, sum_off_on = 0, sum_off_off = 0;
            double k1 = 0, k2 = 0;

            final NeuronComplex cn = col[x][y];
            for (int z = 0; z < deep; z++) {
            	final NeuronSimple sn = s[x][y][z];
                if (sn.occupy) {
                    sum_on_on = 0; sum_on_off = 0; sum_off_on = 0; sum_off_off = 0;
                    for (int i = 0; i < ns_links; i++) {
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
                    if (sn.active) {
                        sum++;
                    }
                }
            }
            cn.sum = sum;
        }
    };

    //Такт 1. Активация колонок (узнавание)
    private Action activateColumn = new Action()  {
        @Override
        //активация колонок если набралась критическая масса активности нейронов обвязки
        public void process(final int x, final int y) {
            int sum = 0;
            final NeuronComplex cn = col[x][y];
            for (int i = 0; i < nsc_links; i++) {
                final Link3d link = cn.s_links[i];
                if (s[link.x][link.y][link.z].active) {
                    sum++;
                }
            }
            cn.active = sum / (double)nsc_links > k_active;
        }
    };

    //Такт 2. Запоминание  и переоценка параметров стабильности нейрона
    private Action remember = new Action()  {
        @Override
        public void process(final int x, final int y) {
            int sumact = 0;

            for (int z = 0; z < deep; z++) {
                final NeuronSimple sn = s[x][y][z];
                //Вычисляем кол-во активных соседей
                sumact = 0;
                for (int i = x - 1; i <= x + 1; i++)
                    for (int j = y - 1; j <= y + 1; j++)
                        sumact += col[i][j].sum;
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
                    if (sn.n_act > n_act_min && sn.n_off_m > sn.n_on * k_non) {
                        sn.occupy = false;
                    }
                } else {
                    //Нейрон свободен. Проверяем основание для записи и записываем если выполняется.
                    int sum = 0;
                    for (int i = 0; i < ns_links; i++) {
                        final Link2dZone link = sn.s_links[i];
                        if (link.zone != null)
                            if (link.zone.col[link.x][link.y].active)
                                sum++;
                    }
                    if (sum > k_mem) {
                        //запоминаем состояние
                        sn.occupy = true;
                        sn.n_on = 1;
                        sn.n_act = 0;
                        sn.p_on = sumact;
                        sn.p_off_m = 0;
                        sn.n_off_m = 0;
                        for (int i = 0; i < ns_links; i++) {
                            final Link2dZone link = sn.s_links[i];
                            link.cond = link.zone.col[link.x][link.y].active;
                        }
                    }
                }
            }
        }
    };
}