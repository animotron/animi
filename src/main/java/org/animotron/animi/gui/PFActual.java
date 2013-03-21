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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.animotron.animi.*;
import org.animotron.animi.cortex.*;
import org.animotron.animi.cortex.old.LinkQ;
import org.animotron.animi.cortex.old.NeuronComplex;
import org.animotron.animi.cortex.old.NeuronSimple;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class PFActual implements Imageable, InternalFrameListener {

	static List<Field> cnFds = new ArrayList<Field>();
	static List<Field> snFds = new ArrayList<Field>();
	
	int zoom = 3;

	static {
		Field[] fields = NeuronComplex.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.isAnnotationPresent(RuntimeParam.class))
				cnFds.add(field);
		}

		fields = NeuronSimple.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.isAnnotationPresent(RuntimeParam.class))
				snFds.add(field);
		}
	}
	
	LayerWLearning zone;
	Point point;
	NeuronComplex cn;
	
	public PFActual(Object[] objs) {
		zone = (LayerWLearning) objs[0];
		point = (Point) objs[1];
		cn = null;//zone.col[point.x][point.y];
	}

	@Override
	public String getImageName() {
		return "running params";
	}

	@Override
	public BufferedImage getImage() {
		
		calcBoxSize();
		
		int num = 7;
//		Iterator<LinkQ> iter = cn.Qs.values().iterator();
//		if (!iter.next().synapse.Qs.isEmpty()) {
//			num = 5;
//		}
		

		BufferedImage image = new BufferedImage((boxSize*zoom+5)*num, (boxSize*zoom*4)+(10*5), BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);

		int x = 0, y = 0;
		
		int textY = g.getFontMetrics(g.getFont()).getHeight();

		y += textY;
		g.drawString("column [ "+point.x+" : "+point.y+" ]", x, y);		

		x = 0;
		y += textY;
        g.drawString("RF", x, y);
        
        BufferedImage img = null;
        for (int pN = 0; pN < zone.depth; pN++) {
        	
        	int cY = y;
        	
			img = drawRF(true, pN);
			g.drawRect(x, cY, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
			g.drawImage(
					img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
					x+1, cY+1, null);
			
			int cX = x + (2+(img.getWidth()*zoom)) + 2;

			img = drawRF(false, pN);
			g.drawRect(cX, cY, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
			g.drawImage(
					img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
					x+1, cY+1, null);

			cY += 2+(img.getHeight()*zoom);
			
//			cY += textY;
//			g.drawString(String.valueOf(zone.freePackageCols(point.x, point.y, pN)), x, cY);		

			cY += textY;
			g.drawString("act "+Math.round(zone.colNeurons.get(point.x, point.y, pN)*1000)/(double)1000, x, cY);		
			
//			System.out.println(zone.packageCols.get(point.x, point.y, pN));
			
			x += (2+(img.getWidth()*zoom)) + 2;
        }

		x = 0;
		y += 2+img.getHeight()*zoom + textY*2;

		y += textY;
        g.drawString("Total RF", x, y);

		img = drawTotalRF();
		g.drawRect(x, y, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
		g.drawImage(
				img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
				x+1, y+1, null);

		
		x = 0;
		y += 2+(img.getHeight()*zoom);
		y += textY;
		g.drawString("act "+String.valueOf(zone.cols.get(point.x, point.y)), x, y);		

		return image;
	}
	
	int boxSize = 0;

	private void calcBoxSize() {
//		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
//		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
//
//		for (LinkQ link : cn.Qs.values()) {
//        	minX = Math.min(minX, link.synapse.x);
//        	minY = Math.min(minY, link.synapse.y);
//
//        	maxX = Math.max(maxX, link.synapse.x);
//        	maxY = Math.max(maxY, link.synapse.y);
//        }
        boxSize = 15;//Math.max(maxX - minX, maxY - minY) + 2;
	}

	private BufferedImage drawRF(boolean isPos, int packageNumber) {
        return Utils.drawRF(
    		isPos,
    		new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB),
    		boxSize, 0, 0, 
    		point.x, point.y,
    		packageNumber,
    		zone.in_zones[0]
		);
	}
	
	private BufferedImage drawTotalRF() {
        BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);

        if (point == null)
        	return image;

        final int cnX = point.x;
        final int cnY = point.y;

        Mapping m = zone.in_zones[0];
        
        int pX = 0, pY = 0;
        for (int l = 0; l < m.ns_links(); l++) {
        	int xi = m.senapses().get(cnX, cnY, l, 0);
        	int yi = m.senapses().get(cnX, cnY, l, 1);

        	if (m.toZone().isSingleReceptionField()) {
	        	pX = (boxSize / 2) + (xi - (int)(m.toZoneCenterX() * m.fX()));
				pY = (boxSize / 2) + (yi - (int)(m.toZoneCenterY() * m.fY()));
        	} else {
	        	pX = (boxSize / 2) + (xi - (int)(cnX * m.fX()));
				pY = (boxSize / 2) + (yi - (int)(cnY * m.fY()));
        	}
        	if (pX >= 0 && pX < boxSize 
        			&& pY >= 0 && pY < boxSize) {
            	
            	final int c = 255;
				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
            }
        }
        return image;
	}

//	private BufferedImage drawInhibitoryRF() {
//        BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
//
//        int pX, pY;
//		for (Link link : cn.s_inhibitoryLinks) {
//        	pX = (boxSize / 2) + (link.synapse.x - cn.x);
//			pY = (boxSize / 2) + (link.synapse.y - cn.y);
//                    	
//			if (       pX > 0 
//        			&& pX < boxSize 
//        			&& pY > 0 
//        			&& pY < boxSize) {
//	                    	
//            	int c = Utils.calcGrey(image, pX, pY);
//				c += 255 * link.w[0];
//				if (c > 255) c = 255;
//				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
//        	}
//        }
//        return image;
//	}

//	private BufferedImage drawTotalInhibitoryRF() {
//        BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
//
//        int pX, pY;
//		for (Link link : cn.s_inhibitoryLinks) {
//        	pX = (boxSize / 2) + (link.synapse.x - cn.x);
//			pY = (boxSize / 2) + (link.synapse.y - cn.y);
//                    	
//			if (       pX > 0 
//        			&& pX < boxSize 
//        			&& pY > 0 
//        			&& pY < boxSize) {
//	                    	
//            	int c = Utils.calcGrey(image, pX, pY);
//				c += 255;
//				if (c > 255) c = 255;
//				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
//        	}
//        }
//        return image;
//	}
	
	private void drawStepUp(NeuronComplex cn, double q, int delay, BufferedImage image) {
		for (LinkQ link : cn.Qs.values()) {
			
			if (link.synapse.Qs.size() == 0) {
	        	int pX = link.synapse.x;
	        	int pY = link.synapse.y;
	        	
				if (       pX > 0 
	        			&& pX < image.getWidth() 
	        			&& pY > 0 
	        			&& pY < image.getHeight()) {
		                    	
			        int value = image.getRGB(pX, pY);

			        int G = Utils.get_green(value);
			        int B = Utils.get_blue(value);
			        int R = Utils.get_red(value);

			        switch (delay) {
					case 0:
						G += 255 * link.q * q;
						if (G > 255) G = 255;
						
						break;

					case 1:
						B += 255 * link.q * q;
						if (B > 255) B = 255;
						
						break;
					
					default:
						R += 255 * link.q * q;
						if (R > 255) R = 255;

						break;
					}

					image.setRGB(pX, pY, Utils.create_rgb(255, R, G, B));
	        	}
			} else {
				drawStepUp(link.synapse, q * link.q, delay, image);
			}
		}
	}

	private BufferedImage draw2upRF() {
		int _boxSize = boxSize*zoom*2;
        BufferedImage image = new BufferedImage(_boxSize, _boxSize, BufferedImage.TYPE_INT_ARGB);
        
        NeuronComplex _cn = null;
        Collection<LinkQ> links = cn.Qs.values();
        while (!links.isEmpty()) {
        	_cn = links.iterator().next().synapse;
        	links = _cn.Qs.values();
        }
        if (_cn != null) {
        	final int x = _cn.zone.height() + 1;
        	final int y = _cn.zone.width() + 1;
            
        	Graphics g = image.getGraphics();
            g.drawLine(0, y, x, y);
            g.drawLine(x, y, x, 0);
        }

		for (LinkQ link : cn.Qs.values()) {
			drawStepUp(link.synapse, link.q, link.delay, image);
        }
        return image;
	}

	private BufferedImage drawIn() {
        BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);

		int pX, pY = 0;
        for (LinkQ link : cn.Qs.values()) {

        	pX = (boxSize / 2) + (link.synapse.x - (int)(cn.x * link.fX));
			pY = (boxSize / 2) + (link.synapse.y - (int)(cn.y * link.fY));
        	
			if (       pX >= 0 
        			&& pX < boxSize 
        			&& pY >= 0 
        			&& pY < boxSize) {
            	
		        int value = image.getRGB(pX, pY);

		        int g = Utils.get_green(value);
		        int b = Utils.get_blue(value);
		        int r = Utils.get_red(value);

		        switch (link.delay) {
				case 0:
					g += 255 * link.synapse.activity[link.delay];
					if (g > 255) g = 255;
					
					break;

				case 1:
					g += 255 * link.synapse.activity[link.delay];
					if (g > 255) g = 255;
					
					break;

				default:
					r += 255 * link.synapse.activity[2];
					if (r > 255) r = 255;

					break;
				}

				image.setRGB(pX, pY, Utils.create_rgb(255, r, g, b));
			} else {
//				System.out.println("WRONG "+pX+" "+pY);
            }
        }
        return image;
	}

	private String getName(Field f) {
		return f.getName();
//		return f.getAnnotation(RuntimeParam.class).name();
	}

	private String getValue(Field f, Object obj) {
		try {
			Object o = f.get(obj);
			if (o.getClass().isArray()) {
				return Arrays.toString((Object[]) o);
			} else {
				return o.toString();
			}
		} catch (Exception e) {
		}
		return "???";
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
	public void internalFrameOpened(InternalFrameEvent e) {
//		zone.getCRF().focusGained(point);
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
//		zone.getCRF().focusLost(point);
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
//		zone.getCRF().closed(point);
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
//		zone.getCRF().focusGained(point);
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
//		zone.getCRF().focusLost(point);
	}

	@Override
	public void refreshImage() {
	}
}