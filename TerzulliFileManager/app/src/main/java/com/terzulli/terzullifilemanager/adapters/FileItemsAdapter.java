package com.terzulli.terzullifilemanager.adapters;

import static android.content.Context.MODE_PRIVATE;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.compressSelectedFilesOperation;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.copyMoveSelectionOperation;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.deleteSelectedFilesOperation;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.extractSelectedFileOperation;
import static com.terzulli.terzullifilemanager.utils.Utils.formatFileDetails;
import static com.terzulli.terzullifilemanager.utils.Utils.isFileAZipArchive;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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

import com.squareup.picasso.Picasso;
import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.RecentsFilesManager;
import com.terzulli.terzullifilemanager.utils.SelectedFilesManager;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileItemsAdapter extends RecyclerView.Adapter<FileItemsAdapter.ItemsViewHolder> {
    private final Context context;
    private final SelectedFilesManager selectedFilesManager;
    private final MainFragment mainFragment;
    private final Activity activityReference;
    private File[] filesAndDirs;

    public FileItemsAdapter(Context context, File[] filesAndFolders, MainFragment mainFragment, Activity activityReference) {
        this.context = context;
        filesAndDirs = filesAndFolders;
        this.mainFragment = mainFragment;
        this.activityReference = activityReference;

        selectedFilesManager = new SelectedFilesManager();
    }

    public void deselectAll() {
        clearSelection();
        mainFragment.refreshAdapterItems();
    }

    public void infoSelectedFile() {
        if (isSelectionModeEnabled() && selectedFilesManager.getSelectedFiles().size() == 1) {
            mainFragment.displayPropertiesDialog(selectedFilesManager.getSelectedFiles().get(0));
        } else if (!isSelectionModeEnabled()) {
            mainFragment.displayPropertiesDialog(new File(mainFragment.getCurrentPath()));
        }
    }

    public void copyMoveSelection(boolean isCopy) {
        if (selectedFilesManager.getSelectedFiles().size() == 0)
            return;

        selectedFilesManager.setOperationOriginPath(mainFragment.getCurrentPath());
        selectedFilesManager.setCopyMoveOperationTypeIsCopy(isCopy);
        saveSelectionForCopyMove();
        clearSelection();
        mainFragment.refreshList(false);

        mainFragment.displayCopyMoveBar(isCopy, selectedFilesManager.getSelectedFilesToCopyMove().size());
    }

    public void compressSelection() {
        if (selectedFilesManager.getSelectedFiles().size() == 0)
            return;

        selectedFilesManager.setOperationOriginPath(mainFragment.getCurrentPath());
        saveSelectionForCompress();
        clearSelection();
        mainFragment.refreshList(true);

        mainFragment.displayCompressToBar(selectedFilesManager.getSelectedFilesToCompress().size());
    }

    public void recoverEventuallyActiveCopyMoveOperation() {
        if (selectedFilesManager.getSelectedFilesToCopyMove().size() != 0) {
            mainFragment.displayCopyMoveBar(selectedFilesManager.copyMoveOperationTypeIsCopy(),
                    selectedFilesManager.getSelectedFilesToCopyMove().size());
        }
    }

    public void recoverEventuallyActiveExtractOperation() {
        if (selectedFilesManager.getFileToExtract() != null) {
            mainFragment.displayExtractToBar();
        }
    }

    public void recoverEventuallyActiveCompressOperation() {
        if (selectedFilesManager.getSelectedFilesToCompress().size() != 0) {
            mainFragment.displayCompressToBar(selectedFilesManager.getSelectedFilesToCompress().size());
        }
    }

    public void submitSearchQuery(final String searchQuery) {
        // recupero tutti i file originali del path corrente in caso siano state fatte query precedenti
        recoverCurrentFilesBeforeQuerySubmit();
        // salvo tutti i file del path corrente
        saveCurrentFilesBeforeQuerySubmit();

        ArrayList<File> searchedResults = new ArrayList<>();

        for (File file : filesAndDirs) {
            if (file != null && file.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                searchedResults.add(file);
        }

        File[] searchedResultsArr = new File[searchedResults.size()];
        int i = 0;
        for (File file : searchedResults)
            searchedResultsArr[i++] = file;

        mainFragment.loadSelection(searchedResultsArr, "");
    }

    public void renameSelectedFile() {
        // controllo se c'è esattamente un file / cartella selezionato
        if (checkSelectedFilesType() == 1 || checkSelectedFilesType() == 2
                || checkSelectedFilesType() == 11 || checkSelectedFilesType() == 12)
            mainFragment.displayRenameDialog(selectedFilesManager.getSelectedFiles().get(0));
    }

    public void createNewDirectory() {
        if (!isSelectionModeEnabled())
            mainFragment.displayNewDirectoryDialog();
    }

    public void extractSelectedFile() {
        if (selectedFilesManager.getFileToExtract() != null) {
            // caso in cui apriamo un file direttamente con il click
            mainFragment.displayExtractToBar();
        }
    }

    public void executeCopyMoveOperationOnThread(boolean isCopy, String destinationPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        mainFragment.hideCopyMoveExtractBar();

        if (isCopy)
            Toast.makeText(context, R.string.action_copy_started, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, R.string.action_move_started, Toast.LENGTH_SHORT).show();

        ArrayList<File> filesToCopyMove = new ArrayList<>(selectedFilesManager.getSelectedFilesToCopyMove().size());
        filesToCopyMove.addAll(selectedFilesManager.getSelectedFilesToCopyMove());
        int nItems = selectedFilesManager.getSelectedFilesToCopyMove().size();

        if (filesToCopyMove.size() != 0) {
            clearSelection();
            selectedFilesManager.clearSelectionFromCopyMove();

            executor.execute(() -> {

                int returnCode = copyMoveSelectionOperation(isCopy, destinationPath, filesToCopyMove, context);

                handler.post(() -> {

                    switch (returnCode) {
                        case 1:
                            String toastMessage;
                            if (isCopy)
                                toastMessage = context.getResources().getString(R.string.action_copy_completed_first_part);
                            else
                                toastMessage = context.getResources().getString(R.string.action_move_completed_first_part);

                            toastMessage += " " + nItems + " " + context.getResources().getString(R.string.action_copy_move_completed_second_part_v2)
                                    + " " + context.getResources().getString(R.string.action_copy_move_completed_third_part);
                            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();

                            break;
                        case -1: // errore durante la copia
                            if (isCopy)
                                mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_copy),
                                        context.getResources().getString(R.string.error_copy_generic));
                            else
                                mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_move),
                                        context.getResources().getString(R.string.error_move_generic));
                            break;
                        case -2: // la nuova location non esiste
                            if (isCopy)
                                mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_copy),
                                        context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir));
                            else
                                mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_move),
                                        context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir));
                            break;
                        case -3: // la nuova destinazione è uno dei file che sto spostando / copiando
                            if (isCopy)
                                mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_copy),
                                        context.getResources().getString(R.string.error_cannot_copy_into_itself));
                            else
                                mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_move),
                                        context.getResources().getString(R.string.error_cannot_move_into_itself));
                            break;
                        default:
                            break;
                    }

                    if (mainFragment.getCurrentPath().equals(destinationPath))
                        mainFragment.refreshList(true);
                });
            });
        }
    }

    public void executeExtractOperationOnThread(String extractPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        mainFragment.hideCopyMoveExtractBar();
        Toast.makeText(context, R.string.action_extraction_started, Toast.LENGTH_SHORT).show();

        executor.execute(() -> {

            int returnCode = extractSelectedFileOperation(new File(selectedFilesManager.getFileToExtract().getPath()),
                    extractPath, context);
            selectedFilesManager.setFileToExtract(null);

            handler.post(() -> {

                switch (returnCode) {
                    case 1:
                        Toast.makeText(context, R.string.action_extract_completed, Toast.LENGTH_SHORT).show();
                        break;
                    case -1:
                        //  errore estrazione
                        mainFragment.displayErrorDialog(context.getResources().getString(R.string.button_extract),
                                context.getResources().getString(R.string.error_extraction_cannot_complete));
                        break;
                    case -2:
                        // errore generico estrazione
                        mainFragment.displayErrorDialog(context.getResources().getString(R.string.button_extract),
                                context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir));
                        break;
                    case -3:
                        // errore estrazione per password
                        mainFragment.displayErrorDialog(context.getResources().getString(R.string.button_extract),
                                context.getResources().getString(R.string.error_check_password_not_supported));
                        break;
                    default:
                        break;
                }

                if (mainFragment.getCurrentPath().equals(extractPath))
                    mainFragment.refreshList(true);
            });
        });
    }

    public void executeCompressOperationOnThread(String compressPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        mainFragment.hideCopyMoveExtractBar();
        Toast.makeText(context, R.string.action_compression_started, Toast.LENGTH_SHORT).show();

        ArrayList<File> filesToCompress = new ArrayList<>(selectedFilesManager.getSelectedFilesToCompress().size());
        filesToCompress.addAll(selectedFilesManager.getSelectedFilesToCompress());

        if (filesToCompress.size() != 0) {
            clearSelection();
            selectedFilesManager.setSelectedFilesToCompress(new ArrayList<>());

            executor.execute(() -> {

                int returnCode = compressSelectedFilesOperation(compressPath, filesToCompress, context);

                handler.post(() -> {

                    switch (returnCode) {
                        case 1:
                            Toast.makeText(context, R.string.action_compress_completed, Toast.LENGTH_SHORT).show();
                            break;
                        case -1: // errore generico durante la compressione
                            mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_compress),
                                    context.getResources().getString(R.string.error_compression_cannot_compress_file));
                            break;
                        case -2: // impossibile creare la cartella di destinazione
                            mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_compress),
                                    context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir));
                            break;
                        case -3: // la destinazione è compresa nei file da comprimere
                            mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_compress),
                                    context.getResources().getString(R.string.error_cannot_compress_into_itself));
                            break;
                        default:
                            break;
                    }

                    if (mainFragment.getCurrentPath().equals(compressPath))
                        mainFragment.refreshList(true);
                });
            });
        }

    }

    public void executeDeleteOperationOnThread(String currentPath) {
        String toastMessage = context.getResources().getString(R.string.delete_toast_first_part)
                + " " + selectedFilesManager.getSelectedFiles().size() + " ";
        if (selectedFilesManager.getSelectedFiles().size() == 1)
            toastMessage += context.getResources().getString(R.string.delete_toast_single_second_part);
        else
            toastMessage += context.getResources().getString(R.string.delete_toast_multiple_second_part);

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        if (isSelectionModeEnabled()) {
            ArrayList<File> filestoDelete = new ArrayList<>(selectedFilesManager.getSelectedFiles().size());
            filestoDelete.addAll(selectedFilesManager.getSelectedFiles());
            clearSelection();

            executor.execute(() -> {
                int returnCode = deleteSelectedFilesOperation(currentPath, filestoDelete, context);

                handler.post(() -> {
                    if (returnCode == -1)mainFragment.displayErrorDialog(context.getResources().getString(R.string.action_delete),
                            context.getResources().getString(R.string.error_cannot_delete_item));

                    if (mainFragment.getCurrentPath().equals(currentPath))
                        mainFragment.refreshList(true);
                });
            });
        }

    }

    public void deleteSelectedFiles() {
        if (isSelectionModeEnabled()) {
            int selectionType = 0;
            String fileName = "";

            switch (checkSelectedFilesType()) {
                case 1:
                case 11:
                    // 2: singolo file
                    selectionType = 2;
                    fileName = selectedFilesManager.getSelectedFiles().get(0).getName();
                    break;
                case 2:
                case 12:
                    // 1: singola directory
                    selectionType = 1;
                    fileName = selectedFilesManager.getSelectedFiles().get(0).getName();
                    break;
                case 3:
                    // 4: multipli file
                    selectionType = 4;
                    break;
                case 4:
                    // 3: multiple directory
                    selectionType = 3;
                    break;
                case 5:
                case 6:
                case 7:
                    // 5: multipla generica
                    selectionType = 5;
                    break;
            }

            mainFragment.displayDeleteSelectionDialog(selectionType, fileName, selectedFilesManager.getSelectedFiles().size());
        }
    }

    private boolean checkIfItemWasSelected(File file) {
        if (selectedFilesManager.getSelectedFiles().isEmpty())
            return false;
        return selectedFilesManager.getSelectedFiles().contains(file);
    }

    public void shareSelectedFiles() {
        if (isSelectionModeEnabled()) {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);

            // impstazione mime type
            String mimetype = Utils.getMimeType(Uri.fromFile(selectedFilesManager.getSelectedFiles().get(0)));
            for (File file : selectedFilesManager.getSelectedFiles()) {
                if (!mimetype.equals(Utils.getMimeType(Uri.fromFile(file)))) {
                    mimetype = "*/*";
                    break;
                }
            }
            intentShareFile.setType(mimetype);

            // allego i file
            ArrayList<Uri> filesToShare = new ArrayList<>();
            for (File file : selectedFilesManager.getSelectedFiles()) {
                filesToShare.add(FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file));
            }
            clearSelection();

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.intent_share_subject));
            intentShareFile.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.intent_share_text));
            intentShareFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare);

            context.startActivity(Intent.createChooser(intentShareFile, context.getResources().getString(R.string.intent_share_title)));
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
     * - 11: selezione completa (generica) ma c'è un solo file
     * - 12: selezione completa (generica) ma c'è una sola cartella
     */

    /* vecchio
     *
     * - 8: selezione generica dentro zip
     * - 9: selezione completa dentro zip
     * - 10: nessuna selezione attiva, ma la cartella corrente è uno zip
     * */
    private int checkSelectedFilesType() {
        if (selectedFilesManager.getSelectedFiles().isEmpty())
            return 0;

        int foundFileCount = 0;
        int foundDirsCount = 0;

        for (File file : selectedFilesManager.getSelectedFiles()) {
            if (file.isDirectory()) {
                foundDirsCount++;
            } else {
                foundFileCount++;
            }
        }

        if (filesAndDirs.length == foundFileCount && filesAndDirs.length == 1)
            return 11;
        if (filesAndDirs.length == foundDirsCount && filesAndDirs.length == 1)
            return 12;
        if (filesAndDirs.length == foundFileCount)
            return 7;
        if (selectedFilesManager.getSelectedFiles().size() == filesAndDirs.length)
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

        return 0; // non dovremmo mai arrivare qui
    }

    public void openWithSelectedFile() {
        if (isSelectionModeEnabled()) {
            if (selectedFilesManager.getSelectedFiles().size() == 1) {
                File file = selectedFilesManager.getSelectedFiles().get(0);

                MimeTypeMap map = MimeTypeMap.getSingleton();
                String ext = Utils.getFileExtension(file);
                String type = map.getMimeTypeFromExtension(ext);

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //Uri data = Uri.parse(file.getAbsolutePath());

                    Uri data = FileProvider.getUriForFile(context,
                            context.getApplicationContext().getPackageName() + ".provider", file);

                    intent.setDataAndType(data, type);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.action_open_with)));
                } catch (Exception e) {
                    Toast.makeText(context, R.string.cant_open_file, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void installApplication(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file),
                "application/vnd.android.package-archive");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.error_package_install, Toast.LENGTH_SHORT).show();
        }
    }

    public void clearFileToExtractSelection() {
        //fileToExtract = null;
        selectedFilesManager.setFileToExtract(null);
    }

    public void clearCurrentFilesBeforeQuerySubmit() {
        selectedFilesManager.clearCurrentFilesBeforeQuerySubmit();
    }

    public void recoverCurrentFilesBeforeQuerySubmit() {

        if (selectedFilesManager.getCurrentFilesBeforeQuerySubmit().size() != 0) {

            filesAndDirs = new File[selectedFilesManager.getCurrentFilesBeforeQuerySubmit().size()];
            int i = 0;
            for (File file : selectedFilesManager.getCurrentFilesBeforeQuerySubmit())
                filesAndDirs[i++] = file;

            selectedFilesManager.clearCurrentFilesBeforeQuerySubmit();
        }
    }

    public String getOperationOriginPath() {
        return selectedFilesManager.getOperationOriginPath();
    }

    public void saveCurrentFilesBeforeQuerySubmit() {
        selectedFilesManager.setCurrentFilesBeforeQuerySubmit(filesAndDirs);
    }

    public void clearSelectionFromCopyMove() {
        selectedFilesManager.clearSelectionFromCopyMove();
    }

    public void recoverSelectionFromCopyMove() {
        selectedFilesManager.recoverSelectionFromCopyMove();
    }

    public void saveSelectionForCopyMove() {
        selectedFilesManager.setSelectedFilesToCopyMove(selectedFilesManager.getSelectedFiles());
        selectedFilesManager.clearSelectedFiles();
    }

    public void clearSelectionFromCompress() {
        selectedFilesManager.clearSelectionFromCompress();
    }

    public void recoverSelectionFromCompress() {
        selectedFilesManager.setSelectedFiles(selectedFilesManager.getSelectedFilesToCompress());
        selectedFilesManager.clearSelectionFromCompress();
    }

    public void saveSelectionForCompress() {
        selectedFilesManager.setSelectedFilesToCompress(selectedFilesManager.getSelectedFiles());
        selectedFilesManager.clearSelectedFiles();
    }

    public void clearSelection() {
        selectedFilesManager.clearSelectedFiles();
    }

    public boolean isSelectionModeEnabled() {
        return !selectedFilesManager.getSelectedFiles().isEmpty();
    }

    public void selectAll() {
        clearSelection();

        ArrayList<File> selectedFilesList = new ArrayList<>(filesAndDirs.length);
        selectedFilesList.addAll(Arrays.asList(filesAndDirs));
        selectedFilesManager.setSelectedFiles(selectedFilesList);

        mainFragment.refreshAdapterItems();
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
        setItemIcon(selectedFile, holder, false);

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
                //toggleItemSelection(selectedFile, holder, true, false);
                toggleItemSelection(selectedFile, holder);
            }
        });

        // gestione selezione item con long click
        holder.itemView.setOnLongClickListener(view -> {
            //toggleItemSelection(selectedFile, holder, false, false);
            toggleItemSelection(selectedFile, holder);

            return true;
        });

        // gestione selezione item con click sull'icona
        //holder.itemIcon.setOnClickListener(view -> toggleItemSelection(selectedFile, holder, true, false));
        holder.itemIcon.setOnClickListener(view -> toggleItemSelection(selectedFile, holder));

        // ripristino lo stato di selezione precedente
        //toggleItemSelection(selectedFile, holder, false, true);
        recoverItemSelectionState(selectedFile, holder);
    }

    private void itemOpenerHandler(File selectedFile) {
        // se il file è una directory, viene caricato il path
        // in caso contrario, si avvia un intent per l'apertura del file

        if (selectedFile.isDirectory()) {
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

        }
    }

    private void setItemIcon(File selectedFile, @NonNull ItemsViewHolder holder, boolean isSelected) {

        if (!isSelected) {
            if (Utils.fileIsImage(selectedFile)) {
                loadImageThumbnailAsync(selectedFile, holder);
            } else
                holder.itemIcon.setImageResource(Utils.getFileTypeIcon(selectedFile));
        } else {
            holder.itemIcon.setImageResource(R.drawable.ic_check_circle_filled);
        }

    }

    private void recoverItemSelectionState(File selectedFile, @NonNull ItemsViewHolder holder) {
        setItemSelectedMode(selectedFile, holder, checkIfItemWasSelected(selectedFile));
    }

    private void toggleItemSelection(File selectedFile, @NonNull ItemsViewHolder holder) {
        setItemSelectedMode(selectedFile, holder, !checkIfItemWasSelected(selectedFile));
    }

    private void setItemSelectedMode(File selectedFile, @NonNull ItemsViewHolder holder, boolean select) {
        int color;
        boolean setSelectedIcon = false;

        if (!select) {
            // unselect
            if(selectedFilesManager.getSelectedFiles().contains(selectedFile))
                selectedFilesManager.removeSelectedFile(selectedFile);

            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            color = outValue.data;

            setItemBackgroundColor(color, holder);
        } else {
            // select
            if(!selectedFilesManager.getSelectedFiles().contains(selectedFile))
                selectedFilesManager.addSelectedFile(selectedFile);

            color = ContextCompat.getColor(context, R.color.item_selected_light);
            setSelectedIcon = true;
        }

        setItemBackgroundColor(color, holder);
        setItemIcon(selectedFile, holder, setSelectedIcon);

        // aggiorno il titolo della toolbar in base al numero di elementi selezionati
        if (!selectedFilesManager.getSelectedFiles().isEmpty()) {
            mainFragment.setActionBarTitle(selectedFilesManager.getSelectedFiles().size() + " " + context.getResources().getString(R.string.selected_file_lowercase));

            if(activityReference instanceof MainActivity)
                ((MainActivity)activityReference).setActionBarToggleCloseButton();
        } else {
            mainFragment.resetActionBarTitle();

            if(activityReference instanceof MainActivity)
                ((MainActivity)activityReference).setActionBarToggleDefault();
        }

        if(activityReference instanceof MainActivity)
            ((MainActivity)activityReference).updateMenuItems(checkSelectedFilesType());
    }

    private void setItemBackgroundColor(final int color, @NonNull ItemsViewHolder holder) {
        holder.itemHolder.setBackgroundColor(color);
        holder.itemIconContainer.setBackgroundColor(color);
        holder.itemIcon.setBackgroundColor(color);
        holder.itemTextContainer.setBackgroundColor(color);
        holder.itemName.setBackgroundColor(color);
        holder.itemDetails.setBackgroundColor(color);
    }

    private void loadImageThumbnailAsync(@NonNull File file, @NonNull ItemsViewHolder holder) {
        // ottengo la dimensione dai metadati
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

        int maxPixelSize = (int) Utils.convertDpToPixel(32, context);
        float actualRatio = (float) opts.outWidth / (float) opts.outHeight;
        float ratioMax = 1.0f;

        int finalWidth = maxPixelSize;
        int finalHeight = maxPixelSize;

        if (ratioMax > actualRatio)
            finalWidth = (int) ((float)maxPixelSize * actualRatio);
        else
            finalHeight = (int) ((float)maxPixelSize / actualRatio);

        Picasso.get()
                .load(file)
                .placeholder(R.drawable.ic_file_generic)
                .resize(finalWidth, finalHeight)
                .into(holder.itemIcon);
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
