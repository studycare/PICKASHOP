package com.oddsoft.pickashop.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.oddsoft.pickashop.Adapter.AutoCompleteSuggestionAdapter;
import com.oddsoft.pickashop.Adapter.PopularAdapter;
import com.oddsoft.pickashop.Global.Constants;
import com.oddsoft.pickashop.Global.Utils;
import com.oddsoft.pickashop.HomeActivity;
import com.oddsoft.pickashop.Models.Popular;
import com.oddsoft.pickashop.Models.SearchResult;
import com.oddsoft.pickashop.Models.SuggestionModel;
import com.oddsoft.pickashop.Network.JsonParser;
import com.oddsoft.pickashop.Network.Response;
import com.oddsoft.pickashop.Network.Url;
import com.oddsoft.pickashop.Network.WebServicesInterface;
import com.oddsoft.pickashop.Network.webServiceFactory;
import com.oddsoft.pickashop.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class HomeFragment extends Fragment {

    public static boolean stopMove = true;
    GetPopularBrands getPopularBrands;
    GetSearchResult getSearchResult;
    ArrayList<Popular> populars;
    int pos = 0, increament = 1;
    Handler handler;
    Runnable runnable;
    PopularAdapter adapter;
    ProgressBar progressBar, progressBar2;
    AutoCompleteTextView location, product;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout_white_bg for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home2, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.home_list);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        populars = new ArrayList<Popular>();

        adapter = new PopularAdapter(populars, getActivity());
        mRecyclerView.setAdapter(adapter);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        progressBar2 = (ProgressBar) rootView.findViewById(R.id.progress2);
        getPopularBrands = new GetPopularBrands();
        getPopularBrands.execute(Url.HOME_POPULAR_URL);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (stopMove) {
                if (pos == populars.size() - 1) {
                    increament = -1;
                } else if (pos == 0) {
                    increament = 1;
                }
                pos = pos + increament;
                    mRecyclerView.smoothScrollToPosition(pos);
                    handler.postDelayed(runnable, 1500);
                }
            }
        };
        handler.postDelayed(runnable, 1500);

        location = (AutoCompleteTextView) rootView.findViewById(R.id.search_edit);
        product = (AutoCompleteTextView) rootView.findViewById(R.id.search_for);

        ArrayList<SuggestionModel> locations, categories;
        locations = JsonParser.getLocations(Utils.getStringSharedPreference(getActivity(), Constants.SHARED_SEARCH_KEYS));
        categories = JsonParser.getCategories(Utils.getStringSharedPreference(getActivity(), Constants.SHARED_SEARCH_KEYS));
        AutoCompleteSuggestionAdapter adapter = new AutoCompleteSuggestionAdapter(getActivity(), locations);
        AutoCompleteSuggestionAdapter adapterCategory = new AutoCompleteSuggestionAdapter(getActivity(), categories);
        location.setThreshold(1);
        product.setThreshold(1);

        location.setAdapter(adapter);
        product.setAdapter(adapterCategory);

        rootView.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressBar2.getVisibility() == View.GONE) {
                    String url = Url.SEARCH_URL;
                    String params = "picktag=search_result&" + "location=" + location.getText().toString()
                            + "&keywords=" + product.getText().toString() + "&type=shops&page=1&perPage=10";
                    getSearchResult = new GetSearchResult();
                    getSearchResult.execute(url, params);
                }
            }
        });
//


//        String[] language ={"C","C++","Java",".NET","iPhone","Android","ASP.NET","PHP"};
//        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(),android.R.layout.select_dialog_item,language);
//        location.setAdapter(adapter2);
//        product.setAdapter(adapter2);
        return rootView;
    }

    @Override
    public void onResume() {
        stopMove = true;
        super.onResume();
    }

    public void startSearchResult(ArrayList<SearchResult> results) {
        SearchResultFragment mSearchResult;
        FragmentManager frMng = getActivity().getSupportFragmentManager();

//        Fragment fr = frMng.findFragmentByTag(Constants.SEARCH_FRAGMENT_TAG);
//        if (fr != null) {
//            mSearchResult = (SearchResultFragment) fr;
//        } else {
//            mSearchResult = new SearchResultFragment();
//        }
        mSearchResult = new SearchResultFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("RESULT", results);
        bundle.putString("Location", location.getText().toString());
        bundle.putString("Shop", product.getText().toString());
        mSearchResult.setArguments(bundle);
        ((HomeActivity) getActivity()).setFragmentOthers(mSearchResult, Constants.SEARCH_FRAGMENT_TAG);

    }

    public void checkUpdate() {
        int currentVersion = 1;
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            currentVersion = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (currentVersion < HomeActivity.PLAYSTORE_VERSION) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Upgrade");
            builder.setMessage("Update available, ready to upgrade?");
            builder.setIcon(R.drawable.pkicon);
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.oddsoft.pickashop"));
                    getActivity().startActivity(intent);
                }
            });
            builder.setNegativeButton("Nop", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();


        }
    }

    private class GetPopularBrands extends
            AsyncTask<String, Void, Response<ArrayList<Popular>>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Response<ArrayList<Popular>> doInBackground(
                String... params) {

            Response<ArrayList<Popular>> response = new Response<ArrayList<Popular>>();
            WebServicesInterface serviceImpl = webServiceFactory
                    .getWebService(getActivity());
            String url = params[0];
            try {
                response = serviceImpl.getPopularBrands(url);
            } catch (JSONException e) {
                response.setThrowable(e);
                e.printStackTrace();
            } catch (IOException e) {
                response.setThrowable(e);
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(Response<ArrayList<Popular>> response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);

            if (response.isSuccess()) {
                populars.removeAll(populars);
                populars.clear();
                populars.addAll(response.getResult());
                adapter.notifyDataSetChanged();
                checkUpdate();
            } else {
                Toast.makeText(getActivity(),response.getServerMessage(),Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class GetSearchResult extends
            AsyncTask<String, Void, Response<ArrayList<SearchResult>>> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressBar2.setVisibility(View.VISIBLE);
            dialog = new ProgressDialog(getActivity());
            dialog.setCancelable(true);
            dialog.setMessage("Loading...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Response<ArrayList<SearchResult>> doInBackground(
                String... params) {

            Response<ArrayList<SearchResult>> response = new Response<ArrayList<SearchResult>>();
            WebServicesInterface serviceImpl = webServiceFactory
                    .getWebService(getActivity());
            String url = params[0];
            try {
                response = serviceImpl.getSearchResult(url, params[1]);
            } catch (JSONException e) {
                response.setThrowable(e);
                e.printStackTrace();
            } catch (IOException e) {
                response.setThrowable(e);
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(Response<ArrayList<SearchResult>> response) {
            super.onPostExecute(response);
//            progressBar2.setVisibility(View.GONE);
            dialog.dismiss();
            if (response.isSuccess()) {
                ArrayList<SearchResult> results = response.getResult();
                if (results.size() > 0) {
                    startSearchResult(results);
                } else {
                    Toast.makeText(getActivity(), "No Result Found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), response.getServerMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
