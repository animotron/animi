/*
 *  Copyright (C) 2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
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
 
 /*
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */

__kernel void computeRetina(
    __global float* output,
    int outputSizeX,

    int safeZone,
    int offsetX, int offsetY,

    float XScale, float YScale,

    int onOff_radius,
    int onOff_regionSize,
    __global int* matrix,
    int type,
    
    float KContr1, float KContr2, float KContr3, int Level_Bright, int Lelel_min,

    __global int* input,
    int inputSizeX, int inputSizeY
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int pos = ((y) * outputSizeX) + x;
    
	//on-off transformation
	
    int rgb = 0;
	int R = 0;
	int G = 0;
	int B = 0;
	int gray = 0;

	int xi = 0;
	int yi = 0;
	
	int numInCenter = 0;
	int numInPeref = 0;
	
	float SC = 0.0f;
	float SP = 0.0f;
	
    for (int onOffX = 0; onOffX < onOff_regionSize; onOffX++)
    {
        for (int onOffY = 0; onOffY < onOff_regionSize; onOffY++)
        {
		    int t = matrix[(onOffY * onOff_regionSize) + onOffX];
		    
		    if (t == 0)
		    {
		    	continue;
	    	}
	        xi = x - onOff_radius + onOffX;
	        yi = y - onOff_radius + onOffY;
	        
		    rgb = input[(inputSizeX * (int)((yi + offsetY + safeZone) * YScale)) + (int)((xi + offsetX + safeZone) * XScale)];
    		R = (rgb >> 16) & 0xFF;
    		G = (rgb >> 8 ) & 0xFF;
    		B = (rgb      ) & 0xFF;
    
		    //calculate gray
    		gray = (R + G + B) / 3;
    
	        //periphery
	        if (t == 1)
	        {
	        	numInPeref++;
		        SP += gray;
	        }
	        //center
	        else if (t == 2)
	        {
	        	numInCenter++;
		        SC += gray;
	        }

        }
    }

	float SA = ((SP + SC) / (float)(numInCenter + numInPeref));

	float K_cont = KContr1 + SA * (KContr2 - KContr1) / (float)Level_Bright;

	if (K_cont < KContr3) K_cont = KContr3;

	SC = SC / numInCenter;
	SP = SP / numInPeref;

	output[pos] = 0;
	if (SA > Lelel_min)
	{
		if (type == 1)
		{
			if (SC / SP > K_cont)
			{
				output[pos] = 1;
			}
		}
		else if (type == 2)
		{
			if (SP / SC > K_cont)
			{
				output[pos] = 1;
			}
		}
		if (type == 3)
		{
			if (SC / SP > K_cont)
			{
				output[pos] = 1;
			}
			else if (SP / SC > K_cont)
			{
				output[pos] = 1;
			}
		}
	}

    //normalize
    //output[pos] = output[pos] / 255.0f;
}
