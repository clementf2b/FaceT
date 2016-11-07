package fyp.hkust.facet;

/**
 * Created by ClementNg on 1/10/2016.
 */

public class Product {

    private String title;
    private String desc;
    private String image;
    private String username;

    public Product()
    {

    }

    public Product(String title, String desc, String image,String username) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.username = username;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
