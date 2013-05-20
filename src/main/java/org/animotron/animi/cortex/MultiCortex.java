/*
 *  Copyright (C) 2012-2013 The Animo Project
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
package org.animotron.animi.cortex;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.animotron.animi.acts.*;
import org.animotron.animi.gui.Application;
import org.animotron.animi.gui.Controller;
import org.animotron.animi.tuning.InhibitoryLearningMatrix;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static org.animotron.matrix.MatrixDelay.*;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 */
public class MultiCortex extends Controller {

    public IRetina retina;
    LayerSimple z_in;
    LayerWLearning layer_1memory;
    LayerWLearningOnReset layer_2correlation;
    LayerWLearning layer_3factor;
    
    MappingHebbian m;
    MappingHebbian mm;

    private MultiCortex(Application app, LayerSimple [] zones) {
    	super(app);

    	this.zones = zones;

        retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
        retina.setNextLayer(zones[0]);
    }

    public MultiCortex(Application app) {
    	super(app);

    	
//    	final int delay = 8;
    	z_in = new LayerSimple("Зрительный нерв", app, 60, 60, 1,
			oneStepAttenuation
//    		new MatrixDelay.Attenuation() {
//
//				@Override
//				public float next(int step, float value) {
//					if (step > delay)
//						return 0f;
//					
//					if (value == 1f && step > 1)
//						return 0f;
//					
//					return (float) ((pow(step - delay, 2) / pow(delay, 2)) * value);
//				}
//        	}
        );
    	z_in.isZeroAvgAxons = false;
        
        //1st zone
//    	LayerWLearning layer_1a = new LayerWLearning("1й образы", this, 5, 5, 4, //120, 120, //160, 120,
//            new Mapping[]{
//                new MappingHebbian(z_in, 200, 1, true, true)
//            },
//            WinnerGetsAll.class,
//            LearningHebbian.class,
//            new Attenuation() {
//	    		@Override
//	    		public float next(int step, float value) {
//	    			return step <= 5 ? value : 0f;
//	    		}
//	    	}
//        );

    	int RF = 100;
    	int W = 20, H = 20;
    	
    	m = new MappingHebbian(z_in, RF, 1, true, false);
    	layer_1memory = new LayerWLearning("1й память", app, W, H, 9,
            new Mapping[]{
                m //new MappingHebbian(z_in, 150, 1, true, false)
            },
            ActivationMemory.class,
            null,//Inhibitory.class,
            Mediator.class,
            InhibitoryLearningMatrix.class,
            LearningMemory.class,
            noAttenuation
        );
        
//    	layer_2correlation = new LayerWLearningOnReset("2й корреляция", app, 20, 20, 1,
//            new Mapping[]{
//                new MappingHebbian(layer_1memory, 12, .5, true, false)
//            },
//            Activation.class, //ActivationMemory.class,
//            WinnerGetsAll.class, //Inhibitory.class,
//            null,//FormLearningMatrix.class,
//            null,//InhibitoryLearningMatrix.class,
//            LearningHebbian.class, //LearningMemory.class,
//            noAttenuation
//        );
//    	layer_2correlation.singleReceptionField = false;
//    	layer_2correlation.inhibitory_number_of_links = 12;
////    	layer_2correlation.cnLearning.factor = 0.01f;
    	
    	mm = new MappingHebbian(z_in, RF, 1, true, false);

    	layer_3factor = new LayerWLearning("3й фактор", app, W, H, 1,
            new Mapping[]{
                new MappingHebbian(layer_1memory, 9, .1, true, false),
                mm
            },
            null, //Activation.class, //ActivationMemory.class,
            null, //WinnerGetsAll.class, //Inhibitory.class,
            null,//FormLearningMatrix.class,
            null,//InhibitoryLearningMatrix.class,
            LearningHebbianOnMemory.class, //LearningMemory.class,
            noAttenuation
        );
    	layer_3factor.singleReceptionField = false;

    	zones = new LayerSimple[]{z_in, layer_1memory, layer_3factor}; 
    	
        setRetina(null);
    }
    
	@Override
	public void setRetina(IRetina retina) {
		if (retina == null)
			this.retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
		else
			this.retina = retina;
		
        this.retina.setNextLayer(z_in);
//        this.retina.addResetLayer(layer_3factor);
        this.retina.addResetLayer(layer_1memory);
	}

	@Override
	public IRetina getRetina() {
		return retina;
	}

	@Override
    public void init() {
    	// Flush all pending tasks
        flush();

		for (LayerSimple zone : zones) {
			zone.init();
		}
		
//    	mm.senapses = m.senapses;
//    	mm._senapses = m._senapses;
    }

    /**
     * Flush all pending tasks and finish all running tasks
     */
    public void flush() {
    }
	
	@Override
	public void process() {

    	retina.process(app.getStimulator());
    	
		for (LayerSimple zone : zones) {
			zone.process();
		}
	}
	
    public void save(Writer out) throws IOException {
    	out.write("<cortex>");
		for (LayerSimple zone : zones) {
			zone.save(out);
		}
    	out.write("</cortex>");
    }


	public static MultiCortex load(Application app, File file) throws IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			SAXPars saxp = new SAXPars(app);
	
			parser.parse(file, saxp);
			
			return saxp.mc;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public static class SAXPars extends DefaultHandler { 
		
		Application app;
		
		MultiCortex mc = null;
		List<LayerSimple> zones = new ArrayList<LayerSimple>();
		
		LayerSimple prevZone = null;
		LayerSimple zone = null;
		
		double fX, fY = 0;
		
		List<MappingHebbian> mappings = new ArrayList<MappingHebbian>();
		
		public SAXPars(Application app) {
			this.app = app;
		}
		
		@Override
	    public void startElement (String uri, String localName, String qName, Attributes attrs) {
			if ("linkS".equals(qName)) {
				
//				new LinkQ(
//						prevZone.getCol(
//							Integer.valueOf(attrs.getValue("sX")),
//							Integer.valueOf(attrs.getValue("sY"))
//						),
//						cn, 
//						Double.valueOf(attrs.getValue("w")),
//						fX,
//						fY,
//						Double.valueOf(attrs.getValue("speed"))
//					);

			} else if ("linkI".equals(qName)) {
				
//				new Link(
//						zone.getCol(
//							Integer.valueOf(attrs.getValue("sX")),
//							Integer.valueOf(attrs.getValue("sY"))
//						),
//						cn, 
//						Double.valueOf(attrs.getValue("w")),
//						LinkType.INHIBITORY
//					);
				
				
			} else if ("cn".equals(qName)) {
//				cn = zone.getCol(
//							Integer.valueOf(attrs.getValue("x")),
//							Integer.valueOf(attrs.getValue("y"))
//						);
			
			} else if ("mapping".equals(qName)) {
				mappings.add(
					new MappingHebbian(
						prevZone,
						Integer.valueOf(attrs.getValue("number-of-synaptic-links")),
						Double.valueOf(attrs.getValue("synaptic-links-dispersion")),
						Boolean.valueOf(attrs.getValue("soft")),
						Boolean.valueOf(attrs.getValue("haveInhibitory"))
					)
				);
				
			} else if ("zone".equals(qName)) {
				if ("complex".equals(attrs.getValue("type"))) {
					LayerWLearning cZone;
					zone = cZone = new LayerWLearning();

					cZone.count = Integer.valueOf(attrs.getValue("count"));
					
				} else {
					zone = new  LayerSimple();
				}
				zone.id = attrs.getValue("id");
				zone.name = attrs.getValue("name");
				zone.width = Integer.valueOf(attrs.getValue("width"));
				zone.height = Integer.valueOf(attrs.getValue("height"));
				zone.active = Boolean.valueOf(attrs.getValue("active"));
				zone.isLearning = Boolean.valueOf(attrs.getValue("learning"));
				
				zone.init();

				if (prevZone != null) {
					fX = prevZone.width() / (double) zone.width();
					fY = prevZone.height() / (double) zone.height();
				}
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if ("zone".equals(qName)) {
				prevZone = zone;
				zones.add(zone);
				
				if (zone instanceof LayerWLearning) {
					((LayerWLearning) zone).in_zones = 
						mappings.toArray(new MappingHebbian[mappings.size()]);
				}
				mappings.clear();
				
				zone = null;
			}
		}
		
		@Override
	    public void endDocument() throws SAXException {
	    	mc = new MultiCortex(app, zones.toArray(new LayerSimple[zones.size()]));
	    }
	}
}
