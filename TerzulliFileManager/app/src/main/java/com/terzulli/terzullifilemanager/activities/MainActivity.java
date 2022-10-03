package com.terzulli.terzullifilemanager.activities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.adapters.ItemsAdapter;
import com.terzulli.terzullifilemanager.databinding.ActivityMainBinding;
import com.terzulli.terzullifilemanager.fragments.AudioFragment;
import com.terzulli.terzullifilemanager.fragments.DownloadFragment;
import com.terzulli.terzullifilemanager.fragments.ImagesFragment;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.fragments.RecentsFragment;
import com.terzulli.terzullifilemanager.fragments.VideosFragment;

import java.util.List;

public class MainActivity extends PermissionsActivity
        implements PermissionsActivity.OnPermissionGranted {

    private static SearchView searchView;
    private static Menu toolbarMenu;
    private static Fragment navHostFragment;
    private static int menuActualCase;
    private AppBarConfiguration AppBarConfiguration;
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    // TODO
    private static void setupSearchView() {

        /*searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    Toast.makeText(MainActivity.this, "back", Toast.LENGTH_SHORT).show();
                    searchView.setIconified(true);
                }
            }
        });*/
    }

    // metodo specifico per navHostFragment: restituisce il fragment corrente
    public static Fragment getForegroundFragment() {
        return navHostFragment == null ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
    }

    /**
     * Funzione per l'aggiornamento degli elementi mostrati nel menu in base al valore del parametro
     * menuCase.
     * <p>
     * Casistiche supportate (valore menuCase):
     * - 1: un file selezionato
     * - 2: una directory selezionata
     * - 3: molteplici file selezionati
     * - 4 e 5: molteplici file o directory selezionati
     * - 6: selezione completa (generica)
     * - 7: selezione completa ma di soli file
     * - 8: selezione generica dentro zip
     * - 9: selezione completa dentro zip
     * - 10: nessuna selezione attiva, ma la cartella corrente è uno zip
     * - default: cartella generica
     *
     * @param menuCase casistica scelta
     */
    public static void updateMenuItems(int menuCase) {
        menuActualCase = menuCase;

        switch (menuActualCase) {
            case 1:
                // 1 file selezionato
                setMenuItemsOneFileSelected();
                break;
            case 2:
                // 1 directory selezionata
                setMenuItemsOneFolderSelected();
                break;
            case 3:
                // molteplici file selezionati
                setMenuItemsMultipleFileSelected();
                break;
            case 4:
            case 5:
                // molteplici file / directory selezionate
                setMenuItemsMultipleGenericSelected();
                break;
            case 6:
                // selezione completa
                setMenuItemsAllSelected();
                break;
            case 7:
                // selezione completa ma di soli file
                setMenuItemsAllSelectedOnlyFiles();
                break;
            case 8:
                // selezione dentro zip
                setMenuItemsOneSelectedInsideZip();
                break;
            case 9:
                // selezione dentro zip
                setMenuItemsAllSelectedInsideZip();
                break;
            case 10:
                // nessuna seleazione ma siamo dentro uno zip
                setMenuItemsZip();
                break;
            default:
                setMenuItemsDefault();
                break;
        }
    }

    private static void setMenuItemsOneSelectedInsideZip() {
        toolbarMenu.findItem(R.id.menu_sort_by).setVisible(true);
        toolbarMenu.findItem(R.id.menu_select_all).setVisible(true);
        toolbarMenu.findItem(R.id.menu_extract).setVisible(true);
        toolbarMenu.findItem(R.id.menu_get_info).setVisible(true);

        // nascondo il resto
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(false);
        toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_new_folder).setVisible(false);
        toolbarMenu.findItem(R.id.menu_open_with).setVisible(true);
        toolbarMenu.findItem(R.id.menu_share).setVisible(false);
        toolbarMenu.findItem(R.id.menu_delete).setVisible(false);
        toolbarMenu.findItem(R.id.menu_search).setVisible(false);
        toolbarMenu.findItem(R.id.menu_copy_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_move_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_compress).setVisible(false);
        toolbarMenu.findItem(R.id.menu_rename).setVisible(false);

    }

    private static void setMenuItemsAllSelectedInsideZip() {
        setMenuItemsOneSelectedInsideZip();

        toolbarMenu.findItem(R.id.menu_select_all).setVisible(false);
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(true);
    }

    private static void setMenuItemsZip() {
        setMenuItemsDefault();
        toolbarMenu.findItem(R.id.menu_new_folder).setVisible(false);
    }

    private static void setMenuItemsOneFileSelected() {
        toolbarMenu.findItem(R.id.menu_open_with).setVisible(true);
        toolbarMenu.findItem(R.id.menu_sort_by).setVisible(true);
        toolbarMenu.findItem(R.id.menu_select_all).setVisible(true);
        toolbarMenu.findItem(R.id.menu_copy_to).setVisible(true);
        toolbarMenu.findItem(R.id.menu_move_to).setVisible(true);
        toolbarMenu.findItem(R.id.menu_compress).setVisible(true);
        toolbarMenu.findItem(R.id.menu_rename).setVisible(true);
        toolbarMenu.findItem(R.id.menu_get_info).setVisible(true);

        // todo mostrare cestino e bottone condividi
        toolbarMenu.findItem(R.id.menu_share).setVisible(true);
        toolbarMenu.findItem(R.id.menu_delete).setVisible(true);
        toolbarMenu.findItem(R.id.menu_search).setVisible(false);

        // nascondo il resto
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(false);
        toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_new_folder).setVisible(false);
        toolbarMenu.findItem(R.id.menu_extract).setVisible(false);
    }

    private static void setMenuItemsOneFolderSelected() {
        // sono sostanzialmente gli stessi...
        setMenuItemsOneFileSelected();

        toolbarMenu.findItem(R.id.menu_open_with).setVisible(false);
        toolbarMenu.findItem(R.id.menu_share).setVisible(false);
    }

    private static void setMenuItemsMultipleGenericSelected() {
        // sono sostanzialmente gli stessi...
        setMenuItemsOneFileSelected();

        toolbarMenu.findItem(R.id.menu_share).setVisible(false);
        toolbarMenu.findItem(R.id.menu_open_with).setVisible(false);
        toolbarMenu.findItem(R.id.menu_rename).setVisible(false);
        toolbarMenu.findItem(R.id.menu_get_info).setVisible(false);
    }

    private static void setMenuItemsMultipleFileSelected() {
        // sono sostanzialmente gli stessi...
        setMenuItemsMultipleGenericSelected();
        toolbarMenu.findItem(R.id.menu_share).setVisible(true);
    }

    private static void setMenuItemsAllSelected() {
        // sono sostanzialmente gli stessi...
        setMenuItemsMultipleGenericSelected();

        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(true);
        toolbarMenu.findItem(R.id.menu_select_all).setVisible(false);
    }

    private static void setMenuItemsAllSelectedOnlyFiles() {
        // sono sostanzialmente gli stessi...
        setMenuItemsAllSelected();
        toolbarMenu.findItem(R.id.menu_share).setVisible(true);

    }

    private static void setMenuItemsDefault() {
        Fragment currentFragment = getForegroundFragment();

        // TODO eventuale gestione view type grid o column

        toolbarMenu.findItem(R.id.menu_search).setVisible(true);

        // default
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(false);
        toolbarMenu.findItem(R.id.menu_copy_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_move_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_compress).setVisible(false);
        //toolbarMenu.findItem(R.id.menu_decompress).setVisible(false);
        toolbarMenu.findItem(R.id.menu_rename).setVisible(false);
        toolbarMenu.findItem(R.id.menu_share).setVisible(false);
        toolbarMenu.findItem(R.id.menu_delete).setVisible(false);
        toolbarMenu.findItem(R.id.menu_sort_by).setVisible(true);
        toolbarMenu.findItem(R.id.menu_open_with).setVisible(false);
        toolbarMenu.findItem(R.id.menu_select_all).setVisible(true);
        toolbarMenu.findItem(R.id.menu_extract).setVisible(false);
        toolbarMenu.findItem(R.id.menu_get_info).setVisible(true);

        if (currentFragment instanceof MainFragment) {
            toolbarMenu.findItem(R.id.menu_new_folder).setVisible(true);

        } else if (currentFragment instanceof RecentsFragment ||
                currentFragment instanceof AudioFragment ||
                currentFragment instanceof DownloadFragment ||
                currentFragment instanceof ImagesFragment ||
                currentFragment instanceof VideosFragment) {

            toolbarMenu.findItem(R.id.menu_new_folder).setVisible(false);
        }

        // TODO gestione file nascosti
        toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(true);
        toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(false);

        // setup search view
        searchView = (SearchView) toolbarMenu.findItem(R.id.menu_search).getActionView();
        setupSearchView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup layout
        setSupportActionBar(findViewById(R.id.main_toolbar));
        toolbarMenu = ((Toolbar) findViewById(R.id.main_toolbar)).getMenu();
        navHostFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_content);
        setupUiItems();
        menuActualCase = 0;

        checkForSystemPermissions();
    }

    private void setupUiItems() {
        // drawer
        drawer = binding.drawerLayout;
        AppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_recents, R.id.nav_images, R.id.nav_videos, R.id.nav_audio, R.id.nav_download,
                R.id.nav_internal_storage, R.id.nav_sd_card, R.id.nav_external_storage)
                .setOpenableLayout(drawer)
                .build();

        // nativation view
        NavigationView navigationView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.main_fragment_content);
        NavigationUI.setupActionBarWithNavController(this, navController, AppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // search view
        toolbar = findViewById(R.id.main_toolbar);

    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragmentsList = fragmentManager.getFragments();

        for (Fragment fragment : fragmentsList) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO eventuale controllo fragment ?

        // TODO implementazione azioni
        switch (item.getItemId()) {
            case R.id.menu_search:
                Toast.makeText(MainActivity.this, "Menu search", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_new_folder:
                ItemsAdapter.createNewDirectory();
                break;
            case R.id.menu_open_with:
                Toast.makeText(MainActivity.this, "Menu open with", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_sort_by:
                Toast.makeText(MainActivity.this, "Menu sort by", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_select_all:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_deselect_all:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_copy_to:
                ItemsAdapter.copyMoveSelection(true);
                break;
            case R.id.menu_move_to:
                ItemsAdapter.copyMoveSelection(false);
                break;
            case R.id.menu_compress:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            /*case R.id.menu_decompress:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;*/
            case R.id.menu_get_info:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_show_hidden:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_dont_show_hidden:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_rename:
                ItemsAdapter.renameSelectedFile();
                break;
            case R.id.menu_delete:
                ItemsAdapter.deleteSelectedFiles();
                break;
            default:
                // non dovremmo mai arrivarci
                return false;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //setMenuItemsDefault();
        updateMenuItems(menuActualCase);

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.main_fragment_content);
        return NavigationUI.navigateUp(navController, AppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getForegroundFragment() != null && getForegroundFragment() instanceof MainFragment) {
            if (MainFragment.goBack())
                finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPermissionGranted() {
        // TODO bisogna ricaricare il contenuto della home ora che abbiamo i permessi per lo storage
        Fragment currentFragment = getForegroundFragment();

        if (currentFragment instanceof MainFragment) {
            return;
        }

        MainFragment.refreshList();

    }

    public void checkForSystemPermissions() {
        if (!checkStoragePermission()) {
            requestStoragePermission(true, this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestAllFilesAccess(this);
        }
    }


}