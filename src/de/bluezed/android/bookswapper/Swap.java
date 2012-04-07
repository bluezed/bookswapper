package de.bluezed.android.bookswapper;

import java.io.Serializable;



//	"prename":"XYZ","postal":"12345","postname":"XYZ","uname":"XYZ","street":"XYZ","shippedon":"2012-04-03 18:33:18","city":"XYZ","author":"XYZ","title":"XYZ","orderedon":"2012-04-03 14:37:33","order":"1234","shipped":"1","book":"12345","user":"123"
public class Swap implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5327488627971382589L;
	public String orderID;
	public String bookID;
	public int type;
	public String title;
	public String author;
	public int status;
	public String userID;
	public String user;
	public String ordered;
	public String shipped;
	public String firstname;
	public String lastname;
	public String street;
	public String postcode;
	public String city;
	
	public Swap() {
		// TODO Auto-generated constructor stub
	}
	
	public Swap(String orderID, String bookID, int type, String title, String author, int status, String userID, String user, String ordered, String shipped, String firstname, String lastname, String street, String postcode, String city) {
		this.orderID 	= orderID;
		this.bookID		= bookID;
		this.type		= type;
		this.title 		= title;
		this.author		= author;
		this.status 	= status;
		this.userID		= userID;
		this.user 		= user;
		this.ordered 	= ordered;
		this.shipped 	= shipped;
		this.firstname 	= firstname;
		this.lastname 	= lastname;
		this.street 	= street;
		this.postcode 	= postcode;
		this.city 		= city;
	}

	@Override
	public String toString() {
		return orderID + " - " + String.valueOf(this.type) + ": " + this.title;
	}
}
