package org.animotron.animi;


// Simple neuron
class SNeuron {
    boolean occupy, active;
    int n_on;               // Number of active cycles after activation
    int n_act;              // Number of cycles after activation
    double p_on;            // Average number of active neighbors at activation moment
    double p_off_m;         // Average number of active neighbors when calm and activity of neighbors more p_on
    int n_off_m;            // Number of passive cycles after activation when activity of neighbors more p_on
    Link2dZone[] s_links;   // Links of synapses connects cortex neurons with projecting nerve bundle
    Link2d[] a_links;       // Axonal connections with nearest cortical columns
    int n1;                 // Counter for links of synapses
    int n2;                 // Counter for axonal connections
}