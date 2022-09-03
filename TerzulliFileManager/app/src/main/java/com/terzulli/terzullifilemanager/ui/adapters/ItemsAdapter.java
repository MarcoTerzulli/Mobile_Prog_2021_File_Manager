package com.terzulli.terzullifilemanager.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;

import java.io.File;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder> {
    private Context context;
    private File[] filesAndDirs;

    public ItemsAdapter(Context context, File[] filesAndFolders) {
        this.context = context;
        this.filesAndDirs = filesAndFolders;

    }

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file_for_list, parent, false);

        return new ItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {
        if (filesAndDirs == null || filesAndDirs.length == 0) {
            return;
        }

        File selectedFile = filesAndDirs[position];

        // dettagli
        holder.item_name.setText(selectedFile.getName());
        holder.item_details.setText(selectedFile.getName());

        // icona
        if (selectedFile.isDirectory()) {
            holder.item_icon.setImageResource(R.drawable.ic_folder);
        } else if (selectedFile.isFile()) {
            // TODO icone varie in base all'estensione
            holder.item_icon.setImageResource(R.drawable.ic_file_generic);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFile.isDirectory()) {
                    //MainFragment.loadPath(selectedFile.getAbsolutePath());
                } else if (selectedFile.isFile()) {
                    // TODO icone varie in base all'estensione
                    String fileType = "image/*";

                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(selectedFile.getAbsolutePath()), fileType);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {

                    }
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return filesAndDirs.length;
    }

    public class ItemsViewHolder extends RecyclerView.ViewHolder {
        private ImageView item_icon;
        private TextView item_name, item_details;

        public ItemsViewHolder(@NonNull View itemView) {
            super(itemView);

            item_icon = itemView.findViewById(R.id.item_icon);
            item_name = itemView.findViewById(R.id.item_name);
            item_details = itemView.findViewById(R.id.item_details);
        }
    }
}
