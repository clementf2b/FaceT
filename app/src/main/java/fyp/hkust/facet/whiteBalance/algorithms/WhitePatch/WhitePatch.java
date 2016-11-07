package fyp.hkust.facet.whiteBalance.algorithms.WhitePatch;


import android.graphics.Bitmap;

import java.util.Arrays;

import fyp.hkust.facet.whiteBalance.Convertor;
import fyp.hkust.facet.whiteBalance.partialConversions.Linearization1D;
import fyp.hkust.facet.whiteBalance.partialConversions.MatrixMultiplication1D;


/**
 * Created by Vladimira Hezelova on 15. 3. 2015.
 * <p/>
 * Image Chromatic Adapatation - White Patch (WP) Method
 */
public class WhitePatch extends Convertor {

    private Bitmap selectedWhite;

    private MatrixMultiplication1D matrixMultiplication1DInstance;
    private Linearization1D linearization1DInstance;

    private float[][] scalingMatrix;

    /**
     * Konsturktor WhitePatch
     * @param image bitmapa povodneho obrazku
     * @param selectedWhite bitmapa vybranej bielej oblasti v obrazku
     */
    public WhitePatch(Bitmap image, Bitmap selectedWhite) {
        super(image);
        this.selectedWhite = selectedWhite;

        this.matrixMultiplication1DInstance = new MatrixMultiplication1D();
        this.linearization1DInstance = new Linearization1D();
        setScalingMatrix();
        balanceWhite();
    }

    /**
     * Nastavi sa skalovacia matica (scalingMatrix) podielom idealnej bielej [255,255,255]
     * a medianom z vybranej bielej uzivatelom
     */
    public void setScalingMatrix() {
        int median = median(selectedWhite);

        float[] rgbRealWhite = getRGBFromValue(median, new float[3]);
        float[] lmsRealWhite = conversionsToXYZ(rgbRealWhite);
        lmsRealWhite = conversionsToLMS(lmsRealWhite, new float[3]);

        float[] lmsIdealWhite = conversionsToXYZ(new float[]{255, 255, 255});
        lmsIdealWhite = conversionsToLMS(lmsIdealWhite, new float[3]);

        float kL = lmsIdealWhite[0] / lmsRealWhite[0];
        float kM = lmsIdealWhite[1] / lmsRealWhite[1];
        float kS = lmsIdealWhite[2] / lmsRealWhite[2];

        scalingMatrix = new float[][]{{kL, 0.0f, 0.0f}, {0.0f, kM, 0.0f}, {0.0f, 0.0f, kS}};
    }

    /**
     * Odstranenie neprirodzeneho odtiena: konveriza medzi priestormi
     * a pouzitie skalovacej matice
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @param outRGB pole s troma hodnotami(kanalmi) pouzite, aby sa neprepisovali hodnoty
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    @Override
    public float[] removeColorCast(float[] pixelData, float[] outRGB){
        pixelData = conversionsToXYZ(pixelData);
        pixelData = conversionsToLMS(pixelData, outRGB);
        pixelData = matrixMultiplication1DInstance.multiply(scalingMatrix, pixelData);
        pixelData = conversionsToXYZ2(pixelData);
        pixelData = conversionToRGB(pixelData, outRGB);
        return pixelData;
    }

    /**
     * Konveriza z priestoru RGB do XYZ
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    private float[] conversionsToXYZ(float[] pixelData) {
        pixelData = linearization1DInstance.normalize(pixelData);
        pixelData = linearization1DInstance.linearize(pixelData);
        pixelData = matrixMultiplication1DInstance.multiply(MatrixMultiplication1D.MATRIX_RGBtoXYZ, pixelData);
        return pixelData;
    }

    /**
     * Konverzia z priestoru XYZ do LMS
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @param outRGB pole s troma hodnotami(kanalmi)
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    private float[] conversionsToLMS(float[] pixelData, float[] outRGB) {
        pixelData = matrixMultiplication1DInstance.multiply(MatrixMultiplication1D.MATRIX_XYZtoLMS, pixelData, outRGB);
        return pixelData;
    }

    /**
     * Konverzia z priestoru LMS do XYZ
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    private float[] conversionsToXYZ2(float[] pixelData) {
        pixelData = matrixMultiplication1DInstance.multiply(MatrixMultiplication1D.MATRIX_LMStoXYZ, pixelData);
        return pixelData;
    }

    /**
     * Konverzia z priestoru XYZ do RGB
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @param outRGB pole s troma hodnotami(kanalmi) pouzite, aby sa neprepisovali hodnoty
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    private float[] conversionToRGB(float[] pixelData, float[] outRGB) {
        pixelData = matrixMultiplication1DInstance.multiply(MatrixMultiplication1D.MATRIX_XYZtoRGB, pixelData, outRGB);
        pixelData = linearization1DInstance.nonLinearize(pixelData);
        pixelData = linearization1DInstance.nonNormalize(pixelData);
        return pixelData;
    }

    /**
     * Pocita median intenzit pixelov zo vstupnej bitmapy (vybrana oblast bielej),
     * cielom tejto metody je vyhnut sa nekorektnym vysledkom vyvaznia bielej zapricinenych
     * vyberom len jedneho pixelu, ktory by mohol byt zasumeny, ci inak degenerovany
     * @param img vstupna bitmapa (vybrana oblast bielej)
     * @return median intenzit pixelov zo vstupnej bitmapy
     */
    private int median(Bitmap img) {
        int height = img.getHeight();
        int width = img.getWidth();
        int[] m = new int[height*width];

        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(img != null && !img.isRecycled()) {
                    m[i * width + j] = img.getPixel(j, i);
                }
            }
        }
        Arrays.sort(m);

        int middle = m.length/2;
        if (m.length%2 == 1) {
            return m[middle];
        } else {
            return (m[middle-1] + m[middle]) / 2;
        }
    }
}
