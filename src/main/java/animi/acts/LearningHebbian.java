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
 * Delta rule. http://en.wikipedia.org/wiki/Delta_rule
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class LearningHebbian extends Task {
	
	@RuntimeParam(name = "count")
	public int count = 10000;

	@RuntimeParam(name = "ny")
	public float ny = 0.1f;
	
	public float factor;
	
	public LearningHebbian(LayerWLearning cz) {
		super(cz);
		
		factor = ny;
//		factor = (float) (ny / Math.pow(2, cz.count / count));
	}

	private static float adjust(
			final Floats in, 
			final Floats posW, 
			final Floats negW, 
			final float activity, 
			final float avg,
			final float factor) {
		
		float sumQ2 = 0.0f;
		for (int index = 0; index < posW.length(); index++) {
    		
//			if (negW != null && negW.getByIndex(index) > 0f) {
//	    		posW.setByIndex(0f, index);
//				continue;
//			}

			final float X = in.getByIndex(index) - avg;
			if (X < 0f) {
				System.out.println();
			}
			
			final float q = posW.getByIndex(index) + X * activity * factor;
//			if (q > 0f) {
	    		posW.setByIndex(q, index);
			
	    		sumQ2 += q * q;
//			} else {
//	    		posW.setByIndex(0f, index);
//			}
		}
	    return sumQ2;
	}

	public static void learn(
			final Floats in, 
			final Floats posW, 
			final Floats negW, 
			final float activity,
			final float avg,
			final float factor) {
		
		if (activity > 0) {
			final float sumQ2 = adjust(in, posW, negW, activity, avg, factor);
			
			Mth.normalization(posW, sumQ2);
		}
	}
	
	private float avg = 0f;

	@Override
	public boolean prepare() {
		avg = 0f;
		
		final Mapping m = cz.in_zones[0];
		
		if (m.haveInhibitoryWeight()) {

			final FloatsImpl neurons = m.frZone().axons;
			
			float sum = 0f;
			for (int index = 0; index < neurons.length(); index++) {
				sum += neurons.getByIndex(index);
			}
			
			avg = sum / (float)neurons.length();
		}
//		System.out.println("Learning");
		
		return true;
	}

	@Override
	public void gpuMethod(final int x, final int y, final int z) {
		
//		if (cz.toLearning.get(x,y,z) <= 0f) return;
		
		final Mapping m = cz.in_zones[0];
		
		if (!m.isDirectLearning()) {
			return;
		}
		
		final float act = m.toZone().neurons.get(x, y, z);
		if (act <= 0) return;
		
		m.senapsesCode().set((float)cz.app.getStimulator().getCode(), x,y,z);
		
//		System.out.println("["+x+","+y+","+z+"] "+act);

		Floats in = new FloatsMapped(m.frZone().axons, m._senapses().sub(x, y, z));
		Floats posW = m.senapseWeight().sub(x, y, z);
		Floats negW = null;
		if (m.haveInhibitoryWeight()) {
			negW = m.inhibitoryWeight().sub(x, y, z);
		}
		
		LearningHebbian.learn(
			in, 
			posW,
			negW,
			act,
			avg,
			factor
		);

//		if (m.haveInhibitoryWeight()) {
//			LearningHebbianAnti.learn(
//				in, 
//				posW, 
//				negW, 
//				act,
//				avg,
//				factor
//			);
//		}
		
//		LayerSimple frLayer = m.frZone();
//		if (frLayer instanceof LayerWLearning) {
//			
//			Mapping _m = ((LayerWLearning) frLayer).in_zones[0];
//			if (_m.isDirectLearning()) {
//				return;
//			}
//			
//			//XXX: to fix!
//			LearningHebbian learning = (LearningHebbian)((LayerWLearning) frLayer).cnLearning;
//			
//			MatrixProxy<Integer[]> senapses = m.senapses().sub(x, y, z);
//			for (int index = 0; index < senapses.length(); index++) {
//				Integer[] xyz = senapses.getByIndex(index);
//				
//				if (xyz == null) {
//					System.out.println("ERROR! at ["+x+","+y+","+z+"] "+index);
//				}
//				
//				final int xi = xyz[0];
//				final int yi = xyz[1];
//				final int zi = xyz[2];
//			
//				final float _act = _m.toZone().neurons.get(xi, yi, zi);
//				
//				if (_act <= 0) {
//					continue;
//				}
//	
//				MatrixMapped<Float> _in = new MatrixMapped<Float>(_m.frZone().neurons, _m._senapses().sub(xi, yi, zi));
//				MatrixProxy<Float> _posW = _m.senapseWeight().sub(xi, yi, zi);
//				Matrix<Float> _negW = null;
//				if (m.haveInhibitoryWeight()) {
//					_m.inhibitoryWeight().sub(xi, yi, zi);
//				}
//				
//				float f = 10;
//				if (m.senapseWeight().getByIndex(index) > 1/(float)2) {
//					f = 0.1f;
//				}
//	
//				//XXX: fix: factor
//				learning.learn(
//						_in, _posW, _negW, 
//						_act * f,
//						0, factor);
//			}
//		}
	}

	@Override
    protected void release() {
    }
}