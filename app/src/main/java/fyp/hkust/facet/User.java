package fyp.hkust.facet;

/**
 * Created by ClementNg on 22/11/2016.
 */
public class User {

    public String image;
    public String name;

    public User(String image,String name) {
        this.image = image;
        this.name = name;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
