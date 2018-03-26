$(function(){
	  $('.nav > li').mouseenter(function() {
	  	  	$('.navbar-brand').stop(true, true);
    		var description = $( this ).attr('description');
    		$('.navbar-brand').text("").fadeOut(0)
    		$('.navbar-brand')[0].style.setProperty('color', '#888' ,'important');
        	$('.navbar-brand').text(description).fadeIn(500);
  		}
	  );
	  
	  $('.nav > li').mouseleave(function() {
	  	  	$('.navbar-brand').stop(true, true);
    		$('.navbar-brand').text("").fadeOut(0)
    		$('.navbar-brand')[0].style.setProperty('color', '#fff' ,'important');
        	$('.navbar-brand').text("REVISION ALIGNER").fadeIn(1000);
  		}
	  );
	  
	  $('.nav > #prjprogress').mouseenter(function() {
	  	  	$('.navbar-brand').stop(true, true);
    		var description = $( this ).attr('description');
    		$('.navbar-brand').text("").fadeOut(0)
    		$('.navbar-brand')[0].style.setProperty('color', '#888' ,'important');
        	$('.navbar-brand').text(description).fadeIn(500);
  		}
	  );
	  
	  $('.nav > #prjprogress').mouseleave(function() {
	  	  	$('.navbar-brand').stop(true, true);
    		$('.navbar-brand').text("").fadeOut(0)
    		$('.navbar-brand')[0].style.setProperty('color', '#fff' ,'important');
        	$('.navbar-brand').text("REVISION ALIGNER").fadeIn(1000);
  		}
	  );
	  
	  $('#emailadd').mouseenter(function() {
	  	  	$(this).css("color", "#ADD8E6");
  		}
	  );
	  
	  $('#emailadd').mouseleave(function() {
	  	  	$(this).css("color", "#1E90FF");
  		}
	  );
	  
      $('body').on('click', '.accept', function() {
      	var menu = $(this).parent();
      	var icon_viewall = $(menu.children('.viewall')[0]).children('.el')[0];
      	$(icon_viewall).css("color", "#444444");
      	var icon_accept = $(menu.children('.accept')[0]).children('.el')[0];
      	$(icon_accept).css("color", "#009999");
      	var icon_reject = $(menu.children('.reject')[0]).children('.el')[0];
      	$(icon_reject).css("color", "");
      	
  		var drag = $($(this).closest('.src_segment').children('drag')[0]);
  		var accepts = drag.children('ins');
  		for (var i = 0; i < accepts.length; i++) {
    		accepts[i].style.display = "inline";
    		accepts[i].style.textDecoration = "none";
			}
		
		var rejects = drag.children('del');
  		for (var i = 0; i < rejects.length; i++) {
    		rejects[i].style.display = "none";
			}
			
		});
		
      $('body').on('click', '.reject', function() {
      	var menu = $(this).parent();
      	var icon_reject = $(menu.children('.reject')[0]).children('.el')[0];
      	$(icon_reject).css("color", "#990000");
      	var icon_accept = $(menu.children('.accept')[0]).children('.el')[0];
      	$(icon_accept).css("color", "");
      	var icon_viewall = $(menu.children('.viewall')[0]).children('.el')[0];
      	$(icon_viewall).css("color", "#444444");
      	
  		var drag = $($(this).closest('.src_segment').children('drag')[0]);
  		var accepts = drag.children('ins');
  		for (var i = 0; i < accepts.length; i++) {
    		accepts[i].style.display = "none";
			}
		
		var rejects = drag.children('del');
  		for (var i = 0; i < rejects.length; i++) {
			rejects[i].style.display = "inline";
			rejects[i].style.textDecoration = "none";
			}
			
		});
		
	  $('body').on('click', '.viewall', function() {
	  	var menu = $(this).parent();
	  	var icon_viewall = $(menu.children('.viewall')[0]).children('.el')[0];
      	$(icon_viewall).css("color", "#3333ff");
      	var icon_reject = $(menu.children('.reject')[0]).children('.el')[0];
      	$(icon_reject).css("color", "");
      	var icon_accept = $(menu.children('.accept')[0]).children('.el')[0];
      	$(icon_accept).css("color", "");
      	
  		var drag = $($(this).closest('.src_segment').children('drag')[0]);
  		var accepts = drag.children('ins');
  		for (var i = 0; i < accepts.length; i++) {
    		accepts[i].style.display = "inline";
    		accepts[i].style.textDecoration = "underline";
			}
		
		var rejects = drag.children('del');
  		for (var i = 0; i < rejects.length; i++) {
			rejects[i].style.display = "inline";
			rejects[i].style.textDecoration = "line-through";
			}
			
		});

		$('body').on('click', '.restore', function() {
      		var menu = $(this).parent();
      		var icon_restore = $(menu.children('.restore')[0]).children('.el')[0];
      		var rmvtrgseg = $(this).closest('.rmv_trg_segment');
      		if($(this).hasClass("selectedrestore")) {
      			$(this).removeClass("selectedrestore");
      			$(rmvtrgseg).removeClass("selectedrestoregrid");
      		} else {
      			$(".selectedrestore").removeClass( "selectedrestore" );
      			$(".selectedrestoregrid").removeClass( "selectedrestoregrid" );
      				
      			$(this).addClass("selectedrestore");
      			$(rmvtrgseg).addClass("selectedrestoregrid");
      		}
		});
		
		$('body').on('click', '.moretrgrestore', function() {
      		var menu = $(this).parent();
      		var icon_insertback = $(menu.children('.moretrgrestore')[0]).children('.el')[0];
      		var insertbacktrgseg = $(this).closest('.more_trg_segment');
      		if($(this).hasClass("selectedrestore")) {
      			$(this).removeClass("selectedrestore");
      			$(insertbacktrgseg).removeClass("selectedrestoregrid");
      		} else {
      			$(".selectedrestore").removeClass( "selectedrestore" );
      			$(".selectedrestoregrid").removeClass( "selectedrestoregrid" );
      				
      			$(this).addClass("selectedrestore");
      			$(insertbacktrgseg).addClass("selectedrestoregrid");
      		}
		});
		
		$('body').on('click', '#emailadd', function() {
      		alert("For any question, please email to 'mxiang@transperfect.com'");
		});
   });