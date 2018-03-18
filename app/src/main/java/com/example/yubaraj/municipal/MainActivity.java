package com.example.yubaraj.municipal;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.yubaraj.municipal.PagerActivity.readSharedSetting;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Spinner districtSpinner, stateSpinner, vdcSpinner;
    private FloatingActionButton show;
    private ImageView imageView, mapImageView;
    private GoogleMap mGoogleMap;
    private Marker marker;
    private Geocoder geocoder;
    private LatLng latLng;
    private List<Address> addresses = new ArrayList<>();
    private boolean isInfoWindowShown = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String m_Text = "";
    private int isStartup = 0;
    private String location;
    //variables for new vdc details
    private String totalPopulation, totalArea, head,subHead,newVdc,headEmail,headPhone;
    //variables for state details
    private String population, area, website, mayor, deputMayor,capital,populationDensity,statePicture,mayorEmail,mayorPhone;
    private Bitmap pic;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private SlidingUpPanelLayout slidingView;
    private boolean isUp;
    private boolean isUserFirstTime;
    public static String PREF_USER_FIRST_TIME;
    private View slider;
    private TextView tvMayorOrHead,tvDeputeMayorOrSubHead,tvMayorOrHeadEmail;
    private TextView tvMayorOrHeadPhone,tvPopulation,tvArea,tvWebsite,tvCapital;
    private TextView tvPopulationDensity,tvPicture;
    private View divider0,divider1,divider2,divider3,divider4,divider5,divider6,divider7,divider8;
    private TextView tvCapitalLabel,tvMayorOrHeadLabel,tvDeputeMayorOrSubHeadLabel,tvHeadEmailLabel,tvHeadPhoneLabel;
    private TextView tvWebsiteLabel,tvTotalPopulationLabel,tvPopulationDensityLabel,tvAreaLabel;
    // private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Onboarding segment to check whether user seen the onboarding intro or nto start
        isUserFirstTime = Boolean.valueOf(readSharedSetting(MainActivity.this, PREF_USER_FIRST_TIME, "true"));

        Intent introIntent = new Intent(MainActivity.this, PagerActivity.class);
        introIntent.putExtra(PREF_USER_FIRST_TIME, isUserFirstTime);

        if (isUserFirstTime)
            startActivity(introIntent);
        //Onboarding end

        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            initialize();

        } else {
            //No Google map layout
        }


    }

    //Inflate menu item
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Handle the menu Item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_feedback) {
            startActivity(new Intent(this, Feedback.class));
        }
        if (id == R.id.action_about) {

        }
        if (id == R.id.action_search) {

            googleSearchBox();

        }
        return super.onOptionsItemSelected(item);
    }

    private void googleSearchBox() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setCountry("NP")
                    .build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    //Close app on double back press
    //Double back to exit from app
//    @Override
//    public void onBackPressed() {
//        //Checking for fragment count on backstack
//        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//            getSupportFragmentManager().popBackStack();
//        } else if (!doubleBackToExitPressedOnce) {
//            this.doubleBackToExitPressedOnce = true;
//            Toast.makeText(this,"Please click BACK again to exit.", Toast.LENGTH_SHORT).show();
//
//            new Handler().postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    doubleBackToExitPressedOnce = false;
//                }
//            }, 2000);
//        } else {
//            super.onBackPressed();
//            return;
//        }
//    }


    // Ask user to exit or not
    @Override
    public void onBackPressed() {

        if (slidingView != null &&

                (slidingView.getPanelState() == PanelState.EXPANDED || slidingView.getPanelState() == PanelState.ANCHORED)) {

            slidingView.setPanelState(PanelState.COLLAPSED);

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Do you want to Exit?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        //super.onBackPressed();
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can not connect to google play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void initialize() {
        districtSpinner = findViewById(R.id.district_spinner);
        stateSpinner = findViewById(R.id.state_spinner);
        vdcSpinner = findViewById(R.id.vdc_spinner);
        //up, down and drag arrow in the sliding pannel
        imageView = findViewById(R.id.sliding_image_view);
        //small map of state or district in sliding view
        mapImageView = findViewById(R.id.map_view);
        tvMayorOrHead=findViewById(R.id.text_view_head);
        tvDeputeMayorOrSubHead=findViewById(R.id.text_view_sub_head);
        tvMayorOrHeadEmail=findViewById(R.id.text_view_email);
        tvMayorOrHeadPhone=findViewById(R.id.text_view_phone);
        tvPopulation=findViewById(R.id.text_view_total_population);
        tvArea=findViewById(R.id.text_view_total_area);
        tvWebsite=findViewById(R.id.text_view_website);
        tvCapital=findViewById(R.id.text_view_capital);
        tvPopulationDensity=findViewById(R.id.text_view_population_density);
        tvDeputeMayorOrSubHeadLabel=findViewById(R.id.label_sub_head);
        tvMayorOrHeadLabel=findViewById(R.id.label_sub_head);

        divider0=findViewById(R.id.divider0);
        divider1=findViewById(R.id.divider1);
        divider2=findViewById(R.id.divider2);
        divider3=findViewById(R.id.divider3);
        divider4=findViewById(R.id.divider4);
        divider5=findViewById(R.id.divider5);
        divider6=findViewById(R.id.divider6);
        divider7=findViewById(R.id.divider7);

        tvWebsiteLabel=findViewById(R.id.label_website);
        tvTotalPopulationLabel=findViewById(R.id.label_total_population);
        tvPopulationDensityLabel=findViewById(R.id.label_population_density);
        tvAreaLabel=findViewById(R.id.label_total_area);
        tvCapitalLabel=findViewById(R.id.label_capital);
        tvHeadEmailLabel=findViewById(R.id.label_email);
        tvHeadPhoneLabel=findViewById(R.id.label_phone);

//        show = findViewById(R.id.show_local_gov);
        geocoder = new Geocoder(this, Locale.getDefault());
        checkNetConnection();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(this);
        slidingView = findViewById(R.id.sliding_layout);
        slider=findViewById(R.id.sliding_view);

        doWork();
    }

    private void checkNetConnection() {
        CheckInternet checkInternet = new CheckInternet();
        if (checkInternet.isNetworkAvailable(this) == false) {

//             Toast.makeText(this,"Please turn on wifi or mobile data",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, NoNet.class);
            intent.putExtra("message", "Please turn on wifi or mobile data and \n Pull down to refresh ");
            startActivity(intent);
            finish();
            return;


        } else if (checkInternet.isOnline() == false) {

            Toast.makeText(this, "Internet is not available", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, NoNet.class);
            intent.putExtra("message", "Internet is not available\nPull down to refresh ");
            startActivity(intent);
            return;

        }
    }


    public void doWork() {
        //Selecting dropdown item from values/string
        ArrayAdapter<CharSequence> state = ArrayAdapter.createFromResource(this, R.array.state, android.R.layout.simple_list_item_1);
        state.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(state);
        slidingView.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override

            public void onPanelSlide(View panel, float slideOffset) {


                Log.i(TAG, "onPanelSlide, offset " + slideOffset);

            }

            @Override

            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                if (newState.toString().equals("EXPANDED")) {
                    imageView.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24dp);
                } else if (newState.toString().equals("COLLAPSED")) {
                    imageView.setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp);
                } else if (newState.toString().equals("DRAGGING")) {
                    imageView.setImageResource(R.drawable.ic_drag_handle_white_24dp);
                }
                Log.i(TAG, "onPanelStateChanged " + newState);

            }


        });
        mapImageView.setOnClickListener(new View.OnClickListener() {
            String string = null;

            @Override
            public void onClick(View view) {
                loadPhoto(pic);
            }
        });

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++isStartup > 1) {

                    if (!stateSpinner.getSelectedItem().equals("Select State")) {
                        slider.setVisibility(View.VISIBLE);
                        String state = (String) stateSpinner.getSelectedItem();
                        String finalDistrictListUrl = makeFinalUrl("http://192.168.100.178:8088/localLevel/rest/districts/state/", state);
                        loadSpinnerData(districtSpinner, finalDistrictListUrl, "district");

                        String finalStateDetailUrl=makeFinalUrl("http://192.168.100.178:8088/localLevel/rest/states/state/",state);
                        Log.d("Show Url::",finalDistrictListUrl+", "+finalStateDetailUrl);
                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, finalStateDetailUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    population = jsonObject.getString("population");
                                    area = jsonObject.getString("area");
                                    mayor = jsonObject.getString("mayor");
                                    deputMayor = jsonObject.getString("deputMayor");
                                    website = jsonObject.getString("website");
                                    capital=jsonObject.getString("capital");
                                    statePicture=jsonObject.getString("statePicture");
                                    populationDensity=jsonObject.getString("density");


                                    tvArea.setText(area);
                                    tvCapital.setText(capital);
                                    tvMayorOrHeadLabel.setText("Mayor");
                                    tvMayorOrHead.setText(mayor);
                                    tvDeputeMayorOrSubHeadLabel.setText("Deput Mayor");
                                    tvDeputeMayorOrSubHead.setText(deputMayor);
                                    tvWebsite.setText(website);
                                    tvPopulationDensity.setText(populationDensity);
                                    tvPopulation.setText(population);

                                    byte[] decodeValue = Base64.decode(statePicture, Base64.DEFAULT);
                                    pic = BitmapFactory.decodeByteArray(decodeValue, 0, decodeValue.length);
                                    mapImageView.setImageBitmap(pic);
                                    slidingView.setPanelState(PanelState.EXPANDED);

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                                Log.d("Data::", population + ", " + deputMayor + ", " + website);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });

                        int socketTimeout = 30000;
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        stringRequest.setRetryPolicy(policy);
                        requestQueue.add(stringRequest);
                       // sDSnackBar(stateSpinner.getSelectedItem().toString());




                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//Creating drowpdown item programatically
        final List<String> list = new ArrayList<String>();
        list.add("Select district");
        ArrayAdapter<String> district = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        district.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(district);

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++isStartup > 2) {
                    String district = districtSpinner.getSelectedItem().toString();
                    if ((districtSpinner.getSelectedItem().equals("Select district")) && (stateSpinner.getSelectedItem().equals("Select State"))) {
                        stateSpinner.performClick();
                        Toast.makeText(MainActivity.this, "Please Select the state first", Toast.LENGTH_SHORT).show();
                    } else if (!district.equals("Select district") && !district.equals("Select One")) {
                        String finalOldVdcListUrl = makeFinalUrl("http://192.168.100.178:8088/localLevel/rest/oldVdcs/district/", district);
                        String finalDistrictDetailUrl=makeFinalUrl("http://192.168.100.178:8088/localLevel/rest/districts/district/",district);
                        loadSpinnerData(vdcSpinner, finalOldVdcListUrl, "oldVdc");

                    }
//                else if(district.equals("Select district")){
//                    Toast.makeText(MainActivity.this, "Please Select the state first", Toast.LENGTH_SHORT).show();
//                    stateSpinner.setFocusable(true);
//                    stateSpinner.requestFocus();
//                   // stateSpinner.performClick();
//                    Toast.makeText(MainActivity.this, "Clicked"+district, Toast.LENGTH_SHORT).show();
//                }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        String vdclist[] = {"Select Vdc"};
        ArrayAdapter<String> vdc = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, vdclist);
        vdc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vdcSpinner.setAdapter(vdc);

        vdcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++isStartup > 3) {
                    final String district, vdc, state;
                    state = stateSpinner.getSelectedItem().toString();
                    district = districtSpinner.getSelectedItem().toString();
                    vdc = String.valueOf(vdcSpinner.getSelectedItem());

                    if ((vdcSpinner.getSelectedItem().equals("Select Vdc")) && (districtSpinner.getSelectedItem().equals("Select district"))) {
                        stateSpinner.performClick();
                        Toast.makeText(MainActivity.this, "Please Select the state first", Toast.LENGTH_SHORT).show();
                    } else if ((vdcSpinner.getSelectedItem().equals("Select Vdc")) && (districtSpinner.getSelectedItem().equals("Select One"))) {
                        districtSpinner.performClick();
                        Toast.makeText(MainActivity.this, "Please Select the district first", Toast.LENGTH_SHORT).show();
                    } else if (!(vdc.equals("Select Vdc")) && !(vdc.equals("Select One"))) {
                        location = district + ", " + vdc + ", " + "Nepal";
                        Log.d("location::", String.valueOf(location));

                        try {
                            List<Address> addressList = geocoder.getFromLocationName(location, 1);
                            if (!addressList.isEmpty()) {
                                Address address = addressList.get(0);
                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                addMarker(latLng, address);
                            } else {

                                Toast.makeText(MainActivity.this, "No match found, Please enter the places nearby", Toast.LENGTH_SHORT).show();
                                // alternateChoice(district);
                                googleSearchBox();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //start of show click
                        String parameter = (String) vdcSpinner.getSelectedItem();
                        String finalNewVdcDetailUrl = makeFinalUrl("http://192.168.100.178:8088/localLevel/rest/newVdcs/newVdcDetails/", parameter);

                        final ArrayList<String> oldVdcArray = new ArrayList<String>();

                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, finalNewVdcDetailUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    totalPopulation = jsonObject.getString("population");
                                    totalArea = jsonObject.getString("area");
                                    head = jsonObject.getString("head");
                                    subHead = jsonObject.getString("subHead");
                                    newVdc = jsonObject.getString("newVdc");
                                    headEmail=jsonObject.getString("email");

                                    tvArea.setText(totalArea);

                                    tvCapital.setVisibility(View.GONE);
                                    tvCapitalLabel.setVisibility(View.GONE);
                                    divider0.setVisibility(View.GONE);
                                    tvMayorOrHead.setText(head);
                                    tvDeputeMayorOrSubHead.setText(subHead);
                                    tvWebsite.setVisibility(View.GONE);
                                    tvWebsiteLabel.setVisibility(View.GONE);
                                    divider5.setVisibility(View.GONE);
                                    tvPopulationDensity.setVisibility(View.GONE);
                                    tvPopulationDensityLabel.setVisibility(View.GONE);
                                    divider7.setVisibility(View.GONE);
                                    tvPopulation.setText(totalPopulation);
                                    tvMayorOrHeadEmail.setText(headEmail);

                                    slidingView.setPanelState(PanelState.EXPANDED);

                                    JSONArray jsonArray = jsonObject.getJSONArray("oldVdc");
                                    ArrayList<String> arrayList = new ArrayList<>();
                                    arrayList.add("Select One");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                        String nameList = jsonObject1.getString("oldVdc");
                                        oldVdcArray.add(nameList);
                                    }
                                    Log.d("OldVdc::", oldVdcArray.toString());
                                    Log.d("Data::", totalPopulation + ", " + totalArea + ", " + head + ", " + subHead);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });

                        int socketTimeout = 30000;
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        stringRequest.setRetryPolicy(policy);
                        requestQueue.add(stringRequest);
                        //End of show click

                       // showSnackBarAction(newVdc);

                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String makeFinalUrl(String baseUrl, String param) {
        String parameter = param;
        String urlWithParameter = null;
        String encodedUrl = null;
        if (param != null) {
            if (param.contains(" ")) {

                parameter = param.replaceAll(" ", "%20");
            }
            try {
                urlWithParameter = baseUrl + java.net.URLEncoder.encode(parameter, "UTF-8");
                Log.d("Pre URL::", urlWithParameter);
                urlWithParameter = urlWithParameter.replaceAll("%2520", "%20");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                URL URL = new URL(urlWithParameter);
                encodedUrl = String.valueOf(URL);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            URL URL = null;
            try {
                URL = new URL(baseUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            encodedUrl = String.valueOf(URL);
        }
        return encodedUrl;
    }

//    private String makeUrl(String paramValue) {
//        paramValue = paramValue.replaceAll(" ", "%20");
//        Log.d("State Parameter::", paramValue);
//        String urll = null;
//        try {
//            urll = "http://192.168.100.178:8088/localLevel/rest/districts/" + java.net.URLEncoder.encode(paramValue, "UTF-8");
//            Log.d("Pre URL::", urll);
//            urll = urll.replaceAll("%2520", "%20");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        try {
//            URL URL = new URL(urll);
//            return (String.valueOf(URL));
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        return urll;
//    }

    private void showSnackBarAction(String bodyText) {
        View parentLayout = findViewById(android.R.id.content);

        Snackbar snackbar = Snackbar.make(parentLayout, bodyText, Snackbar.LENGTH_LONG);
        snackbar.setAction("More Details", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Showing the detail of selected items
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("State: " + stateSpinner.getSelectedItem() + "\n");
                stringBuffer.append("Local level: " + newVdc + "\n");
                stringBuffer.append("Head: " + head + "\n");
                stringBuffer.append("SubHead: " + subHead + "\n");
                stringBuffer.append("Total population: " + totalPopulation + "\n");
                stringBuffer.append("Total area: " + totalArea + "\n");

                showMessage("Details", stringBuffer.toString());

            }
        });
        snackbar.setActionTextColor(getResources().getColor(android.R.color.holo_blue_light));
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();

    }

    private void sDSnackBar(String body) {
        View parentLayout = findViewById(android.R.id.content);

        Snackbar snackbar = Snackbar.make(parentLayout, body, Snackbar.LENGTH_LONG);
        snackbar.setAction("More Details", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Showing the detail of selected items
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("State: " + stateSpinner.getSelectedItem() + "\n");
                stringBuffer.append("Mayor: " + mayor + "\n");
                stringBuffer.append("DeputMayor: " + deputMayor + "\n");
                stringBuffer.append("Total population: " + population + "\n");
                stringBuffer.append("Total area: " + area + "\n");

                showMessage("Details", stringBuffer.toString());

            }
        });
        snackbar.setActionTextColor(getResources().getColor(android.R.color.holo_blue_light));
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void showMessage(String title, String message) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();


    }

    private void alternateChoice(final String district) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Nearby famous place");

        // Set up the input
        final EditText input = new EditText(MainActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                location = district + ", " + m_Text + ", " + "Nepal";
                //start
                try {
                    List<Address> addressList = geocoder.getFromLocationName(location, 1);
                    if (!addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        addMarker(latLng, address);
                    } else {

                        Toast.makeText(MainActivity.this, "No match found", Toast.LENGTH_SHORT).show();
                        //alternateChoice(district);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //end


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        boolean success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        if (mGoogleMap != null) {

            //restrict the user to go outside the given latlng
            //get latlong for corners for specified place
            LatLng one = new LatLng(27.037782, 78.947213);
            LatLng two = new LatLng(29.615528, 88.627444);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //add them to builder
            builder.include(one);
            builder.include(two);

            LatLngBounds bounds = builder.build();

            //get width and height to current display screen
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;

            // 20% padding
            int padding = (int) (width * 0.20);

            //set latlong bounds
            mGoogleMap.setLatLngBoundsForCameraTarget(bounds);

            //move camera to fill the bound to screen
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

            //set zoom to level to current so that you won't be able to zoom out viz. move outside bounds
            //mGoogleMap.setMinZoomPreference(mGoogleMap.getCameraPosition().zoom);
            mGoogleMap.setMinZoomPreference(6.0f);
            setUpMap();
        }


    }

    private void goToLocationZoom(double lat, double lng, int zoom) {
        final LatLng[] latLng = {new LatLng(lat, lng)};
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng[0], zoom);
        //add animation to zoom
        mGoogleMap.animateCamera(cameraUpdate, 1000, null);

        //zoom without animation
//        mGoogleMap.moveCamera(cameraUpdate);
//        mGoogleMap.setMinZoomPreference(10.0f);

//        LatLngBounds ADELAIDE = new LatLngBounds(
//                new LatLng(26.726658, 88.242621), new LatLng(29.774378, 80.311209));
//        // Constrain the camera target to the Adelaide bounds.
//        mGoogleMap.setLatLngBoundsForCameraTarget(ADELAIDE);
    }

    private void setUpMap() {

        if (ActivityCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest
                        .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Grant The permission first", Toast.LENGTH_LONG).show();
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                //save current locationq
                latLng = point;
                Log.d("latlng::", latLng.toString());

                try {
                    addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                    android.location.Address address = addresses.get(0);
                    addMarker(point, address);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                checkNetConnection();
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest
                        .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(MainActivity.this, Manifest
                                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d("locationEmptyCheck:", location.toString());
                double latti, longi;
                if (location != null) {
                    latti = location.getLatitude();
                    longi = location.getLongitude();
                    Log.d("lattiAndLongi::", latti + ", " + longi);
                    LatLng latLng = new LatLng(latti, longi);

                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        android.location.Address address = addresses.get(0);
                        addMarker(latLng, address);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });


        //get location in another way
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. In some rare situations this can be null.
//                        if (location != null) {
//                           double latti,longi;
//                           latti=location.getLatitude();
//                           longi=location.getLongitude();
//                           LatLng latLng=new LatLng(latti,longi);
//                           addMarker(latLng);
//                        }
//                    }
//                });

    }

    private void addMarker(LatLng point, Address address) {

        //remove previously placed Marker
        if (marker != null) {
            marker.remove();
            marker = null;
        }

        //place marker where user just clicked
        String locality = address.getLocality();
        String adminArea = address.getAdminArea();
        String subAdminArea = address.getSubAdminArea();
        String country = address.getCountryName();
        Log.d("address", locality + ", " + adminArea + ", " + subAdminArea + ", " + country);
        MarkerOptions markerOptions = new MarkerOptions()
                .title(locality)
                .position(point)
                .snippet(adminArea + ", " + subAdminArea + ", " + country)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true);

        marker = mGoogleMap.addMarker(markerOptions);
        // marker.showInfoWindow();
        goToLocationZoom(point.latitude, point.longitude, 10);

        Toast.makeText(MainActivity.this, locality + "\n" + adminArea + ","
                + subAdminArea + ", " + ", " + country, Toast.LENGTH_SHORT).show();


        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                // isShowInfoWindow() always give false value so we use this method to show and hide infoWindow
                if (!isInfoWindowShown) {

                    marker.showInfoWindow();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    isInfoWindowShown = true;
                } else {

                    marker.hideInfoWindow();
                    isInfoWindowShown = false;
                }

                return true;
            }
        });
        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                Log.d("System out", "onMarkerDragEnd...");
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
                double latti, longi;
                if (arg0 != null) {
                    latti = arg0.getPosition().latitude;
                    longi = arg0.getPosition().longitude;
                    Log.d("lattiAndLongi::", latti + ", " + longi);
                    LatLng latLng = new LatLng(latti, longi);
                    try {
                        List<Address> addressess = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    android.location.Address address = addresses.get(0);
                    addMarker(latLng, address);
                }
            }

            @Override
            public void onMarkerDrag(Marker arg0) {

            }
        });

    }


    private void loadSpinnerData(final Spinner spinner, String url, final String whatToFind) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
//                    JSONObject jsonObject=new JSONObject(response);
//                        JSONArray jsonArray=jsonObject.getJSONArray("Name");
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add("Select One");
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String nameList = jsonObject1.getString(whatToFind);
                        arrayList.add(nameList);
                    }

                    spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayList));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                LatLng latLng = place.getLatLng();
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                android.location.Address address = addresses.get(0);
                addMarker(latLng, address);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void slideUp(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
        isUp = !isUp;
    }

    // slide the view from its current position to below itself
    public void slideDown(View view, View inView, View rView) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                  // fromXDelta
                0,                      // toXDelta
                0,                  // fromYDelta
                view.getHeight());              // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
//        TranslateAnimation inAnimate = new TranslateAnimation(
//                0,                  // fromXDelta
//                0,                      // toXDelta
//                0,                  // fromYDelta
//               0);              // toYDelta
//        inAnimate.setDuration(500);
//        inAnimate.setFillAfter(true);
//        inView.startAnimation(animate);
//        TranslateAnimation rAnimate = new TranslateAnimation(
//                0,                  // fromXDelta
//                0,                      // toXDelta
//                0,
//                0);// fromYDelta
//               // rView.getHeight());              // toYDelta
//        rAnimate.setDuration(500);
//        rAnimate.setFillAfter(true);
//        rView.startAnimation(animate);

        isUp = !isUp;
    }

    private void loadPhoto(Bitmap pic) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.image_viewer);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.image_viewer,
                (ViewGroup) findViewById(R.id.image_viewer_framelayout));
        ImageView image = layout.findViewById(R.id.touch_image_view);
//       image.setImageDrawable(imageView.getDrawable());
//        image.getLayoutParams().height = height;
//        image.getLayoutParams().width = width;
//        mAttacher = new PhotoViewAttacher(image);
        image.requestLayout();
        dialog.setContentView(layout);
        dialog.setCanceledOnTouchOutside(true);
        if (pic != null) {
            image.setImageBitmap(pic);
        } else {
            image.setImageResource(R.drawable.state_seven);
        }

        dialog.show();

    }
}




