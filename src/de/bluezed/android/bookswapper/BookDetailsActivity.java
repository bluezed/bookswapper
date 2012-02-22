package de.bluezed.android.bookswapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.TextView;


public class BookDetailsActivity extends FragmentActivity {
	
	private static final int BOOKTYPE_MINE	 	= 1;
	private static final int BOOKTYPE_OTHER		= 0;
	
	private static final String BASE_URL 	= "http://www.bookswapper.de";
	private static final String LOGIN_URL 	= BASE_URL + "/swap/login.php";
	private static final String DELETE_URL 	= "/api/delbook/";
	
	private String bookID 	= "";
	private int bookType	= -1;
	
	DefaultHttpClient httpclient = new DefaultHttpClient();
	SharedPreferences preferences;
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_detail);

        Bundle bundle = this.getIntent().getExtras();
        String bookURL = bundle.getString("bookURL");
        bookID = bundle.getString("bookID");
        bookType = bundle.getInt("bookType");
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        BufferedReader in = null;
        try {
            HttpGet request = new HttpGet();
            request.setURI(new URI(bookURL));
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
			
			break;
		}
		return true;
	}
    
    private void deleteBook() {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	if (!logIn()) {
    	        		return;
    	        	}
    	        	
    	        	BufferedReader in = null;
    	            try {
    	            	String delURL = BASE_URL + DELETE_URL + bookID;
    	            	
    	                HttpGet request = new HttpGet();
    	                request.setURI(new URI(delURL));
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
    	                
    	                JSONObject jObject = new JSONObject(page);
    	                //{book:bookid,deletion:success||failure,message:our message for success or failure} 
    	                
    	                String state 	= jObject.getString("deletion").toString();
    	                String message 	= jObject.getString("message").toString();
    	                    	                    	                
    	                Intent mIntent = new Intent();
    	                Bundle bundle = new Bundle();
    	                bundle.putString("option", "delete");
    	                bundle.putString("message", message);
    	                bundle.putString("state", state);
    	                mIntent.putExtras(bundle);
    	                setResult(RESULT_OK, mIntent);
    	                finish();
    	                
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
    	            break;

    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked --> do nothing!
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(this.getString(R.string.sure)).setPositiveButton(this.getString(R.string.yes), dialogClickListener)
    	    .setNegativeButton(this.getString(R.string.no), dialogClickListener).show();

    }
    
    private boolean logIn() {	
    	HttpPost httpost = new HttpPost(LOGIN_URL);
    	
    	String user = preferences.getString("username", "");
    	String pass = preferences.getString("password", "");
    	
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
    		return true;
    	}
    	
    	return false;
    }
}
