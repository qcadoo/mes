/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

////////////////////////////////////////////////////////////////
		// Javascript made by Rasmus - http://www.peters1.dk //
		////////////////////////////////////////////////////////////////
		var SNOW_Picture = "/img/core/snow6.gif"
		var SNOW_no = 100;
	
		var SNOW_browser_IE_NS = (document.body) ? 1 : 0;
		var SNOW_browser_MOZ = (self.innerWidth) ? 1 : 0;
		var SNOW_browser_IE7 = (document.documentElement.clientHeight) ? 1 : 0;
	
		var SNOW_Time;
		var SNOW_dx, SNOW_xp, SNOW_yp;
		var SNOW_am, SNOW_stx, SNOW_sty; 
		var i, SNOW_Browser_Width, SNOW_Browser_Height;
	
		if (SNOW_browser_IE_NS)
		{
			SNOW_Browser_Width = document.body.clientWidth;
			SNOW_Browser_Height = document.body.clientHeight;
		}
		else if (SNOW_browser_MOZ)
		{
			SNOW_Browser_Width = self.innerWidth - 20;
			SNOW_Browser_Height = self.innerHeight;
		}
		else if (SNOW_browser_IE7)
		{
			SNOW_Browser_Width = document.documentElement.clientWidth;
			SNOW_Browser_Height = document.documentElement.clientHeight;
		}
	
		SNOW_dx = new Array();
		SNOW_xp = new Array();
		SNOW_yp = new Array();
		SNOW_am = new Array();
		SNOW_stx = new Array();
		SNOW_sty = new Array();
	
	
		function SNOW_Weather(initialize) { 
			if (initialize) {
				for (i = 0; i < SNOW_no; ++ i) { 
					SNOW_dx[i] = 0; 
					SNOW_xp[i] = Math.random()*(SNOW_Browser_Width-50);
					SNOW_yp[i] = Math.random()*SNOW_Browser_Height;
					SNOW_am[i] = Math.random()*20; 
					SNOW_stx[i] = 0.02 + Math.random()/10;
					SNOW_sty[i] = 0.7 + Math.random();
					var div = $("<div>").attr("id", "SNOW_flake"+ i).attr("style", "position: absolute; z-index: "+ i +"; visibility: visible; top: 15px; left: 15px;");
					var img = $("<img>").attr("src", SNOW_Picture).attr("border", "0");
					div.append(img);
					$("body").append(div);
				}
			}
			
			for (i = 0; i < SNOW_no; ++ i) { 
				SNOW_yp[i] += SNOW_sty[i];
		
				if (SNOW_yp[i] > SNOW_Browser_Height-50) {
					SNOW_xp[i] = Math.random()*(SNOW_Browser_Width-SNOW_am[i]-30);
					SNOW_yp[i] = 0;
					SNOW_stx[i] = 0.02 + Math.random()/10;
					SNOW_sty[i] = 0.7 + Math.random();
				}
		
				SNOW_dx[i] += SNOW_stx[i];
		
				document.getElementById("SNOW_flake"+i).style.top=SNOW_yp[i]+"px";
				document.getElementById("SNOW_flake"+i).style.left=SNOW_xp[i] + SNOW_am[i]*Math.sin(SNOW_dx[i])+"px";
			}
		
			SNOW_Time = setTimeout("SNOW_Weather(false)", 10);
	
		}
		
		jQuery(document).ready(function(){
			
			if (QCD && QCD.global && QCD.global.isSonowOnPage) {
				SNOW_Weather(true); 
			}
		});