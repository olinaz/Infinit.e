/*******************************************************************************
 * Copyright 2012, The Infinit.e Open Source Project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.ikanow.infinit.e.processing.generic.aggregation;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

//import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.elasticsearch.common.Hex;

import com.ikanow.infinit.e.data_model.index.ElasticSearchManager;
import com.ikanow.infinit.e.data_model.index.IndexManager;
import com.ikanow.infinit.e.data_model.index.feature.event.AssociationFeaturePojoIndexMap;
import com.ikanow.infinit.e.data_model.store.DbManager;
import com.ikanow.infinit.e.data_model.store.MongoDbManager;
import com.ikanow.infinit.e.data_model.store.document.AssociationPojo;
import com.ikanow.infinit.e.data_model.store.feature.association.AssociationFeaturePojo;
import com.ikanow.infinit.e.data_model.store.feature.entity.EntityFeaturePojo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class AssociationAggregationUtils {

	// Not currently needed (will be when add document-event frequency updates)
	private static final Logger logger = Logger.getLogger(AssociationAggregationUtils.class);	
	
	private static boolean _diagnosticMode = false;
	public static void setDiagnosticMode(boolean bMode) { _diagnosticMode = bMode; }
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	

	// PUBLIC UTILITIES
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	

	public static String getEventFeatureIndex(AssociationPojo evt) {
		//calculate event_index
		StringBuffer preHashIndex = new StringBuffer();
		preHashIndex.append(evt.getEntity1_index()).append('/').append(evt.getVerb_category()).append('/').append(evt.getEntity2_index());
		if (null != evt.getGeo_index()) {
			preHashIndex.append(evt.getGeo_index());
		}
		return md5checksum(preHashIndex.toString());		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////	

	private static String md5checksum(String toHash)
	{
		try
		{
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(toHash.getBytes(Charset.forName("UTF8")));
			byte[] digest = m.digest();
			return new String(Hex.encodeHex(digest));			
		}
		catch (Exception ex)
		{
			return toHash;
		}		
	}//(TESTED - cut and paste from proven PasswordEncryption class)
	
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	

	// CREATING/UPDATE FEATURES
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * Add events to the elastic search index for events
	 * and the mongodb collection
	 * so they are searchable for searchsuggest
	 * 
	 * Step 1.a, try to just update alias's
	 * Step 1.b, if fail, create new entry
	 * 
	 * Step 2, Update totalfreq and doccount
	 * 
	 * Step 3, After updating totalfreq and doccount, write to ES for every group
	 * 
	 * @param events
	 */
	public static void updateEventFeatures(Map<String, Map<ObjectId, AssociationFeaturePojo>> eventFeatures)
	{
		DBCollection col = DbManager.getFeature().getAssociation();	
		String savedSyncTime = null;
		for (Map<ObjectId, AssociationFeaturePojo> evtCommunity: eventFeatures.values()) {
			
			Iterator<Map.Entry<ObjectId, AssociationFeaturePojo>> it = evtCommunity.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<ObjectId, AssociationFeaturePojo> evtFeatureKV = it.next();				
				try {					
					AssociationFeaturePojo evtFeature = evtFeatureKV.getValue();
					long nSavedDocCount = evtFeature.getDoccount();

					ObjectId communityID = evtFeature.getCommunityId();
					//try to update
					BasicDBObject query = new BasicDBObject(AssociationFeaturePojo.index_, evtFeature.getIndex());
					query.put(AssociationFeaturePojo.communityId_, communityID);

					//Step1 try to update alias
					//update arrays
					BasicDBObject multiopAliasArrays = new BasicDBObject();											
					// Entity1 Alias:
					if (null != evtFeature.getEntity1_index()) {
						evtFeature.addEntity1(evtFeature.getEntity1_index());
					}
					if (null != evtFeature.getEntity1()) 
					{
						BasicDBObject multiopE = new BasicDBObject(MongoDbManager.each_, evtFeature.getEntity1());
						multiopAliasArrays.put(AssociationFeaturePojo.entity1_, multiopE);
					}
					// Entity2 Alias:
					if (null != evtFeature.getEntity2_index()) {
						evtFeature.addEntity2(evtFeature.getEntity2_index());
					}
					if (null != evtFeature.getEntity2()) {
						BasicDBObject multiopE = new BasicDBObject(MongoDbManager.each_, evtFeature.getEntity2());
						multiopAliasArrays.put(AssociationFeaturePojo.entity2_, multiopE);
					}
					// verb/verb cat alias:
					if (null != evtFeature.getVerb_category()) {
						evtFeature.addVerb(evtFeature.getVerb_category());
					}
					if (null != evtFeature.getVerb()) {
						BasicDBObject multiopE = new BasicDBObject(MongoDbManager.each_, evtFeature.getVerb());
						multiopAliasArrays.put(AssociationFeaturePojo.verb_, multiopE);
					}
					BasicDBObject updateOp = new BasicDBObject();	
					updateOp.put(MongoDbManager.addToSet_, multiopAliasArrays);
					// Document count for this event
					BasicDBObject updateFreqDocCount = new BasicDBObject(AssociationFeaturePojo.doccount_, nSavedDocCount);
					updateOp.put(MongoDbManager.inc_,updateFreqDocCount);

					BasicDBObject fields = new BasicDBObject(AssociationFeaturePojo.doccount_, 1);
					fields.put(AssociationFeaturePojo.entity1_, 1);
					fields.put(AssociationFeaturePojo.entity2_, 1);
					fields.put(AssociationFeaturePojo.verb_, 1);
					//(slightly annoying, since only want these if updating dc but won't know
					// until after i've got this object)

					fields.put(AssociationFeaturePojo.db_sync_time_, 1);
					fields.put(AssociationFeaturePojo.db_sync_doccount_, 1);

					DBObject dboUpdate = null;
					if (_diagnosticMode) {
						dboUpdate = col.findOne(query,fields);
					}
					else {
						dboUpdate = col.findAndModify(query,fields,new BasicDBObject(),false,updateOp,false,true);	
							// (can use findAndModify because specify index, ie the shard key)
							// (returns event before the changes above, update the feature object below)
							// (also atomically creates the object if it doesn't exist so is "distributed-safe")
					}
					if ( ( dboUpdate != null ) && !dboUpdate.keySet().isEmpty() )
					{
						AssociationFeaturePojo egp = AssociationFeaturePojo.fromDb(dboUpdate, AssociationFeaturePojo.class);
						evtFeature.setDoccount(egp.getDoccount() + nSavedDocCount);
						evtFeature.setDb_sync_doccount(egp.getDb_sync_doccount());
						evtFeature.setDb_sync_time(egp.getDb_sync_time());
						if (null != egp.getEntity1()) {
							for (String ent: egp.getEntity1()) evtFeature.addEntity1(ent);
						}
						if (null != egp.getEntity2()) {	
							for (String ent: egp.getEntity2()) evtFeature.addEntity2(ent);
						}
						if (null != egp.getVerb()) {
							for (String verb: egp.getVerb()) evtFeature.addVerb(verb);
						}							
						if (_diagnosticMode) {
							System.out.println("EventAggregationUtils.updateEventFeatures, found: " + ((BasicDBObject)egp.toDb()).toString());
							System.out.println("EventAggregationUtils.updateEventFeatures, ^^^ found from query: " + query.toString() + " / " + updateOp.toString());
						}
						// (In background aggregation mode we update db_sync_prio when checking the -otherwise unused, unlike entities- document update schedule) 
					}
					else // (the object in memory is now an accurate representation of the database, minus some fields we'll now add)
					{
						// Synchronization settings for the newly created object
						evtFeature.setDb_sync_doccount(nSavedDocCount);
						if (null == savedSyncTime) {
							savedSyncTime = Long.toString(System.currentTimeMillis());
						}
						evtFeature.setDb_sync_time(savedSyncTime);

						// This is all "distributed safe" (apart from the db_syc_xxx and it doesn't matter if that is 
						// out of date, the update will just be slightly out-of-date at worst) since (otherwise) these fields are 
						// only set here, and the findAndModify is atomic

						BasicDBObject baseFields = new BasicDBObject();
						if (null != evtFeature.getEntity1_index()) {
							baseFields.put(AssociationFeaturePojo.entity1_index_, evtFeature.getEntity1_index());
						}
						if (null != evtFeature.getEntity2_index()) {
							baseFields.put(AssociationFeaturePojo.entity2_index_, evtFeature.getEntity2_index());								
						}
						if (null != evtFeature.getVerb_category()) {
							baseFields.put(AssociationFeaturePojo.verb_category_, evtFeature.getVerb_category());																
						}
						baseFields.put(AssociationFeaturePojo.assoc_type_, evtFeature.getAssociation_type());
						baseFields.put(AssociationFeaturePojo.db_sync_doccount_, evtFeature.getDb_sync_doccount());
						baseFields.put(AssociationFeaturePojo.db_sync_time_, evtFeature.getDb_sync_time());														

						if (!_diagnosticMode) {
							// Store the object
							col.update(query, new BasicDBObject(MongoDbManager.set_, baseFields));
						}
						else {
							System.out.println("EventAggregationUtils.updateEventFeatures, not found: " + query.toString() + " / " + baseFields.toString() + "/ orig_update= " + updateOp.toString());
						}
						evtFeature.setDb_sync_time(null); // (ensures that index re-sync will occur)						
						
						// (Note even in background aggregation mode we still perform the feature synchronization
						//  for new entities - and it has to be right at the end because it "corrupts" the objects)
					}
				}
				catch (Exception e) {
					// Exception, remove from feature list
					it.remove();
					
					// If an exception occurs log the error
					logger.error("Exception Message: " + e.getMessage(), e);
				}				
				
			}// (end loop over all communities for the set of features sharing and index)								
		}// (end loop over indexes) 

	}//TESTED (by eye, reasonably significant changes, but still based on proven Beta code)
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	

	// DOCUMENT UPDATE
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	

	public static void updateMatchingEvents(AssociationFeaturePojo evtFeature, ObjectId communityId)
	{
		//(Not needed until we start to calculate event significance)
	}	
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	

	// INDEX SYNCHRONIZATION
	
	///////////////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////////////////////////////////////////////////////////////	

	// Synchronization: sync vs index, update sync counts for each community
	
	public static void synchronizeEventFeature(AssociationFeaturePojo eventFeature, ObjectId communityId) {
		DBCollection eventFeatureDb = DbManager.getFeature().getAssociation();
		
		// NOTE: Important that feeds update occurs before synchronization, since the sync "corrupts" the event		

		if (_diagnosticMode || (null != eventFeature.getDb_sync_time())) { // Else this is a new feature so don't need to update the feature DB, only the index
			long nCurrTime = System.currentTimeMillis();
			//(query from top of the function, basically lookup on gaz_index)
			BasicDBObject update2 = new BasicDBObject();
			update2.put(AssociationFeaturePojo.db_sync_time_, Long.toString(nCurrTime));
			update2.put(AssociationFeaturePojo.db_sync_doccount_, eventFeature.getDoccount());
			BasicDBObject update = new BasicDBObject(MongoDbManager.set_, update2);
				// (also can be added to below)
			BasicDBObject update3 = new BasicDBObject(EntityFeaturePojo.db_sync_prio_, 1);
			update.put(MongoDbManager.unset_, update3);
			BasicDBObject query = new BasicDBObject(AssociationFeaturePojo.index_, eventFeature.getIndex());
			query.put(AssociationFeaturePojo.communityId_, communityId);

			// Keep the number of entity1 and entity2 sets down to a reasonable number
			// (In the end would like to be able to do this based on date rather than (essentially) completely randomly)
			int nSize;
			BasicDBObject toPull = null;
			if (null != eventFeature.getEntity1()) {
				if ((nSize = eventFeature.getEntity1().size()) > AssociationFeaturePojo.entity_MAXFIELDS) {
					if (null == toPull) toPull = new BasicDBObject();
					ArrayList<String> ent1ToRemove = new ArrayList<String>(eventFeature.getEntity1().size() - AssociationFeaturePojo.entity_MAXFIELDS);
					Iterator<String> it = eventFeature.getEntity1().iterator();
					while (it.hasNext() && (nSize > AssociationFeaturePojo.entity_MAXFIELDS)) {
						String ent = it.next();
						if (-1 == ent.indexOf('/')) { // (ie don't remove the index)
							nSize--;
							it.remove(); // (this removes from the index)
							ent1ToRemove.add(ent);
						}
					}
					toPull.put(AssociationFeaturePojo.entity1_, ent1ToRemove);
						// (this removes from the database)
				}
			}
			if (null != eventFeature.getEntity2()) {
				if ((nSize = eventFeature.getEntity2().size()) > AssociationFeaturePojo.entity_MAXFIELDS) {
					if (null == toPull) toPull = new BasicDBObject();
					ArrayList<String> ent2ToRemove = new ArrayList<String>(eventFeature.getEntity2().size() - AssociationFeaturePojo.entity_MAXFIELDS);
					Iterator<String> it = eventFeature.getEntity2().iterator();
					while (it.hasNext() && (nSize > AssociationFeaturePojo.entity_MAXFIELDS)) {
						String ent = it.next();
						if (-1 == ent.indexOf('/')) { // (ie don't remove the index)
							nSize--;
							it.remove(); // (this removes from the index)
							ent2ToRemove.add(ent);
						}
					}
					toPull.put(AssociationFeaturePojo.entity2_, ent2ToRemove);
						// (this removes from the database)
				}
			}
			if (null != toPull) {
				update.put(MongoDbManager.pullAll_, toPull);
					// (this removes from the database)
			}
			//TESTED (2.1.4.3b, including no index removal clause)
			
			if (_diagnosticMode) {
				System.out.println("EventAggregationUtils.synchronizeEventFeature, featureDB: " + query.toString() + " / " + update.toString());				
			}
			else {
				eventFeatureDb.update(query, update, false, true);
			}
		}

		if (_diagnosticMode) {
			System.out.println("EventAggregationUtils.synchronizeEventFeature, synchronize: " + new StringBuffer(eventFeature.getIndex()).append(':').append(communityId).toString() + " = " + 
					IndexManager.mapToIndex(eventFeature, new AssociationFeaturePojoIndexMap()));
		}
		else {
			ElasticSearchManager esm = IndexManager.getIndex(AssociationFeaturePojoIndexMap.indexName_);				
			esm.addDocument(eventFeature, new AssociationFeaturePojoIndexMap(), null, true);
		}
	}//TESTED
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	// Set flag to synchronize entity features
	
	public static void markAssociationFeatureForSync(AssociationFeaturePojo assocFeature, ObjectId communityId) {
		DBCollection assocFeatureDb = DbManager.getFeature().getAssociation();
		double dPrio = 100.0*(double)assocFeature.getDoccount()/(0.01 + (double)assocFeature.getDb_sync_doccount());
		assocFeature.setDb_sync_prio(dPrio);
		BasicDBObject query = new BasicDBObject(AssociationFeaturePojo.index_, assocFeature.getIndex());
		query.put(AssociationFeaturePojo.communityId_, communityId);
		BasicDBObject update = new BasicDBObject(MongoDbManager.set_, new BasicDBObject(AssociationFeaturePojo.db_sync_prio_, dPrio));
		if (_diagnosticMode) {
			System.out.println("EntityAggregationUtils.markAssociationFeatureForSync, featureDB: " + query.toString() + " / " + update.toString());				
		}
		else {
			assocFeatureDb.update(query, update, false, true);
		}
	}//TESTED
}
