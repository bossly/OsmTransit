package com.bossly.lviv.transit.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.fragments.RoutesFragment;

public class RoutesActivity extends FragmentActivity
{
  
  private RoutesFragment fragmentAll;
  
  private FragmentManager fragmentManager;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    
    super.onCreate( savedInstanceState );
    
    setContentView( R.layout.main );
    
    fragmentManager = getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    
    fragmentAll = new RoutesFragment();
    
    transaction.add( R.id.container, fragmentAll );
    transaction.commit();
  }
}