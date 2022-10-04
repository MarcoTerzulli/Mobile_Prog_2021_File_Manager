package com.terzulli.terzullifilemanager.adapters;

import static com.terzulli.terzullifilemanager.activities.MainActivity.updateMenuItems;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.displayPropertiesDialog;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.resetActionBarTitle;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.setActionBarTitle;
import static com.terzulli.terzullifilemanager.utils.Utils.formatFileDetails;

import android.annotation.SuppressLint;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import moe.feng.common.view.breadcrumbs.BuildConfig;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder> {
    private static ArrayList<File> selectedFiles;
    private static ArrayList<File> selectedFilesToCopyMove;
    private static File[] filesAndDirs = null;
    private static boolean isCurrentDirAnArchive;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public ItemsAdapter(Context context, File[] filesAndFolders, boolean isCurrentDirAnArchive) {
        ItemsAdapter.context = context;
        filesAndDirs = filesAndFolders;
        ItemsAdapter.isCurrentDirAnArchive = isCurrentDirAnArchive;

        if (selectedFiles == null)
            selectedFiles = new ArrayList<>();

        if (selectedFilesToCopyMove == null)
            selectedFilesToCopyMove = new ArrayList<>();
    }

    public static void recoverSelectionFromCopyMove() {
        selectedFiles = new ArrayList<>(selectedFilesToCopyMove.size());
        selectedFiles.addAll(selectedFilesToCopyMove);
    }

    public static void saveSelectionFromCopyMove() {
        selectedFilesToCopyMove = new ArrayList<>(selectedFiles.size());
        selectedFilesToCopyMove.addAll(selectedFiles);
    }

    public static ArrayList<File> getSelectedFilesToCopyMove() {
        return selectedFilesToCopyMove;
    }

    public static void clearSelection() {
        selectedFiles.clear();
    }

    public static boolean isSelectionModeEnabled() {
        if (selectedFiles == null)
            return false;
        return !selectedFiles.isEmpty();
    }

    public static void selectAll() {
        clearSelection();
        selectedFiles.addAll(Arrays.asList(filesAndDirs));
        MainFragment.refreshList();
    }

    public static void deselectAll() {
        clearSelection();
        MainFragment.refreshList();
    }

    public static void infoSelectedFile() {
        if (isSelectionModeEnabled() && selectedFiles.size() == 1) {
            displayPropertiesDialog(selectedFiles.get(0));
        }
    }

    public static void copyMoveSelection(boolean isCopy) {
        if (selectedFiles == null)
            return;

        saveSelectionFromCopyMove();
        clearSelection();
        MainFragment.refreshList();

        MainFragment.displayCopyMoveBar(isCopy, selectedFilesToCopyMove.size());
    }

    public static void copyMoveSelectionOperation(boolean isCopy, String copyPath) {
        ArrayList<File> filesToCopyMove = new ArrayList<>(selectedFilesToCopyMove.size());
        filesToCopyMove.addAll(selectedFilesToCopyMove);

        if (filesToCopyMove.size() == 0)
            return;

        clearSelection();
        selectedFilesToCopyMove = new ArrayList<>();
        MainFragment.hideCopyMoveBar();

        File newLocation = new File(copyPath);
        if (newLocation.exists()) {
            //if (!newLocation.getPath().equals(filesToCopyMove.get(0).getParent())) {
                // copy
                for (File fileToMove : filesToCopyMove) {

                    try {
                        copyFileLowLevelOperation(fileToMove, newLocation);
                    } catch (IOException e) {
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, R.string.error_check_permissions, Toast.LENGTH_LONG).show();
                    }
                }

                // delete if the operation is move
                if (!isCopy) {
                    for (File fileToMove : filesToCopyMove) {
                        deleteRecursive(fileToMove);
                    }
                }
            /*} else {
                Toast.makeText(context, R.string.error_copy_move_same_location, Toast.LENGTH_SHORT).show();
            }*/
        }

        if (MainFragment.getCurrentPath().equals(copyPath))
            MainFragment.refreshList();
        else {
            String toastMessage;
            if (isCopy)
                toastMessage = context.getResources().getString(R.string.action_copy_completed_first_part);
            else
                toastMessage = context.getResources().getString(R.string.action_move_completed_first_part);

            toastMessage += filesToCopyMove.size()
                    + context.getResources().getString(R.string.action_copy_move_completed_second_part)
                    + copyPath + context.getResources().getString(R.string.action_copy_move_completed_third_part);
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /*public static void copyMoveSelectionOperation(boolean isCopy, String copyPath) {
        if (selectedFilestoCopyMove == null)
            return;

        File newLocation = new File(copyPath);
        if (newLocation.exists()) {
            if (!newLocation.getPath().equals(selectedFilestoCopyMove.get(0).getParent())) {
                // copy
                for (File fileToMove : selectedFilestoCopyMove) {

                    try {
                        copyFileLowLevelOperation(fileToMove, newLocation);
                    } catch (IOException e) {
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, R.string.error_check_permissions, Toast.LENGTH_LONG).show();
                    }
                }

                // delete if the operation is move
                if (!isCopy) {
                    for (File fileToMove : selectedFilestoCopyMove) {
                        deleteRecursive(fileToMove);
                    }
                }
            } else {
                Toast.makeText(context, R.string.error_copy_move_same_location, Toast.LENGTH_SHORT).show();
            }
        }

        clearSelection();
        selectedFilestoCopyMove = new ArrayList<>();
        MainFragment.refreshList();
        MainFragment.hideCopyMoveBar();
    }*/

    public static void renameSelectedFile() {
        // controllo se c'è esattamente un file / cartella selezionato
        if (checkSelectedFilesType() == 1 || checkSelectedFilesType() == 2)
            MainFragment.displayRenameDialog(selectedFiles.get(0));
    }

    public static void createNewDirectory() {
        if (!isSelectionModeEnabled())
            MainFragment.displayNewFolderDialog();
    }

    public static void deleteSelectedFilesOperation(String originalPath) {
        if (isSelectionModeEnabled() && !isCurrentDirAnArchive) {
            ArrayList<File> filestoDelete = new ArrayList<>(selectedFiles.size());
            filestoDelete.addAll(selectedFiles);
            clearSelection();

            for (File file : filestoDelete) {
                deleteRecursive(file);
            }

            if (MainFragment.getCurrentPath().equals(originalPath))
                MainFragment.refreshList();
        }
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles()))
                deleteRecursive(child);
        }

        file.delete();
    }

    private static void copyFileLowLevelOperation(File sourceLocation, File targetLocation)
            throws IOException {

        File outFile = new File(targetLocation, sourceLocation.getName());

        // rinomina in caso di duplicato
        String originalName = sourceLocation.getName();
        String newName = sourceLocation.getName();
        int retries, maxRetries = 10000;

        // gestione di omonimia, aggiunge " (i)" al nome (es. "Test (1)")
        for (retries = 1; retries < maxRetries; retries++) {
            outFile = new File(targetLocation, newName);

            if (outFile.exists()) {
                if (outFile.isDirectory())
                    newName = originalName + " (" + retries + ")";
                else
                    newName = originalName.substring(0, originalName.indexOf(".")) +
                            " (" + retries + ")" + originalName.substring(originalName.indexOf("."));
            } else
                break;
        }

        if (retries == maxRetries) {
            throw new IOException("Cannot create file or directory " + outFile.getAbsolutePath());
        }

        if (sourceLocation.isDirectory()) {
            // creo test/livello1
            if (!outFile.exists() && !outFile.mkdirs()) {
                throw new IOException("Cannot create directory " + outFile.getAbsolutePath());
            }

            String[] children = sourceLocation.list();

            for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                // copia ricorsiva
                /*copyFileLowLevelOperation(new File(sourceLocation, children[i]),
                        new File(outFile, children[i]));*/
                copyFileLowLevelOperation(new File(sourceLocation, children[i]),
                        outFile);
            }
        } else {
            try (InputStream in = new FileInputStream(sourceLocation)) {

                if (!outFile.exists())
                    outFile.createNewFile();

                try (OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        }
    }

    public static void deleteSelectedFiles() {
        if (isSelectionModeEnabled() && !isCurrentDirAnArchive) {
            int selectionType = 0;
            String fileName = "";

            switch (checkSelectedFilesType()) {
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

    public static void shareSelectedFiles() {
        if (isSelectionModeEnabled() && !isCurrentDirAnArchive) {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);

            // impstazione mime type
            String mimetype = Utils.getMimeType(Uri.fromFile(selectedFiles.get(0)));
            for (File file : selectedFiles) {
                if (!mimetype.equals(Utils.getMimeType(Uri.fromFile(file)))) {
                    mimetype = "*/*";
                    break;
                }
            }
            intentShareFile.setType(mimetype);

            // allego i file
            for (File file : selectedFiles) {
                //intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
                intentShareFile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID +".provider", file));
            }

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,"Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

            context.startActivity(Intent.createChooser(intentShareFile, "Share File"));
        }
    }

    /**
     * Funzione per ottenere la tipologia di selezione corrente
     *
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

    public static void openWithSelectedFile() {
        if (isSelectionModeEnabled() && !isCurrentDirAnArchive) {
            if (selectedFiles.size() == 1) {
                File file = selectedFiles.get(0);

                MimeTypeMap map = MimeTypeMap.getSingleton();
                String ext = Utils.getFileExtension(file);
                String type = map.getMimeTypeFromExtension(ext);

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data = Uri.parse(file.getAbsolutePath());

                    intent.setDataAndType(data, type);
                    context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.action_open_with)));
                } catch (Exception e) {
                    Toast.makeText(context, R.string.cant_open_file, Toast.LENGTH_SHORT).show();
                }
            }
        }
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
            MainActivity.setActionBarToggleCloseButton();
        } else {
            resetActionBarTitle();
            MainActivity.setActionBarToggleDefault();
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
