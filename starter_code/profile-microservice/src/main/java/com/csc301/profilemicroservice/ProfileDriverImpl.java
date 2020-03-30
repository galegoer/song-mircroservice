package com.csc301.profilemicroservice;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ClientException;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	private String HttpRequest(String url, String type, String decrement) {
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(null, new byte[0]);
		Request request = null;
		if(type == "GET") {
			request = new Request.Builder().url(url).build();
		}else {
			RequestBody formBody = new FormBody.Builder().add("shouldDecrement", "false").build(); 
		    request = new Request.Builder().url(url).put(formBody).build();
			//you can use .post(body)  or .put(body)  or dont use either to use "GET"
		}
		Call call = client.newCall(request);
		Response responseFromMs = null;
		String serviceBody = "{}";
		try {
		    responseFromMs = call.execute();
		    serviceBody = responseFromMs.body().string();
		    return serviceBody;
		} catch (IOException e) {
		    return e.toString();
		}
		
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		try (Session session = driver.session())
        {	
        	try (Transaction tx = session.beginTransaction())
        	{
        		tx.run("CREATE (:profile {userName: $username, fullName: $fullname, password: $password })"
        				+ "-[:created]->(:playlist {plName: $favorites})", parameters("username", userName, "fullname", fullName,
        						"password", password, "favorites", userName+"-favorites"));
        		tx.success();  // Mark this write as successful.
        		return new DbQueryStatus("Created profile", DbQueryExecResult.QUERY_OK);
        	}catch(ClientException e) {
    			return new DbQueryStatus("Username exists", DbQueryExecResult.QUERY_ERROR_GENERIC);
        	}
        }catch(Exception e) {
        	return new DbQueryStatus("Session error", DbQueryExecResult.QUERY_ERROR_GENERIC);
        }
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				
				//check if userName and frnUserName profiles exist
				StatementResult u1 = trans.run("MATCH (a:profile) WHERE a.userName = $uname RETURN a.userName", parameters("uname", userName)); 
				StatementResult u2 = trans.run("MATCH (a:profile) WHERE a.userName = $uname RETURN a.userName", parameters("uname", frndUserName)); 
        		if(!u1.hasNext())  //if not exist
        			return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        		
        		if(!u2.hasNext())  //if not exist
            		return new DbQueryStatus("frndUserName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
            	
            	//check if relationship exists
            	StatementResult relationshipCheck = trans.run("MATCH (:profile { userName: {x} })-[r:follows]->(:profile { userName: {y}}) RETURN type(r)", parameters("x", userName, "y", frndUserName));
            	if(relationshipCheck.hasNext()) {
            		return new DbQueryStatus("relationship already exists", DbQueryExecResult.QUERY_ERROR_GENERIC);
            	}
        		
            	//add relationship
            	StatementResult result = trans.run("MATCH (a:profile), (b:profile) "
						+ "WHERE a.userName = $uname AND b.userName = $frndname "
						+ "CREATE (a)-[r:follows]->(b) "
						+ "RETURN type(r)", parameters("uname", userName, "frndname", frndUserName));
        		trans.success();  // Mark this write as successful.
        		return new DbQueryStatus("relationship successfully added", DbQueryExecResult.QUERY_OK);
        		
        		 
        	}
			catch(Exception e) {
				return new DbQueryStatus("Failed transaction", DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
		
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				
				//check if userName and frnUserName profiles exist
				StatementResult u1 = trans.run("MATCH (a:profile) WHERE a.userName = $uname RETURN a.userName", parameters("uname", userName)); 
				StatementResult u2 = trans.run("MATCH (a:profile) WHERE a.userName = $uname RETURN a.userName", parameters("uname", frndUserName)); 
        		if(!u1.hasNext())  //if not exist
        			return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        		
        		if(!u2.hasNext())  //if not exist
            		return new DbQueryStatus("frndUserName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
            	
            	
        		//check if relationship exists
            	StatementResult relationshipCheck = trans.run("MATCH (:profile { userName: {x} })-[r:follows]->(:profile { userName: {y}}) RETURN type(r)", parameters("x", userName, "y", frndUserName));
            	if(relationshipCheck.hasNext()) { //delete relationship
            		StatementResult result = trans.run("MATCH (:profile { userName: {x} })-[r:follows]->(:profile { userName: {y}}) DELETE r", parameters("x", userName, "y", frndUserName));
            		trans.success();
            		return new DbQueryStatus("relationship successfully removed", DbQueryExecResult.QUERY_OK);
            	}
            	else {
            		return new DbQueryStatus("relationship doesn't exist", DbQueryExecResult.QUERY_ERROR_GENERIC);
            	}
        		
        	}
			catch(Exception e) {
				return new DbQueryStatus("Failed transaction", DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
		
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				JSONObject friendData = new JSONObject();
				Map<String, List<String>> s = new HashMap<String, List<String>>();
				
				DbQueryStatus rtn = new DbQueryStatus("succesfully retrieved friend songs", DbQueryExecResult.QUERY_OK);
				
				//check if userName profiles exist
				StatementResult u1 = trans.run("MATCH (a:profile) WHERE a.userName = $uname RETURN a.userName", parameters("uname", userName)); 
				if(!u1.hasNext())  //if not exist
        			return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				
				//get friends
				StatementResult friends = trans.run("MATCH (:profile { userName: {x} })-[:follows]->(profile) RETURN profile.userName", parameters("x", userName));			
        		if(!friends.hasNext())  
        			return new DbQueryStatus("userName has no friends", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        		        		
        		while (friends.hasNext()) {
        			
        			Record record = friends.next();
        			String frndName = record.get("profile.userName").asString().replaceAll("\\\\", "");
        			String playlistName = frndName+"-favorites";
        			
        			StatementResult getSongs = trans.run("MATCH (:playlist { plName: {x} })-[:includes]->(song) RETURN song.songId", parameters("x", playlistName));
        			       			        			
        			//JSONObject friendSongs = new JSONObject();
        			List<String> songList = new ArrayList();
        		   //for each songID get song title
        			while (getSongs.hasNext()) {
        				JSONObject JSONres = null;
        				Record songRecord = getSongs.next();
        				String songId = songRecord.get("song.songId").asString();
        				String res = HttpRequest("http://localhost:3001" + "/getSongTitleById/"+songId, "GET", "");
        				try {
        				     JSONres = new JSONObject(res);
        				}catch (JSONException err){
        				     System.out.println("failed to convert JSONRes");
        				}        				
        				String songTitle = JSONres.getString("data");
        				songList.add(songTitle);      				
        			}
        			
        			friendData.put(frndName, songList); //place list of titles in JSONObject {friendname : [song titles]}
        			//friendData.put(friendSongs); //add to JSONArray
        			s.put(frndName, songList);
        		} 
        			
        		trans.success();
        		//List<String> testy = new ArrayList<String>();
        		//testy.add("testing");
        		//Map<String, List<String>> s;
        		
        		
        		rtn.setData(s);
            	return rtn;
        		      	           	        		
        	}
			catch(Exception e) {
				return new DbQueryStatus("FAILED TO RUN TRANSACTION", DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
		
		}
	}
}
