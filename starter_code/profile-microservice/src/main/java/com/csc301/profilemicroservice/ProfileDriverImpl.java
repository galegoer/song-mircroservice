package com.csc301.profilemicroservice;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;

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
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		//nProfile:profile)-[:created]->(nPlaylist:playlist)
		//"MATCH (a:actor), (b:movie) "
		//"WHERE a.id = $actorId AND b.id = $movieId "
		//"CREATE (a)-[r:ACTED_IN]->(b) "
		//"RETURN type(r)", parameters("actorId", actorId, "movieId", movieId));
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", userName);
		params.put("fullname", fullName);
		params.put("password", password);
		try (Session session = driver.session())
        {	
        	try (Transaction tx = session.beginTransaction())
        	{
        		tx.run("CREATE (:profile {userName: $username, fullName: $fullname, password: $password })"
        				+ "-[:created]->(:playlist {plName: 'favorites'})", params);
        		tx.success();  // Mark this write as successful.
        		return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
        	}catch(ClientException e) {
    			return new DbQueryStatus("USERNAME EXISTS", DbQueryExecResult.QUERY_ERROR_GENERIC);
        	}
        }catch(Exception e) {
        	return new DbQueryStatus("SESSION ERROR", DbQueryExecResult.QUERY_ERROR_GENERIC);
        }
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		
		return null;
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		
		return null;
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		return null;
	}
}
