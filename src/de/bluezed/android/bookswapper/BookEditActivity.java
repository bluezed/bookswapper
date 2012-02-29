package de.bluezed.android.bookswapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

public class BookEditActivity extends BookswapperActivity {
	
	private EditText textEditISBN;
	private EditText textEditTitle;
	private EditText textEditAuthor;
	private EditText textEditPublisher;
	private EditText textEditSummary;
	private EditText textEditPublished;
	private EditText textEditPages;
	private EditText textEditTags;
	private ImageView imageEditCover;
	private Spinner spinnerEditCat;
	private Spinner spinnerEditCon;
	private RadioButton buttonEditpaperback;
	private RadioButton buttonEdithardcover;
	
	private String bookID 	= "";
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_book);
        
        getSupportActionBar().hide();
        
        Bundle bundle = this.getIntent().getExtras();
        bookID = bundle.getString("bookID");
        
        textEditISBN  		= (EditText) findViewById(R.id.editTextEditISBN);
        textEditTitle 		= (EditText) findViewById(R.id.editTextEditTitle);
        textEditAuthor 		= (EditText) findViewById(R.id.editTextEditAuthor);
        textEditPublisher 	= (EditText) findViewById(R.id.editTextEditPublisher);
        textEditSummary 	= (EditText) findViewById(R.id.editTextEditDescription);
        textEditPublished 	= (EditText) findViewById(R.id.editTextEditPublished);
        textEditPages 		= (EditText) findViewById(R.id.editTextEditPages);
        textEditTags 		= (EditText) findViewById(R.id.editTextEditTags);
        imageEditCover		= (ImageView) findViewById(R.id.imageViewEditCover);
        spinnerEditCat 		= (Spinner) findViewById(R.id.spinnerEditCategory);
        spinnerEditCon 		= (Spinner) findViewById(R.id.spinnerEditCondition); 
        buttonEdithardcover = (RadioButton) findViewById(R.id.radioEditFormatHardcover);
        buttonEditpaperback = (RadioButton) findViewById(R.id.radioEditFormatPaperback);
        
        java.util.List<CharSequence> catList = new ArrayList<CharSequence>();
        for (Map<String,String> catLine : categoryList) {
			catList.add(catLine.get("catname"));
		}
		
		ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, catList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditCat.setAdapter(adapter1);
        
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.condition_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditCon.setAdapter(adapter2);
        
        if (checkLoggedIn()) {
        	loadBookDetails();
        }
    }
    
    private void loadBookDetails() {
        
    	String bookURL = BOOK_URL + bookID;
    	
    	JSONObject jObject = getJSONFromURL(bookURL);
    	if (jObject != null) {
    		try {
//              {"id":"1234","owner":"000","category":"1","isbn":"00000000","title":"XYZ","author":"XYZ","publisher":"XYZ","condition":"1","description":"XYZ","pages":"123","published":"2005","tag":"XYZ","listed":"2007-06-08 13:02:15","format":"paperback"}
        		
				URL newurl = new URL(BASE_URL + "/bigbookimg/" + bookID + ".jpg");
				Bitmap coverPic = BitmapFactory.decodeStream(newurl.openConnection().getInputStream()); 
                imageEditCover.setImageBitmap(coverPic);
                
        		textEditTitle.setText(jObject.getString("title").toString());
        		textEditAuthor.setText(jObject.getString("author").toString());
        		textEditPublisher.setText(jObject.getString("publisher").toString());
        		textEditPages.setText(jObject.getString("pages").toString());
        		textEditPublished.setText(jObject.getString("published").toString());
        		textEditISBN.setText(jObject.getString("isbn").toString());
        		textEditTags.setText(jObject.getString("tag").toString());
        		textEditSummary.setText(Html.fromHtml(jObject.getString("description").toString()));
        		
        		String cat = jObject.getString("category").toString();
        		String catname = getCategory(cat);
        		int catPos = 0;
                for (Map<String,String> catLine : categoryList) {
        			if (catLine.get("catID").equals(cat) && catLine.get("catname").equals(catname)) {
        				break;
        			}
        			catPos++;
        		}
        		spinnerEditCat.setSelection(catPos);
        		
        		int con = jObject.getInt("condition") - 1;
        		spinnerEditCon.setSelection(con);
        		        		
        		String format = jObject.getString("format").toString();
        		if (format.equals("paperback")) buttonEditpaperback.setChecked(true);
        		if (format.equals("hardcover")) buttonEdithardcover.setChecked(true);
        		
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    public void onEditSubmitClick (View view) {
        // check entries
    	boolean checkOK = true;
    	
    	if (textEditISBN.getText().length() == 0) checkOK = false;
    	if (textEditTitle.getText().length() == 0) checkOK = false;
    	if (textEditAuthor.getText().length() == 0) checkOK = false;
    	if (textEditPublisher.getText().length() == 0) checkOK = false;
    	if (textEditPublished.getText().length() == 0) checkOK = false;
    	if (textEditPages.getText().length() == 0) checkOK = false;
    	if (textEditSummary.getText().length() == 0) checkOK = false;
    	
    	if (checkOK) {
    		if (checkNetworkStatus() && checkLoggedIn()) {
    			submitChanges();
        	}    		
    	} else {
    		showAlert(this.getString(R.string.warning), this.getString(R.string.data_missing), this.getString(R.string.ok));
    	}
    }
    
    private void submitChanges() {
    	boolean result = false;
    	
    	String posCon = String.valueOf(spinnerEditCon.getSelectedItemPosition() + 1);
    	String posCat = categoryList.get(spinnerEditCat.getSelectedItemPosition()).get("catID");
    	    	    	   	
    	HttpPost httpost = new HttpPost(EDITBOOK_URL);

    	java.util.List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("id", userID));
    	nvps.add(new BasicNameValuePair("bid", bookID));
    	nvps.add(new BasicNameValuePair("title", textEditTitle.getText().toString()));
    	nvps.add(new BasicNameValuePair("author", textEditAuthor.getText().toString()));
    	nvps.add(new BasicNameValuePair("verlag", textEditPublisher.getText().toString()));
    	nvps.add(new BasicNameValuePair("isbn", textEditISBN.getText().toString()));
    	nvps.add(new BasicNameValuePair("myear", textEditPublished.getText().toString()));
    	nvps.add(new BasicNameValuePair("spages", textEditPages.getText().toString()));
    	nvps.add(new BasicNameValuePair("state", posCon));
    	nvps.add(new BasicNameValuePair("comment", textEditSummary.getText().toString()));
    	nvps.add(new BasicNameValuePair("cat", posCat));
    	if (buttonEditpaperback.isChecked()) nvps.add(new BasicNameValuePair("format", "paperback"));
    	if (buttonEdithardcover.isChecked()) nvps.add(new BasicNameValuePair("format", "hardcover"));
    	nvps.add(new BasicNameValuePair("tags", textEditTags.getText().toString()));
    	nvps.add(new BasicNameValuePair("resurl", BASE_URL + "/bigbookimg/" + bookID + ".jpg"));

    	try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps));

	    	HttpResponse response = httpclient.execute(httpost);
	    	HttpEntity entity = response.getEntity();
	    	
	    	String responseBody = EntityUtils.toString(entity);
	    	
	    	
	    	if (responseBody.contains(textEditTitle.getText().toString()) && responseBody.contains("changes saved")) {
	    		result = true; 
	    	} else {
	    		result = false;
	    	}
	    	
	    	if (entity != null) {
	    	  entity.consumeContent();
	    	}
    	} catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }   
    	
    	Intent mIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putBoolean("result", result);
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }
}
