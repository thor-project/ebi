/**
 * 
 */
var thorApplicationNamespace = {};

$(document).ready(function() {
	thorApplicationNamespace.defVariables();
	thorApplicationNamespace.setBasicSearchClick();
});

/**
 * Declare the variables used by THOR ORCID search
 */
thorApplicationNamespace.defVariables = function defVariables() {
	//EBI-THOR Project WebService Endpoint
	thorApplicationNamespace.thorUrl = 			"http://ves-ebi-36.ebi.ac.uk:8080/ThorWeb/api/orcid/find/";
	//Reference to HTML objects related to Thor Basic Integration
	thorApplicationNamespace.thorTxtSearch =		$('.thorKeyword');
	thorApplicationNamespace.thorthorDivResult =$('.thorResult');
	//JSON whole response
	thorApplicationNamespace.thorJSONResult = 	'';
	//JSON arrays of orcid-search-result
	thorApplicationNamespace.thorProfilesArray = [];
	//OnClick Function for the Thor Basic Integration Search Button
	/**
	 * Thor Basic Integration Search Button onClick
	 * Passes the keyword and callback function to display the results
	 */
	thorApplicationNamespace.fnBasicSearchClick = function(event){
		thorApplicationNamespace.findAllRecordsOrcIds(thorApplicationNamespace.thorTxtSearch.val(), thorApplicationNamespace.displayBasicResults);
	}
}

/**
 * Defines the onClick event to call Thor ORCID search 
 * for objects marked as class 'thorSearchButton'
 */
thorApplicationNamespace.setBasicSearchClick = function setBasicSearchClick() {
	$('.thorSearchButton').on('click', thorApplicationNamespace.fnBasicSearchClick);	
}

thorApplicationNamespace.displayBasicResults = function displayBasicResults(event) {
	var tmpOutput;
	thorApplicationNamespace.thorthorDivResult.text("");
	$.each(thorApplicationNamespace.thorProfilesArray, function( index, value ) {
		var tmpOutput =  thorApplicationNamespace.createBasicResult(thorApplicationNamespace.thorProfilesArray[index]['orcid-profile']);
		thorApplicationNamespace.addToDropDown(tmpOutput[0], tmpOutput[1]);
		});
}

thorApplicationNamespace.addToDropDown = function addToDropDown(optValue, optText) {
	thorApplicationNamespace.thorthorDivResult.append($("<option />").val(optValue).text(optText));
}

thorApplicationNamespace.createBasicResult = function createBasicResult(profile) {
	var orcId = profile['orcid-identifier'].path
	var givenName = thorApplicationNamespace.retrieveValue(profile['orcid-bio']['personal-details']['given-names']);
	var familyName = thorApplicationNamespace.retrieveValue(profile['orcid-bio']['personal-details']['family-name']);
	var outLabel = orcId + " (" + givenName + " " + familyName + ")";
	return [orcId, outLabel];
}

thorApplicationNamespace.retrieveValue = function retrieveValue(element) {
	if (element != null) {
	    var str = element.value;
	}
	else {
	    var str = "";
	}
	return str;
}

thorApplicationNamespace.findAllRecordsOrcIds = function findAllRecordsOrcIds(keyword, callback) {
	var searchUrl =  thorApplicationNamespace.thorUrl + keyword;
	$.ajax({
	        url: searchUrl,
	        success: function(data) {
		    	var thorJSONResult = jQuery.parseJSON( data );
		    	thorApplicationNamespace.thorProfilesArray = thorJSONResult['orcid-search-results']['orcid-search-result'];
		    	callback();
		    }
	    });
}

thorApplicationNamespace.getArrayOrcIds = function getArrayOrcIds(keyword) {
	/*
		var profile = resultArray[0]['orcid-profile'];
	    	var orcId = profile['orcid-identifier'].path
	    	var givenName = profile['orcid-bio']['personal-details']['given-names'].value;
	    	var familyName = profile['orcid-bio']['personal-details']['family-name'].value;
	    	
	       $('.greeting-content').append(orcId + " (" + givenName + " " + familyName + ")");
	*/
	
}