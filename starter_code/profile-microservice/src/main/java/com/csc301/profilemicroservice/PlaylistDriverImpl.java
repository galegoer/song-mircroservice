package com.csc301.profilemicroservice;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}
	
	
	private String HttpRequest(String url, String type) {
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
	public DbQueryStatus likeSong(String userName, String songId) {

		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String playlistName = userName+"-favorites";
				//check if playlist for username exists since it will be unique (username unique and playlist uses that playlist has to be created with username)
				StatementResult playlist = trans.run("MATCH (a:playlist) WHERE a.plName = $playname RETURN a.playlist", parameters("playname", playlistName)); 
        		if(!playlist.hasNext())  //if not exist
        			return new DbQueryStatus("userName not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        		String JSONRes = HttpRequest("http://localhost:3001" + "/getSongTitleById/"+songId, "GET");
        		
        		if(JSONRes.contains("data")) {
        			//data reveals that we have that songID in mongodb
        			StatementResult res = trans.run("MATCH (a:song {songId: $songid}) RETURN a", parameters("songid", songId));
        			if(res.hasNext()) {
        				//song exists
        				StatementResult exists = trans.run("MATCH (:playlist {plName: $playname})-[r:includes]->(:song{songId: $songid}) RETURN r", parameters("playname", playlistName, "songid", songId));
        				if(!exists.hasNext()) {
        					//relationship doesn't exist so create and update favs
        					try {
        						trans.run("MATCH (a:playlist {plName: $playname}),(b:song {songId: $songid}) CREATE (a)-[r:includes]->(b)", parameters("playname", playlistName, "songid", songId));
        					}catch(Exception e) {
        						System.out.println(e.toString());
        					}
        					String updateRes = HttpRequest("http://localhost:3001/updateSongFavouritesCount/"+songId, "PUT");
        				}
        				//relationship already exists or doesn't either way we return OK according to ilir
        			} else {
        				//song doesnt exist in neo4j
            			trans.run("CREATE (:song {songId: $songid})", parameters("songid", songId));
        				trans.run("MATCH (a:playlist {plName: $playname}),(b:song {songId: $songid}) CREATE (a)-[r:includes]->(b)", parameters("playname", playlistName, "songid", songId));
    					String updateRes = HttpRequest("http://localhost:3001/updateSongFavouritesCount/"+songId, "PUT");
        			}
        			trans.success();
        			return new DbQueryStatus("Song added", DbQueryExecResult.QUERY_OK);
        		} else {
        			return new DbQueryStatus("songId does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        		}
        	} catch(Exception e) {
				return new DbQueryStatus("FAILED TO RUN TRANSACTION", DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
		
		}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		
		return null;
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		
		return null;
	}
}
