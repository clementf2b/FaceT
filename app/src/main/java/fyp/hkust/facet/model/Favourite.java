package fyp.hkust.facet.model;

/**
 * Created by ClementNg on 2/4/2017.
 */

public class Favourite {

    private String time;
    private Long colorNo;

    public Favourite(String time, Long colorNo) {
        this.time = time;
        this.colorNo = colorNo;
    }

    public Favourite() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getColorNo() {
        return colorNo;
    }

    public void setColorNo(Long colorNo) {
        this.colorNo = colorNo;
    }

}
