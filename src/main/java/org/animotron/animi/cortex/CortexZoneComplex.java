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

import org.animotron.animi.*;
import org.animotron.animi.acts.*;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Complex cortex zone
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CortexZoneComplex extends CortexZoneSimple {
	
	@Params
	public Mapping[] in_zones;

	CNActivation cnActivation = new CNActivation(this);
	@Params
    Inhibitory inhibitory = new Inhibitory(this);
	@Params
	Restructorization restructorization = new Restructorization(this);

	@InitParam(name="disper")
	public double disper = 1.5;

	@InitParam(name="inhibitory_links")
	public int number_of_inhibitory_links = 30;
	
	@InitParam(name="inhibitory_w")
	public float inhibitory_w = (float)Math.sqrt(1 / (double)number_of_inhibitory_links);
	
	public int inhibitoryLinksSenapse[];
	
	/** Number of synaptic connections of the all simple neurons **/
	public int ns_links;
	/** Number of axonal connections of the all simple neurons **/
	protected int nas_links = 9;
	/** Number of synaptic connections of the complex neuron **/
	public int nsc_links;
	
    /**
     * The OpenCL memory object which store the neuron links for each zone.
     */
    public cl_mem cl_senapseOfinhibitoryLinks;

    CortexZoneComplex() {
		super();
    }

	CortexZoneComplex(String name, MultiCortex mc, int width, int height, Mapping[] in_zones) {
		super(name, mc);
		
		this.width = width;;
		this.height = height;
		
		this.in_zones = in_zones;
    }
	
    public cl_mem inhibitoryLinks;
	
    /**
     * Initializes the OpenCL memory object and the BufferedImage which will later receive the pixels
     */
    public void init() {
    	super.init();
    	
		//count number of links
//    	ns_links = 0;
//        for (Mapping m : in_zones) {
//            ns_links += m.ns_links;
//		}

        //mapping
		for (Mapping m : in_zones) {
			m.map(this);
		}

//		inhibitoryLinks = clCreateBuffer(mc.context, CL_MEM_WRITE_ONLY, inhibitory_links * Sizeof.cl_uint, null, null);

		double X, Y, S;
		double x_in_nerv, y_in_nerv;

		//разброс торозных связей
		inhibitoryLinksSenapse = new int[width * height * number_of_inhibitory_links * 2];
		Arrays.fill(inhibitoryLinksSenapse, 0);

		double sigma, _sigma = disper;
        boolean[][] nerv_links = new boolean[width()][height()];
        
        int _sigma_ = 1;//(int) _sigma;

		//UNDERSTAND: is it ok to have sum ^2 ~ 1
//		double w = Math.sqrt(1 / (double)inhibitory_links);

		for (int x = _sigma_; x < width() - _sigma_; x++) {
			for (int y = _sigma_; y < height() - _sigma_; y++) {
//				System.out.println("x = "+x+" y = "+y);

				x_in_nerv = x;
				y_in_nerv = y;
		        sigma = _sigma;

				// Обнуление массива занятости связей
				for (int n1 = 0; n1 < width(); n1++) {
					for (int n2 = 0; n2 < height(); n2++) {
						nerv_links[n1][n2] = false;
					}
				}

				// преобразование Бокса — Мюллера для получения
				// нормально распределенной величины
				// DispLink - дисперсия связей
				int count = 0;
				for (int i = 0; i < number_of_inhibitory_links; i++) {
                    int lx, ly;
                    do {
//                        do {
                            if (count > number_of_inhibitory_links * 5) {
                            	if (Double.isInfinite(sigma)) {
                            		System.out.println("initialization failed @ x = "+x+" y = "+y);
                            		System.exit(1);
                            	}
                            	sigma += _sigma * .1;
//    							System.out.println("\n"+i+" of ("+sigma+")");
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
//                        } while (!(lx >= 1 && ly >= 1 && lx < width() - 1 && ly < height() - 1));

//                        System.out.print("!");

                    // Проверка на повтор связи
					} while ((lx >= 1 && ly >= 1 && lx < width() - 1 && ly < height() - 1) && nerv_links[lx][ly]);
//                    System.out.print(".");

                    if ((lx >= 1 && ly >= 1 && lx < width() - 1 && ly < height() - 1)) {
						nerv_links[lx][ly] = true;
	
						// Создаем синаптическую связь
//						new Link(getCol(lx, ly), getCol(x, y), w, LinkType.INHIBITORY);
						inhibitoryLinksSenapse[(y*width*number_of_inhibitory_links*2)+ (number_of_inhibitory_links*2*x)+ i*2   ] = lx;
						inhibitoryLinksSenapse[(y*width*number_of_inhibitory_links*2)+ (number_of_inhibitory_links*2*x)+ i*2 +1] = ly;
                    }
				}
//				System.out.println();
			}
		}
		cl_senapseOfinhibitoryLinks = 
				clCreateBuffer(
					mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR,
					inhibitoryLinksSenapse.length * Sizeof.cl_int, Pointer.to(inhibitoryLinksSenapse), null);

//        
//        if (CRF != null) {
//        	CRF.init();
//        }
//        
//        if (RRF != null) {
//        	RRF.init();
//        }
	}
    
	// Картинка активных нейронов по колонкам
//	public BufferedImage[] getSImage() {
//		BufferedImage[] a = new BufferedImage[deep];
//		for (int z = 0; z < deep; z++) {
//			BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
//			for (int x = 0; x < width(); x++) {
//				for (int y = 0; y < height(); y++) {
//					int c = s[x][y][z].activity > 0 ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
//					image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
//				}
//			}
//			a[z] = image;
//		}
//		return a;
//	}
	
	public void prepareForSerialization() {
		CRF = null;
		RRF = null;
	}
	
	ColumnRF_Image CRF = null;
	
	public Imageable getCRF() {
		if (CRF == null)
			CRF = new ColumnRF_Image();
		
		return CRF;
	}

	class ColumnRF_Image implements Imageable {
		
	    private int boxSize = 5;
	    private BufferedImage image;
	    
	    private List<Point> watching = new ArrayList<Point>();
	    private Point atFocus = null;

		ColumnRF_Image() {
			init();
		}

		public void init() {

	        boxSize = 5;

			int maxX = width() * boxSize;
	        int maxY = height() * boxSize;
	        
//	        if (maxX > 600) {
//	        	maxX = 600;
//	        }
//	        if (maxY > 600) {
//	        	maxY = 600;
//	        }
	        
	        image = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
		}
	
		public String getImageName() {
			return "input from output "+CortexZoneComplex.this.name;
		}

		public BufferedImage getImage() {
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
	
			for (int x = 0; x < width(); x++) {
				for (int y = 0; y < height(); y++) {
			        Utils.drawRF(
		        		image, 
		        		boxSize,
		        		x*boxSize, y*boxSize,
		        		x, y, 
		        		in_zones[0]
		    		);
				}
			}
			
			int textY = g.getFontMetrics(g.getFont()).getHeight();
			int x = 0, y = textY;
			g.drawString("count: "+count, x, y);		
			
			return image;
		}

		@Override
		public Object whatAt(Point point) {
			try {
				Point pos = new Point(
					Math.round(point.x / boxSize), 
					Math.round(point.y / boxSize)
				);
				
				if (pos.x > 1 && pos.x < width && pos.y > 1 && pos.y < height) {
					
					watching.add(pos);
					
//					System.out.println("x = "+pos.x+" y = "+pos.y);
					
					return new Object[] { CortexZoneComplex.this, pos };
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

	RRF_Image RRF = null;
	
	public Imageable getRRF() {
		if (RRF == null)
			RRF = new RRF_Image();
		
		return RRF;
	}

	class RRF_Image implements Imageable {
		
	    private BufferedImage image;
	    
	    RRF_Image() {
	    	init();
		}
	    
	    public void init() {
			CortexZoneSimple zone = in_zones[0].frZone;
	        image = new BufferedImage(zone.width, zone.height, BufferedImage.TYPE_INT_RGB);
	    }
	
		public String getImageName() {
			return "restored input";
		}

		public BufferedImage getImage() {
			
			int max = 0, value = 0, R = 0, G = 0, B = 0;

	        Graphics g = image.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
	
			for (int x = 0; x < width(); x++) {
				for (int y = 0; y < height(); y++) {

					Utils.drawRF(
		        		image, 
		        		x, y, 
		        		in_zones[0]
		    		);
				}
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

		@Override
		public void refreshImage() {
		}
	}

    public void process() {
    	if (!isActive())
    		return;
    	
    	cycleActivation();
    	
    	if (isLearning()) {
    		cycleLearning();
    	
    		count++;
    	}
    }
    
    private void performTask(Task task) {
        try {
            mc.taskQueue.put(task);
        
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        mc.finish();
    }

    //Такт 1. Активация колонок (узнавание)
    protected void cycleActivation() {
    	
    	performTask(cnActivation);
//		for (int i : new int[] {1,2,3,4,5}) {
			performTask(inhibitory);
//		}
    }

    //Граничные нейроны не задействованы.
    //Такт 2. Запоминание  и переоценка параметров стабильности нейрона
    private void cycleLearning() {
    	performTask(restructorization);
    }

	public void save(Writer out) throws IOException {
		out.write("<zone type='complex'");
		write(out, "name", name);
		write(out, "id", id);
		write(out, "width", width);
		write(out, "height", height);
		write(out, "speed", speed);
		write(out, "active", active);
		write(out, "learning", learning);
		write(out, "count", count);

		write(out, "inhibitory-links-", disper);
		write(out, "number-of-inhibitory-links", number_of_inhibitory_links);

		out.write(">");
		
		for (Mapping mapping : in_zones) {
			out.write("<mapping");
			write(out, "synaptic-links-dispersion", mapping.disp);
			write(out, "number-of-synaptic-links", mapping.ns_links);
			write(out, "with-zone", mapping.frZone.id);
			write(out, "soft", mapping.soft);
			out.write("/>");
			
		}
		//XXX: fix save
//		for (int x = 0; x < width; x++) {
//			for (int y = 0; y < height; y++) {
//				NeuronComplex cn = col[x][y];
//				
//				out.write("<cn");
//				write(out, "x", cn.x);
//				write(out, "y", cn.y);
//				out.write(">");
//				for (LinkQ link : cn.Qs.values()) {
//					out.write("<linkS");
//					write(out, "w", link.q);
//					write(out, "sX", link.synapse.x);
//					write(out, "sY", link.synapse.y);
//					out.write("/>");
//				}
//				for (Link link : cn.s_inhibitoryLinks) {
//					out.write("<linkI");
//					write(out, "w", link.w);
//					write(out, "sX", link.synapse.x);
//					write(out, "sY", link.synapse.y);
//					out.write("/>");
//				}
//				out.write("</cn>");
//			}
//		}
		out.write("</zone>");
	}
}