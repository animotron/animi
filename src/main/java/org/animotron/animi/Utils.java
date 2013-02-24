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
package org.animotron.animi;

import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;
import org.animotron.animi.cortex.CortexZoneSimple;
import org.animotron.animi.cortex.Mapping;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_event;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import static javolution.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.jocl.CL.*;

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

    public static int create_rgb(int alpha, int r, int g, int b) {
        int rgb = (alpha << 24) + (r << 16) + (g << 8) + b;
        return rgb;
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
        return rgb & 0xFF;
    }
    
	public static BufferedImage drawRF(
			final BufferedImage image, final int boxSize,
			final int offsetX, final int offsetY,
			final int cnX, final int cnY,
			final int pN,
			final Mapping m) {

		int pX = 0, pY = 0;
        for (int l = 0; l < m.ns_links; l++) {
        	final int xi = m.linksSenapse(cnX, cnY, l, 0);
        	final int yi = m.linksSenapse(cnX, cnY, l, 1);
        	
        	pX = (boxSize / 2) + (xi - (int)(cnX * m.fX));
			pY = (boxSize / 2) + (yi - (int)(cnY * m.fY));
                    	
			if (       pX > 0 
        			&& pX < boxSize 
        			&& pY > 0 
        			&& pY < boxSize) {

		        int value = image.getRGB(offsetX + pX, offsetY + pY);

		        int G = Utils.get_green(value);
		        int B = Utils.get_blue(value);
		        int R = Utils.get_red(value);


		        final float w = m.linksWeight(cnX, cnY, 0, l);
				if (Float.isNaN(w) || Float.isInfinite(w)) {
					R = 255;
				
				} else if (w == 0.0f) {
					B = G = R = 0;

				} else if (w > 0.0f) {
					int v = (int) (255 * w * 5);
					if (v > 255) v = 255;
					B = v; G = v; R = v;
				} else {
					B = 255;
					//B += 255 * m.linksWeight[lOffset + l] * 5;
					if (B > 255) G = 255;
				};
				image.setRGB(offsetX + pX, offsetY + pY, create_rgb(255, R, G, B));
        	}
        }
        return image;
	}
	
	public static BufferedImage drawRF(
			final BufferedImage image,
			final int cnX, final int cnY, 
			final Mapping m) {
		
		final CortexZoneSimple sz = m.frZone;
		
        int pX = 0, pY = 0;
        for (int l = 0; l < m.ns_links; l++) {
        	final int xi = m.linksSenapse(cnX, cnY, l, 0);
        	final int yi = m.linksSenapse(cnX, cnY, l, 1);
        	
        	pX = xi;
			pY = yi;
                    	
			if (       pX > 0 
        			&& pX < image.getWidth() 
        			&& pY > 0 
        			&& pY < image.getHeight()) {

				int c = calcGrey(image, pX, pY);
				c += 255 * sz.cols(cnX, cnY) * m.linksWeight(cnX, cnY, 0, l);
				if (c > 255) c = 255;
				image.setRGB(pX, pY, create_rgb(255, c, c, c));
        	}
        }
        return image;
	}

	/*
     * Print "benchmarking" information 
     */
    public static void printBenchmarkInfo(String description, cl_event event) {
        StringBuilder sb = new StringBuilder();
        sb
        	.append(description)
        	.append(" ")
        	.append(computeExecutionTimeMs(event))
        	.append(" ms");
        
        System.out.println(sb.toString());
    }
    
    /*
     * Compute the execution time for the given event, in milliseconds
     */
    private static double computeExecutionTimeMs(cl_event event) {
        long startTime[] = new long[1];
        long endTime[] = new long[1];
        clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_END,   
            Sizeof.cl_ulong, Pointer.to(endTime), null);
        clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_START, 
            Sizeof.cl_ulong, Pointer.to(startTime), null);
        return (endTime[0]-startTime[0]) / 1e6;
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
