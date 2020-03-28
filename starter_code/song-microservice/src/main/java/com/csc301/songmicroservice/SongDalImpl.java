package com.csc301.songmicroservice;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;

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
		
		//document.put("id", songToAdd._id.get());

//may be useful for something else later
//		BasicDBObject documentDetail = new BasicDBObject();
//		documentDetail.put("addressLine1", "Sweet Home");
//		documentDetail.put("addressLine2", "Karol Bagh");
//		documentDetail.put("addressLine3", "New Delhi, India");
//		 
//		document.put("address", documentDetail);
		 
		db.getCollection("songs").insertOne(document);
		
		rtn = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		rtn.setData(document); //unless its "data" : all the puts
		return rtn;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// TODO Auto-generated method stub
		return null;
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