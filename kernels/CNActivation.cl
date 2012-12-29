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

__kernel void computeActivation(
    __global float* output,
    int outputSizeX,

    __global int* tremor,
    int numberOfSteps,

    __global float* package,
    __global int* packageFree,
    int numberOfPackages,

    __global float* linksWeight,
    __global int*   linksSenapse,
    int linksNumber,

    __global float* input,
    int inputSizeX,
    
    float K_POROG_PAKET_UZNAVANIYA,
    float K_POROG_ZNACH_OBRAZA,
    float K_SOOTN_POS_I_NEGATIVE
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int pos = (y * outputSizeX) + x;
    
    int packagePos = 0;
    
    int offset = ((y * outputSizeX) + x) * linksNumber * 2;

	int wOffset = 0;

    float maximum = 0.0f;
    float sum = 0.0f;
    
    int empty = 0;
	bool toRemember = true;
	int rememberOn = 0;
    
    for (int p = 0; p < numberOfPackages; p++)
    {
		packagePos = (((y * outputSizeX) + x) * numberOfPackages) + p;
	    package[packagePos] = 0.0f;

    	wOffset = ((((y * outputSizeX) + x) * numberOfPackages) + p) * linksNumber;
	    for(int l = 0; l < linksNumber; l++)
	    {
	    	if (packageFree[packagePos] == 0)
	    	{
	        	linksWeight[wOffset + l] = 0;
	    	}
	    	else
	    	{
	        	linksWeight[wOffset + l] = linksWeight[wOffset + l];
        	}
	    }
    }
    
    for (int step = 0; step < numberOfSteps; step++)
    {
		int shiftX = tremor[step*2 +0];
		int shiftY = tremor[step*2 +1];
		
		toRemember = true;
		
	    for (int p = 0; p < numberOfPackages; p++)
	    {
	    	wOffset = ((((y * outputSizeX) + x) * numberOfPackages) + p) * linksNumber;

			packagePos = (((y * outputSizeX) + x) * numberOfPackages) + p;
			empty = packageFree[packagePos];
	    		
			sum = 0.0f;
		    for(int l = 0; l < linksNumber; l++)
		    {
		    	int xi = linksSenapse[offset + (l * 2) +0] + shiftX;
		    	int yi = linksSenapse[offset + (l * 2) +1] + shiftY;
		        
		        if (empty == 0)
		        	sum += input[(yi * inputSizeX) + xi] / (float)linksNumber;
	        	else
		        	sum += input[(yi * inputSizeX) + xi] * linksWeight[wOffset + l];
		    }
		    if (
//		    	(empty == 0 && sum < K_POROG_ZNACH_OBRAZA) 
//		    	||
		    	(empty == 0 && sum < K_POROG_ZNACH_OBRAZA))
	    	{
	    		continue;
	    	}
		    
		    //should not be more then one
//	    	if (sum > 1.0f)
//	    	{
//	    		sum = 1.0f;
//			}

		    if (empty == 0)
	    	{
	    		if (toRemember)
	    		{
	    			//remember only if another do not recognize
				    for (int pi = 0; pi < numberOfPackages; pi++)
				    {
				    	int _wOffset_ = ((((y * outputSizeX) + x) * numberOfPackages) + pi) * linksNumber;
			
						float sumi = 0.0f;
					    for(int l = 0; l < linksNumber; l++)
					    {
					    	int xi = linksSenapse[offset + (l * 2) +0] + shiftX;
					    	int yi = linksSenapse[offset + (l * 2) +1] + shiftY;
					        
				        	sumi += input[(yi * inputSizeX) + xi] * linksWeight[_wOffset_ + l];
					    }
					    
					    if (sumi >= K_POROG_PAKET_UZNAVANIYA)
					    {
					    	toRemember = false;
					    	break;
					    }
				    }
			    }
				if (toRemember && p >= rememberOn)
				{
					toRemember = false;
					rememberOn = p+1;
					
				   	wOffset = ((((y * outputSizeX) + x) * numberOfPackages) + p) * linksNumber;

			    	int count = 0;
			    	float sumW = 0;
			    	float w = 0;
			    	
				    for(int l = 0; l < linksNumber; l++)
				    {
				    	int xi = linksSenapse[offset + (l * 2) +0] + shiftX;
				    	int yi = linksSenapse[offset + (l * 2) +1] + shiftY;
				        
				        w = input[(yi * inputSizeX) + xi];
				        linksWeight[wOffset + l] = w;
						sumW += w;

						if (w > 0.0f)
						{
							count++;
						}
				    }
				    
				    for(int l = 0; l < linksNumber; l++)
				    {
				    	if (linksWeight[wOffset + l] == 0.0f)
				    	{
				    		linksWeight[wOffset + l] = -1 / ((float)count * K_SOOTN_POS_I_NEGATIVE);
			    		}
			    		else
			    		{
							linksWeight[wOffset + l] = linksWeight[wOffset + l] / sumW;
						}
					}
	    		
				    package[packagePos] = sum;
				}
	    	}
	    	else
	    	{
	    		//busy
			    maximum = max(sum, maximum);
    		
		    	//record maximum of package activity
			    if (package[packagePos] < sum)
			    {
				    package[packagePos] = sum;
			    }
	    	}
	    }
	}
    //output[pos] = maximum;
    
    int _cycleCols = 0;
    int pN = 0;
    for (int p = 0; p < numberOfPackages; p++)
    {
		packagePos = (((y * outputSizeX) + x) * numberOfPackages) + p;
	    
	    if (packageFree[packagePos] > 0)
	    {
		    pN++;
		    _cycleCols += package[packagePos];
	    }
    }
    
    if (pN > 0)
    {
		output[pos] = _cycleCols / (float)pN;
	}
	else
	{
		output[pos] = 0;
	}
}
