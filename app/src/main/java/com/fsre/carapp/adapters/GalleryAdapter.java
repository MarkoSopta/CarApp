package com.fsre.carapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fsre.carapp.R;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private List<File> imageFiles;
    private Context context;
    private OnImageDeleteListener deleteListener;
    private OnImageEditListener editListener;

    public GalleryAdapter(List<File> imageFiles, Context context, OnImageDeleteListener deleteListener, OnImageEditListener editListener) {
        this.imageFiles = imageFiles;
        this.context = context;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);
        Glide.with(context)
                .load(imageFile)
                .into(holder.imageView);

        holder.deleteButton.setOnClickListener(v -> deleteListener.onImageDelete(imageFile));
        holder.editButton.setOnClickListener(v -> editListener.onImageEdit(imageFile));
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteButton;
        ImageView editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    public interface OnImageDeleteListener {
        void onImageDelete(File imageFile);
    }

    public interface OnImageEditListener {
        void onImageEdit(File imageFile);
    }
}