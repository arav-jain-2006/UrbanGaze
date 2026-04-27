package com.urbangaze.app.ui.trips;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ola.mapsdk.camera.MapControlSettings;
import com.ola.mapsdk.camera.OlaCameraPosition;
import com.ola.mapsdk.interfaces.OlaMapCallback;
import com.ola.mapsdk.model.OlaLatLng;
import com.ola.mapsdk.model.OlaMarkerOptions;
import com.ola.mapsdk.model.OlaPolylineOptions;
import com.ola.mapsdk.model.SnippetPropertiesOptions;
import com.ola.mapsdk.view.OlaMap;
import com.ola.mapsdk.view.OlaMapView;
import com.urbangaze.app.R;
import com.urbangaze.app.data.AppData;
import com.urbangaze.app.model.Destination;
import com.urbangaze.app.model.Trip;
import com.urbangaze.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;

public class ViewTripFragment extends Fragment {
    TextView tripTitle;
    ImageView imgView, imgBackground;
    ImageButton backButton;
    OlaMapView mapView;
    OlaMap olaMap;
    Trip currTrip;
    List<Destination> destList = new ArrayList<>();

    public ViewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.removeCallbacks(null);
        Glide.with(requireContext()).clear(imgBackground);
        Glide.with(requireContext()).clear(imgView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initLocationAndMap();
    }

    private void initViews(View view) {
        tripTitle = view.findViewById(R.id.tripTitle);
        imgView = view.findViewById(R.id.imgView);
        imgBackground = view.findViewById(R.id.imgViewBackground);
        mapView = view.findViewById(R.id.mapView);
        backButton = view.findViewById(R.id.backButton);

        currTrip = AppData.getCurrentTrip();
        tripTitle.setText(currTrip.getTripName());
        destList.addAll(currTrip.getDestinations());

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void initLocationAndMap() {
        MapControlSettings mapControlSettings = new MapControlSettings.Builder()
                .setRotateGesturesEnabled(false)
                .setScrollGesturesEnabled(false)
                .setZoomGesturesEnabled(false)
                .setCompassEnabled(false)
                .setTiltGesturesEnabled(false)
                .setDoubleTapGesturesEnabled(false)
                .build();

        mapView.getMap(Constants.OLA_API_KEY, new OlaMapCallback() {

            @Override
            public void onMapReady(OlaMap olaMapInstance) {
                olaMap = olaMapInstance;
                OlaCameraPosition campos = new OlaCameraPosition.Builder()
                        .setTilt(90)
                        .build();

                olaMap.updateCameraPosition(campos);
                addPolylinesAndMarkers();
                startDestinationTour();
            }

            @Override
            public void onMapError(String error) {
                Log.e("MAP", error);
            }
        }, mapControlSettings);
    }

    private void addPolylinesAndMarkers() {
        ArrayList<OlaLatLng> points = new ArrayList<>();

        for (Destination d: destList) {
            OlaLatLng loc = new OlaLatLng(d.getLatitude(), d.getLongitude(), 0);
            points.add(loc);

            OlaMarkerOptions markerOptions = new OlaMarkerOptions.Builder()
                    .setMarkerId("dest_"+points.size())
                    .setPosition(loc)
                    .setIconSize(1.4f)
                    .setSnippet(d.getTitle())
                    .setSnippetPropertiesOptions(new SnippetPropertiesOptions.Builder()
                            .setSnippetTextSize(12.0f)
                            .setInfoWindowStrokeColor("#1e7f75")
                            .setInfoWindowRadius(40.0f)
                            .build())
                    .setIconIntRes(R.drawable.location_pin)
                    .setIsIconClickable(true)
                    .setIsAnimationEnable(true)
                    .build();

            olaMap.addMarker(markerOptions);

        }

        OlaPolylineOptions polylineOptions = new OlaPolylineOptions.Builder()
                .setPolylineId("pid1")
                .setColor("#1e7f75")
                .setWidth(3f)
                .setPoints(points)
                .build();

        olaMap.addPolyline(polylineOptions);
    }

    private void startDestinationTour() {
        if (destList.isEmpty() || olaMap == null) return;


        final int[] index = {0};

        Runnable tourRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] >= destList.size()) return;

                Destination currentDest = destList.get(index[0]);

                OlaLatLng loc = new OlaLatLng(currentDest.getLatitude() + 0.007, currentDest.getLongitude(), 0);
                olaMap.moveCameraToLatLong(loc, 12f, 1000);

                RequestOptions options = new RequestOptions()
                        .transform(
                                new BlurTransformation(25),
                                new ColorFilterTransformation(Color.parseColor("#80000000"))
                        );

                mapView.postDelayed(() -> {
                    if (getContext() == null) return;

                    Glide.with(requireContext())
                            .load(currentDest.getImageUrl())
                            .apply(options)
                            .into(imgBackground);

                    Glide.with(requireContext())
                            .load(currentDest.getImageUrl())
                            .transition(DrawableTransitionOptions.withCrossFade(300))
                            .into(imgView);

                    index[0]++;
                }, 1000);



                mapView.postDelayed(this, 2500);
            }
        };

        mapView.post(tourRunnable);
    }
}
