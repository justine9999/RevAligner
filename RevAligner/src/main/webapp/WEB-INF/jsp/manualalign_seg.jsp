<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<html>
<head>
<title>Revision Aligner - Editor</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="../css/bootstrap.min.css" />
<link rel="stylesheet" href="../css/font-awesome.css">
<link href="../css/tracks.css" type="text/css" rel="stylesheet" />
<link href="../css/aligner.css" type="text/css" rel="stylesheet" />
<link href="../css/jquery.gridster.css" type="text/css" rel="stylesheet" />
<link href="../css/elusive-icons.css" rel="stylesheet" />
	
<link href="../css/simplePagination.css" rel="stylesheet" />
<link rel="shortcut icon" href="../images/ra.ico" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/bootstrap-submenu.min.js"></script>
<script type="text/javascript" src="../js/jquery.gridster.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/bootstrap.min.js"></script>
	
<script type="text/javascript" src="../js/jquery.simplePagination.js"></script>
<script type="text/javascript" src="../js/aligner.js"></script>
<script src="../js/heartcode-canvasloader-min.js"></script>
<script src="../js/progressbar.js"></script>
<script src="//cdn.jsdelivr.net/jquery.shadow-animation/1/mainfile"></script>
<script type="text/javascript">
    $(function(){      
      var prjid;
      var srclang;
      var trglang;
      var isfareast;
      var empty_next_index = 0;
	  var gridster_src;
	  var gridster_trg;
	  var gridster_rmv_trg;
	  var gridster_more_trg;
	  var sources = [];
	  var sources_tctypes = [];
	  var targets = [];
	  var removed_targets = [];
	  var locked_targets = [];
	  var edited_targets = [];
	  var review_targets = [];
	  var ignore_sources = [];
	  var unit_color_code = "#F5F3EE";
	  var prev_unit_color_code = unit_color_code;
	  var lockedtargetsnum = 0;
	  
	  var maxSegPerPage = 50;
	  var maxSubSegPerPage = 20;
	  var max_display_rmv_trgs = 50;
	  var total_storable_removed_trgs = 0;
	  var total_pre_loadable_trgs = 50;
	  var timeOutCheckInterval = 60000;
	  var autoSaveInterval = 60000;
	  var intervalId;
	  var issavingfile = false;
	  var isfirstin = true;
	  var issearch = false;
	  
	  var currPageNum = 1;
	  var lastPageNum;
	  var lastPageSegNum;
	  var draggableoldrow;
	  
	  var currentSegAlign;
	  var currentEditedText;
	  
	  var username;
	  
	  var windowWidth = window.innerWidth;
	  var windowHeith = window.innerHeight;
	  
	  var progress_bad_color = '#FF8082';
	  var progress_middle_color = '#FFEA82';
	  var progress_done_color = '#80FFC8';
	  var progress_curr_color = progress_bad_color;
	  var bar = new ProgressBar.SemiCircle(prjprogress, {
  	  	strokeWidth: 8,
  	  	color: '#FFEA82',
  	  	trailColor: '#EEEEEE',
    	trailWidth: 1,
  	  	easing: 'easeInOut',
  	  	duration: 1600,
  	  	svgStyle: null,
  	  	text: {
    		value: '',
    		alignToBottom: true
  		},
  		from: {color: '#FFEA82'},
  		to: {color: '#FFEA82'},

  		step: (state, bar) => {
    		bar.path.setAttribute('stroke', state.color);
    		var value = Math.round(bar.value() * 100);
    		//if (value === 0) {
      			//bar.setText('');
    		//} else {
      			bar.setText(value + '%');
    		//}

    		bar.text.style.color = state.color;
  		}
	  });
	
	  bar.text.style.fontFamily = '"Raleway", Helvetica, sans-serif';
	  bar.text.style.fontSize = '1rem';

	  $("#cover").show();
	  var cl0 = new CanvasLoader('coverloading'); 
	  cl0.setShape('spiral');
	  cl0.setDiameter(90);
	  cl0.setColor('#bbbbbb');
	  cl0.setDensity(100);
	  cl0.setFPS(50);
	  cl0.show();
	  
	  $.widget.bridge('uitooltip', $.ui.tooltip);
	  
	  var test = "${prjnum}";
	  if(test != '' && test != null){
	  	  prjid = test;
	  	  $('#prjid').text(" " + prjid);
	  }
	  
	  isfareast = "${isfareast}";
	  $('nav').hide();

	  $.ajax({
	        url: '/RevAligner/rac/readappbaseconfiguration',
	        type: "POST",
	        dataType: 'json',
	             
	          beforeSend: function(xhr) {
	            xhr.setRequestHeader("Accept", "application/json");
	            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	          },
	             
	          success: function(data) {
	        	    timeOutCheckInterval = data.timeoutcheckinterval*1000;
	        	    intervalId_checkSession = setInterval(function(){validateSession()}, timeOutCheckInterval);
	        	    autoSaveInterval = data.autosaveinterval*1000;
	        	    intervalId_autoSave = setInterval(function(){autoSaveAlignment()}, timeOutCheckInterval);
	          },
	          
	          error: function(xhr) {
					alert('failed to read App base configuration, using default...');
					intervalId_checkSession = setInterval(function(){validateSession()}, timeOutCheckInterval);
					intervalId_autoSave = setInterval(function(){autoSaveAlignment()}, timeOutCheckInterval);
	            }
	    });
	  
	    $.ajax({
	        url: 'setupuser',
	        type: "POST",
	        dataType: 'json',
	             
	          beforeSend: function(xhr) {
	            xhr.setRequestHeader("Accept", "application/json");
	            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	          },
	             
	          success: function(data) {
	        	    username = data.username;
	        	    $('.usernamelabel').html("[ " + username + " ]&nbsp;&nbsp;");
	        	    if(data.isadmin == 'true'){
	        	    	$('.usericon').removeClass('fa-user');
	        	    	$('.usericon').addClass('fa-user-circle');
	        	    }
	          },
	          
	          error: function(xhr) {
					alert('failed to fetch current user info...');
	            }
	    });
	    
	    $(window).on('beforeunload', function(){
			if(typeof prjid == 'undefined') {
				//return;
			}
			
			if(issavingfile){
				return;
			}
				
			var arr1 = [];
			var arr2 = [];
			for (i = 0; i < targets.length; i++) { 
	    		var seq = getSeq(targets[i]);
	    		var text = getHtmlText(targets[i]);
	    		arr1[i] = text;
	    		arr2[i] = seq;
			}
			
			var arr3 = [];
			var arr4 = [];
			for (i = 0; i < removed_targets.length; i++) { 
				var seq = getSeq(removed_targets[i]);
				var text  = getHtmlText(removed_targets[i]);
				arr3[i] = text;
	    		arr4[i] = seq;
			}
			
			var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
			
	    	$.ajax({
	            url: '/RevAligner/rac/cancel_session_and_auto_save_seg',
	            async: false,
	            type: "POST",
	            dataType: 'json',
	            data: encodeURIComponent(JSON.stringify(obj)),
	                 
	              beforeSend: function(xhr) {
	                xhr.setRequestHeader("Accept", "application/json");
	                xhr.setRequestHeader("Content-Type", "application/json");
	              },
	                 
	              success: function() {

	              },
	              
	              error: function() {

	              }
	        });
        
			/*$.ajax({
				url: '/RevAligner/rac/cancelsession',
				//url: '/RevAligner/rac/logout',
				async: false,
				type: "GET",
					                 
				beforeSend: function(xhr) {
					xhr.setRequestHeader("Accept", "application/json");
					xhr.setRequestHeader("Content-Type", "application/json");
				},
		                 
		        success: function() {

		        },
		              
		        error: function() {

		        }
		   });*/
		});
	
	    $('body').on('click', '*', function(e) {
	    	
	    	if(typeof prjid == 'undefined') {
				return;
			}
	    	
	    	$.ajax({
	        	url: '/RevAligner/rac/updatelastaccesstime',
	        	type: "GET",
	             
	          	beforeSend: function(xhr) {
	            	xhr.setRequestHeader("Accept", "application/json");
	            	xhr.setRequestHeader("Content-Type", "application/json");
	          	},
	             
	          	success: function(data) {
	          		
	          	},
	          
	          	error: function() {
	          	  	
	          	}
	    	});
	    });
	  
	  $('[data-submenu]').submenupicker();
	  
	  $('#moretrgcol').addClass('notransition');
	  
	  srclang = "${srclang}";
	  trglang = "${trglang}";
	  empty_next_index = "${nullcnt}";
		<c:forEach items="${s_map}" var="listItem">
  			sources.push("<c:out value="${listItem.value[0]}" />" + "#" + "<c:out value="${listItem.value[2]}" />");
  		    sources_tctypes.push("<c:out value="${listItem.value[1]}" />");
		</c:forEach>
	  
	  //verification
	  $.ajax({
	        url: '/RevAligner/rac/checksessionexistence',
	        type: "POST",
 
	          success: function() {
	        	if(sources.length == 0){
					redirectwithpost('/RevAligner/rac/errorloadingaligner');
				}else{
					$('nav').show();
					$('.panel').show();
					$('.seg_progress_holder').show();
					$('.seg_progress').show();
					$('.footer').show();
					var next_page_lt = $('#targetgrids').position().left + $('#targetgrids').width() - $('#next_page').width() - 15;
	    			$('#next_page').css('left',next_page_lt+'px');
	    			
	    			var down_page_lt = ($('#sourcegrids').position().left + $('#sourcegrids').width() + $('#targetgrids').position().left - $('#down_page').width() - 15)/2;
	    			$('#down_page').css('left',down_page_lt+'px');
	    			$('#down_page').show();
	    			
	    			//cl0.hide();
	    			$('#cover').fadeOut( "slow", function() {
				    	$('#cover').empty();
	    				$("#cover").css('opacity','0.3');
	    				isfirstin = false;
				    });
		
					var elem = $('.panel-body');
				    if(elem[0].scrollHeight == (elem.scrollTop() + elem.outerHeight())) {
				        $('.footer').stop().animate({'bottom':'0px'},200);
				    }
				    
				    updateProgress();
				}
	          },
	          
	          error: function() {
					//redirectwithpost('/RevAligner/rac/sessiontimesout');
					$(window).unbind("beforeunload");
					window.location.replace('logout');
	        }
	  });
	
		<c:forEach items="${t_map}" var="listItem">
  			targets.push("<c:out value="${listItem.value[0]}" />" + "#" + "<c:out value="${listItem.value[1]}" />");
		</c:forEach>
		
		<c:forEach items="${m_map}" var="listItem">
  			removed_targets.push("<c:out value="${listItem.value[0]}" />" + "#" + "<c:out value="${listItem.value[1]}" />");
  			total_storable_removed_trgs++;
		</c:forEach>
		
		total_storable_removed_trgs = total_storable_removed_trgs + 20;
		$('.num_trg_storable').text(total_storable_removed_trgs);
			
		<c:forEach items="${lock_para_seq_map}" var="listItem">
  			locked_targets.push("<c:out value="${listItem.value}" />");
  			lockedtargetsnum++;
		</c:forEach>
			
		<c:forEach items="${review_para_seq_map}" var="listItem">
  			review_targets.push("<c:out value="${listItem.value}" />");
		</c:forEach>
			
		<c:forEach items="${ignore_para_seq_map}" var="listItem">
  			ignore_sources.push("<c:out value="${listItem.value}" />");
		</c:forEach>
		
		//source grid template
	    $combo_source = $('<div id="srcseg" class="src_segment" />');
		menu = $('<menu />');
		drag = $('<drag class="text" />');
		segid = $('<span class="label label-default segid" data-toggle="tooltip" title="Segment ID"/>');
		segtctype = $('<span class="label label-info segtctype"/>');
		buttonexpand = $('<button type="button" class="btn btn-link btn-xs expand" data-toggle="tooltip" title="Expand cell" />').append($('<i class="el el-text-height" />'));
		buttonviewall = $('<button type="button" class="btn btn-link btn-xs viewall" data-toggle="tooltip" title="Show all tracks" />').append($('<i class="el el-eye-open" />'));
		buttonaccept = $('<button type="button" class="btn btn-link btn-xs accept" data-toggle="tooltip" title="Accept changes" />').append($('<i class="el el-ok-sign" />'));
		buttonreject = $('<button type="button" class="btn btn-link btn-xs reject" data-toggle="tooltip" title="Reject changes" />').append($('<i class="el el-remove-sign" />'));
		buttonignore = $('<button type="button" class="btn btn-link btn-xs ignore" data-toggle="tooltip" title="Ignore segment" />').append($('<i class="el el-ban-circle" />'));
		menu.append(segid)
			.append(segtctype)
			.append(buttonexpand)
			.append(buttonviewall)
			.append(buttonaccept)
			.append(buttonreject)
			.append(buttonignore);
		$combo_source.append(menu)
				.append(drag);
		
		//target grid template
	    $combo_target = $('<div id="trgseg" class="trg_segment" />');
		menu = $('<menu />');
		drag = $('<drag class="text trgtext" />');
		segid = $('<span class="label label-default segid"  data-toggle="tooltip" title="Segment ID"/>');
		buttonremovel = $('<button type="button" class="btn btn-link btn-xs removegrid" data-toggle="tooltip" title="Remove temporarily" />').append($('<i class="el el-trash" />'));
		buttoninsbelow = $('<button type="button" class="btn btn-link btn-xs insertbelow" data-toggle="tooltip" title="Insert Below" />').append($('<i class="el el-inbox" />'));
		buttonswapextra = $('<button type="button" class="btn btn-link btn-xs swapextra" data-toggle="tooltip" title="restore segment from trash" />').append($('<i class="el el-delicious" />'));
		buttoninsabove = $('<button type="button" class="btn btn-link btn-xs insertabove" data-toggle="tooltip" title="Insert Above" />').append($('<i class="el el-inbox el-rotate-180" />'));
		buttonswap = $('<button type="button" class="btn btn-link btn-xs swap" data-toggle="tooltip" title="swap segments" />').append($('<i class="el el-retweet el-lg" />'));
		buttonconclean = $('<button type="button" class="btn btn-link btn-xs clean" data-toggle="tooltip" title="Clear The Content" />').append($('<i class="el el-broom" />'));
		buttonedit = $('<button type="button" class="btn btn-link btn-xs edit" data-toggle="tooltip" title="Edit Text" />').append($('<i class="el el-edit" />'));
		buttonconfirm = $('<button type="button" class="btn btn-link btn-xs approve" data-toggle="tooltip" title="Not Locked" />').append($('<i class="el el-unlock" />'));
		buttonmerge = $('<button type="button" class="btn btn-link btn-xs merge" data-toggle="tooltip" title="merge" />').append($('<i class="el el-plus" />'));
		buttonsplit = $('<button type="button" class="btn btn-link btn-xs split" data-toggle="tooltip" title="split"/>').append($('<i class="el el-minus" />'));
		buttonreview = $('<button type="button" class="btn btn-link btn-xs toreview" data-toggle="tooltip" title="Need review" />').append($('<i class="el el-question-sign" />'));
		menu.append(segid)
			.append(buttonremovel)
			//.append(buttoninsabove)
			.append(buttonswapextra)
			//.append(buttoninsbelow)
			//.append(buttonsplit)
			//.append(buttonmerge)
			//.append(buttonswap)
			//.append(buttonconclean)
			//.append(buttonedit)
			.append(buttonconfirm)
			.append(buttonreview)
		$combo_target.append(menu)
				.append(drag);
		
		//removed target grid template
		$combo_rmv = $('<div id="rmvtrgseg" class="rmv_trg_segment" />');
		menu = $('<rmvmenu />');
		drag = $('<drag class="rmvtext" />');
		segid = $('<span class="label label-default segid"  data-toggle="tooltip" title="Segment ID"/>');
		buttonremovel = $('<button type="button" class="btn btn-link btn-xs permovegrid" data-toggle="tooltip" title="Discard" />').append($('<i class="el el-remove" />'));
		buttonfindps = $('<button type="button" class="btn btn-link btn-xs findps" data-toggle="tooltip" title="Find preceding/succeeding targets" />').append($('<i class="el el-indent-right" />'));
		buttonrestore = $('<button type="button" class="btn btn-link btn-xs restore" data-toggle="tooltip" title="Restore" />').append($('<i class="el el-return-key" />'));
		buttonexpand = $('<button type="button" class="btn btn-link btn-xs rmvexpand" data-toggle="tooltip" title="Expand cell" />').append($('<i class="el el-text-height" />'));
		menu.append(segid)
			.append(buttonremovel)
			.append(buttonfindps)
			.append(buttonrestore)
			.append(buttonexpand);
		$combo_rmv.append(menu)
			.append(drag);
		
		//more target grid template
		$combo_more = $('<div id="moretrgseg" class="more_trg_segment" />');
		menu = $('<moretrgmenu />');
		drag = $('<drag class="moretrgtext" />');
		segid = $('<span class="label label-default segid"  data-toggle="tooltip" title="Segment ID"/>');
		buttonrestore = $('<button type="button" class="btn btn-link btn-xs moretrgrestore" data-toggle="tooltip" title="Move" />').append($('<i class="el el-return-key" />'));
		buttonconfirm = $('<button type="button" class="btn btn-link btn-xs moretrgapprove" data-toggle="tooltip" title="Not Locked" />').append($('<i class="el el-lock" />'));
		menu.append(segid)
			.append(buttonrestore)
			.append(buttonconfirm);
		$combo_more.append(menu)
			.append(drag);
	  
	  var wd = $(window).width()/3.4;
	  var wd_missing = $(window).width()/3.3;
	  var wd_more = $(window).width()/3.5;
	  var ht = $(window).height()/9.5;
	  var mg_h = $(window).width()/146;
	  var mg_v = $(window).height()/98;

	  gridster_src = $("#sourcegrids > div").gridster({
	  	  namespace: '#sourcegrids',
      	  widget_selector: "div",
          widget_margins: [mg_h, mg_v],
          widget_base_dimensions: [wd, ht],
          autogenerate_stylesheet: false,
      	  resize:{enabled : true}
      }).data('gridster').disable();
      generateStyleSheet("#sourcegrids", ht, wd, mg_h, mg_v, maxSegPerPage);
      //gridster_src.generate_stylesheet({rows: maxSegPerPage, cols: 1});
      
      gridster_trg = $("#targetgrids > div").gridster({
      	  namespace: '#targetgrids',
      	  widget_selector: "div",
          widget_margins: [mg_h, mg_v],
          widget_base_dimensions: [wd, ht],
          autogenerate_stylesheet: false
          /*draggable: {
            handle: '.text',
            start: function(e){
            	window.onwheel = function(){ return false; }
            	var id = $(e.target).parent().attr('id');
            	draggableoldrow = $(e.target).parent().attr("data-row");
        	},
        	stop: function(e){
				window.onwheel = function(){ return true; }
				var id = $(e.target).parent().attr('id');
				var newrow = $(e.target).parent().attr("data-row");
				if(newrow != draggableoldrow) {
        			var oldindex = (currPageNum-1)*maxSegPerPage + parseInt(draggableoldrow) - 1;
        			var newindex = (currPageNum-1)*maxSegPerPage + parseInt(newrow) - 1;
        			var temptext = targets[oldindex];
        			targets[oldindex] = targets[newindex];
        			targets[newindex] = temptext;
            	}
            	window.scrollTo(0, 0);
        	}
          }
      }).data('gridster');*/
      }).data('gridster').disable();
      generateStyleSheet("#targetgrids", ht, wd, mg_h, mg_v, maxSegPerPage);
      //gridster_trg.generate_stylesheet({rows: maxSegPerPage, cols: 1});

      gridster_rmv_trg = $("#rmvtargetgrids > div > div").gridster({
      	  namespace: '#rmvtargetgrids',
      	  widget_selector: "div",
          widget_margins: [mg_h, mg_v],
          widget_base_dimensions: [wd_missing, ht],
          autogenerate_stylesheet: false,
      }).data('gridster').disable();
      generateStyleSheet("#rmvtargetgrids", ht, wd_missing, mg_h, mg_v, total_storable_removed_trgs);
      //gridster_rmv_trg.generate_stylesheet({rows: total_storable_removed_trgs, cols: 1});
      
      gridster_more_trg = $("#moretargetgrids > div > div").gridster({
      	  namespace: '#moretargetgrids',
      	  widget_selector: "div",
          widget_margins: [mg_h, mg_v],
          widget_base_dimensions: [wd_more, ht],
          autogenerate_stylesheet: false,
      }).data('gridster').disable();
      generateStyleSheet("#moretargetgrids", ht, wd_more, mg_h, mg_v, total_pre_loadable_trgs);
      //gridster_more_trg.generate_stylesheet({rows: total_pre_loadable_trgs, cols: 1});
      
      $('.panel-heading').pagination({
        items: sources.length,
        itemsOnPage: maxSegPerPage,
        ellipsePageSet: true,
        cssStyle: 'light-theme',
        selectOnClick: true,
    	onInit : function(){
    		$('.panel-heading').pagination('selectPage', 1);
    		lastPageNum = Math.ceil(sources.length/maxSegPerPage);
    		lastPageSegNum = sources.length - (lastPageNum-1) * maxSegPerPage;
    		var next_page_lt = $('#targetgrids').position().left + $('#targetgrids').width() - $('#next_page').width() - 15;
	    	$('#next_page').css('left',next_page_lt+'px');
	    	var down_page_lt = ($('#sourcegrids').position().left + $('#sourcegrids').width() + $('#targetgrids').position().left - $('#down_page').width() - 15)/2;
	    	$('#down_page').css('left',down_page_lt+'px');
	    },
    	onPageClick: function(pageNumber){
    		disableEvents();
    		
    		if(issearch){
				ini_page(pageNumber);
				enableEvents();
    		}else{
    			$("#cover").show();
			    setTimeout(function(){
			    	ini_page(pageNumber);
					enableEvents();
			    },100);
    		}
    	}
    });
		
		$('body').on('mousedown', '.expand', function(e) {
			var srcseg = $(this).closest('.src_segment');
			var datarow = srcseg.attr("data-row");
			var trgseg = $( ".trg_segment[data-row=" + datarow + "]" );
			var srctext = $(srcseg).find('.text')[0];
			var trgtext = $(trgseg).find('.text')[0];
			
			var srcsegheight = $(srcseg).outerHeight() + srctext.scrollHeight - srctext.offsetHeight ;
			var trgsegheight = $(trgseg).outerHeight() + trgtext.scrollHeight - trgtext.offsetHeight ;
			
			if(srcsegheight > $(srcseg).outerHeight()){
				$(srctext).css({
				    'overflow-y': 'scroll',
				});
				generateStyleSheetSingle("#sourcegrids", ht, wd, mg_h, mg_v, maxSegPerPage, datarow, srcsegheight);
			}
			if(trgsegheight > $(trgseg).outerHeight()){
				$(trgtext).css({
				    'overflow-y': 'scroll',
				});
				generateStyleSheetSingle("#targetgrids", ht, wd, mg_h, mg_v, maxSegPerPage, datarow, trgsegheight);
			}
		});
		
		$('body').on('mouseup', '.expand', function(e) {
			var srcseg = $(this).closest('.src_segment');
			generateStyleSheet("#sourcegrids", ht, wd, mg_h, mg_v, maxSegPerPage);
			generateStyleSheet("#targetgrids", ht, wd, mg_h, mg_v, maxSegPerPage);
			setTimeout(function(){
  				$('.text').css({
					'overflow-y': 'auto',
				});
  			}, 100);
		});
		
		$('body').on('mousedown', '.rmvexpand', function(e) {
			var rmvtrgseg = $(this).closest('.rmv_trg_segment');
			var datarow = rmvtrgseg.attr("data-row");
			var rmvtrgtext = $(rmvtrgseg).find('.rmvtext')[0];
			
			var rmvtrgsegheight = $(rmvtrgtext).outerHeight() + rmvtrgtext.scrollHeight - rmvtrgtext.offsetHeight + 22;
			
			if(rmvtrgsegheight > $(rmvtrgseg).outerHeight()){
				$(rmvtrgtext).css({
				    'overflow-y': 'scroll',
				});
				generateStyleSheetSingle("#rmvtargetgrids", ht, wd_missing, mg_h, mg_v, total_storable_removed_trgs, datarow, rmvtrgsegheight);
			}
		});
		
		$('body').on('mouseup', '.rmvexpand', function(e) {
			var rmvtrgseg = $(this).closest('.rmv_trg_segment');
			generateStyleSheet("#rmvtargetgrids", ht, wd_missing, mg_h, mg_v, total_storable_removed_trgs);
			setTimeout(function(){
  				$('.rmvtext').css({
					'overflow-y': 'auto',
				});
  			}, 100);
		});
		
		$('body').on('click', '.swap', function() {
      		var menu = $(this).parent();
      		var icon_swap = $(menu.children('.swap')[0]).children('.el')[0];
      		var trgseg = $(this).closest('.trg_segment');
      		
      		if($(this).hasClass("selectedswap")) {
      			$(this).removeClass("selectedswap");
      			$(trgseg).removeClass("selectedswapgrid");
      			$(trgseg).find(":not(button.swap)").prop("disabled", false);
      		} else {
      			var swapsegs = $('#targetgrids').find('.selectedswapgrid');
      			if(swapsegs.length == 0){
      				$(this).addClass("selectedswap");
	      			$(trgseg).addClass("selectedswapgrid");
	      			$(trgseg).find(":not(button.swap)").prop("disabled", true);
      			}else if(swapsegs.length == 1){
      				var swapseg = $(swapsegs[0]);
      				var swap_datarow = swapseg.attr("data-row");
      				var swapidx = (currPageNum-1)*maxSegPerPage + parseInt(swap_datarow) - 1;
      				
      				var trg_datarow = trgseg.attr("data-row");
      				var trgidx = (currPageNum-1)*maxSegPerPage + parseInt(trg_datarow) - 1;

      				var temp = targets[swapidx];
      				targets[swapidx] = targets[trgidx];
      				targets[trgidx] = temp;
      				
      				gridster_trg.swap_widgets(swapseg,trgseg);
      				
      				$(swapseg.find('.swap')[0]).removeClass("selectedswap");
	      			swapseg.removeClass("selectedswapgrid");
	      			swapseg.find(":not(button.swap)").prop("disabled", false);
      			}
      		}
		});
		
		$('body').on('click', '.merge', function() {
      		var menu = $(this).parent();
      		var icon_merge = $(menu.children('.merge')[0]).children('.el')[0];
      		var trgseg = $(this).closest('.trg_segment');
      		
      		if($(this).hasClass("selectedmerge")) {
      			$(this).removeClass("selectedmerge");
      			$(trgseg).removeClass("selectedmergegrid");
      			$(trgseg).find(":not(button.merge)").prop("disabled", false);
      		} else {
      			var mergesegs = $('#targetgrids').find('.selectedmergegrid');
      			if(mergesegs.length == 0){
      				$(this).addClass("selectedmerge");
	      			$(trgseg).addClass("selectedmergegrid");
	      			$(trgseg).find(":not(button.merge)").prop("disabled", true);
      			}else if(mergesegs.length == 1){
      				var mergeseg = $(mergesegs[0]);
      				var merge_datarow = parseInt(mergeseg.attr("data-row"));
      				var mergeidx = (currPageNum-1)*maxSegPerPage + merge_datarow - 1;
      				
      				var trg_datarow = parseInt(trgseg.attr("data-row"));
      				var trgidx = (currPageNum-1)*maxSegPerPage + trg_datarow - 1;
					
					var mergedtext;
					if(merge_datarow < trg_datarow){
						if(isfareast == "true"){
							mergedtext = getHtmlText(targets[mergeidx]) + getHtmlText(targets[trgidx]);
						}else{
							mergedtext = getHtmlText(targets[mergeidx]) + " " + getHtmlText(targets[trgidx]);
						}
					}else{
						if(isfareast == "true"){
							mergedtext = getHtmlText(targets[trgidx]) + getHtmlText(targets[mergeidx]);
						}else{
							mergedtext = getHtmlText(targets[trgidx]) + " " + getHtmlText(targets[mergeidx]);
						}
					}
      				
      				if(!getHtmlText(targets[trgidx])){
      					$(trgseg.find('.segid')[0]).text(getSeq(targets[mergeidx]));
      					targets[trgidx] = getSeq(targets[mergeidx]) + "#" + mergedtext;
      				}else{
      					targets[trgidx] = getSeq(targets[trgidx]) + "#" + mergedtext;
      					edited_targets.push(getSeq(targets[trgidx]));
      				}
      				
      				$(trgseg.find('.text')).html(mergedtext);
      				      				
      				$(mergeseg.find('.merge')[0]).removeClass("selectedmerge");
	      			mergeseg.removeClass("selectedmergegrid");
	      			mergeseg.find(":not(button.merge)").prop("disabled", false);
	      			
	      			$($(mergeseg).find('.removegrid')[0]).click();
      			}
      		}
      		
      		updateProgress();
		});
		
		$('body').on('click', '.ignore', function(e) {
			$(this).tooltip('destroy');
			var icon = $(this).children('.el')[0];
			var srcseg = $(this).closest('.src_segment');
			
			var index = (currPageNum-1)*maxSegPerPage + parseInt(srcseg.attr("data-row"))-1;
  			var seq = getSeq(sources[index]);

			if($(icon).hasClass("ignored")) {
				$(icon).removeClass("ignored");
				$(srcseg).removeClass("segignored");
				ignore_sources.splice($.inArray(seq, ignore_sources), 1);
			}else {
				$(icon).addClass("ignored");
				$(srcseg).addClass("segignored");
				ignore_sources.push(seq);
			}
			$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
		});
		
		$('body').on('click', '.toreview', function(e) {
			$(this).tooltip('destroy');
			var icon = $(this).children('.el')[0];
			var trgseg = $(this).closest('.trg_segment');
			
			var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
			var seq = getSeq(sources[index]);

			if($(icon).hasClass("needreview")) {
				$(icon).removeClass("needreview");
				review_targets.splice($.inArray(seq, review_targets), 1);
			}else {
				$(icon).addClass("needreview");
				review_targets.push(seq);
			}
			$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
		});
		
		$('body').on('click', '.approve', function(e) {
			$(this).tooltip('destroy');
			var icon = $(this).children('.el')[0];
			var trgseg = $(this).closest('.trg_segment');
			
			var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
  			var seq = getSeq(sources[index]);
  			
			if($(icon).hasClass("el-unlock")) {
				locked_targets.push(seq);
				$(icon).removeClass("el-unlock");
				$(icon).addClass("el-lock");
				$(this).attr('title','Locked');
				$(trgseg).addClass("lock");
				$(trgseg).addClass("static");
				$(trgseg).find(":not(button.approve):not(button.toreview)").prop("disabled", true);
				$(trgseg).find('.text').css("background-color", "#13bd9c");
				$(trgseg).find('.text').css("color", "#DBFEF8");
				lockedtargetsnum++;
			}else {
				locked_targets.splice($.inArray(seq, locked_targets), 1);
				$(icon).removeClass("el-lock");
				$(icon).addClass("el-unlock");
				$(this).attr('title','Not Locked');
				$(trgseg).removeClass("lock");
				$(trgseg).removeClass("static");
				$(trgseg).find(":not(button.approve):not(button.toreview)").prop("disabled", false);
				$(trgseg).find('.text').css("background-color", "");
				$(trgseg).find('.text').css("color", "");
				lockedtargetsnum--;
			}
			$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
			
			updateProgress();
		});
		
		$('body').on('click', '.edit', function(e) {
			gridster_trg.disable();
			currentEditedText = $(this).closest('.trg_segment').find('.text').html();
			$(this).css("color", "2a6496");
			var textarea = $(this).closest('.trg_segment').find('.text');
          	textarea.attr("contenteditable","true");
          	textarea.css("cursor", "text");
          	textarea.css("background-color", "ccffff");
          	textarea.css("color", "2a6496");
          	textarea.focus();
		});
		
		$('body').on('dblclick', '.trgtext', function(e) {
			var button = $(this).closest('.trg_segment').find('.approve')[0];
			var icon = $(button).children('.el')[0];			
			if($(icon).hasClass("el-unlock")) {
				gridster_trg.disable();
				currentEditedText = $(this).closest('.trg_segment').find('.text').html();
				$(this).closest('.trg_segment').find('.edit').css("color", "2a6496");
	          	$(this).attr("contenteditable","true");
	          	$(this).css("cursor", "text");
	          	$(this).css("background-color", "ccffff");
	          	$(this).css("color", "2a6496");
	          	$(this).focus();
			}
		});
		
		$(document).on('paste','.text',function(e) {
		    e.preventDefault();
		    var text = (e.originalEvent || e).clipboardData.getData('text/plain') || prompt('Paste something..');
		    window.document.execCommand('insertText', false, text);
		});

		$('body').on('blur', '.text', function(e) {
			disableEvents();

			gridster_trg.enable();
          	$(this).attr("contenteditable","false");
          	$(this).css("cursor", "move");
          	$(this).css("background-color", "#ffffff");
          	$(this).css("color", "444");
          	$(this).closest('.trg_segment').find('.edit').css("color", "");
          	$(this).html($(this).html().replace(/&nbsp;/g,' ').replace(/(?:\r\n|\r|\n)/g, '<br>').replace(/(<br>)+$/,''));
          	replaceInvalidTag(this);
          	trimBrs(this);
          	
          	if($(this).html() == currentEditedText){
          		enableEvents();
          		return;
          	}
          			
			var trgseg = $(this).closest('.trg_segment');
			var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
			var temptext = $(this).html().replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').trim();
			var seq = getSeq(targets[index]);
			if(getHtmlText(targets[index]) !== temptext){
				edited_targets.push(seq);
			}
			targets[index] = (seq + "#" + temptext);
			enableEvents();
		});
		
		$('body').on('mouseenter', '.split', function(e) {
			$('body').off('blur','.text');
		});
		
		$('body').on('mouseleave', '.split', function(e) {
			$('body').on('blur', '.text', function(e) {
				disableEvents();
				
				gridster_trg.enable();
	          	$(this).attr("contenteditable","false");
	          	$(this).css("cursor", "move");
	          	$(this).css("background-color", "#ffffff");
	          	$(this).css("color", "444");
	          	$(this).closest('.trg_segment').find('.edit').css("color", "");
	          	$(this).html($(this).html().replace(/&nbsp;/g,' ').replace(/(?:\r\n|\r|\n)/g, '<br>').replace(/(<br>)+$/,''));
	          	replaceInvalidTag(this);
	          	trimBrs(this);
	          	
	          	if($(this).html() == currentEditedText){
	          		enableEvents();
	          		return;
	          	}
	          			
				var trgseg = $(this).closest('.trg_segment');
				var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
				var temptext = $(this).html().replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').trim();
				var seq = getSeq(targets[index]);
				if(getHtmlText(targets[index]) !== temptext){
					edited_targets.push(seq);
				}
				targets[index] = (seq + "#" + temptext);
				enableEvents();
				
				updateProgress();
			});
		});
		
		$("body").on("keyup", function(e){
			disableEvents();
			
			var keycode = (event.keyCode ? event.keyCode : event.which);
		    if(keycode == '13'){
		    	var op = $(".search-modal-dialog").css('opacity');
				if (op == "0.9") 
				{
				    if($('#searchtab').hasClass("active")){
		    			$('.searchtext').click();
			    	}else if($('#searchspecialtab').hasClass("active")){
			    		$('.searchspecialtext').click();
			    	}
				}
		    }

	    	enableEvents();
		});
		
		$("body").on("keypress",".text", function(){
			disableEvents();
			
    		var keycode = (event.keyCode ? event.keyCode : event.which);
    		if(keycode == '13'){
    			if(event.shiftKey){
    				event.preventDefault();
    				if((getCaretCharacterOffsetWithin(this) == $(this).text().length) && (!$(this).html().match(/<br>$/))){
    					insertHtmlAtCaret("<br> <br> ");
    				}else{
    					insertHtmlAtCaret("<br> ");
    				}
    			}else{
    				event.preventDefault();
        			event.target.blur();
    			}
    			event.stopPropagation();
    		}
    		
    		enableEvents();
		});
		
		$(window).keydown(function(e){
		    if(e.keyCode === 114 || (e.ctrlKey && e.keyCode === 70)){
		    	event.preventDefault();
    			$('.search').click();
    		}else if(e.keyCode === 127 || (e.ctrlKey && e.keyCode === 83)){
    			event.preventDefault();
    			$('.searchspecial').click();
    		}else if(e.keyCode === 116){
    			event.preventDefault();
    			if(!$('#searchspecialtab').hasClass("active")){
					$('.nav-tabs a[href="#special"]').tab('show');
		    	}
		    	$('.searchspecialtext').click();
    		}
		});

		$("body").on("keydown",".text", function(){
			disableEvents();
			
    		var evtobj = window.event? event : e;
    		if((evtobj.keyCode == 189 || evtobj.keyCode == 109) && evtobj.ctrlKey && evtobj.shiftKey){
    			event.preventDefault();
    			
    			insertHtmlAtCaret("<caret>");
    			var innerhtml = $(this).html();
    			var pos = innerhtml.indexOf("<caret>");
    			var before = innerhtml.substring(0,pos);
    			var after = innerhtml.substring(pos+15,innerhtml.length);
    			$("caret").remove();
    			
    			var trgseg = $(this).closest('.trg_segment');
    			var nxtindex = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"));
    			var orgtext = getHtmlText(targets[nxtindex-1]);
    			var orgseq = getSeq(targets[nxtindex-1]);
    			var orgdatarow = parseInt(trgseg.attr("data-row"));
    			
    			var button_insertabove = $(trgseg).find('.insertabove')[0];
    			var tempidx = empty_next_index;
    			$(button_insertabove).click();
    			
    			var insertedtrgseg = $( ".trg_segment[data-row=" + orgdatarow + "]" );
    			$($(insertedtrgseg).find('.text')[0]).html(before.trim());
    			
    			targets[nxtindex-1] = "n - " + tempidx + "#" + before.trim();
    			while(nxtindex < (sources.length-1) && $.inArray(getSeq(sources[nxtindex]), locked_targets) !== -1){
    				nxtindex++;
    			}
    			if(nxtindex < currPageNum*maxSegPerPage && nxtindex < targets.length){
    				$(this).html(after.trim());
    			}
    			
    			var more = $('.nav').find('.fa-cubes')[0];
    			var currentpageseg = $(".trg_segment").length;
    			if(nxtindex >= ((currPageNum-1)*maxSegPerPage+currentpageseg) && nxtindex < (currPageNum*maxSegPerPage+currentpageseg) && $(more).hasClass("lightup")){
    				var more_datarow = nxtindex - (currPageNum-1)*maxSegPerPage - currentpageseg + 1;
    				var insertedmoretrgseg = $( ".more_trg_segment[data-row=" + more_datarow + "]" );
    				$($(insertedmoretrgseg).find('.moretrgtext')[0]).html(after.trim());
    			}
    			targets[nxtindex] = orgseq + "#" + after.trim();
    			
    			if(after.trim() !== orgtext){
					edited_targets.push(orgseq);
				}
				
    			event.stopPropagation();
    		}
    		
    		enableEvents();
		});
		
		$("body").on("mousedown",".split", function(e){
			e.preventDefault();
			$(this).tooltip('destroy');
			
			var trgseg = $(this).closest('.trg_segment');
			var texteditor = trgseg.find('.text')[0];

			if((!$(texteditor).attr("contenteditable")) || $(texteditor).attr("contenteditable") == "false"){
				return;
			}
			
    		insertHtmlAtCaret("<caret>");
    		var innerhtml = $(texteditor).html();
    		var pos = innerhtml.indexOf("<caret>");
    		var before = innerhtml.substring(0,pos);
    		var after = innerhtml.substring(pos+15,innerhtml.length);
    		$("caret").remove();
    			
    		var nxtindex = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"));
    		var orgtext = getHtmlText(targets[nxtindex-1]);
    		var orgseq = getSeq(targets[nxtindex-1]);
    		var orgdatarow = parseInt(trgseg.attr("data-row"));
    			
    		var button_insertabove = $(trgseg).find('.insertabove')[0];
    		var tempidx = empty_next_index;
    		$(button_insertabove).click();
    			
    		var insertedtrgseg = $( ".trg_segment[data-row=" + orgdatarow + "]" );
    		$($(insertedtrgseg).find('.text')[0]).html(before.trim());
    		$($(insertedtrgseg).find('.segid')[0]).html(orgseq);
    			
    		targets[nxtindex-1] = orgseq + "#" + before.trim();
    		while(nxtindex < (sources.length-1) && $.inArray(getSeq(sources[nxtindex]), locked_targets) !== -1){
    			nxtindex++;
    		}
    		if(nxtindex < currPageNum*maxSegPerPage && nxtindex < targets.length){
    			$(texteditor).html(after.trim());
    			$($(trgseg).find('.segid')[0]).html("n - " + tempidx);
    		}
    			
    		var more = $('.nav').find('.fa-cubes')[0];
    		var currentpageseg = $(".trg_segment").length;
    		if(nxtindex >= ((currPageNum-1)*maxSegPerPage+currentpageseg) && nxtindex < (currPageNum*maxSegPerPage+currentpageseg) && $(more).hasClass("lightup")){
    			var more_datarow = nxtindex - (currPageNum-1)*maxSegPerPage - currentpageseg + 1;
    			var insertedmoretrgseg = $( ".more_trg_segment[data-row=" + more_datarow + "]" );
    			$($(insertedmoretrgseg).find('.moretrgtext')[0]).html(after.trim());
    			$($(insertedmoretrgseg).find('.segid')[0]).html("n - " + tempidx);
    		}
    		targets[nxtindex] = "n - " + tempidx + "#" + after.trim();
    			
    		if(after.trim() !== orgtext){
				edited_targets.push(orgseq);
			}
			
			$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
			
			updateProgress();
		});

		$('body').on('click', '.clean', function(e) {
			disableEvents();
			
			var trgseg = $(this).closest('.trg_segment');
			var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
  			var text = getHtmlText(targets[index]);
  			var seq = getSeq(targets[index]);
  			if(text) {
				var trash = $('.nav').find('.fa-braille')[0];
				if($(trash).hasClass("lightup")) {
					if(removed_targets.length >= total_storable_removed_trgs) {
  						$('.missing').animate({scrollTop: 0 }, 100);
  						setTimeout(function(){
  						 	$('.num_trg_removed').css("color","#d00000");
  					    	$('.num_trg_removed').popover({container: 'body', trigger: 'manual', delay: {show: 0, hide: 0}});
  					    	$('.num_trg_removed').popover('show');
  							setTimeout(function(){$('.num_trg_removed').popover('hide');}, 3000);
  						}, 300);
  					} else {
  						var tempseq = "n - " + empty_next_index;
		  				var temptext = "n - " + empty_next_index + "#";
		  				empty_next_index++;
  						trgseg.find('.segid').text(tempseq);
						trgseg.find('drag').html('');
  						removed_targets.unshift(targets[index]);
  						targets[index] = temptext;

  						$('.missing').animate({scrollTop: 0 }, 100);
  						setTimeout(function(){
  							var comboclone = $combo_rmv.clone(true);
    						gridster_rmv_trg.add_widget(comboclone,1,1,1,1);
							comboclone.find('.segid').text(seq);
							comboclone.find('drag').html(text);

  							$('.num_trg_removed').text(removed_targets.length);
  						}, 300);
  					}
  				} else {
  					if(removed_targets.length >= total_storable_removed_trgs) {
  						trash.click();
  						$('.num_trg_removed').css("color","#d00000");
  					} else {
  						var tempseq = "n - " + empty_next_index;
		  				var temptext = "n - " + empty_next_index + "#";
		  				empty_next_index++;
  						trgseg.find('.segid').text(tempseq);
						trgseg.find('drag').html('');
  						removed_targets.unshift(targets[index]);
  						targets[index] = temptext;
  					}
  				}
  			}
  			
  			enableEvents();
  			
  			updateProgress();
		});
		
        $('body').on('click', '.removegrid', function(e) {
        	disableEvents();
        	var removeicon = $(this);
        	removeicon.prop('disabled', true);
        	
        	var orgtrgcnt = targets.length;
        	var trgseg = $(this).closest('.trg_segment');
        	var thisrow = parseInt(trgseg.attr("data-row"));
        	//remove grids in next page
        	var locked_more = [];
        	var moretrgcnt = $(".more_trg_segment").length;
        	$( ".more_trg_segment" ).each(function() {
  				var rownum_more = parseInt($(this).attr("data-row"));
  				if(currPageNum == lastPageNum){
  					var index_more = sources.length + rownum_more - 1;
  				}else{
  					var index_more = currPageNum*maxSegPerPage + rownum_more - 1;
  				}
  				if(index_more < sources.length){
  					var seq_more = getSeq(sources[index_more]);
	  				if($.inArray(seq_more, locked_targets) !== -1){
	  					locked_more.push(rownum_more);
	  				}
  				}
			});
			locked_more.sort(function(a, b){return a-b});
        	//remove grids in current page
        	var locked = [];
        	var locked_indices = [];
        	var locked_contents = [];
        	$( ".trg_segment" ).each(function() {
  				var icon = $($(this).find('.approve')[0]).children('i')[0];
  				if($(icon).hasClass("el-lock")) {
  					var rownum = parseInt($(this).attr("data-row"));
  					if(rownum > thisrow){
  						locked.push(rownum);
  					}
  				}
			});
			locked.sort(function(a, b){return a-b});
			//remove targets not on this page
			var start_index
			if(locked.length == 0){
				start_index = currPageNum*maxSegPerPage - 1;
			}else{
				start_index = (currPageNum-1)*maxSegPerPage + locked[0] - 1;
			}
			for(i = (sources.length-1); i >= start_index; i--){
				var tmep_seq = getSeq(sources[i]);
				if($.inArray(tmep_seq, locked_targets) !== -1){
					locked_indices.unshift(i);
					locked_contents.unshift(targets[i]);
					targets.splice(i,1);
				}
			}
			for(i=(locked_more.length-1);i>=0;i--){
				var rownum_more = locked_more[i];
				var locked_trgseg_more = $( ".more_trg_segment[data-row=" + rownum_more + "]" );
				gridster_more_trg.remove_widget(locked_trgseg_more);
				//var toremovetemp_index = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//locked_contents.unshift(targets[toremovetemp_index]);
				//targets.splice(toremovetemp_index,1);
			}
			for(i=(locked.length-1);i>=0;i--){
				var rownum = locked[i];
				var locked_trgseg = $( ".trg_segment[data-row=" + rownum + "]" );
				gridster_trg.remove_widget(locked_trgseg);
				//var toremovetemp_index = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//locked_contents.unshift(targets[toremovetemp_index]);
				//targets.splice(toremovetemp_index,1);
			}
  			var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
  			var text = getHtmlText(targets[index]);
  			var seq = getSeq(targets[index]);
  			var nextsegtext;
  			var nextsegseq;
  			if(currPageNum == lastPageNum){
  				if(orgtrgcnt > sources.length){
  					nextsegtext = getHtmlText(targets[sources.length-locked.length]);
  					nextsegseq = getSeq(targets[sources.length-locked.length]);
  				}else{
  					nextsegtext = "";
  					nextsegseq = "n - " + empty_next_index;
  				}
  			}else{
  				var nextsegidx = currPageNum*maxSegPerPage-locked.length;
  				if(nextsegidx < targets.length){
  					nextsegtext = getHtmlText(targets[currPageNum*maxSegPerPage-locked.length]);
  					nextsegseq = getSeq(targets[currPageNum*maxSegPerPage-locked.length]);
  				}else{
  					nextsegtext = "";
  					nextsegseq = "n - " + empty_next_index;
  				}
  			}
  			var trash = $('.nav').find('.fa-braille')[0];
  			var more = $('.nav').find('.fa-cubes')[0];
  			if($(more).hasClass("lightup")) {
  				if(text) {
  					if(removed_targets.length < total_storable_removed_trgs) {
  						$(this).tooltip('destroy');
  						removed_targets.unshift(targets[index]);
  						targets.splice(index,1);
						gridster_trg.remove_widget(trgseg);
						
						//keeplocks(removeicon, locked, locked_contents, locked_indices);
						//setTimeout(function(){
							var firstmoretrgseg = $(".more_trg_segment[data-row='1']");
							if(firstmoretrgseg.length){
								gridster_more_trg.remove_widget(firstmoretrgseg);
							}
							var comboclone = $combo_target.clone(true);
							comboclone.find('.segid').text(nextsegseq);
							comboclone.find('drag').html(nextsegtext);
    						if(currPageNum == lastPageNum){
    							gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    							var moretrgidx = (currPageNum-1)*maxSegPerPage + lastPageSegNum + moretrgcnt - 1 - locked.length - locked_more.length;
    						}else{
    							gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    							var moretrgidx = currPageNum*maxSegPerPage + moretrgcnt - 1 - locked.length - locked_more.length;
    						}
							
							//determine if add more trg segment
    						if(currPageNum == lastPageNum) {
    							if(moretrgidx < targets.length){
    								var comboclone = $combo_more.clone(true);
		    						gridster_more_trg.add_widget(comboclone,1,1,1,moretrgcnt-locked_more.length);
									comboclone.find('.segid').text(getSeq(targets[moretrgidx]));
									comboclone.find('drag').html(getHtmlText(targets[moretrgidx]));
									comboclone.find('.moretrgapprove').hide();
    							}
    							if(orgtrgcnt <= sources.length){
			    					targets.push("n - " + empty_next_index + "#" + "");
			    					empty_next_index++;
			    				}
    						}else if((currPageNum+1) == lastPageNum){
    							if(orgtrgcnt <= sources.length){
    								var comboclone = $combo_more.clone(true);
		    						gridster_more_trg.add_widget(comboclone,1,1,1,moretrgcnt-locked_more.length);
									comboclone.find('.segid').text("n - " + empty_next_index);
									comboclone.find('drag').html("");
									comboclone.find('.moretrgapprove').hide();
									targets.push("n - " + empty_next_index + "#" + "");
		    						empty_next_index++;
    							}else{
    								if(moretrgidx < targets.length){
	    								var comboclone = $combo_more.clone(true);
			    						gridster_more_trg.add_widget(comboclone,1,1,1,moretrgcnt-locked_more.length);
										comboclone.find('.segid').text(getSeq(targets[moretrgidx]));
										comboclone.find('drag').html(getHtmlText(targets[moretrgidx]));
										comboclone.find('.moretrgapprove').hide();
	    							}
	    							if(orgtrgcnt <= sources.length){
				    					targets.push("n - " + empty_next_index + "#" + "");
				    					empty_next_index++;
				    				}
    							}
    						}else{
    							var comboclone = $combo_more.clone(true);
	    						gridster_more_trg.add_widget(comboclone,1,1,1,moretrgcnt-locked_more.length);
								comboclone.find('.segid').text(getSeq(targets[moretrgidx]));
								comboclone.find('drag').html(getHtmlText(targets[moretrgidx]));
								comboclone.find('.moretrgapprove').hide();
    							if(orgtrgcnt <= sources.length){
		    						targets.push("n - " + empty_next_index + "#" + "");
		    						empty_next_index++;
		    					}
    						}
    						
    						keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
						//},300);					
  					} else {
  						trash.click();
  						$('.num_trg_removed').css("color","#d00000");
  						keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  					}
  				}else{
  					$(this).tooltip('destroy');
  					targets.splice(index,1);
					gridster_trg.remove_widget(trgseg);
					
					//keeplocks(removeicon, locked, locked_contents, locked_indices);
					
					if(orgtrgcnt <= sources.length){
    					targets.push("n - " + empty_next_index + "#" + "");
    					empty_next_index++;
    				}
					//setTimeout(function(){
						var firstmoretrgseg = $(".more_trg_segment[data-row='1']");
						if(firstmoretrgseg.length){
							gridster_more_trg.remove_widget(firstmoretrgseg);
						}
						var comboclone = $combo_target.clone(true);
    					if(currPageNum == lastPageNum){
    						gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    						var moretrgidx = (currPageNum-1)*maxSegPerPage + lastPageSegNum + moretrgcnt - 1 - locked.length - locked_more.length;
    					}else{
    						gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    						var moretrgidx = currPageNum*maxSegPerPage + moretrgcnt - 1 - locked.length - locked_more.length;
    					}
						comboclone.find('.segid').text(nextsegseq);
						comboclone.find('drag').html(nextsegtext);
						
    					if(moretrgidx < targets.length && currPageNum != lastPageNum) {
    						var comboclone = $combo_more.clone(true);
    						gridster_more_trg.add_widget(comboclone,1,1,1,moretrgcnt-locked_more.length);
							comboclone.find('.segid').text(getSeq(targets[moretrgidx]));
							comboclone.find('drag').html(getHtmlText(targets[moretrgidx]));
							comboclone.find('.moretrgapprove').hide();
    					}
    						
    					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
					//},300);	
  				}
  			} else if($(trash).hasClass("lightup")) {
  				if(text) {
  					if(removed_targets.length >= total_storable_removed_trgs) {
  						$('.missing').animate({scrollTop: 0 }, 100);
  						setTimeout(function(){
  						 	$('.num_trg_removed').css("color","#d00000");
  					    	$('.num_trg_removed').popover({container: 'body', trigger: 'manual', delay: {show: 0, hide: 0}});
  					    	$('.num_trg_removed').popover('show');
  							setTimeout(function(){$('.num_trg_removed').popover('hide');}, 3000);
  						}, 300);
  						
  						keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  					} else {
  						$(this).tooltip('destroy');
  						removed_targets.unshift(targets[index]);
  						targets.splice(index,1);
  						gridster_trg.remove_widget(trgseg);
  						
  						//keeplocks(removeicon, locked, locked_contents, locked_indices);
  						
  						if(orgtrgcnt <= sources.length){
	    					targets.push("n - " + empty_next_index + "#" + "");
	    					empty_next_index++;
	    				}

  						$('.missing').animate({scrollTop: 0 }, 100);
  						//setTimeout(function(){
  							var comboclone = $combo_rmv.clone(true);
  							comboclone.find('.segid').text(seq);
							comboclone.find('drag').html(text);
    						gridster_rmv_trg.add_widget(comboclone,1,1,1,1);

  							$('.num_trg_removed').text(removed_targets.length);
  							$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
  							
  							var comboclone = $combo_target.clone(true);
    						if(currPageNum == lastPageNum){
    							gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    						}else{
    							gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    						}
							comboclone.find('.segid').text(nextsegseq);
							comboclone.find('drag').html(nextsegtext);
							
							keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  						//}, 300);
  					}
  				} else {
  					$(this).tooltip('destroy');
  					targets.splice(index,1);
  					gridster_trg.remove_widget(trgseg);
  					
  					//keeplocks(removeicon, locked, locked_contents, locked_indices);
  					
  					if(orgtrgcnt <= sources.length){
    					targets.push("n - " + empty_next_index + "#" + "");
    					empty_next_index++;
    				}
					
					//setTimeout(function(){
  						var comboclone = $combo_target.clone(true);
    					if(currPageNum == lastPageNum){
    						gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    					}else{
    						gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    					}
						comboclone.find('.segid').text(nextsegseq);
						comboclone.find('drag').html(nextsegtext);
						
						keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  					//}, 300);
  				}
  			} else {
  				if(text) {
  					if(removed_targets.length >= total_storable_removed_trgs) {
  						if(!($(trash).hasClass("lightup"))) {
  							trash.click();
  							$('.num_trg_removed').css("color","#d00000");
  						} else {
  							$('.num_trg_removed').css("color","#d00000");
  					    	$('.num_trg_removed').popover({container: 'body', trigger: 'manual', delay: {show: 0, hide: 0}});
  					    	$('.num_trg_removed').popover('show');
  							setTimeout(function(){$('.num_trg_removed').popover('hide');}, 3000);
  						}
  						
  						keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  					} else {
  						$(this).tooltip('destroy');
  						removed_targets.unshift(targets[index]);
  						targets.splice(index,1);
  						gridster_trg.remove_widget(trgseg);
  						
  						//keeplocks(removeicon, locked, locked_contents, locked_indices);
  						
  						if(orgtrgcnt <= sources.length){
	    					targets.push("n - " + empty_next_index + "#" + "");
	    					empty_next_index++;
	    				}
						
						var comboclone = $combo_target.clone(true);
    					if(currPageNum == lastPageNum){
    						gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    					}else{
    						gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    					}
						comboclone.find('.segid').text(nextsegseq);
						comboclone.find('drag').html(nextsegtext);
						
						keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  					}
  				} else {
  					$(this).tooltip('destroy');
  					targets.splice(index,1);
  					gridster_trg.remove_widget(trgseg);
  					
  					//keeplocks(removeicon, locked, locked_contents, locked_indices);
  					
  					if(orgtrgcnt <= sources.length){
    					targets.push("n - " + empty_next_index + "#" + "");
    					empty_next_index++;
    				}
					
					var comboclone = $combo_target.clone(true);
    				if(currPageNum == lastPageNum){
    					gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    				}else{
    					gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    				}
					comboclone.find('.segid').text(nextsegseq);
					comboclone.find('drag').html(nextsegtext);
					
					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  				}
  			}
  			
  			enableEvents();
  			
  			updateProgress();
		});

		$('body').on('click', '.permovegrid', function() {
  			var rmvtrgseg = $(this).closest('.rmv_trg_segment');
  			var index = parseInt(rmvtrgseg.attr("data-row"))-1;
			removed_targets.splice(index,1);
			$(this).tooltip('destroy');
  			gridster_rmv_trg.remove_widget(rmvtrgseg);
  			$('.num_trg_removed').text(removed_targets.length);
  			if(parseInt($('.num_trg_removed').text()) >= parseInt($('.num_trg_storable').text())) {
  				$('.num_trg_removed').css("color","#d00000");
  			} else {
  				$('.num_trg_removed').css("color","#fff");
  			}
  			
  			updateProgress();
		});
		
		$('.nav').find('.el').on('mouseenter', function(event) {
			event.target.style.color = "#fff";
		});
		
		$('.nav').find('.el').on('mouseleave', function(event) {
			var target = $(this);
			if (target.hasClass("lightup")) {
				event.target.style.color = "#fff";
			}else {
				event.target.style.color = "#777";
			}
		});

		$('body').on('click', '.insertbelow', function() {
			disableEvents();
			var removeicon = $(this);
        	removeicon.prop('disabled', true);
			
			$(this).tooltip('destroy');
			var orgtrgcnt = targets.length;
			var trgseg = $(this).closest('.trg_segment');
        	var thisrow = parseInt(trgseg.attr("data-row"));
        	
        	//remove grids in next page
        	var locked_more = [];
        	$( ".more_trg_segment" ).each(function() {
  				var rownum_more = parseInt($(this).attr("data-row"));
  				if(currPageNum == lastPageNum){
  					var index_more = sources.length + rownum_more - 1;
  				}else{
  					var index_more = currPageNum*maxSegPerPage + rownum_more - 1;
  				}
  				if(index_more < sources.length){
  					var seq_more = getSeq(sources[index_more]);
	  				if($.inArray(seq_more, locked_targets) !== -1){
	  					locked_more.push(rownum_more);
	  				}
  				}
			});
			locked_more.sort(function(a, b){return a-b});
			
			//remove grids in current page
        	var locked = [];
        	var locked_indices = [];
        	var locked_contents = [];
        	$( ".trg_segment" ).each(function() {
  				var icon = $($(this).find('.approve')[0]).children('i')[0];
  				if($(icon).hasClass("el-lock")) {
  					var rownum = parseInt($(this).attr("data-row"));
  					if(rownum > thisrow){
  						locked.push(rownum);
  					}
  				}
			});
			locked.sort(function(a, b){return a-b});
			
			//remove targets not on this page
			var start_index
			if(locked.length == 0){
				start_index = currPageNum*maxSegPerPage - 1;
			}else{
				start_index = (currPageNum-1)*maxSegPerPage + locked[0] - 1;
			}
			
			for(i = (sources.length-1); i >= start_index; i--){
				var tmep_seq = getSeq(sources[i]);
				if($.inArray(tmep_seq, locked_targets) !== -1){
					locked_indices.unshift(i);
					locked_contents.unshift(targets[i]);
					targets.splice(i,1);
				}
			}
			
			//remove grids next page
			for(i=(locked_more.length-1);i>=0;i--){
				var rownum_more = locked_more[i];
				var locked_trgseg_more = $( ".more_trg_segment[data-row=" + rownum_more + "]" );
				gridster_more_trg.remove_widget(locked_trgseg_more);
				//var toremovetemp_index = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//locked_contents.unshift(targets[toremovetemp_index]);
				//targets.splice(toremovetemp_index,1);
			}
			//remove grids current page
			for(i=(locked.length-1);i>=0;i--){
				var rownum = locked[i];
				var locked_trgseg = $( ".trg_segment[data-row=" + rownum + "]" );
				gridster_trg.remove_widget(locked_trgseg);
				//var toremovetemp_index = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//locked_contents.unshift(targets[toremovetemp_index]);
				//targets.splice(toremovetemp_index,1);
			}

			var more = $('.nav').find('.fa-cubes')[0];
  			var index_ins = parseInt(trgseg.attr("data-row"));
  			var rmvtrgsegs = $('.missing').find('.selectedrestoregrid');
  			var moretrgsegs = $('.more').find('.selectedrestoregrid');
  			if((rmvtrgsegs.length == 0) && (moretrgsegs.length == 0)) {
  				var tempseq = "n - " + empty_next_index;
  				var text="n - " + empty_next_index + "#";
  				empty_next_index++;
  				targets.splice((currPageNum-1)*maxSegPerPage + index_ins,0,text);
  				//setTimeout(function(){
  					var comboclone = $combo_target.clone(true);
    				gridster_trg.add_widget(comboclone,1,1,1,index_ins+1);
					comboclone.find('.segid').text(getSeq(text));
					comboclone.find('drag').html(getHtmlText(text));
					
					

  					//setTimeout(function(){
  						var totalcurrpagetrgseg = $('.stborder').find('.trg_segment');
  						var lasttrg = $(".trg_segment[data-row='"+ totalcurrpagetrgseg.length +"']");
  						var lasttrgidx = totalcurrpagetrgseg.length + (currPageNum-1)*maxSegPerPage - 1;
  							
  						gridster_trg.remove_widget(lasttrg);
  						var lasttrgtext = getHtmlText(targets[lasttrgidx]);

  						if((lasttrgtext) || (currPageNum != lastPageNum)){
  							if($(more).hasClass("lightup")){
  								//setTimeout(function(){
    								var comboclone = $combo_more.clone(true);
    								gridster_more_trg.add_widget(comboclone,1,1,1,1);
									comboclone.find('.segid').text(getSeq(targets[lasttrgidx]));
									comboclone.find('drag').html(getHtmlText(targets[lasttrgidx]));
									comboclone.find('.moretrgapprove').hide();
    								
    								var totalcurrpagemoretrgsegNum = $(".more_trg_segment").length;
    								var lastmoretrgtext = getHtmlText(targets[lasttrgidx + totalcurrpagemoretrgsegNum - 1]);

    								if((totalcurrpagemoretrgsegNum + locked_more.length) > total_pre_loadable_trgs || (!lastmoretrgtext)){
    									var lastmoretrg = $(".more_trg_segment[data-row='"+ (totalcurrpagemoretrgsegNum) +"']");
    									gridster_more_trg.remove_widget(lastmoretrg);
    								}
    								
    								var lasttotaltrgtext = getHtmlText(targets[targets.length-1]);
	  								if(orgtrgcnt >= sources.length && (!lasttotaltrgtext)){
	  									targets.splice(targets.length-1,1);
	  								}
  							}else{
  								var lasttotaltrgtext = getHtmlText(targets[targets.length-1]);
  								if(orgtrgcnt >= sources.length && (!lasttotaltrgtext)){
  									targets.splice(targets.length-1,1);
  								}
  							}
  							
  							if(!lasttrgtext && (currPageNum == lastPageNum)){
  								targets.splice(lasttrgidx,1);
  							}
  						}else{
  							targets.splice(lasttrgidx,1);
  						}
  					//},300);
  					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  				//},300);
  			} else if((rmvtrgsegs.length != 0) && (moretrgsegs.length == 0)) {
  				var rmvtrgseg = rmvtrgsegs[0];
  				var index_rmv = parseInt($(rmvtrgseg).attr("data-row"))-1;
  				var content = removed_targets[index_rmv];
  				var text = getHtmlText(content);
  				var seq = getSeq(content);
  			
  				gridster_rmv_trg.remove_widget(rmvtrgseg);
  				removed_targets.splice(index_rmv,1);
  			
  				$('.num_trg_removed').text(removed_targets.length);
  				if($('.num_trg_removed').text() >= $('.num_trg_storable').text()) {
  					$('.num_trg_removed').css("color","#d00000");
  				} else {
  					$('.num_trg_removed').css("color","#fff");
  				}

  				//setTimeout(function(){
					targets.splice((currPageNum-1)*maxSegPerPage + index_ins,0,content);

  					var comboclone = $combo_target.clone(true);
    				gridster_trg.add_widget(comboclone,1,1,1,index_ins+1);
					comboclone.find('.segid').text(seq);
					comboclone.find('drag').html(text);
  					
  					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  						
  					//setTimeout(function(){
  						var totalcurrpagetrgseg = $('.stborder').find('.trg_segment');
  						var lasttrg = $(".trg_segment[data-row='"+ totalcurrpagetrgseg.length +"']");
  						var lasttrgidx = totalcurrpagetrgseg.length + (currPageNum-1)*maxSegPerPage - 1;
  						gridster_trg.remove_widget(lasttrg);
  						
  						var lasttrgtext = getHtmlText(targets[lasttrgidx]);
  						if((!lasttrgtext) && (currPageNum == lastPageNum)){
  							targets.splice(lasttrgidx,1);
  						}
  					//},300);
  				//},300);
  			} else if((rmvtrgsegs.length == 0) && (moretrgsegs.length != 0)) {
  				var moretrgseg = moretrgsegs[0];
  				var index_more;
  				if(currPageNum == lastPageNum){
  					index_more = parseInt($(moretrgseg).attr("data-row"))-1 + sources.length - locked.length;
  				}else{
  					index_more = parseInt($(moretrgseg).attr("data-row"))-1 + currPageNum*maxSegPerPage - locked.length;
  				}
  				var content = targets[index_more];
  				var text = getHtmlText(content);
  				var seq = getSeq(content);
  			
  				gridster_more_trg.remove_widget(moretrgseg);
  			
  				//setTimeout(function(){
					targets.splice(index_more,1);
					targets.splice((currPageNum-1)*maxSegPerPage + index_ins,0,content);
					
  					var comboclone = $combo_target.clone(true);
    				gridster_trg.add_widget(comboclone,1,1,1,index_ins+1);
					comboclone.find('.segid').text(seq);
					comboclone.find('drag').html(text);

  					//setTimeout(function(){
  						var totalcurrpagetrgseg = $('.stborder').find('.trg_segment');
  						var lasttrg = $(".trg_segment[data-row='"+ totalcurrpagetrgseg.length +"']");
  						var lasttrgidx = totalcurrpagetrgseg.length + (currPageNum-1)*maxSegPerPage - 1;
  						gridster_trg.remove_widget(lasttrg);
  						
  						var lasttrgtext = getHtmlText(targets[lasttrgidx]);
  						if((lasttrgtext) || (currPageNum != lastPageNum)){
  							//setTimeout(function(){
    							var comboclone = $combo_more.clone(true);
    							gridster_more_trg.add_widget(comboclone,1,1,1,1);
								comboclone.find('.segid').text(getSeq(targets[lasttrgidx]));
								comboclone.find('drag').html(getHtmlText(targets[lasttrgidx]));
								comboclone.find('.moretrgapprove').hide();
    							    							
    							var totalcurrpagemoretrgsegNum = $(".more_trg_segment").length;
    							if((totalcurrpagemoretrgsegNum + locked_more.length) > total_pre_loadable_trgs){
    								setTimeout(function(){
    									var lastmoretrg = $(".more_trg_segment[data-row='"+ (total_pre_loadable_trgs+1) +"']");
    									gridster_more_trg.remove_widget(lastmoretrg);
    								},300);
    							}
  							//},700);
  						}else{
  							targets.splice(lasttrgidx,1);
  						}
  						
  					//},500);
  					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  				//},300);
  			}
  			
  			enableEvents();
  			
  			updateProgress();
		});
		
		$('body').on('click', '.insertabove', function() {
			disableEvents();
			var removeicon = $(this);
        	removeicon.prop('disabled', true);
			
			$(this).tooltip('destroy');
			var orgtrgcnt = targets.length;
			var trgseg = $(this).closest('.trg_segment');
			var thisrow = parseInt(trgseg.attr("data-row"));
			
			//remove grids in next page
        	var locked_more = [];
        	$( ".more_trg_segment" ).each(function() {
  				var rownum_more = parseInt($(this).attr("data-row"));
  				if(currPageNum == lastPageNum){
  					var index_more = sources.length + rownum_more - 1;
  				}else{
  					var index_more = currPageNum*maxSegPerPage + rownum_more - 1;
  				}
  				if(index_more < sources.length){
  					var seq_more = getSeq(sources[index_more]);
	  				if($.inArray(seq_more, locked_targets) !== -1){
	  					locked_more.push(rownum_more);
	  				}
  				}
			});
			locked_more.sort(function(a, b){return a-b});
			
			//remove grids in current page
        	var locked = [];
        	var locked_indices = [];
        	var locked_contents = [];
        	$( ".trg_segment" ).each(function() {
  				var icon = $($(this).find('.approve')[0]).children('i')[0];
  				if($(icon).hasClass("el-lock")) {
  					var rownum = parseInt($(this).attr("data-row"));
  					if(rownum > thisrow){
  						locked.push(rownum);
  					}
  				}
			});
			locked.sort(function(a, b){return a-b});
			
			//remove targets not on this page
			var start_index
			if(locked.length == 0){
				start_index = currPageNum*maxSegPerPage - 1;
			}else{
				start_index = (currPageNum-1)*maxSegPerPage + locked[0] - 1;
			}
			
			for(i = (sources.length-1); i >= start_index; i--){
				var tmep_seq = getSeq(sources[i]);
				if($.inArray(tmep_seq, locked_targets) !== -1){
					locked_indices.unshift(i);
					locked_contents.unshift(targets[i]);
					targets.splice(i,1);
				}
			}
			
			//remove grids next page
			for(i=(locked_more.length-1);i>=0;i--){
				var rownum_more = locked_more[i];
				var locked_trgseg_more = $( ".more_trg_segment[data-row=" + rownum_more + "]" );
				gridster_more_trg.remove_widget(locked_trgseg_more);
				//var toremovetemp_index = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//locked_contents.unshift(targets[toremovetemp_index]);
				//targets.splice(toremovetemp_index,1);
			}
			//remove grids current page
			for(i=(locked.length-1);i>=0;i--){
				var rownum = locked[i];
				var locked_trgseg = $( ".trg_segment[data-row=" + rownum + "]" );
				gridster_trg.remove_widget(locked_trgseg);
				//var toremovetemp_index = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//locked_contents.unshift(targets[toremovetemp_index]);
				//targets.splice(toremovetemp_index,1);
			}

			var more = $('.nav').find('.fa-cubes')[0];
  			var index_ins = parseInt(trgseg.attr("data-row"));
  			var rmvtrgsegs = $('.missing').find('.selectedrestoregrid');
  			var moretrgsegs = $('.more').find('.selectedrestoregrid');
  			if((rmvtrgsegs.length == 0) && (moretrgsegs.length == 0)) {
  				var tempseq = "n - " + empty_next_index;
  				var text = "n - " + empty_next_index + "#";
  				empty_next_index++;
  				targets.splice((currPageNum-1)*maxSegPerPage + index_ins - 1,0,text);

  				//setTimeout(function(){
  					var comboclone = $combo_target.clone(true);
    				gridster_trg.add_widget(comboclone,1,1,1,index_ins);
					comboclone.find('.segid').text(getSeq(text));
					comboclone.find('drag').html(getHtmlText(text));
  					
  					//setTimeout(function(){
  						var totalcurrpagetrgseg = $('.stborder').find('.trg_segment');
  						var lasttrg = $(".trg_segment[data-row='"+ totalcurrpagetrgseg.length +"']");
  						var lasttrgidx = totalcurrpagetrgseg.length + (currPageNum-1)*maxSegPerPage - 1;
  							
  						gridster_trg.remove_widget(lasttrg);
  						
  						var lasttrgtext = getHtmlText(targets[lasttrgidx]);
  						if((lasttrgtext) || (currPageNum != lastPageNum)){
  							if($(more).hasClass("lightup")){
  								//setTimeout(function(){
    								var comboclone = $combo_more.clone(true);
    								gridster_more_trg.add_widget(comboclone,1,1,1,1);
									comboclone.find('.segid').text(getSeq(targets[lasttrgidx]));
									comboclone.find('drag').html(getHtmlText(targets[lasttrgidx]));
									comboclone.find('.moretrgapprove').hide();
    								
    								var totalcurrpagemoretrgsegNum = $(".more_trg_segment").length;
    								var lastmoretrgtext = getHtmlText(targets[lasttrgidx + totalcurrpagemoretrgsegNum - 1]);
    								if((totalcurrpagemoretrgsegNum + locked_more.length) > total_pre_loadable_trgs || (!lastmoretrgtext)){
    									var lastmoretrg = $(".more_trg_segment[data-row='"+ (totalcurrpagemoretrgsegNum) +"']");
    									gridster_more_trg.remove_widget(lastmoretrg);
    								}
    								
    								var lasttotaltrgtext = getHtmlText(targets[targets.length-1]);
	  								if(orgtrgcnt >= sources.length && (!lasttotaltrgtext)){
	  									targets.splice(targets.length-1,1);
	  								}
  							}else{
  								var lasttotaltrgtext = getHtmlText(targets[targets.length-1]);
  								if(orgtrgcnt >= sources.length && (!lasttotaltrgtext)){
  									targets.splice(targets.length-1,1);
  								}
  							}
  							if(!lasttrgtext && (currPageNum == lastPageNum)){
  								targets.splice(lasttrgidx,1);
  							}
  						}else{
  							targets.splice(lasttrgidx,1);
  						}
  					//},300);
  					
  					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  				//},300);
  			} else if((rmvtrgsegs.length != 0) && (moretrgsegs.length == 0)) {
  				var rmvtrgseg = rmvtrgsegs[0];
  				var index_rmv = parseInt($(rmvtrgseg).attr("data-row"))-1;
  				var content = removed_targets[index_rmv];
  				var text = getHtmlText(content);
  				var seq = getSeq(content);
  			
  				gridster_rmv_trg.remove_widget(rmvtrgseg);
  				removed_targets.splice(index_rmv,1);
  			
  				$('.num_trg_removed').text(removed_targets.length);
  				if($('.num_trg_removed').text() >= $('.num_trg_storable').text()) {
  					$('.num_trg_removed').css("color","#d00000");
  				} else {
  					$('.num_trg_removed').css("color","#fff");
  				}
  				
  				//setTimeout(function(){
					targets.splice((currPageNum-1)*maxSegPerPage + index_ins - 1,0,content);

  					var comboclone = $combo_target.clone(true);
    				gridster_trg.add_widget(comboclone,1,1,1,index_ins);
					comboclone.find('.segid').text(seq);
					comboclone.find('drag').html(text);
					
					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  					
  					//setTimeout(function(){
  						var totalcurrpagetrgseg = $('.stborder').find('.trg_segment');
  						var lasttrg = $(".trg_segment[data-row='"+ totalcurrpagetrgseg.length +"']");
  						var lasttrgidx = totalcurrpagetrgseg.length + (currPageNum-1)*maxSegPerPage - 1;
  						gridster_trg.remove_widget(lasttrg);
  						
  						var lasttrgtext = getHtmlText(targets[lasttrgidx]);
  						if((!lasttrgtext) && (currPageNum == lastPageNum)){
  							targets.splice(lasttrgidx,1);
  						}
  					//},300);
  				//},300);
  			} else if((rmvtrgsegs.length == 0) && (moretrgsegs.length != 0)) {
  				var moretrgseg = moretrgsegs[0];
  				var index_more;
  				if(currPageNum == lastPageNum){
  					index_more = parseInt($(moretrgseg).attr("data-row"))-1 + sources.length - locked.length;
  				}else{
  					index_more = parseInt($(moretrgseg).attr("data-row"))-1 + currPageNum*maxSegPerPage - locked.length;
  				}
  				var content = targets[index_more];
  				var text = getHtmlText(content);
  				var seq = getSeq(content);
  			
  				gridster_more_trg.remove_widget(moretrgseg);

  				//setTimeout(function(){
					targets.splice(index_more,1);
  					targets.splice((currPageNum-1)*maxSegPerPage + index_ins - 1,0,content);
  					
  					var comboclone = $combo_target.clone(true);
    				gridster_trg.add_widget(comboclone,1,1,1,index_ins);
					comboclone.find('.segid').text(seq);
					comboclone.find('drag').html(text);
  					
  					//setTimeout(function(){
  						var totalcurrpagetrgseg = $('.stborder').find('.trg_segment');
  						var lasttrg = $(".trg_segment[data-row='"+ totalcurrpagetrgseg.length +"']");
  						var lasttrgidx = totalcurrpagetrgseg.length + (currPageNum-1)*maxSegPerPage - 1;
  						gridster_trg.remove_widget(lasttrg);
  						
  						var lasttrgtext = getHtmlText(targets[lasttrgidx]);
  						if((lasttrgtext) || (currPageNum != lastPageNum)){
  							//setTimeout(function(){
    							var comboclone = $combo_more.clone(true);
    							gridster_more_trg.add_widget(comboclone,1,1,1,1);
								comboclone.find('.segid').text(getSeq(targets[lasttrgidx]));
								comboclone.find('drag').html(getHtmlText(targets[lasttrgidx]));
								comboclone.find('.moretrgapprove').hide();
    								
    							var totalcurrpagemoretrgsegNum = $(".more_trg_segment").length;
    							if((totalcurrpagemoretrgsegNum + locked_more.length) > total_pre_loadable_trgs){
    								setTimeout(function(){
    									var lastmoretrg = $(".more_trg_segment[data-row='"+ (total_pre_loadable_trgs+1) +"']");
    									gridster_more_trg.remove_widget(lastmoretrg);
    								},300);
    							}
  							//},700);
  						}else{
  							targets.splice(lasttrgidx,1);
  						}
  					//},500);
  					
  					keeplocks(removeicon, locked, locked_more, locked_contents, locked_indices);
  				//},300);
  			}
  			
  			enableEvents();
  			
  			updateProgress();
		});

		$('body').on('click', '.swapextra', function() {
			disableEvents();
			
			var trgseg = $(this).closest('.trg_segment');
        	var thisrow = parseInt(trgseg.attr("data-row"));
        	var trgidx = (currPageNum-1)*maxSegPerPage + thisrow - 1;
        	
        	var content = targets[trgidx];
  			var text = getHtmlText(content);
  			var seq = getSeq(content);

  			var rmvtrgsegs = $('.missing').find('.selectedrestoregrid');
  			var moretrgsegs = $('.more').find('.selectedrestoregrid');
  			
  			if((rmvtrgsegs.length == 0) && (moretrgsegs.length == 0)) {
  				
  			} else if((rmvtrgsegs.length != 0) && (moretrgsegs.length == 0)) {
  				var button = $('.selectedrestore')[0];
  				var rmvtrgseg = $(rmvtrgsegs[0]);
  				var index_rmv = parseInt(rmvtrgseg.attr("data-row"))-1;
  				
  				$(trgseg).stop().animate({boxShadow: '0 0 20px #444444'},1);
      			$(rmvtrgseg).stop().animate({boxShadow: '0 0 20px #ffffff'},1);
  				
  				var rmvcontent = removed_targets[index_rmv];
  				var rmvtext = getHtmlText(rmvcontent);
  				var rmvseq = getSeq(rmvcontent);
  			
  				var tempcontent = content;
  				targets[trgidx] = rmvcontent;
  				removed_targets[index_rmv] = tempcontent;
  				
  				$(trgseg.find('.segid')[0]).text(rmvseq);
  				$(trgseg.find('.text')[0]).html(rmvtext);
  				$(rmvtrgseg.find('.segid')[0]).text(seq);
  				$(rmvtrgseg.find('.rmvtext')[0]).html(text);
  				
  				$(button).removeClass("selectedrestore");
      			$(rmvtrgseg).removeClass("selectedrestoregrid");
      			
      			$(trgseg).animate({boxShadow: '0 0 5px rgba(0,0,0,0.3)'},1000);
      			$(rmvtrgseg).animate({boxShadow: '0 0 0px #ffffff'},1000);
  					
  			} else if((rmvtrgsegs.length == 0) && (moretrgsegs.length != 0)) {
  				var button = $('.selectedrestore')[0];
  				var moretrgseg = $(moretrgsegs[0]);
  				var row_more = parseInt(moretrgseg.attr("data-row"))-1;
  				var index_more = (currPageNum-1)*maxSegPerPage + $( ".trg_segment" ).length + row_more;
  				
  				$(trgseg).stop().animate({boxShadow: '0 0 20px #444444'},1);
      			$(moretrgseg).stop().animate({boxShadow: '0 0 20px #ffffff'},1);
      			
  				var morecontent = targets[index_more];
  				var moretext = getHtmlText(morecontent);
  				var moreseq = getSeq(morecontent);
  			
  				var tempcontent = content;
  				targets[trgidx] = morecontent;
  				targets[index_more] = tempcontent;
  				
  				$(trgseg.find('.segid')[0]).text(moreseq);
  				$(trgseg.find('.text')[0]).html(moretext);
  				$(moretrgseg.find('.segid')[0]).text(seq);
  				$(moretrgseg.find('.moretrgtext')[0]).html(text);
  				
  				$(button).removeClass("selectedrestore");
      			$(moretrgseg).removeClass("selectedrestoregrid");

      			$(trgseg).animate({boxShadow: '0 0 5px rgba(0,0,0,0.3)'},1000);
      			$(moretrgseg).animate({boxShadow: '0 0 0px #ffffff'},1000);
  			}
  			
  			enableEvents();
  			
  			updateProgress();
		});
		
	$('.nav').find('.fa-braille').on('click', function(event) {
		var arrow = $('.missing').find('.arrow')[0];
		var trash = $('.nav').find('.fa-braille')[0];
		var share = $('.nav').find('.fa-cubes')[0];
		trash.style.pointerEvents = 'none';
		var panel = $('.missing');
		if (!$(this).hasClass("lightup")) {
			$(this).addClass("lightup");
			trash.style.color="#fff";
			trash.style.boxShadow = "0 0 6px #ffffff";
			var delay;
			if($(share).hasClass("lightup")) {
				share.click();
				delay = 500;
			} else {
				delay = 0;
			}
			setTimeout(function(){
				$('.num_trg_removed').text(removed_targets.length);
				panel.animate({
					'right':'0%'},{
					complete: function() {
						for (i = 0; i < Math.min(removed_targets.length,max_display_rmv_trgs); i++) {
    						var comboclone = $combo_rmv.clone(true);
    						gridster_rmv_trg.add_widget(comboclone,1,1,1,(i+1));
							comboclone.find('.segid').text(getSeq(removed_targets[i]));
							comboclone.find('drag').html(getHtmlText(removed_targets[i]));
						}
						$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
						trash.style.pointerEvents = 'auto';
						
						if($(arrow).hasClass("el-chevron-left")){
							$(arrow).removeClass("el-chevron-left");
							$(arrow).addClass("el-chevron-right");
						}else{
							$(arrow).removeClass("el-chevron-right");
							$(arrow).addClass("el-chevron-left")
						}
					}
				});
			},delay);
		} else {
			$(this).removeClass("lightup");
			trash.style.color="#777";
			trash.style.boxShadow = "0 0 0px #ffffff";
			panel.animate({
					'right':'-33%'},{
					complete: function() {
						if($(arrow).hasClass("el-chevron-left")){
							$(arrow).removeClass("el-chevron-left");
							$(arrow).addClass("el-chevron-right");
						}else{
							$(arrow).removeClass("el-chevron-right");
							$(arrow).addClass("el-chevron-left")
						}
					}
				});
			gridster_rmv_trg.remove_all_widgets();
    		$('#rmvtrgcol').empty();
    		trash.style.pointerEvents = 'auto';
		}	
		return false;	
	});
	
	$("body").on("click",".more_toggle", function(){
		$('.nav').find('.fa-cubes').click();
	});

	$("body").on("click",".missing_toggle", function(){
		$('.nav').find('.fa-braille').click();
	});
	
	$('.nav').find('.fa-cubes').on('click', function(event) {
		var arrow = $('.more').find('.arrow')[0];
		var loadmore = $('.nav').find('.fa-cubes')[0];
		var trash = $('.nav').find('.fa-braille')[0];
		loadmore.style.pointerEvents = 'none';
		var panel = $('.more');
		if (!$(this).hasClass("lightup")) {
			$(this).addClass("lightup");
			loadmore.style.color="#fff";
			loadmore.style.boxShadow = "0 0 6px #ffffff";
			var delay;
			if($(trash).hasClass("lightup")) {
				trash.click();
				delay = 500;
			} else {
				delay = 0;
			}
			
			setTimeout(function(){
				panel.animate({
					'right':'2%'},{
					complete: function() {
  						for (i = 0; i < total_pre_loadable_trgs; i++) {
  							if(currPageNum == lastPageNum){
  								var idx = sources.length + i;
  							}else{
  								var idx = currPageNum*maxSegPerPage + i;
  							}
  							if(idx <= (targets.length - 1)) {
    							var comboclone = $combo_more.clone(true);
    							gridster_more_trg.add_widget(comboclone,1,1,1,(i+1));
								var seq_more = getSeq(targets[idx]);
								comboclone.find('.segid').text(getSeq(targets[idx]));
								comboclone.find('drag').html(getHtmlText(targets[idx]));
								
								if(idx < sources.length){
									var seq_more_src = getSeq(sources[idx]);
									if($.inArray(seq_more_src, locked_targets) !== -1){
  										comboclone.find('.moretrgrestore').hide();
	  								}else{
	  									comboclone.find('.moretrgapprove').hide();
	  								}
								}else{
									comboclone.find('.moretrgapprove').hide();
								}
							}
						}
						
						$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
						loadmore.style.pointerEvents = 'auto';
						
						if($(arrow).hasClass("el-chevron-left")){
							$(arrow).removeClass("el-chevron-left");
							$(arrow).addClass("el-chevron-right");
						}else{
							$(arrow).removeClass("el-chevron-right");
							$(arrow).addClass("el-chevron-left")
						}
					}
				});
			},delay);
		} else {
			$(this).removeClass("lightup");
			loadmore.style.color="#777";
			loadmore.style.boxShadow = "0 0 0px #ffffff";
			panel.animate({
					'right':'-30%'},{
					complete: function() {
						if($(arrow).hasClass("el-chevron-left")){
							$(arrow).removeClass("el-chevron-left");
							$(arrow).addClass("el-chevron-right");
						}else{
							$(arrow).removeClass("el-chevron-right");
							$(arrow).addClass("el-chevron-left")
						}
					}
				});
			gridster_more_trg.remove_all_widgets();
    		$('#moretrgcol').empty();
    		$('#moretrgcol').height("97%");
    		loadmore.style.pointerEvents = 'auto';
		}

		return false;
	});
	
	$('.fa-refresh').on('click', function () {
		$(window).unbind("beforeunload");
		location.reload();
	});
	
	$('.savefile').on('click', function () {
		issavingfile = true;
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		
		var arr1 = [];
		var arr2 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
			var text = getHtmlText(targets[i]);
    		arr1[i] = text;
    		arr2[i] = seq;
		}
		
		var arr3 = [];
		var arr4 = [];
		for (i = 0; i < removed_targets.length; i++) {
			var seq = getSeq(removed_targets[i]);
			var text  = getHtmlText(removed_targets[i]);
			arr3[i] = text;
    		arr4[i] = seq;
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
		
    	$.ajax({
            url: '/RevAligner/rac/save_seg',
            type: "POST",
            dataType: 'json',
            data: encodeURIComponent(JSON.stringify(obj)),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-bad');
              	  $('.message-text').addClass('message-good');
              	  $('.message-text').html('<h4><span class="glyphicon glyphicon-ok-sign"></span>&nbsp;&nbsp;Project Saved Successfully !</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project Failed To Save...</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              }
        });
	});
	
	$('.quicksavefile').on('click', function () {
		issavingfile = true;
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		
		var arr1 = [];
		var arr2 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
			var text = getHtmlText(targets[i]);
    		arr1[i] = text;
    		arr2[i] = seq;
		}
		
		var arr3 = [];
		var arr4 = [];
		for (i = 0; i < removed_targets.length; i++) {
			var seq = getSeq(removed_targets[i]);
			var text  = getHtmlText(removed_targets[i]);
			arr3[i] = text;
    		arr4[i] = seq;
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
		
    	$.ajax({
            url: '/RevAligner/rac/save_seg',
            type: "POST",
            dataType: 'json',
            data: encodeURIComponent(JSON.stringify(obj)),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-bad');
              	  $('.message-text').addClass('message-good');
              	  $('.message-text').html('<h4><span class="glyphicon glyphicon-ok-sign"></span>&nbsp;&nbsp;Project Saved Successfully !</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project Failed To Save...</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              }
        });
	});
	
	$('.nextneedtocheck').on('click', function () {
		event.preventDefault();
		if(!$('#searchspecialtab').hasClass("active")){
			$('.nav-tabs a[href="#special"]').tab('show');
    	}
    	$('.searchspecialtext').click();
	});
	
	$('.saveexportfile').on('click', function () {
		//disable for now
		return;
		issavingfile = true;
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		
		var arr1 = [];
		var arr2 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		var text = getHtmlText(targets[i]);
    		arr1[i] = text;
    		arr2[i] = seq;
		}
		
		var arr3 = [];
		var arr4 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			var text  = getHtmlText(removed_targets[i]);
			arr3[i] = text;
    		arr4[i] = seq;
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
		
    	$.ajax({
            url: '/RevAligner/rac/save_seg',
            type: "POST",
            dataType: 'json',
            data: encodeURIComponent(JSON.stringify(obj)),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-bad');
              	  $('.message-text').addClass('message-good');
              	  $('.message-text').html('<h4><span class="glyphicon glyphicon-ok-sign"></span>&nbsp;&nbsp;Project saved & exporting...</h4>');
              	  $('#message').modal('show');
              	  downloadFromURL('/RevAligner/rac/export', function(){});
              	  issavingfile = false;
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project failed to save...</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              }
        });
	});
	
	$('.generatetoken').on('click', function () {
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
    	$.ajax({
	        url: '/RevAligner/rac/generateaccesstoken',
	        type: "POST",
	        dataType: 'json',
	             
	          beforeSend: function(xhr) {
	            xhr.setRequestHeader("Accept", "application/json");
	            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	          },
	             
	          success: function(data) {
	        	    $('#loading').modal('hide');
	        	    $('#tokenstring').val(data.tokenstring);
	        	    $('#validtime').text(data.expiretime);
              	  	$('#token-modal').modal('show');
	          },
	          
	          error: function(xhr) {
	          	  	$('#loading').modal('hide');
				    alert('failed to generate access token: Not the owner of the project');
	          }
	    });
	});

	$('.prjid').on('click', function () {
		alert("Project ID: " + prjid + '\n' + "Source Language: " + srclang + '\n' + "Target Language: " + trglang + '\n' + "Unlocked segments: " + (sources.length - lockedtargetsnum) + '\n' + '\n' + "Track Change Aligner - Version: 1.0");
	});
	
	$('.closeProject').on('click', function () {
		$('.exitidf').text("closing project");
		$("#exit").modal({backdrop: "static",keyboard: false});
		$('#exit').modal('show');
	});
	
	$('.savebeforeexit').on('click', function () {
		issavingfile = true;
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		
		var arr1 = [];
		var arr2 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		var text = getHtmlText(targets[i]);
    		arr1[i] = text;
    		arr2[i] = seq;
		}
		
		var arr3 = [];
		var arr4 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			var text  = getHtmlText(removed_targets[i]);
			arr3[i] = text;
    		arr4[i] = seq;
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
		
    	$.ajax({
            url: '/RevAligner/rac/save_seg',
            type: "POST",
            dataType: 'json',
            data: encodeURIComponent(JSON.stringify(obj)),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	  $('#loading').modal('hide');
			      $('.message-text').removeClass('message-bad');
			      $('.message-text').addClass('message-good');
			      $('.message-text').html('<h4><span class="glyphicon glyphicon-ok-sign"></span>&nbsp;&nbsp;Project saved and closing now...&nbsp;&nbsp;<span class="timer">3</span></h4>');
			      $('#message').modal({backdrop: "static",keyboard: false});
			      $('#message').modal('show');
			      issavingfile = false;
			      setTimeout(function(){
			      	$('.timer').text("2");
			      },1000);
			      setTimeout(function(){
			      	$('.timer').text("1");
			      },2000);
			      setTimeout(function(){
			      	$('.timer').text("0");
			      },3000);
			      setTimeout(function(){
			    	//redirectwithpost('/RevAligner/rac/sessiontimesout');
			    	$(window).unbind("beforeunload");
			    	window.location.replace('closeproject');
			      },4000);
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project failed to save...</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              }
        });
	});
	
	$('.exitwithoutsave').on('click', function () {
		//redirectwithpost('/RevAligner/rac/sessiontimesout');
		$(window).unbind("beforeunload");
		window.location.replace('closeproject');
	});
	
	$('.exporttranskit').on('click', function () {
		issavingfile = true;
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		
		var arr1 = [];
		var arr2 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		var text = getHtmlText(targets[i]);
    		arr1[i] = text;
    		arr2[i] = seq;
		}
		
		var arr3 = [];
		var arr4 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			var text  = getHtmlText(removed_targets[i]);
			arr3[i] = text;
    		arr4[i] = seq;
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
		
    	$.ajax({
            url: '/RevAligner/rac/save_seg',
            type: "POST",
            dataType: 'json',
            data: encodeURIComponent(JSON.stringify(obj)),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-bad');
              	  $('.message-text').addClass('message-good');
              	  $('.message-text').html('<h4><span class="glyphicon glyphicon-ok-sign"></span>&nbsp;&nbsp;Project saved & exporting translation kit...</h4>');
              	  $('#message').modal('show');
              	  downloadFromURL('/RevAligner/rac/gettranslationkit', function(){});
              	  issavingfile = false;
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project failed to save...</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              }
        });
	});
	
	$('.exportwftmunreview').on('click', function () {
		downloadFromURL('/RevAligner/rac/gettmnotreviewed', function(){});
	});
	
	$('.exportwftmreview').on('click', function () {
		downloadFromURL('/RevAligner/rac/gettmreviewed', function(){});
	});
	
	$('.exporteyrevisionreport').on('click', function () {
		issavingfile = true;
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		
		var arr1 = [];
		var arr2 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		var text = getHtmlText(targets[i]);
    		arr1[i] = text;
    		arr2[i] = seq;
		}
		
		var arr3 = [];
		var arr4 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			var text  = getHtmlText(removed_targets[i]);
			arr3[i] = text;
    		arr4[i] = seq;
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
		
    	$.ajax({
            url: '/RevAligner/rac/save_seg',
            type: "POST",
            dataType: 'json',
            data: encodeURIComponent(JSON.stringify(obj)),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-bad');
              	  $('.message-text').addClass('message-good');
              	  $('.message-text').html('<h4><span class="glyphicon glyphicon-ok-sign"></span>&nbsp;&nbsp;Project saved & exporting EY revision report...</h4>');
              	  $('#message').modal('show');
              	  downloadFromURL('/RevAligner/rac/getsocforey', function(){});
              	  issavingfile = false;
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project failed to save...</h4>');
              	  $('#message').modal('show');
              	  issavingfile = false;
              }
        });
	});
	
	$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
	  	//var target = $(e.target).attr("href");
	  	//alert(target);
	  	$('.search-modal-status').css('color','#777');
		$('.search-modal-status').text("Ready to search")
	});
	
	$('.search').on('click', function () {
		$(".search-modal-dialog").css('opacity','0.9');
		$(".search-modal-dialog").show();
		$(".search-modal-dialog").draggable({
		    handle: '.search-modal-header',
		    cancel: '.searchheadtext',
		    containment: [0,($(window).height()-$('.panel').height()),$(window).width(),$(window).height()]
		});
			
		$('.nav-tabs a[href="#search"]').tab('show');
		$("#searchtext").focus();
	});
	
	$('.searchspecial').on('click', function () {
		$(".search-modal-dialog").css('opacity','0.9');
		$(".search-modal-dialog").show();
		$(".search-modal-dialog").draggable({
		    handle: '.search-modal-header',
		    cancel: '.searchheadtext',
		    containment: [0,($(window).height()-$('.panel').height()),$(window).width(),$(window).height()]
		});
		
		$('.nav-tabs a[href="#special"]').tab('show');
	});
	
	$('input[type=radio][name=special]').on('change', function () {
		if($(this).val() == 'nextnotlocked'){
			$('#searchspecialtext').val('[ next not locked segment ]');
		}else if($(this).val() == 'nextlocked'){
			$('#searchspecialtext').val('[ next locked segment ]');
		}else if($(this).val() == 'nextinsertion'){
			$('#searchspecialtext').val('[ next inserted segment ]');
		}else if($(this).val() == 'nextdeletion'){
			$('#searchspecialtext').val('[ next deleted segment ]');
		}else if($(this).val() == 'nexttracked'){
			$('#searchspecialtext').val('[ next tracked segment ]');
		}else if($(this).val() == 'nextneedsreview'){
			$('#searchspecialtext').val('[ next segment needs review ]');
		}
	});
	
	$('.searchspecialtext').on('click', function () {
		$("#cover").show();
		issearch = true;
		
		$('.searchspecialtext').addClass("el-spin");
		$('.search-modal-status').css('color','#777');
		$('.search-modal-status').text("Searching...")
		setTimeout(function(){
		    var searchtype = $("input[name='special']:checked").val();
			if(searchtype == 'nextnotlocked'){
				findnextnotlocked();
			}else if(searchtype == 'nextlocked'){
				if($(".trgselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var trgseg = $(".trgselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(trgseg).attr("data-row"));
				}
				var end_idx = targets.length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					var seq = getSeq(sources[i]);
					if($.inArray(seq, locked_targets) !== -1){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".trg_segment[data-row='"+ row +"']");
					$(".trg_segment").removeClass('trgselect');
					res.addClass('trgselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					var start_idx2 = 0;
					var end_idx2 = start_idx - 1;
					found = false;
					for(i = start_idx2; i <= end_idx2; i++){
						var seq = getSeq(sources[i]);
						if($.inArray(seq, locked_targets) !== -1){
							found = true;
							found_idx = i;
							break;
						}
					}
								
					if(found){
						$('.search-modal-status').css('color','#3c763d');
						$('.search-modal-status').text("Result found!")
						var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
						var row = (found_idx%maxSegPerPage)+1;
						if(pagenum != currPageNum){
							$('.panel-heading').pagination('selectPage', pagenum);
						}
						var res = $(".trg_segment[data-row='"+ row +"']");
						$(".trg_segment").removeClass('trgselect');
						res.addClass('trgselect');
						$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(searchtype == 'nextinsertion'){
				if($(".srcselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var srcseg = $(".srcselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(srcseg).attr("data-row"));
				}
				var end_idx = sources.length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					if(sources_tctypes[i] == "INSERTION"){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".src_segment[data-row='"+ row +"']");
					$(".src_segment").removeClass('srcselect');
					res.addClass('srcselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					var start_idx2 = 0;
					var end_idx2 = start_idx - 1;
					found = false;
					for(i = start_idx2; i <= end_idx2; i++){
						if(sources_tctypes[i] == "INSERTION"){
							found = true;
							found_idx = i;
							break;
						}
					}
								
					if(found){
						$('.search-modal-status').css('color','#3c763d');
						$('.search-modal-status').text("Result found!")
						var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
						var row = (found_idx%maxSegPerPage)+1;
						if(pagenum != currPageNum){
							$('.panel-heading').pagination('selectPage', pagenum);
						}
						var res = $(".src_segment[data-row='"+ row +"']");
						$(".src_segment").removeClass('srcselect');
						res.addClass('srcselect');
						$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(searchtype == 'nextdeletion'){
				if($(".srcselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var srcseg = $(".srcselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(srcseg).attr("data-row"));
				}
				var end_idx = sources.length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					if(sources_tctypes[i] == "DELETION"){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".src_segment[data-row='"+ row +"']");
					$(".src_segment").removeClass('srcselect');
					res.addClass('srcselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					var start_idx2 = 0;
					var end_idx2 = start_idx - 1;
					found = false;
					for(i = start_idx2; i <= end_idx2; i++){
						if(sources_tctypes[i] == "DELETION"){
							found = true;
							found_idx = i;
							break;
						}
					}
								
					if(found){
						$('.search-modal-status').css('color','#3c763d');
						$('.search-modal-status').text("Result found!")
						var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
						var row = (found_idx%maxSegPerPage)+1;
						if(pagenum != currPageNum){
							$('.panel-heading').pagination('selectPage', pagenum);
						}
						var res = $(".src_segment[data-row='"+ row +"']");
						$(".src_segment").removeClass('srcselect');
						res.addClass('srcselect');
						$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(searchtype == 'nexttracked'){
				if($(".srcselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var srcseg = $(".srcselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(srcseg).attr("data-row"));
				}
				var end_idx = sources.length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					if(sources_tctypes[i] != "NONE"){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".src_segment[data-row='"+ row +"']");
					$(".src_segment").removeClass('srcselect');
					res.addClass('srcselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					var start_idx2 = 0;
					var end_idx2 = start_idx - 1;
					found = false;
					for(i = start_idx2; i <= end_idx2; i++){
						if(sources_tctypes[i] != "NONE"){
							found = true;
							found_idx = i;
							break;
						}
					}
								
					if(found){
						$('.search-modal-status').css('color','#3c763d');
						$('.search-modal-status').text("Result found!")
						var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
						var row = (found_idx%maxSegPerPage)+1;
						if(pagenum != currPageNum){
							$('.panel-heading').pagination('selectPage', pagenum);
						}
						var res = $(".src_segment[data-row='"+ row +"']");
						$(".src_segment").removeClass('srcselect');
						res.addClass('srcselect');
						$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(searchtype == 'nextneedsreview'){
				if($(".trgselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var trgseg = $(".trgselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(trgseg).attr("data-row"));
				}
				var end_idx = sources.length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					var seq = getSeq(sources[i]);
					if($.inArray(seq, review_targets) !== -1){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".trg_segment[data-row='"+ row +"']");
					$(".trg_segment").removeClass('trgselect');
					res.addClass('trgselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					var start_idx2 = 0;
					var end_idx2 = start_idx - 1;
					found = false;
					for(i = start_idx2; i <= end_idx2; i++){
						var seq = getSeq(sources[i]);
						if($.inArray(seq, review_targets) !== -1){
							found = true;
							found_idx = i;
							break;
						}
					}
								
					if(found){
						$('.search-modal-status').css('color','#3c763d');
						$('.search-modal-status').text("Result found!")
						var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
						var row = (found_idx%maxSegPerPage)+1;
						if(pagenum != currPageNum){
							$('.panel-heading').pagination('selectPage', pagenum);
						}
						var res = $(".trg_segment[data-row='"+ row +"']");
						$(".trg_segment").removeClass('trgselect');
						res.addClass('trgselect');
						$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}
			
			$('.searchspecialtext').removeClass("el-spin");
			$("#cover").hide();
			issearch = false;
		}, 500);
	});
	
	$('.searchtext').on('click', function () {
		var searchtext = $('#searchtext').val();
		if(searchtext == '' || searchtext == null){
			$('.search-modal-status').css('color','#8a6d3b');
			$('.search-modal-status').text("Nothing to search!")
			return;
		}
		
		issearch = true;
		$("#cover").show();
		
		$('.searchtext').addClass("el-spin");
		$('.search-modal-status').css('color','#777');
		$('.search-modal-status').text("Searching...")
		setTimeout(function(){
			var sot = $("input[name='sot']:checked").val();
		    var direction = $("input[name='direction']:checked").val();
			var scope = $("input[name='scope']:checked").val();
			var textcase = $("input[name='case']").is(":checked");
			var wrap = $("input[name='wrap']").is(":checked");
			if(!textcase){
				searchtext = searchtext.toLowerCase();
			}
			
			if(sot == 'target' && direction == 'forward' && scope == 'current'){
				if($(".trgselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var trgseg = $(".trgselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(trgseg).attr("data-row"));
				}
				var end_idx = (currPageNum-1)*maxSegPerPage + $('.trg_segment').length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					var tstring = getHtmlText(targets[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var row = i + 1 - (currPageNum-1)*maxSegPerPage;
					var res = $(".trg_segment[data-row='"+ row +"']");
					$(".trg_segment").removeClass('trgselect');
					res.addClass('trgselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = (currPageNum-1)*maxSegPerPage;
						var end_idx2 = start_idx - 1;
						found = false;
						found_idx;
						for(i = start_idx2; i <= end_idx2; i++){
							var tstring = getHtmlText(targets[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var row = i + 1 - (currPageNum-1)*maxSegPerPage;
							var res = $(".trg_segment[data-row='"+ row +"']");
							$(".trg_segment").removeClass('trgselect');
							res.addClass('trgselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(sot == 'source' && direction == 'forward' && scope == 'current'){
				if($(".srcselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var srcseg = $(".srcselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(srcseg).attr("data-row"));
				}
				var end_idx = (currPageNum-1)*maxSegPerPage + $('.src_segment').length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					var tstring = getHtmlText(sources[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var row = i + 1 - (currPageNum-1)*maxSegPerPage;
					var res = $(".src_segment[data-row='"+ row +"']");
					$(".src_segment").removeClass('srcselect');
					res.addClass('srcselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = (currPageNum-1)*maxSegPerPage;
						var end_idx2 = start_idx - 1;
						found = false;
						found_idx;
						for(i = start_idx2; i <= end_idx2; i++){
							var tstring = getHtmlText(sources[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var row = i + 1 - (currPageNum-1)*maxSegPerPage;
							var res = $(".src_segment[data-row='"+ row +"']");
							$(".src_segment").removeClass('srcselect');
							res.addClass('srcselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(sot == 'target' && direction == 'backward' && scope == 'current'){
				if($(".trgselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage + $('.trg_segment').length - 1;
				}else{
					var trgseg = $(".trgselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(trgseg).attr("data-row")) - 2;
				}
				var end_idx = (currPageNum-1)*maxSegPerPage;
				var found = false;
				var found_idx;
				for(i = start_idx; i >= end_idx; i--){
					var tstring = getHtmlText(targets[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var row = i + 1 - (currPageNum-1)*maxSegPerPage;
					var res = $(".trg_segment[data-row='"+ row +"']");
					$(".trg_segment").removeClass('trgselect');
					res.addClass('trgselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = currPageNum*maxSegPerPage - 1;
						var end_idx2 = start_idx + 1;
						found = false;
						found_idx;
						for(i = start_idx2; i >= end_idx2; i--){
							var tstring = getHtmlText(targets[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var row = i + 1 - (currPageNum-1)*maxSegPerPage;
							var res = $(".trg_segment[data-row='"+ row +"']");
							$(".trg_segment").removeClass('trgselect');
							res.addClass('trgselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(sot == 'source' && direction == 'backward' && scope == 'current'){
				if($(".srcselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage + $('.src_segment').length - 1;
				}else{
					var srcseg = $(".srcselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(srcseg).attr("data-row")) - 2;
				}
				var end_idx = (currPageNum-1)*maxSegPerPage;
				var found = false;
				var found_idx;
				for(i = start_idx; i >= end_idx; i--){
					var tstring = getHtmlText(sources[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var row = i + 1 - (currPageNum-1)*maxSegPerPage;
					var res = $(".trg_segment[data-row='"+ row +"']");
					$(".src_segment").removeClass('srcselect');
					res.addClass('srcselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = currPageNum*maxSegPerPage - 1;
						var end_idx2 = start_idx + 1;
						found = false;
						found_idx;
						for(i = start_idx2; i >= end_idx2; i--){
							var tstring = getHtmlText(sources[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var row = i + 1 - (currPageNum-1)*maxSegPerPage;
							var res = $(".trg_segment[data-row='"+ row +"']");
							$(".src_segment").removeClass('srcselect');
							res.addClass('srcselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(sot == 'target' && direction == 'forward' && scope == 'all'){
				if($(".trgselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var trgseg = $(".trgselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(trgseg).attr("data-row"));
				}
				var end_idx = targets.length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					var tstring = getHtmlText(targets[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".trg_segment[data-row='"+ row +"']");
					$(".trg_segment").removeClass('trgselect');
					res.addClass('trgselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = 0;
						var end_idx2 = start_idx - 1;
						found = false;
						found_idx;
						for(i = start_idx2; i <= end_idx2; i++){
							var tstring = getHtmlText(targets[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
							var row = (found_idx%maxSegPerPage)+1;
							if(pagenum != currPageNum){
								$('.panel-heading').pagination('selectPage', pagenum);
							}
							var res = $(".trg_segment[data-row='"+ row +"']");
							$(".trg_segment").removeClass('trgselect');
							res.addClass('trgselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(sot == 'source' && direction == 'forward' && scope == 'all'){
				if($(".srcselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage;
				}else{
					var srcseg = $(".srcselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(srcseg).attr("data-row"));
				}
				var end_idx = sources.length - 1;
				var found = false;
				var found_idx;
				for(i = start_idx; i <= end_idx; i++){
					var tstring = getHtmlText(sources[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".src_segment[data-row='"+ row +"']");
					$(".src_segment").removeClass('srcselect');
					res.addClass('srcselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = 0;
						var end_idx2 = start_idx - 1;
						found = false;
						found_idx;
						for(i = start_idx2; i <= end_idx2; i++){
							var tstring = getHtmlText(sources[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
							var row = (found_idx%maxSegPerPage)+1;
							if(pagenum != currPageNum){
								$('.panel-heading').pagination('selectPage', pagenum);
							}
							var res = $(".src_segment[data-row='"+ row +"']");
							$(".src_segment").removeClass('srcselect');
							res.addClass('srcselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(sot == 'target' && direction == 'backward' && scope == 'all'){
				if($(".trgselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage + $('.trg_segment').length - 1;
				}else{
					var trgseg = $(".trgselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(trgseg).attr("data-row")) - 2;
				}
				var end_idx = 0;
				var found = false;
				var found_idx;
				for(i = start_idx; i >= end_idx; i--){
					var tstring = getHtmlText(targets[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".trg_segment[data-row='"+ row +"']");
					$(".trg_segment").removeClass('trgselect');
					res.addClass('trgselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = sources.length - 1;
						var end_idx2 = start_idx + 1;
						found = false;
						found_idx;
						for(i = start_idx2; i >= end_idx2; i--){
							var tstring = getHtmlText(targets[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
							var row = (found_idx%maxSegPerPage)+1;
							if(pagenum != currPageNum){
								$('.panel-heading').pagination('selectPage', pagenum);
							}
							var res = $(".trg_segment[data-row='"+ row +"']");
							$(".trg_segment").removeClass('trgselect');
							res.addClass('trgselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}else if(sot == 'source' && direction == 'backward' && scope == 'all'){
				if($(".srcselect").length == 0){
					var start_idx = (currPageNum-1)*maxSegPerPage + $('.src_segment').length - 1;
				}else{
					var srcseg = $(".srcselect")[0];
					var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(srcseg).attr("data-row")) - 2;
				}
				var end_idx = 0;
				var found = false;
				var found_idx;
				for(i = start_idx; i >= end_idx; i--){
					var tstring = getHtmlText(sources[i]);
					if(!textcase){
						tstring = tstring.toLowerCase();
					}
					if(tstring.includes(searchtext)){
						found = true;
						found_idx = i;
						break;
					}
				}
				if(found){
					$('.search-modal-status').css('color','#3c763d');
					$('.search-modal-status').text("Result found!")
						
					var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
					var row = (found_idx%maxSegPerPage)+1;
					if(pagenum != currPageNum){
						$('.panel-heading').pagination('selectPage', pagenum);
					}
					var res = $(".src_segment[data-row='"+ row +"']");
					$(".src_segment").removeClass('srcselect');
					res.addClass('srcselect');
					$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
				}else{
					if(wrap){
						var start_idx2 = currPageNum*maxSegPerPage - 1;
						var end_idx2 = start_idx + 1;
						found = false;
						found_idx;
						for(i = start_idx2; i >= end_idx2; i--){
							var tstring = getHtmlText(sources[i]);
							if(!textcase){
								tstring = tstring.toLowerCase();
							}
							if(tstring.includes(searchtext)){
								found = true;
								found_idx = i;
								break;
							}
						}
						
						if(found){
							$('.search-modal-status').css('color','#3c763d');
							$('.search-modal-status').text("Result found!")
								
							var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
							var row = (found_idx%maxSegPerPage)+1;
							if(pagenum != currPageNum){
								$('.panel-heading').pagination('selectPage', pagenum);
							}
							var res = $(".src_segment[data-row='"+ row +"']");
							$(".src_segment").removeClass('srcselect');
							res.addClass('srcselect');
							$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
						}else{
							$('.search-modal-status').css('color','#a94442');
							$('.search-modal-status').text("No result found!")
						}
					}else{
						$('.search-modal-status').css('color','#a94442');
						$('.search-modal-status').text("No result found!")
					}
				}
			}
			
			$('.searchtext').removeClass("el-spin");
			$("#cover").hide();
			issearch = false;
		}, 500);
		
	});
	
	$('.searchclose').on('click', function () {
		$(".search-modal-dialog").hide();
	});
	
	$(document).mouseup(function (e)
	{
	    var container1 = $(".trg_segment");
	    if (container1.is(e.target) || container1.has(e.target).length !== 0 && (!$(e.target).hasClass('trgselect'))) 
	    {
	    	$( ".trg_segment" ).removeClass('trgselect');
	        $(e.target).closest( ".trg_segment" ).addClass('trgselect');
	    }
	    
	    var container2 = $(".src_segment");
	    if (container2.is(e.target) || container2.has(e.target).length !== 0 && (!$(e.target).hasClass('srcselect'))) 
	    {
	    	$( ".src_segment" ).removeClass('srcselect');
	        $(e.target).closest( ".src_segment" ).addClass('srcselect');
	    }
	});
	
	$(document).mousedown(function (e)
	{
	    var container = $(".search-modal-dialog");
	    if (container.is(e.target) || container.has(e.target).length !== 0) 
	    {
	        $(".search-modal-dialog").css('opacity','0.9');
	    }else{
	    	$(".search-modal-dialog").css('opacity','0.3');
	    }
	});
	
	$(window).resize(function(){
		var org_mg_v = mg_v;
		
		wd = $(window).width()/3.4;
		wd_missing = $(window).width()/3.3;
		wd_more = $(window).width()/3.5;
		ht = $(window).height()/9.5;
		mg_h = $(window).width()/146;
		mg_v = $(window).height()/98;
	  
    	gridster_src.resize_widget_dimensions({
			widget_base_dimensions: [wd, ht]
		});
		generateStyleSheet("#sourcegrids", ht, wd, mg_h, mg_v, maxSegPerPage);
			
		gridster_trg.resize_widget_dimensions({
			   widget_base_dimensions: [wd, ht]
		});
		generateStyleSheet("#targetgrids", ht, wd, mg_h, mg_v, maxSegPerPage);
		
		gridster_rmv_trg.resize_widget_dimensions({
			widget_base_dimensions: [wd_missing, ht]
		});
		generateStyleSheet("#rmvtargetgrids", ht, wd_missing, mg_h, mg_v, total_storable_removed_trgs);
			
		gridster_more_trg.resize_widget_dimensions({
			widget_base_dimensions: [wd_more, ht]
		});
		generateStyleSheet("#moretargetgrids", ht, wd_more, mg_h, mg_v, total_pre_loadable_trgs);

		var next_page_lt = $('#targetgrids').position().left + $('#targetgrids').width() - $('#next_page').width() - 15;
	    $('#next_page').css('left',next_page_lt+'px');
	    
	    var down_page_lt = ($('#sourcegrids').position().left + $('#sourcegrids').width() + $('#targetgrids').position().left - $('#down_page').width() - 15)/2;
	    $('#down_page').css('left',down_page_lt+'px');
	});
		
	$(window).focus(function(e) {
		validateSession();
	});

	$('#prev_page').on('click', function () {
		$('.panel-heading').pagination('prevPage');
	});
	
	$('#next_page').on('click', function () {
		$('.panel-heading').pagination('nextPage');
	});
	
	$('#down_page').on('click', function () {
		var y = $('.panel-body').scrollTop();
		var h = $('.panel-body').height();
		$('.panel-body').animate(
			{scrollTop: (y + h)},
			200
		);
	});
	
	$('.signout').on('click', function () {
		$(window).unbind("beforeunload");
		window.location.replace('logout');
	});
	
			
	$('body').on('click', '.findps', function() {
		issearch = true;
		$("#cover").show();
		
		var rmvseg = $(this).closest('.rmv_trg_segment');
		setTimeout(function(){
			
	  		var index = parseInt(rmvseg.attr("data-row"))-1;
			var seq = parseInt(getSeq(removed_targets[index]));
			
			var start_idx = 0;
			var end_idx = targets.length - 1;
			var found_idx = end_idx;
			var prev_idx = -1;
			for(i = start_idx; i <= end_idx; i++){
				var tempseqstr = getSeq(targets[i]);
				if(tempseqstr.startsWith("n - ")){
					continue;
				}
				var tempseq = parseInt(tempseqstr.split(" - ")[0]);
				if(tempseq > seq){
					if(i == start_idx || prev_idx == -1){
						found_idx = i;
					}else{
						found_idx = prev_idx;
					}
					break;
				}
				prev_idx = i;
			}
			
			var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
			var row = (found_idx%maxSegPerPage)+1;
			if(pagenum != currPageNum){
				$('.panel-heading').pagination('selectPage', pagenum);
			}
			var res = $(".trg_segment[data-row='"+ row +"']");
			$(".trg_segment").removeClass('trgselect');
			res.addClass('trgselect');
			$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());

	      	issearch = false;
			$("#cover").hide();
		},300);
	});
	
	$('body').on('click', '#prjprogress', function() {
		bar.animate(0, {
		    duration: 0.01
		}, function() {
		    var ratio = locked_targets.length/(removed_targets.length + targets.length)
			if(ratio > 0.6 && ratio != 1.0){
				progress_curr_color = progress_middle_color;
			}else if(ratio == 1.0){
				progress_curr_color = progress_done_color;
			}
			var opts = {
				from: { color: progress_bad_color},
				to: { color: progress_curr_color}
			};
			
			bar.animate(parseFloat(ratio.toString().substring(0,4)), opts);
			
			var newtitle = "<div class='prjprogressdetail'>unlocked target segment(s): <b>" + (targets.length-locked_targets.length) + "</b> / <b>" + targets.length + "</b><br />unaligned target segment(s): <b>" + removed_targets.length + "</b></div>";
			$('#prjprogress').tooltip('destroy');
			$('#prjprogress').tooltip({
				container: 'body',
				title: newtitle,
				delay: {show: 600, hide: 0},
				placement : 'bottom',
				html : 'true'
			});
		});
	});
	
	$("#tokenstring").on("click", function () {
	   $(this).select();
	});
	
	function findnextnotlocked() {
		if($(".trgselect").length == 0){
			var start_idx = (currPageNum-1)*maxSegPerPage;
		}else{
			var trgseg = $(".trgselect")[0];
			var start_idx = (currPageNum-1)*maxSegPerPage + parseInt($(trgseg).attr("data-row"));
		}
		var end_idx = targets.length - 1;
		var found = false;
		var found_idx = -1;
		for(i = start_idx; i <= end_idx; i++){
			var seq = getSeq(sources[i]);
			if($.inArray(seq, locked_targets) == -1){
				found = true;
				found_idx = i;
				break;
			}
		}
		if(found){
			$('.search-modal-status').css('color','#3c763d');
			$('.search-modal-status').text("Result found!")
			var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
			var row = (found_idx%maxSegPerPage)+1;
			if(pagenum != currPageNum){
				$('.panel-heading').pagination('selectPage', pagenum);
			}
			var res = $(".trg_segment[data-row='"+ row +"']");
			$(".trg_segment").removeClass('trgselect');
			res.addClass('trgselect');
			$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
		}else{
			var start_idx2 = 0;
			var end_idx2 = start_idx - 1;
			found = false;
			for(i = start_idx2; i <= end_idx2; i++){
				var seq = getSeq(sources[i]);
				if($.inArray(seq, locked_targets) == -1){
					found = true;
					found_idx = i;
					break;
				}
			}
						
			if(found){
				$('.search-modal-status').css('color','#3c763d');
				$('.search-modal-status').text("Result found!")
				var pagenum = Math.ceil((found_idx + 1)/maxSegPerPage);
				var row = (found_idx%maxSegPerPage)+1;
				if(pagenum != currPageNum){
					$('.panel-heading').pagination('selectPage', pagenum);
				}
				var res = $(".trg_segment[data-row='"+ row +"']");
				$(".trg_segment").removeClass('trgselect');
				res.addClass('trgselect');
				$('.panel-body').scrollTop(res.offset().top - $('.panel-body').offset().top + $('.panel-body').scrollTop());
			}else{
				$('.search-modal-status').css('color','#a94442');
				$('.search-modal-status').text("No result found!")
			}
		}
	}
	
	function redirectwithpost(redirectUrl) {
		var form = $('<form action="' + redirectUrl + '" method="post">' + '<input type="hidden"' + '"></input>' + '</form>');
		$('body').append(form);
		$(form).submit();
	};
	
	function disableEvents()
	{
		$('*').css('pointer-events','none');
	}
	
	function enableEvents()
	{
		$('*').css('pointer-events','auto');
	}
	
	function autoSaveAlignment() {
		if(issavingfile){
			return;
		}
		$('.savefile').css('pointer-events','none');
		$('.saveexportfile').css('pointer-events','none');
		$('.savebeforeexit').css('pointer-events','none');
		$('.exporttranskit').css('pointer-events','none');
			
		var arr1 = [];
		var arr2 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		var text = getHtmlText(targets[i]);
    		arr1[i] = text;
    		arr2[i] = seq;
		}
		
		var arr3 = [];
		var arr4 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			var text  = getHtmlText(removed_targets[i]);
			arr3[i] = text;
    		arr4[i] = seq;
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':arr3,'arr4':arr4,'arr5':locked_targets,'nullcnt':empty_next_index,'arr6':edited_targets,'arr7':review_targets,'arr8':ignore_sources};
		
    	$.ajax({
            url: '/RevAligner/rac/auto_save_seg',
            type: "POST",
            dataType: 'json',
            data: encodeURIComponent(JSON.stringify(obj)),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	  $('.savefile').css('pointer-events','auto');
				  $('.saveexportfile').css('pointer-events','auto');
				  $('.savebeforeexit').css('pointer-events','auto');
				  $('.exporttranskit').css('pointer-events','auto');
              },
              
              error: function() {
			  	  $('.savefile').css('pointer-events','auto');
				  $('.saveexportfile').css('pointer-events','auto');
				  $('.savebeforeexit').css('pointer-events','auto');
				  $('.exporttranskit').css('pointer-events','auto');
              }
        });
	}
	
	function validateSession() {
		if(typeof prjid == 'undefined') {
			return;
		}
		
		$.ajax({
            url: '/RevAligner/rac/checkifsessionvalid',
            type: "GET",
            dataType: 'text',
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "text/plain");
                xhr.setRequestHeader("Content-Type", "text/plain");
              },
                 
              success: function() {
              	    
              },
              
              error: function() {
    				$(window).unbind("beforeunload");
					redirectwithpost('/RevAligner/rac/sessiontimesout');
					clearInterval(intervalId_checkSession);
             }
        });
	}

		function ini_page(pageNumber) {
			
			$(".panel-body").scrollTop(0);
	    					
		    currPageNum = pageNumber;

		    gridster_src.remove_all_widgets();

		    gridster_trg.remove_all_widgets();
		    		
		    gridster_more_trg.remove_all_widgets();

		   	$('#srccol').empty();

		    $('#trgcol').empty();
		    		
		    $('#moretrgcol').empty();
		    $('#moretrgcol').height("97%");

			var prev_seq;
			if(pageNumber == 1){
				prev_seq = "0 - 0 - 0";
			}else{
				prev_seq = getSeq(sources[(pageNumber-1)*maxSegPerPage - 1]);
			}
					
			for (i = 0; i < maxSegPerPage; i++) {
				var idx = (pageNumber-1)*maxSegPerPage + i;
				if(idx <= (sources.length - 1)) {
					var comboclone = $combo_source.clone(true);
					var cur_seq = getSeq(sources[idx]);
					comboclone.find('.segid').text(cur_seq);
					var tp = sources_tctypes[idx];
					var label = comboclone.find('.segtctype');
					$(label).addClass("tp"+tp.replace("/", ""));
					label.text(sources_tctypes[idx]);
					comboclone.find('drag').html(getHtmlText(sources[idx]));
					var cur_unit_color_code;
					if(prev_seq.split(" - ")[0] == cur_seq.split(" - ")[0] && prev_seq.split(" - ")[1] == cur_seq.split(" - ")[1]){
						cur_unit_color_code = prev_unit_color_code;
					}else{
						if(prev_unit_color_code == unit_color_code){
							cur_unit_color_code = "#FFFFFF";
						}else{
							cur_unit_color_code = unit_color_code;
						}
					}
					comboclone.find('.text').css("background-color",cur_unit_color_code);
					prev_seq = cur_seq;
					prev_unit_color_code = cur_unit_color_code;
		    		gridster_src.add_widget(comboclone,1,1,1,(i+1));
				}
			}

			for (i = 0; i < maxSegPerPage; i++) {
				var idx = (pageNumber-1)*maxSegPerPage + i;
				if(idx <= (sources.length - 1)) {
					var comboclone = $combo_target.clone(true);
					comboclone.find('.segid').text(getSeq(targets[idx]));
					comboclone.find('drag').html(getHtmlText(targets[idx]));
		    		gridster_trg.add_widget(comboclone,1,1,1,(i+1));
				}
			}
					
			$( ".trg_segment" ).each(function() {
				var index = (currPageNum-1)*maxSegPerPage + parseInt($(this).attr("data-row"))-1;
		  		var seq = getSeq(sources[index]);
		  		if($.inArray(seq, locked_targets) !== -1){
		  			var icon = $(this).find('.el-unlock');
		  			$(icon).removeClass("el-unlock");
					$(icon).addClass("el-lock");
					var button = $(this).find('.approve');
					$(button).attr('title','Locked');
					$(this).addClass("lock");
					$(this).addClass("static");
					$(this).find(":not(button.approve):not(button.toreview)").prop("disabled", true);
					$(this).find('.text').css("background-color", "#13bd9c");
					$(this).find('.text').css("color", "#DBFEF8");
		  		}
		  				
		  		if($.inArray(seq, review_targets) !== -1){
		  			var icon = $(this).find('.toreview .el')[0];
					$(icon).addClass("needreview");
		  		}
			});
					
			$( ".src_segment" ).each(function() {
				var index = (currPageNum-1)*maxSegPerPage + parseInt($(this).attr("data-row"))-1;
		  		var seq = getSeq(sources[index]);
		  		var srcseg = $(this);
		  		
		  		if($.inArray(seq, ignore_sources) !== -1){
		  			var icon = $($(this).find('.ignore')[0]).children('.el')[0];
					$(icon).addClass("ignored");
					$(this).addClass("segignored");
		  		}
			});
					
			var more = $('.nav').find('.fa-cubes')[0];
		  	if($(more).hasClass("lightup")) {
		  		for (i = 0; i < total_pre_loadable_trgs; i++) {
		  			if(currPageNum == lastPageNum){
		  				var idx = sources.length + i;
		  			}else{
		  				var idx = currPageNum*maxSegPerPage + i;
		  			}
		  			if(idx <= (targets.length - 1)) {
		    			var comboclone = $combo_more.clone(true);
		    			gridster_more_trg.add_widget(comboclone,1,1,1,(i+1));
						var seq_more = getSeq(targets[idx]);
						comboclone.find('.segid').text(seq_more);
						comboclone.find('drag').html(getHtmlText(targets[idx]));
								
						if(idx < sources.length){
							var seq_more_src = getSeq(sources[idx]);
							if($.inArray(seq_more_src, locked_targets) !== -1){
		  						comboclone.find('.moretrgrestore').hide();
			  				}else{
			  					comboclone.find('.moretrgapprove').hide();
			  				}
						}else{
							comboclone.find('.moretrgapprove').hide();
						}
					}
				}
		  	}
					
			var currentP = $('.panel-heading').pagination('getCurrentPage');
			var lastP = $('.panel-heading').pagination('getPagesCount');
			if(currentP == 1){
				$('#prev_page').prop("disabled", true);
			}else{
				$('#prev_page').prop("disabled", false);
			}
			if(currentP == lastP){
				$('#next_page').prop("disabled", true);
			}else{
				$('#next_page').prop("disabled", false);
			}
			    	
		    $('.tpINSERTION').tooltip({container: 'body', title: "Insertion is locked automatically"});
			$('.tpDELETION').tooltip({container: 'body', title: "Deletion will not be in the TXLF"});
			$('.tpNA').tooltip({container: 'body', title: "Fake source segment used to line up with extra target segment"});
			$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
					
			if(!isfirstin){
				$("#cover").hide();	
			}
		}
		
		function insertHtmlAtCaret(html) {
		    var sel, range;
		    if (window.getSelection) {
		        // IE9 and non-IE
		        sel = window.getSelection();
		        if (sel.getRangeAt && sel.rangeCount) {
		            range = sel.getRangeAt(0);
		            range.deleteContents();

		            // Range.createContextualFragment() would be useful here but is
		            // only relatively recently standardized and is not supported in
		            // some browsers (IE9, for one)
		            var el = document.createElement("div");
		            el.innerHTML = html;
		            var frag = document.createDocumentFragment(), node, lastNode;
		            while ( (node = el.firstChild) ) {
		                lastNode = frag.appendChild(node);
		            }
		            range.insertNode(frag);

		            // Preserve the selection
		            if (lastNode) {
		                range = range.cloneRange();
		                range.setStartAfter(lastNode);
		                range.collapse(true);
		                sel.removeAllRanges();
		                sel.addRange(range);
		            }
		        }
		    } else if (document.selection && document.selection.type != "Control") {
		        // IE < 9
		        document.selection.createRange().pasteHTML(html);
		    }
		}

		function setSelectionRange(element, count){
			var range = document.createRange();
			var sel = window.getSelection();
			range.setStartAfter(element.childNodes[2*count+1], 0);
			range.collapse(true);
			sel.removeAllRanges();
			sel.addRange(range);
		}
		
		function getCaretCharacterOffsetWithin(element) {
		    var caretOffset = 0;
		    var doc = element.ownerDocument || element.document;
		    var win = doc.defaultView || doc.parentWindow;
		    var sel;
		    if (typeof win.getSelection != "undefined") {
		        sel = win.getSelection();
		        if (sel.rangeCount > 0) {
		            var range = win.getSelection().getRangeAt(0);
		            var preCaretRange = range.cloneRange();
		            preCaretRange.selectNodeContents(element);
		            preCaretRange.setEnd(range.endContainer, range.endOffset);
		            caretOffset = preCaretRange.toString().length;
		        }
		    } else if ( (sel = doc.selection) && sel.type != "Control") {
		        var textRange = sel.createRange();
		        var preCaretTextRange = doc.body.createTextRange();
		        preCaretTextRange.moveToElementText(element);
		        preCaretTextRange.setEndPoint("EndToEnd", textRange);
		        caretOffset = preCaretTextRange.text.length;
		    }

		    return caretOffset;
		}

      	function getHtmlText(s) {
    		return s.substring(s.indexOf("#") + 1)
    				.replace(/&lt;/g, '<')
            		.replace(/&gt;/g, '>')
            		.replace(/&amp;/g, '&');
		}
		
		function downloadFromURL(url, callback){
	   		var hiddenIFrameID = 'hiddenDownloader0';
	   		var iframe = document.createElement('iframe');
	   		iframe.id = hiddenIFrameID;
	   		iframe.style.display = 'none';
	   		document.body.appendChild(iframe);
	   		iframe.src = url;
	   		callback();
		}

		function getSeq(s) {
    		return s.substring(0,s.indexOf("#"));
		}
		
		function keeplocks(item, locked, locked_more, locked_contents, locked_indices) {
			for(i=0;i<locked.length;i++){
  				var rownum = locked[i];
  				var comboclone = $combo_target.clone(true);
				gridster_trg.add_widget(comboclone,1,1,1,rownum);
				comboclone.find('.segid').text(getSeq(locked_contents[i]));
				comboclone.find('drag').html(getHtmlText(locked_contents[i]));
				var icon = comboclone.find('.el-unlock');
				$(icon).removeClass("el-unlock");
				$(icon).addClass("el-lock");
				$(comboclone).addClass("lock");
				$(comboclone).addClass("static");
				$(comboclone).find(":not(button.approve):not(button.toreview)").prop("disabled", true);
				$(comboclone).find('.text').css("background-color", "#13bd9c");
				$(comboclone).find('.text').css("color", "#DBFEF8");
				
				//var insertpos = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//targets.splice(insertpos,0,locked_contents[i]);
  			}
  			
  			for(i=locked.length;i<(locked.length+locked_more.length);i++){
  				var rownum_more = locked_more[i-locked.length];
  				var comboclone = $combo_more.clone(true);
				gridster_more_trg.add_widget(comboclone,1,1,1,rownum_more);
				comboclone.find('.segid').text(getSeq(locked_contents[i]));
				comboclone.find('drag').html(getHtmlText(locked_contents[i]));
				comboclone.find('.moretrgrestore').hide();
				
				//var insertpos = (currPageNum-1)*maxSegPerPage + rownum - 1;
				//targets.splice(insertpos,0,locked_contents[i]);
  			}
  			
  			var start_index = (currPageNum-1)*maxSegPerPage + locked[0] - 1;
  			for(i=0; i < locked_contents.length; i++){
  				targets.splice(locked_indices[i],0,locked_contents[i]);
  			}
  			
  			setTimeout(function(){
  				item.prop('disabled',false);
  			}, 2000);
  			
		}
		
		function generateStyleSheet(namespace, height, width, margin_h, margin_v, segcount){
			var styles = '';
			styles += (namespace + ' [data-sizex="1"] { width:' + width + 'px;}');
			styles += (namespace + ' [data-sizey="1"] { height:' + height + 'px;}');
			styles += (namespace + ' [data-col="1"] { left:' + margin_h + 'px;}');
			
			styles += (namespace + ' [data-row="1"] { top:' + margin_v + 'px;}');
			var inc = 2 * margin_v + height;
			var prev = margin_v;
			//for (i = opts.rows; i >= 0; i--) {
			for (i = 1; i < (segcount+3); i++) {
				prev += inc;
	            styles += (namespace + ' [data-row="' + (i + 1) + '"] { top:' + prev + 'px;} ');
	        }
	        
	        var d = document;
	        for(i = 0; i < d.getElementsByTagName('style').length; i++){
	        	var sty = d.getElementsByTagName('style')[i];
	        	if(sty.getAttribute('generated-from') == namespace){
	        		sty.parentNode.removeChild(sty);
	        		break;
	        	}
	        }
	        
		    var tag = d.createElement('style');
		    d.getElementsByTagName('head')[0].appendChild(tag);
		    tag.setAttribute('type', 'text/css');
		    tag.setAttribute('generated-from', namespace);

		    if (tag.styleSheet) {
		    	tag.styleSheet.cssText = styles;
		    }else{
		        tag.appendChild(document.createTextNode(styles));
		    }
		}
		
		function generateStyleSheetSingle(namespace, height, width, margin_h, margin_v, segcount, trgsegindex, trgheight){
			var styles = '';
			styles += (namespace + ' [data-sizex="1"] { width:' + width + 'px;}');
			styles += (namespace + ' [data-sizey="1"] { height:' + height + 'px;}');
			styles += (namespace + ' [data-col="1"] { left:' + margin_h + 'px;}');
			
			styles += (namespace + ' [data-row="1"] { top:' + margin_v + 'px;}');
			var inc = 2 * margin_v + height;
			var prev = margin_v;
			//for (i = opts.rows; i >= 0; i--) {
			for (i = 1; i < (segcount+3); i++) {
				if(i == trgsegindex){
					prev += 2 * margin_v + trgheight;
					styles += (namespace + ' [data-row="' + trgsegindex + '"] { height:' + trgheight + 'px;}');
				}else{
					prev += inc;
				}
				styles += (namespace + ' [data-row="' + (i + 1) + '"] { top:' + prev + 'px;} ');
	        }
	        
	        var d = document;
	        for(i = 0; i < d.getElementsByTagName('style').length; i++){
	        	var sty = d.getElementsByTagName('style')[i];
	        	if(sty.getAttribute('generated-from') == namespace){
	        		sty.parentNode.removeChild(sty);
	        		break;
	        	}
	        }
	        
		    var tag = d.createElement('style');
		    d.getElementsByTagName('head')[0].appendChild(tag);
		    tag.setAttribute('type', 'text/css');
		    tag.setAttribute('generated-from', namespace);

		    if (tag.styleSheet) {
		    	tag.styleSheet.cssText = styles;
		    }else{
		        tag.appendChild(document.createTextNode(styles));
		    }
		}
		
		function isBrOrWhitespace(node) {
		    return node && ( (node.nodeType == 1 && node.nodeName.toLowerCase() == "br") ||
		           (node.nodeType == 3 && /^\s*$/.test(node.nodeValue) ) );
		}

		function trimBrs(node) {
			while ( isBrOrWhitespace(node.firstChild) ) {
			    node.removeChild(node.firstChild);
			}
			while ( isBrOrWhitespace(node.lastChild) ) {
			    node.removeChild(node.lastChild);
			}
		}
		
		function isInvalidTag(node) {
		    return (node && node.nodeType == 1 && node.nodeName.toLowerCase() != "br");
		}
		
		function replaceInvalidTag(node) {
			var children = node.children;
			for (var i = 0; i < children.length; i++) {
				var child = children[i];
				if(isInvalidTag(child)){
					$(child).replaceWith($(child).text());
				}
			}
		}
		
		function updateProgress() {
			var ratio = locked_targets.length/(removed_targets.length + targets.length)
			var progress_target_color = progress_curr_color;
			if(ratio > 0.6 && ratio != 1.0){
				progress_target_color = progress_middle_color;
			}else if(ratio == 1.0){
				progress_target_color = progress_done_color;
			}
			var opts = {
				from: { color: progress_curr_color},
				to: { color: progress_target_color}
			};
			
			bar.animate(parseFloat(ratio.toString().substring(0,4)), opts);
			progress_curr_color = progress_target_color;
			
			var newtitle = "<div class='prjprogressdetail'>unlocked target segment(s): <b>" + (targets.length-locked_targets.length) + "</b> / <b>" + targets.length + "</b><br />unaligned target segment(s): <b>" + removed_targets.length + "</b></div>";
			$('#prjprogress').tooltip('destroy');
			$('#prjprogress').tooltip({
				container: 'body',
				title: newtitle,
				delay: {show: 600, hide: 0},
				placement : 'bottom',
				html : 'true'
			});
		}
    });
</script>
<style>
.gridster .player {
    background: #BBB;
}

.gridster .preview-holder {
    border: dashed;
    border-width: 1px;
}

.dropdown-menu {
    width: 280px !important;
    background-color: #bbb;
    font-size: 15px;
}

a{
	cursor:pointer;
}
</style>
</head>
<body spellcheck="false">
	<nav class="navbar navbar-inverse navbar-fixed-top" style="display:none">
  		<div class="container-fluid">
    		<div class="navbar-header">
      			<a class="navbar-brand">REVISION ALIGNER</a>
    		</div>
    		<div>
      			<ul class="nav navbar-nav">
        			<li description="Manage Project" class="dropdown">
    					<a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-th-list"></i></a>
    					<ul class="dropdown-menu">
            				<li><a class="savefile">Save<span class="glyphicon pull-right glyphicon-floppy-disk mprj"></span></a></li>
            				<li><a class="saveexportfile">Save And Export<span class="glyphicon pull-right glyphicon-export mprj"></span></a></li>
    						<li><a class="generatetoken">Generate Access Token<span class="glyphicon pull-right glyphicon-eye-open mprj"></span></a></li>
    						<li><a class="config">Configuration<span class="glyphicon pull-right glyphicon-cog mprj"></span></a></li>
    						<li><a class="prjid">About<span class="glyphicon pull-right glyphicon-info-sign mprj"></span></a></li>
    						<li class="divider"></li>
            				<li><a class="closeProject">Close Project<span class="glyphicon pull-right glyphicon-off mprj"></span></a></li>
          				</ul>
    				</li>
        			<li description="More Target Grids"><a><i class="fa fa-cubes"></i></a></li>
        			<li description="Show Removed Trans"><a><i class="fa fa-braille"></i></a></li>
    				<li description="Get Translation Kit" class="dropdown">
    					<a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-download"></i></a>
    					<ul class="dropdown-menu">
            				<li><a class="exporttranskit">Download Translation Kit<span class="glyphicon pull-right glyphicon-briefcase dl"></span></a></li>
            				<li class="dropdown-submenu">
    							<a class="exportwftm">Download as Wordfast TM<span class="glyphicon pull-right glyphicon-chevron-right dl"></span></a>
    							<ul class="dropdown-menu">
    								<li><a class="exportwftmreview">Reviewed<span class="glyphicon pull-right glyphicon-list-alt dl text-info"></span></a></li>
    								<li><a class="exportwftmunreview">Not Reviewed<span class="glyphicon pull-right glyphicon-list-alt dl text-warning"></span></a></li>
    							</ul>
    						</li>
    						<li><a class="exporteyrevisionreport">Download EY Revision Report<span class="glyphicon pull-right glyphicon-stats dl"></span></a></li>
          				</ul>
    				</li>
    				<li description="Tools" class="dropdown">
    					<a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-gavel"></i></a>
    					<ul class="dropdown-menu">
            				<li><a class="search">Search<span class="glyphicon pull-right glyphicon-search tl"></span></a></li>
    						<li><a class="searchspecial">Search Special<span class="glyphicon pull-right glyphicon-search text-danger tl"></span></a></li>
          				</ul>
    				</li>
    				<li description="View" class="dropdown">
    					<a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-laptop"></i></a>
    				</li>
    				<li description="Settings" class="dropdown">
    					<a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-cogs"></i></a>
    				</li>
    				<div description="Project Progress" id="prjprogress">
    					
    				</div>
      			</ul>
    			<ul class="nav navbar-nav" style="float: right">
    				<li description="" class="pull-right"><a><i class="fa fa-refresh"></i></a></li>
    				<li description="Manage Acount" class="pull-right">
    					<a class="dropdown-toggle" data-toggle="dropdown">
    						<span class="usernamelabel"></span>
    						<i class="fa fa-user usericon"></i>
   						</a>
    					<ul class="dropdown-menu">
    						<li><a class="profile">Acount Info<i class="fa fa-street-view pull-right ma"></i></a></li>
    						<li><a class="markprj">Mark Project<i class="fa fa-star-o pull-right ma"></i></a></li>
    						<li><a class="history">History<i class="fa fa-history pull-right ma"></i></a></li>
    						<li class="divider"></li>
            				<li><a class="signout">Sign Out<i class="fa fa-sign-out pull-right ma"></i></a></li>
          				</ul>
    				</li>
    			</ul>
    		</div>
  		</div>
	</nav>
	<div class="seg_progress_holder" style="display:none">
		<div class="seg_progress" style="display:none">
		</div>
	</div>
	<div class="panel panel-default" style="display:none">
  		<div class="panel-heading"></div>
  		<div class="quicktools">
  			<a class="quicksavefile" data-toggle="tooltip" title="Save"><span class="glyphicon glyphicon-floppy-disk"></span></a>
  			<a class="nextneedtocheck" data-toggle="tooltip" title="Go to next segment that needs to check"><span class="glyphicon glyphicon-arrow-right"></span></a>
  		</div>
  		<div class="panel-body">
  			<div id="sourcegrids" class="gridster">
				<h4 style="margin-bottom:4px">
					<span class="label label-default">S o u r c e</span>
				</h4>
				<div id="srccol" class="stborder">
				</div>
				<br>
			</div>
			<div id="targetgrids" class="gridster">
				<h4 style="margin-bottom:4px">
					<span class="label label-default">T a r g e t</span>
				</h4>
				<div id="trgcol" class="stborder">
				</div>
				<br>
			</div>
			<div id="moretargetgrids" class="more nvisible gridster">
				<button class="btn btn-link btn-xs more_toggle_top pull-left"></button>
				<button class="btn btn-link btn-xs more_toggle pull-left el-1x" data-toggle="tooltip" title="" data-original-title="toggle">
					<i class="el el-chevron-left arrow"></i>
				</button>
				<button class="btn btn-link btn-xs more_toggle_bot pull-left"></button>
				<h4>
					<span class="badge num_trg_more">&nbsp;&nbsp;Target Paragraphs on Next Page&nbsp;&nbsp;</span>
				</h4>
				<br>
				<div class="moretrgcolouter">
					<div id="moretrgcol" class="moretrgborder">
					</div>
				</div>
			</div>
			<div id="rmvtargetgrids" class="missing nvisible gridster">
				<button class="btn btn-link btn-xs missing_toggle_top pull-left"></button>
				<button class="btn btn-link btn-xs missing_toggle pull-left el-1x" data-toggle="tooltip" title="" data-original-title="toggle">
					<i class="el el-chevron-left arrow"></i>
				</button>
				<button class="btn btn-link btn-xs missing_toggle_bot pull-left"></button>
				<h4>
					<span class="badge num_trg_removed" data-toggle="popover" data-placement="top" data-content="You have reached the maximun number of segments that can be stored here !">0</span>
					<span class="badge divider"> / </span>
					<span class="badge num_trg_storable"></span>
				</h4>
				<br>
				<div class="rmvtrgcolouter">
					<div id="rmvtrgcol" class="rmvborder">
					</div>
				</div>
			</div>
  		</div>
	</div>
	<button id="prev_page" class="btn btn-default btn-sm" type="button" style="display:none">
		<i class="el el-play el-rotate-180"></i>
		Prev Page
	</button>
	<button id="down_page" class="btn btn-default btn-sm" type="button" style="display:none">
		&nbsp;&nbsp;
		<i class="el el-eject el-rotate-180 el-lg"></i>
		&nbsp;&nbsp;
	</button>
	<button id="next_page" class="btn btn-default btn-sm" type="button" style="display:none">
		Next Page
		<i class="el el-play"></i>
	</button>
	<footer class="footer" style="display:none">
		<div class="glyphicon glyphicon-envelope"></div><a id="emailadd">mxiang@transperfect.com</a>
		<div class="glyphicon glyphicon-tag pull-right"><span id="prjid"></span></div>
	</footer>
	
	<div id="loading" class="modal" role="dialog">
  		<div class="modal-dialog modal-sm">
				<div id="block_1" class="barlittle"></div>
				<div id="block_2" class="barlittle"></div>
				<div id="block_3" class="barlittle"></div>
				<div id="block_4" class="barlittle"></div>
				<div id="block_5" class="barlittle"></div>
				<div id="block_6" class="barlittle"></div>
				<div id="block_7" class="barlittle"></div>
				<div id="block_8" class="barlittle"></div>
				<div id="block_9" class="barlittle"></div>
    	</div>
	</div>
	<div id="message" class="modal fade" role="dialog">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-body">
        			<div class="message-text"></div>
        			<div class="download-text"></div>
      			</div>
			</div>
		</div>
	</div>
	<div id="token-modal" class="modal fade" role="dialog">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-body">
					<div class="tokencontainer">
						<h4><span class="label label-info TokenT">Token</span><input type="text" class="form-control" id="tokenstring" readonly></h4>
        				<h5>Valid Until: <span id="validtime"></span></h5>
					</div>
      			</div>
			</div>
		</div>
	</div>
	<div id="exit" class="modal fade" role="dialog">
  		<div class="modal-dialog exit-dialog">
    		<div class="modal-content">
      			<div class="modal-body">
        			<h4><span class="glyphicon glyphicon-question-sign"></span>&nbsp;&nbsp;Do you want to save before <span class="exitidf"></span> ?</h4>
      			</div>
      			<div class="modal-footer">
      				<button type="button" class="btn btn-default savebeforeexit" data-dismiss="modal">Yes</button>
        			<button type="button" class="btn btn-default exitwithoutsave" data-dismiss="modal">No</button>
      			</div>
    		</div>
  		</div>
	</div>
	<div class="search-modal-dialog" style="display:none">
    	<div class="search-modal-header">
	    	<ul class="nav nav-tabs">
				<li id="searchtab" class="active">
					<a data-toggle="tab" class="searchheadtext" href="#search">Search</a>
			    </li>
			    <li id="searchspecialtab">
					<a data-toggle="tab" class="searchheadtext" href="#special">Special</a>
			    </li>
			</ul>
			<i class="el el-remove searchclose"></i>
	    </div>
	    <hr class="search-saperator">
	    <div class="tab-content">
			<div id="search" class="tab-pane fade in active">
			  	<div class="search-modal-body">
		        	<div class="inner-addon right-addon">
			          	<i class="el el-search el-lg searchtext"></i>
			          	<input type="text" class="form-control" id="searchtext">
			        </div>
					<div class="radio">
						<label class="radio-inline"><input type="radio" name="sot" value="source">Source</label>
						<label class="radio-inline"><input type="radio" name="sot" value="target" checked>Target</label>
			      	</div>
					<div class="radio">
						<label class="radio-inline"><input type="radio" name="direction" value="forward" checked>Forward</label>
						<label class="radio-inline"><input type="radio" name="direction" value="backward">Backward</label>
			      	</div>
			      	<div class="radio">
						<label class="radio-inline"><input type="radio" name="scope" value="current">In current page</label>
						<label class="radio-inline"><input type="radio" name="scope" value="all" checked>Across all pages</label>
			      	</div>
			      	<div class="checkbox">
			      		<label><input type="checkbox" name="case" value="">Case sensitive</label>
						<label><input type="checkbox" name="wrap" value="" checked>Wrap search</label>
			      	</div>
		      	</div>
			</div>
			<div id="special" class="tab-pane fade">
				<div class="search-special-modal-body">
					<div class="inner-addon right-addon">
			          	<i class="el el-search el-lg searchspecialtext"></i>
			          	<input type="text" class="form-control" id="searchspecialtext" value="[ next not locked segment ]" disabled>
			        </div>
					<div class="radio">
						<label class="radio-inline"><input type="radio" name="special" value="nextnotlocked" checked>Next not locked</label>
						<label class="radio-inline"><input type="radio" name="special" value="nextlocked">Next locked</label>
			      	</div>
					<div class="radio">
						<label class="radio-inline"><input type="radio" name="special" value="nextinsertion">Insertion</label>
						<label class="radio-inline"><input type="radio" name="special" value="nextdeletion">Deletion</label>
						<label class="radio-inline"><input type="radio" name="special" value="nexttracked">Tracked</label>
						<label class="radio-inline"><input type="radio" name="special" value="nextneedsreview">Needs review</label>
			      	</div>
				</div>
			</div>
		</div>
		<div class="search-modal-status">Ready to search</div>
    </div>
	<div id="cover" style="display:none">
		<div id="coverholder">
			<span id="coverloading" class="wrapper">LOADING &middot;&middot;&middot;&middot;</span>
		</div>
	</div>
<script>
$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});

    $('.panel-body').scroll(function(e) {
    	var elem = $(e.currentTarget);
    	if(elem[0].scrollHeight < (elem.scrollTop() + elem.outerHeight() + 1)) {
        	$('.footer').stop().animate({'bottom':'0px'},200);
        	$('#prev_page').fadeIn();
        	$('#next_page').fadeIn();
        	$('#down_page').fadeOut();
        }else{
        	$('.footer').stop().animate({'bottom':'-30px'},0);
        	$('#prev_page').fadeOut();
        	$('#next_page').fadeOut();
        	$('#down_page').fadeIn();
        }
        var perc = ((elem.scrollTop()/(elem[0].scrollHeight-elem.outerHeight()))*100) + '%';
        $('.seg_progress').css('width', perc);
    });
});

</script>
<style>
body
{
	overflow: hidden;
	padding-top: 25px;
	padding-right: 0!important;
	padding-bottom:65px;
	margin-left:0;
	margin-right:0;
}

#cover {
    background-color:#fefefe;
    position:fixed;
    width:100%;
    height:100%;
    top:0px;
    left:0px;
    text-align:center;
    z-index:1000;
}

#coverholder {
    position:relative;
    top:25%;
    font-weight:bold;
}

#prev_page {
	display:none;
	position:fixed;
	left:15px;
	bottom:4%;
	font-weight:bold;
}

#down_page {
	display:none;
	position:fixed;
	left:15px;
	bottom:4%;
	border-style: none;
	opacity:0.2;
}

#down_page:hover { 
    opacity:1;
}

#next_page {
	display:none;
	position:fixed;
	bottom:4%;
	font-weight:bold;
}

#emailadd {
	cursor:pointer;
	color: #1E90FF;
}

.glyphicon-tag {
	color: #FFFFFF;
	position: relative;
	right: 4%;
	margin-top: 1px;
}

#prjid {
	bottom: 1px;
	position: relative;
	color: #FFFFFF;
}

.closealignsegs {
	padding: 4px;
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

.barlittle {
    background-color: #2187e7;
    background-image: -moz-linear-gradient(45deg, #000 25%, #000);
    background-image: -webkit-linear-gradient(45deg, #000 25%, #000);
    border-left: 1px solid #444;
    border-top: 1px solid #444;
    border-right: 1px solid #444;
    border-bottom: 1px solid #444;
    width: 15px;
    height: 15px;
    display:inline-block;
    opacity: 0.1;
    -moz-transform: scale(0.7);
    -webkit-transform: scale(0.7);
    -moz-animation: move 1s infinite linear;
    -webkit-animation: move 1s infinite linear;
}

#block_1 {
    -moz-animation-delay: .6s;
    -webkit-animation-delay: .6s;
}

#block_2 {
    -moz-animation-delay: .5s;
    -webkit-animation-delay: .5s;
}

#block_3 {
    -moz-animation-delay: .4s;
    -webkit-animation-delay: .4s;
}

#block_4 {
    -moz-animation-delay: .3s;
    -webkit-animation-delay: .3s;
}

#block_5 {
    -moz-animation-delay: .2s;
    -webkit-animation-delay: .2s;
}

#block_6 {
    -moz-animation-delay: .3s;
    -webkit-animation-delay: .3s;
}

#block_7 {
    -moz-animation-delay: .4s;
    -webkit-animation-delay: .4s;
}

#block_8 {
    -moz-animation-delay: .5s;
    -webkit-animation-delay: .5s;
}

#block_9 {
    -moz-animation-delay: .6s;
    -webkit-animation-delay: .6s;
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

#loading {
	opacity: 0.5;
	z-index:1001;
}

#loading > .modal-dialog {
	top:33%;
	z-index:1001;
}

.message-good{
	color: #088A68;
}

.message-bad{
	color: #FF4000;
}

.modal-dialog{
	position: fixed;
	top:30%;
	left:35%;
	width: 420px;
	height:5%;
	opacity: 0.9;
	text-align:center;
}

.alnsegdialog{
	position: fixed;
	top:20%;
	left:25%;
	width: 780px;
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

.modal-open {
  overflow-y: scroll;
}

.navbar {
	margin-bottom: 0px;
}

.panel {
	width:100%;
	position: relative;
	overflow-x: hidden;
	overflow-y: hidden;
}

.seg_progress_holder {
	position: relative;
	height: 10px;
	max-height: 620px;
	overflow-x: hidden;
	overflow-y: scroll !important;
	background-color:#d5d8dc;
}

.seg_progress {
	height: 10px;
	width: 0%;
	margin-left: -3px;
	background-color:#888888;
	transform: skewX(-30deg);
}

.seg_btn_menu {
	position:fixed;
	top:56px;
	right:16px;
}

.quit_align_seg {
	border-radius: 0 !important;
	padding-top: 8px;
}

.more_toggle_top {
	position:absolute;
	top:33%;
	left:-1%;
	background:#444;
	padding:0px;
	height:15%;
	width:2%;
	-ms-transform: skewY(-60deg); /* IE 9 */
    -webkit-transform: skewY(-60deg); /* Safari */
    transform: skewY(-60deg); /* Standard syntax */
}

.more_toggle_bot {
	position:absolute;
	top:37%;
	left:-1%;
	background:#444;
	padding:0px;
	height:15%;
	width:2%;
	-ms-transform: skewY(60deg); /* IE 9 */
    -webkit-transform: skewY(60deg); /* Safari */
    transform: skewY(60deg); /* Standard syntax */
}

.more_toggle {
	position:absolute;
	top:35%;
	left:-1%;
	background:#444;
	color:#fff;
	padding:0px;
	height:15%;
	width:2%;
	z-index:2;
}

.more_toggle:hover, .more_toggle:focus, .more_toggle:active, .more_toggle.active, .open>.dropdown-toggle.more_toggle {
    background:#444;
}

.more_toggle_top:hover, .more_toggle_top:focus, .more_toggle_top:active, .more_toggle_top.active, .open>.dropdown-toggle.more_toggle_top {
    background:#444;
}

.more_toggle_bot:hover, .more_toggle_bot:focus, .more_toggle_bot:active, .more_toggle_bot.active, .open>.dropdown-toggle.more_toggle_bot {
    background:#444;
}

.missing_toggle_top {
	position:absolute;
	top:33%;
	left:-1%;
	background:#222;
	padding:0px;
	height:15%;
	width:2%;
	-ms-transform: skewY(-60deg); /* IE 9 */
    -webkit-transform: skewY(-60deg); /* Safari */
    transform: skewY(-60deg); /* Standard syntax */
}

.missing_toggle_bot {
	position:absolute;
	top:37%;
	left:-1%;
	background:#222;
	padding:0px;
	height:15%;
	width:2%;
	-ms-transform: skewY(60deg); /* IE 9 */
    -webkit-transform: skewY(60deg); /* Safari */
    transform: skewY(60deg); /* Standard syntax */
}

.missing_toggle {
	position:absolute;
	top:35%;
	left:-1%;
	background:#222;
	color:#fff;
	padding:0px;
	height:15%;
	width:2%;
	z-index:2;
}

.missing_toggle:hover, .missing_toggle:focus, .missing_toggle:active, .missing_toggle.active, .open>.dropdown-toggle.missing_toggle {
    background:#222;
}

.missing_toggle_top:hover, .missing_toggle_top:focus, .missing_toggle_top:active, .missing_toggle_top.active, .open>.dropdown-toggle.missing_toggle_top {
    background:#222;
}

.missing_toggle_bot:hover, .missing_toggle_bot:focus, .missing_toggle_bot:active, .missing_toggle_bot.active, .open>.dropdown-toggle.missing_toggle_bot {
    background:#222;
}

.approve_align_seg {
	border-radius: 0 !important;
	padding-top: 8px;
}

.seg_locked {
	color:#13bd9c;
}

.panel-heading{
	position: relative;
	padding:10px;
}

.quicktools{
	background-color:#e1eaef;
	position: relative;
	padding:5px;
	padding-left:15px;
	font-size:15px;
}

.panel-body{
	position: relative;
	height:91.5%;
	overflow-x:hidden;
	overflow-y:scroll;
	padding-bottom:225px;
}

.missing h4 {
	text-align: center;
}

.more h4 {
	text-align: center;
}

.form-control:focus {
    border-color: #ffffff;
    box-shadow: 0 0 10px #ffffff;
}

#tokenstring {
	display:inline-block !important;
	cursor: text;
	height:23px;
	margin-left:5px;
	width:80%;
}

#validtime{
	font-weight: bold;
}

.tokencontainer {
	width:80%;
	margin:auto;
	text-align:left;
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


.missing {
	position: fixed;
    top: 55px;
    right: -33%;
    width:35%;
    height:95%;
    background-color: #222;
    border-style: solid;
    border-color: #666;
    border-width: 1px;
    padding-top:20px;
    padding-bottom:20px;
    float:right;
    opacity: 1;
    z-index: 2;
}

.rmvtrgcolouter{
	height: 97%;
	overflow: auto;
}

.more {
	position: fixed;
    top: 55px;
    right: -30%;
    width:33%;
    height:95%;
    background-color: #444;
    border-style: solid;
    border-color: #666;
    border-width: 1px;
    padding-top:20px;
    padding-bottom:20px;
    float:right;
    opacity: 1;
    z-index: 2;
}

.moretrgcolouter{
	height: 97%;
	overflow: auto;
}

.extranav0 {
	z-index: 11;
	position: fixed;
    top: 70px;
    right:30px;
    width:23%;
    height:60px;
    background-color:#222;
    border-style: solid;
    border-color: #222;
    border-width: 1px;
    padding:10px;
    float:right;
}
.extranav1 {
	z-index: 10;
	position: fixed;
    top: 30px;
    right:-60px;
    width:50%;
    height:100px;
    background-color:#222;
    border-style: solid;
    border-color: #222;
    border-width: 1px;
    padding:10px;
    float:right;
    -webkit-transform: rotate(0deg) skew(45deg); 
    transform: rotate(0deg) skew(45deg);
}

.divider {
	background: #222;
}

.glyphicon-envelope {
	color:#fff;
	padding-right: 6px;
}

.glyphicon-ok-sign {
	top:3px;
}

.glyphicon-exclamation-sign{
	top:3px;
}

.glyphicon-question-sign{
	top:3px;
}

.modal-open {
	overflow-y:hidden;
}

.mprj{
	top:4px;
}

.dl{
	top:4px;
}

.tl{
	top:4px;
}

.ma{
	margin-top:3px;
}

.dropdown-submenu {
    position: relative;
}

.dropdown-submenu>.dropdown-menu {
    top: 0;
    left: 100%;
    margin-top: -6px;
    margin-left: 0px;
}

.dropdown-submenu:hover>.dropdown-menu {
    display: block;
    width: 200px !important;
}

.dropdown-submenu>a:after {
    display: block;
    content: " ";
    float: right;
    width: 0;
    height: 0;
    border-color: transparent;
    border-style: solid;
    border-width: 5px 0 5px 5px;
    border-left-color: #ccc;
    margin-top: 5px;
    margin-right: -10px;
}

.dropdown-submenu:hover>a:after {
    border-left-color: #fff;
}

.dropdown-submenu.pull-left {
    float: none;
}

.dropdown-submenu.pull-left>.dropdown-menu {
    left: -100%;
    margin-left: 10px;
}

.radio {
	margin: 6px;
	border-bottom-style: solid;
	border-bottom-width:thin;
	border-bottom-color:#cccccc;
	padding-left:6px;
	padding-top:3px;
}

.radio input {
	margin-bottom: 2px;
	bottom: 1px;
}

.checkbox {
	margin: 6px;
	margin-top:-3px;
	border-bottom-style: solid;
	border-bottom-width:thin;
	border-bottom-color:#cccccc;
	padding-left:6px;
	padding-top:3px;
}

.checkbox input {
	margin-bottom: 2px;
	bottom: 1px;
}

.checkbox label {
	margin-right: 10px;
}

.TokenT {
	padding-top:4px;
}

.quicksavefile {
	margin-right: 10px;
}

</style>
</body>
</html>