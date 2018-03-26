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
<script type="text/javascript" src="../js/bootstrap.min.js"></script>
<script type="text/javascript" src="../js/jquery.gridster.js"></script>
	
<script type="text/javascript" src="../js/jquery.simplePagination.js"></script>
<script type="text/javascript" src="../js/aligner.js"></script>
<script src="../js/heartcode-canvasloader-min.js"></script>
<script type="text/javascript">
    $(function(){      
      var prjid;
      var srclang;
      var trglang;
      var empty_next_index = 0;
	  var gridster_src;
	  var gridster_sub_src;
	  var gridster_trg;
	  var gridster_sub_trg;
	  var gridster_rmv_trg;
	  var gridster_more_trg;
	  var sources = [];
	  var sources_tctypes = [];
	  var targets = [];
	  var removed_targets = [];
	  var locked_targets = [];
	  var seg_aligned_targets = [];
	  var src_segs = {};
	  var src_segs_tctypes = {};
	  var trg_segs = {};
	  
	  var maxSegPerPage = 30;
	  var maxSubSegPerPage = 20;
	  var total_storable_removed_trgs = 50;
	  var total_pre_loadable_trgs = 20;
	  var timeOutCheckInterval = 60000;
	  var intervalId;
	  
	  var currPageNum = 1;
	  var lastPageNum;
	  var lastPageSegNum;
	  var draggableoldrow;
	  
	  var currentSegAlign;
	  var currentEditedText;
	  
	  var windowWidth = window.innerWidth;
	  var windowHeith = window.innerHeight;
	  
	  $("#cover").show();
	  var cl0 = new CanvasLoader('coverloading'); 
	  cl0.setShape('spiral');
	  cl0.setDiameter(90);
	  cl0.setColor('#bbbbbb');
	  cl0.setDensity(100);
	  cl0.setFPS(50);
	  cl0.show();
	  
	  var cl1 = new CanvasLoader('sessiontimeoutloading');
	  cl1.setShape('spiral');
	  cl1.setDiameter(16);
	  
	  var test = "${prjnum}";
	  if(test != '' && test != null){
	  	  prjid = test;
	  }
	  
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
	        	    intervalId = setInterval(function(){validateSession()}, timeOutCheckInterval);
	          },
	          
	          error: function(xhr) {
					alert('failed to read App base configuration, using default...');
					intervalId = setInterval(function(){validateSession()}, timeOutCheckInterval);
	            }
	    });
	  
	    $(window).on('beforeunload', function(){
			if(typeof prjid == 'undefined') {
				return;
			}
			$.ajax({
				url: '/RevAligner/rac/cancelsession',
				async: false,
				type: "GET",
					                 
				beforeSend: function(xhr) {
					xhr.setRequestHeader("Accept", "application/json");
					xhr.setRequestHeader("Content-Type", "application/json");
				},
		                 
		        success: function() {
			    	return "";
		        },
		              
		        error: function() {
		           	return "";
		        }
		   });
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
	  
	  $('.num_trg_storable').text(total_storable_removed_trgs);
	  $('#moretrgcol').addClass('notransition');
	  
	  srclang = "${srclang}";
	  trglang = "${trglang}";
	  empty_next_index = "${nullcnt}";
		<c:forEach items="${s_map}" var="listItem">
  			sources.push("<c:out value="${listItem.value[1]}" />" + "#" + "<c:out value="${listItem.value[0]}" />");
  		    sources_tctypes.push("<c:out value="${listItem.value[2]}" />");
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
					$('.panel_seg').show();
					$('.footer').show();
					var next_page_lt = $('#targetgrids').position().left + $('#targetgrids').width() - $('#next_page').width() - 15;
	    			$('#next_page').css('left',next_page_lt+'px');
	    			
	    			var down_page_lt = ($('#sourcegrids').position().left + $('#sourcegrids').width() + $('#targetgrids').position().left - $('#down_page').width() - 15)/2;
	    			$('#down_page').css('left',down_page_lt+'px');
	    			$('#down_page').show();
	    			
	    			//cl0.hide();
	    			$('#cover').fadeOut();
	    					
					var elem = $('.panel-body');
				    if(elem[0].scrollHeight == (elem.scrollTop() + elem.outerHeight())) {
				        $('.footer').stop().animate({'bottom':'0px'},200);
				    }
				}
	          },
	          
	          error: function() {
					redirectwithpost('/RevAligner/rac/sessiontimesout');
	        }
	  });
	
		<c:forEach items="${t_map}" var="listItem">
  			targets.push("<c:out value="${listItem.value[1]}" />" + "#" + "<c:out value="${listItem.value[0]}" />");
		</c:forEach>
		
		<c:forEach items="${m_map}" var="listItem">
  			removed_targets.push("<c:out value="${listItem.value[1]}" />" + "#" + "<c:out value="${listItem.value[0]}" />");
		</c:forEach>
			
		<c:forEach items="${src_segs_map}" var="listItem">
  			var id = "<c:out value="${listItem.value[1]}" />";
  			var segstring = "<c:out value="${listItem.value[0]}" />";
  			var segs = segstring.split('|||');
  			src_segs[id] = segs;
  			
  			var s_types = "<c:out value="${listItem.value[2]}" />";
  			var types = s_types.split('|||');
  			src_segs_tctypes[id] = types;
		</c:forEach>

		<c:forEach items="${trg_segs_map}" var="listItem">
  			var s = "<c:out value="${listItem.value[1]}" />" + "#" + "<c:out value="${listItem.value[0]}" />";
  			var id = getSeq(s);
  			var segstring = getHtmlText(s);
  			var segs = segstring.split('|||');
  			trg_segs[id] = segs;
		</c:forEach>
			
		<c:forEach items="${lock_para_seq_map}" var="listItem">
  			locked_targets.push("<c:out value="${listItem.value}" />");
		</c:forEach>
			
		<c:forEach items="${aligned_para_seq_map}" var="listItem">
  			seg_aligned_targets.push("<c:out value="${listItem.value}" />");
		</c:forEach>
		
		//source grid template
	    $combo_source = $('<div id="srcseg" class="src_segment" />');
		menu = $('<menu />');
		drag = $('<drag class="text" />');
		segid = $('<span class="label label-default segid" data-toggle="tooltip" title="Paragraph ID"/>');
		segtctype = $('<span class="label label-info segtctype"/>');
		buttonexpand = $('<button type="button" class="btn btn-link btn-xs expand" data-toggle="tooltip" title="Expand cell" />').append($('<i class="el el-text-height" />'));
		buttonviewall = $('<button type="button" class="btn btn-link btn-xs viewall" data-toggle="tooltip" title="Show all tracks" />').append($('<i class="el el-eye-open" />'));
		buttonaccept = $('<button type="button" class="btn btn-link btn-xs accept" data-toggle="tooltip" title="Accept changes" />').append($('<i class="el el-ok-sign" />'));
		buttonreject = $('<button type="button" class="btn btn-link btn-xs reject" data-toggle="tooltip" title="Reject changes" />').append($('<i class="el el-remove-sign" />'));
		menu.append(segid)
			.append(segtctype)
			.append(buttonexpand)
			.append(buttonviewall)
			.append(buttonaccept)
			.append(buttonreject);
		$combo_source.append(menu)
				.append(drag);
		
		//sub source grid template
	    $combo_sub_source = $('<div id="subsrcseg" class="src_segment" />');
		menu = $('<submenu />');
		drag = $('<drag class="subtext" />');
		segid = $('<span class="label label-default subsegid"  data-toggle="tooltip" title="Segment ID"/>');
		segtctype = $('<span class="label label-info subsegtctype"/>');
		buttonviewall = $('<button type="button" class="btn btn-link btn-xs viewall" data-toggle="tooltip" title="Show all tracks" />').append($('<i class="el el-eye-open" />'));
		buttonaccept = $('<button type="button" class="btn btn-link btn-xs accept" data-toggle="tooltip" title="Accept changes" />').append($('<i class="el el-ok-sign" />'));
		buttonreject = $('<button type="button" class="btn btn-link btn-xs reject" data-toggle="tooltip" title="Reject changes" />').append($('<i class="el el-remove-sign" />'));
		menu.append(segid)
			.append(segtctype)
			.append(buttonviewall)
			.append(buttonaccept)
			.append(buttonreject);
		$combo_sub_source.append(menu)
				.append(drag);
		
		//target grid template
	    $combo_target = $('<div id="trgseg" class="trg_segment" />');
		menu = $('<menu />');
		drag = $('<drag class="text trgtext" />');
		segid = $('<span class="label label-default segid"  data-toggle="tooltip" title="Paragraph ID"/>');
		buttonremovel = $('<button type="button" class="btn btn-link btn-xs removegrid" data-toggle="tooltip" title="Remove temporarily" />').append($('<i class="el el-trash" />'));
		buttoninsbelow = $('<button type="button" class="btn btn-link btn-xs insertbelow" data-toggle="tooltip" title="Insert Below" />').append($('<i class="el el-inbox" />'));
		buttoninsabove = $('<button type="button" class="btn btn-link btn-xs insertabove" data-toggle="tooltip" title="Insert Above" />').append($('<i class="el el-inbox el-rotate-180" />'));
		buttonconclean = $('<button type="button" class="btn btn-link btn-xs clean" data-toggle="tooltip" title="Clear The Content" />').append($('<i class="el el-broom" />'));
		buttonedit = $('<button type="button" class="btn btn-link btn-xs edit" data-toggle="tooltip" title="Edit Text" />').append($('<i class="el el-edit" />'));
		buttonconfirm = $('<button type="button" class="btn btn-link btn-xs approve" data-toggle="tooltip" title="Not Approved" />').append($('<i class="el el-unlock" />'));
		buttonconalignsegs = $('<button type="button" class="btn btn-link btn-xs alignsegs" data-toggle="tooltip" title="Align Segments" />').append($('<i class="el el-align-justify" />'));
		menu.append(segid)
			.append(buttonremovel)
			.append(buttoninsabove)
			.append(buttoninsbelow)
			.append(buttonconclean)
			.append(buttonedit)
			.append(buttonconfirm)
			.append(buttonconalignsegs);
		$combo_target.append(menu)
				.append(drag);
		
		//target grid template
	    $combo_sub_target = $('<div id="subtrgseg" class="sub_trg_segment" />');
		menu = $('<submenu />');
		drag = $('<drag class="subtext subtrgtext" contenteditable="false">');
		segid = $('<span class="label label-default subsegid"  data-toggle="tooltip" title="Segment ID"/>');
		buttonremovel = $('<button type="button" class="btn btn-link btn-xs subremovegrid" data-toggle="tooltip" title="Discard" />').append($('<i class="el el-trash" />'));
		buttonedit = $('<button type="button" class="btn btn-link btn-xs subedit" data-toggle="tooltip" title="Edit Text" />').append($('<i class="el el-edit" />'));
		menu.append(segid)
			.append(buttonremovel)
			.append(buttonedit)
		$combo_sub_target.append(menu)
				.append(drag);
		
		//removed target grid template
		$combo_rmv = $('<div id="rmvtrgseg" class="rmv_trg_segment" />');
		menu = $('<rmvmenu />');
		drag = $('<drag class="rmvtext" />');
		segid = $('<span class="label label-default segid"  data-toggle="tooltip" title="Paragraph ID"/>');
		buttonremovel = $('<button type="button" class="btn btn-link btn-xs permovegrid" data-toggle="tooltip" title="Discard" />').append($('<i class="el el-remove" />'));
		buttonrestore = $('<button type="button" class="btn btn-link btn-xs restore" data-toggle="tooltip" title="Restore" />').append($('<i class="el el-return-key" />'));
		menu.append(segid)
			.append(buttonremovel)
			.append(buttonrestore);
		$combo_rmv.append(menu)
			.append(drag);
		
		//more target grid template
		$combo_more = $('<div id="moretrgseg" class="more_trg_segment" />');
		menu = $('<moretrgmenu />');
		drag = $('<drag class="moretrgtext" />');
		segid = $('<span class="label label-default segid"  data-toggle="tooltip" title="Paragraph ID"/>');
		buttonrestore = $('<button type="button" class="btn btn-link btn-xs moretrgrestore" data-toggle="tooltip" title="Move" />').append($('<i class="el el-return-key" />'));
		buttonconfirm = $('<button type="button" class="btn btn-link btn-xs moretrgapprove" data-toggle="tooltip" title="Not Approved" />').append($('<i class="el el-lock" />'));
		menu.append(segid)
			.append(buttonrestore)
			.append(buttonconfirm);
		$combo_more.append(menu)
			.append(drag);
	  
	  var wd = $(window).width()/3.4;
	  var wd_sub = $(window).width()/3.2;
	  var wd_missing = $(window).width()/3.3;
	  var wd_more = $(window).width()/3.5;
	  var ht = $(window).height()/6.5;
	  var ht_sub = $(window).height()/9.74;
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
      
      gridster_sub_src = $("#subsourcegrids > div").gridster({
      	  namespace: '#subsourcegrids',
      	  widget_selector: "div",
          widget_margins: [mg_h, mg_v],
          widget_base_dimensions: [wd_sub, ht_sub],
          autogenerate_stylesheet: false,
      }).data('gridster').disable();
      generateStyleSheet("#subsourcegrids", ht_sub, wd_sub, mg_h, mg_v, maxSubSegPerPage);
      //gridster_sub_src.generate_stylesheet({rows: maxSubSegPerPage, cols: 1});
      
      gridster_trg = $("#targetgrids > div").gridster({
      	  namespace: '#targetgrids',
      	  widget_selector: "div",
          widget_margins: [mg_h, mg_v],
          widget_base_dimensions: [wd, ht],
          autogenerate_stylesheet: false,
          draggable: {
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
					//if(id == "trgseg"){
        				var oldindex = (currPageNum-1)*maxSegPerPage + parseInt(draggableoldrow) - 1;
        				var newindex = (currPageNum-1)*maxSegPerPage + parseInt(newrow) - 1;
        				var temptext = targets[oldindex];
        				targets[oldindex] = targets[newindex];
        				targets[newindex] = temptext;
        			/*}else if(id == "subtrgseg"){
        				var oldindex = parseInt(draggableoldrow) - 1;
        				var newindex = parseInt(newrow) - 1;
        				
        				var index = (currPageNum-1)*maxSegPerPage + currentSegAlign - 1;
  						var trgseq = getSeq(targets[index]);
						var t_segs = trg_segs[trgseq];
        				var temptext = t_segs[oldindex];
        				t_segs.splice(oldindex,1);
        				t_segs.splice(newindex,0,temptext);
        			}*/
            	}
            	window.scrollTo(0, 0);
        	}
          }
      }).data('gridster');
      generateStyleSheet("#targetgrids", ht, wd, mg_h, mg_v, maxSegPerPage);
      //gridster_trg.generate_stylesheet({rows: maxSegPerPage, cols: 1});
      
      gridster_sub_trg = $("#subtargetgrids > div").gridster({
      	  namespace: '#subtargetgrids',
      	  widget_selector: "div",
          widget_margins: [mg_h, mg_v],
          widget_base_dimensions: [wd_sub, ht_sub],
          shift_larger_widgets_down: false,
          autogenerate_stylesheet: false,
      }).data('gridster');
      generateStyleSheet("#subtargetgrids", ht_sub, wd_sub, mg_h, mg_v, maxSubSegPerPage);
      //gridster_sub_trg.generate_stylesheet({rows: maxSubSegPerPage, cols: 1});
      
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
    		
    		$(".panel-body").scrollTop(0);
    					
    		currPageNum = pageNumber;

    		gridster_src.remove_all_widgets();

    		gridster_trg.remove_all_widgets();
    		
    		gridster_more_trg.remove_all_widgets();

    		$('#srccol').empty();

    		$('#trgcol').empty();
    		
    		$('#moretrgcol').empty();
    		$('#moretrgcol').height("97%");

			for (i = 0; i < maxSegPerPage; i++) {
				var idx = (pageNumber-1)*maxSegPerPage + i;
				if(idx <= (sources.length - 1)) {
					var comboclone = $combo_source.clone(true);
					comboclone.find('.segid').text(getSeq(sources[idx]));
					var tp = sources_tctypes[idx];
					var label = comboclone.find('.segtctype');
					$(label).addClass("tp"+tp);
					label.text(sources_tctypes[idx]);
					comboclone.find('drag').html(getHtmlText(sources[idx]));
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
					$(icon).attr('title','Approved');
					$(this).addClass("lock");
					$(this).addClass("static");
					$(this).find(":not(button.approve):not(button.alignsegs)").prop("disabled", true);
					$(this).find('.text').css("background-color", "#13bd9c");
					$(this).find('.text').css("color", "#DBFEF8");
  				}
  				
  				if($.inArray(seq, seg_aligned_targets) !== -1){
  					var icon_align = $(this).find('.alignsegs').children('.el')[0];
  					$(icon_align).addClass("seg_approved");
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
	    	
			$('.tpDELETION').tooltip({container: 'body', title: "Deletion will not be in the TXLF"});
			$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
			
			enableEvents();
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
		
		$('body').on('click', '.approve', function(e) {
			$(this).tooltip('destroy');
			var icon = $(this).children('.el')[0];
			var trgseg = $(this).closest('.trg_segment');
			var icon_align = $(trgseg.find('.alignsegs')[0]).children(".el")[0];
			
			var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
  			var seq = getSeq(sources[index]);
  			
			if($(icon).hasClass("el-unlock")) {
				locked_targets.push(seq);
				$(icon).removeClass("el-unlock");
				$(icon).addClass("el-lock");
				$(this).attr('title','Approved');
				$(trgseg).addClass("lock");
				$(trgseg).addClass("static");
				$(trgseg).find(":not(button.approve):not(button.alignsegs)").prop("disabled", true);
				$(trgseg).find('.text').css("background-color", "#13bd9c");
				$(trgseg).find('.text').css("color", "#DBFEF8");
			}else {
				locked_targets.splice($.inArray(seq, locked_targets), 1);
				$(icon).removeClass("el-lock");
				$(icon).addClass("el-unlock");
				$(this).attr('title','Not approved');
				$(trgseg).removeClass("lock");
				$(trgseg).removeClass("static");
				$(trgseg).find(":not(button.approve):not(button.alignsegs)").prop("disabled", false);
				$(trgseg).find('.text').css("background-color", "");
				$(trgseg).find('.text').css("color", "");
				
				seg_aligned_targets.splice($.inArray(seq, seg_aligned_targets), 1);
				$(icon_align).removeClass("seg_approved");
			}
			$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
		});
		
		$('body').on('click', '.subedit', function(e) {
			gridster_sub_trg.disable();
			currentEditedText = $(this).closest('.sub_trg_segment').find('.subtext').html();
			$(this).css("color", "2a6496");
			var textarea = $(this).closest('.sub_trg_segment').find('.subtext');
          	textarea.attr("contenteditable","true");
          	textarea.css("cursor", "text");
          	textarea.css("background-color", "ccffff");
          	textarea.css("color", "2a6496");
          	textarea.focus();
		});
		
		$('body').on('dblclick', '.subtrgtext', function(e) {
			gridster_sub_trg.disable();
			currentEditedText = $(this).closest('.sub_trg_segment').find('.subtext').html();
			$(this).closest('.sub_trg_segment').find('.subedit').css("color", "2a6496");
          	$(this).attr("contenteditable","true");
          	$(this).css("cursor", "text");
          	$(this).css("background-color", "ccffff");
          	$(this).css("color", "2a6496");
          	$(this).focus();
		});
		
		$('body').on('click', '.subremovegrid', function(e) {
			disableEvents();
			
			$(this).tooltip('destroy');
			var trgsubseg = $(this).closest('.sub_trg_segment');
          	var index = (currPageNum-1)*maxSegPerPage + currentSegAlign - 1;
  			var trgseq = getSeq(targets[index]);
			var subindex = parseInt(trgsubseg.attr("data-row"))-1;
			
			var newparatext = updateSegText(index,subindex,"");
			(trg_segs[trgseq]).splice(subindex,1);

			var src_cnt = $('#subsrccol').children('#subsrcseg').length;
			var trg_cnt = $('#subtrgcol').children('#subtrgseg').length;
			
			if(src_cnt < trg_cnt){
				gridster_sub_trg.remove_widget(trgsubseg);
				$( ".sub_trg_segment" ).each(function() {
	  				$(this).find('.subsegid').text(parseInt($(this).attr("data-row"))-1);
				});
				var he = $(".panel_seg").height() - 2 * mg_v - ht_sub;
  				var hes = he.toString() + "px";
				$(".panel_seg").animate({
					height: hes
				}, 400);
			}else{
				gridster_sub_trg.remove_widget(trgsubseg);
				$( ".sub_trg_segment" ).each(function() {
	  				$(this).find('.subsegid').text(parseInt($(this).attr("data-row"))-1);
				});
				(trg_segs[trgseq]).splice((trg_cnt-1).toString(),0,"");
				var comboclone = $combo_sub_target.clone(true);
				comboclone.find('.subsegid').text((trg_cnt-1).toString());
				comboclone.find('drag').html("");
    			gridster_sub_trg.add_widget(comboclone,1,1,1,trg_cnt);
			}
			
            targets[index] = trgseq + "#" + newparatext;
            var trgseg = $( ".trg_segment[data-row=" + currentSegAlign + "]" );
            trgseg.find('.text').html(newparatext);
            
            enableEvents();
		});
		
		$(document).on('paste','.subtrgtext',function(e) {
		    e.preventDefault();
		    var text = (e.originalEvent || e).clipboardData.getData('text/plain') || prompt('Paste something..');
		    window.document.execCommand('insertText', false, text);
		});
		
		$('body').on('blur', '.subtrgtext', function(e) {
			disableEvents();
			
			gridster_sub_trg.enable();
          	$(this).attr("contenteditable","false");
          	$(this).css("cursor", "move");
          	$(this).css("background-color", "");
          	$(this).css("color", "444");
          	$(this).closest('.sub_trg_segment').find('.subedit').css("color", "");
          	$(this).html($(this).html().replace(/&nbsp;/g,'').replace(/(<br>)+$/,''));
          	if($(this).html() == currentEditedText){
          		enableEvents();
          		return;
          	}
          	
          	var subtrgseg = $(this).closest('.sub_trg_segment');
          	var index = (currPageNum-1)*maxSegPerPage + currentSegAlign - 1;
  			var trgseq = getSeq(targets[index]);
			var subindex = parseInt(subtrgseg.attr("data-row"))-1;
			var temptext = $(this).html().replace(/&lt;/g,'&amp;lt;').replace(/&gt;/g,'&amp;gt;').replace(/</g,'&lt;').replace(/>/g,'&gt;');;
			var newparatext = updateSegText(index,subindex,temptext);
            targets[index] = trgseq + "#" + newparatext;
            var trgseg = $( ".trg_segment[data-row=" + currentSegAlign + "]" );
            trgseg.find('.text').html(newparatext);
            
            enableEvents();
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
          	$(this).css("background-color", "");
          	$(this).css("color", "444");
          	$(this).closest('.trg_segment').find('.edit').css("color", "");
          	$(this).html($(this).html().replace(/&nbsp;/g,' ').replace(/(<br>)+$/,''));
          	if($(this).html() == currentEditedText){
          		enableEvents();
          		return;
          	}
          			
			var trgseg = $(this).closest('.trg_segment');
			var index = (currPageNum-1)*maxSegPerPage + parseInt(trgseg.attr("data-row"))-1;
			var temptext = $(this).html().replace(/&lt;/g,'&amp;lt;').replace(/&gt;/g,'&amp;gt;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
			var seq = getSeq(targets[index]);
			targets[index] = (seq + "#" + temptext);
			
			if(temptext){
				$.ajax({
	            	url: '/RevAligner/rac/updateparagraph',
	            	type: "POST",
	            	data: encodeURIComponent(temptext),
	                 
	              	beforeSend: function(xhr) {
	                	xhr.setRequestHeader("Accept", "application/json");
	                	xhr.setRequestHeader("Content-Type", "application/json");
	              	},
	                 
	              	success: function(data) {
	              		var newsegs = [];
	              		var iii = 0;
	              	  	while (typeof data.trgsegs[iii] !== "undefined"){
	              	  		newsegs[iii] = data.trgsegs[iii];
	              	  	  	iii++;
	              	  	}
	              	  	trg_segs[seq] = newsegs;
	              	},
	              
	              	error: function() {
	              	  	alert('failed to update paragraph');
	              	}
	        	});
			}else{
				trg_segs[seq] = [];
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
    					insertHtmlAtCaret("<br><br>");
    				}else{
    					insertHtmlAtCaret("<br>");
    				}
    			}else{
    				event.preventDefault();
        			event.target.blur();
    			}
    			event.stopPropagation();
    		}
    		
    		enableEvents();
		});
		
		$("body").on("keypress",".subtext", function(){
			disableEvents();
			
    		var keycode = (event.keyCode ? event.keyCode : event.which);
    		if(keycode == '13'){
    			if(event.shiftKey){
    				event.preventDefault();
    				if((getCaretCharacterOffsetWithin(this) == $(this).text().length) && (!$(this).html().match(/<br>$/))){
    					insertHtmlAtCaret("<br><br>");
    				}else{
    					insertHtmlAtCaret("<br>");
    				}
    			}else{
    				event.preventDefault();
        			event.target.blur();
    			}
    			event.stopPropagation();
    		}
    		
    		enableEvents();
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
  					    	$('[data-toggle="popover"]').popover({container: 'body', trigger: 'manual', delay: {show: 0, hide: 0}});
  					    	$('[data-toggle="popover"]').popover('show');
  							setTimeout(function(){$('[data-toggle="popover"]').popover('hide');}, 3000);
  						}, 300);
  					} else {
  						var tempseq = "n - " + empty_next_index;
		  				var temptext = "n - " + empty_next_index + "#";
		  				empty_next_index++;
  						trgseg.find('.segid').text(tempseq);
						trgseg.find('drag').html('');
  						removed_targets.unshift(targets[index]);
  						targets[index] = temptext;
  						trg_segs[tempseq] = [];

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
  						trg_segs[tempseq] = [];
  					}
  				}
  			}
  			
  			enableEvents();
		});
		
        $('body').on('click', '.removegrid', function(e) {
        	disableEvents();
        	
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
  				nextsegtext = getHtmlText(targets[currPageNum*maxSegPerPage-locked.length]);
  				nextsegseq = getSeq(targets[currPageNum*maxSegPerPage-locked.length]);
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
						
						//keeplocks(locked, locked_contents, locked_indices);
						
						if(orgtrgcnt <= sources.length){
    						targets.push("n - " + empty_next_index + "#" + "");
    						empty_next_index++;
    						trg_segs[nextsegseq] = [];
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

							//determine if add more trg paragraph
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
    						
    						keeplocks(locked, locked_more, locked_contents, locked_indices);
						//},300);					
  					} else {
  						trash.click();
  						$('.num_trg_removed').css("color","#d00000");
  					}
  				}else{
  					$(this).tooltip('destroy');
  					targets.splice(index,1);
					gridster_trg.remove_widget(trgseg);
					
					//keeplocks(locked, locked_contents, locked_indices);
					
					if(orgtrgcnt <= sources.length){
    					targets.push("n - " + empty_next_index + "#" + "");
    					empty_next_index++;
    					trg_segs[nextsegseq] = [];
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
    						
    					keeplocks(locked, locked_more, locked_contents, locked_indices);
					//},300);	
  				}
  			} else if($(trash).hasClass("lightup")) {
  				if(text) {
  					if(removed_targets.length >= total_storable_removed_trgs) {
  						$('.missing').animate({scrollTop: 0 }, 100);
  						setTimeout(function(){
  						 	$('.num_trg_removed').css("color","#d00000");
  					    	$('[data-toggle="popover"]').popover({container: 'body', trigger: 'manual', delay: {show: 0, hide: 0}});
  					    	$('[data-toggle="popover"]').popover('show');
  							setTimeout(function(){$('[data-toggle="popover"]').popover('hide');}, 3000);
  						}, 300);
  					} else {
  						$(this).tooltip('destroy');
  						removed_targets.unshift(targets[index]);
  						targets.splice(index,1);
  						gridster_trg.remove_widget(trgseg);
  						
  						//keeplocks(locked, locked_contents, locked_indices);
  						
  						if(orgtrgcnt <= sources.length){
    						targets.push("n - " + empty_next_index + "#" + "");
    						empty_next_index++;
    						trg_segs[nextsegseq] = [];
    					}

  						$('.missing').animate({scrollTop: 0 }, 100);
  						//setTimeout(function(){
  							var comboclone = $combo_rmv.clone(true);
    						gridster_rmv_trg.add_widget(comboclone,1,1,1,1);
							comboclone.find('.segid').text(seq);
							comboclone.find('drag').html(text);

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
							
							keeplocks(locked, locked_more, locked_contents, locked_indices);
  						//}, 300);
  					}
  				} else {
  					$(this).tooltip('destroy');
  					targets.splice(index,1);
  					gridster_trg.remove_widget(trgseg);
  					
  					//keeplocks(locked, locked_contents, locked_indices);
  					
  					if(orgtrgcnt <= sources.length){
    					targets.push("n - " + empty_next_index + "#" + "");
    					empty_next_index++;
    					trg_segs[nextsegseq] = [];
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
						
						keeplocks(locked, locked_more, locked_contents, locked_indices);
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
  					    	$('[data-toggle="popover"]').popover({container: 'body', trigger: 'manual', delay: {show: 0, hide: 0}});
  					    	$('[data-toggle="popover"]').popover('show');
  							setTimeout(function(){$('[data-toggle="popover"]').popover('hide');}, 3000);
  						}
  					} else {
  						$(this).tooltip('destroy');
  						removed_targets.unshift(targets[index]);
  						targets.splice(index,1);
  						gridster_trg.remove_widget(trgseg);
  						
  						//keeplocks(locked, locked_contents, locked_indices);
  						
  						if(orgtrgcnt <= sources.length){
    						targets.push("n - " + empty_next_index + "#" + "");
    						empty_next_index++;
    						trg_segs[nextsegseq] = [];
    					}
						
						var comboclone = $combo_target.clone(true);
    					if(currPageNum == lastPageNum){
    						gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    					}else{
    						gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    					}
						comboclone.find('.segid').text(nextsegseq);
						comboclone.find('drag').html(nextsegtext);
						
						keeplocks(locked, locked_more, locked_contents, locked_indices);
  					}
  				} else {
  					$(this).tooltip('destroy');
  					targets.splice(index,1);
  					gridster_trg.remove_widget(trgseg);
  					
  					//keeplocks(locked, locked_contents, locked_indices);
  					
  					if(orgtrgcnt <= sources.length){
    					targets.push("n - " + empty_next_index + "#" + "");
    					empty_next_index++;
    					trg_segs[nextsegseq] = [];
    				}
					
					var comboclone = $combo_target.clone(true);
    				if(currPageNum == lastPageNum){
    					gridster_trg.add_widget(comboclone,1,1,1,lastPageSegNum-locked.length);
    				}else{
    					gridster_trg.add_widget(comboclone,1,1,1,maxSegPerPage-locked.length);
    				}
					comboclone.find('.segid').text(nextsegseq);
					comboclone.find('drag').html(nextsegtext);
					
					keeplocks(locked, locked_more, locked_contents, locked_indices);
  				}
  			}
  			
  			enableEvents();
		});

		$('body').on('click', '.permovegrid', function() {
  			var rmvtrgseg = $(this).closest('.rmv_trg_segment');
  			var index = parseInt(rmvtrgseg.attr("data-row"))-1;
			removed_targets.splice(index,1);
			$(this).tooltip('destroy');
  			gridster_rmv_trg.remove_widget(rmvtrgseg);
  			$('.num_trg_removed').text(removed_targets.length);
  			if($('.num_trg_removed').text() >= $('.num_trg_storable').text()) {
  				$('.num_trg_removed').css("color","#d00000");
  			} else {
  				$('.num_trg_removed').css("color","#fff");
  			}
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
  				trg_segs[tempseq] = [];
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
    								if(totalcurrpagemoretrgsegNum > total_pre_loadable_trgs || (!lastmoretrgtext)){
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
  						}else{
  							targets.splice(lasttrgidx,1);
  						}
  					//},300);
  					
  					keeplocks(locked, locked_more, locked_contents, locked_indices);
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
  					
  					keeplocks(locked, locked_more, locked_contents, locked_indices);
  						
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
    							if(totalcurrpagemoretrgsegNum > total_pre_loadable_trgs){
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
  					keeplocks(locked, locked_more, locked_contents, locked_indices);
  				//},300);
  			}
  			
  			enableEvents();
		});
		
		$('body').on('click', '.insertabove', function() {
			disableEvents();
			
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
  				trg_segs[tempseq] = [];

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
    								if(totalcurrpagemoretrgsegNum > total_pre_loadable_trgs || (!lastmoretrgtext)){
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
  						}else{
  							targets.splice(lasttrgidx,1);
  						}
  					//},300);
  					
  					keeplocks(locked, locked_more, locked_contents, locked_indices);
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
					
					keeplocks(locked, locked_more, locked_contents, locked_indices);
  					
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
    							if(totalcurrpagemoretrgsegNum > total_pre_loadable_trgs){
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
  					
  					keeplocks(locked, locked_more, locked_contents, locked_indices);
  				//},300);
  			}
  			
  			enableEvents();
		});
	
		$('body').on('click', '.alignsegs', function() {
			var trgseg = $(this).closest('.trg_segment');
			currentSegAlign = parseInt(trgseg.attr("data-row"));
			var icon = $(this).children('.el')[0];
			var index = (currPageNum-1)*maxSegPerPage + currentSegAlign - 1;
			var srcseq = getSeq(sources[index]);
  			var trgseq = getSeq(targets[index]);
  			//alert(currentSegAlign + "," + index + "," + srcseq + "," + trgseq);

			if((srcseq in src_segs) && (trgseq in trg_segs)){
				$('.panel-body').css("overflow-y","hidden");
				$('.more').hide();
				$('.missing').hide();
				$('#prev_page').hide();
				$('#next_page').hide();
				$('#down_page').hide();
				$('.footer').hide();
				
				var s_segs = src_segs[srcseq];
				var tctypes = src_segs_tctypes[srcseq];
				var t_segs = trg_segs[trgseq];
				var bigger = Math.max(s_segs.length, t_segs.length);

				var he = bigger * (ht_sub + mg_v * 2) + $('.panel_seg').height();
  				var hes = he.toString() + "px";

				var icon_approve = $(".approve_align_seg").children('.el')[0];
				if($(icon).hasClass("seg_approved")) {
					$(icon_approve).addClass("seg_approved");
				}else {
					$(icon_approve).removeClass("seg_approved");
				}
				$(".seg_btn_menu").removeClass("hidden");
				
				for(i=0; i<s_segs.length;i++){
					var comboclone = $combo_sub_source.clone(true);
					comboclone.find('.subsegid').text(i);
					var tp = tctypes[i];
					var label = comboclone.find('.subsegtctype');
					$(label).addClass("tp"+tp);
					label.text(tp);
					comboclone.find('drag').html(getHtmlText(s_segs[i]));
    				gridster_sub_src.add_widget(comboclone,1,1,1,(i+1));
				}
				for(i=0; i<t_segs.length;i++){
					var comboclone = $combo_sub_target.clone(true);
					comboclone.find('.subsegid').text(i);
					comboclone.find('drag').html(t_segs[i]);
    				gridster_sub_trg.add_widget(comboclone,1,1,1,(i+1));
				}
				for(i=t_segs.length; i<bigger;i++){
					var comboclone = $combo_sub_target.clone(true);
					comboclone.find('.subsegid').text(i);
					comboclone.find('drag').html("");
    				gridster_sub_trg.add_widget(comboclone,1,1,1,(i+1));
				}
				
				$('.tpDELETION').tooltip({container: 'body', title: "Deletion will not be in the TXLF"});
				$('[data-toggle="tooltip"]').tooltip({container: 'body',delay: {show: 600, hide: 0}});
				setTimeout(function(){
					$('.panel').fadeTo('slow',.4);
					$('.panel').append('<div class="dis" style="position: absolute;top:0;left:0;width: 100%;height:100%;z-index:2;opacity:0.4;filter: alpha(opacity = 50)"></div>');
				},300);
				
				$(".panel_seg").animate({
					height: hes
				}, 400);
			}
		});
		
		$('body').on('click', '.quit_align_seg', function() {
			gridster_sub_src.remove_all_widgets();
			$('#subsrccol').empty();
			gridster_sub_trg.remove_all_widgets();
			$('#subtrgcol').empty();
			
			$(".seg_btn_menu").addClass("hidden");
			
			$(".panel_seg").animate({
				height: "20px"
				}, 400, function(){
					var elem = $('.panel-body');
					if(elem[0].scrollHeight == (elem.scrollTop() + elem.outerHeight())) {
						$('#prev_page').show();
						$('#next_page').show();
					}else{
						$('#down_page').show();
					}
					$('.footer').show();
			});
			
			$('.panel').fadeTo('slow',1);
			$('.panel').find(".dis").remove();
			
			var trgseg = $("#trgseg[data-row='" + currentSegAlign + "']");
			var icon = $(".approve_align_seg").children('.el')[0];
			currentSegAlign = parseInt(trgseg.attr("data-row"));
			var index = (currPageNum-1)*maxSegPerPage + currentSegAlign - 1;
			var seq = getSeq(sources[index]);
			
			var icon_align = $(trgseg.find('.alignsegs')[0]).children(".el")[0];
			var button_approve = trgseg.find('.approve')[0];
			var icon_approve = $(trgseg.find('.approve')[0]).children(".el")[0];
			if($(icon).hasClass("seg_approved")) {
				seg_aligned_targets.push(seq);
				$(icon_align).addClass("seg_approved");
				
				if($(icon_approve).hasClass("el-unlock")) {
					locked_targets.push(seq);
					$(icon_approve).removeClass("el-unlock");
					$(icon_approve).addClass("el-lock");
					$(button_approve).attr('title','Approved');
					$(trgseg).addClass("lock");
					$(trgseg).addClass("static");
					$(trgseg).find(":not(button.approve):not(button.alignsegs)").prop("disabled", true);
					$(trgseg).find('.text').css("background-color", "#13bd9c");
					$(trgseg).find('.text').css("color", "#DBFEF8");
				}
			}else {
				seg_aligned_targets.splice($.inArray(seq, seg_aligned_targets), 1);
				$(icon_align).removeClass("seg_approved");
			}
			
			$('.more').show();
			$('.missing').show();
			$('.panel-body').css("overflow-y","scroll");
		});
		
		$('body').on('click', '.approve_align_seg', function(e) {
			var icon = $(this).children('.el')[0];
			if($(icon).hasClass("seg_approved")) {
				$(icon).removeClass("seg_approved");
			}else {
				$(icon).addClass("seg_approved");
				$('.quit_align_seg').click();
			}
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
						for (i = 0; i < removed_targets.length; i++) {
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
	
	$('.savefile').on('click', function () {
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		
		var arr5 = [];
		var arr1 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		arr5[i] = getHtmlText(targets[i]);
    		arr1[i] = trg_segs[seq].concat();
    		arr1[i].unshift(seq);
		}
		
		var arr6 = [];
		var arr2 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			arr6[i] = getHtmlText(removed_targets[i]);
			arr2[i] = trg_segs[seq].concat();
    		arr2[i].unshift(seq);
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':locked_targets,'arr4':seg_aligned_targets,'arr5':arr5,'arr6':arr6,'nullcnt':empty_next_index};
		
    	$.ajax({
            url: '/RevAligner/rac/save',
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
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project Failed To Save...</h4>');
              	  $('#message').modal('show');
              }
        });
	});
	
	$('.saveexportfile').on('click', function () {
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		var arr5 = [];
		var arr1 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		arr5[i] = getHtmlText(targets[i]);
    		arr1[i] = trg_segs[seq].concat();
    		arr1[i].unshift(seq);
		}
		
		var arr6 = [];
		var arr2 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			arr6[i] = getHtmlText(removed_targets[i]);
			arr2[i] = trg_segs[seq].concat();
    		arr2[i].unshift(seq);
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':locked_targets,'arr4':seg_aligned_targets,'arr5':arr5,'arr6':arr6,'nullcnt':empty_next_index};
		
    	$.ajax({
            url: '/RevAligner/rac/save',
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
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project failed to save...</h4>');
              	  $('#message').modal('show');
              }
        });
	});

	$('.prjid').on('click', function () {
		alert("Project ID: " + prjid + '\n' + "Source Language: " + srclang + '\n' + "Target Language: " + trglang);
	});
	
	$('.closeProject').on('click', function () {
		$('.exitidf').text("closing project");
		$("#exit").modal({backdrop: "static",keyboard: false});
		$('#exit').modal('show');
	});
	
	$('.savebeforeexit').on('click', function () {
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		var arr5 = [];
		var arr1 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		arr5[i] = getHtmlText(targets[i]);
    		arr1[i] = trg_segs[seq].concat();
    		arr1[i].unshift(seq);
		}
		
		var arr6 = [];
		var arr2 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			arr6[i] = getHtmlText(removed_targets[i]);
			arr2[i] = trg_segs[seq].concat();
    		arr2[i].unshift(seq);
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':locked_targets,'arr4':seg_aligned_targets,'arr5':arr5,'arr6':arr6,'nullcnt':empty_next_index};
		
    	$.ajax({
            url: '/RevAligner/rac/save',
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
			    	redirectwithpost('/RevAligner/rac/sessiontimesout');
			      },4000);
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project failed to save...</h4>');
              	  $('#message').modal('show');
              }
        });
	});
	
	$('.exitwithoutsave').on('click', function () {
		redirectwithpost('/RevAligner/rac/sessiontimesout');
	});
	
	$('.fa-download').on('click', function () {
		$("#loading").modal({backdrop: "static",keyboard: false});
		$('#loading').modal('show');
		var arr5 = [];
		var arr1 = [];
		for (i = 0; i < targets.length; i++) { 
    		var seq = getSeq(targets[i]);
    		arr5[i] = getHtmlText(targets[i]);
    		arr1[i] = trg_segs[seq].concat();
    		arr1[i].unshift(seq);
		}
		
		var arr6 = [];
		var arr2 = [];
		for (i = 0; i < removed_targets.length; i++) { 
			var seq = getSeq(removed_targets[i]);
			arr6[i] = getHtmlText(removed_targets[i]);
			arr2[i] = trg_segs[seq].concat();
    		arr2[i].unshift(seq);
		}
		
		var obj = {'arr1':arr1,'arr2':arr2,'arr3':locked_targets,'arr4':seg_aligned_targets,'arr5':arr5,'arr6':arr6,'nullcnt':empty_next_index};
		
    	$.ajax({
            url: '/RevAligner/rac/save',
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
              },
              
              error: function() {
              	  $('#loading').modal('hide');
              	  $('.message-text').removeClass('message-good');
              	  $('.message-text').addClass('message-bad');
            	  $('.message-text').html('<h4><span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;Project failed to save...</h4>');
              	  $('#message').modal('show');
              }
        });
	});
	
	$(window).resize(function(){
		var org_ht_sub = ht_sub;
		var org_mg_v = mg_v;
		
		wd = $(window).width()/3.4;
		wd_sub = $(window).width()/3.2;
		wd_missing = $(window).width()/3.3;
		wd_more = $(window).width()/3.5;
		ht = $(window).height()/6.5;
		ht_sub = $(window).height()/9.74;
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
			
		gridster_sub_src.resize_widget_dimensions({
			widget_base_dimensions: [wd_sub, ht_sub]
		});
		generateStyleSheet("#subsourcegrids", ht_sub, wd_sub, mg_h, mg_v, maxSubSegPerPage);
			
		gridster_sub_trg.resize_widget_dimensions({
			widget_base_dimensions: [wd_sub, ht_sub]
		});
		generateStyleSheet("#subtargetgrids", ht_sub, wd_sub, mg_h, mg_v, maxSubSegPerPage);
			
		gridster_rmv_trg.resize_widget_dimensions({
			widget_base_dimensions: [wd_missing, ht]
		});
		generateStyleSheet("#rmvtargetgrids", ht, wd_missing, mg_h, mg_v, total_storable_removed_trgs);
			
		gridster_more_trg.resize_widget_dimensions({
			widget_base_dimensions: [wd_more, ht]
		});
		generateStyleSheet("#moretargetgrids", ht, wd_more, mg_h, mg_v, total_pre_loadable_trgs);
		
		var count = $('.substborder').children('.sub_trg_segment').length;
		var he = $('.panel_seg').height() - count * ((org_ht_sub - ht_sub) + (org_mg_v - mg_v) * 2);
  		var hes = he.toString() + "px";

		$(".panel_seg").animate({
			height: hes
		}, 400);
					
		var next_page_lt = $('#targetgrids').position().left + $('#targetgrids').width() - $('#next_page').width() - 15;
	    $('#next_page').css('left',next_page_lt+'px');
	    
	    var down_page_lt = ($('#sourcegrids').position().left + $('#sourcegrids').width() + $('#targetgrids').position().left - $('#down_page').width() - 15)/2;
	    $('#down_page').css('left',down_page_lt+'px');
	});
		
	$(window).focus(function(e) {
		validateSession();
	});
	
	
	
	$('.timeoutclose').on('click', function () {
		$('.timeoutclose').hide();
		cl1.show();
		$(window).unbind("beforeunload");
		redirectwithpost('/RevAligner/rac/sessiontimesout');
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
                 
              success: function(data) {
              	    
              },
              
              error: function(xhr) {
    				$('.modal-dialog').css('opacity', '0.2');
			    	$(".sessiontimeout-dialog").css('opacity', '0.9');
			    		
				    $("#sessiontimeout").modal({backdrop: "static",keyboard: false});
					$('#sessiontimeout').modal('show');
					clearInterval(intervalId);
             }
        });
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
			alert($(element).text());
			alert(element.childNodes.length);
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

		function updateSegText(paraindex, segindex, newtext) {
    		var paratext = targets[paraindex];
    		var text = getHtmlText(paratext);
    		var seq = getSeq(paratext);
    		var segs = trg_segs[seq];
    		var oldtext = segs[segindex];
    		var count = 0;
    		for(i = 0; i < segs.length; i++){
    			var t = segs[i];
    			if(t.includes(oldtext)){
    				count++;
    			}
				if(i == segindex){break;}
    		}
    		trg_segs[seq][segindex] = newtext;
			var nth = 0;
			var re = new RegExp(oldtext,"g");
			text = text.replace(re, function (match) {
			    nth++;
			    return (nth === count) ? newtext : match;
			});
			
    		return text;
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
		
		function keeplocks(locked, locked_more, locked_contents, locked_indices) {
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
				$(comboclone).find(":not(button.approve):not(button.alignsegs)").prop("disabled", true);
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
		}
		
		function generateStyleSheet(namespace, height, width, margin_h, margin_v, count){
			var styles = '';
			styles += (namespace + ' [data-sizex="1"] { width:' + width + 'px;}');
			styles += (namespace + ' [data-sizey="1"] { height:' + height + 'px;}');
			styles += (namespace + ' [data-col="1"] { left:' + margin_h + 'px;}');
			
			styles += (namespace + ' [data-row="1"] { top:' + margin_v + 'px;}');
			var inc = 2 * margin_v + height;
			var prev = margin_v;
			//for (i = opts.rows; i >= 0; i--) {
			for (i = 1; i < count; i++) {
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
			for (i = 1; i < segcount; i++) {
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
    width: 260px !important;
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
    						<li><a class="config">Configuration<span class="glyphicon pull-right glyphicon-cog mprj"></span></a></li>
    						<li><a class="prjid">About<span class="glyphicon pull-right glyphicon-info-sign mprj"></span></a></li>
    						<li class="divider"></li>
            				<li><a class="closeProject">Close Project<span class="glyphicon pull-right glyphicon-off mprj"></span></a></li>
          				</ul>
    				</li>
        			<li description="More Target Grids"><a><i class="fa fa-cubes"></i></a></li>
        			<li description="Show Removed Trans"><a><i class="fa fa-braille"></i></a></li>
    				<li description="Get Translation Kit"><a><i class="fa fa-download"></i></a></li>
      			</ul>
    			<ul class="nav navbar-nav" style="float: right">
    				<li description="Manage Acount" class="pull-right">
    					<a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-user"></i></a>
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
	<div class="panel_seg" style="display:none">
		<div class="btn-group seg_btn_menu hidden">
			<button type="button" class="btn btn-link btn-lg approve_align_seg" data-toggle="tooltip" title="" data-original-title="Approve">
				<i class="el el-thumbs-up el-2x"></i>
			</button>
			<button type="button" class="btn btn-link btn-lg quit_align_seg" data-toggle="tooltip" title="" data-original-title="Close">
				<i class="el el-remove el-2x"></i>
			</button>
		</div>

    	<div id="subsourcegrids" class="gridster">
			<div id="subsrccol" class="substborder">
							
			</div>
		</div>

    	<div id="subtargetgrids" class="gridster">
			<div id="subtrgcol" class="substborder">
							
			</div>
		</div>

	</div>
	<div class="panel panel-default" style="display:none">
  		<div class="panel-heading"></div>
  		<div class="panel-body">
  			<div id="sourcegrids" class="gridster">
				<h4 style="margin-bottom:4px"><span class="label label-default">S o u r c e</span></h4>
				<div id="srccol" class="stborder">
				</div>
			</div>
			<div id="targetgrids" class="gridster">
				<h4 style="margin-bottom:4px"><span class="label label-default">T a r g e t</span></h4>
				<div id="trgcol" class="stborder">
				</div>
			</div>
			<div id="moretargetgrids" class="more nvisible gridster">
				<button class="btn btn-link btn-xs more_toggle_top pull-left"></button>
				<button class="btn btn-link btn-xs more_toggle pull-left el-1x" data-toggle="tooltip" title="" data-original-title="toggle">
					<i class="el el-chevron-left arrow"></i>
				</button>
				<button class="btn btn-link btn-xs more_toggle_bot pull-left"></button>
				<h4>
					<span class="badge num_trg_more">&nbsp;&nbsp;Target Paragraphs of Next Page&nbsp;&nbsp;</span>
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
		<span class="glyphicon glyphicon-envelope"></span><a id="emailadd">mxiang@transperfect.com</a>
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
	<div id="sessiontimeout" class="modal fade" role="dialog">
  		<div class="modal-dialog sessiontimeout-dialog">
    		<div class="modal-content">
      			<div class="modal-body">
        			<h4>
        				<span class="glyphicon glyphicon-exclamation-sign"></span>&nbsp;&nbsp;This page is out of date ...
        				<i class="fa fa-close timeoutclose"></i>
        				<span id="sessiontimeoutloading" class="wrapper"></span>
        			</h4>
      			</div>
    		</div>
  		</div>
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
    });
});

</script>
<style>
body
{
	overflow: hidden;
	padding-top: 25px;
	padding-right: 0!important;
	padding-bottom:75px;
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

.panel_seg {
	position: relative;
	height: 20px;
	max-height: 620px;
	overflow-x: hidden;
	overflow-y: scroll !important;
	background-color:#d5d8dc;
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

.seg_approved {
	color:#13bd9c;
}

.panel-heading{
	position: relative;
	padding:10px;
}

.panel-body{
	position: relative;
	height:95%;
	overflow-x:hidden;
	overflow-y:scroll;
	padding-bottom:200px;
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

.ma{
	margin-top:3px;
}

.sessiontimeout-dialog{
	width:320px!important;
}

.timeoutclose{
	margin-top:2px;
	float:right;
	cursor:pointer;
}

#sessiontimeoutloading{
	float:right;
	margin-top:3px;
}
</style>
</body>
</html>