package de.bluezed.android.bookswapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
	private static final String KEY 			= "AIzaSyCjHNFXZvQTkyBNLvW_VbP_sJ0bChpLZVU";
	private static final String BASE_URL		= "http://www.bookswapper.de";
	private static final String LOGIN_URL 		= BASE_URL + "/swap/login.php";
	private static final String RESTRICTED_URL 	= BASE_URL + "/swap/member.php?action=add";
	private static final String ADDBOOK_URL 	= BASE_URL + "/swap/addbook.php?action=add";
	private static final String MYBOOKS_URL		= BASE_URL + "/swap/member.php?action=mybooks";
	
	private boolean loggedIn 		= false;
	private String userID 			= "";
	private Bitmap coverImage 		= null;
	private String uploadImageLink 	= "";
	private String app_ver			= "";
	
	private EditText textISBN;
	private EditText textTitle;
	private EditText textAuthor;
	private EditText textPublisher;
	private EditText textSummary;
	private EditText textPublished;
	private EditText textPages;
	private EditText textTags;
	private ImageView imageCover;
	private ImageView imageGoogle;
	private Spinner spinnerCat;
	private Spinner spinnerCon;
	private ListView myList;
	
	java.util.List<String> bookData = new ArrayList<String>();
	
	SharedPreferences preferences;
	DefaultHttpClient httpclient = new DefaultHttpClient();
	
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
        
        checkNetworkStatus();
    }	
	
	 @Override
	 protected Dialog onCreateDialog(int id) {
		 switch (id) {
	        case DIALOG_LOGIN:
	        	if (!loggedIn || userID.length() == 0) {
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
		            checkBox.setChecked(true);
		            
		            alert.setPositiveButton(this.getString(R.string.login), new DialogInterface.OnClickListener() { 
		            public void onClick(DialogInterface dialog, int whichButton) { 
		            	String user = input1.getText().toString();
		            	String pass = input2.getText().toString();
		            	
		            	Editor edit = preferences.edit();
		            	if (checkBox.isChecked()) {
	                    	edit.putString("username", user);
	                		edit.putString("password", pass);
	                    } else {
	                    	edit.putString("username", "");
	                		edit.putString("password", "");
	                    }
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
	        	break;
	        	
	        case DIALOG_LOGIN_DATA:
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
	            checkBox.setChecked(true);
	            
	            alert.setPositiveButton(this.getString(R.string.login), new DialogInterface.OnClickListener() { 
	            public void onClick(DialogInterface dialog, int whichButton) { 
	            	String user = input1.getText().toString();
	            	String pass = input2.getText().toString();
	            	
	            	Editor edit = preferences.edit();
	            	if (checkBox.isChecked()) {
                    	edit.putString("username", user);
                		edit.putString("password", pass);
                    } else {
                    	edit.putString("username", "");
                		edit.putString("password", "");
                    }
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
			loadMyBooks();
			break;
		case R.id.searchBooks:
			setContentView(R.layout.search);
			break;
		case R.id.addBook:
			setContentView(R.layout.add_book);
			loadAddBook();
			break;
		case android.R.id.home:
			setContentView(R.layout.main);
			break;
		}
		return true;
	}
	
	private void loadAddBook() {
		textISBN  		= (EditText) findViewById(R.id.editText1);
        textTitle 		= (EditText) findViewById(R.id.editText2);
        textAuthor 		= (EditText) findViewById(R.id.editText3);
        textPublisher 	= (EditText) findViewById(R.id.editText4);
        textSummary 	= (EditText) findViewById(R.id.editText5);
        textPublished 	= (EditText) findViewById(R.id.editText8);
        textPages 		= (EditText) findViewById(R.id.editText7);
        textTags 		= (EditText) findViewById(R.id.editTextTags);
        imageCover		= (ImageView) findViewById(R.id.imageViewCover);
        imageGoogle		= (ImageView) findViewById(R.id.imageViewGoogle);
        spinnerCat 		= (Spinner) findViewById(R.id.spinner1);
        spinnerCon 		= (Spinner) findViewById(R.id.spinner2); 
        
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(adapter1);
        
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.condition_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCon.setAdapter(adapter2);
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
    
    public void onSearchClick (View view) {
    	showAlert("Info", "Feature not implemented yet...\nComing soon!","OK");
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
    
    private boolean checkLoggedIn() {
    	String user = preferences.getString("username", "");
		String pass = preferences.getString("password", "");
		
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
    
    private boolean checkNetworkStatus() {
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
        
        RadioButton paperback = (RadioButton) findViewById(R.id.radio0);
        paperback.setChecked(true);
        
        if (clearISBN) {
        	textISBN.setText("");
        }
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(100, 5, 0, 0);
        imageGoogle.setLayoutParams(lp);
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
    		if (userID.equals("")) {
    			loggedIn = false;
    		}
    	}
    	
    	return;
    }
    
    private void getUserID() {
    	BufferedReader in = null;
        try {
            HttpGet request = new HttpGet();
            request.setURI(new URI(RESTRICTED_URL));
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
            
            String name = "";
            String value = "";
            
            Document doc = Jsoup.parse(page);

            Element content = doc.getElementById("frm_book");
            Elements inputs = content.getElementsByTag("input");
            for (Element input : inputs) {
              name = input.attr("name");
              value = input.attr("value");
              if (name.equals("id") && value.length() > 0 && input.attr("type").equals("hidden")) {
            	  break;
              }
            }

            userID = value;
            
            } catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
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
    }
        
    private void addBook() {
    	int posCon = spinnerCon.getSelectedItemPosition() + 1;
    	int posCat = spinnerCat.getSelectedItemPosition() + 1;
    	if (posCat >= 7) posCat++;
    	
    	RadioButton paperback = (RadioButton) findViewById(R.id.radio0);
    	RadioButton hardcover = (RadioButton) findViewById(R.id.radio1);
    	   	
    	HttpPost httpost = new HttpPost(ADDBOOK_URL);

    	java.util.List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("id", userID));
    	nvps.add(new BasicNameValuePair("title", textTitle.getText().toString()));
    	nvps.add(new BasicNameValuePair("author", textAuthor.getText().toString()));
    	nvps.add(new BasicNameValuePair("verlag", textPublisher.getText().toString()));
    	nvps.add(new BasicNameValuePair("isbn", textISBN.getText().toString()));
    	nvps.add(new BasicNameValuePair("myear", textPublished.getText().toString()));
    	nvps.add(new BasicNameValuePair("spages", textPages.getText().toString()));
    	nvps.add(new BasicNameValuePair("state", String.valueOf(posCon)));
    	nvps.add(new BasicNameValuePair("comment", textSummary.getText().toString()));
    	nvps.add(new BasicNameValuePair("cat", String.valueOf(posCat)));
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
    	myList = (ListView) findViewById(R.id.listMyBooks);
    	
    	if (checkLoggedIn()) {
        	// Populate my books list
    		bookData.clear();
    		java.util.List<Map<String, String>> bookListData = new ArrayList<Map<String, String>>();
    		BufferedReader in = null;
            try {
                HttpGet request = new HttpGet();
                request.setURI(new URI(MYBOOKS_URL));
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
                
                Document doc = Jsoup.parse(page);
                
                String bookText = "";
                String title = "";
                String author = "";
                String bookLink = "";
                
                Elements books = doc.getElementsByClass("ubook");
                for (Element book : books) {               	
                	Map<String, String> record = new HashMap<String, String>(2);
                	bookText = book.text();
                	Elements links = book.getElementsByTag("a");
                	for (Element link : links) { 
      			  		title = link.text();
      			  		bookLink = BASE_URL + link.attr("href");
      			  		break;
                	}
                	author = bookText.substring(title.length());
                	
                	record.put("title", title);
                	record.put("author", author);
                	
                	bookListData.add(record);
         
                	bookData.add(bookLink);
                }
                
                SimpleAdapter adapter = new SimpleAdapter(this, bookListData,
      	              android.R.layout.simple_list_item_2,
      	              new String[] {"title", "author"},
      	              new int[] {android.R.id.text1, android.R.id.text2});
      			
      			myList.setAdapter(adapter);
                
                } catch (URISyntaxException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (ClientProtocolException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IOException e) {
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
                
                myList.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {

                     if (bookData.get(position) != null) {
                    	 showBookDetails(bookData.get(position));                   	 
                     }
                    }
                });

            } 
    	} 
    }
    
    private void showBookDetails(String bookURL) {
    	setContentView(R.layout.book_detail);
    	
    	BufferedReader in = null;
        try {
        	String bookLink = Uri.encode(bookURL, ":/");
            HttpGet request = new HttpGet();
            request.setURI(new URI(bookLink));
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
            
            if (page.contains("<h1>Page not found</h1>")) {
            	showAlert(BookswapperActivity.this.getString(R.string.warning), BookswapperActivity.this.getString(R.string.loading_failed), BookswapperActivity.this.getString(R.string.ok));
            	return;
            }
                        
            Document doc = Jsoup.parse(page);
            
            Element book = doc.getElementById("bigbook");
            
            Elements images = book.getElementsByTag("img");
            for (Element image : images) {  
            	URL newurl = new URL(BASE_URL + image.attr("src")); 
                Bitmap coverPic = BitmapFactory.decodeStream(newurl.openConnection().getInputStream()); 
                ImageView imagePic= (ImageView) findViewById(R.id.imageShowCover);
                imagePic.setImageBitmap(coverPic);
                break;
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
            		if (detail.text().contains("this book is your book")) break;
            		
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
                        
            } catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
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
    }
}