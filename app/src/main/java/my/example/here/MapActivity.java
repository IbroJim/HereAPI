package my.example.here;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.SupportMapFragment;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.search.DiscoveryRequest;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.SearchRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements Map.OnTransformListener, PositioningManager.OnPositionChangedListener{

    private static final String EXTRA_SEARCH_REQUEST="search";
    private Map map = null;
    private SupportMapFragment mapFragment = null;
    private PositioningManager mPositioningManager;
    private boolean mTransforming;
    private Runnable mPendingUpdate;
    private  GeoCoordinate seattle=null;


    private SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragmnet);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);


    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mPositioningManager != null) {
            mPositioningManager.stop();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mPositioningManager != null) {
            mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }
    private void initialize() {
        mapFragment =getMapFragment();
        mapFragment.setRetainInstance(false);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    mapFragment.init(new OnEngineInitListener() {
                        @Override
                        public void onEngineInitializationCompleted(Error error) {
                            if (error == OnEngineInitListener.Error.NONE) {
                                map = mapFragment.getMap();
                                map.setCenter(new GeoCoordinate(61.497961, 23.763606, 0.0), Map.Animation.NONE);
                                map.setZoomLevel(map.getMaxZoomLevel() - 1);
                                map.addTransformListener(MapActivity.this);
                                mPositioningManager = PositioningManager.getInstance();
                                mPositioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(MapActivity.this));
                                if (mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK)) {
                                    map.getPositionIndicator().setVisible(true);
                                    searchPharmacy();
                                }
                            } else {
                                System.out.println("ERROR: Cannot initialize Map Fragment");
                            }
                        }
                    });


                }
            }
        });


    }
    private void searchPharmacy(){
        class SearchRequestListener implements ResultListener<DiscoveryResultPage> {
            final List<GeoCoordinate> geoCoordinatesList=new ArrayList<>();
            @Override
            public void onCompleted(DiscoveryResultPage data, ErrorCode error) {
                if (error != ErrorCode.NONE) {
                    // Handle error

                } else {
                    PlaceLink link = null;
                     for(int i=0;i<data.getPlaceLinks().size();i++) {
                         link=data.getPlaceLinks().get(i);
                         GeoCoordinate a = link.getPosition();
                         geoCoordinatesList.add(a);
                         Log.d("Hello", a.toString());

                     }
                     setMap(geoCoordinatesList);
                     setPath(geoCoordinatesList);

                   //  setMap(geoCoordinatesList);
                  //     List<DiscoveryResult> arrayList =data.getItems();
                     //   for (int i=0;arrayList.size()>i;i++){
                       //     String a=arrayList.get(i).getTitle();
                         //   Log.d("Hello",a);
                        //}
                }

            }
        }
        
        try {
         GeoPosition position=mPositioningManager.getLastKnownPosition();
         seattle=position.getCoordinate();
            Intent intent=getIntent();
            String search=intent.getStringExtra(EXTRA_SEARCH_REQUEST);
         DiscoveryRequest request = new SearchRequest(search).setSearchCenter(seattle);
         request.setCollectionSize(10);
         ErrorCode error = request.execute(new SearchRequestListener());
         if( error != ErrorCode.NONE ) {
            }

        } catch (IllegalArgumentException ex) {
        }
    }
    private void setMap(List<GeoCoordinate> list){
        List<MapObject> mapMarkerList=new ArrayList<>();
        for (int i=0;i<list.size();i++){
            mapMarkerList.add(new MapMarker(list.get(i)));
        }
        map.addMapObjects(mapMarkerList);
   }
    @Override
    public void onPositionUpdated(final PositioningManager.LocationMethod locationMethod, final GeoPosition geoPosition, final boolean b) {
        final GeoCoordinate coordinate = geoPosition.getCoordinate();
        if (mTransforming) {
            mPendingUpdate = new Runnable() {
                @Override
                public void run() {
                    onPositionUpdated(locationMethod, geoPosition, b);
                }
            };
        } else {
            map.setCenter(coordinate, Map.Animation.LINEAR);
        }
    }
    @Override
    public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

    }
    @Override
    public void onMapTransformStart() {
        mTransforming = true;
    }
    @Override
    public void onMapTransformEnd(MapState mapState) {
        mTransforming = false;
        if (mPendingUpdate != null) {
            mPendingUpdate.run();
            mPendingUpdate = null;
        }

    }
    private void setPath(List<GeoCoordinate> list){
        class RouteListener implements RouteManager.Listener {

            // Method defined in Listener
            public void onProgress(int percentage) {
                // Display a message indicating calculation progress
            }

            @Override
            public void onCalculateRouteFinished(RouteManager.Error error, List<RouteResult> list) {
                if (error == RouteManager.Error.NONE) {
                    // Render the route on the map
                    MapRoute mapRoute = new MapRoute(list.get(0).getRoute());
                    map.addMapObject(mapRoute);
                } else {
                    // Display a message indicating route calculation failure
                }
            }

        }    //
        RouteManager rm = new RouteManager();
        RoutePlan routePlan = new RoutePlan();
        routePlan.addWaypoint(seattle);
        routePlan.addWaypoint(list.get(0));
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);
        rm.calculateRoute(routePlan, new RouteListener());
    }

}






