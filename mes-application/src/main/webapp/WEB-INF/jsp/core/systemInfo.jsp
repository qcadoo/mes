<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<style type="text/css">
	#systemInfoContent {
		margin: 20px;
		font-family:Arial, Helvetica, sans-serif;
	}
	#systemInfoContent #systemInfoHeader {
		font-size: 16px;
		font-weight: bold;
		margin-bottom: 20px;
	}
	#systemInfoContent .systemInfoItem {
		margin-top: 10px;
		margin-left: 20px;
		font-size: 13px;
	}
	#systemInfoContent .systemInfoItem .systemInfoItemLabel {
		display: inline-block;
		width: 100px;
		color: #616161;
	}
	#systemInfoContent .systemInfoItem .systemInfoItemValue {
		
	}
</style>

<div id="systemInfoContent">

	<div id="systemInfoHeader">
		${translationsMap['core.systemInfo.header']}
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.name.label']}</span>
		<span class="systemInfoItemValue">${applicationName}</span>
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.version.label']}</span>
		<span class="systemInfoItemValue">${applicationVersion}</span>
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.build.label']}</span>
		<span class="systemInfoItemValue">${buildNumber}</span>
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.buildDate.label']}</span>
		<span class="systemInfoItemValue">${buildDate}</span>
	</div>
</div>