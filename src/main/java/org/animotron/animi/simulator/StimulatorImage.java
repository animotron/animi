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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.animotron.animi.Utils;
import org.animotron.animi.cortex.MultiCortex;
import org.animotron.animi.gui.Application;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class StimulatorImage extends AbstractStimulator {
	
	File folder = null;
	File[] images;
	int current = 0;

    public StimulatorImage(Application application, MultiCortex cortexs) {
    	super(application, cortexs);
	}

	public void init() {
		if (folder == null) {
			folder = new File("images");
		}
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		images = folder.listFiles();
		if (images.length == 0) {
			Utils.grabImages("http://www.freefoto.com/gallery/homepage?count=10", folder);
			images = folder.listFiles();
		}
		
		//prepare first image
		getNextImage();
	}
    
	@Override
	public BufferedImage getNextImage() {
		Image loaded;
		try {
			if (images.length <= current)
				current = 0;
			
			loaded = loadImage(images[current]);
			
		} catch (Exception e) {
			e.printStackTrace();
			return img;
		} finally {
			current++;
		}

        img = new BufferedImage(mc.retina.width(), mc.retina.height(), BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        
        g.drawImage(loaded, 0, 0, img.getWidth(), img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);

        return img;
	}

	private Image loadImage(File file) throws IOException {
		return ImageIO.read(file);
	}
}
