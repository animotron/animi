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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.animotron.animi.Imageable;
import org.animotron.animi.RuntimeParam;
import org.animotron.animi.cortex.NeuronComplex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class RunningParamsFrame implements Imageable {
	
	NeuronComplex sn;
	List<Field> fds = new ArrayList<Field>();
	
	public RunningParamsFrame(NeuronComplex sn) {
		this.sn = sn;
		
		Field[] fields = sn.getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.isAnnotationPresent(RuntimeParam.class))
				fds.add(field);
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
        g.setColor(Color.WHITE);

		int textY = g.getFontMetrics(g.getFont()).getHeight();
		int x = 0, y = 0;
		
		for (Field f : fds) {
			y += textY;
	        g.drawString(getName(f), x, y);		

	        y += textY;
	        g.drawString(getValue(f), x, y);		
		}

		return image;
	}
	
	private String getName(Field f) {
		return f.getAnnotation(RuntimeParam.class).name();
	}

	private String getValue(Field f) {
		try {
			return f.get(sn).toString();
		} catch (Exception e) {
		}
		return "???";
	}

	@Override
	public Object whatAt(Point point) {
		return null;
	}

}
