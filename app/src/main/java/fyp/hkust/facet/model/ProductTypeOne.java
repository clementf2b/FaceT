package fyp.hkust.facet.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by ClementNg on 1/10/2016.
 */

public class ProductTypeOne {

    private String productName;
    private String brandID;
    private String description;
    private String productImage;
    private Long colorNo;
    private String category;
    private String uid;
    private ArrayList<String> color;
    private Long releaseDate;

    public ProductTypeOne()
    {

    }

    public ProductTypeOne(String productName, String brandID, String description, String productImage, Long colorNo, String category, String uid, ArrayList<String> color, Long releaseDate) {
        this.productName = productName;
        this.brandID = brandID;
        this.description = description;
        this.productImage = productImage;
        this.colorNo = colorNo;
        this.category = category;
        this.uid = uid;
        this.color = color;
        this.releaseDate = releaseDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrandID() {
        return brandID;
    }

    public void setBrandID(String brandID) {
        this.brandID = brandID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Long getColorNo() {
        return colorNo;
    }

    public void setColorNo(Long colorNo) {
        this.colorNo = colorNo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getColor() {
        return color;
    }

    public void setColor(ArrayList<String> color) {
        this.color = color;
    }

    public Long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Long releaseDate) {
        this.releaseDate = releaseDate;
    }
}
