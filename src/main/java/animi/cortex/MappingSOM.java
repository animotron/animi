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
package animi.cortex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import animi.*;
import animi.cortex.MappingHebbian.ColumnRF_Image;
import animi.matrix.*;

/**
 * Projection description of the one zone to another
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class MappingSOM implements Mapping {
	
	private static final boolean debug = false;
	
	private static final Random rnd = new Random();
	
	interface Value {
		public float value(int x1, int y1, int x2, int y2, double sigma);
	}
	
	private LayerSimple frZone;
	private LayerWLearning toZone;
	
	public double fX = 1;
	public double fY = 1;

	public float w;
	
	@InitParam(name="ns_links")
    public int ns_links;           // Number of synaptic connections for the layer
    
	@InitParam(name="nl_links")
    public int nl_links;           // Number of lateral connections for the layer

	/** дисперсия связей **/
	@InitParam(name="disp")
	public double disp;      // Describe a size of sensor field

	private Matrix<Integer[]> senapses;
	private Matrix<Integer> _senapses;

	private Matrix<Float> senapseWeight;
	private Matrix<Float> inhibitoryWeight;
	
	private Matrix<Float> lateralWeight;
	private Matrix<Integer[]> lateralSenapse;
	private Value lateralWeightValue;
	
	private void linksSenapse(int Sx, int Sy, int Sz, int x, int y, int z, int l) {
		if (toZone.singleReceptionField) {
			for (int xi = 0; xi < toZone.width(); xi++) {
				for (int yi = 0; yi < toZone.height(); yi++) {
					for (int zi = 0; zi < toZone.depth; zi++) {
						senapses.set(new Integer[] {Sx, Sy, Sz}, xi, yi, zi, l);

						_senapses.set(Sx, xi, yi, zi, l, 0);
						_senapses.set(Sy, xi, yi, zi, l, 1);
						_senapses.set(Sz, xi, yi, zi, l, 2);
					}
				}
			}
		} else {
			senapses.set(new Integer[] {Sx, Sy, Sz}, x, y, z, l);

			_senapses.set(Sx, x, y, z, l, 0);
			_senapses.set(Sy, x, y, z, l, 1);
			_senapses.set(Sz, x, y, z, l, 2);
		}
		
//		senapses.debug("linksSenapse");
	}

	private void lateralSenapse(int Sx, int Sy, int Sz, int x, int y, int z, int l) {
		if (toZone.singleReceptionField) {
			for (int xi = 0; xi < toZone.width(); xi++) {
				for (int yi = 0; yi < toZone.height(); yi++) {
					for (int zi = 0; zi < toZone.depth; zi++) {
						lateralSenapse.set(new Integer[] {Sx, Sy, Sz}, xi, yi, zi, l);
					}
				}
			}
		} else {
			lateralSenapse.set(new Integer[] {Sx, Sy, Sz}, x, y, z, l);
		}
	}

	MappingSOM () {}
	
    public MappingSOM(LayerSimple zone, int ns_links, double disp, int nl_links, Value lateralWeightValue) {
        frZone = zone;
        
        this.disp = disp;
        this.ns_links = ns_links;
        
        this.nl_links = nl_links;
        
        this.lateralWeightValue = lateralWeightValue;
        if (this.lateralWeightValue == null) {
        	this.lateralWeightValue = new Value() {
				@Override
				public float value(int x1, int y1, int x2, int y2, double sigma) {
	            	return 
            			(float) Math.exp( 
	            			- Math.sqrt( (x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1) )
	            			/ sigma
	        			);
				}
        	};
        }
        
//		DecimalFormat df = new DecimalFormat("0.00000");
//
//		double value = 0f;
//        double sigma2 = Math.sqrt(6*6 / Math.PI);
//        int lx = 2, ly = 2;
//        for (int x = 0; x < 6; x++) {
//            for (int y = 0; y < 6; y++) {
//        
//            	value = Math.exp( 
//	            			- Math.sqrt( (lx - x)*(lx - x) + (ly - y)*(ly - y) )
//	            			/ 2.0
//            			);
//            	
//            	System.out.print(df.format( value ) );
//            	System.out.print(" ");
//            }
//            System.out.println();
//        }        
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
		w = (1 / (float)(ns_links * frZone.depth));// / norm;

		senapses = new MatrixArrayInteger(3, toZone.width(), toZone.height(), toZone.depth(), ns_links * frZone.depth);
		_senapses = new MatrixInteger(toZone.width(), toZone.height(), toZone.depth(), ns_links * frZone.depth, 3);
		_senapses.fill(0);
		
	    senapseWeight = new MatrixFloat(toZone.width(), toZone.height(), toZone.depth(), ns_links * frZone.depth);
	    senapseWeight.init(new Matrix.Value<Float>() {
			@Override
			public Float get(int... dims) {
				return getInitWeight();
			}
		});
	    
	    inhibitoryWeight = new MatrixFloat(toZone.width(), toZone.height(), toZone.depth(), ns_links * frZone.depth);
	    inhibitoryWeight.init(new Matrix.Value<Float>() {
			@Override
			public Float get(int... dims) {
				return 0f;//getInitWeight();
			}
		});

	    lateralWeight = new MatrixFloat(toZone.width(), toZone.height(), toZone.depth(), nl_links);
	    lateralWeight.init(new Matrix.Value<Float>() {
			@Override
			public Float get(int... dims) {
				return 0f;
			}
		});
	    
	    lateralSenapse = new MatrixArrayInteger(3, toZone.width(), toZone.height(), toZone.depth, nl_links);
	    lateralSenapse.init(new Matrix.Value<Integer[]>() {
			@Override
			public Integer[] get(int... dims) {
				return new Integer[] {0, 0, 0};
			}
		});

		fX = frZone.width() / (double) toZone.width();
		fY = frZone.height() / (double) toZone.height();

		final boolean[][] nerv_links = new boolean[frZone.width()][frZone.height()];
		final boolean[][] lateral_nerv_links = new boolean[toZone.width()][toZone.height()];
        
		if (toZone.singleReceptionField) {

			initReceptionFields(
				(int)(toZone.width() / 2.0), 
				(int)(toZone.height() / 2.0), 
				nerv_links);
			
	        for (int x = 0; x < toZone.width(); x++) {
				for (int y = 0; y < toZone.height(); y++) {
					initLateral(
						x, y, 
						lateral_nerv_links);
				}
	        }
		} else {

	        for (int x = 0; x < toZone.width(); x++) {
				for (int y = 0; y < toZone.height(); y++) {
	
					initReceptionFields(x, y, nerv_links);

					initLateral(x, y, lateral_nerv_links);
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

		int lx = -1, ly = 0;

		// преобразование Бокса — Мюллера для получения
		// нормально распределенной величины
		// DispLink - дисперсия связей
		int count = 0;
		for (int i = 0; i < ns_links; i++) {
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
                
                if (ns_links == frZone.width() * frZone.height()) {
                    lx++;
                    if (lx >= frZone.width()) {
                    	lx = 0;
                    	ly++;
                    }
                	
                } else {
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
                }

            //определяем, что не вышли за границы поля колонок
            //колонки по периметру не задействованы
            // Проверка на повтор связи
			} while ( lx < 0 || ly < 0 || lx >= frZone.width() || ly >= frZone.height() || nerv_links[lx][ly] );

            if (lx >= 0 && ly >= 0 && lx < frZone.width() && ly < frZone.height()) {
                if (debug) System.out.print(".");

                nerv_links[lx][ly] = true;

				// Создаем синаптическую связь
                for (int z = 0; z < toZone.depth; z++) {
                    //XXX: fixed? depth 
                	int j = i * frZone.depth;
                	for (int lz = 0; lz < frZone.depth; lz++) {
                		linksSenapse(lx, ly, lz, x, y, z, j + lz);
                	}
                }
            } else {
            	if (debug) System.out.print("!");
            }
		}
		if (debug) System.out.println();
    }
    
    private void initLateral(final int x, final int y, final boolean[][] nerv_links) {
        float value = 0f;
        
    	double X, Y, S;
		double x_in_nerv, y_in_nerv;
        double _sigma, sigma;
        
        double sigma2 = Math.sqrt(nl_links / Math.PI) / 3;

		// Определение координат текущего нейрона в масштабе
		// проецируемой зоны
		x_in_nerv = x;
		y_in_nerv = y;
//		System.out.println("x_in_nerv = "+x_in_nerv+" y_in_nerv = "+y_in_nerv);

        _sigma = disp;// * ((m.zone.width() + m.zone.height()) / 2);
        sigma = _sigma;

		// Обнуление массива занятости связей
		for (int n1 = 0; n1 < toZone.width(); n1++) {
			for (int n2 = 0; n2 < toZone.height(); n2++) {
				nerv_links[n1][n2] = false;
			}
		}

		// преобразование Бокса — Мюллера для получения
		// нормально распределенной величины
		// DispLink - дисперсия связей
		int count = 0;
		for (int i = 0; i < nl_links; i++) {
            int lx, ly;
            do {
                if (count > nl_links * 3) {
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
			} while ( lx < 0 || ly < 0 || lx >= toZone.width() || ly >= toZone.height() || nerv_links[lx][ly] );

            if (lx >= 0 && ly >= 0 && lx < toZone.width() && ly < toZone.height()) {
                if (debug) System.out.print(".");

                nerv_links[lx][ly] = true;

            	value = lateralWeightValue.value(x, y, lx, ly, sigma2);

                // Создаем синаптическую связь
                //XXX: fix relation with depth, if present?
                for (int z = 0; z < toZone.depth; z++) {
                	for (int lz = 0; lz < toZone.depth; lz++) {
                    	lateralSenapse(lx, ly, lz, x, y, z, i);
                    }
                    lateralWeight.set(value, x, y, z, i);
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
	public Matrix<Integer[]> senapses() {
		return senapses;
	}

	@Override
	public Matrix<Integer> _senapses() {
		return _senapses;
	}

	@Override
	public Matrix<Float> senapseWeight() {
		return senapseWeight;
	}

	@Override
	public Matrix<Integer[]> lateralSenapse() {
		return lateralSenapse;
	}

	@Override
	public Matrix<Float> lateralWeight() {
		return lateralWeight;
	}

	@Override
	public boolean haveInhibitoryWeight() {
		return inhibitoryWeight == null;
	}

	@Override
	public Matrix<Float> inhibitoryWeight() {
		return inhibitoryWeight;
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

	ShowByMax imageable = null;

	@Override
	public Imageable getImageable() {
		if (imageable == null) {
			imageable = new ShowByMax();
		}
		return imageable;
	}
	
	class ShowByMax implements Imageable {
		
		private int boxMini;
		private int boxSize;
		private int boxN;
	    private BufferedImage image;
	    
	    private List<Point> watching = new ArrayList<Point>();
	    //XXX: private Point atFocus = null;

	    public ShowByMax() {
	    	init();
		}

		public void init() {

	        boxMini = 16;
	        boxN = (int) Math.round( Math.sqrt(toZone.depth) );
	        boxSize = boxMini * boxN;

			int maxX = toZone.width() * boxSize;
	        int maxY = toZone.height() * boxSize;

	        image = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
		}

		@Override
		public String getImageName() {
			return MappingSOM.this+" "+toZone.name;
		}

		@Override
		public void refreshImage() {
		}

		@Override
		public BufferedImage getImage() {
			Graphics g = image.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setColor(Color.WHITE);

			Mapping m = ((LayerWLearning)frZone).in_zones[0];
			
			float max = 0;
			int pos = 0;

			for (int x = 0; x < toZone.width(); x++) {
				for (int y = 0; y < toZone.height(); y++) {
					final MatrixProxy<Float> ws = senapseWeight.sub(x, y);
					
					max = 0; pos = -1;
					for (int index = 0; index < ws.length(); index++) {
						if (max < ws.getByIndex(index)) {
							max = ws.getByIndex(index);
							pos = index;
						}
					}
					if (pos == -1)
						continue;
					
					final MatrixProxy<Integer[]> sf = senapses.sub(x,y);
					
					Integer[] xyz = sf.getByIndex(pos);
					
					final int xi = xyz[0];
					final int yi = xyz[1];
					final int zi = xyz[2];
					
					final int offsetX = boxSize * x;
					final int offsetY = boxSize * y;
					
					Utils.drawRF(true , image, boxMini, 
							offsetX, offsetY,
							xi, yi, zi, m);
					
					Utils.drawNA(image, MappingSOM.this, 0, offsetX, offsetY, x, y, 0, 0, 0);
				}
			}
			
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
					
					return new ShowByOne(pos.x, pos.y);
				}
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		public void focusGained(Point point) {
		}

		@Override
		public void focusLost(Point point) {
		}

		@Override
		public void closed(Point point) {
		}
	}

	class ShowByOne implements Imageable {
		
	    private BufferedImage image;
	    
	    final Mapping m;
	    final ColumnRF_Image rf;
	    final int nX;
	    final int nY;
	    
	    public ShowByOne(int nX, int nY) {
	    	this.nX = nX;
	    	this.nY = nY;
	    	
			m = ((LayerWLearning)frZone).in_zones[0];
			rf = (ColumnRF_Image) m.getImageable();
		}

		@Override
		public String getImageName() {
			return "["+nX+","+nY+"] "+MappingSOM.this+" "+toZone.name;
		}

		@Override
		public void refreshImage() {
		}

		@Override
		public BufferedImage getImage() {
			int R = 0, G = 0, B = 0;
			
			image = rf.getImage();
			
			ColorModel cm = image.getColorModel();
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			WritableRaster raster = image.copyData(null);
			image = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
			
			Graphics g = image.getGraphics();

			final MatrixProxy<Float> pos = senapseWeight.sub(nX, nY, 0);
			final MatrixProxy<Float> neg = inhibitoryWeight.sub(nX, nY, 0);
			
			final MatrixProxy<Integer[]> sf = senapses.sub(nX, nY, 0);

			Integer[] xyz = null;
			
			for (int index = 0; index < pos.length(); index++) {
				
				xyz = sf.getByIndex(index);
				
				final int xi = xyz[0];
				final int yi = xyz[1];
				final int zi = xyz[2];
				
				final int offsetX = xi * rf.boxSize;
				final int offsetY = yi * rf.boxSize;
				
				final int pY = (int) ( zi / rf.boxN );
				final int pX = zi - (rf.boxN * pY);
					
				//weight box
				G = (int) (255 * pos.getByIndex(index) * 100);
				if (G > 255) G = 255;
				
				B = (int) (255 * neg.getByIndex(index) * 100);
				if (B > 255) B = 255;
				
				
				g.setColor(new Color(R, G, B));
				g.draw3DRect(
						offsetX + rf.boxMini * pX + 1, 
						offsetY + rf.boxMini * pY + 1, 
						rf.boxMini - 2, rf.boxMini - 2, true);

			}
			
			return image;
		}

		@Override
		public Object whatAt(Point point) {
			return null;
		}

		@Override
		public void focusGained(Point point) {
		}

		@Override
		public void focusLost(Point point) {
		}

		@Override
		public void closed(Point point) {
		}
	}

	@Override
	public boolean isDirectLearning() {
		return true;
	}

	@Override
	public Matrix<Float> senapsesCode() {
		// TODO Auto-generated method stub
		return null;
	}
}