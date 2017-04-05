package fyp.hkust.facet.model;

/**
 * Created by bentley on 5/4/2017.
 */

public class Shop {

    private String shopName, shopAddress, shopDistrict, shopImage;
    private Double lantitide, longtitude;

    public Shop() {
    }

    public Shop(String shopName, String shopAddress, String shopDistrict, String shopImage, Double lantitide, Double longtitude) {
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.shopDistrict = shopDistrict;
        this.shopImage = shopImage;
        this.lantitide = lantitide;
        this.longtitude = longtitude;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getShopDistrict() {
        return shopDistrict;
    }

    public void setShopDistrict(String shopDistrict) {
        this.shopDistrict = shopDistrict;
    }

    public Double getLantitide() {
        return lantitide;
    }

    public void setLantitide(Double lantitide) {
        this.lantitide = lantitide;
    }

    public Double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(Double longtitude) {
        this.longtitude = longtitude;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }
}
