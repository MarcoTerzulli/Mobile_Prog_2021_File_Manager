package com.terzulli.terzullifilemanager.ui.adapters;

import android.content.Context;
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
    private File[] filesAndFolders;

    public ItemsAdapter(Context context, File[] filesAndFolders) {
        this.context = context;
        this.filesAndFolders = filesAndFolders;

    }

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file_for_list, parent, false);

        return new ItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {
        if (filesAndFolders == null || filesAndFolders.length == 0) {
            return;
        }

        File selectedFile = filesAndFolders[position];

        holder.item_name.setText(selectedFile.getName());

        // icona
        if (selectedFile.isDirectory()) {
            holder.item_icon.setImageResource(R.drawable.ic_folder);
        } else if (selectedFile.isFile()) {
            // TODO icone varie in base all'estensione
            holder.item_icon.setImageResource(R.drawable.ic_file_generic);
        }

        holder.item_details.setText(selectedFile.getName());


    }

    @Override
    public int getItemCount() {
        return filesAndFolders.length;
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
