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

import static org.jocl.CL.*;

import java.util.Arrays;

import org.animotron.animi.InitParam;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

/**
 * Projection description of the one zone to another
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Mapping {
	public CortexZoneSimple frZone;
	public CortexZoneSimple toZone;
	
	@InitParam(name="ns_links")
    public int ns_links;           // Number of synaptic connections for the zone
    
	/** дисперсия связей **/
	@InitParam(name="disp")
	public double disp;      // Describe a size of sensor field

	@InitParam(name="soft")
	public boolean soft = true;
	
	public double fX = 1;
	public double fY = 1;

	public float linksWeight[];
	public int linksWeightRecordSize;
	
	public int linksSenapse[];
	public int linksSenapseRecordSize;
	
    /**
     * The OpenCL memory object which store the neuron links for each zone.
     */
    public cl_mem cl_links;
    public Pointer pnt_links;
    public cl_mem cl_senapseOfLinks;

	Mapping () {}
	
    public Mapping(CortexZoneSimple zone, int ns_links, double disp, boolean soft) {
        this.frZone = zone;
        this.disp = disp;
        this.ns_links = ns_links;
        this.soft = soft;
    }

    public String toString() {
    	return "mapping "+frZone.toString();
    }

	// Связи распределяются случайным образом.
	// Плотность связей убывает экспоненциально с удалением от колонки.
	public void map(CortexZoneComplex zone) {
		toZone = zone;
	    
		System.out.println(toZone);

	    linksWeight = new float[frZone.width() * frZone.height() * ns_links];
		Arrays.fill(linksWeight, 0);
		
		linksWeightRecordSize = ns_links;

		linksSenapse = new int[frZone.width() * frZone.height() * ns_links * 2];
		Arrays.fill(linksSenapse, 0);
		
		linksSenapseRecordSize = ns_links*2;

//        for (int x = 0; x < zone.width(); x++) {
//			for (int y = 0; y < zone.height(); y++) {
//				zone.col[x][y].a_links.clear();
//				zone.col[x][y].a_Qs.clear();
//			}
//        }

		fX = zone.width() / (double) toZone.width();
		fY = zone.height() / (double) toZone.height();

        double X, Y, S;
		double x_in_nerv, y_in_nerv;
        double _sigma, sigma;

        boolean[][] nerv_links = new boolean[frZone.width()][frZone.height()];
        
		float sumQ2 = (1 / (float)ns_links * 1 / (float)ns_links) * ns_links;
		float norm = (float) Math.sqrt(sumQ2);
		float w = (1 / (float)ns_links) / norm;

        for (int x = 0; x < toZone.width(); x++) {
			for (int y = 0; y < toZone.height(); y++) {
//				System.out.println("x = "+x+" y = "+y);

				// Определение координат текущего нейрона в масштабе
				// проецируемой зоны
				x_in_nerv = x * frZone.width() / (double) toZone.width();
				y_in_nerv = y * frZone.height() / (double) toZone.height();
//				System.out.println("x_in_nerv = "+x_in_nerv+" y_in_nerv = "+y_in_nerv);

                _sigma = disp;// * ((m.zone.width() + m.zone.height()) / 2);
                sigma = _sigma;

				// Обнуление массива занятости связей
				for (int n1 = 0; n1 < frZone.width(); n1++) {
					for (int n2 = 0; n2 < frZone.height(); n2++) {
						nerv_links[n1][n2] = false;
					}
				}

				// преобразование Бокса — Мюллера для получения
				// нормально распределенной величины
				// DispLink - дисперсия связей
				int count = 0;
				for (int i = 0; i < ns_links; i++) {
                    int lx, ly;
                    do {
//                        do {
                            if (count > ns_links * 3) {
                            	if (Double.isInfinite(sigma)) {
                            		System.out.println("initialization failed @ x = "+x+" y = "+y);
                            		System.exit(1);
                            	}
                            	sigma *= 1.05;//_sigma * 0.1;
//    							System.out.println("\n"+i+" of "+ns_links+" ("+sigma+")");
                            	count = 0;
                            }
                            count++;
                            	
                            do {
                                X = 2.0 * Math.random() - 1;
                                Y = 2.0 * Math.random() - 1;
                                S = X * X + Y * Y;
                            } while (S > 1 || S == 0);
                            S = Math.sqrt(-2 * Math.log(S) / S);
                            double dX = X * S * sigma;
                            double dY = Y * S * sigma;
                            lx = (int) Math.round(x_in_nerv + dX);
                            ly = (int) Math.round(y_in_nerv + dY);

                            //определяем, что не вышли за границы поля колонок
                            //колонки по периметру не задействованы
//                        } while (!(soft || (lx >= 1 && ly >= 1 && lx < zone.width() - 1 && ly < zone.height() - 1)));

                    // Проверка на повтор связи
					} while ( lx < 1 || ly < 1 || lx > frZone.width() - 1 || ly > frZone.height() - 1 || nerv_links[lx][ly] );

                    if (lx >= 1 && ly >= 1 && lx < frZone.width() - 1 && ly < frZone.height() - 1) {
                        System.out.print(".");

                        nerv_links[lx][ly] = true;
	
						// Создаем синаптическую связь
                        linksWeight [(y*toZone.width*ns_links  )+ (ns_links  *x)+ i     ] = w;
                        linksSenapse[(y*toZone.width*ns_links*2)+ (ns_links*2*x)+ i*2   ] = lx;
                        linksSenapse[(y*toZone.width*ns_links*2)+ (ns_links*2*x)+ i*2 +1] = ly;
//						new LinkQ(zone.getCol(lx, ly), toZone.col[x][y], (1 / (double)ns_links) / norm, fX, fY, toZone.speed);
                    } else {
                    	System.out.print("!");
                    }
				}
				System.out.println();
			}
		}
        
		cl_links = 
			clCreateBuffer(
				frZone.mc.context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR,
				linksWeight.length * Sizeof.cl_float, Pointer.to(linksWeight), null
			);
		
		cl_senapseOfLinks = 
			clCreateBuffer(
				frZone.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR,
				linksSenapse.length * Sizeof.cl_int, Pointer.to(linksSenapse), null);
	}
}