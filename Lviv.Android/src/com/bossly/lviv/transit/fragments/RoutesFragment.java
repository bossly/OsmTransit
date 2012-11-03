package com.bossly.lviv.transit.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bossly.lviv.transit.CoreApplication;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.RouteAdapter;
import com.bossly.lviv.transit.activities.RouteMapActivity;

public class RoutesFragment extends Fragment implements TextWatcher, OnClickListener, OnItemClickListener
{
  
  public EditText vEditText;
  
  public ListView vListView;
  
  public RouteAdapter m_adapter;
  
  private int selected = 0;
  
  private int position = 0;
  
  private int scroll_y;
  
  private ArrayList<Route> m_data;
  
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    
    View content = inflater.inflate( R.layout.fr_routes, container, false );
    
    vEditText = ( EditText )content.findViewById( R.id.editText1 );
    vListView = ( ListView )content.findViewById( R.id.listView1 );
    
    vEditText.addTextChangedListener( this );
    
    vEditText.setOnEditorActionListener( new OnEditorActionListener()
    {
      
      @Override
      public boolean onEditorAction( TextView v, int keyCode, KeyEvent event )
      {
        if( keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_CALL )
        {
          hideKeyboard();
          
          return true;
        }
        
        return false;
      }
    } );
    
    vListView.setEmptyView( content.findViewById( android.R.id.empty ) );
    vListView.setOnItemClickListener( this );
    
    content.findViewById( R.id.button_reload ).setOnClickListener( this );
    
    if( savedInstanceState != null )
    {
      scroll_y = savedInstanceState.getInt( "scroll_y" );
      position = savedInstanceState.getInt( "position" );
      selected = savedInstanceState.getInt( "selected" );
    }
    
    CoreApplication app = ( CoreApplication )getActivity().getApplication();
    m_data = new ArrayList<Route>( app.data );
    
    m_adapter = new RouteAdapter( getActivity(), m_data );
    
    vListView.setAdapter( m_adapter );
    vListView.setSelectionFromTop( position, scroll_y );
    
    m_adapter.getFilter().filter( vEditText.getText().toString() );
    
    return content;
  }
  
  @Override
  public void onSaveInstanceState( Bundle outState )
  {
    
    super.onSaveInstanceState( outState );
    
    // save scroll position
    if( vListView != null && vListView.getChildCount() > 0 )
    {
      outState.putInt( "scroll_y", vListView.getChildAt( 0 ).getTop() );
      outState.putInt( "position", vListView.getFirstVisiblePosition() );
    }
    
    outState.putInt( "selected", selected );
  }
  
  private void hideKeyboard()
  {
    // hide virtual keyboard
    InputMethodManager imm = ( InputMethodManager )getActivity().getSystemService( Context.INPUT_METHOD_SERVICE );
    
    imm.hideSoftInputFromWindow( vEditText.getWindowToken(), 0 );
  }
  
  /* TextWatcher */

  @Override
  public void afterTextChanged( Editable arg0 )
  {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public void beforeTextChanged( CharSequence s, int start, int count, int after )
  {
    // TODO Auto-generated method stub
    
  }
  
  Handler handler = new Handler()
  {
    public void handleMessage( Message msg )
    {
      if( msg.obj != null )
      {
        if( m_adapter != null )
        {
          m_adapter.getFilter().filter( msg.obj.toString() );
        }
      }
      
    };
  };

  private Message msg;
  
  @Override
  public void onTextChanged( CharSequence s, int start, int before, int count )
  {
    if( msg != null )
    {
      handler.removeMessages( 0 );
    }
    
    if( m_adapter != null )
    {
      msg = handler.obtainMessage( 0, s.toString() );
      handler.sendMessageDelayed( msg, 100 );
      // m_adapter.getFilter().filter( s );
    }
  }
  
  @Override
  public void onClick( View v )
  {
    vEditText.setText( new String() );
  }
  
  /* OnItemClickListener */

  @Override
  public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
  {
    hideKeyboard();
    
    scroll_y = vListView.getChildAt( 0 ).getTop();
    position = vListView.getFirstVisiblePosition();
    selected = arg2;
    
    Route route = ( Route )arg0.getItemAtPosition( arg2 );
    
    Intent intent = new Intent( getActivity(), RouteMapActivity.class );
    intent.putExtra( RouteMapActivity.EXTRA_ROUTE, route );
    startActivity( intent );
  }
}
