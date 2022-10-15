package com.terzulli.terzullifilemanager.adapters;

import static android.content.Context.MODE_PRIVATE;
import static com.terzulli.terzullifilemanager.activities.MainActivity.closeSearchView;
import static com.terzulli.terzullifilemanager.activities.MainActivity.updateMenuItems;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.displayExtractToBar;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.displayPropertiesDialog;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.resetActionBarTitle;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.setActionBarTitle;
import static com.terzulli.terzullifilemanager.utils.Utils.formatFileDetails;
import static com.terzulli.terzullifilemanager.utils.Utils.isFileAZipArchive;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.terzulli.terzullifilemanager.utils.Utils;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder> {
    private static ArrayList<File> currentFilesBeforeQuerySubmit;
    private static ArrayList<File> selectedFiles;
    private static ArrayList<File> selectedFilesToCopyMove;
    private static ArrayList<File> selectedFilesToCompress;
    private static File[] filesAndDirs = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static boolean copyMoveOperationTypeIsCopy = false;
    private static File fileToExtract = null;
    private static String operationStartPath;

    public ItemsAdapter(Context context, File[] filesAndFolders) {
        ItemsAdapter.context = context;
        filesAndDirs = filesAndFolders;

        if (selectedFiles == null)
            selectedFiles = new ArrayList<>();

        if (selectedFilesToCopyMove == null)
            selectedFilesToCopyMove = new ArrayList<>();

        if (currentFilesBeforeQuerySubmit == null)
            currentFilesBeforeQuerySubmit = new ArrayList<>();

        if (selectedFilesToCompress == null)
            selectedFilesToCompress = new ArrayList<>();

    }

    public static void clearFileToExtractSelection() {
        fileToExtract = null;
    }

    public static void clearCurrentFilesBeforeQuerySubmit() {
        if(currentFilesBeforeQuerySubmit != null)
            currentFilesBeforeQuerySubmit.clear();
    }

    public static void recoverCurrentFilesBeforeQuerySubmit() {
        //if (filesAndDirs.length < currentFilesBeforeQuerySubmit.size()) {
        if (currentFilesBeforeQuerySubmit.size() != 0) {

            filesAndDirs = new File[currentFilesBeforeQuerySubmit.size()];
            int i = 0;
            for (File file : currentFilesBeforeQuerySubmit)
                filesAndDirs[i++] = file;

            currentFilesBeforeQuerySubmit.clear();
        }
    }

    public static String getOperationStartPath() {
        return operationStartPath;
    }

    public static void saveCurrentFilesBeforeQuerySubmit() {
        currentFilesBeforeQuerySubmit = new ArrayList<>(filesAndDirs.length);
        currentFilesBeforeQuerySubmit.addAll(Arrays.asList(filesAndDirs));
    }

    public static void clearSelectionFromCopyMove() {
        selectedFilesToCopyMove.clear();
    }

    public static void recoverSelectionFromCopyMove() {
        selectedFiles = new ArrayList<>(selectedFilesToCopyMove.size());
        selectedFiles.addAll(selectedFilesToCopyMove);
        selectedFilesToCopyMove.clear();
    }

    public static void saveSelectionFromCopyMove() {
        selectedFilesToCopyMove = new ArrayList<>(selectedFiles.size());
        selectedFilesToCopyMove.addAll(selectedFiles);
        selectedFiles.clear();
    }

    public static void clearSelectionFromCompress() {
        selectedFilesToCompress.clear();
    }

    public static void recoverSelectionFromCompress() {
        selectedFiles = new ArrayList<>(selectedFilesToCompress.size());
        selectedFiles.addAll(selectedFilesToCompress);
        selectedFilesToCompress.clear();
    }

    public static void saveSelectionFromCompress() {
        selectedFilesToCompress = new ArrayList<>(selectedFiles.size());
        selectedFilesToCompress.addAll(selectedFiles);
        selectedFiles.clear();
    }

    /*public static ArrayList<File> getSelectedFilesToCopyMove() {
        return selectedFilesToCopyMove;
    }*/

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
        } else if (!isSelectionModeEnabled()) {
            displayPropertiesDialog(new File(MainFragment.getCurrentPath()));
        }
    }

    public static void copyMoveSelection(boolean isCopy) {
        if (selectedFiles == null)
            return;

        operationStartPath = MainFragment.getCurrentPath();
        copyMoveOperationTypeIsCopy = isCopy;
        saveSelectionFromCopyMove();
        clearSelection();
        MainFragment.refreshList();

        MainFragment.displayCopyMoveBar(isCopy, selectedFilesToCopyMove.size());
    }

    public static void compressSelection() {
        if (selectedFiles == null)
            return;

        operationStartPath = MainFragment.getCurrentPath();
        saveSelectionFromCompress();
        clearSelection();
        MainFragment.refreshList();

        MainFragment.displayCompressToBar(selectedFilesToCompress.size());
    }

    public static void recoverEventuallyActiveCopyMoveOperation() {
        if (selectedFilesToCopyMove.size() != 0) {
            MainFragment.displayCopyMoveBar(copyMoveOperationTypeIsCopy, selectedFilesToCopyMove.size());
        }
    }

    public static void recoverEventuallyActiveExtractOperation() {
        if (fileToExtract != null) {
            MainFragment.displayExtractToBar();
        }
    }

    public static void recoverEventuallyActiveCompressOperation() {
        if (selectedFilesToCompress.size() != 0) {
            MainFragment.displayCompressToBar(selectedFilesToCompress.size());
        }
    }

    public static void submitSearchQuery(final String searchQuery) {
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

        MainFragment.loadSelection(searchedResultsArr, "");
    }




    /**
     * FUnzione interna per la copia o spostamento di file e cartelle (ricorsiva)
     *
     * @param isCopy          indica se l'operazione è di copia (true) o spostamento (false)
     * @param destinationPath path di destinazione
     * @return codice di esecuzione:
     * - 1: operazione completata con successo
     * - -1: generata eccezione durante l'operazione
     */
    public static int copyMoveSelectionOperation(boolean isCopy, String destinationPath) {
        ArrayList<File> filesToCopyMove = new ArrayList<>(selectedFilesToCopyMove.size());
        filesToCopyMove.addAll(selectedFilesToCopyMove);

        if (filesToCopyMove.size() == 0)
            return -1;

        clearSelection();
        selectedFilesToCopyMove = new ArrayList<>();

        File newLocation = new File(destinationPath);
        if (newLocation.exists()) {
            //if (!newLocation.getPath().equals(filesToCopyMove.get(0).getParent())) {
            // copy
            for (File fileToMove : filesToCopyMove) {

                try {
                    copyFileLowLevelOperation(fileToMove, newLocation);
                } catch (IOException e) {
                    return -1;
                }
            }

            // delete if the operation is move
            if (!isCopy) {
                for (File fileToMove : filesToCopyMove) {
                    deleteRecursive(fileToMove);
                }
            }
        }

        return 1;
    }

    public static void renameSelectedFile() {
        // controllo se c'è esattamente un file / cartella selezionato
        if (checkSelectedFilesType() == 1 || checkSelectedFilesType() == 2
                || checkSelectedFilesType() == 11 || checkSelectedFilesType() == 12)
            MainFragment.displayRenameDialog(selectedFiles.get(0));
    }

    public static void createNewDirectory() {
        if (!isSelectionModeEnabled())
            MainFragment.displayNewDirectoryDialog();
    }

    public static void extractSelectedFile() {
        if (fileToExtract != null) {
            // caso in cui apriamo un file direttamente con il click
            //extractSelectedFilesOperation(fileToDeCompress, fileToDeCompress.getParent());
            displayExtractToBar();
        }
    }

    public static void executeCopyMoveOperationOnThread(boolean isCopy, String destinationPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        MainFragment.hideCopyMoveExtractBar();

        if (isCopy)
            Toast.makeText(context, R.string.action_copy_started, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, R.string.action_move_started, Toast.LENGTH_SHORT).show();

        executor.execute(() -> {

            //Background work here
            int nItems = selectedFilesToCopyMove.size();
            int returnCode = copyMoveSelectionOperation(isCopy, destinationPath);

            handler.post(() -> {
                //UI Thread work here

                switch (returnCode) {
                    case 1:
                        String toastMessage;
                        if (isCopy)
                            toastMessage = context.getResources().getString(R.string.action_copy_completed_first_part);
                        else
                            toastMessage = context.getResources().getString(R.string.action_move_completed_first_part);

                        /*toastMessage += " " + nItems + " "
                                + context.getResources().getString(R.string.action_copy_move_completed_second_part) + " "
                                + destinationPath + context.getResources().getString(R.string.action_copy_move_completed_third_part); */
                        toastMessage += " " + nItems + " " + context.getResources().getString(R.string.action_copy_move_completed_third_part);
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();

                        break;
                    case -1:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

                if (MainFragment.getCurrentPath().equals(destinationPath))
                    MainFragment.refreshList();
            });
        });
    }

    public static void executeExtractOperationOnThread(String extractPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        MainFragment.hideCopyMoveExtractBar();
        Toast.makeText(context, R.string.action_extraction_started, Toast.LENGTH_SHORT).show();

        executor.execute(() -> {

            //Background work here
            int returnCode = extractSelectedFileOperation(new File(fileToExtract.getPath()), extractPath);

            handler.post(() -> {
                //UI Thread work here

                switch (returnCode) {
                    case 1:
                        Toast.makeText(context, R.string.action_extract_completed, Toast.LENGTH_SHORT).show();
                        break;
                    case -1:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        break;
                    case -2:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, R.string.error_check_permissions, Toast.LENGTH_LONG).show();
                        break;
                    case -3:
                        Toast.makeText(context, R.string.error_check_password_not_supported, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

                if (MainFragment.getCurrentPath().equals(extractPath))
                    MainFragment.refreshList();
            });
        });
    }

    public static void executeCompressOperationOnThread(String compressPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        MainFragment.hideCopyMoveExtractBar();
        Toast.makeText(context, R.string.action_compression_started, Toast.LENGTH_SHORT).show();

        executor.execute(() -> {

            //Background work here
            int returnCode = compressSelectedFilesOperation(compressPath);

            handler.post(() -> {
                //UI Thread work here

                switch (returnCode) {
                    case 1:
                        Toast.makeText(context, R.string.action_compress_completed, Toast.LENGTH_SHORT).show();
                        break;
                    case -1:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        break;
                    case -2:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, R.string.error_check_permissions, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }

                if (MainFragment.getCurrentPath().equals(compressPath))
                    MainFragment.refreshList();
            });
        });
    }

    /**
     * Funzione interna per l'estrazione di un archivio zip
     *
     * @param fileToExtract archivio da estrarre
     * @param extractPath   path in cui estrarre l'archivio
     * @return codice di esecuzione:
     * - 1: estrazione andata a buon fine
     * - -1: errore durante l'estrazione dello zip
     * - -2: errore durante la creazione della cartella di estrazione. Nome duplicato (superati tentativi max) o mancanza permessi storage
     * - -3: archivio protetto da password
     */
    private static int extractSelectedFileOperation(File fileToExtract, String extractPath) {
        if (fileToExtract != null) {
            ItemsAdapter.fileToExtract = null;

            String newName = fileToExtract.getName().substring(0, fileToExtract.getName().length() - ".zip".length());
            String originalName = newName;
            File extractLocation = new File(extractPath, newName);
            int i, maxRetries = 10000;

            // gestione di omonimia, aggiunge " (i)" al nome (es. "Test (1)")
            for (i = 1; i < maxRetries; i++) {
                extractLocation = new File(extractPath, newName);

                if (extractLocation.exists())
                    newName = originalName + " (" + i + ")";
                else
                    break;
            }

            if (i == maxRetries || !extractLocation.mkdirs()) {
                return -2;
            }

            try {
                ZipFile zipFile = new ZipFile(fileToExtract.getPath());
                if (zipFile.isEncrypted()) {
                    return -3;
                }

                zipFile.extractAll(extractLocation.getPath());

            } catch (Exception e) {
                Log.w("DEBUG", e.toString());
                return -1;
            }

            return 1;
        }
        return -1;
    }

    /**
     * Funzione interna per la compressione di file in un archivio zip
     *
     * @param compressPath path in cui creare l'archivio
     * @return codice di esecuzione:
     * - 1: estrazione andata a buon fine
     * - -1: errore durante la creazione dello zip
     * - -2: errore Nome archivio duplicato (superati tentativi max) o mancanza permessi storage
     */
    private static int compressSelectedFilesOperation(String compressPath) {
        ArrayList<File> filesToCompress = new ArrayList<>(selectedFilesToCompress.size());
        filesToCompress.addAll(selectedFilesToCompress);

        if (filesToCompress.size() == 0)
            return -1;

        clearSelection();
        selectedFilesToCompress = new ArrayList<>();

        String newName = "archive";
        String originalName = newName;
        File extractLocation = new File(compressPath, newName + ".zip");
        int i, maxRetries = 10000;

        // gestione di omonimia, aggiunge " (i)" al nome (es. "Test (1)")
        for (i = 1; i < maxRetries; i++) {
            extractLocation = new File(compressPath, newName + ".zip");

            if (extractLocation.exists())
                newName = originalName + " (" + i + ")";
            else
                break;
        }

        if (i == maxRetries) {
            return -2;
        }

        ZipFile zipFile = new ZipFile(extractLocation.getPath());

        for (File fileToCompress : filesToCompress) {
            try {
                if (fileToCompress.isDirectory())
                    zipFile.addFolder(fileToCompress);
                else
                    zipFile.addFile(fileToCompress);
            } catch (Exception e) {
                return -1;
            }
        }
        return 1;
    }

    public static void deleteSelectedFilesOperation(String originalPath) {
        if (isSelectionModeEnabled()) {
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
        if (isSelectionModeEnabled()) {
            int selectionType = 0;
            String fileName = "";

            switch (checkSelectedFilesType()) {
                case 1:
                case 11:
                    // 2: singolo file
                    selectionType = 2;
                    fileName = selectedFiles.get(0).getName();
                    break;
                case 2:
                case 12:
                    // 1: singola directory
                    selectionType = 1;
                    fileName = selectedFiles.get(0).getName();
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

            MainFragment.displayDeleteSelectionDialog(selectionType, fileName, selectedFiles.size());
        }
    }

    private static boolean checkIfItemWasSelected(File file) {
        if (selectedFiles.isEmpty())
            return false;
        return selectedFiles.contains(file);
    }

    public static void shareSelectedFiles() {
        if (isSelectionModeEnabled()) {
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
                        context.getPackageName() + ".provider", file));
            }

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
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
     * - 11: selezione completa (generica) ma c'è un solo file
     * - 12: selezione completa (generica) ma c'è una sola cartella
     */

    /* vecchio
     *
     * - 8: selezione generica dentro zip
     * - 9: selezione completa dentro zip
     * - 10: nessuna selezione attiva, ma la cartella corrente è uno zip
     * */
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

        if (filesAndDirs.length == foundFileCount && filesAndDirs.length == 1)
            return 11;
        if (filesAndDirs.length == foundDirsCount && filesAndDirs.length == 1)
            return 12;
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

        return 0; // non dovremmo mai arrivare qui
    }

    public static void openWithSelectedFile() {
        if (isSelectionModeEnabled()) {
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

    private static void installApplication(File file) {
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
            closeSearchView();
            MainFragment.loadPath(selectedFile.getAbsolutePath(), true, false);
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
                    fileToExtract = selectedFile;
                    extractSelectedFile();
                } else if (type.equals("application/vnd.android.package-archive")
                        || type.equals("application/zip") || type.equals("application/java-archive")) {

                    // TODO verificare se serve richiedere i permessi per installare
                    installApplication(selectedFile);
                } else {

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
    }

    private void setItemIcon(int position, @NonNull ItemsViewHolder holder, boolean isSelected) {
        File selectedFile = filesAndDirs[position];

        if (!isSelected) {
            if (Utils.fileIsImage(selectedFile)) {
                loadImageThumbnailAsync(selectedFile, holder);

                /*Bitmap icon = loadImageThumbnail(selectedFile);
                if (icon != null)
                    holder.itemIcon.setImageBitmap(icon);
                else
                    holder.itemIcon.setImageResource(R.drawable.ic_file_generic);*/
            } else
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

    private Bitmap loadImageThumbnail(File file) {
        if (file == null)
            return null;

        //Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (file.exists()) {
            // resize image to 48dp
            Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
            int maxPixelSize = (int) Utils.convertDpToPixel(48, context);

            return Utils.resizeBitmap(image, maxPixelSize, maxPixelSize);
        }

        return null;
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
