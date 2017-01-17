package fyp.hkust.facet.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;

import static org.opencv.imgproc.Imgproc.INTER_LINEAR;

public class SwapFaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swap_face);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_swap_face_layout), fontType);
    }


    // Apply affine transform calculated using srcTri and dstTri to src
    void applyAffineTransform(Mat warpImage, Mat src, MatOfPoint2f srcTri, MatOfPoint2f dstTri)
    {
        // Given a pair of triangles, find the affine transform.
        Mat warpMat = Imgproc.getAffineTransform( srcTri, dstTri );
        // Apply the Affine Transform just found to the src image
        Imgproc.warpAffine( src, warpImage, warpMat, warpImage.size(), INTER_LINEAR);
    }

    // Calculate Delaunay triangles for set of points
// Returns the vector of indices of 3 points for each triangle
//    static void calculateDelaunayTriangles(Rect rect, MatOfPoint2f points, Vector<MatOfInt> delaunayTri){
//
//        // Create an instance of Subdiv2D
//        Subdiv2D subdiv = new Subdiv2D(rect);
//
//        // Insert points into subdiv
//        MatOfPoint2f::iterator it = points.begin();
//        Iterator it;
//        it = points.iterator();
//        for(  it != points.end(); it++)
//        subdiv.insert(*it);
//
//        MatOfFloat6 triangleList = new MatOfFloat6();
//        subdiv.getTriangleList(triangleList);
//        MatOfPoint2f pt = new MatOfPoint2f(3);
//        MatOfInt ind = new MatOfPoint2f(3);
//
//        for( size_t i = 0; i < triangleList.size(); i++ )
//        {
//            MatOfFloat6 t = triangleList[i];
//            pt[0] = MatOfPoint2f(t[0], t[1]);
//            pt[1] = MatOfPoint2f(t[2], t[3]);
//            pt[2] = MatOfPoint2f(t[4], t[5]);
//
//            if ( rect.contains(pt[0]) && rect.contains(pt[1]) && rect.contains(pt[2])){
//                for(int j = 0; j < 3; j++)
//                    for(size_t k = 0; k < points.size(); k++)
//                        if(Math.abs(pt[j].x - points[k].x) < 1.0 && Math.abs(pt[j].y - points[k].y) < 1)
//                            ind[j] = k;
//
//                delaunayTri.add(ind);
//            }
//        }
//
//    }

}
