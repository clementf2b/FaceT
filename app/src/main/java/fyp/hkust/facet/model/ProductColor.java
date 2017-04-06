package fyp.hkust.facet.model;

import java.util.ArrayList;

/**
 * Created by ClementNg on 6/4/2017.
 */

public class  ProductColor {

    private Long colorNo;
    private ArrayList<ArrayList<String>> color;

    public ProductColor() {
    }

    public ProductColor(Long colorNo, ArrayList<ArrayList<String>> color) {

        this.colorNo = colorNo;
        this.color = color;
    }

    public Long getColorNo() {
        return colorNo;
    }

    public void setColorNo(Long colorNo) {
        this.colorNo = colorNo;
    }

    public ArrayList<ArrayList<String>> getColor() {
        return color;
    }

    public void setColor(ArrayList<ArrayList<String>> color) {
        this.color = color;
    }
}
