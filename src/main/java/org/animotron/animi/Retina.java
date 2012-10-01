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
package org.animotron.animi;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Retina {
	
	/* x */
	private int width;
	/* y */
	private int height;
	
	//Числовое представление сетчатки. Черно-белая картинка.
	private int preprocessing[][];
	
//	private int receptiveField[][] = null;
	private ReceptiveField[][] MSensPol;
	
	private OnOff onOff = new OnOff();
	
	public Retina(int width, int height) {
		this.width = width;
		this.height = height;
		
		preprocessing = new int[width][height];
	}
	
	public void initialize() {
		
	}
	
	private Layer NL = null;
	public void setNextLayer(Layer layer) {
		NL = layer;
		FillMSensPol();
	}
	
    // создание связей сенсорных полей
	private void FillMSensPol() {
		MSensPol = new ReceptiveField[NL.width()][NL.height()];

        double XScale = (width - onOff.sensPoLength()) / (double)NL.width();
        double YScale = (height - onOff.sensPoLength()) / (double)NL.height();

        int X, Y;
        int NC, NP;

        boolean fl = true;
        for (int ix = 0; ix < NL.width(); ix++) {
        	for (int iy = 0; iy < NL.height(); iy++) {

        		ReceptiveField mSensPol = new ReceptiveField();
                MSensPol[ix][iy] = mSensPol;
                mSensPol.centr = new int[onOff.NSensCentr()][2];
        		mSensPol.peref = new int[onOff.NSensPeref()][2];

                // распределение он и офф полей
                if (fl) {
                	mSensPol.type = 3;
                } else {
                	mSensPol.type = 3;
                }
                fl = !fl;

                X = (int)Math.round( ix * XScale + onOff.RSensPol() + 1 );
                Y = (int)Math.round( iy * YScale + onOff.RSensPol() + 1 );

                NC = 0;
                NP = 0;

                for (int i = 0; i < onOff.sensPoLength(); i++) {
                    for (int j = 0; j < onOff.sensPoLength(); j++) {

                    	switch (onOff.getType(i, j)) {

                        case 0:
                            break;
                        case 1:

                            mSensPol.peref[NP][0] = X - onOff.RSensPol() + i;
                            mSensPol.peref[NP][1] = Y - onOff.RSensPol() + j;

                            NP = NP + 1;
                            break;
                        case 2:

                        	mSensPol.centr[NC][0] = X - onOff.RSensPol() + i;
                        	mSensPol.centr[NC][1] = Y - onOff.RSensPol() + j;

                            NC = NC + 1;
                            break;
						default:
							break;
                		}
                    }
                }
        	}
        }
    }
	
	//constants
    //минимальное соотношение средней контрасности переферии и центра сенсорного поля, необходимое для активации
    //контрастность для темных элементов (0)
    public static double KContr1 = 1.45;
    //контрастность для светлых элементов (Level_Bright)
    public static double KContr2 = 1.15;
	//минимальная контрастность
    public static double KContr3 = 1.15;
	//(0..255)
    public static int Level_Bright = 100;
    public static int Lelel_min = 10;// * N_Col;

    public void process(BufferedImage image) {

    	//preprocessing
    	for (int x = 0; x < width; x++)
        	for (int y = 0; y < height; y++)
        		preprocessing[x][y] = Utils.calcGrey(image, x, y);
        
    	double SP, SC, SA;
        double K_cont;
        for (int ix = 0; ix < NL.width(); ix++) {
        	for (int iy = 0; iy < NL.height(); iy++) {

        		ReceptiveField mSensPol = MSensPol[ix][iy];

                NL.set(ix,iy,false);

                SC = 0;
                for (int j = 0; j < onOff.NSensCentr(); j++) {

                    SC += preprocessing[mSensPol.centr[j][0]][mSensPol.centr[j][1]];
                }

                SP = 0;
                for (int j = 0; j < onOff.NSensPeref(); j++) {

                    SP += preprocessing[mSensPol.peref[j][0]][mSensPol.peref[j][1]];
                }

                SA = ((SP + SC) / (onOff.NSensCentr() + onOff.NSensPeref()));

                K_cont = KContr1 + SA * (KContr2 - KContr1) / Level_Bright;

                if (K_cont < KContr3) K_cont = KContr3;

                SC = SC / onOff.NSensCentr();
                SP = SP / onOff.NSensPeref();

                if (SA > Lelel_min) {
                	switch (mSensPol.type) {
                    case 1:

                        if (SC / SP > K_cont)
                        	NL.set(ix,iy,true);

						break;

                    case 2:

                        if (SP / SC > K_cont)
                        	NL.set(ix,iy,true);

                        break;

                    case 3:

                        if (SC / SP > K_cont || SP / SC > K_cont) 
                        	NL.set(ix,iy,true);

                        break;
					default:
						break;
					}
                }
        	}
        }
    }
    
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}

    public BufferedImage getImage() {
    	
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int c = preprocessing[x][y];
                image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
            }
        }
        return image;
    }

}
