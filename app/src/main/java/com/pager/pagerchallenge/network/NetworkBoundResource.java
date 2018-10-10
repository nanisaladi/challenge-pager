package com.pager.pagerchallenge.network;

/**
 * Created by Venkat on 09/10/18.
 */

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.pager.pagerchallenge.network.util.Resource;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Network Bound Resource API to save data from remote to local DB.
 */
public abstract class NetworkBoundResource<ResultType, RequestType> {
    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();
    private  static final String TAG = NetworkBoundResource.class.getSimpleName();

    @MainThread
    public NetworkBoundResource() {
        setValue(Resource.loading(null));
        final LiveData<ResultType> dbSource = loadFromDb();
        Log.d(TAG, "Data source: "+dbSource.getValue());
        result.addSource(dbSource, new Observer<ResultType>() {
            @Override
            public void onChanged(@Nullable ResultType resultType) {
                Log.d(TAG, "In loop data source: "+dbSource);
                result.removeSource(dbSource);
                if (shouldFetch(resultType)) {
                    Log.d(NetworkBoundResource.class.getSimpleName(), "About to fetch from NetworkBoundResource");
                    fetchFromNetwork(dbSource);
                } else {
                    Log.d(NetworkBoundResource.class.getSimpleName(), "Data already present");
                    result.addSource(dbSource, resultType1 -> setValue(Resource.success(resultType1)));
                }
            }
        });
    }

    private void fetchFromNetwork(final LiveData<ResultType> dbSource) {
        result.addSource(dbSource, newData -> setValue(Resource.loading(newData)));
        createCall().enqueue(new Callback<RequestType>() {
            @Override
            public void onResponse(Call<RequestType> call, Response<RequestType> response) {
                result.removeSource(dbSource);
                saveResultAndReInit(response.body());
            }

            @Override
            public void onFailure(Call<RequestType> call, Throwable t) {
                onFetchFailed();
                result.removeSource(dbSource);
                result.addSource(dbSource, newData -> setValue(Resource.error(t.getMessage(), newData)));
            }
        });
    }

    @MainThread
    protected void onFetchFailed() {
        Log.i(NetworkBoundResource.class.getSimpleName(), "Network call failed");
    }

    @MainThread
    private void saveResultAndReInit(RequestType body) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                saveCallResult(body);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                result.addSource(loadFromDb(), newData -> setValue(Resource.success(newData)));
            }
        }.execute();
    }

    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestType body);

    @NonNull
    @MainThread
    protected abstract Call<RequestType> createCall();

    private void setValue(Resource<ResultType> newData) {
        if (!Objects.equals(result.getValue(), newData)) {
            result.setValue(newData);
        }
    }

    @MainThread
    protected abstract boolean shouldFetch(ResultType resultType);

    @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    public final LiveData<Resource<ResultType>> getAsLiveData() {
        return result;
    }
}
