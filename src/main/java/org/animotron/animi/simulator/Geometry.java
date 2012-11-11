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
package org.animotron.animi.simulator;

import java.awt.Color;
import java.awt.Graphics;

import org.animotron.animi.RuntimeParam;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class Geometry implements Figure {
	
    @RuntimeParam(name = "active")
    public boolean active = true;

    public void drawImage(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawRect(100, 100, 100, 100);
		g.fillRect(250, 100, 100, 100);

		g.drawLine(400, 100, 450, 200);
		
		g.drawOval(100, 250, 100, 100);
		g.fillOval(250, 250, 100, 100);
	}
	
	public void step() {
	}

	@Override
	public boolean isActive() {
		return active;
	}
}