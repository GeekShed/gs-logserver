/*
WyldRyde-Logger Log Server - V1.0

User Class File

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
import java.util.*;
import java.sql.*;

// User class
public class User {
	// Vars
	private String nick, ident, gecos, ip, server, hostname, flags; // User info
	private long timestamp; // Last NICK timestamp
	private int sport, dport; // Source and dest ports
	private Vector<Character> modesOn = new Vector<Character>(20); // Modes set on
	private Vector<Character> modesOff = new Vector<Character>(20); // Modes set off
	private Vector<String> channels = new Vector<String>(60); // Channels joined
	private Vector<String> nicknames = new Vector<String>(60); // Nicknames used
	private Connection mysql; // Mysql connection
	private Statement stmt; // Mysql statement
	private long mysqlRecordId = 0; // Mysql insert id

	// Constructor
	public User(String nick, String ident, String gecos, String ip, String server, String hostname, long timestamp, Connection mysql) {
		// Store data
		this.nick = nick;
		this.ident = ident;
		this.gecos = gecos;
		this.ip = ip;
		this.server = server;
		this.hostname = hostname;
		this.timestamp = timestamp;
		this.mysql = mysql;

		// Create statement object
		try {
			this.stmt = mysql.createStatement();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Save current nickname to nicknames table
		this.nicknames.add(nick);
		this.insertNicknames();
	}

	// Accessors and mutators
	public void setMySqlRecordId(long id) {
		this.mysqlRecordId = id;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNick() {
		return this.nick;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public void setGecos(String gecos) {
		this.gecos = gecos;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public void setSPort(int sport) {
		this.sport = sport;
	}

	public void setDPort(int dport) {
		this.dport = dport;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public void addModeOn(char mode) {
		if (modesOn.capacity() == modesOn.size()) {
			modesOn.ensureCapacity(modesOn.size() + 100);
		}

		modesOn.add(new Character(mode));
	}

	public void addModeOff(char mode) {
		modesOff.add(new Character(mode));
	}

	public void addChannel(String channel) {
		channels.add(channel);
	}

	public void addNickname(String nickname) {
		nicknames.add(nickname);
		this.nick = nickname;
	}

	// Overridden method to give sane class debug output
	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("Nick = " + this.nick + "\n");
		buf.append("Ident = " + this.ident + "\n");
		buf.append("Gecos = " + this.gecos + "\n");
		buf.append("IP = " + this.ip + "\n");
		buf.append("Server = " + this.server + "\n");
		buf.append("Hostname = " + this.hostname + "\n");
		buf.append("Flags = " + this.flags + "\n");
		buf.append("Source Port = " + this.sport + "\n");
		buf.append("Destination Port = " + this.dport + "\n");
		buf.append("Modes On = " + this.modesOn + "\n");
		buf.append("Modes Off = " + this.modesOff + "\n");
		buf.append("Channels = " + this.channels + "\n");
		buf.append("Nicknames = " + this.nicknames + "\n");
		buf.append("SQL ID = " + this.mysqlRecordId + "\n");

		return buf.toString();
	}

	// Create the user record in the database
	public void createInDB() {
		String sql = "INSERT INTO `logs` (`nick`, `gecos`, `server`, `dport`, `ip`, `sport`, `flags`, `userhost`, `timestamp`, `con_timestamp`) VALUES ('" + this.nick + "', '" + this.gecos + "', '" + this.server + "', '" + this.dport + "', '" + this.ip + "', '" + this.sport + "', '" + this.flags + "', '" + this.hostname + "', " + this.timestamp + ", " + this.timestamp + ")";

		// Attempt to insert into logs table
		try {
			this.stmt.executeUpdate(sql);

			// Get mysql insert id
			ResultSet rs = this.stmt.executeQuery("SELECT LAST_INSERT_ID() AS insid");
			rs.next();
			this.mysqlRecordId = rs.getLong("insid");
		}
		// On failure
		catch (Exception e) {
			// If not dup key error
			if (!e.getMessage().substring(0, 68).equals("Duplicate key or integrity constraint violation message from server:")) {
				e.printStackTrace();
			}
			else { // If dup key error
				// Get the ID of row causing clash and remember
				sql = "SELECT `id` FROM `logs` WHERE `nick`='" + this.nick + "' AND `ip`='" + this.ip + "' AND `timestamp`='" + this.timestamp + "'";

				try {
					ResultSet rs = this.stmt.executeQuery(sql);
					rs.next();
					this.mysqlRecordId = rs.getLong("id");
				}
				catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	// Update data in logs table row
	public void updateInDB() {
		if (this.mysqlRecordId != 0) {
			// Query
			String sql = "UPDATE `logs` SET `nick`='" + this.nick + "', `gecos`='" + this.gecos + "', `server`='" + this.server + "', `dport`='" + this.dport + "', `ip`='" + this.ip + "', `sport`='" + this.sport + "', `flags`='" + this.flags + "', `userhost`='" + this.hostname + "', `timestamp`='" + this.timestamp + "' WHERE `id`=" + this.mysqlRecordId;

			try {
				this.stmt.executeUpdate(sql);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Add nicknames from vector to table
	public void insertNicknames() {
		if (nicknames.size() > 0 && this.mysqlRecordId != 0) {
			String sql = "INSERT INTO `nicknames` (`lid`, `nickname`, `timestamp`) VALUES ";

			// Loop vector and build query
			for (int i = 0; i < nicknames.size(); i++) {
				sql = sql + "('" + this.mysqlRecordId + "', '" + nicknames.get(i) + "', UNIX_TIMESTAMP()), ";
			}

			// Trim query
			sql = sql.substring(0, sql.length() - 2);

			// Clear vector
			nicknames.clear();

			// Do Query
			try {
				this.stmt.executeUpdate(sql);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Add modes from Vector "v" to table "table"
	public void insertModes(String table, Vector v) {
		if (v.size() > 0 && this.mysqlRecordId != 0) {
			String sql = "INSERT INTO `" + table + "` (`lid`, `mode`, `timestamp`) VALUES ";

			// Loop vector and build query
			for (int i = 0; i < v.size(); i++) {
				sql = sql + "('" + this.mysqlRecordId + "', '" + v.get(i) + "', UNIX_TIMESTAMP()), ";
			}

			// Trim query
			sql = sql.substring(0, sql.length() - 2);

			// Clear vector
			v.clear();

			// Do query
			try {
				this.stmt.executeUpdate(sql);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Overloaded method to add both modes on and off to db
	public void insertModes() {
		insertModes("modes_on", this.modesOn);
		insertModes("modes_off", this.modesOff);
	}

	// Insert channels from vector into db
	public void insertChannels() {
		if (channels.size() > 0 && this.mysqlRecordId != 0) {
			String sql = "INSERT INTO `channels` (`lid`, `channel`, `timestamp`) VALUES ";

			// Loop vector and build query
			for (int i = 0; i < channels.size(); i++) {
				sql = sql + "('" + this.mysqlRecordId + "', '" + channels.get(i) + "', UNIX_TIMESTAMP()), ";
			}

			// Trim query
			sql = sql.substring(0, sql.length() - 2);

			// Clear vector
			channels.clear();

			// Do query
			try {
				this.stmt.executeUpdate(sql);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}