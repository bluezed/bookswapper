package de.bluezed.android.bookswapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class BookDetailsActivity extends BookswapperActivity {
		
	private String bookID 	= "";
	private int bookType	= -1;
	private String ownerID	= "";
	private JSONObject jObject = null;
	private DefaultHttpClient httpclient = new DefaultHttpClient();
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_detail);
        
        httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, this.getString(R.string.app_name_internal) + app_ver);
        
        getSupportActionBar().hide();
        
        Bundle bundle = this.getIntent().getExtras();
        bookID = bundle.getString("bookID");
        bookType = bundle.getInt("bookType");
        
        if (checkNetworkStatus()) {
        	if (bookType == BOOKTYPE_MINE) {
        		checkLoggedIn();
        	}
        	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
    		final Handler handler = new Handler() {
    		   public void handleMessage(Message msg) {
    		      dialog.dismiss();
    		      fillItems();
    		      }
    		   };
    		Thread checkUpdate = new Thread() {  
    		   public void run() {
    			  loadBookDetails();
    		      handler.sendEmptyMessage(0);
    		      }
    		   };
    		checkUpdate.start();
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {	    			
    	if (intent != null) {	    			
    		Bundle bundle = intent.getExtras();
    		if (bundle.getBoolean("result")) {
    			Toast.makeText(this, this.getString(R.string.book_changed), Toast.LENGTH_SHORT).show();
    		} else {
    			showAlert(this.getString(R.string.warning), this.getString(R.string.book_not_changed), this.getString(R.string.ok));
    		}
    	}   
    	
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		      fillItems();
		      }
		   };
		Thread checkUpdate = new Thread() {  
		   public void run() {
			  loadBookDetails();
		      handler.sendEmptyMessage(0);
		      }
		   };
		checkUpdate.start();
    	
    }
    
    private void loadBookDetails() {
        
    	String bookURL = BOOK_URL + bookID;
    	
    	if (bookType == BOOKTYPE_MINE) {
    		jObject = getJSONFromURL(bookURL, true, httpclient, cookies);
    	} else {
    		jObject = getJSONFromURL(bookURL, false, httpclient, cookies);
    	}
    }
    
    private void fillItems() {
    	if (jObject != null) {
    		try {
//              {"id":"1234","owner":"000","category":"1","isbn":"00000000","title":"XYZ","author":"XYZ","publisher":"XYZ","condition":"1","description":"XYZ","pages":"123","published":"2005","tag":"XYZ","listed":"2007-06-08 13:02:15","format":"paperback"}
        		
    			ownerID = jObject.getString("owner").toString();
    			
    			DrawableManager drawableList = new DrawableManager();
    			ImageView imagePic= (ImageView) findViewById(R.id.imageShowCover);
    			drawableList.fetchDrawableOnThread(BASE_URL + "/bigbookimg/" + bookID + ".jpg", imagePic);
                
        		TextView textBTitle = (TextView) findViewById(R.id.textTitle);
        		textBTitle.setText(jObject.getString("title").toString());
        		
        		TextView textBAuthor = (TextView) findViewById(R.id.textAuthor);
        		textBAuthor.setText(jObject.getString("author").toString());
        		
        		getAllCats();
        		String cat = jObject.getString("category").toString();
        		TextView textBCategory = (TextView) findViewById(R.id.textCategory);
        		textBCategory.setText(getCategory(cat));
        		
        		TextView textBPublisher = (TextView) findViewById(R.id.textPublisher);
        		textBPublisher.setText(jObject.getString("publisher").toString());
        		
        		TextView textBPages = (TextView) findViewById(R.id.textPages);
        		textBPages.setText(jObject.getString("pages").toString());
        		
        		TextView textBPublished = (TextView) findViewById(R.id.textPublished);
        		textBPublished.setText(jObject.getString("published").toString());
        		
        		int con = jObject.getInt("condition") - 1;
        		Resources res = getResources();
        		String[] cond = res.getStringArray(R.array.condition_array);
        		TextView textBCondition = (TextView) findViewById(R.id.textCondition);
        		textBCondition.setText(cond[con].toString());
				
        		TextView textBISBN = (TextView) findViewById(R.id.textISBN);
        		textBISBN.setText(jObject.getString("isbn").toString());
				
        		TextView textBFormat = (TextView) findViewById(R.id.textFormat);
        		textBFormat.setText(jObject.getString("format").toString());
        		
        		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jObject.getString("listed").toString());
        		String listed = new SimpleDateFormat("dd/MM/yyyy").format(date);        		
        		TextView textBListed = (TextView) findViewById(R.id.textListed);
        		textBListed.setText(listed);
        		
        		TextView textBTags = (TextView) findViewById(R.id.textTags);
        		textBTags.setText(jObject.getString("tag").toString());
        		
        		TextView textBComment = (TextView) findViewById(R.id.textDescription);
        		textBComment.setText(Html.fromHtml(jObject.getString("description").toString()));
        		
        		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
   		MenuInflater inflater = getMenuInflater();
   		switch (bookType) {
   			case BOOKTYPE_MINE:
   				inflater.inflate(R.menu.mybookmenu, menu);
   				break;
   			case BOOKTYPE_OTHER:
   				inflater.inflate(R.menu.otherbookmenu, menu);
   				break;
   		}   		
   		return true;
   	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.deleteBook:
			deleteBook();
			break;
		case R.id.editBook:
			Bundle bundle = new Bundle();
	    	bundle.putString("bookID", bookID);
	    	Intent detailsIntent = new Intent(this.getApplicationContext(), BookEditActivity.class);
	    	detailsIntent.putExtras(bundle);
	    	startActivityForResult(detailsIntent, INTENT_BOOKEDIT);
			break;
		case R.id.swap:
			if (checkLoggedIn()) {
				if (userID.equals(ownerID)) {
					showAlert(this.getString(R.string.warning), this.getString(R.string.your_own_book), this.getString(R.string.ok));
				} else {
					final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
					final Handler handler = new Handler() {
					   public void handleMessage(Message msg) {
						   dialog.dismiss();
							if (msg.arg1 < 1) {
								showNotEnoughMessage();
							} else {
								swapBook();
							}

					   }
					};
					Thread checkUpdate = new Thread() {  
					   public void run() {
						  Message msg1 = Message.obtain();
						  msg1.arg1 = getTokenNumber();
					      handler.sendMessage(msg1);
					   }
					};
					checkUpdate.start();
				}
			}
			break;
		case R.id.tokens2:
			if (checkLoggedIn()) {
				showTokenDialog();
			}
			break;
		case R.id.tokens3:
			if (checkLoggedIn()) {
				showTokenDialog();
			}
			break;
		}
		return true;
	}
    
    private void showNotEnoughMessage() {
    	showAlert(this.getString(R.string.warning), this.getString(R.string.not_enough_tokens), this.getString(R.string.ok));
    }
    
    private void swapBook() {
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
			   	dialog.dismiss();
				swapContinue(msg.arg1);
		   }
		};
		Thread checkUpdate = new Thread() {  
		   public void run() {
			  Message msg1 = Message.obtain();
			  msg1.arg1 = getTokenNumber();
		      handler.sendMessage(msg1);
		   }
		};
		checkUpdate.start();
	}
    
    private void swapContinue(int tokens) {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	if (!checkLoggedIn()) {
    	        		return;
    	        	}
    	        	
    	        	doTask(SWAP_URL + bookID, RETURN_SWAP);
    	            
	    	        break;

    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked --> do nothing!
    	            break;
    	        }
    	    }
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(this.getString(R.string.swap_book));
    	builder.setMessage(this.getString(R.string.token_amount) + " " + String.valueOf(tokens) + "\n" + this.getString(R.string.sure))
    		.setPositiveButton(this.getString(R.string.yes), dialogClickListener)
    	    .setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
    }
    
    private void deleteBook() {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	if (!checkLoggedIn()) {
    	        		return;
    	        	}
    	        	
    	        	doTask(DELETE_URL + bookID, RETURN_DELETE);
    	        	
	    	        break;

    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked --> do nothing!
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(this.getString(R.string.delete));
    	builder.setMessage(this.getString(R.string.sure)).setPositiveButton(this.getString(R.string.yes), dialogClickListener)
    	    .setNegativeButton(this.getString(R.string.no), dialogClickListener).show();

    }
    
    private void doTask(final String taskURL, final int returnType) {
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		      Bundle result = msg.getData();
		      	      
		      Intent mIntent = new Intent();
              Bundle bundle = new Bundle();
              bundle.putInt("option", returnType);
              bundle.putString("message", result.getString("message"));
              bundle.putString("state", result.getString("state"));
              mIntent.putExtras(bundle);
              setResult(RESULT_OK, mIntent);
              finish();
		   }
		};
		Thread checkUpdate = new Thread() {
		   public void run() {
			   String state = "";
			   String message = "";
			   		        
		        JSONObject jObject = getJSONFromURL(taskURL, true, httpclient, cookies);
		    	if (jObject != null) {
		        	try {	
		        		// Swap:
		        		//{"book":"12345","swap":"success","message":"swap requested, the swapper who listed the book has been informed. you can check the status in "my swaps"."}
		        		// Delete:
		        		//{book:bookid,deletion:success||failure,message:our message for success or failure} 
		        		
		        		switch (returnType) {
		        			case RETURN_SWAP:
		        				state = jObject.getString("swap").toString();
		        				break;
		        			case RETURN_DELETE:
		        				state = jObject.getString("deletion").toString();
		        				break;
		        		}
						
						message = jObject.getString("message").toString();
		                
		        	} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
		    	}
		    	
		    	Message msg1 = Message.obtain();
		    	Bundle bundle = new Bundle();
		    	bundle.putString("state", state);
		    	bundle.putString("message", message);
		    	msg1.setData(bundle);
		    	
		    	handler.sendMessage(msg1);
		   }
		};
		checkUpdate.start();
    }
}
