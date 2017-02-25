package fyp.hkust.facet.model;

/**
 * Created by ClementNg on 22/11/2016.
 */
public class User {

    public String image;
    public String uid;
    public String name;
    public String email;
    public String gender;
    public String password;
    public String aboutMe;


    public User(String image, String uid, String name, String email, String gender, String password, String aboutMe) {
        this.uid = uid;
        this.image = image;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.password = password;
        this.aboutMe = aboutMe;
    }

    public User() {
    }

    public String getUid() {
        return uid;
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

    public String getEmail() {
        return email;
    }

//    public void setEmail(String email) {
//        this.email = email;
//    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

}
