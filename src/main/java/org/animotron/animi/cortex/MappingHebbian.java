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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.animotron.animi.Imageable;
import org.animotron.animi.InitParam;
import org.animotron.animi.Utils;
import org.animotron.matrix.Matrix;
import org.animotron.matrix.MatrixFloat;
import org.animotron.matrix.MatrixInteger;

/**
 * Projection description of the one zone to another
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class MappingHebbian implements Mapping {
	
	private static final boolean debug = false;
	
	private static final Random rnd = new Random();
	
	private LayerSimple frZone;
	private LayerWLearning toZone;
	
	@InitParam(name="ns_links")
    public int ns_links;           // Number of synaptic connections for the zone
    
	/** дисперсия связей **/
	@InitParam(name="disp")
	public double disp;      // Describe a size of sensor field

	@InitParam(name="soft")
	public boolean soft = true;
	
	public double fX = 1;
	public double fY = 1;

	public float w;
	
	private Matrix<Integer> senapse;
	
	private Matrix<Float> senapseWeight;
	private Matrix<Float> inhibitoryWeight;
	
	private void linksSenapse(final int Sx, final int Sy, final int Sz, final int x, final int y, final int z, final int l) {
		if (toZone.singleReceptionField) {
			for (int xi = 0; xi < toZone.width(); xi++) {
				for (int yi = 0; yi < toZone.height(); yi++) {
					for (int zi = 0; zi < toZone.depth; zi++) {
						senapse.set(Sx, xi, yi, zi, l, 0);
						senapse.set(Sy, xi, yi, zi, l, 1);
						senapse.set(Sz, xi, yi, zi, l, 2);
					}
				}
			}
		} else {
			senapse.set(Sx, x, y, z, l, 0);
			senapse.set(Sy, x, y, z, l, 1);
			senapse.set(Sz, x, y, z, l, 3);
		}
	}

	MappingHebbian () {}
	
    public MappingHebbian(LayerSimple zone, int ns_links, double disp, boolean soft) {
        frZone = zone;
        this.disp = disp;
        this.ns_links = ns_links;
        this.soft = soft;
    }

    public String toString() {
    	return "mapping "+frZone.toString();
    }

	// Связи распределяются случайным образом.
	// Плотность связей убывает экспоненциально с удалением от колонки.
	public void map(LayerWLearning zone) {
		toZone = zone;
	    
		System.out.println(toZone);
		
//		float norm = (float) Math.sqrt(sumQ2);
		w = (1 / (float)ns_links);// / norm;

	    senapseWeight = new MatrixFloat(toZone.width(), toZone.height(), toZone.depth, ns_links);
	    senapseWeight.init(new Matrix.Value<Float>() {
			@Override
			public Float get(int... dims) {
				return getInitWeight();
			}
		});
	    
	    inhibitoryWeight = new MatrixFloat(toZone.width(), toZone.height(), toZone.depth, ns_links);
	    inhibitoryWeight.init(new Matrix.Value<Float>() {
			@Override
			public Float get(int... dims) {
				return getInitWeight();
			}
		});

		senapse = new MatrixInteger(toZone.width(), toZone.height(), toZone.depth, ns_links, 3);
		senapse.fill(0);
		
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
			initReceptionFields(
				(int)(toZone.width() / 2.0), 
				(int)(toZone.height() / 2.0), 
				nerv_links);
			
		} else {
	        for (int x = 0; x < toZone.width(); x++) {
				for (int y = 0; y < toZone.height(); y++) {
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
                if (count > ns_links * 3) {
                	if (Double.isInfinite(sigma)) {
                		System.out.println("initialization failed @ x = "+x+" y = "+y);
                		System.exit(1);
                	}
                	sigma *= 1.05;//_sigma * 0.1;
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
            // Проверка на повтор связи
			} while ( lx < 1 || ly < 1 || lx > frZone.width() - 1 || ly > frZone.height() - 1 || nerv_links[lx][ly] );

            if (lx >= 1 && ly >= 1 && lx < frZone.width() - 1 && ly < frZone.height() - 1) {
                if (debug) System.out.print(".");

                nerv_links[lx][ly] = true;

				// Создаем синаптическую связь
                for (int z = 0; z < toZone.depth; z++) {
                    for (int lz = 0; lz < frZone.depth; lz++) {
                    	linksSenapse(lx, ly, lz, x, y, z, i);
                    }
                }
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

	public Float getInitWeight() {
		return w * rnd.nextFloat();
	}

	@Override
	public LayerSimple frZone() {
		return frZone;
	}

	@Override
	public LayerWLearning toZone() {
		return toZone;
	}

	@Override
	public Matrix<Integer> senapses() {
		return senapse;
	}

	@Override
	public Matrix<Float> senapseWeight() {
		return senapseWeight;
	}

	@Override
	public Matrix<Float> lateralWeight() {
		return null;
	}

	@Override
	public double fX() {
		return fX;
	}

	@Override
	public double fY() {
		return fY;
	}

	@Override
	public int ns_links() {
		return ns_links;
	}

	@Override
	public double disp() {
		return disp;
	}

	@Override
	public boolean soft() {
		return soft;
	}

	@Override
	public Matrix<Integer[]> lateralSenapse() {
		return null;
	}
	
	@Override
	public Matrix<Float> inhibitoryWeight() {
		return inhibitoryWeight;
	}

	ColumnRF_Image imageable = null;

	@Override
	public Imageable getImageable() {
		if (imageable == null) {
			imageable = new ColumnRF_Image();
		}
		return imageable;
	}
	
	protected class ColumnRF_Image implements Imageable {
		
		private int Xl;
		private int Yl;
		
		protected int boxMini;
		protected int boxSize;
		protected int boxN;
	    private BufferedImage image;
	    
	    private List<Point> watching = new ArrayList<Point>();
	    private Point atFocus = null;

		ColumnRF_Image() {
			init();
			
			this.Xl = 0;
			this.Yl = 0;
		}

		public void init() {

	        boxMini = 16;
	        boxN = (int) Math.ceil( Math.sqrt(toZone.depth) );
	        boxSize = boxMini * boxN;

			int maxX = toZone.width() * boxSize;
	        int maxY = toZone.height() * boxSize;

	        image = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
		}
	
		public String getImageName() {
			return "cols map "+MappingHebbian.this.toZone().name+" ["+Xl+","+Yl+"]";
		}

		public BufferedImage getImage() {
//			in_zones[0].toZone.colWeights.debug("colWeights");

			Graphics g = image.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
	
			g.setColor(Color.YELLOW);
			for (Point p : watching) {
				if (atFocus == p) {
					g.setColor(Color.RED);
					g.draw3DRect(p.x*boxSize, p.y*boxSize, boxSize, boxSize, true);
					g.setColor(Color.YELLOW);
				} else
					g.draw3DRect(p.x*boxSize, p.y*boxSize, boxSize, boxSize, true);
			}
			
			for (int x = 0; x < toZone().width(); x++) {
				for (int y = 0; y < toZone().height(); y++) {
					g.setColor(Color.DARK_GRAY);
					g.draw3DRect(x*boxSize, y*boxSize, boxSize, boxSize, true);
					
					Utils.drawRF(
		        		image, g,
		        		boxSize,
		        		boxMini,
		        		x*boxSize, y*boxSize,
		        		x, y,
		        		Xl, Yl,
		        		MappingHebbian.this
		    		);
				}
			}

//			g.setColor(Color.WHITE);
//			
//			int textY = g.getFontMetrics(g.getFont()).getHeight();
//			int x = 0, y = textY;
//			g.drawString("count: "+count, x, y);
			
//			currentPackage++;
//			if (!(currentPackage < package_size))
//				currentPackage = 0;
			
			return image;
		}

		@Override
		public Object whatAt(Point point) {
			try {
				Point pos = new Point(
					point.x / boxSize, 
					point.y / boxSize
				);
				
				if (pos.x >= 0 && pos.x < toZone.width && pos.y >= 0 && pos.y < toZone.height) {
					
					watching.add(pos);
					
					return new Object[] { toZone, pos };
				}
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		public void focusGained(Point point) {
			atFocus = point;
		}

		@Override
		public void focusLost(Point point) {
			atFocus = null;
		}

		@Override
		public void closed(Point point) {
			watching.remove(point);
		}

		@Override
		public void refreshImage() {
		}
	}
}