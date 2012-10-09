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
import java.lang.reflect.Field;

import org.animotron.animi.Imageable;
import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.NeuronComplex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class RunningParamsFrame implements Imageable {
	
	NeuronComplex sn;
	
	public RunningParamsFrame(NeuronComplex sn) {
		this.sn = sn;
		
		Field[] fields = sn.getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
			RuntimeParam ann = fields[i].getAnnotation(RuntimeParam.class);
			if (ann != null)
				System.out.println(ann.name());
		}
	}

	@Override
	public String getImageName() {
		return "running params";
	}

	@Override
	public BufferedImage getImage() {
		BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

		int textY = g.getFontMetrics(g.getFont()).getHeight();

        g.setColor(Color.WHITE);

        g.drawString("!!! fps; !!! cycles;", 0, textY);		
        
        return image;
	}

}
