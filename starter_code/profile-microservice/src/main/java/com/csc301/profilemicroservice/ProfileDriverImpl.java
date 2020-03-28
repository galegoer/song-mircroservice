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
		try (Session session = driver.session())
        {	
        	try (Transaction tx = session.beginTransaction())
        	{
        		StatementResult result = tx.run("MATCH (a:profile) WHERE a.userName = $userName RETURN a", parameters("userName", userName));
        		if(!result.hasNext()) {
        			tx.run("CREATE (a:profile {userName: {username}, fullName: {fullname}, password: {password}})-[:created]->(b:playlist)", parameters("username", userName,"fullname", fullName, "password", password));
        			tx.success();  // Mark this write as successful.
        			session.close();
        			return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
        		} else {
        			return new DbQueryStatus("USERNAME EXISTS", DbQueryExecResult.QUERY_ERROR_GENERIC);
        		}
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
