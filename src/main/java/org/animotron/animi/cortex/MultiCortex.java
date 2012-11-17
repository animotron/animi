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
package org.animotron.animi.cortex;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.animotron.animi.Params;
import org.animotron.animi.gui.Application;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class MultiCortex {

    public boolean active = false;
    
    public long count = 0;
    
    public Retina retina;

    public CortexZoneSimple z_in;
    public CortexZoneComplex z_1st;
    public CortexZoneComplex z_goriz1;
    public CortexZoneComplex z_2nd;
    public CortexZoneComplex z_goriz2;
    public CortexZoneComplex z_3rd;
//  @Params
//  public CortexZoneSimple z_good;
//  @Params
//  public CortexZoneSimple z_bad;

    @Params
    public CortexZoneSimple [] zones;

    private MultiCortex(CortexZoneSimple [] zones) {
    	this.zones = zones;

        retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
        retina.setNextLayer(zones[0]);
    }

    public MultiCortex() {

        z_in = new CortexZoneSimple("Input", this);

        z_1st = new CortexZoneComplex("1st", this, 200, 200,
            new Mapping[]{
                new Mapping(z_in, 50, 1, false) //7x7 (50)
            }
        );
        z_in.nextLayers(new CortexZoneSimple[] {z_1st});

        z_goriz1 = new CortexZoneComplex("1st G", this, 20, 20,
            new Mapping[]{
                new Mapping(z_1st, 400, 2, false) //20x20 (400)
            }
        );
        z_goriz1.inhibitory_links = 0;
        z_goriz1.disper = 1;

        z_1st.nextLayers(new CortexZoneSimple[] {z_goriz1});

        z_2nd = new CortexZoneComplex("2nd", this, 50, 50,
            new Mapping[]{
                new Mapping(z_1st, 400, 2, false),
                new Mapping(z_goriz1, 300, 2, true)
            }
        );
        z_1st.nextLayers(new CortexZoneSimple[] {z_2nd});
        z_goriz1.nextLayers(new CortexZoneSimple[] {z_2nd});

//        z_3rd = new CortexZoneComplex("3rd", this, 32, 32,
//            new Mapping[]{
//                new Mapping(z_2nd, 300, 5)
//            }
//        );

        zones = new CortexZoneSimple[]{z_in, z_1st, z_goriz1, z_2nd};
//        zones = new CortexZoneSimple[]{z_in, z_1st};
        
        retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
        retina.setNextLayer(z_in);
    }


	public void init() {
		for (CortexZoneSimple zone : zones) {
			zone.init();
		}
	}

//    public void process() {
//    	CortexZoneSimple prev = null;
//		for (CortexZoneSimple zone : zones) {
//			if (prev != null) {
//				zone.zero();
//				prev.process();
//				prev.activateNextLayer();
//			}
//			prev = zone;
//		}
//		if (prev != null) {
//			prev.process();
//
//			Application.count.setText(String.valueOf(count++));
//		}
//    }

    public void process() {
		for (CortexZoneSimple zone : zones) {
			zone.process();
		}
		Application.count.setText(String.valueOf(count++));
    }

    public void save(Writer out) throws IOException {
    	out.write("<cortex>");
		for (CortexZoneSimple zone : zones) {
			zone.save(out);
		}
    	out.write("</cortex>");
    }


	public static MultiCortex load(File file) throws IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			SAXPars saxp = new SAXPars();
	
			parser.parse(file, saxp);
			
			return saxp.mc;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public static class SAXPars extends DefaultHandler { 
		
		MultiCortex mc = null;
		List<CortexZoneSimple> zones = new ArrayList<CortexZoneSimple>();
		
		CortexZoneSimple prevZone = null;
		CortexZoneSimple zone = null;
		
		double fX, fY = 0;
		
		NeuronComplex cn = null;
		
		List<Mapping> mappings = new ArrayList<Mapping>();
		
		@Override
	    public void startElement (String uri, String localName, String qName, Attributes attrs) {
			if ("linkS".equals(qName)) {
				
				new LinkQ(
						prevZone.getCol(
							Integer.valueOf(attrs.getValue("sX")),
							Integer.valueOf(attrs.getValue("sY"))
						),
						cn, 
						Double.valueOf(attrs.getValue("w")),
						fX,
						fY
					);

			} else if ("linkI".equals(qName)) {
				
				new Link(
						zone.getCol(
							Integer.valueOf(attrs.getValue("sX")),
							Integer.valueOf(attrs.getValue("sY"))
						),
						cn, 
						Double.valueOf(attrs.getValue("w")),
						LinkType.INHIBITORY
					);
				
				
			} else if ("cn".equals(qName)) {
				cn = zone.getCol(
							Integer.valueOf(attrs.getValue("x")),
							Integer.valueOf(attrs.getValue("y"))
						);
			
			} else if ("mapping".equals(qName)) {
				mappings.add(
					new Mapping(
						prevZone,
						Integer.valueOf(attrs.getValue("number-of-synaptic-links")),
						Double.valueOf(attrs.getValue("synaptic-links-dispersion")),
						Boolean.valueOf(attrs.getValue("soft"))
					)
				);
				
			} else if ("zone".equals(qName)) {
				if ("complex".equals(attrs.getValue("type"))) {
					CortexZoneComplex cZone;
					zone = cZone = new CortexZoneComplex();

					cZone.count = Integer.valueOf(attrs.getValue("count"));
					
				} else {
					zone = new  CortexZoneSimple();
				}
				zone.id = attrs.getValue("id");
				zone.name = attrs.getValue("name");
				zone.width = Integer.valueOf(attrs.getValue("width"));
				zone.height = Integer.valueOf(attrs.getValue("height"));
				zone.active = Boolean.valueOf(attrs.getValue("active"));
				zone.learning = Boolean.valueOf(attrs.getValue("learning"));
				
				zone.initCols();

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
				
				if (zone instanceof CortexZoneComplex) {
					((CortexZoneComplex) zone).in_zones = 
						mappings.toArray(new Mapping[mappings.size()]);
				}
				mappings.clear();
				
				zone = null;
			}
		}
		
	    public void endDocument() throws SAXException {
	    	mc = new MultiCortex(zones.toArray(new CortexZoneSimple[zones.size()]));
	    }
	}
}
