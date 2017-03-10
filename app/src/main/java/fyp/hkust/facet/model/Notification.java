package fyp.hkust.facet.model;

/**
 * Created by ClementNg on 8/3/2017.
 */

public class Notification {

    String action;
    String sender_user_id;
    String product_id;
    String sender_image;
    String sender_username;
    String time;
    String product_image;
    String product_name;

    public Notification()
    {

    }

    public Notification(String action, String sender_user_id, String product_id, String sender_username, String time, String product_image, String product_name) {
        this.action = action;
        this.sender_user_id = sender_user_id;
        this.product_id = product_id;
        this.sender_username = sender_username;
        this.time = time;
        this.product_image = product_image;
        this.product_name = product_name;
    }

    public Notification(String action, String sender_id, String product_id, String sender_image, String sender_username, String time, String product_image, String product_name) {
        this.action = action;
        this.sender_user_id = sender_id;
        this.product_id = product_id;
        this.sender_image = sender_image;
        this.sender_username = sender_username;
        this.time = time;
        this.product_image = product_image;
        this.product_name = product_name;
    }

    public Notification(String action, String sender_id, String product_id, String sender_image, String sender_username, String time) {
        this.action = action;
        this.sender_user_id = sender_id;
        this.product_id = product_id;
        this.sender_image = sender_image;
        this.sender_username = sender_username;
        this.time = time;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getSender_image() {
        return sender_image;
    }

    public void setSender_image(String sender_image) {
        this.sender_image = sender_image;
    }

    public String getSender_username() {
        return sender_username;
    }

    public void setSender_username(String sender_username) {
        this.sender_username = sender_username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSender_id() {
        return sender_user_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_user_id = sender_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
