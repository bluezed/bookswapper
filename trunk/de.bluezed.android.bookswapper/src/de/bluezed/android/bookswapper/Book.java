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
