package fyp.hkust.facet.model;

/**
 * Created by ClementNg on 4/4/2017.
 */

public class Brand {

    private String brand;
    private String description;
    private String image;

    public Brand(String brand, String description, String image) {
        this.brand = brand;
        this.description = description;
        this.image = image;
    }

    public Brand()
    {

    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
