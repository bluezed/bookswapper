/**
   Bookswapper App
   Copyright (C) 2012 Thomas Geppert (bluezed.apps@gmail.com)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software Foundation,
   Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/

package de.bluezed.android.bookswapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.Menu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;


public class SwapDetailsActivity extends BookswapperActivity {
	private DefaultHttpClient httpclient = new DefaultHttpClient();
	private Swap swap = null;
	
	/** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swap_details);
        
        httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, this.getString(R.string.app_name_internal) + app_ver);
        
        getSupportActionBar().hide();

        swap = (Swap)this.getIntent().getSerializableExtra("order");
        
        fillDetails(swap);
    }
    
    @Override
   	public boolean onCreateOptionsMenu(Menu menu) {
   		return true;
   	}
    
    private void fillDetails(Swap order) {    	
    	TextView textSTitle = (TextView) findViewById(R.id.textSwapTitle);
		textSTitle.setText(order.title);
		
		TextView textSAuthor = (TextView) findViewById(R.id.textSwapAuthor);
		textSAuthor.setText(order.author);
		
		TextView textSUser = (TextView) findViewById(R.id.textSwapUser);
		textSUser.setText(order.user);
		
		TextView textSStatus = (TextView) findViewById(R.id.textSwapStatus);
		switch (order.status) {
			case BookswapperActivity.BOOK_SHIPPED:
				textSStatus.setText(this.getString(R.string.shipped) + " " + order.shipped);
				break;
			case BookswapperActivity.BOOK_NOT_SHIPPED:
				textSStatus.setText(this.getString(R.string.not_shipped) + " " + order.ordered);
				break;
		}
		
		TextView textSType 			= (TextView) findViewById(R.id.textSwapType);
		TextView textSAddressLabel 	= (TextView) findViewById(R.id.textSwapAddressLabel);
		TextView textSAddress 		= (TextView) findViewById(R.id.textSwapAddress);
		Button buttonSConfirm		= (Button) findViewById(R.id.buttonSwapConfirm);
		switch (order.type) {
			case BookswapperActivity.BOOK_IN:
				textSType.setText(this.getString(R.string.incoming));
				textSAddressLabel.setVisibility(View.INVISIBLE);
				textSAddress.setVisibility(View.INVISIBLE);
				buttonSConfirm.setText(this.getString(R.string.mark_received));
				break;
			case BookswapperActivity.BOOK_OUT:
				textSType.setText(this.getString(R.string.outgoing));
				textSAddress.setText(order.firstname + " " + order.lastname + "\n" + order.street + "\n" + order.postcode + " " + order.city);
				if (order.status == BookswapperActivity.BOOK_NOT_SHIPPED) {
					buttonSConfirm.setText(this.getString(R.string.mark_shipped));
				} else {
					buttonSConfirm.setVisibility(View.INVISIBLE);
				}				
				break;
		}
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
	    LayoutInflater factory11 = LayoutInflater.from(getBaseContext());            
	    final View feedbackView = factory11.inflate(R.layout.swap_feedback, null);
	
	    AlertDialog.Builder alert11 = new AlertDialog.Builder(this); 
	
	    alert11.setTitle(R.string.feedback);  
	    alert11.setView(feedbackView); 
	    alert11.create();
	    	            	            
	    final RadioButton positive = (RadioButton) feedbackView.findViewById(R.id.radioFeedbackPos);
    	final RadioButton neutral = (RadioButton) feedbackView.findViewById(R.id.radioFeedbackNeu);
    	final RadioButton negative = (RadioButton) feedbackView.findViewById(R.id.radioFeedbackNeg);
    	final EditText comment = (EditText) feedbackView.findViewById(R.id.editTextFeedbackComment);
    	
	    alert11.setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() { 
	        @Override
			public void onClick(DialogInterface dialog, int whichButton) { 
	        	// Send feedback  
	        	List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	        	nvps.add(new BasicNameValuePair("order", swap.orderID));
		    	nvps.add(new BasicNameValuePair("book", swap.bookID));
		    	nvps.add(new BasicNameValuePair("info", swap.title));
		    	nvps.add(new BasicNameValuePair("client", swap.userID));
		    	
		    	if (positive.isChecked()) nvps.add(new BasicNameValuePair("feedback", "1"));
		    	if (neutral.isChecked()) nvps.add(new BasicNameValuePair("feedback", "0"));
		    	if (negative.isChecked()) nvps.add(new BasicNameValuePair("feedback", "-1"));
		    	
		    	nvps.add(new BasicNameValuePair("message", comment.getText().toString()));
				
		    	doConfirm(BookswapperActivity.RECEIVED_URL, nvps);	
	        } 
	    });
	    
	    alert11.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() { 
	        @Override
			public void onClick(DialogInterface dialog, int whichButton) { 
	          // Just close it... 
	        } 
	      }); 
	    
	   alert11.show();
	   
	   return null;
	}
    
    public void onSwapConfirmClick (View view) {
    	if (swap == null || !checkLoggedIn()) {
    		return;
    	}
    	
    	switch (swap.type) {
			case BookswapperActivity.BOOK_IN:
				// Got it! -> give feedback
				showDialog(BookswapperActivity.DIALOG_FEEDBACK);
				break;
				
			case BookswapperActivity.BOOK_OUT:
				// Sent it!
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("orderid", swap.orderID));
		    	nvps.add(new BasicNameValuePair("bookid", swap.bookID));
				doConfirm(BookswapperActivity.SHIPPED_URL, nvps);			
				break;
    	}
    }
    
    private void doConfirm (final String confirmURL, final List<NameValuePair> nvps) {	  	
    	if (!checkNetworkStatus()) {
    		return;
    	}
    	
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
 		final Handler handler = new Handler() {
 		   @Override
		public void handleMessage(Message msg) {
 		      dialog.dismiss();
 		      finish();
 		   }
 		};
 		Thread checkUpdate = new Thread() {  
 		   @Override
		public void run() {
 			  doTask(confirmURL, nvps); 
 			  Message message = handler.obtainMessage();
              handler.sendMessage(message);
 		   }
 		};
 		checkUpdate.start();
    }
    
    private void doTask(String confirmURL, List<NameValuePair> nvps) {   	
    	HttpPost httpost = new HttpPost(confirmURL);

    	try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps));
			
			httpclient.setCookieStore(cookies);
	    	HttpResponse response = httpclient.execute(httpost);
	    	HttpEntity entity = response.getEntity();
	    	BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
	    	entity.consumeContent();
    	} catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }
}
