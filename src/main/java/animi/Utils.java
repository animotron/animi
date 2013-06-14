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
package animi;

import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

import animi.cortex.LayerSimple;
import animi.cortex.Mapping;
import animi.matrix.Matrix;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import static javolution.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Utils {

//    private final static double LUM_RED = 0.299;
//    private final static double LUM_GREEN = 0.587;
//    private final static double LUM_BLUE = 0.114;

    public static int calcGrey(final BufferedImage img, final int x, final int y) {
        int value = img.getRGB(x, y);

        int r = get_red(value);
        int g = get_green(value);
        int b = get_blue(value);
        
//        return r+g+b;
//        return (r+g+b) /3;
        return (int) Math.round((r + g + b) / (double)3);
    }

    public static int rgb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF) << 0);
    }

    public static int get_alpha(int rgb) {
        return (rgb >> 24) & 0xFF;
        // return rgb & 0xFF000000;
    }

    public static int get_red(int rgb) {
        return (rgb >> 16) & 0xFF;
        // return rgb & 0x00FF0000;
    }

    public static int get_green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int get_blue(int rgb) {
        return (rgb >> 0) & 0xFF;
    }
    
	public static BufferedImage drawRF(
			final BufferedImage image, final Graphics g, final int boxSize, final int boxMini,
			final int offsetX, final int offsetY,
			final int cnX, final int cnY, 
			final int Xl, final int Yl,
			final Mapping m) {

		BufferedImage img = image;
		
		final int size = (int) (boxSize / (double)boxMini);
		
		for (int pX = 0; pX < size; pX++) {
			for (int pY = 0; pY < size; pY++) {
				final int z = (pY * size) + pX;
				if (z < m.toZone().depth) {
					
					img = drawRF(true , image, boxMini, 
							offsetX + boxMini * pX, 
							offsetY + boxMini * pY,
							cnX, cnY, z, m);
					
//					img = drawRF(false, image, boxMini, 
//							offsetX + boxMini * pX, 
//							offsetY + boxMini * pY,
//							cnX, cnY, z, m);
					
					drawNA(image, m, boxMini, offsetX, offsetY, cnX, cnY, z, pX, pY);

					//weight box
//					gray = (int) (255 * m.toZone().colWeights.get(Xl, Yl, cnX, cnY, p));
//					if (gray > 255) gray = 255;
//					
//					g.setColor(new Color(gray, gray, gray));
//					g.draw3DRect(
//							offsetX + boxMini * pX + 1, 
//							offsetY + boxMini * pY + 1, 
//							boxMini - 2, boxMini - 2, true);
				}
			}
		}
		return img;
	}
	
	private static int color(final float act) {
		int R = 0, G = 0, B = 0;
		if (act >= 0) {
			R = (int) (255 * act);
			if (R > 255) R = 255;
			
			B = 255;
		
		} else {
			B = (int) (255 * -act);
			if (B > 255) B = 255;
			if (B < 50) B = 50;
		}
		
		return Utils.rgb(255, R, G, B);
	}
	
	public static void drawNA(
			final BufferedImage image, final Mapping m, final int boxMini,
			final int offsetX, final int offsetY,
			final int cnX, final int cnY, final int cnZ,
			final int pX, final int pY
			) {
		
		//point of neuron activity in top left corner
		image.setRGB(
				offsetX + boxMini * pX + 2, 
				offsetY + boxMini * pY + 2,
				color(m.toZone().neurons.get(cnX, cnY, cnZ)));

		//point of post neuron activity in top left plus one pixel left corner
		image.setRGB(
				offsetX + boxMini * pX + 3, 
				offsetY + boxMini * pY + 2,
				color(m.toZone().axons.get(cnX, cnY, cnZ)));
	}

    public static BufferedImage drawRF(
			final boolean isPos,
			final BufferedImage image, final int boxSize,
			final int offsetX, final int offsetY,
			final int cnX, final int cnY, final int cnZ,
			final Mapping m) {

    	Matrix<Float> ws = m.senapseWeight().sub(cnX, cnY, cnZ);
    	
    	float maximum = Float.MIN_VALUE;
    	float minimum = Float.MAX_VALUE;
        for (int index = 0; index < ws.length(); index++) {
        	final float w = ws.getByIndex(index);
        	
        	if (maximum < w)
        		maximum = w;
        	
        	if (minimum > w)
        		minimum = w;
        }

    	int R = 0, G = 0, B = 0;

        int pX = 0, pY = 0;
        for (int l = 0; l < m.ns_links(); l++) {

            R = 0; G = 0; B = 0;

            final int xi = m._senapses().get(cnX, cnY, cnZ, l, 0);
        	final int yi = m._senapses().get(cnX, cnY, cnZ, l, 1);
        	//XXX: final int zi = m.senapses().get(cnX, cnY, cnZ, l, 2);
        	
        	if (m.toZone().isSingleReceptionField() || m.toZone().in_zones.length == 2) { //WORKAROUND: m.toZone().in_zones.length == 2 
	        	pX = (boxSize / 2) + (xi - (int)(m.toZoneCenterX() * m.fX()));
				pY = (boxSize / 2) + (yi - (int)(m.toZoneCenterY() * m.fY()));
        	} else {
        		pX = (boxSize / 2) + (xi - (int)(cnX * m.fX()));
        		pY = (boxSize / 2) + (yi - (int)(cnY * m.fY()));
        	}
                    	
			if (       pX > 0 
        			&& pX < boxSize 
        			&& pY > 0 
        			&& pY < boxSize) {

		        final float w = m.senapseWeight().get(cnX, cnY, cnZ, l);

	        	if (Float.isNaN(w) || Float.isInfinite(w)) {
					R = 255; G = 255; B = 255;

				} else if (w >= 0.0f) {
					R = (int) (255 * (w / maximum));

				} else {
					B = (int) (255 * (w / minimum));
				};
				
				image.setRGB(offsetX + pX, offsetY + pY, rgb(255, R, G, B));
        	}
        }
        return image;
	}
	
	public static BufferedImage drawRF(
			final BufferedImage image,
			final int cnX, final int cnY, 
			final Mapping m) {
		
		final LayerSimple sz = m.frZone();
		
		if (sz.neurons.get(cnX, cnY) > 0) {
			return image;
		}
				
        int pX = 0, pY = 0;
        for (int l = 0; l < m.ns_links(); l++) {
        	final int xi = m._senapses().get(cnX, cnY, l, 0);
        	final int yi = m._senapses().get(cnX, cnY, l, 1);
        	
        	pX = xi;
			pY = yi;
                    	
			if (       pX > 0 
        			&& pX < image.getWidth() 
        			&& pY > 0 
        			&& pY < image.getHeight()) {

				int c = calcGrey(image, pX, pY);
				c += 255 * sz.neurons.get(cnX, cnY) * m.senapseWeight().get(cnX, cnY, 0, l) * 1000;
				if (c > 255) c = 255;
				image.setRGB(pX, pY, rgb(255, c, c, c));
        	}
        }
        return image;
	}

	public static String debug(float[] array) {
		return debug(array, 7);
	}
	
	public static String debug(float[] array, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(array[i]).append(", ");
		}
		return sb.append(array[count]).toString();
	}

    public static String fileName(URL url) {
    	final String path = url.getPath();
    	return path.substring(path.lastIndexOf('/'), path.length()-1);
    }

	private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    public static void grabImages(String source, File target) {
    	XMLStreamReader reader = null;
    	InputStream is = null;
    	OutputStream os = null;
    	try {
    		URL url = new URL(source);
	        reader = FACTORY.createXMLStreamReader(url.openStream());
	        while (reader.hasNext()) {
	            if (reader.next() == START_ELEMENT) {
	                if (reader.getLocalName().equals("img")) {
	                    String src = reader.getAttributeValue(null, "src").toString();
	                    URL image = new URL(url, src);
	                    is = image.openConnection().getInputStream();
	                    os = new FileOutputStream(new File(target, fileName(image)));
	                    byte[] buff = new byte[1024 * 4];
	                    int len;
	                    while ((len = is.read(buff)) > 0) {
	                        os.write(buff, 0, len);
	                    }
	                    os.close();
	                    is.close();
	                    os = null;
	                    is = null;
	                }
	            }
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		if (reader != null) {
				try {
					reader.close();
				} catch (XMLStreamException e) {
				}
    		}
    		
    		if (os != null) {
    			try {
					os.close();
				} catch (IOException e) {
				}
    		}
    		if (is != null) {
    			try {
					is.close();
				} catch (IOException e) {
				}
    		}
    	}
    }
    
    public static void main(String[] args) {
		File folder = new File("images");
		folder.mkdirs();
		
		Utils.grabImages(
			"http://www.freefoto.com/gallery/homepage?count=2",
			folder
		);
	}

}
