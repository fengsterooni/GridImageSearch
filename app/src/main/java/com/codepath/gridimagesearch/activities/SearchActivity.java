package com.codepath.gridimagesearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.codepath.gridimagesearch.R;
import com.codepath.gridimagesearch.adapters.ImageResultsAdapter;
import com.codepath.gridimagesearch.fragments.FilterDialog;
import com.codepath.gridimagesearch.listeners.EndlessScrollListener;
import com.codepath.gridimagesearch.models.ImageResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity implements FilterDialog.FilterChangedListener{
    private GridView gvResults;
    private TextView tvNoNetwork;
    private ArrayList<ImageResult> imageResults;
    private ImageResultsAdapter aImageResults;
    private AsyncHttpClient client;
    private String query;
    private final String searchUrlBase = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";

    private String imageSize = "any";
    private String imageColor = "any";
    private String imageType = "any";
    private String imageSite = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        // getSupportActionBar().setIcon(R.drawable.pictures);

        setupViews();

        // Setup AsyncHTTPClient
        client = new AsyncHttpClient();

        // Create the data source
        imageResults = new ArrayList<ImageResult>();
        // Attaches the data source to an adapter
        aImageResults = new ImageResultsAdapter(this, imageResults);
        // Link the adapter to the adapterview (gridView)
        gvResults.setAdapter(aImageResults);
        // Endless Scroll Listener
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                customLoadMoreDataFromApi(page);
                // or customLoadMoreDataFromApi(totalItemsCount);
            }
        });
    }

    private void setupViews() {
        gvResults = (GridView) findViewById(R.id.gvResults);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent
                Intent intent = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                // Get the image result to display
                ImageResult result = imageResults.get(position);
                // Pass image result into the intent
                //intent.putExtra("url", result.fullUrl);
                intent.putExtra("result", result);
                // Launch the new activity
                startActivity(intent);
            }
        });
        tvNoNetwork = (TextView) findViewById(R.id.tvNoNetwork);
    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int offset) {
        // This method probably sends out a network request and appends new data items to your adapter.
        // Use the offset value and add it as a parameter to your API request to retrieve paginated data.
        // Deserialize API response and then construct new objects to append to the adapter

        // Since Google Image Search API only allow 64 images, we shall stop requesting new
        // pages after the offset greater than 7
        if (offset > 7)
            return;

        performSearch(query, offset);
    }

    public void performSearch(String query, final int offset) {
        String imageString = buildFilterString().toString();
        Log.i("INFO", "Image String: " + imageString);
        String searchUrl = searchUrlBase + query + imageString + "&rsz=8" + "&start=" + offset * 8;
        Log.i("INFO", "Search URL: " + searchUrl);

        client.get(searchUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("DEBUG", response.toString());
                JSONArray imageResultJson = null;

                try {
                    imageResultJson = response.getJSONObject("responseData").getJSONArray("results");
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultJson));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i("INFO", imageResults.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String string) {
                // Check Internet Availability
                if (!isNetworkAvailable()) {
                    tvNoNetwork.setVisibility(View.VISIBLE);
                    return false;
                }

                tvNoNetwork.setVisibility(View.GONE);

                aImageResults.clear();
                imageResults.clear();
                query = string;
                // perform query here
                performSearch(string, 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Intent intent = new Intent(this, SettingActivity.class);
            // startActivityForResult(intent, SEARCH_SETTINGS);
            FragmentManager fm = getSupportFragmentManager();
            FilterDialog filterDialog = FilterDialog.newInstance("Some Title");
            filterDialog.show(fm, "Filter Fragment");
        }

        return super.onOptionsItemSelected(item);
    }

    public StringBuilder buildFilterString() {
        StringBuilder imageString = null;

        if (imageSize.equals("any")) imageString = new StringBuilder("&imgsz=small%7Cmedium%7Clarge%7Cxlarge");
        else
            imageString = new StringBuilder("&imgsz=" + imageSize);

        if (!imageColor.equals("any")) imageString.append("&imgcolor=" + imageColor);

        if (!imageType.equals("any")) imageString.append("&imgtype=" + imageType);

        if (imageSite != null) imageString.append("&as_sitesearch=" + imageSite);

        Log.i("INFO", "IMAGE_STRING   ------>>>>   " + imageString);

        return imageString;

    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onFinishFilterDialog(String imgSize, String imgColor, String imgType, String imgSite) {
        this.imageSize = imgSize;
        this.imageColor = imgColor;
        this.imageType = imgType;
        if (imgSize != null)
            this.imageSite = imgSite;

        Log.i("INFO", imageSize + " " + imageColor + " " + imageType + " " + imageSite + "HELLO");

        buildFilterString();
    }
}
