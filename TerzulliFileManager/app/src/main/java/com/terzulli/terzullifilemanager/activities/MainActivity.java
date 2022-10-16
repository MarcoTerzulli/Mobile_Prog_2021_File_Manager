package com.terzulli.terzullifilemanager.activities;

import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.saveCurrentFilesBeforeQuerySubmit;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.submitSearchQuery;
import static com.terzulli.terzullifilemanager.fragments.MainFragment.isACustomLocationDisplayed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.adapters.ItemsAdapter;
import com.terzulli.terzullifilemanager.databinding.ActivityMainBinding;
import com.terzulli.terzullifilemanager.fragments.MainFragment;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.util.Objects;

public class MainActivity extends PermissionsActivity
        implements PermissionsActivity.OnPermissionGranted {

    @SuppressLint("StaticFieldLeak")
    private static SearchView searchView;
    private static Menu toolbarMenu;
    private static Fragment navHostFragment;
    private static int menuActualCase;
    private static SharedPreferences sharedPreferences;
    private static DrawerLayout drawer;
    private static ActionBarDrawerToggle actionBarDrawerToggle;
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;
    private AppBarConfiguration AppBarConfiguration;
    private ActivityMainBinding binding;
    private Toolbar toolbar;

    private static void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                submitSearchQuery(newText);

                return false;
            }
        });

        // gestione bottone back della searchview
        final MenuItem itemSearch = toolbarMenu.findItem(R.id.menu_search);
        itemSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // non si fa nulla quando il menù di ricerca si apre
                saveCurrentFilesBeforeQuerySubmit();

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //clearSelectionAndActiveOperations();

                updateMenuItems(menuActualCase);

                new Handler().postDelayed(MainFragment::refreshList, 10);

                return true;
            }
        });

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

    public static void closeSearchView() {
        if (searchView != null && !searchView.isIconified()) {
            final MenuItem itemSearch = toolbarMenu.findItem(R.id.menu_search);
            MenuItemCompat.collapseActionView(itemSearch);
            //searchView.setIconified(true);
        }
    }

    public static boolean isSearchActive() {
        if (searchView != null) {
            return !searchView.isIconified();
        }
        return false;
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
     * - 11: selezione completa (generica) ma c'è un solo file
     * - 12: selezione completa (generica) ma c'è una sola cartella
     * - default: cartella generica
     *
     * @param menuCase casistica scelta
     */
    /* vecchio

     * - 8: selezione generica dentro zip
     * - 9: selezione completa dentro zip
     * - 10: nessuna selezione attiva, ma la cartella corrente è uno zip
     */
    public static void updateMenuItems(int menuCase) {

        if (toolbarMenu == null || toolbarMenu.findItem(R.id.menu_search) == null)
            return;

        menuActualCase = menuCase;

        switch (menuActualCase) {
            case 1:
                // 1 file selezionato
                setMenuItemsOneFileSelected();
                break;
            case 2:
                // 1 directory selezionata
                setMenuItemsOneDirectorySelected();
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
            /*case 8:
                // selezione dentro zip
                setMenuItemsOneSelectedInsideZip();
                break;
            case 9:
                // selezione completa dentro zip
                setMenuItemsAllSelectedInsideZip();
                break;
            case 10:
                // nessuna seleazione ma siamo dentro uno zip
                setMenuItemsZip();
                break;*/
            case 11:
                //  selezione completa (generica) ma c'è un solo file
                setMenuItemsAllSelectedOneFile();
                break;
            case 12:
                // - 12: selezione completa (generica) ma c'è una sola cartella
                setMenuItemsAllSelectedOneDirectory();
                break;
            default:
                setMenuItemsDefault();
                break;
        }

        // nasconde alcune voci all'interno delle pagine "Images", "Recents", "Audio" e "Videos"
        if(isACustomLocationDisplayed()) {
            toolbarMenu.findItem(R.id.menu_new_directory).setVisible(false);
            toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(false);
            toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(false);
        }

        // nasconde alcune voci del menù se la searchview è attiva
        if(!searchView.isIconified()) {
            toolbarMenu.findItem(R.id.menu_new_directory).setVisible(false);
            toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(false);
            toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(false);
        }
    }

    /*private static void setMenuItemsOneSelectedInsideZip() {
        toolbarMenu.findItem(R.id.menu_sort_by).setVisible(true);
        toolbarMenu.findItem(R.id.menu_select_all).setVisible(true);
        toolbarMenu.findItem(R.id.menu_extract).setVisible(true);
        toolbarMenu.findItem(R.id.menu_get_info).setVisible(true);

        // nascondo il resto
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(false);
        //toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(!sharedPreferences.getBoolean("showHidden", false));
        //toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(sharedPreferences.getBoolean("showHidden", false));
        toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_new_directory).setVisible(false);
        toolbarMenu.findItem(R.id.menu_open_with).setVisible(true);
        toolbarMenu.findItem(R.id.menu_share).setVisible(false);
        toolbarMenu.findItem(R.id.menu_delete).setVisible(false);
        toolbarMenu.findItem(R.id.menu_search).setVisible(false);
        toolbarMenu.findItem(R.id.menu_copy_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_move_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_compress).setVisible(false);
        toolbarMenu.findItem(R.id.menu_rename).setVisible(false);
    }*/

    /*private static void setMenuItemsAllSelectedInsideZip() {
        setMenuItemsOneSelectedInsideZip();

        toolbarMenu.findItem(R.id.menu_select_all).setVisible(false);
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(true);
    }

    private static void setMenuItemsZip() {
        setMenuItemsDefault();
        toolbarMenu.findItem(R.id.menu_new_directory).setVisible(false);
    }*/

    private static void setMenuItemsOneFileSelected() {
        toolbarMenu.findItem(R.id.menu_open_with).setVisible(true);
        toolbarMenu.findItem(R.id.menu_sort_by).setVisible(true);
        toolbarMenu.findItem(R.id.menu_select_all).setVisible(true);
        toolbarMenu.findItem(R.id.menu_copy_to).setVisible(true);
        toolbarMenu.findItem(R.id.menu_move_to).setVisible(true);
        toolbarMenu.findItem(R.id.menu_compress).setVisible(true);
        toolbarMenu.findItem(R.id.menu_rename).setVisible(true);
        toolbarMenu.findItem(R.id.menu_get_info).setVisible(true);

        toolbarMenu.findItem(R.id.menu_share).setVisible(true);
        toolbarMenu.findItem(R.id.menu_delete).setVisible(true);
        toolbarMenu.findItem(R.id.menu_search).setVisible(false);

        // nascondo il resto
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(false);
        //toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(!sharedPreferences.getBoolean("showHidden", false));
        //toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(sharedPreferences.getBoolean("showHidden", false));
        toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(false);
        toolbarMenu.findItem(R.id.menu_new_directory).setVisible(false);
        toolbarMenu.findItem(R.id.menu_extract).setVisible(false);
    }

    private static void setMenuItemsOneDirectorySelected() {
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

    private static void setMenuItemsAllSelectedOneFile() {
        // sono sostanzialmente gli stessi...
        setMenuItemsOneFileSelected();

        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(true);
        toolbarMenu.findItem(R.id.menu_select_all).setVisible(false);
    }

    private static void setMenuItemsAllSelectedOneDirectory() {
        // sono sostanzialmente gli stessi...
        setMenuItemsOneDirectorySelected();

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
            toolbarMenu.findItem(R.id.menu_new_directory).setVisible(true);

        } /*else if (currentFragment instanceof RecentsFragment ||
                currentFragment instanceof AudioFragment ||
                currentFragment instanceof DownloadFragment ||
                currentFragment instanceof ImagesFragment ||
                currentFragment instanceof VideosFragment) {

            toolbarMenu.findItem(R.id.menu_new_directory).setVisible(false);
        }*/

        toolbarMenu.findItem(R.id.menu_show_hidden).setVisible(!sharedPreferences.getBoolean("showHidden", false));
        toolbarMenu.findItem(R.id.menu_dont_show_hidden).setVisible(sharedPreferences.getBoolean("showHidden", false));

        // setup search view
        searchView = (SearchView) toolbarMenu.findItem(R.id.menu_search).getActionView();
        setupSearchView();
    }

    public static void setActionBarToggleDefault() {
        Drawable drawerIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_menu, activity.getTheme());

        actionBarDrawerToggle.setHomeAsUpIndicator(drawerIcon);

        actionBarDrawerToggle.setToolbarNavigationClickListener(v -> {
            if (drawer.isDrawerVisible(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        drawer.addDrawerListener(actionBarDrawerToggle);
    }

    public static void setActionBarToggleCloseButton() {
        Drawable drawerIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_menu_close, activity.getTheme());

        actionBarDrawerToggle.setHomeAsUpIndicator(drawerIcon);

        actionBarDrawerToggle.setToolbarNavigationClickListener(v -> {
            if (getForegroundFragment() != null && getForegroundFragment() instanceof MainFragment) {
                if (MainFragment.goBack())
                    activity.finish();
                else
                    setActionBarToggleDefault();
            }
        });

        drawer.addDrawerListener(actionBarDrawerToggle);
    }

    public static void setActionBarToggleBackButton() {
        Drawable drawerIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_menu_arrow_back, activity.getTheme());

        actionBarDrawerToggle.setHomeAsUpIndicator(drawerIcon);

        actionBarDrawerToggle.setToolbarNavigationClickListener(v -> {
            if (getForegroundFragment() != null && getForegroundFragment() instanceof MainFragment) {
                if (MainFragment.goBack())
                    activity.finish();
                else
                    setActionBarToggleDefault();
            }
        });

        drawer.addDrawerListener(actionBarDrawerToggle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = this;

        sharedPreferences = getSharedPreferences("TerzulliFileManager", MODE_PRIVATE);
        initializePreferences();

        // setup layout
        setSupportActionBar(findViewById(R.id.main_toolbar));
        toolbarMenu = ((Toolbar) findViewById(R.id.main_toolbar)).getMenu();
        navHostFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_content);
        setupUiItems();
        menuActualCase = 0;

        initializeActionBarDrawerToggle();
        setActionBarToggleDefault();

        checkForSystemPermissions();
    }

    private void initializePreferences() {
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();

        if(!sharedPreferences.contains("showHidden"))
            sharedPrefEditor.putBoolean("showHidden", false);

        if(!sharedPreferences.contains("sortBy"))
            sharedPrefEditor.putString("sortBy", Utils.strSortByName);

        if(!sharedPreferences.contains("sortOrderAscending"))
            sharedPrefEditor.putBoolean("sortOrderAscending", true);

        sharedPrefEditor.apply();
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
        initializeDrawerDestionations(navController);

        // search view
        toolbar = findViewById(R.id.main_toolbar);

    }

    /*public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragmentsList = fragmentManager.getFragments();

        for (Fragment fragment : fragmentsList) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //setupSearchView();

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO eventuale controllo fragment ?

        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();

        switch (item.getItemId()) {
            /*case R.id.menu_search:
                // TODO
                break;*/
            case R.id.menu_new_directory:
                ItemsAdapter.createNewDirectory();
                break;
            case R.id.menu_open_with:
                ItemsAdapter.openWithSelectedFile();
                break;
            case R.id.menu_sort_by:
                MainFragment.displaySortByDialog();
                break;
            case R.id.menu_select_all:
                ItemsAdapter.selectAll();
                break;
            case R.id.menu_deselect_all:
                ItemsAdapter.deselectAll();
                break;
            case R.id.menu_copy_to:
                closeSearchView();
                ItemsAdapter.copyMoveSelection(true);
                break;
            case R.id.menu_move_to:
                ItemsAdapter.copyMoveSelection(false);
                break;
            case R.id.menu_compress:
                ItemsAdapter.compressSelection();
                break;
            /*case R.id.menu_decompress:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;*/
            case R.id.menu_get_info:
                ItemsAdapter.infoSelectedFile();
                break;
            case R.id.menu_show_hidden:
                sharedPrefEditor.putBoolean("showHidden", true);
                sharedPrefEditor.apply();
                MainFragment.refreshList();
                break;
            case R.id.menu_dont_show_hidden:
                sharedPrefEditor.putBoolean("showHidden", false);
                sharedPrefEditor.apply();
                MainFragment.refreshList();
                break;
            case R.id.menu_rename:
                ItemsAdapter.renameSelectedFile();
                break;
            case R.id.menu_delete:
                ItemsAdapter.deleteSelectedFiles();
                break;
            case R.id.menu_share:
                ItemsAdapter.shareSelectedFiles();
                break;
            default:
                // non dovremmo mai arrivarci
                return false;
        }

        /* R.id.nav_recents, R.id.nav_images, R.id.nav_videos, R.id.nav_audio, R.id.nav_download,
                R.id.nav_internal_storage, R.id.nav_sd_card, R.id.nav_external_storage */

        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //setMenuItemsDefault();
        if (toolbarMenu == null) {
            toolbarMenu = ((Toolbar) findViewById(R.id.main_toolbar)).getMenu();
        }

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
        Fragment currentFragment = getForegroundFragment();

        // ricarico il contenuto ora che ho i permessi per lo storage
        if (currentFragment instanceof MainFragment) {
            MainFragment.refreshList();
        }


    }

    public void checkForSystemPermissions() {
        if (!checkStoragePermission()) {
            requestStoragePermission(true, this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestAllFilesAccess(this);
        }
    }

    private void initializeActionBarDrawerToggle() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view)
            {
                //getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                //setActionBar().setTitle("Settings");
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };

        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    @SuppressLint("NonConstantResourceId")
    private void initializeDrawerDestionations(NavController navController) {

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {


        /* R.id.nav_recents, R.id.nav_images, R.id.nav_videos, R.id.nav_audio, R.id.nav_download,
                R.id.nav_internal_storage, R.id.nav_sd_card, R.id.nav_external_storage */


            switch (destination.getId()) {
                case R.id.nav_recents:
                    MainFragment.displayRecentsFiles();
                    break;
                case R.id.nav_images:
                    MainFragment.displayImagesFiles();
                    break;
                case R.id.nav_videos:
                    MainFragment.displayVideosFiles();
                    break;
                case R.id.nav_audio:
                    MainFragment.displayAudioFiles();
                    break;
                case R.id.nav_download:
                    MainFragment.loadPathDownload(true);
                    break;
                case R.id.nav_internal_storage:
                    MainFragment.loadPathInternal(true);
                    break;
                case R.id.nav_sd_card:
                    /*
                    L'accesso alla sd card non è stato implementato per ragioni tempistiche.
                    Ho scelto di non rimuovere la struttura sottostante, che ho disabilitato
                    (nascondendo gli elementi corrispondendit)
                    */
                    break;
                case R.id.nav_external_storage:
                    /*
                    L'accesso allo storage esterno non è stato implementato per ragioni tempistiche.
                    Ho scelto di non rimuovere la struttura sottostante, che ho disabilitato
                    (nascondendo gli elementi corrispondendit)
                    */
                    break;
                default:
                    break;
            }

            /*if(destination.getId() == R.id.nav_recents) {
                toolbar.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.VISIBLE);
            }*/
        });
    }


}