<!DOCTYPE html>
<html lang="" xmlns:th="http://www.thymeleaf.org">
	<head>
		<meta charset="utf-8"/>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<meta name="viewport" content="width=device-width, initial-scale=1"/>
        <script src="//jwpsrv.com/library/sXkIbkQdEeKWASIACp8kUw.js"></script>
		<title>Title Page</title>

		<!-- Bootstrap CSS -->
		<link th:href="@{/css/finder/index.css}" rel="stylesheet"/>

		<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
		<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
		<![endif]-->
	</head>
	<body>
		<div id="content"></div>
        <script th:src="@{/js/lib/common.min.js}" type="text/javascript"></script>
		<script th:src="@{/js/finder/index.js}" type="text/javascript"></script>
	</body>
</html>