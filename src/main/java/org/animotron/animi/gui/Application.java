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
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.*;

import org.animotron.animi.Imageable;
import org.animotron.animi.cortex.*;
import org.animotron.animi.simulator.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Application extends JFrame {
	
	private static final long serialVersionUID = 3243253015790558286L;
	
	static Application _ = null;

//	private JMenuItem miInit = null;
//	private JMenuItem miRun = null;
//	private JMenuItem miPause = null;
//	private JMenuItem miResume = null;
//	private JMenuItem miStop = null;
	
	public static MultiCortex cortexs = null;
	
	JDesktopPane desktop;
	
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
//        createFrame(stimulator);
        
        setJMenuBar(createMenuBar());
        
        c.add(createToolBar(),BorderLayout.NORTH);
        c.add(desktop, BorderLayout.CENTER);
        c.add(createStatusBar(),BorderLayout.SOUTH);
 
        //Make dragging a little faster but perhaps uglier.
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                pause();
            }

			@Override
            public void windowDeiconified(WindowEvent e) {
                run();
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
//        menu = new JMenu("Control");
//        menu.setMnemonic(KeyEvent.VK_D);
//        menuBar.add(menu);
// 
//		addMenu(menu, "init", KeyEvent.VK_I, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//	            init();
//			}
//		});
//
//        miRun = addMenu(menu, "run", KeyEvent.VK_R, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//	            run();
//			}
//		});
//        miRun.setEnabled(false);
//
//        miPause = addMenu(menu, "pause", KeyEvent.VK_P, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//	            pause();
//			}
//		});
//        miPause.setEnabled(false);

        return menuBar;
    }
    
//    private JMenuItem addMenu(JMenu menu, String name, int key, ActionListener listener) {
//    	JMenuItem menuItem = new JMenuItem(name);
//        menuItem.setMnemonic(key);
//        menuItem.setAccelerator(
//    		KeyStroke.getKeyStroke(key, ActionEvent.ALT_MASK)
//		);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
//        
//        return menuItem;
//    }
    
    protected JToolBar createToolBar() {
    	JToolBar bar = new JToolBar();
    	
    	final Kryo kryo = new Kryo();
    	
        JButton button = new JButton("Load");
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Input input = new Input(new FileInputStream("file.bin"));
					cortexs = kryo.readObject(input, MultiCortex.class);
					input.close();
					
					createViews();

					run();
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
					boolean was = cortexs.active;
					pause();
					cortexs.prepareForSerialization();
					try {
						if (was) Thread.sleep(1000);
						
						Output output = new Output(new FileOutputStream("file.bin"));
						kryo.writeObject(output, cortexs);
						output.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
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

        button = new JButton("Pause");
        button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        button.setMnemonic(KeyEvent.VK_P);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pause();
			}
        });
        bar.add(button);

        return bar;
    }

    protected JPanel createStatusBar() {
    	JPanel bar = new JPanel();
    	
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
    	
    	createViews();

//    	camView.resume();

//    	miRun.setEnabled(true);
//    	
//    	miPause.setEnabled(false);
//    	miResume.setEnabled(false);
//    	
//    	miStop.setEnabled(false);
    }
    
    private void createViews() {
    	clearFrames();
        createFrame(stimulator);

        for (CortexZoneSimple zone : cortexs.zones) {
        	if (zone instanceof CortexZoneComplex) {
				CortexZoneComplex z = (CortexZoneComplex) zone;

				createFrame(z);
				createFrame(z.getCRF());
				createFrame(z.getRRF());
			} else
				createFrame(zone);
        }
    }
    
    private void run() {
    	if (cortexs != null) {
			cortexs.active = true;
	        stimulator.start();
			
//			miRun.setEnabled(false);
//			
//			miPause.setEnabled(true);
//			miResume.setEnabled(false);
//    	
//			miStop.setEnabled(true);
    	}
    }
    
    private void step() {
    	if (cortexs != null && !cortexs.active) {
			cortexs.active = true;
	        stimulator.prosess();
			cortexs.active = false;
    	}
    }

    private void pause() {
    	if (cortexs != null) {
			stimulator.pause();
			cortexs.active = false;
			
//			miPause.setEnabled(false);
//			miResume.setEnabled(true);
    	}
    }

	Stimulator stimulator = new Stimulator(
            Retina.WIDTH, Retina.HEIGHT,
            new Figure[] {
                    new HLineAnime(
                            Retina.WIDTH - 4, 0,
                            new int[][] {
                                    {Retina.WIDTH / 2, 2},
                                    {Retina.WIDTH / 2, Retina.HEIGHT - 3},
                                    {Retina.WIDTH / 2, 2}
                            }
                    ),

                    new HLineAnime(
                            Retina.WIDTH - 4, 0,
                            new int[][] {
                                    {Retina.WIDTH / 2, 3},
                                    {Retina.WIDTH / 2, Retina.HEIGHT - 2},
                                    {Retina.WIDTH / 2, 3}
                            }
                    )



//            		new HLineAnime(
//    				10, 0,
//    				new int[][] {
//    						{4, 4},
//    						{Retina.WIDTH - 8, 4},
//    						{Retina.WIDTH - 8, Retina.HEIGHT - 8},
//    						{4, Retina.HEIGHT - 8},
//    						{4, 4}
//    				}
//			),
//            		new VLineAnime(
//    				10, 0,
//    				new int[][] {
//    						{Retina.WIDTH - 8, 4},
//    						{Retina.WIDTH - 8, Retina.HEIGHT - 8},
//    						{4, Retina.HEIGHT - 8},
//    						{4, 4},
//    						{Retina.WIDTH - 8, 4},
//    				}
//			),
//            		new HLineAnime(
//    				10, 0,
//    				new int[][] {
//    						{Retina.WIDTH - 8, Retina.HEIGHT - 8},
//    						{4, Retina.HEIGHT - 8},
//    						{4, 4},
//    						{Retina.WIDTH - 8, 4},
//    						{Retina.WIDTH - 8, Retina.HEIGHT - 8}
//    				}
//			),
//        		new VLineAnime(
//    				10, 0,
//    				new int[][] {
//    						{4, Retina.HEIGHT - 8},
//    						{4, 4},
//    						{Retina.WIDTH - 8, 4},
//    						{Retina.WIDTH - 8, Retina.HEIGHT - 8},
//    						{4, Retina.HEIGHT - 8}
//    				}
//			)

            		
//            		new LineAnime(
//            				10, 0,
//            				new int[][] {
//            						{4, 4},
//            						{Retina.WIDTH - 8, 4},
//            						{Retina.WIDTH - 8, Retina.HEIGHT - 8},
//            						{4, Retina.HEIGHT - 8},
//            						{4, 4}
//            				}
//    				),
//            		new LineAnime(
//            				10, -.03,
//            				new int[][] {
//            						{Retina.WIDTH - 8, Retina.HEIGHT - 8},
//            						{4, Retina.HEIGHT - 8},
//            						{4, 4},
//            						{Retina.WIDTH - 8, 4},
//            						{Retina.WIDTH - 8, Retina.HEIGHT - 8}
//            				}
//    				)
//                    new LineAnime(
//                            30, -0.03,
//                            new int[][] {
//                                    {40, 40},
//                                    {Retina.WIDTH - 40, 40},
//                                    {Retina.WIDTH - 40, Retina.HEIGHT - 40},
//                                    {40, Retina.HEIGHT - 40},
//                                    {40, 40}
//                            }
//                    ),
//                    new LineAnime(
//                            30, 0.03,
//                            new int[][] {
//                                    {Retina.WIDTH - 40, 40},
//                                    {Retina.WIDTH - 40, Retina.HEIGHT - 40},
//                                    {40, Retina.HEIGHT - 40},
//                                    {40, 40},
//                                    {Retina.WIDTH - 40, 40},
//                            }
//                    ),
//                    new LineAnime(
//                            30, -0.07,
//                            new int[][] {
//                                    {Retina.WIDTH - 40, Retina.HEIGHT - 40},
//                                    {40, Retina.HEIGHT - 40},
//                                    {40, 40},
//                                    {Retina.WIDTH - 40, 40},
//                                    {Retina.WIDTH - 40, Retina.HEIGHT - 40}
//                            }
//                    ),
//                    new LineAnime(
//                            30, 0.07,
//                            new int[][] {
//                                    {40, Retina.HEIGHT - 40},
//                                    {40, 40},
//                                    {Retina.WIDTH - 40, 40},
//                                    {Retina.WIDTH - 40, Retina.HEIGHT - 40},
//                                    {40, Retina.HEIGHT - 40}
//                            }
//                    ),
//                    new OvalAnime(30,
//                            new int[][] {
//                                    {50, 50},
//                                    {Retina.WIDTH - 50, Retina.HEIGHT - 50},
//                                    {Retina.WIDTH - 50, 50},
//                                    {50, Retina.HEIGHT - 50},
//                                    {50, 50}
//                            }
//                    ),
//                    new RectAnime(
//                            50, 0.05,
//                            new int[][] {
//                                    {40, 40},
//                                    {40, Retina.HEIGHT - 40},
//                                    {Retina.WIDTH - 40, Retina.HEIGHT - 40},
//                                    {Retina.WIDTH - 40, 40},
//                                    {40, 40}
//                            }
//                    )
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
}
