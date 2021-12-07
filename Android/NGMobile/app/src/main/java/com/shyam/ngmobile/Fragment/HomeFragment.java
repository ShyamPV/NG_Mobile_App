package com.shyam.ngmobile.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shyam.ngmobile.Adapter.PostAdapter;
import com.shyam.ngmobile.Model.Post;
import com.shyam.ngmobile.PostDetailActivity;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Services.Utils;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private RecyclerView postRecyclerView;
    private PostAdapter adapter;
    private LinearLayoutManager layoutManager;
    private View view;
    private TextView title;
    private static final String POST_ID = "postID";

    @Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);

        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        title = view.findViewById(R.id.home_title);
        title.setText(Utils.getCurrentMember().getFullName());

        layoutManager = new LinearLayoutManager(view.getContext());

        setRecyclerDetails();

        return view;
    }

    private void setRecyclerDetails() {

        ArrayList<Post> posts = getPosts();
        adapter = new PostAdapter(posts);

        postRecyclerView.setHasFixedSize(true);
        postRecyclerView.setLayoutManager(layoutManager);
        postRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(post -> {
            showPostDetails(post.getPostID());
        });
    }

    private void showPostDetails(String postID) {
        startActivity(new Intent(view.getContext(), PostDetailActivity.class).putExtra(POST_ID, postID));
    }

    private ArrayList<Post> getPosts() {
        Calendar date = Calendar.getInstance();

        ArrayList<Post> posts = new ArrayList<>();

        date.set(2021, 11, 31);
        posts.add(new Post("", "New Years Celebration",
                "This is party of the new year of 2022", date.getTime(),
                "", ""));
        date.set(2021, 11, 12);
        posts.add(new Post("", "Music Night",
                "This is party of the new year of 2022", date.getTime(),
                "", ""));
        date.set(2021, 4, 8);
        posts.add(new Post("", "Club AGM",
                "This is party of the new year of 2022", date.getTime(),
                "", ""));
        date.set(2021, 2, 8);
        posts.add(new Post("", "Cricket Finals",
                "This is party of the new year of 2022", date.getTime(),
                "", ""));
        date.set(2020, 0, 31);
        posts.add(new Post("", "Club Event",
                "This is party of the new year of 2022", date.getTime(),
                "", ""));

        return posts;
    }
}
