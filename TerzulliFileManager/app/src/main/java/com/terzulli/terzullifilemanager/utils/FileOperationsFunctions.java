package com.terzulli.terzullifilemanager.utils;

import static android.content.Context.MODE_PRIVATE;

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

public abstract class FileOperationsFunctions {
    public static final String STR_OPERATION_NEW_FOLDER = "new_folder";
    public static final String STR_OPERATION_COMPRESS = "compress";
    public static final String STR_OPERATION_EXTRACT = "extract";
    public static final String STR_OPERATION_COPY = "copy";
    public static final String STR_OPERATION_MOVE = "move";
    public static final String STR_OPERATION_RENAME = "rename";
    public static final String STR_OPERATION_DELETE = "delete";

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

                    TableItem tableItem = new TableItem((int) logId, opItem.getName() , opItem.getPath(),
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
                TableItem tableItem = new TableItem((int) logId, operationItem.getName(), operationItem.getPath(),
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
     * @param context contesto
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

        FileOperationsFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), STR_OPERATION_EXTRACT, extractPath, "",
                operationErrorDescription, fileToExtract, "");

        return returnCode;
    }

    /**
     * Funzione per la creazione di directory
     *
     * @param currentDirectory path in cui creare la directory
     * @param newDirectoryName nome della nuova directory
     * @param context contesto
     * @return codice di esecuzione:
     * - 1: operazione completata con successo
     * - -1: impossibile eseguire l'operazione
     */
    public static int createDirectoryOperation(@NonNull File currentDirectory, @NonNull String newDirectoryName,
                                                  @NonNull Context context) {
        int returnCode = 1;
        String operationErrorDescription = "";

        File newDir = new File(currentDirectory, newDirectoryName);
        String originalName = newDirectoryName;
        int i, maxRetries = 10000;

        // gestione di omonimia, aggiunge " (i)" al nome (es. "Test (1)")
        for (i = 1; i < maxRetries; i++) {
            newDir = new File(currentDirectory, newDirectoryName);

            if (newDir.exists())
                newDirectoryName = originalName + " (" + i + ")";
            else
                break;
        }

        if (i == maxRetries) {
            returnCode = -1;
            operationErrorDescription = context.getResources().getString(R.string.error_cannot_create_folder);
        } else {
            if (!newDir.mkdirs()) {
                returnCode = -1;
                operationErrorDescription = context.getResources().getString(R.string.error_cannot_create_folder);
            }
        }

        // salvataggio risultato operazione su log
        FileOperationsFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), STR_OPERATION_NEW_FOLDER, currentDirectory.getAbsolutePath(), currentDirectory.getAbsolutePath(),
                operationErrorDescription, newDir, "");

        return returnCode;
    }

    /**
     * Funzione per la rinominazione di file
     *
     * @param file file su cui effettuare l'operazione
     * @param newName nuovo nome del file
     * @param context contesto
     * @return codice di esecuzione:
     * - 1: operazione completata con successo
     * - -1: impossibile eseguire l'operazione
     * - -2: il file non esiste
     */
    public static int renameSelectedFileOperation(@NonNull File file, @NonNull String newName,
                                                   @NonNull Context context) {
        int returnCode;
        String operationErrorDescription = "";
        String path = "";

        File dir = file.getParentFile();
        if (dir != null && dir.exists()) {
            File from = new File(dir, file.getName());
            File to = new File(dir, newName);
            path = dir.getAbsolutePath();

            if (from.exists() && from.renameTo(to)) {
                returnCode = 1;
            } else {
                operationErrorDescription = context.getResources().getString(R.string.error_cannot_rename);
                returnCode = -1;
            }

            // aggiunta file rinominato ai file recenti
            if(!to.isDirectory() && returnCode == 1) {
                RecentsFilesManager recentsFilesManager = new RecentsFilesManager(context.getSharedPreferences("TerzulliFileManager", MODE_PRIVATE));
                recentsFilesManager.addFileToRecentsFilesList(to);
            }
        } else {
            returnCode = -2;
            operationErrorDescription = context.getResources().getString(R.string.error_rename_not_exists);
        }

        // salvataggio risultato operazione su log
        FileOperationsFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), STR_OPERATION_RENAME, path, "",
                operationErrorDescription, file, newName);

        return returnCode;
    }

    /**
     * Funzione per la cancellazione di file
     *
     * @param originalPath path dei file da cancellare
     * @param filestoDelete file da cancellare
     * @param context contesto
     * @return codice di esecuzione:
     * - 1: operazione completata con successo
     * - -1: impossibile eseguire l'operazione su alcuni file
     */
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
        FileOperationsFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context), new Date(),
                filesWithErrors.isEmpty(), STR_OPERATION_DELETE, originalPath, "",
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
     * FUnzione per la copia o spostamento di file e cartelle (ricorsiva)
     *
     * @param isCopy          indica se l'operazione è di copia (true) o spostamento (false)
     * @param destinationPath path di destinazione
     * @param filesToCopyMove file su cui eseguire l'operazione
     * @param context contesto
     * @return codice di esecuzione:
     * - 1: operazione completata con successo
     * - -1: generata eccezione durante l'operazione
     * - -2: impossible creare la directory di destinazione
     * - -3: la directory di destinazione è uno dei file che si sta copiando /spostando
     */
    public static int copyMoveSelectionOperation(boolean isCopy, @NonNull String destinationPath,
                                                 @NonNull ArrayList<File>filesToCopyMove,
                                                 @NonNull Context context) {
        File newLocation = new File(destinationPath);

        ArrayList<File> filesWithErrors = new ArrayList<>();
        String operationType = STR_OPERATION_COPY;
        if(!isCopy)
            operationType = STR_OPERATION_MOVE;
        String operationErrorDescription = "";
        int returnCode = 1;

        if (newLocation.exists()) {
            // controllo che la destinazione non sia uno dei file su cui sto effettuando l'operazione
            if(filesToCopyMove.contains(newLocation)) {
                returnCode = -3;
                filesWithErrors.addAll(filesToCopyMove);
                // l'operazione viene interrotta
            } else {
                // copy
                for (File fileToMove : filesToCopyMove) {
                    try {
                        copyFileLowLevelOperation(fileToMove, newLocation);
                    } catch (IOException e) {
                        filesWithErrors.add(fileToMove);
                        returnCode = -1;
                    }
                }

                // delete if the operation is move
                if (!isCopy) {
                    for (File fileToMove : filesToCopyMove) {
                        deleteRecursive(fileToMove);
                    }
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
        else if (returnCode == -3){
            if(isCopy)
                operationErrorDescription = context.getResources().getString(R.string.error_cannot_copy_into_itself);
            else
                operationErrorDescription = context.getResources().getString(R.string.error_cannot_move_into_itself);
        }

        // salvataggio risultato operazione su log
        FileOperationsFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
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

                if (!outFile.exists() && !outFile.createNewFile())
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
     * Funzione per la compressione di file in un archivio zip
     *
     * @param compressPath path in cui creare l'archivio
     * @param filesToCompress file su cui eseguire l'operazione
     * @param context contesto
     * @return codice di esecuzione:
     * - 1: estrazione andata a buon fine
     * - -1: errore durante la creazione dello zip
     * - -2: errore Impossibile creare il file di destinazione
     * - -3: la directory di destinazione è uno dei file che si sta comprimendo
     */
    public static int compressSelectedFilesOperation(@NonNull String compressPath,
                                                     @NonNull ArrayList<File> filesToCompress,
                                                     @NonNull Context context) {

        int returnCode = 1;
        ArrayList<File> filesWithErrors = new ArrayList<>();
        String operationErrorDescription = "";

        String newName = "archive";
        String originalName = newName;
        File compressedFile = new File(compressPath, newName + ".zip");
        int i, maxRetries = 10000;

        // controllo che la destinazione non sia uno dei file su cui sto effettuando l'operazione
        if(filesToCompress.contains(new File(compressPath))) {
            returnCode = -3;
            filesWithErrors.addAll(filesToCompress);
            // l'operazione viene interrotta
        } else {
            // gestione di omonimia, aggiunge " (i)" al nome (es. "Test (1)")
            for (i = 1; i < maxRetries; i++) {
                compressedFile = new File(compressPath, newName + ".zip");

                if (compressedFile.exists())
                    newName = originalName + " (" + i + ")";
                else
                    break;
            }

            if (i == maxRetries) {
                returnCode = -2;
            } else {

                ZipFile zipFile = new ZipFile(compressedFile.getPath());

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
            operationErrorDescription = context.getResources().getString(R.string.error_extraction_cannot_create_dest_file);
        else if (returnCode == -3)
            operationErrorDescription = context.getResources().getString(R.string.error_cannot_compress_into_itself);

        // salvataggio risultato operazione su log
        FileOperationsFunctions.insertOpLogIntoDatabase(LogDatabase.getInstance(context),
                new Date(), (returnCode == 1), STR_OPERATION_COMPRESS, "", compressPath,
                operationErrorDescription, filesToCompress, filesWithErrors);

        // aggiunta file compresso ai file recenti
        if(!compressedFile.isDirectory() && returnCode == 1) {
            RecentsFilesManager recentsFilesManager = new RecentsFilesManager(context.getSharedPreferences("TerzulliFileManager", MODE_PRIVATE));
            recentsFilesManager.addFileToRecentsFilesList(compressedFile);
        }

        return returnCode;
    }


}
