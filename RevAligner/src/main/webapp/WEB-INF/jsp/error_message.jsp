<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<html>
<head>
<title>Revision Aligner - Error</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="../css/bootstrap.min.css" />
<link rel="shortcut icon" href="../images/ra.ico" />

<script type="text/javascript" src="../js/jquery.js"></script>	
<script type="text/javascript" src="../js/bootstrap.min.js"></script>
<script type="text/javascript">
    $(function(){
    	$.ajax({
	        url: '/RevAligner/rac/logoutwithoutredirect',
	        type: "GET",
	             
	        beforeSend: function(xhr) {
	            xhr.setRequestHeader("Accept", "application/json");
	            xhr.setRequestHeader("Content-Type", "application/json");
	        },
	             
	        success: function(data) {
	          	
	        },
	          
	        error: function() {
	          	alert("failed to log out");
	        }
	    });
	    	
    	var message = "${message}";
    	$('.message').html($('.message').html() + '&nbsp;&nbsp;' + message);
    	$(".modal").modal({backdrop: "static",keyboard: false});
    	$('.modal').modal('show');
		
		$('.btmp').on('click', function () {
	    	window.location.replace('entry');
		});
    });
</script>
<style>
.modal {
  	text-align: center;
  	padding: 0!important;
  	margin: 0!important;
  	height: 100%;
  	background-color: #fefefe;
}

.modal-dialog{
	text-align: center;
	display: inline-block;
	top: 20%;
}

.glyphicon-exclamation-sign{
	top:3px;
}

.sessiontimeout-dialog{
	width:340px!important;
}

.btmp{
	cursor:pointer;
}
</style>
</head>
<body>
	<div id="sessiontimeout" class="modal fade" role="dialog">
  		<div class="modal-dialog sessiontimeout-dialog">
    		<div class="modal-content">
      			<div class="modal-body">
        			<h4 class="message"><span class="glyphicon glyphicon-exclamation-sign"></span></h4>
    				<h6>back to <a class="btmp">main page</a></h6>
      			</div>
    		</div>
  		</div>
	</div>
</body>
</html>