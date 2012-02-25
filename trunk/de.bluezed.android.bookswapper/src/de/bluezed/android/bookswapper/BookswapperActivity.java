package de.bluezed.android.bookswapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.google.api.services.books.model.VolumeVolumeInfoIndustryIdentifiers;
import com.google.api.services.books.model.Volumes;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class BookswapperActivity extends FragmentActivity {
    
	private static final int DIALOG_LOGIN 		= 1000;
	private static final int DIALOG_LOGIN_DATA 	= 1500;
	private static final int DIALOG_ABOUT 		= 2000;
	private static final int DIALOG_TOKENS		= 3000;
	
	private static final int INTENT_BOOKDETAILS = 1000;
	protected static final int INTENT_BOOKEDIT 	= 2000;
	
	protected static final int BOOKTYPE_MINE	= 1;
	protected static final int BOOKTYPE_OTHER	= 0;
	
	protected static final int RETURN_DELETE	= 0;
	protected static final int RETURN_SWAP		= 2;
	protected static final int RETURN_SEARCH	= 3;
	protected static final int RETURN_ADD		= 4;
	protected static final int RETURN_HOME		= 5;
	protected static final int RETURN_MYBOOKS	= 6;
	
	private static final String KEY 			= "AIzaSyCjHNFXZvQTkyBNLvW_VbP_sJ0bChpLZVU";
	
	protected static final String BASE_URL		= "http://www.bookswapper.de";
	private static final String LOGIN_URL 		= BASE_URL + "/swap/login.php";
	private static final String MYID_URL 		= BASE_URL + "/api/my";
	private static final String ADDBOOK_URL 	= BASE_URL + "/swap/addbook.php?action=add";
	protected static final String EDITBOOK_URL 	= BASE_URL + "/swap/addbook.php?action=edit";
	private static final String MYBOOKS_URL		= BASE_URL + "/api/mybooks";
	private static final String SEARCH_URL		= BASE_URL + "/api/search/";
	protected static final String BOOK_URL		= BASE_URL + "/api/book/";
	protected static final String DELETE_URL 	= BASE_URL + "/api/delbook/";
	protected static final String CATS_URL 		= BASE_URL + "/api/cats";
	private static final String SIGNUP_URL		= BASE_URL + "/swap/registration.php?action=signup";
	private static final String TOKEN_URL		= BASE_URL + "/swap/member.php?action=showtokens";
	protected static final String SWAP_URL		= BASE_URL + "/api/order/";
			
	protected boolean loggedIn 		= false;
	protected String userID 		= "";
	private Bitmap coverImage 		= null;
	private String uploadImageLink 	= "";
	private String app_ver			= "";
	
	private java.util.List<String> bookData 					= new ArrayList<String>();
	protected java.util.List<Map<String,String>> categoryList	= new ArrayList<Map<String,String>>();
	
	protected EditText textISBN;
	protected EditText textTitle;
	protected EditText textAuthor;
	protected EditText textPublisher;
	protected EditText textSummary;
	protected EditText textPublished;
	protected EditText textPages;
	protected EditText textTags;
	protected ImageView imageCover;
	protected ImageView imageGoogle;
	protected Spinner spinnerCat;
	protected Spinner spinnerCon;
	protected ListView myList;
	
	protected SharedPreferences preferences;
	protected DefaultHttpClient httpclient = new DefaultHttpClient();
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        preferences 	= PreferenceManager.getDefaultSharedPreferences(this);
        
        try
        {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (NameNotFoundException e)
        {
           // not found
        }
        
        if (preferences.getString("versionCheck", "0").compareTo(app_ver) != 0) {
        	showDialog(DIALOG_ABOUT);
        	Editor edit = preferences.edit();
        	edit.putString("versionCheck", app_ver);
        	edit.commit();
        }
        
        // Remove username and password if remember is not set!
        if (!preferences.getBoolean("rememberPassword", false)) {
        	Editor edit = preferences.edit();
        	edit.putString("username", "");
        	edit.putString("password", "");
        	edit.commit();
        }
		   
        // Get all the categories
        if (checkNetworkStatus()) {
//        	{"cats":[{"maincat":"fiction","catid":"2","catname":"crime & mystery"},{"maincat":"fiction","catid":"16","catname":"horror"},{"maincat":"fiction","catid":"3","catname":"romance"},{"maincat":"fiction","catid":"14","catname":"historical romance"},{"maincat":"fiction","catid":"10","catname":"humour"},{"maincat":"fiction","catid":"4","catname":"sci-fi & fantasy"},{"maincat":"fiction","catid":"5","catname":"chick lit"},{"maincat":"fiction","catid":"13","catname":"children's"},{"maincat":"fiction","catid":"15","catname":"historical fiction"},{"maincat":"fiction","catid":"1","catname":"novels general"},{"maincat":"non-fiction","catid":"12","catname":"history & politics"},{"maincat":"non-fiction","catid":"11","catname":"mind & body"},{"maincat":"non-fiction","catid":"6","catname":"memoirs & biographies"},{"maincat":"non-fiction","catid":"8","catname":"travel books"},{"maincat":"non-fiction","catid":"9","catname":"other non-fiction"},],"complete":"true"}
        	
        	if (categoryList.size() == 0) {
	        	JSONObject jObject = getJSONFromURL(CATS_URL);
	    		if (jObject != null) {
	    			try {
	    				JSONArray resultArray = jObject.getJSONArray("cats");
	                    
	    	            String catname 	= "";
	    	            String catID 	= "";  	            
	    	            for (int i = 0; i < resultArray.length() - 1; i++) {
	    	    			
							catname = resultArray.getJSONObject(i).getString("catname").toString();
	    	    			catID 	= resultArray.getJSONObject(i).getString("catid").toString();
	    	    			
	    	    			Map<String, String> record = new HashMap<String, String>(2);
	    	    			record.put("catID", catID);
	    	    			record.put("catname", catname);
	    	            	
	    	    			categoryList.add(record);
	    	            }
	    			} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
        	}
        }
    }	
	
	 @Override
	 protected Dialog onCreateDialog(int id) {
		 switch (id) {
	        case DIALOG_LOGIN:
	        	if (!loggedIn || userID.length() == 0) {
	        		showLoginDialog();
	        	}
	        	break;
	        	
	        case DIALOG_LOGIN_DATA:
	        	showLoginDialog();
	        	break;
	        
	        case DIALOG_TOKENS:
	        	showTokenDialog();
	        	break;
	        	
	        case DIALOG_ABOUT:
	        	LayoutInflater factory1 = LayoutInflater.from(this);            
	            final View aboutView = factory1.inflate(R.layout.about, null);

	            AlertDialog.Builder alert1 = new AlertDialog.Builder(this); 

	            alert1.setTitle(R.string.about);  
	            alert1.setView(aboutView); 
	            alert1.create();
	            
	            final TextView version = (TextView) aboutView.findViewById(R.id.textViewVersion);
	            version.setText(this.getString(R.string.version) + " " + app_ver);
	            
	            TextView feedback = (TextView) aboutView.findViewById(R.id.textViewEmail);
	            feedback.setText(Html.fromHtml("<a href=\"" + this.getString(R.string.feedback_link) + "\">" + this.getString(R.string.email) + "</a>"));
	            feedback.setMovementMethod(LinkMovementMethod.getInstance());
	            
	            TextView bookswapper = (TextView) aboutView.findViewById(R.id.textViewBookswapper);
	            bookswapper.setText(Html.fromHtml("<a href=\"" + this.getString(R.string.bookswapper_link) + "\">" + this.getString(R.string.bookswapper_link) + "</a>"));
	            bookswapper.setMovementMethod(LinkMovementMethod.getInstance());
	            
	            TextView graphics = (TextView) aboutView.findViewById(R.id.textViewGraphicsLink);
	            graphics.setText(Html.fromHtml("<a href=\"" + this.getString(R.string.graphics_link) + "\">" + this.getString(R.string.graphics_link) + "</a>"));
	            graphics.setMovementMethod(LinkMovementMethod.getInstance());
	            
	            alert1.setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() { 
		            public void onClick(DialogInterface dialog, int whichButton) { 
		            	// Just close it         	
		            } 
	            });

	            alert1.show();
	            
	        	break;
		 }
	     return null;
	 }
	
	 protected void showTokenDialog() {
		int token = getTokenNumber();
		showAlert(this.getString(R.string.show_tokens), this.getString(R.string.token_amount) + " " + String.valueOf(token), this.getString(R.string.ok));
	 }
	 
	 protected int getTokenNumber() {
		 int token = 0;
		 if (checkLoggedIn()) {
			JSONObject jObject = getJSONFromURL(MYID_URL);
			if (jObject != null) {
				try {
//					{"myid":"000","tokencount":"0","tokenout":"0","tokenin":"0"}
					String uID = jObject.getString("myid").toString();
					if (!uID.equals("not logged in")) {
						token = jObject.getInt("tokencount");
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		 }
		 return token;
	 }
	 
	 private void showLoginDialog() {
		 LayoutInflater factory = LayoutInflater.from(this);            
         final View textEntryView = factory.inflate(R.layout.login, null);

         AlertDialog.Builder alert = new AlertDialog.Builder(this); 

         alert.setTitle(R.string.login); 
         alert.setMessage(R.string.login_prompt); 
         // Set an EditText view to get user input  
         alert.setView(textEntryView); 
         alert.create();

         final EditText input1 = (EditText) textEntryView.findViewById(R.id.editTextUser);
         final EditText input2 = (EditText) textEntryView.findViewById(R.id.editTextPass);
         final CheckBox checkBox = (CheckBox) textEntryView.findViewById(R.id.checkBoxRemember);
         
         input1.setText(preferences.getString("username", ""));
         input2.setText(preferences.getString("password", ""));
         checkBox.setChecked(preferences.getBoolean("rememberPassword", true));
         
         alert.setPositiveButton(this.getString(R.string.login), new DialogInterface.OnClickListener() { 
         public void onClick(DialogInterface dialog, int whichButton) { 
         	String user = input1.getText().toString();
         	String pass = input2.getText().toString();
         	
         	Editor edit = preferences.edit();
            edit.putString("username", user);
         	edit.putString("password", pass);
         	edit.putBoolean("rememberPassword", checkBox.isChecked());
         	edit.commit();
         	
         	doLogin(user, pass);
         	
         	if (!loggedIn) {
         		showAlert(BookswapperActivity.this.getString(R.string.warning), BookswapperActivity.this.getString(R.string.login_error), BookswapperActivity.this.getString(R.string.ok));
     			return;
     		}		            	
         } 
         }); 

         alert.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() { 
           public void onClick(DialogInterface dialog, int whichButton) { 
             // Canceled. 
           } 
         }); 

         alert.show();
	 }
	 
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.change_login:
			showDialog(DIALOG_LOGIN_DATA);
			break;
		case R.id.about:
			showDialog(DIALOG_ABOUT);
			break;
		case R.id.myBooks:
			setContentView(R.layout.my_books);
			if (checkNetworkStatus()) {
				loadMyBooks();
			}
			break;
		case R.id.searchBooks:
			setContentView(R.layout.search);
			break;
		case R.id.addBook:
			setContentView(R.layout.add_book);
			loadAddBook();
			break;
		case R.id.tokens1:
			if (checkLoggedIn()) {
				showDialog(DIALOG_TOKENS);
			}
			break;
		case android.R.id.home:
			setContentView(R.layout.main);
			break;
		}
		return true;
	}
	
	protected String getCategory(String catID) {
		String category = "";
		
		for (Map<String,String> catLine : categoryList) {
			if (catLine.get("catID").equals(catID)) {
				category = catLine.get("catname");
				break;
			}
		}
		
		return category;
	}
	
	private void loadAddBook() {
		textISBN  		= (EditText) findViewById(R.id.editTextAddISBN);
        textTitle 		= (EditText) findViewById(R.id.editTextAddTitle);
        textAuthor 		= (EditText) findViewById(R.id.editTextAddAuthor);
        textPublisher 	= (EditText) findViewById(R.id.editTextAddPublisher);
        textSummary 	= (EditText) findViewById(R.id.editTextAddDescription);
        textPublished 	= (EditText) findViewById(R.id.editTextAddPublished);
        textPages 		= (EditText) findViewById(R.id.editTextAddPages);
        textTags 		= (EditText) findViewById(R.id.editTextTags);
        imageCover		= (ImageView) findViewById(R.id.imageViewCover);
        imageGoogle		= (ImageView) findViewById(R.id.imageViewGoogle);
        spinnerCat 		= (Spinner) findViewById(R.id.spinnerCategory);
        spinnerCon 		= (Spinner) findViewById(R.id.spinnerCondition); 
        		
        java.util.List<CharSequence> catList = new ArrayList<CharSequence>();
        for (Map<String,String> catLine : categoryList) {
			catList.add(catLine.get("catname"));
		}
		
		ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, catList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(adapter1);
        
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.condition_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCon.setAdapter(adapter2);
	}
	
	public void onSignUpClick (View view) {
		Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(SIGNUP_URL));
		startActivity(viewIntent); 
	}	
	
    public void onBarcodeScanClick (View view) {    	
    	clearFields(false);
    	if (checkNetworkStatus()) {
	    	IntentIntegrator integrator = new IntentIntegrator(this);
	    	integrator.initiateScan();
    	}
    };
    
    public void onManualSearchClick (View view) {
    	clearFields(false);
    	if (checkNetworkStatus() && textISBN != null && textISBN.length() > 0) {
    		doSearch(textISBN.getText().toString());
    	}
    }
        
    public void onSubmitClick (View view) {
        // check entries
    	boolean checkOK = true;
    	
    	if (textISBN.getText().length() == 0) checkOK = false;
    	if (textTitle.getText().length() == 0) checkOK = false;
    	if (textAuthor.getText().length() == 0) checkOK = false;
    	if (textPublisher.getText().length() == 0) checkOK = false;
    	if (textPublished.getText().length() == 0) checkOK = false;
    	if (textPages.getText().length() == 0) checkOK = false;
    	if (textSummary.getText().length() == 0) checkOK = false;
    	
    	if (checkOK) {
    		if (checkNetworkStatus() && checkLoggedIn()) {
    			addBook();
        	}    		
    	} else {
    		showAlert(this.getString(R.string.warning), this.getString(R.string.data_missing), this.getString(R.string.ok));
    	}
    }
    
    protected boolean checkLoggedIn() {
    	String user = preferences.getString("username", "");
		String pass = preferences.getString("password", "");
		
		if (!checkNetworkStatus()) {
			return false;
		}
		
		if (user.length() > 0 && pass.length() > 0) {
			if (!loggedIn || userID.length() == 0) {
				doLogin(user, pass);
			}
			
			if (loggedIn) {
				return true;
			} else {	
    			showAlert(BookswapperActivity.this.getString(R.string.warning), BookswapperActivity.this.getString(R.string.login_error), BookswapperActivity.this.getString(R.string.ok));
    			return false;
    		}
		} else {
			showDialog(DIALOG_LOGIN);

			if (loggedIn) {
				return true;
			}
		}
		
		// should never get here!
		return false;
    }
    
    protected boolean checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        
        Boolean isOnline = false;
        
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            isOnline = true;
        } else {
        	showAlert(this.getString(R.string.warning), this.getString(R.string.not_connected), this.getString(R.string.ok));
        }
        
        return isOnline;
    }

    protected void showAlert(String title, String message, String buttonText) {
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
    
    private void clearFields(boolean clearISBN) {
    	textTitle.setText("");
    	textAuthor.setText("");
    	textPublisher.setText("");
    	textSummary.setText("");
    	textPublished.setText("");
    	textPages.setText("");
    	textTags.setText("");
        imageCover.setImageResource(R.drawable.empty);
        spinnerCon.setSelection(0);
        spinnerCat.setSelection(0);
        
        RadioButton paperback = (RadioButton) findViewById(R.id.radioFormatPaperback);
        paperback.setChecked(true);
        
        if (clearISBN) {
        	textISBN.setText("");
        }
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(100, 5, 0, 0);
        imageGoogle.setLayoutParams(lp);
    }
        
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	switch (requestCode) {
	    	case INTENT_BOOKDETAILS:
	    		if (intent != null) {	    			
		    		Bundle bundle = intent.getExtras();
		    		int option = -1;
		    		option = bundle.getInt("option");
		    		if (option > -1) {
		    			switch (option) {
			    			case RETURN_DELETE:
			    				showAlert(this.getString(R.string.info), bundle.getString("state") + "\n" + bundle.getString("message"), this.getString(R.string.ok));
			    				loadMyBooks();
			    				break;
			    			case RETURN_SWAP:
			    				showAlert(this.getString(R.string.info), bundle.getString("state") + "\n" + bundle.getString("message"), this.getString(R.string.ok));
			    				populateBookList(BOOKTYPE_OTHER);
			    				break;
			    			case RETURN_MYBOOKS:
			    				setContentView(R.layout.my_books);
			    				if (checkNetworkStatus()) {
			    					loadMyBooks();
			    				}
			    				break;
			    			case RETURN_HOME:
			    				setContentView(R.layout.main);
			    				break;
			    			case RETURN_SEARCH:
			    				populateBookList(BOOKTYPE_OTHER);
			    				break;
			    			case RETURN_ADD:
			    				setContentView(R.layout.add_book);
			    				loadAddBook();
			    				break;
		    			}
		    		}
	    		}
	    		break;
	    	
	    	default:
		    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		 		if (scanResult != null) {
		            String contents = scanResult.getContents();
		            // Handle successful scan
		            textISBN.setText(contents);
		            if (checkNetworkStatus()) {
		            	doSearch(contents);
		            }
		 		} 
		 		else {
		            // Handle cancel
		 			showAlert(this.getString(R.string.info), this.getString(R.string.error_scan), this.getString(R.string.ok));
		        }
		 		break;
    	}
    }
    
    private void doSearch (String contents) {               	   	
   	   	String query = "isbn:" + contents;
   	   	
    	JsonFactory jsonFactory = new JacksonFactory();
        try {	                  
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
		    .setApplicationName(this.getString(R.string.app_name_internal) + app_ver)
		    .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
	          public void initialize(JsonHttpRequest request) {
                BooksRequest booksRequest = (BooksRequest) request;
                booksRequest.setKey(KEY);
              }
            })
            .build();

        List volumesList = books.volumes().list(query);

        String isbn			= "";
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
          
          // Try to get ISBN_10
          for (VolumeVolumeInfoIndustryIdentifiers industry : volumeInfo.getIndustryIdentifiers()) {
        	  if (industry.getType().equals("ISBN_10")) {
        		  isbn = industry.getIdentifier();
        		  break;
        	  } else if (industry.getType().equals("ISBN_13")) {
        		  isbn = industry.getIdentifier();
        	  }
          }
          
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
        		  uploadImageLink = volumeInfo.getImageLinks().getThumbnail();
        	  } 
        	  
        	  if (volumeInfo.getImageLinks().getMedium() != null && volumeInfo.getImageLinks().getMedium().length() > 0) {
        		  uploadImageLink = volumeInfo.getImageLinks().getMedium();
        	  } 
        	  else if (volumeInfo.getImageLinks().getSmall() != null && volumeInfo.getImageLinks().getSmall().length() > 0) {
        		  uploadImageLink = volumeInfo.getImageLinks().getSmall();
        	  }
        	  
        	  if (imageLink.length() == 0) {
        		  imageLink = uploadImageLink;
        	  }
          }
          
          // Only need the first result!
          break;
        }
        
        /* Set the result to be displayed in our GUI. */
        if (isbn.length() > 0) {
        	textISBN.setText(isbn);
        }
        textTitle.setText(title);
        textAuthor.setText(author);
        textPublisher.setText(publisher);
        textSummary.setText(summary);
        textPublished.setText(published);
        textPages.setText(pages);        
        
        URL newurl = new URL(imageLink); 
        coverImage = BitmapFactory.decodeStream(newurl.openConnection().getInputStream()); 
        imageCover.setImageBitmap(coverImage);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 5, 0, 0);
        imageGoogle.setLayoutParams(lp);
    }
    
    private void doLogin (String user, String pass) {	
    	if (!checkNetworkStatus()) {
    		loggedIn = false;
    	}
    	
    	HttpPost httpost = new HttpPost(LOGIN_URL);

    	java.util.List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("nick", user));
    	nvps.add(new BasicNameValuePair("pass", pass));

    	try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps));

	    	HttpResponse response = httpclient.execute(httpost);
	    	HttpEntity entity = response.getEntity();
	
	    	if (entity != null) {
	    	  entity.consumeContent();
	    	}
    	} catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

    	java.util.List<Cookie> cookies = httpclient.getCookieStore().getCookies();
    	
    	if (cookies.size() > 2) {
    		loggedIn = true;
    		getUserID();
    		if (userID.length() == 0) {
    			loggedIn = false;
    		}
    	}
    	
    	return;
    }
    
    protected void getUserID() {  
    	JSONObject jObject = getJSONFromURL(MYID_URL);
		if (jObject != null) {
			try {
//				{myid:id||not logged in}
				userID = jObject.getString("myid").toString();
				if (userID.equals("not logged in")) {
					userID = "";
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
        
    private void addBook() {
    	String posCon = String.valueOf(spinnerCon.getSelectedItemPosition() + 1);
    	String posCat = categoryList.get(spinnerCat.getSelectedItemPosition()).get("catID");
    	    	
    	RadioButton paperback = (RadioButton) findViewById(R.id.radioFormatPaperback);
    	RadioButton hardcover = (RadioButton) findViewById(R.id.radioFormatHardcover);
    	   	
    	HttpPost httpost = new HttpPost(ADDBOOK_URL);

    	java.util.List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("id", userID));
    	nvps.add(new BasicNameValuePair("title", textTitle.getText().toString()));
    	nvps.add(new BasicNameValuePair("author", textAuthor.getText().toString()));
    	nvps.add(new BasicNameValuePair("verlag", textPublisher.getText().toString()));
    	nvps.add(new BasicNameValuePair("isbn", textISBN.getText().toString()));
    	nvps.add(new BasicNameValuePair("myear", textPublished.getText().toString()));
    	nvps.add(new BasicNameValuePair("spages", textPages.getText().toString()));
    	nvps.add(new BasicNameValuePair("state", posCon));
    	nvps.add(new BasicNameValuePair("comment", textSummary.getText().toString()));
    	nvps.add(new BasicNameValuePair("cat", posCat));
    	if (paperback.isChecked()) nvps.add(new BasicNameValuePair("format", "paperback"));
    	if (hardcover.isChecked()) nvps.add(new BasicNameValuePair("format", "hardcover"));
    	nvps.add(new BasicNameValuePair("tags", textTags.getText().toString()));
    	nvps.add(new BasicNameValuePair("resurl", uploadImageLink));

    	try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps));

	    	HttpResponse response = httpclient.execute(httpost);
	    	HttpEntity entity = response.getEntity();
	    	
	    	String responseBody = EntityUtils.toString(entity);
	    	
	    	if (responseBody.contains(textTitle.getText().toString()) && responseBody.contains("swappable books")) {
	    		showAlert(this.getString(R.string.success), this.getString(R.string.book_added), this.getString(R.string.ok));
	    		clearFields(true);
	    	} else {
	    		showAlert(this.getString(R.string.warning), this.getString(R.string.book_not_added), this.getString(R.string.ok));
	    	}
	    	
	    	if (entity != null) {
	    	  entity.consumeContent();	    	  
	    	}
    	} catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }   	
    }
    
    private void loadMyBooks() {
    	populateBookList(BOOKTYPE_MINE);
    }
    
    public void onSearchClick (View view) {
    	populateBookList(BOOKTYPE_OTHER);
    }
    
    private void populateBookList(int bookType) {
    	String strURL = "";
    	if (bookType == BOOKTYPE_MINE) {
    		if (!checkLoggedIn()) {
    			return;
    		}
    		myList = (ListView) findViewById(R.id.listMyBooks);
    	   	strURL = MYBOOKS_URL;
    	} else if (bookType == BOOKTYPE_OTHER) {
    		myList = (ListView) findViewById(R.id.listViewSearchResult);
    		EditText textSearch = (EditText) findViewById(R.id.editTextSearch);
        	strURL = SEARCH_URL + textSearch.getText().toString();
    	} else {
    		return;
    	}
    	    	
    	// Populate my books list
		bookData.clear();

		JSONObject jObject = getJSONFromURL(strURL);
		if (jObject != null) {
			try {
				String hits;
			
				hits = jObject.getString("hits").toString();
            
	            if (hits.equals("not logged in")) {
	            	showAlert(BookswapperActivity.this.getString(R.string.warning), BookswapperActivity.this.getString(R.string.loading_failed), BookswapperActivity.this.getString(R.string.ok));
	            	return;
	            }
	            
	            JSONArray resultArray = jObject.getJSONArray("results");
	                            
	            String title = "";
	            String author = "";
	            String bookID = "";
	            java.util.List<Map<String, String>> bookListData = new ArrayList<Map<String, String>>();
	            
	            for (int i = 0; i < resultArray.length() - 1; i++) {
	    			title = resultArray.getJSONObject(i).getString("title").toString();
	    			author = resultArray.getJSONObject(i).getString("author").toString();
	    			bookID = resultArray.getJSONObject(i).getString("book").toString();
	    			
	    			Map<String, String> record = new HashMap<String, String>(2);
	    			record.put("title", title);
	            	record.put("author", author);
	    			bookListData.add(record);
	    			
	            	bookData.add(bookID);
	    		}
	            
	            SimpleAdapter adapter = new SimpleAdapter(this, bookListData,
	  	              android.R.layout.simple_list_item_2,
	  	              new String[] {"title", "author"},
	  	              new int[] {android.R.id.text1, android.R.id.text2});
	  			
	  			myList.setAdapter(adapter);
	  			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        if (bookType == BOOKTYPE_MINE) {
        	myList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

                 if (bookData.get(position) != null) {
                	 showBookDetails(bookData.get(position), BOOKTYPE_MINE);                   	 
                 }
                }
            });
        } else if (bookType == BOOKTYPE_OTHER) {
        	myList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

                 if (bookData.get(position) != null) {
                	 showBookDetails(bookData.get(position), BOOKTYPE_OTHER);                   	 
                 }
                }
            });
        }		
    }
    
    private void showBookDetails(String bookID, int bookType) {
    	Bundle bundle = new Bundle();
    	bundle.putString("bookID", bookID);
    	bundle.putInt("bookType", bookType);
    	Intent detailsIntent = new Intent(this.getApplicationContext(), BookDetailsActivity.class);
    	detailsIntent.putExtras(bundle);
    	startActivityForResult(detailsIntent, INTENT_BOOKDETAILS);
    }
    
    protected JSONObject getJSONFromURL(String loadURL) {
    	JSONObject jObject = null;
    	BufferedReader in = null;
        try {
            HttpGet request = new HttpGet();
            request.setURI(new URI(Uri.encode(loadURL, ":/?=")));
            HttpResponse response = httpclient.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            String page = sb.toString();
            
            jObject = new JSONObject(page);
            
	        } catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
	        if (in != null) {
	            try {
	                in.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
        }
        
        return jObject;
    }
    
    protected Document getJSoupFromURL(String loadURL) {
    	Document jSoupDoc = null;
    	BufferedReader in = null;
        try {
            HttpGet request = new HttpGet();
            request.setURI(new URI(Uri.encode(loadURL, ":/?=")));
            HttpResponse response = httpclient.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            String page = sb.toString();
                        
            jSoupDoc = Jsoup.parse(page);
            
        } catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return jSoupDoc;
    }
}