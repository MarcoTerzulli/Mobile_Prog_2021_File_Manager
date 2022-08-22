package com.terzulli.terzullifilemanager.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.databinding.ActivityMainBinding;
import com.terzulli.terzullifilemanager.ui.fragments.MainFragment;
import com.terzulli.terzullifilemanager.ui.fragments.recents.RecentsFragment;

public class MainActivity extends PermissionsActivity
        implements PermissionsActivity.OnPermissionGranted {

    private AppBarConfiguration AppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(findViewById(R.id.toolbar_main));
        /*binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_recents, R.id.nav_images, R.id.nav_videos, R.id.nav_audio, R.id.nav_download,
                R.id.nav_internal_storage, R.id.nav_sd_card, R.id.nav_external_storage)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, AppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        //navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        /*NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        NavigationUI.setupWithNavController(binding.navView, navController);*/

        checkForSystemPermissions();
    }

    /*@Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //displayFragment(item.getItemId());
        return true;
    }

    private void displayFragment(int framentId) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (framentId) {
            case R.id.nav_recents:
                fragment = new RecentsFragment();
                title  = "Recents";
                break;
            case R.id.nav_images:
                fragment = new RecentsFragment();
                title  = "Recents";
                break;
            case R.id.nav_videos:
                fragment = new RecentsFragment();
                title  = "Recents";
                break;
            case R.id.nav_download:
                fragment = new RecentsFragment();
                title  = "Recents";
                break;
            case R.id.nav_internal_storage:
                fragment = new MainFragment();
                title  = "Internal Storage";
                break;
            case R.id.nav_sd_card:
                fragment = new MainFragment();
                title  = "SD Card";
                break;
            case R.id.nav_external_storage:
                fragment = new MainFragment();
                title = "USB Storage";
                break;

        }

        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_main, fragment);
            fragmentTransaction.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // menu per scheda recenti
        menu.findItem(R.id.new_folder).setVisible(false);
        menu.findItem(R.id.sort_by).setVisible(true);
        menu.findItem(R.id.select_all).setVisible(true);
        menu.findItem(R.id.deselect_all).setVisible(false);
        menu.findItem(R.id.copy_to).setVisible(false);
        menu.findItem(R.id.move_to).setVisible(false);
        menu.findItem(R.id.compress).setVisible(false);
        menu.findItem(R.id.decompress).setVisible(false);
        menu.findItem(R.id.get_info).setVisible(true);
        menu.findItem(R.id.show_hidden).setVisible(true);
        menu.findItem(R.id.dont_show_hidden).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

}