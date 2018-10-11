package com.pager.pagerchallenge;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.squareup.moshi.Moshi;

import java.util.Collection;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * View Model to fetch the data from remote
 */
public class UsersViewModel extends ViewModel {

    private MutableLiveData<Collection<User>> users;

    HashMap<String, User> userHashMap = new HashMap<>();

    public LiveData<Collection<User>> getUsers() {
        if (users == null) {
            users = new MutableLiveData<>();
            loadUsers();
        }
        return users;
    }

    private void loadUsers() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pager-team.herokuapp.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create()).build();
        CompositeDisposable disposable = new CompositeDisposable();
        RealTeamRepository realTeamRepository = new RealTeamRepository(retrofit);
        RealGetUsersUseCase useCase =
                new RealGetUsersUseCase(new HttpRolesRepository(retrofit), realTeamRepository,
                        new SocketRepository("http://ios-hiring-backend.dokku.canillitapp.com", new OkHttpClient(),
                                new Moshi.Builder().build()));

        disposable.add(
                useCase.exec().
                        subscribeOn(Schedulers.computation()).
                        observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                                    Log.v("User", user.toString());
                                    userHashMap.put(user.name(), user);
                                    users.setValue(userHashMap.values());
                                },
                                throwable -> Log.v("Error", throwable.toString()))
        );
    }
}
