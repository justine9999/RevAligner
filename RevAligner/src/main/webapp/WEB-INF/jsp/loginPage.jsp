<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>Revision Aligner - login</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../css/bootstrap.min.css" type="text/css" rel="stylesheet" />
<link href="../css/elusive-icons.css" rel="stylesheet" />
<link rel="shortcut icon" href="../images/ra.ico" />

<script type="text/javascript" src="../js/jquery.js"></script>	
<script type="text/javascript" src="../js/bootstrap.min.js"></script>
<script src="../js/heartcode-canvasloader-min.js"></script>
<script type="text/javascript">
    $(function(){
    	var cl = new CanvasLoader('lgloading');
		cl.setShape('spiral');
		cl.setDiameter(15);
		
    	$(".modal").modal({backdrop: "static",keyboard: false});
    	$('.modal').modal('show');
		
		$('#loginsubmit').on('click', function () {
			cl.show();
			var username = $('#username').val();
			var password = $('#password').val();
			if(username == null || username == '' || password == null || password == ''){
				cl.hide();
				return;
			}
			var obj = {'username':username,'password':password};
			
	    	$.ajax({
	            url: '/RevAligner/rac/user/checkuser',
	            type: "POST",
	            dataType: 'json',
            	data: encodeURIComponent(JSON.stringify(obj)),
	                 
	              beforeSend: function(xhr) {
	                xhr.setRequestHeader("Accept", "application/json");
	                xhr.setRequestHeader("Content-Type", "application/json");
	              },
	                 
	              success: function() {
	              	  $('#loginform').submit();
	              },
	              
	              error: function() {
					  cl.hide();
	              }
	        });
		});
		
		$(document).keypress(function(event){
			var keycode = (event.keyCode ? event.keyCode : event.which);
			if(keycode == '13'){
				event.preventDefault();
				cl.show();
				var username = $('#username').val();
				var password = $('#password').val();
				if(username == null || username == '' || password == null || password == ''){
					cl.hide();
					return;
				}
				var obj = {'username':username,'password':password};
				
		    	$.ajax({
		            url: '/RevAligner/rac/user/checkuser',
		            type: "POST",
		            dataType: 'json',
	            	data: encodeURIComponent(JSON.stringify(obj)),
		                 
		              beforeSend: function(xhr) {
		                xhr.setRequestHeader("Accept", "application/json");
		                xhr.setRequestHeader("Content-Type", "application/json");
		              },
		                 
		              success: function() {
		              	  $('#loginform').submit();
		              },
		              
		              error: function() {

		              }
		        });
			}
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

.modal-header{
	padding:5;
}

.login-dialog{
	width:340px!important;
}


.inner-addon {
  position: relative;
}

/* style glyph */
.inner-addon .el {
  position: absolute;
  padding: 10px;
  pointer-events: none;
}

/* align glyph */
.left-addon .el  { left:  0px;}
.right-addon .el { right: 0px;}

/* add padding  */
.left-addon input  { padding-left:  40px; }
.right-addon input { padding-right: 30px; }

#loginsubmit {
	font-weight:bold;
	background-color:#dddddd;
}

.btn-default{
	color:#777777;
}

#lgloading {
	position:absolute;
	margin-left:10px;
	padding-top:2px;
}
</style>
</head>
<body>
	<div class="modal fade" role="dialog">
  		<div class="modal-dialog login-dialog">
    		<div class="modal-content">
    			<div class="modal-header">
    				Revision Aligner
	      		</div>
      			<div class="modal-body">
    				<form id="loginform" action="${loginUrl}" method="post">
	    				<c:if test="${param.error != null}">
	                    	<div class="alert alert-danger">
	                        	<p>Incorrect username or password.</p>
	                        </div>
	                    </c:if>
	        			<div class="inner-addon left-addon">
		          			<i class="el el-torso"></i>
		          			<input type="text" class="form-control" id="username" name="ssoId">
		        		</div>
						</br>
						<div class="inner-addon left-addon">
							<i class="el el-key"></i>
							<input type="password" class="form-control" id="password" name="password">
						</div>
						</br>
						<button type="button" id="loginsubmit" class="btn btn-default btn-block">
							LOG IN
							<span id="lgloading" class="wrapper"></span>
						</button>
						<div class="form-actions" style="display:none">
							<input type="submit">
						</div>
					</form>
      			</div>
    		</div>
  		</div>
	</div>
</body>
</html>