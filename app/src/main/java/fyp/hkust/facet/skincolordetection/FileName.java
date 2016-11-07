package fyp.hkust.facet.skincolordetection;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Vladimira Hezelova on 8. 12. 2015.
 */
public class FileName {
    private String fileName;
    private String sDate;
    private String path;
    private String extension;

    /**
     * Konsturktor FileName
     * @param imagePath cesta k obrazku vybranemu uzivatelom pre vyvazenie bielej
     */
    public FileName(String imagePath) {
        Date date = new Date();
        sDate = new SimpleDateFormat("yyyyMMdd_hhmmss").format(date);

        path = new File(imagePath).getParent();

        String fileNameWithExtension = new File(imagePath).getName();
        fileName = sDate;
        extension = "jpeg";
        int pos = fileNameWithExtension.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileNameWithExtension.substring(0, pos);
            extension = fileNameWithExtension.substring(pos);
        }
    }

    /**
     * Vracia absolutnu cestu s novym nazvom obrazku,
     * tj. cesta k povodnemu obrazku + nazov obrazku + datum a cas + typ formatu
     * @return destinationFilename
     */
    public String getDestinationFilename() {
        String destinationFilename = path + File.separatorChar + fileName + sDate + extension;
        Log.i("destinationFilename", destinationFilename);
        return destinationFilename;
    }

    /**
     * Vracia nazov noveho obrazku, tj. nazov povodneho obrazku + datum a cas
     * @return
     */
    public String getNewFilename() {
        String newFilename = fileName + sDate;
        Log.i("newFilename", newFilename);
        return newFilename;
    }
}
