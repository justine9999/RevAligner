<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE HTML>
<html>
<head>
<meta charset="utf-8">
<title>Revision Aligner</title>
<script src="../js/jquery.min.js"></script>
<script src="../js/sockjs-0.3.4.js"></script>
<script src="../js/stomp.js"></script>
<script src="../js/jquery.flip.min.js"></script>

<script src="../js/jquery.ui.widget.js"></script>
<script src="../js/jquery.iframe-transport.js"></script>
<script src="../js/jquery.fileupload.js"></script>
<script src="../js/progressbar.js"></script>

<script src="../js/bootstrap.min.js"></script>
<link href="../css/bootstrap.min.css" type="text/css" rel="stylesheet" />
<link href="../css/tracks.css" type="text/css" rel="stylesheet" />
<link href="../css/elusive-icons.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/fontastic.css">
<link rel="stylesheet" href="../css/font-awesome.css">

<link rel="shortcut icon" href="../images/ra.ico" />

<script src="../js/myuploadfunction.js"></script>
<script src="../js/heartcode-canvasloader-min.js"></script>

<style>
body {
	background: url(${pageContext.request.contextPath}/images/bodybg3.png);
	overflow:hidden;
}

.front-content{
	width:1050px;
	padding:15px
}

.back-content{
	width:1050px;
	padding:15px
}
.cornerbtn{
	float:right;
	position:fixed;
	top: -10px;
	left: 95%;
	z-index:999;
	color: #666;
}

.progress-bar {
	background-color: #999;
}

.progress {
  position: relative;
}

.progress span {
    position: absolute;
    display: block;
    width: 100%;
    color: black;
}

.notransition {
  -webkit-transition: none !important;
  -moz-transition: none !important;
  -o-transition: none !important;
  -ms-transition: none !important;
  transition: none !important;
}

.btn-primary { 
  color: #ffffff; 
  background-color: #888888; 
  border-color: #888888; 
} 
 
.btn-primary:hover, 
.btn-primary:focus, 
.btn-primary:active, 
.btn-primary.active, 
.open .dropdown-toggle.btn-primary { 
  color: #ffffff; 
  background-color: #666666; 
  border-color: #888888; 
} 
 
.btn-primary:active, 
.btn-primary.active, 
.open .dropdown-toggle.btn-primary { 
  background-image: none; 
} 
 
.btn-primary.disabled, 
.btn-primary[disabled], 
fieldset[disabled] .btn-primary, 
.btn-primary.disabled:hover, 
.btn-primary[disabled]:hover, 
fieldset[disabled] .btn-primary:hover, 
.btn-primary.disabled:focus, 
.btn-primary[disabled]:focus, 
fieldset[disabled] .btn-primary:focus, 
.btn-primary.disabled:active, 
.btn-primary[disabled]:active, 
fieldset[disabled] .btn-primary:active, 
.btn-primary.disabled.active, 
.btn-primary[disabled].active, 
fieldset[disabled] .btn-primary.active { 
  background-color: #888888; 
  border-color: #888888;
  cursor: default;
} 
 
.btn-primary .badge { 
  color: #888888; 
  background-color: #ffffff; 
}

.front, .back {
  background-color: #fff;
}

.fade {
    opacity: 0;
    -webkit-transition: opacity .3s linear;
    transition: opacity .3s linear;
}

.modal-header {
	padding:6px;
	background-color: #aaaaaa;
	font-family: "Trebuchet MS", Helvetica, sans-serif
	
    -webkit-border-top-left-radius: 6px!important;
    -webkit-border-top-right-radius: 6px!important;
    -moz-border-radius-topleft: 6px!important;
    -moz-border-radius-topright: 6px!important;
    border-top-left-radius: 6px!important;
    border-top-right-radius: 6px!important;
}

.modal {
  	text-align: center;
  	padding: 0!important;
  	margin: 0!important;
  	height: 100%;
}

.modal-body {
    padding:9px 15px;
    border-bottom:1px solid #ffffff;
    background-color: #ffffff;
    -webkit-border-top-left-radius: 5px;
    -webkit-border-top-right-radius: 5px;
    -webkit-border-bottom-left-radius: 5px;
    -webkit-border-bottom-right-radius: 5px;
    -moz-border-radius-topleft: 5px;
    -moz-border-radius-topright: 5px;
    -moz-border-radius-bottomleft: 5px;
    -moz-border-radius-bottomright: 5px;
    border-top-left-radius: 5px;
    border-top-right-radius: 5px;
    border-bottom-left-radius: 5px;
    border-bottom-right-radius: 5px;
}

.modal-footer {
	padding:6px;
	background-color: #eeeeee;
	border-bottom:1px solid #ffffff!important;
    -webkit-border-bottom-left-radius: 6px!important;
    -webkit-border-bottom-right-radius: 6px!important;
    -moz-border-radius-bottomleft: 6px!important;
    -moz-border-radius-bottomright: 6px!important;
    border-top-bottom-radius: 6px!important;
    border-top-bottom-radius: 6px!important;
}

.modal-dialog{
	text-align: left;
	display: inline-block;
}

.createprj {
	width:600px;
	top:25%;
}

.iniprj {
	width:1050px;
	top:20%;
}

.continueprj {
	width: 750px;
	top:25%;
}

.next {
	padding:4px;
}

.next2 {
	padding:4px;
}

.backtomain {
	padding:4px;
	position:relative;
	float:left;
	left:10px;
}

.modal-title {
	font-weight: bold;
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
.left-addon input  { padding-left:  30px; }
.right-addon input { padding-right: 30px; }

#new, #continue {
	font-weight: bold;
	color: #444444;
}

.langs {
	height: auto;
    max-height: 180px;
    overflow-x: hidden;
    width: 320px !important;
}

.dropdown button {
	width:230px;
}

#projectid{
	color:#8A0829;
	overflow:hidden;
}

#ctprojectid{
	color:#A94442;
	font-size:16px;
}

#submission{
	color:#3A8D98;
	overflow:hidden;
}

#prvprjnum, #prvprjpkg, #browsepkg, #searchprj, #validatepkg{
	margin-left:0px;
	margin-top:4px;
	height:35px;
}

#tokenstring, #token {
	color:#132e82;
	margin-left:0px;
	margin-top:4px;
}

.el-check-empty, el-check {
	cursor:pointer;
}

#searchprjgood, #validateprjgood, #processfilegood, #converttargetgood{
	display:none;
	color: #088A68;
}

#searchprjbad, #validateprjbad, #processfilebad, #converttargetbad {
	display:none;
	color: #FF4000;
}

#searchprjnoaccess {
	display:none;
	color: #3765D0;
}

textarea:focus, input:focus, .uneditable-input:focus {   
    border-color: rgba(128, 128, 128, 0.8) !important;
    box-shadow: 0 1px 1px rgba(128, 128, 128, 0.075) inset, 0 0 8px rgba(128, 128, 128, 0.6) !important;
    outline: 0 none !important;
}

.icon{
	display:inline;
	margin:0;
	padding:0;
	position:relative;
	top:4px;
	font-size:20px
}

.headtext{
	display:inline;
	margin:0;
	padding:0;
	position:relative;
	font-size:18px
}

.wrapper {
	float:right;
	top:5px;
}

#gtaloading {
	position:absolute;
	margin-left:10px;
}

#downloadtrgloading {
	position:absolute;
	margin-left:10px;
}

canvas {
	vertical-align:middle;
}

.icon-stack {
    display: inline-block;
    position: relative;
    vertical-align: -35%;
}

.icon-stack [class^="icon-"], .icon-stack [class*=" icon-"] {
    display: block;
    height: 100%;
    line-height: inherit;
    position: absolute;
    text-align: center;
    width: 100%;
}

.glyphicon-ban-circle
{
    color:red;
}

a{
	cursor:pointer;
}

#alignsegoption {
	padding-bottom:5px;
	cursor:pointer;
	color:#444444;
}

#alignsegoption i{
	font-weight:bold;
}

.aligntype:hover{
	color:#000000;
}

.useautosave {
	margin-left:30px;
}

.useautosave:hover{
	color:#000000;
}

#aligntrgoption {
	padding-top:10px;
	padding-bottom:10px;
	padding-left:2px;
	cursor:pointer;
	color:#444444;
}

#aligntrgoption i{
	font-weight:bold;
}

.preservefmt:hover{
	color:#000000;
	margin-left: 20px;
}

.notpreservefmt:hover{
	color:#000000;
}

.table{
	margin-bottom:0px;
}

td{
	max-width:400px!important;
	word-wrap:break-word;
}

.fa-user {
	margin-top: 2px;
}

.navbar {
	z-index: 1200;
	margin:0;
	height:50px;
}

.ma{
	margin-top:3px;
}

.navbar-brand{
	font-family: Baskerville, "Hoefler Text", Garamond, "Times New Roman", serif;
	font-weight:bold;
}

.checkbox, .radio {
	margin-left: 10px;
	display: inline;
}

.alignres {
	position: relative;
	padding: .3em .6em .2em;
	margin-left: 8px;
}

.prjlist {
	position:absolute;
	float:left;
	top:50px;
	left:-50%;
	width:50%;
	height:calc(100% - 50px);
	padding:10px;
	background-color:#ccc;
	overflow-y:auto;
	overflow-x:hidden;
	z-index:2000;
}

.prjlist .header {
	font-weight: bold;
}

.user-menu {
    width: 200px !important;
}

.prjitemheader {
	margin-right:0px;
	margin-left:-15px;
	padding:2px 2px 2px 0px;
	border-bottom-style:solid;
	border-bottom-width:1px;
	word-wrap: break-word;
}

.prjitem {
	margin-right:0px;
	margin-left:-15px;
	padding:2px 2px 2px 0px;
	background-color:#ccc;
	margin-top:2px;
	border-bottom-style:solid;
	border-bottom-width:1px;
	cursor: pointer !important;
	word-wrap: break-word;
}

.prjloaded {
	color:#31708f;
}

.prjitem:hover {
	background:#ddd;
}

.prjicon {
	color:#888;
}

#prjlistloading {
	position: absolute;
	left:45%;
	top:45%;
	margin:0;
	opacity:0.2
}

.disabled {
    pointer-events:none;
    opacity:0.6;
}

.prjlistclose {
	cursor: pointer;
	position: absolute;
	right:10px;
	z-index:2000;
}

.prjlist::-webkit-scrollbar-track
{
	-webkit-box-shadow: inset 0 0 6px rgba(0,0,0,0.3);
	background-color: #ccc;
}

.prjlist::-webkit-scrollbar
{
	width: 20px;
	background-color: #ccc;
}

.prjlist::-webkit-scrollbar-thumb
{
	-webkit-box-shadow: inset 0 0 6px rgba(0,0,0,.3);
	background-color: #ccc;
	border-style:solid;
	border-width:1px;
	border-color:#fff;
}

.prjlist::-webkit-scrollbar-thumb:hover {
    background-color: #bbb;
}

.prjlist::-webkit-scrollbar-thumb:active {
    background-color: #aaa;
}

.show {
	letter-spacing: 2px;
}

.popover {background-color: tomato; color:#fff}
.popover.top > .arrow:after {border-top-color: tomato; }
.popover-content {background-color: tomato;}

.prjprogress {
  height: 3px;
  position: relative;
  font-size: 8px;
  text-align: right !important;
}

.prjname {
  padding-top: 2px;
  border-right-style: solid;
  border-right-width: 1px;
  border-right-color: #aaaaaa;
  font-size: 11px;
}

.prjsubname {
	padding-top: 2px;
	font-size: 11px;
}

.prjdate {
	padding-top: 2px;
	font-size: 10px;
}

.prjuser {
	padding-top: 2px;
	font-size: 10px;
}

.center {
	text-align: center;
}

.load_barlittle {
    background-color: #999;
    background-image: -moz-linear-gradient(45deg, #999 25%, #999);
    background-image: -webkit-linear-gradient(45deg, #999 25%, #999);
    border-left: 1px solid #999;
    border-top: 1px solid #999;
    border-right: 1px solid #999;
    border-bottom: 1px solid #999;
    margin-left:5px;
    margin-right:5px;
    width: 15px;
    height: 15px;
    display:inline-block;
    opacity: 0.1;
    -moz-transform: scale(0.7);
    -webkit-transform: scale(0.7);
    -moz-animation: move 1s infinite linear;
    -webkit-animation: move 1s infinite linear;
}

#load_block_1 {
    -moz-animation-delay: .3s;
    -webkit-animation-delay: .3s;
}

#load_block_2 {
    -moz-animation-delay: .2s;
    -webkit-animation-delay: .2s;
}

#load_block_3 {
    -moz-animation-delay: .3s;
    -webkit-animation-delay: .3s;
}

@-moz-keyframes move {
    0% {
        -moz-transform: scale(1.2);
        opacity: 1;
    }

    100% {
        -moz-transform: scale(0.7);
        opacity: 0.1;
    };
}

@-webkit-keyframes move {
    0% {
        -webkit-transform: scale(1.2);
        opacity: 1;
    }

    100% {
        -webkit-transform: scale(0.7);
        opacity: 0.1;
    };
}

.prjloadbutton {
	opacity:0.3;
	font-size:50px;
	vertical-align:middle;
	display:inline-block;
	cursor:pointer;
}

.prjloadbutton:hover, .prjloadbutton:focus, .prjloadbutton:active {
    color:#000;
}

.loadanime {
	vertical-align:middle;
	display:inline-block;
}

.prjload {
	height: 60px;
	line-height: 75px;
	text-align: center;
}
</style>
</head>
<body>
<nav class="navbar navbar-inverse navbar-fixed-top">
  	<div class="container-fluid">
    	<div class="navbar-header">
      		<a class="navbar-brand">REVISION ALIGNER</a>
    	</div>
    	<div>
    		<ul class="nav navbar-nav" style="float:right">
    			<li description="Manage Acount" class="pull-right">
    				<a class="dropdown-toggle usericon" data-toggle="dropdown">
    					<span class="usernaamelabel"></span>
    					<i class="fa fa-user fa-lg"></i>
    				</a>
    				<ul class="dropdown-menu user-menu">
    					<li><a class="profile">Acount Info<i class="fa fa-street-view pull-right ma"></i></a></li>
    					<li><a class="projects disabled">Show Project List<i class="fa fa-list-alt pull-right ma"></i></a></li>
    					<li class="divider"></li>
            			<li><a class="signout">Sign Out<i class="fa fa-sign-out pull-right ma"></i></a></li>
          			</ul>
    			</li>
    		</ul>
    	</div>
  	</div>
</nav>
<div class="prjlist">
	<span id="prjlistloading" class="wrapper"></span>
	<div class="row prjitemheader">
	  <div class="col-sm-3 header">Project Name</div>
	  <div class="col-sm-2 header center">Progress</div>
	  <div class="col-sm-2 header center">Submission Name</div>
	  <div class="col-sm-2 header center">Date Created</div>
	  <div class="col-sm-1 header center">User</div>
	  <div class="col-sm-1 header center"></div>
	  <div class="col-sm-1 header prjlistclose center"><i class="el el-remove"></i></div>
	</div>
</div>
<div id="createproject" class="modal fade" role="dialog">
	<div class="modal-dialog modal-lg createprj">
    	<div class="modal-content">
    		<div class="modal-header">
    			&nbsp;
        		<div class="icon icon-file-directory"></div>
    			<div class="headtext">&nbsp;OPEN PROJECT</div>
      		</div>
    		<div class="modal-body">
    			<div class="inner-addon left-addon">
          			<i class="el el-check-empty checknew"></i>
          			<button id="new" type="button" class="btn btn-default btn-block">Create New Project</button>
        		</div>
				</br>
				<div class="inner-addon left-addon">
					<i class="el el-check-empty checkcont"></i>
					<button id="continue" type="button" class="btn btn-default btn-block">Continue Previous Project</button>
				</div>
			</div>
			<div class="modal-footer">
        		<button type="button" class="btn btn-default next" disabled>
        			&nbsp;&nbsp;Next&nbsp;&nbsp;
        			<span id="createprjloading" class="wrapper"></span>
        		</button>
        		&nbsp;&nbsp;&nbsp;
      		</div>
    	</div>
  	</div>
</div>
<div id="uploadModal" class="modal fade" role="dialog">
	<div class="modal-dialog modal-lg iniprj">
    	<!-- Modal content-->
    	<div class="cornerbtn nready"><h3><span class="glyphicon pull-left glyphicon-transfer"></span></h3></div>
    	<div id="card"> 
  			<div class="front"> 
    			<div class="modal-content">
    				<div id="prjreview" class="modal-header">
        				&nbsp;
        				<div class="icon icon-file-submodule"></div>
    					<div class="headtext">&nbsp;CREATE NEW PROJECT</div>
      				</div>
    				<div class="front-content">
    					<div class="row">
    						<div class="col-md-3">
    							<label>Project ID</label>
    						</div>
    						<div class="col-md-3">
    							<label>Submission Name</label>
    						</div>
    						<div class="col-md-3">
    							<label>Source Language</label>
    						</div>
    						<div class="col-md-3">
    							<label>Target Language</label>
    						</div>
    					</div>
    					<div class="row">
    						<div class="col-md-3">
    							<textarea class="form-control" rows="1" id="projectid" disabled></textarea>
    						</div>
    						<div class="col-md-3">
    							<textarea class="form-control" rows="1" id="submission" spellcheck=false></textarea>
    						</div>
    						<div class="col-md-3">
    							<div class="dropdown">
  									<button class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">Choose One&nbsp;&nbsp;&nbsp;&nbsp;<span class="caret"></span></button>
  									<ul id="src_lang" class="dropdown-menu langs">
  										<li><a>English</a></li>
  										<li class="divider"></li>
  									</ul>
								</div>
    						</div>
    						<div class="col-md-3">
    							<div class="dropdown">
  									<button class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">Choose One&nbsp;&nbsp;&nbsp;&nbsp;<span class="caret"></span></button>
  									<ul id="trg_lang" class="dropdown-menu langs"></ul>
								</div>
    						</div>
    					</div>
  						<hr/>
						<button id="addsrc" type="button" class="btn btn-primary" disabled>Add Source</button>
						<button id="addtrg" type="button" class="btn btn-primary" disabled>Add Target</button>
						<button class="uploadfiles btn btn-primary" type="button" disabled>Upload Files</button>
						<button class="processfiles btn btn-primary" type="button" data-toggle="popover" data-placement="top" data-trigger="focus" disabled>
							<span id="prcsbtntext">Process Files</span>
							&nbsp;&nbsp;
							<span id="pfloading" class="wrapper"></span>
							<span id="processfilegood" class="glyphicon glyphicon-ok"></span>
       						<span id="processfilebad" class="glyphicon glyphicon-remove"></span>
						</button>
						<div class="radio">
							<label><input type="radio" name="alignertype" value="auto" checked>Auto Aligner</label>
							<span class="label label-primary alignres"></span>
						</div>
						<div class="radio">
							<label><input type="radio" name="alignertype" value="sequential">Sequential Aligner</label>
						</div>
						<input id="fileuploadsrc" class="fupload" type="file" name="files[]" data-url="upload/source" multiple style='display: none;'>
						<input id="fileuploadtrg" class="fupload" type="file" name="files[]" data-url="upload/target" multiple style='display: none;'>
						<br/>
						<br/>
						<div class="progress">
    						<div id = "progress_all" class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">
    							<span class="show">0% Complete</span>
  							</div>
						</div>
						<table id="added-files" class="table">
							<tr id="tbheader">
								<th style="width:10%">S / T</th>
								<th style="width:30%">File Name</th>
								<th style="width:10%">File Size</th>
								<th style="width:20%">Stats</th>
								<th style="width:10%">Action</th>
								<th style="width:20%">Progress</th>
							</tr>
						</table>
						<div id="alignsegoption" style="display:none">
							<i class="el el-check aligntype">&nbsp;&nbsp;Align on segment level</i>
							<i class="el el-check-empty useautosave">&nbsp;&nbsp;Use the lastest auto-saved data</i>
						</div>
						<button id="goAlignment" type="button" class="btn btn-primary btn-block" disabled>
							<span class="glyphicon glyphicon-edit"></span>
							&nbsp;
							Go To Revision Aligner
							<span id="gtaloading"></span>
						</button>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default backtomain">&nbsp;&nbsp;Back&nbsp;&nbsp;</button>&nbsp;&nbsp;&nbsp;
		      		</div>
    			</div>
  			</div> 
  			<div class="back">
    			<div class="modal-content">
    				<div class="modal-header">
        				<h4 class="modal-title">&nbsp;&nbsp;<i class="el el-file-edit headtext"></i>&nbsp;&nbsp;CREATE TARGET FILE<span id="ctprojectid"><span></h4>
      				</div>
    				<div class="back-content">
						<button id="addtrans" type="button" class="btn btn-primary">Add Translated Txlf</button>
						<button class="uploadtrans btn btn-primary" type="button" disabled>Upload Files</button>
						<button class="processtrans btn btn-primary" type="button" disabled>
							<span id="convbtntext">Convert File</span>
							&nbsp;&nbsp;
							<span id="cfloading" class="wrapper"></span>
							<span id="converttargetgood" class="glyphicon glyphicon-ok"></span>
       						<span id="converttargetbad" class="glyphicon glyphicon-remove"></span>
						</button>
						<input id="fileuploadtrans" class="fuploadtrans" type="file" name="files[]" data-url="upload/merge" multiple style='display: none;'>
						<div id="aligntrgoption">
							<i class="el el-check notpreservefmt">&nbsp;&nbsp;Match Source formatting</i>
							<i class="el el-check-empty preservefmt">&nbsp;&nbsp;Match old target formatting</i>
						</div>
						<div class="progress">
    						<div id = "progress_all_trans" class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">
    							<span class="show_trans">0% Complete</span>
  							</div>
						</div>
						<table id="added-files-trans" class="table">
							<tr id="tbheader_trans">
								<th style="width:40%">File Name</th>
								<th style="width:10%">File Size</th>
								<th style="width:20%">Stats</th>
								<th style="width:10%">Action</th>
								<th style="width:20%">Progress</th>
							</tr>
						</table>
						<button id="downloadTarget" type="button" class="btn btn-primary btn-block" disabled>
								<span class="glyphicon glyphicon-save"></span>
								&nbsp;
								Download Target File
								<span id="downloadtrgloading"></span>
						</button>
					</div>
    			</div>
  			</div> 
		</div>
  	</div>
</div>
<div id="continueproject" class="modal fade" role="dialog">
	<div class="modal-dialog modal-lg continueprj">
    	<div class="modal-content">
    		<div class="modal-header">
    			&nbsp;
        		<div class="icon icon-file-symlink-directory"></div>
    			<div class="headtext">&nbsp;CONTINUE PREVIOUS PROJECT</div>
      		</div>
    		<div class="modal-body">
    			<i class="el el-check-empty checkprjnum">&nbsp;&nbsp;<label>Find old project by project number</label></i>
    			<div class="input-group">
   					<input type="text" class="form-control" id="prvprjnum" spellcheck=false disabled>
   					<span class="input-group-btn">
       					<button id="searchprj" class="btn btn-default" type="button" disabled>
       						<span id="searchact" class="glyphicon glyphicon-search"></span>
       						<span id="searchprjloading" class="wrapper"></span>
       						<span id="searchprjgood" class="glyphicon glyphicon-ok"></span>
       						<span id="searchprjbad" class="glyphicon glyphicon-remove"></span>
       						<span id="searchprjnoaccess" class="glyphicon glyphicon-hourglass"></span>
       					</button>
   					</span>
				</div>
				<div class="input-group">
					<span class="input-group-btn">
       					<button id="token" class="btn btn-default" type="button" disabled>
       						<i class="fa fa-key" aria-hidden="true"></i>
       					</button>
   					</span>
   					<input type="text" class="form-control" id="tokenstring" spellcheck=false disabled>
				</div>
				</br>
				<i class="el el-check-empty checkprjpkg">&nbsp;&nbsp;<label>Recreate from project package</label></i>
				<div class="input-group">
   					<input type="text" class="form-control" id="prvprjpkg" disabled>
   					<span class="input-group-btn">
       					<button id="browsepkg" class="btn btn-default" type="button" disabled>Browse</button>
       					<input id="fileuploadpkg" class="fuploadpkg" type="file" name="files[]" data-url="upload/pkg" multiple style='display: none;'>
   						<button id="validatepkg" class="btn btn-default" type="button" disabled>
   							<span id="validateact" class="glyphicon glyphicon-hand-left"></span>
   							<span id="validateprjloading" class="wrapper"></span>
       						<span id="validateprjgood" class="glyphicon glyphicon-ok"></span>
       						<span id="validateprjbad" class="glyphicon glyphicon-remove"></span>
   						</button>
   					</span>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default backtomain">&nbsp;&nbsp;Back&nbsp;&nbsp;</button>&nbsp;&nbsp;&nbsp;
        		<button type="button" class="btn btn-default next2" disabled>&nbsp;&nbsp;Next&nbsp;&nbsp;</button>&nbsp;&nbsp;&nbsp;
      		</div>
    	</div>
  	</div>
</div>
</body> 
</html>