package fyp.hkust.facet;

/**
 * Created by ClementNg on 1/10/2016.
 */

public class Product {

    private String postId;
    private String title;
    private String desc;
    private String image;
    private String username;

    private String uid;

    public Product()
    {

    }

    public Product(String postId,String title, String desc, String image,String username,String uid) {
        this.postId = postId;
        this.title = title;
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
