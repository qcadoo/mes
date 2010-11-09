<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<style type="text/css">
	#systemInfoContent {
		margin: 20px;
	}
	#systemInfoContent #systemInfoHeader {
		color: red;
	}
</style>

<div id="systemInfoContent">

	<div id="systemInfoHeader">
		SYSTEM INFO:
	</div>
	<div>
		nazwa: ${applicationName}
	</div>
	<div>
		wersja: ${applicationVersion}
	</div>
	<div>
		build: ${buildNumber}
	</div>
	<div>
		data buildu: ${buildDate}
	</div>
</div>