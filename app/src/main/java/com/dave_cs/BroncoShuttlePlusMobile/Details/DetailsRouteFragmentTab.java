package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoService;
import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.JacksonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsRouteFragmentTab extends android.support.v4.app.Fragment {

    private ArrayList<SimpleRouteInfo> listItems = new ArrayList<>();
    private SimpleRouteInfoAdapter adapter;

    @Override
    @SuppressWarnings("Unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            propagateRoutes();
        else
            listItems.add(new SimpleRouteInfo());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.details_route_fragment_layout, container, false);
        adapter = new SimpleRouteInfoAdapter(this.getContext(), listItems);

        final ListView listView = (ListView) v.findViewById(R.id.routeList);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleRouteInfo info = (SimpleRouteInfo) listView.getItemAtPosition(position);
                android.support.v4.app.Fragment newFrag = new DetailsAdvRouteFragmentTab();
                Bundle bundle = new Bundle();
                bundle.putString("routeName", info.getRouteName());
                newFrag.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.tabcontent,newFrag,info.getRouteName() + "")
                        .addToBackStack("simpleRoute")
                        .commit();
            }
        });
        return v;
    }

    private void propagateRoutes(){
        // reach to server and pull route info
        String[] routes = {"A", "B1", "B2", "C"};
        for(String str: routes) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://dave-cs.com")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            SimpleRouteInfoService simpleRouteInfoService = retrofit.create(SimpleRouteInfoService.class);
            Call<SimpleRouteInfo> call = simpleRouteInfoService.getInfo(str);
            call.enqueue(new Callback<SimpleRouteInfo>() {

                @Override
                public void onResponse(Response<SimpleRouteInfo> response) {
                    if (response.isSuccess()) {
                        listItems.add(response.body());
                        Collections.sort(listItems);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("<Error>","no access to Internet");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("<Error>",t.getLocalizedMessage());
                }
            });
        }
    }
}