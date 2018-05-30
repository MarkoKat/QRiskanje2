package uni.fe.tnuv.qrtest5;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;



public class LocationListAdapter extends ArrayAdapter<LocationInfo>{
    private static final String TAG = "LocationListAdapter";

    private Context mContext;

    public LocationListAdapter( Context context, int resource, ArrayList<LocationInfo> objects) {
        super(context, resource, objects);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position,View convertView, ViewGroup parent) {
        String uID = getItem(position).getuID();
        String ime = getItem(position).getIme();
        String opis = getItem(position).getOpis();
        float lat = getItem(position).getLat();
        float lng = getItem(position).getLng();
        // za dobit razdaljo, trenutno je karnekej
        double dist = 1 + Math.random() * (999 - 1);
        //double dist = getItem(position).getDist();

        LocationInfo location = new LocationInfo();
        location.setuID(uID);
        location.setIme(ime);
        location.setOpis(opis);
        location.setLat(lat);
        location.setLng(lng);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.adapter_location_view, parent, false);


        TextView tvName = (TextView) convertView.findViewById(R.id.location_name);
        TextView tvLat = (TextView) convertView.findViewById(R.id.location_lat);
        TextView tvLng = (TextView) convertView.findViewById(R.id.location_lng);
        TextView tvDist = (TextView) convertView.findViewById(R.id.location_distance);

        tvName.setText(ime);
        tvLat.setText(Float.toString(lat));
        tvLng.setText(Float.toString(lng));
        tvDist.setText(Double.toString(dist));

        return convertView;

    }
}
