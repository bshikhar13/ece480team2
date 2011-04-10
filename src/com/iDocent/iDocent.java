package com.iDocent;

import java.util.ArrayList;
import java.util.Timer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//Main activity class
public class iDocent extends Activity implements OnInitListener{
	static final int DIALOG_SET_SCAN_RATE = 0;
	WifiManager wifi;
	
	//TTS Stuff
	TextToSpeech tts;
	private int MY_DATA_CHECK_CODE = 0;

	//Layout objects
	Menu menu;
    private GLSurfaceView mGLView;
    private Renderer mRenderer;
	
	//Timer objects
	Timer timer;	
	ScanTask scanner;
	Integer scanRate = 1000;
	
	//locations
	float posX=20;
	float posY=-20;
	
	float xStart=0;
	float yStart=0;
	
	//Boolean to turn off wifi at the end of the app if it was off at the start
	boolean wifiWasEnabled;
	
	ScanResultReceiver SRR;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        mGLView = new GLSurfaceView(this);
        mRenderer = new Renderer();
        mGLView.setRenderer(mRenderer);
        mGLView.setKeepScreenOn(true);
        setContentView(mGLView);
        
        UpdateLocation(posX, posY);
        
        //Obtain access to and turn on WiFi
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiWasEnabled = wifi.isWifiEnabled();
        wifi.setWifiEnabled(true);
        
		//Set up to capture Wi-Fi scan results ready event
        SRR = new ScanResultReceiver(this);
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(SRR,i);
		
        //Start the timer running the scanner
		timer = new Timer();		
		scanner = new ScanTask(wifi, this);	
		timer.scheduleAtFixedRate(scanner, 0, scanRate);
		
		//Test for TTS
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);	
    }

	/**
	 * Overrides the default key down event processing function to handle events
	 * specific to this application.
	                          
	{@link onKeyDown}. 
	 *
	                         
	@param  keyCode - the integer code value of the key that was pressed
			event - an object describing the event
	 *  
	   
	@return boolean - success of the event processing
	@see iDocent 
	 */
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	if(keyCode == 4)//Back key
    	{
    		//End the app gracefully
    		scanner.cancel();
			timer.cancel();	
			timer.purge();
			wifi.setWifiEnabled(wifiWasEnabled);
			tts.speak("Goodbye", TextToSpeech.QUEUE_FLUSH, null);
			tts.shutdown();
			mGLView.setKeepScreenOn(false);
			SRR.End();
			this.onBackPressed();
			this.finish();
			System.exit(0);
			return true;
    	}
    	else
    		return super.onKeyDown(keyCode, event);
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			xStart=event.getX();
			yStart=event.getY();
			return true;
		}
		else if(event.getAction()==MotionEvent.ACTION_MOVE)
		{
			mRenderer.MoveCamera(event.getX()-xStart, event.getY()-yStart);
			return true;
		}
		else if(event.getAction()==MotionEvent.ACTION_UP)
		{
			mRenderer.CenterCamera();
			xStart = yStart = 0;
			return true;
		}
		return false;
	}
    
	/**
	 * Handles the event caused by the user pressing the Zoom in button
	                          
	{@link ZoomInButton}. 
	 *
	                         
	@param  none
	 *   
	 */
    public void ZoomInButton()
    {
    	mRenderer.zoomIn();
    }
    
	/**
	 * Handles the event caused by the user pressing the Zoom out button
	                          
	{@link ZoomOutButton}. 
	 *
	                         
	@param  none
	 *   
	 */
    public void ZoomOutButton()
    {
    	mRenderer.zoomOut();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent Data){
    	if(requestCode == MY_DATA_CHECK_CODE){
    		if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
    			//Libraries are there
    			tts = new TextToSpeech(this, this);
    		}
    		else{
    			///Missing proper libraries, download them
    			Intent installIntent = new Intent();
    			installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
    			startActivity(installIntent);
    		}
    	}
    }
	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS){
			
		}
		else{
			
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	/**
	 * Redirects menu button press events to the appropriate functions.
	                          
	{@link onOptionsItemSelected}. 
	 *
	                         
	@param  item - the menu item that was pressed
	 *   
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		case R.id.zoom_in:
	    	tts.speak("Zoom In", TextToSpeech.QUEUE_FLUSH, null);
	    	ZoomInButton();
	    	return true;
		case R.id.zoom_out:
	    	tts.speak("Zoom Out", TextToSpeech.QUEUE_FLUSH, null);
			ZoomOutButton();
			return true;
	    case R.id.list:
	    	tts.speak("Select Destination", TextToSpeech.QUEUE_FLUSH, null);
	        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        Spinner spinner = new SpokenSpinner(this, tts);
	        
	        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adapter);

	        adapter.add("Select Room Number");
	        adapter.add("Room 1225");
	        
	        alert.setView(spinner);
	        final ArrayAdapter<CharSequence> a = adapter;
	        final Spinner s = spinner;
	        
          alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
        	  String selected = (a.getItem(s.getLastVisiblePosition()).toString());
        	  if(s.getLastVisiblePosition() != 0)
        	  {
	              Toast.makeText(getApplicationContext(), selected,
	                      Toast.LENGTH_SHORT).show();
	              tts.speak("Navigating to "+selected, TextToSpeech.QUEUE_FLUSH, null);
        	  }
          }
      });

      alert.setNegativeButton("Cancel",
              new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                	  tts.speak("Canceling new room selection", TextToSpeech.QUEUE_FLUSH, null);
                      dialog.cancel();
                  }
              });
	        
	        alert.show();
	        
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
    
	/**
	 * Updates the location of the user in iDocent and in the renderer
	                          
	{@link UpdateLocation}. 
	 *
	                         
	@param  x - the location in the x direction
			y - the location in the y direction
	 *   
	 */
    public void UpdateLocation(float x, float y)
    {
    	posX = x;
    	posY = y;
    	mRenderer.UpdateLocation(posX, posY);
    }
    
	/**
	 * Obtain the location of the user in the x direction
	                          
	{@link getPosX}. 
	 *
	                         
	@param  none
	
	@return float - the user's location in the x direction
	 *   
	 */
    public float getPosX()
    {
    	return(posX);
    }
    
	/**
	 * Obtain the location of the user in the y direction
	                          
	{@link getPosY}. 
	 *
	                         
	@param  none
	
	@return float - the user's location in the y direction
	 *   
	 */
    public float getPosY()
    {
    	return(posY);
    }

	public void EndTimer() {
		scanner.cancel();
		timer.cancel();	
		timer.purge();		
	}
}