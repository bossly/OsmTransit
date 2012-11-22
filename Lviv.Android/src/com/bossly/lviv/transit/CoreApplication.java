package com.bossly.lviv.transit;

import java.util.List;

import android.app.Application;
import android.content.Context;

public class CoreApplication extends Application
{
  public RoutesLoader m_loader;
  
  public List<Route> data;
  
  @Override
  public void onCreate()
  {
    super.onCreate();
    
    m_loader = new RoutesLoader( this );
  }

  public static CoreApplication get(Context context)
  {
  	return (CoreApplication)context.getApplicationContext();
  }  
}
