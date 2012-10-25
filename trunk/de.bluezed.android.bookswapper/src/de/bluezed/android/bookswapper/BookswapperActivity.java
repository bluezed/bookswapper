package de.bluezed.android.bookswapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.google.api.services.books.model.VolumeVolumeInfoImageLinks;
import com.google.api.services.books.model.VolumeVolumeInfoIndustryIdentifiers;
import com.google.api.services.books.model.Volumes;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBar;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BookswapperActivity extends FragmentActivity implements ActionBar.TabListener {
    
	private static final int DIALOG_LOGIN_DATA 	= 1000;
	private static final int DIALOG_ABOUT 		= 2000;
	private static final int DIALOG_TOKENS		= 3000;
	protected static final int DIALOG_FEEDBACK	= 4000;
	
	private static final int INTENT_BOOKDETAILS 	= 1000;
	protected static final int INTENT_BOOKEDIT 		= 2000;
	protected static final int INTENT_SWAPDETAILS	= 3000;
	
	protected static final int BOOKTYPE_MINE	= 1;
	protected static final int BOOKTYPE_OTHER	= 0;
	
	protected static final int RETURN_DELETE	= 0;
	protected static final int RETURN_SWAP		= 1;
	protected static final int RETURN_USERBOOKS = 2;
	
	public static final int BOOK_IN				= 0;
	public static final int BOOK_OUT			= 1;
	
	public static final int BOOK_NOT_SHIPPED	= 0;
	public static final int BOOK_SHIPPED		= 1;
	
	public static final int BOOK_LISTED			= 0;
	public static final int BOOK_READING		= -1;
	public static final int BOOK_HOLIDAY		= -2;
	public static final int BOOK_NOSTATUS		= 99;
	
	private static final String KEY 			= "AIzaSyCjHNFXZvQTkyBNLvW_VbP_sJ0bChpLZVU";
	
	protected static final String BASE_URL		= "http://www.bookswapper.de";
	private static final String MYID_URL 		= BASE_URL + "/api/my";
	private static final String ADDBOOK_URL 	= BASE_URL + "/swap/addbook.php?action=add";
	protected static final String EDITBOOK_URL 	= BASE_URL + "/swap/addbook.php?action=edit";
	protected static final String RELISTBOOK_URL= BASE_URL + "/swap/addbook.php?action=restore";
	private static final String MYBOOKS_URL		= BASE_URL + "/api/mybooks";
	private static final String SEARCH_URL		= BASE_URL + "/api/search/";
	private static final String USER_BOOKS_URL	= BASE_URL + "/api/user/";
	protected static final String BOOK_URL		= BASE_URL + "/api/book/";
	protected static final String DELETE_URL 	= BASE_URL + "/api/delbook/";
	protected static final String CATS_URL 		= BASE_URL + "/api/cats";
	private static final String SIGNUP_URL		= BASE_URL + "/swap/registration.php?action=signup";
	protected static final String SWAP_URL		= BASE_URL + "/api/order/";
	private static final String MYSWAPS_URL		= BASE_URL + "/api/myswaps";
	private static final String LOGOUT_URL		= BASE_URL + "/swap/logout.php";
	protected static final String SHIPPED_URL	= BASE_URL + "/api/shipped";
	protected static final String RECEIVED_URL	= BASE_URL + "/api/gotit";
	protected static final String NEWBOOKS_URL	= BASE_URL + "/api/new";
	
	protected String userID 		= "";
	protected String userName 		= "";
	private String uploadImageLink 	= "";
	protected String app_ver		= "";
	private String hits				= "";
	private String query			= "";
	private Volumes volumes			= null;
	private CountDownLatch latch	= null;
	private boolean freshStart		= true;
	
	protected java.util.List<Map<String,String>> categoryList	= new ArrayList<Map<String,String>>();
	protected java.util.List<Book> bookListData 				= new ArrayList<Book>();
	protected java.util.List<Swap> swapListData 				= new ArrayList<Swap>();
	
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
	protected EditText textSearch;
	
	protected SharedPreferences preferences;
	private DefaultHttpClient httpclient 	= new DefaultHttpClient();
	protected CookieStore cookies			= null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        final ActionBar ab = getSupportActionBar();
        
        // set defaults for logo & home up
 		ab.setDisplayHomeAsUpEnabled(false);
 		ab.setDisplayUseLogoEnabled(false); 		
 		
        // set up tabs nav
		ab.addTab(ab.newTab().setText(this.getString(R.string.search_books)).setTabListener(this));		
		ab.addTab(ab.newTab().setText(this.getString(R.string.my_books)).setTabListener(this));
		ab.addTab(ab.newTab().setText(this.getString(R.string.my_swaps)).setTabListener(this));
		ab.addTab(ab.newTab().setText(this.getString(R.string.add_book)).setTabListener(this));
		
		setContentView(R.layout.main);
		
 		// default to tab navigation
		if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
        		
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        try
        {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (NameNotFoundException e)
        {
           // not found
        }
        
        httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, this.getString(R.string.app_name_internal) + app_ver);
        
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
        } else {
        	userName = preferences.getString("username", "");
        }
         
        getAllCats();
    }
    
    protected void getAllCats() {
    	if (!checkNetworkStatus()) {
    		return;
    	}
    	
    	latch = new CountDownLatch(1);
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		   }
		};
		Thread checkUpdate = new Thread() {  
		   public void run() {
			  loadCats();
		      handler.sendEmptyMessage(0);
		      latch.countDown();
		   }
		};
		checkUpdate.start();		
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void loadCats() {
//     {"cats":[{"maincat":"fiction","catid":"2","catname":"crime & mystery"},{"maincat":"fiction","catid":"16","catname":"horror"},{"maincat":"fiction","catid":"3","catname":"romance"},{"maincat":"fiction","catid":"14","catname":"historical romance"},{"maincat":"fiction","catid":"10","catname":"humour"},{"maincat":"fiction","catid":"4","catname":"sci-fi & fantasy"},{"maincat":"fiction","catid":"5","catname":"chick lit"},{"maincat":"fiction","catid":"13","catname":"children's"},{"maincat":"fiction","catid":"15","catname":"historical fiction"},{"maincat":"fiction","catid":"1","catname":"novels general"},{"maincat":"non-fiction","catid":"12","catname":"history & politics"},{"maincat":"non-fiction","catid":"11","catname":"mind & body"},{"maincat":"non-fiction","catid":"6","catname":"memoirs & biographies"},{"maincat":"non-fiction","catid":"8","catname":"travel books"},{"maincat":"non-fiction","catid":"9","catname":"other non-fiction"},],"complete":"true"}
    
    	// Get all the categories
    	if (categoryList.size() == 0) {
        	JSONObject jObject = getJSONFromURL(CATS_URL, false, httpclient, cookies);
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
    
	 @Override
	 protected Dialog onCreateDialog(int id) {
		 switch (id) {
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
		 final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
			final Handler handler = new Handler() {
			   public void handleMessage(Message msg) {
				   dialog.dismiss();
				   showTokenMessage(msg.arg1);
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
	 
	 private void showTokenMessage(int tokens) {
		 showAlert(this.getString(R.string.show_tokens), this.getString(R.string.token_amount) + " " + String.valueOf(tokens), this.getString(R.string.ok));
	 }
	 
	 protected int getTokenNumber() {
		 int token = 0;
		 if (checkLoggedIn()) {
			JSONObject jObject = getJSONFromURL(MYID_URL, true, httpclient, cookies);
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
         	final String user = input1.getText().toString();
         	final String pass = input2.getText().toString();
         	
         	Editor edit = preferences.edit();
            edit.putString("username", user);
         	edit.putString("password", pass);
         	edit.putBoolean("rememberPassword", checkBox.isChecked());
         	edit.commit();
         	
         	latch = new CountDownLatch(1);
         	if (cookies != null) {
	    		final Handler handler = new Handler() {
	    		   public void handleMessage(Message msg) {
	    			   doLogin(user, pass);
	    		   }
	    		};
	    		Thread checkUpdate = new Thread() {  
	    		   public void run() {
	    			  doLogout();
	    			  cookies = null;
	    		      handler.sendEmptyMessage(0);
	    		      latch.countDown();
	    		   }
	    		};
	    		checkUpdate.start();
	    		try {
 				   latch.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
         	} else {
         		doLogin(user, pass);
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
	 
	 private void doLogout() {
         try {
        	 HttpGet request = new HttpGet();
        	 request.setURI(new URI(LOGOUT_URL));
	       	 httpclient.setCookieStore((CookieStore) cookies);        
	         httpclient.execute(request);
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
	
	@Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar().getSelectedTab().getPosition() == 1) {
        	loadMyBooks();
		}
    }

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		getSupportActionBar().show();
		switch (requestCode) {
	    	case INTENT_BOOKDETAILS:
	    		if (intent != null) {	    			
		    		Bundle bundle = intent.getExtras();
		    		int option = -1;
		    		option = bundle.getInt("option");
		    		if (option > -1) {
		    			switch (option) {
			    			case RETURN_DELETE:
			    				loadMyBooks();
			    				Toast.makeText(this, bundle.getString("state") + "\n" + bundle.getString("message"), Toast.LENGTH_LONG).show();
			    				break;
			    			case RETURN_SWAP:
			    				loadOtherBooks(BOOKTYPE_OTHER, "");
			    				showAlert(this.getString(R.string.info), bundle.getString("state") + "\n" + bundle.getString("message"), this.getString(R.string.ok));
			    				break;
			    			case RETURN_USERBOOKS:
			    				loadOtherBooks(BOOKTYPE_OTHER, bundle.getString("ownerID"));
			    				break;
		    			}
		    		}
	    		}
	    		break;
	    		
	    	case INTENT_SWAPDETAILS:
	    		setContentView(R.layout.my_swaps);
				if (checkLoggedIn()) {
					loadMySwaps();
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
        
        getAllCats();
        
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
	
	private void loadSearch() {
		 textSearch = (EditText) findViewById(R.id.editTextSearch);
		 loadOtherBooks(BOOKTYPE_OTHER, "NEWBOOK");
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
    		if (checkLoggedIn()) {
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
		
		if (cookies != null && userID.length() > 0) {
			return true;
		} else {
			try {
				if (user.length() > 0 && pass.length() > 0) {
					latch = new CountDownLatch(1);
					doLogin(user, pass);
					latch.await();
				} else {			
					showDialog(DIALOG_LOGIN_DATA);
				}
				
				if (cookies != null && userID.length() > 0) {
					return true;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return false;
    }
    
    private void doLogin (final String user, final String pass) {	
    	userName = user;
    	
    	if (!checkNetworkStatus()) {
    		return;
    	}
    	
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
 		final Handler handler = new Handler() {
 		   public void handleMessage(Message msg) {
 		      dialog.dismiss();
 		      if (!(Boolean)msg.obj) {
 		    	  showLoginError();
 		      }
 		   }
 		};
 		Thread checkUpdate = new Thread() {  
 		   public void run() {
 			  Message message = handler.obtainMessage(1, logIn(user, pass));
              handler.sendMessage(message);
              latch.countDown();
 		   }
 		};
 		checkUpdate.start();
    }
    
    private void showLoginError() {
    	showAlert(this.getString(R.string.warning), this.getString(R.string.login_error), this.getString(R.string.ok));
    }
    
    private boolean logIn(String user, String pass) {
    	boolean loggedIn = false;
    	
    	HttpPost httpost = new HttpPost(MYID_URL);

    	java.util.List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("nick", user));
    	nvps.add(new BasicNameValuePair("pass", pass));

    	try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps));

	    	HttpResponse response = httpclient.execute(httpost);
	    	BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            String page = sb.toString();
            
            JSONObject jObject = new JSONObject(page);
            
            if (jObject != null) {
//    			{myid:id||not logged in}
				userID = jObject.getString("myid").toString();
				if (isNumeric(userID)) {
					cookies = httpclient.getCookieStore();
					loggedIn = true;
				} else {
					userID = "";
				}
            }
            
    	} catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return loggedIn;
    }
    
    public boolean isNumeric(String s) {  
        return java.util.regex.Pattern.matches("\\d+", s);  
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
    
    private void queryGoogleBooks(final JsonFactory jsonFactory, final String query) throws Exception {
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		      loadFromGoogle(volumes);
		      }
		   };
		Thread checkUpdate = new Thread() {  
		   public void run() {
			   // Set up Books client.
			   if (volumes != null) {
				   volumes.clear();
			   }
		    	final Books books = Books.builder(new NetHttpTransport(), jsonFactory)
				    .setApplicationName(BookswapperActivity.this.getString(R.string.app_name_internal) + app_ver)
				    .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
			          public void initialize(JsonHttpRequest request) {
		                BooksRequest booksRequest = (BooksRequest) request;
		                booksRequest.setKey(KEY);
		              }
		            })
		            .build();

		        List volumesList;
				try {
					volumesList = books.volumes().list(query);
			        // Execute the query.
			        volumes = volumesList.execute();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      handler.sendEmptyMessage(0);
		      }
		   };
		checkUpdate.start();
    }
    
    private void loadFromGoogle(Volumes volumes) {        
        String isbn			= "";
        String title 		= "";
        String author 		= "";
        String publisher 	= "";
        String summary 		= "";
        String published	= "";
        String pages 		= "";
        String imageLink 	= "";
        
        if (volumes == null || volumes.getTotalItems() == 0 || volumes.getItems() == null) {
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
        	  VolumeVolumeInfoImageLinks imageLinks = volumeInfo.getImageLinks();

        	  if (imageLinks.getThumbnail() != null && imageLinks.getThumbnail().length() > 0) {
        		  imageLink = imageLinks.getThumbnail();
        		  uploadImageLink = imageLinks.getThumbnail();
        	  } 
        	  
        	  if (imageLinks.getExtraLarge() != null && imageLinks.getExtraLarge().length() > 0) {
        		  uploadImageLink = imageLinks.getExtraLarge();
        	  } else if (imageLinks.getLarge() != null && imageLinks.getLarge().length() > 0) {
        		  uploadImageLink = imageLinks.getLarge();
        	  } else if (imageLinks.getMedium() != null && imageLinks.getMedium().length() > 0) {
        		  uploadImageLink = imageLinks.getMedium();
        	  } else if (imageLinks.getSmall() != null && imageLinks.getSmall().length() > 0) {
        		  uploadImageLink = imageLinks.getSmall();
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
	    
        if (imageLink.length() > 0) {
	        DrawableManager drawableList = new DrawableManager();
			drawableList.fetchDrawableOnThread(imageLink, imageCover);
        }
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 5, 0, 0);
        imageGoogle.setLayoutParams(lp); 
    }
            
    private void addBook() {
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
			   dialog.dismiss();
			   boolean result = msg.arg1 == 1 ? true : false;
			   showAddResult(result);
		   }
		};
		Thread checkUpdate = new Thread() {  
		   public void run() {
			  Message msg1 = Message.obtain();
			  msg1.arg1 = doSubmit();
		      handler.sendMessage(msg1);
		   }
		};
		checkUpdate.start();
    }
    
    private void showAddResult(boolean result) {
    	if (result) {
			   showAlert(this.getString(R.string.success), this.getString(R.string.book_added), this.getString(R.string.ok));
			   clearFields(true);
		   } else {
			   showAlert(this.getString(R.string.warning), this.getString(R.string.book_not_added), this.getString(R.string.ok));
		   }
    }
    
    private int doSubmit() {
    	int result = 0;
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
			httpclient.setCookieStore(cookies);
	    	HttpResponse response = httpclient.execute(httpost);
	    	HttpEntity entity = response.getEntity();
	    	
	    	String responseBody = EntityUtils.toString(entity);
	    	
	    	if (responseBody.contains(textTitle.getText().toString()) && responseBody.contains("swappable books")) {
	    		result = 1;
	    	} else {
	    		result = 0;
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
    
    private void loadMyBooks() {
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		      fillList(BOOKTYPE_MINE, "");
		      }
		   };
		Thread checkUpdate = new Thread() {  
		   public void run() {
			  populateBookList(BOOKTYPE_MINE, "");
		      handler.sendEmptyMessage(0);
		      }
		   };
		checkUpdate.start();
    	
    }
    
    private void loadOtherBooks(int bookType, final String ownerID) {
    	if (!checkNetworkStatus()) {
    		return;
    	}
    	
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		      fillList(BOOKTYPE_OTHER, ownerID);
		   }
		};
		Thread checkUpdate = new Thread() {  
		   public void run() {
			  populateBookList(BOOKTYPE_OTHER, ownerID);
		      handler.sendEmptyMessage(0);
		   }
		};
		checkUpdate.start();
    }
    
    public void onSearchClick (View view) {
    	if (!checkNetworkStatus()) {
    		return;
    	}
    	
    	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		      fillList(BOOKTYPE_OTHER, "");
		      }
		   };
		Thread checkUpdate = new Thread() {  
		   public void run() {
			  populateBookList(BOOKTYPE_OTHER, "");
		      handler.sendEmptyMessage(0);
		      }
		   };
		checkUpdate.start();
    }
    
    private void populateBookList(int bookType, String ownerID) {
    	JSONObject jObject = null;
    	String strURL = "";
    	if (bookType == BOOKTYPE_MINE) {
    		if (!checkLoggedIn()) {
    			return;
    		}
    		myList = (ListView) findViewById(R.id.listMyBooks);
    	   	strURL = MYBOOKS_URL;
    	   	jObject = getJSONFromURL(strURL, true, httpclient, cookies);
    	} else if (bookType == BOOKTYPE_OTHER) {
    		myList = (ListView) findViewById(R.id.listViewSearchResult);
    		if (ownerID.length() > 0) {
    			if (ownerID.equals("NEWBOOK")) {
    				// show new books
    				strURL = NEWBOOKS_URL;
    			} else {
    				// user books
    				strURL = USER_BOOKS_URL + ownerID;
    			}
    		} else {
    			// search books
    			String searchText = textSearch.getText().toString();
    			
    			if (searchText.length() > 0) {
    				strURL = SEARCH_URL + searchText;
    			} else {
    				strURL = NEWBOOKS_URL;
    			}    			
    		}
			jObject = getJSONFromURL(strURL, false, httpclient, cookies);
    	} else {
    		return;
    	}
    	    	
    	// Populate my books list
        bookListData.clear();
        query = "";
        hits = "";
        
        if (jObject != null) {
			try {
//				My_Books: {"results":[{"book":"12345","title":"XYZ","author":"XYZ","isbn":"12345678","status":"0"}],"query":"xyz","hits":"123"}
//					status: [0=swappable],[-1 = currently reading],[-2 = on holiday]
				
//				Book_Search: {"results":[{"book":"12345","title":"XYZ","author":"XYZ","isbn":" ","description":"XYZ"}],"query":"xyz","hits":"123"}
			
				hits = jObject.getString("hits").toString();
            
	            if (hits.equals("not logged in")) {
	            	Toast.makeText(this, this.getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show();
	            	return;
	            }
	            
	            query = jObject.getString("query").toString();
	            
	            JSONArray resultArray = jObject.getJSONArray("results");
	                            
	            String title = "";
	            String author = "";
	            String bookID = "";
	            String bookLink = "";
	            int status = 0;
	            
	            for (int i = 0; i < resultArray.length() - 1; i++) {
	    			title = resultArray.getJSONObject(i).getString("title").toString();
	    			author = resultArray.getJSONObject(i).getString("author").toString();
	    			bookID = resultArray.getJSONObject(i).getString("book").toString();
	    				    			
	    			if (bookType == BOOKTYPE_MINE) {
	    				status = resultArray.getJSONObject(i).getInt("status");
	    				switch (status) {
	    					case BOOK_READING:
	    						title = "[" + this.getString(R.string.currenty_reading) + "]\n" + title;
	    						break;
		    				case BOOK_HOLIDAY:
		    					title = "[" + this.getString(R.string.on_holiday) + "]\n" + title;
		    					break;
	    				}
	    			}
	    			
	    			bookLink = BASE_URL + "/bigbookimg/" + bookID + ".jpg";
	    			
	    			// Construct Book object
	    			Book book = new Book(bookID, title, author, bookLink, status);
	    			
	    			bookListData.add(book);
	    		}
	            
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    }	
			
     private void fillList(int bookType, final String ownerID) {
		// Create a customized ArrayAdapter
        BookListArrayAdapter adapter = new BookListArrayAdapter(
        		getApplicationContext(), R.layout.booklist_listview, bookListData);

       	myList.setAdapter(adapter);
  		
        if (bookType == BOOKTYPE_MINE) {
        	String message = String.format(this.getString(R.string.current_books), getCountBookStatus(bookListData, BOOK_LISTED), getCountBookStatus(bookListData, BOOK_READING), getCountBookStatus(bookListData, BOOK_HOLIDAY));
  			Toast.makeText(this, message, Toast.LENGTH_LONG).show(); 

        	myList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

                 if (bookListData.get(position) != null) {
                	 showBookDetails(bookListData.get(position).bookID, BOOKTYPE_MINE, bookListData.get(position).status);                   	 
                 }
                }
            });
        } else if (bookType == BOOKTYPE_OTHER) {
        	String message = "";
        	if (ownerID.length() > 0) {
        		message = String.format(this.getString(R.string.user_books_message), hits, query);
        	} else {
	        	message = String.format(this.getString(R.string.query_result), query, hits);
        	}
        	if (Integer.valueOf(hits) > 50) {
  				message = message + this.getString(R.string.showing_first).toString();
  			}
  			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();        	

        	myList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

                 if (bookListData.get(position) != null) {
                	 showBookDetails(bookListData.get(position).bookID, BOOKTYPE_OTHER, BOOK_NOSTATUS);                   	 
                 }
                }
            });
        }		
    }
     
     protected int getCountBookStatus(java.util.List<Book> bookList, int status) {
  		int count = 0; 		
  		for (Book book : bookList) {
  			if (book.status == status) {
  				count++;
  			}
  		} 		
  		return count;
  	}

     private void loadMySwaps() {
     	final ProgressDialog dialog = ProgressDialog.show(this, this.getString(R.string.loading), this.getString(R.string.please_wait), true);
 		final Handler handler = new Handler() {
 		   public void handleMessage(Message msg) {
 		      dialog.dismiss();
 		      fillSwapList();
 		      }
 		   };
 		Thread checkUpdate = new Thread() {  
 		   public void run() {
 			  populateSwapList();
 		      handler.sendEmptyMessage(0);
 		      }
 		   };
 		checkUpdate.start();
     	
     }
    
     private void populateSwapList() {
    	 swapListData.clear();
    	 JSONObject jObject = getJSONFromURL(MYSWAPS_URL, true, httpclient, cookies);
 		 if (jObject != null) {
 			try {
// 				{"swapout":[{"hits":"1","query":"swapout"},{"nr":"99","prename":"XYZ","postal":"12345","postname":"XYZ","uname":"XYZ","street":"XYZ","shippedon":"2012-04-03 18:33:18","city":"XYZ","author":"XYZ","title":"XYZ","orderedon":"2012-04-03 14:37:33","order":"1234","shipped":"1","book":"12345","user":"123"},null],"swapin":[{"hits":"0","query":"swapin"},{},null]}
// 				shipped: 0=no, 1=yes
 				
 				JSONObject jSwaps = jObject.getJSONObject("swaps");
 				
 				JSONArray resultArrayOut 	= jSwaps.getJSONArray("swapout");
 				JSONArray resultArrayIn 	= jSwaps.getJSONArray("swapin");
 				
 				String orderID		= "";
 				String bookID		= "";
 				String userID		= "";
	            String title 		= "";
	            String author 		= "";
	            int status			= -1;
	            String user 		= "";
	            int hitsOut 		= 0;
	            int hitsIn 			= 0;
	            int type			= -1;
	            String shippedOn 	= "";
	            String orderedOn 	= "";
	            Date date 			= null;
	            String firstname	= "";
	            String lastname		= "";
	            String street		= "";
	            String postcode		= "";
	            String city			= "";
	            
	            // Incoming books
	            for (int i = 0; i < resultArrayIn.length() - 1; i++) {
	            	if (i == 0) {
	    				hitsIn = resultArrayIn.getJSONObject(i).getInt("hits");
	    			} else if (i <= hitsIn) {
	    				type	= BOOK_IN;
	    				orderID = resultArrayIn.getJSONObject(i).getString("order").toString();
	    				bookID 	= resultArrayIn.getJSONObject(i).getString("book").toString();
	    				userID 	= resultArrayIn.getJSONObject(i).getString("user").toString();
	    				title 	= resultArrayIn.getJSONObject(i).getString("title").toString();
	    				author 	= resultArrayIn.getJSONObject(i).getString("author").toString();
	    				status 	= resultArrayIn.getJSONObject(i).getInt("shipped");
	    				user 	= resultArrayIn.getJSONObject(i).getString("uname").toString();
	    				
	    				date 		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultArrayIn.getJSONObject(i).getString("orderedon").toString());
	            		orderedOn 	= new SimpleDateFormat("dd/MM/yyyy").format(date);
	            		date 		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultArrayIn.getJSONObject(i).getString("shippedon").toString());
	            		shippedOn 	= new SimpleDateFormat("dd/MM/yyyy").format(date);  
	    				
	            		Swap swap = new Swap(orderID, bookID, type, title, author, status, userID, user, orderedOn, shippedOn, firstname, lastname, street, postcode, city);
		    			
		    			swapListData.add(swap);
	    			}
	            }
	            
	            // Outgoing books
	            for (int i = 0; i < resultArrayOut.length() - 1; i++) {
	    			if (i == 0) {
	    				hitsOut = resultArrayOut.getJSONObject(i).getInt("hits");
	    			} else if (i <= hitsOut) {
	    				type		= BOOK_OUT;
	    				orderID 	= resultArrayOut.getJSONObject(i).getString("order").toString();
	    				bookID 		= resultArrayOut.getJSONObject(i).getString("book").toString();
	    				userID 		= resultArrayOut.getJSONObject(i).getString("user").toString();
	    				title 		= resultArrayOut.getJSONObject(i).getString("title").toString();
	    				author 		= resultArrayOut.getJSONObject(i).getString("author").toString();
	    				status 		= resultArrayOut.getJSONObject(i).getInt("shipped");
	    				user 		= resultArrayOut.getJSONObject(i).getString("uname").toString();
	    				firstname 	= resultArrayOut.getJSONObject(i).getString("prename").toString();
	    				lastname 	= resultArrayOut.getJSONObject(i).getString("postname").toString();
	    				street 		= resultArrayOut.getJSONObject(i).getString("street").toString() + " " + resultArrayOut.getJSONObject(i).getString("nr").toString();
	    				postcode 	= resultArrayOut.getJSONObject(i).getString("postal").toString();
	    				city 		= resultArrayOut.getJSONObject(i).getString("city").toString();
	    				
	    				date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultArrayOut.getJSONObject(i).getString("orderedon").toString());
	            		orderedOn = new SimpleDateFormat("dd/MM/yyyy").format(date);
	            		date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultArrayOut.getJSONObject(i).getString("shippedon").toString());
	            		shippedOn = new SimpleDateFormat("dd/MM/yyyy").format(date);  
	    				
	            		Swap swap = new Swap(orderID, bookID, type, title, author, status, userID, user, orderedOn, shippedOn, firstname, lastname, street, postcode, city);
		    			
		    			swapListData.add(swap);
	    			}
	            }
	            
 			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		 }
     }
     
     private void fillSwapList() {
    	// Create a customized ArrayAdapter
        SwapListArrayAdapter adapter = new SwapListArrayAdapter(
         		getApplicationContext(), R.layout.booklist_listview, swapListData);
 		
        myList = (ListView) findViewById(R.id.listMySwaps);
 		myList.setAdapter(adapter);
 		
 		String message = String.format(this.getString(R.string.swap_message), getCountSwapType(swapListData, BOOK_IN), getCountSwapType(swapListData, BOOK_OUT));
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		
		myList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {

             if (swapListData.get(position) != null) {
            	 showSwapDetails(swapListData.get(position));                   	 
             }
            }
        });
     }
     
 	
 	protected int getCountSwapType(java.util.List<Swap> swapList, int type) {
 		int count = 0; 		
 		for (Swap swap : swapList) {
 			if (swap.type == type) {
 				count++;
 			}
 		} 		
 		return count;
 	}
 	     
    private void showBookDetails(String bookID, int bookType, int status) {
    	Bundle bundle = new Bundle();
    	bundle.putString("bookID", bookID);
    	bundle.putInt("bookType", bookType);
    	bundle.putInt("bookStatus", status);
    	Intent detailsIntent = new Intent(this.getApplicationContext(), BookDetailsActivity.class);
    	detailsIntent.putExtras(bundle);
    	startActivityForResult(detailsIntent, INTENT_BOOKDETAILS);
    }
    
    private void showSwapDetails(Swap order) {
    	Intent detailsIntent = new Intent(this.getApplicationContext(), SwapDetailsActivity.class);
    	detailsIntent.putExtra("order", order);
    	startActivityForResult(detailsIntent, INTENT_SWAPDETAILS);
    }
    
    protected JSONObject getJSONFromURL(String loadURL, boolean needsLogin, DefaultHttpClient client, CookieStore cookieStore) {
    	JSONObject jObject = null;
    	BufferedReader in = null;
        try {
            HttpGet request = new HttpGet();
            request.setURI(new URI(Uri.encode(loadURL, ":/?=")));
            
            if (needsLogin) {
            	client.setCookieStore(cookieStore);
            }
            
            HttpResponse response = client.execute(request);
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

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		if (getCurrentFocus() == this.findViewById(R.layout.main)) {
			onTabSelected(tab, ft);	
		}
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		switch (tab.getPosition()) {
			case 0:
				if (!freshStart) {
					setContentView(R.layout.search);
					loadSearch();
				}
				freshStart = false;
				break;
			case 1:
				setContentView(R.layout.my_books);
				if (checkLoggedIn()) {
					loadMyBooks();
				}
				break;
			case 2:
				setContentView(R.layout.my_swaps);
				if (checkLoggedIn()) {
					loadMySwaps();
				}
				break;
			case 3:
				setContentView(R.layout.add_book);
				loadAddBook();
				break;
		}
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}