package uni.fe.tnuv.qrtest5;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
        float dist = getItem(position).getDist();
        // za dobit razdaljo, trenutno je karnekej


        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.adapter_location_view, parent, false);
        TextView tvName = convertView.findViewById(R.id.location_name);
        TextView tvDist = convertView.findViewById(R.id.location_distance);

        String urejenaRazdalja = "";
        if (dist >= 0){
            if(dist > 1000) {
                float distance2 = dist / 1000;
                urejenaRazdalja = (double)Math.round(distance2 * 10d) / 10d + "km";
            }
            else {
                urejenaRazdalja = Math.round(dist) + "m";
            }
            float hue = 125 - dist/150000*125;
            if (hue < 1) hue = 1;
            tvDist.setBackgroundColor(Color.HSVToColor(new float[]{ hue, 0.5f, 1f }));
        }

        LocationInfo location = new LocationInfo();
        location.setuID(uID);
        location.setIme(ime);

        tvName.setText(ime);
        tvDist.setText(urejenaRazdalja);


        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        convertView.startAnimation(animation);

        return convertView;

    }
}
