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
package animi.charts;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.jzy3d.bridge.IFrame;
import org.jzy3d.bridge.swing.FrameSwing;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.Settings;
import org.jzy3d.chart.controllers.keyboard.camera.AWTCameraKeyController;
import org.jzy3d.chart.controllers.keyboard.camera.NewtCameraKeyController;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.SwingChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import animi.tuning.Codes;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class ChartFrame extends JInternalFrame implements IFrame {
	
	protected static Rectangle DEFAULT_WINDOW = new Rectangle(0,0,600,600);
	Chart chart;
	
    public ChartFrame() {
	    super("Chart",
	            true, //resizable
	            true, //closable
	            false, //maximizable
	            true);//iconifiable
	    
	    Settings.getInstance().setHardwareAccelerated(true);
	    
	    setLocation(100, 100);

	    init();
	    initialize(chart, DEFAULT_WINDOW, "name here");
	}
    
    protected double[][] values = new double[Codes.CODES][Codes.SHIFTS];
    
    protected void init() {
        // Create the object to represent the function over the given range.
        surface = 
    		Builder.buildOrthonormal(
        		new OrthonormalGrid(rangeX, Codes.CODES, rangeY, Codes.SHIFTS),
        		mapper
    		);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(true);

        // Create a chart
        chart = new Chart(new SwingChartComponentFactory(), Quality.Advanced, getCanvasType());
        chart.addController(new AWTCameraKeyController());
        
        updateSurface();
        
        //chart.pauseAnimator();
    }
    
	// Define a function to plot
    Mapper mapper = new Mapper() {
        public double f(double x, double y) {
//        	System.out.println("Mapper "+x+" "+y+" = "+values[(int)x][(int)y]);
        	if (Double.isNaN(x) || Double.isNaN(y))
        		return Double.NaN;
        	
//        	if (values[(int)x][(int)y] == 0)
//                return x * Math.sin(x * y);
        	
        	return values[(int)x][(int)y];
        }
    };

    // Define range and precision for the function to plot
    Range rangeX = new Range(0, Codes.CODES-1);
    Range rangeY = new Range(0, Codes.SHIFTS-1);

    Shape surface = null;
    
    public void updateSurface() {
//    	if (surface != null)
//            chart.getScene().getGraph().remove(surface);
    		
    	Shape old = surface;
        surface = 
    		Builder.buildOrthonormal(
        		new OrthonormalGrid(rangeX, Codes.CODES, rangeY, Codes.SHIFTS),
        		mapper
    		);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        chart.getScene().getGraph().add(surface);
        chart.getScene().getGraph().remove(old);

        chart.render();
        
        repaint();
    }
    
    protected String canvasType="swing";//"awt";//"newt";
    public String getCanvasType(){
	    return canvasType;
	}

	@Override
	public void initialize(Chart chart, Rectangle bounds, String title) {
		this.chart = chart;

		Container contentPane = getContentPane();
		BorderLayout layout = new BorderLayout();
		contentPane.setLayout(layout);

//		addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				ChartFrame.this.remove((java.awt.Component) ChartFrame.this.chart.getCanvas());
//				ChartFrame.this.chart.dispose();
//				ChartFrame.this.chart = null;
//				ChartFrame.this.dispose();
//			}
//		});

		JPanel panel3d = new JPanel();
		panel3d.setLayout(new java.awt.BorderLayout());
		panel3d.add((JComponent) chart.getCanvas());

		contentPane.add((JComponent) chart.getCanvas(), BorderLayout.CENTER);
		setVisible(true);
		setTitle(title + " [Animi]");
		pack();
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	@Override
	public void initialize(Chart chart, Rectangle bounds, String title, String message) {
		initialize(chart, bounds, title);
	}
}
