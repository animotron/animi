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
    }
    
//    protected void processColors(float array[]) {
//    	cz.refreshImage();
//    }
	
	public void execute() {
		prepare();
		do {
//			System.out.println("Execute "+getClass());
			for (int x = 0; x < sz.width; x++) {
				for (int y = 0; y < sz.height; y++) {
					gpuMethod(x, y);
				}
			}
		} while (!isDone());
	}
	
	public void prepare() {}

	public abstract void gpuMethod(final int x, final int y);
	
	public boolean isDone() {
		return true;
	}

	protected abstract void release();
}
