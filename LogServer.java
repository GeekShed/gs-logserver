/*
GeekShed-Logger Log Server - V1.4

Main LogServer Class File

GeekShed Will -NOT- Provide Support With This Code!

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
import java.util.*;

// Log server class
public class LogServer {
	// Main method
	public static void main(String args[]) {
		MySQL con = null;

		HashMap<String, String> options = Utils.parseConfig("logserver.conf");

		// Create MySQL Connection

		// MySQL Connection
		// Syntax: <server>, <port>, <database>, <username>, <password>
		con = new MySQL(options.get("mysql-host"), Integer.parseInt(options.get("mysql-port")), options.get("mysql-database"), options.get("mysql-user"), options.get("mysql-password"));

		// Create new instance of client server
		// Syntax: <remote ip>, <link port>, <server name>, <server description>, <linkpass>, <MySQL Connection Variable>
		Client c = new Client(options.get("link-ip"), Integer.parseInt(options.get("link-port")), options.get("logserver-name"), options.get("logserver-description"), options.get("link-password"), options.get("local-ip"), Integer.parseInt(options.get("local-port")), con);

		// Set the Client instance in the MySQL instance so MySQL can output errors
		con.setClient(c);

		// Connect and process
		try {
			c.connect();
			c.process();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
