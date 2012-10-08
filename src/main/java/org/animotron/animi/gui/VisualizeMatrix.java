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
package org.animotron.animi.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.animotron.animi.Imageable;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class VisualizeMatrix extends JInternalFrame {
	
	private static final long serialVersionUID = -592610047528698167L;

	private final Repainter repainter;

	private ImageCanvas canvas = new ImageCanvas();
	
	private BufferedImage image = null;
	
	public VisualizeMatrix(Imageable simulator) {
	    super("V",
	            true, //resizable
	            true, //closable
	            false, //maximizable
	            true);//iconifiable
		    
		setLocation(100, 100);
		BufferedImage img = simulator.getImage();
	    setSize(img.getWidth()+10, img.getHeight()+10);

		setOpaque(true);
		setDoubleBuffered(true);

		getContentPane().add(canvas);
		
		repainter = new Repainter(canvas, simulator);
		repainter.start();
	}
	
	private volatile boolean paused = false;

	/**
	 * Pause rendering.
	 */
	public void pause() {
		if (paused) {
			return;
		}
		paused = true;
		System.out.println("paused");
	}

	/**
	 * Resume rendering.
	 */
	public void resume() {
		if (!paused) {
			return;
		}
		synchronized (repainter) {
			repainter.notifyAll();
		}
		paused = false;
		System.out.println("resumed");
	}

	
	private class ImageCanvas extends JComponent {
		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, this);
		}
	}
	
    private class Repainter extends Thread {

    	Imageable simulator;
    	JComponent component;

		public Repainter(JComponent comp, Imageable sim) {
			component = comp;
			simulator = sim;
			
			setDaemon(true);
		}

		@Override
		public void run() {
			//super.run();
            while (simulator != null) {
				try {
					if (paused) {
						synchronized (this) {
							this.wait();
						}
					}
					if (simulator != null) {
						System.out.println("get image");
						image = simulator.getImage();
						component.repaint();
					}
                    
//                    Thread.sleep(1000 / frequency);
				} catch (Throwable e) {
                }
			}
		}
	}
}
