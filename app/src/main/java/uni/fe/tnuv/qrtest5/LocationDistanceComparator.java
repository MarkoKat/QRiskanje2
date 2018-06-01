package uni.fe.tnuv.qrtest5;

import java.util.Comparator;

public class LocationDistanceComparator implements Comparator<LocationInfo>
{
    public int compare(LocationInfo left, LocationInfo right) {
        if (left.getDist() > right.getDist()) { return 1; }
        else { return -1;}
    }
}
