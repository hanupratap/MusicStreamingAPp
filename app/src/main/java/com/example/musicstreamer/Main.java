package com.example.musicstreamer;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;

import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.appcompat.app.AppCompatActivity;

import io.paperdb.Paper;

public class Main extends AppCompatActivity  {

    FragmentManager fragmentManager ;
    BottomNavigationView bottomNavigationView;
    public PlayerService mService;
    public boolean mBound = false;

    private boolean playWhenReady = false;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private boolean closeLogic = false;

    FragmentTransaction transaction;
    Fragment selectedFragment = null;



    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(true);
            initializePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(false);

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.main_fragment,new TrackList(), TrackList.class.toString());
        transaction.addToBackStack(TrackList.class.toString());
        transaction.commit();



        bottomNavigationView = findViewById(R.id.nav);



        selectedFragment = new TrackList();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fr = fragmentManager.findFragmentById(R.id.main_fragment);
                if(fr!=null){
                    Log.e("fragment=", fr.getClass().getSimpleName());
                    selectedFragment = fr;

                }
            }
        });

        App.player = new SimpleExoPlayer.Builder(this).setTrackSelector(new DefaultTrackSelector(this)).build();


        App.player.addListener(new com.google.android.exoplayer2.Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == com.google.android.exoplayer2.Player.STATE_READY) {
//                    Toast.makeText(Main.this, "Playing", Toast.LENGTH_SHORT).show();
                    // media actually playing
                    closeLogic = false;
                } else if (playWhenReady) {
                    // might be idle (plays after prepare()),
                    // buffering (plays when data available)
                    // or ended (plays when seek away from end)
//                    Toast.makeText(Main.this, "Paused", Toast.LENGTH_SHORT).show();
                    closeLogic = true;

                } else {
                    // player paused in any state
                    closeLogic = true;
                }
            }
        });


        bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(false);


        if(!App.firstStart)
        {
            if(App.current_track.id == null)
            {
                Paper.init(this);
                App.current_track = Paper.book().read("current_track");

                if(App.current_track.id != null)
                {
                    bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(true);
                }
                else
                {
                    bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(false);
                }

            }
            else
            {
                bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(true);
            }

        }





        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.explore:

//                        if (!selectedFragment.getClass().toString().equals(TrackList.class.toString())) {
//                            selectedFragment = new TrackList();
//                            getSupportFragmentManager().popBackStack(Player.class.toString(), 0);
//                            transaction = getSupportFragmentManager().beginTransaction();
//                            transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
//
//                            transaction.add(R.id.main_fragment, selectedFragment, TrackList.class.toString());
//                            transaction.addToBackStack(TrackList.class.toString());
//                            transaction.commit();
//                        }

                        displayFragmentTrackList();


                        return true;

                    case R.id.play:



                        Fragment temp_frag = getSupportFragmentManager().findFragmentByTag(Player.class.toString());
                        selectedFragment = new Player();
                        if(temp_frag == null)
                        {
                            transaction = getSupportFragmentManager().beginTransaction();
                            getSupportFragmentManager().popBackStack(Player.class.toString(),FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            transaction.add(R.id.main_fragment, selectedFragment, Player.class.toString());
                            transaction.addToBackStack(Player.class.toString());
                            transaction.commit();
                        }
                        else
                        {
                            displayFragmentPlayer();
                        }


                        return true;


                    case R.id.search:


//                        if (!selectedFragment.getClass().toString().equals(Search.class.toString()))
//                        {
//                            selectedFragment = new Search();
//                            getSupportFragmentManager().popBackStack(Player.class.toString(),0);
//                            transaction = getSupportFragmentManager().beginTransaction();
//                            transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
//                            transaction.add(R.id.main_fragment, selectedFragment, Search.class.toString());
//                            transaction.addToBackStack(Search.class.toString());
//                            transaction.commit();
//                        }

                        displayFragmentSearch();



                        return true;

                }
                return false;

            }

        });
    }



    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


    void initializePlayer() {

        if (mBound) {
            App.player = mService.getplayerInstance();
            App.playerControlView.setPlayer(App.player);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed()
    {




            String a = selectedFragment.getClass().toString();
            String b = Player.class.toString();
            if(a.equals(b))
            {
                displayFragmentTrackList();
                bottomNavigationView.getMenu().findItem(R.id.explore).setChecked(true);
            }
            else
            {
                if(mBound==false)
                {
                    finishAffinity();
                }
                else {
                    if(closeLogic)
                    {
                        releasePlayer();
                        finish();
                    }
                    else
                    {
                        minimizeApp();
                    }

                }
            }





    }



    @Override
    protected void onDestroy() {

        if(App.current_track!=null)
        {
            Paper.book().write("current_track", App.current_track);

        }
        if(mBound)
        {
            this.unbindService(mConnection);
            mService.stopSelf();
        }

        mBound = false;

            releasePlayer();


        super.onDestroy();

    }




    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//            releasePlayer();

    }

    public void signOut()
    {
        FirebaseAuth.getInstance().signOut();
        restartApp();
    }


    private void restartApp() {

        finish();
        Intent intent = new Intent(this, Dispatcher.class);
        intent .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    protected void releasePlayer() {


        if (App.player != null) {

            playWhenReady = App.player.getPlayWhenReady();
            playbackPosition = App.player.getCurrentPosition();
            currentWindow = App.player.getCurrentWindowIndex();
            App.player.release();
            App.player = null;
        }
    }


    protected void displayFragmentTrackList() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.hide(selectedFragment);
        Fragment fragmentA = getSupportFragmentManager().findFragmentByTag(TrackList.class.toString());
        Fragment fragmentB = getSupportFragmentManager().findFragmentByTag(Player.class.toString());
        Fragment fragmentC = getSupportFragmentManager().findFragmentByTag(Search.class.toString());
        Fragment fragmentD = getSupportFragmentManager().findFragmentByTag(SearchResults.class.toString());
        if (fragmentA!=null && fragmentA.isAdded()) { // if the fragment is already in container
            selectedFragment = fragmentA;
            ft.show(fragmentA);
        } else { // fragment needs to be added to frame container
            ft.add(R.id.main_fragment, new TrackList(), TrackList.class.toString());
            ft.addToBackStack(TrackList.class.toString());
            selectedFragment = fragmentA;
        }
        // Hide fragment B
        if (fragmentB!=null && fragmentB.isAdded()) { ft.hide(fragmentB); }
        // Hide fragment C
        if (fragmentC!=null && fragmentC.isAdded()) { ft.hide(fragmentC); }

        //Hide fragment D
        if(fragmentD!=null)
        {
            if (fragmentD.isAdded()) { ft.hide(fragmentD); }
        }
        //Hide Fragment D
        if (fragmentD!=null && fragmentD.isAdded()) { ft.hide(fragmentD); }


        // Commit changes
        ft.commit();
    }



    protected void displayFragmentPlayer() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(selectedFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        Fragment fragmentA = getSupportFragmentManager().findFragmentByTag(Player.class.toString());
        Fragment fragmentB = getSupportFragmentManager().findFragmentByTag(Search.class.toString());
        Fragment fragmentC = getSupportFragmentManager().findFragmentByTag(TrackList.class.toString());
        Fragment fragmentD = getSupportFragmentManager().findFragmentByTag(SearchResults.class.toString());
        if (fragmentA!=null && fragmentA.isAdded()) { // if the fragment is already in container
            ft.show(fragmentA);
            selectedFragment = fragmentA;
        } else { // fragment needs to be added to frame container
            selectedFragment = fragmentA;
            ft.add(R.id.main_fragment, new Search(), Search.class.toString());
            ft.addToBackStack(Search.class.toString());
        }
        // Hide fragment B
        if (fragmentB!=null && fragmentB.isAdded()) { ft.hide(fragmentB); }
        // Hide fragment C
        if (fragmentC!=null && fragmentC.isAdded()) { ft.hide(fragmentC); }
        //Hide Fragment D
        if (fragmentD!=null && fragmentD.isAdded()) { ft.hide(fragmentD); }
        // Commit changes
        ft.commit();
    }

    protected void displayFragmentSearch() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(selectedFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        Fragment fragmentA = getSupportFragmentManager().findFragmentByTag(Search.class.toString());
        Fragment fragmentB = getSupportFragmentManager().findFragmentByTag(Player.class.toString());
        Fragment fragmentC = getSupportFragmentManager().findFragmentByTag(TrackList.class.toString());
        Fragment fragmentD = getSupportFragmentManager().findFragmentByTag(SearchResults.class.toString());
        if (fragmentA!=null && fragmentA.isAdded()) { // if the fragment is already in container
            ft.show(fragmentA);
            selectedFragment = fragmentA;
        } else { // fragment needs to be added to frame container
            ft.add(R.id.main_fragment, new Search(), Search.class.toString());
            ft.addToBackStack(Search.class.toString());
            selectedFragment = fragmentA;
        }
        // Hide fragment B
        if (fragmentB!=null && fragmentB.isAdded()) { ft.hide(fragmentB); }
        // Hide fragment C
        if (fragmentC!=null && fragmentC.isAdded()) { ft.hide(fragmentC); }
        //Hide Fragment D
        if (fragmentD!=null && fragmentD.isAdded()) { ft.hide(fragmentD); }
        // Commit changes
        ft.commit();
    }

}





