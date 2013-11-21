<!--
Copyright 2012 The Infinit.e Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" session="false" %>
<%@ include file="inc/sharedFunctions.jsp" %>

<%!
	// 
	int currentPage = 1;
	int itemsToShowPerPage = 10;
	String action = "";
	String logoutAction = "";
	String listFilter = "";
	HashSet<String> communityMembers = new HashSet<String>();

	//
	String editTableTitle = "Add New Community";
	String communityid = "";
	String visibleCommunityId = "";
	String name = "";
	String description = "";
	String tags = "";
	String isSystemCommunity = "";
	String isPersonalCommunity = "";
	String ownerId = "";
	String communityStatus = "";
	String ownerDisplayName = "";
	String ownerEmail = "";
	String numberOfMembers = "";
	String usersCanCreateSubCommunities = "";
	String usersCanCreateSubCommunitiesChecked = "";
	String registrationRequiresApproval = "";
	String registrationRequiresApprovalChecked = "";
	String isPublic = "";
	String isPublicChecked = "";
	String usersCanSelfRegister = "";
	String usersCanSelfRegisterChecked = "";
	
	String communityAttributesAreReadonly = "disabled=\"disabled\"";
	String userAttributesAreReadonly = "disabled=\"disabled\"";
	
	String editMembersLink = "";

%>

<%
	messageToDisplay = "";
	
	// 
	if (isLoggedIn) 
	{
		communityMembers.clear();
		
		// Capture value in the left handed table filter field
		if (request.getParameter("listFilter") != null) 
		{
			listFilter = request.getParameter("listFilter");
		}
		else if (request.getParameter("listFilterStr") != null) 
		{
			listFilter = request.getParameter("listFilterStr");
		}
		else
		{
			listFilter = "";
		}
		
		// Determine which action to perform on postback/request
		action = "";
		if (request.getParameter("action") != null) action = request.getParameter("action").toLowerCase();
		if (request.getParameter("dispatchAction") != null) action = request.getParameter("dispatchAction").toLowerCase();
		if (request.getParameter("clearForm") != null) action = request.getParameter("clearForm").toLowerCase();
		if (request.getParameter("filterList") != null) action = request.getParameter("filterList").toLowerCase();
		if (request.getParameter("clearFilter") != null) action = request.getParameter("clearFilter").toLowerCase();
		if (request.getParameter("logoutButton") != null) action = request.getParameter("logoutButton").toLowerCase();
		if (request.getParameter("addSelected") != null) action = request.getParameter("addSelected").toLowerCase();
		
		// Capture input for page value if passed to handle the page selected in the left hand list of items
		if (request.getParameter("page") != null) 
		{
			currentPage = Integer.parseInt( request.getParameter("page").toLowerCase() );
		}
		else
		{
			currentPage = 1;
		}
		
		try
		{
			// Always clear the form first so there is no bleed over of values from previous requests
			clearForm();

			// Read in values from the edit form
			communityid = (request.getParameter("communityid") != null) ? request.getParameter("communityid") : "";
			name = (request.getParameter("name") != null) ? request.getParameter("name") : "";
			description = (request.getParameter("description") != null) ? request.getParameter("description") : "";
			tags = (request.getParameter("tags") != null) ? request.getParameter("tags") : "";
			isPublic = (request.getParameter("isPublic") != null) ? request.getParameter("isPublic") : "";
			usersCanCreateSubCommunities = (request.getParameter("usersCanCreateSubCommunities") != null) ? request.getParameter("usersCanCreateSubCommunities") : "";
			registrationRequiresApproval = (request.getParameter("registrationRequiresApproval") != null) ? request.getParameter("registrationRequiresApproval") : "";
			usersCanSelfRegister = (request.getParameter("usersCanSelfRegister") != null) ? request.getParameter("usersCanSelfRegister") : "";

			Boolean redirect = false;
			
			// if redirect == true refresh the page to update the edit form's content to reflect changes
			if (redirect) 
			{
				String urlParams = "";
				if (listFilter.length() > 0) urlParams = "&listFilterStr="+ listFilter;
				if (currentPage > 1) urlParams += "&page=" + currentPage;
				out.println("<meta http-equiv=\"refresh\" content=\"0;url=communities.jsp?action=edit&communityid=" 
					+ communityid + urlParams + "\">");
			}
			
			populateEditForm(communityid, request, response);
			
			if (action.equals("addselected")) 
			{
				String[] ids= request.getParameterValues("peopleToAdd");
				
				int nAdded = 0;
				int nFailed = 0;
				for (String id: ids) {
					
					if (!addPersonToCommunity(id, communityid, request, response)) {
						nFailed++;
					}
					else 
						nAdded++;
				}
				messageToDisplay = "Bulk community join: added " + nAdded + ", failed: " + nFailed; 
				
				out.print("<meta http-equiv=\"refresh\" content=\"0;url=addmembers.jsp?communityid=" + communityid);
				if (currentPage > 1) {
					out.print("&page=" + currentPage);
				}
				if (listFilter.length() > 0) {
					out.print("&listFilterStr=" + listFilter);					
				}
				out.println("\">");
			}
			else if (action.equals("filterlist")) 
			{
				currentPage = 1;
				populateEditForm(communityid, request, response);
			}
			else if (action.equals("clearfilter")) 
			{
				listFilter = "";
				populateEditForm(communityid, request, response);
			}
			else if (action.equals("logout")) 
			{
				logOut(request, response);
				out.println("<meta http-equiv=\"refresh\" content=\"0;url=index.jsp\">");
			}
		}
		catch (Exception e)
		{
			//System.out.println(e.getMessage());
		}
	}
	
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="stylesheet" type="text/css" href="inc/manager.css" />
	<script type="text/javascript" src="inc/utilities.js"></script>
	<link rel="shortcut icon" href="image/favicon.ico" />
	<title>Infinit.e.Manager - Communities</title>
	
</head>
<body>

<%
	// !-- Create JavaScript Popup --
	if (messageToDisplay.length() > 0) { 
%>
	<script language="javascript" type="text/javascript">
		alert("<%=messageToDisplay %>");
	</script>
<% 
	} 
%>

	<form method="post">
	
<%@ include file="inc/header.jsp" %>

<%
	if (!isLoggedIn) 
	{
%>
		<%@ include file="inc/login_form.jsp" %>
<%
	}
	else
	{
%>
	
	<table class="standardTable" cellpadding="5" cellspacing="0" width="100%">
	<tr valign="top">
		<td width="30%" bgcolor="#ffffff">
		
			<table class="standardTable" cellpadding="5" cellspacing="1" width="100%">
			<tr>
				<td class="headerLink">Communities</td>
				<td align="right">
					<input type="text" id="listFilter" 
					onkeydown="if (event.keyCode == 13) { setDipatchAction('filterList'); 
					document.getElementById('filterList').click(); }" 
					name="listFilter" size="20" value="<%=listFilter %>"/><button name="filterList" 
					value="filterList">Filter</button>
					<button name="clearFilter" value="clearFilter">Clear</button>
					</td>
			</tr>
			<tr>
				<td colspan="2" bgcolor="white"><%=listItems(request, response) %></td>
			</tr>
			<tr>
				<td colspan="2" >
				<button name="addSelected" onclick="return confirm('Do you really wish to add the selected people to this community?');" name="addSelected" value="addSelected">Add selected people</button>
				<div style="float: right"><a href='communities.jsp?action=edit&communityid=<%=communityid %>'>Back</a></div>
				</td>
			</tr>
			</table>
		</td>
		
		<td width="70%" bgcolor="#ffffff">
		
			<table class="standardTable" cellpadding="5" cellspacing="1" width="100%">
			<tr>
				<td class="headerLink"><%=editTableTitle %></td>
				<td align="right"><button name="clearForm" value="clearForm">New Community</button></td>
			</tr>
			<tr>
				<td colspan="2" bgcolor="white">
					<table class="standardSubTable" cellpadding="5" cellspacing="1" width="100%">
					<tr>
						<td bgcolor="#ffffff" width="30%">Status:</td>
						<td bgcolor="#ffffff" width="70%"><%=communityStatus %></td>
					</tr>
					<tr>
						<td bgcolor="#ffffff" width="30%">Community Id:</td>
						<td bgcolor="#ffffff" width="70%"><input type="text" readonly id="communityid" name="communityid" value="<%=visibleCommunityId %>" size="50" /></td>
					</tr>
					<tr>
						<td bgcolor="#ffffff" width="30%">Name:</td>
						<td bgcolor="#ffffff" width="70%"><input type="text" readonly id="name" name="name" value="<%=name%>" size="50" /></td>
					</tr>
					<tr valign="top">
						<td bgcolor="#ffffff" width="30%">Description:</td>
						<td bgcolor="#ffffff" width="70%">
							<textarea cols="60" rows="5" id="description" readonly name="description"><%=description %></textarea>
						</td>
					</tr>
					<tr valign="top">
						<td bgcolor="#ffffff" width="30%">Tags:</td>
						<td bgcolor="#ffffff" width="70%">
							<textarea cols="60" rows="3" id="tags" readonly name="tags"><%=tags %></textarea>
						</td>
					</tr>
					<tr>
						<td bgcolor="#ffffff" width="30%">Owner:</td>
						<td bgcolor="#ffffff" width="70%"><%=ownerDisplayName %> - <%=ownerEmail %></td>
					</tr>
					<tr valign="top">
						<td bgcolor="#ffffff" width="30%">Community Attributes:</td>
						<td bgcolor="#ffffff" width="70%">
							<table cellpadding="5" cellspacing="1" width="100%">
								<tr>
									<td><input type="checkbox" name="isPublic" <%=isPublicChecked %> 
										disabled="disabled"  /> Is Public</td>
								</tr>
								<tr>
									<td><input type="checkbox" name="usersCanSelfRegister" <%=usersCanSelfRegisterChecked %>
										disabled="disabled"  /> Users Can Self Register</td>
								</tr>
								<tr>
									<td><input type="checkbox" name="registrationRequiresApproval" <%=registrationRequiresApprovalChecked %>
										disabled="disabled"  /> Registration Requires Approval</td>
								</tr>
								<tr>
									<td><input type="checkbox" name="usersCanCreateSubCommunities" <%=usersCanCreateSubCommunitiesChecked %>
										disabled="disabled" /> Users Can Create Subcommunites</td>
								</tr>
							</table>
						</td>
					</tr>
					<!-- <tr valign="top">
						<td bgcolor="#ffffff" width="30%" class="disabledText">User Attributes:</td>
						<td bgcolor="#ffffff" width="70%">
							<table cellpadding="5" cellspacing="1" width="100%" class="disabledText">
								<tr>
									<td><input type="checkbox" name="publishQueriesToActivityFeed" <%=userAttributesAreReadonly %> /> User Queries are Published to Activity Feed</td>
								</tr>
								<tr>
									<td><input type="checkbox" name="publishLoginToActivityFeed" <%=userAttributesAreReadonly %> /> User Login Published to Activity Feed</td>
								</tr>
								<tr>
									<td><input type="checkbox" name="publishSharingToActivityFeed" <%=userAttributesAreReadonly %> /> User Shares Published to Activity Feed</td>
								</tr>
								<tr>
									<td><input type="checkbox" name="publishCommentsToActivityFeed" <%=userAttributesAreReadonly %> /> User Comments Published to Activity Feed</td>
								</tr>
								<tr>
									<td><input type="checkbox" name="publishCommentsPublicly" <%=userAttributesAreReadonly %> /> User Comments are Public</td>
								</tr>
							</table>
						</td>
					</tr>  -->
					<tr>
						<td bgcolor="#ffffff" width="30%">Number of Members:</td>
						<td bgcolor="#ffffff" width="70%"><%=numberOfMembers %></td>
					</tr>
					<tr>
						<td bgcolor="#ffffff" width="30%">Members:</td>
						<td bgcolor="#ffffff" width="70%"><%=editMembersLink %></td>
					</tr>
					</table>
					
				</td>
			</tr>
			</table>
		
		</td>
		
	<tr>
	</table>
	</form>
<%
	}
%>

<%@ include file="inc/footer.jsp" %>
</body>
</html>




<%!

// validateFormFields
private boolean validateFormFields()
{
	boolean isValid = true;
	ArrayList<String> al = new ArrayList<String>();
	if (name.length() < 1) al.add("Name");
	if (description.length() < 1) al.add("Description");
	if (tags.length() < 1) al.add("Tags");
	if (al.size() > 0)
	{
		isValid = false;
		messageToDisplay = "Error, the following required fields are missing: " + al.toString();
	}
	return isValid;
}  // TESTED

// populateEditForm - 
private void populateEditForm(String id, HttpServletRequest request, HttpServletResponse response) 
{
	clearForm();
	if (id != null && id != "") 
	{
		try 
		{
			editTableTitle = "Add People To Community";
			
			// Get person from API
			JSONObject communityResponse = new JSONObject( getCommunity(id, request, response) );
			JSONObject community = communityResponse.getJSONObject("data");
			String status = community.getString("communityStatus").substring(0,1).toUpperCase() + community.getString("communityStatus").substring(1);
			communityStatus =  status;
			visibleCommunityId = id;
			name = community.getString("name");
			description = community.getString("description");
			numberOfMembers = community.getString("numberOfMembers");
			ownerDisplayName = community.getString("ownerDisplayName");
			
			// Community tags
			if (community.has("tags"))
			{
				String listOfTags = community.getString("tags").replace("[", "");
				listOfTags = listOfTags.replace("]", "");
				listOfTags = listOfTags.replace("\"", "");
				tags = listOfTags;
			}
			
			// Community Attributes Check Boxes
			JSONObject communityAttributes = community.getJSONObject("communityAttributes");
			JSONObject isPublic = communityAttributes.getJSONObject("isPublic");
			isPublicChecked = (isPublic.getString("value").equalsIgnoreCase("true")) ? "checked=\"checked\"" : "";
			JSONObject usersCanCreateSubCommunities = communityAttributes.getJSONObject("usersCanCreateSubCommunities");
			usersCanCreateSubCommunitiesChecked = (usersCanCreateSubCommunities.getString("value").equalsIgnoreCase("true")) ? "checked=\"checked\"" : "";
			JSONObject registrationRequiresApproval = communityAttributes.getJSONObject("registrationRequiresApproval");
			registrationRequiresApprovalChecked = (registrationRequiresApproval.getString("value").equalsIgnoreCase("true")) ? "checked=\"checked\"" : "";
			JSONObject usersCanSelfRegister = communityAttributes.getJSONObject("usersCanSelfRegister");
			usersCanSelfRegisterChecked = (usersCanSelfRegister.getString("value").equalsIgnoreCase("true")) ? "checked=\"checked\"" : "";
			
			// Get an array of members
			JSONArray members = community.getJSONArray("members");
			for (int i = 0; i < members.length(); i++)
			{
				JSONObject member = members.getJSONObject(i);
				communityMembers.add(member.getString("_id"));
				if (member.getString("userType").equalsIgnoreCase("owner"))
				{
					ownerEmail = member.getString("email");
				}
			}

			editMembersLink = "<a href='members.jsp?communityid=" + communityid + "' title='Edit Community Members'>Edit Community Members</a>";
		} 
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}
	}
}  // TESTED




// clearForm
private void clearForm()
{
	editTableTitle = "Add New Community";
	communityStatus = "";
	visibleCommunityId = "";
	name =  "";
	description = "";
	tags = "";
	numberOfMembers = "";
	isPublicChecked = "";
	usersCanCreateSubCommunitiesChecked = "";
	registrationRequiresApprovalChecked = "";
	usersCanSelfRegisterChecked = "";
	ownerDisplayName = "";
	ownerEmail = "";
	editMembersLink = "";
}  // TESTED



//listItems -
private String listItems(HttpServletRequest request, HttpServletResponse response)
{
	StringBuffer people = new StringBuffer();
	Map<String, String> listOfPeople = getListOfAllPeople(request, response, communityMembers);
	
	if (listOfPeople.size() > 0)
	{
		people.append("<table class=\"listTable\" cellpadding=\"3\" cellspacing=\"1\" width=\"100%\" >");

		// Sort the sources alphabetically
		List<String> sortedKeys = new ArrayList<String>(listOfPeople.keySet());
		Collections.sort( sortedKeys, String.CASE_INSENSITIVE_ORDER );
		
		// Filter the list
		List<String> sortedAndFilteredKeys = new ArrayList<String>();
		for (String key : sortedKeys)
		{
			if ( listFilter.length() > 0 )
			{
				if ( key.toLowerCase().contains( listFilter.toLowerCase() ) ) sortedAndFilteredKeys.add( key );
			}
			else
			{
				sortedAndFilteredKeys.add( key );
			}
		}
		
		// If the user has filtered the list down we might need to adjust our page calculations
		// e.g. 20 total items might = 2 pages but filtered down to 5 items there would only be 1
		// Calculate first item to start with with
		// Page = 1, item = 1
		// Page = X, item = ( ( currentPage - 1 ) * itemsToShowPerPage ) + 1;
		int startItem = 1;
		int endItem = startItem + itemsToShowPerPage - 1;
		if (currentPage > 1)
		{
			startItem = ( ( currentPage - 1 ) * itemsToShowPerPage ) + 1;
			endItem = ( startItem + itemsToShowPerPage ) - 1;
		}

		int currentItem = 1;
		for (String key : sortedAndFilteredKeys)
		{
			if (currentItem >= startItem && currentItem <= endItem)
			{
				String name = key;
				String id = listOfPeople.get(key).toString();
				String editLink = "";
				String deleteLink = "";
	
				String listFilterString = "";
				if (listFilter.length() > 0) listFilterString = "&listFilterStr="+ listFilter;
				
				editLink = "<a href=\"people.jsp?action=edit&personid=" + id + "&page=" + currentPage 
						+ listFilterString + "\" title=\"Edit User Account\">" + name + "</a>";
	
				// Create the HTML table row
				people.append("<tr>");
				people.append("<td bgcolor=\"white\" width=\"100%\">" + editLink + "</td>");
				people.append("<td align=\"center\" bgcolor=\"white\"><input type=\"checkbox\" name=\"peopleToAdd\" value=\"" + id + "\"/></td>");
				people.append("</tr>");
			}
			currentItem++;
		}
		
		// Calculate number of pages, current page, page links...
		people.append("<tr><td colspan=\"2\" align=\"center\" class=\"subTableFooter\">");
		// --------------------------------------------------------------------------------
		// Create base URL for each page
		StringBuffer baseUrl = new StringBuffer();
		baseUrl.append("addmembers.jsp?communityid=" + communityid);
		if (listFilter.length() > 0) baseUrl.append('&').append("listFilterStr=").append(listFilter);
		baseUrl.append("&page=");
		people.append( createPageString( sortedAndFilteredKeys.size(), itemsToShowPerPage, currentPage, baseUrl.toString() ));
		people.append("</td></tr>");
		// --------------------------------------------------------------------------------
		people.append("</table>");
	}
	else
	{
		people.append("No user accounts were retrieved");
	}

	return people.toString();
}



//addPersonToCommunity
private boolean addPersonToCommunity(String person, String community, HttpServletRequest request, HttpServletResponse response)
{
	try
	{
		JSONObject updateResponse = new JSONObject ( new JSONObject ( addToCommunity(community, person, request, response) ).getString("response") );
		if (updateResponse.getString("success").equalsIgnoreCase("true"))
		{
			messageToDisplay = "Success: Person added to community."; return true;
		}
		else
		{
			messageToDisplay = "Error: Unable to add person to community."; return false;
		}
	}
	catch (Exception e)
	{
		messageToDisplay = "Error: Unable to add person to community. (" + e.getMessage() + ")"; return false;
	}
}  // TESTED



%>
