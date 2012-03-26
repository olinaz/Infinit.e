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
package com.ikanow.infinit.e.data_model.index.feature.event;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ikanow.infinit.e.data_model.index.BasePojoIndexMap;
import com.ikanow.infinit.e.data_model.index.ElasticSearchPojos;
import com.ikanow.infinit.e.data_model.store.feature.association.AssociationFeaturePojo;

public class AssociationFeaturePojoIndexMap implements BasePojoIndexMap<AssociationFeaturePojo> {

	// Misc access constants:
	final public static String indexName_ = "association_index";
	
	@Override
	public GsonBuilder extendBuilder(GsonBuilder gp) {
		return gp.registerTypeAdapter(AssociationFeaturePojo.class, new EventFeaturePojoSerializer());
	}
	
	/////////////////////////////////////////////////////////////////////////////////////

	// Index synchronization
	
	protected static class EventFeaturePojoSerializer implements JsonSerializer<AssociationFeaturePojo> 
	{
		@Override
		public JsonElement serialize(AssociationFeaturePojo evt, Type typeOfT, JsonSerializationContext context)
		{
			String sIndex = evt.getIndex();
			String sCommunity = evt.getCommunityId().toString();
			synchronizeWithIndex(evt);			
			JsonElement jo = new GsonBuilder().create().toJsonTree(evt, typeOfT);			
			jo.getAsJsonObject().add("communityId", new JsonPrimitive(sCommunity));
			jo.getAsJsonObject().add("_id", new JsonPrimitive(new StringBuffer(sIndex).append(':').append(sCommunity).toString()));
				// (use an _id of index:community in elasticsearch)
			
			return jo;		
		}
		private static void synchronizeWithIndex(AssociationFeaturePojo evt) {
			evt.setCommunityId(null);
			evt.setIndex(null); // (both these are set post serialization, above)
			
			evt.setDb_sync_doccount(null);
			evt.setDb_sync_time(null);

			//TODO (INF-1234) Eventually want to index this information, but it's currently not clear what to do with
			// it inside the assocSuggest (which is hte only place it's currently used)
			if (null == evt.getEntity1_index()) {
				evt.setEntity1(null);
			}
			if (null == evt.getEntity2_index()) {
				evt.setEntity2(null);
			}
}	
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	// Elastic Search mapping
	
	public static class Mapping
	{
		public static class RootObject 
		{
			ElasticSearchPojos.SourcePojo _source = new ElasticSearchPojos.SourcePojo(false, null, null);
			ElasticSearchPojos.AllPojo _all = new ElasticSearchPojos.AllPojo(true);
			
			public static class RootProperties 
			{
				ElasticSearchPojos.FieldStringPojo entity1 = new ElasticSearchPojos.FieldStringPojo("yes", "analyzed", null).setAnalyzer("suggestAnalyzer");
				ElasticSearchPojos.FieldStringPojo entity1_index = new ElasticSearchPojos.FieldStringPojo("yes", "not_analyzed", null);
				ElasticSearchPojos.FieldStringPojo verb = new ElasticSearchPojos.FieldStringPojo("yes", "analyzed", null).setAnalyzer("suggestAnalyzer");
				ElasticSearchPojos.FieldStringPojo verb_category = new ElasticSearchPojos.FieldStringPojo("yes", "not_analyzed", null);
				ElasticSearchPojos.FieldStringPojo entity2 = new ElasticSearchPojos.FieldStringPojo("yes", "analyzed", null).setAnalyzer("suggestAnalyzer");
				ElasticSearchPojos.FieldStringPojo entity2_index = new ElasticSearchPojos.FieldStringPojo("yes", "not_analyzed", null);
				ElasticSearchPojos.FieldStringPojo geo_index = new ElasticSearchPojos.FieldStringPojo("yes", "not_analyzed", null);
				ElasticSearchPojos.FieldStringPojo assoc_type = new ElasticSearchPojos.FieldStringPojo("yes", "not_analyzed", null);
				ElasticSearchPojos.FieldLongPojo totalfreq = new ElasticSearchPojos.FieldLongPojo("yes", null, null);
				ElasticSearchPojos.FieldLongPojo doccount = new ElasticSearchPojos.FieldLongPojo("yes", null, null);
				ElasticSearchPojos.FieldStringPojo communityId = new ElasticSearchPojos.FieldStringPojo("yes", "not_analyzed", null);
			}
			
			RootProperties properties = new RootProperties();
		} 
		
		RootObject event_index = new RootObject();
	}
}