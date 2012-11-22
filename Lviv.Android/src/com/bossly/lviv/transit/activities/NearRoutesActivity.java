package com.bossly.lviv.transit.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.fragments.NearRoutesFragment;

public class NearRoutesActivity extends FragmentActivity
{  
  public static final String PREF_AGREEMENT_AGREE_V1 = "agreement_user_agree_v1.0";
  
  private NearRoutesFragment fragmentNear;
  
  private FragmentManager fragmentManager;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    
    setContentView( R.layout.main );
    
    fragmentManager = getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    
    fragmentNear = new NearRoutesFragment();
    
    transaction.add( R.id.container, fragmentNear );
    transaction.commit();
  }
  
  @Override
  protected void onResume()
  {
    super.onResume();
    
    fragmentNear.startDetermineUserLocation();
  }
  
  @Override
  protected void onPause()
  {
    super.onPause();
    
    fragmentNear.stopDetermineUserLocation();
  }
}