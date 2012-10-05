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
package org.animotron.animi.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.ds.openimaj.OpenImajDriver;

import org.animotron.animi.cortex.CortexZoneComplex;
import org.animotron.animi.cortex.CortexZoneSimple;
import org.animotron.animi.cortex.Retina;
import org.animotron.animi.simulator.Simulator;
import org.animotron.animi.simulator.SimulatorRectAnime;

import static org.animotron.animi.gui.Application.cortexs;

/**
 * 
 * @author Bartosz Firyn (SarXos)
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class WebcamPanel extends JPanel implements WebcamListener, MouseListener {

	private static final long serialVersionUID = 5792962512394656227L;

	private int frequency = 60; // Hz

    private long fps;
    private long frame = 0;
    private long t0 = System.currentTimeMillis();
    private long count = 0;

    // convert the original colored image to grayscale
//    ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

    private class Repainter extends Thread {

    	boolean show = true;
    	
		public Repainter() {
			setDaemon(true);
		}

		@Override
		public void run() {
			//super.run();
            while (simulator != null || webcam.isOpen()) {
				try {
					if (paused) {
						synchronized (this) {
							this.wait();
						}
					}
					if (simulator != null) {
						image = simulator.getImage();
					} else {
						image = webcam.getImage();
					}

                    if (cortexs != null && image != null) {
                    	cortexs.retina.process(image);
                    	
                    	if (cortexs.active) {
                    		cortexs.cycle1();
                    		cortexs.cycle2();
                    		count++;
                    	}
                    }
                    
                    //Thread.sleep(1000 / frequency);
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
                    if (show) {
                    	WebcamPanel.this.repaint();
                    }
                    show = !show;
                }
			}
		}
	}
    
    private Simulator simulator;

	private Webcam webcam = null;
    private BufferedImage image = null;
    private BufferedImage buffer = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);
    private Repainter repainter = null;

	public WebcamPanel() {
		
		//simulate
		if (true) {
			simulator = new SimulatorRectAnime(
                    Retina.WIDTH, Retina.HEIGHT, 100,
                    new int[][] {
                            {80, 80},
                            {Retina.WIDTH - 80, 80},
                            {Retina.WIDTH - 80, Retina.HEIGHT - 80},
                            {80, Retina.HEIGHT - 80},
                            {80, 80}
                    }
            );
			repainter = new Repainter();
			repainter.start();
		}

		SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

            	if (simulator == null) {
	                Webcam.setDriver(new OpenImajDriver());
	                webcam = Webcam.getDefault();
	                if (webcam == null) {
	                    System.out.println("No webcams found...");
	                    System.exit(1);
	                }
	                webcam.setViewSize(new Dimension(Retina.WIDTH, Retina.HEIGHT));
	                webcam.addWebcamListener(WebcamPanel.this);
	
	                if (!webcam.isOpen()) {
	                    webcam.open();
	                }
            	}
            }
        });
		
		addMouseListener(this);
	}
	
    @Override
	protected void paintComponent(Graphics _g) {
    	
//    	buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    	Graphics g = buffer.getGraphics();

		super.paintComponent(g);

		if (image == null) {
			return;
		}

    	int textY;
		int x = 0;
		int y = textY = getFontMetrics(getFont()).getHeight();

        g.setColor(Color.BLACK);

        g.drawString(fps + " fps; "+count+" cycles;", 0, textY);

    	int zoomX = image.getWidth();//3;
		int zoomY = image.getHeight();//3;
        g.drawImage(
    		image.getScaledInstance(zoomX, zoomY, Image.SCALE_SMOOTH),
    		x, y, null
		);
        x += zoomX + 2;

        if (cortexs != null) {
//	        BufferedImage retina = cortexs.retina.getImage();
//	        g.drawImage(
//        		retina.getScaledInstance(zoomX, zoomY, Image.SCALE_SMOOTH),
//        		x, y, null
//    		);
//	        x += zoomX + 2;
	        
	        for (CortexZoneSimple zone : cortexs.zones) {
	        	
                g.drawString(zone.toString(), x, textY);
                
                BufferedImage img = zone.getColImage();
                g.drawImage(
//            		img.getScaledInstance(img.getWidth()*2, img.getHeight()*2, Image.SCALE_AREA_AVERAGING),
            		img,
            		x, y, null
        		);
    	        x += img.getWidth() + 2;
                
                if (zone instanceof CortexZoneComplex) {
                    x = 0;
                    y += zoomY + 2;

                    CortexZoneComplex cz = (CortexZoneComplex) zone;

    	        	y += getFontMetrics(getFont()).getHeight();
    	        	textY = y;
                    g.drawString("входной слой от выходных колонок", x, textY);

                    BufferedImage RFimg = cz.getColumnRFimage();
                    g.drawImage(
                    		RFimg,
//                    		RFimg.getScaledInstance(RFimg.getWidth()*2, RFimg.getHeight()*2, Image.SCALE_AREA_AVERAGING),
                    		x, y, null);
                    x += RFimg.getWidth() + 2;
                }
            }
        }
        
        if (zoomPoint != null) {
        	zoomX = zoomPoint.x;
        	zoomY = zoomPoint.y;

        	BufferedImage zoomer = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        	int zoomerX = 0, zoomerY = 0;
        	for (int zX = zoomX - 20; zX <= zoomX + 20; zX++) {
        		zoomerY = 0;
            	for (int zY = zoomY - 20; zY <= zoomY + 20; zY++) {
            		if (zX > buffer.getWidth() && zY > buffer.getHeight()) {

        				zoomer.setRGB(zoomerX, zoomerY, Color.BLACK.getRGB());
            		} else
	            		try {
	            			zoomer.setRGB(zoomerX, zoomerY, buffer.getRGB(zX, zY));
	            		} catch (Exception e) {

	            			try {
	            				zoomer.setRGB(zoomerX, zoomerY, Color.BLACK.getRGB());
	            			} catch (Exception e1) {
							}
						}
            		zoomerY++;
            	}
            	zoomerX++;
        	}
        	int zoomFactor = 10;
        	g.drawImage(
//    			zoomer, 
    			zoomer.getScaledInstance(
					zoomer.getWidth()*zoomFactor, 
					zoomer.getHeight()*zoomFactor, 
					Image.SCALE_AREA_AVERAGING
				),
    			getWidth() - (zoomer.getWidth()*zoomFactor), 0, null);
        }
        _g.drawImage(buffer, 0, 0, null);
    }

//	@Override
//	protected void paintComponent(Graphics g) {
//
//		super.paintComponent(g);
//
//		if (image == null) {
//			return;
//		}
//
//		int x = image.getWidth()/3;
//		int y = image.getHeight()/3;
//        g.drawImage(
//    		image.getScaledInstance(x, y, Image.SCALE_SMOOTH),
//    		0, 0, null
//		);
//
//        if (cortexs != null) {
//	        BufferedImage retina = cortexs.retina.getImage();
//	        g.drawImage(
//        		retina.getScaledInstance(x, y, Image.SCALE_SMOOTH),
//        		x+2, 0, null
//    		);
//	
//	        y += 2;
//	        
//	        for (CortexZoneSimple zone : cortexs.zones) {
//                x = 0;
//	        	
//	        	y += getFontMetrics(getFont()).getHeight();
//	        	int textY = y;
//                g.drawString(zone.toString(), x, textY);
//                
//                y += 2;
//	        	
//                BufferedImage img = zone.getColImage();
//                g.drawImage(
//            		img.getScaledInstance(img.getWidth()*2, img.getHeight()*2, Image.SCALE_AREA_AVERAGING),
//            		x, y, null
//        		);
//                
//                x += img.getWidth() * 2 + 3;
////                if (zone instanceof CortexZoneComplex) {
////                    CortexZoneComplex cz = (CortexZoneComplex) zone;
////
////        	        g2d.drawString("активные нейроны по колонкам", x, textY);
////
////                    for (BufferedImage image : cz.getSImage()) {
////                        g.drawImage(image, x, y, null);
////                        x += cz.width() + 2;
////                    }
////        	        g2d.drawString("занятые нейроны по колонкам", x, textY);
////                    for (BufferedImage image : cz.getOccupyImage()) {
////                        g.drawImage(image, x, y, null);
////                        x += cz.width() + 2;
////                    }
////                }
//                y += img.getHeight()*2 + 2;
//            }
//        }
//    }

	@Override
	public void webcamOpen(WebcamEvent we) {
		if (repainter == null) {
			repainter = new Repainter();
		}
		repainter.start();
		setPreferredSize(webcam.getViewSize());
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		try {
			repainter.join();
			repainter = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	
	public void stop() {
		webcam.close();
	}

	/**
	 * @return Rendering frequency (in Hz or FPS).
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Set rendering frequency (in Hz or FPS). Min is 1 and max is 100.
	 * 
	 * @param frequency
	 */
	public void setFrequency(int frequency) {
		if (frequency > 100) {
			frequency = 100;
		}
		if (frequency < 1) {
			frequency = 1;
		}
		this.frequency = frequency;
	}
	
	Point zoomPoint = null;

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		zoomPoint = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}
