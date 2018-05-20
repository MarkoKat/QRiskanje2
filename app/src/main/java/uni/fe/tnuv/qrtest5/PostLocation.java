package uni.fe.tnuv.qrtest5;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class PostLocation {
    public String lId;
    public String ime;
    public String opis;
    public float lat;
    public float lng;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public PostLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public PostLocation(String lId, String ime, String opis, float lat, float lng) {
        this.lId = lId;
        this.ime = ime;
        this.opis = opis;
        this.lat = lat;
        this.lng = lng;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("lId", lId);
        result.put("ime", ime);
        result.put("opis", opis);
        result.put("lat", lat);
        result.put("lng", lng);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

}
