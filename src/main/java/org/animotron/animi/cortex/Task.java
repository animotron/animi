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
package org.animotron.animi.cortex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract base class for tasks that refer to a region of the Mandelbrot 
 * set. The processColors method of this class may be implemented to 
 * either fill the whole image with the preview, or to fill a small 
 * region of the image with a single tile.
 * 
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public abstract class Task {
	
	protected LayerSimple sz;
	protected LayerWLearning cz = null;
	
    /**
     * Creates a new Task that computes.
     * 
     * @param outputMem The target memory object
     */
    protected Task(LayerSimple sz) {
    	this.sz = sz;
    	if (sz instanceof LayerWLearning) {
    		this.cz = (LayerWLearning) sz;
		}
    	
    	init();
    }
    
//    protected void processColors(float array[]) {
//    	cz.refreshImage();
//    }
	
	public void execute() {
		if (!prepare())
			return;
		
		executor();
		
//		do {
//			for (int x = 0; x < sz.width; x++) {
//				for (int y = 0; y < sz.height; y++) {
//					for (int z = 0; z < sz.depth; z++) {
//						gpuMethod(x, y, z);
//					}
//				}
//			}
//		} while (!isDone());
	}
	
	public boolean prepare() {
		return true;
	}

	public abstract void gpuMethod(final int x, final int y, final int z);
	
	public boolean isDone() {
		return true;
	}

	protected abstract void release();
	
	int n_threads = Runtime.getRuntime().availableProcessors();
	ExecutorService executorService = Executors.newFixedThreadPool(n_threads * 2);
	
	List<Callable<Void>> todolist;
	
	private void init() {
		int n_tasks = n_threads;
		
		todolist = new ArrayList<Callable<Void>>();
		
		int tX = 0, tY = 0;
		for (int x = 0; x < sz.width; x += n_tasks) {
			for (int y = 0; y < sz.height; y += n_tasks) {
				
				if ((tX = x+n_tasks) > sz.width)  tX = sz.width;
				if ((tY = y+n_tasks) > sz.height) tY = sz.height;
					
				todolist.add(new Job<Void>(x, tX, y, tY));
			}
		}
	}
	
	public void executor() {
		try {
			do {
				executorService.invokeAll(todolist);
			} while (!isDone());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class Job<T> implements Callable<T> {
		
		int fX, tX, fY, tY;
		
		public Job(int fX, int tX, int fY, int tY) {
			this.fX = fX;
			this.tX = tX;
			this.fY = fY;
			this.tY = tY;
		}

		@Override
		public T call() throws Exception {

			for (int x = fX; x < tX; x++) {
				for (int y = fY; y < tY; y++) {
					for (int z = 0; z < sz.depth; z++) {
						gpuMethod(x, y, z);
					}
				}
			}

			return null;
		}
		
	}

}
