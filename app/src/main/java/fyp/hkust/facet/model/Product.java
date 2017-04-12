package fyp.hkust.facet.model;

import java.util.ArrayList;

/**
 * Created by ClementNg on 1/10/2016.
 */

public class Product {

    private String productName;
    private String brandID;
    private String description;
    private String productImage;
    private Long colorNo;
    private String category;
    private String uid;
    private Long releaseDate;
    private int validate;

    public Product(String productName, String brandID, String description, String productImage, Long colorNo, String category, String uid, Long releaseDate, int validate) {
        this.productName = productName;
        this.brandID = brandID;
        this.description = description;
        this.productImage = productImage;
        this.colorNo = colorNo;
        this.category = category;
        this.uid = uid;
        this.releaseDate = releaseDate;
        this.validate = validate;
    }

    public Product(String productName, String brandID, String description, String productImage, Long colorNo, String category, String uid, Long releaseDate) {
        this.productName = productName;
        this.brandID = brandID;
        this.description = description;
        this.productImage = productImage;
        this.colorNo = colorNo;
        this.category = category;
        this.uid = uid;
        this.releaseDate = releaseDate;
    }

    public Product()
    {

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

    public Long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getValidate() {
        return validate;
    }

    public void setValidate(int validate) {
        this.validate = validate;
    }

}
