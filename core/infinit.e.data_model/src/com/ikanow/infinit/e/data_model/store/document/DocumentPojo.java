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
/**
 * 
 */
package com.ikanow.infinit.e.data_model.store.document;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.types.ObjectId;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import com.ikanow.infinit.e.data_model.store.BaseDbPojo;
import com.ikanow.infinit.e.data_model.store.config.source.SourcePojo;
import com.mongodb.BasicDBList;

/**
 * @author apiggott
 * The generic document data model
 */
public class DocumentPojo extends BaseDbPojo {
	// Standard static function for readability
	@SuppressWarnings("unchecked")
	static public TypeToken<List<DocumentPojo>> listType() { return new TypeToken<List<DocumentPojo>>(){}; }

	//*** IMPORTANT: don't add to this list without considering the ES mapping in DocumentIndexPojoMap

	// Storage (Mongo) data model
	// API data model is the same except where otherwise specified (DocumentApiPojoMap converts)
	// For index data model see DocumentIndexPojoMap

	////////////////////////////////////////////////////////////////////////////////	

	// Stored Fields:

	// Basic metadata
	private ObjectId _id = null;
	final public static String _id_ = "_id";
	private String title = null;
	final public static String title_ = "title";
	private String url = null;
	final public static String url_ = "url";
	private Date created = null;
	final public static String created_ = "created";
	private Date modified = null;
	final public static String modified_ = "modified";
	private Date publishedDate = null;
	final public static String publishedDate_ = "publishedDate";

	// Data source
	private String source = null; // (API side is Set<String>)
	final public static String source_ = "source";
	private String sourceKey = null; // (API side is Set<String>)
	final public static String sourceKey_ = "sourceKey";
	private String mediaType = null; // (API side is Set<String>)
	final public static String mediaType_ = "mediaType";
	transient String sourceType = null; //feed, db, or filesys	
	final public static String sourceType_ = "sourceType";

	// Content
	private String description = null;
	final public static String description_ = "description";
	// Enriched content
	private List<EntityPojo> entities = null;
	final public static String entities_ = "entities";
	// (moved metadata to beta because of wholesale changes)

	// Data source
	private Set<String> tags = null;
	final public static String tags_ = "tags";
	private ObjectId communityId = null; 
	final public static String communityId_ = "communityId";
		// (note as far as the API is concerned this a Set<String>)

	//currently only used for xml files
	private String sourceUrl = null;
	final public static String sourceUrl_ = "sourceUrl";

	// Enriched content
	private List<AssociationPojo> associations = null; 
	final public static String associations_ = "associations";
	private LinkedHashMap<String, Object[]> metadata = null; // has to be [] to allow for 1+  
	final public static String metadata_ = "metadata";
	private GeoPojo docGeo = null; // holds the location of the document, if it has one separate to its entities and events
	final public static String docGeo_ = "docGeo";

	// Mongo/Elasticsearch-specific field
	private String index = null; // The name of the index to which the feed's been added
	final public static String index_ = "index";

/////////////////////////////////////////////////////////////////////////////////////////////////	
	
// The following won't be stored in the DB (either created by index map or transient)
	
	// Alpha unstored (eg index or API fields)
	
	// Content
	private String fullText = null;	
	final public static String fullText_ = "fullText";

	// Per query (transient, created on the way to the API for query, not currently stored anywhere)
	
	private Double aggregateSignif; // The document significance normalized against Lucene relevance 
	final public static String aggregateSignif_ = "aggregateSignif";
	private Double queryRelevance; // The Lucene relevance normalized against Infinit.e significance
	final public static String queryRelevance_ = "queryRelevance";
	private Double score; // The combined scores (vs the query weighting)	
	final public static String score_ = "score";
	
	// Alpha transient:
	
	transient String tmpFullText = null; // (temporary storage until obj written to MongoDB)

	// Beta unstored (eg index or API fields)
	
	// Index-specific fields (ElasticSearch):
	private Set<String> locs = null;
	final public static String locs_ = "locs";

	@SuppressWarnings("unused")
	private List<GeoPojo> timeRanges = null; // (won't be used for beta - allow encapsulation of time ranges as 2d points)
	final public static String timeRanges_ = "timeRanges";
	private Set<Integer> months = null; // (dates represented as YYYYMM - used to generate histograms, nothing else)
	final public static String months_ = "months";

	// Beta transient:
	
	private transient SourcePojo _source = null; // (handy accessor for the "parent" source info)

	//header & Footer Data (doesn't persist in the DB - used for extraction and enrichment)
	private transient int headerEndIndex = 0; // (obv starts at 0)
	private transient int footerStartIndex = Integer.MAX_VALUE; // (obv ends at the end of the document)
	private transient Set<String> headerFields = null;
	private transient Set<String> footerFields = null;
	private transient String headerText = null; // (\n-separated list of headerFields)
	private transient String footerText = null; // (\n-separated list of headerFields)

	// V0 transient
	
	// multi-community/source handling
	private transient String duplicateFrom = null; // Indicates this document should be cloned from the DB entry with matching URL, "duplicateFrom" source
	private transient DocumentPojo cloneFrom = null; // Indicate this document should be cloned from the "cloneFrom" in memory copy after enrichment 
	
	////////////////////////////////////////////////////////////////////////////////

	// Alpha gets and sets	

	public DocumentPojo()
	{
	}

	public ObjectId getId() {
		return _id;
	}
	public void setId(ObjectId _id) {
		this._id = _id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @param created the created to set
	 */
	public Date getCreated() {
		return this.created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getModified() {
		return this.modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public Date getPublishedDate() {
		return this.publishedDate;
	}
	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSourceKey() {
		return sourceKey;
	}
	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public void setEntities(List<EntityPojo> entities) {
		this.entities = entities;
	}
	public List<EntityPojo> getEntities() {
		return entities;
	}

	public String getMediaType() {
		return mediaType;
	}
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	public String getFullText() {
		return (null == fullText)?tmpFullText:fullText;
	}
	public void setFullText(String fullText) {
		this.tmpFullText = fullText;
	}
	public void makeFullTextNonTransient() {
		this.fullText = this.tmpFullText;
	}

	public void setSourceType(String sourceType)
	{
		this.sourceType = sourceType;
	}

	public String getSourceType()
	{
		return this.sourceType;
	}

	public SourcePojo getTempSource() { return _source; }
	public void setTempSource(SourcePojo tempSource) { _source = tempSource; }

	////////////////////////////////////////////////////////////////////////////////

	// Alpha utility	

	////////////////////////////////////////////////////////////////////////////////

	// Beta gets and sets

	public void setAssociations(List<AssociationPojo> events)
	{
		this.associations = events;
	}

	public List<AssociationPojo> getAssociations()
	{
		return this.associations;
	}
	public void addToMetadata(String fieldName, Object fieldVal) {
		if (null == metadata) {
			metadata = new LinkedHashMap<String, Object[]>();
		}
		Object obj[] = new Object[1]; obj[0] = fieldVal;
		metadata.put(fieldName, obj);
	}
	public void addToMetadata(String fieldName, Object[] fieldVals) {
		if (null == metadata) {
			metadata = new LinkedHashMap<String, Object[]>();
		}
		metadata.put(fieldName, fieldVals);
	}

	public void setMetadata(LinkedHashMap<String, Object[]> metadata)
	{
		this.metadata = metadata;
	}

	public LinkedHashMap<String, Object[]> getMetadata()
	{
		return this.metadata;
	}

	public LinkedHashMap<String, Object[]> getMetaData() {
		return metadata;
	}

	public Set<String> getTags() { 
		return tags; 
	}
	public void setTags(Set<String> tags_) { 
		tags = tags_;
	}
	public void addTags(Set<String> tags_) { 
		tags.addAll(tags_);
	}
	public void setCommunityId(ObjectId communityId) {
		this.communityId = communityId;
	}
	public ObjectId getCommunityId() {
		return this.communityId;
	}

	public GeoPojo getDocGeo() {
		return docGeo;
	}

	public void setDocGeo(GeoPojo docGeo) {
		this.docGeo = GeoPojo.cleanseBadGeotag(docGeo);
	}

	/**
	 * @param locs the locs to set
	 */
	public void setLocs(Set<String> locs) {
		this.locs = locs;
	}

	/**
	 * @return the locs
	 */
	public Set<String> getLocs() {
		return locs;
	}

	/**
	 * @param months the months to set
	 */
	public void setMonths(Set<Integer> months) {
		this.months = months;
	}

	/**
	 * @return the months
	 */
	public Set<Integer> getMonths() {
		return months;
	}

	/**
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	/**
	 * @return the sourceUrl
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}

	/**
	 * @return the index
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(String index) {
		this.index = index;
	}

	////////////////////////////////////////////////////////////////////////////////

	// Beta utility

	// Add the metadata as separate lines to perform extraction on them

	public String metaDataToText() {
		StringBuffer sb = new StringBuffer();
		for ( Object md : metadata.values())
		{
			sb.append(md).append('\n');
		}
		return sb.toString();
	}//TOTEST - to be done during DB integration

	////////////////////////////////////////////////////////////////////////////////

	//(Still beta) Header Footer Stuff ... can be used by entity extractors

	/**
	 * @return the headerStartIndex
	 */
	public int getHeaderEndIndex() {
		return headerEndIndex;
	}

	/**
	 * @param headerStartIndex the headerStartIndex to set
	 */
	public void setHeaderEndIndex(int headerEndIndex) {
		this.headerEndIndex = headerEndIndex;
	}

	/**
	 * @return the footerStartIndex
	 */
	public int getFooterStartIndex() {
		return footerStartIndex;
	}

	/**
	 * @param footerEndIndex the footerEndIndex to set
	 */
	public void setFooterStartIndex(int footerStartIndex) {
		this.footerStartIndex = footerStartIndex;
	}

	public void addToHeader(String sHeaderField) {
		if (headerFields == null)
			headerFields = new HashSet<String>();
		headerFields.add(sHeaderField.toLowerCase());
	}
	public void addToFooter(String sFooterField) {
		if (footerFields == null)
			footerFields = new HashSet<String>();
		footerFields.add(sFooterField.toLowerCase());
	}
	public Set<String> getHeaderFields() {
		return headerFields;
	}
	public Set<String> getFooterFields() {
		return footerFields;
	}
	public String getHeader() {
		if (null == headerFields) {
			return "";
		}
		return headerText;
	}
	public String getFooter() {
		if (null == footerFields) {
			return "";
		}
		return footerText;
	}
	public String getBody() {
		if (null == getFullText())
		{
			return null;
		}
		else{
			if (footerStartIndex == Integer.MAX_VALUE && headerEndIndex == 0 )
			{
				return getFullText();
			}
			else if (footerStartIndex > getFullText().length()) {
				return getFullText().substring(headerEndIndex);
			}
			else {
				return getFullText().substring(headerEndIndex, footerStartIndex);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	// V0 gets and sets
	
	public void setDuplicateFrom(String sourceKey) {
		duplicateFrom = sourceKey;
	}
	public String getDuplicateFrom() {
		return duplicateFrom;
	}
	public void setCloneFrom(DocumentPojo masterClone) {
		cloneFrom = masterClone;
	}
	public DocumentPojo getCloneFrom() {
		return cloneFrom;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	// Base overrides:

	public GsonBuilder extendBuilder(GsonBuilder gp) {
		return gp.registerTypeAdapter(DocumentPojo.class, new DocumentPojoDeserializer());
	}
	protected static class DocumentPojoDeserializer implements JsonDeserializer<DocumentPojo> 
	{
		@Override
		public DocumentPojo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject metadata = json.getAsJsonObject().getAsJsonObject("metadata");
			if (null != metadata) {
				json.getAsJsonObject().remove("metadata");
			}
			DocumentPojo doc = BaseDbPojo.getDefaultBuilder().create().fromJson(json, DocumentPojo.class);  
			if (null != metadata) {				
				for (Entry<String, JsonElement> entry: metadata.entrySet()) {
					// Very slow, better to do something like "http://stackoverflow.com/questions/5699323/using-json-with-mongodb"
					// (or in fact MongoDbUtil)
					// (note the only high performance use of documents with metadata enabled bypasses the data_model, so not a big problem at the moment)
					doc.addToMetadata(entry.getKey(), 
							((BasicDBList)com.mongodb.util.JSON.parse(entry.getValue().toString())).toArray());
				}				
			}
			return doc;
		}
	}
	////////////////////////////////////////////////////////////////////////////////	

	// Per query (transient, created on the way to the API for query, not currently stored anywhere)
	
	public Double getAggregateSignif() {
		return aggregateSignif;
	}

	public void setAggregateSignif(Double aggregateSignif) {
		this.aggregateSignif = aggregateSignif;
	}

	public Double getQueryRelevance() {
		return queryRelevance;
	}

	public void setQueryRelevance(Double queryRelevance) {
		this.queryRelevance = queryRelevance;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

}