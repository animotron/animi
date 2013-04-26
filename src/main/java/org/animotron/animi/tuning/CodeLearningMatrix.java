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
package org.animotron.animi.tuning;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.animotron.animi.Imageable;
import org.animotron.animi.Utils;
import org.animotron.animi.cortex.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CodeLearningMatrix extends Task implements Imageable {
	
	BufferedImage img;

	public CodeLearningMatrix(LayerWLearning cz) {
		super(cz);
		
		img = new BufferedImage(cz.width, cz.height, BufferedImage.TYPE_INT_RGB);
	}
	
	@Override
	public void prepare() {
		cz.learning.fill(0f);
		cz.toLearning.fill(0f);
	}

	public void gpuMethod(final int x, final int y, final int z) {
//		if (x == (int)(cz.width / 2.0) && y == (int)(cz.height / 2.0)) {
		
		final float act = cz.neurons.get(x,y,z);
		if (act <= 0f)
			return;
		
		Mapping m = cz.in_zones[0];
		
		for (int xx = 0; xx < cz.width; xx++) {
			for (int yy = 0; yy < cz.height; yy++) {
				for (int zz = 0; zz < cz.depth; zz++) {
				
					final int dx = xx - x;
					final int dy = yy - y;
					final int dz = zz - z;
	
					final double l = .2 / (Math.sqrt(dx*dx + dy*dy + dz*dz) + 1.0);
					
					float factor = cz.learning.get(xx,yy,zz) + (float)(l > 0f ? l * act : 0f);
					
					cz.learning.set(factor, xx,yy,zz);
				}
			}
		}
//		}
	}
	
	public boolean isDone() {
		Mapping m = cz.in_zones[0];
		
		for (int xx = 0; xx < cz.width; xx++) {
			for (int yy = 0; yy < cz.height; yy++) {
				for (int zz = 0; zz < cz.depth; zz++) {
					final float w = m.senapseWeight().get(xx, yy, zz, 0);
					
					float factor = cz.learning.get(xx,yy,zz);
					factor += w >= 0 ? 0f : 1f;
					
					cz.learning.set(factor, xx,yy,zz);
				}
			}
		}
		
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
		final float maximum = cz.learning.maximum();
		
    	DataBufferInt dataBuffer = (DataBufferInt)img.getRaster().getDataBuffer();
    	int data[] = dataBuffer.getData();

//    	cz.learning.debug("learning");
    	
    	int R = 0, G = 0, B = 0;
    	for (int i = 0; i < data.length; i++) {
    		float act = cz.learning.getByIndex(i) / maximum;
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