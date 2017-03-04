package fyp.hkust.facet.model;

/**
 * Created by ClementNg on 4/3/2017.
 */

public class Comment {

    private String comment;
    private String uid_image;
    private String uid;
    private String comment_image;
    private String comment_time;
    private String username;

    public Comment() {
    }

    public Comment(String comment, String uid_image, String uid, String comment_image, String comment_time, String username) {
        this.comment = comment;
        this.uid_image = uid_image;
        this.uid = uid;
        this.comment_image = comment_image;
        this.comment_time = comment_time;
        this.username = username;
    }

    public Comment(String comment, String uid_image, String uid, String comment_time, String username) {
        this.comment = comment;
        this.uid_image = uid_image;
        this.uid = uid;
        this.comment_time = comment_time;
        this.username = username;
    }

    public Comment(String comment, String uid, String comment_time, String username) {
        this.comment = comment;
        this.uid = uid;
        this.comment_time = comment_time;
        this.username = username;
    }

    public String getUid_image() {
        return uid_image;
    }

    public void setUid_image(String uid_image) {
        this.uid_image = uid_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment_image() {
        return comment_image;
    }

    public void setComment_image(String comment_image) {
        this.comment_image = comment_image;
    }

    public String getComment_time() {
        return comment_time;
    }

    public void setComment_time(String comment_time) {
        this.comment_time = comment_time;
    }

}
