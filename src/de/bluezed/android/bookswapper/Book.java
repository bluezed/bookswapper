package de.bluezed.android.bookswapper;

public class Book
{
	public String title;
	public String author;
	public String bookLink;

	public Book()
		{
			// TODO Auto-generated constructor stub
		}

	public Book(String title, String author, String bookLink)
		{
			this.title = title;
			this.author = author;
			this.bookLink = bookLink;
		}

	@Override
	public String toString()
		{
			return this.title;
		}
}
