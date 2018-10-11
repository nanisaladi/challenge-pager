package com.pager.pagerchallenge;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.list_content)
  RecyclerView recyclerView;

  UsersViewModel viewModel;
  List<User> teamResponses = new ArrayList<>();
  ListAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    adapter = new ListAdapter(getApplicationContext(), teamResponses);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    viewModel = ViewModelProviders.of(this).get(UsersViewModel.class);
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewModel.getUsers().observe(this,
            users -> {
      if(users != null) {
        Log.v("MainActivity", "size: "+users.size());
        teamResponses.clear();
        teamResponses.addAll(users);
        adapter.notifyDataSetChanged();
      } else {
        Log.v("MainActivity", "NULL");
      }
    });
  }

  /**
   * List view adapter to set data. This logic should be moved to {@link ListAdapter}
   */
  private static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ItemHodler> {

    private List<User> teamUsers = null;
    Context context;

    ListAdapter(Context context, List<User> teamUsers) {
      this.context = context;
      this.teamUsers = teamUsers;
    }

    @Override
    public ItemHodler onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
      return new ItemHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHodler holder, int position) {
      User user = teamUsers.get(position);
      if(user != null) {
        setText(holder.title, user.name());
        setText(holder.languages, user.languages());
        setText(holder.role, ""+user.role());
        setText(holder.skills, user.skills());
        setText(holder.location, user.location());
        setText(holder.status, user.status().isEmpty() ? "Not Available" : user.status());
      }
    }

    private void setText(TextView field, List<String> values) {
      if(values != null && !values.isEmpty()) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String v: values) {
          stringBuilder.append(v).append(",").append(" ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        field.setText(stringBuilder.toString());
      }
    }

    private void setText(TextView field, String name) {
      field.setText(name);
    }

    @Override
    public int getItemCount() {
      return teamUsers.size();
    }

    static class ItemHodler extends RecyclerView.ViewHolder {
      ImageView imageView;
      TextView title;
      TextView languages;
      TextView skills;
      TextView status;
      TextView role;
      TextView github;
      TextView location;

      ItemHodler(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.titleView);
        imageView = itemView.findViewById(R.id.image);
        languages = itemView.findViewById(R.id.language_value);
        skills = itemView.findViewById(R.id.skills_value);
        status = itemView.findViewById(R.id.status_value);
        role = itemView.findViewById(R.id.role);
        github = itemView.findViewById(R.id.github);
        location = itemView.findViewById(R.id.location_value);
      }
    }
  }
}
