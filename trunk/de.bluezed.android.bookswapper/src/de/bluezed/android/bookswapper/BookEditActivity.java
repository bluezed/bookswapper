package de.bluezed.android.bookswapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	private int bookStatus = BOOK_NOSTATUS;
	private JSONObject jObject = null;
	private DefaultHttpClient httpclient = new DefaultHttpClient();
	
//	private Uri imageUri;
//	private String imagePath;
//	private String ownImageLink = "";
	
	/** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_book);
        
        httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, this.getString(R.string.app_name_internal) + app_ver);
        
        getSupportActionBar().hide();
        
        Bundle bundle = this.getIntent().getExtras();
        bookID = bundle.getString("bookID");
        bookStatus = bundle.getInt("bookStatus");
        
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
        
        getAllCats();
        
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
        	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
    		final Handler handler = new Handler() {
    		   @Override
			public void handleMessage(Message msg) {
    		      dialog.dismiss();
    		      fillItems();
    		      }
    		   };
    		Thread checkUpdate = new Thread() {  
    		   @Override
			public void run() {
    			  loadBookDetails();
    		      handler.sendEmptyMessage(0);
    		      }
    		   };
    		checkUpdate.start();
        }
    }
    
    private void loadBookDetails() {
        
    	String bookURL = BOOK_URL + bookID;
    	
    	jObject = getJSONFromURL(bookURL, true, httpclient, cookies);
    }
    
    private void fillItems() {
    	if (jObject != null) {
    		try {
//              {"id":"1234","owner":"000","category":"1","isbn":"00000000","title":"XYZ","author":"XYZ","publisher":"XYZ","condition":"1","description":"XYZ","pages":"123","published":"2005","tag":"XYZ","listed":"2007-06-08 13:02:15","format":"paperback"}
        		
    			DrawableManager drawableList = new DrawableManager();
    			drawableList.fetchDrawableOnThread(BASE_URL + "/bigbookimg/" + bookID + ".jpg", imageEditCover);
    			
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
    		if (checkLoggedIn()) {
    			submitChanges();
        	}    		
    	} else {
    		showAlert(this.getString(R.string.warning), this.getString(R.string.data_missing), this.getString(R.string.ok));
    	}
    }
    
    private void submitChanges() {
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   @Override
		public void handleMessage(Message msg) {
			   dialog.dismiss();
			   boolean result = msg.arg1 == 1 ? true : false;
		      
		      	Intent mIntent = new Intent();
		        Bundle bundle = new Bundle();
		        bundle.putBoolean("result", result);
		        bundle.putInt("bookStatus", bookStatus);
		        mIntent.putExtras(bundle);
		        setResult(RESULT_OK, mIntent);
		        finish();
		   }
		};
		Thread checkUpdate = new Thread() {  
		   @Override
		public void run() {
			  Message msg1 = Message.obtain();
			  msg1.arg1 = doSubmit();
		      handler.sendMessage(msg1);
		   }
		};
		checkUpdate.start();
    }
    
    private int doSubmit() {
    	int result = 0;
    	String url = EDITBOOK_URL;
    	
    	if (bookStatus == BOOK_READING) {
    		url = RELISTBOOK_URL;
    	}
    	
    	String posCon = String.valueOf(spinnerEditCon.getSelectedItemPosition() + 1);
    	String posCat = categoryList.get(spinnerEditCat.getSelectedItemPosition()).get("catID");
    	    	    	   	
    	HttpPost httpost = new HttpPost(url);

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
    	
    	if (ownImageLink.length() > 0) {
    		nvps.add(new BasicNameValuePair("resurl", ownImageLink));
    	} else {
    		nvps.add(new BasicNameValuePair("resurl", BASE_URL + "/bigbookimg/" + bookID + ".jpg"));
    	}
    	
    	try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps));
			httpclient.setCookieStore(cookies);
	    	HttpResponse response = httpclient.execute(httpost);
	    	HttpEntity entity = response.getEntity();
	    	
	    	String responseBody = EntityUtils.toString(entity);
	    	
	    	result = 0;
	    	if (bookStatus == BOOK_READING) {
	    		if (responseBody.contains("book relisted")) {
	    			result = 1;
	    		}
	    	} else {
	    		if (responseBody.contains(textEditTitle.getText().toString()) && responseBody.contains("changes saved")) {
		    		result = 1; 
		    	} 
	    	}
	    	
	    	if (entity != null) {
	    	  entity.consumeContent();
	    	}
    	} catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }   
    	
    	return result;
    }
  
    @Override
	public void onPhotoClick(View view) {
    	dispatchPhotoIntent(INTENT_PHOTO_EDIT); 
    }
 }
