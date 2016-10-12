package fyp.hkust.facet.whiteBalance.algorithms.histogramStretching;

import android.graphics.Bitmap;
import android.util.Log;

import fyp.hkust.facet.whiteBalance.Convertor;


/**
 * Created by Vladimira Hezelova on 22. 10. 2015.
 */
public class HistogramStretching extends Convertor {

    private int[] low;
    private int[] high;
    private float min;
    private float max;
    private float[] ratio;

    private Bitmap originalBitmap;

    /**
     * Konsturktor HistogramStretching
     * @param bitmap bitmapa povodneho obrazku
     */
    public HistogramStretching(Bitmap bitmap) {
        super(bitmap);
        this.originalBitmap = bitmap;
        this.min = 0;
        this.max = 255;
        this.low = new int[3];
        this.high = new int[3];
        this.ratio = new float[3];
        setScalingCoefficients();
        balanceWhite();
    }

    /**
     * Zisti sa low(najnizsia) a high(najvyssia) intenzita kazdeho kanalu,
     * vypocita sa pomer ratio[canal] = (max - min) / (high[canal] - low[canal]),
     * kde max je 255 a min je 0
     */
    public void findBoundary() {
        int[][] histogram = getHistogram();

        long start = System.currentTimeMillis();

        int percentil = (int) (originalBitmap.getWidth()*originalBitmap.getHeight() * 0.05);
        int intensity = 0;
        int number = 0;

        for(int canal = 0; canal < 3; canal++) {
            while(number < percentil) {
                number += histogram[canal][intensity];
                intensity++;
            }
            low[canal] = intensity-1;

            intensity = 255;
            number = 0;

            while(number < percentil) {
                number += histogram[canal][intensity];
                intensity--;
            }
            high[canal] = intensity+1;

            intensity = 0;
            number = 0;
            ratio[canal] = (max - min) / (high[canal] - low[canal]);
        }

        long end = System.currentTimeMillis();
        double time = (double) (end - start) / 1000;
        Log.i("Find boundary", "time of conversions = " + time + "seconds");
    }

    /**
     * Nastavia sa skalovacie koeficienty
     */
    public void setScalingCoefficients() {
        long start = System.currentTimeMillis();

        findBoundary();

        long end = System.currentTimeMillis();
        double time = (double) (end - start) / 1000;
        Log.i("setScalingCoefficients", "time of conversions = " + time + "seconds");
    }

    /**
     * Pocita histogram intenzit pixelov v obrazku
     * @return pole histogram[kanal][intenzita<0,255>]=pocet pixelov
     */
    public int[][] getHistogram() {
        long start = System.currentTimeMillis();

        int[][] histogram = new int[3][256];

        int value;
        int intensity[] = new int[3];
        int height = originalBitmap.getHeight();
        int width = originalBitmap.getWidth();

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                if(originalBitmap != null && !originalBitmap.isRecycled()) {
                    value = originalBitmap.getPixel(j,i);

                    intensity[0] = (value >> 16) & 0xff; //red
                    intensity[1] = (value >>  8) & 0xff; //green
                    intensity[2] = (value      ) & 0xff;  //blue

                    histogram[0][intensity[0]]++;
                    histogram[1][intensity[1]]++;
                    histogram[2][intensity[2]]++;
                }
            }
        }
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / 1000;
        Log.i("getHistogran", "time of conversions = " + time + "seconds");

        return histogram;
    }

    /**
     * Odstranenie neprirodzeneho odtiena algoritmom HistogramStretching (percentilovy)
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @param outRGB
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    @Override
    public float[] removeColorCast(float[] pixelData, float[] outRGB) {

        for(int i = 0; i < 3; i++) {
            if(pixelData[i] < low[i]) {
                pixelData[i] = min;
            } else if(pixelData[i] > high[i]) {
                pixelData[i] = max;
            } else {
                pixelData[i] = (pixelData[i] - low[i]) * ratio[i] + min;
            }
        }
        return pixelData;
    }
}
