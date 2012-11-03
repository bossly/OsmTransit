package com.bossly.lviv.transit;

import java.util.List;

import android.app.Application;

public class CoreApplication extends Application
{
  public RoutesLoader m_loader;
  
  public List<Route> data;
  
  @Override
  public void onCreate()
  {
    // TODO Auto-generated method stub
    super.onCreate();
    
    m_loader = new RoutesLoader( this );
  }

}
