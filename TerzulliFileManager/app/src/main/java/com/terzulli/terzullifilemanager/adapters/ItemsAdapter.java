package com.terzulli.terzullifilemanager.adapters;

import static com.terzulli.terzullifilemanager.utils.Utils.formatFileDetails;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.Utils;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_for_list, parent, false);

        return new ItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {
        if (filesAndDirs == null || filesAndDirs.length == 0) {
            return;
        }

        File selectedFile = filesAndDirs[position];

        //icona
        if (Utils.fileIsImage(selectedFile))
            holder.item_icon.setImageBitmap(loadImagePreview(selectedFile));
        else
            holder.item_icon.setImageResource(Utils.getFileTypeIcon(selectedFile));

        // dettagli
        holder.item_name.setText(selectedFile.getName());
        if (selectedFile.isDirectory()) {
            // questo permette di centrare la textview con il nome all'interno del container
            holder.item_details.setText("");
            holder.item_details.setVisibility(View.GONE);
        } else {
            holder.item_details.setText(formatFileDetails(selectedFile));
            holder.item_details.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedFile.isDirectory()) {
                    MainFragment.loadPath(selectedFile.getAbsolutePath());
                } else if (selectedFile.isFile()) {

                    MimeTypeMap map = MimeTypeMap.getSingleton();
                    String ext = Utils.getFileExtension(selectedFile);
                    String type = map.getMimeTypeFromExtension(ext);

                    //if (type == null)
                    //type = "*/*";

                    if (type == null)
                        Toast.makeText(context, R.string.cant_open_file, Toast.LENGTH_SHORT).show();
                    else {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri data = Uri.parse(selectedFile.getAbsolutePath());

                            intent.setDataAndType(data, type);
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.cant_open_file, Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });

    }

    private Bitmap loadImagePreview(File file) {
        if (file == null)
            return null;

        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }

        return null;
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
