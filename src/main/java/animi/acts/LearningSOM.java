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
package animi.acts;

import animi.RuntimeParam;
import animi.cortex.*;
import animi.matrix.*;

/**
 * Self-organizing feature map.
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningSOM extends Task {
	
	@RuntimeParam(name = "count")
	public int count = 10000;

	@RuntimeParam(name = "ny")
	public float ny = 0.01f;
	
	private float factor;
	
	public LearningSOM(LayerWLearning cz) {
		super(cz);
		
		factor = ny;
//		factor = (float) (ny / Math.pow(2, cz.count / count));
	}

	private static float adjust(
			final Floats in, 
			final Floats posW, 
			final Floats negW, 
			final float avg, 
			final float factor) {
		
		float sumQ2 = 0.0f;
		for (int index = 0; index < posW.length(); index++) {
    		
			if (negW.getByIndex(index) > 0f) {
				
	    		posW.setByIndex(0f, index);
				continue;
			}

			final float X = in.getByIndex(index) - avg;

			final float q = posW.getByIndex(index) + X * factor;
    		
			if (q > 0f) {
	    		posW.setByIndex(q, index);
	    		
	    		sumQ2 += q * q;
			} else {
	    		posW.setByIndex(0f, index);
			}
		}
	    
	    return sumQ2;
	}

	public static void learn(
			final MappingSOM m,
			final ArrayOfIntegers lateralSenapse, 
			final Floats lateralWeight,
			final float avg,
			final float factor) {
		
		for (int index = 0; index < lateralWeight.length(); index++) {
			int[] xyz = lateralSenapse.getByIndex(index);
			
			final int xi = xyz[0];
			final int yi = xyz[1];
			final int zi = xyz[2];
		
			final Floats posW = m.senapseWeight().sub(xi, yi, zi);
			final Floats negW = m.inhibitoryWeight().sub(xi, yi, zi);
			
			final float sumQ2 = adjust(
					new FloatsMapped(m.frZone().axons, m._senapses().sub(xi, yi, zi)), 
					posW,
					negW,
					avg,
					factor * lateralWeight.getByIndex(index)
			);
			
//			System.out.println("["+xi+","+yi+","+zi+"] "+lateralWeight.getByIndex(index));
			
			Mth.normalization(posW, sumQ2);
		}
	}

	private float avg = 0f;

	@Override
	public boolean prepare() {
		avg = 0f;
		
		final Mapping m = cz.in_zones[0];

		final FloatsImpl neurons = m.frZone().axons;
		
		float sum = 0f;
		for (int index = 0; index < neurons.length(); index++) {
			sum += neurons.getByIndex(index);
		}
		
		avg = sum / (float)neurons.length();
		
		return true;
	}

	@Override
	public void gpuMethod(final int x, final int y, final int z) {
		
//		System.out.println("! ["+x+","+y+","+z+"]");
		
		final MappingSOM m = (MappingSOM) cz.in_zones[0];
		
		final float act = m.toZone().neurons.get(x, y, z);

		if (act <= 0) {
			return;
		}
		
		final ArrayOfIntegersProxy lateralSenapse = m.lateralSenapse().sub(x, y, z);
		final FloatsProxy lateralWeight = m.lateralWeight().sub(x, y, z);

		LearningSOM.learn(
			m,
			lateralSenapse,
			lateralWeight,
			avg,
			factor * act
		);
		
//		LearningSOMAnti.learn(
//			m,
//			lateralSenapse,
//			lateralWeight,
//			avg,
//			factor * act
//		);

//		LearningSOMLateral.learn(
//			m,
//			lateralSenapse,
//			lateralWeight,
//			act,
//			0.001f
//		);
	}

	@Override
	public boolean isDone() {
//		final MappingSOM m = (MappingSOM) cz.in_zones[0];
//		m.frZone().debugAxons("axons");
//		m.senapseWeight().debug("senapseWeight");
//		m.inhibitoryWeight().debug("inhibitoryWeight");
		
		return super.isDone();
	}

	@Override
    protected void release() {
    }
}
