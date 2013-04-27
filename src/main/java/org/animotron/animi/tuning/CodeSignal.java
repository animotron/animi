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

import java.awt.image.BufferedImage;
import java.util.Random;

import org.animotron.animi.Utils;
import org.animotron.animi.cortex.IRetina;
import org.animotron.animi.cortex.LayerSimple;
import org.animotron.animi.cortex.LayerWLearning;
import org.animotron.animi.gui.Application;
import org.animotron.animi.simulator.AbstractStimulator;
import org.animotron.animi.simulator.Stimulator;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CodeSignal extends AbstractStimulator implements IRetina {
	
	Integer signal;
	Random rnd = new Random();
	
	public CodeSignal(Application application) {
		super(application);
	}

	@Override
	public String getImageName() {
		return "Code signal";
	}

	@Override
	public void init() {
		img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	}

	@Override
	public void reset() {
	}

	@Override
	public void refreshImage() {
	}

	@Override
	public boolean isReset() {
		return true;
	}

	@Override
	public BufferedImage getNextImage() {
		img.setRGB(0, 0, Utils.rgb(255, signal = rnd.nextInt(Codes.CODES), rnd.nextInt(5), 0));

		return img;
	}
	
	LayerSimple nextLayer = null;
	LayerWLearning resetLayer = null;

	@Override
	public void setNextLayer(LayerSimple layer) {
		nextLayer = layer;
	}

	@Override
	public void setResetLayer(LayerWLearning layer) {
		resetLayer = layer;
	}

	@Override
	public void process(Stimulator stimulator) {
		getNextImage();

		nextLayer.neurons.set((float)signal, 0, 0, 0);
	}

	@Override
	public int worldWidth() {
		return 1;
	}

	@Override
	public int worldHeight() {
		return 1;
	}

	@Override
	public int worldStep() {
		return 1;
	}

	@Override
	public int width() {
		return 1;
	}

	@Override
	public int height() {
		return 1;
	}

	@Override
	public int getCode() {
		return signal;
	}
}
