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
			
		return null;
	}
}
