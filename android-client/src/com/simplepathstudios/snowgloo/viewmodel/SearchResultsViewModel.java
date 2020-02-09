package com.simplepathstudios.snowgloo.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.SearchResults;
import com.simplepathstudios.snowgloo.api.model.ServerInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsViewModel extends ViewModel {
    public MutableLiveData<SearchResults> Data;
    public SearchResultsViewModel(){
        Data = new MutableLiveData<>();
    }

    public void load(String query) {
        if(query.isEmpty() || query.length() == 0){
            return;
        }
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().search(query).enqueue(new Callback<SearchResults>() {

            @Override
            public void onResponse(Call<SearchResults> call, Response<SearchResults> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<SearchResults> call, Throwable t) {
                LoadingIndicator.setLoading(false);
            }
        });
    }
}
