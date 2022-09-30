package com.terzulli.terzullifilemanager.adapters;

import static com.terzulli.terzullifilemanager.activities.MainActivity.updateMenuItems;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.resetActionBarTitle;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.setActionBarTitle;
import static com.terzulli.terzullifilemanager.utils.Utils.formatFileDetails;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder> {
    private static ArrayList<File> selectedFiles;
    private static File[] filesAndDirs = null;
    private final Context context;
    private static boolean isCurrentDirAnArchive;

    public ItemsAdapter(Context context, File[] filesAndFolders, boolean isCurrentDirAnArchive) {
        this.context = context;
        filesAndDirs = filesAndFolders;
        ItemsAdapter.isCurrentDirAnArchive = isCurrentDirAnArchive;

        if (selectedFiles == null)
            selectedFiles = new ArrayList<>();
    }

    public static void clearSelection() {
        selectedFiles.clear();
    }

    public static boolean isSelectionModeEnabled() {
        if (selectedFiles == null)
            return false;
        return !selectedFiles.isEmpty();
    }

    public static void renameSelectedFile() {
        // controllo se c'è esattamente un file / cartella selezionato
        if (checkSelectedFilesType() == 1 || checkSelectedFilesType() == 2)
            MainFragment.displayRenameDialog(selectedFiles.get(0));
    }

    public static void createNewDirectory() {
        if (!isSelectionModeEnabled())
            MainFragment.displayNewFolderDialog();
    }

    public static void deleteSelectedFilesOperation()  {
        if (isSelectionModeEnabled() && !isCurrentDirAnArchive) {
            for (File file : selectedFiles) {
                file.delete();
            }
            clearSelection();
            MainFragment.refreshList();
        }
    }

    public static void deleteSelectedFiles() {
        if (isSelectionModeEnabled() && !isCurrentDirAnArchive) {
            int selectionType = 0;
            String fileName = "";

            switch (checkSelectedFilesType()){
                case 1:
                    selectionType = 2;
                    fileName = selectedFiles.get(0).getName();
                    break;
                case 2:
                    selectionType = 1;
                    fileName = selectedFiles.get(0).getName();
                    break;
                case 3:
                    selectionType = 4;
                    break;
                case 4:
                    selectionType = 3;
                    break;
                case 5:
                case 6:
                case 7:
                    selectionType = 5;
                    break;
            }

            MainFragment.displayDeleteSelectionDialog(selectionType, fileName, selectedFiles.size());
        }
    }

    private static boolean checkIfItemWasSelected(File file) {
        if (selectedFiles.isEmpty())
            return false;
        return selectedFiles.contains(file);
    }

    /**
     * Funzione per ottenere la tipologia di selezione corrente
     * @return la tipologia di selezione attiva:
     * - 1: un file selezionato
     * - 2: una directory selezionata
     * - 3: molteplici file selezionati
     * - 4: molteplici directory selezionate
     * - 5: molteplici file o directory selezionati
     * - 6: selezione completa (generica)
     * - 7: selezione completa ma di soli file
     * - 8: selezione generica dentro zip
     * - 9: selezione completa dentro zip
     * - 10: nessuna selezione attiva, ma la cartella corrente è uno zip
     */
    private static int checkSelectedFilesType() {
        if (selectedFiles.isEmpty())
            return 0;

        int foundFileCount = 0;
        int foundDirsCount = 0;

        for (File file : selectedFiles) {
            if (file.isDirectory()) {
                foundDirsCount++;
            } else {
                foundFileCount++;
            }
        }

        if (filesAndDirs.length == foundFileCount)
            return 7;
        if (selectedFiles.size() == filesAndDirs.length)
            return 6;
        if (foundDirsCount > 1 && foundFileCount == 0)
            return 4;
        if (foundDirsCount > 1 || (foundDirsCount > 0 && foundFileCount > 0))
            return 5;
        if (foundFileCount == 1)
            return 1;
        if (foundDirsCount == 1)
            return 2;
        if (foundFileCount > 1)
            return 3;

        // TODO gestione zip

        return 0; // non dovremmo mai arrivare qui
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
        setItemIcon(position, holder, false);

        // background
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        setItemBackgroundColor(outValue.data, holder);

        // dettagli
        holder.itemName.setText(selectedFile.getName());
        if (selectedFile.isDirectory()) {
            // questo permette di centrare la textview con il nome all'interno del container
            holder.itemDetails.setText("");
            holder.itemDetails.setVisibility(View.GONE);
        } else {
            holder.itemDetails.setText(formatFileDetails(selectedFile));
            holder.itemDetails.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(view -> {

            if (!isSelectionModeEnabled()) {
                itemOpenerHandler(selectedFile);
            } else {
                toggleItemSelection(position, holder, true, false);
            }
        });

        // gestione selezione item con long click
        holder.itemView.setOnLongClickListener(view -> {
            toggleItemSelection(position, holder, false, false);

            return true;
        });

        // gestione selezione item con click sull'icona
        holder.itemIcon.setOnClickListener(view -> toggleItemSelection(position, holder, true, false));

        // ripristino lo stato di selezione precedente
        toggleItemSelection(position, holder, false, true);

    }

    private void itemOpenerHandler(File selectedFile) {
        // se il file è una directory, viene caricato il path
        // in caso contrario, si avvia un intent per l'apertura del file

        if (selectedFile.isDirectory()) {
            clearSelection();
            MainFragment.loadPath(selectedFile.getAbsolutePath(), true);
        } else if (selectedFile.isFile()) {

            MimeTypeMap map = MimeTypeMap.getSingleton();
            String ext = Utils.getFileExtension(selectedFile);
            String type = map.getMimeTypeFromExtension(ext);

            //if (type == null)
            //type = "*/*";

            // TODO gestione apertura zip

            // TODO gestione installazione apk e richiesta permessi

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

    private void setItemIcon(int position, @NonNull ItemsViewHolder holder, boolean isSelected) {
        File selectedFile = filesAndDirs[position];

        if (!isSelected) {
            if (Utils.fileIsImage(selectedFile))
                holder.itemIcon.setImageBitmap(loadImagePreview(selectedFile));
            else
                holder.itemIcon.setImageResource(Utils.getFileTypeIcon(selectedFile));
        } else {
            holder.itemIcon.setImageResource(R.drawable.ic_check_circle_filled);
        }

    }

    private void toggleItemSelection(int position, @NonNull ItemsViewHolder holder, boolean unselect, boolean recoverLastState) {
        File selectedFile = filesAndDirs[position];
        int color;
        boolean setSelectedIcon = false;

        if (!recoverLastState) {
            // comportamento normale
            if (selectedFiles.contains(selectedFile) && unselect) {
                // unselect
                selectedFiles.remove(selectedFile);

                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                color = outValue.data;

                setItemBackgroundColor(color, holder);
            } else {
                // select
                selectedFiles.add(selectedFile);

                color = ContextCompat.getColor(context, R.color.item_selected_light);
                setSelectedIcon = true;
            }
        } else {
            // comportamento di ripristino dello stato (ignora il toggle)
            if (checkIfItemWasSelected(selectedFile)) {
                color = ContextCompat.getColor(context, R.color.item_selected_light);
                setSelectedIcon = true;
            } else {
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                color = outValue.data;

                setItemBackgroundColor(color, holder);
            }
        }

        setItemBackgroundColor(color, holder);
        setItemIcon(position, holder, setSelectedIcon);

        // aggiorno il titolo della toolbar in base al numero di elementi selezionati
        if (!selectedFiles.isEmpty()) {
            setActionBarTitle(selectedFiles.size() + " " + context.getResources().getString(R.string.selected_file_lowercase));
        } else {
            resetActionBarTitle();
        }
        updateMenuItems(checkSelectedFilesType());
    }

    private void setItemBackgroundColor(final int color, @NonNull ItemsViewHolder holder) {
        holder.itemHolder.setBackgroundColor(color);
        holder.itemIconContainer.setBackgroundColor(color);
        holder.itemIcon.setBackgroundColor(color);
        holder.itemTextContainer.setBackgroundColor(color);
        holder.itemName.setBackgroundColor(color);
        holder.itemDetails.setBackgroundColor(color);
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
        if (filesAndDirs == null)
            return 0;
        return filesAndDirs.length;
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
