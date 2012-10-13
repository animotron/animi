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
package org.animotron.animi.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.animotron.animi.Imageable;
import org.animotron.animi.cortex.CortexZoneComplex;
import org.animotron.animi.cortex.CortexZoneSimple;
import org.animotron.animi.cortex.MultiCortex;
import org.animotron.animi.cortex.Retina;
import org.animotron.animi.simulator.Figure;
import org.animotron.animi.simulator.RectAnime;
import org.animotron.animi.simulator.Stimulator;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Application extends JFrame {
	
	private static final long serialVersionUID = 3243253015790558286L;
	
	static Application _ = null;

	private JMenuItem miInit = null;
	private JMenuItem miRun = null;
	private JMenuItem miPause = null;
	private JMenuItem miResume = null;
	private JMenuItem miStop = null;
	
	public static MultiCortex cortexs = null;
	
	JDesktopPane desktop;
	
	private Application() {
		
		_ = this;
		
		setTitle("Animi");
		
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                  screenSize.width  - inset*2,
                  screenSize.height - inset*2);
 
        desktop = new JDesktopPane();
        createFrame(stimulator);
        
        setContentPane(desktop);
        setJMenuBar(createMenuBar());
 
        //Make dragging a little faster but perhaps uglier.
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stop();
            }

			@Override
            public void windowDeiconified(WindowEvent e) {
                resume();
            }

            @Override
            public void windowIconified(WindowEvent e) {
                pause();
            }
        });

//        //constants control
//        //минимальное соотношение средней контрасности переферии и центра сенсорного поля, 
//        //необходимое для активации контрастность для темных элементов (0)
//		addDoubleSlider("соот.  переферии и центра", "KContr1", tools);
//		//контрастность для светлых элементов
//		addDoubleSlider("контрастность для светлых", "KContr2", tools);
//		addDoubleSlider("минимальная контрастность", "KContr3", tools);
//		
//		addIntSlider("Bright Level","Level_Bright",tools);
//
//		camView = new WebcamPanel();
//        add(camView, CENTER);
//
        setBounds(0, 0, 800, 600);
        setLocationByPlatform(true);
	}
	
    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
 
        //Set up the lone menu.
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);
 
        //Set up the first menu item.
        JMenuItem menuItem = new JMenuItem("Retina");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(
    		KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK)
		);
        menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            createFrame(stimulator);
			}
		});
        menu.add(menuItem);
 
        //Set up the second menu item.
        menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.setAccelerator(
    		KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK)
		);
        menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        System.exit(0);
			}
        	
        });
        menu.add(menuItem);
 
        //control
        menu = new JMenu("Control");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);
 
        miInit = addMenu(menu, "init", KeyEvent.VK_I, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            init();
			}
		});

        miRun = addMenu(menu, "run", KeyEvent.VK_R, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            run();
			}
		});
        miRun.setEnabled(false);

        miPause = addMenu(menu, "pause", KeyEvent.VK_P, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            pause();
			}
		});
        miPause.setEnabled(false);

        miResume = addMenu(menu, "resume", KeyEvent.VK_E, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            resume();
			}
		});
        miResume.setEnabled(false);

        miStop = addMenu(menu, "stop", KeyEvent.VK_S, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            stop();
			}
		});
        miStop.setEnabled(false);

        return menuBar;
    }
    
    private JMenuItem addMenu(JMenu menu, String name, int key, ActionListener listener) {
    	JMenuItem menuItem = new JMenuItem(name);
        menuItem.setMnemonic(key);
        menuItem.setAccelerator(
    		KeyStroke.getKeyStroke(key, ActionEvent.ALT_MASK)
		);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        return menuItem;
    }
    
    private void init() {
//    	camView.resume();
    	//задание параметров зон коры и структуры связей
    	//BrainInit
    	//инициализация зон коры
    	//CortexInit
    	//Начальный сброс "хорошо - плохо"
    	cortexs = new MultiCortex();
    	
    	PFInitialization form = new PFInitialization(this, cortexs);
    	form.setVisible(true);
        desktop.add(form);
        try {
        	form.setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {}
    }
    
    protected void initialize() {
    	
    	cortexs.init();
    	
        for (CortexZoneSimple zone : cortexs.zones) {
        	if (zone instanceof CortexZoneComplex) {
				CortexZoneComplex z = (CortexZoneComplex) zone;

				createFrame(z);
				createFrame(z.getCRF());
			} else
				createFrame(zone);
        }
        stimulator.start();

//    	camView.resume();

    	miInit.setEnabled(false);
    	
    	miRun.setEnabled(true);
    	
    	miPause.setEnabled(false);
    	miResume.setEnabled(false);
    	
    	miStop.setEnabled(false);
    }
    
    private void run() {
    	cortexs.active = true;
    	
    	miInit.setEnabled(false);
    	
    	miRun.setEnabled(false);
    	
    	miPause.setEnabled(true);
    	miResume.setEnabled(false);
    	
    	miStop.setEnabled(true);
    }
    
    private void pause() {
		cortexs.active = false; 
		
		miPause.setEnabled(false);
		miResume.setEnabled(true);
    }
    
    private void resume() {
		cortexs.active = true; 
		
		miResume.setEnabled(false);
		miPause.setEnabled(true);
    }
	
    private void stop() {
		cortexs.active = false; 
		
		miInit.setEnabled(true);
		
		miRun.setEnabled(false);
		
		miPause.setEnabled(false);
		miResume.setEnabled(false);
		
		miStop.setEnabled(false);
	}

	private void addDoubleSlider(String name, final String constName, JPanel tools) {
		final Field field;
		double value;
		try {
			field = MultiCortex.class.getDeclaredField(constName);
			value = field.getDouble(MultiCortex.class);
		} catch (Exception ex) {
			return;
		}
		
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.black));

        GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		
        panel.add(new JLabel(name), c);

        final JLabel label = new JLabel();
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 50, 200, (int)Math.round(value * 100));
        slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				try {
					field.setDouble(MultiCortex.class, source.getValue() / 100.0);
			        label.setText(String.valueOf(field.get(MultiCortex.class)));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

        label.setText(String.valueOf(value));
        label.setLabelFor(slider);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
        panel.add(label, c);

        //Turn on labels at major tick marks.
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		c.gridx = 1;
		c.gridy = 1;
		panel.add(slider, c);
		
        tools.add(panel);
	}

	private void addIntSlider(String name, final String constName, JPanel tools) {
		final Field field;
		int value;
		try {
			field = MultiCortex.class.getDeclaredField(constName);
			value = field.getInt(MultiCortex.class);
		} catch (Exception ex) {
			return;
		}
		
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.black));

        GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		
        panel.add(new JLabel(name), c);

        final JLabel label = new JLabel();
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 255, value);
        slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				try {
					field.setInt(MultiCortex.class, source.getValue());
			        label.setText(String.valueOf(field.get(MultiCortex.class)));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

        label.setText(String.valueOf(value));
        label.setLabelFor(slider);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
        panel.add(label, c);

        //Turn on labels at major tick marks.
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		c.gridx = 1;
		c.gridy = 1;
		panel.add(slider, c);
		
        tools.add(panel);
	}

	Stimulator stimulator = new Stimulator(
            Retina.WIDTH, Retina.HEIGHT,
            new Figure[] {
                    new RectAnime(
                            50, 0.05,
                            new int[][] {
                                    {40, 40},
                                    {Retina.WIDTH - 40, 40},
                                    {Retina.WIDTH - 40, Retina.HEIGHT - 40},
                                    {40, Retina.HEIGHT - 40},
                                    {40, 40}
                            }
                    )
            }
    );

    //Create a new internal frame.
    protected void createFrame(Imageable imageable) {
        Visualizer frame = new Visualizer( imageable );
        frame.setVisible(true); //necessary as of 1.3
        desktop.add(frame);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {}
    }
 
	public void closeFrame(JInternalFrame frame) {
		desktop.remove(frame);
		frame.dispose();
	}

	/**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
 
        //Create and set up the window.
        Application frame = new Application();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Display the window.
        frame.setVisible(true);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
