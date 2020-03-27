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

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		MongoCollection<Document> songs = db.getCollection("songs");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", songId);
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