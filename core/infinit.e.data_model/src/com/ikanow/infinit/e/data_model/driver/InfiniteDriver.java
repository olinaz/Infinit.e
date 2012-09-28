package com.ikanow.infinit.e.data_model.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.ikanow.infinit.e.data_model.api.ApiManager;
import com.ikanow.infinit.e.data_model.api.ResponsePojo;
import com.ikanow.infinit.e.data_model.api.ResponsePojo.ResponseObject;
import com.ikanow.infinit.e.data_model.api.authentication.WordPressAuthPojo;
import com.ikanow.infinit.e.data_model.api.authentication.WordPressSetupPojo;
import com.ikanow.infinit.e.data_model.api.authentication.WordPressUserPojo;
import com.ikanow.infinit.e.data_model.api.config.SourcePojoApiMap;
import com.ikanow.infinit.e.data_model.api.custom.mapreduce.CustomMapReduceResultPojo;
import com.ikanow.infinit.e.data_model.api.custom.mapreduce.CustomMapReduceResultPojoApiMap;
import com.ikanow.infinit.e.data_model.api.knowledge.AdvancedQueryPojo;
import com.ikanow.infinit.e.data_model.api.knowledge.DocumentPojoApiMap;
import com.ikanow.infinit.e.data_model.api.social.community.CommunityPojoApiMap;
import com.ikanow.infinit.e.data_model.api.social.person.PersonPojoApiMap;
import com.ikanow.infinit.e.data_model.api.social.sharing.SharePojoApiMap;
import com.ikanow.infinit.e.data_model.store.config.source.SourcePojo;
import com.ikanow.infinit.e.data_model.store.custom.mapreduce.CustomMapReduceJobPojo;
import com.ikanow.infinit.e.data_model.store.document.DocumentPojo;
import com.ikanow.infinit.e.data_model.store.social.community.CommunityPojo;
import com.ikanow.infinit.e.data_model.store.social.person.PersonPojo;
import com.ikanow.infinit.e.data_model.store.social.sharing.SharePojo;

public class InfiniteDriver 
{
	private static String DEFAULT_API_ROOT = null;
	private static String DEFAULT_USER = null;
	private static String DEFAULT_PASSWORD = null;
	
	private String apiRoot;
	private String user;
	private String password;
	
	private static String cookie = null;
	
	public InfiniteDriver()
	{
		apiRoot = DEFAULT_API_ROOT;
		user = DEFAULT_USER;
		password = DEFAULT_PASSWORD;
	}
	
	public InfiniteDriver(String apiRootUrl)
	{
		if (apiRootUrl == null)
			apiRoot = DEFAULT_API_ROOT;
		else
			apiRoot = apiRootUrl;
		
		user = DEFAULT_USER;
		password = DEFAULT_PASSWORD;
	}
	
	public InfiniteDriver(String apiRootUrl, String username, String unencryptedPassword)
	{
		if (apiRootUrl == null)
			apiRoot = DEFAULT_API_ROOT;
		else
			apiRoot = apiRootUrl;
		
		if (username == null || unencryptedPassword == null)
		{
			user = DEFAULT_USER;
			password = DEFAULT_PASSWORD;
		}
		else
		{
			user = username;
			password = unencryptedPassword;
		}
	}
	
	static public void setDefaultUser(String username)
	{
		DEFAULT_USER = username;	
	}
	
	static public void setDefaultPassword(String unencryptedPassword)
	{
		DEFAULT_PASSWORD = unencryptedPassword;
	}
	
	static public void setDefaultApiRoot(String rootUrl)
	{
		DEFAULT_API_ROOT = rootUrl;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// AUTHENTICATION
	
	/**
	 * Logs the user in specified by setUser() using the password
	 * specified by setPassword().
	 * @return true if the user was logged in successfully.
	 */
	public Boolean login()
	{
		return login(new ResponseObject());
	}
	
	public Boolean adminLogin()
	{
		return adminLogin(new ResponseObject());
	}

	/**
	 * Logs the user in specified by setUser() using the password
	 * specified by setPassword().
	 * @param responseObject this object will be said from the response of the API call
	 * @return true if the user was logged in successfully.
	 */
	public Boolean login(ResponseObject responseObject)
	{
		return login (user, password, responseObject);
	}
	
	public Boolean adminLogin(ResponseObject responseObject)
	{
		return adminLogin (user, password, responseObject);
	}

	/**
	 * Logs the user in with the specified username and password.
	 * This overrides the setUser and setPassword commands
	 * @param username The username to log in
	 * @param password the unencrypted password for the username
	 * @param responseObject this object will be said from the response of the API call
	 * @return true if the user was logged in successfully.
	 */
	public Boolean login(String username, String password, ResponseObject responseObject)
	{
		cookie = null;
		try {
			String address = apiRoot + "auth/login/" + username + "/" + encryptEncodePassword(password);
			String loginResult;
			loginResult = sendRequest(address, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(loginResult, ResponsePojo.class);
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			return responseObject.isSuccess();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public Boolean adminLogin(String username, String password, ResponseObject responseObject)
	{
		cookie = null;
		try {
			String address = apiRoot + "auth/login/admin/" + username + "/" + encryptEncodePassword(password);
			String loginResult;
			loginResult = sendRequest(address, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(loginResult, ResponsePojo.class);
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			return responseObject.isSuccess();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Logs the current logged in user out.
	 * @return true if logout was successful.
	 */
	public Boolean logout()
	{
		try {
			String address = apiRoot + "auth/logout/";
			String logoutResult;
			logoutResult = sendRequest(address, null);
			ResponsePojo response = ResponsePojo.fromApi(logoutResult, ResponsePojo.class);

			//cookie set to null so that new cookie will be grabbed on next login
			cookie = null;

			return response.getResponse().isSuccess();
		}
		catch (Exception e) 
		{
			cookie = null;
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// SOCIAL - COMMUNITIES
	
	/**
	 * createCommunity
	 * @param communityName
	 * @param communityDesc
	 * @param communityTag
	 * @param responsePojo
	 * @return CommunityPojo
	 */
	public CommunityPojo createCommunity(String communityName, String communityDesc, String communityTags,String parentid, ResponseObject responseObject )
	{
		try{
			String createCommunityAddress = apiRoot + "social/community/add/" + URLEncoder.encode(communityName,"UTF-8") + "/" +
					URLEncoder.encode(communityDesc,"UTF-8") + "/" + URLEncoder.encode(communityTags,"UTF-8");
			if (null != parentid)
				createCommunityAddress += "/" + URLEncoder.encode(parentid,"UTF-8");
			
			String communityresult = sendRequest(createCommunityAddress, null);

			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(communityresult, ResponsePojo.class, CommunityPojo.class, new CommunityPojoApiMap()); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			
			return (CommunityPojo)internal_responsePojo.getData();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public String addToCommunity(String communityId, String personId, ResponseObject responseObject )
	{
		try{
			String addToCommunityAddress = apiRoot + "social/community/member/invite/" + URLEncoder.encode(communityId,"UTF-8") + "/" +
					URLEncoder.encode(personId,"UTF-8") + "/";
			String inviteresult = sendRequest(addToCommunityAddress, null);

			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(inviteresult, ResponsePojo.class, CommunityPojo.class, new CommunityPojoApiMap()); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return responseObject.getMessage();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public String forcefullyAddToCommunity(String communityId, String personId, ResponseObject responseObject )
	{
		try{
			String addToCommunityAddress = apiRoot + "social/community/member/invite/" + URLEncoder.encode(communityId,"UTF-8") + "/" +
					URLEncoder.encode(personId,"UTF-8") + "/?skipinvitation=true";
			String inviteresult = sendRequest(addToCommunityAddress, null);

			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(inviteresult, ResponsePojo.class, CommunityPojo.class, new CommunityPojoApiMap()); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return responseObject.getMessage();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public CommunityPojo getCommunity(String communityIdentifier, ResponseObject responseObject )
	{
		try{
			String getCommunityAddress = apiRoot + "social/community/get/" + URLEncoder.encode(communityIdentifier,"UTF-8") + "";
			String getResult = sendRequest(getCommunityAddress, null);

			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(getResult, ResponsePojo.class, CommunityPojo.class, new CommunityPojoApiMap()); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return (CommunityPojo)internal_responsePojo.getData();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public List<CommunityPojo> getAllCommunity(ResponseObject responseObject)
	{
		try{
			String getCommunityAddress = apiRoot + "social/community/getall";
			String getResult = sendRequest(getCommunityAddress, null);
			
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(getResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			
			List<CommunityPojo> communities = null;
			
			communities = ApiManager.mapListFromApi((JsonElement)internal_responsePojo.getData(), 
															CommunityPojo.listType(), null);			
			
			return communities;
		}
		catch (Exception e) 
		{
			responseObject.setSuccess(false);
			responseObject.setMessage(e.getMessage());
		}		
		return null;
	}

	public Boolean deleteCommunity(String communityId, ResponseObject responseObject)
	{
		try{
			String addToCommunityAddress = apiRoot + "social/community/remove/" + URLEncoder.encode(communityId,"UTF-8");
			String inviteresult = sendRequest(addToCommunityAddress, null);

			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(inviteresult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return responseObject.isSuccess();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public Boolean updateCommunity(String communityId, CommunityPojo communityPojo, ResponseObject responseObject)
	{
		try{
			String addToCommunityAddress = apiRoot + "social/community/update/" + URLEncoder.encode(communityId,"UTF-8");
			String updateResult = sendRequest(addToCommunityAddress, new Gson().toJson(communityPojo));
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(updateResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return responseObject.isSuccess();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// SOCIAL - SHARES
	
	public List<SharePojo> searchShares(String searchCriteria, String searchString, String typeFilter, ResponseObject responseObject)
	{
		try {
			StringBuffer url = new StringBuffer(apiRoot).append("social/share/search/");
			if (null != searchCriteria) {
				url.append("?searchby=").append(searchCriteria).append("&id=").append(URLEncoder.encode(searchString, "UTF-8"));
			}
			if (null != typeFilter) {
				if (null != searchCriteria) {
					url.append("&");
				}
				else {
					url.append("?");				
				}
				url.append("type=").append(typeFilter);
			}
			String deleteResult = sendRequest(url.toString(), null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(deleteResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			
			List<SharePojo> shares = null;
			
			shares = ApiManager.mapListFromApi((JsonElement)internal_responsePojo.getData(), 
															SharePojo.listType(), null);			
			
			return shares;
		}
		catch (Exception e) {
			responseObject.setSuccess(false);
			responseObject.setMessage(e.getMessage());
		}
		return null;
	}
	//TOTEST
	
	public SharePojo getShare(String shareId, ResponseObject responseObject)
	{
		try{
			String getShareAddress = apiRoot + "social/share/get/" + URLEncoder.encode(shareId,"UTF-8");
			String getResult = sendRequest(getShareAddress, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(getResult, ResponsePojo.class, SharePojo.class, new SharePojoApiMap(null)); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return (SharePojo)internal_responsePojo.getData();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	
	public SharePojo addShareJSON(String title, String description, String type, String jsonString , ResponseObject responseObject)
	{
		try{
			String addShareAddress = apiRoot + "social/share/add/json/" + URLEncoder.encode(type, "UTF-8") + "/" + URLEncoder.encode(title,"UTF-8") + "/" + URLEncoder.encode(description,"UTF-8");
			String addResult = sendRequest(addShareAddress, jsonString);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(addResult, ResponsePojo.class, SharePojo.class, new SharePojoApiMap(null)); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return (SharePojo)internal_responsePojo.getData();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public Boolean updateShareJSON(String shareId, String title, String description, String type, String jsonString , ResponseObject responseObject)
	{
		try{
			String updateShareAddress = apiRoot + "social/share/update/json/" + shareId + "/" + URLEncoder.encode(type,"UTF-8") + "/" + URLEncoder.encode(title,"UTF-8") + "/" + URLEncoder.encode(description,"UTF-8");
			String updateResult = sendRequest(updateShareAddress, jsonString);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(updateResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return responseObject.isSuccess();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	public Boolean removeShare(String shareId, ResponseObject responseObject)
	{
		try{
			String removeShareAddress = apiRoot + "social/share/remove/" + URLEncoder.encode(shareId,"UTF-8");
			String updateResult = sendRequest(removeShareAddress, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(updateResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return responseObject.isSuccess();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	public Boolean addShareToCommunity(String shareId, String comment, String communityId, ResponseObject responseObject)
	{
		try{
			String addCommunityAddress = apiRoot + "social/share/add/community/" + shareId + "/" + URLEncoder.encode(comment,"UTF-8") + "/" + URLEncoder.encode(communityId,"UTF-8");
			String updateResult = sendRequest(addCommunityAddress, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(updateResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);

			return responseObject.isSuccess();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	

	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// SOCIAL - PEOPLE
	
	public String deletePerson(String personId, ResponseObject responseObject)
	{
		try{
			String deletePersonAddress = apiRoot + "social/person/delete/" + URLEncoder.encode(personId,"UTF-8");
			String deleteResult = sendRequest(deletePersonAddress, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(deleteResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			
			return responseObject.getMessage();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * getPerson
	 * @param personId  Can be the person id, email address, or Null. If Null, it will return information about current user.
	 * @param responsePojo
	 * @return PersonPojo
	 */
	public PersonPojo getPerson(String personId, ResponseObject responseObject)
	{
		try{
			String getPersonAddress = apiRoot + "social/person/get";

			if (personId != null)
				getPersonAddress +=  "/" + URLEncoder.encode(personId,"UTF-8");

			String getResult = sendRequest(getPersonAddress, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(getResult, ResponsePojo.class, PersonPojo.class, new PersonPojoApiMap());  
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			return (PersonPojo)internal_responsePojo.getData();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}



	public String registerPerson(String first_name, String last_name, String phone, String email, String password, String accountType, ResponseObject responseObject) 
	{
		try {
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy kk:mm:ss aa");
			String today = formatter.format(date);
			String encrypted_password;

			encrypted_password = encryptWithoutEncode(password);

			WordPressUserPojo wpuser = new WordPressUserPojo();
			WordPressAuthPojo wpauth = new WordPressAuthPojo();

			wpuser.setCreated(today);
			wpuser.setModified(today);
			wpuser.setFirstname(first_name);
			wpuser.setLastname(last_name);
			wpuser.setPhone(phone);

			ArrayList<String> emailArray = new ArrayList<String>();
			emailArray.add(email);
			wpuser.setEmail(emailArray);

			//wpauth.setWPUserID(email);
			wpauth.setPassword(encrypted_password);
			wpauth.setAccountType(accountType);
			wpauth.setCreated(today);
			wpauth.setModified(today);
			
			WordPressSetupPojo wpSetup = new WordPressSetupPojo();
			wpSetup.setAuth(wpauth);
			wpSetup.setUser(wpuser);

			return registerPerson(wpSetup, responseObject);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null; 
	}
	
	public String registerPerson(WordPressSetupPojo wpSetup, ResponseObject responseObject) 
	{

		String theUrl = apiRoot + "social/person/register";
		String data =  new Gson().toJson(wpSetup);
		String registerResult = sendRequest(theUrl, data);
		ResponsePojo internal_responsePojo = ResponsePojo.fromApi(registerResult, ResponsePojo.class); 
		ResponseObject internal_ro = internal_responsePojo.getResponse();
		responseObject = shallowCopy(responseObject, internal_ro);
		
		return responseObject.getMessage();
	}
	
	public String updatePerson(String first_name, String last_name, String phone, String email, String password, String accountType, ResponseObject responseObject) 
	{
		try {
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy kk:mm:ss aa");
			String today = formatter.format(date);
			String encrypted_password;

			encrypted_password = encryptWithoutEncode(password);


			WordPressUserPojo wpuser = new WordPressUserPojo();
			WordPressAuthPojo wpauth = new WordPressAuthPojo();

			wpuser.setCreated(today);
			wpuser.setModified(today);
			wpuser.setFirstname(first_name);
			wpuser.setLastname(last_name);
			wpuser.setPhone(phone);
			ArrayList<String> emailArray = new ArrayList<String>();
			emailArray.add(email);
			wpuser.setEmail(emailArray);
			wpauth.setWPUserID(email);
			wpauth.setPassword(encrypted_password);
			wpauth.setAccountType(accountType);
			wpauth.setCreated(today);
			wpauth.setModified(today);

			return updatePerson(wpuser, wpauth, responseObject);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null; 
	}
	
	public String updatePerson(WordPressUserPojo wpuser, WordPressAuthPojo wpauth, ResponseObject responseObject) 
	{

		String theUrl = apiRoot + "social/person/update";
		String data = "{ \"user\":" + new Gson().toJson(wpuser) + ", \"auth\":" + new Gson().toJson(wpauth) + "}";
		String updateResult = sendRequest(theUrl, data);
		ResponsePojo internal_responsePojo = ResponsePojo.fromApi(updateResult, ResponsePojo.class); 
		ResponseObject internal_ro = internal_responsePojo.getResponse();
		responseObject = shallowCopy(responseObject, internal_ro);
		
		return responseObject.getMessage();
	}
	
	public String updatePersonEmail(String id, String newEmail, ResponseObject responseObject) 
	{
		try{
			String updateEmailAddress = apiRoot + "social/person/update/password/" + URLEncoder.encode(id,"UTF-8") + "/" + URLEncoder.encode(newEmail,"UTF-8");
			String updateResult = sendRequest(updateEmailAddress, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(updateResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
		
			return responseObject.getMessage();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// CONFIG - SOURCE
	
	public SourcePojo getSource(String sourceId, ResponseObject responseObject)
	{
		try{
			String getSourceAddress = apiRoot + "config/source/get/" + URLEncoder.encode(sourceId,"UTF-8");

			String getResult = sendRequest(getSourceAddress, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(getResult, ResponsePojo.class, SourcePojo.class, new SourcePojoApiMap(null));  
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			return (SourcePojo)internal_responsePojo.getData();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public SourcePojo saveSource(SourcePojo source, String communityId, ResponseObject responseObject)
	{
		try {
			String address = apiRoot + "config/source/save/" + communityId + "/";
			String saveResult;
			saveResult = sendRequest(address, new Gson().toJson(source));
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(saveResult, ResponsePojo.class, SourcePojo.class, new SourcePojoApiMap(null));
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			return (SourcePojo)internal_responsePojo.getData();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public String deleteSource(String sourceId, String communityId, ResponseObject responseObject)
	{
		try {
			String address = apiRoot + "config/source/delete/" + sourceId + "/" + communityId;
			String deleteResult = sendRequest(address, null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(deleteResult, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			responseObject = shallowCopy(responseObject, internal_ro);
			
			return responseObject.getMessage();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return null;
	}

	public List<DocumentPojo> testSource(SourcePojo testSrc, ResponseObject response, int nToReturn, boolean bReturnFullText) {
		
		StringBuffer theUrl = new StringBuffer(apiRoot).append("config/source/test");
		if (nToReturn > 0) {
			theUrl.append("?numReturn=").append(nToReturn);
		}
		if (bReturnFullText) {
			if (nToReturn > 0) {
				theUrl.append('&');
			}
			else {
				theUrl.append('?');
			}
			theUrl.append("returnFullText=true");
		}
		String map = ApiManager.mapToApi(testSrc, null);		
		String testResult = sendRequest(theUrl.toString(), map);
		
		ResponsePojo internal_response = ResponsePojo.fromApi(testResult, ResponsePojo.class);
		shallowCopy(response, internal_response.getResponse());
		List<DocumentPojo> docs = null;
		if (response.isSuccess()) {
			docs = ApiManager.mapListFromApi((JsonElement)internal_response.getData(), 
																	DocumentPojo.listType(), new DocumentPojoApiMap());
		}
		
		return docs;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// KNOWLEDGE - QUERY
	
	// Queries are slightly different - the entire ResponsePojo is used.
	
	public ResponsePojo sendQuery(AdvancedQueryPojo query, ObjectId communityId, ResponseObject response) {
		ArrayList<ObjectId> community = new ArrayList<ObjectId>(1);
		community.add(communityId);
		return sendQuery(query, community, response);
	}
	public ResponsePojo sendQuery(AdvancedQueryPojo query, Collection<ObjectId> communities, ResponseObject response) {
		
		StringBuffer theUrl = new StringBuffer(apiRoot).append("knowledge/document/query/");
		boolean bFirstComm = true;
		for (ObjectId commId: communities) {
			if (!bFirstComm) {
				bFirstComm = false;
				theUrl.append(',');
			}
			theUrl.append(commId.toString());
		}
		String testResult = sendRequest(theUrl.toString(), query.toApi());
		
		ResponsePojo internal_response = ResponsePojo.fromApi(testResult, ResponsePojo.class);
		shallowCopy(response, internal_response.getResponse());
		
		return internal_response;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// CUSTOM - CREATE PLUGIN TASK
	
	// The following fields of taskConfig are filled in (string unless otherwise specified):
	// - jobtitle, jobdesc
	// - jarURL: the name of the JAR in the share
	// - inputCollection
	// - firstSchedule (Date) ... when it will be run
	// - scheduleFreq (SCHEDULE_FREQUENCY)
	// - mapper, combiner, reducer
	// - outputKey, outputValue
	// - appendResult, ageOutInDays
	// - jobDependencies ... though using the input param that allows them to be specified as jobtitles is recommended
	
	public ObjectId createCustomPluginTask(CustomMapReduceJobPojo taskConfig, Collection<String> dependencies, ObjectId communityId, ResponseObject response) {
		ArrayList<ObjectId> community = new ArrayList<ObjectId>(1);
		community.add(communityId);
		return createCustomPluginTask(taskConfig, dependencies, community, response);
	}
	public ObjectId createCustomPluginTask(CustomMapReduceJobPojo taskConfig, Collection<String> dependencies, Collection<ObjectId> communities, ResponseObject response) {
		return createCustomPluginTask(taskConfig, dependencies, communities, response, false);
	}
	
	private ObjectId createCustomPluginTask(CustomMapReduceJobPojo taskConfig, Collection<String> dependencies, Collection<ObjectId> communities, ResponseObject response, boolean bUpdate) {
		ObjectId retVal = null;
		try {
			StringBuffer url = new StringBuffer(apiRoot).append("custom/mapreduce/");
			if (bUpdate) {
				if (taskConfig._id != null) {
					url.append("updatejob/").append(taskConfig._id.toString()).append("/");				
				}
				else {
					url.append("updatejob/").append(taskConfig.jobtitle).append("/");
					taskConfig.jobtitle = null;
				}//TESTED
			}
			else {
				url.append("schedulejob/");				
			}			
			if (null != communities) {
				for (ObjectId communityId: communities) {
					url.append(communityId.toString()).append(',');
				}
				url.setLength(url.length() - 1);
			}
			else {
				url.append("null");
			}
			url.append('/');
			
			if ((null != taskConfig.jobDependencies) && !taskConfig.jobDependencies.isEmpty()) {
				for (ObjectId jobId: taskConfig.jobDependencies) {
					url.append(jobId.toString()).append(',');
				}
				url.setLength(url.length() - 1);				
			}
			else if ((null != dependencies) && !dependencies.isEmpty()) {
				for (String jobTitle: dependencies) {
					url.append(jobTitle).append(',');
				}
				url.setLength(url.length() - 1);								
			}
			else {
				url.append("null");				
			}
			url.append("/");

			// "nextRunTime"==first Schedule (date)
			if (null != taskConfig.firstSchedule) {
				taskConfig.nextRunTime = taskConfig.firstSchedule.getTime();
			}
			
			String json = sendRequest(url.toString(), ApiManager.mapToApi(taskConfig, null));
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(json, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			response = shallowCopy(response, internal_ro);
			
			if (response.isSuccess()) {
				JsonPrimitive retValObj = (JsonPrimitive)internal_responsePojo.getData(); 
				retVal = new ObjectId(retValObj.getAsString());
			}			
		} 
		catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		return retVal;
	}
	//TESTED (one minor clause to test, will leave for now)
	
	///////////////////////////////////////////////////////////////////
	
	// Updates ... as above but jobtitle or _id must be specified
	
	public ObjectId updateCustomPluginTask(CustomMapReduceJobPojo taskConfig, Collection<String> dependencies, ObjectId communityId, ResponseObject response) {
		ArrayList<ObjectId> community = new ArrayList<ObjectId>(1);
		community.add(communityId);
		return updateCustomPluginTask(taskConfig, dependencies, community, response);
	}
	
	public ObjectId updateCustomPluginTask(CustomMapReduceJobPojo taskConfig, Collection<String> dependencies, Collection<ObjectId> communities, ResponseObject response) {
		return createCustomPluginTask(taskConfig, dependencies, communities, response, true);
	}
	//TESTED
		
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// CUSTOM - CREATE PLUGIN TASK
	
	// The following fields of taskConfig are filled in (string unless otherwise specified):
	// - jobtitle, jobdesc
	// - firstSchedule (Date) ... when it will be run
	// - scheduleFreq (SCHEDULE_FREQUENCY)
	// - appendResult, ageOutInDays
	
	public ObjectId createCustomSavedQueryTask(CustomMapReduceJobPojo taskConfig, AdvancedQueryPojo query, ObjectId communityId, ResponseObject response) {
		ArrayList<ObjectId> community = new ArrayList<ObjectId>(1);
		community.add(communityId);
		return createCustomSavedQueryTask(taskConfig, query, community, response);
	}
	public ObjectId createCustomSavedQueryTask(CustomMapReduceJobPojo taskConfig, ObjectId communityId, ResponseObject response) {
		ArrayList<ObjectId> community = new ArrayList<ObjectId>(1);
		community.add(communityId);
		return createCustomSavedQueryTask(taskConfig, null, community, response);
	}
	public ObjectId createCustomSavedQueryTask(CustomMapReduceJobPojo taskConfig, AdvancedQueryPojo query, Collection<ObjectId> communities, ResponseObject response) {
		if (null != query) {
			taskConfig.query = query.toApi();
		}
		return createCustomPluginTask(taskConfig, null, communities, response);
	}
	//TESTED
	
	///////////////////////////////////////////////////////////////////
	
	// Updates ... as above but jobtitle or _id must be specified
	
	public ObjectId updateCustomSavedQueryTask(CustomMapReduceJobPojo taskConfig, AdvancedQueryPojo query, ObjectId communityId, ResponseObject response) {
		ArrayList<ObjectId> community = new ArrayList<ObjectId>(1);
		community.add(communityId);
		return updateCustomSavedQueryTask(taskConfig, query, community, response);
	}
	public ObjectId updateCustomSavedQueryTask(CustomMapReduceJobPojo taskConfig, ObjectId communityId, ResponseObject response) {
		ArrayList<ObjectId> community = new ArrayList<ObjectId>(1);
		community.add(communityId);
		return updateCustomPluginTask(taskConfig, null, community, response);
	}
	public ObjectId updateCustomSavedQueryTask(CustomMapReduceJobPojo taskConfig, AdvancedQueryPojo query, Collection<ObjectId> communities, ResponseObject response) {
		if (null != query) {
			taskConfig.query = query.toApi();
		}
		return updateCustomPluginTask(taskConfig, null, communities, response);		
	}
	//TESTED
	
	public CustomMapReduceResultPojo getCustomTaskOrQueryResults(String jobtitle, ResponseObject response)
	{
		CustomMapReduceResultPojo retVal = null;
		try
		{
			StringBuilder url = new StringBuilder(apiRoot).append("custom/mapreduce/getresults/");
			if ( null != jobtitle)
			{
				url.append(jobtitle);
			}
				
			String json = sendRequest(url.toString(), null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(json, ResponsePojo.class, CustomMapReduceResultPojo.class, new CustomMapReduceResultPojoApiMap()); 
			
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			response = shallowCopy(response, internal_ro);
			
			if (response.isSuccess()) 
			{
				//JsonElement js = (JsonElement)internal_responsePojo.getData();
				//retVal = ApiManager.mapFromApi(js, CustomMapReduceResultPojo.class, null);
				retVal = (CustomMapReduceResultPojo)internal_responsePojo.getData();
				
			}
		}
		catch (Exception e)
		{
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		return retVal;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////

	public List<CustomMapReduceJobPojo> getCustomTaskOrQuery(String jobtitle, ResponseObject response) {
		List<CustomMapReduceJobPojo> retVal = null;
		try {
			StringBuilder url = new StringBuilder(apiRoot).append("custom/mapreduce/getjobs/");
			if (null != jobtitle) {
				url.append(jobtitle);
			}
			// (else get all)
			
			String json = sendRequest(url.toString(), null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(json, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			response = shallowCopy(response, internal_ro);
			
			if (response.isSuccess()) {
				retVal = ApiManager.mapListFromApi((JsonElement)internal_responsePojo.getData(), 
													CustomMapReduceJobPojo.listType(), null);
			}
		}
		catch (Exception e) { 
			response.setSuccess(false);
			response.setMessage(e.getMessage());			
		}
		return retVal;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// And deletes, apply to either:	
	
	public boolean deleteCustomTaskOrQuery(String jobtitle, ResponseObject response) {
		try {
			StringBuilder url = new StringBuilder(apiRoot).append("custom/mapreduce/removejob/");
			url.append(jobtitle);
			
			String json = sendRequest(url.toString(), null);
			ResponsePojo internal_responsePojo = ResponsePojo.fromApi(json, ResponsePojo.class); 
			ResponseObject internal_ro = internal_responsePojo.getResponse();
			response = shallowCopy(response, internal_ro);
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		return response.isSuccess();
	}
	public boolean deleteCustomTaskOrQuery(ObjectId taskId, ResponseObject response) {
		return deleteCustomTaskOrQuery(taskId.toString(), response);
	}	
	//TESTED
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// UTILITY	
	
	///////// Request Calls	
	
	public static String sendRequest(String urlAddress, String postData)
	{
		try {
			if (postData == null)
				return sendGetRequest(urlAddress);
			else
				return sendPostRequest(urlAddress, postData);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


	private static String sendPostRequest(String urlAddress, String data) throws MalformedURLException, IOException
	{	
		String result = "";

		URLConnection urlConnection = new URL(urlAddress).openConnection();

		if ( cookie != null )
			urlConnection.setRequestProperty("Cookie", cookie);

		urlConnection.setDoOutput(true);
		urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
		((HttpURLConnection)urlConnection).setRequestMethod("POST");

		// Post JSON string to URL

		OutputStream os = urlConnection.getOutputStream();

		byte[] b = data.getBytes("UTF-8");

		os.write(b);

		// Receive results back from API

		InputStream inStream = urlConnection.getInputStream();

		result = IOUtils.toString(inStream, "UTF-8");

		inStream.close();

		//save cookie if cookie is null
		if ( cookie == null )
		{
			String headername;
			for ( int i = 1; (headername = urlConnection.getHeaderFieldKey(i)) != null; i++ )
			{
				if ( headername.equals("Set-Cookie") )
				{
					cookie = urlConnection.getHeaderField(i);
					break;
				}
			}
		}     


		return result;
	}
	
	

	public static String sendGetRequest(String urlAddress) throws Exception
	{
		URL url = new URL(urlAddress);
		URLConnection urlConnection = url.openConnection();
		if ( cookie != null )
			urlConnection.setRequestProperty("Cookie", cookie);
		((HttpURLConnection)urlConnection).setRequestMethod("GET");

		//read back result
		BufferedReader inStream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		StringBuilder strBuilder = new StringBuilder();
		String buffer;           
		while ( (buffer = inStream.readLine()) != null )
		{
			strBuilder.append(buffer);
		}       
		inStream.close();

		//save cookie if cookie is null
		if ( cookie == null )
		{
			String headername;
			for ( int i = 1; (headername = urlConnection.getHeaderFieldKey(i)) != null; i++ )
			{
				if ( headername.equals("Set-Cookie") )
				{
					cookie = urlConnection.getHeaderField(i);
					break;
				}
			}
		}       
		return strBuilder.toString();
	}  
	
	
	///////////Password Encryption

	public String encryptEncodePassword(String pword) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		return URLEncoder.encode(encryptWithoutEncode(pword), "UTF-8");
	}

	public static String encryptWithoutEncode(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException 
	{	
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(password.getBytes("UTF-8"));

		return new String(Base64.encodeBase64(md.digest()));
	}
	
	
	
	//////////Shallow Copy Response Object
	
	private ResponseObject shallowCopy(ResponseObject toCopy, ResponseObject fromCopy)
	{
		toCopy.setAction(fromCopy.getAction());
		toCopy.setMessage(fromCopy.getMessage());
		toCopy.setSuccess(fromCopy.isSuccess());
		toCopy.setTime(fromCopy.getTime());
		return fromCopy;
	}
}
