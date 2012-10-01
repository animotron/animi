package org.animotron.animi;

// Projection description of the one zone to another
class Mapping {
    CortexZoneSimple zone;       // Projecting zone
    int ns_links;           // Number of synaptic connections for the zone
    double disp_links;      // Grouping parameter. Describe a size of sensor field

    public Mapping(CortexZoneSimple zone, int ns_links, double disp_links) {
        this.zone = zone;
        this.ns_links = ns_links;
        this.disp_links = disp_links;
    }

    public String toString() {
    	return "mapping "+zone.toString();
    }
}