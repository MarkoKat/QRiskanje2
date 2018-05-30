package uni.fe.tnuv.qrtest5;

public class LocationInfo {

    public String uID;
    public String ime;
    public String opis;
    public float lat;
    public float lng;

    /*public LocationInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }*/


    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getAll(){
        return this.uID+" - "+this.ime+" - "+this.opis+" - "+this.lat+" - "+this.lng;
    }
}
