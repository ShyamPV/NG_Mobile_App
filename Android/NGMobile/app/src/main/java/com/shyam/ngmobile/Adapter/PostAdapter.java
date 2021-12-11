package com.shyam.ngmobile.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shyam.ngmobile.Model.Post;
import com.shyam.ngmobile.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);

    ArrayList<Post> postList;
    private OnItemClickListener listener;

    public PostAdapter(ArrayList<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @NotNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View postCard = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item_view, parent, false);

        return new PostHolder(postCard);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PostAdapter.PostHolder holder, int position) {
        Post post = postList.get(position);
        holder.setDetails(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public class PostHolder extends RecyclerView.ViewHolder {

        TextView postTitle, postDate;
        ImageView image;

        public PostHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            postTitle = itemView.findViewById(R.id.post_title_text);
            postDate = itemView.findViewById(R.id.post_date_text);
            image = itemView.findViewById(R.id.post_image_view);
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION || listener != null) {
                    listener.onItemClick(postList.get(position));
                }
            });
        }

        public void setDetails(Post post) {
            if (!post.getImageURL().equals("")) {
                Picasso.get().load(post.getImageURL()).into(image);
            }else{
                image.setImageResource(R.drawable.ng_logo_transparent);
            }
            postTitle.setText(post.getTitle());
            postDate.setText(String.format("Date: %s", dateFormatter.format(post.getStartTime())));
        }
    }


}
