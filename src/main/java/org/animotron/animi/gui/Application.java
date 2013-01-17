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
package org.animotron.animi.gui;

import static org.animotron.animi.cortex.MultiCortex.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.animotron.animi.Imageable;
import org.animotron.animi.cortex.*;
import org.animotron.animi.simulator.*;
import org.jocl.cl_platform_id;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Application extends JFrame {
	
	private static final long serialVersionUID = 3243253015790558286L;
	
	public static Application _ = null;

	public MultiCortex cortexs = null;
	
    public long fps;

    JDesktopPane desktop;

	private JMenuBar menuBar;
	
	public JLabel count;
	
    String stimulatorClass = "org.animotron.animi.simulator.StimulatorAnime";
    Stimulator stimulator = null;
	
	private Application() {
		
		_ = this;
		
		setTitle("Animi");
		
		Container c = getContentPane();
		setLayout(new BorderLayout());
		
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                  screenSize.width  - inset*2,
                  screenSize.height - inset*2);
 
        desktop = new JDesktopPane();
        
        setJMenuBar(menuBar = createMenuBar());
        
        c.add(createToolBar(),BorderLayout.NORTH);
        c.add(desktop, BorderLayout.CENTER);
        c.add(createStatusBar(),BorderLayout.SOUTH);
 
        //Make dragging a little faster but perhaps uglier.
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stop();
            }

			@Override
            public void windowDeiconified(WindowEvent e) {
//                run();
            }

            @Override
            public void windowIconified(WindowEvent e) {
//                pause();
            }
        });

//		camView = new WebcamPanel();
//        add(camView, CENTER);
//
        setBounds(0, 0, 800, 600);
        setLocationByPlatform(true);
	}
	
    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
 
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);
 
        JMenuItem menuItem = new JMenuItem("Quit");
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
 
        menu = new JMenu("Stimulator");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);

        String[][] sets = 
    		new String[][] {
        		new String[] {"Webcamera", "org.animotron.animi.simulator.StimulatorWebcam"},
        		new String[] {"Image", "org.animotron.animi.simulator.StimulatorImage"},
        		new String[] {"Static", "org.animotron.animi.simulator.StimulatorStatic"},
        		new String[] {"Anime", "org.animotron.animi.simulator.StimulatorAnime"}
			};
        
        ButtonGroup group = new ButtonGroup();
        for (final String[] set : sets) {
	        menuItem = new JRadioButtonMenuItem(set[0]);
	        menuItem.setSelected(set[1] == stimulatorClass);
	        menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stimulatorClass = set[1];
					createStimulator();
				}
	        });
	        menu.add(menuItem);
	        group.add(menuItem);
        }

        return menuBar;
    }
    
    JMenu windowsMenu = null;
    protected void createWindowsMenuBar(JMenuBar menuBar) {
    	if (windowsMenu != null) {
    		windowsMenu.removeAll();
    	} else {
	        windowsMenu = new JMenu("Windows");
	        windowsMenu.setMnemonic(KeyEvent.VK_W);
	        menuBar.add(windowsMenu);
    	}
    	
    	addMenu(windowsMenu, stimulator);
 
        for (CortexZoneSimple zone : cortexs.zones) {
        	if (zone instanceof CortexZoneComplex) {
        		JMenu menu = new JMenu(zone.toString());
    	        windowsMenu.add(menu);

    	        CortexZoneComplex z = (CortexZoneComplex) zone;

    	        addMenu(menu, z);
    	        addMenu(menu, z.getCRF());
    	        addMenu(menu, z.getRRF());
			} else {
				addMenu(windowsMenu, zone);
			}
        }
    }
    
    private void addMenu(JMenu menu, final Imageable imageable) {
        JMenuItem menuItem = new JMenuItem(imageable.getImageName());
        menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		    	createFrame(imageable);
			}
		});
        menu.add(menuItem);
    }
    
    JToolBar bar;
    
    protected JToolBar createToolBar() {
    	if (bar == null) {
    		bar = new JToolBar();
    	} else {
    		bar.removeAll();
    	}
    	
        JButton button = new JButton("Load");
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					final JFileChooser fc = new JFileChooser();
					
					int returnVal = fc.showOpenDialog(Application.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            
			            cortexs = MultiCortex.load(Application.this, file);
						
			        	createViews();

						run();
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
        bar.add(button);
        
        button = new JButton("Save");
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cortexs != null) {
					final JFileChooser fc = new JFileChooser();
					
					int returnVal = fc.showSaveDialog(Application.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();

						int was = MODE;
						if (was == RUN)
							stop();
						
						//cortexs.prepareForSerialization();
						try {
							if (was >= STEP) Thread.sleep(1000);
							
							BufferedWriter out = new BufferedWriter(new FileWriter(file));
							cortexs.save(out);
							out.close();
							
							System.out.println("saved.");
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						if (was == RUN)
							run();
					}
				}
			}
		});
        bar.add(button);
        
        bar.addSeparator();

        button = new JButton("Stimulator");
//        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
//        button.setMnemonic(KeyEvent.VK_I);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StimulatorParams form = new StimulatorParams(Application.this, stimulator);
		    	form.setVisible(true);
		        desktop.add(form);
		        try {
		        	form.setSelected(true);
		        } catch (java.beans.PropertyVetoException e1) {}
			}
        });
        bar.add(button);

        button = new JButton("Next");
        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        button.setMnemonic(KeyEvent.VK_N);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stimulator != null) {
					stimulator.reset();
//					cortexs.retina.resetShift();
				}
			}
        });
        bar.add(button);

        bar.addSeparator();

        button = new JButton("Init");
        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
        button.setMnemonic(KeyEvent.VK_I);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				init();
			}
        });
        bar.add(button);
        
        bar.addSeparator();

        button = new JButton("Run");
        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        button.setMnemonic(KeyEvent.VK_R);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				run();
			}
        });
        bar.add(button);

        button = new JButton("Step");
        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        button.setMnemonic(KeyEvent.VK_S);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				step();
			}
        });
        bar.add(button);

        button = new JButton("Stop");
        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        button.setMnemonic(KeyEvent.VK_P);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
        });
        bar.add(button);

        bar.addSeparator();

        button = new JButton("Refresh");
        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
        button.setMnemonic(KeyEvent.VK_F);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
        });
        bar.add(button);

        return bar;
    }

    private void addToBar() {
        bar.addSeparator();

        for (final CortexZoneSimple z : cortexs.zones) {
        	if (!(z instanceof CortexZoneComplex)) {
//	        	final JCheckBox chB_ = new JCheckBox("Saccade");
//	        	chB_.addActionListener(new ActionListener() {
//					
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						z.saccade = !z.saccade;
//						chB_.setSelected(z.saccade);
//					}
//		        });
//		        bar.add(chB_);
        		continue;
			}
        	final CortexZoneComplex zone = (CortexZoneComplex)z;
        	
        	final JLabel label = new JLabel(zone.toString());
	        bar.add(label);
        	
        	final JCheckBox chB = new JCheckBox("Active");
        	chB.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					zone.active = !zone.active;
					chB.setSelected(zone.isActive());
				}
	        });
	        bar.add(chB);

	        final JCheckBox chL = new JCheckBox("Learning");
	        chL.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					zone.learning = !zone.learning;
					chL.setSelected(zone.isLearning());
				}
	        });
	        bar.add(chL);
	        bar.addSeparator();
        }
    }

    protected JPanel createStatusBar() {
    	JPanel bar = new JPanel();
    	
    	bar.setBorder(new BevelBorder(BevelBorder.LOWERED));
    	bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
    	count = new JLabel("status");
    	count.setHorizontalAlignment(SwingConstants.LEFT);
    	bar.add(count);
    	
    	return bar;
    }
    
    private void init() {
//    	camView.resume();
    	//задание параметров зон коры и структуры связей
    	//BrainInit
    	//инициализация зон коры
    	//CortexInit
    	//Начальный сброс "хорошо - плохо"
    	if (cortexs == null)
    		cortexs = new MultiCortex(this);
    	
    	PFInitialization form = new PFInitialization(this, cortexs);
    	form.setVisible(true);
        desktop.add(form);
        try {
        	form.setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {}
    }
    
    protected void initialize(final cl_platform_id platform, final long deviceType) {
    	cortexs.init(platform, deviceType);
    	
    	createViews();
    }
    
    private void createStimulator() {
    	if (cortexs == null)
    		return;
    	
    	try {
			@SuppressWarnings("unchecked")
			Class<Stimulator> clazz = (Class<Stimulator>) Class.forName(stimulatorClass);
			Constructor<Stimulator> constructor = clazz.getConstructor(Application.class);
			
			stimulator = constructor.newInstance(this);
			
	    	createFrame(stimulator);
    	} catch (Exception e) {
		}
    }
    
    private void createViews() {
    	createStimulator();
    	
    	clearFrames();
    	
    	createToolBar();
    	
    	createWindowsMenuBar(menuBar);
    	addToBar();

    	createFrame(stimulator);
    }
    
    private synchronized void run() {
    	if (cortexs != null) {
			cortexs.start();
    	}
    }
    
    private synchronized void step() {
    	if (cortexs != null && MODE <= STEP) {
			MODE = STEP;
			cortexs.process();
    	}
    }

    private synchronized void stop() {
    	if (cortexs != null) {
    		cortexs.stop();
    	}
    }

    //Create a new internal frame.
    protected void createFrame(Imageable imageable) {
        Visualizer frame = new Visualizer( imageable );
        frame.refresh();
        frame.setVisible(true); //necessary as of 1.3
        desktop.add(frame);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {}
    }
    
    public void refresh() {
    	System.out.println("refresh");
        int count = desktop.getComponentCount();
        for (int i = 0; i < count; i++) {
            Component comp = desktop.getComponent(i);
            if (comp instanceof Visualizer) {
            	((Visualizer)comp).refresh();
            }
        }
    }

    private void clearFrames() {
    	desktop.removeAll();
    	Visualizer.reset();
    	desktop.repaint();
    }
 
	public void closeFrame(JInternalFrame frame) {
		frame.setVisible(false);
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

	public Stimulator getStimulator() {
		return stimulator;
	}
}
