package de.bluezed.android.bookswapper;

public class Swap {
	public int type;
	public String title;
	public int status;
	public String user;
	public String ordered;
	public String shipped;

	public Swap() {
		// TODO Auto-generated constructor stub
	}

	public Swap(int type, String title, int status, String user, String ordered, String shipped) {
		this.type	= type;
		this.title 	= title;
		this.status = status;
		this.user 	= user;
		this.ordered = ordered;
		this.shipped = shipped;
	}

	@Override
	public String toString() {
		return String.valueOf(this.type) + ": " + this.title;
	}
}
