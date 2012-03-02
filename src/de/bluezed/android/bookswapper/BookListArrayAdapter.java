package de.bluezed.android.bookswapper;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookListArrayAdapter extends ArrayAdapter<Book> {
	private ImageView bookIcon;
	private TextView bookTitle;
	private TextView bookAuthor;
	private List<Book> books = new ArrayList<Book>();
	private DrawableManager drawableList = new DrawableManager();
	 
	public BookListArrayAdapter(Context context, int textViewResourceId,
			List<Book> objects) {
		super(context, textViewResourceId, objects);
		this.books = objects;
	}

	public int getCount() {
		return this.books.size();
	}

	public Book getItem(int index) {
		return this.books.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.booklist_listview, parent, false);
		}

		// Get item
		Book book = getItem(position);
		
		// Get reference to ImageView 
		bookIcon = (ImageView) row.findViewById(R.id.imageViewListBook);
		
		// Get reference to TextView - title
		bookTitle = (TextView) row.findViewById(R.id.textViewListTitle);
		
		// Get reference to TextView - author
		bookAuthor = (TextView) row.findViewById(R.id.textViewListSubtitle);

		//Set 
		bookTitle.setText(book.title);
		bookAuthor.setText(book.author);
		
		// Set book icon only if on fast internet!
		ConnectivityManager connManager = (ConnectivityManager)this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		TelephonyManager teleMan = (TelephonyManager)this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = teleMan.getNetworkType();
	
		if (mWifi.isConnected() || 
			networkType == TelephonyManager.NETWORK_TYPE_UMTS ||
			networkType == TelephonyManager.NETWORK_TYPE_HSDPA ||
			networkType == TelephonyManager.NETWORK_TYPE_LTE) {
        	bookIcon.setImageDrawable(drawableList.fetchDrawable(book.bookLink));
        } else {
        	bookIcon.setImageResource(R.drawable.empty);
        }
				
		return row;
	}
}

