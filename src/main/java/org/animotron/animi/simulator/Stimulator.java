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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.animotron.animi.Imageable;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class Stimulator implements Runnable, Imageable {
	
	protected BufferedImage image;

	private int frequency = 60; // Hz

    private long fps;
    private long frame = 0;
    private long t0 = System.currentTimeMillis();
    private long count = 0;

    private boolean run = true;
    
    protected Stimulator() {}

    protected Stimulator(int frequency) {
    	this.frequency = frequency;
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
				final BufferedImage image = getImage();

                if (cortexs != null && image != null) {
                	cortexs.retina.process(image);
                	
                	if (cortexs.active) {
                		cortexs.cycle1();
                		cortexs.cycle2();
                		count++;
                	}
                }
                
                if (frequency != 0)
                	Thread.sleep(1000 / frequency);
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
	
	private Thread th = null;
	private volatile boolean paused = false;
	
	public synchronized void start() {
		if (th != null) return;
		
		th = new Thread(this);
		th.setDaemon(true);
		th.start();
	}

	public void pause() {
		if (paused) return;

		paused = true;
	}

	public void resume() {
		if (!paused) return;

		synchronized (this) {
			notifyAll();
		}
		paused = false;
	}
	

	@Override
	public String getImageName() {
		return this.getClass().getSimpleName();
	}

	public BufferedImage getImage() {
        step();
        
        //workaround
        BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();

    	drawImage(g);
        
        return img;
	}
	
	public BufferedImage getUserImage() {
        //workaround
        BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics g = img.getGraphics();

    	drawImage(g);
    	drawUImage(g);
        
    	return img;
	}

	protected abstract void drawImage(Graphics g);
	
	protected void drawUImage(Graphics g) {
		int textY = g.getFontMetrics(g.getFont()).getHeight();

        g.setColor(Color.WHITE);

        g.drawString(fps + " fps; "+count+" cycles;", 0, textY);
	}

	protected abstract void step();
}
