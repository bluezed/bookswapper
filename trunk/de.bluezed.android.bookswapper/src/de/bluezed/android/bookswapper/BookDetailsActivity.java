package de.bluezed.android.bookswapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.TextView;


public class BookDetailsActivity extends BookswapperActivity {
		
	private String bookID 	= "";
	private String bookURL	= "";
	private int bookType	= -1;
		
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_detail);

        Bundle bundle = this.getIntent().getExtras();
        bookURL = bundle.getString("bookURL");
        bookID = bundle.getString("bookID");
        bookType = bundle.getInt("bookType");
        
        if (checkNetworkStatus()) {
        	loadBookDetails();
        }
    }
    
    private void loadBookDetails() {
        
    	Document doc = getJSoupFromURL(bookURL);
    	if (doc != null) {
            
            Element book = doc.getElementById("bigbook");
            
            try {
	            Elements images = book.getElementsByTag("img");
	            for (Element image : images) {  
	            	URL newurl;
					newurl = new URL(BASE_URL + image.attr("src"));
					Bitmap coverPic = BitmapFactory.decodeStream(newurl.openConnection().getInputStream()); 
	                ImageView imagePic= (ImageView) findViewById(R.id.imageShowCover);
	                imagePic.setImageBitmap(coverPic);
	                break;
	            }
            } catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
            
            int count = 0;
            Elements lines = book.getElementsByTag("b");
            for (Element detail : lines) {               	
            	switch (count) {
            	case 0:
            		TextView textBTitle = (TextView) findViewById(R.id.textTitle);
            		textBTitle.setText(detail.text());
            		break;
            	case 1:
            		TextView textBAuthor = (TextView) findViewById(R.id.textAuthor);
            		textBAuthor.setText(detail.text());
            		break;
            	case 2:
            		TextView textBPublisher = (TextView) findViewById(R.id.textPublisher);
            		textBPublisher.setText(detail.text());
            		break;
            	case 3:
            		TextView textBPages = (TextView) findViewById(R.id.textPages);
            		textBPages.setText(detail.text());
            		break;
            	case 4:
            		TextView textBPublished = (TextView) findViewById(R.id.textPublished);
            		textBPublished.setText(detail.text());
            		break;
            	case 5:
            		TextView textBCondition = (TextView) findViewById(R.id.textCondition);
            		textBCondition.setText(detail.text());
            		break;
            	case 6:
            		TextView textBISBN = (TextView) findViewById(R.id.textISBN);
            		textBISBN.setText(detail.text());
            		break;
            	case 7:
            		TextView textBFormat = (TextView) findViewById(R.id.textFormat);
            		textBFormat.setText(detail.text());
            		break;
            	case 8:
            		TextView textBListed = (TextView) findViewById(R.id.textListed);
            		textBListed.setText(detail.text());
            		break;
            	case 9:
            		TextView textBTags = (TextView) findViewById(R.id.textTags);
            		textBTags.setText(detail.text());
            		break;
            	}            	
            	count++;
            }
            
            String bookComment = "";
            boolean found = false;
            lines = book.getElementsByTag("p");
            for (Element detail : lines) {               
            	if (found) {
            		if (detail.text().contains("librarything info")) break;
            		
            		if (bookComment.equals("")) {
            			bookComment = detail.text();
            		} else {
            			bookComment = bookComment + "\n" + detail.text();
            		}            		
            	}
            	
            	if (detail.text().equals("comment:")) {
            		found = true;
            	}
            }
            
            TextView textBComment = (TextView) findViewById(R.id.textDescription);
    		textBComment.setText(bookComment);
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
			// ToDo: Edit dialog
			break;
		case R.id.swap:
			int token = getTokenNumber();
			if (token < 1) {
				showAlert(this.getString(R.string.warning), this.getString(R.string.not_enough_tokens), this.getString(R.string.ok));
			} else {
				swapBook();
			}
			break;
		case R.id.tokens2:
			showTokenDialog();
			break;
		case R.id.tokens3:
			showTokenDialog();
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
    	            String swapURL = SWAP_URL + "&book=" + bookID + "&bookto=" + userID;
    	        	Document doc = getJSoupFromURL(swapURL);
    	        	if (doc != null) {
    	        		Element content = doc.getElementById("main");
    	        		showAlert("Info", content.text().toString(), "OK");
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
	    	                bundle.putString("option", "delete");
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
