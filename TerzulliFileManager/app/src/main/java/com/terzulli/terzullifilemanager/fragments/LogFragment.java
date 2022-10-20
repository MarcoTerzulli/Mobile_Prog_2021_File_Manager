package com.terzulli.terzullifilemanager.fragments;

import static com.terzulli.terzullifilemanager.utils.Utils.strOperationCompress;
import static com.terzulli.terzullifilemanager.utils.Utils.strOperationCopy;
import static com.terzulli.terzullifilemanager.utils.Utils.strOperationDelete;
import static com.terzulli.terzullifilemanager.utils.Utils.strOperationExtract;
import static com.terzulli.terzullifilemanager.utils.Utils.strOperationMove;
import static com.terzulli.terzullifilemanager.utils.Utils.strOperationNewFolder;
import static com.terzulli.terzullifilemanager.utils.Utils.strOperationRename;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.database.LogDatabase;
import com.terzulli.terzullifilemanager.database.entities.TableItem;
import com.terzulli.terzullifilemanager.database.entities.TableLog;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogFragment extends Fragment {

    private int logId = -1;
    private View view;
    private TextView typeValue, timestampValue, resultValue, retriedValue, nItemsValue, nItemsFailedValue,
            originPathValue, destPathValue, errorReasonValue, filesValue, failedFilesValue;
    private LinearLayout retriedContainer, nItemsContainer, nFailedItemsContainer, originPathContainer,
        destPathContainer, errorReasonContainer;
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

                switch (thisLog.getOperationType()) {
                    case strOperationNewFolder:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_new_folder));
                        break;
                    case strOperationCompress:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_compress));
                        break;
                    case strOperationExtract:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_extract));
                        break;
                    case strOperationCopy:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_copy));
                        break;
                    case strOperationMove:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_move));
                        break;
                    case strOperationRename:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_rename));
                        break;
                    case strOperationDelete:
                        typeValue.setText(view.getResources().getString(R.string.log_op_type_delete));
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
                        log_retry_bar.setVisibility(View.GONE);
                    } else {
                        retriedValue.setText(view.getResources().getString(R.string.log_op_retried_false));
                        log_retry_bar.setVisibility(View.VISIBLE);
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
                    ArrayList<TableItem> failedItemsList = new ArrayList<>();
                    assert itemsList != null;
                    for (TableItem item : itemsList) {
                        if (item.isOpFailed())
                            failedItemsList.add(item);
                    }

                    failedFilesContainer.setVisibility(View.VISIBLE);
                    nFailedItemsContainer.setVisibility(View.VISIBLE);

                    nItemsFailedValue.setText(failedItemsList.size() + "");
                } else {
                    failedFilesContainer.setVisibility(View.GONE);
                    nFailedItemsContainer.setVisibility(View.GONE);
                }

                assert itemsList != null;
                nItemsValue.setText(itemsList.size() + "");


                // todo riempimento item list
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

        retriedContainer = view.findViewById(R.id.log_op_retried_container);
        nItemsContainer = view.findViewById(R.id.log_op_number_of_items_container);
        nFailedItemsContainer = view.findViewById(R.id.log_op_number_of_failed_items_container);
        originPathContainer = view.findViewById(R.id.log_op_start_path_container);
        destPathContainer = view.findViewById(R.id.log_op_destination_path_container);
        errorReasonContainer = view.findViewById(R.id.log_op_error_reason_container);

        failedFilesContainer = view.findViewById(R.id.log_op_files_failed_container);
        log_retry_bar = view.findViewById(R.id.log_retry_bar);

        loadLogContent();
        if(requireActivity() instanceof MainActivity) {
            ((MainActivity)requireActivity()).setActionBarToggleBackButton();
        }

        return view;
    }
}