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

    __global float* cycleCols,

    __global float* input,
    int inputSizeX
) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    int pos = (y * outputSizeX) + x;
    
    int packagePos = 0;
    
    int XSize = linksNumber * 2;
    int offset = (y * outputSizeX * XSize) + (x * XSize);

    int linksOffset = (y * outputSizeX * linksNumber * numberOfPackages) + (x * linksNumber * numberOfPackages);
	int wOffset = 0;

    float maximum = 0.0f;
    float sum = 0.0f;
    
    int empty = 1;
	bool toRemember = true;
	int rememberOn = 0;
    
    for (int step = 0; step < numberOfSteps; step++)
    {
		int shiftX = tremor[step*2 +0];
		int shiftY = tremor[step*2 +1];
		
		toRemember = true;
		
	    for (int p = 0; p < numberOfPackages; p++)
	    {
	    	wOffset = linksOffset + (p * linksNumber);

			packagePos = (y * outputSizeX * numberOfPackages) + (x * numberOfPackages) + p;
			empty = packageFree[packagePos];

			sum = 0.0f;
		    for(int l = 0; l < linksNumber; l++)
		    {
		    	int xi = linksSenapse[offset + (l * 2) +0] + shiftX;
		    	int yi = linksSenapse[offset + (l * 2) +1] + shiftY;
		        
		        if (empty == 1)
		        	sum += input[(yi * inputSizeX) + xi] / (float)linksNumber;
	        	else
		        	sum += input[(yi * inputSizeX) + xi] * linksWeight[wOffset + l];
		    }
		    
		    //should not be more then one
	    	if (sum > 1.0f)
	    	{
	    		sum = 1.0f;
			}
			
		    package[packagePos] = 0.0f;
		    
		    if (empty == 1)
	    	{
	    		//free
	    		if (sum < 0.1f) //10% of RF
	    		{
				    sum = 0.0f;
				}
				else if (toRemember && p >= rememberOn)
				{
					toRemember = false;
					rememberOn = p+1;
					
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
				    		linksWeight[wOffset + l] = -1 / ((float)count * 0.5);
			    		}
			    		else
			    		{
							linksWeight[wOffset + l] = linksWeight[wOffset + l] / sumW;
						}
					}
				}
	    	}
	    	else
	    	{
	    		//busy
			    if (sum < 0.5f) 
			    {
		    		sum = 0.0f;
	    		}
	    		else
	    		{
				    maximum = max(sum, maximum);
	    		}
	    		
			    for(int l = 0; l < linksNumber; l++)
			    {
			        linksWeight[wOffset + l] = 1;
			    }
	    		
	    	}
	    	
	    	//record maximum of package activity
		    if (package[packagePos] < sum)
		    {
			    package[packagePos] = sum;
		    }
	    }
	}
    output[pos] = maximum;
    
    int _cycleCols = 0;
    int pN = 0;
    for (int p = 0; p < numberOfPackages; p++)
    {
		packagePos = (y * outputSizeX * numberOfPackages) + (x * numberOfPackages) + p;
	    
	    if (packageFree[packagePos] < 1)
	    {
		    pN++;
		    if (package[packagePos] > 0.0f)
		    {
				_cycleCols++;
			}
	    }
    }
    
	cycleCols[pos] = _cycleCols / (float)pN;
}
