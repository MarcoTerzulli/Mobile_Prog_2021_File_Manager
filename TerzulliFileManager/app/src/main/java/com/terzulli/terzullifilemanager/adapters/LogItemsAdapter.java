package com.terzulli.terzullifilemanager.adapters;

import static com.terzulli.terzullifilemanager.utils.FileOperationsFunctions.strOperationCompress;
import static com.terzulli.terzullifilemanager.utils.FileOperationsFunctions.strOperationCopy;
import static com.terzulli.terzullifilemanager.utils.FileOperationsFunctions.strOperationDelete;
import static com.terzulli.terzullifilemanager.utils.FileOperationsFunctions.strOperationExtract;
import static com.terzulli.terzullifilemanager.utils.FileOperationsFunctions.strOperationMove;
import static com.terzulli.terzullifilemanager.utils.FileOperationsFunctions.strOperationNewFolder;
import static com.terzulli.terzullifilemanager.utils.FileOperationsFunctions.strOperationRename;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.database.LogDatabase;
import com.terzulli.terzullifilemanager.database.entities.TableLog;
import com.terzulli.terzullifilemanager.fragments.LogFragment;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.Utils;

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

        // nome
        switch (selectedLog.getOperationType()) {
            case strOperationNewFolder:
                holder.itemName.setText(context.getResources().getString(R.string.log_op_type_new_folder));
                break;
            case strOperationCompress:
                holder.itemName.setText(context.getResources().getString(R.string.log_op_type_compress));
                break;
            case strOperationExtract:
                holder.itemName.setText(context.getResources().getString(R.string.log_op_type_extract));
                break;
            case strOperationCopy:
                holder.itemName.setText(context.getResources().getString(R.string.log_op_type_copy));
                break;
            case strOperationMove:
                holder.itemName.setText(context.getResources().getString(R.string.log_op_type_move));
                break;
            case strOperationRename:
                holder.itemName.setText(context.getResources().getString(R.string.log_op_type_rename));
                break;
            case strOperationDelete:
                holder.itemName.setText(context.getResources().getString(R.string.log_op_type_delete));
                break;
            default:
                // non dovremmo mai arrivarci
                holder.itemName.setText(selectedLog.getOperationType());
        }

        // dettagli
        holder.itemDetails.setVisibility(View.VISIBLE);
        holder.itemDetails.setText(Utils.formatDateDetailsFullWithMilliseconds(selectedLog.getTimestamp()));

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

        // background
        TypedValue backgroundColor = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, backgroundColor, true);

        holder.itemView.setOnClickListener(view -> {
            if(activityReference instanceof MainActivity) {

                LogFragment logFragment = new LogFragment();

                Bundle args = new Bundle();
                args.putInt("logId", selectedLog.getId());
                logFragment.setArguments(args);

                ((MainActivity)activityReference).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_content, logFragment, "log_fragment")
                        .commit();
            }
        });

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

        public ItemsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
            itemDetails = itemView.findViewById(R.id.item_details);
        }
    }
}
