package de.bluezed.android.bookswapper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class BookswapperActivity extends Activity {
    
	private EditText textISBN;
	private EditText textTitle;
	private EditText textAuthor;
	private EditText textPublisher;
	private EditText textSummary;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textISBN  		= (EditText) findViewById(R.id.editText1);
        textTitle 		= (EditText) findViewById(R.id.editText2);
        textAuthor 		= (EditText) findViewById(R.id.editText3);
        textPublisher 	= (EditText) findViewById(R.id.editText4);
        textSummary 	= (EditText) findViewById(R.id.editText5);
    }
    
    public void onBarcodeScanClick (View view) {    	
    	IntentIntegrator integrator = new IntentIntegrator(this);
    	integrator.initiateScan();
    };
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		if (scanResult != null) {
            String contents = scanResult.getContents();
            // Handle successful scan
            textISBN.setText(contents);
            
            // Get details
            URL url = null;
			try {
				url = new URL(R.string.isbndb_url + "access_key=" + R.string.isbndb_license + "&index1=isbn&value1=" + contents + "&results=texts");
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
            HttpURLConnection urlConnection = null;
            String response = null;
            
			try {
				urlConnection = (HttpURLConnection) url.openConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                response = readStream(in);
            }
            catch (Exception e) {
				textTitle.setText(e.getMessage());
            }
             finally {
            	urlConnection.disconnect();

                /* Get a SAXParser from the SAXPArserFactory. */
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = null;
				try {
					sp = spf.newSAXParser();
				} catch (ParserConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

                /* Get the XMLReader of the SAXParser we created. */
                XMLReader xr = null;
				try {
					xr = sp.getXMLReader();
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                /* Create a new ContentHandler and apply it to the XML-Reader*/ 
                ExampleHandler myExampleHandler = new ExampleHandler();
                xr.setContentHandler(myExampleHandler);

                /* Parse the xml-data from our URL. */
                InputSource inputSource = new InputSource();
                inputSource.setEncoding("UTF-8");
				inputSource.setCharacterStream(new StringReader(response));

                /* Parse the xml-data from our URL. */
                try {
					xr.parse(inputSource);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                /* Parsing has finished. */

                /* Our ExampleHandler now provides the parsed data to us. */
                ParsedExampleDataSet parsedExampleDataSet = myExampleHandler.getParsedData();

                /* Set the result to be displayed in our GUI. */
                String title = parsedExampleDataSet.toString("title");
                
                if (title == null || title == "") {
                	title = ">> No book data found! <<";                	
                }
                	
                textTitle.setText(title);
                textAuthor.setText(parsedExampleDataSet.toString("author"));
                textPublisher.setText(parsedExampleDataSet.toString("publisher"));
                textSummary.setText(parsedExampleDataSet.toString("summary"));

        	}       
        } 
 		else {
            // Handle cancel
        	textISBN.setText("Error scanning!");
        }
    }
    
    public static String readStream(InputStream in) throws IOException {
          StringBuilder sb = new StringBuilder();
          BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);

          for (String line = r.readLine(); line != null; line = r.readLine()) {
        	  sb.append(line).append("\n");
          }

          in.close();

          return sb.toString();
    }
    
    public class ExampleHandler extends DefaultHandler {

        // ===========================================================
        // Fields
        // ===========================================================

        private boolean in_title = false;
        private boolean in_author = false;
        private boolean in_publisher = false;
        private boolean in_summary = false;

        private ParsedExampleDataSet myParsedExampleDataSet = new ParsedExampleDataSet();

        // ===========================================================
        // Getter & Setter
        // ===========================================================

        public ParsedExampleDataSet getParsedData() {
            return this.myParsedExampleDataSet;
        }

        // ===========================================================
        // Methods
        // ===========================================================
        @Override
        public void startDocument() throws SAXException {
            this.myParsedExampleDataSet = new ParsedExampleDataSet();
        }

        @Override
        public void endDocument() throws SAXException {
            // Nothing to do
        }

        /** Gets be called on opening tags like: 
         * <tag> 
         * Can provide attribute(s), when xml was like:
         * <tag attribute="attributeValue">*/
        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) throws SAXException {
            super.startElement(uri, localName, qName, atts);
            if (localName.equals("Title")) {
                this.in_title = true;
            }
            else if (localName.equals("AuthorsText")) {
                this.in_author = true;
            }
            else if (localName.equals("PublisherText")) {
                this.in_publisher = true;
            }
            else if (localName.equals("Summary")) {
                this.in_summary = true;
            }

        } 


        /** Gets be called on closing tags like: 
         * </tag> */
        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
        	if (localName.equals("Title")) {
                this.in_title = false;
            }
            else if (localName.equals("AuthorsText")) {
                this.in_author = false;
            }
            else if (localName.equals("PublisherText")) {
                this.in_publisher = false;
            }
            else if (localName.equals("Summary")) {
                this.in_summary = false;
            }
        }       


        /** Gets be called on the following structure: 
         * <tag>characters</tag> */
        @Override
        public void characters(char ch[], int start, int length) {
            if(this.in_title) {
                myParsedExampleDataSet.setExtractedTitle(new String(ch));
            }
            else if (this.in_author) {
            	 myParsedExampleDataSet.setExtractedAuthor(new String(ch));
            }
            else if (this.in_publisher) {
            	 myParsedExampleDataSet.setExtractedPublisher(new String(ch));
            }
            else if (this.in_summary) {
            	 myParsedExampleDataSet.setExtractedSummary(new String(ch));
            }
        }
    }

    public class ParsedExampleDataSet {
    	private String extractedString	 	= null;
        private String extractedTitle 		= null;
        private String extractedAuthor 		= null;
        private String extractedPublisher 	= null;
        private String extractedSummary 	= null;
        
        public String getExtractedString() {
            return extractedString;
        }
        public void setExtractedTitle(String extractedString) {
            this.extractedTitle = extractedString;
        }
        public void setExtractedAuthor(String extractedString) {
            this.extractedAuthor = extractedString;
        }
        public void setExtractedPublisher(String extractedString) {
            this.extractedPublisher = extractedString;
        }
        public void setExtractedSummary(String extractedString) {
            this.extractedSummary = extractedString;
        }

        public String toString(String tag){
            if (tag == "title") {
            	return this.extractedTitle;
            }
            else if (tag == "author") {
            	return this.extractedAuthor;
            }
            else if (tag == "publisher") {
            	return this.extractedPublisher;
            }
            else if (tag == "summary") {
            	return this.extractedSummary;
            }
            else {
            	return null;
            }
        }

    }

}