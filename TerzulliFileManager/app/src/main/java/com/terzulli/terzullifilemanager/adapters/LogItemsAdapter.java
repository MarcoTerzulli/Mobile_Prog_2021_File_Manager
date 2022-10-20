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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.database.LogDatabase;
import com.terzulli.terzullifilemanager.database.entities.TableLog;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class LogItemsAdapter extends RecyclerView.Adapter<LogItemsAdapter.ItemsViewHolder> {
    private final Context context;
    private final MainFragment mainFragment;
    private final Activity activityReference;
    private ArrayList<TableLog> logsList;
    private LogDatabase logDatabase;

    public LogItemsAdapter(Context context, ArrayList<TableLog> logsList, MainFragment mainFragment, Activity activityReference) {
        this.context = context;
        this.mainFragment = mainFragment;
        this.activityReference = activityReference;

        this.logsList = new ArrayList<>(logsList.size());
        this.logsList.addAll(logsList);

        logDatabase = LogDatabase.getInstance(context);
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

        TableLog selectedLog = logsList.get(position);

        //icona
        //setItemIcon(selectedFile, holder, false);

        // background
        TypedValue backgroundColor = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, backgroundColor, true);

        // dettagli
        holder.itemName.setText(selectedLog.getOperationType());
        holder.itemDetails.setVisibility(View.VISIBLE);
        holder.itemDetails.setText(Utils.formatDateDetailsFull(selectedLog.getTimestamp()));

        // colori ed icona
        int color = ContextCompat.getColor(context, R.color.log_success);
        if(!selectedLog.getOperationSuccess()) {
            if(selectedLog.isRetried()) {
                color = ContextCompat.getColor(context, R.color.log_retried);
                holder.itemIcon.setImageResource(R.drawable.ic_log_warning);
            } else {
                color = ContextCompat.getColor(context, R.color.log_error);
                holder.itemIcon.setImageResource(R.drawable.ic_log_error);
            }
        } else
            holder.itemIcon.setImageResource(R.drawable.ic_log_success);
        holder.itemName.setTextColor(color);
        holder.itemDetails.setTextColor(color);
        DrawableCompat.setTint(DrawableCompat.wrap(holder.itemIcon.getDrawable()), color);

        holder.itemView.setOnClickListener(view -> {
            // todo gestione visualizzazione dettagli log
        });

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
