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

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.animotron.animi.Imageable;
import org.animotron.animi.cortex.NeuronComplex;
import org.animotron.animi.simulator.Stimulator;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Visualizer extends JInternalFrame {
	
	private static final long serialVersionUID = -592610047528698167L;

	private Imageable simulator;
	private final Repainter repainter;

	private ImageCanvas canvas = new ImageCanvas();
	
	private BufferedImage image = null;
	
	public Visualizer(Imageable simulator) {
	    super(simulator.getImageName(),
	            true, //resizable
	            true, //closable
	            false, //maximizable
	            true);//iconifiable
	    
	    this.simulator = simulator;
		    
		setLocation(100, 100);
		BufferedImage img = simulator.getImage();
	    setSize(img.getWidth()+10, img.getHeight()+10);

//		setOpaque(true);
//		setDoubleBuffered(true);

		getContentPane().add(canvas);
		
		repainter = new Repainter(canvas);
		repainter.start();
	}
	
	private class ImageCanvas extends JComponent {

		private static final long serialVersionUID = -6516267401181020599L;
		
		public ImageCanvas() {
			addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					Object obj = simulator.whatAt(e.getPoint());
					//hack
					if (obj.getClass().isArray()) {
						Application._.createFrame(new RunningParamsFrame((Object[])obj));
					}
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});
		}

		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, this);
		}
	}
	
    private class Repainter extends org.animotron.animi.gui.Repainter {
    	
		public Repainter(JComponent comp) {
			super(comp);
		}

		@Override
		protected void prepareImage() {
			if (simulator instanceof Stimulator) {
				image = ((Stimulator) simulator).getUserImage();
				
			} else
				image = simulator.getImage();
		}
	}
}
