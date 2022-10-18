package com.terzulli.terzullifilemanager.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class SelectedFilesManager {
    private static ArrayList<File> currentFilesBeforeQuerySubmit = new ArrayList<>();
    private static ArrayList<File> selectedFiles = new ArrayList<>();
    private static ArrayList<File> selectedFilesToCopyMove = new ArrayList<>();
    private static ArrayList<File> selectedFilesToCompress = new ArrayList<>();
    private static boolean copyMoveOperationTypeIsCopy = false;
    private static File fileToExtract = null;
    private static String operationStartPath;

    public SelectedFilesManager() {
        // non si fa nulla
    }

    public String getOperationStartPath() {
        return operationStartPath;
    }

    public void setOperationStartPath(String operationStartPath) {
        SelectedFilesManager.operationStartPath = operationStartPath;
    }

    public File getFileToExtract() {
        return fileToExtract;
    }

    public void setFileToExtract(File fileToExtract) {
        SelectedFilesManager.fileToExtract = fileToExtract;
    }

    public Boolean copyMoveOperationTypeIsCopy() {
        return copyMoveOperationTypeIsCopy;
    }

    public void setCopyMoveOperationTypeIsCopy(Boolean copyMoveOperationTypeIsCopy)  {
        SelectedFilesManager.copyMoveOperationTypeIsCopy = copyMoveOperationTypeIsCopy;
    }

    public ArrayList<File> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(ArrayList<File> selectedFiles) {
        SelectedFilesManager.selectedFiles = selectedFiles;
    }

    public void setSelectedFilesToCopyMove(ArrayList<File> selectedFilesToCopyMove) {
        SelectedFilesManager.selectedFilesToCopyMove = new ArrayList<>(selectedFilesToCopyMove.size());
        SelectedFilesManager.selectedFilesToCopyMove.addAll(selectedFilesToCopyMove);
    }

    public ArrayList<File> getSelectedFilesToCopyMove() {
        return selectedFilesToCopyMove;
    }

    public void setSelectedFilesToCompress(ArrayList<File> selectedFilesToCompress) {
        SelectedFilesManager.selectedFilesToCompress = new ArrayList<>(selectedFilesToCompress.size());
        SelectedFilesManager.selectedFilesToCompress.addAll(selectedFilesToCompress);
    }

    public ArrayList<File> getSelectedFilesToCompress() {
        return selectedFilesToCompress;
    }

    public void setCurrentFilesBeforeQuerySubmit(File[] currentFilesBeforeQuerySubmit) {
        SelectedFilesManager.currentFilesBeforeQuerySubmit = new ArrayList<>(currentFilesBeforeQuerySubmit.length);
        SelectedFilesManager.currentFilesBeforeQuerySubmit.addAll(Arrays.asList(currentFilesBeforeQuerySubmit));
    }

    public ArrayList<File> getCurrentFilesBeforeQuerySubmit() {
        return currentFilesBeforeQuerySubmit;
    }

    public void clearCurrentFilesBeforeQuerySubmit() {
        currentFilesBeforeQuerySubmit.clear();
    }

    public void clearSelectionFromCopyMove() {
        selectedFilesToCopyMove.clear();
    }

    public void clearSelectedFiles() {
        selectedFiles.clear();
    }

    public void clearSelectionFromCompress() {
        selectedFilesToCompress.clear();
    }

    public void recoverSelectionFromCopyMove() {
        selectedFiles = new ArrayList<>(selectedFilesToCopyMove.size());
        selectedFiles.addAll(selectedFilesToCopyMove);
        selectedFilesToCopyMove.clear();
    }

    public void saveSelectionForCopyMove() {
        selectedFilesToCopyMove = new ArrayList<>(selectedFiles.size());
        selectedFilesToCopyMove.addAll(selectedFiles);
        selectedFiles.clear();
    }

    public void recoverSelectionFromCompress() {
        selectedFiles = new ArrayList<>(selectedFilesToCompress.size());
        selectedFiles.addAll(selectedFilesToCompress);
        selectedFilesToCompress.clear();
    }

    public void addSelectedFile(File file) {
        selectedFiles.add(file);
    }

    public void removeSelectedFile(File file) {
        selectedFiles.remove(file);
    }

}
