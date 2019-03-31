package my.example.here;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.SupportMapFragment;
import com.here.android.mpa.search.Address;
import com.here.android.mpa.search.Category;
import com.here.android.mpa.search.DiscoveryRequest;
import com.here.android.mpa.search.DiscoveryResult;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.Place;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.PlaceRequest;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.ReverseGeocodeRequest;
import com.here.android.mpa.search.SearchRequest;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private Map map = null;
    private SupportMapFragment mapFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragmnet);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(55.751429, 37.639054, 0.0),
                            Map.Animation.NONE);


                    MapMarker defaultMarker = new MapMarker();
                    defaultMarker.setCoordinate(new GeoCoordinate(55.752000, 37.639054, 0.0));
                    map.addMapObject(defaultMarker);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);

                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });

        searchPharmacy();
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
            GeoCoordinate seattle
                    = new GeoCoordinate(55.751429,37.639054, 0.0);

            DiscoveryRequest request =
                    new SearchRequest("hospital").setSearchCenter(seattle);

            // limit number of items in each result page to 10
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




   }






