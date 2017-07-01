/**
   Bookswapper App
   Copyright (C) 2012 Thomas Geppert (bluezed.apps@gmail.com)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software Foundation,
   Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/

package de.bluezed.android.bookswapper;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import de.bluezed.android.bookswapper.ImageThreadLoader.ImageLoadedListener;
import android.content.Context;
import android.graphics.Bitmap;
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
	private ImageThreadLoader imageLoader = new ImageThreadLoader();
	
	public BookListArrayAdapter(Context context, int textViewResourceId,
			List<Book> objects) {
		super(context, textViewResourceId, objects);
		this.books = objects;
	}

	@Override
	public int getCount() {
		return this.books.size();
	}

	@Override
	public Book getItem(int index) {
		return this.books.get(index);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.booklist_listview, parent, false);
		}

		// Get item
		Book book = getItem(position);
		
		if (book != null) {
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
				networkType >= 3) { // At least UMTS!!!
	        	
				Bitmap cachedImage = null;
			    try {
			      cachedImage = imageLoader.loadImage(book.bookLink, new ImageLoadedListener() {
			      @Override
				public void imageLoaded(Bitmap imageBitmap) {
			    	  bookIcon.setImageBitmap(imageBitmap);
			    	  notifyDataSetChanged();                }
			      });
			    } catch (MalformedURLException e) {
			     
			    }

			    if( cachedImage != null ) {
			      bookIcon.setImageBitmap(cachedImage);
			    }
	        } else {
	        	bookIcon.setImageResource(R.drawable.no_thumb);
	        }
		}	
		return row;
	}
}

