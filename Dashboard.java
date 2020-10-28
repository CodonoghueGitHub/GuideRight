package com.example.guideright;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Dashboard extends AppCompatActivity implements OnMapReadyCallback, LocationListener, RoutingListener {

    private static final String LOG_TAG = "tag" ;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFused;                                       //getting user location helper
    private PlacesClient places;                                                 //Nearby Places
    private List<AutocompletePrediction> predictList;       //Prediction for places as user types                   //Code adapted from https://www.youtube.com/watch?v=ifoVBdtXsv0&t=1670s

    private Location lastKnownLocation;
    private LocationCallback locationCallback;              //Used for getting current location if lastKnownLocation is unknown

    private MaterialSearchBar searchBar;
    private FirebaseAuth auth;
    FirebaseFirestore fstore;
    private View mapView;
    private final float default_zoom = 10;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggleOnAndOff;
    NavigationView navigationView;
    TextView distance,time,showD,showT,units1,units2;
    private double currentLat;
    private double currentLong;
    String[] placeTypeList = {"restaurant","airport","museum","park","local_government_office"};
    static String url;
    String type;
    String jsn;
    JSONObject json;
    int placePosition;
    Button showRoute;
    private LatLng latLngEnd;
    private LatLng latLngStart;
    private List<Polyline> polylines =null;
    String placeType;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        searchBar = findViewById(R.id.searchBar);
        navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        drawerLayout = findViewById(R.id.navDrawer);
        fstore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        distance = findViewById(R.id.DShow);
        time = findViewById(R.id.DShow2);
        showD = findViewById(R.id.distanceShow2);
        showT = findViewById(R.id.TimeShow);
        units1 = findViewById(R.id.unit);
        units2 = findViewById(R.id.unit2);
        showRoute = findViewById(R.id.ShowRouteBtn);

        GetPreferred();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_profile:
                        Intent in = new Intent(Dashboard.this,SavedLocations.class);
                        startActivity(in);
                        finish();
                        break;
                    case R.id.nav_Settings:
//                       Intent in1 = new Intent(Dashboard.this,SettingsPage.class);
//                       startActivity(in1);
//                       finish();

                        Toast.makeText(Dashboard.this," "+ placeType, Toast.LENGTH_LONG).show();

                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        mFused = LocationServices.getFusedLocationProviderClient(Dashboard.this);
        Places.initialize(Dashboard.this,"AIzaSyC_JuTA7mJi_2rbWmJ2T6tj59mE_hOc4QQ");        //Billing account added to access Google Places API and Google Directions API, free trial expires end of December 2020
        places = Places.createClient(this);

        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();



        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled)
            {
            }

            @Override
            public void onSearchConfirmed(CharSequence text)
            {
                startSearch(text.toString(),true,null,true);
            }

            @Override
            public void onButtonClicked(int buttonCode)
            {
                if(buttonCode == MaterialSearchBar.BUTTON_NAVIGATION)
                {
                    toggleOnAndOff = new ActionBarDrawerToggle(Dashboard.this,drawerLayout,(R.string.nav_app_bar_open_drawer_description),(R.string.nav_app_bar_navigate_up_description));     //Menu drawer show and hide
                    drawerLayout.addDrawerListener(toggleOnAndOff);
                    toggleOnAndOff.syncState();
                    drawerLayout.openDrawer(GravityCompat.START);

                }
                else if(buttonCode == MaterialSearchBar.BUTTON_BACK)
                {
                    searchBar.disableSearch();
                    searchBar.hideSuggestionsList();
                }
            }

        });


        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

                LocationBias bias = FindAutocompletePredictionsRequest.builder().getLocationBias();
                FindAutocompletePredictionsRequest predRequest = FindAutocompletePredictionsRequest.builder().setLocationBias(bias).setTypeFilter(TypeFilter.ESTABLISHMENT).setCountry("ZA").setSessionToken(token).setQuery(s.toString()).build();      //Building a prediction request for auto suggestion
                places.findAutocompletePredictions(predRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful())
                        {
                            FindAutocompletePredictionsResponse predRespon = task.getResult();
                            if(predRespon != null)
                            {
                                predictList = predRespon.getAutocompletePredictions();
                                List<String> suggestList = new ArrayList<>();
                                for(int i=0; i<predictList.size();i++)                                  //Loop through different predicted suggestions
                                {
                                    AutocompletePrediction pred = predictList.get(i);
                                    suggestList.add(pred.getFullText(null).toString());
                                }
                                searchBar.updateLastSuggestions(suggestList);


                            }
                        }
                        else
                        {
                            Log.i("tag","Prediction task failed");
                            Objects.requireNonNull(task.getException()).getMessage();
                        }
                    }
                });


                searchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
                    @Override
                    public void OnItemClickListener(int position, View v) {
                        if(position >= predictList.size())
                        {
                            return;
                        }


                        AutocompletePrediction selectedPredict = predictList.get(position);
                        String suggestion = searchBar.getLastSuggestions().get(position).toString();
                        searchBar.setText(suggestion);
                        InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        if(im!=null)
                        {
                            im.hideSoftInputFromWindow(searchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                            String placeId = selectedPredict.getPlaceId();
                            List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);

                            FetchPlaceRequest fetchPlaceReq = FetchPlaceRequest.builder(placeId,placeFields).build();
                            places.fetchPlace(fetchPlaceReq).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                                @Override
                                public void onSuccess(FetchPlaceResponse fetchPlaceResponse)
                                {
                                    Place place = fetchPlaceResponse.getPlace();
                                    LatLng latLngPlace = place.getLatLng();
                                    if(latLngPlace != null)
                                    {
                                        searchBar.hideSuggestionsList();
                                       Marker mark = mMap.addMarker (new MarkerOptions().position(latLngPlace).title(searchBar.getText()));
                                       mark.showInfoWindow();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngPlace,13));
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    if(e instanceof ApiException)
                                    {
                                        ApiException apiExep = (ApiException) e;
                                        apiExep.printStackTrace();
                                        Toast.makeText(Dashboard.this,"Place not found",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void OnItemDeleteListener(int position, View v)
                    {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }


     // Manipulates the map once available.

    @SuppressLint("MissingPermission")      //Permission Already Asked
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mapView = findViewById(R.id.map);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                marker.showInfoWindow();
                distance.setVisibility(View.INVISIBLE);
                time.setVisibility(View.INVISIBLE);
                showD.setVisibility(View.INVISIBLE);
                showT.setVisibility(View.INVISIBLE);
                units1.setVisibility(View.INVISIBLE);
                units2.setVisibility(View.INVISIBLE);
                latLngEnd = marker.getPosition();
                showRoute.setVisibility(View.VISIBLE);
                return true;
            }
        });


        showRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                distance.setVisibility(View.VISIBLE);
                time.setVisibility(View.VISIBLE);
                showD.setVisibility(View.VISIBLE);
                showT.setVisibility(View.VISIBLE);
                units1.setVisibility(View.VISIBLE);
                units2.setVisibility(View.VISIBLE);

                showRoute.setVisibility(View.INVISIBLE);
                Findroutes(latLngStart,latLngEnd);
            }
        });
        if(mapView != null)
        {
            View locationBtn = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));            //Changing my location button to the bottom right of the screen
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationBtn.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,40,180);

        }
        else{
            Toast.makeText(Dashboard.this,"No icon",Toast.LENGTH_SHORT).show();
        }

        //Checking if GPS setting on device is turned on


        LocationRequest locationReq = LocationRequest.create();
        locationReq.setInterval(10000);
        locationReq.setFastestInterval(5000);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationReq);

        SettingsClient settings = LocationServices.getSettingsClient(Dashboard.this);
        Task<LocationSettingsResponse> task = settings.checkLocationSettings(builder.build());                                          //Checking location settings and changing it
        task.addOnSuccessListener(Dashboard.this, new OnSuccessListener<LocationSettingsResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.O_MR1)
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse)
            {
                getCurrentLocation();


            }
        });

        task.addOnFailureListener(Dashboard.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                if(e instanceof ResolvableApiException)
                {
                    ResolvableApiException resolving = (ResolvableApiException) e;
                    try {
                        resolving.startResolutionForResult(Dashboard.this, 23);             //Get user to trn on gps
                    } catch (IntentSender.SendIntentException ex)
                    {
                        ex.printStackTrace();
                    }
                }

            }
        });
    }
    private String downloadUrl(String s) throws IOException {
        url = s;
        try (InputStream stream = new URL(s).openStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder builder = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                String data = builder.toString();
                JSONObject json = new JSONObject(data);
                reader.close();
                return data;
            } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 23) {
            if (resultCode == RESULT_OK) {
                getCurrentLocation();

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation()
    {

        mFused.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task)
            {
                if(task.isSuccessful())
                {
                    lastKnownLocation = task.getResult();
                    if(lastKnownLocation != null)
                    {
                        latLngStart = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                        currentLat = lastKnownLocation.getLatitude();
                        currentLong = lastKnownLocation.getLongitude();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()),13));
                        try {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            DataParser dp = new DataParser();
                            JSONArray array = dp.parse(downloadUrl("https://maps.googleapis.com/maps/api/place/search/json?location=" + currentLat + "," + currentLong + "+&radius=5000&types="+type+"&sensor=true&key=AIzaSyC_JuTA7mJi_2rbWmJ2T6tj59mE_hOc4QQ"));
                            for (int i = 0; i < array.length(); i++) {
                                // Create a marker for each nearby location in the JSON data.
                                JSONObject jsonObj = array.getJSONObject(i);
                                JSONObject locationObj = jsonObj .getJSONObject("geometry")
                                        .getJSONObject("location");

                                mMap.addMarker(new MarkerOptions()
                                        .title(jsonObj.getString("name"))
                                        .position(new LatLng(
                                                locationObj.getDouble("lat"),
                                                locationObj.getDouble("lng")
                                        ))

                                );



                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        LocationRequest locaReq = LocationRequest.create();
                        locaReq.setInterval(10000);
                        locaReq.setFastestInterval(5000);
                        locaReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult)
                            {
                                super.onLocationResult(locationResult);
                                if(locationResult == null){
                                    return;
                                }
                                lastKnownLocation = locationResult.getLastLocation();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()),13));
                                mFused.removeLocationUpdates(locationCallback);
                                try {
                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                    StrictMode.setThreadPolicy(policy);
                                    DataParser dp = new DataParser();
                                    JSONArray array = dp.parse(downloadUrl(""));
                                    for (int i = 0; i < array.length(); i++)
                                    {
                                        // Create a marker for each city in the JSON data.
                                        JSONObject jsonObj = array.getJSONObject(i);
                                        JSONObject locationObj = jsonObj .getJSONObject("geometry")
                                                .getJSONObject("location");

                                        mMap.addMarker(new MarkerOptions()
                                                .title(jsonObj.getString("name"))
                                                .position(new LatLng(
                                                        locationObj.getDouble("lat"),
                                                        locationObj.getDouble("lng")
                                                ))
                                        );
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }



                            }
                        };
                        mFused.requestLocationUpdates(locaReq,locationCallback,null);

                    }
                }
                else
                {
                    Toast.makeText(Dashboard.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void GetPreferred()
    {
        String Uid;

        Uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DocumentReference DocR = fstore.collection("users").document(Uid);

        DocR.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                placeType = value.getString("Preferred LandMark");
                if ("Restaurants".equals(placeType)) {
                    type = "restaurant";
                } else if ("Airports".equals(placeType)) {
                    type = "airport";
                } else if ("Museum".equals(placeType)) {
                    type = "museum";
                } else if ("Parks".equals(placeType)) {
                    type = "park";
                }
                type =  "local_government_office";
                Toast.makeText(Dashboard.this," "+type,Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onLocationChanged(Location location)
    {

    }

    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(Dashboard.this,"Unable to get location", Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener((RoutingListener) this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyD4uStbluZBnwKADWRtCPalZoddDXdNQbs")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
        //Findroutes(latLngStart,latLngEnd);
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(Dashboard.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(latLngStart);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

            }
            else {

            }

        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(latLngStart,latLngEnd);
    }



}