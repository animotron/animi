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
package animi.gui;

import animi.Params;
import animi.RuntimeParam;
import animi.cortex.IRetina;
import animi.cortex.LayerSimple;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public abstract class Controller implements Runnable {

    public static final int STOP = 0;
    public static final int PAUSE = 1;
    public static final int STEP = 2;
    public static final int RUN = 3;

    public static int MODE = STOP;

    public final Application app;
    
    @RuntimeParam(name = "frequency")
	public int frequency = 0; // Hz

    private long frame = 0;
    private long t0 = System.currentTimeMillis();

    private boolean run = true;
    
    public long count = 0;
    
    @Params
    public LayerSimple [] zones;

    public Controller(Application app) {
    	this.app = app;
	}
    
    public void init() {
		for (LayerSimple zone : zones) {
			zone.init();
		}
    }
    
    public abstract void setRetina(IRetina retina);
	public abstract IRetina getRetina();
    
	@Override
	public void run() {
        while (run) {
			try {
				if (paused) {
					synchronized (this) {
						this.wait();
					}
				}
                long t = System.currentTimeMillis();
				
                processing();
                
                if (frequency != 0) {
                    t = (1000 / frequency) - (System.currentTimeMillis() - t);

                    if (t > 0)
                    	Thread.sleep(t);
                    else
                    	//give some rest any way
                    	Thread.sleep(5);

                } else {
                	//give some rest any way
                	Thread.sleep(5);
                }

			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
                frame++;
                long t = System.currentTimeMillis();
                long dt = t - t0;
                if (dt > 1000) {
                    app.fps = 1000 * frame / dt;
                    frame = 0;
                    t0 = t;
                }
            }
		}
	}

	protected void processing() {
        if (MODE >= STEP) {

        	process();
    		
        	count++;
    		
    		if (MODE == STEP) {
    			MODE = STOP;
        		app.count.setText(String.valueOf(count));
        		app.refresh();
    		
    		} else if (MODE == RUN && count % 10 == 0) {
        		app.count.setText(String.valueOf(count));
    			app.refresh();

    		}
        }
	}

	public abstract void process();

	private Thread th = null;
	private volatile boolean paused = false;
	
	public synchronized void start() {
		if (th != null) {
			th.interrupt();
		}
		
		MODE = RUN;

		run = true;
		paused = false;
		
		th = new Thread(this);
		th.setDaemon(true);
		th.start();
	}

	public synchronized void stop() {
		MODE = (MODE == RUN) ? PAUSE : STOP;

		run = false;
		try {
			if (th != null) {
				th.join();
			}
		} catch (InterruptedException e) {
			th.interrupt();
		}
		paused = true;
		th = null;
	}

	public void resume() {
		paused = false;
		synchronized (this) {
			notifyAll();
		}
	}
}
