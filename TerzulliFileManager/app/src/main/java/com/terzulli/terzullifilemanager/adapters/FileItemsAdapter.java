package com.terzulli.terzullifilemanager.adapters;

import static android.content.Context.MODE_PRIVATE;
import static com.terzulli.terzullifilemanager.utils.Utils.formatFileDetails;
import static com.terzulli.terzullifilemanager.utils.Utils.isFileAZipArchive;
import static com.terzulli.terzullifilemanager.utils.Utils.strOperationMove;

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
import com.terzulli.terzullifilemanager.database.LogDatabase;
import com.terzulli.terzullifilemanager.database.entities.TableItem;
import com.terzulli.terzullifilemanager.database.entities.TableLog;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.RecentsFilesManager;
import com.terzulli.terzullifilemanager.utils.SelectedFilesManager;
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
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileItemsAdapter extends RecyclerView.Adapter<FileItemsAdapter.ItemsViewHolder> {
    private final Context context;
    private final SelectedFilesManager selectedFilesManager;
    private final MainFragment mainFragment;
    private final Activity activityReference;
    private File[] filesAndDirs;
    private LogDatabase logDatabase;

    public FileItemsAdapter(Context context, File[] filesAndFolders, MainFragment mainFragment, Activity activityReference) {
        this.context = context;
        filesAndDirs = filesAndFolders;
        this.mainFragment = mainFragment;
        this.activityReference = activityReference;

        selectedFilesManager = new SelectedFilesManager();
        logDatabase = LogDatabase.getInstance(context);
    }

    /**
     * Funzione per inserimento asincrono di log nel database
     * @param logDatabase istanza del database
     * @param timestamp timestamp dell' 'operazione
     * @param operationSuccess flag che indica se l'operazione è andata a buon fine
     * @param operationType tipologia operazione
     * @param originPath path di origine dell'operazione
     * @param destinationPath (eventuale) path di destinazione dell'operazione
     * @param description descrizione dell'operazione
     * @param operationItems lista di item coinvolti
     * @param operationFailedItems lista di item per cui l'operazione non è andata a buon fine
     */
    public static void insertOpLogIntoDatabase(LogDatabase logDatabase, Date timestamp, boolean operationSuccess,
                                               String operationType, String originPath, String destinationPath,
                                               String description, ArrayList<File> operationItems,
                                               ArrayList<File> operationFailedItems) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            // creazione entry nella tabella log
            TableLog tableLog = new TableLog(timestamp, operationSuccess, operationType, originPath,
                    destinationPath, description);
            long logId = logDatabase.logDao().insert(tableLog);

            // insert andata a buon fine
            if(logId != -1) {
                // inserimento entry nella tabella
                for (File opItem : operationItems) {
                    boolean failed = operationFailedItems.contains(opItem);

                    TableItem tableItem = new TableItem((int) logId, opItem.getName(), opItem.getAbsolutePath(),
                            "", failed);
                    logDatabase.itemDao().insert(tableItem);
                }
            }
        });
    }

    /**
     * Funzione per inserimento asincrono di log nel database
     * @param logDatabase istanza del database
     * @param timestamp timestamp dell' 'operazione
     * @param operationSuccess flag che indica se l'operazione è andata a buon fine
     * @param operationType tipologia operazione
     * @param originPath path di origine dell'operazione
     * @param destinationPath path di destinazione dell'operazione
     * @param description descrizione dell'operazione
     * @param operationItem item su cui è stata svolta l'operazione
     * @param itemNewName (eventuale) nuovo nome dell'item
     */
    public static void insertOpLogIntoDatabase(LogDatabase logDatabase, Date timestamp, boolean operationSuccess,
                                               String operationType, String originPath, String destinationPath,
                                               String description, File operationItem, String itemNewName) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            // creazione entry nella tabella log
            TableLog tableLog = new TableLog(timestamp, operationSuccess, operationType, originPath,
                    destinationPath, description);
            long logId = logDatabase.logDao().insert(tableLog);

            // insert andata a buon fine
            if(logId != -1) {
                // inserimento entry nella tabella
                TableItem tableItem = new TableItem((int) logId, operationItem.getName(), operationItem.getAbsolutePath(),
                        itemNewName, !operationSuccess);
                logDatabase.itemDao().insert(tableItem);
            }
        });


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
        mainFragment.refreshList(false);

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

    /**
     * FUnzione interna per la copia o spostamento di file e cartelle (ricorsiva)
     *
     * @param isCopy          indica se l'operazione è di copia (true) o spostamento (false)
     * @param destinationPath path di destinazione
     * @return codice di esecuzione:
     * - 1: operazione completata con successo
     * - -1: generata eccezione durante l'operazione
     */
    public int copyMoveSelectionOperation(boolean isCopy, String destinationPath) {
        ArrayList<File> filesToCopyMove = new ArrayList<>(selectedFilesManager.getSelectedFilesToCopyMove().size());
        filesToCopyMove.addAll(selectedFilesManager.getSelectedFilesToCopyMove());

        if (filesToCopyMove.size() == 0)
            return -1;

        clearSelection();
        selectedFilesManager.clearSelectionFromCopyMove();
        File newLocation = new File(destinationPath);

        ArrayList<File> filesWithErrors = new ArrayList<>();
        String operationType = Utils.strOperationCopy;
        if(!isCopy)
            operationType = strOperationMove;
        String operationErrorDescription = "";
        int returnCode = 1;

        if (newLocation.exists()) {
            // copy
            for (File fileToMove : filesToCopyMove) {
                try {
                    copyFileLowLevelOperation(fileToMove, newLocation);
                } catch (IOException e) {
                    filesWithErrors.add(fileToMove);
                    returnCode = -1;
                    //return -1;
                }
            }

            // delete if the operation is move
            if (!isCopy) {
                for (File fileToMove : filesToCopyMove) {
                    deleteRecursive(fileToMove);
                }
            }
        } else {
            filesWithErrors.add(newLocation);
            returnCode = -2;
        }

        if(returnCode == -1) {
            if(isCopy)
                operationErrorDescription = context.getResources().getString(R.string.error_copy_generic);
            else
                operationErrorDescription = context.getResources().getString(R.string.error_move_generic);
        }
        else if (returnCode == -2)
            operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir);

        // salvataggio risultato operazione su log
        FileItemsAdapter.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), operationType, "", destinationPath,
                operationErrorDescription, filesToCopyMove, filesWithErrors);

        return returnCode;
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

        executor.execute(() -> {

            int nItems = selectedFilesManager.getSelectedFilesToCopyMove().size();
            int returnCode = copyMoveSelectionOperation(isCopy, destinationPath);

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
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        break;
                    case -2: // la nuova location non esiste
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

                if (mainFragment.getCurrentPath().equals(destinationPath))
                    mainFragment.refreshList(false);
            });
        });
    }

    public void executeExtractOperationOnThread(String extractPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        mainFragment.hideCopyMoveExtractBar();
        Toast.makeText(context, R.string.action_extraction_started, Toast.LENGTH_SHORT).show();

        executor.execute(() -> {

            int returnCode = extractSelectedFileOperation(new File(selectedFilesManager.getFileToExtract().getPath()),
                    extractPath);

            handler.post(() -> {
                String operationType = Utils.strOperationExtract;
                String operationErrorDescription = "";

                switch (returnCode) {
                    case 1:
                        Toast.makeText(context, R.string.action_extract_completed, Toast.LENGTH_SHORT).show();
                        break;
                    case -1:
                        //  errore estrazione
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, R.string.error_check_permissions, Toast.LENGTH_LONG).show();
                        operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_complete);
                        break;
                    case -2:
                        // errore generico estrazione
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, R.string.error_check_permissions, Toast.LENGTH_LONG).show();
                        operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir);
                        break;
                    case -3:
                        // errore estrazione per password
                        Toast.makeText(context, R.string.error_check_password_not_supported, Toast.LENGTH_SHORT).show();
                        operationErrorDescription = context.getResources().getString(R.string.error_check_password_not_supported);
                        break;
                    default:
                        break;
                }

                // salvataggio risultato operazione su log
                FileItemsAdapter.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                        new Date(), (returnCode == 1), operationType, extractPath, "",
                        operationErrorDescription, new File(selectedFilesManager.getFileToExtract().getPath()), "");

                if (mainFragment.getCurrentPath().equals(extractPath))
                    mainFragment.refreshList(false);
            });
        });
    }

    public void executeCompressOperationOnThread(String compressPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        mainFragment.hideCopyMoveExtractBar();
        Toast.makeText(context, R.string.action_compression_started, Toast.LENGTH_SHORT).show();

        executor.execute(() -> {

            int returnCode = compressSelectedFilesOperation(compressPath);

            handler.post(() -> {

                switch (returnCode) {
                    case 1:
                        Toast.makeText(context, R.string.action_compress_completed, Toast.LENGTH_SHORT).show();
                        break;
                    case -1:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, R.string.error_check_permissions, Toast.LENGTH_LONG).show();
                        break;
                    case -2:
                        Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

                if (mainFragment.getCurrentPath().equals(compressPath))
                    mainFragment.refreshList(false);
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
    private int extractSelectedFileOperation(File fileToExtract, String extractPath) {
        if (fileToExtract != null) {
            selectedFilesManager.setFileToExtract(null);

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
    private int compressSelectedFilesOperation(String compressPath) {
        ArrayList<File> filesToCompress = new ArrayList<>(selectedFilesManager.getSelectedFilesToCompress().size());
        filesToCompress.addAll(selectedFilesManager.getSelectedFilesToCompress());

        int returnCode = 1;
        ArrayList<File> filesWithErrors = new ArrayList<>();
        String operationType = Utils.strOperationCompress;
        String operationErrorDescription = "";

        if (filesToCompress.size() == 0)
            returnCode = -1;
        else {
            clearSelection();
            selectedFilesManager.setSelectedFilesToCompress(new ArrayList<>());

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
                returnCode = -2;
            } else {

                ZipFile zipFile = new ZipFile(extractLocation.getPath());

                for (File fileToCompress : filesToCompress) {
                    try {
                        if (fileToCompress.isDirectory())
                            zipFile.addFolder(fileToCompress);
                        else
                            zipFile.addFile(fileToCompress);
                    } catch (Exception e) {
                        filesWithErrors.add(fileToCompress);
                        returnCode = -1;
                    }
                }
            }

        }

        if(returnCode == -1)
            operationErrorDescription = context.getResources().getString(R.string.error_compression_cannot_compress_file);
        else if (returnCode == -2)
            operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir);

        // salvataggio risultato operazione su log
        FileItemsAdapter.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), operationType, "", compressPath,
                operationErrorDescription, filesToCompress, filesWithErrors);


        return returnCode;
    }

    public void deleteSelectedFilesOperation(String originalPath) {
        if (isSelectionModeEnabled()) {
            ArrayList<File> filestoDelete = new ArrayList<>(selectedFilesManager.getSelectedFiles().size());
            filestoDelete.addAll(selectedFilesManager.getSelectedFiles());
            clearSelection();

            ArrayList<File> filesWithErrors = new ArrayList<>();

            for (File file : filestoDelete) {
                if(!deleteRecursive(file))
                    filesWithErrors.add(file);
            }

            String operationType = Utils.strOperationDelete;
            String operationErrorDescription = "";
            if(!filesWithErrors.isEmpty())
                operationErrorDescription = context.getResources().getString(R.string.error_cannot_delete_item);

            // salvataggio risultato operazione su log
            FileItemsAdapter.insertOpLogIntoDatabase(LogDatabase.getInstance(context), new Date(),
                    filesWithErrors.isEmpty(), operationType, originalPath, "",
                    operationErrorDescription, filestoDelete, filesWithErrors);

            if (mainFragment.getCurrentPath().equals(originalPath))
                mainFragment.refreshList(false);
        }
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles()))
                deleteRecursive(child);
        }

        return file.delete();
    }

    private void copyFileLowLevelOperation(File sourceLocation, File targetLocation)
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

                /*if (!outFile.exists())
                    outFile.createNewFile();*/

                if (!outFile.exists() && outFile.createNewFile())
                    throw new IOException("Cannot create file " + outFile.getAbsolutePath());

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
