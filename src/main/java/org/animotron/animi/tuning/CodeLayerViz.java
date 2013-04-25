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
import org.animotron.animi.cortex.LayerWLearning;
import org.animotron.animi.cortex.Mapping;
import org.animotron.matrix.Matrix;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class CodeLayerViz implements Imageable {
	
	LayerWLearning layer;
	
	BufferedImage img;
	
	public CodeLayerViz(LayerWLearning layer) {
		this.layer = layer;
		
		img = new BufferedImage(layer.width, layer.height, BufferedImage.TYPE_INT_RGB);
	}

	@Override
	public String getImageName() {
		return "Градиент "+layer.getImageName();
	}

	@Override
	public void refreshImage() {
	}

	@Override
	public BufferedImage getImage() {
    	DataBufferInt dataBuffer = (DataBufferInt)img.getRaster().getDataBuffer();
    	int data[] = dataBuffer.getData();

		Matrix<Float> ws = layer.in_zones[0].senapseWeight();
//		ws.debug("ws");
    	for (int i = 0; i < data.length; i++) {
    		if (ws.getByIndex(i) >= 0f) {
    			data[i] = Codes.GRADIENT_RAINBOW[Math.round( ws.getByIndex(i) )].getRGB();
    		} else {
    			System.out.println("");
    		}
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