package com.project.markpollution;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.markpollution.CustomAdapter.CircleTransform;
import com.project.markpollution.CustomAdapter.FeedRecyclerViewAdapter;
import com.project.markpollution.CustomAdapter.PopupInfoWindowAdapter;
import com.project.markpollution.Objects.Category;
import com.project.markpollution.Objects.PollutionPoint;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private NavigationView navigationView;
    private FloatingActionButton fab;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Spinner spnCate;
    private ImageView imgGetLocation;
    public static ArrayList<PollutionPoint> listPo;
    private List<Category> listCate;
    private List<PollutionPoint> listPoByCateID;
    private List<PollutionPoint> listSeriousPo;
    private HashMap<String, Uri> images = new HashMap<>();
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String url_retrive_pollutionPoint = "http://indi.com.vn/dev/markpollution/RetrievePollutionPoint.php";
    private String url_retrieve_cate = "http://indi.com.vn/dev/markpollution/RetrieveCategory.php";
    private String url_RetrievePollutionByCateID = "http://indi.com.vn/dev/markpollution/RetrievePollutionBy_CateID.php?id_cate=";
    private String url_RetrievePollutionOrderByRate = "http://indi.com.vn/dev/markpollution/RetrievePollutionOrderByRate.php";
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView recyclerViewFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fetchData();
        initView();
        setNavigationHeader();
        loadSpinnerCate();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        imgGetLocation = (ImageView) findViewById(R.id.imgGetLocation);
        spnCate = (Spinner) findViewById(R.id.spnCateMap);
        recyclerViewFeed = (RecyclerView) findViewById(R.id.recyclerViewFeed);

        // initialize bottomSheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // initialize firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        mapFragment.getMapAsync(this);
        fab.setOnClickListener(this);
    }

    private void fetchData(){
        Intent intent = getIntent();
        String po_data = intent.getStringExtra("po_data");
        try {
            JSONObject jObj = new JSONObject(po_data);
            JSONArray arr = jObj.getJSONArray("result");
            listPo = new ArrayList<>();
            for(int i=0; i<arr.length(); i++){
                JSONObject po = arr.getJSONObject(i);
                String id_po = po.getString("id_po");
                String id_cate = po.getString("id_cate");
                String id_user = po.getString("id_user");
                double lat = po.getDouble("lat");
                double lng = po.getDouble("lng");
                String title = po.getString("title");
                String desc = po.getString("desc");
                String image = po.getString("image");
                String time = po.getString("time");

                listPo.add(new PollutionPoint(id_po, id_cate, id_user, lat, lng, title, desc, image, time));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // simultaneously listening data changed on Firebase
//        listenDataChanged();
    }

    private void setNavigationHeader() {
        View view = navigationView.getHeaderView(0);
        TextView tvNavName = (TextView) view.findViewById(R.id.username);
        TextView tvNavEmail = (TextView) view.findViewById(R.id.email);
        ImageView ivNavAvatar = (ImageView) view.findViewById(R.id.profile_image);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String avatar = intent.getStringExtra("avatar");
        tvNavName.setText(name);
        tvNavEmail.setText(email);
        Picasso.with(this).load(Uri.parse(avatar)).resize(250, 250).transform(new CircleTransform()).into(ivNavAvatar);
    }

    private void loadSpinnerCate() {
        StringRequest stringReq = new StringRequest(Request.Method.GET, url_retrieve_cate, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    JSONArray arr = jObj.getJSONArray("result");
                    listCate = new ArrayList<>();
                    listCate.add(0, new Category("0", "Show All"));
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject cate = arr.getJSONObject(i);
                        listCate.add(new Category(cate.getString("id_cate"), cate.getString("name_cate")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayAdapter<Category> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout
                        .simple_spinner_item,
                        listCate);
                adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
                spnCate.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(MainActivity.this).add(stringReq);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setInfoWindowAdapter(new PopupInfoWindowAdapter(this, LayoutInflater.from(this), images));
        googleMap.setOnInfoWindowClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Application hasn't granted permission to access your location", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        filterPollutionByCate(googleMap);
        loadRecyclerViewFeed();

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void addMarker(GoogleMap map, PollutionPoint po) {
        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(po.getLat(), po.getLng()))
                .title(po.getTitle())
                .snippet(po.getDesc()));
        marker.setTag(po.getId());

        images.put(marker.getId(), Uri.parse(po.getImage()));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));

    }

    // Each time filter pollutionPoints by Category. It requests to server => Maybe I'll load data from static List<PollutionPoint>
    // Get pollution points by cateID and extract into map
    private void getPollutionByCateID(String CateID) {
        String completed_RetrievePollutionByCateID = url_RetrievePollutionByCateID + CateID;
        JsonObjectRequest objReq = new JsonObjectRequest(Request.Method.GET, completed_RetrievePollutionByCateID, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray arr = response.getJSONArray("result");
                            listPoByCateID = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject po = arr.getJSONObject(i);
                                String id_po = po.getString("id_po");
                                String id_cate = po.getString("id_cate");
                                String id_user = po.getString("id_user");
                                double lat = po.getDouble("lat");
                                double lng = po.getDouble("lng");
                                String title = po.getString("title");
                                String desc = po.getString("desc");
                                String image = po.getString("image");
                                String time = po.getString("time");

                                listPoByCateID.add(new PollutionPoint(id_po, id_cate, id_user, lat, lng, title, desc, image, time));
                            }
                            // extract markers in list<> markers into the map
                            mMap.clear();
                            for (PollutionPoint po : listPoByCateID) {
                                addMarker(mMap, po);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(MainActivity.this).add(objReq);
    }

    private void filterPollutionByCate(final GoogleMap googleMap){
        spnCate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Category cate = (Category) spnCate.getItemAtPosition(i);
                String CateID = cate.getId();

                if(!CateID.equals("0")){
                    getPollutionByCateID(CateID);
                }else{
                    googleMap.clear();
                    for (PollutionPoint po : listPo) {
                        addMarker(googleMap, po);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    private void getSeriousPollution(){
        JsonObjectRequest objReq = new JsonObjectRequest(Request.Method.GET, url_RetrievePollutionOrderByRate, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("status").equals("success")){
                                listSeriousPo = new ArrayList<>();
                                JSONArray arr = response.getJSONArray("response");
                                for(int i=0; i<arr.length(); i++){
                                    JSONObject po = arr.getJSONObject(i);
                                    listSeriousPo.add(new PollutionPoint(po.getString("id_po"), po.getString
                                            ("id_cate"), po.getString("id_user"), po.getDouble("lat"), po.getDouble
                                            ("lng"), po.getString("title"), po.getString("desc"), po.getString
                                            ("image"), po.getString("time")));
                                }
                                mMap.clear();
                                for(PollutionPoint po: listSeriousPo){
                                    addMarker(mMap, po);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Volley", error.getMessage());
            }
        });

        Volley.newRequestQueue(this).add(objReq);
    }

    private void loadRecyclerViewFeed(){
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewFeed.setLayoutManager(layout);
        recyclerViewFeed.setAdapter(new FeedRecyclerViewAdapter(this, listPo));

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(36);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_recentPo) {
        } else if (id == R.id.nav_reportMgr) {
            startActivity(new Intent(this, ReportManagementActivity.class));
        } else if (id == R.id.nav_nearbyPo) {

        } else if (id == R.id.nav_seriousPo) {
            getSeriousPollution();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
      if(view == fab){
          Snackbar.make(view, "Mark the pollution point and click OK ", Snackbar.LENGTH_INDEFINITE).setAction("OK",
                  new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          Intent i = new Intent(MainActivity.this, SendReportActivity.class);
                          i.putExtra("Lat", mMap.getCameraPosition().target.latitude);
                          i.putExtra("Long", mMap.getCameraPosition().target.longitude);
                          startActivity(i);
                      }
                  }).show();
      }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(this, DetailReportActivity.class);
        intent.putExtra("id_po", marker.getTag().toString());
        startActivity(intent);
    }
}
