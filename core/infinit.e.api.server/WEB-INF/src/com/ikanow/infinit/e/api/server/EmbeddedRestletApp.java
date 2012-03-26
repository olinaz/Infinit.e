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
package com.ikanow.infinit.e.api.server;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.restlet.Application;  
import org.restlet.Restlet;  
import org.restlet.Router; 
import org.restlet.resource.Resource;

import com.ikanow.infinit.e.api.authentication.LoginInterface;
import com.ikanow.infinit.e.api.config.source.SourceInterface;
import com.ikanow.infinit.e.api.custom.mapreduce.CustomInterface;
import com.ikanow.infinit.e.api.gui.UIInterface;
import com.ikanow.infinit.e.api.knowledge.DocumentInterface;
import com.ikanow.infinit.e.api.knowledge.QueryInterface;
import com.ikanow.infinit.e.api.knowledge.SearchInterface;
import com.ikanow.infinit.e.api.social.community.CommunityInterface;
import com.ikanow.infinit.e.api.social.community.PersonInterface;
import com.ikanow.infinit.e.api.social.sharing.ShareInterface;
import com.ikanow.infinit.e.data_model.Globals;

public class EmbeddedRestletApp extends Application 
{
    /** 
     * Creates a root Restlet that will receive all incoming calls. 
     */  
    @Override  
    public Restlet createRoot() { 
		Globals.setIdentity(Globals.Identity.IDENTITY_API);
		String configpath = null;
		// First try override from system
		if (null == (configpath = (String) System.getProperties().get("INFINITE_CONFIG_HOME"))) { 
			configpath = getContext().getParameters().getFirstValue("configpath");
			// Then get override from build.xml
		}
    	if(configpath != null) {
    		Globals.overrideConfigLocation(configpath);
    	}
    	PropertyConfigurator.configure(Globals.getLogPropertiesLocation());
    	
    	Logger logger = Logger.getLogger(EmbeddedRestletApp.class);
   
    	if (logger.getEffectiveLevel() != Level.DEBUG)
        {
        	java.util.logging.Logger.getLogger("org.restlet.Component.LogService").setLevel(java.util.logging.Level.OFF);
        }
        // Create a router Restlet that routes each call to a  
        // new instance of HelloWorldResource.
    	
        Router router = new Router(getContext());

        // Might Want to add something in here for Trusted Routing (IP ADDRESS CAPTURE ETC)
        
        /*
         * All responses will be returned in either JSON or XML 
         * format eg wt=json or wt=xml and will conform the 
         * Lucene / Solr format
         * 
         * all search function will act as a proxy to solr
         * which will run in a seperate tomcat process
         * 
         * all requests must include a API key that is in the
         * form of guid eg api=__GUID__
         * 
         * all posts will require authentication 
         * 
         * the remainder of the api will be modeled after flickr
         * 
         */
        
        /***************************************************
         ** Authentication
         ** 
         ****************************************************/
        
// AUTHENTICATION        
        
        attach(router, "/auth/login",LoginInterface.class);
        attach(router, "/auth/login/{user}/{pass}", LoginInterface.class); 
        attach(router, "/auth/login/admin/{user}/{pass}",LoginInterface.class);
        attach(router, "/auth/keepalive",LoginInterface.class);
        attach(router, "/auth/logout",LoginInterface.class);
        attach(router, "/auth/forgotpassword",LoginInterface.class);
        attach(router, "/auth/deactivate",LoginInterface.class);
        
        /***************************************************
        ** Define the knowledge search routes 
        ** (we can either call this knowledge or search 
        ** or something else that makes sense) 
        ** also thinking about making the default the feeds
        ****************************************************/
        
// SOURCES:
        
        // we should not allow this api call, was just for testing
        //BETA NAMING
        router.attach("/knowledge/sources/{source}", SourceInterface.class);
        router.attach("/knowledge/sources/save/{communityid}/", SourceInterface.class);
        router.attach("/knowledge/sources/add/{sourceurl}/{sourcetitle}/{sourcedesc}/{extracttype}/{sourcetags}/{mediatype}/{communityid}", SourceInterface.class);  
        router.attach("/knowledge/sources/approve/{source}/{communityid}/", SourceInterface.class);   
        router.attach("/knowledge/sources/decline/{source}/{communityid}",SourceInterface.class);
        router.attach("/knowledge/sources/good/{communityid}", SourceInterface.class);  
        router.attach("/knowledge/sources/bad/{communityid}", SourceInterface.class);  
        router.attach("/knowledge/sources/pending/{communityid}",SourceInterface.class);
        //VO NAMING
        attach(router, "/config/source/get/{sourceid}", SourceInterface.class);
        attach(router, "/config/source/save/{communityid}", SourceInterface.class);
        attach(router, "/config/source/add/{sourceurl}/{sourcetitle}/{sourcedesc}/{extracttype}/{sourcetags}/{mediatype}/{communityid}", SourceInterface.class);  
        attach(router, "/config/source/approve/{sourceid}/{communityid}", SourceInterface.class);   
        attach(router, "/config/source/decline/{sourceid}/{communityid}",SourceInterface.class);
        attach(router, "/config/source/good/{communityid}", SourceInterface.class);  
        attach(router, "/config/source/bad/{communityid}", SourceInterface.class);  
        attach(router, "/config/source/pending/{communityid}",SourceInterface.class);
        attach(router, "/config/source/test",SourceInterface.class);
        attach(router, "/config/source/delete/{sourceid}/{communityid}",SourceInterface.class);
        attach(router, "/config/source/delete/docs/{sourceid}/{communityid}",SourceInterface.class);
        
  //CUSTOM MAP REDUCE
        //BETA NAMING
        router.attach("/knowledge/mapreduce/schedulejob/{jobtitle}/{jobdesc}/{communityIds}/{jarURL}/{timeToRun}/{frequencyToRun}/{mapperClass}/{reducerClass}/{combinerClass}/{query}/{inputcollection}/{outputKey}/{outputValue}", CustomInterface.class);
        router.attach("/knowledge/mapreduce/getresults/{jobid}", CustomInterface.class);
        router.attach("/knowledge/mapreduce/getjobs", CustomInterface.class);
        router.attach("/knowledge/mapreduce/{inputcollection}/{map}/{reduce}/{query}", CustomInterface.class);          
        //VO NAMING
        attach(router, "/custom/mapreduce/schedulejob/{jobtitle}/{jobdesc}/{communityIds}/{jarURL}/{timeToRun}/{frequencyToRun}/{mapperClass}/{reducerClass}/{combinerClass}/{query}/{inputcollection}/{outputKey}/{outputValue}", CustomInterface.class);
        attach(router, "/custom/mapreduce/getresults/{jobid}", CustomInterface.class);
        attach(router, "/custom/mapreduce/getjobs", CustomInterface.class);
        attach(router, "/custom/mapreduce/{inputcollection}/{map}/{reduce}/{query}", CustomInterface.class);          
              
// SEARCHES:
        //BETA NAMING
        router.attach("/knowledge/searchSuggest/{term}/{communityids}",SearchInterface.class);
        router.attach("/knowledge/searchEventSuggest/{ent1}/{verb}/{ent2}/{field}/{communityids}",SearchInterface.class);           
        router.attach("/knowledge/aliasSuggest/{field}/{term}/{communityids}",SearchInterface.class);           
        router.attach("/knowledge/doc/{feedid}/{communityids}", DocumentInterface.class);
        router.attach("/knowledge/feed/{feedid}/{communityids}", DocumentInterface.class);
        //VO NAMING
        attach(router, "/knowledge/query/{communityids}",QueryInterface.class); 
        attach(router, "/knowledge/feature/geoSuggest/{term}/{communityids}", SearchInterface.class); //UNTESTED
        attach(router, "/knowledge/feature/entitySuggest/{term}/{communityids}",SearchInterface.class);
        attach(router, "/knowledge/feature/aliasSuggest/{field}/{term}/{communityids}",SearchInterface.class);        
        attach(router, "/knowledge/document/get/{feedid}", DocumentInterface.class);  
        attach(router, "/knowledge/document/query/{communityids}",QueryInterface.class);    
        attach(router, "/knowledge/feature/eventSuggest/{ent1}/{verb}/{ent2}/{field}/{communityids}",SearchInterface.class);
        	// (This is obsolete but leave in for another couple of releases)
        attach(router, "/knowledge/feature/assocSuggest/{ent1}/{verb}/{ent2}/{field}/{communityids}",SearchInterface.class);
        
// UISETUP:
        
        //BETA NAMING
        router.attach("/knowledge/uisetup/get/", UIInterface.class);   
        router.attach("/knowledge/uisetup/update/{modules}/{query}/{communityids}", UIInterface.class);   
        router.attach("/knowledge/uisetup/update/{modules}/{communityids}", UIInterface.class);   //POST version        
        router.attach("/knowledge/uisetup/modules/get/",UIInterface.class);   
        router.attach("/knowledge/uisetup/modules/user/get", UIInterface.class);   
        router.attach("/knowledge/uisetup/modules/user/set/{modules}",UIInterface.class);  
        router.attach("/knowledge/uisetup/modules/search/{term}", UIInterface.class);   
        router.attach("/knowledge/uisetup/modules/install/",UIInterface.class); // (query is POSTed)   
        router.attach("/knowledge/uisetup/modules/delete/{modules}",UIInterface.class); // (actually {} is the module id, named group to make life simple in resource)   
        //V0 NAMING
        attach(router, "/social/gui/uisetup/get", UIInterface.class);   
        attach(router, "/social/gui/uisetup/update/{modules}/{query}/{communityids}", UIInterface.class);   
        attach(router, "/social/gui/uisetup/update/{modules}/{communityids}", UIInterface.class);   //POST version        
        attach(router, "/social/gui/modules/get",UIInterface.class);   
        attach(router, "/social/gui/modules/user/get", UIInterface.class);
        attach(router, "/social/gui/modules/user/set/{modules}",UIInterface.class);  
        attach(router, "/social/gui/modules/search/{term}", UIInterface.class);   
        attach(router, "/social/gui/modules/install",UIInterface.class); // (query is POSTed)   
        attach(router, "/social/gui/modules/delete/{modules}",UIInterface.class); // (actually {} is the module id, named group to make life simple in resource)   
                
        /***************************************************
        ** Define the social routes
        ****************************************************/
                
// COMMUNITIES: 
        
        // Get - list of communities
        //BETA NAMING
        router.attach("/community/get/{communityid}", CommunityInterface.class);   
        router.attach("/community/getall/", CommunityInterface.class);
        router.attach("/community/getsystem/", CommunityInterface.class);
        router.attach("/community/getpublic/", CommunityInterface.class);
        router.attach("/community/getprivate/", CommunityInterface.class); 
        //V0 NAMING
        attach(router, "/social/community/get/{communityid}", CommunityInterface.class);   
        attach(router, "/social/community/getall", CommunityInterface.class);
        attach(router, "/social/community/getsystem", CommunityInterface.class);
        attach(router, "/social/community/getpublic", CommunityInterface.class);
        attach(router, "/social/community/getprivate", CommunityInterface.class); 
        
        // Add/update/remove communities
        //BURCH changed the add community method to only allow a user adding it to himself, the
        //expanded function will be left in CommunityController for when we need to handle that
        //BETA NAMING
        router.attach("/community/add/{name}/{description}/{tags}/",CommunityInterface.class);
        router.attach("/community/add/{name}/{description}/{tags}/{parentid}/",CommunityInterface.class);
        router.attach("/community/remove/{id}",CommunityInterface.class);
        router.attach("/community/update/{communityid}/", CommunityInterface.class); 
        //V0 NAMING
        attach(router, "/social/community/add/{name}/{description}/{tags}",CommunityInterface.class);
        attach(router, "/social/community/add/{name}/{description}/{tags}/{parentid}",CommunityInterface.class);
        attach(router, "/social/community/remove/{id}",CommunityInterface.class);
        attach(router, "/social/community/update/{communityid}", CommunityInterface.class); 
        
        // Add/remove members, update their status or type
        //BETA NAMING
        router.attach("/community/member/update/status/{communityid}/{personid}/{userstatus}", CommunityInterface.class);
        router.attach("/community/member/update/type/{communityid}/{personid}/{usertype}", CommunityInterface.class);
        //V0 NAMING
        attach(router, "/social/community/member/update/status/{communityid}/{personid}/{userstatus}", CommunityInterface.class);
        attach(router, "/social/community/member/update/type/{communityid}/{personid}/{usertype}", CommunityInterface.class);
        
        // Join/leave a community, update community profile information
        //BETA NAMING
        router.attach("/community/member/join/{communityid}", CommunityInterface.class);  
        router.attach("/community/member/leave/{communityid}", CommunityInterface.class);
        router.attach("/community/member/invite/{communityid}/{personid}", CommunityInterface.class); 
        router.attach("/community/requestresponse/{requestid}/{response}", CommunityInterface.class);
        //V0 NAMING
        attach(router, "/social/community/member/join/{communityid}", CommunityInterface.class);  
        attach(router, "/social/community/member/leave/{communityid}", CommunityInterface.class);
        attach(router, "/social/community/member/invite/{communityid}/{personid}", CommunityInterface.class); 
        attach(router, "/social/community/requestresponse/{requestid}/{response}", CommunityInterface.class);

// PEOPLE/PERSON:
        
        // Person - Get
        //BETA NAMING
        router.attach("/person/get/", PersonInterface.class);
        router.attach("/person/get/{personid}", PersonInterface.class);
        //V0 NAMING
        attach(router, "/social/person/get", PersonInterface.class);
        attach(router, "/social/person/get/{personid}", PersonInterface.class);

        //WORDPRESS CALLS, wordpress still calls the old /people/ but i now route thru personResource
        //BETA NAMING
        router.attach("/people/register/{wpuser}/{wpauth}", PersonInterface.class);   
        router.attach("/people/wpupdate/{wpuser}/{wpauth}", PersonInterface.class);  
        router.attach("/people/register",PersonInterface.class);   
        router.attach("/people/wpupdate", PersonInterface.class);   
        //V0 NAMING
        attach(router, "/social/person/register/{wpuser}/{wpauth}", PersonInterface.class); //(REST version) 
        attach(router, "/social/person/register", PersonInterface.class); // (POST version)   
        attach(router, "/social/person/wpupdate/{wpuser}/{wpauth}", PersonInterface.class); //(legacy REST version)
        attach(router, "/social/person/update/{wpuser}/{wpauth}", PersonInterface.class); //(REST version)  
        attach(router, "/social/person/update", PersonInterface.class);  // (POST versions/URL params) 
        attach(router, "/social/person/wpupdate", PersonInterface.class); // (URL params)
        attach(router, "/social/person/delete/{userid}", PersonInterface.class);  // (GET) 
        attach(router, "/social/person/update/password/{wpuser}/{wpauth}", PersonInterface.class);  // (GET, user=userIdStr or WPUserID, wpauth=new password) 
        attach(router, "/social/person/update/email/{wpuser}/{wpauth}", PersonInterface.class);  // (GET, user=userIdStr or WPUserID, wpauth=new email) 
        //END WORDPRESS CALLS   
        
        //////////////////////////////Shares //////////////////////////////
        // Add/Update Shares
        //BETA NAMING
        router.attach("/share/add/binary/{title}/{description}/", ShareInterface.class);
        router.attach("/share/add/json/{type}/{title}/{description}/", ShareInterface.class);
        router.attach("/share/update/json/{id}/{type}/{title}/{description}/", ShareInterface.class);
        router.attach("/share/update/binary/{id}/{title}/{description}",ShareInterface.class); 
        router.attach("/share/add/ref/{type}/{documentid}/{title}/{description}/", ShareInterface.class);
        router.attach("/share/update/ref/{id}/{type}/{documentid}/{title}/{description}/", ShareInterface.class);
        //V0 NAMING
        attach(router, "/social/share/add/binary/{title}/{description}", ShareInterface.class);
        attach(router, "/social/share/update/binary/{id}/{title}/{description}",ShareInterface.class); 
        attach(router, "/social/share/save/json/{id}/{type}/{title}/{description}", ShareInterface.class);
        attach(router, "/social/share/add/json/{type}/{title}/{description}", ShareInterface.class);
        attach(router, "/social/share/update/json/{id}/{type}/{title}/{description}", ShareInterface.class);
        attach(router, "/social/share/add/ref/{type}/{documentid}/{title}/{description}", ShareInterface.class);
        attach(router, "/social/share/update/ref/{id}/{type}/{documentid}/{title}/{description}", ShareInterface.class);
        
        // Communities
        //BETA NAMING
        router.attach("/share/add/community/{shareid}/{comment}/{communityid}/", ShareInterface.class);
        router.attach("/share/remove/community/{shareid}/{communityid}/", ShareInterface.class);
        //V0 NAMING
        attach(router, "/social/share/add/community/{shareid}/{comment}/{communityid}", ShareInterface.class);
        attach(router, "/social/share/remove/community/{shareid}/{communityid}", ShareInterface.class);
        
        // Delete Share from DB
        //BETA NAMING
        router.attach("/share/remove/{shareid}/", ShareInterface.class);
        //V0 NAMING
        attach(router, "/social/share/remove/{shareid}", ShareInterface.class);
        
        // Get/Search for Shares
        //BETA NAMING
        router.attach("/share/get/{id}", ShareInterface.class);
        router.attach("/share/get/{id}/", ShareInterface.class);
        router.attach("/share/search/", ShareInterface.class);
        //V0 NAMING
        attach(router, "/social/share/get/{id}", ShareInterface.class);
        attach(router, "/social/share/search", ShareInterface.class);
        
        return router;  
    }
    static private void attach(Router router, String url,  Class<? extends Resource> clazz) {
    	if (url.endsWith("/")) {
    		throw new IllegalArgumentException("Trailing / is automatically added as option");
    	}
    	router.attach(url, clazz);
    	router.attach(url + "/", clazz);
    }
}  