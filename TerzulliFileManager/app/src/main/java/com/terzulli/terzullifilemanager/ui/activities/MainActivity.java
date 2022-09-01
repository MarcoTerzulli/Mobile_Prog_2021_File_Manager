package com.terzulli.terzullifilemanager.ui.activities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.databinding.ActivityMainBinding;
import com.terzulli.terzullifilemanager.ui.fragments.AudioFragment;
import com.terzulli.terzullifilemanager.ui.fragments.DownloadFragment;
import com.terzulli.terzullifilemanager.ui.fragments.ImagesFragment;
import com.terzulli.terzullifilemanager.ui.fragments.MainFragment;
import com.terzulli.terzullifilemanager.ui.fragments.VideosFragment;
import com.terzulli.terzullifilemanager.ui.fragments.recents.RecentsFragment;

import java.io.File;
import java.util.List;

public class MainActivity extends PermissionsActivity
        implements PermissionsActivity.OnPermissionGranted {

    private AppBarConfiguration AppBarConfiguration;
    private ActivityMainBinding binding;
    private RecyclerView recyclerView;
    private List<File> fileList;
    private DrawerLayout drawer;
    //private Menu toolbarMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup layout
        setSupportActionBar(findViewById(R.id.toolbar_main));
        setupDrawer();
        //updateMenuItems();

        checkForSystemPermissions();
    }

    private void setupDrawer() {
        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        AppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_recents, R.id.nav_images, R.id.nav_videos, R.id.nav_audio, R.id.nav_download,
                R.id.nav_internal_storage, R.id.nav_sd_card, R.id.nav_external_storage)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.main_fragment_content);
        NavigationUI.setupActionBarWithNavController(this, navController, AppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    // metodo specifico per navHostFragment: restituisce il fragment corrente
    public Fragment getForegroundFragment(){
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

    /*
    public void invalidateFragment() {
        supportInvalidateOptionsMenu();
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //toolbarMenu = menu;

        //setupOptionsMenu();
        //updateMenuItems();

        return super.onCreateOptionsMenu(menu);
        //return true;
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

        Toast.makeText(MainActivity.this, currentFragment.getClass().getName(), Toast.LENGTH_SHORT).show();

        // TODO eventuale gestione view type grid o column
        Menu toolbarMenu = ((Toolbar) findViewById(R.id.toolbar_main)).getMenu();

        menu.findItem(R.id.menu_search).setVisible(true);

        // default
        toolbarMenu.findItem(R.id.menu_deselect_all).setVisible(false);
        toolbarMenu.findItem(R.id.menu_copy_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_move_to).setVisible(false);
        toolbarMenu.findItem(R.id.menu_compress).setVisible(false);
        toolbarMenu.findItem(R.id.menu_decompress).setVisible(false);
        menu.findItem(R.id.menu_sort_by).setVisible(true);
        menu.findItem(R.id.menu_select_all).setVisible(true);
        menu.findItem(R.id.menu_get_info).setVisible(true);

        if (currentFragment instanceof MainFragment) {

            Toast.makeText(MainActivity.this, "instance of main frag", Toast.LENGTH_SHORT).show();
            menu.findItem(R.id.menu_new_folder).setVisible(true);
        } else if (currentFragment instanceof RecentsFragment ||
                currentFragment instanceof AudioFragment ||
                currentFragment instanceof DownloadFragment ||
                currentFragment instanceof ImagesFragment ||
                currentFragment instanceof VideosFragment) {

            Toast.makeText(MainActivity.this, "instance of altri frag", Toast.LENGTH_SHORT).show();
            menu.findItem(R.id.menu_new_folder).setVisible(false);
        }

        // TODO gestione file nascosti
        menu.findItem(R.id.menu_show_hidden).setVisible(false);
        menu.findItem(R.id.menu_dont_show_hidden).setVisible(false);

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
        //getSupportFragmentManager().popBackStackImmediate();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPermissionGranted() {
        // TODO bisogna ricaricare il contenuto della home ora che abbiamo i permessi per lo storage

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