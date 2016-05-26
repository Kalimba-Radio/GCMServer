package com.javapapers.java.gcm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

@WebServlet("/GCMMulticast")
public class GCMMulticast extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Put your Google API Server Key here
	private static final String GOOGLE_SERVER_KEY = "AIzaSyB9swx9qGyUwYKLxbJjO5owyQddXMRhnuY";
	static final String MESSAGE_KEY = "message";
	static final String REG_ID_STORE = "GCMRegId.txt";

	public GCMMulticast() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		MulticastResult result = null;

		String share = request.getParameter("shareRegId");

		// GCM RedgId of Android device to send push notification

		if (share != null && !share.isEmpty()) {
			writeToFile(request.getParameter("regId"));
			request.setAttribute("pushStatus", "GCM RegId Received.");
			request.getRequestDispatcher("index.jsp")
					.forward(request, response);
		} else {

			try {

				String userMessage = request.getParameter("message");
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message message = new Message.Builder().timeToLive(30)
						.delayWhileIdle(true).addData(MESSAGE_KEY, userMessage)
						.build();
				//Set regIdSet = readFromDB();
				Set<String> regIdSet= new HashSet<String>() {{
					  add("ecKNrRVS4nw:APA91bFXgKXYmrKrROkrRBC6zlksqWiYJrJ5vhIa1RXH8nJ8zz7ETONUqPvsVZ05VVHBHEkF25RzfUyXLAvOcSc_v4--E0DZ6aQGZ6870LDlovxwo6gmZ20-iG-ijhiU3hZK4p-sbdJS"); 
				}};
				System.out.println("regId: " + regIdSet);
				List regIdList = new ArrayList();
				regIdList.addAll(regIdSet);
				result = sender.send(message, regIdList, 1);
				request.setAttribute("pushStatus", result.toString());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				request.setAttribute("pushStatus",
						"RegId required: " + ioe.toString());
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("pushStatus", e.toString());
			}
			request.getRequestDispatcher("index.jsp")
					.forward(request, response);
		}
	}

	private void writeToFile(String regId) throws IOException {
		Set regIdSet = readFromFile();

		if (!regIdSet.contains(regId)) {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(REG_ID_STORE, true)));
			out.println(regId);
			out.close();
		}

	}

	private Set readFromFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(REG_ID_STORE));
		String regId = "";
		Set regIdSet = new HashSet();
		while ((regId = br.readLine()) != null) {
			if (!regId.equals("null") && regId.length() > 0) {
				regIdSet.add(regId);
			}
		}
		br.close();
		return regIdSet;
	}

	private Set readFromDB()  {
		// BufferedReader br = new BufferedReader(new FileReader(REG_ID_STORE));
		
		Set regIdSet = new HashSet();
		Database database = new Database();
		try {
			Connection connection = database.Get_Connection();
			// Project project= new Project();
			// feeds=project.GetFeeds(connection);
			PreparedStatement ps = connection
					.prepareStatement("SELECT GCM_REGID FROM sonic_user");

			// ps.setString(1,uname);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {

				regIdSet.add(rs.getString("GCM_REGID"));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return regIdSet;
	}
}