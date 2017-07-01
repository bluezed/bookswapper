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

public class Book {
	public String bookID;
	public String title;
	public String author;
	public String bookLink;
	public int status;

	public Book() {
		// TODO Auto-generated constructor stub
	}

	public Book(String bookID, String title, String author, String bookLink, int status) {
		this.bookID = bookID;
		this.title = title;
		this.author = author;
		this.bookLink = bookLink;
		this.status = status;
	}

	@Override
	public String toString() {
		return this.bookID + ": " + this.title;
	}
}
