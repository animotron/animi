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

import org.animotron.animi.simulator.Stimulator;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
//TODO: macula lutea
public class Retina {

	//Сетчатка
//  public static final int WIDTH = 192;
//	public static final int HEIGHT = 144;
//	public static final int WIDTH = 160;
//	public static final int HEIGHT = 120;
	public static final int WIDTH = 30;
	public static final int HEIGHT = 30;
	
	/* x */
	private int width;
	/* y */
	private int height;
	
	RetinaZone retinaTask = null;
	
	public Retina() {
		this(WIDTH, HEIGHT);
	}

	public Retina(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	private CortexZoneSimple NL = null;
	public void setNextLayer(CortexZoneSimple sz) {
		NL = sz;
		
		width = sz.width();
		height = sz.height();

		initialize();
	}
	
    // создание связей сенсорных полей
	private void initialize() {
        retinaTask = new RetinaZone(this, (CortexZoneSimple)NL);
		
//		sensorField = new OnOffReceptiveField[NL.width()][NL.height()];
//
//        double XScale = (width - onOff.regionSize()) / (double)NL.width();
//        double YScale = (height - onOff.regionSize()) / (double)NL.height();
//
//        int X, Y;
//        int NC, NP;
//
//        int fl = 3;
//        
//        for (int ix = 0; ix < NL.width(); ix++) {
////        	fl++; if (fl == 3) fl = 1;
//        	for (int iy = 0; iy < NL.height(); iy++) {
//
//        		OnOffReceptiveField mSensPol = new OnOffReceptiveField();
//                sensorField[ix][iy] = mSensPol;
//                mSensPol.center = new int[onOff.numInCenter()][2];
//        		mSensPol.periphery = new int[onOff.numInPeref()][2];
//
//                // распределение он и офф полей
//            	mSensPol.type = fl;
//
//                X = (int)Math.round( ix * XScale + onOff.radius() + 1 );
//                Y = (int)Math.round( iy * YScale + onOff.radius() + 1 );
//
//                NC = 0;
//                NP = 0;
//
//                for (int i = 0; i < onOff.regionSize(); i++) {
//                    for (int j = 0; j < onOff.regionSize(); j++) {
//
//                    	switch (onOff.getType(i, j)) {
//
//                        case 0:
//                            break;
//                        case 1:
//
//                            mSensPol.periphery[NP][0] = X - onOff.radius() + i;
//                            mSensPol.periphery[NP][1] = Y - onOff.radius() + j;
//
//                            NP = NP + 1;
//                            break;
//                        case 2:
//
//                        	mSensPol.center[NC][0] = X - onOff.radius() + i;
//                        	mSensPol.center[NC][1] = Y - onOff.radius() + j;
//
//                            NC = NC + 1;
//                            break;
//						default:
//							break;
//                		}
//                    }
//                }
//        	}
//        }
    }
	
    BufferedImage image = null;
	
	public void process(Stimulator stimulator) {
		
        retinaTask.setInput(image = stimulator.getNextImage());

    	try {
            NL.mc.addTask(retinaTask);
        
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    	NL.mc.finish();
        NL.refreshImage();
    }
    
	public int worldSafeZone() {
		return worldStep() * OnOffMatrix.radius;
	}

	public int worldStep() {
		return (OnOffMatrix.centeRadius * 2) - 1;
	}

	public int worldWidth() {
		return (width  * worldStep()) + (worldSafeZone() * 2);
	}
	
	public int worldHeight() {
		return (height * worldStep()) + (worldSafeZone() * 2);
	}

	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}

    public BufferedImage getImage() {
        return image;
    }
    
//    public class RetinaTask extends Task {
//    	
//    	float XScale, YScale;
//    	
//    	int input[];
//    	int inputSizeX, inputSizeY;
//    	
//    	private int input(int x, int y) {
//    		return input[(y * inputSizeX) + x];
//    	}
//    	
//    	protected cl_mem inputMem;
//
//		protected RetinaTask(CortexZoneSimple sz, 
//				float XScale, float YScale, 
//				int input[], 
//				int inputSizeX, int inputSizeY) {
//			
//			super(sz);
//			
//	    	this.XScale = XScale;
//	    	this.YScale = YScale;
//	    	
//	    	this.input = input;
//	    	this.inputSizeX = inputSizeX;
//	    	this.inputSizeY = inputSizeY;
//		}
//
//	    protected void setupArguments(cl_kernel kernel) {
//	    	super.setupArguments(kernel);
//
//	        clSetKernelArg(kernel,  2, Sizeof.cl_int, Pointer.to(new int[] {safeZone}));
//	        clSetKernelArg(kernel,  3, Sizeof.cl_float, Pointer.to(new float[] {XScale}));
//	        clSetKernelArg(kernel,  4, Sizeof.cl_float, Pointer.to(new float[] {YScale}));
//
//	        clSetKernelArg(kernel,  5, Sizeof.cl_int, Pointer.to(new int[] {onOff.radius}));
//	        clSetKernelArg(kernel,  6, Sizeof.cl_int, Pointer.to(new int[] {onOff.regionSize}));
//	        clSetKernelArg(kernel,  7, Sizeof.cl_mem, Pointer.to(onOff.cl_matrix));
//	        clSetKernelArg(kernel,  8, Sizeof.cl_int, Pointer.to(new int[] {1}));
//	        
//	        clSetKernelArg(kernel,  9, Sizeof.cl_float, Pointer.to(new float[] {KContr1}));
//	        clSetKernelArg(kernel,  10, Sizeof.cl_float, Pointer.to(new float[] {KContr2}));
//	        clSetKernelArg(kernel,  11, Sizeof.cl_float, Pointer.to(new float[] {KContr3}));
//	        clSetKernelArg(kernel,  12, Sizeof.cl_int, Pointer.to(new int[] {Level_Bright}));
//	        clSetKernelArg(kernel,  13, Sizeof.cl_int, Pointer.to(new int[] {Lelel_min}));
//
//	        inputMem = clCreateBuffer(
//	    		sz.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
//	    		input.length * Sizeof.cl_int, Pointer.to(input), null
//			);
//	        clSetKernelArg(kernel,  14, Sizeof.cl_mem, Pointer.to(inputMem));
//	        clSetKernelArg(kernel,  15, Sizeof.cl_int, Pointer.to(new int[] {inputSizeX}));
//	        clSetKernelArg(kernel,  16, Sizeof.cl_int, Pointer.to(new int[] {inputSizeY}));
//	    }
//
//	    @Override
//		protected void release() {
//	    	clReleaseMemObject(inputMem);
//		}
//	    
//	    private void output(float value, int x, int y) {
//	    	sz.cols(value, x, y);
//	    }
//
//		@Override
//		public void gpuMethod(int x, int y) {
//		    int rgb = input(
//		    		(int)((x + safeZone) * XScale),
//		    		(int)((y + safeZone) * YScale)
//	    		);
//
//		    int R = (rgb >> 16) & 0xFF;
//			int G = (rgb >> 8 ) & 0xFF;
//			int B = (rgb      ) & 0xFF;
//
//		    //calculate gray
//			float gray = (R + G + B) / 3.0f;
//
//			if (gray > 0.0f) {
//				output(1, x, y);
//			
//			} else {
//				output(0, x, y);
//			
//			}
//		}
//    }
}
