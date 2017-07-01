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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SwapListArrayAdapter extends ArrayAdapter<Swap> {
	private ImageView swapIcon;
	private TextView bookTitle;
	private TextView subtitle;
	private List<Swap> swaps = new ArrayList<Swap>();
	 
	public SwapListArrayAdapter(Context context, int textViewResourceId, List<Swap> objects) {
		super(context, textViewResourceId, objects);
		this.swaps = objects;
	}

	@Override
	public int getCount() {
		return this.swaps.size();
	}

	@Override
	public Swap getItem(int index) {
		return this.swaps.get(index);
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
		Swap swap = getItem(position);
		
		// Get reference to ImageView 
		swapIcon = (ImageView) row.findViewById(R.id.imageViewListBook);
		
		// Get reference to TextView - title
		bookTitle = (TextView) row.findViewById(R.id.textViewListTitle);
		
		// Get reference to TextView - subtitle
		subtitle = (TextView) row.findViewById(R.id.textViewListSubtitle);

		//Set 
		bookTitle.setText(swap.title);
		
		String subText = "";
		switch (swap.type) {
			case BookswapperActivity.BOOK_IN:
				subText = this.getContext().getString(R.string.from) + " " + swap.user;
				// Set swap icon!
				swapIcon.setImageResource(R.drawable.stat_sys_download);
				break;
			case BookswapperActivity.BOOK_OUT:
				subText = this.getContext().getString(R.string.to) + " " + swap.user;
				// Set swap icon!
				swapIcon.setImageResource(R.drawable.stat_sys_upload);
				break;
		}
		
		subText = subText + "\n" + this.getContext().getString(R.string.status) + ":";
				
		switch (swap.status) {
			case BookswapperActivity.BOOK_SHIPPED:
				subText = subText + " " + this.getContext().getString(R.string.shipped) + " " + swap.shipped;
				break;
			case BookswapperActivity.BOOK_NOT_SHIPPED:
				subText = subText + " " + this.getContext().getString(R.string.not_shipped) + " " + swap.ordered;
				break;
		}		
		
		subtitle.setText(subText);
				
		return row;
	}
}

