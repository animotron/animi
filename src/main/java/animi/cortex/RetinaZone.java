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
package animi.cortex;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import animi.matrix.Matrix;
import animi.matrix.MatrixFloat;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class RetinaZone extends Task {

	//constants
    //минимальное соотношение средней контрасности переферии и центра сенсорного поля, необходимое для активации
    //контрастность для темных элементов (0)
    public static float KContr1 = 1.45f;
    //контрастность для светлых элементов (Level_Bright)
    public static float KContr2 = 1.15f;
	//минимальная контрастность
    public static float KContr3 = 1.15f;
	//(0..255)
    public static int Level_Bright = 100;
    public static int Lelel_min = 10;// * N_Col;

    Retina retina;
    
    float XScale, YScale;
	
	Matrix<Float> history = null;
	Matrix<Float> avgs = null;
	
	int count = -1;
	
	int[] input;
	int inputSizeX, inputSizeY;
	
	OnOffMatrix matrix;

	public static int safeZone = (int)(0.1 * ((Retina.WIDTH + Retina.HEIGHT) / 2));

	protected RetinaZone(Retina retina, LayerSimple sz) {
		super(sz);
		
		this.retina = retina;
		
    	matrix = new OnOffMatrix();
	}
	
	public void setInput(final BufferedImage image) {
		
		if (history == null) {// || history.length != sz.cols.length) {
			history = new MatrixFloat(sz.neurons);
			history.fill(0f);
		}
		
		if (avgs == null) {
			avgs = new MatrixFloat(sz.neurons);
			avgs.fill(0f);
		}
		
		XScale = (image.getWidth()  - (retina.worldSafeZone() * 2)) / (float)sz.width;
		YScale = (image.getHeight() - (retina.worldSafeZone() * 2)) / (float)sz.height;

//		Graphics g = image.getGraphics();
//		g.drawRect(10, 10, image.getWidth() - 20, image.getHeight() - 20);

        DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] _data_ = dataBuffer.getData();
        int[] data = new int[_data_.length];
        System.arraycopy(_data_, 0, data, 0, _data_.length);

    	this.input = data;
    	
    	inputSizeX = image.getWidth();
    	inputSizeY = image.getHeight();
    	
    	count++;
	}

	@Override
	protected void release() {
	}

	private int input(int x, int y) {
		return input[(y * inputSizeX) + x];
	}

	private void output(final float value, final int x, final int y, final int z) {
    	sz.neurons.set(value, x, y, z);
    }
	
	private float gray(final int x, final int y) {
	    int rgb = input(x, y);

	    int R = (rgb >> 16) & 0xFF;
		int G = (rgb >> 8 ) & 0xFF;
		int B = (rgb      ) & 0xFF;

	    //calculate gray
		float gray = (R + G + B) / 3.0f;

		return gray;
	}
	
	private int onOff(
			final int type,
			final float SA, final float SC, final float SP,
			float K_cont) {
		if (SA > Lelel_min) {
			if (type == 1) {
				//On-centre
				if (SC / SP > K_cont) {
					return 1;
				}
			
			} else if (type == 2) {
				//Off-centre
				if (SP / SC > K_cont) {
					return 1;
				}
			
			} else if (type == 3) {
				if (SC / SP > K_cont || SP / SC > K_cont) {
					return 1;
				}
			}
		}
		return 0;
	}
	
	@Override
	public void gpuMethod(final int x, final int y, final int z) {
    	
    	int numInPeref = 0;
    	int numInCenter = 0;
    	int SP = 0;
    	int SC = 0;
    	
    	int xi, yi = 0;
    	
    	int t = 0;
    	for (int mX = 0; mX < matrix.regionSize(); mX++) {
        	for (int mY = 0; mY < matrix.regionSize(); mY++) {

        		t = matrix.getType(mX, mY);
        		//if nothing than do nothing
			    if (t == 0) continue;

		        xi = retina.worldSafeZone() + (int)(x * XScale) + mX - matrix.radius();
		        yi = retina.worldSafeZone() + (int)(y * YScale) + mY - matrix.radius();

		        //periphery
		        if (t == 1) {
		        	try {
				        SP += gray(xi, yi);
			        	numInPeref++;
		        	} catch (ArrayIndexOutOfBoundsException e) {
		        		//ignore if gets out
		        	}
		        }
		        //center
		        else if (t == 2) {
		        	try {
			        	numInCenter++;
				        SC += gray(xi, yi);
		        	} catch (ArrayIndexOutOfBoundsException e) {
		        		//ignore if gets out
		        	}
		        }
        	}
    	}
    	if (numInCenter == 0 || numInPeref == 0) {
			history.set(0f, x, y, z);
			output(0, x, y, z);
			return;
    	}
    	
		float SA = ((SP + SC) / (float)(numInCenter + numInPeref));
		
		float K_cont = KContr1 + SA * (KContr2 - KContr1) / (float)Level_Bright;
	
		if (K_cont < KContr3) K_cont = KContr3;
	
		SC = SC / numInCenter;
		SP = SP / numInPeref;
		
		int type = ((x + y) % 2) + 1;
	
		float value = Float.NaN;//0.2f;
		
		float onOffValue = onOff(type, SA, SC, SP, K_cont);
		
		int oppositeStimuli = onOff(type % 2 + 1, SA, SC, SP, K_cont);
		
		if (oppositeStimuli == 1) {
			value = -1f;
			
			//if no stimuli, check if opposite was 
		} else if (onOffValue == 0) {
			if (history.get(x, y, z) == 1f && oppositeStimuli == 0) {
				//ответ после противоположного стимула
//				value = 0.9f;
			}
		} else {
			//увидил свой образ
			value = 1f;
		}

		//as it is
		output(value, x, y, z);
		
		//устредненное значение
		//final float avg = avgs.get(x,y,z);
		//output(value - avg, x, y, z);
		//avgs.set( (avg * (float)count + value) / (float)(count + 1), x,y,z);
		
//		history.set((float)oppositeStimuli, x, y, z);
	}
}
