package uni.fe.tnuv.qrtest5;

import java.util.Comparator;

public class LocationNameComparator implements Comparator<LocationInfo>
{
    public int compare(LocationInfo left, LocationInfo right) {
        return left.getIme().compareTo(right.getIme());
    }
}