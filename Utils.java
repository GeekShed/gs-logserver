/*
WyldRyde-Logger Log Server - V1.2

Utils Static Class File

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
import java.io.*;
import java.text.*;

// Utils class
public class Utils {
	// Method to assemble array slice given array, start and length
	public static String assembleSlice(String[] input, int start, int length) {
		int s = start;
		StringBuffer buf = new StringBuffer();

		// Loop length items of array from start
		for (int i = 0; i < length; i++) {
			buf.append(input[s + i] + " "); // Append to buffer
		}

		// Return String form of buffer
		return buf.toString();
	}

	// Overloaded method to assemble array slice given array and start
	public static String assembleSlice(String[] input, int start) {
		// Call self with full length calculation
		return assembleSlice(input, start, input.length - start);
	}

	// Method to translate a config file into a hash map
	public static HashMap<String, String> parseConfig(String file) {
		// Create a hash map
		HashMap<String, String> options = new HashMap<String, String>();

		// Process File
		try {
			// Readers
			File inFile = new File(file);
			FileInputStream fis = new FileInputStream(inFile);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader in = new BufferedReader(new InputStreamReader(dis));

			// String
			String line;

			// Loop Lines
			while ((line = in.readLine()) != null) {
				// Trim the line
				line = line.trim();

				// Check it's not a blank line or a comment
				if (!line.matches("^$") && !line.matches("^//.*")) {
					// Check it's in the valid format e.g. name:value
					if (!line.matches("^.+:.*$")) {
						// Throw exception if not
						throw new ParseException(line, 0);
					}
					else {
						// Get position of divider
						int div = line.indexOf(":");

						// Split the 2 into a new hashmap entry
						options.put(line.substring(0, div), line.substring(div + 1));
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Return the hash map
		return options;
	}
}