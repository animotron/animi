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
package org.animotron.animi;


/**
 * Simple neuron
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
class NeuronSimple {
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