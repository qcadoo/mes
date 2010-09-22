<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String ctx = request.getContextPath();
%>

<html>
    <head>
        <title>Instalacja modułów</title>
    </head>
    <body>
        <h1>Instalacja modułów</h1>
        <div id="errorBox">
        </div>
            <script type="text/javascript">
		
			
				function getUrlVars(){
					var vars = [], hash;
					var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
					for(var i = 0; i < hashes.length; i++){
						hash = hashes[i].split('=');
						vars.push(hash[0]);
						vars[hash[0]] = hash[1];
					}
			
					return vars;
				}
		
				var hash = getUrlVars();
				
				if (hash['error'] == 1) {
					document.getElementById('errorBox').innerHTML="Błąd instalacji";
				} else if(hash['error'] == 2) {
					document.getElementById('errorBox').innerHTML="Wersja jest zainstalowana";
				} else if(hash['error'] == 3) {
					document.getElementById('errorBox').innerHTML="Przekroczono wielkość pliku";
				}
		
			</script>
        
		<input type="button" value="Pobierz" name="download" onclick="location.href='upload.html'"/>
		<form method="get" action="<%=ctx%>/remove.html">
            <input type="text" name="entityId"/>
            <input type="submit" value="Usuń"/>
        </form>
		<input type="button" value="Instaluj pobrane moduły" name="restart" onclick="location.href='restart.html'" />
		<form method="get" action="<%=ctx%>/deinstall.html">
			<input type="text" name="entityId"/>
            <input type="text" name="codeId"/>
            <input type="submit" value="Odinstaluj"/>
        </form>
    </body>
</html>