/*
WyldRyde-Logger Log Server - V1.3

MySQL Class File

WyldRyde Will -NOT- Provide Support With This Code!

Copyright (c) 2008 Phil Lavin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

// Libs
import java.sql.*;

//MySQL Class
public class MySQL {
	private String host, database, user, password;
	private int port;
	Connection con;
	Client c;

	// Constructor
	public MySQL(String host, int port, String database, String user, String password) {
		// Remember Details
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
		this.port = port;

		// Connect
		connect();
	}

	// Method to set the Client instance
	public void setClient(Client c) {
		this.c = c;
	}


	// Method to connect
	public void connect() {
		boolean err = false;

		// Create the class with voodoo
		try {
			Class c = Class.forName("com.mysql.jdbc.Driver");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Try to connect to the sql server
		try {
			// Create connection object
			con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
		}
		catch (SQLException e) {
			// If we're not connected
			if (e.getSQLState().equals("08S01")) {
				this.clientSendGlobops("Unable To Connect To MySQL Server. Retrying in 30 seconds..."); // Whine a little

				// Print error
				e.printStackTrace();

				// Wait 30 seconds
				try {
					Thread.sleep(30000);
				}
				catch (Exception e2) {}

				// Reconnect
				connect();
			}
			else {
				// Print error to console
				e.printStackTrace();
			}

			// Error was thrown
			err = true;
		}

		// If no error thrown...
		if (!err) {
			this.clientSendGlobops("Connected to MySQL Server!");
		}
	}

	// Update query method
	public void executeUpdate(String sql) throws Exception {
		try {
			// Make statement
			Statement stmt = this.getStatement();

			// Execute
			stmt.executeUpdate(sql);
		}
		catch (SQLException e) {
			// Add handlers here
			if (e.getSQLState().equals("08S01")) {
				this.clientSendGlobops("MySQL Server Has Gone Away! Attempting Reconnect...");
				this.connect();
			}
			else {
				// Throw the exception on down
				throw e;
			}
		}
	}

	// Query method
	public ResultSet executeQuery(String sql) throws Exception {
		try {
			// Make statement
			Statement stmt = this.getStatement();

			// Execute
			return stmt.executeQuery(sql);
		}
		catch (SQLException e) {
			// Add handlers here
			if (e.getSQLState().equals("08S01")) {
				this.clientSendGlobops("MySQL Server Has Gone Away! Attempting Reconnect...");
				this.connect();
			}
			else {
				// Throw the exception on down
				throw e;
			}
		}

		return null;
	}

	// Method to create a statement
	private Statement getStatement() throws Exception {
		// Make statement and return
		return this.con.createStatement();
	}

	// Methods to escape data for sql input
	public static String sqlEscape(String data) {
		data = data.replace("\\", "\\\\");
		data = data.replace("\"", "\\\"");
		data = data.replace("'", "\\'");
		data = data.replace("`", "\\`");
		data = data.replace("%", "\\%");
		data = data.replace("(", "\\(");
		data = data.replace(")", "\\)");

		return data;
	}

	public static String sqlEscape(int d) {
		Integer dataint = new Integer(d);
		String data = dataint.toString();

		return sqlEscape(data);
	}

	public static String sqlEscape(long d) {
		Long dataint = new Long(d);
		String data = dataint.toString();

		return sqlEscape(data);
	}

	public void clientSendGlobops(String s) {
		if (c != null) {
			c.send("GLOBOPS " + s + "");
		}

		System.out.println("System Message: " + s);
	}
}