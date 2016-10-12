package fyp.hkust.facet.whiteBalance.algorithms.improvedWP;

import android.graphics.Bitmap;

import java.util.Random;

import fyp.hkust.facet.whiteBalance.Convertor;

/**
* Created by Vladimira Hezelova on 20. 8. 2015.
        */
public class ImprovedWP extends Convertor {

    private Bitmap originalBitmap;

    private float[] illuminationEstimation;

    /**
     * Konsturktor ImprovedWP
     * @param image bitmapa povodneho obrazku
     */
    public ImprovedWP(Bitmap image) {
        super(image);
        this.originalBitmap = image;
        conversion();
    }

    /**
     * Vyvazenie bielej algoritmom ImprovedWP (Improved White Patch)
     * n: nahodna vzorka n pixelov
     * m: pouzije sa m takychto vzoriek
     * performIlluminationEstimation: zisti sa Illuminant
     * balanceWhite: ostrani sa neprirodzeny odtien(vyvazi biela)
     */
    public void conversion() {
        int n = 50;
        int m = 10;
        performIlluminationEstimation(n, m);
        balanceWhite();
    }

    /**
     * Pocita Illuminant(farbu zdroja osvetlenia)
     * @param n pocet pixelov nahodnej vzorky
     * @param m pocet nahodnych vzoriek
     */
    public void performIlluminationEstimation(int n, int m) {

        float[] result = new float[]{0,0,0,0};
        float[] max = new float[]{0,0,0,0};

        float start = 0;
        float end = 1;

        int row;
        int col;

        float p1;
        float p2;

        Random randD1 = new Random();
        Random randD2 = new Random();
        float randomfloat1;
        float randomfloat2;

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        for(int i = 0; i < m; i++) {
            max[0] = 0;
            max[1] = 0;
            max[2] = 0;
            for(int j = 0; j < n; j++) {
                randomfloat1 = 1 * randD1.nextFloat();
                p1 = start + (randomfloat1 * (end - start));

                randomfloat2 = 1 * randD2.nextFloat();
                p2 = start + (randomfloat2 * (end - start));

                row=(int)((height-1)*p1);
                col=(int)((width-1)*p2);

                if(originalBitmap != null && !originalBitmap.isRecycled()) {
                    int value = originalBitmap.getPixel(col, row);
                    float[] pixelData = new float[3];
                    pixelData = getRGBFromValue(value, pixelData);
                    for(int k = 0; k < 3; k++) {
                        if(max[k] < pixelData[k]) {
                            max[k] = pixelData[k];
                        }
                    }
                }
            }
            result[0] += max[0];
            result[1] += max[1];
            result[2] += max[2];

        }

        float sum = result[0]*result[0]+result[1]*result[1]+result[2]*result[2];
        sum /= 3;
        sum = (float) Math.sqrt(sum);

        result[0] /= sum;
        result[1] /= sum;
        result[2] /= sum;

        illuminationEstimation = result;
    }

    /**
     * Odstranenie neprirodzeneho odtiena pomocou vypocitaneho Illumianntu
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @param outRGB
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    @Override
    public float[] removeColorCast(float[] pixelData, float[] outRGB){

        for (int k=0;k<3;++k){
            pixelData[k]/=illuminationEstimation[k];
            if(pixelData[k] > 255) {
                pixelData[k] = 255;
            }
        }
        return pixelData;
    }
}
