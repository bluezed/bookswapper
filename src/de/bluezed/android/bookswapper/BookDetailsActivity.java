package de.bluezed.android.bookswapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
		
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_detail);

        Bundle bundle = this.getIntent().getExtras();
        bookID = bundle.getString("bookID");
        bookType = bundle.getInt("bookType");
        
        if (checkNetworkStatus()) {
        	loadBookDetails();
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
    	loadBookDetails();
    }
    
    private void loadBookDetails() {
        
    	String bookURL = BOOK_URL + bookID;
    	
    	JSONObject jObject = getJSONFromURL(bookURL);
    	if (jObject != null) {
    		try {
//              {"id":"1234","owner":"000","category":"1","isbn":"00000000","title":"XYZ","author":"XYZ","publisher":"XYZ","condition":"1","description":"XYZ","pages":"123","published":"2005","tag":"XYZ","listed":"2007-06-08 13:02:15","format":"paperback"}
        		
    			ownerID = jObject.getString("owner").toString();
    			
        		URL newurl;
				newurl = new URL(BASE_URL + "/bigbookimg/" + bookID + ".jpg");
				Bitmap coverPic = BitmapFactory.decodeStream(newurl.openConnection().getInputStream()); 
                ImageView imagePic= (ImageView) findViewById(R.id.imageShowCover);
                imagePic.setImageBitmap(coverPic);
                
        		TextView textBTitle = (TextView) findViewById(R.id.textTitle);
        		textBTitle.setText(jObject.getString("title").toString());
        		
        		TextView textBAuthor = (TextView) findViewById(R.id.textAuthor);
        		textBAuthor.setText(jObject.getString("author").toString());
        		
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
        		
        		
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
					int token = getTokenNumber();
					if (token < 1) {
						showAlert(this.getString(R.string.warning), this.getString(R.string.not_enough_tokens), this.getString(R.string.ok));
					} else {
						swapBook();
					}
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
		case R.id.searchBooks:
			Intent mIntent2 = new Intent();
            Bundle bundle2 = new Bundle();
            bundle2.putInt("option", RETURN_SEARCH);
            mIntent2.putExtras(bundle2);
            setResult(RESULT_OK, mIntent2);
            finish();
			break;
		case R.id.addBook:
			Intent mIntent3 = new Intent();
            Bundle bundle3 = new Bundle();
            bundle3.putInt("option", RETURN_ADD);
            mIntent3.putExtras(bundle3);
            setResult(RESULT_OK, mIntent3);
            finish();
			break;
		case android.R.id.home:
			Intent mIntent4 = new Intent();
            Bundle bundle4 = new Bundle();
            bundle4.putInt("option", RETURN_HOME);
            mIntent4.putExtras(bundle4);
            setResult(RESULT_OK, mIntent4);
            finish();
			break;
		case R.id.myBooks:
			Intent mIntent5 = new Intent();
            Bundle bundle5 = new Bundle();
            bundle5.putInt("option", RETURN_MYBOOKS);
            mIntent5.putExtras(bundle5);
            setResult(RESULT_OK, mIntent5);
            finish();
			break;
		}
		return true;
	}
    
    private void swapBook() {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	if (!checkLoggedIn()) {
    	        		return;
    	        	}      	
    	            String swapURL = SWAP_URL + bookID;
    	            
    	            JSONObject jObject = getJSONFromURL(swapURL);
	            	if (jObject != null) {
		            	try {	
		            		//{"book":"12345","swap":"success","message":"swap requested, the swapper who listed the book has been informed. you can check the status in "my swaps"."}
		            		
		            		String state;
							
							state = jObject.getString("swap").toString();
						
	    	                String message 	= jObject.getString("message").toString();
	    	                    	                    	                
	    	                Intent mIntent = new Intent();
	    	                Bundle bundle = new Bundle();
	    	                bundle.putInt("option", RETURN_SWAP);
	    	                bundle.putString("message", message);
	    	                bundle.putString("state", state);
	    	                mIntent.putExtras(bundle);
	    	                setResult(RESULT_OK, mIntent);
	    	                finish();
		            	} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	        	}
	    	        break;

    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked --> do nothing!
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(this.getString(R.string.swap_book));
    	builder.setMessage(this.getString(R.string.token_amount) + " " + getTokenNumber() + "\n" + this.getString(R.string.sure))
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
    	        	    	            
	            	String delURL = DELETE_URL + bookID;
	            	
	            	JSONObject jObject = getJSONFromURL(delURL);
	            	if (jObject != null) {
	            		try {       	
	            			//{book:bookid,deletion:success||failure,message:our message for success or failure} 
    	                
	            			String state;
						
							state = jObject.getString("deletion").toString();
						
	    	                String message 	= jObject.getString("message").toString();
	    	                    	                    	                
	    	                Intent mIntent = new Intent();
	    	                Bundle bundle = new Bundle();
	    	                bundle.putInt("option", RETURN_DELETE);
	    	                bundle.putString("message", message);
	    	                bundle.putString("state", state);
	    	                mIntent.putExtras(bundle);
	    	                setResult(RESULT_OK, mIntent);
	    	                finish();
	            		
	            		} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	            }
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
}
