package com.oddsoft.pickashop.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.oddsoft.pickashop.Fragments.CompanyHomeFragment;
import com.oddsoft.pickashop.Global.Constants;
import com.oddsoft.pickashop.HomeActivity;
import com.oddsoft.pickashop.Models.CompanyDetails;
import com.oddsoft.pickashop.Models.SearchResult;
import com.oddsoft.pickashop.Network.Response;
import com.oddsoft.pickashop.Network.Url;
import com.oddsoft.pickashop.Network.WebServicesInterface;
import com.oddsoft.pickashop.Network.webServiceFactory;
import com.oddsoft.pickashop.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by afsal on 8/6/15.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    Context context;
    private ArrayList<SearchResult> results;
    private int lastPosition = -1;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SearchResultAdapter(ArrayList<SearchResult> results, Context mContext) {
        this.results = results;
        this.context = mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_cell, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void onItemClick(View view, int pos) {
                if (view.getId() == R.id.comp_phone1) {
                    String m = "tel:" + results.get(pos).company_phone1;
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse(m));
                    context.startActivity(callIntent);
                } else if (view.getId() == R.id.comp_phone2) {
                    String m = "tel:" + results.get(pos).company_phone2;
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse(m));
                    context.startActivity(callIntent);
                } else {
                    new GetCompDetails(results.get(pos)).execute(Url.COMP_URL, "picktag=basic_details&company_id=" + results.get(pos).company_id);
                }
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mName.setText(results.get(position).company_name);
        holder.mCity.setText(results.get(position).company_city);
        holder.mPhone1.setText(results.get(position).company_phone1);
        holder.mPhone2.setText(results.get(position).company_phone2);
        holder.mtype.setText(results.get(position).company_type);
        try {
            Glide.with(context).load(results.get(position).company_image)
                    .error(R.drawable.banner)
                    .placeholder(R.drawable.banner)
                    .into(holder.mProfileImg);
        } catch (IllegalArgumentException e) {

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return results.size();
    }


    public void startDetails(Context context, SearchResult model, CompanyDetails result) {
        CompanyHomeFragment mCompanyHomeFragment;
        FragmentManager frMng = ((HomeActivity) context).getSupportFragmentManager();

        Fragment fr = frMng.findFragmentByTag(Constants.COMPANY_HOME_FRAGMENT_TAG);
        if (fr != null) {
            mCompanyHomeFragment = (CompanyHomeFragment) fr;
        } else {
            mCompanyHomeFragment = new CompanyHomeFragment();
        }
//        mCompanyHomeFragment = new CompanyHomeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("Selected", model);
        bundle.putSerializable("Company", result);
        mCompanyHomeFragment.setArguments(bundle);
        ((HomeActivity) context).setFragmentOthers(mCompanyHomeFragment, Constants.COMPANY_HOME_FRAGMENT_TAG);

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mName;
        public TextView mCity;
        public TextView mtype;
        public TextView mPhone1;
        public TextView mPhone2;
        public ImageView mProfileImg;

        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            mListener = listener;
            mName = (TextView) v.findViewById(R.id.comp_name);
            mCity = (TextView) v.findViewById(R.id.comp_city);
            mtype = (TextView) v.findViewById(R.id.comp_type);
            mPhone1 = (TextView) v.findViewById(R.id.comp_phone1);
            mPhone2 = (TextView) v.findViewById(R.id.comp_phone2);

            mProfileImg = (ImageView) v.findViewById(R.id.comp_img);
            v.setOnClickListener(this);
            mPhone1.setOnClickListener(this);
            mPhone2.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int itemPosition = getAdapterPosition();
            mListener.onItemClick(view, itemPosition);
//
        }

        public interface IMyViewHolderClicks {
            void onItemClick(View view, int pos);
        }
    }

    private class GetCompDetails extends
            AsyncTask<String, Void, Response<CompanyDetails>> {
        ProgressDialog dialog;
        SearchResult model;

        public GetCompDetails(SearchResult model) {
            this.model = model;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressBar2.setVisibility(View.VISIBLE);
            dialog = new ProgressDialog(context);
            dialog.setMessage("Loading...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Response<CompanyDetails> doInBackground(
                String... params) {

            Response<CompanyDetails> response = new Response<CompanyDetails>();
            WebServicesInterface serviceImpl = webServiceFactory
                    .getWebService(context);
            String url = params[0];
            try {
                response = serviceImpl.getCompnyDetails(url, params[1]);
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
        protected void onPostExecute(Response<CompanyDetails> response) {
            super.onPostExecute(response);
//            progressBar2.setVisibility(View.GONE);
            dialog.dismiss();
            if (response.isSuccess()) {
                CompanyDetails results = response.getResult();
                startDetails(context, model, results);
            } else {
                Toast.makeText(context, response.getServerMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

}
