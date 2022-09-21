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

    private AppBarConfiguration AppBarConfiguration;
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private SearchView searchView;
    private Toolbar toolbar;
    private long timeBackPressed;
    private static final int backPressedInterval = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup layout
        setSupportActionBar(findViewById(R.id.main_toolbar));
        setupUiItems();

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

    // TODO
    private void setupSearchView() {

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
    public Fragment getForegroundFragment() {
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_content);
        return navHostFragment == null ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
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
                Toast.makeText(MainActivity.this, "Menu new folder", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_move_to:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_compress:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_decompress:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_get_info:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_show_hidden:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_dont_show_hidden:
                Toast.makeText(MainActivity.this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                break;
            default:
                // non dovremmo mai arrivarci
                return false;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_content);
        Fragment currentFragment = getForegroundFragment();

        // TODO eventuale gestione view type grid o column
        Menu toolbarMenu = ((Toolbar) findViewById(R.id.main_toolbar)).getMenu();

        menu.findItem(R.id.menu_search).setVisible(true);

        // default
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(false);
        toolbarMenu.findItem(R.id.menu_copy_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_move_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_compress).setVisible(false);
        toolbarMenu.findItem(R.id.menu_decompress).setVisible(false);
        menu.findItem(R.id.menu_sort_by).setVisible(true);
        menu.findItem(R.id.menu_open_with).setVisible(false);
        menu.findItem(R.id.menu_select_all).setVisible(true);
        menu.findItem(R.id.menu_get_info).setVisible(true);

        if (currentFragment instanceof MainFragment) {
            menu.findItem(R.id.menu_new_folder).setVisible(true);

        } else if (currentFragment instanceof RecentsFragment ||
                currentFragment instanceof AudioFragment ||
                currentFragment instanceof DownloadFragment ||
                currentFragment instanceof ImagesFragment ||
                currentFragment instanceof VideosFragment) {

            menu.findItem(R.id.menu_new_folder).setVisible(false);
        }

        // TODO gestione file nascosti
        menu.findItem(R.id.menu_show_hidden).setVisible(false);
        menu.findItem(R.id.menu_dont_show_hidden).setVisible(false);

        // setup search view
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        setupSearchView();

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
            if (!MainFragment.isInHomePath()) {
                // se non siamo nella home, la gestione è quella classica nel tornare indietro nelle directory
                MainFragment.loadPath(MainFragment.getParentPath());
            } else {
                if (timeBackPressed + backPressedInterval > System.currentTimeMillis())
                    finish();
                else {
                    timeBackPressed = System.currentTimeMillis();
                    Toast.makeText(MainActivity.this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPermissionGranted() {
        // TODO bisogna ricaricare il contenuto della home ora che abbiamo i permessi per lo storage
        Fragment currentFragment = getForegroundFragment();

        if (currentFragment != null && currentFragment instanceof MainFragment) {
            return;
        }

        MainFragment.updateList();

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