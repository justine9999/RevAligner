$(function () {
	'use strict';
	var timeOutCheckInterval = 60000;
	var data_src = [];
	var data_trg = [];
	var data_txml = [];
	var data_pkg = [];
	var data_src_size = 0;
	var data_trg_size = 0;
	var data_txml_size = 0;
	var data_pkg_size = 0;
	var data_loaded_src = 0;
	var data_loaded_trg = 0;
	var data_loaded_txml = 0;
	var src_lang = "";
	var trg_lang = "";
	var prjid;
	var subname;
	var prj_creation_time;
	var prvprjinfo = [];
	var username;
	var intervalId;
	var tempkeystrokes = "";
	var lastkeypresstime = Date.now();
	var langs = [];
	var gotoaligner = false;
	var autoalignratio;
	var isadmin = false;
	var prjinfos = [];
	
	var progress_bad_color = '#FF8082';
	var progress_middle_color = '#FFEA82';
	var progress_done_color = '#80FFC8';
                                                                 
	var cl = new CanvasLoader('pfloading');
	cl.setShape('spiral');
	cl.setDiameter(16);
	
	var cl2 = new CanvasLoader('cfloading');
	cl2.setShape('spiral');
	cl2.setDiameter(16);
	
	var cl3 = new CanvasLoader('searchprjloading'); 
	cl3.setShape('spiral');
	cl3.setDiameter(16);
	
	var cl4 = new CanvasLoader('validateprjloading'); 
	cl4.setShape('spiral');
	cl4.setDiameter(16);
	
	var cl5 = new CanvasLoader('gtaloading'); 
	cl5.setShape('spiral');
	cl5.setDiameter(16);
	
	var cl6 = new CanvasLoader('downloadtrgloading'); 
	cl6.setShape('spiral');
	cl6.setDiameter(16);
	
	var cl8 = new CanvasLoader('prjlistloading');
	cl8.setShape('spiral');
	cl8.setDiameter(46);
	
	var stompClient = null;

	$("#createproject").modal({backdrop: false});
	$("#createproject").modal({show: true});
	
	$('.back-content').hide();
	$('.alignres').hide();
	//$("#uploadModal").modal({backdrop: false});
	//$("#uploadModal").modal({show: true});
	//$( ".modal-dialog" ).animate({
    //top: "+=30"
  //}, 300);
	
	$.ajax({
        url: 'readappbaseconfiguration',
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
        	    $('.usernaamelabel').html("[ " + username + " ]&nbsp;&nbsp;");
        	    if(data.isadmin == 'true'){
        	    	isadmin = true;
        	    	$('.usericon i').removeClass('fa-user');
        	    	$('.usericon i').addClass('fa-user-circle');
        	    }
          },
          
          error: function(xhr) {
				alert('failed to fetch current user info...');
            }
    });
	
	$('body').on('click', '* :not(.backtomain)', function(e) {
		if(typeof prjid == 'undefined') {
			return;
		}
		
    	$.ajax({
        	url: 'updatelastaccesstime',
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
    	
    //project info row
	var prj_info_row = $('<div class="row prjitem" />');
	var name = $('<div class="col-sm-3 prjname" />');
	var progress = $('<div class="col-sm-2 prjprogress center" />');
	var sname = $('<div class="col-sm-2 prjsubname center" />');
	var date = $('<div class="col-sm-2 prjdate center" />');
	var user = $('<div class="col-sm-1 prjuser center" />');
	
	var relief = $('<div class="col-sm-1 center" />');
	var relief_content = $('<i class="prjrelief fa fa-unlink" />');
	relief.append(relief_content);
	
	var icon = $('<div class="col-sm-1 center" />');
	var icon_content = $('<i class="prjicon" />');
	icon.append(icon_content);
	
	prj_info_row.append(name)
		.append(progress)
			.append(sname)
				.append(date)
					.append(user)
						.append(relief)
							.append(icon);
	
	
	//load more prj row
	var prj_load_row = $('<div class="prjload" />');
	var load_button = $('<i class="fa fa-ellipsis-h prjloadbutton" />');
	var anime = $('<div class="loadanime" style="display:none"/>');
	anime.append('<div id="load_block_1" class="load_barlittle"></div>')
			.append('<div id="load_block_2" class="load_barlittle"></div>')
				.append('<div id="load_block_3" class="load_barlittle"></div>')
	prj_load_row.append(load_button);
	prj_load_row.append(anime);

    	
    $('.signout').on('click', function () {
    	$(window).unbind("beforeunload");
		window.location.replace('logout');
	});
	
	
	$('.uploadfiles').on('click', function () {
		$('.uploadfiles').prop('disabled', true);
		$('#progress_all').removeClass('notransition');
		
		var prjinfo = [];
		prjinfo[0] = prjid;
		prjinfo[1] = prj_creation_time;
		prjinfo[2] = src_lang;
		prjinfo[3] = trg_lang;
		prjinfo[4] = subname;
		
		$.ajax({
            url: 'sendprjinfo',
            type: "POST",
            data: JSON.stringify(prjinfo),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {              	  	
              	  	if($('#stats_src').text()=="ready to upload"){
						$('#addsrc').prop('disabled', true);
						$('#stats_src').text('uploading...');
						$('.dropdown > button').prop('disabled', true);
						$.each(data_src, function (index, file) {
            				file.submit();
        				});
					}
		
        			if($('#stats_trg').text()=="ready to upload"){
        				$('#addtrg').prop('disabled', true);
						$('#stats_trg').text('uploading...');
						$('.dropdown > button').prop('disabled', true);
						$.each(data_trg, function (index, file) {
            				file.submit();
        				});
					};
              },
              
              error: function() {
              	  alert('failed to pass project infomation');
              }
        });

	});
	$('#addsrc').on('click', function() { $('#fileuploadsrc').click();return false;});
	$('#addtrg').on('click', function() { $('#fileuploadtrg').click();return false;});
    $('.fupload').fileupload({
    	url: 'upload',
        autoUpload: false,
        acceptFileTypes: /(\.|\/)(doc|docx|ra|txml)$/i,
        maxFileSize: 999000,
		add: function (e, data) {
			if(this.id == 'fileuploadsrc') {
				data_src_size = data.files[0].size;
				data_src.pop();
				$("#tr_src").remove();
				$("#tbheader").after(
            	$('<tr id="tr_src"/>')
            		.append($('<td/>').text('Source'))
            		.append($('<td/>').text(data.files[0].name))
            		.append($('<td/>').text((data.files[0].size/1024).toFixed(2) + " K"))
            		.append($('<td/>').append($('<span id="stats_src" class="stats"/>').text("ready to upload")))
            		.append($('<td/>').append($('<span id="action_src"/>').text("N/A")))
            		.append($('<td/>').append($('<div class="progress">').append($('<div id = "progress_src" class="progress-bar progress-bar-striped" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">'))))
            		.hide()
            		.fadeIn(500)
				); 
				data_src.push(data);
			} else if (this.id == 'fileuploadtrg') {
				data_trg_size = data.files[0].size;
				data_trg.pop();
				$("#tr_trg").remove();
				$("#added-files").append(
            	$('<tr id="tr_trg"/>')
            		.append($('<td/>').text('Target'))
            		.append($('<td/>').text(data.files[0].name))
            		.append($('<td/>').text((data.files[0].size/1024).toFixed(2) + " K"))
            		.append($('<td/>').append($('<span id="stats_trg" class="stats"/>').text("ready to upload")))
            		.append($('<td/>').append($('<span id="action_trg"/>').text("N/A")))
            		.append($('<td/>').append($('<div class="progress">').append($('<div id = "progress_trg" class="progress-bar progress-bar-striped" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">'))))
					.hide()
            		.fadeIn(500)
				); 
				data_trg.push(data);
			}
			
			if( !(($('#stats_src').text()=="uploaded") && ($('#stats_trg').text()=="uploaded")) ) {
            	$('.uploadfiles').prop('disabled', false);
            	$('#progress_all').addClass('notransition');
            	$('#progress_all').css(
                	'width','0%'
            	);
            	$('.show').text('0% Complete');
        	}
		},
		
        done: function () {
        	if(this.id == 'fileuploadsrc') {
        		$("#src_lang").prev().prop('disabled', true);
				$('#stats_src').text('uploaded');
				$('#action_src').html("<a class='remove' href='del/source'>Remove</a>");
				data_src.pop();
			} else if (this.id == 'fileuploadtrg') {
				$("#trg_lang").prev().prop('disabled', true);
				$('#stats_trg').text('uploaded');
				$('#action_trg').html("<a class='remove' href='del/target'>Remove</a>");
				data_trg.pop();
			}
        	
        	if(($('#stats_src').text()=="uploaded") && ($('#stats_trg').text()=="uploaded")){
        		$('.processfiles').prop('disabled', false);
        		$('#submission').prop('disabled', true);
    		}
        },
 		
 		fail: function () {
 			if(this.id == 'fileuploadsrc') {
				$('#stats_src').text('upload failed');
				$('#action_src').text("N/A");
				$('#addsrc').prop('disabled', false);
				data_src.pop();
			} else if (this.id == 'fileuploadtrg') {
				$('#stats_trg').text('upload failed');
				$('#action_trg').text("N/A");
				$('#addtrg').prop('disabled', false);
				data_trg.pop();
			}
    	},
    		
        progressall: function (e, data) {

            var progress = parseInt(data.loaded / data.total * 100, 10);
            if(this.id == 'fileuploadsrc') {
            	data_loaded_src = data.loaded;
				$('#progress_src').css(
                	'width',
                	progress + '%'
            	);
			} else if (this.id == 'fileuploadtrg') {
				data_loaded_trg = data.loaded;
				$('#progress_trg').css(
                	'width',
                	progress + '%'
            	);
			}

			var progress_all = parseInt((data_loaded_src + data_loaded_trg) / (data_src_size + data_trg_size) * 100, 10);
			$('#progress_all').css(
                	'width',
                	progress_all + '%'
            	);
            $('.show').text(
                progress_all + '% Complete'
            );
        },
 
    });
    
    $('body').on('click', 'a.remove', function(event) {
        $.ajax({
            url: $(event.target).attr("href"),
            type: "GET",
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	var link = $(event.target).attr("href");
              	var tr = $(event.target).closest('tr');
                tr.fadeOut(300, function(){tr.remove()});
                if(link.endsWith('source')){
                	data_src.pop();
                	$('#addsrc').prop('disabled', false);
                	$("#src_lang").prev().prop('disabled', false);
                } else if(link.endsWith('target')) {
                	data_trg.pop();
                	$('#addtrg').prop('disabled', false);
                	$("#trg_lang").prev().prop('disabled', false);
                }
                
                $('#progress_all').addClass('notransition');
            	$('#progress_all').css(
                	'width','0%'
            	);
            	$('.show').text('0% Complete');
            	
                $("#processfilegood").hide();
                $("#processfilebad").hide();
                $('.processfiles').prop('disabled', true);
                $('#submission').prop('disabled', false);
              },
              
              error: function() {
              	  	alert("Cannot delete files on server!");
                	var link = $(event.target).attr("href");         
                  if(link.endsWith('source')){
                	  $('#stats_src').text('cannot delete');
                  } else if(link.endsWith('target')) {
                	  $('#stats_trg').text('cannot delete');
                  }
                }
        });
        
        event.preventDefault();
    });
    
    $('.processfiles').on('click', function (e) {
    	$('#progress_all').addClass('notransition');
        $('#progress_all').css(
            'width','100%'
        );
        $('.show').css('color','#ffffff');
        $('.show').text('Starting file alignment...');
        
        stompClient = null;
        var socket = new SockJS('/RevAligner/rac/chkalignprogress');
	    stompClient = Stomp.over(socket);
        
	    stompClient.connect({}, function() {
	        stompClient.subscribe('/topic/'+prjid, function(alprogress){
	           	var fpprogress = alprogress.body;
	           	if(fpprogress != -1 && fpprogress != -2){
		            var msg = "";
		            switch(fpprogress) {
		            	case "0":
					        msg = "Starting file alignment...";
					        break;
					    case "10":
					        msg = "Reformatting source documents...";
					        break;
					    case "20":
					        msg = "Converting source documents...";
					        break;
					    case "40":
					        msg = "Converting target documents...";
					        break;
					    case "60":
					        msg = "Verifying paragraphs mapping...";
					        break;
					    case "70":
					        msg = "Aligning files using Auto-Aligner...";
					        break;
					    case "90":
					        msg = "Verifying segments mapping...";
					        break;
					    case "100":
					        msg = "File alignment complete.";
					        break;
					    default:
					    	msg = "Aligning files using Auto-Aligner (estimate " + (parseInt(fpprogress)-200) + " minutes)...";
					        break;
					}

		            $('.show').text(msg);
		            if(fpprogress == 100){
		            	if(stompClient != null){stompClient.disconnect();}
		            	setTimeout(function(){
					      	cl.hide();
			              	$('#processfilebad').hide();
			              	$('#processfilegood').show();
			            	$('#prcsbtntext').html('Files Processed');
							$('.useautosave').css('pointer-events', 'none');
							$('.useautosave').css('opacity', '0.6');
			            	$('#alignsegoption').show();
			            	$('#goAlignment').prop('disabled', false);
			            	
			            	$.ajax({
					            url: 'readnbalignmentresult',
					            type: "GET",
					            dataType: 'json',
					                 
					              beforeSend: function(xhr) {
					                xhr.setRequestHeader("Accept", "application/json");
					                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
					              },
					                 
					              success: function(data) {
					              	  autoalignratio = data.ratio;
					              	  if(autoalignratio != 'undefined' && autoalignratio != "null" && autoalignratio != null){
						            	  $('.alignres').text(autoalignratio);
						            	  $('.alignres').show();
						              }
					              },
					              
					              error: function(xhr) {
					            	  $('.alignres').text("N/A");
						              $('.alignres').show();
					              }
					        });
			            	
					    },1000);
		        	}
	           	}
	        });

	        $('#progress_all').removeClass('notransition');
	        stompClient.send("/app/getalignprogress", {}, prjid);
	        
	    	$('#action_src').text("N/A");
	        $('#action_trg').text("N/A");
	    	$('#prcsbtntext').html('Processing');
	    	$('#processfilegood').hide();
	        $('#processfilebad').hide();
	    	cl.show();
	    	$('.processfiles').prop('disabled', true);
	    	$("input[name='alignertype'").prop('disabled', true);
	    	var aligntype = $("input[name='alignertype']:checked").val();

	    	$.ajax({
	            url: 'align',
	            type: "POST",
	            data: "aligntype=" + aligntype,
	            dataType: 'json',
	                 
	              beforeSend: function(xhr) {
	                xhr.setRequestHeader("Accept", "application/json");
	                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	              },
	                 
	              success: function() {

	              },
	              
	              error: function(xhr) {
	            	  cl.hide(); 
			          
	            	  $('#processfilebad').show();
	              	  $('#processfilegood').hide();
	            	  $('#prcsbtntext').html('Process Files');
	            	  
	            	  $("input[name='alignertype'").prop('disabled', false);
	            	  $('#action_src').html("<a class='remove' href='del/source'>Remove</a>");
	            	  $('#action_trg').html("<a class='remove' href='del/target'>Remove</a>");
	            	  if(stompClient != null){stompClient.disconnect();}
	            	  setTimeout(function(){
	            	  	$('#progress_all').addClass('notransition');
			            $('#progress_all').css(
			              'width','0%'
			            );
			            $('.show').css('color','black');
			            $('.show').text('0% Complete');
			          
	            	  	$('[data-toggle="popover"]').popover({container: 'body', trigger: 'manual', delay: {show: 0, hide: 0}, content: (xhr.responseText+" !")});
  					  	$('[data-toggle="popover"]').popover('show');
	            	  }, 1000);
	             }
	        });
	    });
	});

	$(document).mousedown(function (e)
	{
	    var container = $(".processfiles");
	    if ((!container.is(e.target)) && container.has(e.target).length == 0) 
	    {
	    	var popover = $('.processfiles').data('bs.popover');
	    	if(typeof popover != 'undefined'){
	    		if (popover.tip().is(':visible')) {
				    $('[data-toggle="popover"]').popover('hide');
				    $('.processfiles').prop('disabled', false);
				}
	    	}
	    }
	});
	
	$('body').on('click', '#goAlignment', function() {
		cl5.show();
		$('#goAlignment').prop('disabled', true);
		gotoaligner = true;
		var ckb1 = $('.aligntype');
		if(ckb1.hasClass('el-check-empty')){
			window.location.replace('Aligner');
		}else{
			var ckb2 = $('.useautosave');
			if(ckb2.hasClass('el-check-empty')){
				window.location.replace('SegAligner');
			}else{
				window.location.replace('SegAlignerAuto');
			}
		}
    });

    $('.uploadtrans').on('click', function () {
		$('.uploadtrans').prop('disabled', true);
		$('#progress_all_trans').removeClass('notransition');
		if($('#stats_txml').text()=="ready to upload"){
			$('#addtrans').prop('disabled', true);
			$('#stats_txml').text('uploading...');
			$.each(data_txml, function (index, file) {
            	file.submit();
        	});
		}
	});
	
	$('#addtrans').on('click', function() { $('#fileuploadtrans').click();return false;});
	
    $('.fuploadtrans').fileupload({
    	url: 'upload',
        autoUpload: false,
        acceptFileTypes: /(\.|\/)(doc|docx)$/i,
        maxFileSize: 999000,
		add: function (e, data) {
			data_txml_size = data.files[0].size;
			data_txml.pop();
			$("#tr_txml").remove();
			$("#tbheader_trans").after(
            $('<tr id="tr_txml"/>')
            	.append($('<td/>').text(data.files[0].name))
            	.append($('<td/>').text((data.files[0].size/1024).toFixed(2) + " K"))
            	.append($('<td/>').append($('<span id="stats_txml" class="stats"/>').text("ready to upload")))
            	.append($('<td/>').append($('<span id="action_txml"/>').text("N/A")))
            	.append($('<td/>').append($('<div class="progress">').append($('<div id = "progress_txml" class="progress-bar progress-bar-striped" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">'))))
			); 
			data_txml.push(data);

			if( !(($('#stats_txml').text()=="uploaded")) ) {
            	$('.uploadtrans').prop('disabled', false)
            	$('#progress_all_trans').addClass('notransition');
            	$('#progress_all_trans').css(
                	'width','0%'
            	);
            	$('.show_trans').text('0% Complete'
            	);
        	}
		},
		
        done: function () {
		$('#stats_txml').text('uploaded');
		$('#action_txml').html("<a class='removetrans' href='del/merge'>Remove</a>");
		data_src.pop();
        	if(($('#stats_txml').text()=="uploaded")){
        		$('.processtrans').prop('disabled', false);
    		}
        },
 		
 		fail: function () {
 			$('#stats_txml').text('upload failed');
			$('#action_txml').text("N/A");
			$('#addtrans').prop('disabled', false);
			data_txml.pop();
    	},
    		
        progressall: function (e, data) {

            var progress_trans = parseInt(data.loaded / data.total * 100, 10);
            data_loaded_txml = data.loaded;
			$('#progress_txml').css(
                	'width',
                	progress_trans + '%'
            	);

			var progress_all = parseInt((data_loaded_txml) / (data_txml_size) * 100, 10);
			$('#progress_all_trans').css(
                	'width',
                	progress_all + '%'
            	);
            $('.show_trans').text(
                progress_all + '% Complete'
            );
        },
 
        dropZone: $('#dropzone')
    });
    
    $('body').on('click', 'a.removetrans', function(event) {
        $.ajax({
            url: $(event.target).attr("href"),
            type: "GET",
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
              	var link = $(event.target).attr("href");         
                var tr = $(event.target).closest('tr');
                tr.fadeOut(300, function(){tr.remove()});
                data_txml.pop();
                $('#addtrans').prop('disabled', false);
                $('.processtrans').prop('disabled', true);
                
                $('#progress_all_trans').addClass('notransition');
            	$('#progress_all_trans').css(
                	'width','0%'
            	);
            	$('.show_trans').text('0% Complete');
            	
                $('#converttargetgood').hide();
              	$('#converttargetbad').hide();
              },
              
              error: function() {
              	  	alert("Cannot delete files on server!");
                	var link = $(event.target).attr("href");         
                	$('#stats_txml').text('cannot delete');
                }
        });
        
        event.preventDefault();
    });
    
    $('.processtrans').on('click', function () {
    	$('#action_txml').text("N/A");
    	$('#convbtntext').html('Processing...');
    	$('#converttargetgood').hide();
        $('#converttargetbad').hide();
    	cl2.show();
    	$('.processtrans').prop('disabled', true);
    	
    	var preservefmt = "false";
    	var ckb = $('.preservefmt');
		if(ckb.hasClass('el-check')){
			preservefmt = "true";
		}
    	
    	$.ajax({
            url: 'merge',
            type: "POST",
            data: "preservefmt=" + preservefmt,
	        dataType: 'json',
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
              },
                 
              success: function() {
              	  cl2.hide();
              	  $('#converttargetbad').hide();
              	  $('#converttargetgood').show();
            	  $('#convbtntext').html('Files Converted');
            	  $('.preservefmt').css('pointer-events', 'none');
				  $('.preservefmt').css('opacity', '0.6');
            	  $('#downloadTarget').prop('disabled', false);
              },
              
              error: function() {
            	  cl2.hide();
            	  $('#converttargetgood').hide();
              	  $('#converttargetbad').show();
            	  $('#convbtntext').html('Convert File');
            	  $('.processtrans').prop('disabled', false);
            	  $('#action_txml').html("<a class='removetrans' href='del/merge'>Remove</a>");
                }
        	});
		});
		
	$('#downloadTarget').on('click', function () {
    	$('#downloadTarget').prop('disabled', true);
    	cl6.show();
    	
    	$.ajax({
            url: 'gettarget',
            type: "GET",
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {
            	  $('#downloadTarget').prop('disabled', false);
            	  downloadFromURL('gettarget', function(){cl6.hide();});
              },
              
              error: function() {
            	  alert('download target doc failed!');
            	  $('.processtrans').prop('disabled', false);
                }
        	});
		});

    $('.cornerbtn').mouseenter(function() {
    		if($(this).hasClass("yready")){
    			$(this).css('color', '#444');
	  	  		$(this).css('cursor', 'pointer');
    		}
  		}
	  );
	  
	$('.cornerbtn').mouseleave(function() {
			if($(this).hasClass("yready")){
				$(this).css('color', '#666');
			}
  		}
	  );
    
    $('.cornerbtn').click(function() {
    		if($(this).hasClass("yready")){
    			$("#card").flip('toggle');
	    		if($('.front-content').is(':visible')){
	    			$('.front-content').hide();
	    			$('.back-content').show();
	    		} else {
	    			$('.back-content').hide();
	    			$('.front-content').show();
	    		}
	    		$('.cornerbtn').fadeOut(0);
    		}
  		}
	  );
	 
	$("#card").flip({
        trigger: "manual",
      });
        
    $("#card").on('flip:done',function(){
  		$('.cornerbtn').fadeIn(300);
	});
	
	$("#new").click(function() {
		var checknew = $('.checknew');
		var checkcont = $('.checkcont');
		var buttoncont = checkcont.next();
		var buttonnext = $('.next');
		if(checknew.hasClass('el-check-empty')){
			checknew.removeClass('el-check-empty')
			checknew.addClass('el-check')
			$(this).css('background-color', '#E0F8EC');
			checkcont.removeClass('el-check')
			checkcont.addClass('el-check-empty')
			buttoncont.css('background-color', '');
			buttonnext.prop('disabled', false);
		}else{
			checknew.removeClass('el-check')
			checknew.addClass('el-check-empty')
			$(this).css('background-color', '');
			buttonnext.prop('disabled', true);
		}
	});
	
	$('.aligntype').click(function() {
		var ckb = $(this);
		if(ckb.hasClass('el-check-empty')){
			ckb.removeClass('el-check-empty')
			ckb.addClass('el-check')
		}else{
			ckb.removeClass('el-check')
			ckb.addClass('el-check-empty')
		}
	});
	
	$('.preservefmt').click(function() {
		var ckb = $(this);
		if(ckb.hasClass('el-check-empty')){
			ckb.removeClass('el-check-empty')
			ckb.addClass('el-check')
		}else{
			ckb.removeClass('el-check')
			ckb.addClass('el-check-empty')
		}
	});
	
	$('.useautosave').click(function() {
		var ckb = $(this);
		if(ckb.hasClass('el-check-empty')){
			ckb.removeClass('el-check-empty')
			ckb.addClass('el-check')
		}else{
			ckb.removeClass('el-check')
			ckb.addClass('el-check-empty')
		}
	});
	
	$("#continue").click(function() {
		var checkcont = $('.checkcont');
		var checknew = $('.checknew');
		var buttoncnew = checknew.next();
		var buttonnext = $('.next');
		if(checkcont.hasClass('el-check-empty')){
			checkcont.removeClass('el-check-empty')
			checkcont.addClass('el-check')
			$(this).css('background-color', '#F7F8E0');
			checknew.removeClass('el-check')
			checknew.addClass('el-check-empty')
			buttoncnew.css('background-color', '');
			buttonnext.prop('disabled', false);
		}else{
			checkcont.removeClass('el-check')
			checkcont.addClass('el-check-empty')
			$(this).css('background-color', '');
			buttonnext.prop('disabled', true);
		}
	});
	
	$(".langs").on('click', 'li a', function(){
		var langtext = $(this).text();
		$(this).parent().parent().prev().html(langtext + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
		var id = $(this).closest('ul').attr('id');
		if(id == 'src_lang'){
			src_lang = langtext;
			if(trg_lang != ""){
				$('#addsrc').prop('disabled', false);
				$('#addtrg').prop('disabled', false);
			}
		}else{
			trg_lang = langtext;
			if(src_lang != ""){
				$('#addsrc').prop('disabled', false);
				$('#addtrg').prop('disabled', false);
			}
		}
	});
	
	$(".next").click(function() {
		var checknew = $('.checknew');
		var checkcont = $('.checkcont');
		
		$('.checknew').prop('disabled',true);
		$('#new').prop('disabled',true);
		$('.checkcont').prop('disabled',true);
		$('#continue').prop('disabled',true);

		var time = $.now();
		var id = makeid();
		var tmp_prjid = "RA" + "#" + time + id;
		var dt = new Date();
		prj_creation_time = dt.getFullYear() + "/" + ('0' + (dt.getMonth()+1)).slice(-2) + "/" + ('0' + dt.getDate()).slice(-2) + " " + ('0' + dt.getHours()).slice(-2) + ":" + ('0' + dt.getMinutes()).slice(-2) + ":" + ('0' + dt.getSeconds()).slice(-2);
		var obj = {'prjid':tmp_prjid};
			
		if(checknew.hasClass('el-check')){
			$.ajax({
              url: 'startnewsession',
              type: "POST",
              data: "prjid=" + tmp_prjid,
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
              },
                 
              success: function() {
              	  $('.checknew').prop('disabled',false);
				  $('#new').prop('disabled',false);
				  $('.checkcont').prop('disabled',false);
				  $('#continue').prop('disabled',false);
				  
              	  prjid = tmp_prjid;
              	  
              	  var langspath = "../langs/langs.txt"
				
				  $("#createproject").modal("hide");
				  setTimeout(function(){
					  $("#projectid").text(tmp_prjid);
					  $("#ctprojectid").text(" - " + tmp_prjid);
					  $.get(langspath, function(data) {
	      				  langs = data.split('#');
	      				  var dropdowns = $(".langs");
	  					  for (var i = 0; i < dropdowns.length; i++) {
	    					  var dropdown = dropdowns[i];
	    					  for(var j = 0; j < langs.length; j++) {
	    						  $(dropdown).append($("<li><a>" + langs[j] + "</a></li>"))
	    					  }
						  }
					
						  $("#uploadModal").modal({backdrop: false});
						  $("#uploadModal").modal({show: true});
	  				  }, 'text');
				  },300);
              },
              
              error: function() {
            	  alert('failed to create new project, active project found!');
              	  $('.checknew').prop('disabled',false);
				  $('#new').prop('disabled',false);
				  $('.checkcont').prop('disabled',false);
				  $('#continue').prop('disabled',false);
                }
        	});
		}else{
			$.ajax({
              url: 'checkactiveprj',
              type: "POST",
                 
              success: function() {
              	  $('.checknew').prop('disabled',false);
				  $('#new').prop('disabled',false);
				  $('.checkcont').prop('disabled',false);
				  $('#continue').prop('disabled',false);
				  $("#createproject").modal("hide");
				  $('.projects').removeClass("disabled");
				  setTimeout(function(){
					  $("#continueproject").modal({backdrop: false});
					  $("#continueproject").modal({show: true});
				  },300);
              },
              
              error: function() {
            	  alert('failed to continue old project, active project found!');
              	  $('.checknew').prop('disabled',false);
				  $('#new').prop('disabled',false);
				  $('.checkcont').prop('disabled',false);
				  $('#continue').prop('disabled',false);
                }
        	});
		}
	});
	
	function makeid()
	{
    	var text = "";
    	var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    	for( var i=0; i < 1; i++ ){
        	text += possible.charAt(Math.floor(Math.random() * possible.length));
		}
    	return text;
	}
	
	$(".checkprjnum").click(function() {
		var checkprjnum = $(this);
		var checkprjpkg = $('.checkprjpkg');
		if(checkprjnum.hasClass('el-check-empty')){
			checkprjnum.removeClass('el-check-empty')
			checkprjnum.addClass('el-check')
			//$(this).css('background-color', '#E0F8EC');
			checkprjpkg.removeClass('el-check')
			checkprjpkg.addClass('el-check-empty')
			$('#prvprjnum').prop('disabled', false);
			$('#tokenstring').prop('disabled', false);
			$('#prvprjpkg').prop('disabled', true);
			$('#browsepkg').prop('disabled', true);
			$('#validatepkg').prop('disabled', true);
		}else{
			checkprjnum.removeClass('el-check')
			checkprjnum.addClass('el-check-empty')
			$('#prvprjnum').prop('disabled', true);
			$('#tokenstring').prop('disabled', true);
			$('#searchprj').prop('disabled', true);
		}
	});
	
	$('#submission').bind('input propertychange', function() {
    	subname = $('#submission').val().trim();
	});
	
	$(".checkprjpkg").click(function() {
		var checkprjnum = $('.checkprjnum');
		var checkprjpkg = $(this);
		if(checkprjpkg.hasClass('el-check-empty')){
			checkprjpkg.removeClass('el-check-empty')
			checkprjpkg.addClass('el-check')
			//$(this).css('background-color', '#E0F8EC');
			checkprjnum.removeClass('el-check')
			checkprjnum.addClass('el-check-empty')
			$('#prvprjpkg').prop('disabled', false);
			$('#prvprjnum').prop('disabled', true);
			$('#tokenstring').prop('disabled', true);
			$('#browsepkg').prop('disabled', false);
			$('#searchprj').prop('disabled', true);
		}else{
			checkprjpkg.removeClass('el-check')
			checkprjpkg.addClass('el-check-empty')
			$('#prvprjpkg').prop('disabled', true);
			$('#browsepkg').prop('disabled', true);
		}
	});
	
	$( "#prvprjnum" ).focus(function() {
  		$('#searchprjgood').hide();
  		$('#searchprjbad').hide();
  		$('#searchprjnoaccess').hide();
  		$('.glyphicon-search').show();
  		$('#searchprj').prop('disabled', false);
  		$(".next2").prop('disabled', true);
	});
	
	$( "#tokenstring" ).focus(function() {
  		$('#searchprjgood').hide();
  		$('#searchprjbad').hide();
  		$('#searchprjnoaccess').hide();
  		$('.glyphicon-search').show();
  		$('#searchprj').prop('disabled', false);
  		$(".next2").prop('disabled', true);
	});

	$("#searchprj").click(function() {
		var prjnum = $('#prvprjnum').val().trim();
		var token = $('#tokenstring').val().trim();
		if(prjnum) {
			$(".checkprjpkg").css('color', '#bbb');
			$(".checkprjpkg").css('cursor', 'not-allowed');
			$(".checkprjpkg").css("pointer-events", "none");
			
			$(".checkprjnum").css('color', '#888');
			$(".checkprjnum").css('cursor', 'not-allowed');
			$(".checkprjnum").css("pointer-events", "none");
			
			$('.glyphicon-search').hide();
			cl3.show();
			$('#prvprjnum').prop('disabled', true);
			$('#tokenstring').prop('disabled', true);
			$.ajax({
            url: 'searchprj',
            type: "POST",
            data: 'prjid=' + prjnum + '&token=' + token,
            dataType: 'json',
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
              },
                 
              success: function(data) {
              	    cl3.hide();
            	  	$('#searchprjgood').show();
            	  	$("#searchprj").prop('disabled', true);
					$('#prvprjnum').prop('disabled', true);
					$('#tokenstring').prop('disabled', true);
					$(".next2").prop('disabled', false);
					
					$(".checkprjnum").css('color', '#888');
					$(".checkprjnum").css('cursor', 'not-allowed');
					$(".checkprjnum").off();
					
              	    prjid = prjnum;
              	    subname = data.raprojectsubmissionname;
              	    
              	  	prvprjinfo.push(data.raprojectsourcelanguagecode);
              	  	prvprjinfo.push(data.raprojecttargetlanguagecode);
              	  	prvprjinfo.push(data.rasourcefilename);
              	  	prvprjinfo.push(data.ratargetfilename);
              	  	prvprjinfo.push(data.rasourcefilesize);
              	  	prvprjinfo.push(data.ratargetfilesize);
              	  	prvprjinfo.push(data.isaligned);
              	  	prvprjinfo.push(data.isoutfortrans);
              	  	prvprjinfo.push(data.alignerbasetype);
              	  	prvprjinfo.push(data.alignertype);
              	  	prvprjinfo.push(data.isautosavedfileexist);
              	  	
              	  	$('.prjitem').prop('disabled', false);
              	  	$('.prjname').removeClass("prjloaded");
              	  	$( ".prjname:contains('" + prjid + "')" ).addClass("prjloaded");
              	  	$('.prjlist').css('opacity', '1.0');
              },
              
              error: function(xhr) {
              	    $(".checkprjpkg").css('color', '');
					$(".checkprjpkg").css('cursor', '');
					$(".checkprjpkg").css("pointer-events", "auto");
					
					$(".checkprjnum").css('color', '');
					$(".checkprjnum").css('cursor', '');
					$(".checkprjnum").css("pointer-events", "auto");
					
    				var error = xhr.responseText;
            	    cl3.hide();
            	  	
            	  	$("#searchprj").prop('disabled', true);
					$('#prvprjnum').prop('disabled', false);
					$('#tokenstring').prop('disabled', false);
					
            	    if(error == 'cannot access'){
            	    	$('#searchprjnoaccess').show();
            	    }else if(error == 'cannot find'){
            	    	$('#searchprjbad').show();
            	    }else{
            	    	$('#searchprjbad').show();
            	    }
            	    
            	    $('.prjitem').prop('disabled', false);
            	    $('.prjname').removeClass("prjloaded");
            	    $('.prjlist').css('opacity', '1.0');
                }
        	});
		}else {
			alert('Please enter project number first!');
		}
	});
	
	$(".next2").click(function() {
		var checkprjnum = $('.checkprjnum');
		var checkprjpkg = $('.checkprjpkg');
		
		$('.modal-dialog').animate({'left':''});
		$('.prjlist').animate({
			'left':'-50%'},{
			complete: function() {
				$('.projects').addClass("disabled");
				$("#continueproject").modal("hide");
				
				var langspath = "../langs/langs.txt";
				setTimeout(function(){
					$("#projectid").text(prjid);
					$("#ctprojectid").text(" - " + prjid);
					$("#submission").text(subname);
					$.get(langspath, function(data) {
		      			langs = data.split('#');
		      			var dropdowns = $(".langs");
		  				for (var i = 0; i < dropdowns.length; i++) {
		    				var dropdown = dropdowns[i];
		    				for(var j = 0; j < langs.length; j++) {
		    					$(dropdown).append($("<li><a href='#'>" + langs[j] + "</a></li>"))
		    				}
						}
						
						if(prvprjinfo[0]){
							src_lang = prvprjinfo[0];
							$("#src_lang").prev().html(prvprjinfo[0] + '&nbsp;&nbsp;&nbsp;&nbsp;<span class="caret"></span>');
							$("#src_lang").prev().prop('disabled', true);
						}
						if(prvprjinfo[1]){
							trg_lang = prvprjinfo[1];
							$("#trg_lang").prev().html(prvprjinfo[1] + '&nbsp;&nbsp;&nbsp;&nbsp;<span class="caret"></span>');
							$("#trg_lang").prev().prop('disabled', true);
						}
							
						if(prvprjinfo[3] && prvprjinfo[5]){
							$("#tbheader").after(
								$('<tr id="tr_trg"/>')
		            				.append($('<td/>').text('Target'))
		            				.append($('<td/>').text(prvprjinfo[3]))
		            				.append($('<td/>').text(prvprjinfo[5]))
		            				.append($('<td/>').append($('<span id="stats_trg" class="stats"/>').text('uploaded')))
		            				.append($('<td/>').append($('<span id="action_trg"/>').html("<a class='remove' href='del/target'>Remove</a>")))
		            				.append($('<td/>').append($('<div class="progress">').append($('<div id = "progress_trg" class="progress-bar progress-bar-striped" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 100%;">'))))
							);
							$('#addtrg').prop('disabled', true);
						}else{
							$('#addtrg').prop('disabled', false);
						}
								
						if(prvprjinfo[2] && prvprjinfo[4]){
							$("#tbheader").after(
								$('<tr id="tr_src"/>')
		            				.append($('<td/>').text('Source'))
		            				.append($('<td/>').text(prvprjinfo[2]))
		            				.append($('<td/>').text(prvprjinfo[4]))
		            				.append($('<td/>').append($('<span id="stats_src" class="stats"/>').text('uploaded')))
		            				.append($('<td/>').append($('<span id="action_src"/>').html("<a class='remove' href='del/source'>Remove</a>")))
		            				.append($('<td/>').append($('<div class="progress">').append($('<div id = "progress_src" class="progress-bar progress-bar-striped" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 100%;">'))))
							);
							$('#addsrc').prop('disabled', true);
						}else{
							$('#addsrc').prop('disabled', false);
						}
								
						$("#prjreview").html('&nbsp;<div class="icon icon-file-symlink-directory"></div><div class="headtext">&nbsp;CONTINUE PREVIOUS PROJECT</div>');
						$('#progress_all').css('width','100%');
		            	$('.show').text('100% Complete');
		            	
		            	$('#submission').prop('disabled', true);	
		            	if(prvprjinfo[6] == "true"){
		            		$('#action_src').text("N/A");
		        			$('#action_trg').text("N/A");
		            		$('#alignsegoption').show();
		            		if(typeof prvprjinfo[8] != 'undefined'){
		            			if(prvprjinfo[8] == "paragraph"){
		            				$('.aligntype').removeClass('el-check');
									$('.aligntype').addClass('el-check-empty');
		            			}

		            			$('.aligntype').css('pointer-events', 'none');
								$('.aligntype').css('opacity', '0.6');
		            		}
		            		
		            		if(typeof prvprjinfo[9] != 'undefined'){
		            			if(prvprjinfo[9] == "auto"){
		            				$("input[name='alignertype'][value='auto']").prop('checked', true);
		            			}else{
		            				$("input[name='alignertype'][value='sequential']").prop('checked', true);
		            			}
		            		}
		            		
		            		if(typeof prvprjinfo[10] != 'undefined'){
		            			if(prvprjinfo[10] !== "true"){
		            				$('.useautosave').css('pointer-events', 'none');
									$('.useautosave').css('opacity', '0.6');
		            			}else{
		            				$('.useautosave').css('pointer-events', 'auto');
									$('.useautosave').css('opacity', '1.0');
									$('.useautosave').removeClass('el-check-empty');
									$('.useautosave').addClass('el-check');
		            			}
		            		}
		            		
		            		$('#goAlignment').prop('disabled', false);
		            		$('#addsrc').prop('disabled', true);
		            		$('#addtrg').prop('disabled', true);
		            		$("input[name='alignertype'").prop('disabled', true);
		            	}else{
		            		if(prvprjinfo[3] && prvprjinfo[5] && prvprjinfo[2] && prvprjinfo[4]){
		            			$('.processfiles').prop('disabled', false);
		            			$("input[name='alignertype'").prop('disabled', false);
		            		}else{
		            			$('#submission').prop('disabled', false);
		            		}
		            	}
		            		
		            	if(prvprjinfo[7] == "true"){
		            		$('.cornerbtn').removeClass("nready");
		            		$('.cornerbtn').addClass("yready");
		            	}

						$("#uploadModal").modal({backdrop: false});
						$("#uploadModal").modal({show: true});

		  			}, 'text');
				},300);
			}
		});
		
		
		
		
	});
	
	$('#browsepkg').on('click', function() { $('#fileuploadpkg').click();return false;});
	
	$('#validatepkg').on('click', function () {
		$('.glyphicon-hand-left').hide();
		cl4.show();
		$('#prvprjpkg').prop('disabled', true);
		$('#browsepkg').prop('disabled', true);
		$('#validatepkg').prop('disabled', true);
		
		var prjinfo = [];
		var time = $.now();
		var id = makeid();
		prjid = "RA" + "#" + time + id;
		var dt = new Date();
		prj_creation_time = dt.getFullYear() + "/" + ('0' + (dt.getMonth()+1)).slice(-2) + "/" + ('0' + dt.getDate()).slice(-2) + " " + ('0' + dt.getHours()).slice(-2) + ":" + ('0' + dt.getMinutes()).slice(-2) + ":" + ('0' + dt.getSeconds()).slice(-2);
		prjinfo[0] = prjid;
		prjinfo[1] = prj_creation_time;
		prjinfo[2] = "X";
		prjinfo[3] = "X";
		
		$.ajax({
            url: 'sendprjinfo',
            type: "POST",
            data: JSON.stringify(prjinfo),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {              	  	
              	  	if($('#prvprjpkg').val()){
					$.each(data_pkg, function (index, file) {
		            		file.submit();
		        		});
					}
              },
              
              error: function() {
              	  cl4.hide();
              	  $('#validateprjbad').show();
              }
        });

	});

    $('.fuploadpkg').fileupload({
    	dataType: 'text',
    	url: 'upload',
        autoUpload: false,
        acceptFileTypes: /(\.|\/)(ra)$/i,
        maxFileSize: 999000,
		add: function (e, data) {
			data_pkg_size = data.files[0].size;
			data_pkg.pop();
			data_pkg.push(data);
			$('#prvprjpkg').val(data.files[0].name);
			$('#validateprjgood').hide();
			$('#validateprjbad').hide();
			$('.glyphicon-hand-left').show();
			$('#validatepkg').prop('disabled', false);
			$('.next2').prop('disabled', true);
		},
		
        done: function (e, data) {
        	var result =  data.result;
            var obj = jQuery.parseJSON(result);
        	prvprjinfo.push(obj.raprojectsourcelanguagecode);
      	  	prvprjinfo.push(obj.raprojecttargetlanguagecode);
      	  	prvprjinfo.push(obj.rasourcefilename);
      	  	prvprjinfo.push(obj.ratargetfilename);
      	  	prvprjinfo.push(obj.rasourcefilesize);
      	  	prvprjinfo.push(obj.ratargetfilesize);
      	  	prvprjinfo.push(obj.isaligned);
      	  	prvprjinfo.push(obj.isoutfortrans);
			cl4.hide();
			$('#validateprjgood').show();
			$('#prvprjpkg').prop('disabled', false);
			$('#browsepkg').prop('disabled', false);
			$(".next2").prop('disabled', false);
        },
 		
 		fail: function () {
 			cl4.hide();
 			$('#validateprjbad').show();
 			$('#browsepkg').prop('disabled', false);
    	},

        dropZone: $('#dropzone')
    });
    	
    $(window).focus(function(e) {
    	validateSession();
	});
	
	$('.backtomain').on('click', function () {
		$.ajax({
              url: 'backtomain',
              type: "POST",
                 
              success: function() {
              	  prjid = null;
              	  $(window).unbind("beforeunload");
				  location.reload();
              },
              
              error: function() {
            	  alert('failed to get back to main menu, please re-login');
                }
        	});
		});
	
	$(window).on('beforeunload', function(){
		if(stompClient != null){stompClient.disconnect();}
		if(typeof prjid == 'undefined' || gotoaligner) {
			gotoaligner = false;
			return;
		}
		
		$.ajax({
				url: '/RevAligner/rac/cancelsession',
				//url: '/RevAligner/rac/logout',
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
	
	$("body").on("keydown",".dropdown-toggle", function(){
    	if($(this).parent().hasClass("open")){
    		var keycode = (event.keyCode ? event.keyCode : event.which);
    		var id = $($(this).next()).attr('id');
    		if(keycode == '38'){
    			if(id == 'src_lang'){
    				if(src_lang){
    					for(var i = 0; i < langs.length; i++){
			    			if((src_lang == langs[i]) && (i > 0)){
			    				var item = $('#src_lang').children('li')[(i+2)];
			    				var item_prev = $('#src_lang').children('li')[(i+1)];
			    				$(item).css('background-color','#ffffff');
			    				$(item_prev).css('background-color','#f6f6f6');
			    				
			    				var wintop = $('#src_lang').offset().top;
			    				var itop = $(item_prev).offset().top;
			    				
			    				var winbot = $('#src_lang').offset().top + $('#src_lang').height();
			    				var ibot = $(item_prev).offset().top + $(item_next).height();
			    				
			    				if(itop < wintop){
			    					var aim_sp = itop - wintop + $('#src_lang').scrollTop();
			    					$('#src_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}else if(winbot < ibot){
			    					var aim_sp = ibot - winbot + $('#src_lang').scrollTop() + $('#src_lang').height();
			    					$('#src_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}
			    				
			    				$(this).html(langs[i-1] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
			    				src_lang = langs[i-1];
			    				break;
			    			}
			    		}
    				}
	    		}else{
	    			if(trg_lang){
	    				for(var i = 0; i < langs.length; i++){
			    			if((trg_lang == langs[i]) && (i > 0)){
			    				var item = $('#trg_lang').children('li')[i];
			    				var item_prev = $('#trg_lang').children('li')[(i-1)];
			    				$(item).css('background-color','#ffffff');
			    				$(item_prev).css('background-color','#f6f6f6');
			    				var wintop = $('#trg_lang').offset().top;
			    				var itop = $(item_prev).offset().top;
			    				
			    				var winbot = $('#trg_lang').offset().top + $('#trg_lang').height();
			    				var ibot = $(item_prev).offset().top + $(item_next).height();
			    				
			    				if(itop < wintop){
			    					var aim_sp = itop - wintop + $('#trg_lang').scrollTop();
			    					$('#trg_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}else if(winbot < ibot){
			    					var aim_sp = ibot - winbot + $('#trg_lang').scrollTop() + $('#trg_lang').height();
			    					$('#trg_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}
			    				
			    				$(this).html(langs[i-1] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
			    				trg_lang = langs[i-1];
			    				break;
			    			}
			    		}
	    			}
	    		}
    		}else if(keycode == '40'){
    			if(id == 'src_lang'){
    				if(src_lang){
    					for(var i = 0; i < langs.length; i++){
			    			if((src_lang == langs[i]) && (i < (langs.length-1))){
			    				var item = $('#src_lang').children('li')[(i+2)];
			    				var item_next = $('#src_lang').children('li')[(i+3)];
			    				$(item).css('background-color','#ffffff');
			    				$(item_next).css('background-color','#f6f6f6');
			    				
			    				var winbot = $('#src_lang').offset().top + $('#src_lang').height();
			    				var ibot = $(item_next).offset().top + $(item_next).height();
			    				
			    				var wintop = $('#src_lang').offset().top;
			    				var itop = $(item_next).offset().top;
			    				
			    				if(winbot < ibot){
			    					var aim_sp = ibot - winbot + $('#src_lang').scrollTop();
			    					$('#src_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}else if(itop < wintop){
			    					var aim_sp = itop - wintop + $('#src_lang').scrollTop() - $('#src_lang').height() + $(item_next).height();
			    					$('#src_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}
			    				
			    				$(this).html(langs[i+1] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
			    				src_lang = langs[i+1];
			    				break;
			    			}
			    		}
    				}else{
    					var item_next = $('#src_lang').children('li')[2];
			    		$(item_next).css('background-color','#f6f6f6');
			    		
			    		var winbot = $('#src_lang').offset().top + $('#src_lang').height();
			    		var ibot = $(item_next).offset().top + $(item_next).height();
			    		
			    		var wintop = $('#src_lang').offset().top;
			    		var itop = $(item_next).offset().top;
			    		
			    		if(winbot < ibot){
			    			var aim_sp = ibot - winbot + $('#src_lang').scrollTop();
			    			$('#src_lang').animate({
								scrollTop: aim_sp
							}, 100);
			    		}else if(itop < wintop){
			    			var aim_sp = itop - wintop + $('#src_lang').scrollTop() - $('#src_lang').height() + $(item_next).height();
			    			$('#src_lang').animate({
								scrollTop: aim_sp
							}, 100);
			    		}
			    		$(this).html(langs[0] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
			    		src_lang = langs[0];
    				}
	    		}else{
	    			if(trg_lang){
	    				for(var i = 0; i < langs.length; i++){
			    			if((trg_lang == langs[i]) && (i < (langs.length-1))){
			    				var item = $('#trg_lang').children('li')[i];
			    				var item_next = $('#trg_lang').children('li')[(i+1)];
			    				$(item).css('background-color','#ffffff');
			    				$(item_next).css('background-color','#f6f6f6');
			    				
			    				var winbot = $('#trg_lang').offset().top + $('#trg_lang').height();
			    				var ibot = $(item_next).offset().top + $(item_next).height();
			    				
			    				var wintop = $('#trg_lang').offset().top;
			    				var itop = $(item_next).offset().top;
			    				
			    				if(winbot < ibot){
			    					var aim_sp = ibot - winbot + $('#trg_lang').scrollTop();
			    					$('#trg_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}else if(itop < wintop){
			    					var aim_sp = itop - wintop + $('#trg_lang').scrollTop() - $('#trg_lang').height() + $(item_next).height();
			    					$('#trg_lang').animate({
										   scrollTop: aim_sp
									}, 100);
			    				}
			    				
			    				$(this).html(langs[i+1] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
			    				trg_lang = langs[i+1];
			    				break;
			    			}
			    		}
	    			}else{
    					var item_next = $('#trg_lang').children('li')[0];
			    		$(item_next).css('background-color','#f6f6f6');
			    		
			    		var winbot = $('#trg_lang').offset().top + $('#trg_lang').height();
			    		var ibot = $(item_next).offset().top + $(item_next).height();
			    		
			    		var wintop = $('#trg_lang').offset().top;
			    		var itop = $(item_next).offset().top;
			    		
			    		if(winbot < ibot){
			    			var aim_sp = ibot - winbot + $('#trg_lang').scrollTop();
			    			$('#trg_lang').animate({
								scrollTop: aim_sp
							}, 100);
			    		}else if(itop < wintop){
			    			var aim_sp = itop - wintop + $('#trg_lang').scrollTop() - $('#trg_lang').height() + $(item_next).height();
			    			$('#trg_lang').animate({
								scrollTop: aim_sp
							}, 100);
			    		}
			    		
			    		$(this).html(langs[0] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
			    		trg_lang = langs[0];
    				}
	    		}
    		}else{
    			var now = Date.now();
				if((now - lastkeypresstime) > 500){
					tempkeystrokes = "";
				}
				lastkeypresstime = now;
	    		tempkeystrokes += String.fromCharCode(keycode);
	    		var found = false;
	    		if(id == 'src_lang'){
	    			for(var i = 0; i < langs.length; i++){
		    			var item = $('#src_lang').children('li')[(i+2)];
		    			if(langs[i].toLowerCase().startsWith(tempkeystrokes.toLowerCase()) && (!found)){
		    				$(this).html(langs[i] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
		    				$(item).css('background-color','#f6f6f6');
		    				$('#src_lang').animate({
						        scrollTop: ($(item).offset().top - $('#src_lang').offset().top + $('#src_lang').scrollTop())
						    }, 100);
						    found = true;
						    src_lang = langs[i];
						    //break;
		    			}else{
		    				$(item).css('background-color','#ffffff');
		    			}
		    		}
	    		}else{
	    			for(var i = 0; i < langs.length; i++){
		    			var item = $('#trg_lang').children('li')[i];
		    			if(langs[i].toLowerCase().startsWith(tempkeystrokes.toLowerCase()) && (!found)){
		    				$(this).html(langs[i] + "&nbsp;&nbsp;&nbsp;&nbsp;<span class='caret'></span>");
		    				$(item).css('background-color','#f6f6f6');
		    				$('#trg_lang').animate({
						        scrollTop: ($(item).offset().top - $('#trg_lang').offset().top + $('#trg_lang').scrollTop())
						    }, 100);
						    found = true;
						    trg_lang = langs[i];
						    //break;
		    			}else{
		    				$(item).css('background-color','#ffffff');
		    			}
		    		}
	    		}
    		}
    	}
	});
	
	$("body").on("click",".dropdown-toggle", function(){
		if(!$(this).parent().hasClass("open")){
			var id = $($(this).next()).attr('id');
			if(id == 'src_lang'){
				if(src_lang){
					for(var i = 0; i < langs.length; i++){
						var item = $('#src_lang').children('li')[(i+2)];
						if(src_lang == langs[i]){
							$(item).css('background-color','#f6f6f6');
						}else{
							$(item).css('background-color','#ffffff');
						}
					}
				}
			}else{
				if(trg_lang){
					for(var i = 0; i < langs.length; i++){
						var item = $('#trg_lang').children('li')[i];
						if(trg_lang == langs[i]){
							$(item).css('background-color','#f6f6f6');
						}else{
							$(item).css('background-color','#ffffff');
						}
					}
				}
			}
		}else{
			var id = $($(this).next()).attr('id');
			if(id == 'src_lang'){
				if(trg_lang != ""){
					$('#addsrc').prop('disabled', false);
					$('#addtrg').prop('disabled', false);
				}
			}else{
				if(src_lang != ""){
					$('#addsrc').prop('disabled', false);
					$('#addtrg').prop('disabled', false);
				}
			}
		}
	});
	
	$("body").on("click",".projects", function(){
		$(".prjitem").remove();
		$('.prjlist').animate({
			'left':'0%'},{
			complete: function() {
				cl8.show();
				$.ajax({
		            url: 'getprojectlist',
		            type: "GET",
		            dataType: 'json',
		                 
		              beforeSend: function(xhr) {
		                xhr.setRequestHeader("Accept", "application/json");
		                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		              },
		                 
		              success: function(data) {
		              	  cl8.hide();
		              	  prjinfos = data.prjinfo;
		              	  for(var i = 0;i < Math.min(prjinfos.length,30); i++){
		              	  	  var prjinfo = prjinfos[i];
		              	  	  var clone = prj_info_row.clone(true);
		              	  	  $(clone.children('.prjname')[0]).text(prjinfo[0]);
		              	  	  $(clone.children('.prjsubname')[0]).text(prjinfo[5]);
		              	  	  $(clone.children('.prjdate')[0]).text(prjinfo[1]);
		              	  	  $(clone.children('.prjuser')[0]).text(prjinfo[2]);
		              	  	  if(!isadmin){
		              	  	  	  $(clone.find('.prjrelief')[0]).hide();
		              	  	  }
		              	  	  
		              	  	  if(prjinfo[2] == username){
		              	  	  	  $(clone.find('.prjicon')[0]).addClass('fa fa-square-o');
		              	  	  }else{
		              	  	  	  $(clone.find('.prjicon')[0]).addClass('fa fa-clone');
		              	  	  }
							  
							  if(prjinfo[3] == "true"){
							  	  $(clone.children('.prjname')[0]).addClass("prjloaded");
							  }
							  
		              	  	  $('.prjlist').append(clone);
		              	  	  
		              	  	  var container = document.getElementsByClassName("prjprogress")[i];
		              	  	  var bar = new ProgressBar.Line(container, {
							  strokeWidth: 4,
							  easing: 'easeInOut',
							  duration: 1400,
							  trailColor: '#eee',
							  trailWidth: 1,
							  svgStyle: {width: '100%', height: '100%'},
							  text: {
							    style: {
							      // Text color.
							      // Default: same as stroke color (options.color)
							      color: '#222',
							      position: 'relative',
							      right: '0',
							      top: '0px',
							      padding: 0,
							      margin: 0,
							      transform: null
							    },
							    autoStyleContainer: false
							  },
							  step: function(state, bar) {
							  	bar.path.setAttribute('stroke', state.color);
							    bar.setText(Math.round(bar.value() * 100) + ' %');
							  }
							});
							
							var ratio = parseFloat(prjinfo[4]);
							var progress_curr_color = progress_bad_color;
							if(ratio > 0.6 && ratio != 1.0){
								progress_curr_color = progress_middle_color;
							}else if(ratio == 1.0){
								progress_curr_color = progress_done_color;
							}
							var opts = {
								from: { color: progress_bad_color},
								to: { color: progress_curr_color}
							};
							bar.animate(prjinfo[4], opts);  // Number from 0.0 to 1.0
		              	  }
		              	  
		              	  if(prjinfos.length != $('.prjitem').length){
							  $('.prjlist').append(prj_load_row.clone(true));
						  }
		              },
		              
		              error: function() {
		            	  alert('error retrieving project list');
		             }
		        });
			}
		});
		
		$('.modal-dialog').animate({'left':'25%'});
	});
	
	$("body").on("click",".prjloadbutton", function(){
		$('.prjitem').prop('disabled', true);
		$('.prjlist').css('opacity', '0.6');
			
		$('.prjloadbutton').remove();
		$('.loadanime').show();
		setTimeout(function(){
			$('.prjload').remove();
			var count = $('.prjitem').length;

			for(var i = count;i < Math.min(prjinfos.length,(count+30)); i++){
				var prjinfo = prjinfos[i];
				var clone = prj_info_row.clone(true);
				$(clone.children('.prjname')[0]).text(prjinfo[0]);
				$(clone.children('.prjsubname')[0]).text(prjinfo[5]);
				$(clone.children('.prjdate')[0]).text(prjinfo[1]);
				$(clone.children('.prjuser')[0]).text(prjinfo[2]);
				if(!isadmin){
				    $(clone.find('.prjrelief')[0]).hide();
				}

				if(prjinfo[2] == username){
				    $(clone.find('.prjicon')[0]).addClass('fa fa-square-o');
				}else{
				    $(clone.find('.prjicon')[0]).addClass('fa fa-clone');
				}
				
				if(prjinfo[3] == "true"){
					  $(clone.children('.prjname')[0]).addClass("prjloaded");
				}
				
				$('.prjlist').append(clone);
				           	  	  
				var container = document.getElementsByClassName("prjprogress")[i];
				var bar = new ProgressBar.Line(container, {
					strokeWidth: 4,
					easing: 'easeInOut',
					duration: 1400,
					trailColor: '#eee',
					trailWidth: 1,
					svgStyle: {width: '100%', height: '100%'},
					text: {
				  	style: {
				    	// Text color.
				    	// Default: same as stroke color (options.color)
				    	color: '#222',
				    	position: 'relative',
				    	right: '0',
				    	top: '0px',
				    	padding: 0,
				    	margin: 0,
				    	transform: null
				  	},
				  	autoStyleContainer: false
				},
				step: function(state, bar) {
					bar.path.setAttribute('stroke', state.color);
				  	bar.setText(Math.round(bar.value() * 100) + ' %');
				}
				});
									
				var ratio = parseFloat(prjinfo[4]);
				var progress_curr_color = progress_bad_color;
				if(ratio > 0.6 && ratio != 1.0){
					progress_curr_color = progress_middle_color;
				}else if(ratio == 1.0){
					progress_curr_color = progress_done_color;
				}
				var opts = {
					from: { color: progress_bad_color},
					to: { color: progress_curr_color}
				};
				
				bar.animate(prjinfo[4], opts);  // Number from 0.0 to 1.0
			}
			
			if(prjinfos.length != $('.prjitem').length){
				$('.prjlist').append(prj_load_row.clone(true));
			}
			
			$('.prjitem').prop('disabled', false);
			$('.prjlist').css('opacity', '1.0');
		},2000);
	});
	
	
	$("body").on("click",".prjlistclose", function(){
		$('.prjlist').animate({
			'left':'-50%'},{
			complete: function() {
				$('.prjitem').remove();
				prjinfos = [];
				$('.prjload').remove();
			}
		});
		
		$('.modal-dialog').animate({'left':''});
	});
	
	$("body").on("click",".prjitem", function(){
		var icon = $(this).find('.prjicon')[0];
		var prjname = $(this).children('.prjname')[0];
		if(($(icon).hasClass("fa-square-o") || isadmin) && (!$(prjname).hasClass("prjloaded"))){
			$('.prjitem').prop('disabled', true);
			$('.prjlist').css('opacity', '0.6');
			
			$('.prjitem').css('background-color','');
			$(this).css('background-color','#ececec');
			
			$('.prjicon').each(function() {
				if($(this).hasClass("fa-square")){
					$(this).removeClass("fa-square");
					$(this).addClass("fa-square-o");
				}
			});
			
			$(icon).removeClass("fa-square-o");
			$(icon).addClass("fa-square");
			
			if($('.checkprjnum').hasClass('el-check-empty')){
				$('.checkprjnum').click();
			}
			$('#prvprjnum').val($(this).children('.prjname').text());
			$('#tokenstring').val('');
			$("#prvprjnum").focus();
			$('#searchprj').click();
		}else{
			$('.prjitem').css('background-color','');
			$(this).css('background-color','#ececec');
		}
	});
	
	$("body").on("click",".fa-clone", function(){
		$('.prjitem').prop('disabled', true);
		$('.prjlist').css('opacity', '0.6');
		var icon = $(this);
		$(this).addClass('fa-spin');
		
		var prjitem = $(this).closest('.prjitem');
		var ratio = $($($(prjitem).children('.prjprogress')[0]).children('.progressbar-text')[0]).text().split(" ")[0];
		var prjinfo = [];
		prjinfo[0] = $($(prjitem).children('.prjname')[0]).text();
		prjinfo[1] = $($(prjitem).children('.prjuser')[0]).text();
		var dt = new Date();
		var prj_clone_time = dt.getFullYear() + "/" + ('0' + (dt.getMonth()+1)).slice(-2) + "/" + ('0' + dt.getDate()).slice(-2) + " " + ('0' + dt.getHours()).slice(-2) + ":" + ('0' + dt.getMinutes()).slice(-2) + ":" + ('0' + dt.getSeconds()).slice(-2);
		prjinfo[2] = prj_clone_time;
		prjinfo[3] = $($(prjitem).children('.prjsubname')[0]).text();
		
		$.ajax({
            url: 'cloneproject',
            type: "POST",
            data: JSON.stringify(prjinfo),
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader("Content-Type", "application/json");
              },
                 
              success: function() {              	  	
              	  	icon.removeClass('fa-spin');
					$('.prjlist').css('opacity', '1.0');
					$('.prjitem').prop('disabled', false);
					var trgprjname = prjinfo[0] + "(C)";
					
					var clone = prj_info_row.clone(true);
		            $(clone.children('.prjname')[0]).text(trgprjname);
		            $(clone.children('.prjdate')[0]).text(prj_clone_time);
		            $(clone.children('.prjuser')[0]).text(username);
		            $(clone.children('.prjsubname')[0]).text(prjinfo[3]);
		            $(clone.find('.prjicon')[0]).addClass('fa fa-square-o');
		            setTimeout(function(){
		            	$('.prjlist').animate({scrollTop:0}, 300);
		            	var firstprjitem = $('.prjitem')[0];
						$(clone).insertBefore(firstprjitem);
						
						var container = document.getElementsByClassName("prjprogress")[0];
		              	var bar = new ProgressBar.Line(container, {
							strokeWidth: 4,
							easing: 'easeInOut',
							duration: 1400,
							trailColor: '#eee',
							trailWidth: 1,
							svgStyle: {width: '100%', height: '100%'},
							text: {
							  style: {
							  	// Text color.
							  	// Default: same as stroke color (options.color)
							  	color: '#222',
							  	position: 'relative',
							  	right: '0',
							  	top: '0px',
							  	padding: 0,
							  	margin: 0,
							  	transform: null
							  },
							  autoStyleContainer: false
							},
							step: function(state, bar) {
								bar.path.setAttribute('stroke', state.color);
								bar.setText(Math.round(bar.value() * 100) + ' %');
							}
						});
						
						
						var ratioF = parseFloat(ratio/100);
						var progress_curr_color = progress_bad_color;
						if(ratioF > 0.6 && ratioF != 1.0){
							progress_curr_color = progress_middle_color;
						}else if(ratioF == 1.0){
							progress_curr_color = progress_done_color;
						}
						var opts = {
							from: { color: progress_bad_color},
							to: { color: progress_curr_color}
						};
						bar.animate(ratioF.toString(), opts);  // Number from 0.0 to 1.0
				  },300);
              },
              
              error: function() {
              	  icon.removeClass('fa-spin');
				  $('.prjlist').css('opacity', '1.0');
				  $('.prjitem').prop('disabled', false);	
              	  alert('failed to clone project');
              }
        });
	});
	
	$("body").on("click",".prjrelief", function(e){
		e.stopPropagation();
		
		$('.prjitem').prop('disabled', true);
		$('.prjlist').css('opacity', '0.6');
		var icon = $(this);
		$(this).addClass('fa-spin');
		
		var prjitem = $(this).closest('.prjitem');
		var prjnum = $($(prjitem).children('.prjname')[0]).text();
		var user = $($(prjitem).children('.prjuser')[0]).text();
		var value = user + "_" + prjnum;

		$.ajax({
            url: 'reliefproject',
            type: "POST",
            data: 'value=' + value,
            dataType: 'json',
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "application/json");
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
              },
                 
              success: function() {              	  	
              	  icon.removeClass('fa-spin');
              	  $($(prjitem).children('.prjname')[0]).removeClass("prjloaded");
				  $('.prjlist').css('opacity', '1.0');
				  $('.prjitem').prop('disabled', false);
              },
              
              error: function() {
              	  icon.removeClass('fa-spin');
				  $('.prjlist').css('opacity', '1.0');
				  $('.prjitem').prop('disabled', false);	
              	  alert('failed to close project');
              }
        });
	});
	
	function redirectwithpost(redirectUrl) {
		var form = $('<form action="' + redirectUrl + '" method="post">' + '<input type="hidden"' + '"></input>' + '</form>');
		$('body').append(form);
		$(form).submit();
	};
		
	function downloadFromURL(url, callback){
	   	var hiddenIFrameID = 'hiddenDownloader0';
	   	var iframe = document.createElement('iframe');
	   	iframe.id = hiddenIFrameID;
	   	iframe.style.display = 'none';
	   	document.body.appendChild(iframe);
	   	iframe.src = url;
	   	callback();
	}
		
	function validateSession() {
		if(typeof prjid == 'undefined') {
			return;
		}
		
		$.ajax({
            url: 'checkifsessionvalid',
            type: "GET",
            dataType: 'text',
                 
              beforeSend: function(xhr) {
                xhr.setRequestHeader("Accept", "text/plain");
                xhr.setRequestHeader("Content-Type", "text/plain");
              },
                 
              success: function(data) {
				    
              },
              
              error: function(xhr) {
              	  	if(stompClient != null){stompClient.disconnect();}
              	  	
					$(window).unbind("beforeunload");
					redirectwithpost('/RevAligner/rac/sessiontimesout');
					clearInterval(intervalId);
              }
        });
	}
});