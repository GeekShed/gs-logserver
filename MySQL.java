/*
WyldRyde-Logger Log Server - V1.2

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

	// Method to connect
	public void connect() {
		try {
			Class c = Class.forName("com.mysql.jdbc.Driver");

			// Create connection object
			con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Update query method
	public void executeUpdate(String sql) throws Exception {
		// Make statement
		Statement stmt = this.getStatement();

		try {
			// Execute
			stmt.executeUpdate(sql);
		}
		catch (Exception e) {
			// Add handlers here

			// Throw the exception on down
			throw e;
		}
	}

	// Query method
	public ResultSet executeQuery(String sql) throws Exception {
		// Make statement
		Statement stmt = this.getStatement();

		try {
			// Execute
			return stmt.executeQuery(sql);
		}
		catch (Exception e) {
			// Add handlers here

			// Throw the exception on down
			throw e;
		}
	}

	// Method to create a statement
	private Statement getStatement() {
		// Create statement object
		try {
			// Make statement and return
			return this.con.createStatement();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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
}
