package com.shyam.ngmobile.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.shyam.ngmobile.Adapter.PostAdapter;
import com.shyam.ngmobile.Model.Post;
import com.shyam.ngmobile.PostDetailActivity;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Services.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment {

    private RecyclerView postRecyclerView;
    private PostAdapter adapter;
    private LinearLayoutManager layoutManager;
    private View view;
    private TextView title;
    private static final String POST_ID = "postID";
    private CollectionReference postRef;
    private DocumentSnapshot lastVisible;
    private int pageListLimit;
    private boolean isScrolling;
    private boolean isLastItemReached;
    private ArrayList<Post> postList;
    private SweetAlertDialog pDialog;


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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        postRef = db.collection("post");
        pageListLimit = 10;


        pDialog = new SweetAlertDialog(view.getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Getting Posts...");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
        pDialog.setCancelable(false);


        Query query = postRef.orderBy("startTime", Query.Direction.DESCENDING).limit(pageListLimit);

        getPosts(query);

        return view;
    }

    private void getPosts(Query query) {
        pDialog.show();

        Post.GetQueryPosts(query, (postArraylist, lastSnapshot) -> {
            if (postArraylist.size() > 0) {
                postList = new ArrayList<>(postArraylist);
                lastVisible = lastSnapshot;
                setRecyclerDetails(postList, query);
            } else {
                pDialog.dismiss();
                Utils.displayMessage(getActivity(), "Error!", "No Club Updates found");
            }
        });
    }

    private void setRecyclerDetails(ArrayList<Post> postList, Query query) {
        adapter = new PostAdapter(postList);

        postRecyclerView.setHasFixedSize(true);
        postRecyclerView.setLayoutManager(layoutManager);
        postRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(post -> {
            showPostDetails(post.getPostID());
        });

        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();

                if (isScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !isLastItemReached) {

                    isScrolling = false;

                    Query nextQuery = query.startAfter(lastVisible);

                    Post.GetQueryPosts(nextQuery, (postArraylist, lastSnapshot) -> {
                        postList.addAll(postArraylist);
                        adapter.notifyDataSetChanged();

                        if (postArraylist.size() > 0) {
                            lastVisible = lastSnapshot;
                        }

                        if (postArraylist.size() < pageListLimit) {
                            isLastItemReached = true;
                            Toast.makeText(getContext(), "Last Post Reached", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };

        postRecyclerView.addOnScrollListener(onScrollListener);

        pDialog.dismiss();
    }

    private void showPostDetails(String postID) {
        startActivity(new Intent(view.getContext(), PostDetailActivity.class).putExtra(POST_ID, postID));
    }


}
