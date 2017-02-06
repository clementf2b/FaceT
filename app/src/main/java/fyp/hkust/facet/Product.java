package fyp.hkust.facet;

/**
 * Created by ClementNg on 1/10/2016.
 */

public class Product {

    private String title;
    private String brand;
    private String desc;
    private String image;
    private String username;
    private String uid;

    public Product()
    {

    }

    public Product(String title,String brand, String desc, String image,String username,String uid) {
        this.title = title;
        this.brand = brand;
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.uid = uid;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
