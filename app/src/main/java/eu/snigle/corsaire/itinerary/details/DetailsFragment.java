package eu.snigle.corsaire.itinerary.details;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.snigle.corsaire.R;
import eu.snigle.corsaire.itinerary.Itinerary;


public class DetailsFragment extends ListFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    public void updateData(Context context, Itinerary itinerary) {
        List<Map<String,Object>> data = new ArrayList<>();
        for (Step step: itinerary.steps ) {
            Map<String, Object> el = new HashMap<String, Object>();
            el.put("narrative", Html.fromHtml(step.narrative));
            el.put("distance", step.distance+"m");
            data.add(el);
        }
        setListAdapter(new SimpleAdapter(context,data,R.layout.list_view_detail, new String[]{"narrative", "distance"}, new int[]{R.id.titre, R.id.distance}));
    }
}
