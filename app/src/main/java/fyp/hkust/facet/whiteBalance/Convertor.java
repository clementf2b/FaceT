package fyp.hkust.facet.whiteBalance;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Vladimira Hezelova on 6. 12. 2015.
 */
public abstract class Convertor {

    private Bitmap originalBitmap;
    private int width;
    private int height;
    private Bitmap convertedBitmap;
    private float[] outRGB;

    /**
     * Konsturktor Convertor pouzity pre algoritmy
     * @param bitmap bitmapa povodneho obrazku
     */
    public Convertor(Bitmap bitmap) {
        this.originalBitmap = bitmap;
        this.width = originalBitmap.getWidth();
        this.height = originalBitmap.getHeight();
        this.convertedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        this.outRGB = new float[3];
    }

    /**
     * Vyvazvoanie bielej volane z tried algoritmov. Nacita hodnotu pixelu z povodneho obrazku,
     * rozdeli ju na 3 RGB kanaly, odstrani neprirodzene odtiene metodou removeColorCast, ktoru ma
     * implementovanu kazdy algoritus zvlast, ziskanu hodnotu troch kanalov vlozi do jednej hodnoty int,
     * a to vlozi do novej bitmapy convertedBitmap
     */
    public void balanceWhite() {
        int value, i, j;
        float[] rgb = new float[3];
        long start = System.currentTimeMillis();

        for(i = 0; i < height; i++) {
            for(j = 0; j < width; j++) {
                if(originalBitmap != null && !originalBitmap.isRecycled()) {
                    value = originalBitmap.getPixel(j,i);
                    rgb = getRGBFromValue(value, rgb);
                    rgb = removeColorCast(rgb, outRGB);
                    convertedBitmap.setPixel(j, i, getValueFromRGB(rgb));
                }
            }
        }

        long end = System.currentTimeMillis();
        double time = (double) (end - start) / 1000;
        Log.i("balanceWhite", "time of conversions = " + time + "seconds");
    }

    /**
     * Odstrani neprirodzene odtiedne v obrazku. Kazdy algoritmus implementuje zvlast.
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @param outRGB pole s troma hodnotami(kanalmi) pouzite, aby sa neprepisovali hodnoty
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    public abstract float[] removeColorCast(float[] pixelData, float[] outRGB);

    /**
     * Ziska jednu hodnotu int bitovym posunom z pola troch floatov znazornujucich jednotlive kanaly RGB
     * @param rgb rgb[0] inenzita pixelu cerveneho kanalu,
     *            rgb[1] inenzita pixelu zeleneho kanalu,
     *            rgb[2] inenzita pixelu modreho kanalu
     * @return hodnotu pixelu v int
     */
    public int getValueFromRGB(float[] rgb) {
        int R = (int) rgb[0];
        int G = (int) rgb[1];
        int B = (int) rgb[2];
        return ((R & 0xFF) << 16) | ((G & 0xFF) << 8)  | ((B & 0xFF));
    }

    /**
     * Bitovym posunom ziska pole troch floatov znazornujucich jednotlive kanaly RGB z jednej hodnoty int
     * @param value intenzita pixelu v int
     * @param rgb pole rgb[3] do ktoreho sa vlozi vysledok
     * @return rgb[0] inenzita pixelu cerveneho kanalu,
     *          rgb[1] inenzita pixelu zeleneho kanalu,
     *          rgb[2] inenzita pixelu modreho kanalu
     */
    public float[] getRGBFromValue(int value, float rgb[]) {
        rgb[0] = (value >> 16) & 0xff; //red
        rgb[1] = (value >>  8) & 0xff; //green
        rgb[2] = (value      ) & 0xff;  //blue
        return rgb;
    }

    /**
     * Vracia konvertovanu bitmapu s vyvazenou bielou
     * @return convertedBitmap
     */
    public Bitmap getConvertedBitmap() {
        return convertedBitmap;
    }

}
