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
package org.animotron.animi.acts;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

import org.animotron.animi.Imageable;
import org.animotron.animi.Utils;
import org.animotron.animi.cortex.*;
import org.animotron.matrix.MatrixFloat;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class FormLearningMatrix extends Task implements Imageable {
	
	BufferedImage img;

	public FormLearningMatrix(LayerWLearning cz) {
		super(cz);
		
		img = new BufferedImage(cz.width, cz.height, BufferedImage.TYPE_INT_RGB);
	}
	
//	Matrix<Float> matrix;
//	int stage = 0;
	Mapping m;
	
	@Override
	public boolean prepare() {
//		stage = 0;
//		
//		matrix = cz.learning.copy();

		m = cz.in_zones[0];

		cz.pure.fill(0f);
		cz.learning.fill(0f);
		cz.toLearning.fill(0f);
		
		return true;
	}
	
	int half = 1;

	public void gpuMethod(final int x, final int y, final int z) {
		if (z != 0)
			return;
		
//		switch (stage) {
//		case 0:
//			final float delta = matrix.get(x,y,z) / (float)(half * 2 * 3);
//
//			int fx = x - half; if (fx < 0) fx = 0;
//			int tx = x + half; if (tx >= cz.width) tx = cz.width - 1;
//			
//			int fy = y - half; if (fy < 0) fy = 0;
//			int ty = y + half; if (ty >= cz.height) ty = cz.height - 1;
//
//			int fz = z - half; if (fz < 0) fz = 0;
//			int tz = z + half; if (tz >= cz.depth) tz = cz.depth - 1;
//
//			for (int xx = fx; xx <= tx; xx++) {
//				for (int yy = fy; yy <= ty; yy++) {
//					for (int zz = fz; zz <= tz; zz++) {
//						if (xx == x && yy == y && zz == z)
//							continue;
//						
//						cz.learning.set(delta, xx,yy,zz);
//					}
//				}
//			}
//			
//			break;
//
//		case 1:
			
		float act = 0;
		for (int zz = 0; zz < cz.depth; zz++) {
			if (act < cz.neurons.get(x,y,zz)) {
				act = cz.neurons.get(x,y,zz);
			}
		}
		for (int zz = 0; zz < cz.depth; zz++) {
			if (act < cz.axons().get(x,y,zz)) {
				act = cz.axons().get(x,y,zz);
			}
		}
//		act = m.senapsesCode().get(x,y,z) >= 0 ? act : .4f + (rnd.nextFloat() * .2f);
//		act = act > 0 ? act : .4f + (rnd.nextFloat() * .2f);
			
		for (int xx = 0; xx < cz.width; xx++) {
			for (int yy = 0; yy < cz.height; yy++) {
//				for (int zz = 0; zz < cz.depth; zz++) {
				
					final int dx = xx - x;
					final int dy = yy - y;
//					final int dz = zz - z;
	
					final double length = .2 / ((dx*dx + dy*dy) + 1.0);
//					final double length = .2 / (Math.sqrt(dx*dx + dy*dy + dz*dz) + 1.0);
					
					float factor = cz.learning.get(xx,yy,0) + (float)(length * act);
					
					cz.pure.set(factor, xx,yy);

					for (int zz = 0; zz < cz.depth; zz++) {
//						float f = m.senapsesCode().get(x,y,z) >= 0 ? factor : factor + .1f + (rnd.nextFloat() * .2f);
//						act = act > 0 ? act : .4f + (rnd.nextFloat() * .2f);

						cz.learning.set(factor, xx,yy,zz);
//					}
				}
			}
		}
//			break;
//		}
	}
	
	Random rnd = new Random();
	
	public boolean isDone() {
//		stage++;
//		
//		if (stage < 2)
//			return false;
		
//		cz.learning.debug("before");
		
		if (cz.app.contr.count <= 1) {
			cz.learning.set(10f, (int)(cz.width / 2.0), (int)(cz.height / 2.0), 0);
		}

		//additional factor for neurons without memory
		for (int xx = 0; xx < cz.width; xx++) {
			for (int yy = 0; yy < cz.height; yy++) {
				
				final float factor = cz.learning.get(xx,yy,0);
				
				for (int zz = 0; zz < cz.depth; zz++) {
					final float w = 
							m.senapsesCode().get(xx,yy,zz) >= 0 ? 0f : 1f + (rnd.nextFloat() * .1f);

					cz.learning.set(factor + w, xx,yy,zz);
				}
			}
		}

//		cz.learning.debug("after");

		return true;
	}

	@Override
    protected void release() {
    }

	@Override
	public String getImageName() {
		return "Learning matrix";
	}

	@Override
	public void refreshImage() {
	}
	
	@Override
	public BufferedImage getImage() {
		final MatrixFloat matrix = cz.learning;//pure;
		
		final float maximum = matrix.maximum();
		
    	DataBufferInt dataBuffer = (DataBufferInt)img.getRaster().getDataBuffer();
    	int data[] = dataBuffer.getData();

//    	cz.learning.debug("learning");
    	
    	int R = 0, G = 0, B = 0;
    	for (int i = 0; i < data.length; i++) {
    		float act = matrix.getByIndex(i) / maximum;
    		if (act >= 0.5f) {
    			R = (int) (254 * (act - 0.5f) / 0.5);
    			B = (int) (254 * (1 - act));
    		} else {
    			R = 0;//(int) (254 * (1 - act));
    			B = (int) (127 * act / 0.5);
    		}
//			R = G = B = (int)(254 * act);
//			if (R > 255) {
//				R = G = B = 255;
//			}

    		data[i] = Utils.rgb(255, R, G, B);
    	}
		
		return img;
	}

	@Override
	public Object whatAt(Point point) {
		return null;
	}

	@Override
	public void focusGained(Point point) {}

	@Override
	public void focusLost(Point point) {}

	@Override
	public void closed(Point point) {}
}