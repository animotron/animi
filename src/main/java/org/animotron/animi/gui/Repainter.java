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

import javax.swing.JComponent;

import org.animotron.animi.Imageable;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class Repainter implements Runnable {

	private double frequency;

	private JComponent component;
	private Thread th;

	public Repainter(JComponent comp, Imageable imageable) {
		component = comp;
		frequency = imageable.frequency();
		
		th = new Thread(this);
		th.setDaemon(true);
	}

	@Override
	public void run() {
		//super.run();
        while (running) {
			try {
				if (paused) {
					synchronized (this) {
						this.wait();
					}
				}
				prepareImage();
				component.repaint();
                
				if (frequency != 0)
					Thread.sleep((int)(1000 / frequency));
				
			} catch (Throwable e) {
            }
		}
	}

	protected abstract void prepareImage();

	private volatile boolean running = false;
	private volatile boolean paused = false;

	/**
	 * Start rendering thread.
	 */
	public void start() {
		running = true;
		paused = false;
		th.start();
	}

	/**
	 * Pause rendering thread.
	 */
	public void pause() {
		if (paused) {
			return;
		}
		paused = true;
	}

	/**
	 * Resume rendering thread.
	 */
	public void resume() {
		if (!paused) {
			return;
		}
		synchronized (this) {
			this.notifyAll();
		}
		paused = false;
	}
	
	public void stop() {
		running = false;
		try {
			th.join(1000);
		} catch (InterruptedException e) {
		}
	}
}
