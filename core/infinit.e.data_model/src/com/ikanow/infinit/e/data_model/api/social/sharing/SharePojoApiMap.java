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
package com.ikanow.infinit.e.data_model.api.social.sharing;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ikanow.infinit.e.data_model.api.BaseApiPojo;
import com.ikanow.infinit.e.data_model.api.BasePojoApiMap;
import com.ikanow.infinit.e.data_model.store.social.sharing.SharePojo;
import com.ikanow.infinit.e.data_model.store.social.sharing.SharePojo.ShareCommunityPojo;

public class SharePojoApiMap implements BasePojoApiMap<SharePojo> {

	public SharePojoApiMap(Set<ObjectId> allowedCommunityIds) {
		_allowedCommunityIds = allowedCommunityIds;
	}
	private Set<ObjectId> _allowedCommunityIds = null;
	
	public GsonBuilder extendBuilder(GsonBuilder gp) {
		return gp.registerTypeAdapter(SharePojo.class, new SharePojoSerializer(_allowedCommunityIds));
	}
	protected static class SharePojoSerializer implements JsonSerializer<SharePojo> 
	{
		private Set<ObjectId> _allowedCommunityIds = null;
		SharePojoSerializer(Set<ObjectId> allowedCommunityIds) {
			_allowedCommunityIds = allowedCommunityIds;
		}
		@Override
		public JsonElement serialize(SharePojo share, Type typeOfT, JsonSerializationContext context)
		{
			List<ShareCommunityPojo> communities = share.getCommunities();
			if ((null != communities) && (null != _allowedCommunityIds)) {
				Iterator<ShareCommunityPojo> commIt = communities.iterator();
				while (commIt.hasNext()) {
					if (!_allowedCommunityIds.contains(commIt.next().get_id())) {
						commIt.remove();
							// (ok we're "corrupting" the share pojo here to make life easy)
					}
				}
				if (communities.isEmpty()) { // Somehow a security error has occurred
					//Exception out and hope for the best!
					throw new RuntimeException("Insufficient access permissions on this object");					
				}
			}
			JsonElement json = BaseApiPojo.getDefaultBuilder().create().toJsonTree(share);
			return json;
		}		
	}
}//TESTED