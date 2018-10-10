package com.pager.pagerchallenge.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.pager.pagerchallenge.TeamService;

import java.util.List;

/**
 * Created by Venkat on 09/10/18.
 */
@Dao
public interface UserDao {

    @Query("SELECT * from Team")
    LiveData<TeamService.TeamResponse> getTeam();

    @Insert
    void insertAll(List<TeamService.TeamResponse> teamResponses);
}
