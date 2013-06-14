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
package animi.simulator;

import java.awt.Dimension;
import java.awt.image.BufferedImage;


import animi.gui.Application;

import com.github.sarxos.webcam.*;
//import com.github.sarxos.webcam.ds.openimaj.OpenImajDriver;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class StimulatorWebcam extends AbstractStimulator implements WebcamListener {

	private Webcam webcam = null;

    public StimulatorWebcam(Application application) {
    	super(application);
    	
//        Webcam.setDriver(new OpenImajDriver());
        webcam = Webcam.getDefault();
        if (webcam == null) {
            System.out.println("No webcams found...");
            System.exit(1);
        }
//        webcam.setViewSize(new Dimension(Retina.WIDTH, Retina.HEIGHT));
        webcam.setViewSize(new Dimension(640, 480));
        webcam.addWebcamListener(this);

        if (!webcam.isOpen()) {
            webcam.open();
        }
	}
    
    public void init() {
    }

	@Override
	public void webcamOpen(WebcamEvent we) {
    	//prepare first image
    	getNextImage();
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
	}

	public BufferedImage getNextImage() {
        img = webcam.getImage();
		
		return img;
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
