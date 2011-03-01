/*
GeekShed-Logger Log Server - V1.4

Client Class File

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
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

// Client class
public class Client {
	// Statics
	private static String VERSION = "1.4";

	// Vars
	private String remoteIP, localIP; // Link server IP
	private int remotePort, localPort; // Link server port
	private String remotePass; // Link server password
	private String serverName; // This server name
	private String serverDescription; // This server description
	private Socket sock; // Link socket
	private BufferedReader in; // Reader
	private PrintWriter out; // Writer
	private boolean connected = false; // Connected to link server fully?
	private Vector<User> Users = new Vector<User>(100, 100); // Users on network
	private HashMap<String, String> getInfoNick = new HashMap<String, String>(50); // Used to stop laggy servers confusing GETINFO parser
	private MySQL mysql; // Mysql connection
	private StringBuffer sendQ = new StringBuffer(); // SendQ
	private boolean clientIntroduced = false;

	// Constructor
	public Client(String remoteIP, int remotePort, String serverName, String serverDescription, String remotePass, String localIP, int localPort, MySQL mysql) {
		// Remember data
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.localIP = localIP;
		this.localPort = localPort;
		this.remotePass = remotePass;
		this.serverName = serverName;
		this.serverDescription = serverDescription;
		this.mysql = mysql;
	}

	// Method to connect to the link server
	public void connect() throws Exception {
		// Create socket
		sock = new Socket(remoteIP, remotePort, InetAddress.getByName(localIP), localPort);

		// Create reader and writer
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out = new PrintWriter(sock.getOutputStream(), true);

		// Send essential connect data
		send("PROTOCTL NICKv2");
		send("PASS " + this.remotePass);
		send("SERVER " + serverName + " 1 :" + serverDescription);

		// This will be put in the send queue. Send it so we can continue.
		sendSendQ();
	}

	// Method to loop and process socket
	public void process() throws Exception {
		String line;

		// Loop and read
		while ((line = in.readLine()) != null) {
			// Print to console
			System.out.println("<- " + line);
			// Handle data
			handle(line);
		}
	}

	// Method to handle data
	private void handle(String line) throws Exception {
		// Tokenize data by \s
		String[] tokens = line.split("\\s");

		// Handle pings
		if (tokens[0].equals("PING")) {
			send("PONG " + Utils.assembleSlice(tokens, 1));
		}
		// Last server connection process message - we're now connected fully
		else if (tokens[0].equals("NETINFO")) {
			connected = true;

			// Clear what's left of the send queue.
			sendSendQ();

			// Check if we have a client, introduce if not
			if (!this.clientIntroduced) {
				send("NICK Logger 1 " + System.currentTimeMillis() / 1000 + " logger " + serverName.toLowerCase() + " " + serverName + " 0" + " +oANHSBr * :Logger");
				send(":" + serverName + " SWHOIS Logger :is a Network Service");

				this.clientIntroduced = true;
			}

			// Join some channels
			send(":Logger JOIN #serverbans");
		}
		// Handle new user NICK commands
		else if (tokens[0].equals("NICK")) {
			// Make the gecos
			String gecos = Utils.assembleSlice(tokens, 10);
			gecos = gecos.substring(1);

			// Create new user object
			User u = new User(tokens[1], tokens[4], gecos, tokens[6], (tokens[9].equals("*")?tokens[5]:tokens[9]), Long.parseLong(tokens[3]), mysql);

			// Add modes
			for (int i = 1; i < tokens[8].length(); i++) {
				u.addModeOn(tokens[8].charAt(i));
			}

			// Add object to vector
			Users.add(u);

			// Check if we have a client, introduce if not
			if (!this.clientIntroduced) {
				send("NICK Logger 1 " + System.currentTimeMillis() / 1000 + " logger " + serverName.toLowerCase() + " " + serverName + " 0" + " +oANHSBr * :Logger");
				send(":" + serverName + " SWHOIS Logger :is a Network Service");

				this.clientIntroduced = true;
			}

			// Get info on the user from pseudo-admin
			send(":Logger GETINFO " + tokens[1] + " " + tokens[1]);
		}
		// Handle channel JOINs
		else if (tokens[1].equals("JOIN")) {
			// Get user's object
			User u = this.getUserByNick(tokens[0].substring(1));

			if (u == null) {
				System.out.println("User Not Found!");
			}
			else { // If all ok
				// Tokenize the channel list
				String[] splittok = Utils.assembleSlice(tokens, 2).split(",");

				// Loop and add channels
				for (String s : splittok) {
					u.addChannel(s);
				}

				// Insert channels to DB
				u.insertChannels();
			}
		}
		// Handle TKLs
		else if (tokens[1].equals("TKL")) {
			// Build query
			String sql = null;

			// If setting on
			if (tokens[2].equals("+")) {
				// Build Reason
				String reason = tokens[9].substring(1);

				for (int i = 10; i < tokens.length; i++) {
					reason = reason + " " + tokens[i];
				}

				sql = "INSERT INTO `tkl` (`on`, `type`, `user`, `host`, `source`, `expiretime`, `settime`, `reason`) VALUES (1, '" + MySQL.sqlEscape(tokens[3]) + "', '" + MySQL.sqlEscape(tokens[4]) + "', '" + MySQL.sqlEscape(tokens[5]) + "', '" + MySQL.sqlEscape(tokens[6]) + "', " + MySQL.sqlEscape(tokens[7]) + ", " + MySQL.sqlEscape(tokens[8]) + ", '" + MySQL.sqlEscape(reason) + "')";
				sql += " ON DUPLICATE KEY UPDATE `expiretime`=" + MySQL.sqlEscape(tokens[7]) + ", `reason`='" + MySQL.sqlEscape(reason) + "'";
			}
			// If setting off
			else {
				sql = "INSERT INTO `tkl` (`on`, `type`, `user`, `host`, `source`, `expiretime`, `settime`, `reason`) VALUES (0, '" + MySQL.sqlEscape(tokens[3]) + "', '" + MySQL.sqlEscape(tokens[4]) + "', '" + MySQL.sqlEscape(tokens[5]) + "', '" + MySQL.sqlEscape(tokens[6]) + "', null, null, null)";
			}

			// Attempt to insert into tlk table
			try {
				this.mysql.executeUpdate(sql);
			}
			// On failure
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// If we're fully connected
		if (connected) {
			// Handle VERSION
			if (tokens[1].equals("VERSION")) {
				send(":" + this.serverName + " 351 " + tokens[0].substring(1) + " GeekShed Log Server " + this.VERSION);
			}
			// Handle QUITs
			else if (tokens[1].equals("QUIT")) {
				// Get user id
				int ptr = getUserIdByNick(tokens[0].substring(1));

				// Remove user from vector
				if (ptr != -1) {
					Users.remove(ptr);
				}
			}
			// Handle MODE changes
			else if (tokens[1].equals("MODE")) {
				// If not channel mode
				if (tokens[2].charAt(0) != '#') {
					// Get user object
					User u = this.getUserByNick(tokens[2]);

					if (u == null) {
						System.out.println("User Not Found!");
					}
					else { // If all ok
						boolean on = true;

						// Loop mode string
						for (int i = (tokens[3].charAt(0) == ':'?1:0); i < tokens[3].length(); i++) {
							// Is + or -? Set on flag on/off respectively
							if (tokens[3].charAt(i) == '+') {
								on = true;
							}
							else if (tokens[3].charAt(i) == '-') {
								on = false;
							}
							else {
								if (on) { // If mode on
									// Add mode on
									u.addModeOn(tokens[3].charAt(i));
								}
								else { // If mode off
									// Add mode off
									u.addModeOff(tokens[3].charAt(i));
								}
							}
						}

						// Insert modes into DB
						u.insertModes();
					}
				}
			}
			// Handle nickname changes
			else if (tokens[1].equals("NICK")) {
				// Get user object
				User u = this.getUserByNick(tokens[0].substring(1));

				if (u == null) {
					System.out.println("User Not Found!");
				}
				else { // If all ok
					// Change nick in user object
					u.setNick(tokens[2]);

					// "Random and unexplained bug" fix...
					if (tokens[3].substring(0,1).equals(":")) {
						tokens[3] = tokens[3].substring(1);
					}

					// Set last NICK timestamp
					u.setTimestamp(Integer.parseInt(tokens[3]));
					// Update above in logs table
					u.updateInDB();
					// Add nickname to nicknames table
					u.addNickname(tokens[2]);
					u.insertNicknames();
				}
			}
			else if (tokens[1].equals("KICK")) {
				this.send(":Logger PRIVMSG #serverbans :[Kick] " + tokens[3] + " was kicked from " + tokens[2] + " by " + tokens[0].substring(1));
			}
			// Handle getinfo replies (raw numeric 339)
			else if (tokens[1].equals("339")) {
				User u = null;

				// If is start of reply
				if (tokens[3].equals(":Info")) {
					this.getInfoNick.put(tokens[0], tokens[5]); // Map server name to nickname
				}
				else if (tokens[3].substring(0, 2) != ":=") { // In all other cases, besides the occurance of a dividing line...
					// Get user object
					u = this.getUserByNick(this.getInfoNick.get(tokens[0]));

					if (u == null) {
						System.out.println("User Not Found!");
					}
					else { // If all ok
						// If is :Connected line
						if (tokens[3].equals(":Connected")) {
							u.setDPort(Integer.decode(tokens[8])); // Set destination port
						}
						// If is :Remote line
						else if (tokens[3].equals(":Remote")) {
							u.setSPort(Integer.decode(tokens[7].substring(0, tokens[7].length() - 1))); // Set source port
							u.setIP(tokens[5]); // Set IP
						}
						// If is :Flags line
						else if (tokens[3].equals(":Flags:")) {
							u.setFlags(Utils.assembleSlice(tokens, 4)); // Set flags
						}
						// If is end of GETINFO reply
						else if (tokens[3].equals(":End")) {
							u.createInDB(); // Create user in db
							u.insertNicknames(); // Add user's nickname
							u.insertModes(); // Add user's modes
							u.insertChannels(); // Add user's channels
						}
					}
				}
			}
		}
	}

	// Method to send data to server
	public void send(String data) {
		if (this.connected) {
			// Send to server
			this.out.println(data);

			// Send to console
			String[] dataSplit = data.split("\n");

			for (String dataItem : dataSplit) {
				System.out.println("-> " + dataItem);
			}
		}
		else {
			// Send the sendQ if the next append will make it too long
			if (this.sendQ.length() + data.length() >= 511) {
				this.sendSendQ();
			}

			this.sendQ.append(data + "\n");
		}
	}

	// Method to send the SendQ to server
	public void sendSendQ() {
		// Send to server
		this.out.println(this.sendQ.toString());

		// Send to console
		String[] dataSplit = this.sendQ.toString().split("\n");

		for (String dataItem : dataSplit) {
			System.out.println("-> " + dataItem);
		}

		// Reset queue
		this.sendQ = new StringBuffer();
	}

	// Method to get user object by nickname
	private User getUserByNick(String nick) {
		// Get ID pointer
		int ptr = getUserIdByNick(nick);

		if (ptr == -1) {
			return null;
		}
		else {
			// Return user object
			return Users.elementAt(ptr);
		}
	}

	// Method to get user ID by nickname
	private int getUserIdByNick(String nick) {
		// Loop user objects
		for (int i = 0; i < this.Users.size(); i++) {
			// Compare nicknames
			if (Users.elementAt(i).getNick().equals(nick)) {
				// Return id
				return i;
			}
		}

		return -1;
	}
}
