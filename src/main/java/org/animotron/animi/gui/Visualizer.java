/*
 *  Copyright (C) 2012-2013 The Animo Project
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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameListener;

import org.animotron.animi.Imageable;
import org.animotron.animi.simulator.Stimulator;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Visualizer extends JInternalFrame {
	
	private static final long serialVersionUID = -592610047528698167L;
	
	private static Point smallP = new Point(10, 10);
	private static Point bigP = new Point(200, 10);
	
	public static void reset() {
		smallP = new Point(10, 10);
		bigP = new Point(200, 10);
	}

	private Imageable simulator;

	private ImageCanvas canvas = new ImageCanvas();
	
	private BufferedImage image = null;
	private Image bufImage = null;
	
	private int zoom = 2;
	
	public Visualizer(Imageable imageable) {
	    super(imageable.getImageName(),
	            true, //resizable
	            true, //closable
	            false, //maximizable
	            true);//iconifiable
	    
	    this.simulator = imageable;
	    
	    if (imageable instanceof InternalFrameListener)
	    	addInternalFrameListener((InternalFrameListener) imageable);

	    BufferedImage img = imageable.getImage();

		Dimension size = new Dimension(img.getWidth()*zoom+20, img.getHeight()*zoom+40);
	    
		if (img.getWidth() > 200) {
			setLocation(bigP.x, bigP.y);
		    setSize(size);
		    
		    if (bigP.y > 700) {
		    	bigP.x = 200;
		    	bigP.y = 500;
				setLocation(bigP.x, bigP.y);

		    } else if (bigP.y > 500) {
		    	bigP.x += 20;
		    	bigP.y += 20;
		    	
		    } else 
			    bigP.y += getSize().height + 10;

	    } else {
			setLocation(smallP.x, smallP.y);
		    setSize(size);
		    
		    if (smallP.y > 700) {
		    	smallP.y = 10;
				setLocation(smallP.x, smallP.y);

		    } else if (smallP.y > 500) {
		    	smallP.y += 20;

		    } else 
		    	smallP.y += getSize().height + 10;
	    }

		getContentPane().add(canvas);
		
		addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				zoom += e.getWheelRotation();
				if (zoom <= 0) zoom = 1;
				
				bufImage = null;
				
				refresh();
			}
		});
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
					Point point = new Point(
						(int)(e.getX() / (double)zoom), 
						(int)(e.getY() / (double)zoom)
					);
					Object obj = simulator.whatAt(point);
					//hack
					if (obj != null && obj.getClass().isArray()) {
						Application._.createFrame(new PFActual((Object[])obj));

					} else if (obj instanceof Imageable) {
						Application._.createFrame((Imageable)obj);
					
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
			if (image == null) return;
			
			if (bufImage == null) {
				bufImage = image.getScaledInstance(image.getWidth()*zoom, image.getHeight()*zoom, Image.SCALE_AREA_AVERAGING);
			}

			g.drawImage(
					bufImage,
					0, 0, this);
		}
	}
	
	public void refresh() {
		if (simulator instanceof Stimulator) {
			image = ((Stimulator) simulator).getUserImage();
			
		} else {
			image = simulator.getImage();
		
		}
		
		bufImage = null;

		repaint();
	}
}
