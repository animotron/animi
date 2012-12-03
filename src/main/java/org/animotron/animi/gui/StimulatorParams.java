/*
 *  Copyright (C) 2012 The Animo Project
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
package org.animotron.animi.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import javax.swing.*;

import org.animotron.animi.InitParam;
import org.animotron.animi.Params;
import org.animotron.animi.RuntimeParam;
import org.animotron.animi.simulator.StaticStimulator;
import org.animotron.animi.simulator.Stimulator;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class StimulatorParams extends JInternalFrame {
	
	private static final long serialVersionUID = -2223763417833552625L;
	
	JPanel panel;
	boolean readOnly;

	public StimulatorParams(final Application app, Stimulator stimulator) {
	    super("Stimulator params",
	            false, //resizable
	            false, //closable
	            false, //maximizable
	            false);//iconifiable
	    
	    readOnly = false;
	    
	    panel = new JPanel();
	    
	    GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        
        scan(gbc, null, stimulator);
		
		JButton btInit = new JButton("Apply");
		btInit.setEnabled(!readOnly);
		btInit.setMnemonic(KeyEvent.VK_A);
        btInit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});

        JButton btCancel = new JButton("Cancel");
        btCancel.setMnemonic(KeyEvent.VK_C);
        btCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.closeFrame(StimulatorParams.this);
			}
		});
		
//        Insets insets = btInit.getInsets();
//        insets.left = btInit.getPreferredSize().width;
//        EmptyBorder border = new EmptyBorder(insets);

        gbc.gridy++;
        gbc.gridx = 1;
        panel.add(btInit, gbc);

        gbc.gridx++;
        panel.add(btCancel, gbc);
        
		setLocation(100, 100);
		
		setContentPane(panel);
		
		pack();
	}
	
	private void scan(GridBagConstraints gbc, Field f, Object _obj) {
		Object obj = _obj;
		if (f != null)
			try {
				f.setAccessible(true);
				obj = f.get(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		Class<?> clazz = obj.getClass();
		
		if (clazz.isArray()) {
			Object[] objs = ((Object[])obj);
			for (int i = 0; i < objs.length; i++) {
				Object o = objs[i];
				addSep(gbc, o.toString());
				scan(gbc, null, o);
			}
			return;
		}

		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
//			System.out.println(field.getName());
			if (field.isAnnotationPresent(InitParam.class) || field.isAnnotationPresent(RuntimeParam.class)) {
				addField(gbc, field, obj);
			
			} else if (field.isAnnotationPresent(Params.class)) {
//				System.out.println("# "+field.getName());
				addSep(gbc, field.getName());
				scan(gbc, field, obj);
			}
		}
	}

	private void addSep(GridBagConstraints gbc, String name) {
		JLabel label = new JLabel(name);

        gbc.gridy++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(label, gbc);
        gbc.gridwidth = 1;
	}

	private void addField(GridBagConstraints gbc, final Field f, final Object obj) {
		if (f.getType() == boolean.class) {
	        final JCheckBox chBox = new JCheckBox(getName(f), getBooleanValue(f, obj));
	        
	        if (f.isAnnotationPresent(InitParam.class)) {
	        	chBox.setEnabled(!readOnly);
	        }
	        
	        chBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					f.setAccessible(true);
					try {
						f.set(obj, chBox.isSelected());
					} catch (Exception e1) {
						chBox.setSelected(getBooleanValue(f, obj));
					}
				}
			});
	        
	        gbc.gridy++;
	        gbc.gridx = 1;
	        panel.add(chBox, gbc);
			
		} else {
	
	        final JTextField text = new JTextField(getValue(f, obj));
	        
	        if (f.isAnnotationPresent(InitParam.class)) {
	        	text.setEditable(!readOnly);
	        }
	        
	        text.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					Class<?> type = f.getType();
					if (int.class == type) {
						f.setAccessible(true);
						try {
							f.set(obj, Integer.valueOf(text.getText()));
						} catch (Exception e1) {
							text.setText(getValue(f, obj));
						}
					} else if (double.class == type) {
							f.setAccessible(true);
							try {
								f.set(obj, Double.valueOf(text.getText()));
							} catch (Exception e1) {
								text.setText(getValue(f, obj));
							}
					} else {
						System.out.println("unknown type "+type);
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			JLabel label = new JLabel(getName(f));

	        gbc.gridy++;
	        gbc.gridx = 1;
	        panel.add(label, gbc);

	        gbc.gridx++;
	        panel.add(text, gbc);
		}
	}

	private String getName(Field f) {
		return f.getName();
//		return f.getAnnotation(RuntimeParam.class).name();
	}

	private Boolean getBooleanValue(Field f, Object obj) {
		try {
			f.setAccessible(true);
			return Boolean.valueOf((Boolean) f.get(obj));
		} catch (Exception e) {
		}
		return false;
	}

	private String getValue(Field f, Object obj) {
		try {
			f.setAccessible(true);
			return f.get(obj).toString();
		} catch (Exception e) {
		}
		return "???";
	}
}
