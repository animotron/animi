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
package org.animotron.animi.simulator;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.animotron.animi.Utils;
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

    public StimulatorImage(Application application) {
    	super(application);
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
		
		int width = 640;
		int height = 480;

//		final Retina retina = app.cortexs.retina;
//		int width = retina.width()+retina.safeZone*2;
//		if (width < loaded.getWidth(null))
//			width = loaded.getWidth(null);
//			
//		int height = retina.height()+retina.safeZone*2;
//		if (height < loaded.getHeight(null))
//			height = loaded.getHeight(null);
		

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        
        g.drawImage(loaded, 0, 0, width, height, 0, 0, width, height, null);

        return img;
	}

	private Image loadImage(File file) throws IOException {
		return ImageIO.read(file);
	}

	@Override
	public boolean isReset() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCode() {
		return -1;
	}
}
