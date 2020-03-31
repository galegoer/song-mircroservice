package com.csc301.songmicroservice;

import java.io.IOException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ExecutableFindOperation.ExecutableFind;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
		if(songToAdd.getSongName() == null || songToAdd.getSongArtistFullName() == null || songToAdd.getSongAlbum() == null) {
			return new DbQueryStatus("Missing song info", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		document.put("songName", songToAdd.getSongName());
		document.put("songArtistFullName", songToAdd.getSongArtistFullName());
		document.put("songAlbum", songToAdd.getSongAlbum());
		document.put("songAmountFavourites", songToAdd.getSongAmountFavourites());
		
		 
		db.getCollection("songs").insertOne(document);
		
		rtn = new DbQueryStatus("Added song", DbQueryExecResult.QUERY_OK);
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
        d.replace("_id", songId);
        DbQueryStatus status = new DbQueryStatus("Song found", DbQueryExecResult.QUERY_OK);
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
        	return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
        
        Document d = cursor.first();
        DbQueryStatus status = new DbQueryStatus("Song found", DbQueryExecResult.QUERY_OK);
        status.setData(d.getString("songName"));
		return status;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {  //might need to make it delete from all favorites list?! no according to discussion board
		MongoCollection<Document> songs = db.getCollection("songs");
		BasicDBObject query = new BasicDBObject();
		
		query.put("_id", new ObjectId(songId));
		FindIterable<Document> cursor = songs.find(query);
        
		if (cursor.first() != null) { 
        	//song found
			songs.deleteOne(new Document("_id", new ObjectId(songId)));
        	return new DbQueryStatus("Deleted song", DbQueryExecResult.QUERY_OK);
        }
		return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		Song song = db.findById(songId, Song.class);
		if (song == null) { 
        	//song not found
        	return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
		//else found
        if (shouldDecrement) {
        	if(song.getSongAmountFavourites() == 0) {
        		//Decrement something that has no favs
        		return new DbQueryStatus("Cannot decrement count", DbQueryExecResult.QUERY_ERROR_GENERIC);
        	}
        	song.setSongAmountFavourites(song.getSongAmountFavourites() - 1);
        	db.save(song);
            DbQueryStatus status = new DbQueryStatus("Updated favorites", DbQueryExecResult.QUERY_OK);
            
            return status;
        } else {
        	//else increment
        	song.setSongAmountFavourites(song.getSongAmountFavourites() + 1);
        	DbQueryStatus status = new DbQueryStatus("Updated favorites", DbQueryExecResult.QUERY_OK);
        	db.save(song);
        	
        	return status;
        }
	}
}