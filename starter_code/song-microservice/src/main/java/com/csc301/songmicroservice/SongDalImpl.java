package com.csc301.songmicroservice;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.DBObject;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
		
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		DbQueryStatus rtn;
		
		Document document = new Document(); //BasicDbobject doc = new basicDebobeject()
		document.put("songName", songToAdd.getSongName());
		document.put("songArtistFullName", songToAdd.getSongArtistFullName());
		document.put("songAlbum", songToAdd.getSongAlbum());
		document.put("songAmountFavourites", songToAdd.getSongAmountFavourites());
		
		 
		db.getCollection("songs").insertOne(document);
		
		rtn = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		rtn.setData(document); //unless its "data" : all the puts
		return rtn;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {		
		MongoCollection<Document> songs = db.getCollection("songs");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(songId));
		FindIterable<Document> cursor = songs.find(query);
        
		if (cursor.first() == null) { 
        	//song not found
        	return new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
        
        Document d = cursor.first();
        DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
        status.setData(d);
		return status;
			
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		MongoCollection<Document> songs = db.getCollection("songs");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(songId));
		FindIterable<Document> cursor = songs.find(query);
        
		if (cursor.first() == null) { 
        	//song not found
        	return new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
        
        Document d = cursor.first();
        DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
        status.setData(d.getString("songName"));
		return status;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		return null;
	}
}