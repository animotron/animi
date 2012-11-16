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

import org.animotron.animi.InitParam;
import org.animotron.animi.Utils;
import org.animotron.animi.acts.Act;
import org.animotron.animi.acts.ActWithMax;
import org.animotron.animi.acts.UpDownCNActivation;
import org.animotron.animi.acts.Zero;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;
import java.util.UUID;

/**
 * Simple cortex zone
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CortexZoneSimple implements Layer {

	String id = UUID.randomUUID().toString();

	String name;
    MultiCortex mc;
    
    /** State of complex neurons (outputs cortical columns) **/
    public NeuronComplex[][] col;
    
	@InitParam(name="width")
	public int width = 100;
	@InitParam(name="height")
	public int height = 100;
	
	public int count = 0;

	public Zero zero = new Zero();
	public UpDownCNActivation nextLayerActivation = new UpDownCNActivation();

	CortexZoneSimple() {
    	name = null;
    	mc = null;
    }

    public CortexZoneSimple(String name, MultiCortex mc) {
        this.name = name;
        this.mc = mc;
    }
    
    public void initCols() {
    	count = 0;
        col = new NeuronComplex[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                col[x][y] = new NeuronComplex(this, x, y);
            }
        }
    }

    public void init() {
    	initCols();
    }

    public BufferedImage getImage() {
        int c;
    	
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = col[x][y].backProjection > 0 ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
            }
        }
        return image;
    }

	@Override
	public double frequency() {
		return 1;
	}

	@Override
	public Object whatAt(Point point) {
		return col[point.x][point.y];
	}

    public String getImageName() {
    	return toString();
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

    Random rnd = new Random();

    public boolean saccade = false;
    
    private int next() {
		return rnd.nextInt(40) - 20;
//		int r = 20 + rnd.nextInt(20);
//		
//		if (rnd.nextBoolean())
//			return -r;
//		
//		return r;
    }

	@Override
	public void process() {
    	if (saccade) {
    		int vX = next();
    		int vY = next();
    		int steps = 10;//rnd.nextInt(20);
    		
    		int dx, dy = 0;
    		for (int step = 1; step <= steps; step++) {
    			dx = vX * step;
    			dy = vY * step;
    			for (int x = 0; x < width; x++) {
    	    		if (x+dx < 0 || x+dx >= width)
    	    			continue;
    	    		
        			for (int y = 0; y < height; y++) {
        	    		if (y+dy < 0 || y+dy >= height)
        	    			continue;
        	    		
        	    		col[x][y].activity += col[x + dx][y + dy].activity;
        	    		if (col[x][y].activity > 1)
        	    			col[x][y].activity = 1;
        			}
    			}
    		}
    	}
	}

	CortexZoneSimple[] nextLayers = null;
	
	public void nextLayers(CortexZoneSimple[] nextLayers) {
		this.nextLayers = nextLayers;
	}
	
	public void activateNextLayer() {
		boolean haveActive = false;
		for (CortexZoneSimple layer : nextLayers) {
			haveActive = layer.isActive() || haveActive;
		}
		if (haveActive)
			cycle(0, 0, width(), height(), nextLayerActivation);
	}
	
	public void zero() {
		cycle(0, 0, width(), height(), zero);
	}

	@Override
	public void set(int x, int y, double b) {
		col[x][y].activity = b;
		col[x][y].backProjection = b;
		col[x][y].posActivity = b;
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

	public NeuronComplex getCol(int x, int y) {
		return col[x][y];
	}
	
    public void cycle (int x1, int y1, int x2, int y2, Act<CortexZoneSimple> task) {
    	try {
	        for (int x = x1; x < x2; x++) {
	            for (int y = y1; y < y2; y++) {
	                task.process(this, x, y);
	            }
	        }
    	} catch (Throwable e) {
		}
    }

    public double cycle (int x1, int y1, int x2, int y2, ActWithMax<CortexZoneSimple> task, double max) {
    	try {
	        for (int x = x1; x < x2; x++) {
	            for (int y = y1; y < y2; y++) {
	            	max = task.process(this, x, y, max);
	            }
	        }
		} catch (Throwable e) {
		}
        return max;
    }
    
	public boolean learning = false;
	public boolean isLearning() {
		return learning;
	}
	
    public boolean active = false;
	public boolean isActive() {
		return active;
	}
	
	protected void write(Writer out, String name, Object value) throws IOException {
		out.write(" ");
		out.write(name);
		out.write("='");
		out.write(String.valueOf(value));
		out.write("'");
	}

	public void save(Writer out) throws IOException {
		out.write("<zone type='simple'");
		write(out, "name", name);
		write(out, "id", id);
		write(out, "width", width);
		write(out, "height", height);
		write(out, "active", active);
		write(out, "learning", learning);
		out.write("/>");
	}
}