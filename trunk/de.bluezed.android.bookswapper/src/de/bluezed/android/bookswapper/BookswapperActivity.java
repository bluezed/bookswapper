package de.bluezed.android.bookswapper;

import java.net.URL;
import java.util.StringTokenizer;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequest;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.VolumeVolumeInfo;
import com.google.api.services.books.model.Volumes;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class BookswapperActivity extends Activity {
    
	private String KEY = null;
	private EditText textISBN;
	private EditText textTitle;
	private EditText textAuthor;
	private EditText textPublisher;
	private EditText textSummary;
	private EditText textPublished;
	private EditText textPages;
	private ImageView imageCover;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        KEY				= this.getString(R.string.google_api_key);
        textISBN  		= (EditText) findViewById(R.id.editText1);
        textTitle 		= (EditText) findViewById(R.id.editText2);
        textAuthor 		= (EditText) findViewById(R.id.editText3);
        textPublisher 	= (EditText) findViewById(R.id.editText4);
        textSummary 	= (EditText) findViewById(R.id.editText5);
        textPublished 	= (EditText) findViewById(R.id.editText8);
        textPages 	= (EditText) findViewById(R.id.editText7);
        imageCover		= (ImageView) findViewById(R.id.imageView1);
        
        TextView feedback = (TextView) findViewById(R.id.textView1);
        feedback.setText(Html.fromHtml("<a href=\"" + this.getString(R.string.feedback_link) + "\">" + this.getString(R.string.copyright) + "</a>"));
        feedback.setMovementMethod(LinkMovementMethod.getInstance());
        
        checkNetworkStatus();
    }
    
    private boolean checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        
        Boolean isOnline = false;
        
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            isOnline = true;
        }
        
        if (!isOnline) {
        	showAlert(this.getString(R.string.warning), this.getString(R.string.not_connected), this.getString(R.string.ok));
        }
        
        return isOnline;
    }

    private void showAlert(String title, String message, String buttonText) {
    	final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle(title);
    	alertDialog.setMessage(message);
    	alertDialog.setButton(buttonText, new DialogInterface.OnClickListener() {
    	   public void onClick(DialogInterface dialog, int which) {
    	      alertDialog.cancel();
    	   }
    	});
    	alertDialog.setIcon(R.drawable.book_icon);
    	alertDialog.show();
    }
    
    private void clearFields() {
    	textTitle.setText("");
    	textAuthor.setText("");
    	textPublisher.setText("");
    	textSummary.setText("");
    	textPublished.setText("");
    	textPages.setText("");
        imageCover.setImageResource(R.drawable.empty);
    }
    
    public void onBarcodeScanClick (View view) {    	
    	clearFields();
    	if (checkNetworkStatus()) {
	    	IntentIntegrator integrator = new IntentIntegrator(this);
	    	integrator.initiateScan();
    	}
    };
    
    public void onManualSearchClick (View view) {
    	clearFields();
    	if (checkNetworkStatus() && textISBN != null && textISBN.length() > 0) {
    		doSearch(textISBN.getText().toString());
    	}
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		if (scanResult != null) {
            String contents = scanResult.getContents();
            // Handle successful scan
            textISBN.setText(contents);
            doSearch(contents);
 		} 
 		else {
            // Handle cancel
 			showAlert(this.getString(R.string.info), this.getString(R.string.error_scan), this.getString(R.string.ok));
        }
    }
    
    private void doSearch (String contents) {            
            // Get details
            JsonFactory jsonFactory = new JacksonFactory();
            try {
	              String query = "isbn:" + contents;

	              try {
	            	  queryGoogleBooks(jsonFactory, query);
	            	  // Success!
	              } catch (GoogleJsonResponseException e) {
	            	  // message already includes parsed response
	            	  System.err.println(e.getMessage());
	              } catch (HttpResponseException e) {
	            	  // message doesn't include parsed response
	            	  System.err.println(e.getMessage());
	            	  System.err.println(e.getResponse().parseAsString());
	              }
            } catch (Throwable t) {
            	t.printStackTrace();
            }
    }
    
    private void queryGoogleBooks(JsonFactory jsonFactory, String query) throws Exception {
    	// Set up Books client.
    	final Books books = Books.builder(new NetHttpTransport(), jsonFactory)
		    .setApplicationName(this.getString(R.string.app_name_internal))
		    .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
	          public void initialize(JsonHttpRequest request) {
                BooksRequest booksRequest = (BooksRequest) request;
                booksRequest.setKey(KEY);
              }
            })
            .build();

        List volumesList = books.volumes().list(query);

        String title 		= "";
        String author 		= "";
        String publisher 	= "";
        String summary 		= "";
        String published	= "";
        String pages 		= "";
        String imageLink 	= "";
                
        // Execute the query.
        Volumes volumes = volumesList.execute();
        if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
        	showAlert(this.getString(R.string.info), this.getString(R.string.not_found), this.getString(R.string.ok));
        	return;
        }

        // Output results
        for (Volume volume : volumes.getItems()) {
          VolumeVolumeInfo volumeInfo = volume.getVolumeInfo();

          // Title
          if (volumeInfo.getTitle() != null && volumeInfo.getTitle().length() > 0) {
        	  title = volumeInfo.getTitle();
          }
                    
          // Author(s).
          java.util.List<String> authors = volumeInfo.getAuthors();
          if (authors != null && !authors.isEmpty()) {
            for (int i = 0; i < authors.size(); ++i) {
              author = author + authors.get(i);
              if (i < authors.size() - 1) {
                author = author + ", ";
              }
            }
          }
          
          // Publisher
          if (volumeInfo.getPublisher() != null && volumeInfo.getPublisher().length() > 0) {
              publisher = volumeInfo.getPublisher();
          }
          
          // Published
          if (volumeInfo.getPublishedDate() != null && volumeInfo.getPublishedDate().length() > 0) {                
        	  published = volumeInfo.getPublishedDate();
        	  StringTokenizer st = new StringTokenizer(published, "-");
        	  if (st.countTokens() > 1) {
        		  published = st.nextToken();
        	  }
          }
          
          // Pages
          if (volumeInfo.getPageCount() != null) {
              pages = volumeInfo.getPageCount().toString();
          }
          
          // Description (if any)
          if (volumeInfo.getDescription() != null && volumeInfo.getDescription().length() > 0) {
            summary = volumeInfo.getDescription();
          }          
          
          // Image link
          if (volumeInfo.getImageLinks() != null) {
        	  if (volumeInfo.getImageLinks().getThumbnail() != null && volumeInfo.getImageLinks().getThumbnail().length() > 0) {
        		  imageLink = volumeInfo.getImageLinks().getThumbnail();  
        	  } 
        	  else if (volumeInfo.getImageLinks().getSmall() != null && volumeInfo.getImageLinks().getSmall().length() > 0) {
        		  imageLink = volumeInfo.getImageLinks().getSmall();  
        	  }
        	  else if (volumeInfo.getImageLinks().getMedium() != null && volumeInfo.getImageLinks().getMedium().length() > 0) {
        		  imageLink = volumeInfo.getImageLinks().getMedium();  
        	  }        	  
          }
        }
        
        /* Set the result to be displayed in our GUI. */
        textTitle.setText(title);
        textAuthor.setText(author);
        textPublisher.setText(publisher);
        textSummary.setText(summary);
        textPublished.setText(published);
        textPages.setText(pages);        
        
        URL newurl = new URL(imageLink); 
        Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream()); 
        imageCover.setImageBitmap(mIcon_val);   
    }
}