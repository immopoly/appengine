
/**
 * functions for experimental login
 */
    	function getTokenFromUrl(){
    		if(typeof document.location.search != "undefined"){
    			token = document.location.search.substr(7);
    			document.forms[2].token.value=token;
			}
		}
    	
    	function passwordmail(){
			username=document.forms[0].username.value;
			email=document.forms[0].email.value;

			alert('try sending passwordmail for '+username+' '+email);
				$.ajax({
                   url: "http://immopoly.appspot.com/user/sendpasswordmail",
                   context: document.body,
                   data:{'username':username,'email':email},
                   dataType:"json",
                   success: function(data){
                     alert('passwordmail response '+JSON.stringify(data));
                   },
                     error: function(){
                     alert('error ');
                   }
                 });		
    	}
    	
    	function login(){
			username=document.forms[1].username.value;
			password=document.forms[1].password.value;
			document.forms[2].token.value=token;
			alert('Hello Immopoly '+username+' '+password);
				$.ajax({
                   url: "http://immopoly.appspot.com/user/login",
                   context: document.body,
                   data:{'username':username,'password':password},
                   dataType:"json",
                   success: function(data){
                     alert('login response '+JSON.stringify(data) );
                     token=data["org.immopoly.common.User"].token;
                     document.forms[2].token.value=token;
                   },
                     error: function(){
                     alert('error ');
                   }
                 });		
    	}
    	
    	function changePassword(){
			username=document.forms[2].username.value;
			password=document.forms[2].password.value;
			token=document.forms[2].token.value;
			email=document.forms[2].email.value;

			alert('Change Password '+username+' '+password);
				$.ajax({
                   url: "http://immopoly.appspot.com/user/password",
                   context: document.body,
                   data:{'username':username,'password':password,'token':token,'email':email},
                   dataType:"json",
                   success: function(data){
                     alert('changed password response '+JSON.stringify(data));
                   },
                     error: function(){
                     alert('error');
                   }
                 });		
    	}

/**    	
 *  functions for main page	
 */ 

    //working without network traffic
    var localmode = true;
    //activates debug output
    var debugmode = true;	
    
    /**
     * loads the data for the calltype via JSON-request and updates the given table-id with the data, if parseable
     * @param id table to update
     * @param callType type of call to make for JSON
     * @param startVal on list functions for paging results (default: 0)
     * @param endVal on list functions for paging results (default: 10)
     */
	function updateTable(id, callType, startVal, endVal){
  		
		//check values
  		if(typeof id == "undefined" || typeof callType == "undefined"){
  			return; 
  		}
  		
  		if(typeof startVal == "undefined"){
  			startVal = 0;
  		}
  		
  		if(typeof endVal == "undefined"){
  			endVal  = 10;
  		}
  		
  		if(localmode){
  			//load static files instead of connecting to the live server
  			url = "/"+callType+".json";
  		}else{
  			url = "user/"+callType+"?start="+startVal+"&end="+endVal;
  		}
  		
  		$.getJSON(url, function(jsonData){
  			
  			logger(jsonData);
  			//delete ajax indicator
  			$("#"+ callType +"_list tbody").html("");
  			
  			//add the entries
  			$(jsonData).each(function(intIndex){
  				logger(jsonData[intIndex]);
  				entry = objectToArrayVar(callType, jsonData[intIndex], intIndex);
  				logger(entry);
  				row = buildTableRow(entry)
  				logger(row);
  				$("#"+ callType +"_list tbody").append(row);
  			});
  			
  			//attach the tablesorter
  			$("#"+ callType +"_list").tablesorter({widgets: ['zebra']}); 
  			//TODO add jquery observer for each column
  			
  		});
  		
  	}
	
	/**
	 * converts our json-objects into arrays of strings
	 * @param callType defines how the object should be handled
	 * @param jsonData the json-object to convert
	 * @param intIndex helper index to handle lists better
	 * @returns {Array} of object instances
	 */
	function objectToArrayVar(callType, jsonData, intIndex){
		
		var entryData = new Array();
		
		switch (callType) {
		case "top":
			
			user = jsonData["org.immopoly.common.User"];
			
			entryData.push( intIndex+1 ); //Rank is index+1
			entryData.push( user.username );
			entryData.push( formatMoney(user.info.balance) );
			entryData.push( formatMoney(user.info.lastRent) );
			entryData.push( formatMoney(user.info.lastProvision) );
			
			break;
		case "history":
			history = jsonData["org.immopoly.common.History"];
			
			date = new Date(history.time);
			dateString =  date.getDate() + "." + (date.getMonth() + 1) + "." + date.getFullYear() + "&nbsp;" + date.getHours() + ":" + date.getMinutes();
			
			entryData.push(history.username);
			entryData.push(dateString);
			text = history.text;
			if(history.exposeId)
				text=text+" <a href='http://www.immobilienscout24.de/expose/"+history.exposeId+"' target='_new'>Expose "+history.exposeId+"</a>"
			entryData.push(text);

			break;

		}
		
		return entryData;
	}
  	
	/**
	 * obvious function name is obvious
	 * @param entry String array, each entry will be wrapped in <td>-element
	 * @returns {String}
	 */
  	function buildTableRow(entry){
  		
  		var row = "<tr>";
  		$(entry).each(function(intIndex){
  			
  			row += "<td>"+entry[intIndex]+"</td>";
  		});
  		
  		return row+"</tr>"
  	}
  	
  	/**
  	 * formats the given field as currency value
  	 * @param number number to format
  	 * @param currency string for currency to attach
  	 * @returns formatted value
  	 */
  	function formatMoney(number,currency){
  		
  		if(typeof currency == "undefined"){
  			currency = "Eur";
  		}
  		
  		if(isNaN(parseFloat(number))){
  			return number;
  		}

  		return parseFloat(number).toFixed(2)+ " " + currency;
  	}
  	
  	/**
  	 * links a username to his userprofile
  	 * @param field field to update with the link
  	 * @returns jQuery field on success, void otherwise
  	 */
  	function linkUserprofile(field){
  		
  		if(typeof field == "undefined"){
  			$this = $(this);
  		}else if($(field).length > 0){
  			$this = $(field).eq(0);  			
  		}else{
  			return;
  		}
  		
  		url = "/user/profile/"+$this.html();
  		$this.html('<a href="'+url+'" title="Userprofil anzeigen...">'+ $this.html()+ '</a>');
  		
  		return $this;
  	}
  	
  	/**
  	 * wrapper for safe logging (tests if console.log exists and if debugmode is true)
  	 * @param message
  	 */
  	function logger(messageObj){
  		
  		//make sure we have console attached
  		if(typeof console == "undefined" || typeof console.log == "undefined"){
  			return;
  		}
  		
  		if(typeof debugmode == undefined || debugmode == false){
  			return;
  		}
  		
  		console.log(messageObj);
  	}
  
  	//do on start
    $(document).ready(function() 
      { 
		updateTable("#top_makler","top");
		updateTable("#history_list","history");
      } 
    ); 
    
