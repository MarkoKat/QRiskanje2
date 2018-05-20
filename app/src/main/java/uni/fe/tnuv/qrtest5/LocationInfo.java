package uni.fe.tnuv.qrtest5;

public class LocationInfo {

    public String ime;
    public String opis;
    public float lat;
    public float lng;

    /*public LocationInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }*/

    public LocationInfo(String ime, String opis, float lat, float lng) {
        this.ime = ime;
        this.opis = opis;
        this.lat = lat;
        this.lng = lng;
    }

}
