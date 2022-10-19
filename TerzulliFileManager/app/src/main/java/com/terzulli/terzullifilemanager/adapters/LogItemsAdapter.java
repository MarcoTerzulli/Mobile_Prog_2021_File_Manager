package com.terzulli.terzullifilemanager.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.database.entities.TableLog;
import com.terzulli.terzullifilemanager.fragments.MainFragment;

import java.io.File;
import java.util.ArrayList;

public class LogItemsAdapter extends RecyclerView.Adapter<LogItemsAdapter.ItemsViewHolder> {
    private final Context context;
    private final MainFragment mainFragment;
    private final Activity activityReference;
    private ArrayList<TableLog> logsList;

    public LogItemsAdapter(Context context, ArrayList<TableLog> logsList, MainFragment mainFragment, Activity activityReference) {
        this.context = context;
        this.mainFragment = mainFragment;
        this.activityReference = activityReference;

        this.logsList = new ArrayList<>(logsList.size());
        this.logsList.addAll(logsList);
    }

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_for_list, parent, false);

        return new ItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {
        if (logsList == null || logsList.size() == 0) {
            return;
        }

        TableLog selectedFile = logsList.get(position);

        //icona
        //setItemIcon(selectedFile, holder, false);

        // background
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        // dettagli
        holder.itemName.setText(selectedFile.getOperationType());
        holder.itemDetails.setVisibility(View.VISIBLE);


        /*holder.itemName.setText(selectedFile.getName());
        if (selectedFile.isDirectory()) {
            // questo permette di centrare la textview con il nome all'interno del container
            holder.itemDetails.setText("");
            holder.itemDetails.setVisibility(View.GONE);
        } else {
            holder.itemDetails.setText(formatFileDetails(selectedFile));
            holder.itemDetails.setVisibility(View.VISIBLE);
        }*/

        holder.itemView.setOnClickListener(view -> {

        });

        // gestione selezione item con long click
        holder.itemView.setOnLongClickListener(view -> {

            return true;
        });

        // gestione selezione item con click sull'icona
        //holder.itemIcon.setOnClickListener(view -> toggleItemSelection(selectedFile, holder, true, false));
        //holder.itemIcon.setOnClickListener(view -> toggleItemSelection(selectedFile, holder));

        // ripristino lo stato di selezione precedente
        //toggleItemSelection(selectedFile, holder, false, true);
        //recoverItemSelectionState(selectedFile, holder);
    }

    private void itemOpenerHandler(File selectedFile) {
        // se il file è una directory, viene caricato il path
        // in caso contrario, si avvia un intent per l'apertura del file

        /*if (selectedFile.isDirectory()) {
            clearSelection();
            if(activityReference instanceof MainActivity)
                ((MainActivity)activityReference).closeSearchView();
            mainFragment.loadPath(selectedFile.getAbsolutePath(), true, false);
        } else if (selectedFile.isFile()) {

            MimeTypeMap map = MimeTypeMap.getSingleton();
            String ext = Utils.getFileExtension(selectedFile);
            String type = map.getMimeTypeFromExtension(ext);

            if (type == null) {
                Toast.makeText(context, R.string.cant_open_file, Toast.LENGTH_SHORT).show();
            } else {
                RecentsFilesManager recentsFilesManager = new RecentsFilesManager(context.getSharedPreferences("TerzulliFileManager", MODE_PRIVATE));
                recentsFilesManager.addFileToRecentsFilesList(selectedFile);

                if (isFileAZipArchive(selectedFile)) {
                    //fileToExtract = selectedFile;
                    selectedFilesManager.setFileToExtract(selectedFile);
                    extractSelectedFile();
                } else if (type.equals("application/vnd.android.package-archive")
                        || type.equals("application/zip") || type.equals("application/java-archive")) {

                    installApplication(selectedFile);
                } else {

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        Uri data = FileProvider.getUriForFile(context,
                                context.getApplicationContext().getPackageName() + ".provider", selectedFile);

                        intent.setDataAndType(data, type);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.cant_open_file, Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }*/
    }

    private void setItemIcon(File selectedFile, @NonNull ItemsViewHolder holder, boolean isSelected) {

        /*if (!isSelected) {
            if (Utils.fileIsImage(selectedFile)) {
                loadImageThumbnailAsync(selectedFile, holder);
            } else
                holder.itemIcon.setImageResource(Utils.getFileTypeIcon(selectedFile));
        } else {
            holder.itemIcon.setImageResource(R.drawable.ic_check_circle_filled);
        }*/

    }



    @Override
    public int getItemCount() {
        if (logsList == null)
            return 0;
        return logsList.size();
    }

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {
        private final ImageView itemIcon;
        private final TextView itemName, itemDetails;
        private final RelativeLayout itemHolder, itemIconContainer, itemTextContainer;

        public ItemsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
            itemDetails = itemView.findViewById(R.id.item_details);
            itemHolder = itemView.findViewById(R.id.item_holder);
            itemIconContainer = itemView.findViewById(R.id.item_icon_container);
            itemTextContainer = itemView.findViewById(R.id.item_text_container);
        }
    }
}
