<!DOCTYPE HTML>
<html>
<head>
<meta charset="utf-8">
<title>Revision Aligner</title>
<script src="js/jquery.min.js"></script>
<script type="text/javascript">
    $(function(){
    	redirectwithpost('/RevAligner/rac/project');
    	
    	function redirectwithpost(redirectUrl) {
			var form = $('<form action="' + redirectUrl + '" method="post">' + '<input type="hidden"' + '"></input>' + '</form>');
			$('body').append(form);
			$(form).submit();
		};
    });
</script>
</head>
<body>
</body> 
</html>