package com.terzulli.terzullifilemanager.fragments;

import static com.terzulli.terzullifilemanager.utils.FileFunctions.strOperationCompress;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.strOperationCopy;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.strOperationDelete;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.strOperationExtract;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.strOperationMove;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.strOperationNewFolder;
import static com.terzulli.terzullifilemanager.utils.FileFunctions.strOperationRename;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.database.LogDatabase;
import com.terzulli.terzullifilemanager.database.entities.TableItem;
import com.terzulli.terzullifilemanager.database.entities.TableLog;
import com.terzulli.terzullifilemanager.utils.FileFunctions;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogFragment extends Fragment {

    private int logId = -1;
    private View view;
    private TextView typeValue, timestampValue, resultValue, retriedValue, nItemsValue, nItemsFailedValue,
            originPathValue, destPathValue, errorReasonValue, filesValue, failedFilesValue, newNameValue;
    private LinearLayout retriedContainer, nFailedItemsContainer, originPathContainer,
        destPathContainer, errorReasonContainer, newNameContainer;
    private RelativeLayout failedFilesContainer, log_retry_bar;

    public LogFragment() {
        // Required empty public constructor
    }

    private void loadLogContent() {
        if(logId == -1) {
            Toast.makeText(view.getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            LogDatabase logDatabase = LogDatabase.getInstance(view.getContext());
            assert logDatabase != null;
            ArrayList<TableItem> itemsList = (ArrayList<TableItem>) Objects.requireNonNull(logDatabase.logDao()).findItemList(logId);
            TableLog thisLog = Objects.requireNonNull(logDatabase.logDao()).findById(logId);

            handler.post(() -> {
                // se nel frattempo l'utente ha scelto di caricare un'altra schermata,
                // annullo questo caricamento

                switch (Objects.requireNonNull(thisLog).getOperationType()) {
                    case strOperationNewFolder:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_new_folder));

                        destPathContainer.setVisibility(View.VISIBLE);
                        newNameContainer.setVisibility(View.VISIBLE);

                        destPathValue.setText(thisLog.getDestinationPath());
                        if(itemsList != null && !itemsList.isEmpty())
                            newNameValue.setText(itemsList.get(0).getName());
                        break;
                    case strOperationCompress:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_compress));
                        destPathContainer.setVisibility(View.VISIBLE);
                        destPathValue.setText(thisLog.getDestinationPath());
                        break;
                    case strOperationExtract:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_extract));
                        destPathContainer.setVisibility(View.VISIBLE);
                        destPathValue.setText(thisLog.getDestinationPath());
                        break;
                    case strOperationCopy:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_copy));
                        destPathContainer.setVisibility(View.VISIBLE);
                        destPathValue.setText(thisLog.getDestinationPath());
                        break;
                    case strOperationMove:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_move));
                        destPathContainer.setVisibility(View.VISIBLE);
                        destPathValue.setText(thisLog.getDestinationPath());
                        break;
                    case strOperationRename:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_rename));

                        originPathContainer.setVisibility(View.VISIBLE);
                        newNameContainer.setVisibility(View.VISIBLE);

                        originPathValue.setText(thisLog.getOriginPath());
                        if(itemsList != null && !itemsList.isEmpty())
                            newNameValue.setText(itemsList.get(0).getNewName());
                        break;
                    case strOperationDelete:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_delete));
                        originPathContainer.setVisibility(View.VISIBLE);
                        originPathValue.setText(thisLog.getOriginPath());
                        break;
                    default:
                        // non dovremmo mai arrivarci
                        typeValue.setText(thisLog.getOperationType());
                }

                timestampValue.setText(Utils.formatDateDetailsFullWithMilliseconds(thisLog.getTimestamp()));

                if(thisLog.getOperationSuccess()) {
                    resultValue.setText(view.getResources().getString(R.string.log_op_success));
                    retriedContainer.setVisibility(View.GONE);
                    errorReasonContainer.setVisibility(View.GONE);
                } else {
                    resultValue.setText(view.getResources().getString(R.string.log_op_failed));
                    errorReasonValue.setText(thisLog.getDescription());
                    retriedContainer.setVisibility(View.VISIBLE);
                    errorReasonContainer.setVisibility(View.VISIBLE);

                    if(thisLog.isRetried()) {
                        retriedValue.setText(view.getResources().getString(R.string.log_op_retried_true));
                    } else {
                        retriedValue.setText(view.getResources().getString(R.string.log_op_retried_false));
                        displayRetryBar();
                    }
                }

                if(!thisLog.getOriginPath().equals("")) {
                    originPathValue.setVisibility(View.VISIBLE);
                    originPathValue.setText(thisLog.getOriginPath());
                } else
                    originPathContainer.setVisibility(View.GONE);

                if(!thisLog.getDestinationPath().equals("")) {
                    destPathValue.setVisibility(View.VISIBLE);
                    destPathValue.setText(thisLog.getDestinationPath());
                } else
                    destPathContainer.setVisibility(View.GONE);

                if(!thisLog.getOperationSuccess()) {
                    StringBuilder opFailedFilesText = new StringBuilder();
                    ArrayList<TableItem> failedItemsList = new ArrayList<>();
                    assert itemsList != null;
                    for (TableItem item : itemsList) {
                        if (item.isOpFailed()) {
                            opFailedFilesText.append("<b>").append(item.getName()).append("</b><br>").append(item.getOriginPath()).append("<br><br>");
                            failedItemsList.add(item);
                        }
                    }
                    failedFilesValue.setText(Html.fromHtml(String.valueOf(opFailedFilesText), Html.FROM_HTML_MODE_LEGACY));

                    failedFilesContainer.setVisibility(View.VISIBLE);
                    nFailedItemsContainer.setVisibility(View.VISIBLE);

                    nItemsFailedValue.setText(failedItemsList.size() + "");
                } else {
                    failedFilesContainer.setVisibility(View.GONE);
                    nFailedItemsContainer.setVisibility(View.GONE);
                }

                assert itemsList != null;
                nItemsValue.setText(itemsList.size() + "");

                StringBuilder opFilesText = new StringBuilder();
                for (TableItem item : itemsList) {
                    opFilesText.append("<b>").append(item.getName()).append("</b><br>").append(item.getOriginPath()).append("<br><br>");
                }
                filesValue.setText(Html.fromHtml(String.valueOf(opFilesText), Html.FROM_HTML_MODE_LEGACY));
            });
        });
    }

    public void displayRetryBar() {
        log_retry_bar.setVisibility(View.VISIBLE);

        Button btnCancel = view.findViewById(R.id.log_retry_bar_btn_cancel_operation);
        Button btnConfirm = view.findViewById(R.id.log_retry_bar_confirm_operation);

        btnCancel.setOnClickListener(view -> log_retry_bar.setVisibility(View.GONE));

        btnConfirm.setOnClickListener(view -> {
            log_retry_bar.setVisibility(View.GONE);
            retriedValue.setText(view.getResources().getString(R.string.log_op_retried_true));

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                // aggiorno lo stato
                LogDatabase logDatabase = LogDatabase.getInstance(view.getContext());
                assert logDatabase != null;
                Objects.requireNonNull(logDatabase.logDao()).updateRetried(logId, true);

                // ritento l'operazione
                TableLog thisLog = Objects.requireNonNull(logDatabase.logDao()).findById(logId);
                ArrayList<TableItem> itemsList = (ArrayList<TableItem>) Objects.requireNonNull(logDatabase.logDao()).findItemList(logId);

                // ottengo i file su cui è fallita
                ArrayList<TableItem> failedItemsList = new ArrayList<>();
                assert itemsList != null;
                for (TableItem item : itemsList) {
                    if (item.isOpFailed()) {
                        failedItemsList.add(item);
                    }
                }

                ArrayList<File> failedFilesList = new ArrayList<>();
                for (TableItem item : failedItemsList) {
                    failedFilesList.add(new File(item.getOriginPath()));
                }

                int returnCode = -1;

                if(!failedItemsList.isEmpty()) {
                    switch (Objects.requireNonNull(thisLog).getOperationType()) {
                        case strOperationNewFolder:
                            returnCode = FileFunctions.createDirectoryOperation(new File(thisLog.getDestinationPath()),
                                    failedItemsList.get(0).getName(), view.getContext());
                            break;
                        case strOperationCompress:
                            returnCode = FileFunctions.compressSelectedFilesOperation(thisLog.getDestinationPath(),
                                    failedFilesList, view.getContext());
                            break;
                        case strOperationExtract:
                            returnCode = FileFunctions.extractSelectedFileOperation(new File(failedItemsList.get(0).getOriginPath()),
                                    thisLog.getDestinationPath(),view.getContext());
                            break;
                        case strOperationCopy:
                            returnCode = FileFunctions.copyMoveSelectionOperation(true, thisLog.getDestinationPath(),
                                    failedFilesList, view.getContext());
                            break;
                        case strOperationMove:
                            returnCode = FileFunctions.copyMoveSelectionOperation(false, thisLog.getDestinationPath(),
                                    failedFilesList, view.getContext());
                            break;
                        case strOperationRename:
                            returnCode = FileFunctions.renameSelectedFileOperation(new File(thisLog.getOriginPath()),
                                    failedItemsList.get(0).getNewName(), view.getContext());
                            break;
                        case strOperationDelete:
                            returnCode = FileFunctions.deleteSelectedFilesOperation(thisLog.getOriginPath(),
                                    failedFilesList, view.getContext());
                            break;
                        default:
                            // non dovremmo mai arrivarci
                    }
                }

                int finalReturnCode = returnCode;
                handler.post(() -> {
                    if(finalReturnCode == 1)
                        Toast.makeText(view.getContext(), R.string.log_op_retried_successfully, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(view.getContext(), R.string.log_op_retried_error, Toast.LENGTH_SHORT).show();

                });
            });
        });
    }

    public void goBack() {
        Activity activityReference = requireActivity();

        if(activityReference instanceof MainActivity) {
            ((MainActivity)activityReference).setActionBarToggleDefault();

            MainFragment mainFragment = new MainFragment();
            ((MainActivity)activityReference).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_content, mainFragment, "main_fragment")
                    .commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            logId = getArguments().getInt("logId", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_log, container, false);

        typeValue = view.findViewById(R.id.log_op_type_value);
        timestampValue = view.findViewById(R.id.log_op_timestamp_value);
        resultValue = view.findViewById(R.id.log_op_result_value);
        retriedValue = view.findViewById(R.id.log_op_retried_value);
        nItemsValue = view.findViewById(R.id.log_op_number_of_items_value);
        nItemsFailedValue = view.findViewById(R.id.log_op_number_of_failed_items_value);
        originPathValue = view.findViewById(R.id.log_op_start_path_value);
        destPathValue = view.findViewById(R.id.log_op_destination_path_value);
        errorReasonValue = view.findViewById(R.id.log_op_error_reason_value);
        filesValue = view.findViewById(R.id.log_op_files_value);
        failedFilesValue = view.findViewById(R.id.log_op_files_failed_value);
        newNameValue = view.findViewById(R.id.log_op_new_name_value);

        retriedContainer = view.findViewById(R.id.log_op_retried_container);
        nFailedItemsContainer = view.findViewById(R.id.log_op_number_of_failed_items_container);
        originPathContainer = view.findViewById(R.id.log_op_start_path_container);
        destPathContainer = view.findViewById(R.id.log_op_destination_path_container);
        errorReasonContainer = view.findViewById(R.id.log_op_error_reason_container);
        newNameContainer = view.findViewById(R.id.log_op_new_name_container);

        failedFilesContainer = view.findViewById(R.id.log_op_files_failed_container);
        log_retry_bar = view.findViewById(R.id.log_retry_bar);

        loadLogContent();
        if(requireActivity() instanceof MainActivity) {
            ((MainActivity)requireActivity()).setActionBarToggleBackButton();
        }

        return view;
    }
}