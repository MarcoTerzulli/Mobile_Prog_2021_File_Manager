package com.terzulli.terzullifilemanager.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.database.LogDatabase;
import com.terzulli.terzullifilemanager.database.entities.TableItem;
import com.terzulli.terzullifilemanager.database.entities.TableLog;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileFunctions {
    public static final String strOperationNewFolder = "new_folder";
    public static final String strOperationCompress = "compress";
    public static final String strOperationExtract = "extract";
    public static final String strOperationCopy = "copy";
    public static final String strOperationMove = "move";
    public static final String strOperationRename = "rename";
    public static final String strOperationDelete = "delete";

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
    public static void insertOpLogIntoDatabase(LogDatabase logDatabase, @NonNull Date timestamp,
                                               boolean operationSuccess,
                                               @NonNull String operationType, @NonNull String originPath,
                                               @NonNull String destinationPath, @NonNull String description,
                                               @NonNull ArrayList<File> operationItems,
                                               @NonNull ArrayList<File> operationFailedItems) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            // creazione entry nella tabella log
            TableLog tableLog = new TableLog(timestamp, operationSuccess, operationType, originPath,
                    destinationPath, description);
            long logId = Objects.requireNonNull(logDatabase.logDao()).insert(tableLog);

            // insert andata a buon fine
            if(logId != -1) {
                // inserimento entry nella tabella
                for (File opItem : operationItems) {
                    boolean failed = operationFailedItems.contains(opItem);

                    TableItem tableItem = new TableItem((int) logId, opItem.getName(), opItem.getAbsolutePath(),
                            "", failed);
                    Objects.requireNonNull(logDatabase.itemDao()).insert(tableItem);
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
    public static void insertOpLogIntoDatabase(LogDatabase logDatabase, @NonNull Date timestamp,
                                               boolean operationSuccess,
                                               @NonNull String operationType, @NonNull String originPath,
                                               @NonNull String destinationPath, @NonNull String description,
                                               @NonNull File operationItem, @NonNull String itemNewName) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            // creazione entry nella tabella log
            TableLog tableLog = new TableLog(timestamp, operationSuccess, operationType, originPath,
                    destinationPath, description);
            long logId = Objects.requireNonNull(logDatabase.logDao()).insert(tableLog);

            // insert andata a buon fine
            if(logId != -1) {
                // inserimento entry nella tabella
                TableItem tableItem = new TableItem((int) logId, operationItem.getName(), operationItem.getAbsolutePath(),
                        itemNewName, !operationSuccess);
                Objects.requireNonNull(logDatabase.itemDao()).insert(tableItem);
            }
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
    public static int extractSelectedFileOperation(File fileToExtract, @NonNull String extractPath,
                                                   @NonNull Context context) {
        String operationErrorDescription = "";
        int returnCode = 1;

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
            returnCode = -2;
        } else {

            try {
                ZipFile zipFile = new ZipFile(fileToExtract.getPath());
                if (zipFile.isEncrypted()) {
                    returnCode = -3;
                } else
                    zipFile.extractAll(extractLocation.getPath());

            } catch (Exception e) {
                returnCode = -1;
            }
        }

        // salvataggio log
        switch (returnCode) {
            case -1:
                operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_complete);
                break;
            case -2:
                // errore generico estrazione
                operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir);
                break;
            case -3:
                // errore estrazione per password
                operationErrorDescription = context.getResources().getString(R.string.error_check_password_not_supported);
                break;
            default:
                break;
        }

        FileFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), strOperationExtract, extractPath, "",
                operationErrorDescription, fileToExtract, "");

        return returnCode;
    }


    public static int deleteSelectedFilesOperation(@NonNull String originalPath, @NonNull ArrayList<File> filestoDelete,
                                                   @NonNull Context context) {
        ArrayList<File> filesWithErrors = new ArrayList<>();

        for (File file : filestoDelete) {
            if(!deleteRecursive(file))
                filesWithErrors.add(file);
        }

        String operationErrorDescription = "";
        if(!filesWithErrors.isEmpty())
            operationErrorDescription = context.getResources().getString(R.string.error_cannot_delete_item);

        // salvataggio risultato operazione su log
        FileFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context), new Date(),
                filesWithErrors.isEmpty(), strOperationDelete, originalPath, "",
                operationErrorDescription, filestoDelete, filesWithErrors);

        if(!filesWithErrors.isEmpty())
            return -1;
        return 1;
    }

    private static boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles()))
                deleteRecursive(child);
        }

        return file.delete();
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
    public static int copyMoveSelectionOperation(boolean isCopy, @NonNull String destinationPath,
                                                 @NonNull ArrayList<File>filesToCopyMove,
                                                 @NonNull Context context) {
        File newLocation = new File(destinationPath);

        ArrayList<File> filesWithErrors = new ArrayList<>();
        String operationType = strOperationCopy;
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
        FileFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), operationType, "", destinationPath,
                operationErrorDescription, filesToCopyMove, filesWithErrors);

        return returnCode;
    }

    private static void copyFileLowLevelOperation(@NonNull File sourceLocation, @NonNull File targetLocation)
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

    /**
     * Funzione interna per la compressione di file in un archivio zip
     *
     * @param compressPath path in cui creare l'archivio
     * @return codice di esecuzione:
     * - 1: estrazione andata a buon fine
     * - -1: errore durante la creazione dello zip
     * - -2: errore Nome archivio duplicato (superati tentativi max) o mancanza permessi storage
     */
    public static int compressSelectedFilesOperation(@NonNull String compressPath,
                                                     @NonNull ArrayList<File> filesToCompress,
                                                     @NonNull Context context) {

        int returnCode = 1;
        ArrayList<File> filesWithErrors = new ArrayList<>();
        String operationErrorDescription = "";

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

        if(returnCode == -1)
            operationErrorDescription = context.getResources().getString(R.string.error_compression_cannot_compress_file);
        else if (returnCode == -2)
            operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_create_dest_dir);

        // salvataggio risultato operazione su log
        FileFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), strOperationCompress, "", compressPath,
                operationErrorDescription, filesToCompress, filesWithErrors);


        return returnCode;
    }


}
