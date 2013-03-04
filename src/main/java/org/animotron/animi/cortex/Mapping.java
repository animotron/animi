/*
 *  Copyright (C) 2012-2013 The Animo Project
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

import java.util.Random;

import org.animotron.animi.InitParam;

/**
 * Projection description of the one zone to another
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Mapping {
	
	private static final boolean debug = false;
	
	private static final Random rnd = new Random();
	
	public CortexZoneSimple frZone;
	public CortexZoneComplex toZone;
	
	@InitParam(name="ns_links")
    public int ns_links;           // Number of synaptic connections for the zone
    
	/** дисперсия связей **/
	@InitParam(name="disp")
	public double disp;      // Describe a size of sensor field

	@InitParam(name="soft")
	public boolean soft = true;
	
	public double fX = 1;
	public double fY = 1;

	public Matrix<Float> linksWeight;
	
	public Matrix<Float> inhibitoryWeight;
	
	public Matrix<Integer> linksSenapse;
	
	private void linksSenapse(int Sx, int Sy, int x, int y, int l) {
		if (toZone.singleReceptionField) {
			for (int xi = 0; xi < toZone.width(); xi++) {
				for (int yi = 0; yi < toZone.height(); yi++) {
					linksSenapse.set(Sx, xi, yi, l, 0);
					linksSenapse.set(Sy, xi, yi, l, 1);
				}
			}
		} else {
			linksSenapse.set(Sx, x, y, l, 0);
			linksSenapse.set(Sy, x, y, l, 1);
		}
	}

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
		
//		float norm = (float) Math.sqrt(sumQ2);
		final float w = (1 / (float)ns_links);// / norm;

	    linksWeight = new MatrixFloat(toZone.width(), toZone.height(), toZone.package_size, ns_links);
	    linksWeight.init(new Matrix.Value<Float>() {
			@Override
			public Float get() {
				return w * rnd.nextFloat();
			}
		});
	    
	    inhibitoryWeight = new MatrixFloat(toZone.width(), toZone.height(), toZone.package_size, ns_links);
	    inhibitoryWeight.init(new Matrix.Value<Float>() {
			@Override
			public Float get() {
				return w * rnd.nextFloat();
			}
		});

		linksSenapse = new MatrixInteger(toZone.width(), toZone.height(), ns_links, 2);
		linksSenapse.fill(0);
		
//        for (int x = 0; x < zone.width(); x++) {
//			for (int y = 0; y < zone.height(); y++) {
//				zone.col[x][y].a_links.clear();
//				zone.col[x][y].a_Qs.clear();
//			}
//        }

		fX = frZone.width() / (double) toZone.width();
		fY = frZone.height() / (double) toZone.height();

		final boolean[][] nerv_links = new boolean[frZone.width()][frZone.height()];
        
		if (toZone.singleReceptionField) {
//			fX = 0;
//			fY = 0;

			initReceptionFields(
				(int)(toZone.width() / 2.0), 
				(int)(toZone.height() / 2.0), 
				nerv_links);
			
		} else {
//			float sumQ2 = (1 / (float)ns_links * 1 / (float)ns_links) * ns_links;
	        for (int x = 0; x < toZone.width(); x++) {
				for (int y = 0; y < toZone.height(); y++) {
//					System.out.println("x = "+x+" y = "+y);
	
					initReceptionFields(x, y, nerv_links);
				}
			}
		}
	}
	
    private void initReceptionFields(final int x, final int y, final boolean[][] nerv_links) {
        double X, Y, S;
		double x_in_nerv, y_in_nerv;
        double _sigma, sigma;

		// Определение координат текущего нейрона в масштабе
		// проецируемой зоны
		x_in_nerv = x * frZone.width() / (double) toZone.width();
		y_in_nerv = y * frZone.height() / (double) toZone.height();
//		System.out.println("x_in_nerv = "+x_in_nerv+" y_in_nerv = "+y_in_nerv);

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
//                do {
                    if (count > ns_links * 3) {
                    	if (Double.isInfinite(sigma)) {
                    		System.out.println("initialization failed @ x = "+x+" y = "+y);
                    		System.exit(1);
                    	}
                    	sigma *= 1.05;//_sigma * 0.1;
//						System.out.println("\n"+i+" of "+ns_links+" ("+sigma+")");
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
//                } while (!(soft || (lx >= 1 && ly >= 1 && lx < zone.width() - 1 && ly < zone.height() - 1)));

            // Проверка на повтор связи
			} while ( lx < 1 || ly < 1 || lx > frZone.width() - 1 || ly > frZone.height() - 1 || nerv_links[lx][ly] );

            if (lx >= 1 && ly >= 1 && lx < frZone.width() - 1 && ly < frZone.height() - 1) {
                if (debug) System.out.print(".");

                nerv_links[lx][ly] = true;

				// Создаем синаптическую связь
                linksSenapse(lx, ly, x, y, i);
            } else {
            	if (debug) System.out.print("!");
            }
		}
		if (debug) System.out.println();
    }
    
    public int toZoneCenterX() {
    	return (int)(toZone.width()  / 2.0);
	}

    public int toZoneCenterY() {
    	return (int)(toZone.height() / 2.0);
	}
}