/*
 *  Copyright (C) 2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animi.
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
package org.animotron.animi.simulator;

import static org.animotron.animi.gui.Application.cortexs;
import static org.animotron.animi.cortex.MultiCortex.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.animotron.animi.Imageable;
import org.animotron.animi.Params;
import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.MultiCortex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class StaticStimulator implements Runnable, Imageable {
	
	@RuntimeParam(name = "frequency")
	public int frequency = 0; // Hz

    @Params
    public Figure[] figures;
    
    private long fps;
    private long frame = 0;
    private long t0 = System.currentTimeMillis();

    private boolean run = true;
    
    MultiCortex mc;

    public StaticStimulator(MultiCortex cortexs) {
    	mc = cortexs;
    	init();
	}

	public void init() {
        figures = new Figure[] {
        	new OvalAnime(15, mc.retina.width(), mc.retina.height()),
        	new RectAnime(15, mc.retina.width(), mc.retina.height()),
        	new Triangle(15, mc.retina.width(), mc.retina.height())
        };
	}
    
	public void reset() {
		for (Figure figure : figures) {
			figure.reset();
        };
	}

	@Override
	public void run() {
        while (run) {
			try {
				if (paused) {
					synchronized (this) {
						this.wait();
					}
				}
                long t = System.currentTimeMillis();
				
				prosess();
                
                if (frequency != 0) {
                    t = (1000 / frequency) - (System.currentTimeMillis() - t);

                    if (t > 0)
                    	Thread.sleep(t);
                }

			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
                frame++;
                long t = System.currentTimeMillis();
                long dt = t - t0;
                if (dt > 1000) {
                    fps = 1000 * frame / dt;
                    frame = 0;
                    t0 = t;
                }
            }
		}
	}
	
	public void prosess() {
		final BufferedImage image = getImage();

        if (cortexs != null && MODE >= STEP && image != null) {
        	cortexs.retina.process(image);
        	
    		cortexs.process();
        }
	}
	
	private Thread th = null;
	private volatile boolean paused = false;
	
	public synchronized void start() {
		resume();
		
		if (th != null) return;
		
		th = new Thread(this);
		th.setDaemon(true);
		th.start();
	}

	public void pause() {
		paused = true;
	}

	public void resume() {
		paused = false;
		synchronized (this) {
			notifyAll();
		}
	}
	

	@Override
	public String getImageName() {
		return this.getClass().getSimpleName();
	}

	public BufferedImage getImage() {

        BufferedImage img = new BufferedImage(mc.retina.width(), mc.retina.height(), BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();

        for (Figure i : figures) {
    		i.drawImage(g);
        }

        return img;
	}
	
	public BufferedImage getUserImage() {
        //workaround
        BufferedImage img = new BufferedImage(mc.retina.width(), mc.retina.height(), BufferedImage.TYPE_INT_RGB);

        Graphics g = img.getGraphics();

        for (Figure i : figures) {
    		i.drawImage(g);
        }
        drawUImage(g);
        
    	return img;
	}

	protected void drawUImage(Graphics g) {
		int textY = g.getFontMetrics(g.getFont()).getHeight();

        g.setColor(Color.WHITE);

        g.drawString(fps + " fps; "+cortexs.count+" cycles;", 0, textY);
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
		// TODO Auto-generated method stub
		
	}
}
