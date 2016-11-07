package fyp.hkust.facet.whiteBalance.algorithms.grayWorld;

import android.graphics.Bitmap;

import fyp.hkust.facet.whiteBalance.Convertor;
import fyp.hkust.facet.whiteBalance.partialConversions.Linearization1D;
import fyp.hkust.facet.whiteBalance.partialConversions.MatrixMultiplication1D;


/**
 * Created by Vladimira Hezelova on 25. 4. 2015.
 */
public class GrayWorld extends Convertor {

    private Bitmap originalBitmap;

    private MatrixMultiplication1D matrixMultiplication1DInstance;
    private Linearization1D linearization1DInstance;

    private float[][] scalingMatrix;

    /**
     * Konstruktor GrayWorld
     * @param image bitmapa povodneho obrazku
     */
    public GrayWorld(Bitmap image) {
        super(image);
        this.originalBitmap = image;

        this.matrixMultiplication1DInstance = new MatrixMultiplication1D();
        this.linearization1DInstance = new Linearization1D();
        setScalingMatrix();
        balanceWhite();
    }

    /**
     * Nastavi sa skalovacia matica (scalingMatrix)
     */
    public void setScalingMatrix() {
        Average average = new Average(originalBitmap);
        scalingMatrix = average.getScalingMatrix();
    }

    /**
     * Odstranenie neprirodzeneho odtiena: normalizaciou, pouzitim skalovacej matice
     * a navratom z normalizacie
     * @param pixelData pole s troma hodnotami(kanalmi)
     * @param outRGB
     * @return konvertovany pixel zlozeny z troch kanalov
     */
    @Override
    public float[] removeColorCast(float[] pixelData, float[] outRGB) {
        pixelData = linearization1DInstance.normalize(pixelData);
        pixelData = matrixMultiplication1DInstance.multiply(scalingMatrix, pixelData);
        pixelData = linearization1DInstance.nonNormalize(pixelData);
        return pixelData;
    }
}
