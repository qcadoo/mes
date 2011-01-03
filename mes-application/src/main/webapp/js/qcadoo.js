/*!
 * jQuery JavaScript Library v1.4.2
 * http://jquery.com/
 *
 * Copyright 2010, John Resig
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * Includes Sizzle.js
 * http://sizzlejs.com/
 * Copyright 2010, The Dojo Foundation
 * Released under the MIT, BSD, and GPL Licenses.
 *
 * Date: Sat Feb 13 22:33:48 2010 -0500
 */
(function(A,w){function ma(){if(!c.isReady){try{s.documentElement.doScroll("left")}catch(a){setTimeout(ma,1);return}c.ready()}}function Qa(a,b){b.src?c.ajax({url:b.src,async:false,dataType:"script"}):c.globalEval(b.text||b.textContent||b.innerHTML||"");b.parentNode&&b.parentNode.removeChild(b)}function X(a,b,d,f,e,j){var i=a.length;if(typeof b==="object"){for(var o in b)X(a,o,b[o],f,e,d);return a}if(d!==w){f=!j&&f&&c.isFunction(d);for(o=0;o<i;o++)e(a[o],b,f?d.call(a[o],o,e(a[o],b)):d,j);return a}return i?
e(a[0],b):w}function J(){return(new Date).getTime()}function Y(){return false}function Z(){return true}function na(a,b,d){d[0].type=a;return c.event.handle.apply(b,d)}function oa(a){var b,d=[],f=[],e=arguments,j,i,o,k,n,r;i=c.data(this,"events");if(!(a.liveFired===this||!i||!i.live||a.button&&a.type==="click")){a.liveFired=this;var u=i.live.slice(0);for(k=0;k<u.length;k++){i=u[k];i.origType.replace(O,"")===a.type?f.push(i.selector):u.splice(k--,1)}j=c(a.target).closest(f,a.currentTarget);n=0;for(r=
j.length;n<r;n++)for(k=0;k<u.length;k++){i=u[k];if(j[n].selector===i.selector){o=j[n].elem;f=null;if(i.preType==="mouseenter"||i.preType==="mouseleave")f=c(a.relatedTarget).closest(i.selector)[0];if(!f||f!==o)d.push({elem:o,handleObj:i})}}n=0;for(r=d.length;n<r;n++){j=d[n];a.currentTarget=j.elem;a.data=j.handleObj.data;a.handleObj=j.handleObj;if(j.handleObj.origHandler.apply(j.elem,e)===false){b=false;break}}return b}}function pa(a,b){return"live."+(a&&a!=="*"?a+".":"")+b.replace(/\./g,"`").replace(/ /g,
"&")}function qa(a){return!a||!a.parentNode||a.parentNode.nodeType===11}function ra(a,b){var d=0;b.each(function(){if(this.nodeName===(a[d]&&a[d].nodeName)){var f=c.data(a[d++]),e=c.data(this,f);if(f=f&&f.events){delete e.handle;e.events={};for(var j in f)for(var i in f[j])c.event.add(this,j,f[j][i],f[j][i].data)}}})}function sa(a,b,d){var f,e,j;b=b&&b[0]?b[0].ownerDocument||b[0]:s;if(a.length===1&&typeof a[0]==="string"&&a[0].length<512&&b===s&&!ta.test(a[0])&&(c.support.checkClone||!ua.test(a[0]))){e=
true;if(j=c.fragments[a[0]])if(j!==1)f=j}if(!f){f=b.createDocumentFragment();c.clean(a,b,f,d)}if(e)c.fragments[a[0]]=j?f:1;return{fragment:f,cacheable:e}}function K(a,b){var d={};c.each(va.concat.apply([],va.slice(0,b)),function(){d[this]=a});return d}function wa(a){return"scrollTo"in a&&a.document?a:a.nodeType===9?a.defaultView||a.parentWindow:false}var c=function(a,b){return new c.fn.init(a,b)},Ra=A.jQuery,Sa=A.$,s=A.document,T,Ta=/^[^<]*(<[\w\W]+>)[^>]*$|^#([\w-]+)$/,Ua=/^.[^:#\[\.,]*$/,Va=/\S/,
Wa=/^(\s|\u00A0)+|(\s|\u00A0)+$/g,Xa=/^<(\w+)\s*\/?>(?:<\/\1>)?$/,P=navigator.userAgent,xa=false,Q=[],L,$=Object.prototype.toString,aa=Object.prototype.hasOwnProperty,ba=Array.prototype.push,R=Array.prototype.slice,ya=Array.prototype.indexOf;c.fn=c.prototype={init:function(a,b){var d,f;if(!a)return this;if(a.nodeType){this.context=this[0]=a;this.length=1;return this}if(a==="body"&&!b){this.context=s;this[0]=s.body;this.selector="body";this.length=1;return this}if(typeof a==="string")if((d=Ta.exec(a))&&
(d[1]||!b))if(d[1]){f=b?b.ownerDocument||b:s;if(a=Xa.exec(a))if(c.isPlainObject(b)){a=[s.createElement(a[1])];c.fn.attr.call(a,b,true)}else a=[f.createElement(a[1])];else{a=sa([d[1]],[f]);a=(a.cacheable?a.fragment.cloneNode(true):a.fragment).childNodes}return c.merge(this,a)}else{if(b=s.getElementById(d[2])){if(b.id!==d[2])return T.find(a);this.length=1;this[0]=b}this.context=s;this.selector=a;return this}else if(!b&&/^\w+$/.test(a)){this.selector=a;this.context=s;a=s.getElementsByTagName(a);return c.merge(this,
a)}else return!b||b.jquery?(b||T).find(a):c(b).find(a);else if(c.isFunction(a))return T.ready(a);if(a.selector!==w){this.selector=a.selector;this.context=a.context}return c.makeArray(a,this)},selector:"",jquery:"1.4.2",length:0,size:function(){return this.length},toArray:function(){return R.call(this,0)},get:function(a){return a==null?this.toArray():a<0?this.slice(a)[0]:this[a]},pushStack:function(a,b,d){var f=c();c.isArray(a)?ba.apply(f,a):c.merge(f,a);f.prevObject=this;f.context=this.context;if(b===
"find")f.selector=this.selector+(this.selector?" ":"")+d;else if(b)f.selector=this.selector+"."+b+"("+d+")";return f},each:function(a,b){return c.each(this,a,b)},ready:function(a){c.bindReady();if(c.isReady)a.call(s,c);else Q&&Q.push(a);return this},eq:function(a){return a===-1?this.slice(a):this.slice(a,+a+1)},first:function(){return this.eq(0)},last:function(){return this.eq(-1)},slice:function(){return this.pushStack(R.apply(this,arguments),"slice",R.call(arguments).join(","))},map:function(a){return this.pushStack(c.map(this,
function(b,d){return a.call(b,d,b)}))},end:function(){return this.prevObject||c(null)},push:ba,sort:[].sort,splice:[].splice};c.fn.init.prototype=c.fn;c.extend=c.fn.extend=function(){var a=arguments[0]||{},b=1,d=arguments.length,f=false,e,j,i,o;if(typeof a==="boolean"){f=a;a=arguments[1]||{};b=2}if(typeof a!=="object"&&!c.isFunction(a))a={};if(d===b){a=this;--b}for(;b<d;b++)if((e=arguments[b])!=null)for(j in e){i=a[j];o=e[j];if(a!==o)if(f&&o&&(c.isPlainObject(o)||c.isArray(o))){i=i&&(c.isPlainObject(i)||
c.isArray(i))?i:c.isArray(o)?[]:{};a[j]=c.extend(f,i,o)}else if(o!==w)a[j]=o}return a};c.extend({noConflict:function(a){A.$=Sa;if(a)A.jQuery=Ra;return c},isReady:false,ready:function(){if(!c.isReady){if(!s.body)return setTimeout(c.ready,13);c.isReady=true;if(Q){for(var a,b=0;a=Q[b++];)a.call(s,c);Q=null}c.fn.triggerHandler&&c(s).triggerHandler("ready")}},bindReady:function(){if(!xa){xa=true;if(s.readyState==="complete")return c.ready();if(s.addEventListener){s.addEventListener("DOMContentLoaded",
L,false);A.addEventListener("load",c.ready,false)}else if(s.attachEvent){s.attachEvent("onreadystatechange",L);A.attachEvent("onload",c.ready);var a=false;try{a=A.frameElement==null}catch(b){}s.documentElement.doScroll&&a&&ma()}}},isFunction:function(a){return $.call(a)==="[object Function]"},isArray:function(a){return $.call(a)==="[object Array]"},isPlainObject:function(a){if(!a||$.call(a)!=="[object Object]"||a.nodeType||a.setInterval)return false;if(a.constructor&&!aa.call(a,"constructor")&&!aa.call(a.constructor.prototype,
"isPrototypeOf"))return false;var b;for(b in a);return b===w||aa.call(a,b)},isEmptyObject:function(a){for(var b in a)return false;return true},error:function(a){throw a;},parseJSON:function(a){if(typeof a!=="string"||!a)return null;a=c.trim(a);if(/^[\],:{}\s]*$/.test(a.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,"@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,"]").replace(/(?:^|:|,)(?:\s*\[)+/g,"")))return A.JSON&&A.JSON.parse?A.JSON.parse(a):(new Function("return "+
a))();else c.error("Invalid JSON: "+a)},noop:function(){},globalEval:function(a){if(a&&Va.test(a)){var b=s.getElementsByTagName("head")[0]||s.documentElement,d=s.createElement("script");d.type="text/javascript";if(c.support.scriptEval)d.appendChild(s.createTextNode(a));else d.text=a;b.insertBefore(d,b.firstChild);b.removeChild(d)}},nodeName:function(a,b){return a.nodeName&&a.nodeName.toUpperCase()===b.toUpperCase()},each:function(a,b,d){var f,e=0,j=a.length,i=j===w||c.isFunction(a);if(d)if(i)for(f in a){if(b.apply(a[f],
d)===false)break}else for(;e<j;){if(b.apply(a[e++],d)===false)break}else if(i)for(f in a){if(b.call(a[f],f,a[f])===false)break}else for(d=a[0];e<j&&b.call(d,e,d)!==false;d=a[++e]);return a},trim:function(a){return(a||"").replace(Wa,"")},makeArray:function(a,b){b=b||[];if(a!=null)a.length==null||typeof a==="string"||c.isFunction(a)||typeof a!=="function"&&a.setInterval?ba.call(b,a):c.merge(b,a);return b},inArray:function(a,b){if(b.indexOf)return b.indexOf(a);for(var d=0,f=b.length;d<f;d++)if(b[d]===
a)return d;return-1},merge:function(a,b){var d=a.length,f=0;if(typeof b.length==="number")for(var e=b.length;f<e;f++)a[d++]=b[f];else for(;b[f]!==w;)a[d++]=b[f++];a.length=d;return a},grep:function(a,b,d){for(var f=[],e=0,j=a.length;e<j;e++)!d!==!b(a[e],e)&&f.push(a[e]);return f},map:function(a,b,d){for(var f=[],e,j=0,i=a.length;j<i;j++){e=b(a[j],j,d);if(e!=null)f[f.length]=e}return f.concat.apply([],f)},guid:1,proxy:function(a,b,d){if(arguments.length===2)if(typeof b==="string"){d=a;a=d[b];b=w}else if(b&&
!c.isFunction(b)){d=b;b=w}if(!b&&a)b=function(){return a.apply(d||this,arguments)};if(a)b.guid=a.guid=a.guid||b.guid||c.guid++;return b},uaMatch:function(a){a=a.toLowerCase();a=/(webkit)[ \/]([\w.]+)/.exec(a)||/(opera)(?:.*version)?[ \/]([\w.]+)/.exec(a)||/(msie) ([\w.]+)/.exec(a)||!/compatible/.test(a)&&/(mozilla)(?:.*? rv:([\w.]+))?/.exec(a)||[];return{browser:a[1]||"",version:a[2]||"0"}},browser:{}});P=c.uaMatch(P);if(P.browser){c.browser[P.browser]=true;c.browser.version=P.version}if(c.browser.webkit)c.browser.safari=
true;if(ya)c.inArray=function(a,b){return ya.call(b,a)};T=c(s);if(s.addEventListener)L=function(){s.removeEventListener("DOMContentLoaded",L,false);c.ready()};else if(s.attachEvent)L=function(){if(s.readyState==="complete"){s.detachEvent("onreadystatechange",L);c.ready()}};(function(){c.support={};var a=s.documentElement,b=s.createElement("script"),d=s.createElement("div"),f="script"+J();d.style.display="none";d.innerHTML="   <link/><table></table><a href='/a' style='color:red;float:left;opacity:.55;'>a</a><input type='checkbox'/>";
var e=d.getElementsByTagName("*"),j=d.getElementsByTagName("a")[0];if(!(!e||!e.length||!j)){c.support={leadingWhitespace:d.firstChild.nodeType===3,tbody:!d.getElementsByTagName("tbody").length,htmlSerialize:!!d.getElementsByTagName("link").length,style:/red/.test(j.getAttribute("style")),hrefNormalized:j.getAttribute("href")==="/a",opacity:/^0.55$/.test(j.style.opacity),cssFloat:!!j.style.cssFloat,checkOn:d.getElementsByTagName("input")[0].value==="on",optSelected:s.createElement("select").appendChild(s.createElement("option")).selected,
parentNode:d.removeChild(d.appendChild(s.createElement("div"))).parentNode===null,deleteExpando:true,checkClone:false,scriptEval:false,noCloneEvent:true,boxModel:null};b.type="text/javascript";try{b.appendChild(s.createTextNode("window."+f+"=1;"))}catch(i){}a.insertBefore(b,a.firstChild);if(A[f]){c.support.scriptEval=true;delete A[f]}try{delete b.test}catch(o){c.support.deleteExpando=false}a.removeChild(b);if(d.attachEvent&&d.fireEvent){d.attachEvent("onclick",function k(){c.support.noCloneEvent=
false;d.detachEvent("onclick",k)});d.cloneNode(true).fireEvent("onclick")}d=s.createElement("div");d.innerHTML="<input type='radio' name='radiotest' checked='checked'/>";a=s.createDocumentFragment();a.appendChild(d.firstChild);c.support.checkClone=a.cloneNode(true).cloneNode(true).lastChild.checked;c(function(){var k=s.createElement("div");k.style.width=k.style.paddingLeft="1px";s.body.appendChild(k);c.boxModel=c.support.boxModel=k.offsetWidth===2;s.body.removeChild(k).style.display="none"});a=function(k){var n=
s.createElement("div");k="on"+k;var r=k in n;if(!r){n.setAttribute(k,"return;");r=typeof n[k]==="function"}return r};c.support.submitBubbles=a("submit");c.support.changeBubbles=a("change");a=b=d=e=j=null}})();c.props={"for":"htmlFor","class":"className",readonly:"readOnly",maxlength:"maxLength",cellspacing:"cellSpacing",rowspan:"rowSpan",colspan:"colSpan",tabindex:"tabIndex",usemap:"useMap",frameborder:"frameBorder"};var G="jQuery"+J(),Ya=0,za={};c.extend({cache:{},expando:G,noData:{embed:true,object:true,
applet:true},data:function(a,b,d){if(!(a.nodeName&&c.noData[a.nodeName.toLowerCase()])){a=a==A?za:a;var f=a[G],e=c.cache;if(!f&&typeof b==="string"&&d===w)return null;f||(f=++Ya);if(typeof b==="object"){a[G]=f;e[f]=c.extend(true,{},b)}else if(!e[f]){a[G]=f;e[f]={}}a=e[f];if(d!==w)a[b]=d;return typeof b==="string"?a[b]:a}},removeData:function(a,b){if(!(a.nodeName&&c.noData[a.nodeName.toLowerCase()])){a=a==A?za:a;var d=a[G],f=c.cache,e=f[d];if(b){if(e){delete e[b];c.isEmptyObject(e)&&c.removeData(a)}}else{if(c.support.deleteExpando)delete a[c.expando];
else a.removeAttribute&&a.removeAttribute(c.expando);delete f[d]}}}});c.fn.extend({data:function(a,b){if(typeof a==="undefined"&&this.length)return c.data(this[0]);else if(typeof a==="object")return this.each(function(){c.data(this,a)});var d=a.split(".");d[1]=d[1]?"."+d[1]:"";if(b===w){var f=this.triggerHandler("getData"+d[1]+"!",[d[0]]);if(f===w&&this.length)f=c.data(this[0],a);return f===w&&d[1]?this.data(d[0]):f}else return this.trigger("setData"+d[1]+"!",[d[0],b]).each(function(){c.data(this,
a,b)})},removeData:function(a){return this.each(function(){c.removeData(this,a)})}});c.extend({queue:function(a,b,d){if(a){b=(b||"fx")+"queue";var f=c.data(a,b);if(!d)return f||[];if(!f||c.isArray(d))f=c.data(a,b,c.makeArray(d));else f.push(d);return f}},dequeue:function(a,b){b=b||"fx";var d=c.queue(a,b),f=d.shift();if(f==="inprogress")f=d.shift();if(f){b==="fx"&&d.unshift("inprogress");f.call(a,function(){c.dequeue(a,b)})}}});c.fn.extend({queue:function(a,b){if(typeof a!=="string"){b=a;a="fx"}if(b===
w)return c.queue(this[0],a);return this.each(function(){var d=c.queue(this,a,b);a==="fx"&&d[0]!=="inprogress"&&c.dequeue(this,a)})},dequeue:function(a){return this.each(function(){c.dequeue(this,a)})},delay:function(a,b){a=c.fx?c.fx.speeds[a]||a:a;b=b||"fx";return this.queue(b,function(){var d=this;setTimeout(function(){c.dequeue(d,b)},a)})},clearQueue:function(a){return this.queue(a||"fx",[])}});var Aa=/[\n\t]/g,ca=/\s+/,Za=/\r/g,$a=/href|src|style/,ab=/(button|input)/i,bb=/(button|input|object|select|textarea)/i,
cb=/^(a|area)$/i,Ba=/radio|checkbox/;c.fn.extend({attr:function(a,b){return X(this,a,b,true,c.attr)},removeAttr:function(a){return this.each(function(){c.attr(this,a,"");this.nodeType===1&&this.removeAttribute(a)})},addClass:function(a){if(c.isFunction(a))return this.each(function(n){var r=c(this);r.addClass(a.call(this,n,r.attr("class")))});if(a&&typeof a==="string")for(var b=(a||"").split(ca),d=0,f=this.length;d<f;d++){var e=this[d];if(e.nodeType===1)if(e.className){for(var j=" "+e.className+" ",
i=e.className,o=0,k=b.length;o<k;o++)if(j.indexOf(" "+b[o]+" ")<0)i+=" "+b[o];e.className=c.trim(i)}else e.className=a}return this},removeClass:function(a){if(c.isFunction(a))return this.each(function(k){var n=c(this);n.removeClass(a.call(this,k,n.attr("class")))});if(a&&typeof a==="string"||a===w)for(var b=(a||"").split(ca),d=0,f=this.length;d<f;d++){var e=this[d];if(e.nodeType===1&&e.className)if(a){for(var j=(" "+e.className+" ").replace(Aa," "),i=0,o=b.length;i<o;i++)j=j.replace(" "+b[i]+" ",
" ");e.className=c.trim(j)}else e.className=""}return this},toggleClass:function(a,b){var d=typeof a,f=typeof b==="boolean";if(c.isFunction(a))return this.each(function(e){var j=c(this);j.toggleClass(a.call(this,e,j.attr("class"),b),b)});return this.each(function(){if(d==="string")for(var e,j=0,i=c(this),o=b,k=a.split(ca);e=k[j++];){o=f?o:!i.hasClass(e);i[o?"addClass":"removeClass"](e)}else if(d==="undefined"||d==="boolean"){this.className&&c.data(this,"__className__",this.className);this.className=
this.className||a===false?"":c.data(this,"__className__")||""}})},hasClass:function(a){a=" "+a+" ";for(var b=0,d=this.length;b<d;b++)if((" "+this[b].className+" ").replace(Aa," ").indexOf(a)>-1)return true;return false},val:function(a){if(a===w){var b=this[0];if(b){if(c.nodeName(b,"option"))return(b.attributes.value||{}).specified?b.value:b.text;if(c.nodeName(b,"select")){var d=b.selectedIndex,f=[],e=b.options;b=b.type==="select-one";if(d<0)return null;var j=b?d:0;for(d=b?d+1:e.length;j<d;j++){var i=
e[j];if(i.selected){a=c(i).val();if(b)return a;f.push(a)}}return f}if(Ba.test(b.type)&&!c.support.checkOn)return b.getAttribute("value")===null?"on":b.value;return(b.value||"").replace(Za,"")}return w}var o=c.isFunction(a);return this.each(function(k){var n=c(this),r=a;if(this.nodeType===1){if(o)r=a.call(this,k,n.val());if(typeof r==="number")r+="";if(c.isArray(r)&&Ba.test(this.type))this.checked=c.inArray(n.val(),r)>=0;else if(c.nodeName(this,"select")){var u=c.makeArray(r);c("option",this).each(function(){this.selected=
c.inArray(c(this).val(),u)>=0});if(!u.length)this.selectedIndex=-1}else this.value=r}})}});c.extend({attrFn:{val:true,css:true,html:true,text:true,data:true,width:true,height:true,offset:true},attr:function(a,b,d,f){if(!a||a.nodeType===3||a.nodeType===8)return w;if(f&&b in c.attrFn)return c(a)[b](d);f=a.nodeType!==1||!c.isXMLDoc(a);var e=d!==w;b=f&&c.props[b]||b;if(a.nodeType===1){var j=$a.test(b);if(b in a&&f&&!j){if(e){b==="type"&&ab.test(a.nodeName)&&a.parentNode&&c.error("type property can't be changed");
a[b]=d}if(c.nodeName(a,"form")&&a.getAttributeNode(b))return a.getAttributeNode(b).nodeValue;if(b==="tabIndex")return(b=a.getAttributeNode("tabIndex"))&&b.specified?b.value:bb.test(a.nodeName)||cb.test(a.nodeName)&&a.href?0:w;return a[b]}if(!c.support.style&&f&&b==="style"){if(e)a.style.cssText=""+d;return a.style.cssText}e&&a.setAttribute(b,""+d);a=!c.support.hrefNormalized&&f&&j?a.getAttribute(b,2):a.getAttribute(b);return a===null?w:a}return c.style(a,b,d)}});var O=/\.(.*)$/,db=function(a){return a.replace(/[^\w\s\.\|`]/g,
function(b){return"\\"+b})};c.event={add:function(a,b,d,f){if(!(a.nodeType===3||a.nodeType===8)){if(a.setInterval&&a!==A&&!a.frameElement)a=A;var e,j;if(d.handler){e=d;d=e.handler}if(!d.guid)d.guid=c.guid++;if(j=c.data(a)){var i=j.events=j.events||{},o=j.handle;if(!o)j.handle=o=function(){return typeof c!=="undefined"&&!c.event.triggered?c.event.handle.apply(o.elem,arguments):w};o.elem=a;b=b.split(" ");for(var k,n=0,r;k=b[n++];){j=e?c.extend({},e):{handler:d,data:f};if(k.indexOf(".")>-1){r=k.split(".");
k=r.shift();j.namespace=r.slice(0).sort().join(".")}else{r=[];j.namespace=""}j.type=k;j.guid=d.guid;var u=i[k],z=c.event.special[k]||{};if(!u){u=i[k]=[];if(!z.setup||z.setup.call(a,f,r,o)===false)if(a.addEventListener)a.addEventListener(k,o,false);else a.attachEvent&&a.attachEvent("on"+k,o)}if(z.add){z.add.call(a,j);if(!j.handler.guid)j.handler.guid=d.guid}u.push(j);c.event.global[k]=true}a=null}}},global:{},remove:function(a,b,d,f){if(!(a.nodeType===3||a.nodeType===8)){var e,j=0,i,o,k,n,r,u,z=c.data(a),
C=z&&z.events;if(z&&C){if(b&&b.type){d=b.handler;b=b.type}if(!b||typeof b==="string"&&b.charAt(0)==="."){b=b||"";for(e in C)c.event.remove(a,e+b)}else{for(b=b.split(" ");e=b[j++];){n=e;i=e.indexOf(".")<0;o=[];if(!i){o=e.split(".");e=o.shift();k=new RegExp("(^|\\.)"+c.map(o.slice(0).sort(),db).join("\\.(?:.*\\.)?")+"(\\.|$)")}if(r=C[e])if(d){n=c.event.special[e]||{};for(B=f||0;B<r.length;B++){u=r[B];if(d.guid===u.guid){if(i||k.test(u.namespace)){f==null&&r.splice(B--,1);n.remove&&n.remove.call(a,u)}if(f!=
null)break}}if(r.length===0||f!=null&&r.length===1){if(!n.teardown||n.teardown.call(a,o)===false)Ca(a,e,z.handle);delete C[e]}}else for(var B=0;B<r.length;B++){u=r[B];if(i||k.test(u.namespace)){c.event.remove(a,n,u.handler,B);r.splice(B--,1)}}}if(c.isEmptyObject(C)){if(b=z.handle)b.elem=null;delete z.events;delete z.handle;c.isEmptyObject(z)&&c.removeData(a)}}}}},trigger:function(a,b,d,f){var e=a.type||a;if(!f){a=typeof a==="object"?a[G]?a:c.extend(c.Event(e),a):c.Event(e);if(e.indexOf("!")>=0){a.type=
e=e.slice(0,-1);a.exclusive=true}if(!d){a.stopPropagation();c.event.global[e]&&c.each(c.cache,function(){this.events&&this.events[e]&&c.event.trigger(a,b,this.handle.elem)})}if(!d||d.nodeType===3||d.nodeType===8)return w;a.result=w;a.target=d;b=c.makeArray(b);b.unshift(a)}a.currentTarget=d;(f=c.data(d,"handle"))&&f.apply(d,b);f=d.parentNode||d.ownerDocument;try{if(!(d&&d.nodeName&&c.noData[d.nodeName.toLowerCase()]))if(d["on"+e]&&d["on"+e].apply(d,b)===false)a.result=false}catch(j){}if(!a.isPropagationStopped()&&
f)c.event.trigger(a,b,f,true);else if(!a.isDefaultPrevented()){f=a.target;var i,o=c.nodeName(f,"a")&&e==="click",k=c.event.special[e]||{};if((!k._default||k._default.call(d,a)===false)&&!o&&!(f&&f.nodeName&&c.noData[f.nodeName.toLowerCase()])){try{if(f[e]){if(i=f["on"+e])f["on"+e]=null;c.event.triggered=true;f[e]()}}catch(n){}if(i)f["on"+e]=i;c.event.triggered=false}}},handle:function(a){var b,d,f,e;a=arguments[0]=c.event.fix(a||A.event);a.currentTarget=this;b=a.type.indexOf(".")<0&&!a.exclusive;
if(!b){d=a.type.split(".");a.type=d.shift();f=new RegExp("(^|\\.)"+d.slice(0).sort().join("\\.(?:.*\\.)?")+"(\\.|$)")}e=c.data(this,"events");d=e[a.type];if(e&&d){d=d.slice(0);e=0;for(var j=d.length;e<j;e++){var i=d[e];if(b||f.test(i.namespace)){a.handler=i.handler;a.data=i.data;a.handleObj=i;i=i.handler.apply(this,arguments);if(i!==w){a.result=i;if(i===false){a.preventDefault();a.stopPropagation()}}if(a.isImmediatePropagationStopped())break}}}return a.result},props:"altKey attrChange attrName bubbles button cancelable XcharCode clientX clientY ctrlKey currentTarget data detail eventPhase fromElement handler keyCode layerX layerY metaKey newValue offsetX offsetY originalTarget pageX pageY prevValue relatedNode relatedTarget screenX screenY shiftKey srcElement target toElement view wheelDelta which".split(" "),
fix:function(a){if(a[G])return a;var b=a;a=c.Event(b);for(var d=this.props.length,f;d;){f=this.props[--d];a[f]=b[f]}if(!a.target)a.target=a.srcElement||s;if(a.target.nodeType===3)a.target=a.target.parentNode;if(!a.relatedTarget&&a.fromElement)a.relatedTarget=a.fromElement===a.target?a.toElement:a.fromElement;if(a.pageX==null&&a.clientX!=null){b=s.documentElement;d=s.body;a.pageX=a.clientX+(b&&b.scrollLeft||d&&d.scrollLeft||0)-(b&&b.clientLeft||d&&d.clientLeft||0);a.pageY=a.clientY+(b&&b.scrollTop||
d&&d.scrollTop||0)-(b&&b.clientTop||d&&d.clientTop||0)}if(!a.which&&(a.XcharCode||a.XcharCode===0?a.XcharCode:a.keyCode))a.which=a.XcharCode||a.keyCode;if(!a.metaKey&&a.ctrlKey)a.metaKey=a.ctrlKey;if(!a.which&&a.button!==w)a.which=a.button&1?1:a.button&2?3:a.button&4?2:0;return a},guid:1E8,proxy:c.proxy,special:{ready:{setup:c.bindReady,teardown:c.noop},live:{add:function(a){c.event.add(this,a.origType,c.extend({},a,{handler:oa}))},remove:function(a){var b=true,d=a.origType.replace(O,"");c.each(c.data(this,
"events").live||[],function(){if(d===this.origType.replace(O,""))return b=false});b&&c.event.remove(this,a.origType,oa)}},beforeunload:{setup:function(a,b,d){if(this.setInterval)this.onbeforeunload=d;return false},teardown:function(a,b){if(this.onbeforeunload===b)this.onbeforeunload=null}}}};var Ca=s.removeEventListener?function(a,b,d){a.removeEventListener(b,d,false)}:function(a,b,d){a.detachEvent("on"+b,d)};c.Event=function(a){if(!this.preventDefault)return new c.Event(a);if(a&&a.type){this.originalEvent=
a;this.type=a.type}else this.type=a;this.timeStamp=J();this[G]=true};c.Event.prototype={preventDefault:function(){this.isDefaultPrevented=Z;var a=this.originalEvent;if(a){a.preventDefault&&a.preventDefault();a.returnValue=false}},stopPropagation:function(){this.isPropagationStopped=Z;var a=this.originalEvent;if(a){a.stopPropagation&&a.stopPropagation();a.cancelBubble=true}},stopImmediatePropagation:function(){this.isImmediatePropagationStopped=Z;this.stopPropagation()},isDefaultPrevented:Y,isPropagationStopped:Y,
isImmediatePropagationStopped:Y};var Da=function(a){var b=a.relatedTarget;try{for(;b&&b!==this;)b=b.parentNode;if(b!==this){a.type=a.data;c.event.handle.apply(this,arguments)}}catch(d){}},Ea=function(a){a.type=a.data;c.event.handle.apply(this,arguments)};c.each({mouseenter:"mouseover",mouseleave:"mouseout"},function(a,b){c.event.special[a]={setup:function(d){c.event.add(this,b,d&&d.selector?Ea:Da,a)},teardown:function(d){c.event.remove(this,b,d&&d.selector?Ea:Da)}}});if(!c.support.submitBubbles)c.event.special.submit=
{setup:function(){if(this.nodeName.toLowerCase()!=="form"){c.event.add(this,"click.specialSubmit",function(a){var b=a.target,d=b.type;if((d==="submit"||d==="image")&&c(b).closest("form").length)return na("submit",this,arguments)});c.event.add(this,"keypress.specialSubmit",function(a){var b=a.target,d=b.type;if((d==="text"||d==="password")&&c(b).closest("form").length&&a.keyCode===13)return na("submit",this,arguments)})}else return false},teardown:function(){c.event.remove(this,".specialSubmit")}};
if(!c.support.changeBubbles){var da=/textarea|input|select/i,ea,Fa=function(a){var b=a.type,d=a.value;if(b==="radio"||b==="checkbox")d=a.checked;else if(b==="select-multiple")d=a.selectedIndex>-1?c.map(a.options,function(f){return f.selected}).join("-"):"";else if(a.nodeName.toLowerCase()==="select")d=a.selectedIndex;return d},fa=function(a,b){var d=a.target,f,e;if(!(!da.test(d.nodeName)||d.readOnly)){f=c.data(d,"_change_data");e=Fa(d);if(a.type!=="focusout"||d.type!=="radio")c.data(d,"_change_data",
e);if(!(f===w||e===f))if(f!=null||e){a.type="change";return c.event.trigger(a,b,d)}}};c.event.special.change={filters:{focusout:fa,click:function(a){var b=a.target,d=b.type;if(d==="radio"||d==="checkbox"||b.nodeName.toLowerCase()==="select")return fa.call(this,a)},keydown:function(a){var b=a.target,d=b.type;if(a.keyCode===13&&b.nodeName.toLowerCase()!=="textarea"||a.keyCode===32&&(d==="checkbox"||d==="radio")||d==="select-multiple")return fa.call(this,a)},beforeactivate:function(a){a=a.target;c.data(a,
"_change_data",Fa(a))}},setup:function(){if(this.type==="file")return false;for(var a in ea)c.event.add(this,a+".specialChange",ea[a]);return da.test(this.nodeName)},teardown:function(){c.event.remove(this,".specialChange");return da.test(this.nodeName)}};ea=c.event.special.change.filters}s.addEventListener&&c.each({focus:"focusin",blur:"focusout"},function(a,b){function d(f){f=c.event.fix(f);f.type=b;return c.event.handle.call(this,f)}c.event.special[b]={setup:function(){this.addEventListener(a,
d,true)},teardown:function(){this.removeEventListener(a,d,true)}}});c.each(["bind","one"],function(a,b){c.fn[b]=function(d,f,e){if(typeof d==="object"){for(var j in d)this[b](j,f,d[j],e);return this}if(c.isFunction(f)){e=f;f=w}var i=b==="one"?c.proxy(e,function(k){c(this).unbind(k,i);return e.apply(this,arguments)}):e;if(d==="unload"&&b!=="one")this.one(d,f,e);else{j=0;for(var o=this.length;j<o;j++)c.event.add(this[j],d,i,f)}return this}});c.fn.extend({unbind:function(a,b){if(typeof a==="object"&&
!a.preventDefault)for(var d in a)this.unbind(d,a[d]);else{d=0;for(var f=this.length;d<f;d++)c.event.remove(this[d],a,b)}return this},delegate:function(a,b,d,f){return this.live(b,d,f,a)},undelegate:function(a,b,d){return arguments.length===0?this.unbind("live"):this.die(b,null,d,a)},trigger:function(a,b){return this.each(function(){c.event.trigger(a,b,this)})},triggerHandler:function(a,b){if(this[0]){a=c.Event(a);a.preventDefault();a.stopPropagation();c.event.trigger(a,b,this[0]);return a.result}},
toggle:function(a){for(var b=arguments,d=1;d<b.length;)c.proxy(a,b[d++]);return this.click(c.proxy(a,function(f){var e=(c.data(this,"lastToggle"+a.guid)||0)%d;c.data(this,"lastToggle"+a.guid,e+1);f.preventDefault();return b[e].apply(this,arguments)||false}))},hover:function(a,b){return this.mouseenter(a).mouseleave(b||a)}});var Ga={focus:"focusin",blur:"focusout",mouseenter:"mouseover",mouseleave:"mouseout"};c.each(["live","die"],function(a,b){c.fn[b]=function(d,f,e,j){var i,o=0,k,n,r=j||this.selector,
u=j?this:c(this.context);if(c.isFunction(f)){e=f;f=w}for(d=(d||"").split(" ");(i=d[o++])!=null;){j=O.exec(i);k="";if(j){k=j[0];i=i.replace(O,"")}if(i==="hover")d.push("mouseenter"+k,"mouseleave"+k);else{n=i;if(i==="focus"||i==="blur"){d.push(Ga[i]+k);i+=k}else i=(Ga[i]||i)+k;b==="live"?u.each(function(){c.event.add(this,pa(i,r),{data:f,selector:r,handler:e,origType:i,origHandler:e,preType:n})}):u.unbind(pa(i,r),e)}}return this}});c.each("blur focus focusin focusout load resize scroll unload click dblclick mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave change select submit keydown keypress keyup error".split(" "),
function(a,b){c.fn[b]=function(d){return d?this.bind(b,d):this.trigger(b)};if(c.attrFn)c.attrFn[b]=true});A.attachEvent&&!A.addEventListener&&A.attachEvent("onunload",function(){for(var a in c.cache)if(c.cache[a].handle)try{c.event.remove(c.cache[a].handle.elem)}catch(b){}});(function(){function a(g){for(var h="",l,m=0;g[m];m++){l=g[m];if(l.nodeType===3||l.nodeType===4)h+=l.nodeValue;else if(l.nodeType!==8)h+=a(l.childNodes)}return h}function b(g,h,l,m,q,p){q=0;for(var v=m.length;q<v;q++){var t=m[q];
if(t){t=t[g];for(var y=false;t;){if(t.sizcache===l){y=m[t.sizset];break}if(t.nodeType===1&&!p){t.sizcache=l;t.sizset=q}if(t.nodeName.toLowerCase()===h){y=t;break}t=t[g]}m[q]=y}}}function d(g,h,l,m,q,p){q=0;for(var v=m.length;q<v;q++){var t=m[q];if(t){t=t[g];for(var y=false;t;){if(t.sizcache===l){y=m[t.sizset];break}if(t.nodeType===1){if(!p){t.sizcache=l;t.sizset=q}if(typeof h!=="string"){if(t===h){y=true;break}}else if(k.filter(h,[t]).length>0){y=t;break}}t=t[g]}m[q]=y}}}var f=/((?:\((?:\([^()]+\)|[^()]+)+\)|\[(?:\[[^[\]]*\]|['"][^'"]*['"]|[^[\]'"]+)+\]|\\.|[^ >+~,(\[\\]+)+|[>+~])(\s*,\s*)?((?:.|\r|\n)*)/g,
e=0,j=Object.prototype.toString,i=false,o=true;[0,0].sort(function(){o=false;return 0});var k=function(g,h,l,m){l=l||[];var q=h=h||s;if(h.nodeType!==1&&h.nodeType!==9)return[];if(!g||typeof g!=="string")return l;for(var p=[],v,t,y,S,H=true,M=x(h),I=g;(f.exec(""),v=f.exec(I))!==null;){I=v[3];p.push(v[1]);if(v[2]){S=v[3];break}}if(p.length>1&&r.exec(g))if(p.length===2&&n.relative[p[0]])t=ga(p[0]+p[1],h);else for(t=n.relative[p[0]]?[h]:k(p.shift(),h);p.length;){g=p.shift();if(n.relative[g])g+=p.shift();
t=ga(g,t)}else{if(!m&&p.length>1&&h.nodeType===9&&!M&&n.match.ID.test(p[0])&&!n.match.ID.test(p[p.length-1])){v=k.find(p.shift(),h,M);h=v.expr?k.filter(v.expr,v.set)[0]:v.set[0]}if(h){v=m?{expr:p.pop(),set:z(m)}:k.find(p.pop(),p.length===1&&(p[0]==="~"||p[0]==="+")&&h.parentNode?h.parentNode:h,M);t=v.expr?k.filter(v.expr,v.set):v.set;if(p.length>0)y=z(t);else H=false;for(;p.length;){var D=p.pop();v=D;if(n.relative[D])v=p.pop();else D="";if(v==null)v=h;n.relative[D](y,v,M)}}else y=[]}y||(y=t);y||k.error(D||
g);if(j.call(y)==="[object Array]")if(H)if(h&&h.nodeType===1)for(g=0;y[g]!=null;g++){if(y[g]&&(y[g]===true||y[g].nodeType===1&&E(h,y[g])))l.push(t[g])}else for(g=0;y[g]!=null;g++)y[g]&&y[g].nodeType===1&&l.push(t[g]);else l.push.apply(l,y);else z(y,l);if(S){k(S,q,l,m);k.uniqueSort(l)}return l};k.uniqueSort=function(g){if(B){i=o;g.sort(B);if(i)for(var h=1;h<g.length;h++)g[h]===g[h-1]&&g.splice(h--,1)}return g};k.matches=function(g,h){return k(g,null,null,h)};k.find=function(g,h,l){var m,q;if(!g)return[];
for(var p=0,v=n.order.length;p<v;p++){var t=n.order[p];if(q=n.leftMatch[t].exec(g)){var y=q[1];q.splice(1,1);if(y.substr(y.length-1)!=="\\"){q[1]=(q[1]||"").replace(/\\/g,"");m=n.find[t](q,h,l);if(m!=null){g=g.replace(n.match[t],"");break}}}}m||(m=h.getElementsByTagName("*"));return{set:m,expr:g}};k.filter=function(g,h,l,m){for(var q=g,p=[],v=h,t,y,S=h&&h[0]&&x(h[0]);g&&h.length;){for(var H in n.filter)if((t=n.leftMatch[H].exec(g))!=null&&t[2]){var M=n.filter[H],I,D;D=t[1];y=false;t.splice(1,1);if(D.substr(D.length-
1)!=="\\"){if(v===p)p=[];if(n.preFilter[H])if(t=n.preFilter[H](t,v,l,p,m,S)){if(t===true)continue}else y=I=true;if(t)for(var U=0;(D=v[U])!=null;U++)if(D){I=M(D,t,U,v);var Ha=m^!!I;if(l&&I!=null)if(Ha)y=true;else v[U]=false;else if(Ha){p.push(D);y=true}}if(I!==w){l||(v=p);g=g.replace(n.match[H],"");if(!y)return[];break}}}if(g===q)if(y==null)k.error(g);else break;q=g}return v};k.error=function(g){throw"Syntax error, unrecognized expression: "+g;};var n=k.selectors={order:["ID","NAME","TAG"],match:{ID:/#((?:[\w\u00c0-\uFFFF-]|\\.)+)/,
CLASS:/\.((?:[\w\u00c0-\uFFFF-]|\\.)+)/,NAME:/\[name=['"]*((?:[\w\u00c0-\uFFFF-]|\\.)+)['"]*\]/,ATTR:/\[\s*((?:[\w\u00c0-\uFFFF-]|\\.)+)\s*(?:(\S?=)\s*(['"]*)(.*?)\3|)\s*\]/,TAG:/^((?:[\w\u00c0-\uFFFF\*-]|\\.)+)/,CHILD:/:(only|nth|last|first)-child(?:\((even|odd|[\dn+-]*)\))?/,POS:/:(nth|eq|gt|lt|first|last|even|odd)(?:\((\d*)\))?(?=[^-]|$)/,PSEUDO:/:((?:[\w\u00c0-\uFFFF-]|\\.)+)(?:\((['"]?)((?:\([^\)]+\)|[^\(\)]*)+)\2\))?/},leftMatch:{},attrMap:{"class":"className","for":"htmlFor"},attrHandle:{href:function(g){return g.getAttribute("href")}},
relative:{"+":function(g,h){var l=typeof h==="string",m=l&&!/\W/.test(h);l=l&&!m;if(m)h=h.toLowerCase();m=0;for(var q=g.length,p;m<q;m++)if(p=g[m]){for(;(p=p.previousSibling)&&p.nodeType!==1;);g[m]=l||p&&p.nodeName.toLowerCase()===h?p||false:p===h}l&&k.filter(h,g,true)},">":function(g,h){var l=typeof h==="string";if(l&&!/\W/.test(h)){h=h.toLowerCase();for(var m=0,q=g.length;m<q;m++){var p=g[m];if(p){l=p.parentNode;g[m]=l.nodeName.toLowerCase()===h?l:false}}}else{m=0;for(q=g.length;m<q;m++)if(p=g[m])g[m]=
l?p.parentNode:p.parentNode===h;l&&k.filter(h,g,true)}},"":function(g,h,l){var m=e++,q=d;if(typeof h==="string"&&!/\W/.test(h)){var p=h=h.toLowerCase();q=b}q("parentNode",h,m,g,p,l)},"~":function(g,h,l){var m=e++,q=d;if(typeof h==="string"&&!/\W/.test(h)){var p=h=h.toLowerCase();q=b}q("previousSibling",h,m,g,p,l)}},find:{ID:function(g,h,l){if(typeof h.getElementById!=="undefined"&&!l)return(g=h.getElementById(g[1]))?[g]:[]},NAME:function(g,h){if(typeof h.getElementsByName!=="undefined"){var l=[];
h=h.getElementsByName(g[1]);for(var m=0,q=h.length;m<q;m++)h[m].getAttribute("name")===g[1]&&l.push(h[m]);return l.length===0?null:l}},TAG:function(g,h){return h.getElementsByTagName(g[1])}},preFilter:{CLASS:function(g,h,l,m,q,p){g=" "+g[1].replace(/\\/g,"")+" ";if(p)return g;p=0;for(var v;(v=h[p])!=null;p++)if(v)if(q^(v.className&&(" "+v.className+" ").replace(/[\t\n]/g," ").indexOf(g)>=0))l||m.push(v);else if(l)h[p]=false;return false},ID:function(g){return g[1].replace(/\\/g,"")},TAG:function(g){return g[1].toLowerCase()},
CHILD:function(g){if(g[1]==="nth"){var h=/(-?)(\d*)n((?:\+|-)?\d*)/.exec(g[2]==="even"&&"2n"||g[2]==="odd"&&"2n+1"||!/\D/.test(g[2])&&"0n+"+g[2]||g[2]);g[2]=h[1]+(h[2]||1)-0;g[3]=h[3]-0}g[0]=e++;return g},ATTR:function(g,h,l,m,q,p){h=g[1].replace(/\\/g,"");if(!p&&n.attrMap[h])g[1]=n.attrMap[h];if(g[2]==="~=")g[4]=" "+g[4]+" ";return g},PSEUDO:function(g,h,l,m,q){if(g[1]==="not")if((f.exec(g[3])||"").length>1||/^\w/.test(g[3]))g[3]=k(g[3],null,null,h);else{g=k.filter(g[3],h,l,true^q);l||m.push.apply(m,
g);return false}else if(n.match.POS.test(g[0])||n.match.CHILD.test(g[0]))return true;return g},POS:function(g){g.unshift(true);return g}},filters:{enabled:function(g){return g.disabled===false&&g.type!=="hidden"},disabled:function(g){return g.disabled===true},checked:function(g){return g.checked===true},selected:function(g){return g.selected===true},parent:function(g){return!!g.firstChild},empty:function(g){return!g.firstChild},has:function(g,h,l){return!!k(l[3],g).length},header:function(g){return/h\d/i.test(g.nodeName)},
text:function(g){return"text"===g.type},radio:function(g){return"radio"===g.type},checkbox:function(g){return"checkbox"===g.type},file:function(g){return"file"===g.type},password:function(g){return"password"===g.type},submit:function(g){return"submit"===g.type},image:function(g){return"image"===g.type},reset:function(g){return"reset"===g.type},button:function(g){return"button"===g.type||g.nodeName.toLowerCase()==="button"},input:function(g){return/input|select|textarea|button/i.test(g.nodeName)}},
setFilters:{first:function(g,h){return h===0},last:function(g,h,l,m){return h===m.length-1},even:function(g,h){return h%2===0},odd:function(g,h){return h%2===1},lt:function(g,h,l){return h<l[3]-0},gt:function(g,h,l){return h>l[3]-0},nth:function(g,h,l){return l[3]-0===h},eq:function(g,h,l){return l[3]-0===h}},filter:{PSEUDO:function(g,h,l,m){var q=h[1],p=n.filters[q];if(p)return p(g,l,h,m);else if(q==="contains")return(g.textContent||g.innerText||a([g])||"").indexOf(h[3])>=0;else if(q==="not"){h=
h[3];l=0;for(m=h.length;l<m;l++)if(h[l]===g)return false;return true}else k.error("Syntax error, unrecognized expression: "+q)},CHILD:function(g,h){var l=h[1],m=g;switch(l){case "only":case "first":for(;m=m.previousSibling;)if(m.nodeType===1)return false;if(l==="first")return true;m=g;case "last":for(;m=m.nextSibling;)if(m.nodeType===1)return false;return true;case "nth":l=h[2];var q=h[3];if(l===1&&q===0)return true;h=h[0];var p=g.parentNode;if(p&&(p.sizcache!==h||!g.nodeIndex)){var v=0;for(m=p.firstChild;m;m=
m.nextSibling)if(m.nodeType===1)m.nodeIndex=++v;p.sizcache=h}g=g.nodeIndex-q;return l===0?g===0:g%l===0&&g/l>=0}},ID:function(g,h){return g.nodeType===1&&g.getAttribute("id")===h},TAG:function(g,h){return h==="*"&&g.nodeType===1||g.nodeName.toLowerCase()===h},CLASS:function(g,h){return(" "+(g.className||g.getAttribute("class"))+" ").indexOf(h)>-1},ATTR:function(g,h){var l=h[1];g=n.attrHandle[l]?n.attrHandle[l](g):g[l]!=null?g[l]:g.getAttribute(l);l=g+"";var m=h[2];h=h[4];return g==null?m==="!=":m===
"="?l===h:m==="*="?l.indexOf(h)>=0:m==="~="?(" "+l+" ").indexOf(h)>=0:!h?l&&g!==false:m==="!="?l!==h:m==="^="?l.indexOf(h)===0:m==="$="?l.substr(l.length-h.length)===h:m==="|="?l===h||l.substr(0,h.length+1)===h+"-":false},POS:function(g,h,l,m){var q=n.setFilters[h[2]];if(q)return q(g,l,h,m)}}},r=n.match.POS;for(var u in n.match){n.match[u]=new RegExp(n.match[u].source+/(?![^\[]*\])(?![^\(]*\))/.source);n.leftMatch[u]=new RegExp(/(^(?:.|\r|\n)*?)/.source+n.match[u].source.replace(/\\(\d+)/g,function(g,
h){return"\\"+(h-0+1)}))}var z=function(g,h){g=Array.prototype.slice.call(g,0);if(h){h.push.apply(h,g);return h}return g};try{Array.prototype.slice.call(s.documentElement.childNodes,0)}catch(C){z=function(g,h){h=h||[];if(j.call(g)==="[object Array]")Array.prototype.push.apply(h,g);else if(typeof g.length==="number")for(var l=0,m=g.length;l<m;l++)h.push(g[l]);else for(l=0;g[l];l++)h.push(g[l]);return h}}var B;if(s.documentElement.compareDocumentPosition)B=function(g,h){if(!g.compareDocumentPosition||
!h.compareDocumentPosition){if(g==h)i=true;return g.compareDocumentPosition?-1:1}g=g.compareDocumentPosition(h)&4?-1:g===h?0:1;if(g===0)i=true;return g};else if("sourceIndex"in s.documentElement)B=function(g,h){if(!g.sourceIndex||!h.sourceIndex){if(g==h)i=true;return g.sourceIndex?-1:1}g=g.sourceIndex-h.sourceIndex;if(g===0)i=true;return g};else if(s.createRange)B=function(g,h){if(!g.ownerDocument||!h.ownerDocument){if(g==h)i=true;return g.ownerDocument?-1:1}var l=g.ownerDocument.createRange(),m=
h.ownerDocument.createRange();l.setStart(g,0);l.setEnd(g,0);m.setStart(h,0);m.setEnd(h,0);g=l.compareBoundaryPoints(Range.START_TO_END,m);if(g===0)i=true;return g};(function(){var g=s.createElement("div"),h="script"+(new Date).getTime();g.innerHTML="<a name='"+h+"'/>";var l=s.documentElement;l.insertBefore(g,l.firstChild);if(s.getElementById(h)){n.find.ID=function(m,q,p){if(typeof q.getElementById!=="undefined"&&!p)return(q=q.getElementById(m[1]))?q.id===m[1]||typeof q.getAttributeNode!=="undefined"&&
q.getAttributeNode("id").nodeValue===m[1]?[q]:w:[]};n.filter.ID=function(m,q){var p=typeof m.getAttributeNode!=="undefined"&&m.getAttributeNode("id");return m.nodeType===1&&p&&p.nodeValue===q}}l.removeChild(g);l=g=null})();(function(){var g=s.createElement("div");g.appendChild(s.createComment(""));if(g.getElementsByTagName("*").length>0)n.find.TAG=function(h,l){l=l.getElementsByTagName(h[1]);if(h[1]==="*"){h=[];for(var m=0;l[m];m++)l[m].nodeType===1&&h.push(l[m]);l=h}return l};g.innerHTML="<a href='#'></a>";
if(g.firstChild&&typeof g.firstChild.getAttribute!=="undefined"&&g.firstChild.getAttribute("href")!=="#")n.attrHandle.href=function(h){return h.getAttribute("href",2)};g=null})();s.querySelectorAll&&function(){var g=k,h=s.createElement("div");h.innerHTML="<p class='TEST'></p>";if(!(h.querySelectorAll&&h.querySelectorAll(".TEST").length===0)){k=function(m,q,p,v){q=q||s;if(!v&&q.nodeType===9&&!x(q))try{return z(q.querySelectorAll(m),p)}catch(t){}return g(m,q,p,v)};for(var l in g)k[l]=g[l];h=null}}();
(function(){var g=s.createElement("div");g.innerHTML="<div class='test e'></div><div class='test'></div>";if(!(!g.getElementsByClassName||g.getElementsByClassName("e").length===0)){g.lastChild.className="e";if(g.getElementsByClassName("e").length!==1){n.order.splice(1,0,"CLASS");n.find.CLASS=function(h,l,m){if(typeof l.getElementsByClassName!=="undefined"&&!m)return l.getElementsByClassName(h[1])};g=null}}})();var E=s.compareDocumentPosition?function(g,h){return!!(g.compareDocumentPosition(h)&16)}:
function(g,h){return g!==h&&(g.contains?g.contains(h):true)},x=function(g){return(g=(g?g.ownerDocument||g:0).documentElement)?g.nodeName!=="HTML":false},ga=function(g,h){var l=[],m="",q;for(h=h.nodeType?[h]:h;q=n.match.PSEUDO.exec(g);){m+=q[0];g=g.replace(n.match.PSEUDO,"")}g=n.relative[g]?g+"*":g;q=0;for(var p=h.length;q<p;q++)k(g,h[q],l);return k.filter(m,l)};c.find=k;c.expr=k.selectors;c.expr[":"]=c.expr.filters;c.unique=k.uniqueSort;c.text=a;c.isXMLDoc=x;c.contains=E})();var eb=/Until$/,fb=/^(?:parents|prevUntil|prevAll)/,
gb=/,/;R=Array.prototype.slice;var Ia=function(a,b,d){if(c.isFunction(b))return c.grep(a,function(e,j){return!!b.call(e,j,e)===d});else if(b.nodeType)return c.grep(a,function(e){return e===b===d});else if(typeof b==="string"){var f=c.grep(a,function(e){return e.nodeType===1});if(Ua.test(b))return c.filter(b,f,!d);else b=c.filter(b,f)}return c.grep(a,function(e){return c.inArray(e,b)>=0===d})};c.fn.extend({find:function(a){for(var b=this.pushStack("","find",a),d=0,f=0,e=this.length;f<e;f++){d=b.length;
c.find(a,this[f],b);if(f>0)for(var j=d;j<b.length;j++)for(var i=0;i<d;i++)if(b[i]===b[j]){b.splice(j--,1);break}}return b},has:function(a){var b=c(a);return this.filter(function(){for(var d=0,f=b.length;d<f;d++)if(c.contains(this,b[d]))return true})},not:function(a){return this.pushStack(Ia(this,a,false),"not",a)},filter:function(a){return this.pushStack(Ia(this,a,true),"filter",a)},is:function(a){return!!a&&c.filter(a,this).length>0},closest:function(a,b){if(c.isArray(a)){var d=[],f=this[0],e,j=
{},i;if(f&&a.length){e=0;for(var o=a.length;e<o;e++){i=a[e];j[i]||(j[i]=c.expr.match.POS.test(i)?c(i,b||this.context):i)}for(;f&&f.ownerDocument&&f!==b;){for(i in j){e=j[i];if(e.jquery?e.index(f)>-1:c(f).is(e)){d.push({selector:i,elem:f});delete j[i]}}f=f.parentNode}}return d}var k=c.expr.match.POS.test(a)?c(a,b||this.context):null;return this.map(function(n,r){for(;r&&r.ownerDocument&&r!==b;){if(k?k.index(r)>-1:c(r).is(a))return r;r=r.parentNode}return null})},index:function(a){if(!a||typeof a===
"string")return c.inArray(this[0],a?c(a):this.parent().children());return c.inArray(a.jquery?a[0]:a,this)},add:function(a,b){a=typeof a==="string"?c(a,b||this.context):c.makeArray(a);b=c.merge(this.get(),a);return this.pushStack(qa(a[0])||qa(b[0])?b:c.unique(b))},andSelf:function(){return this.add(this.prevObject)}});c.each({parent:function(a){return(a=a.parentNode)&&a.nodeType!==11?a:null},parents:function(a){return c.dir(a,"parentNode")},parentsUntil:function(a,b,d){return c.dir(a,"parentNode",
d)},next:function(a){return c.nth(a,2,"nextSibling")},prev:function(a){return c.nth(a,2,"previousSibling")},nextAll:function(a){return c.dir(a,"nextSibling")},prevAll:function(a){return c.dir(a,"previousSibling")},nextUntil:function(a,b,d){return c.dir(a,"nextSibling",d)},prevUntil:function(a,b,d){return c.dir(a,"previousSibling",d)},siblings:function(a){return c.sibling(a.parentNode.firstChild,a)},children:function(a){return c.sibling(a.firstChild)},contents:function(a){return c.nodeName(a,"iframe")?
a.contentDocument||a.contentWindow.document:c.makeArray(a.childNodes)}},function(a,b){c.fn[a]=function(d,f){var e=c.map(this,b,d);eb.test(a)||(f=d);if(f&&typeof f==="string")e=c.filter(f,e);e=this.length>1?c.unique(e):e;if((this.length>1||gb.test(f))&&fb.test(a))e=e.reverse();return this.pushStack(e,a,R.call(arguments).join(","))}});c.extend({filter:function(a,b,d){if(d)a=":not("+a+")";return c.find.matches(a,b)},dir:function(a,b,d){var f=[];for(a=a[b];a&&a.nodeType!==9&&(d===w||a.nodeType!==1||!c(a).is(d));){a.nodeType===
1&&f.push(a);a=a[b]}return f},nth:function(a,b,d){b=b||1;for(var f=0;a;a=a[d])if(a.nodeType===1&&++f===b)break;return a},sibling:function(a,b){for(var d=[];a;a=a.nextSibling)a.nodeType===1&&a!==b&&d.push(a);return d}});var Ja=/ jQuery\d+="(?:\d+|null)"/g,V=/^\s+/,Ka=/(<([\w:]+)[^>]*?)\/>/g,hb=/^(?:area|br|col|embed|hr|img|input|link|meta|param)$/i,La=/<([\w:]+)/,ib=/<tbody/i,jb=/<|&#?\w+;/,ta=/<script|<object|<embed|<option|<style/i,ua=/checked\s*(?:[^=]|=\s*.checked.)/i,Ma=function(a,b,d){return hb.test(d)?
a:b+"></"+d+">"},F={option:[1,"<select multiple='multiple'>","</select>"],legend:[1,"<fieldset>","</fieldset>"],thead:[1,"<table>","</table>"],tr:[2,"<table><tbody>","</tbody></table>"],td:[3,"<table><tbody><tr>","</tr></tbody></table>"],col:[2,"<table><tbody></tbody><colgroup>","</colgroup></table>"],area:[1,"<map>","</map>"],_default:[0,"",""]};F.optgroup=F.option;F.tbody=F.tfoot=F.colgroup=F.caption=F.thead;F.th=F.td;if(!c.support.htmlSerialize)F._default=[1,"div<div>","</div>"];c.fn.extend({text:function(a){if(c.isFunction(a))return this.each(function(b){var d=
c(this);d.text(a.call(this,b,d.text()))});if(typeof a!=="object"&&a!==w)return this.empty().append((this[0]&&this[0].ownerDocument||s).createTextNode(a));return c.text(this)},wrapAll:function(a){if(c.isFunction(a))return this.each(function(d){c(this).wrapAll(a.call(this,d))});if(this[0]){var b=c(a,this[0].ownerDocument).eq(0).clone(true);this[0].parentNode&&b.insertBefore(this[0]);b.map(function(){for(var d=this;d.firstChild&&d.firstChild.nodeType===1;)d=d.firstChild;return d}).append(this)}return this},
wrapInner:function(a){if(c.isFunction(a))return this.each(function(b){c(this).wrapInner(a.call(this,b))});return this.each(function(){var b=c(this),d=b.contents();d.length?d.wrapAll(a):b.append(a)})},wrap:function(a){return this.each(function(){c(this).wrapAll(a)})},unwrap:function(){return this.parent().each(function(){c.nodeName(this,"body")||c(this).replaceWith(this.childNodes)}).end()},append:function(){return this.domManip(arguments,true,function(a){this.nodeType===1&&this.appendChild(a)})},
prepend:function(){return this.domManip(arguments,true,function(a){this.nodeType===1&&this.insertBefore(a,this.firstChild)})},before:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,false,function(b){this.parentNode.insertBefore(b,this)});else if(arguments.length){var a=c(arguments[0]);a.push.apply(a,this.toArray());return this.pushStack(a,"before",arguments)}},after:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,false,function(b){this.parentNode.insertBefore(b,
this.nextSibling)});else if(arguments.length){var a=this.pushStack(this,"after",arguments);a.push.apply(a,c(arguments[0]).toArray());return a}},remove:function(a,b){for(var d=0,f;(f=this[d])!=null;d++)if(!a||c.filter(a,[f]).length){if(!b&&f.nodeType===1){c.cleanData(f.getElementsByTagName("*"));c.cleanData([f])}f.parentNode&&f.parentNode.removeChild(f)}return this},empty:function(){for(var a=0,b;(b=this[a])!=null;a++)for(b.nodeType===1&&c.cleanData(b.getElementsByTagName("*"));b.firstChild;)b.removeChild(b.firstChild);
return this},clone:function(a){var b=this.map(function(){if(!c.support.noCloneEvent&&!c.isXMLDoc(this)){var d=this.outerHTML,f=this.ownerDocument;if(!d){d=f.createElement("div");d.appendChild(this.cloneNode(true));d=d.innerHTML}return c.clean([d.replace(Ja,"").replace(/=([^="'>\s]+\/)>/g,'="$1">').replace(V,"")],f)[0]}else return this.cloneNode(true)});if(a===true){ra(this,b);ra(this.find("*"),b.find("*"))}return b},html:function(a){if(a===w)return this[0]&&this[0].nodeType===1?this[0].innerHTML.replace(Ja,
""):null;else if(typeof a==="string"&&!ta.test(a)&&(c.support.leadingWhitespace||!V.test(a))&&!F[(La.exec(a)||["",""])[1].toLowerCase()]){a=a.replace(Ka,Ma);try{for(var b=0,d=this.length;b<d;b++)if(this[b].nodeType===1){c.cleanData(this[b].getElementsByTagName("*"));this[b].innerHTML=a}}catch(f){this.empty().append(a)}}else c.isFunction(a)?this.each(function(e){var j=c(this),i=j.html();j.empty().append(function(){return a.call(this,e,i)})}):this.empty().append(a);return this},replaceWith:function(a){if(this[0]&&
this[0].parentNode){if(c.isFunction(a))return this.each(function(b){var d=c(this),f=d.html();d.replaceWith(a.call(this,b,f))});if(typeof a!=="string")a=c(a).detach();return this.each(function(){var b=this.nextSibling,d=this.parentNode;c(this).remove();b?c(b).before(a):c(d).append(a)})}else return this.pushStack(c(c.isFunction(a)?a():a),"replaceWith",a)},detach:function(a){return this.remove(a,true)},domManip:function(a,b,d){function f(u){return c.nodeName(u,"table")?u.getElementsByTagName("tbody")[0]||
u.appendChild(u.ownerDocument.createElement("tbody")):u}var e,j,i=a[0],o=[],k;if(!c.support.checkClone&&arguments.length===3&&typeof i==="string"&&ua.test(i))return this.each(function(){c(this).domManip(a,b,d,true)});if(c.isFunction(i))return this.each(function(u){var z=c(this);a[0]=i.call(this,u,b?z.html():w);z.domManip(a,b,d)});if(this[0]){e=i&&i.parentNode;e=c.support.parentNode&&e&&e.nodeType===11&&e.childNodes.length===this.length?{fragment:e}:sa(a,this,o);k=e.fragment;if(j=k.childNodes.length===
1?(k=k.firstChild):k.firstChild){b=b&&c.nodeName(j,"tr");for(var n=0,r=this.length;n<r;n++)d.call(b?f(this[n],j):this[n],n>0||e.cacheable||this.length>1?k.cloneNode(true):k)}o.length&&c.each(o,Qa)}return this}});c.fragments={};c.each({appendTo:"append",prependTo:"prepend",insertBefore:"before",insertAfter:"after",replaceAll:"replaceWith"},function(a,b){c.fn[a]=function(d){var f=[];d=c(d);var e=this.length===1&&this[0].parentNode;if(e&&e.nodeType===11&&e.childNodes.length===1&&d.length===1){d[b](this[0]);
return this}else{e=0;for(var j=d.length;e<j;e++){var i=(e>0?this.clone(true):this).get();c.fn[b].apply(c(d[e]),i);f=f.concat(i)}return this.pushStack(f,a,d.selector)}}});c.extend({clean:function(a,b,d,f){b=b||s;if(typeof b.createElement==="undefined")b=b.ownerDocument||b[0]&&b[0].ownerDocument||s;for(var e=[],j=0,i;(i=a[j])!=null;j++){if(typeof i==="number")i+="";if(i){if(typeof i==="string"&&!jb.test(i))i=b.createTextNode(i);else if(typeof i==="string"){i=i.replace(Ka,Ma);var o=(La.exec(i)||["",
""])[1].toLowerCase(),k=F[o]||F._default,n=k[0],r=b.createElement("div");for(r.innerHTML=k[1]+i+k[2];n--;)r=r.lastChild;if(!c.support.tbody){n=ib.test(i);o=o==="table"&&!n?r.firstChild&&r.firstChild.childNodes:k[1]==="<table>"&&!n?r.childNodes:[];for(k=o.length-1;k>=0;--k)c.nodeName(o[k],"tbody")&&!o[k].childNodes.length&&o[k].parentNode.removeChild(o[k])}!c.support.leadingWhitespace&&V.test(i)&&r.insertBefore(b.createTextNode(V.exec(i)[0]),r.firstChild);i=r.childNodes}if(i.nodeType)e.push(i);else e=
c.merge(e,i)}}if(d)for(j=0;e[j];j++)if(f&&c.nodeName(e[j],"script")&&(!e[j].type||e[j].type.toLowerCase()==="text/javascript"))f.push(e[j].parentNode?e[j].parentNode.removeChild(e[j]):e[j]);else{e[j].nodeType===1&&e.splice.apply(e,[j+1,0].concat(c.makeArray(e[j].getElementsByTagName("script"))));d.appendChild(e[j])}return e},cleanData:function(a){for(var b,d,f=c.cache,e=c.event.special,j=c.support.deleteExpando,i=0,o;(o=a[i])!=null;i++)if(d=o[c.expando]){b=f[d];if(b.events)for(var k in b.events)e[k]?
c.event.remove(o,k):Ca(o,k,b.handle);if(j)delete o[c.expando];else o.removeAttribute&&o.removeAttribute(c.expando);delete f[d]}}});var kb=/z-?index|font-?weight|opacity|zoom|line-?height/i,Na=/alpha\([^)]*\)/,Oa=/opacity=([^)]*)/,ha=/float/i,ia=/-([a-z])/ig,lb=/([A-Z])/g,mb=/^-?\d+(?:px)?$/i,nb=/^-?\d/,ob={position:"absolute",visibility:"hidden",display:"block"},pb=["Left","Right"],qb=["Top","Bottom"],rb=s.defaultView&&s.defaultView.getComputedStyle,Pa=c.support.cssFloat?"cssFloat":"styleFloat",ja=
function(a,b){return b.toUpperCase()};c.fn.css=function(a,b){return X(this,a,b,true,function(d,f,e){if(e===w)return c.curCSS(d,f);if(typeof e==="number"&&!kb.test(f))e+="px";c.style(d,f,e)})};c.extend({style:function(a,b,d){if(!a||a.nodeType===3||a.nodeType===8)return w;if((b==="width"||b==="height")&&parseFloat(d)<0)d=w;var f=a.style||a,e=d!==w;if(!c.support.opacity&&b==="opacity"){if(e){f.zoom=1;b=parseInt(d,10)+""==="NaN"?"":"alpha(opacity="+d*100+")";a=f.filter||c.curCSS(a,"filter")||"";f.filter=
Na.test(a)?a.replace(Na,b):b}return f.filter&&f.filter.indexOf("opacity=")>=0?parseFloat(Oa.exec(f.filter)[1])/100+"":""}if(ha.test(b))b=Pa;b=b.replace(ia,ja);if(e)f[b]=d;return f[b]},css:function(a,b,d,f){if(b==="width"||b==="height"){var e,j=b==="width"?pb:qb;function i(){e=b==="width"?a.offsetWidth:a.offsetHeight;f!=="border"&&c.each(j,function(){f||(e-=parseFloat(c.curCSS(a,"padding"+this,true))||0);if(f==="margin")e+=parseFloat(c.curCSS(a,"margin"+this,true))||0;else e-=parseFloat(c.curCSS(a,
"border"+this+"Width",true))||0})}a.offsetWidth!==0?i():c.swap(a,ob,i);return Math.max(0,Math.round(e))}return c.curCSS(a,b,d)},curCSS:function(a,b,d){var f,e=a.style;if(!c.support.opacity&&b==="opacity"&&a.currentStyle){f=Oa.test(a.currentStyle.filter||"")?parseFloat(RegExp.$1)/100+"":"";return f===""?"1":f}if(ha.test(b))b=Pa;if(!d&&e&&e[b])f=e[b];else if(rb){if(ha.test(b))b="float";b=b.replace(lb,"-$1").toLowerCase();e=a.ownerDocument.defaultView;if(!e)return null;if(a=e.getComputedStyle(a,null))f=
a.getPropertyValue(b);if(b==="opacity"&&f==="")f="1"}else if(a.currentStyle){d=b.replace(ia,ja);f=a.currentStyle[b]||a.currentStyle[d];if(!mb.test(f)&&nb.test(f)){b=e.left;var j=a.runtimeStyle.left;a.runtimeStyle.left=a.currentStyle.left;e.left=d==="fontSize"?"1em":f||0;f=e.pixelLeft+"px";e.left=b;a.runtimeStyle.left=j}}return f},swap:function(a,b,d){var f={};for(var e in b){f[e]=a.style[e];a.style[e]=b[e]}d.call(a);for(e in b)a.style[e]=f[e]}});if(c.expr&&c.expr.filters){c.expr.filters.hidden=function(a){var b=
a.offsetWidth,d=a.offsetHeight,f=a.nodeName.toLowerCase()==="tr";return b===0&&d===0&&!f?true:b>0&&d>0&&!f?false:c.curCSS(a,"display")==="none"};c.expr.filters.visible=function(a){return!c.expr.filters.hidden(a)}}var sb=J(),tb=/<script(.|\s)*?\/script>/gi,ub=/select|textarea/i,vb=/color|date|datetime|email|hidden|month|number|password|range|search|tel|text|time|url|week/i,N=/=\?(&|$)/,ka=/\?/,wb=/(\?|&)_=.*?(&|$)/,xb=/^(\w+:)?\/\/([^\/?#]+)/,yb=/%20/g,zb=c.fn.load;c.fn.extend({load:function(a,b,d){if(typeof a!==
"string")return zb.call(this,a);else if(!this.length)return this;var f=a.indexOf(" ");if(f>=0){var e=a.slice(f,a.length);a=a.slice(0,f)}f="GET";if(b)if(c.isFunction(b)){d=b;b=null}else if(typeof b==="object"){b=c.param(b,c.ajaxSettings.traditional);f="POST"}var j=this;c.ajax({url:a,type:f,dataType:"html",data:b,complete:function(i,o){if(o==="success"||o==="notmodified")j.html(e?c("<div />").append(i.responseText.replace(tb,"")).find(e):i.responseText);d&&j.each(d,[i.responseText,o,i])}});return this},
serialize:function(){return c.param(this.serializeArray())},serializeArray:function(){return this.map(function(){return this.elements?c.makeArray(this.elements):this}).filter(function(){return this.name&&!this.disabled&&(this.checked||ub.test(this.nodeName)||vb.test(this.type))}).map(function(a,b){a=c(this).val();return a==null?null:c.isArray(a)?c.map(a,function(d){return{name:b.name,value:d}}):{name:b.name,value:a}}).get()}});c.each("ajaxStart ajaxStop ajaxComplete ajaxError ajaxSuccess ajaxSend".split(" "),
function(a,b){c.fn[b]=function(d){return this.bind(b,d)}});c.extend({get:function(a,b,d,f){if(c.isFunction(b)){f=f||d;d=b;b=null}return c.ajax({type:"GET",url:a,data:b,success:d,dataType:f})},getScript:function(a,b){return c.get(a,null,b,"script")},getJSON:function(a,b,d){return c.get(a,b,d,"json")},post:function(a,b,d,f){if(c.isFunction(b)){f=f||d;d=b;b={}}return c.ajax({type:"POST",url:a,data:b,success:d,dataType:f})},ajaxSetup:function(a){c.extend(c.ajaxSettings,a)},ajaxSettings:{url:location.href,
global:true,type:"GET",contentType:"application/x-www-form-urlencoded",processData:true,async:true,xhr:A.XMLHttpRequest&&(A.location.protocol!=="file:"||!A.ActiveXObject)?function(){return new A.XMLHttpRequest}:function(){try{return new A.ActiveXObject("Microsoft.XMLHTTP")}catch(a){}},accepts:{xml:"application/xml, text/xml",html:"text/html",script:"text/javascript, application/javascript",json:"application/json, text/javascript",text:"text/plain",_default:"*/*"}},lastModified:{},etag:{},ajax:function(a){function b(){e.success&&
e.success.call(k,o,i,x);e.global&&f("ajaxSuccess",[x,e])}function d(){e.complete&&e.complete.call(k,x,i);e.global&&f("ajaxComplete",[x,e]);e.global&&!--c.active&&c.event.trigger("ajaxStop")}function f(q,p){(e.context?c(e.context):c.event).trigger(q,p)}var e=c.extend(true,{},c.ajaxSettings,a),j,i,o,k=a&&a.context||e,n=e.type.toUpperCase();if(e.data&&e.processData&&typeof e.data!=="string")e.data=c.param(e.data,e.traditional);if(e.dataType==="jsonp"){if(n==="GET")N.test(e.url)||(e.url+=(ka.test(e.url)?
"&":"?")+(e.jsonp||"callback")+"=?");else if(!e.data||!N.test(e.data))e.data=(e.data?e.data+"&":"")+(e.jsonp||"callback")+"=?";e.dataType="json"}if(e.dataType==="json"&&(e.data&&N.test(e.data)||N.test(e.url))){j=e.jsonpCallback||"jsonp"+sb++;if(e.data)e.data=(e.data+"").replace(N,"="+j+"$1");e.url=e.url.replace(N,"="+j+"$1");e.dataType="script";A[j]=A[j]||function(q){o=q;b();d();A[j]=w;try{delete A[j]}catch(p){}z&&z.removeChild(C)}}if(e.dataType==="script"&&e.cache===null)e.cache=false;if(e.cache===
false&&n==="GET"){var r=J(),u=e.url.replace(wb,"$1_="+r+"$2");e.url=u+(u===e.url?(ka.test(e.url)?"&":"?")+"_="+r:"")}if(e.data&&n==="GET")e.url+=(ka.test(e.url)?"&":"?")+e.data;e.global&&!c.active++&&c.event.trigger("ajaxStart");r=(r=xb.exec(e.url))&&(r[1]&&r[1]!==location.protocol||r[2]!==location.host);if(e.dataType==="script"&&n==="GET"&&r){var z=s.getElementsByTagName("head")[0]||s.documentElement,C=s.createElement("script");C.src=e.url;if(e.scriptCharset)C.charset=e.scriptCharset;if(!j){var B=
false;C.onload=C.onreadystatechange=function(){if(!B&&(!this.readyState||this.readyState==="loaded"||this.readyState==="complete")){B=true;b();d();C.onload=C.onreadystatechange=null;z&&C.parentNode&&z.removeChild(C)}}}z.insertBefore(C,z.firstChild);return w}var E=false,x=e.xhr();if(x){e.username?x.open(n,e.url,e.async,e.username,e.password):x.open(n,e.url,e.async);try{if(e.data||a&&a.contentType)x.setRequestHeader("Content-Type",e.contentType);if(e.ifModified){c.lastModified[e.url]&&x.setRequestHeader("If-Modified-Since",
c.lastModified[e.url]);c.etag[e.url]&&x.setRequestHeader("If-None-Match",c.etag[e.url])}r||x.setRequestHeader("X-Requested-With","XMLHttpRequest");x.setRequestHeader("Accept",e.dataType&&e.accepts[e.dataType]?e.accepts[e.dataType]+", */*":e.accepts._default)}catch(ga){}if(e.beforeSend&&e.beforeSend.call(k,x,e)===false){e.global&&!--c.active&&c.event.trigger("ajaxStop");x.abort();return false}e.global&&f("ajaxSend",[x,e]);var g=x.onreadystatechange=function(q){if(!x||x.readyState===0||q==="abort"){E||
d();E=true;if(x)x.onreadystatechange=c.noop}else if(!E&&x&&(x.readyState===4||q==="timeout")){E=true;x.onreadystatechange=c.noop;i=q==="timeout"?"timeout":!c.httpSuccess(x)?"error":e.ifModified&&c.httpNotModified(x,e.url)?"notmodified":"success";var p;if(i==="success")try{o=c.httpData(x,e.dataType,e)}catch(v){i="parsererror";p=v}if(i==="success"||i==="notmodified")j||b();else c.handleError(e,x,i,p);d();q==="timeout"&&x.abort();if(e.async)x=null}};try{var h=x.abort;x.abort=function(){x&&h.call(x);
g("abort")}}catch(l){}e.async&&e.timeout>0&&setTimeout(function(){x&&!E&&g("timeout")},e.timeout);try{x.send(n==="POST"||n==="PUT"||n==="DELETE"?e.data:null)}catch(m){c.handleError(e,x,null,m);d()}e.async||g();return x}},handleError:function(a,b,d,f){if(a.error)a.error.call(a.context||a,b,d,f);if(a.global)(a.context?c(a.context):c.event).trigger("ajaxError",[b,a,f])},active:0,httpSuccess:function(a){try{return!a.status&&location.protocol==="file:"||a.status>=200&&a.status<300||a.status===304||a.status===
1223||a.status===0}catch(b){}return false},httpNotModified:function(a,b){var d=a.getResponseHeader("Last-Modified"),f=a.getResponseHeader("Etag");if(d)c.lastModified[b]=d;if(f)c.etag[b]=f;return a.status===304||a.status===0},httpData:function(a,b,d){var f=a.getResponseHeader("content-type")||"",e=b==="xml"||!b&&f.indexOf("xml")>=0;a=e?a.responseXML:a.responseText;e&&a.documentElement.nodeName==="parsererror"&&c.error("parsererror");if(d&&d.dataFilter)a=d.dataFilter(a,b);if(typeof a==="string")if(b===
"json"||!b&&f.indexOf("json")>=0)a=c.parseJSON(a);else if(b==="script"||!b&&f.indexOf("javascript")>=0)c.globalEval(a);return a},param:function(a,b){function d(i,o){if(c.isArray(o))c.each(o,function(k,n){b||/\[\]$/.test(i)?f(i,n):d(i+"["+(typeof n==="object"||c.isArray(n)?k:"")+"]",n)});else!b&&o!=null&&typeof o==="object"?c.each(o,function(k,n){d(i+"["+k+"]",n)}):f(i,o)}function f(i,o){o=c.isFunction(o)?o():o;e[e.length]=encodeURIComponent(i)+"="+encodeURIComponent(o)}var e=[];if(b===w)b=c.ajaxSettings.traditional;
if(c.isArray(a)||a.jquery)c.each(a,function(){f(this.name,this.value)});else for(var j in a)d(j,a[j]);return e.join("&").replace(yb,"+")}});var la={},Ab=/toggle|show|hide/,Bb=/^([+-]=)?([\d+-.]+)(.*)$/,W,va=[["height","marginTop","marginBottom","paddingTop","paddingBottom"],["width","marginLeft","marginRight","paddingLeft","paddingRight"],["opacity"]];c.fn.extend({show:function(a,b){if(a||a===0)return this.animate(K("show",3),a,b);else{a=0;for(b=this.length;a<b;a++){var d=c.data(this[a],"olddisplay");
this[a].style.display=d||"";if(c.css(this[a],"display")==="none"){d=this[a].nodeName;var f;if(la[d])f=la[d];else{var e=c("<"+d+" />").appendTo("body");f=e.css("display");if(f==="none")f="block";e.remove();la[d]=f}c.data(this[a],"olddisplay",f)}}a=0;for(b=this.length;a<b;a++)this[a].style.display=c.data(this[a],"olddisplay")||"";return this}},hide:function(a,b){if(a||a===0)return this.animate(K("hide",3),a,b);else{a=0;for(b=this.length;a<b;a++){var d=c.data(this[a],"olddisplay");!d&&d!=="none"&&c.data(this[a],
"olddisplay",c.css(this[a],"display"))}a=0;for(b=this.length;a<b;a++)this[a].style.display="none";return this}},_toggle:c.fn.toggle,toggle:function(a,b){var d=typeof a==="boolean";if(c.isFunction(a)&&c.isFunction(b))this._toggle.apply(this,arguments);else a==null||d?this.each(function(){var f=d?a:c(this).is(":hidden");c(this)[f?"show":"hide"]()}):this.animate(K("toggle",3),a,b);return this},fadeTo:function(a,b,d){return this.filter(":hidden").css("opacity",0).show().end().animate({opacity:b},a,d)},
animate:function(a,b,d,f){var e=c.speed(b,d,f);if(c.isEmptyObject(a))return this.each(e.complete);return this[e.queue===false?"each":"queue"](function(){var j=c.extend({},e),i,o=this.nodeType===1&&c(this).is(":hidden"),k=this;for(i in a){var n=i.replace(ia,ja);if(i!==n){a[n]=a[i];delete a[i];i=n}if(a[i]==="hide"&&o||a[i]==="show"&&!o)return j.complete.call(this);if((i==="height"||i==="width")&&this.style){j.display=c.css(this,"display");j.overflow=this.style.overflow}if(c.isArray(a[i])){(j.specialEasing=
j.specialEasing||{})[i]=a[i][1];a[i]=a[i][0]}}if(j.overflow!=null)this.style.overflow="hidden";j.curAnim=c.extend({},a);c.each(a,function(r,u){var z=new c.fx(k,j,r);if(Ab.test(u))z[u==="toggle"?o?"show":"hide":u](a);else{var C=Bb.exec(u),B=z.cur(true)||0;if(C){u=parseFloat(C[2]);var E=C[3]||"px";if(E!=="px"){k.style[r]=(u||1)+E;B=(u||1)/z.cur(true)*B;k.style[r]=B+E}if(C[1])u=(C[1]==="-="?-1:1)*u+B;z.custom(B,u,E)}else z.custom(B,u,"")}});return true})},stop:function(a,b){var d=c.timers;a&&this.queue([]);
this.each(function(){for(var f=d.length-1;f>=0;f--)if(d[f].elem===this){b&&d[f](true);d.splice(f,1)}});b||this.dequeue();return this}});c.each({slideDown:K("show",1),slideUp:K("hide",1),slideToggle:K("toggle",1),fadeIn:{opacity:"show"},fadeOut:{opacity:"hide"}},function(a,b){c.fn[a]=function(d,f){return this.animate(b,d,f)}});c.extend({speed:function(a,b,d){var f=a&&typeof a==="object"?a:{complete:d||!d&&b||c.isFunction(a)&&a,duration:a,easing:d&&b||b&&!c.isFunction(b)&&b};f.duration=c.fx.off?0:typeof f.duration===
"number"?f.duration:c.fx.speeds[f.duration]||c.fx.speeds._default;f.old=f.complete;f.complete=function(){f.queue!==false&&c(this).dequeue();c.isFunction(f.old)&&f.old.call(this)};return f},easing:{linear:function(a,b,d,f){return d+f*a},swing:function(a,b,d,f){return(-Math.cos(a*Math.PI)/2+0.5)*f+d}},timers:[],fx:function(a,b,d){this.options=b;this.elem=a;this.prop=d;if(!b.orig)b.orig={}}});c.fx.prototype={update:function(){this.options.step&&this.options.step.call(this.elem,this.now,this);(c.fx.step[this.prop]||
c.fx.step._default)(this);if((this.prop==="height"||this.prop==="width")&&this.elem.style)this.elem.style.display="block"},cur:function(a){if(this.elem[this.prop]!=null&&(!this.elem.style||this.elem.style[this.prop]==null))return this.elem[this.prop];return(a=parseFloat(c.css(this.elem,this.prop,a)))&&a>-10000?a:parseFloat(c.curCSS(this.elem,this.prop))||0},custom:function(a,b,d){function f(j){return e.step(j)}this.startTime=J();this.start=a;this.end=b;this.unit=d||this.unit||"px";this.now=this.start;
this.pos=this.state=0;var e=this;f.elem=this.elem;if(f()&&c.timers.push(f)&&!W)W=setInterval(c.fx.tick,13)},show:function(){this.options.orig[this.prop]=c.style(this.elem,this.prop);this.options.show=true;this.custom(this.prop==="width"||this.prop==="height"?1:0,this.cur());c(this.elem).show()},hide:function(){this.options.orig[this.prop]=c.style(this.elem,this.prop);this.options.hide=true;this.custom(this.cur(),0)},step:function(a){var b=J(),d=true;if(a||b>=this.options.duration+this.startTime){this.now=
this.end;this.pos=this.state=1;this.update();this.options.curAnim[this.prop]=true;for(var f in this.options.curAnim)if(this.options.curAnim[f]!==true)d=false;if(d){if(this.options.display!=null){this.elem.style.overflow=this.options.overflow;a=c.data(this.elem,"olddisplay");this.elem.style.display=a?a:this.options.display;if(c.css(this.elem,"display")==="none")this.elem.style.display="block"}this.options.hide&&c(this.elem).hide();if(this.options.hide||this.options.show)for(var e in this.options.curAnim)c.style(this.elem,
e,this.options.orig[e]);this.options.complete.call(this.elem)}return false}else{e=b-this.startTime;this.state=e/this.options.duration;a=this.options.easing||(c.easing.swing?"swing":"linear");this.pos=c.easing[this.options.specialEasing&&this.options.specialEasing[this.prop]||a](this.state,e,0,1,this.options.duration);this.now=this.start+(this.end-this.start)*this.pos;this.update()}return true}};c.extend(c.fx,{tick:function(){for(var a=c.timers,b=0;b<a.length;b++)a[b]()||a.splice(b--,1);a.length||
c.fx.stop()},stop:function(){clearInterval(W);W=null},speeds:{slow:600,fast:200,_default:400},step:{opacity:function(a){c.style(a.elem,"opacity",a.now)},_default:function(a){if(a.elem.style&&a.elem.style[a.prop]!=null)a.elem.style[a.prop]=(a.prop==="width"||a.prop==="height"?Math.max(0,a.now):a.now)+a.unit;else a.elem[a.prop]=a.now}}});if(c.expr&&c.expr.filters)c.expr.filters.animated=function(a){return c.grep(c.timers,function(b){return a===b.elem}).length};c.fn.offset="getBoundingClientRect"in s.documentElement?
function(a){var b=this[0];if(a)return this.each(function(e){c.offset.setOffset(this,a,e)});if(!b||!b.ownerDocument)return null;if(b===b.ownerDocument.body)return c.offset.bodyOffset(b);var d=b.getBoundingClientRect(),f=b.ownerDocument;b=f.body;f=f.documentElement;return{top:d.top+(self.pageYOffset||c.support.boxModel&&f.scrollTop||b.scrollTop)-(f.clientTop||b.clientTop||0),left:d.left+(self.pageXOffset||c.support.boxModel&&f.scrollLeft||b.scrollLeft)-(f.clientLeft||b.clientLeft||0)}}:function(a){var b=
this[0];if(a)return this.each(function(r){c.offset.setOffset(this,a,r)});if(!b||!b.ownerDocument)return null;if(b===b.ownerDocument.body)return c.offset.bodyOffset(b);c.offset.initialize();var d=b.offsetParent,f=b,e=b.ownerDocument,j,i=e.documentElement,o=e.body;f=(e=e.defaultView)?e.getComputedStyle(b,null):b.currentStyle;for(var k=b.offsetTop,n=b.offsetLeft;(b=b.parentNode)&&b!==o&&b!==i;){if(c.offset.supportsFixedPosition&&f.position==="fixed")break;j=e?e.getComputedStyle(b,null):b.currentStyle;
k-=b.scrollTop;n-=b.scrollLeft;if(b===d){k+=b.offsetTop;n+=b.offsetLeft;if(c.offset.doesNotAddBorder&&!(c.offset.doesAddBorderForTableAndCells&&/^t(able|d|h)$/i.test(b.nodeName))){k+=parseFloat(j.borderTopWidth)||0;n+=parseFloat(j.borderLeftWidth)||0}f=d;d=b.offsetParent}if(c.offset.subtractsBorderForOverflowNotVisible&&j.overflow!=="visible"){k+=parseFloat(j.borderTopWidth)||0;n+=parseFloat(j.borderLeftWidth)||0}f=j}if(f.position==="relative"||f.position==="static"){k+=o.offsetTop;n+=o.offsetLeft}if(c.offset.supportsFixedPosition&&
f.position==="fixed"){k+=Math.max(i.scrollTop,o.scrollTop);n+=Math.max(i.scrollLeft,o.scrollLeft)}return{top:k,left:n}};c.offset={initialize:function(){var a=s.body,b=s.createElement("div"),d,f,e,j=parseFloat(c.curCSS(a,"marginTop",true))||0;c.extend(b.style,{position:"absolute",top:0,left:0,margin:0,border:0,width:"1px",height:"1px",visibility:"hidden"});b.innerHTML="<div style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;'><div></div></div><table style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;' cellpadding='0' cellspacing='0'><tr><td></td></tr></table>";
a.insertBefore(b,a.firstChild);d=b.firstChild;f=d.firstChild;e=d.nextSibling.firstChild.firstChild;this.doesNotAddBorder=f.offsetTop!==5;this.doesAddBorderForTableAndCells=e.offsetTop===5;f.style.position="fixed";f.style.top="20px";this.supportsFixedPosition=f.offsetTop===20||f.offsetTop===15;f.style.position=f.style.top="";d.style.overflow="hidden";d.style.position="relative";this.subtractsBorderForOverflowNotVisible=f.offsetTop===-5;this.doesNotIncludeMarginInBodyOffset=a.offsetTop!==j;a.removeChild(b);
c.offset.initialize=c.noop},bodyOffset:function(a){var b=a.offsetTop,d=a.offsetLeft;c.offset.initialize();if(c.offset.doesNotIncludeMarginInBodyOffset){b+=parseFloat(c.curCSS(a,"marginTop",true))||0;d+=parseFloat(c.curCSS(a,"marginLeft",true))||0}return{top:b,left:d}},setOffset:function(a,b,d){if(/static/.test(c.curCSS(a,"position")))a.style.position="relative";var f=c(a),e=f.offset(),j=parseInt(c.curCSS(a,"top",true),10)||0,i=parseInt(c.curCSS(a,"left",true),10)||0;if(c.isFunction(b))b=b.call(a,
d,e);d={top:b.top-e.top+j,left:b.left-e.left+i};"using"in b?b.using.call(a,d):f.css(d)}};c.fn.extend({position:function(){if(!this[0])return null;var a=this[0],b=this.offsetParent(),d=this.offset(),f=/^body|html$/i.test(b[0].nodeName)?{top:0,left:0}:b.offset();d.top-=parseFloat(c.curCSS(a,"marginTop",true))||0;d.left-=parseFloat(c.curCSS(a,"marginLeft",true))||0;f.top+=parseFloat(c.curCSS(b[0],"borderTopWidth",true))||0;f.left+=parseFloat(c.curCSS(b[0],"borderLeftWidth",true))||0;return{top:d.top-
f.top,left:d.left-f.left}},offsetParent:function(){return this.map(function(){for(var a=this.offsetParent||s.body;a&&!/^body|html$/i.test(a.nodeName)&&c.css(a,"position")==="static";)a=a.offsetParent;return a})}});c.each(["Left","Top"],function(a,b){var d="scroll"+b;c.fn[d]=function(f){var e=this[0],j;if(!e)return null;if(f!==w)return this.each(function(){if(j=wa(this))j.scrollTo(!a?f:c(j).scrollLeft(),a?f:c(j).scrollTop());else this[d]=f});else return(j=wa(e))?"pageXOffset"in j?j[a?"pageYOffset":
"pageXOffset"]:c.support.boxModel&&j.document.documentElement[d]||j.document.body[d]:e[d]}});c.each(["Height","Width"],function(a,b){var d=b.toLowerCase();c.fn["inner"+b]=function(){return this[0]?c.css(this[0],d,false,"padding"):null};c.fn["outer"+b]=function(f){return this[0]?c.css(this[0],d,false,f?"margin":"border"):null};c.fn[d]=function(f){var e=this[0];if(!e)return f==null?null:this;if(c.isFunction(f))return this.each(function(j){var i=c(this);i[d](f.call(this,j,i[d]()))});return"scrollTo"in
e&&e.document?e.document.compatMode==="CSS1Compat"&&e.document.documentElement["client"+b]||e.document.body["client"+b]:e.nodeType===9?Math.max(e.documentElement["client"+b],e.body["scroll"+b],e.documentElement["scroll"+b],e.body["offset"+b],e.documentElement["offset"+b]):f===w?c.css(e,d):this.css(d,typeof f==="string"?f:f+"px")}});A.jQuery=A.$=c})(window);


Encoder = {

	// When encoding do we convert characters into html or numerical entities
	EncodeType : "entity",  // entity OR numerical

	isEmpty : function(val){
		if(val){
			return ((val===null) || val.length==0 || /^\s+$/.test(val));
		}else{
			return true;
		}
	},
	// Convert HTML entities into numerical entities
	HTML2Numerical : function(s){
		var arr1 = new Array('&nbsp;','&iexcl;','&cent;','&pound;','&curren;','&yen;','&brvbar;','&sect;','&uml;','&copy;','&ordf;','&laquo;','&not;','&shy;','&reg;','&macr;','&deg;','&plusmn;','&sup2;','&sup3;','&acute;','&micro;','&para;','&middot;','&cedil;','&sup1;','&ordm;','&raquo;','&frac14;','&frac12;','&frac34;','&iquest;','&agrave;','&aacute;','&acirc;','&atilde;','&Auml;','&aring;','&aelig;','&ccedil;','&egrave;','&eacute;','&ecirc;','&euml;','&igrave;','&iacute;','&icirc;','&iuml;','&eth;','&ntilde;','&ograve;','&oacute;','&ocirc;','&otilde;','&Ouml;','&times;','&oslash;','&ugrave;','&uacute;','&ucirc;','&Uuml;','&yacute;','&thorn;','&szlig;','&agrave;','&aacute;','&acirc;','&atilde;','&auml;','&aring;','&aelig;','&ccedil;','&egrave;','&eacute;','&ecirc;','&euml;','&igrave;','&iacute;','&icirc;','&iuml;','&eth;','&ntilde;','&ograve;','&oacute;','&ocirc;','&otilde;','&ouml;','&divide;','&Oslash;','&ugrave;','&uacute;','&ucirc;','&uuml;','&yacute;','&thorn;','&yuml;','&quot;','&amp;','&lt;','&gt;','&oelig;','&oelig;','&scaron;','&scaron;','&yuml;','&circ;','&tilde;','&ensp;','&emsp;','&thinsp;','&zwnj;','&zwj;','&lrm;','&rlm;','&ndash;','&mdash;','&lsquo;','&rsquo;','&sbquo;','&ldquo;','&rdquo;','&bdquo;','&dagger;','&dagger;','&permil;','&lsaquo;','&rsaquo;','&euro;','&fnof;','&alpha;','&beta;','&gamma;','&delta;','&epsilon;','&zeta;','&eta;','&theta;','&iota;','&kappa;','&lambda;','&mu;','&nu;','&xi;','&omicron;','&pi;','&rho;','&sigma;','&tau;','&upsilon;','&phi;','&chi;','&psi;','&omega;','&alpha;','&beta;','&gamma;','&delta;','&epsilon;','&zeta;','&eta;','&theta;','&iota;','&kappa;','&lambda;','&mu;','&nu;','&xi;','&omicron;','&pi;','&rho;','&sigmaf;','&sigma;','&tau;','&upsilon;','&phi;','&chi;','&psi;','&omega;','&thetasym;','&upsih;','&piv;','&bull;','&hellip;','&prime;','&prime;','&oline;','&frasl;','&weierp;','&image;','&real;','&trade;','&alefsym;','&larr;','&uarr;','&rarr;','&darr;','&harr;','&crarr;','&larr;','&uarr;','&rarr;','&darr;','&harr;','&forall;','&part;','&exist;','&empty;','&nabla;','&isin;','&notin;','&ni;','&prod;','&sum;','&minus;','&lowast;','&radic;','&prop;','&infin;','&ang;','&and;','&or;','&cap;','&cup;','&int;','&there4;','&sim;','&cong;','&asymp;','&ne;','&equiv;','&le;','&ge;','&sub;','&sup;','&nsub;','&sube;','&supe;','&oplus;','&otimes;','&perp;','&sdot;','&lceil;','&rceil;','&lfloor;','&rfloor;','&lang;','&rang;','&loz;','&spades;','&clubs;','&hearts;','&diams;');
		var arr2 = new Array('&#160;','&#161;','&#162;','&#163;','&#164;','&#165;','&#166;','&#167;','&#168;','&#169;','&#170;','&#171;','&#172;','&#173;','&#174;','&#175;','&#176;','&#177;','&#178;','&#179;','&#180;','&#181;','&#182;','&#183;','&#184;','&#185;','&#186;','&#187;','&#188;','&#189;','&#190;','&#191;','&#192;','&#193;','&#194;','&#195;','&#196;','&#197;','&#198;','&#199;','&#200;','&#201;','&#202;','&#203;','&#204;','&#205;','&#206;','&#207;','&#208;','&#209;','&#210;','&#211;','&#212;','&#213;','&#214;','&#215;','&#216;','&#217;','&#218;','&#219;','&#220;','&#221;','&#222;','&#223;','&#224;','&#225;','&#226;','&#227;','&#228;','&#229;','&#230;','&#231;','&#232;','&#233;','&#234;','&#235;','&#236;','&#237;','&#238;','&#239;','&#240;','&#241;','&#242;','&#243;','&#244;','&#245;','&#246;','&#247;','&#248;','&#249;','&#250;','&#251;','&#252;','&#253;','&#254;','&#255;','&#34;','&#38;','&#60;','&#62;','&#338;','&#339;','&#352;','&#353;','&#376;','&#710;','&#732;','&#8194;','&#8195;','&#8201;','&#8204;','&#8205;','&#8206;','&#8207;','&#8211;','&#8212;','&#8216;','&#8217;','&#8218;','&#8220;','&#8221;','&#8222;','&#8224;','&#8225;','&#8240;','&#8249;','&#8250;','&#8364;','&#402;','&#913;','&#914;','&#915;','&#916;','&#917;','&#918;','&#919;','&#920;','&#921;','&#922;','&#923;','&#924;','&#925;','&#926;','&#927;','&#928;','&#929;','&#931;','&#932;','&#933;','&#934;','&#935;','&#936;','&#937;','&#945;','&#946;','&#947;','&#948;','&#949;','&#950;','&#951;','&#952;','&#953;','&#954;','&#955;','&#956;','&#957;','&#958;','&#959;','&#960;','&#961;','&#962;','&#963;','&#964;','&#965;','&#966;','&#967;','&#968;','&#969;','&#977;','&#978;','&#982;','&#8226;','&#8230;','&#8242;','&#8243;','&#8254;','&#8260;','&#8472;','&#8465;','&#8476;','&#8482;','&#8501;','&#8592;','&#8593;','&#8594;','&#8595;','&#8596;','&#8629;','&#8656;','&#8657;','&#8658;','&#8659;','&#8660;','&#8704;','&#8706;','&#8707;','&#8709;','&#8711;','&#8712;','&#8713;','&#8715;','&#8719;','&#8721;','&#8722;','&#8727;','&#8730;','&#8733;','&#8734;','&#8736;','&#8743;','&#8744;','&#8745;','&#8746;','&#8747;','&#8756;','&#8764;','&#8773;','&#8776;','&#8800;','&#8801;','&#8804;','&#8805;','&#8834;','&#8835;','&#8836;','&#8838;','&#8839;','&#8853;','&#8855;','&#8869;','&#8901;','&#8968;','&#8969;','&#8970;','&#8971;','&#9001;','&#9002;','&#9674;','&#9824;','&#9827;','&#9829;','&#9830;');
		return this.swapArrayVals(s,arr1,arr2);
	},	

	// Convert Numerical entities into HTML entities
	NumericalToHTML : function(s){
		var arr1 = new Array('&#160;','&#161;','&#162;','&#163;','&#164;','&#165;','&#166;','&#167;','&#168;','&#169;','&#170;','&#171;','&#172;','&#173;','&#174;','&#175;','&#176;','&#177;','&#178;','&#179;','&#180;','&#181;','&#182;','&#183;','&#184;','&#185;','&#186;','&#187;','&#188;','&#189;','&#190;','&#191;','&#192;','&#193;','&#194;','&#195;','&#196;','&#197;','&#198;','&#199;','&#200;','&#201;','&#202;','&#203;','&#204;','&#205;','&#206;','&#207;','&#208;','&#209;','&#210;','&#211;','&#212;','&#213;','&#214;','&#215;','&#216;','&#217;','&#218;','&#219;','&#220;','&#221;','&#222;','&#223;','&#224;','&#225;','&#226;','&#227;','&#228;','&#229;','&#230;','&#231;','&#232;','&#233;','&#234;','&#235;','&#236;','&#237;','&#238;','&#239;','&#240;','&#241;','&#242;','&#243;','&#244;','&#245;','&#246;','&#247;','&#248;','&#249;','&#250;','&#251;','&#252;','&#253;','&#254;','&#255;','&#34;','&#38;','&#60;','&#62;','&#338;','&#339;','&#352;','&#353;','&#376;','&#710;','&#732;','&#8194;','&#8195;','&#8201;','&#8204;','&#8205;','&#8206;','&#8207;','&#8211;','&#8212;','&#8216;','&#8217;','&#8218;','&#8220;','&#8221;','&#8222;','&#8224;','&#8225;','&#8240;','&#8249;','&#8250;','&#8364;','&#402;','&#913;','&#914;','&#915;','&#916;','&#917;','&#918;','&#919;','&#920;','&#921;','&#922;','&#923;','&#924;','&#925;','&#926;','&#927;','&#928;','&#929;','&#931;','&#932;','&#933;','&#934;','&#935;','&#936;','&#937;','&#945;','&#946;','&#947;','&#948;','&#949;','&#950;','&#951;','&#952;','&#953;','&#954;','&#955;','&#956;','&#957;','&#958;','&#959;','&#960;','&#961;','&#962;','&#963;','&#964;','&#965;','&#966;','&#967;','&#968;','&#969;','&#977;','&#978;','&#982;','&#8226;','&#8230;','&#8242;','&#8243;','&#8254;','&#8260;','&#8472;','&#8465;','&#8476;','&#8482;','&#8501;','&#8592;','&#8593;','&#8594;','&#8595;','&#8596;','&#8629;','&#8656;','&#8657;','&#8658;','&#8659;','&#8660;','&#8704;','&#8706;','&#8707;','&#8709;','&#8711;','&#8712;','&#8713;','&#8715;','&#8719;','&#8721;','&#8722;','&#8727;','&#8730;','&#8733;','&#8734;','&#8736;','&#8743;','&#8744;','&#8745;','&#8746;','&#8747;','&#8756;','&#8764;','&#8773;','&#8776;','&#8800;','&#8801;','&#8804;','&#8805;','&#8834;','&#8835;','&#8836;','&#8838;','&#8839;','&#8853;','&#8855;','&#8869;','&#8901;','&#8968;','&#8969;','&#8970;','&#8971;','&#9001;','&#9002;','&#9674;','&#9824;','&#9827;','&#9829;','&#9830;');
		var arr2 = new Array('&nbsp;','&iexcl;','&cent;','&pound;','&curren;','&yen;','&brvbar;','&sect;','&uml;','&copy;','&ordf;','&laquo;','&not;','&shy;','&reg;','&macr;','&deg;','&plusmn;','&sup2;','&sup3;','&acute;','&micro;','&para;','&middot;','&cedil;','&sup1;','&ordm;','&raquo;','&frac14;','&frac12;','&frac34;','&iquest;','&agrave;','&aacute;','&acirc;','&atilde;','&Auml;','&aring;','&aelig;','&ccedil;','&egrave;','&eacute;','&ecirc;','&euml;','&igrave;','&iacute;','&icirc;','&iuml;','&eth;','&ntilde;','&ograve;','&oacute;','&ocirc;','&otilde;','&Ouml;','&times;','&oslash;','&ugrave;','&uacute;','&ucirc;','&Uuml;','&yacute;','&thorn;','&szlig;','&agrave;','&aacute;','&acirc;','&atilde;','&auml;','&aring;','&aelig;','&ccedil;','&egrave;','&eacute;','&ecirc;','&euml;','&igrave;','&iacute;','&icirc;','&iuml;','&eth;','&ntilde;','&ograve;','&oacute;','&ocirc;','&otilde;','&ouml;','&divide;','&Oslash;','&ugrave;','&uacute;','&ucirc;','&uuml;','&yacute;','&thorn;','&yuml;','&quot;','&amp;','&lt;','&gt;','&oelig;','&oelig;','&scaron;','&scaron;','&yuml;','&circ;','&tilde;','&ensp;','&emsp;','&thinsp;','&zwnj;','&zwj;','&lrm;','&rlm;','&ndash;','&mdash;','&lsquo;','&rsquo;','&sbquo;','&ldquo;','&rdquo;','&bdquo;','&dagger;','&dagger;','&permil;','&lsaquo;','&rsaquo;','&euro;','&fnof;','&alpha;','&beta;','&gamma;','&delta;','&epsilon;','&zeta;','&eta;','&theta;','&iota;','&kappa;','&lambda;','&mu;','&nu;','&xi;','&omicron;','&pi;','&rho;','&sigma;','&tau;','&upsilon;','&phi;','&chi;','&psi;','&omega;','&alpha;','&beta;','&gamma;','&delta;','&epsilon;','&zeta;','&eta;','&theta;','&iota;','&kappa;','&lambda;','&mu;','&nu;','&xi;','&omicron;','&pi;','&rho;','&sigmaf;','&sigma;','&tau;','&upsilon;','&phi;','&chi;','&psi;','&omega;','&thetasym;','&upsih;','&piv;','&bull;','&hellip;','&prime;','&prime;','&oline;','&frasl;','&weierp;','&image;','&real;','&trade;','&alefsym;','&larr;','&uarr;','&rarr;','&darr;','&harr;','&crarr;','&larr;','&uarr;','&rarr;','&darr;','&harr;','&forall;','&part;','&exist;','&empty;','&nabla;','&isin;','&notin;','&ni;','&prod;','&sum;','&minus;','&lowast;','&radic;','&prop;','&infin;','&ang;','&and;','&or;','&cap;','&cup;','&int;','&there4;','&sim;','&cong;','&asymp;','&ne;','&equiv;','&le;','&ge;','&sub;','&sup;','&nsub;','&sube;','&supe;','&oplus;','&otimes;','&perp;','&sdot;','&lceil;','&rceil;','&lfloor;','&rfloor;','&lang;','&rang;','&loz;','&spades;','&clubs;','&hearts;','&diams;');
		return this.swapArrayVals(s,arr1,arr2);
	},


	// Numerically encodes all unicode characters
	numEncode : function(s){
		
		if(this.isEmpty(s)) return "";

		var e = "";
		for (var i = 0; i < s.length; i++)
		{
			var c = s.charAt(i);
			if (c < " " || c > "~")
			{
				c = "&#" + c.charCodeAt() + ";";
			}
			e += c;
		}
		return e;
	},
	
	// HTML Decode numerical and HTML entities back to original values
	htmlDecode : function(s){

		var c,m,d = s;
		
		if(this.isEmpty(d)) return "";

		// convert HTML entites back to numerical entites first
		d = this.HTML2Numerical(d);
		
		// look for numerical entities &#34;
		arr=d.match(/&#[0-9]{1,5};/g);
		
		// if no matches found in string then skip
		if(arr!=null){
			for(var x=0;x<arr.length;x++){
				m = arr[x];
				c = m.substring(2,m.length-1); //get numeric part which is refernce to unicode character
				// if its a valid number we can decode
				if(c >= -32768 && c <= 65535){
					// decode every single match within string
					d = d.replace(m, String.fromCharCode(c));
				}else{
					d = d.replace(m, ""); //invalid so replace with nada
				}
			}			
		}

		return d;
	},		

	// encode an input string into either numerical or HTML entities
	htmlEncode : function(s,dbl){
			
		if(this.isEmpty(s)) return "";

		// do we allow double encoding? E.g will &amp; be turned into &amp;amp;
		dbl = dbl | false; //default to prevent double encoding
		
		// if allowing double encoding we do ampersands first
		if(dbl){
			if(this.EncodeType=="numerical"){
				s = s.replace(/&/g, "&#38;");
			}else{
				s = s.replace(/&/g, "&amp;");
			}
		}

		// convert the xss chars to numerical entities ' " < >
		s = this.XSSEncode(s,false);
		
		if(this.EncodeType=="numerical" || !dbl){
			// Now call function that will convert any HTML entities to numerical codes
			s = this.HTML2Numerical(s);
		}

		// Now encode all chars above 127 e.g unicode
		s = this.numEncode(s);

		// now we know anything that needs to be encoded has been converted to numerical entities we
		// can encode any ampersands & that are not part of encoded entities
		// to handle the fact that I need to do a negative check and handle multiple ampersands &&&
		// I am going to use a placeholder

		// if we don't want double encoded entities we ignore the & in existing entities
		if(!dbl){
			s = s.replace(/&#/g,"##AMPHASH##");
		
			if(this.EncodeType=="numerical"){
				s = s.replace(/&/g, "&#38;");
			}else{
				s = s.replace(/&/g, "&amp;");
			}

			s = s.replace(/##AMPHASH##/g,"&#");
		}
		
		// replace any malformed entities
		s = s.replace(/&#\d*([^\d;]|$)/g, "$1");

		if(!dbl){
			// safety check to correct any double encoded &amp;
			s = this.correctEncoding(s);
		}

		// now do we need to convert our numerical encoded string into entities
		if(this.EncodeType=="entity"){
			s = this.NumericalToHTML(s);
		}

		return s;					
	},

	// Encodes the basic 4 characters used to malform HTML in XSS hacks
	XSSEncode : function(s,en){
		if(!this.isEmpty(s)){
			en = en || true;
			// do we convert to numerical or html entity?
			if(en){
				s = s.replace(/\'/g,"&#39;"); //no HTML equivalent as &apos is not cross browser supported
				s = s.replace(/\"/g,"&quot;");
				s = s.replace(/</g,"&lt;");
				s = s.replace(/>/g,"&gt;");
			}else{
				s = s.replace(/\'/g,"&#39;"); //no HTML equivalent as &apos is not cross browser supported
				s = s.replace(/\"/g,"&#34;");
				s = s.replace(/</g,"&#60;");
				s = s.replace(/>/g,"&#62;");
			}
			return s;
		}else{
			return "";
		}
	},

	// returns true if a string contains html or numerical encoded entities
	hasEncoded : function(s){
		if(/&#[0-9]{1,5};/g.test(s)){
			return true;
		}else if(/&[A-Z]{2,6};/gi.test(s)){
			return true;
		}else{
			return false;
		}
	},

	// will remove any unicode characters
	stripUnicode : function(s){
		return s.replace(/[^\x20-\x7E]/g,"");
		
	},

	// corrects any double encoded &amp; entities e.g &amp;amp;
	correctEncoding : function(s){
		return s.replace(/(&amp;)(amp;)+/,"$1");
	},


	// Function to loop through an array swaping each item with the value from another array e.g swap HTML entities with Numericals
	swapArrayVals : function(s,arr1,arr2){
		if(this.isEmpty(s)) return "";
		var re;
		if(arr1 && arr2){
			//ShowDebug("in swapArrayVals arr1.length = " + arr1.length + " arr2.length = " + arr2.length)
			// array lengths must match
			if(arr1.length == arr2.length){
				for(var x=0,i=arr1.length;x<i;x++){
					re = new RegExp(arr1[x], 'g');
					s = s.replace(re,arr2[x]); //swap arr1 item with matching item from arr2	
				}
			}
		}
		return s;
	},

	inArray : function( item, arr ) {
		for ( var i = 0, x = arr.length; i < x; i++ ){
			if ( arr[i] === item ){
				return i;
			}
		}
		return -1;
	}

};

/*!
 * jQuery UI 1.8.5
 *
 * Copyright 2010, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI
 */
(function(c,j){function k(a){return!c(a).parents().andSelf().filter(function(){return c.curCSS(this,"visibility")==="hidden"||c.expr.filters.hidden(this)}).length}c.ui=c.ui||{};if(!c.ui.version){c.extend(c.ui,{version:"1.8.5",keyCode:{ALT:18,BACKSPACE:8,CAPS_LOCK:20,COMMA:188,COMMAND:91,COMMAND_LEFT:91,COMMAND_RIGHT:93,CONTROL:17,DELETE:46,DOWN:40,END:35,ENTER:13,ESCAPE:27,HOME:36,INSERT:45,LEFT:37,MENU:93,NUMPAD_ADD:107,NUMPAD_DECIMAL:110,NUMPAD_DIVIDE:111,NUMPAD_ENTER:108,NUMPAD_MULTIPLY:106,
NUMPAD_SUBTRACT:109,PAGE_DOWN:34,PAGE_UP:33,PERIOD:190,RIGHT:39,SHIFT:16,SPACE:32,TAB:9,UP:38,WINDOWS:91}});c.fn.extend({_focus:c.fn.focus,focus:function(a,b){return typeof a==="number"?this.each(function(){var d=this;setTimeout(function(){c(d).focus();b&&b.call(d)},a)}):this._focus.apply(this,arguments)},scrollParent:function(){var a;a=c.browser.msie&&/(static|relative)/.test(this.css("position"))||/absolute/.test(this.css("position"))?this.parents().filter(function(){return/(relative|absolute|fixed)/.test(c.curCSS(this,
"position",1))&&/(auto|scroll)/.test(c.curCSS(this,"overflow",1)+c.curCSS(this,"overflow-y",1)+c.curCSS(this,"overflow-x",1))}).eq(0):this.parents().filter(function(){return/(auto|scroll)/.test(c.curCSS(this,"overflow",1)+c.curCSS(this,"overflow-y",1)+c.curCSS(this,"overflow-x",1))}).eq(0);return/fixed/.test(this.css("position"))||!a.length?c(document):a},zIndex:function(a){if(a!==j)return this.css("zIndex",a);if(this.length){a=c(this[0]);for(var b;a.length&&a[0]!==document;){b=a.css("position");
if(b==="absolute"||b==="relative"||b==="fixed"){b=parseInt(a.css("zIndex"));if(!isNaN(b)&&b!=0)return b}a=a.parent()}}return 0},disableSelection:function(){return this.bind("mousedown.ui-disableSelection selectstart.ui-disableSelection",function(a){a.preventDefault()})},enableSelection:function(){return this.unbind(".ui-disableSelection")}});c.each(["Width","Height"],function(a,b){function d(f,g,l,m){c.each(e,function(){g-=parseFloat(c.curCSS(f,"padding"+this,true))||0;if(l)g-=parseFloat(c.curCSS(f,
"border"+this+"Width",true))||0;if(m)g-=parseFloat(c.curCSS(f,"margin"+this,true))||0});return g}var e=b==="Width"?["Left","Right"]:["Top","Bottom"],h=b.toLowerCase(),i={innerWidth:c.fn.innerWidth,innerHeight:c.fn.innerHeight,outerWidth:c.fn.outerWidth,outerHeight:c.fn.outerHeight};c.fn["inner"+b]=function(f){if(f===j)return i["inner"+b].call(this);return this.each(function(){c.style(this,h,d(this,f)+"px")})};c.fn["outer"+b]=function(f,g){if(typeof f!=="number")return i["outer"+b].call(this,f);return this.each(function(){c.style(this,
h,d(this,f,true,g)+"px")})}});c.extend(c.expr[":"],{data:function(a,b,d){return!!c.data(a,d[3])},focusable:function(a){var b=a.nodeName.toLowerCase(),d=c.attr(a,"tabindex");if("area"===b){b=a.parentNode;d=b.name;if(!a.href||!d||b.nodeName.toLowerCase()!=="map")return false;a=c("img[usemap=#"+d+"]")[0];return!!a&&k(a)}return(/input|select|textarea|button|object/.test(b)?!a.disabled:"a"==b?a.href||!isNaN(d):!isNaN(d))&&k(a)},tabbable:function(a){var b=c.attr(a,"tabindex");return(isNaN(b)||b>=0)&&c(a).is(":focusable")}});
c(function(){var a=document.createElement("div"),b=document.body;c.extend(a.style,{minHeight:"100px",height:"auto",padding:0,borderWidth:0});c.support.minHeight=b.appendChild(a).offsetHeight===100;b.removeChild(a).style.display="none"});c.extend(c.ui,{plugin:{add:function(a,b,d){a=c.ui[a].prototype;for(var e in d){a.plugins[e]=a.plugins[e]||[];a.plugins[e].push([b,d[e]])}},call:function(a,b,d){if((b=a.plugins[b])&&a.element[0].parentNode)for(var e=0;e<b.length;e++)a.options[b[e][0]]&&b[e][1].apply(a.element,
d)}},contains:function(a,b){return document.compareDocumentPosition?a.compareDocumentPosition(b)&16:a!==b&&a.contains(b)},hasScroll:function(a,b){if(c(a).css("overflow")==="hidden")return false;b=b&&b==="left"?"scrollLeft":"scrollTop";var d=false;if(a[b]>0)return true;a[b]=1;d=a[b]>0;a[b]=0;return d},isOverAxis:function(a,b,d){return a>b&&a<b+d},isOver:function(a,b,d,e,h,i){return c.ui.isOverAxis(a,d,h)&&c.ui.isOverAxis(b,e,i)}})}})(jQuery);
;/*
 * jQuery UI Datepicker 1.8.5
 *
 * Copyright 2010, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Datepicker
 *
 * Depends:
 *	jquery.ui.core.js
 */
(function(d,G){function L(){this.debug=false;this._curInst=null;this._keyEvent=false;this._disabledInputs=[];this._inDialog=this._datepickerShowing=false;this._mainDivId="ui-datepicker-div";this._inlineClass="ui-datepicker-inline";this._appendClass="ui-datepicker-append";this._triggerClass="ui-datepicker-trigger";this._dialogClass="ui-datepicker-dialog";this._disableClass="ui-datepicker-disabled";this._unselectableClass="ui-datepicker-unselectable";this._currentClass="ui-datepicker-current-day";this._dayOverClass=
"ui-datepicker-days-cell-over";this.regional=[];this.regional[""]={closeText:"Done",prevText:"Prev",nextText:"Next",currentText:"Today",monthNames:["January","February","March","April","May","June","July","August","September","October","November","December"],monthNamesShort:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],dayNames:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],dayNamesShort:["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],dayNamesMin:["Su",
"Mo","Tu","We","Th","Fr","Sa"],weekHeader:"Wk",dateFormat:"mm/dd/yy",firstDay:0,isRTL:false,showMonthAfterYear:false,yearSuffix:""};this._defaults={showOn:"focus",showAnim:"fadeIn",showOptions:{},defaultDate:null,appendText:"",buttonText:"...",buttonImage:"",buttonImageOnly:false,hideIfNoPrevNext:false,navigationAsDateFormat:false,gotoCurrent:false,changeMonth:false,changeYear:false,yearRange:"c-10:c+10",showOtherMonths:false,selectOtherMonths:false,showWeek:false,calculateWeek:this.iso8601Week,shortYearCutoff:"+10",
minDate:null,maxDate:null,duration:"fast",beforeShowDay:null,beforeShow:null,onSelect:null,onChangeMonthYear:null,onClose:null,numberOfMonths:1,showCurrentAtPos:0,stepMonths:1,stepBigMonths:12,altField:"",altFormat:"",constrainInput:true,showButtonPanel:false,autoSize:false};d.extend(this._defaults,this.regional[""]);this.dpDiv=d('<div id="'+this._mainDivId+'" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div>')}function E(a,b){d.extend(a,
b);for(var c in b)if(b[c]==null||b[c]==G)a[c]=b[c];return a}d.extend(d.ui,{datepicker:{version:"1.8.5"}});var y=(new Date).getTime();d.extend(L.prototype,{markerClassName:"hasDatepicker",log:function(){this.debug&&console.log.apply("",arguments)},_widgetDatepicker:function(){return this.dpDiv},setDefaults:function(a){E(this._defaults,a||{});return this},_attachDatepicker:function(a,b){var c=null;for(var e in this._defaults){var f=a.getAttribute("date:"+e);if(f){c=c||{};try{c[e]=eval(f)}catch(h){c[e]=
f}}}e=a.nodeName.toLowerCase();f=e=="div"||e=="span";if(!a.id){this.uuid+=1;a.id="dp"+this.uuid}var i=this._newInst(d(a),f);i.settings=d.extend({},b||{},c||{});if(e=="input")this._connectDatepicker(a,i);else f&&this._inlineDatepicker(a,i)},_newInst:function(a,b){return{id:a[0].id.replace(/([^A-Za-z0-9_])/g,"\\\\$1"),input:a,selectedDay:0,selectedMonth:0,selectedYear:0,drawMonth:0,drawYear:0,inline:b,dpDiv:!b?this.dpDiv:d('<div class="'+this._inlineClass+' ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all"></div>')}},
_connectDatepicker:function(a,b){var c=d(a);b.append=d([]);b.trigger=d([]);if(!c.hasClass(this.markerClassName)){this._attachments(c,b);c.addClass(this.markerClassName).keydown(this._doKeyDown).keypress(this._doKeyPress).keyup(this._doKeyUp).bind("setData.datepicker",function(e,f,h){b.settings[f]=h}).bind("getData.datepicker",function(e,f){return this._get(b,f)});this._autoSize(b);d.data(a,"datepicker",b)}},_attachments:function(a,b){var c=this._get(b,"appendText"),e=this._get(b,"isRTL");b.append&&
b.append.remove();if(c){b.append=d('<span class="'+this._appendClass+'">'+c+"</span>");a[e?"before":"after"](b.append)}a.unbind("focus",this._showDatepicker);b.trigger&&b.trigger.remove();c=this._get(b,"showOn");if(c=="focus"||c=="both")a.focus(this._showDatepicker);if(c=="button"||c=="both"){c=this._get(b,"buttonText");var f=this._get(b,"buttonImage");b.trigger=d(this._get(b,"buttonImageOnly")?d("<img/>").addClass(this._triggerClass).attr({src:f,alt:c,title:c}):d('<button type="button"></button>').addClass(this._triggerClass).html(f==
""?c:d("<img/>").attr({src:f,alt:c,title:c})));a[e?"before":"after"](b.trigger);b.trigger.click(function(){d.datepicker._datepickerShowing&&d.datepicker._lastInput==a[0]?d.datepicker._hideDatepicker():d.datepicker._showDatepicker(a[0]);return false})}},_autoSize:function(a){if(this._get(a,"autoSize")&&!a.inline){var b=new Date(2009,11,20),c=this._get(a,"dateFormat");if(c.match(/[DM]/)){var e=function(f){for(var h=0,i=0,g=0;g<f.length;g++)if(f[g].length>h){h=f[g].length;i=g}return i};b.setMonth(e(this._get(a,
c.match(/MM/)?"monthNames":"monthNamesShort")));b.setDate(e(this._get(a,c.match(/DD/)?"dayNames":"dayNamesShort"))+20-b.getDay())}a.input.attr("size",this._formatDate(a,b).length)}},_inlineDatepicker:function(a,b){var c=d(a);if(!c.hasClass(this.markerClassName)){c.addClass(this.markerClassName).append(b.dpDiv).bind("setData.datepicker",function(e,f,h){b.settings[f]=h}).bind("getData.datepicker",function(e,f){return this._get(b,f)});d.data(a,"datepicker",b);this._setDate(b,this._getDefaultDate(b),
true);this._updateDatepicker(b);this._updateAlternate(b)}},_dialogDatepicker:function(a,b,c,e,f){a=this._dialogInst;if(!a){this.uuid+=1;this._dialogInput=d('<input type="text" id="'+("dp"+this.uuid)+'" style="position: absolute; top: -100px; width: 0px; z-index: -10;"/>');this._dialogInput.keydown(this._doKeyDown);d("body").append(this._dialogInput);a=this._dialogInst=this._newInst(this._dialogInput,false);a.settings={};d.data(this._dialogInput[0],"datepicker",a)}E(a.settings,e||{});b=b&&b.constructor==
Date?this._formatDate(a,b):b;this._dialogInput.val(b);this._pos=f?f.length?f:[f.pageX,f.pageY]:null;if(!this._pos)this._pos=[document.documentElement.clientWidth/2-100+(document.documentElement.scrollLeft||document.body.scrollLeft),document.documentElement.clientHeight/2-150+(document.documentElement.scrollTop||document.body.scrollTop)];this._dialogInput.css("left",this._pos[0]+20+"px").css("top",this._pos[1]+"px");a.settings.onSelect=c;this._inDialog=true;this.dpDiv.addClass(this._dialogClass);this._showDatepicker(this._dialogInput[0]);
d.blockUI&&d.blockUI(this.dpDiv);d.data(this._dialogInput[0],"datepicker",a);return this},_destroyDatepicker:function(a){var b=d(a),c=d.data(a,"datepicker");if(b.hasClass(this.markerClassName)){var e=a.nodeName.toLowerCase();d.removeData(a,"datepicker");if(e=="input"){c.append.remove();c.trigger.remove();b.removeClass(this.markerClassName).unbind("focus",this._showDatepicker).unbind("keydown",this._doKeyDown).unbind("keypress",this._doKeyPress).unbind("keyup",this._doKeyUp)}else if(e=="div"||e=="span")b.removeClass(this.markerClassName).empty()}},
_enableDatepicker:function(a){var b=d(a),c=d.data(a,"datepicker");if(b.hasClass(this.markerClassName)){var e=a.nodeName.toLowerCase();if(e=="input"){a.disabled=false;c.trigger.filter("button").each(function(){this.disabled=false}).end().filter("img").css({opacity:"1.0",cursor:""})}else if(e=="div"||e=="span")b.children("."+this._inlineClass).children().removeClass("ui-state-disabled");this._disabledInputs=d.map(this._disabledInputs,function(f){return f==a?null:f})}},_disableDatepicker:function(a){var b=
d(a),c=d.data(a,"datepicker");if(b.hasClass(this.markerClassName)){var e=a.nodeName.toLowerCase();if(e=="input"){a.disabled=true;c.trigger.filter("button").each(function(){this.disabled=true}).end().filter("img").css({opacity:"0.5",cursor:"default"})}else if(e=="div"||e=="span")b.children("."+this._inlineClass).children().addClass("ui-state-disabled");this._disabledInputs=d.map(this._disabledInputs,function(f){return f==a?null:f});this._disabledInputs[this._disabledInputs.length]=a}},_isDisabledDatepicker:function(a){if(!a)return false;
for(var b=0;b<this._disabledInputs.length;b++)if(this._disabledInputs[b]==a)return true;return false},_getInst:function(a){try{return d.data(a,"datepicker")}catch(b){throw"Missing instance data for this datepicker";}},_optionDatepicker:function(a,b,c){var e=this._getInst(a);if(arguments.length==2&&typeof b=="string")return b=="defaults"?d.extend({},d.datepicker._defaults):e?b=="all"?d.extend({},e.settings):this._get(e,b):null;var f=b||{};if(typeof b=="string"){f={};f[b]=c}if(e){this._curInst==e&&
this._hideDatepicker();var h=this._getDateDatepicker(a,true);E(e.settings,f);this._attachments(d(a),e);this._autoSize(e);this._setDateDatepicker(a,h);this._updateDatepicker(e)}},_changeDatepicker:function(a,b,c){this._optionDatepicker(a,b,c)},_refreshDatepicker:function(a){(a=this._getInst(a))&&this._updateDatepicker(a)},_setDateDatepicker:function(a,b){if(a=this._getInst(a)){this._setDate(a,b);this._updateDatepicker(a);this._updateAlternate(a)}},_getDateDatepicker:function(a,b){(a=this._getInst(a))&&
!a.inline&&this._setDateFromField(a,b);return a?this._getDate(a):null},_doKeyDown:function(a){var b=d.datepicker._getInst(a.target),c=true,e=b.dpDiv.is(".ui-datepicker-rtl");b._keyEvent=true;if(d.datepicker._datepickerShowing)switch(a.keyCode){case 9:d.datepicker._hideDatepicker();c=false;break;case 13:c=d("td."+d.datepicker._dayOverClass,b.dpDiv).add(d("td."+d.datepicker._currentClass,b.dpDiv));c[0]?d.datepicker._selectDay(a.target,b.selectedMonth,b.selectedYear,c[0]):d.datepicker._hideDatepicker();
return false;case 27:d.datepicker._hideDatepicker();break;case 33:d.datepicker._adjustDate(a.target,a.ctrlKey?-d.datepicker._get(b,"stepBigMonths"):-d.datepicker._get(b,"stepMonths"),"M");break;case 34:d.datepicker._adjustDate(a.target,a.ctrlKey?+d.datepicker._get(b,"stepBigMonths"):+d.datepicker._get(b,"stepMonths"),"M");break;case 35:if(a.ctrlKey||a.metaKey)d.datepicker._clearDate(a.target);c=a.ctrlKey||a.metaKey;break;case 36:if(a.ctrlKey||a.metaKey)d.datepicker._gotoToday(a.target);c=a.ctrlKey||
a.metaKey;break;case 37:if(a.ctrlKey||a.metaKey)d.datepicker._adjustDate(a.target,e?+1:-1,"D");c=a.ctrlKey||a.metaKey;if(a.originalEvent.altKey)d.datepicker._adjustDate(a.target,a.ctrlKey?-d.datepicker._get(b,"stepBigMonths"):-d.datepicker._get(b,"stepMonths"),"M");break;case 38:if(a.ctrlKey||a.metaKey)d.datepicker._adjustDate(a.target,-7,"D");c=a.ctrlKey||a.metaKey;break;case 39:if(a.ctrlKey||a.metaKey)d.datepicker._adjustDate(a.target,e?-1:+1,"D");c=a.ctrlKey||a.metaKey;if(a.originalEvent.altKey)d.datepicker._adjustDate(a.target,
a.ctrlKey?+d.datepicker._get(b,"stepBigMonths"):+d.datepicker._get(b,"stepMonths"),"M");break;case 40:if(a.ctrlKey||a.metaKey)d.datepicker._adjustDate(a.target,+7,"D");c=a.ctrlKey||a.metaKey;break;default:c=false}else if(a.keyCode==36&&a.ctrlKey)d.datepicker._showDatepicker(this);else c=false;if(c){a.preventDefault();a.stopPropagation()}},_doKeyPress:function(a){var b=d.datepicker._getInst(a.target);if(d.datepicker._get(b,"constrainInput")){b=d.datepicker._possibleChars(d.datepicker._get(b,"dateFormat"));
var c=String.fromCharCode(a.charCode==G?a.keyCode:a.charCode);return a.ctrlKey||c<" "||!b||b.indexOf(c)>-1}},_doKeyUp:function(a){a=d.datepicker._getInst(a.target);if(a.input.val()!=a.lastVal)try{if(d.datepicker.parseDate(d.datepicker._get(a,"dateFormat"),a.input?a.input.val():null,d.datepicker._getFormatConfig(a))){d.datepicker._setDateFromField(a);d.datepicker._updateAlternate(a);d.datepicker._updateDatepicker(a)}}catch(b){d.datepicker.log(b)}return true},_showDatepicker:function(a){a=a.target||
a;if(a.nodeName.toLowerCase()!="input")a=d("input",a.parentNode)[0];if(!(d.datepicker._isDisabledDatepicker(a)||d.datepicker._lastInput==a)){var b=d.datepicker._getInst(a);d.datepicker._curInst&&d.datepicker._curInst!=b&&d.datepicker._curInst.dpDiv.stop(true,true);var c=d.datepicker._get(b,"beforeShow");E(b.settings,c?c.apply(a,[a,b]):{});b.lastVal=null;d.datepicker._lastInput=a;d.datepicker._setDateFromField(b);if(d.datepicker._inDialog)a.value="";if(!d.datepicker._pos){d.datepicker._pos=d.datepicker._findPos(a);
d.datepicker._pos[1]+=a.offsetHeight}var e=false;d(a).parents().each(function(){e|=d(this).css("position")=="fixed";return!e});if(e&&d.browser.opera){d.datepicker._pos[0]-=document.documentElement.scrollLeft;d.datepicker._pos[1]-=document.documentElement.scrollTop}c={left:d.datepicker._pos[0],top:d.datepicker._pos[1]};d.datepicker._pos=null;b.dpDiv.css({position:"absolute",display:"block",top:"-1000px"});d.datepicker._updateDatepicker(b);c=d.datepicker._checkOffset(b,c,e);b.dpDiv.css({position:d.datepicker._inDialog&&
d.blockUI?"static":e?"fixed":"absolute",display:"none",left:c.left+"px",top:c.top+"px"});if(!b.inline){c=d.datepicker._get(b,"showAnim");var f=d.datepicker._get(b,"duration"),h=function(){d.datepicker._datepickerShowing=true;var i=d.datepicker._getBorders(b.dpDiv);b.dpDiv.find("iframe.ui-datepicker-cover").css({left:-i[0],top:-i[1],width:b.dpDiv.outerWidth(),height:b.dpDiv.outerHeight()})};b.dpDiv.zIndex(d(a).zIndex()+1);d.effects&&d.effects[c]?b.dpDiv.show(c,d.datepicker._get(b,"showOptions"),f,
h):b.dpDiv[c||"show"](c?f:null,h);if(!c||!f)h();b.input.is(":visible")&&!b.input.is(":disabled")&&b.input.focus();d.datepicker._curInst=b}}},_updateDatepicker:function(a){var b=this,c=d.datepicker._getBorders(a.dpDiv);a.dpDiv.empty().append(this._generateHTML(a)).find("iframe.ui-datepicker-cover").css({left:-c[0],top:-c[1],width:a.dpDiv.outerWidth(),height:a.dpDiv.outerHeight()}).end().find("button, .ui-datepicker-prev, .ui-datepicker-next, .ui-datepicker-calendar td a").bind("mouseout",function(){d(this).removeClass("ui-state-hover");
this.className.indexOf("ui-datepicker-prev")!=-1&&d(this).removeClass("ui-datepicker-prev-hover");this.className.indexOf("ui-datepicker-next")!=-1&&d(this).removeClass("ui-datepicker-next-hover")}).bind("mouseover",function(){if(!b._isDisabledDatepicker(a.inline?a.dpDiv.parent()[0]:a.input[0])){d(this).parents(".ui-datepicker-calendar").find("a").removeClass("ui-state-hover");d(this).addClass("ui-state-hover");this.className.indexOf("ui-datepicker-prev")!=-1&&d(this).addClass("ui-datepicker-prev-hover");
this.className.indexOf("ui-datepicker-next")!=-1&&d(this).addClass("ui-datepicker-next-hover")}}).end().find("."+this._dayOverClass+" a").trigger("mouseover").end();c=this._getNumberOfMonths(a);var e=c[1];e>1?a.dpDiv.addClass("ui-datepicker-multi-"+e).css("width",17*e+"em"):a.dpDiv.removeClass("ui-datepicker-multi-2 ui-datepicker-multi-3 ui-datepicker-multi-4").width("");a.dpDiv[(c[0]!=1||c[1]!=1?"add":"remove")+"Class"]("ui-datepicker-multi");a.dpDiv[(this._get(a,"isRTL")?"add":"remove")+"Class"]("ui-datepicker-rtl");
a==d.datepicker._curInst&&d.datepicker._datepickerShowing&&a.input&&a.input.is(":visible")&&!a.input.is(":disabled")&&a.input.focus()},_getBorders:function(a){var b=function(c){return{thin:1,medium:2,thick:3}[c]||c};return[parseFloat(b(a.css("border-left-width"))),parseFloat(b(a.css("border-top-width")))]},_checkOffset:function(a,b,c){var e=a.dpDiv.outerWidth(),f=a.dpDiv.outerHeight(),h=a.input?a.input.outerWidth():0,i=a.input?a.input.outerHeight():0,g=document.documentElement.clientWidth+d(document).scrollLeft(),
k=document.documentElement.clientHeight+d(document).scrollTop();b.left-=this._get(a,"isRTL")?e-h:0;b.left-=c&&b.left==a.input.offset().left?d(document).scrollLeft():0;b.top-=c&&b.top==a.input.offset().top+i?d(document).scrollTop():0;b.left-=Math.min(b.left,b.left+e>g&&g>e?Math.abs(b.left+e-g):0);b.top-=Math.min(b.top,b.top+f>k&&k>f?Math.abs(f+i):0);return b},_findPos:function(a){for(var b=this._get(this._getInst(a),"isRTL");a&&(a.type=="hidden"||a.nodeType!=1);)a=a[b?"previousSibling":"nextSibling"];
a=d(a).offset();return[a.left,a.top]},_hideDatepicker:function(a){var b=this._curInst;if(!(!b||a&&b!=d.data(a,"datepicker")))if(this._datepickerShowing){a=this._get(b,"showAnim");var c=this._get(b,"duration"),e=function(){d.datepicker._tidyDialog(b);this._curInst=null};d.effects&&d.effects[a]?b.dpDiv.hide(a,d.datepicker._get(b,"showOptions"),c,e):b.dpDiv[a=="slideDown"?"slideUp":a=="fadeIn"?"fadeOut":"hide"](a?c:null,e);a||e();if(a=this._get(b,"onClose"))a.apply(b.input?b.input[0]:null,[b.input?b.input.val():
"",b]);this._datepickerShowing=false;this._lastInput=null;if(this._inDialog){this._dialogInput.css({position:"absolute",left:"0",top:"-100px"});if(d.blockUI){d.unblockUI();d("body").append(this.dpDiv)}}this._inDialog=false}},_tidyDialog:function(a){a.dpDiv.removeClass(this._dialogClass).unbind(".ui-datepicker-calendar")},_checkExternalClick:function(a){if(d.datepicker._curInst){a=d(a.target);a[0].id!=d.datepicker._mainDivId&&a.parents("#"+d.datepicker._mainDivId).length==0&&!a.hasClass(d.datepicker.markerClassName)&&
!a.hasClass(d.datepicker._triggerClass)&&d.datepicker._datepickerShowing&&!(d.datepicker._inDialog&&d.blockUI)&&d.datepicker._hideDatepicker()}},_adjustDate:function(a,b,c){a=d(a);var e=this._getInst(a[0]);if(!this._isDisabledDatepicker(a[0])){this._adjustInstDate(e,b+(c=="M"?this._get(e,"showCurrentAtPos"):0),c);this._updateDatepicker(e)}},_gotoToday:function(a){a=d(a);var b=this._getInst(a[0]);if(this._get(b,"gotoCurrent")&&b.currentDay){b.selectedDay=b.currentDay;b.drawMonth=b.selectedMonth=b.currentMonth;
b.drawYear=b.selectedYear=b.currentYear}else{var c=new Date;b.selectedDay=c.getDate();b.drawMonth=b.selectedMonth=c.getMonth();b.drawYear=b.selectedYear=c.getFullYear()}this._notifyChange(b);this._adjustDate(a)},_selectMonthYear:function(a,b,c){a=d(a);var e=this._getInst(a[0]);e._selectingMonthYear=false;e["selected"+(c=="M"?"Month":"Year")]=e["draw"+(c=="M"?"Month":"Year")]=parseInt(b.options[b.selectedIndex].value,10);this._notifyChange(e);this._adjustDate(a)},_clickMonthYear:function(a){var b=
this._getInst(d(a)[0]);b.input&&b._selectingMonthYear&&setTimeout(function(){b.input.focus()},0);b._selectingMonthYear=!b._selectingMonthYear},_selectDay:function(a,b,c,e){var f=d(a);if(!(d(e).hasClass(this._unselectableClass)||this._isDisabledDatepicker(f[0]))){f=this._getInst(f[0]);f.selectedDay=f.currentDay=d("a",e).html();f.selectedMonth=f.currentMonth=b;f.selectedYear=f.currentYear=c;this._selectDate(a,this._formatDate(f,f.currentDay,f.currentMonth,f.currentYear))}},_clearDate:function(a){a=
d(a);this._getInst(a[0]);this._selectDate(a,"")},_selectDate:function(a,b){a=this._getInst(d(a)[0]);b=b!=null?b:this._formatDate(a);a.input&&a.input.val(b);this._updateAlternate(a);var c=this._get(a,"onSelect");if(c)c.apply(a.input?a.input[0]:null,[b,a]);else a.input&&a.input.trigger("change");if(a.inline)this._updateDatepicker(a);else{this._hideDatepicker();this._lastInput=a.input[0];typeof a.input[0]!="object"&&a.input.focus();this._lastInput=null}},_updateAlternate:function(a){var b=this._get(a,
"altField");if(b){var c=this._get(a,"altFormat")||this._get(a,"dateFormat"),e=this._getDate(a),f=this.formatDate(c,e,this._getFormatConfig(a));d(b).each(function(){d(this).val(f)})}},noWeekends:function(a){a=a.getDay();return[a>0&&a<6,""]},iso8601Week:function(a){a=new Date(a.getTime());a.setDate(a.getDate()+4-(a.getDay()||7));var b=a.getTime();a.setMonth(0);a.setDate(1);return Math.floor(Math.round((b-a)/864E5)/7)+1},parseDate:function(a,b,c){if(a==null||b==null)throw"Invalid arguments";b=typeof b==
"object"?b.toString():b+"";if(b=="")return null;for(var e=(c?c.shortYearCutoff:null)||this._defaults.shortYearCutoff,f=(c?c.dayNamesShort:null)||this._defaults.dayNamesShort,h=(c?c.dayNames:null)||this._defaults.dayNames,i=(c?c.monthNamesShort:null)||this._defaults.monthNamesShort,g=(c?c.monthNames:null)||this._defaults.monthNames,k=c=-1,l=-1,u=-1,j=false,o=function(p){(p=z+1<a.length&&a.charAt(z+1)==p)&&z++;return p},m=function(p){o(p);p=new RegExp("^\\d{1,"+(p=="@"?14:p=="!"?20:p=="y"?4:p=="o"?
3:2)+"}");p=b.substring(s).match(p);if(!p)throw"Missing number at position "+s;s+=p[0].length;return parseInt(p[0],10)},n=function(p,w,H){p=o(p)?H:w;for(w=0;w<p.length;w++)if(b.substr(s,p[w].length).toLowerCase()==p[w].toLowerCase()){s+=p[w].length;return w+1}throw"Unknown name at position "+s;},r=function(){if(b.charAt(s)!=a.charAt(z))throw"Unexpected literal at position "+s;s++},s=0,z=0;z<a.length;z++)if(j)if(a.charAt(z)=="'"&&!o("'"))j=false;else r();else switch(a.charAt(z)){case "d":l=m("d");
break;case "D":n("D",f,h);break;case "o":u=m("o");break;case "m":k=m("m");break;case "M":k=n("M",i,g);break;case "y":c=m("y");break;case "@":var v=new Date(m("@"));c=v.getFullYear();k=v.getMonth()+1;l=v.getDate();break;case "!":v=new Date((m("!")-this._ticksTo1970)/1E4);c=v.getFullYear();k=v.getMonth()+1;l=v.getDate();break;case "'":if(o("'"))r();else j=true;break;default:r()}if(c==-1)c=(new Date).getFullYear();else if(c<100)c+=(new Date).getFullYear()-(new Date).getFullYear()%100+(c<=e?0:-100);if(u>
-1){k=1;l=u;do{e=this._getDaysInMonth(c,k-1);if(l<=e)break;k++;l-=e}while(1)}v=this._daylightSavingAdjust(new Date(c,k-1,l));if(v.getFullYear()!=c||v.getMonth()+1!=k||v.getDate()!=l)throw"Invalid date";return v},ATOM:"yy-mm-dd",COOKIE:"D, dd M yy",ISO_8601:"yy-mm-dd",RFC_822:"D, d M y",RFC_850:"DD, dd-M-y",RFC_1036:"D, d M y",RFC_1123:"D, d M yy",RFC_2822:"D, d M yy",RSS:"D, d M y",TICKS:"!",TIMESTAMP:"@",W3C:"yy-mm-dd",_ticksTo1970:(718685+Math.floor(492.5)-Math.floor(19.7)+Math.floor(4.925))*24*
60*60*1E7,formatDate:function(a,b,c){if(!b)return"";var e=(c?c.dayNamesShort:null)||this._defaults.dayNamesShort,f=(c?c.dayNames:null)||this._defaults.dayNames,h=(c?c.monthNamesShort:null)||this._defaults.monthNamesShort;c=(c?c.monthNames:null)||this._defaults.monthNames;var i=function(o){(o=j+1<a.length&&a.charAt(j+1)==o)&&j++;return o},g=function(o,m,n){m=""+m;if(i(o))for(;m.length<n;)m="0"+m;return m},k=function(o,m,n,r){return i(o)?r[m]:n[m]},l="",u=false;if(b)for(var j=0;j<a.length;j++)if(u)if(a.charAt(j)==
"'"&&!i("'"))u=false;else l+=a.charAt(j);else switch(a.charAt(j)){case "d":l+=g("d",b.getDate(),2);break;case "D":l+=k("D",b.getDay(),e,f);break;case "o":l+=g("o",(b.getTime()-(new Date(b.getFullYear(),0,0)).getTime())/864E5,3);break;case "m":l+=g("m",b.getMonth()+1,2);break;case "M":l+=k("M",b.getMonth(),h,c);break;case "y":l+=i("y")?b.getFullYear():(b.getYear()%100<10?"0":"")+b.getYear()%100;break;case "@":l+=b.getTime();break;case "!":l+=b.getTime()*1E4+this._ticksTo1970;break;case "'":if(i("'"))l+=
"'";else u=true;break;default:l+=a.charAt(j)}return l},_possibleChars:function(a){for(var b="",c=false,e=function(h){(h=f+1<a.length&&a.charAt(f+1)==h)&&f++;return h},f=0;f<a.length;f++)if(c)if(a.charAt(f)=="'"&&!e("'"))c=false;else b+=a.charAt(f);else switch(a.charAt(f)){case "d":case "m":case "y":case "@":b+="0123456789";break;case "D":case "M":return null;case "'":if(e("'"))b+="'";else c=true;break;default:b+=a.charAt(f)}return b},_get:function(a,b){return a.settings[b]!==G?a.settings[b]:this._defaults[b]},
_setDateFromField:function(a,b){if(a.input.val()!=a.lastVal){var c=this._get(a,"dateFormat"),e=a.lastVal=a.input?a.input.val():null,f,h;f=h=this._getDefaultDate(a);var i=this._getFormatConfig(a);try{f=this.parseDate(c,e,i)||h}catch(g){this.log(g);e=b?"":e}a.selectedDay=f.getDate();a.drawMonth=a.selectedMonth=f.getMonth();a.drawYear=a.selectedYear=f.getFullYear();a.currentDay=e?f.getDate():0;a.currentMonth=e?f.getMonth():0;a.currentYear=e?f.getFullYear():0;this._adjustInstDate(a)}},_getDefaultDate:function(a){return this._restrictMinMax(a,
this._determineDate(a,this._get(a,"defaultDate"),new Date))},_determineDate:function(a,b,c){var e=function(h){var i=new Date;i.setDate(i.getDate()+h);return i},f=function(h){try{return d.datepicker.parseDate(d.datepicker._get(a,"dateFormat"),h,d.datepicker._getFormatConfig(a))}catch(i){}var g=(h.toLowerCase().match(/^c/)?d.datepicker._getDate(a):null)||new Date,k=g.getFullYear(),l=g.getMonth();g=g.getDate();for(var u=/([+-]?[0-9]+)\s*(d|D|w|W|m|M|y|Y)?/g,j=u.exec(h);j;){switch(j[2]||"d"){case "d":case "D":g+=
parseInt(j[1],10);break;case "w":case "W":g+=parseInt(j[1],10)*7;break;case "m":case "M":l+=parseInt(j[1],10);g=Math.min(g,d.datepicker._getDaysInMonth(k,l));break;case "y":case "Y":k+=parseInt(j[1],10);g=Math.min(g,d.datepicker._getDaysInMonth(k,l));break}j=u.exec(h)}return new Date(k,l,g)};if(b=(b=b==null?c:typeof b=="string"?f(b):typeof b=="number"?isNaN(b)?c:e(b):b)&&b.toString()=="Invalid Date"?c:b){b.setHours(0);b.setMinutes(0);b.setSeconds(0);b.setMilliseconds(0)}return this._daylightSavingAdjust(b)},
_daylightSavingAdjust:function(a){if(!a)return null;a.setHours(a.getHours()>12?a.getHours()+2:0);return a},_setDate:function(a,b,c){var e=!b,f=a.selectedMonth,h=a.selectedYear;b=this._restrictMinMax(a,this._determineDate(a,b,new Date));a.selectedDay=a.currentDay=b.getDate();a.drawMonth=a.selectedMonth=a.currentMonth=b.getMonth();a.drawYear=a.selectedYear=a.currentYear=b.getFullYear();if((f!=a.selectedMonth||h!=a.selectedYear)&&!c)this._notifyChange(a);this._adjustInstDate(a);if(a.input)a.input.val(e?
"":this._formatDate(a))},_getDate:function(a){return!a.currentYear||a.input&&a.input.val()==""?null:this._daylightSavingAdjust(new Date(a.currentYear,a.currentMonth,a.currentDay))},_generateHTML:function(a){var b=new Date;b=this._daylightSavingAdjust(new Date(b.getFullYear(),b.getMonth(),b.getDate()));var c=this._get(a,"isRTL"),e=this._get(a,"showButtonPanel"),f=this._get(a,"hideIfNoPrevNext"),h=this._get(a,"navigationAsDateFormat"),i=this._getNumberOfMonths(a),g=this._get(a,"showCurrentAtPos"),k=
this._get(a,"stepMonths"),l=i[0]!=1||i[1]!=1,u=this._daylightSavingAdjust(!a.currentDay?new Date(9999,9,9):new Date(a.currentYear,a.currentMonth,a.currentDay)),j=this._getMinMaxDate(a,"min"),o=this._getMinMaxDate(a,"max");g=a.drawMonth-g;var m=a.drawYear;if(g<0){g+=12;m--}if(o){var n=this._daylightSavingAdjust(new Date(o.getFullYear(),o.getMonth()-i[0]*i[1]+1,o.getDate()));for(n=j&&n<j?j:n;this._daylightSavingAdjust(new Date(m,g,1))>n;){g--;if(g<0){g=11;m--}}}a.drawMonth=g;a.drawYear=m;n=this._get(a,
"prevText");n=!h?n:this.formatDate(n,this._daylightSavingAdjust(new Date(m,g-k,1)),this._getFormatConfig(a));n=this._canAdjustMonth(a,-1,m,g)?'<a class="ui-datepicker-prev ui-corner-all" onclick="DP_jQuery_'+y+".datepicker._adjustDate('#"+a.id+"', -"+k+", 'M');\" title=\""+n+'"><span class="ui-icon ui-icon-circle-triangle-'+(c?"e":"w")+'">'+n+"</span></a>":f?"":'<a class="ui-datepicker-prev ui-corner-all ui-state-disabled" title="'+n+'"><span class="ui-icon ui-icon-circle-triangle-'+(c?"e":"w")+'">'+
n+"</span></a>";var r=this._get(a,"nextText");r=!h?r:this.formatDate(r,this._daylightSavingAdjust(new Date(m,g+k,1)),this._getFormatConfig(a));f=this._canAdjustMonth(a,+1,m,g)?'<a class="ui-datepicker-next ui-corner-all" onclick="DP_jQuery_'+y+".datepicker._adjustDate('#"+a.id+"', +"+k+", 'M');\" title=\""+r+'"><span class="ui-icon ui-icon-circle-triangle-'+(c?"w":"e")+'">'+r+"</span></a>":f?"":'<a class="ui-datepicker-next ui-corner-all ui-state-disabled" title="'+r+'"><span class="ui-icon ui-icon-circle-triangle-'+
(c?"w":"e")+'">'+r+"</span></a>";k=this._get(a,"currentText");r=this._get(a,"gotoCurrent")&&a.currentDay?u:b;k=!h?k:this.formatDate(k,r,this._getFormatConfig(a));h=!a.inline?'<button type="button" class="ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all" onclick="DP_jQuery_'+y+'.datepicker._hideDatepicker();">'+this._get(a,"closeText")+"</button>":"";e=e?'<div class="ui-datepicker-buttonpane ui-widget-content">'+(c?h:"")+(this._isInRange(a,r)?'<button type="button" class="ui-datepicker-current ui-state-default ui-priority-secondary ui-corner-all" onclick="DP_jQuery_'+
y+".datepicker._gotoToday('#"+a.id+"');\">"+k+"</button>":"")+(c?"":h)+"</div>":"";h=parseInt(this._get(a,"firstDay"),10);h=isNaN(h)?0:h;k=this._get(a,"showWeek");r=this._get(a,"dayNames");this._get(a,"dayNamesShort");var s=this._get(a,"dayNamesMin"),z=this._get(a,"monthNames"),v=this._get(a,"monthNamesShort"),p=this._get(a,"beforeShowDay"),w=this._get(a,"showOtherMonths"),H=this._get(a,"selectOtherMonths");this._get(a,"calculateWeek");for(var M=this._getDefaultDate(a),I="",C=0;C<i[0];C++){for(var N=
"",D=0;D<i[1];D++){var J=this._daylightSavingAdjust(new Date(m,g,a.selectedDay)),t=" ui-corner-all",x="";if(l){x+='<div class="ui-datepicker-group';if(i[1]>1)switch(D){case 0:x+=" ui-datepicker-group-first";t=" ui-corner-"+(c?"right":"left");break;case i[1]-1:x+=" ui-datepicker-group-last";t=" ui-corner-"+(c?"left":"right");break;default:x+=" ui-datepicker-group-middle";t="";break}x+='">'}x+='<div class="ui-datepicker-header ui-widget-header ui-helper-clearfix'+t+'">'+(/all|left/.test(t)&&C==0?c?
f:n:"")+(/all|right/.test(t)&&C==0?c?n:f:"")+this._generateMonthYearHeader(a,g,m,j,o,C>0||D>0,z,v)+'</div><table class="ui-datepicker-calendar"><thead><tr>';var A=k?'<th class="ui-datepicker-week-col">'+this._get(a,"weekHeader")+"</th>":"";for(t=0;t<7;t++){var q=(t+h)%7;A+="<th"+((t+h+6)%7>=5?' class="ui-datepicker-week-end"':"")+'><span title="'+r[q]+'">'+s[q]+"</span></th>"}x+=A+"</tr></thead><tbody>";A=this._getDaysInMonth(m,g);if(m==a.selectedYear&&g==a.selectedMonth)a.selectedDay=Math.min(a.selectedDay,
A);t=(this._getFirstDayOfMonth(m,g)-h+7)%7;A=l?6:Math.ceil((t+A)/7);q=this._daylightSavingAdjust(new Date(m,g,1-t));for(var O=0;O<A;O++){x+="<tr>";var P=!k?"":'<td class="ui-datepicker-week-col">'+this._get(a,"calculateWeek")(q)+"</td>";for(t=0;t<7;t++){var F=p?p.apply(a.input?a.input[0]:null,[q]):[true,""],B=q.getMonth()!=g,K=B&&!H||!F[0]||j&&q<j||o&&q>o;P+='<td class="'+((t+h+6)%7>=5?" ui-datepicker-week-end":"")+(B?" ui-datepicker-other-month":"")+(q.getTime()==J.getTime()&&g==a.selectedMonth&&
a._keyEvent||M.getTime()==q.getTime()&&M.getTime()==J.getTime()?" "+this._dayOverClass:"")+(K?" "+this._unselectableClass+" ui-state-disabled":"")+(B&&!w?"":" "+F[1]+(q.getTime()==u.getTime()?" "+this._currentClass:"")+(q.getTime()==b.getTime()?" ui-datepicker-today":""))+'"'+((!B||w)&&F[2]?' title="'+F[2]+'"':"")+(K?"":' onclick="DP_jQuery_'+y+".datepicker._selectDay('#"+a.id+"',"+q.getMonth()+","+q.getFullYear()+', this);return false;"')+">"+(B&&!w?"&#xa0;":K?'<span class="ui-state-default">'+q.getDate()+
"</span>":'<a class="ui-state-default'+(q.getTime()==b.getTime()?" ui-state-highlight":"")+(q.getTime()==J.getTime()?" ui-state-active":"")+(B?" ui-priority-secondary":"")+'" href="#">'+q.getDate()+"</a>")+"</td>";q.setDate(q.getDate()+1);q=this._daylightSavingAdjust(q)}x+=P+"</tr>"}g++;if(g>11){g=0;m++}x+="</tbody></table>"+(l?"</div>"+(i[0]>0&&D==i[1]-1?'<div class="ui-datepicker-row-break"></div>':""):"");N+=x}I+=N}I+=e+(d.browser.msie&&parseInt(d.browser.version,10)<7&&!a.inline?'<iframe src="javascript:false;" class="ui-datepicker-cover" frameborder="0"></iframe>':
"");a._keyEvent=false;return I},_generateMonthYearHeader:function(a,b,c,e,f,h,i,g){var k=this._get(a,"changeMonth"),l=this._get(a,"changeYear"),u=this._get(a,"showMonthAfterYear"),j='<div class="ui-datepicker-title">',o="";if(h||!k)o+='<span class="ui-datepicker-month">'+i[b]+"</span>";else{i=e&&e.getFullYear()==c;var m=f&&f.getFullYear()==c;o+='<select class="ui-datepicker-month" onchange="DP_jQuery_'+y+".datepicker._selectMonthYear('#"+a.id+"', this, 'M');\" onclick=\"DP_jQuery_"+y+".datepicker._clickMonthYear('#"+
a.id+"');\">";for(var n=0;n<12;n++)if((!i||n>=e.getMonth())&&(!m||n<=f.getMonth()))o+='<option value="'+n+'"'+(n==b?' selected="selected"':"")+">"+g[n]+"</option>";o+="</select>"}u||(j+=o+(h||!(k&&l)?"&#xa0;":""));if(h||!l)j+='<span class="ui-datepicker-year">'+c+"</span>";else{g=this._get(a,"yearRange").split(":");var r=(new Date).getFullYear();i=function(s){s=s.match(/c[+-].*/)?c+parseInt(s.substring(1),10):s.match(/[+-].*/)?r+parseInt(s,10):parseInt(s,10);return isNaN(s)?r:s};b=i(g[0]);g=Math.max(b,
i(g[1]||""));b=e?Math.max(b,e.getFullYear()):b;g=f?Math.min(g,f.getFullYear()):g;for(j+='<select class="ui-datepicker-year" onchange="DP_jQuery_'+y+".datepicker._selectMonthYear('#"+a.id+"', this, 'Y');\" onclick=\"DP_jQuery_"+y+".datepicker._clickMonthYear('#"+a.id+"');\">";b<=g;b++)j+='<option value="'+b+'"'+(b==c?' selected="selected"':"")+">"+b+"</option>";j+="</select>"}j+=this._get(a,"yearSuffix");if(u)j+=(h||!(k&&l)?"&#xa0;":"")+o;j+="</div>";return j},_adjustInstDate:function(a,b,c){var e=
a.drawYear+(c=="Y"?b:0),f=a.drawMonth+(c=="M"?b:0);b=Math.min(a.selectedDay,this._getDaysInMonth(e,f))+(c=="D"?b:0);e=this._restrictMinMax(a,this._daylightSavingAdjust(new Date(e,f,b)));a.selectedDay=e.getDate();a.drawMonth=a.selectedMonth=e.getMonth();a.drawYear=a.selectedYear=e.getFullYear();if(c=="M"||c=="Y")this._notifyChange(a)},_restrictMinMax:function(a,b){var c=this._getMinMaxDate(a,"min");a=this._getMinMaxDate(a,"max");b=c&&b<c?c:b;return b=a&&b>a?a:b},_notifyChange:function(a){var b=this._get(a,
"onChangeMonthYear");if(b)b.apply(a.input?a.input[0]:null,[a.selectedYear,a.selectedMonth+1,a])},_getNumberOfMonths:function(a){a=this._get(a,"numberOfMonths");return a==null?[1,1]:typeof a=="number"?[1,a]:a},_getMinMaxDate:function(a,b){return this._determineDate(a,this._get(a,b+"Date"),null)},_getDaysInMonth:function(a,b){return 32-(new Date(a,b,32)).getDate()},_getFirstDayOfMonth:function(a,b){return(new Date(a,b,1)).getDay()},_canAdjustMonth:function(a,b,c,e){var f=this._getNumberOfMonths(a);
c=this._daylightSavingAdjust(new Date(c,e+(b<0?b:f[0]*f[1]),1));b<0&&c.setDate(this._getDaysInMonth(c.getFullYear(),c.getMonth()));return this._isInRange(a,c)},_isInRange:function(a,b){var c=this._getMinMaxDate(a,"min");a=this._getMinMaxDate(a,"max");return(!c||b.getTime()>=c.getTime())&&(!a||b.getTime()<=a.getTime())},_getFormatConfig:function(a){var b=this._get(a,"shortYearCutoff");b=typeof b!="string"?b:(new Date).getFullYear()%100+parseInt(b,10);return{shortYearCutoff:b,dayNamesShort:this._get(a,
"dayNamesShort"),dayNames:this._get(a,"dayNames"),monthNamesShort:this._get(a,"monthNamesShort"),monthNames:this._get(a,"monthNames")}},_formatDate:function(a,b,c,e){if(!b){a.currentDay=a.selectedDay;a.currentMonth=a.selectedMonth;a.currentYear=a.selectedYear}b=b?typeof b=="object"?b:this._daylightSavingAdjust(new Date(e,c,b)):this._daylightSavingAdjust(new Date(a.currentYear,a.currentMonth,a.currentDay));return this.formatDate(this._get(a,"dateFormat"),b,this._getFormatConfig(a))}});d.fn.datepicker=
function(a){if(!d.datepicker.initialized){d(document).mousedown(d.datepicker._checkExternalClick).find("body").append(d.datepicker.dpDiv);d.datepicker.initialized=true}var b=Array.prototype.slice.call(arguments,1);if(typeof a=="string"&&(a=="isDisabled"||a=="getDate"||a=="widget"))return d.datepicker["_"+a+"Datepicker"].apply(d.datepicker,[this[0]].concat(b));if(a=="option"&&arguments.length==2&&typeof arguments[1]=="string")return d.datepicker["_"+a+"Datepicker"].apply(d.datepicker,[this[0]].concat(b));
return this.each(function(){typeof a=="string"?d.datepicker["_"+a+"Datepicker"].apply(d.datepicker,[this].concat(b)):d.datepicker._attachDatepicker(this,a)})};d.datepicker=new L;d.datepicker.initialized=false;d.datepicker.uuid=(new Date).getTime();d.datepicker.version="1.8.5";window["DP_jQuery_"+y]=d})(jQuery);
;

﻿/* Afrikaans initialisation for the jQuery UI date picker plugin. */
/* Written by Renier Pretorius. */
jQuery(function($){
	$.datepicker.regional['af'] = {
		closeText: 'Selekteer',
		prevText: 'Vorige',
		nextText: 'Volgende',
		currentText: 'Vandag',
		monthNames: ['Januarie','Februarie','Maart','April','Mei','Junie',
		'Julie','Augustus','September','Oktober','November','Desember'],
		monthNamesShort: ['Jan', 'Feb', 'Mrt', 'Apr', 'Mei', 'Jun',
		'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Des'],
		dayNames: ['Sondag', 'Maandag', 'Dinsdag', 'Woensdag', 'Donderdag', 'Vrydag', 'Saterdag'],
		dayNamesShort: ['Son', 'Maa', 'Din', 'Woe', 'Don', 'Vry', 'Sat'],
		dayNamesMin: ['So','Ma','Di','Wo','Do','Vr','Sa'],
		weekHeader: 'Wk',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['af']);
});
﻿/* Arabic Translation for jQuery UI date picker plugin. */
/* Khaled Al Horani -- koko.dw@gmail.com */
/* خالد الحوراني -- koko.dw@gmail.com */
/* NOTE: monthNames are the original months names and they are the Arabic names, not the new months name فبراير - يناير and there isn't any Arabic roots for these months */
jQuery(function($){
	$.datepicker.regional['ar'] = {
		closeText: 'إغلاق',
		prevText: '&#x3c;السابق',
		nextText: 'التالي&#x3e;',
		currentText: 'اليوم',
		monthNames: ['كانون الثاني', 'شباط', 'آذار', 'نيسان', 'آذار', 'حزيران',
		'تموز', 'آب', 'أيلول',	'تشرين الأول', 'تشرين الثاني', 'كانون الأول'],
		monthNamesShort: ['1','2','3','4','5','6','7','8','9','10','11','12'],
		dayNames: ['السبت', 'الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة'],
		dayNamesShort: ['سبت', 'أحد', 'اثنين', 'ثلاثاء', 'أربعاء', 'خميس', 'جمعة'],
		dayNamesMin: ['سبت', 'أحد', 'اثنين', 'ثلاثاء', 'أربعاء', 'خميس', 'جمعة'],
		weekHeader: 'أسبوع',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
  		isRTL: true,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ar']);
});﻿/* Azerbaijani (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Jamil Najafov (necefov33@gmail.com). */
jQuery(function($) {
	$.datepicker.regional['az'] = {
		closeText: 'Bağla',
		prevText: '&#x3c;Geri',
		nextText: 'İrəli&#x3e;',
		currentText: 'Bugün',
		monthNames: ['Yanvar','Fevral','Mart','Aprel','May','İyun',
		'İyul','Avqust','Sentyabr','Oktyabr','Noyabr','Dekabr'],
		monthNamesShort: ['Yan','Fev','Mar','Apr','May','İyun',
		'İyul','Avq','Sen','Okt','Noy','Dek'],
		dayNames: ['Bazar','Bazar ertəsi','Çərşənbə axşamı','Çərşənbə','Cümə axşamı','Cümə','Şənbə'],
		dayNamesShort: ['B','Be','Ça','Ç','Ca','C','Ş'],
		dayNamesMin: ['B','B','Ç','С','Ç','C','Ş'],
		weekHeader: 'Hf',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['az']);
});﻿/* Bulgarian initialisation for the jQuery UI date picker plugin. */
/* Written by Stoyan Kyosev (http://svest.org). */
jQuery(function($){
    $.datepicker.regional['bg'] = {
        closeText: 'затвори',
        prevText: '&#x3c;назад',
        nextText: 'напред&#x3e;',
		nextBigText: '&#x3e;&#x3e;',
        currentText: 'днес',
        monthNames: ['Януари','Февруари','Март','Април','Май','Юни',
        'Юли','Август','Септември','Октомври','Ноември','Декември'],
        monthNamesShort: ['Яну','Фев','Мар','Апр','Май','Юни',
        'Юли','Авг','Сеп','Окт','Нов','Дек'],
        dayNames: ['Неделя','Понеделник','Вторник','Сряда','Четвъртък','Петък','Събота'],
        dayNamesShort: ['Нед','Пон','Вто','Сря','Чет','Пет','Съб'],
        dayNamesMin: ['Не','По','Вт','Ср','Че','Пе','Съ'],
		weekHeader: 'Wk',
        dateFormat: 'dd.mm.yy',
		firstDay: 1,
        isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
    $.datepicker.setDefaults($.datepicker.regional['bg']);
});
﻿/* Bosnian i18n for the jQuery UI date picker plugin. */
/* Written by Kenan Konjo. */
jQuery(function($){
	$.datepicker.regional['bs'] = {
		closeText: 'Zatvori', 
		prevText: '&#x3c;', 
		nextText: '&#x3e;', 
		currentText: 'Danas', 
		monthNames: ['Januar','Februar','Mart','April','Maj','Juni',
		'Juli','August','Septembar','Oktobar','Novembar','Decembar'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedelja','Ponedeljak','Utorak','Srijeda','Četvrtak','Petak','Subota'],
		dayNamesShort: ['Ned','Pon','Uto','Sri','Čet','Pet','Sub'],
		dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
		weekHeader: 'Wk',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['bs']);
});/* Inicialització en català per a l'extenció 'calendar' per jQuery. */
/* Writers: (joan.leon@gmail.com). */
jQuery(function($){
	$.datepicker.regional['ca'] = {
		closeText: 'Tancar',
		prevText: '&#x3c;Ant',
		nextText: 'Seg&#x3e;',
		currentText: 'Avui',
		monthNames: ['Gener','Febrer','Mar&ccedil;','Abril','Maig','Juny',
		'Juliol','Agost','Setembre','Octubre','Novembre','Desembre'],
		monthNamesShort: ['Gen','Feb','Mar','Abr','Mai','Jun',
		'Jul','Ago','Set','Oct','Nov','Des'],
		dayNames: ['Diumenge','Dilluns','Dimarts','Dimecres','Dijous','Divendres','Dissabte'],
		dayNamesShort: ['Dug','Dln','Dmt','Dmc','Djs','Dvn','Dsb'],
		dayNamesMin: ['Dg','Dl','Dt','Dc','Dj','Dv','Ds'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ca']);
});﻿/* Czech initialisation for the jQuery UI date picker plugin. */
/* Written by Tomas Muller (tomas@tomas-muller.net). */
jQuery(function($){
	$.datepicker.regional['cs'] = {
		closeText: 'Zavřít',
		prevText: '&#x3c;Dříve',
		nextText: 'Později&#x3e;',
		currentText: 'Nyní',
		monthNames: ['leden','únor','březen','duben','květen','červen',
        'červenec','srpen','září','říjen','listopad','prosinec'],
		monthNamesShort: ['led','úno','bře','dub','kvě','čer',
		'čvc','srp','zář','říj','lis','pro'],
		dayNames: ['neděle', 'pondělí', 'úterý', 'středa', 'čtvrtek', 'pátek', 'sobota'],
		dayNamesShort: ['ne', 'po', 'út', 'st', 'čt', 'pá', 'so'],
		dayNamesMin: ['ne','po','út','st','čt','pá','so'],
		weekHeader: 'Týd',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['cs']);
});
﻿/* Danish initialisation for the jQuery UI date picker plugin. */
/* Written by Jan Christensen ( deletestuff@gmail.com). */
jQuery(function($){
    $.datepicker.regional['da'] = {
		closeText: 'Luk',
        prevText: '&#x3c;Forrige',
		nextText: 'Næste&#x3e;',
		currentText: 'Idag',
        monthNames: ['Januar','Februar','Marts','April','Maj','Juni',
        'Juli','August','September','Oktober','November','December'],
        monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
        'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Søndag','Mandag','Tirsdag','Onsdag','Torsdag','Fredag','Lørdag'],
		dayNamesShort: ['Søn','Man','Tir','Ons','Tor','Fre','Lør'],
		dayNamesMin: ['Sø','Ma','Ti','On','To','Fr','Lø'],
		weekHeader: 'Uge',
        dateFormat: 'dd-mm-yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
    $.datepicker.setDefaults($.datepicker.regional['da']);
});
﻿/* German initialisation for the jQuery UI date picker plugin. */
/* Written by Milian Wolff (mail@milianw.de). */
jQuery(function($){
	$.datepicker.regional['de'] = {
		closeText: 'schließen',
		prevText: '&#x3c;zurück',
		nextText: 'Vor&#x3e;',
		currentText: 'heute',
		monthNames: ['Januar','Februar','März','April','Mai','Juni',
		'Juli','August','September','Oktober','November','Dezember'],
		monthNamesShort: ['Jan','Feb','Mär','Apr','Mai','Jun',
		'Jul','Aug','Sep','Okt','Nov','Dez'],
		dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
		dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		weekHeader: 'Wo',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['de']);
});
﻿/* Greek (el) initialisation for the jQuery UI date picker plugin. */
/* Written by Alex Cicovic (http://www.alexcicovic.com) */
jQuery(function($){
	$.datepicker.regional['el'] = {
		closeText: 'Κλείσιμο',
		prevText: 'Προηγούμενος',
		nextText: 'Επόμενος',
		currentText: 'Τρέχων Μήνας',
		monthNames: ['Ιανουάριος','Φεβρουάριος','Μάρτιος','Απρίλιος','Μάιος','Ιούνιος',
		'Ιούλιος','Αύγουστος','Σεπτέμβριος','Οκτώβριος','Νοέμβριος','Δεκέμβριος'],
		monthNamesShort: ['Ιαν','Φεβ','Μαρ','Απρ','Μαι','Ιουν',
		'Ιουλ','Αυγ','Σεπ','Οκτ','Νοε','Δεκ'],
		dayNames: ['Κυριακή','Δευτέρα','Τρίτη','Τετάρτη','Πέμπτη','Παρασκευή','Σάββατο'],
		dayNamesShort: ['Κυρ','Δευ','Τρι','Τετ','Πεμ','Παρ','Σαβ'],
		dayNamesMin: ['Κυ','Δε','Τρ','Τε','Πε','Πα','Σα'],
		weekHeader: 'Εβδ',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['el']);
});﻿/* English/UK initialisation for the jQuery UI date picker plugin. */
/* Written by Stuart. */
jQuery(function($){
	$.datepicker.regional['en-GB'] = {
		closeText: 'Done',
		prevText: 'Prev',
		nextText: 'Next',
		currentText: 'Today',
		monthNames: ['January','February','March','April','May','June',
		'July','August','September','October','November','December'],
		monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
		'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
		dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
		dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
		dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
		weekHeader: 'Wk',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['en-GB']);
});
﻿/* Esperanto initialisation for the jQuery UI date picker plugin. */
/* Written by Olivier M. (olivierweb@ifrance.com). */
jQuery(function($){
	$.datepicker.regional['eo'] = {
		closeText: 'Fermi',
		prevText: '&lt;Anta',
		nextText: 'Sekv&gt;',
		currentText: 'Nuna',
		monthNames: ['Januaro','Februaro','Marto','Aprilo','Majo','Junio',
		'Julio','Aŭgusto','Septembro','Oktobro','Novembro','Decembro'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Aŭg','Sep','Okt','Nov','Dec'],
		dayNames: ['Dimanĉo','Lundo','Mardo','Merkredo','Ĵaŭdo','Vendredo','Sabato'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Ĵaŭ','Ven','Sab'],
		dayNamesMin: ['Di','Lu','Ma','Me','Ĵa','Ve','Sa'],
		weekHeader: 'Sb',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['eo']);
});
/* Inicialización en español para la extensión 'UI date picker' para jQuery. */
/* Traducido por Vester (xvester@gmail.com). */
jQuery(function($){
	$.datepicker.regional['es'] = {
		closeText: 'Cerrar',
		prevText: '&#x3c;Ant',
		nextText: 'Sig&#x3e;',
		currentText: 'Hoy',
		monthNames: ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
		'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'],
		monthNamesShort: ['Ene','Feb','Mar','Abr','May','Jun',
		'Jul','Ago','Sep','Oct','Nov','Dic'],
		dayNames: ['Domingo','Lunes','Martes','Mi&eacute;rcoles','Jueves','Viernes','S&aacute;bado'],
		dayNamesShort: ['Dom','Lun','Mar','Mi&eacute;','Juv','Vie','S&aacute;b'],
		dayNamesMin: ['Do','Lu','Ma','Mi','Ju','Vi','S&aacute;'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['es']);
});﻿/* Estonian initialisation for the jQuery UI date picker plugin. */
/* Written by Mart Sõmermaa (mrts.pydev at gmail com). */
jQuery(function($){
	$.datepicker.regional['et'] = {
		closeText: 'Sulge',
		prevText: 'Eelnev',
		nextText: 'Järgnev',
		currentText: 'Täna',
		monthNames: ['Jaanuar','Veebruar','Märts','Aprill','Mai','Juuni',
		'Juuli','August','September','Oktoober','November','Detsember'],
		monthNamesShort: ['Jaan', 'Veebr', 'Märts', 'Apr', 'Mai', 'Juuni',
		'Juuli', 'Aug', 'Sept', 'Okt', 'Nov', 'Dets'],
		dayNames: ['Pühapäev', 'Esmaspäev', 'Teisipäev', 'Kolmapäev', 'Neljapäev', 'Reede', 'Laupäev'],
		dayNamesShort: ['Pühap', 'Esmasp', 'Teisip', 'Kolmap', 'Neljap', 'Reede', 'Laup'],
		dayNamesMin: ['P','E','T','K','N','R','L'],
		weekHeader: 'Sm',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['et']);
}); ﻿/* Euskarako oinarria 'UI date picker' jquery-ko extentsioarentzat */
/* Karrikas-ek itzulia (karrikas@karrikas.com) */
jQuery(function($){
	$.datepicker.regional['eu'] = {
		closeText: 'Egina',
		prevText: '&#x3c;Aur',
		nextText: 'Hur&#x3e;',
		currentText: 'Gaur',
		monthNames: ['Urtarrila','Otsaila','Martxoa','Apirila','Maiatza','Ekaina',
		'Uztaila','Abuztua','Iraila','Urria','Azaroa','Abendua'],
		monthNamesShort: ['Urt','Ots','Mar','Api','Mai','Eka',
		'Uzt','Abu','Ira','Urr','Aza','Abe'],
		dayNames: ['Igandea','Astelehena','Asteartea','Asteazkena','Osteguna','Ostirala','Larunbata'],
		dayNamesShort: ['Iga','Ast','Ast','Ast','Ost','Ost','Lar'],
		dayNamesMin: ['Ig','As','As','As','Os','Os','La'],
		weekHeader: 'Wk',
		dateFormat: 'yy/mm/dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['eu']);
});﻿/* Persian (Farsi) Translation for the jQuery UI date picker plugin. */
/* Javad Mowlanezhad -- jmowla@gmail.com */
/* Jalali calendar should supported soon! (Its implemented but I have to test it) */
jQuery(function($) {
	$.datepicker.regional['fa'] = {
		closeText: 'بستن',
		prevText: '&#x3c;قبلي',
		nextText: 'بعدي&#x3e;',
		currentText: 'امروز',
		monthNames: ['فروردين','ارديبهشت','خرداد','تير','مرداد','شهريور',
		'مهر','آبان','آذر','دي','بهمن','اسفند'],
		monthNamesShort: ['1','2','3','4','5','6','7','8','9','10','11','12'],
		dayNames: ['يکشنبه','دوشنبه','سه‌شنبه','چهارشنبه','پنجشنبه','جمعه','شنبه'],
		dayNamesShort: ['ي','د','س','چ','پ','ج', 'ش'],
		dayNamesMin: ['ي','د','س','چ','پ','ج', 'ش'],
		weekHeader: 'هف',
		dateFormat: 'yy/mm/dd',
		firstDay: 6,
		isRTL: true,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['fa']);
});/* Finnish initialisation for the jQuery UI date picker plugin. */
/* Written by Harri Kilpi� (harrikilpio@gmail.com). */
jQuery(function($){
    $.datepicker.regional['fi'] = {
		closeText: 'Sulje',
		prevText: '&laquo;Edellinen',
		nextText: 'Seuraava&raquo;',
		currentText: 'T&auml;n&auml;&auml;n',
        monthNames: ['Tammikuu','Helmikuu','Maaliskuu','Huhtikuu','Toukokuu','Kes&auml;kuu',
        'Hein&auml;kuu','Elokuu','Syyskuu','Lokakuu','Marraskuu','Joulukuu'],
        monthNamesShort: ['Tammi','Helmi','Maalis','Huhti','Touko','Kes&auml;',
        'Hein&auml;','Elo','Syys','Loka','Marras','Joulu'],
		dayNamesShort: ['Su','Ma','Ti','Ke','To','Pe','Su'],
		dayNames: ['Sunnuntai','Maanantai','Tiistai','Keskiviikko','Torstai','Perjantai','Lauantai'],
		dayNamesMin: ['Su','Ma','Ti','Ke','To','Pe','La'],
		weekHeader: 'Vk',
        dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
    $.datepicker.setDefaults($.datepicker.regional['fi']);
});
﻿/* Faroese initialisation for the jQuery UI date picker plugin */
/* Written by Sverri Mohr Olsen, sverrimo@gmail.com */
jQuery(function($){
	$.datepicker.regional['fo'] = {
		closeText: 'Lat aftur',
		prevText: '&#x3c;Fyrra',
		nextText: 'Næsta&#x3e;',
		currentText: 'Í dag',
		monthNames: ['Januar','Februar','Mars','Apríl','Mei','Juni',
		'Juli','August','September','Oktober','November','Desember'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mei','Jun',
		'Jul','Aug','Sep','Okt','Nov','Des'],
		dayNames: ['Sunnudagur','Mánadagur','Týsdagur','Mikudagur','Hósdagur','Fríggjadagur','Leyardagur'],
		dayNamesShort: ['Sun','Mán','Týs','Mik','Hós','Frí','Ley'],
		dayNamesMin: ['Su','Má','Tý','Mi','Hó','Fr','Le'],
		weekHeader: 'Vk',
		dateFormat: 'dd-mm-yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['fo']);
});
﻿/* Swiss-French initialisation for the jQuery UI date picker plugin. */
/* Written Martin Voelkle (martin.voelkle@e-tc.ch). */
jQuery(function($){
	$.datepicker.regional['fr-CH'] = {
		closeText: 'Fermer',
		prevText: '&#x3c;Préc',
		nextText: 'Suiv&#x3e;',
		currentText: 'Courant',
		monthNames: ['Janvier','Février','Mars','Avril','Mai','Juin',
		'Juillet','Août','Septembre','Octobre','Novembre','Décembre'],
		monthNamesShort: ['Jan','Fév','Mar','Avr','Mai','Jun',
		'Jul','Aoû','Sep','Oct','Nov','Déc'],
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Jeu','Ven','Sam'],
		dayNamesMin: ['Di','Lu','Ma','Me','Je','Ve','Sa'],
		weekHeader: 'Sm',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['fr-CH']);
});﻿/* French initialisation for the jQuery UI date picker plugin. */
/* Written by Keith Wood (kbwood{at}iinet.com.au) and Stéphane Nahmani (sholby@sholby.net). */
jQuery(function($){
	$.datepicker.regional['fr'] = {
		closeText: 'Fermer',
		prevText: '&#x3c;Préc',
		nextText: 'Suiv&#x3e;',
		currentText: 'Courant',
		monthNames: ['Janvier','Février','Mars','Avril','Mai','Juin',
		'Juillet','Août','Septembre','Octobre','Novembre','Décembre'],
		monthNamesShort: ['Jan','Fév','Mar','Avr','Mai','Jun',
		'Jul','Aoû','Sep','Oct','Nov','Déc'],
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Jeu','Ven','Sam'],
		dayNamesMin: ['Di','Lu','Ma','Me','Je','Ve','Sa'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['fr']);
});﻿/* Hebrew initialisation for the UI Datepicker extension. */
/* Written by Amir Hardon (ahardon at gmail dot com). */
jQuery(function($){
	$.datepicker.regional['he'] = {
		closeText: 'סגור',
		prevText: '&#x3c;הקודם',
		nextText: 'הבא&#x3e;',
		currentText: 'היום',
		monthNames: ['ינואר','פברואר','מרץ','אפריל','מאי','יוני',
		'יולי','אוגוסט','ספטמבר','אוקטובר','נובמבר','דצמבר'],
		monthNamesShort: ['1','2','3','4','5','6',
		'7','8','9','10','11','12'],
		dayNames: ['ראשון','שני','שלישי','רביעי','חמישי','שישי','שבת'],
		dayNamesShort: ['א\'','ב\'','ג\'','ד\'','ה\'','ו\'','שבת'],
		dayNamesMin: ['א\'','ב\'','ג\'','ד\'','ה\'','ו\'','שבת'],
		weekHeader: 'Wk',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: true,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['he']);
});
﻿/* Croatian i18n for the jQuery UI date picker plugin. */
/* Written by Vjekoslav Nesek. */
jQuery(function($){
	$.datepicker.regional['hr'] = {
		closeText: 'Zatvori',
		prevText: '&#x3c;',
		nextText: '&#x3e;',
		currentText: 'Danas',
		monthNames: ['Siječanj','Veljača','Ožujak','Travanj','Svibanj','Lipanj',
		'Srpanj','Kolovoz','Rujan','Listopad','Studeni','Prosinac'],
		monthNamesShort: ['Sij','Velj','Ožu','Tra','Svi','Lip',
		'Srp','Kol','Ruj','Lis','Stu','Pro'],
		dayNames: ['Nedjelja','Ponedjeljak','Utorak','Srijeda','Četvrtak','Petak','Subota'],
		dayNamesShort: ['Ned','Pon','Uto','Sri','Čet','Pet','Sub'],
		dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
		weekHeader: 'Tje',
		dateFormat: 'dd.mm.yy.',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['hr']);
});/* Hungarian initialisation for the jQuery UI date picker plugin. */
/* Written by Istvan Karaszi (jquery@spam.raszi.hu). */
jQuery(function($){
	$.datepicker.regional['hu'] = {
		closeText: 'bezárás',
		prevText: '&laquo;&nbsp;vissza',
		nextText: 'előre&nbsp;&raquo;',
		currentText: 'ma',
		monthNames: ['Január', 'Február', 'Március', 'Április', 'Május', 'Június',
		'Július', 'Augusztus', 'Szeptember', 'Október', 'November', 'December'],
		monthNamesShort: ['Jan', 'Feb', 'Már', 'Ápr', 'Máj', 'Jún',
		'Júl', 'Aug', 'Szep', 'Okt', 'Nov', 'Dec'],
		dayNames: ['Vasárnap', 'Hétfö', 'Kedd', 'Szerda', 'Csütörtök', 'Péntek', 'Szombat'],
		dayNamesShort: ['Vas', 'Hét', 'Ked', 'Sze', 'Csü', 'Pén', 'Szo'],
		dayNamesMin: ['V', 'H', 'K', 'Sze', 'Cs', 'P', 'Szo'],
		weekHeader: 'Hé',
		dateFormat: 'yy-mm-dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['hu']);
});
/* Armenian(UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Levon Zakaryan (levon.zakaryan@gmail.com)*/
jQuery(function($){
	$.datepicker.regional['hy'] = {
		closeText: 'Փակել',
		prevText: '&#x3c;Նախ.',
		nextText: 'Հաջ.&#x3e;',
		currentText: 'Այսօր',
		monthNames: ['Հունվար','Փետրվար','Մարտ','Ապրիլ','Մայիս','Հունիս',
		'Հուլիս','Օգոստոս','Սեպտեմբեր','Հոկտեմբեր','Նոյեմբեր','Դեկտեմբեր'],
		monthNamesShort: ['Հունվ','Փետր','Մարտ','Ապր','Մայիս','Հունիս',
		'Հուլ','Օգս','Սեպ','Հոկ','Նոյ','Դեկ'],
		dayNames: ['կիրակի','եկուշաբթի','երեքշաբթի','չորեքշաբթի','հինգշաբթի','ուրբաթ','շաբաթ'],
		dayNamesShort: ['կիր','երկ','երք','չրք','հնգ','ուրբ','շբթ'],
		dayNamesMin: ['կիր','երկ','երք','չրք','հնգ','ուրբ','շբթ'],
		weekHeader: 'ՇԲՏ',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['hy']);
});/* Indonesian initialisation for the jQuery UI date picker plugin. */
/* Written by Deden Fathurahman (dedenf@gmail.com). */
jQuery(function($){
	$.datepicker.regional['id'] = {
		closeText: 'Tutup',
		prevText: '&#x3c;mundur',
		nextText: 'maju&#x3e;',
		currentText: 'hari ini',
		monthNames: ['Januari','Februari','Maret','April','Mei','Juni',
		'Juli','Agustus','September','Oktober','Nopember','Desember'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mei','Jun',
		'Jul','Agus','Sep','Okt','Nop','Des'],
		dayNames: ['Minggu','Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'],
		dayNamesShort: ['Min','Sen','Sel','Rab','kam','Jum','Sab'],
		dayNamesMin: ['Mg','Sn','Sl','Rb','Km','jm','Sb'],
		weekHeader: 'Mg',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['id']);
});/* Icelandic initialisation for the jQuery UI date picker plugin. */
/* Written by Haukur H. Thorsson (haukur@eskill.is). */
jQuery(function($){
	$.datepicker.regional['is'] = {
		closeText: 'Loka',
		prevText: '&#x3c; Fyrri',
		nextText: 'N&aelig;sti &#x3e;',
		currentText: '&Iacute; dag',
		monthNames: ['Jan&uacute;ar','Febr&uacute;ar','Mars','Apr&iacute;l','Ma&iacute','J&uacute;n&iacute;',
		'J&uacute;l&iacute;','&Aacute;g&uacute;st','September','Okt&oacute;ber','N&oacute;vember','Desember'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Ma&iacute;','J&uacute;n',
		'J&uacute;l','&Aacute;g&uacute;','Sep','Okt','N&oacute;v','Des'],
		dayNames: ['Sunnudagur','M&aacute;nudagur','&THORN;ri&eth;judagur','Mi&eth;vikudagur','Fimmtudagur','F&ouml;studagur','Laugardagur'],
		dayNamesShort: ['Sun','M&aacute;n','&THORN;ri','Mi&eth;','Fim','F&ouml;s','Lau'],
		dayNamesMin: ['Su','M&aacute;','&THORN;r','Mi','Fi','F&ouml;','La'],
		weekHeader: 'Vika',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['is']);
});/* Italian initialisation for the jQuery UI date picker plugin. */
/* Written by Antonello Pasella (antonello.pasella@gmail.com). */
jQuery(function($){
	$.datepicker.regional['it'] = {
		closeText: 'Chiudi',
		prevText: '&#x3c;Prec',
		nextText: 'Succ&#x3e;',
		currentText: 'Oggi',
		monthNames: ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
			'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'],
		monthNamesShort: ['Gen','Feb','Mar','Apr','Mag','Giu',
			'Lug','Ago','Set','Ott','Nov','Dic'],
		dayNames: ['Domenica','Luned&#236','Marted&#236','Mercoled&#236','Gioved&#236','Venerd&#236','Sabato'],
		dayNamesShort: ['Dom','Lun','Mar','Mer','Gio','Ven','Sab'],
		dayNamesMin: ['Do','Lu','Ma','Me','Gi','Ve','Sa'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['it']);
});
﻿/* Japanese initialisation for the jQuery UI date picker plugin. */
/* Written by Kentaro SATO (kentaro@ranvis.com). */
jQuery(function($){
	$.datepicker.regional['ja'] = {
		closeText: '閉じる',
		prevText: '&#x3c;前',
		nextText: '次&#x3e;',
		currentText: '今日',
		monthNames: ['1月','2月','3月','4月','5月','6月',
		'7月','8月','9月','10月','11月','12月'],
		monthNamesShort: ['1月','2月','3月','4月','5月','6月',
		'7月','8月','9月','10月','11月','12月'],
		dayNames: ['日曜日','月曜日','火曜日','水曜日','木曜日','金曜日','土曜日'],
		dayNamesShort: ['日','月','火','水','木','金','土'],
		dayNamesMin: ['日','月','火','水','木','金','土'],
		weekHeader: '週',
		dateFormat: 'yy/mm/dd',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: true,
		yearSuffix: '年'};
	$.datepicker.setDefaults($.datepicker.regional['ja']);
});/* Korean initialisation for the jQuery calendar extension. */
/* Written by DaeKwon Kang (ncrash.dk@gmail.com). */
jQuery(function($){
	$.datepicker.regional['ko'] = {
		closeText: '닫기',
		prevText: '이전달',
		nextText: '다음달',
		currentText: '오늘',
		monthNames: ['1월(JAN)','2월(FEB)','3월(MAR)','4월(APR)','5월(MAY)','6월(JUN)',
		'7월(JUL)','8월(AUG)','9월(SEP)','10월(OCT)','11월(NOV)','12월(DEC)'],
		monthNamesShort: ['1월(JAN)','2월(FEB)','3월(MAR)','4월(APR)','5월(MAY)','6월(JUN)',
		'7월(JUL)','8월(AUG)','9월(SEP)','10월(OCT)','11월(NOV)','12월(DEC)'],
		dayNames: ['일','월','화','수','목','금','토'],
		dayNamesShort: ['일','월','화','수','목','금','토'],
		dayNamesMin: ['일','월','화','수','목','금','토'],
		weekHeader: 'Wk',
		dateFormat: 'yy-mm-dd',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: '년'};
	$.datepicker.setDefaults($.datepicker.regional['ko']);
});/* Kazakh (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Dmitriy Karasyov (dmitriy.karasyov@gmail.com). */
jQuery(function($){
	$.datepicker.regional['kz'] = {
		closeText: 'Жабу',
		prevText: '&#x3c;Алдыңғы',
		nextText: 'Келесі&#x3e;',
		currentText: 'Бүгін',
		monthNames: ['Қаңтар','Ақпан','Наурыз','Сәуір','Мамыр','Маусым',
		'Шілде','Тамыз','Қыркүйек','Қазан','Қараша','Желтоқсан'],
		monthNamesShort: ['Қаң','Ақп','Нау','Сәу','Мам','Мау',
		'Шіл','Там','Қыр','Қаз','Қар','Жел'],
		dayNames: ['Жексенбі','Дүйсенбі','Сейсенбі','Сәрсенбі','Бейсенбі','Жұма','Сенбі'],
		dayNamesShort: ['жкс','дсн','ссн','срс','бсн','жма','снб'],
		dayNamesMin: ['Жк','Дс','Сс','Ср','Бс','Жм','Сн'],
		weekHeader: 'Не',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['kz']);
});
/* Lithuanian (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* @author Arturas Paleicikas <arturas@avalon.lt> */
jQuery(function($){
	$.datepicker.regional['lt'] = {
		closeText: 'Uždaryti',
		prevText: '&#x3c;Atgal',
		nextText: 'Pirmyn&#x3e;',
		currentText: 'Šiandien',
		monthNames: ['Sausis','Vasaris','Kovas','Balandis','Gegužė','Birželis',
		'Liepa','Rugpjūtis','Rugsėjis','Spalis','Lapkritis','Gruodis'],
		monthNamesShort: ['Sau','Vas','Kov','Bal','Geg','Bir',
		'Lie','Rugp','Rugs','Spa','Lap','Gru'],
		dayNames: ['sekmadienis','pirmadienis','antradienis','trečiadienis','ketvirtadienis','penktadienis','šeštadienis'],
		dayNamesShort: ['sek','pir','ant','tre','ket','pen','šeš'],
		dayNamesMin: ['Se','Pr','An','Tr','Ke','Pe','Še'],
		weekHeader: 'Wk',
		dateFormat: 'yy-mm-dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['lt']);
});/* Latvian (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* @author Arturas Paleicikas <arturas.paleicikas@metasite.net> */
jQuery(function($){
	$.datepicker.regional['lv'] = {
		closeText: 'Aizvērt',
		prevText: 'Iepr',
		nextText: 'Nāka',
		currentText: 'Šodien',
		monthNames: ['Janvāris','Februāris','Marts','Aprīlis','Maijs','Jūnijs',
		'Jūlijs','Augusts','Septembris','Oktobris','Novembris','Decembris'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','Jūn',
		'Jūl','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['svētdiena','pirmdiena','otrdiena','trešdiena','ceturtdiena','piektdiena','sestdiena'],
		dayNamesShort: ['svt','prm','otr','tre','ctr','pkt','sst'],
		dayNamesMin: ['Sv','Pr','Ot','Tr','Ct','Pk','Ss'],
		weekHeader: 'Nav',
		dateFormat: 'dd-mm-yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['lv']);
});/* Malaysian initialisation for the jQuery UI date picker plugin. */
/* Written by Mohd Nawawi Mohamad Jamili (nawawi@ronggeng.net). */
jQuery(function($){
	$.datepicker.regional['ms'] = {
		closeText: 'Tutup',
		prevText: '&#x3c;Sebelum',
		nextText: 'Selepas&#x3e;',
		currentText: 'hari ini',
		monthNames: ['Januari','Februari','Mac','April','Mei','Jun',
		'Julai','Ogos','September','Oktober','November','Disember'],
		monthNamesShort: ['Jan','Feb','Mac','Apr','Mei','Jun',
		'Jul','Ogo','Sep','Okt','Nov','Dis'],
		dayNames: ['Ahad','Isnin','Selasa','Rabu','Khamis','Jumaat','Sabtu'],
		dayNamesShort: ['Aha','Isn','Sel','Rab','kha','Jum','Sab'],
		dayNamesMin: ['Ah','Is','Se','Ra','Kh','Ju','Sa'],
		weekHeader: 'Mg',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ms']);
});﻿/* Dutch (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Mathias Bynens <http://mathiasbynens.be/> */
jQuery(function($){
	$.datepicker.regional.nl = {
		closeText: 'Sluiten',
		prevText: '←',
		nextText: '→',
		currentText: 'Vandaag',
		monthNames: ['januari', 'februari', 'maart', 'april', 'mei', 'juni',
		'juli', 'augustus', 'september', 'oktober', 'november', 'december'],
		monthNamesShort: ['jan', 'feb', 'maa', 'apr', 'mei', 'jun',
		'jul', 'aug', 'sep', 'okt', 'nov', 'dec'],
		dayNames: ['zondag', 'maandag', 'dinsdag', 'woensdag', 'donderdag', 'vrijdag', 'zaterdag'],
		dayNamesShort: ['zon', 'maa', 'din', 'woe', 'don', 'vri', 'zat'],
		dayNamesMin: ['zo', 'ma', 'di', 'wo', 'do', 'vr', 'za'],
		weekHeader: 'Wk',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional.nl);
});/* Norwegian initialisation for the jQuery UI date picker plugin. */
/* Written by Naimdjon Takhirov (naimdjon@gmail.com). */
jQuery(function($){
    $.datepicker.regional['no'] = {
		closeText: 'Lukk',
        prevText: '&laquo;Forrige',
		nextText: 'Neste&raquo;',
		currentText: 'I dag',
        monthNames: ['Januar','Februar','Mars','April','Mai','Juni',
        'Juli','August','September','Oktober','November','Desember'],
        monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','Jun',
        'Jul','Aug','Sep','Okt','Nov','Des'],
		dayNamesShort: ['Søn','Man','Tir','Ons','Tor','Fre','Lør'],
		dayNames: ['Søndag','Mandag','Tirsdag','Onsdag','Torsdag','Fredag','Lørdag'],
		dayNamesMin: ['Sø','Ma','Ti','On','To','Fr','Lø'],
		weekHeader: 'Uke',
        dateFormat: 'yy-mm-dd',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
    $.datepicker.setDefaults($.datepicker.regional['no']);
});
/* Polish initialisation for the jQuery UI date picker plugin. */
/* Written by Jacek Wysocki (jacek.wysocki@gmail.com). */
jQuery(function($){
	$.datepicker.regional['pl'] = {
		closeText: 'Zamknij',
		prevText: '&#x3c;Poprzedni',
		nextText: 'Następny&#x3e;',
		currentText: 'Dziś',
		monthNames: ['Styczeń','Luty','Marzec','Kwiecień','Maj','Czerwiec',
		'Lipiec','Sierpień','Wrzesień','Październik','Listopad','Grudzień'],
		monthNamesShort: ['Sty','Lu','Mar','Kw','Maj','Cze',
		'Lip','Sie','Wrz','Pa','Lis','Gru'],
		dayNames: ['Niedziela','Poniedziałek','Wtorek','Środa','Czwartek','Piątek','Sobota'],
		dayNamesShort: ['Nie','Pn','Wt','Śr','Czw','Pt','So'],
		dayNamesMin: ['N','Pn','Wt','Śr','Cz','Pt','So'],
		weekHeader: 'Tydz',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['pl']);
});
/* Brazilian initialisation for the jQuery UI date picker plugin. */
/* Written by Leonildo Costa Silva (leocsilva@gmail.com). */
jQuery(function($){
	$.datepicker.regional['pt-BR'] = {
		closeText: 'Fechar',
		prevText: '&#x3c;Anterior',
		nextText: 'Pr&oacute;ximo&#x3e;',
		currentText: 'Hoje',
		monthNames: ['Janeiro','Fevereiro','Mar&ccedil;o','Abril','Maio','Junho',
		'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
		monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun',
		'Jul','Ago','Set','Out','Nov','Dez'],
		dayNames: ['Domingo','Segunda-feira','Ter&ccedil;a-feira','Quarta-feira','Quinta-feira','Sexta-feira','S&aacute;bado'],
		dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','S&aacute;b'],
		dayNamesMin: ['Dom','Seg','Ter','Qua','Qui','Sex','S&aacute;b'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['pt-BR']);
});﻿/* Romanian initialisation for the jQuery UI date picker plugin.
 *
 * Written by Edmond L. (ll_edmond@walla.com)
 * and Ionut G. Stan (ionut.g.stan@gmail.com)
 */
jQuery(function($){
	$.datepicker.regional['ro'] = {
		closeText: 'Închide',
		prevText: '&laquo; Luna precedentă',
		nextText: 'Luna următoare &raquo;',
		currentText: 'Azi',
		monthNames: ['Ianuarie','Februarie','Martie','Aprilie','Mai','Iunie',
		'Iulie','August','Septembrie','Octombrie','Noiembrie','Decembrie'],
		monthNamesShort: ['Ian', 'Feb', 'Mar', 'Apr', 'Mai', 'Iun',
		'Iul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
		dayNames: ['Duminică', 'Luni', 'Marţi', 'Miercuri', 'Joi', 'Vineri', 'Sâmbătă'],
		dayNamesShort: ['Dum', 'Lun', 'Mar', 'Mie', 'Joi', 'Vin', 'Sâm'],
		dayNamesMin: ['Du','Lu','Ma','Mi','Jo','Vi','Sâ'],
		weekHeader: 'Săpt',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ro']);
});
/* Russian (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Andrew Stromnov (stromnov@gmail.com). */
jQuery(function($){
	$.datepicker.regional['ru'] = {
		closeText: 'Закрыть',
		prevText: '&#x3c;Пред',
		nextText: 'След&#x3e;',
		currentText: 'Сегодня',
		monthNames: ['Январь','Февраль','Март','Апрель','Май','Июнь',
		'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'],
		monthNamesShort: ['Янв','Фев','Мар','Апр','Май','Июн',
		'Июл','Авг','Сен','Окт','Ноя','Дек'],
		dayNames: ['воскресенье','понедельник','вторник','среда','четверг','пятница','суббота'],
		dayNamesShort: ['вск','пнд','втр','срд','чтв','птн','сбт'],
		dayNamesMin: ['Вс','Пн','Вт','Ср','Чт','Пт','Сб'],
		weekHeader: 'Не',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ru']);
});/* Slovak initialisation for the jQuery UI date picker plugin. */
/* Written by Vojtech Rinik (vojto@hmm.sk). */
jQuery(function($){
	$.datepicker.regional['sk'] = {
		closeText: 'Zavrieť',
		prevText: '&#x3c;Predchádzajúci',
		nextText: 'Nasledujúci&#x3e;',
		currentText: 'Dnes',
		monthNames: ['Január','Február','Marec','Apríl','Máj','Jún',
		'Júl','August','September','Október','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Máj','Jún',
		'Júl','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedel\'a','Pondelok','Utorok','Streda','Štvrtok','Piatok','Sobota'],
		dayNamesShort: ['Ned','Pon','Uto','Str','Štv','Pia','Sob'],
		dayNamesMin: ['Ne','Po','Ut','St','Št','Pia','So'],
		weekHeader: 'Ty',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['sk']);
});
/* Slovenian initialisation for the jQuery UI date picker plugin. */
/* Written by Jaka Jancar (jaka@kubje.org). */
/* c = &#x10D;, s = &#x161; z = &#x17E; C = &#x10C; S = &#x160; Z = &#x17D; */
jQuery(function($){
	$.datepicker.regional['sl'] = {
		closeText: 'Zapri',
		prevText: '&lt;Prej&#x161;nji',
		nextText: 'Naslednji&gt;',
		currentText: 'Trenutni',
		monthNames: ['Januar','Februar','Marec','April','Maj','Junij',
		'Julij','Avgust','September','Oktober','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Avg','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedelja','Ponedeljek','Torek','Sreda','&#x10C;etrtek','Petek','Sobota'],
		dayNamesShort: ['Ned','Pon','Tor','Sre','&#x10C;et','Pet','Sob'],
		dayNamesMin: ['Ne','Po','To','Sr','&#x10C;e','Pe','So'],
		weekHeader: 'Teden',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['sl']);
});
﻿/* Albanian initialisation for the jQuery UI date picker plugin. */
/* Written by Flakron Bytyqi (flakron@gmail.com). */
jQuery(function($){
	$.datepicker.regional['sq'] = {
		closeText: 'mbylle',
		prevText: '&#x3c;mbrapa',
		nextText: 'Përpara&#x3e;',
		currentText: 'sot',
		monthNames: ['Janar','Shkurt','Mars','Prill','Maj','Qershor',
		'Korrik','Gusht','Shtator','Tetor','Nëntor','Dhjetor'],
		monthNamesShort: ['Jan','Shk','Mar','Pri','Maj','Qer',
		'Kor','Gus','Sht','Tet','Nën','Dhj'],
		dayNames: ['E Diel','E Hënë','E Martë','E Mërkurë','E Enjte','E Premte','E Shtune'],
		dayNamesShort: ['Di','Hë','Ma','Më','En','Pr','Sh'],
		dayNamesMin: ['Di','Hë','Ma','Më','En','Pr','Sh'],
		weekHeader: 'Ja',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['sq']);
});
﻿/* Serbian i18n for the jQuery UI date picker plugin. */
/* Written by Dejan Dimić. */
jQuery(function($){
	$.datepicker.regional['sr-SR'] = {
		closeText: 'Zatvori',
		prevText: '&#x3c;',
		nextText: '&#x3e;',
		currentText: 'Danas',
		monthNames: ['Januar','Februar','Mart','April','Maj','Jun',
		'Jul','Avgust','Septembar','Oktobar','Novembar','Decembar'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Avg','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedelja','Ponedeljak','Utorak','Sreda','Četvrtak','Petak','Subota'],
		dayNamesShort: ['Ned','Pon','Uto','Sre','Čet','Pet','Sub'],
		dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
		weekHeader: 'Sed',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['sr-SR']);
});
﻿/* Serbian i18n for the jQuery UI date picker plugin. */
/* Written by Dejan Dimić. */
jQuery(function($){
	$.datepicker.regional['sr'] = {
		closeText: 'Затвори',
		prevText: '&#x3c;',
		nextText: '&#x3e;',
		currentText: 'Данас',
		monthNames: ['Јануар','Фебруар','Март','Април','Мај','Јун',
		'Јул','Август','Септембар','Октобар','Новембар','Децембар'],
		monthNamesShort: ['Јан','Феб','Мар','Апр','Мај','Јун',
		'Јул','Авг','Сеп','Окт','Нов','Дец'],
		dayNames: ['Недеља','Понедељак','Уторак','Среда','Четвртак','Петак','Субота'],
		dayNamesShort: ['Нед','Пон','Уто','Сре','Чет','Пет','Суб'],
		dayNamesMin: ['Не','По','Ут','Ср','Че','Пе','Су'],
		weekHeader: 'Сед',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['sr']);
});
﻿/* Swedish initialisation for the jQuery UI date picker plugin. */
/* Written by Anders Ekdahl ( anders@nomadiz.se). */
jQuery(function($){
    $.datepicker.regional['sv'] = {
		closeText: 'Stäng',
        prevText: '&laquo;Förra',
		nextText: 'Nästa&raquo;',
		currentText: 'Idag',
        monthNames: ['Januari','Februari','Mars','April','Maj','Juni',
        'Juli','Augusti','September','Oktober','November','December'],
        monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
        'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNamesShort: ['Sön','Mån','Tis','Ons','Tor','Fre','Lör'],
		dayNames: ['Söndag','Måndag','Tisdag','Onsdag','Torsdag','Fredag','Lördag'],
		dayNamesMin: ['Sö','Må','Ti','On','To','Fr','Lö'],
		weekHeader: 'Ve',
        dateFormat: 'yy-mm-dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
    $.datepicker.setDefaults($.datepicker.regional['sv']);
});
﻿/* Tamil (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by S A Sureshkumar (saskumar@live.com). */
jQuery(function($){
	$.datepicker.regional['ta'] = {
		closeText: 'மூடு',
		prevText: 'முன்னையது',
		nextText: 'அடுத்தது',
		currentText: 'இன்று',
		monthNames: ['தை','மாசி','பங்குனி','சித்திரை','வைகாசி','ஆனி',
		'ஆடி','ஆவணி','புரட்டாசி','ஐப்பசி','கார்த்திகை','மார்கழி'],
		monthNamesShort: ['தை','மாசி','பங்','சித்','வைகா','ஆனி',
		'ஆடி','ஆவ','புர','ஐப்','கார்','மார்'],
		dayNames: ['ஞாயிற்றுக்கிழமை','திங்கட்கிழமை','செவ்வாய்க்கிழமை','புதன்கிழமை','வியாழக்கிழமை','வெள்ளிக்கிழமை','சனிக்கிழமை'],
		dayNamesShort: ['ஞாயிறு','திங்கள்','செவ்வாய்','புதன்','வியாழன்','வெள்ளி','சனி'],
		dayNamesMin: ['ஞா','தி','செ','பு','வி','வெ','ச'],
		weekHeader: 'Не',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['ta']);
});
﻿/* Thai initialisation for the jQuery UI date picker plugin. */
/* Written by pipo (pipo@sixhead.com). */
jQuery(function($){
	$.datepicker.regional['th'] = {
		closeText: 'ปิด',
		prevText: '&laquo;&nbsp;ย้อน',
		nextText: 'ถัดไป&nbsp;&raquo;',
		currentText: 'วันนี้',
		monthNames: ['มกราคม','กุมภาพันธ์','มีนาคม','เมษายน','พฤษภาคม','มิถุนายน',
		'กรกฏาคม','สิงหาคม','กันยายน','ตุลาคม','พฤศจิกายน','ธันวาคม'],
		monthNamesShort: ['ม.ค.','ก.พ.','มี.ค.','เม.ย.','พ.ค.','มิ.ย.',
		'ก.ค.','ส.ค.','ก.ย.','ต.ค.','พ.ย.','ธ.ค.'],
		dayNames: ['อาทิตย์','จันทร์','อังคาร','พุธ','พฤหัสบดี','ศุกร์','เสาร์'],
		dayNamesShort: ['อา.','จ.','อ.','พ.','พฤ.','ศ.','ส.'],
		dayNamesMin: ['อา.','จ.','อ.','พ.','พฤ.','ศ.','ส.'],
		weekHeader: 'Wk',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['th']);
});/* Turkish initialisation for the jQuery UI date picker plugin. */
/* Written by Izzet Emre Erkan (kara@karalamalar.net). */
jQuery(function($){
	$.datepicker.regional['tr'] = {
		closeText: 'kapat',
		prevText: '&#x3c;geri',
		nextText: 'ileri&#x3e',
		currentText: 'bugün',
		monthNames: ['Ocak','Şubat','Mart','Nisan','Mayıs','Haziran',
		'Temmuz','Ağustos','Eylül','Ekim','Kasım','Aralık'],
		monthNamesShort: ['Oca','Şub','Mar','Nis','May','Haz',
		'Tem','Ağu','Eyl','Eki','Kas','Ara'],
		dayNames: ['Pazar','Pazartesi','Salı','Çarşamba','Perşembe','Cuma','Cumartesi'],
		dayNamesShort: ['Pz','Pt','Sa','Ça','Pe','Cu','Ct'],
		dayNamesMin: ['Pz','Pt','Sa','Ça','Pe','Cu','Ct'],
		weekHeader: 'Hf',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['tr']);
});/* Ukrainian (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Maxim Drogobitskiy (maxdao@gmail.com). */
jQuery(function($){
	$.datepicker.regional['uk'] = {
		closeText: 'Закрити',
		prevText: '&#x3c;',
		nextText: '&#x3e;',
		currentText: 'Сьогодні',
		monthNames: ['Січень','Лютий','Березень','Квітень','Травень','Червень',
		'Липень','Серпень','Вересень','Жовтень','Листопад','Грудень'],
		monthNamesShort: ['Січ','Лют','Бер','Кві','Тра','Чер',
		'Лип','Сер','Вер','Жов','Лис','Гру'],
		dayNames: ['неділя','понеділок','вівторок','середа','четвер','п’ятниця','субота'],
		dayNamesShort: ['нед','пнд','вів','срд','чтв','птн','сбт'],
		dayNamesMin: ['Нд','Пн','Вт','Ср','Чт','Пт','Сб'],
		weekHeader: 'Не',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['uk']);
});﻿/* Vietnamese initialisation for the jQuery UI date picker plugin. */
/* Translated by Le Thanh Huy (lthanhhuy@cit.ctu.edu.vn). */
jQuery(function($){
	$.datepicker.regional['vi'] = {
		closeText: 'Đóng',
		prevText: '&#x3c;Trước',
		nextText: 'Tiếp&#x3e;',
		currentText: 'Hôm nay',
		monthNames: ['Tháng Một', 'Tháng Hai', 'Tháng Ba', 'Tháng Tư', 'Tháng Năm', 'Tháng Sáu',
		'Tháng Bảy', 'Tháng Tám', 'Tháng Chín', 'Tháng Mười', 'Tháng Mười Một', 'Tháng Mười Hai'],
		monthNamesShort: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
		'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'],
		dayNames: ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'],
		dayNamesShort: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
		dayNamesMin: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
		weekHeader: 'Tu',
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['vi']);
});
/* Chinese initialisation for the jQuery UI date picker plugin. */
/* Written by Cloudream (cloudream@gmail.com). */
jQuery(function($){
	$.datepicker.regional['zh-CN'] = {
		closeText: '关闭',
		prevText: '&#x3c;上月',
		nextText: '下月&#x3e;',
		currentText: '今天',
		monthNames: ['一月','二月','三月','四月','五月','六月',
		'七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort: ['一','二','三','四','五','六',
		'七','八','九','十','十一','十二'],
		dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin: ['日','一','二','三','四','五','六'],
		weekHeader: '周',
		dateFormat: 'yy-mm-dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: true,
		yearSuffix: '年'};
	$.datepicker.setDefaults($.datepicker.regional['zh-CN']);
});
/* Chinese initialisation for the jQuery UI date picker plugin. */
/* Written by SCCY (samuelcychan@gmail.com). */
jQuery(function($){
	$.datepicker.regional['zh-HK'] = {
		closeText: '關閉',
		prevText: '&#x3c;上月',
		nextText: '下月&#x3e;',
		currentText: '今天',
		monthNames: ['一月','二月','三月','四月','五月','六月',
		'七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort: ['一','二','三','四','五','六',
		'七','八','九','十','十一','十二'],
		dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin: ['日','一','二','三','四','五','六'],
		weekHeader: '周',
		dateFormat: 'dd-mm-yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: true,
		yearSuffix: '年'};
	$.datepicker.setDefaults($.datepicker.regional['zh-HK']);
});
﻿/* Chinese initialisation for the jQuery UI date picker plugin. */
/* Written by Ressol (ressol@gmail.com). */
jQuery(function($){
	$.datepicker.regional['zh-TW'] = {
		closeText: '關閉',
		prevText: '&#x3c;上月',
		nextText: '下月&#x3e;',
		currentText: '今天',
		monthNames: ['一月','二月','三月','四月','五月','六月',
		'七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort: ['一','二','三','四','五','六',
		'七','八','九','十','十一','十二'],
		dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin: ['日','一','二','三','四','五','六'],
		weekHeader: '周',
		dateFormat: 'yy/mm/dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: true,
		yearSuffix: '年'};
	$.datepicker.setDefaults($.datepicker.regional['zh-TW']);
});


/*
 * jQuery Autocomplete plugin 1.1
 *
 * Copyright (c) 2009 Jörn Zaefferer
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *
 * Revision: $Id: jquery.autocomplete.js 15 2009-08-22 10:30:27Z joern.zaefferer $
 */

;(function($) {
	
$.fn.extend({
	autocomplete: function(searchFunction, options) {
		urlOrData = "NOURL";
		var isUrl = typeof urlOrData == "string";
		options = $.extend({}, $.Autocompleter.defaults, {
			url: isUrl ? urlOrData : null,
			data: isUrl ? null : urlOrData,
			searchFunction: searchFunction,
			delay: isUrl ? $.Autocompleter.defaults.delay : 10,
			max: options && !options.scroll ? 10 : 150
		}, options);
		
		// if highlight is set to false, replace it with a do-nothing function
		options.highlight = options.highlight || function(value) { return value; };
		
		// if the formatMatch option is not specified, then use formatItem for backwards compatibility
		options.formatMatch = options.formatMatch || options.formatItem;
		
		return this.each(function() {
			new $.Autocompleter(this, options);
		});
	},
	result: function(handler) {
		return this.bind("result", handler);
	},
	search: function(handler) {
		return this.trigger("search", [handler]);
	},
	flushCache: function() {
		return this.trigger("flushCache");
	},
	setOptions: function(options){
		return this.trigger("setOptions", [options]);
	},
	unautocomplete: function() {
		return this.trigger("unautocomplete");
	},
	showResults: function(results) {
		return this.trigger("showResults", [results]);
	}
});

$.Autocompleter = function(input, options) {

	var KEY = {
		UP: 38,
		DOWN: 40,
		DEL: 46,
		TAB: 9,
		RETURN: 13,
		ESC: 27,
		COMMA: 188,
		PAGEUP: 33,
		PAGEDOWN: 34,
		BACKSPACE: 8
	};

	// Create $ object for input element
	var $input = $(input).attr("autocomplete", "off").addClass(options.inputClass);

	var timeout;
	var previousValue = "";
	var cache = $.Autocompleter.Cache(options);
	var hasFocus = 0;
	var lastKeyPressCode;
	var config = {
		mouseDownOnSelect: false
	};
	var select = $.Autocompleter.Select(options, input, selectCurrent, config);
	
	var blockSubmit;
	
	// prevent form submit in opera when selecting with return key
	$.browser.opera && $(input.form).bind("submit.autocomplete", function() {
		if (blockSubmit) {
			blockSubmit = false;
			return false;
		}
	});
	
	// only opera doesn't trigger keydown multiple times while pressed, others don't work with keypress at all
	$input.bind(($.browser.opera ? "keypress" : "keydown") + ".autocomplete", function(event) {
		// a keypress means the input has focus
		// avoids issue where input had focus before the autocomplete was applied
		hasFocus = 1;
		// track last key pressed
		lastKeyPressCode = event.keyCode;
		switch(event.keyCode) {
		
			case KEY.UP:
				event.preventDefault();
				if ( select.visible() ) {
					select.prev();
				} else {
					onChange(0, true);
				}
				break;
				
			case KEY.DOWN:
				event.preventDefault();
				if ( select.visible() ) {
					select.next();
				} else {
					onChange(0, true);
				}
				break;
				
			case KEY.PAGEUP:
				event.preventDefault();
				if ( select.visible() ) {
					select.pageUp();
				} else {
					onChange(0, true);
				}
				break;
				
			case KEY.PAGEDOWN:
				event.preventDefault();
				if ( select.visible() ) {
					select.pageDown();
				} else {
					onChange(0, true);
				}
				break;
			
			// matches also semicolon
			case options.multiple && $.trim(options.multipleSeparator) == "," && KEY.COMMA:
			case KEY.TAB:
			case KEY.RETURN:
				if( selectCurrent() ) {
					// stop default to prevent a form submit, Opera needs special handling
					event.preventDefault();
					blockSubmit = true;
					return false;
				}
				break;
				
			case KEY.ESC:
				select.hide();
				break;
				
			default:
				clearTimeout(timeout);
				timeout = setTimeout(onChange, options.delay);
				break;
		}
	}).focus(function(){
		// track whether the field has focus, we shouldn't process any
		// results if the field no longer has focus
		hasFocus++;
	}).blur(function() {
		hasFocus = 0;
		if (!config.mouseDownOnSelect) {
			hideResults();
		}
	}).click(function() {
		// show select when clicking in a focused field
		if ( hasFocus++ > 1 && !select.visible() ) {
			onChange(0, true);
		}
	}).bind("search", function() {
		// TODO why not just specifying both arguments?
		var fn = (arguments.length > 1) ? arguments[1] : null;
		function findValueCallback(q, data) {
			var result;
			if( data && data.length ) {
				for (var i=0; i < data.length; i++) {
					if( data[i].result.toLowerCase() == q.toLowerCase() ) {
						result = data[i];
						break;
					}
				}
			}
			if( typeof fn == "function" ) fn(result);
			else $input.trigger("result", result && [result.data, result.value]);
		}
		$.each(trimWords($input.val()), function(i, value) {
			request(value, findValueCallback, findValueCallback);
		});
	}).bind("flushCache", function() {
		cache.flush();
	}).bind("setOptions", function() {
		$.extend(options, arguments[1]);
		// if we've updated the data, repopulate
		if ( "data" in arguments[1] )
			cache.populate();
	}).bind("unautocomplete", function() {
		select.unbind();
		$input.unbind();
		$(input.form).unbind(".autocomplete");
	}).bind("showResults", function() {
		var results = arguments[1];
		QCD.info(results);
		receiveData("aa", results)
	});
	
	
	function selectCurrent() {
		var selected = select.selected();
		if( !selected )
			return false;
		
		var v = selected.result;
		previousValue = v;
		
		if ( options.multiple ) {
			var words = trimWords($input.val());
			if ( words.length > 1 ) {
				var seperator = options.multipleSeparator.length;
				var cursorAt = $(input).selection().start;
				var wordAt, progress = 0;
				$.each(words, function(i, word) {
					progress += word.length;
					if (cursorAt <= progress) {
						wordAt = i;
						return false;
					}
					progress += seperator;
				});
				words[wordAt] = v;
				// TODO this should set the cursor to the right position, but it gets overriden somewhere
				//$.Autocompleter.Selection(input, progress + seperator, progress + seperator);
				v = words.join( options.multipleSeparator );
			}
			v += options.multipleSeparator;
		}
		
		$input.val(v);
		hideResultsNow();
		$input.trigger("result", [selected.data, selected.value]);
		return true;
	}
	
	function onChange(crap, skipPrevCheck) {
		if( lastKeyPressCode == KEY.DEL ) {
			select.hide();
			return;
		}
		
		var currentValue = $input.val();
		
		if ( !skipPrevCheck && currentValue == previousValue )
			return;
		
		previousValue = currentValue;
		
		currentValue = lastWord(currentValue);
		if ( currentValue.length >= options.minChars) {
			$input.addClass(options.loadingClass);
			if (!options.matchCase)
				currentValue = currentValue.toLowerCase();
			request(currentValue, receiveData, hideResultsNow);
		} else {
			stopLoading();
			select.hide();
		}
	};
	
	function trimWords(value) {
		if (!value)
			return [""];
		if (!options.multiple)
			return [$.trim(value)];
		return $.map(value.split(options.multipleSeparator), function(word) {
			return $.trim(value).length ? $.trim(word) : null;
		});
	}
	
	function lastWord(value) {
		if ( !options.multiple )
			return value;
		var words = trimWords(value);
		if (words.length == 1) 
			return words[0];
		var cursorAt = $(input).selection().start;
		if (cursorAt == value.length) {
			words = trimWords(value)
		} else {
			words = trimWords(value.replace(value.substring(cursorAt), ""));
		}
		return words[words.length - 1];
	}
	
	// fills in the input box w/the first match (assumed to be the best match)
	// q: the term entered
	// sValue: the first matching result
	function autoFill(q, sValue){
		// autofill in the complete box w/the first match as long as the user hasn't entered in more data
		// if the last user key pressed was backspace, don't autofill
		if( options.autoFill && (lastWord($input.val()).toLowerCase() == q.toLowerCase()) && lastKeyPressCode != KEY.BACKSPACE ) {
			// fill in the value (keep the case the user has typed)
			$input.val($input.val() + sValue.substring(lastWord(previousValue).length));
			// select the portion of the value not typed by the user (so the next character will erase)
			$(input).selection(previousValue.length, previousValue.length + sValue.length);
		}
	};

	function hideResults() {
		clearTimeout(timeout);
		timeout = setTimeout(hideResultsNow, 200);
	};

	function hideResultsNow() {
		var wasVisible = select.visible();
		select.hide();
		clearTimeout(timeout);
		stopLoading();
		if (options.mustMatch) {
			// call search and run callback
			$input.search(
				function (result){
					// if no value found, clear the input box
					if( !result ) {
						if (options.multiple) {
							var words = trimWords($input.val()).slice(0, -1);
							$input.val( words.join(options.multipleSeparator) + (words.length ? options.multipleSeparator : "") );
						}
						else {
							$input.val( "" );
							$input.trigger("result", null);
						}
					}
				}
			);
		}
	};

	function receiveData(q, data) {
		if ( data && data.length && hasFocus ) {
			stopLoading();
			select.display(data, q);
			autoFill(q, data[0].value);
			select.show();
		} else {
			hideResultsNow();
		}
	};

	function request(term, success, failure) {
		if (!options.matchCase)
			term = term.toLowerCase();
		var data = cache.load(term);
		// recieve the cached data
		//if (data && data.length) {
		//	success(term, data);
		// if an AJAX url has been supplied, try loading the data now
		//} else if( (typeof options.url == "string") && (options.url.length > 0) ){
		
		//QCD.info(options.searchFunction);
		options.searchFunction(lastWord(term));
		
//		if( (typeof options.url == "string") && (options.url.length > 0) ){
//			
//			var extraParams = {
//				timestamp: +new Date()
//			};
//			$.each(options.extraParams, function(key, param) {
//				extraParams[key] = typeof param == "function" ? param() : param;
//			});
//			
//			$.ajax({
//				// try to leverage ajaxQueue plugin to abort previous requests
//				mode: "abort",
//				// limit abortion to this input
//				port: "autocomplete" + input.name,
//				dataType: options.dataType,
//				url: options.url,
//				data: $.extend({
//					q: lastWord(term),
//					limit: options.max
//				}, extraParams),
//				success: function(data) {
//					var parsed = options.parse && options.parse(data) || parse(data);
//					cache.add(term, parsed);
//					success(term, parsed);
//				}
//			});
//		} else {
//			// if we have a failure, we need to empty the list -- this prevents the the [TAB] key from selecting the last successful match
//			select.emptyList();
//			failure(term);
//		}
	};
	
	function parse(data) {
		var parsed = [];
		var rows = data.split("\n");
		for (var i=0; i < rows.length; i++) {
			var row = $.trim(rows[i]);
			if (row) {
				row = row.split("|");
				parsed[parsed.length] = {
					data: row,
					value: row[0],
					result: options.formatResult && options.formatResult(row, row[0]) || row[0]
				};
			}
		}
		return parsed;
	};

	function stopLoading() {
		$input.removeClass(options.loadingClass);
	};

};

$.Autocompleter.defaults = {
	inputClass: "ac_input",
	resultsClass: "ac_results",
	loadingClass: "ac_loading",
	minChars: 1,
	delay: 400,
	matchCase: false,
	matchSubset: true,
	matchContains: false,
	cacheLength: 10,
	max: 100,
	mustMatch: false,
	extraParams: {},
	selectFirst: true,
	formatItem: function(row) { return row[0]; },
	formatMatch: null,
	autoFill: false,
	width: 0,
	multiple: false,
	multipleSeparator: ", ",
	highlight: function(value, term) {
		return value.replace(new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + term.replace(/([\^\$\(\)\[\]\{\}\*\.\+\?\|\\])/gi, "\\$1") + ")(?![^<>]*>)(?![^&;]+;)", "gi"), "<strong>$1</strong>");
	},
    scroll: true,
    scrollHeight: 180
};

$.Autocompleter.Cache = function(options) {

	var data = {};
	var length = 0;
	
	function matchSubset(s, sub) {
		if (!options.matchCase) 
			s = s.toLowerCase();
		var i = s.indexOf(sub);
		if (options.matchContains == "word"){
			i = s.toLowerCase().search("\\b" + sub.toLowerCase());
		}
		if (i == -1) return false;
		return i == 0 || options.matchContains;
	};
	
	function add(q, value) {
		if (length > options.cacheLength){
			flush();
		}
		if (!data[q]){ 
			length++;
		}
		data[q] = value;
	}
	
	function populate(){
		if( !options.data ) return false;
		// track the matches
		var stMatchSets = {},
			nullData = 0;

		// no url was specified, we need to adjust the cache length to make sure it fits the local data store
		if( !options.url ) options.cacheLength = 1;
		
		// track all options for minChars = 0
		stMatchSets[""] = [];
		
		// loop through the array and create a lookup structure
		for ( var i = 0, ol = options.data.length; i < ol; i++ ) {
			var rawValue = options.data[i];
			// if rawValue is a string, make an array otherwise just reference the array
			rawValue = (typeof rawValue == "string") ? [rawValue] : rawValue;
			
			var value = options.formatMatch(rawValue, i+1, options.data.length);
			if ( value === false )
				continue;
				
			var firstChar = value.charAt(0).toLowerCase();
			// if no lookup array for this character exists, look it up now
			if( !stMatchSets[firstChar] ) 
				stMatchSets[firstChar] = [];

			// if the match is a string
			var row = {
				value: value,
				data: rawValue,
				result: options.formatResult && options.formatResult(rawValue) || value
			};
			
			// push the current match into the set list
			stMatchSets[firstChar].push(row);

			// keep track of minChars zero items
			if ( nullData++ < options.max ) {
				stMatchSets[""].push(row);
			}
		};

		// add the data items to the cache
		$.each(stMatchSets, function(i, value) {
			// increase the cache size
			options.cacheLength++;
			// add to the cache
			add(i, value);
		});
	}
	
	// populate any existing data
	setTimeout(populate, 25);
	
	function flush(){
		data = {};
		length = 0;
	}
	
	return {
		flush: flush,
		add: add,
		populate: populate,
		load: function(q) {
			if (!options.cacheLength || !length)
				return null;
			/* 
			 * if dealing w/local data and matchContains than we must make sure
			 * to loop through all the data collections looking for matches
			 */
			if( !options.url && options.matchContains ){
				// track all matches
				var csub = [];
				// loop through all the data grids for matches
				for( var k in data ){
					// don't search through the stMatchSets[""] (minChars: 0) cache
					// this prevents duplicates
					if( k.length > 0 ){
						var c = data[k];
						$.each(c, function(i, x) {
							// if we've got a match, add it to the array
							if (matchSubset(x.value, q)) {
								csub.push(x);
							}
						});
					}
				}				
				return csub;
			} else 
			// if the exact item exists, use it
			if (data[q]){
				return data[q];
			} else
			if (options.matchSubset) {
				for (var i = q.length - 1; i >= options.minChars; i--) {
					var c = data[q.substr(0, i)];
					if (c) {
						var csub = [];
						$.each(c, function(i, x) {
							if (matchSubset(x.value, q)) {
								csub[csub.length] = x;
							}
						});
						return csub;
					}
				}
			}
			return null;
		}
	};
};

$.Autocompleter.Select = function (options, input, select, config) {
	var CLASSES = {
		ACTIVE: "ac_over"
	};
	
	var listItems,
		active = -1,
		data,
		term = "",
		needsInit = true,
		element,
		list;
	
	// Create results
	function init() {
		if (!needsInit)
			return;
		element = $("<div/>")
		.hide()
		.addClass(options.resultsClass)
		.css("position", "absolute")
		.appendTo(document.body);
	
		list = $("<ul/>").appendTo(element).mouseover( function(event) {
			if(target(event).nodeName && target(event).nodeName.toUpperCase() == 'LI') {
	            active = $("li", list).removeClass(CLASSES.ACTIVE).index(target(event));
			    $(target(event)).addClass(CLASSES.ACTIVE);            
	        }
		}).click(function(event) {
			$(target(event)).addClass(CLASSES.ACTIVE);
			select();
			// TODO provide option to avoid setting focus again after selection? useful for cleanup-on-focus
			input.focus();
			return false;
		}).mousedown(function() {
			config.mouseDownOnSelect = true;
		}).mouseup(function() {
			config.mouseDownOnSelect = false;
		});
		
		if( options.width > 0 )
			element.css("width", options.width);
			
		needsInit = false;
	} 
	
	function target(event) {
		var element = event.target;
		while(element && element.tagName != "LI")
			element = element.parentNode;
		// more fun with IE, sometimes event.target is empty, just ignore it then
		if(!element)
			return [];
		return element;
	}

	function moveSelect(step) {
		listItems.slice(active, active + 1).removeClass(CLASSES.ACTIVE);
		movePosition(step);
        var activeItem = listItems.slice(active, active + 1).addClass(CLASSES.ACTIVE);
        if(options.scroll) {
            var offset = 0;
            listItems.slice(0, active).each(function() {
				offset += this.offsetHeight;
			});
            if((offset + activeItem[0].offsetHeight - list.scrollTop()) > list[0].clientHeight) {
                list.scrollTop(offset + activeItem[0].offsetHeight - list.innerHeight());
            } else if(offset < list.scrollTop()) {
                list.scrollTop(offset);
            }
        }
	};
	
	function movePosition(step) {
		active += step;
		if (active < 0) {
			active = listItems.size() - 1;
		} else if (active >= listItems.size()) {
			active = 0;
		}
	}
	
	function limitNumberOfItems(available) {
		return options.max && options.max < available
			? options.max
			: available;
	}
	
	function fillList() {
		list.empty();
		var max = limitNumberOfItems(data.length);
		for (var i=0; i < max; i++) {
			if (!data[i])
				continue;
			var formatted = options.formatItem(data[i].data, i+1, max, data[i].value, term);
			if ( formatted === false )
				continue;
			var li = $("<li/>").html( options.highlight(formatted, term) ).addClass(i%2 == 0 ? "ac_even" : "ac_odd").appendTo(list)[0];
			$.data(li, "ac_data", data[i]);
		}
		listItems = list.find("li");
		if ( options.selectFirst ) {
			listItems.slice(0, 1).addClass(CLASSES.ACTIVE);
			active = 0;
		}
		// apply bgiframe if available
		if ( $.fn.bgiframe )
			list.bgiframe();
	}
	
	return {
		display: function(d, q) {
			init();
			data = d;
			term = q;
			fillList();
		},
		next: function() {
			moveSelect(1);
		},
		prev: function() {
			moveSelect(-1);
		},
		pageUp: function() {
			if (active != 0 && active - 8 < 0) {
				moveSelect( -active );
			} else {
				moveSelect(-8);
			}
		},
		pageDown: function() {
			if (active != listItems.size() - 1 && active + 8 > listItems.size()) {
				moveSelect( listItems.size() - 1 - active );
			} else {
				moveSelect(8);
			}
		},
		hide: function() {
			element && element.hide();
			listItems && listItems.removeClass(CLASSES.ACTIVE);
			active = -1;
		},
		visible : function() {
			return element && element.is(":visible");
		},
		current: function() {
			return this.visible() && (listItems.filter("." + CLASSES.ACTIVE)[0] || options.selectFirst && listItems[0]);
		},
		show: function() {
			var offset = $(input).offset();
			element.css({
				width: typeof options.width == "string" || options.width > 0 ? options.width : $(input).width(),
				top: offset.top + input.offsetHeight,
				left: offset.left
			}).show();
            if(options.scroll) {
                list.scrollTop(0);
                list.css({
					maxHeight: options.scrollHeight,
					overflow: 'auto'
				});
				
                if($.browser.msie && typeof document.body.style.maxHeight === "undefined") {
					var listHeight = 0;
					listItems.each(function() {
						listHeight += this.offsetHeight;
					});
					var scrollbarsVisible = listHeight > options.scrollHeight;
                    list.css('height', scrollbarsVisible ? options.scrollHeight : listHeight );
					if (!scrollbarsVisible) {
						// IE doesn't recalculate width when scrollbar disappears
						listItems.width( list.width() - parseInt(listItems.css("padding-left")) - parseInt(listItems.css("padding-right")) );
					}
                }
                
            }
		},
		selected: function() {
			var selected = listItems && listItems.filter("." + CLASSES.ACTIVE).removeClass(CLASSES.ACTIVE);
			return selected && selected.length && $.data(selected[0], "ac_data");
		},
		emptyList: function (){
			list && list.empty();
		},
		unbind: function() {
			element && element.remove();
		}
	};
};

$.fn.selection = function(start, end) {
	if (start !== undefined) {
		return this.each(function() {
			if( this.createTextRange ){
				var selRange = this.createTextRange();
				if (end === undefined || start == end) {
					selRange.move("character", start);
					selRange.select();
				} else {
					selRange.collapse(true);
					selRange.moveStart("character", start);
					selRange.moveEnd("character", end);
					selRange.select();
				}
			} else if( this.setSelectionRange ){
				this.setSelectionRange(start, end);
			} else if( this.selectionStart ){
				this.selectionStart = start;
				this.selectionEnd = end;
			}
		});
	}
	var field = this[0];
	if ( field.createTextRange ) {
		var range = document.selection.createRange(),
			orig = field.value,
			teststring = "<->",
			textLength = range.text.length;
		range.text = teststring;
		var caretAt = field.value.indexOf(teststring);
		field.value = orig;
		this.selection(caretAt, caretAt + textLength);
		return {
			start: caretAt,
			end: caretAt + textLength
		}
	} else if( field.selectionStart !== undefined ){
		return {
			start: field.selectionStart,
			end: field.selectionEnd
		}
	}
};

})(jQuery);

﻿/*!
 * jQuery blockUI plugin
 * Version 2.33 (29-MAR-2010)
 * @requires jQuery v1.2.3 or later
 *
 * Examples at: http://malsup.com/jquery/block/
 * Copyright (c) 2007-2008 M. Alsup
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 * Thanks to Amir-Hossein Sobhi for some excellent contributions!
 */

;(function($) {

if (/1\.(0|1|2)\.(0|1|2)/.test($.fn.jquery) || /^1.1/.test($.fn.jquery)) {
	alert('blockUI requires jQuery v1.2.3 or later!  You are using v' + $.fn.jquery);
	return;
}

$.fn._fadeIn = $.fn.fadeIn;

var noOp = function() {};

// this bit is to ensure we don't call setExpression when we shouldn't (with extra muscle to handle
// retarded userAgent strings on Vista)
var mode = document.documentMode || 0;
var setExpr = $.browser.msie && (($.browser.version < 8 && !mode) || mode < 8);
var ie6 = $.browser.msie && /MSIE 6.0/.test(navigator.userAgent) && !mode;

// global $ methods for blocking/unblocking the entire page
$.blockUI   = function(opts) { install(window, opts); };
$.unblockUI = function(opts) { remove(window, opts); };

// convenience method for quick growl-like notifications  (http://www.google.com/search?q=growl)
$.growlUI = function(title, message, timeout, onClose) {
	var $m = $('<div class="growlUI"></div>');
	if (title) $m.append('<h1>'+title+'</h1>');
	if (message) $m.append('<h2>'+message+'</h2>');
	if (timeout == undefined) timeout = 3000;
	$.blockUI({
		message: $m, fadeIn: 700, fadeOut: 1000, centerY: false,
		timeout: timeout, showOverlay: false,
		onUnblock: onClose, 
		css: $.blockUI.defaults.growlCSS
	});
};

// plugin method for blocking element content
$.fn.block = function(opts) {
	return this.unblock({ fadeOut: 0 }).each(function() {
		if ($.css(this,'position') == 'static')
			this.style.position = 'relative';
		if ($.browser.msie)
			this.style.zoom = 1; // force 'hasLayout'
		install(this, opts);
	});
};

// plugin method for unblocking element content
$.fn.unblock = function(opts) {
	return this.each(function() {
		remove(this, opts);
	});
};

$.blockUI.version = 2.33; // 2nd generation blocking at no extra cost!

// override these in your code to change the default behavior and style
$.blockUI.defaults = {
	// message displayed when blocking (use null for no message)
	message:  '<h1>Please wait...</h1>',

	title: null,	  // title string; only used when theme == true
	draggable: true,  // only used when theme == true (requires jquery-ui.js to be loaded)
	
	theme: false, // set to true to use with jQuery UI themes
	
	// styles for the message when blocking; if you wish to disable
	// these and use an external stylesheet then do this in your code:
	// $.blockUI.defaults.css = {};
	css: {
		padding:	0,
		margin:		0,
		width:		'30%',
		top:		'40%',
		left:		'35%',
		textAlign:	'center',
		color:		'#000',
		border:		'3px solid #aaa',
		backgroundColor:'#fff',
		cursor:		'wait'
	},
	
	// minimal style set used when themes are used
	themedCSS: {
		width:	'30%',
		top:	'40%',
		left:	'35%'
	},

	// styles for the overlay
	overlayCSS:  {
		backgroundColor: '#000',
		opacity:	  	 0.6,
		cursor:		  	 'wait'
	},

	// styles applied when using $.growlUI
	growlCSS: {
		width:  	'350px',
		top:		'10px',
		left:   	'',
		right:  	'10px',
		border: 	'none',
		padding:	'5px',
		opacity:	0.6,
		cursor: 	'default',
		color:		'#fff',
		backgroundColor: '#000',
		'-webkit-border-radius': '10px',
		'-moz-border-radius':	 '10px',
		'border-radius': 		 '10px'
	},
	
	// IE issues: 'about:blank' fails on HTTPS and javascript:false is s-l-o-w
	// (hat tip to Jorge H. N. de Vasconcelos)
	iframeSrc: /^https/i.test(window.location.href || '') ? 'javascript:false' : 'about:blank',

	// force usage of iframe in non-IE browsers (handy for blocking applets)
	forceIframe: false,

	// z-index for the blocking overlay
	baseZ: 1000,

	// set these to true to have the message automatically centered
	centerX: true, // <-- only effects element blocking (page block controlled via css above)
	centerY: true,

	// allow body element to be stetched in ie6; this makes blocking look better
	// on "short" pages.  disable if you wish to prevent changes to the body height
	allowBodyStretch: true,

	// enable if you want key and mouse events to be disabled for content that is blocked
	bindEvents: true,

	// be default blockUI will supress tab navigation from leaving blocking content
	// (if bindEvents is true)
	constrainTabKey: true,

	// fadeIn time in millis; set to 0 to disable fadeIn on block
	fadeIn:  200,

	// fadeOut time in millis; set to 0 to disable fadeOut on unblock
	fadeOut:  400,

	// time in millis to wait before auto-unblocking; set to 0 to disable auto-unblock
	timeout: 0,

	// disable if you don't want to show the overlay
	showOverlay: true,

	// if true, focus will be placed in the first available input field when
	// page blocking
	focusInput: true,

	// suppresses the use of overlay styles on FF/Linux (due to performance issues with opacity)
	applyPlatformOpacityRules: true,
	
	// callback method invoked when fadeIn has completed and blocking message is visible
	onBlock: null,

	// callback method invoked when unblocking has completed; the callback is
	// passed the element that has been unblocked (which is the window object for page
	// blocks) and the options that were passed to the unblock call:
	//	 onUnblock(element, options)
	onUnblock: null,

	// don't ask; if you really must know: http://groups.google.com/group/jquery-en/browse_thread/thread/36640a8730503595/2f6a79a77a78e493#2f6a79a77a78e493
	quirksmodeOffsetHack: 4
};

// private data and functions follow...

var pageBlock = null;
var pageBlockEls = [];

function install(el, opts) {
	var full = (el == window);
	var msg = opts && opts.message !== undefined ? opts.message : undefined;
	opts = $.extend({}, $.blockUI.defaults, opts || {});
	opts.overlayCSS = $.extend({}, $.blockUI.defaults.overlayCSS, opts.overlayCSS || {});
	var css = $.extend({}, $.blockUI.defaults.css, opts.css || {});
	var themedCSS = $.extend({}, $.blockUI.defaults.themedCSS, opts.themedCSS || {});
	msg = msg === undefined ? opts.message : msg;

	// remove the current block (if there is one)
	if (full && pageBlock)
		remove(window, {fadeOut:0});

	// if an existing element is being used as the blocking content then we capture
	// its current place in the DOM (and current display style) so we can restore
	// it when we unblock
	if (msg && typeof msg != 'string' && (msg.parentNode || msg.jquery)) {
		var node = msg.jquery ? msg[0] : msg;
		var data = {};
		$(el).data('blockUI.history', data);
		data.el = node;
		data.parent = node.parentNode;
		data.display = node.style.display;
		data.position = node.style.position;
		if (data.parent)
			data.parent.removeChild(node);
	}

	var z = opts.baseZ;

	// blockUI uses 3 layers for blocking, for simplicity they are all used on every platform;
	// layer1 is the iframe layer which is used to supress bleed through of underlying content
	// layer2 is the overlay layer which has opacity and a wait cursor (by default)
	// layer3 is the message content that is displayed while blocking

	var lyr1 = ($.browser.msie || opts.forceIframe) 
		? $('<iframe class="blockUI" style="z-index:'+ (z++) +';display:none;border:none;margin:0;padding:0;position:absolute;width:100%;height:100%;top:0;left:0" src="'+opts.iframeSrc+'"></iframe>')
		: $('<div class="blockUI" style="display:none"></div>');
	var lyr2 = $('<div class="blockUI blockOverlay" style="z-index:'+ (z++) +';display:none;border:none;margin:0;padding:0;width:100%;height:100%;top:0;left:0"></div>');
	
	var lyr3, s;
	if (opts.theme && full) {
		s = '<div class="blockUI blockMsg blockPage ui-dialog ui-widget ui-corner-all" style="z-index:'+z+';display:none;position:fixed">' +
				'<div class="ui-widget-header ui-dialog-titlebar blockTitle">'+(opts.title || '&nbsp;')+'</div>' +
				'<div class="ui-widget-content ui-dialog-content"></div>' +
			'</div>';
	}
	else if (opts.theme) {
		s = '<div class="blockUI blockMsg blockElement ui-dialog ui-widget ui-corner-all" style="z-index:'+z+';display:none;position:absolute">' +
				'<div class="ui-widget-header ui-dialog-titlebar blockTitle">'+(opts.title || '&nbsp;')+'</div>' +
				'<div class="ui-widget-content ui-dialog-content"></div>' +
			'</div>';
	}
	else if (full) {
		s = '<div class="blockUI blockMsg blockPage" style="z-index:'+z+';display:none;position:fixed"></div>';
	}			
	else {
		s = '<div class="blockUI blockMsg blockElement" style="z-index:'+z+';display:none;position:absolute"></div>';
	}
	lyr3 = $(s);

	// if we have a message, style it
	if (msg) {
		if (opts.theme) {
			lyr3.css(themedCSS);
			lyr3.addClass('ui-widget-content');
		}
		else 
			lyr3.css(css);
	}

	// style the overlay
	if (!opts.applyPlatformOpacityRules || !($.browser.mozilla && /Linux/.test(navigator.platform)))
		lyr2.css(opts.overlayCSS);
	lyr2.css('position', full ? 'fixed' : 'absolute');

	// make iframe layer transparent in IE
	if ($.browser.msie || opts.forceIframe)
		lyr1.css('opacity',0.0);

	//$([lyr1[0],lyr2[0],lyr3[0]]).appendTo(full ? 'body' : el);
	var layers = [lyr1,lyr2,lyr3], $par = full ? $('body') : $(el);
	$.each(layers, function() {
		this.appendTo($par);
	});
	
	if (opts.theme && opts.draggable && $.fn.draggable) {
		lyr3.draggable({
			handle: '.ui-dialog-titlebar',
			cancel: 'li'
		});
	}

	// ie7 must use absolute positioning in quirks mode and to account for activex issues (when scrolling)
	var expr = setExpr && (!$.boxModel || $('object,embed', full ? null : el).length > 0);
	if (ie6 || expr) {
		// give body 100% height
		if (full && opts.allowBodyStretch && $.boxModel)
			$('html,body').css('height','100%');

		// fix ie6 issue when blocked element has a border width
		if ((ie6 || !$.boxModel) && !full) {
			var t = sz(el,'borderTopWidth'), l = sz(el,'borderLeftWidth');
			var fixT = t ? '(0 - '+t+')' : 0;
			var fixL = l ? '(0 - '+l+')' : 0;
		}

		// simulate fixed position
		$.each([lyr1,lyr2,lyr3], function(i,o) {
			var s = o[0].style;
			s.position = 'absolute';
			if (i < 2) {
				full ? s.setExpression('height','Math.max(document.body.scrollHeight, document.body.offsetHeight) - (jQuery.boxModel?0:'+opts.quirksmodeOffsetHack+') + "px"')
					 : s.setExpression('height','this.parentNode.offsetHeight + "px"');
				full ? s.setExpression('width','jQuery.boxModel && document.documentElement.clientWidth || document.body.clientWidth + "px"')
					 : s.setExpression('width','this.parentNode.offsetWidth + "px"');
				if (fixL) s.setExpression('left', fixL);
				if (fixT) s.setExpression('top', fixT);
			}
			else if (opts.centerY) {
				if (full) s.setExpression('top','(document.documentElement.clientHeight || document.body.clientHeight) / 2 - (this.offsetHeight / 2) + (blah = document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop) + "px"');
				s.marginTop = 0;
			}
			else if (!opts.centerY && full) {
				var top = (opts.css && opts.css.top) ? parseInt(opts.css.top) : 0;
				var expression = '((document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop) + '+top+') + "px"';
				s.setExpression('top',expression);
			}
		});
	}

	// show the message
	if (msg) {
		if (opts.theme)
			lyr3.find('.ui-widget-content').append(msg);
		else
			lyr3.append(msg);
		if (msg.jquery || msg.nodeType)
			$(msg).show();
	}

	if (($.browser.msie || opts.forceIframe) && opts.showOverlay)
		lyr1.show(); // opacity is zero
	if (opts.fadeIn) {
		var cb = opts.onBlock ? opts.onBlock : noOp;
		var cb1 = (opts.showOverlay && !msg) ? cb : noOp;
		var cb2 = msg ? cb : noOp;
		if (opts.showOverlay)
			lyr2._fadeIn(opts.fadeIn, cb1);
		if (msg)
			lyr3._fadeIn(opts.fadeIn, cb2);
	}
	else {
		if (opts.showOverlay)
			lyr2.show();
		if (msg)
			lyr3.show();
		if (opts.onBlock)
			opts.onBlock();
	}

	// bind key and mouse events
	bind(1, el, opts);

	if (full) {
		pageBlock = lyr3[0];
		pageBlockEls = $(':input:enabled:visible',pageBlock);
		if (opts.focusInput)
			setTimeout(focus, 20);
	}
	else
		center(lyr3[0], opts.centerX, opts.centerY);

	if (opts.timeout) {
		// auto-unblock
		var to = setTimeout(function() {
			full ? $.unblockUI(opts) : $(el).unblock(opts);
		}, opts.timeout);
		$(el).data('blockUI.timeout', to);
	}
};

// remove the block
function remove(el, opts) {
	var full = (el == window);
	var $el = $(el);
	var data = $el.data('blockUI.history');
	var to = $el.data('blockUI.timeout');
	if (to) {
		clearTimeout(to);
		$el.removeData('blockUI.timeout');
	}
	opts = $.extend({}, $.blockUI.defaults, opts || {});
	bind(0, el, opts); // unbind events
	
	var els;
	if (full) // crazy selector to handle odd field errors in ie6/7
		els = $('body').children().filter('.blockUI').add('body > .blockUI');
	else
		els = $('.blockUI', el);

	if (full)
		pageBlock = pageBlockEls = null;

	if (opts.fadeOut) {
		els.fadeOut(opts.fadeOut);
		setTimeout(function() { reset(els,data,opts,el); }, opts.fadeOut);
	}
	else
		reset(els, data, opts, el);
};

// move blocking element back into the DOM where it started
function reset(els,data,opts,el) {
	els.each(function(i,o) {
		// remove via DOM calls so we don't lose event handlers
		if (this.parentNode)
			this.parentNode.removeChild(this);
	});

	if (data && data.el) {
		data.el.style.display = data.display;
		data.el.style.position = data.position;
		if (data.parent)
			data.parent.appendChild(data.el);
		$(el).removeData('blockUI.history');
	}

	if (typeof opts.onUnblock == 'function')
		opts.onUnblock(el,opts);
};

// bind/unbind the handler
function bind(b, el, opts) {
	var full = el == window, $el = $(el);

	// don't bother unbinding if there is nothing to unbind
	if (!b && (full && !pageBlock || !full && !$el.data('blockUI.isBlocked')))
		return;
	if (!full)
		$el.data('blockUI.isBlocked', b);

	// don't bind events when overlay is not in use or if bindEvents is false
	if (!opts.bindEvents || (b && !opts.showOverlay)) 
		return;

	// bind anchors and inputs for mouse and key events
	var events = 'mousedown mouseup keydown keypress';
	b ? $(document).bind(events, opts, handler) : $(document).unbind(events, handler);

// former impl...
//	   var $e = $('a,:input');
//	   b ? $e.bind(events, opts, handler) : $e.unbind(events, handler);
};

// event handler to suppress keyboard/mouse events when blocking
function handler(e) {
	// allow tab navigation (conditionally)
	if (e.keyCode && e.keyCode == 9) {
		if (pageBlock && e.data.constrainTabKey) {
			var els = pageBlockEls;
			var fwd = !e.shiftKey && e.target == els[els.length-1];
			var back = e.shiftKey && e.target == els[0];
			if (fwd || back) {
				setTimeout(function(){focus(back)},10);
				return false;
			}
		}
	}
	// allow events within the message content
	if ($(e.target).parents('div.blockMsg').length > 0)
		return true;

	// allow events for content that is not being blocked
	return $(e.target).parents().children().filter('div.blockUI').length == 0;
};

function focus(back) {
	if (!pageBlockEls)
		return;
	var e = pageBlockEls[back===true ? pageBlockEls.length-1 : 0];
	if (e)
		e.focus();
};

function center(el, x, y) {
	var p = el.parentNode, s = el.style;
	var l = ((p.offsetWidth - el.offsetWidth)/2) - sz(p,'borderLeftWidth');
	var t = ((p.offsetHeight - el.offsetHeight)/2) - sz(p,'borderTopWidth');
	if (x) s.left = l > 0 ? (l+'px') : '0';
	if (y) s.top  = t > 0 ? (t+'px') : '0';
};

function sz(el, p) {
	return parseInt($.css(el,p))||0;
};

})(jQuery);


/**
 * Cookie plugin
 *
 * Copyright (c) 2006 Klaus Hartl (stilbuero.de)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 */

/**
 * Create a cookie with the given name and value and other optional parameters.
 *
 * @example $.cookie('the_cookie', 'the_value');
 * @desc Set the value of a cookie.
 * @example $.cookie('the_cookie', 'the_value', { expires: 7, path: '/', domain: 'jquery.com', secure: true });
 * @desc Create a cookie with all available options.
 * @example $.cookie('the_cookie', 'the_value');
 * @desc Create a session cookie.
 * @example $.cookie('the_cookie', null);
 * @desc Delete a cookie by passing null as value. Keep in mind that you have to use the same path and domain
 *       used when the cookie was set.
 *
 * @param String name The name of the cookie.
 * @param String value The value of the cookie.
 * @param Object options An object literal containing key/value pairs to provide optional cookie attributes.
 * @option Number|Date expires Either an integer specifying the expiration date from now on in days or a Date object.
 *                             If a negative value is specified (e.g. a date in the past), the cookie will be deleted.
 *                             If set to null or omitted, the cookie will be a session cookie and will not be retained
 *                             when the the browser exits.
 * @option String path The value of the path atribute of the cookie (default: path of page that created the cookie).
 * @option String domain The value of the domain attribute of the cookie (default: domain of page that created the cookie).
 * @option Boolean secure If true, the secure attribute of the cookie will be set and the cookie transmission will
 *                        require a secure protocol (like HTTPS).
 * @type undefined
 *
 * @name $.cookie
 * @cat Plugins/Cookie
 * @author Klaus Hartl/klaus.hartl@stilbuero.de
 */

/**
 * Get the value of a cookie with the given name.
 *
 * @example $.cookie('the_cookie');
 * @desc Get the value of a cookie.
 *
 * @param String name The name of the cookie.
 * @return The value of the cookie.
 * @type String
 *
 * @name $.cookie
 * @cat Plugins/Cookie
 * @author Klaus Hartl/klaus.hartl@stilbuero.de
 */
jQuery.cookie = function(name, value, options) {
    if (typeof value != 'undefined') { // name and value given, set cookie
        options = options || {};
        if (value === null) {
            value = '';
            options.expires = -1;
        }
        var expires = '';
        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if (typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
        }
        // CAUTION: Needed to parenthesize options.path and options.domain
        // in the following expressions, otherwise they evaluate to undefined
        // in the packed version for some reason...
        var path = options.path ? '; path=' + (options.path) : '';
        var domain = options.domain ? '; domain=' + (options.domain) : '';
        var secure = options.secure ? '; secure' : '';
        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
    } else { // only name given, get cookie
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                // Does this cookie string begin with the name we want?
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
};

/* 
* jqGrid  3.8 - jQuery Grid 
* Copyright (c) 2008, Tony Tomov, tony@trirand.com 
* Dual licensed under the MIT and GPL licenses 
* http://www.opensource.org/licenses/mit-license.php 
* http://www.gnu.org/licenses/gpl-2.0.html 
* Date:2010-09-21 
* Modules: grid.base.js; jquery.fmatter.js; grid.custom.js; grid.common.js; grid.formedit.js; jquery.searchFilter.js; grid.inlinedit.js; grid.celledit.js; jqModal.js; jqDnR.js; grid.subgrid.js; grid.grouping.js; grid.treegrid.js; grid.import.js; JsonXml.js; grid.setcolumns.js; grid.postext.js; grid.tbltogrid.js; grid.jqueryui.js; 
*/
(function(b){b.jgrid=b.jgrid||{};b.extend(b.jgrid,{htmlDecode:function(f){if(f=="&nbsp;"||f=="&#160;"||f.length==1&&f.charCodeAt(0)==160)return"";return!f?f:String(f).replace(/&amp;/g,"&").replace(/&gt;/g,">").replace(/&lt;/g,"<").replace(/&quot;/g,'"')},htmlEncode:function(f){return!f?f:String(f).replace(/&/g,"&amp;").replace(/>/g,"&gt;").replace(/</g,"&lt;").replace(/\"/g,"&quot;")},format:function(f){var j=b.makeArray(arguments).slice(1);if(f===undefined)f="";return f.replace(/\{(\d+)\}/g,function(i,
c){return j[c]})},getCellIndex:function(f){f=b(f);if(f.is("tr"))return-1;f=(!f.is("td")&&!f.is("th")?f.closest("td,th"):f)[0];if(b.browser.msie)return b.inArray(f,f.parentNode.cells);return f.cellIndex},stripHtml:function(f){f+="";var j=/<("[^"]*"|'[^']*'|[^'">])*>/gi;if(f)return(f=f.replace(j,""))&&f!=="&nbsp;"&&f!=="&#160;"?f.replace(/\"/g,"'"):"";else return f},stringToDoc:function(f){var j;if(typeof f!=="string")return f;try{j=(new DOMParser).parseFromString(f,"text/xml")}catch(i){j=new ActiveXObject("Microsoft.XMLDOM");
j.async=false;j.loadXML(f)}return j&&j.documentElement&&j.documentElement.tagName!="parsererror"?j:null},parse:function(f){f=f;if(f.substr(0,9)=="while(1);")f=f.substr(9);if(f.substr(0,2)=="/*")f=f.substr(2,f.length-4);f||(f="{}");return b.jgrid.useJSON===true&&typeof JSON==="object"&&typeof JSON.parse==="function"?JSON.parse(f):eval("("+f+")")},parseDate:function(f,j){var i={m:1,d:1,y:1970,h:0,i:0,s:0},c,e,k;if(j&&j!==null&&j!==undefined){j=b.trim(j);j=j.split(/[\\\/:_;.\t\T\s-]/);f=f.split(/[\\\/:_;.\t\T\s-]/);
var l=b.jgrid.formatter.date.monthNames,a=b.jgrid.formatter.date.AmPm,r=function(u,B){if(u===0){if(B==12)B=0}else if(B!=12)B+=12;return B};c=0;for(e=f.length;c<e;c++){if(f[c]=="M"){k=b.inArray(j[c],l);if(k!==-1&&k<12)j[c]=k+1}if(f[c]=="F"){k=b.inArray(j[c],l);if(k!==-1&&k>11)j[c]=k+1-12}if(f[c]=="a"){k=b.inArray(j[c],a);if(k!==-1&&k<2&&j[c]==a[k]){j[c]=k;i.h=r(j[c],i.h)}}if(f[c]=="A"){k=b.inArray(j[c],a);if(k!==-1&&k>1&&j[c]==a[k]){j[c]=k-2;i.h=r(j[c],i.h)}}if(j[c]!==undefined)i[f[c].toLowerCase()]=
parseInt(j[c],10)}i.m=parseInt(i.m,10)-1;f=i.y;if(f>=70&&f<=99)i.y=1900+i.y;else if(f>=0&&f<=69)i.y=2E3+i.y}return new Date(i.y,i.m,i.d,i.h,i.i,i.s,0)},jqID:function(f){f+="";return f.replace(/([\.\:\[\]])/g,"\\$1")},getAccessor:function(f,j){var i,c,e,k;if(typeof j==="function")return j(f);i=f[j];if(i===undefined)try{if(typeof j==="string")e=j.split(".");if(k=e.length)for(i=f;i&&k--;){c=e.shift();i=i[c]}}catch(l){}return i},ajaxOptions:{},from:function(f){return new (function(j,i){if(typeof j=="string")j=
b.data(j);var c=this,e=j,k=true,l=false,a=i,r=/[\$,%]/g,u=null,B=null,G=false,Q="",J=[],M=true;if(typeof j=="object"&&j.push){if(j.length>0)M=typeof j[0]!="object"?false:true}else throw"data provides is not an array";this._hasData=function(){return e===null?false:e.length===0?false:true};this._getStr=function(n){var m=[];l&&m.push("jQuery.trim(");m.push("String("+n+")");l&&m.push(")");k||m.push(".toLowerCase()");return m.join("")};this._strComp=function(n){return typeof n=="string"?".toString()":
""};this._group=function(n,m){return{field:n.toString(),unique:m,items:[]}};this._toStr=function(n){if(l)n=b.trim(n);k||(n=n.toLowerCase());return n=n.toString().replace(new RegExp('\\"',"g"),'\\"')};this._funcLoop=function(n){var m=[];b.each(e,function(p,A){m.push(n(A))});return m};this._append=function(n){if(a===null)a="";else a+=Q===""?" && ":Q;if(G)a+="!";a+="("+n+")";G=false;Q=""};this._setCommand=function(n,m){u=n;B=m};this._resetNegate=function(){G=false};this._repeatCommand=function(n,m){if(u===
null)return c;if(n!=null&&m!=null)return u(n,m);if(B===null)return u(n);if(!M)return u(n);return u(B,n)};this._equals=function(n,m){return c._compare(n,m,1)===0};this._compare=function(n,m,p){if(p===undefined)p=1;if(n===undefined)n=null;if(m===undefined)m=null;if(n===null&&m===null)return 0;if(n===null&&m!==null)return 1;if(n!==null&&m===null)return-1;if(!k){n=n.toLowerCase();m=m.toLowerCase()}if(n<m)return-p;if(n>m)return p;return 0};this._performSort=function(){if(J.length!==0)e=c._doSort(e,0)};
this._doSort=function(n,m){var p=J[m].by,A=J[m].dir,t=J[m].type,H=J[m].datefmt;if(m==J.length-1)return c._getOrder(n,p,A,t,H);m++;n=c._getGroup(n,p,A,t,H);p=[];for(A=0;A<n.length;A++){t=c._doSort(n[A].items,m);for(H=0;H<t.length;H++)p.push(t[H])}return p};this._getOrder=function(n,m,p,A,t){var H=[],S=[],Y=p=="a"?1:-1,O,fa;if(A===undefined)A="text";fa=A=="float"||A=="number"||A=="currency"||A=="numeric"?function(P){P=parseFloat(String(P).replace(r,""));return isNaN(P)?0:P}:A=="int"||A=="integer"?function(P){return P?
parseFloat(String(P).replace(r,"")):0}:A=="date"||A=="datetime"?function(P){return b.jgrid.parseDate(t,P).getTime()}:b.isFunction(A)?A:function(P){P||(P="");return b.trim(String(P).toUpperCase())};b.each(n,function(P,ba){O=b.jgrid.getAccessor(ba,m);if(O===undefined)O="";O=fa(O,ba);S.push({vSort:O,index:P})});S.sort(function(P,ba){P=P.vSort;ba=ba.vSort;return c._compare(P,ba,Y)});A=0;for(var ca=n.length;A<ca;){p=S[A].index;H.push(n[p]);A++}return H};this._getGroup=function(n,m,p,A,t){var H=[],S=null,
Y=null,O;b.each(c._getOrder(n,m,p,A,t),function(fa,ca){O=b.jgrid.getAccessor(ca,m);if(O===undefined)O="";if(!c._equals(Y,O)){Y=O;S!=null&&H.push(S);S=c._group(m,O)}S.items.push(ca)});S!=null&&H.push(S);return H};this.ignoreCase=function(){k=false;return c};this.useCase=function(){k=true;return c};this.trim=function(){l=true;return c};this.noTrim=function(){l=false;return c};this.combine=function(n){var m=b.from(e);k||m.ignoreCase();l&&m.trim();n=n(m).showQuery();c._append(n);return c};this.execute=
function(){var n=a,m=[];if(n===null)return c;b.each(e,function(){eval(n)&&m.push(this)});e=m;return c};this.data=function(){return e};this.select=function(n){c._performSort();if(!c._hasData())return[];c.execute();if(b.isFunction(n)){var m=[];b.each(e,function(p,A){m.push(n(A))});return m}return e};this.hasMatch=function(){if(!c._hasData())return false;c.execute();return e.length>0};this.showQuery=function(n){var m=a;if(m===null)m="no query found";if(b.isFunction(n)){n(m);return c}return m};this.andNot=
function(n,m,p){G=!G;return c.and(n,m,p)};this.orNot=function(n,m,p){G=!G;return c.or(n,m,p)};this.not=function(n,m,p){return c.andNot(n,m,p)};this.and=function(n,m,p){Q=" && ";if(n===undefined)return c;return c._repeatCommand(n,m,p)};this.or=function(n,m,p){Q=" || ";if(n===undefined)return c;return c._repeatCommand(n,m,p)};this.isNot=function(n){G=!G;return c.is(n)};this.is=function(n){c._append("this."+n);c._resetNegate();return c};this._compareValues=function(n,m,p,A,t){var H;H=M?"this."+m:"this";
if(p===undefined)p=null;p=p===null?m:p;switch(t.stype===undefined?"text":t.stype){case "int":case "integer":p=isNaN(Number(p))?"0":p;H="parseInt("+H+",10)";p="parseInt("+p+",10)";break;case "float":case "number":case "numeric":p=String(p).replace(r,"");p=isNaN(Number(p))?"0":p;H="parseFloat("+H+")";p="parseFloat("+p+")";break;case "date":case "datetime":p=String(b.jgrid.parseDate(t.newfmt||"Y-m-d",p).getTime());H='jQuery.jgrid.parseDate("'+t.srcfmt+'",'+H+").getTime()";break;default:H=c._getStr(H);
p=c._getStr('"'+c._toStr(p)+'"')}c._append(H+" "+A+" "+p);c._setCommand(n,m);c._resetNegate();return c};this.equals=function(n,m,p){return c._compareValues(c.equals,n,m,"==",p)};this.greater=function(n,m,p){return c._compareValues(c.greater,n,m,">",p)};this.less=function(n,m,p){return c._compareValues(c.less,n,m,"<",p)};this.greaterOrEquals=function(n,m,p){return c._compareValues(c.greaterOrEquals,n,m,">=",p)};this.lessOrEquals=function(n,m,p){return c._compareValues(c.lessOrEquals,n,m,"<=",p)};this.startsWith=
function(n,m){var p=m===undefined||m===null?n:m;p=l?b.trim(p.toString()).length:p.toString().length;if(M)c._append(c._getStr("this."+n)+".substr(0,"+p+") == "+c._getStr('"'+c._toStr(m)+'"'));else{p=l?b.trim(m.toString()).length:m.toString().length;c._append(c._getStr("this")+".substr(0,"+p+") == "+c._getStr('"'+c._toStr(n)+'"'))}c._setCommand(c.startsWith,n);c._resetNegate();return c};this.endsWith=function(n,m){var p=m===undefined||m===null?n:m;p=l?b.trim(p.toString()).length:p.toString().length;
M?c._append(c._getStr("this."+n)+".substr("+c._getStr("this."+n)+".length-"+p+","+p+') == "'+c._toStr(m)+'"'):c._append(c._getStr("this")+".substr("+c._getStr("this")+'.length-"'+c._toStr(n)+'".length,"'+c._toStr(n)+'".length) == "'+c._toStr(n)+'"');c._setCommand(c.endsWith,n);c._resetNegate();return c};this.contains=function(n,m){M?c._append(c._getStr("this."+n)+'.indexOf("'+c._toStr(m)+'",0) > -1'):c._append(c._getStr("this")+'.indexOf("'+c._toStr(n)+'",0) > -1');c._setCommand(c.contains,n);c._resetNegate();
return c};this.groupBy=function(n,m,p,A){if(!c._hasData())return null;return c._getGroup(e,n,m,p,A)};this.orderBy=function(n,m,p,A){m=m===undefined||m===null?"a":b.trim(m.toString().toLowerCase());if(p===null||p===undefined)p="text";if(A===null||A===undefined)A="Y-m-d";if(m=="desc"||m=="descending")m="d";if(m=="asc"||m=="ascending")m="a";J.push({by:n,dir:m,type:p,datefmt:A});return c};return c})(f,null)},extend:function(f){b.extend(b.fn.jqGrid,f);this.no_legacy_api||b.fn.extend(f)}});b.fn.jqGrid=
function(f){if(typeof f=="string"){var j=b.jgrid.getAccessor(b.fn.jqGrid,f);if(!j)throw"jqGrid - No such method: "+f;var i=b.makeArray(arguments).slice(1);return j.apply(this,i)}return this.each(function(){if(!this.grid){var c=b.extend(true,{url:"",height:150,page:1,rowNum:20,rowTotal:null,records:0,pager:"",pgbuttons:true,pginput:true,colModel:[],rowList:[],colNames:[],sortorder:"asc",sortname:"",datatype:"xml",mtype:"GET",altRows:false,selarrrow:[],savedRow:[],shrinkToFit:true,xmlReader:{},jsonReader:{},
subGrid:false,subGridModel:[],reccount:0,lastpage:0,lastsort:0,selrow:null,beforeSelectRow:null,onSelectRow:null,onSortCol:null,ondblClickRow:null,onRightClickRow:null,onPaging:null,onSelectAll:null,loadComplete:null,gridComplete:null,loadError:null,loadBeforeSend:null,afterInsertRow:null,beforeRequest:null,onHeaderClick:null,viewrecords:false,loadonce:false,multiselect:false,multikey:false,editurl:null,search:false,caption:"",hidegrid:true,hiddengrid:false,postData:{},userData:{},treeGrid:false,
treeGridModel:"nested",treeReader:{},treeANode:-1,ExpandColumn:null,tree_root_level:0,prmNames:{page:"page",rows:"rows",sort:"sidx",order:"sord",search:"_search",nd:"nd",id:"id",oper:"oper",editoper:"edit",addoper:"add",deloper:"del",subgridid:"id",npage:null,totalrows:"totalrows"},forceFit:false,gridstate:"visible",cellEdit:false,cellsubmit:"remote",nv:0,loadui:"enable",toolbar:[false,""],scroll:false,multiboxonly:false,deselectAfterSort:true,scrollrows:false,autowidth:false,scrollOffset:18,cellLayout:5,
subGridWidth:20,multiselectWidth:20,gridview:false,rownumWidth:25,rownumbers:false,pagerpos:"center",recordpos:"right",footerrow:false,userDataOnFooter:false,hoverrows:true,altclass:"ui-priority-secondary",viewsortcols:[false,"vertical",true],resizeclass:"",autoencode:false,remapColumns:[],ajaxGridOptions:{},direction:"ltr",toppager:false,headertitles:false,scrollTimeout:40,data:[],_index:{},grouping:false,groupingView:{groupField:[],groupOrder:[],groupText:[],groupColumnShow:[],groupSummary:[],showSummaryOnHide:false,
sortitems:[],sortnames:[],groupDataSorted:false,summary:[],summaryval:[],plusicon:"ui-icon-circlesmall-plus",minusicon:"ui-icon-circlesmall-minus"},ignoreCase:false},b.jgrid.defaults,f||{}),e={headers:[],cols:[],footers:[],dragStart:function(d,g,h){this.resizing={idx:d,startX:g.clientX,sOL:h[0]};this.hDiv.style.cursor="col-resize";this.curGbox=b("#rs_m"+c.id,"#gbox_"+c.id);this.curGbox.css({display:"block",left:h[0],top:h[1],height:h[2]});b.isFunction(c.resizeStart)&&c.resizeStart.call(this,g,d);
document.onselectstart=function(){return false}},dragMove:function(d){if(this.resizing){var g=d.clientX-this.resizing.startX;d=this.headers[this.resizing.idx];var h=c.direction==="ltr"?d.width+g:d.width-g,q;if(h>33){this.curGbox.css({left:this.resizing.sOL+g});if(c.forceFit===true){q=this.headers[this.resizing.idx+c.nv];g=c.direction==="ltr"?q.width-g:q.width+g;if(g>33){d.newWidth=h;q.newWidth=g}}else{this.newWidth=c.direction==="ltr"?c.tblwidth+g:c.tblwidth-g;d.newWidth=h}}}},dragEnd:function(){this.hDiv.style.cursor=
"default";if(this.resizing){var d=this.resizing.idx,g=this.headers[d].newWidth||this.headers[d].width;g=parseInt(g,10);this.resizing=false;b("#rs_m"+c.id).css("display","none");c.colModel[d].width=g;this.headers[d].width=g;this.headers[d].el.style.width=g+"px";this.cols[d].style.width=g+"px";if(this.footers.length>0)this.footers[d].style.width=g+"px";if(c.forceFit===true){g=this.headers[d+c.nv].newWidth||this.headers[d+c.nv].width;this.headers[d+c.nv].width=g;this.headers[d+c.nv].el.style.width=g+
"px";this.cols[d+c.nv].style.width=g+"px";if(this.footers.length>0)this.footers[d+c.nv].style.width=g+"px";c.colModel[d+c.nv].width=g}else{c.tblwidth=this.newWidth||c.tblwidth;b("table:first",this.bDiv).css("width",c.tblwidth+"px");b("table:first",this.hDiv).css("width",c.tblwidth+"px");this.hDiv.scrollLeft=this.bDiv.scrollLeft;if(c.footerrow){b("table:first",this.sDiv).css("width",c.tblwidth+"px");this.sDiv.scrollLeft=this.bDiv.scrollLeft}}b.isFunction(c.resizeStop)&&c.resizeStop.call(this,g,d)}this.curGbox=
null;document.onselectstart=function(){return true}},populateVisible:function(){e.timer&&clearTimeout(e.timer);e.timer=null;var d=b(e.bDiv).height();if(d){var g=b("table:first",e.bDiv),h=b("> tbody > tr:gt(0):visible:first",g).outerHeight()||e.prevRowHeight;if(h){e.prevRowHeight=h;var q=c.rowNum,o=e.scrollTop=e.bDiv.scrollTop,x=Math.round(g.position().top)-o,w=x+g.height();h=h*q;var C,D,s;if(w<d&&x<=0&&(c.lastpage===undefined||parseInt((w+o+h-1)/h,10)<=c.lastpage)){D=parseInt((d-w+h-1)/h,10);if(w>=
0||D<2||c.scroll===true){C=Math.round((w+o)/h)+1;x=-1}else x=1}if(x>0){C=parseInt(o/h,10)+1;D=parseInt((o+d)/h,10)+2-C;s=true}if(D)if(!(c.lastpage&&C>c.lastpage||c.lastpage==1))if(e.hDiv.loading)e.timer=setTimeout(e.populateVisible,c.scrollTimeout);else{c.page=C;if(s){e.selectionPreserver(g[0]);e.emptyRows(e.bDiv,false)}e.populate(D)}}}},scrollGrid:function(){if(c.scroll){var d=e.bDiv.scrollTop;if(e.scrollTop===undefined)e.scrollTop=0;if(d!=e.scrollTop){e.scrollTop=d;e.timer&&clearTimeout(e.timer);
e.timer=setTimeout(e.populateVisible,c.scrollTimeout)}}e.hDiv.scrollLeft=e.bDiv.scrollLeft;if(c.footerrow)e.sDiv.scrollLeft=e.bDiv.scrollLeft},selectionPreserver:function(d){var g=d.p,h=g.selrow,q=g.selarrrow?b.makeArray(g.selarrrow):null,o=d.grid.bDiv.scrollLeft,x=g.gridComplete;g.gridComplete=function(){g.selrow=null;g.selarrrow=[];if(g.multiselect&&q&&q.length>0)for(var w=0;w<q.length;w++)q[w]!=h&&b(d).jqGrid("setSelection",q[w],false);h&&b(d).jqGrid("setSelection",h,false);d.grid.bDiv.scrollLeft=
o;g.gridComplete=x;g.gridComplete&&x()}}};if(this.tagName!="TABLE")alert("Element is not a table");else{b(this).empty();this.p=c;var k,l,a;if(this.p.colNames.length===0)for(k=0;k<this.p.colModel.length;k++)this.p.colNames[k]=this.p.colModel[k].label||this.p.colModel[k].name;if(this.p.colNames.length!==this.p.colModel.length)alert(b.jgrid.errors.model);else{var r=b("<div class='ui-jqgrid-view'></div>"),u,B=b.browser.msie?true:false,G=b.browser.safari?true:false;a=this;a.p.direction=b.trim(a.p.direction.toLowerCase());
if(b.inArray(a.p.direction,["ltr","rtl"])==-1)a.p.direction="ltr";l=a.p.direction;b(r).insertBefore(this);b(this).appendTo(r).removeClass("scroll");var Q=b("<div class='ui-jqgrid ui-widget ui-widget-content ui-corner-all'></div>");b(Q).insertBefore(r).attr({id:"gbox_"+this.id,dir:l});b(r).appendTo(Q).attr("id","gview_"+this.id);u=B&&b.browser.version<=6?'<iframe style="display:block;position:absolute;z-index:-1;filter:Alpha(Opacity=\'0\');" src="javascript:false;"></iframe>':"";b("<div class='ui-widget-overlay jqgrid-overlay' id='lui_"+
this.id+"'></div>").append(u).insertBefore(r);b("<div class='loading ui-state-default ui-state-active' id='load_"+this.id+"'>"+this.p.loadtext+"</div>").insertBefore(r);b(this).attr({cellSpacing:"0",cellPadding:"0",border:"0",role:"grid","aria-multiselectable":!!this.p.multiselect,"aria-labelledby":"gbox_"+this.id});var J=function(d,g){d=parseInt(d,10);return isNaN(d)?g?g:0:d},M=function(d,g,h){var q=a.p.colModel[d],o=q.align,x='style="',w=q.classes,C=q.name;if(o)x+="text-align:"+o+";";if(q.hidden===
true)x+="display:none;";if(g===0)x+="width: "+e.headers[d].width+"px;";x+='"'+(w!==undefined?' class="'+w+'"':"")+(q.title&&h?' title="'+b.jgrid.stripHtml(h)+'"':"");x+=' aria-describedby="'+a.p.id+"_"+C+'"';return x},n=function(d){return d===undefined||d===null||d===""?"&#160;":a.p.autoencode?b.jgrid.htmlEncode(d):d+""},m=function(d,g,h,q,o){h=a.p.colModel[h];if(typeof h.formatter!=="undefined"){d={rowId:d,colModel:h,gid:a.p.id};g=b.isFunction(h.formatter)?h.formatter.call(a,g,d,q,o):b.fmatter?b.fn.fmatter(h.formatter,
g,d,q,o):n(g)}else g=n(g);return g},p=function(d,g,h,q,o){d=m(d,g,h,o,"add");return'<td role="gridcell" '+M(h,q,d)+">"+d+"</td>"},A=function(d,g,h){d='<input role="checkbox" type="checkbox" id="jqg_'+a.p.id+"_"+d+'" class="cbox" name="jqg_'+a.p.id+"_"+d+'"/>';g=M(g,h,"");return'<td role="gridcell" aria-describedby="'+a.p.id+'_cb" '+g+">"+d+"</td>"},t=function(d,g,h,q){h=(parseInt(h,10)-1)*parseInt(q,10)+1+g;d=M(d,g,"");return'<td role="gridcell" aria-describedby="'+a.p.id+'_rn" class="ui-state-default jqgrid-rownum" '+
d+">"+h+"</td>"},H=function(d){var g,h=[],q=0,o;for(o=0;o<a.p.colModel.length;o++){g=a.p.colModel[o];if(g.name!=="cb"&&g.name!=="subgrid"&&g.name!=="rn"){h[q]=d=="local"?g.name:d=="xml"?g.xmlmap||g.name:g.jsonmap||g.name;q++}}return h},S=function(d){var g=a.p.remapColumns;if(!g||!g.length)g=b.map(a.p.colModel,function(h,q){return q});if(d)g=b.map(g,function(h){return h<d?null:h-d});return g},Y=function(d,g){if(a.p.deepempty)b("#"+a.p.id+" tbody:first tr:gt(0)").remove();else{var h=b("#"+a.p.id+" tbody:first tr:first")[0];
b("#"+a.p.id+" tbody:first").empty().append(h)}if(g&&a.p.scroll){b(">div:first",d).css({height:"auto"}).children("div:first").css({height:0,display:"none"});d.scrollTop=0}},O=function(){var d=a.p.data.length,g,h,q;g=a.p.rownumbers===true?1:0;h=a.p.multiselect===true?1:0;q=a.p.subGrid===true?1:0;g=a.p.keyIndex===false||a.p.loadonce===true?a.p.localReader.id:a.p.colModel[a.p.keyIndex+h+q+g].name;for(h=0;h<d;h++){q=b.jgrid.getAccessor(a.p.data[h],g);a.p._index[q]=h}},fa=function(d,g,h,q,o){var x=new Date,
w=a.p.datatype!="local"&&a.p.loadonce||a.p.datatype=="xmlstring",C,D=a.p.datatype=="local"?"local":"xml";if(w){a.p.data=[];a.p._index={};a.p.localReader.id=C="_id_"}a.p.reccount=0;if(b.isXMLDoc(d)){if(a.p.treeANode===-1&&!a.p.scroll){Y(g,false);h=1}else h=h>1?h:1;var s,v=0,y,E,I=0,F=0,K=0,z,U=[],V,T={},N,L,X=[],oa=a.p.altRows===true?" "+a.p.altclass:"";a.p.xmlReader.repeatitems||(U=H(D));z=a.p.keyIndex===false?a.p.xmlReader.id:a.p.keyIndex;if(U.length>0&&!isNaN(z)){if(a.p.remapColumns&&a.p.remapColumns.length)z=
b.inArray(z,a.p.remapColumns);z=U[z]}D=(z+"").indexOf("[")===-1?U.length?function(da,Z){return b(z,da).text()||Z}:function(da,Z){return b(a.p.xmlReader.cell,da).eq(z).text()||Z}:function(da,Z){return da.getAttribute(z.replace(/[\[\]]/g,""))||Z};a.p.userData={};b(a.p.xmlReader.page,d).each(function(){a.p.page=this.textContent||this.text||0});b(a.p.xmlReader.total,d).each(function(){a.p.lastpage=this.textContent||this.text;if(a.p.lastpage===undefined)a.p.lastpage=1});b(a.p.xmlReader.records,d).each(function(){a.p.records=
this.textContent||this.text||0});b(a.p.xmlReader.userdata,d).each(function(){a.p.userData[this.getAttribute("name")]=this.textContent||this.text});d=b(a.p.xmlReader.root+" "+a.p.xmlReader.row,d);var ga=d.length,$=0;if(d&&ga){var ha=parseInt(a.p.rowNum,10),ra=a.p.scroll?(parseInt(a.p.page,10)-1)*ha+1:1;if(o)ha*=o+1;o=b.isFunction(a.p.afterInsertRow);var ia={},xa="";if(a.p.grouping&&a.p.groupingView.groupCollapse===true)xa=' style="display:none;"';for(;$<ga;){N=d[$];L=D(N,ra+$);s=h===0?0:h+1;s=(s+$)%
2==1?oa:"";X.push("<tr"+xa+' id="'+L+'" role="row" class ="ui-widget-content jqgrow ui-row-'+a.p.direction+""+s+'">');if(a.p.rownumbers===true){X.push(t(0,$,a.p.page,a.p.rowNum));K=1}if(a.p.multiselect===true){X.push(A(L,K,$));I=1}if(a.p.subGrid===true){X.push(b(a).jqGrid("addSubGridCell",I+K,$+h));F=1}if(a.p.xmlReader.repeatitems){V||(V=S(I+F+K));var Ba=b(a.p.xmlReader.cell,N);b.each(V,function(da){var Z=Ba[this];if(!Z)return false;y=Z.textContent||Z.text;T[a.p.colModel[da+I+F+K].name]=y;X.push(p(L,
y,da+I+F+K,$+h,N))})}else for(s=0;s<U.length;s++){y=b(U[s],N).text();T[a.p.colModel[s+I+F+K].name]=y;X.push(p(L,y,s+I+F+K,$+h,N))}X.push("</tr>");if(a.p.grouping){s=a.p.groupingView.groupField.length;E=[];for(var ya=0;ya<s;ya++)E.push(T[a.p.groupingView.groupField[ya]]);ia=b(a).jqGrid("groupingPrepare",X,E,ia,T);X=[]}if(w){T[C]=L;a.p.data.push(T)}if(a.p.gridview===false){if(a.p.treeGrid===true){s=a.p.treeANode>=-1?a.p.treeANode:0;E=b(X.join(""))[0];b(a.rows[$+s+h]).after(E);try{b(a).jqGrid("setTreeNode",
T,E)}catch(Ia){}}else b("tbody:first",g).append(X.join(""));if(a.p.subGrid===true)try{b(a).jqGrid("addSubGrid",a.rows[a.rows.length-1],I+K)}catch(Ja){}o&&a.p.afterInsertRow.call(a,L,T,N);X=[]}T={};v++;$++;if(v==ha)break}}if(a.p.gridview===true)if(a.p.grouping&&V){b(a).jqGrid("groupingRender",ia,a.p.colModel.length);ia=null}else b("tbody:first",g).append(X.join(""));a.p.totaltime=new Date-x;if(v>0)if(a.p.records===0)a.p.records=ga;X=null;if(!a.p.treeGrid&&!a.p.scroll)a.grid.bDiv.scrollTop=0;a.p.reccount=
v;a.p.treeANode=-1;a.p.userDataOnFooter&&b(a).jqGrid("footerData","set",a.p.userData,true);if(w){a.p.records=ga;a.p.lastpage=Math.ceil(ga/ha)}q||a.updatepager(false,true);if(w){for(;v<ga;){N=d[v];L=D(N,v);if(a.p.xmlReader.repeatitems){V||(V=S(I+F+K));var Ea=b(a.p.xmlReader.cell,N);b.each(V,function(da){var Z=Ea[this];if(!Z)return false;y=Z.textContent||Z.text;T[a.p.colModel[da+I+F+K].name]=y})}else for(s=0;s<U.length;s++){y=b(U[s],N).text();T[a.p.colModel[s+I+F+K].name]=y}T[C]=L;a.p.data.push(T);
T={};v++}O()}}},ca=function(d,g,h,q,o){var x=new Date;if(d){if(a.p.treeANode===-1&&!a.p.scroll){Y(g,false);h=1}else h=h>1?h:1;var w,C,D=a.p.datatype!="local"&&a.p.loadonce||a.p.datatype=="jsonstring";if(D){a.p.data=[];a.p._index={};w=a.p.localReader.id="_id_"}a.p.reccount=0;if(a.p.datatype=="local"){g=a.p.localReader;C="local"}else{g=a.p.jsonReader;C="json"}var s=0,v,y,E,I=[],F,K=0,z=0,U=0,V,T,N={},L;E=[];var X=a.p.altRows===true?" "+a.p.altclass:"";a.p.page=b.jgrid.getAccessor(d,g.page)||0;V=b.jgrid.getAccessor(d,
g.total);a.p.lastpage=V===undefined?1:V;a.p.records=b.jgrid.getAccessor(d,g.records)||0;a.p.userData=b.jgrid.getAccessor(d,g.userdata)||{};g.repeatitems||(F=I=H(C));C=a.p.keyIndex===false?g.id:a.p.keyIndex;if(I.length>0&&!isNaN(C)){if(a.p.remapColumns&&a.p.remapColumns.length)C=b.inArray(C,a.p.remapColumns);C=I[C]}if(T=b.jgrid.getAccessor(d,g.root)){V=T.length;d=0;var oa=parseInt(a.p.rowNum,10),ga=a.p.scroll?(parseInt(a.p.page,10)-1)*oa+1:1;if(o)oa*=o+1;var $=b.isFunction(a.p.afterInsertRow),ha={},
ra="";if(a.p.grouping&&a.p.groupingView.groupCollapse===true)ra=' style="display:none;"';for(;d<V;){o=T[d];L=b.jgrid.getAccessor(o,C);if(L===undefined){L=ga+d;if(I.length===0)if(g.cell)L=o[g.cell][C]||L}v=h===1?0:h;v=(v+d)%2==1?X:"";E.push("<tr"+ra+' id="'+L+'" role="row" class= "ui-widget-content jqgrow ui-row-'+a.p.direction+""+v+'">');if(a.p.rownumbers===true){E.push(t(0,d,a.p.page,a.p.rowNum));U=1}if(a.p.multiselect){E.push(A(L,U,d));K=1}if(a.p.subGrid){E.push(b(a).jqGrid("addSubGridCell",K+U,
d+h));z=1}if(g.repeatitems){if(g.cell)o=b.jgrid.getAccessor(o,g.cell);F||(F=S(K+z+U))}for(y=0;y<F.length;y++){v=b.jgrid.getAccessor(o,F[y]);E.push(p(L,v,y+K+z+U,d+h,o));N[a.p.colModel[y+K+z+U].name]=v}E.push("</tr>");if(a.p.grouping){v=a.p.groupingView.groupField.length;y=[];for(var ia=0;ia<v;ia++)y.push(N[a.p.groupingView.groupField[ia]]);ha=b(a).jqGrid("groupingPrepare",E,y,ha,N);E=[]}if(D){N[w]=L;a.p.data.push(N)}if(a.p.gridview===false){if(a.p.treeGrid===true){v=a.p.treeANode>=-1?a.p.treeANode:
0;E=b(E.join(""))[0];b(a.rows[d+v+h]).after(E);try{b(a).jqGrid("setTreeNode",N,E)}catch(xa){}}else b("#"+a.p.id+" tbody:first").append(E.join(""));if(a.p.subGrid===true)try{b(a).jqGrid("addSubGrid",a.rows[a.rows.length-1],K+U)}catch(Ba){}$&&a.p.afterInsertRow.call(a,L,N,o);E=[]}N={};s++;d++;if(s==oa)break}if(a.p.gridview===true)a.p.grouping&&F?b(a).jqGrid("groupingRender",ha,a.p.colModel.length):b("#"+a.p.id+" tbody:first").append(E.join(""));a.p.totaltime=new Date-x;if(s>0)if(a.p.records===0)a.p.records=
V;if(!a.p.treeGrid&&!a.p.scroll)a.grid.bDiv.scrollTop=0;a.p.reccount=s;a.p.treeANode=-1;a.p.userDataOnFooter&&b(a).jqGrid("footerData","set",a.p.userData,true);if(D){a.p.records=V;a.p.lastpage=Math.ceil(V/oa)}q||a.updatepager(false,true);if(D){for(;s<V;){o=T[s];L=b.jgrid.getAccessor(o,C);if(L===undefined){L=ga+s;if(I.length===0)if(g.cell)L=o[g.cell][C]||L}if(o){if(g.repeatitems){if(g.cell)o=b.jgrid.getAccessor(o,g.cell);F||(F=S(K+z+U))}for(y=0;y<F.length;y++){v=b.jgrid.getAccessor(o,F[y]);N[a.p.colModel[y+
K+z+U].name]=v}N[w]=L;a.p.data.push(N);N={}}s++}O()}}}},P=function(){var d,g=false,h=[],q=[],o,x,w;if(b.isArray(a.p.data)){var C=a.p.grouping?a.p.groupingView:false;b.each(a.p.colModel,function(){x=this.sorttype||"text";if(x=="date"||x=="datetime"){if(this.formatter&&typeof this.formatter==="string"&&this.formatter=="date"){o=this.formatoptions&&this.formatoptions.srcformat?this.formatoptions.srcformat:b.jgrid.formatter.date.srcformat;w=this.formatoptions&&this.formatoptions.newformat?this.formatoptions.newformat:
b.jgrid.formatter.date.newformat}else o=w=this.datefmt||"Y-m-d";h[this.name]={stype:x,srcfmt:o,newfmt:w}}else h[this.name]={stype:x,srcfmt:"",newfmt:""};if(a.p.grouping&&this.name==C.groupField[0])q[0]=h[this.name];if(!g&&(this.index==a.p.sortname||this.name==a.p.sortname)){d=this.name;g=true}});if(a.p.treeGrid)b(a).jqGrid("SortTree",d,a.p.sortorder,h[d].stype,h[d].srcfmt);else{var D={eq:function(z){return z.equals},ne:function(z){return z.not().equals},lt:function(z){return z.less},le:function(z){return z.lessOrEquals},
gt:function(z){return z.greater},ge:function(z){return z.greaterOrEquals},cn:function(z){return z.contains},nc:function(z){return z.not().contains},bw:function(z){return z.startsWith},bn:function(z){return z.not().startsWith},en:function(z){return z.not().endsWith},ew:function(z){return z.endsWith},ni:function(z){return z.not().equals},"in":function(z){return z.equals}},s=b.jgrid.from(a.p.data);if(a.p.ignoreCase)s=s.ignoreCase();if(a.p.search===true){var v=a.p.postData.filters,y;if(v){if(typeof v==
"string")v=b.jgrid.parse(v);for(var E=0,I=v.rules.length,F;E<I;E++){F=v.rules[E];y=v.groupOp;if(D[F.op]&&F.field&&F.data&&y)s=y.toUpperCase()=="OR"?D[F.op](s)(F.field,F.data,h[F.field]).or():D[F.op](s)(F.field,F.data,h[F.field])}}else try{s=D[a.p.postData.searchOper](s)(a.p.postData.searchField,a.p.postData.searchString,h[a.p.postData.searchField])}catch(K){}}if(a.p.grouping){s.orderBy(C.groupField[0],C.groupOrder[0],q[0].stype,q[0].srcfmt);C.groupDataSorted=true}if(d&&a.p.sortorder&&g)a.p.sortorder.toUpperCase()==
"DESC"?s.orderBy(a.p.sortname,"d",h[d].stype,h[d].srcfmt):s.orderBy(a.p.sortname,"a",h[d].stype,h[d].srcfmt);D=s.select();s=parseInt(a.p.rowNum,10);v=D.length;y=parseInt(a.p.page,10);E=Math.ceil(v/s);I={};D=D.slice((y-1)*s,y*s);h=s=null;I[a.p.localReader.total]=E;I[a.p.localReader.page]=y;I[a.p.localReader.records]=v;I[a.p.localReader.root]=D;D=null;return I}}},ba=function(){a.grid.hDiv.loading=true;if(!a.p.hiddengrid)switch(a.p.loadui){case "disable":break;case "enable":b("#load_"+a.p.id).show();
break;case "block":b("#lui_"+a.p.id).show();b("#load_"+a.p.id).show();break}},pa=function(){a.grid.hDiv.loading=false;switch(a.p.loadui){case "disable":break;case "enable":b("#load_"+a.p.id).hide();break;case "block":b("#lui_"+a.p.id).hide();b("#load_"+a.p.id).hide();break}},ja=function(d){if(!a.grid.hDiv.loading){var g=a.p.scroll&&d===false,h={},q,o=a.p.prmNames;if(a.p.page<=0)a.p.page=1;if(o.search!==null)h[o.search]=a.p.search;if(o.nd!==null)h[o.nd]=(new Date).getTime();if(o.rows!==null)h[o.rows]=
a.p.rowNum;if(o.page!==null)h[o.page]=a.p.page;if(o.sort!==null)h[o.sort]=a.p.sortname;if(o.order!==null)h[o.order]=a.p.sortorder;if(a.p.rowTotal!==null&&o.totalrows!==null)h[o.totalrows]=a.p.rowTotal;var x=a.p.loadComplete,w=b.isFunction(x);w||(x=null);var C=0;d=d||1;if(d>1)if(o.npage!==null){h[o.npage]=d;C=d-1;d=1}else x=function(s){a.p.page++;a.grid.hDiv.loading=false;w&&a.p.loadComplete.call(a,s);ja(d-1)};else o.npage!==null&&delete a.p.postData[o.npage];if(a.p.grouping){b(a).jqGrid("groupingSetup");
if(a.p.groupingView.groupDataSorted===true)h[o.sort]=a.p.groupingView.groupField[0]+" "+a.p.groupingView.groupOrder[0]+", "+h[o.sort]}b.extend(a.p.postData,h);var D=!a.p.scroll?1:a.rows.length-1;if(b.isFunction(a.p.datatype))a.p.datatype.call(a,a.p.postData,"load_"+a.p.id);else{b.isFunction(a.p.beforeRequest)&&a.p.beforeRequest.call(a);q=a.p.datatype.toLowerCase();switch(q){case "json":case "jsonp":case "xml":case "script":b.ajax(b.extend({url:a.p.url,type:a.p.mtype,dataType:q,data:b.isFunction(a.p.serializeGridData)?
a.p.serializeGridData.call(a,a.p.postData):a.p.postData,success:function(s){q==="xml"?fa(s,a.grid.bDiv,D,d>1,C):ca(s,a.grid.bDiv,D,d>1,C);x&&x.call(a,s);g&&a.grid.populateVisible();if(a.p.loadonce||a.p.treeGrid)a.p.datatype="local";pa()},error:function(s,v,y){b.isFunction(a.p.loadError)&&a.p.loadError.call(a,s,v,y);pa()},beforeSend:function(s){ba();b.isFunction(a.p.loadBeforeSend)&&a.p.loadBeforeSend.call(a,s)}},b.jgrid.ajaxOptions,a.p.ajaxGridOptions));break;case "xmlstring":ba();h=b.jgrid.stringToDoc(a.p.datastr);
fa(h,a.grid.bDiv);w&&a.p.loadComplete.call(a,h);a.p.datatype="local";a.p.datastr=null;pa();break;case "jsonstring":ba();h=typeof a.p.datastr=="string"?b.jgrid.parse(a.p.datastr):a.p.datastr;ca(h,a.grid.bDiv);w&&a.p.loadComplete.call(a,h);a.p.datatype="local";a.p.datastr=null;pa();break;case "local":case "clientside":ba();a.p.datatype="local";h=P();ca(h,a.grid.bDiv,D,d>1,C);x&&x.call(a,h);g&&a.grid.populateVisible();pa();break}}}};u=function(d,g){var h="",q="<table cellspacing='0' cellpadding='0' border='0' style='table-layout:auto;' class='ui-pg-table'><tbody><tr>",
o="",x,w,C,D,s=function(v){var y;if(b.isFunction(a.p.onPaging))y=a.p.onPaging.call(a,v);a.p.selrow=null;if(a.p.multiselect){a.p.selarrrow=[];b("#cb_"+b.jgrid.jqID(a.p.id),a.grid.hDiv).attr("checked",false)}a.p.savedRow=[];if(y=="stop")return false;return true};d=d.substr(1);x="pg_"+d;w=d+"_left";C=d+"_center";D=d+"_right";b("#"+d).append("<div id='"+x+"' class='ui-pager-control' role='group'><table cellspacing='0' cellpadding='0' border='0' class='ui-pg-table' style='width:100%;table-layout:fixed;' role='row'><tbody><tr><td id='"+
w+"' align='left'></td><td id='"+C+"' align='center' style='white-space:pre;'></td><td id='"+D+"' align='right'></td></tr></tbody></table></div>").attr("dir","ltr");if(a.p.rowList.length>0){o="<td dir='"+l+"'>";o+="<select class='ui-pg-selbox' role='listbox'>";for(w=0;w<a.p.rowList.length;w++)o+='<option role="option" value="'+a.p.rowList[w]+'"'+(a.p.rowNum==a.p.rowList[w]?' selected="selected"':"")+">"+a.p.rowList[w]+"</option>";o+="</select></td>"}if(l=="rtl")q+=o;if(a.p.pginput===true)h="<td dir='"+
l+"'>"+b.jgrid.format(a.p.pgtext||"","<input class='ui-pg-input' type='text' size='2' maxlength='7' value='0' role='textbox'/>","<span id='sp_1'></span>")+"</td>";if(a.p.pgbuttons===true){w=["first"+g,"prev"+g,"next"+g,"last"+g];l=="rtl"&&w.reverse();q+="<td id='"+w[0]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-first'></span></td>";q+="<td id='"+w[1]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-prev'></span></td>";q+=h!==""?"<td class='ui-pg-button ui-state-disabled' style='width:4px;'><span class='ui-separator'></span></td>"+
h+"<td class='ui-pg-button ui-state-disabled' style='width:4px;'><span class='ui-separator'></span></td>":"";q+="<td id='"+w[2]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-next'></span></td>";q+="<td id='"+w[3]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-end'></span></td>"}else if(h!=="")q+=h;if(l=="ltr")q+=o;q+="</tr></tbody></table>";a.p.viewrecords===true&&b("td#"+d+"_"+a.p.recordpos,"#"+x).append("<div dir='"+l+"' style='text-align:"+a.p.recordpos+
"' class='ui-paging-info'></div>");b("td#"+d+"_"+a.p.pagerpos,"#"+x).append(q);o=b(".ui-jqgrid").css("font-size")||"11px";b(document.body).append("<div id='testpg' class='ui-jqgrid ui-widget ui-widget-content' style='font-size:"+o+";visibility:hidden;' ></div>");q=b(q).clone().appendTo("#testpg").width();b("#testpg").remove();if(q>0){if(h!="")q+=50;b("td#"+d+"_"+a.p.pagerpos,"#"+x).width(q)}a.p._nvtd=[];a.p._nvtd[0]=q?Math.floor((a.p.width-q)/2):Math.floor(a.p.width/3);a.p._nvtd[1]=0;q=null;b(".ui-pg-selbox",
"#"+x).bind("change",function(){a.p.page=Math.round(a.p.rowNum*(a.p.page-1)/this.value-0.5)+1;a.p.rowNum=this.value;if(g)b(".ui-pg-selbox",a.p.pager).val(this.value);else a.p.toppager&&b(".ui-pg-selbox",a.p.toppager).val(this.value);if(!s("records"))return false;ja();return false});if(a.p.pgbuttons===true){b(".ui-pg-button","#"+x).hover(function(){if(b(this).hasClass("ui-state-disabled"))this.style.cursor="default";else{b(this).addClass("ui-state-hover");this.style.cursor="pointer"}},function(){if(!b(this).hasClass("ui-state-disabled")){b(this).removeClass("ui-state-hover");
this.style.cursor="default"}});b("#first"+g+", #prev"+g+", #next"+g+", #last"+g,"#"+d).click(function(){var v=J(a.p.page,1),y=J(a.p.lastpage,1),E=false,I=true,F=true,K=true,z=true;if(y===0||y===1)z=K=F=I=false;else if(y>1&&v>=1)if(v===1)F=I=false;else{if(!(v>1&&v<y))if(v===y)z=K=false}else if(y>1&&v===0){z=K=false;v=y-1}if(this.id==="first"+g&&I){a.p.page=1;E=true}if(this.id==="prev"+g&&F){a.p.page=v-1;E=true}if(this.id==="next"+g&&K){a.p.page=v+1;E=true}if(this.id==="last"+g&&z){a.p.page=y;E=true}if(E){if(!s(this.id))return false;
ja()}return false})}a.p.pginput===true&&b("input.ui-pg-input","#"+x).keypress(function(v){if((v.charCode?v.charCode:v.keyCode?v.keyCode:0)==13){a.p.page=b(this).val()>0?b(this).val():a.p.page;if(!s("user"))return false;ja();return false}return this})};var Ca=function(d,g,h,q){if(a.p.colModel[g].sortable)if(!(a.p.savedRow.length>0)){if(!h){if(a.p.lastsort==g)if(a.p.sortorder=="asc")a.p.sortorder="desc";else{if(a.p.sortorder=="desc")a.p.sortorder="asc"}else a.p.sortorder=a.p.colModel[g].firstsortorder||
"asc";a.p.page=1}if(q)if(a.p.lastsort==g&&a.p.sortorder==q&&!h)return;else a.p.sortorder=q;h=b("thead:first",a.grid.hDiv).get(0);b("tr th:eq("+a.p.lastsort+") span.ui-grid-ico-sort",h).addClass("ui-state-disabled");b("tr th:eq("+a.p.lastsort+")",h).attr("aria-selected","false");b("tr th:eq("+g+") span.ui-icon-"+a.p.sortorder,h).removeClass("ui-state-disabled");b("tr th:eq("+g+")",h).attr("aria-selected","true");if(!a.p.viewsortcols[0])if(a.p.lastsort!=g){b("tr th:eq("+a.p.lastsort+") span.s-ico",
h).hide();b("tr th:eq("+g+") span.s-ico",h).show()}d=d.substring(5);a.p.sortname=a.p.colModel[g].index||d;h=a.p.sortorder;if(b.isFunction(a.p.onSortCol))if(a.p.onSortCol.call(a,d,g,h)=="stop"){a.p.lastsort=g;return}if(a.p.datatype=="local")a.p.deselectAfterSort&&b(a).jqGrid("resetSelection");else{a.p.selrow=null;a.p.multiselect&&b("#cb_"+b.jgrid.jqID(a.p.id),a.grid.hDiv).attr("checked",false);a.p.selarrrow=[];a.p.savedRow=[]}if(a.p.scroll){h=a.grid.bDiv.scrollLeft;Y(a.grid.bDiv,true);a.grid.hDiv.scrollLeft=
h}a.p.subGrid&&a.p.datatype=="local"&&b("td.sgexpanded","#"+a.p.id).each(function(){b(this).trigger("click")});ja();a.p.lastsort=g;if(a.p.sortname!=d&&g)a.p.lastsort=g}},Fa=function(d){var g=d,h;for(h=d+1;h<a.p.colModel.length;h++)if(a.p.colModel[h].hidden!==true){g=h;break}return g-d},Ga=function(d){var g,h={},q=G?0:a.p.cellLayout;for(g=h[0]=h[1]=h[2]=0;g<=d;g++)if(a.p.colModel[g].hidden===false)h[0]+=a.p.colModel[g].width+q;if(a.p.direction=="rtl")h[0]=a.p.width-h[0];h[0]-=a.grid.bDiv.scrollLeft;
if(b(a.grid.cDiv).is(":visible"))h[1]+=b(a.grid.cDiv).height()+parseInt(b(a.grid.cDiv).css("padding-top"),10)+parseInt(b(a.grid.cDiv).css("padding-bottom"),10);if(a.p.toolbar[0]===true&&(a.p.toolbar[1]=="top"||a.p.toolbar[1]=="both"))h[1]+=b(a.grid.uDiv).height()+parseInt(b(a.grid.uDiv).css("border-top-width"),10)+parseInt(b(a.grid.uDiv).css("border-bottom-width"),10);if(a.p.toppager)h[1]+=b(a.grid.topDiv).height()+parseInt(b(a.grid.topDiv).css("border-bottom-width"),10);h[2]+=b(a.grid.bDiv).height()+
b(a.grid.hDiv).height();return h};this.p.id=this.id;if(b.inArray(a.p.multikey,["shiftKey","altKey","ctrlKey"])==-1)a.p.multikey=false;a.p.keyIndex=false;for(k=0;k<a.p.colModel.length;k++)if(a.p.colModel[k].key===true){a.p.keyIndex=k;break}a.p.sortorder=a.p.sortorder.toLowerCase();if(a.p.grouping===true){a.p.scroll=false;a.p.rownumbers=false;a.p.subGrid=false;a.p.treeGrid=false;a.p.gridview=true}if(this.p.treeGrid===true){try{b(this).jqGrid("setTreeGrid")}catch(Ka){}if(a.p.datatype!="local")a.p.localReader=
{id:"_id_"}}if(this.p.subGrid)try{b(a).jqGrid("setSubGrid")}catch(La){}if(this.p.multiselect){this.p.colNames.unshift("<input role='checkbox' id='cb_"+this.p.id+"' class='cbox' type='checkbox'/>");this.p.colModel.unshift({name:"cb",width:G?a.p.multiselectWidth+a.p.cellLayout:a.p.multiselectWidth,sortable:false,resizable:false,hidedlg:true,search:false,align:"center",fixed:true})}if(this.p.rownumbers){this.p.colNames.unshift("");this.p.colModel.unshift({name:"rn",width:a.p.rownumWidth,sortable:false,
resizable:false,hidedlg:true,search:false,align:"center",fixed:true})}a.p.xmlReader=b.extend(true,{root:"rows",row:"row",page:"rows>page",total:"rows>total",records:"rows>records",repeatitems:true,cell:"cell",id:"[id]",userdata:"userdata",subgrid:{root:"rows",row:"row",repeatitems:true,cell:"cell"}},a.p.xmlReader);a.p.jsonReader=b.extend(true,{root:"rows",page:"page",total:"total",records:"records",repeatitems:true,cell:"cell",id:"id",userdata:"userdata",subgrid:{root:"rows",repeatitems:true,cell:"cell"}},
a.p.jsonReader);a.p.localReader=b.extend(true,{root:"rows",page:"page",total:"total",records:"records",repeatitems:false,cell:"cell",id:"id",userdata:"userdata",subgrid:{root:"rows",repeatitems:true,cell:"cell"}},a.p.localReader);if(a.p.scroll){a.p.pgbuttons=false;a.p.pginput=false;a.p.rowList=[]}a.p.data.length&&O();var aa="<thead><tr class='ui-jqgrid-labels' role='rowheader'>",Da,ma,sa,qa,ta,W,R,na;ma=na="";if(a.p.shrinkToFit===true&&a.p.forceFit===true)for(k=a.p.colModel.length-1;k>=0;k--)if(!a.p.colModel[k].hidden){a.p.colModel[k].resizable=
false;break}if(a.p.viewsortcols[1]=="horizontal"){na=" ui-i-asc";ma=" ui-i-desc"}Da=B?"class='ui-th-div-ie'":"";na="<span class='s-ico' style='display:none'><span sort='asc' class='ui-grid-ico-sort ui-icon-asc"+na+" ui-state-disabled ui-icon ui-icon-triangle-1-n ui-sort-"+l+"'></span>";na+="<span sort='desc' class='ui-grid-ico-sort ui-icon-desc"+ma+" ui-state-disabled ui-icon ui-icon-triangle-1-s ui-sort-"+l+"'></span></span>";for(k=0;k<this.p.colNames.length;k++){ma=a.p.headertitles?' title="'+b.jgrid.stripHtml(a.p.colNames[k])+
'"':"";aa+="<th id='"+a.p.id+"_"+a.p.colModel[k].name+"' role='columnheader' class='ui-state-default ui-th-column ui-th-"+l+"'"+ma+">";ma=a.p.colModel[k].index||a.p.colModel[k].name;aa+="<div id='jqgh_"+a.p.colModel[k].name+"' "+Da+">"+a.p.colNames[k];a.p.colModel[k].width=a.p.colModel[k].width?parseInt(a.p.colModel[k].width,10):150;if(typeof a.p.colModel[k].title!=="boolean")a.p.colModel[k].title=true;if(ma==a.p.sortname)a.p.lastsort=k;aa+=na+"</div></th>"}aa+="</tr></thead>";na=null;b(this).append(aa);
b("thead tr:first th",this).hover(function(){b(this).addClass("ui-state-hover")},function(){b(this).removeClass("ui-state-hover")});if(this.p.multiselect){var za=[],ua;b("#cb_"+b.jgrid.jqID(a.p.id),this).bind("click",function(){if(this.checked){b("[id^=jqg_"+a.p.id+"_]").attr("checked",true);b(a.rows).each(function(d){if(d>0)if(!b(this).hasClass("subgrid")&&!b(this).hasClass("jqgroup")){b(this).addClass("ui-state-highlight").attr("aria-selected","true");a.p.selarrrow[d]=a.p.selrow=this.id}});ua=true;
za=[]}else{b("[id^=jqg_"+a.p.id+"_]").attr("checked",false);b(a.rows).each(function(d){if(!b(this).hasClass("subgrid")){b(this).removeClass("ui-state-highlight").attr("aria-selected","false");za[d]=this.id}});a.p.selarrrow=[];a.p.selrow=null;ua=false}if(b.isFunction(a.p.onSelectAll))a.p.onSelectAll.call(a,ua?a.p.selarrrow:za,ua)})}if(a.p.autowidth===true){aa=b(Q).innerWidth();a.p.width=aa>0?aa:"nw"}(function(){var d=0,g=a.p.cellLayout,h=0,q,o=a.p.scrollOffset,x,w=false,C,D=0,s=0,v=0,y;if(G)g=0;b.each(a.p.colModel,
function(){if(typeof this.hidden==="undefined")this.hidden=false;if(this.hidden===false){d+=J(this.width,0);if(this.fixed){D+=this.width;s+=this.width+g}else h++;v++}});if(isNaN(a.p.width))a.p.width=e.width=d;else e.width=a.p.width;a.p.tblwidth=d;if(a.p.shrinkToFit===false&&a.p.forceFit===true)a.p.forceFit=false;if(a.p.shrinkToFit===true&&h>0){C=e.width-g*h-s;if(!isNaN(a.p.height)){C-=o;w=true}d=0;b.each(a.p.colModel,function(E){if(this.hidden===false&&!this.fixed){this.width=x=Math.round(C*this.width/
(a.p.tblwidth-D));d+=x;q=E}});y=0;if(w){if(e.width-s-(d+g*h)!==o)y=e.width-s-(d+g*h)-o}else if(!w&&Math.abs(e.width-s-(d+g*h))!==1)y=e.width-s-(d+g*h);a.p.colModel[q].width+=y;a.p.tblwidth=d+y+D+v*g;if(a.p.tblwidth>a.p.width){a.p.colModel[q].width-=a.p.tblwidth-parseInt(a.p.width,10);a.p.tblwidth=a.p.width}}})();b(Q).css("width",e.width+"px").append("<div class='ui-jqgrid-resize-mark' id='rs_m"+a.p.id+"'>&#160;</div>");b(r).css("width",e.width+"px");aa=b("thead:first",a).get(0);var va="";if(a.p.footerrow)va+=
"<table role='grid' style='width:"+a.p.tblwidth+"px' class='ui-jqgrid-ftable' cellspacing='0' cellpadding='0' border='0'><tbody><tr role='row' class='ui-widget-content footrow footrow-"+l+"'>";r=b("tr:first",aa);var wa="<tr class='jqgfirstrow' role='row' style='height:auto'>";a.p.disableClick=false;b("th",r).each(function(d){sa=a.p.colModel[d].width;if(typeof a.p.colModel[d].resizable==="undefined")a.p.colModel[d].resizable=true;if(a.p.colModel[d].resizable){qa=document.createElement("span");b(qa).html("&#160;").addClass("ui-jqgrid-resize ui-jqgrid-resize-"+
l);b.browser.opera||b(qa).css("cursor","col-resize");b(this).addClass(a.p.resizeclass)}else qa="";b(this).css("width",sa+"px").prepend(qa);var g="";if(a.p.colModel[d].hidden){b(this).css("display","none");g="display:none;"}wa+="<td role='gridcell' style='height:0px;width:"+sa+"px;"+g+"'>";e.headers[d]={width:sa,el:this};ta=a.p.colModel[d].sortable;if(typeof ta!=="boolean")ta=a.p.colModel[d].sortable=true;g=a.p.colModel[d].name;g=="cb"||g=="subgrid"||g=="rn"||a.p.viewsortcols[2]&&b("div",this).addClass("ui-jqgrid-sortable");
if(ta)if(a.p.viewsortcols[0]){b("div span.s-ico",this).show();d==a.p.lastsort&&b("div span.ui-icon-"+a.p.sortorder,this).removeClass("ui-state-disabled")}else if(d==a.p.lastsort){b("div span.s-ico",this).show();b("div span.ui-icon-"+a.p.sortorder,this).removeClass("ui-state-disabled")}if(a.p.footerrow)va+="<td role='gridcell' "+M(d,0,"")+">&#160;</td>"}).mousedown(function(d){if(b(d.target).closest("th>span.ui-jqgrid-resize").length==1){var g=b.jgrid.getCellIndex(this);if(a.p.forceFit===true)a.p.nv=
Fa(g);e.dragStart(g,d,Ga(g));return false}}).click(function(d){if(a.p.disableClick)return a.p.disableClick=false;var g="th>div.ui-jqgrid-sortable",h,q;a.p.viewsortcols[2]||(g="th>div>span>span.ui-grid-ico-sort");d=b(d.target).closest(g);if(d.length==1){g=b.jgrid.getCellIndex(this);if(!a.p.viewsortcols[2]){h=true;q=d.attr("sort")}Ca(b("div",this)[0].id,g,h,q);return false}});if(a.p.sortable&&b.fn.sortable)try{b(a).jqGrid("sortableColumns",r)}catch(Ma){}if(a.p.footerrow)va+="</tr></tbody></table>";
wa+="</tr>";this.appendChild(document.createElement("tbody"));b(this).addClass("ui-jqgrid-btable").append(wa);wa=null;r=b("<table class='ui-jqgrid-htable' style='width:"+a.p.tblwidth+"px' role='grid' aria-labelledby='gbox_"+this.id+"' cellspacing='0' cellpadding='0' border='0'></table>").append(aa);var ea=a.p.caption&&a.p.hiddengrid===true?true:false;k=b("<div class='ui-jqgrid-hbox"+(l=="rtl"?"-rtl":"")+"'></div>");aa=null;e.hDiv=document.createElement("div");b(e.hDiv).css({width:e.width+"px"}).addClass("ui-state-default ui-jqgrid-hdiv").append(k);
b(k).append(r);r=null;ea&&b(e.hDiv).hide();if(a.p.pager){if(typeof a.p.pager=="string"){if(a.p.pager.substr(0,1)!="#")a.p.pager="#"+a.p.pager}else a.p.pager="#"+b(a.p.pager).attr("id");b(a.p.pager).css({width:e.width+"px"}).appendTo(Q).addClass("ui-state-default ui-jqgrid-pager ui-corner-bottom");ea&&b(a.p.pager).hide();u(a.p.pager,"")}a.p.cellEdit===false&&a.p.hoverrows===true&&b(a).bind("mouseover",function(d){R=b(d.target).closest("tr.jqgrow");b(R).attr("class")!=="subgrid"&&b(R).addClass("ui-state-hover");
return false}).bind("mouseout",function(d){R=b(d.target).closest("tr.jqgrow");b(R).removeClass("ui-state-hover");return false});var ka,la;b(a).before(e.hDiv).click(function(d){W=d.target;var g=b(W).hasClass("cbox");R=b(W,a.rows).closest("tr.jqgrow");if(b(R).length===0)return this;var h=true;if(b.isFunction(a.p.beforeSelectRow))h=a.p.beforeSelectRow.call(a,R[0].id,d);if(W.tagName=="A"||(W.tagName=="INPUT"||W.tagName=="TEXTAREA"||W.tagName=="OPTION"||W.tagName=="SELECT")&&!g)return this;if(h===true){if(a.p.cellEdit===
true)if(a.p.multiselect&&g)b(a).jqGrid("setSelection",R[0].id,true);else{ka=R[0].rowIndex;la=b.jgrid.getCellIndex(W);try{b(a).jqGrid("editCell",ka,la,true)}catch(q){}}else if(a.p.multikey)if(d[a.p.multikey])b(a).jqGrid("setSelection",R[0].id,true);else{if(a.p.multiselect&&g){g=b("[id^=jqg_"+a.p.id+"_]").attr("checked");b("[id^=jqg_"+a.p.id+"_]").attr("checked",!g)}}else{if(a.p.multiselect&&a.p.multiboxonly)if(!g){b(a.p.selarrrow).each(function(o,x){o=a.rows.namedItem(x);b(o).removeClass("ui-state-highlight");
b("#jqg_"+a.p.id+"_"+b.jgrid.jqID(x)).attr("checked",false)});a.p.selarrrow=[];b("#cb_"+b.jgrid.jqID(a.p.id),a.grid.hDiv).attr("checked",false)}b(a).jqGrid("setSelection",R[0].id,true)}if(b.isFunction(a.p.onCellSelect)){ka=R[0].id;la=b.jgrid.getCellIndex(W);a.p.onCellSelect.call(a,ka,la,b(W).html(),d)}d.stopPropagation()}else return this}).bind("reloadGrid",function(d,g){if(a.p.treeGrid===true)a.p.datatype=a.p.treedatatype;g&&g.current&&a.grid.selectionPreserver(a);if(a.p.datatype=="local"){b(a).jqGrid("resetSelection");
a.p.data.length&&O()}else if(!a.p.treeGrid){a.p.selrow=null;if(a.p.multiselect){a.p.selarrrow=[];b("#cb_"+b.jgrid.jqID(a.p.id),a.grid.hDiv).attr("checked",false)}a.p.savedRow=[]}a.p.scroll&&Y(a.grid.bDiv,true);if(g&&g.page){d=g.page;if(d>a.p.lastpage)d=a.p.lastpage;if(d<1)d=1;a.p.page=d;a.grid.bDiv.scrollTop=a.grid.prevRowHeight?(d-1)*a.grid.prevRowHeight*a.p.rowNum:0}if(a.grid.prevRowHeight&&a.p.scroll){delete a.p.lastpage;a.grid.populateVisible()}else a.grid.populate();return false});b.isFunction(this.p.ondblClickRow)&&
b(this).dblclick(function(d){W=d.target;R=b(W,a.rows).closest("tr.jqgrow");if(b(R).length===0)return false;ka=R[0].rowIndex;la=b.jgrid.getCellIndex(W);a.p.ondblClickRow.call(a,b(R).attr("id"),ka,la,d);return false});b.isFunction(this.p.onRightClickRow)&&b(this).bind("contextmenu",function(d){W=d.target;R=b(W,a.rows).closest("tr.jqgrow");if(b(R).length===0)return false;a.p.multiselect||b(a).jqGrid("setSelection",R[0].id,true);ka=R[0].rowIndex;la=b.jgrid.getCellIndex(W);a.p.onRightClickRow.call(a,b(R).attr("id"),
ka,la,d);return false});e.bDiv=document.createElement("div");b(e.bDiv).append(b('<div style="position:relative;'+(B&&b.browser.version<8?"height:0.01%;":"")+'"></div>').append("<div></div>").append(this)).addClass("ui-jqgrid-bdiv").css({height:a.p.height+(isNaN(a.p.height)?"":"px"),width:e.width+"px"}).scroll(e.scrollGrid);b("table:first",e.bDiv).css({width:a.p.tblwidth+"px"});if(B){b("tbody",this).size()==2&&b("tbody:gt(0)",this).remove();a.p.multikey&&b(e.bDiv).bind("selectstart",function(){return false})}else a.p.multikey&&
b(e.bDiv).bind("mousedown",function(){return false});ea&&b(e.bDiv).hide();e.cDiv=document.createElement("div");var Aa=a.p.hidegrid===true?b("<a role='link' href='javascript:void(0)'/>").addClass("ui-jqgrid-titlebar-close HeaderButton").hover(function(){Aa.addClass("ui-state-hover")},function(){Aa.removeClass("ui-state-hover")}).append("<span class='ui-icon ui-icon-circle-triangle-n'></span>").css(l=="rtl"?"left":"right","0px"):"";b(e.cDiv).append(Aa).append("<span class='ui-jqgrid-title"+(l=="rtl"?
"-rtl":"")+"'>"+a.p.caption+"</span>").addClass("ui-jqgrid-titlebar ui-widget-header ui-corner-top ui-helper-clearfix");b(e.cDiv).insertBefore(e.hDiv);if(a.p.toolbar[0]){e.uDiv=document.createElement("div");if(a.p.toolbar[1]=="top")b(e.uDiv).insertBefore(e.hDiv);else a.p.toolbar[1]=="bottom"&&b(e.uDiv).insertAfter(e.hDiv);if(a.p.toolbar[1]=="both"){e.ubDiv=document.createElement("div");b(e.uDiv).insertBefore(e.hDiv).addClass("ui-userdata ui-state-default").attr("id","t_"+this.id);b(e.ubDiv).insertAfter(e.hDiv).addClass("ui-userdata ui-state-default").attr("id",
"tb_"+this.id);ea&&b(e.ubDiv).hide()}else b(e.uDiv).width(e.width).addClass("ui-userdata ui-state-default").attr("id","t_"+this.id);ea&&b(e.uDiv).hide()}if(a.p.toppager){a.p.toppager=a.p.id+"_toppager";e.topDiv=b("<div id='"+a.p.toppager+"'></div>")[0];a.p.toppager="#"+a.p.toppager;b(e.topDiv).insertBefore(e.hDiv).addClass("ui-state-default ui-jqgrid-toppager").width(e.width);u(a.p.toppager,"_t")}if(a.p.footerrow){e.sDiv=b("<div class='ui-jqgrid-sdiv'></div>")[0];k=b("<div class='ui-jqgrid-hbox"+
(l=="rtl"?"-rtl":"")+"'></div>");b(e.sDiv).append(k).insertAfter(e.hDiv).width(e.width);b(k).append(va);e.footers=b(".ui-jqgrid-ftable",e.sDiv)[0].rows[0].cells;if(a.p.rownumbers)e.footers[0].className="ui-state-default jqgrid-rownum";ea&&b(e.sDiv).hide()}k=null;if(a.p.caption){var Ha=a.p.datatype;if(a.p.hidegrid===true){b(".ui-jqgrid-titlebar-close",e.cDiv).click(function(d){var g=b.isFunction(a.p.onHeaderClick);if(a.p.gridstate=="visible"){b(".ui-jqgrid-bdiv, .ui-jqgrid-hdiv","#gview_"+a.p.id).slideUp("fast");
a.p.pager&&b(a.p.pager).slideUp("fast");a.p.toppager&&b(a.p.toppager).slideUp("fast");if(a.p.toolbar[0]===true){a.p.toolbar[1]=="both"&&b(e.ubDiv).slideUp("fast");b(e.uDiv).slideUp("fast")}a.p.footerrow&&b(".ui-jqgrid-sdiv","#gbox_"+a.p.id).slideUp("fast");b("span",this).removeClass("ui-icon-circle-triangle-n").addClass("ui-icon-circle-triangle-s");a.p.gridstate="hidden";b("#gbox_"+a.p.id).hasClass("ui-resizable")&&b(".ui-resizable-handle","#gbox_"+a.p.id).hide();if(g)ea||a.p.onHeaderClick.call(a,
a.p.gridstate,d)}else if(a.p.gridstate=="hidden"){b(".ui-jqgrid-hdiv, .ui-jqgrid-bdiv","#gview_"+a.p.id).slideDown("fast");a.p.pager&&b(a.p.pager).slideDown("fast");a.p.toppager&&b(a.p.toppager).slideDown("fast");if(a.p.toolbar[0]===true){a.p.toolbar[1]=="both"&&b(e.ubDiv).slideDown("fast");b(e.uDiv).slideDown("fast")}a.p.footerrow&&b(".ui-jqgrid-sdiv","#gbox_"+a.p.id).slideDown("fast");b("span",this).removeClass("ui-icon-circle-triangle-s").addClass("ui-icon-circle-triangle-n");if(ea){a.p.datatype=
Ha;ja();ea=false}a.p.gridstate="visible";b("#gbox_"+a.p.id).hasClass("ui-resizable")&&b(".ui-resizable-handle","#gbox_"+a.p.id).show();g&&a.p.onHeaderClick.call(a,a.p.gridstate,d)}return false});if(ea){a.p.datatype="local";b(".ui-jqgrid-titlebar-close",e.cDiv).trigger("click")}}}else b(e.cDiv).hide();b(e.hDiv).after(e.bDiv).mousemove(function(d){if(e.resizing){e.dragMove(d);return false}});b(".ui-jqgrid-labels",e.hDiv).bind("selectstart",function(){return false});b(document).mouseup(function(){if(e.resizing){e.dragEnd();
return false}return true});a.formatCol=M;a.sortData=Ca;a.updatepager=function(d,g){var h,q,o,x,w,C,D,s="";o=parseInt(a.p.page,10)-1;if(o<0)o=0;o*=parseInt(a.p.rowNum,10);w=o+a.p.reccount;if(a.p.scroll){h=b("tbody:first > tr:gt(0)",a.grid.bDiv);o=w-h.length;a.p.reccount=h.length;if(q=h.outerHeight()||a.grid.prevRowHeight){h=o*q;q=parseInt(a.p.records,10)*q;b(">div:first",a.grid.bDiv).css({height:q}).children("div:first").css({height:h,display:h?"":"none"})}a.grid.bDiv.scrollLeft=a.grid.hDiv.scrollLeft}s=
a.p.pager?a.p.pager:"";s+=a.p.toppager?s?","+a.p.toppager:a.p.toppager:"";if(s){D=b.jgrid.formatter.integer||{};h=J(a.p.page);q=J(a.p.lastpage);b(".selbox",s).attr("disabled",false);if(a.p.pginput===true){b(".ui-pg-input",s).val(a.p.page);b("#sp_1",s).html(b.fmatter?b.fmatter.util.NumberFormat(a.p.lastpage,D):a.p.lastpage)}if(a.p.viewrecords)if(a.p.reccount===0)b(".ui-paging-info",s).html(a.p.emptyrecords);else{x=o+1;C=a.p.records;if(b.fmatter){x=b.fmatter.util.NumberFormat(x,D);w=b.fmatter.util.NumberFormat(w,
D);C=b.fmatter.util.NumberFormat(C,D)}b(".ui-paging-info",s).html(b.jgrid.format(a.p.recordtext,x,w,C))}if(a.p.pgbuttons===true){if(h<=0)h=q=0;if(h==1||h===0){b("#first, #prev",a.p.pager).addClass("ui-state-disabled").removeClass("ui-state-hover");a.p.toppager&&b("#first_t, #prev_t",a.p.toppager).addClass("ui-state-disabled").removeClass("ui-state-hover")}else{b("#first, #prev",a.p.pager).removeClass("ui-state-disabled");a.p.toppager&&b("#first_t, #prev_t",a.p.toppager).removeClass("ui-state-disabled")}if(h==
q||h===0){b("#next, #last",a.p.pager).addClass("ui-state-disabled").removeClass("ui-state-hover");a.p.toppager&&b("#next_t, #last_t",a.p.toppager).addClass("ui-state-disabled").removeClass("ui-state-hover")}else{b("#next, #last",a.p.pager).removeClass("ui-state-disabled");a.p.toppager&&b("#next_t, #last_t",a.p.toppager).removeClass("ui-state-disabled")}}}d===true&&a.p.rownumbers===true&&b("td.jqgrid-rownum",a.rows).each(function(v){b(this).html(o+1+v)});g&&a.p.jqgdnd&&b(a).jqGrid("gridDnD","updateDnD");
b.isFunction(a.p.gridComplete)&&a.p.gridComplete.call(a)};a.refreshIndex=O;a.formatter=function(d,g,h,q,o){return m(d,g,h,q,o)};b.extend(e,{populate:ja,emptyRows:Y});this.grid=e;a.addXmlData=function(d){fa(d,a.grid.bDiv)};a.addJSONData=function(d){ca(d,a.grid.bDiv)};this.grid.cols=this.rows[0].cells;ja();a.p.hiddengrid=false;b(window).unload(function(){a=null})}}}})};b.jgrid.extend({getGridParam:function(f){var j=this[0];if(j&&j.grid)return f?typeof j.p[f]!="undefined"?j.p[f]:null:j.p},setGridParam:function(f){return this.each(function(){this.grid&&
typeof f==="object"&&b.extend(true,this.p,f)})},getDataIDs:function(){var f=[],j=0,i,c=0;this.each(function(){if((i=this.rows.length)&&i>0)for(;j<i;){if(b(this.rows[j]).hasClass("jqgrow")){f[c]=this.rows[j].id;c++}j++}});return f},setSelection:function(f,j){return this.each(function(){function i(l){var a=b(c.grid.bDiv)[0].clientHeight,r=b(c.grid.bDiv)[0].scrollTop,u=c.rows[l].offsetTop;l=c.rows[l].clientHeight;if(u+l>=a+r)b(c.grid.bDiv)[0].scrollTop=u-(a+r)+l+r;else if(u<a+r)if(u<r)b(c.grid.bDiv)[0].scrollTop=
u}var c=this,e,k;if(f!==undefined){j=j===false?false:true;if(e=c.rows.namedItem(f+"")){if(c.p.scrollrows===true){k=c.rows.namedItem(f).rowIndex;k>=0&&i(k)}if(c.p.multiselect){c.p.selrow=e.id;k=b.inArray(c.p.selrow,c.p.selarrrow);if(k===-1){e.className!=="ui-subgrid"&&b(e).addClass("ui-state-highlight").attr("aria-selected","true");e=true;b("#jqg_"+c.p.id+"_"+b.jgrid.jqID(c.p.selrow)).attr("checked",e);c.p.selarrrow.push(c.p.selrow);c.p.onSelectRow&&j&&c.p.onSelectRow.call(c,c.p.selrow,e)}else{e.className!==
"ui-subgrid"&&b(e).removeClass("ui-state-highlight").attr("aria-selected","false");e=false;b("#jqg_"+c.p.id+"_"+b.jgrid.jqID(c.p.selrow)).attr("checked",e);c.p.selarrrow.splice(k,1);c.p.onSelectRow&&j&&c.p.onSelectRow.call(c,c.p.selrow,e);e=c.p.selarrrow[0];c.p.selrow=e===undefined?null:e}}else if(e.className!=="ui-subgrid"){c.p.selrow&&b(c.rows.namedItem(c.p.selrow)).removeClass("ui-state-highlight").attr("aria-selected","false");c.p.selrow=e.id;b(e).addClass("ui-state-highlight").attr("aria-selected",
"true");c.p.onSelectRow&&j&&c.p.onSelectRow.call(c,c.p.selrow,true)}}}})},resetSelection:function(){return this.each(function(){var f=this,j;if(f.p.multiselect){b(f.p.selarrrow).each(function(i,c){j=f.rows.namedItem(c);b(j).removeClass("ui-state-highlight").attr("aria-selected","false");b("#jqg_"+f.p.id+"_"+b.jgrid.jqID(c)).attr("checked",false)});b("#cb_"+b.jgrid.jqID(f.p.id)).attr("checked",false);f.p.selarrrow=[]}else if(f.p.selrow){b("#"+f.p.id+" tbody:first tr#"+b.jgrid.jqID(f.p.selrow)).removeClass("ui-state-highlight").attr("aria-selected",
"false");f.p.selrow=null}f.p.savedRow=[]})},getRowData:function(f){var j={},i,c=false,e,k=0;this.each(function(){var l=this,a,r;if(typeof f=="undefined"){c=true;i=[];e=l.rows.length}else{r=l.rows.namedItem(f);if(!r)return j;e=2}for(;k<e;){if(c)r=l.rows[k];if(b(r).hasClass("jqgrow")){b("td",r).each(function(u){a=l.p.colModel[u].name;if(a!=="cb"&&a!=="subgrid"&&a!=="rn")if(l.p.treeGrid===true&&a==l.p.ExpandColumn)j[a]=b.jgrid.htmlDecode(b("span:first",this).html());else try{j[a]=b.unformat(this,{rowId:r.id,
colModel:l.p.colModel[u]},u)}catch(B){j[a]=b.jgrid.htmlDecode(b(this).html())}});if(c){i.push(j);j={}}}k++}});return i?i:j},delRowData:function(f){var j=false,i,c;this.each(function(){var e=this;if(i=e.rows.namedItem(f)){b(i).remove();e.p.records--;e.p.reccount--;e.updatepager(true,false);j=true;if(e.p.multiselect){c=b.inArray(f,e.p.selarrrow);c!=-1&&e.p.selarrrow.splice(c,1)}if(f==e.p.selrow)e.p.selrow=null}else return false;if(e.p.datatype=="local"){var k=e.p._index[f];if(typeof k!="undefined"){e.p.data.splice(k,
1);e.refreshIndex()}}if(e.p.altRows===true&&j){var l=e.p.altclass;b(e.rows).each(function(a){a%2==1?b(this).addClass(l):b(this).removeClass(l)})}});return j},setRowData:function(f,j,i){var c,e=true,k;this.each(function(){if(!this.grid)return false;var l=this,a,r,u=typeof i,B={};r=l.rows.namedItem(f);if(!r)return false;if(j)try{b(this.p.colModel).each(function(J){c=this.name;if(j[c]!==undefined){B[c]=this.formatter&&typeof this.formatter==="string"&&this.formatter=="date"?b.unformat.date(j[c],this):
j[c];a=l.formatter(f,j[c],J,j,"edit");k=this.title?{title:b.jgrid.stripHtml(a)}:{};l.p.treeGrid===true&&c==l.p.ExpandColumn?b("td:eq("+J+") > span:first",r).html(a).attr(k):b("td:eq("+J+")",r).html(a).attr(k)}});if(l.p.datatype=="local"){var G=l.p._index[f];if(typeof G!="undefined")l.p.data[G]=b.extend(true,l.p.data[G],B);B=null}}catch(Q){e=false}if(e)if(u==="string")b(r).addClass(i);else u==="object"&&b(r).css(i)});return e},addRowData:function(f,j,i,c){i||(i="last");var e=false,k,l,a,r,u,B,G,Q,
J="",M,n,m,p,A;if(j){if(b.isArray(j)){M=true;i="last";n=f}else{j=[j];M=false}this.each(function(){var t=this,H=j.length;u=t.p.rownumbers===true?1:0;a=t.p.multiselect===true?1:0;r=t.p.subGrid===true?1:0;if(!M)if(typeof f!="undefined")f+="";else{f=t.p.records+1+"";if(t.p.keyIndex!==false){n=t.p.colModel[t.p.keyIndex+a+r+u].name;if(typeof j[0][n]!="undefined")f=j[0][n]}}m=t.p.altclass;for(var S=0,Y="",O={},fa=b.isFunction(t.p.afterInsertRow)?true:false;S<H;){p=j[S];l="";if(M){try{f=p[n]}catch(ca){f=
t.p.records+1+""}Y=t.p.altRows===true?(t.rows.length-1)%2===0?m:"":""}if(u){J=t.formatCol(0,1,"");l+='<td role="gridcell" aria-describedby="'+t.p.id+'_rn" class="ui-state-default jqgrid-rownum" '+J+">0</td>"}if(a){Q='<input role="checkbox" type="checkbox" id="jqg_'+t.p.id+"_"+f+'" class="cbox"/>';J=t.formatCol(u,1,"");l+='<td role="gridcell" aria-describedby="'+t.p.id+'_cb" '+J+">"+Q+"</td>"}if(r)l+=b(t).jqGrid("addSubGridCell",a+u,1);for(G=a+r+u;G<t.p.colModel.length;G++){A=t.p.colModel[G];k=A.name;
O[k]=A.formatter&&typeof A.formatter==="string"&&A.formatter=="date"?b.unformat.date(p[k],A):p[k];Q=t.formatter(f,b.jgrid.getAccessor(p,k),G,p,"edit");J=t.formatCol(G,1,Q);l+='<td role="gridcell" aria-describedby="'+t.p.id+"_"+k+'" '+J+">"+Q+"</td>"}l='<tr id="'+f+'" role="row" class="ui-widget-content jqgrow ui-row-'+t.p.direction+" "+Y+'">'+l+"</tr>";if(t.p.subGrid===true){l=b(l)[0];b(t).jqGrid("addSubGrid",l,a+u)}if(t.rows.length===0)b("table:first",t.grid.bDiv).append(l);else switch(i){case "last":b(t.rows[t.rows.length-
1]).after(l);break;case "first":b(t.rows[0]).after(l);break;case "after":if(B=t.rows.namedItem(c))b(t.rows[B.rowIndex+1]).hasClass("ui-subgrid")?b(t.rows[B.rowIndex+1]).after(l):b(B).after(l);break;case "before":if(B=t.rows.namedItem(c)){b(B).before(l);B=B.rowIndex}break}t.p.records++;t.p.reccount++;fa&&t.p.afterInsertRow.call(t,f,p,p);S++;if(t.p.datatype=="local"){t.p._index[f]=t.p.data.length;t.p.data.push(O);O={}}}if(t.p.altRows===true&&!M)if(i=="last")(t.rows.length-1)%2==1&&b(t.rows[t.rows.length-
1]).addClass(m);else b(t.rows).each(function(P){P%2==1?b(this).addClass(m):b(this).removeClass(m)});t.updatepager(true,true);e=true})}return e},footerData:function(f,j,i){function c(r){for(var u in r)if(r.hasOwnProperty(u))return false;return true}var e,k=false,l={},a;if(typeof f=="undefined")f="get";if(typeof i!="boolean")i=true;f=f.toLowerCase();this.each(function(){var r=this,u;if(!r.grid||!r.p.footerrow)return false;if(f=="set")if(c(j))return false;k=true;b(this.p.colModel).each(function(B){e=
this.name;if(f=="set"){if(j[e]!==undefined){u=i?r.formatter("",j[e],B,j,"edit"):j[e];a=this.title?{title:b.jgrid.stripHtml(u)}:{};b("tr.footrow td:eq("+B+")",r.grid.sDiv).html(u).attr(a);k=true}}else if(f=="get")l[e]=b("tr.footrow td:eq("+B+")",r.grid.sDiv).html()})});return f=="get"?l:k},ShowHideCol:function(f,j){return this.each(function(){var i=this,c=false;if(i.grid){if(typeof f==="string")f=[f];j=j!="none"?"":"none";var e=j===""?true:false;b(this.p.colModel).each(function(k){if(b.inArray(this.name,
f)!==-1&&this.hidden===e){b("tr",i.grid.hDiv).each(function(){b("th:eq("+k+")",this).css("display",j)});b(i.rows).each(function(l){b("td:eq("+k+")",i.rows[l]).css("display",j)});i.p.footerrow&&b("td:eq("+k+")",i.grid.sDiv).css("display",j);if(j=="none")i.p.tblwidth-=this.width+i.p.cellLayout;else i.p.tblwidth+=this.width;this.hidden=!e;c=true}});if(c===true){b("table:first",i.grid.hDiv).width(i.p.tblwidth);b("table:first",i.grid.bDiv).width(i.p.tblwidth);i.grid.hDiv.scrollLeft=i.grid.bDiv.scrollLeft;
if(i.p.footerrow){b("table:first",i.grid.sDiv).width(i.p.tblwidth);i.grid.sDiv.scrollLeft=i.grid.bDiv.scrollLeft}i.p.shrinkToFit===true&&b(i).jqGrid("setGridWidth",i.grid.width-0.0010,true)}}})},hideCol:function(f){return this.each(function(){b(this).jqGrid("ShowHideCol",f,"none")})},showCol:function(f){return this.each(function(){b(this).jqGrid("ShowHideCol",f,"")})},remapColumns:function(f,j,i){function c(l){var a;a=l.length?b.makeArray(l):b.extend({},l);b.each(f,function(r){l[r]=a[this]})}function e(l,
a){b(">tr"+(a||""),l).each(function(){var r=this,u=b.makeArray(r.cells);b.each(f,function(){var B=u[this];B&&r.appendChild(B)})})}var k=this.get(0);c(k.p.colModel);c(k.p.colNames);c(k.grid.headers);e(b("thead:first",k.grid.hDiv),i&&":not(.ui-jqgrid-labels)");j&&e(b("#"+k.p.id+" tbody:first"),".jqgfirstrow, tr.jqgrow, tr.jqfoot");k.p.footerrow&&e(b("tbody:first",k.grid.sDiv));if(k.p.remapColumns)if(k.p.remapColumns.length)c(k.p.remapColumns);else k.p.remapColumns=b.makeArray(f);k.p.lastsort=b.inArray(k.p.lastsort,
f);if(k.p.treeGrid)k.p.expColInd=b.inArray(k.p.expColInd,f)},setGridWidth:function(f,j){return this.each(function(){if(this.grid){var i=this,c,e=0,k=i.p.cellLayout,l,a=0,r=false,u=i.p.scrollOffset,B,G=0,Q=0,J=0,M;if(typeof j!="boolean")j=i.p.shrinkToFit;if(!isNaN(f)){f=parseInt(f,10);i.grid.width=i.p.width=f;b("#gbox_"+i.p.id).css("width",f+"px");b("#gview_"+i.p.id).css("width",f+"px");b(i.grid.bDiv).css("width",f+"px");b(i.grid.hDiv).css("width",f+"px");i.p.pager&&b(i.p.pager).css("width",f+"px");
i.p.toppager&&b(i.p.toppager).css("width",f+"px");if(i.p.toolbar[0]===true){b(i.grid.uDiv).css("width",f+"px");i.p.toolbar[1]=="both"&&b(i.grid.ubDiv).css("width",f+"px")}i.p.footerrow&&b(i.grid.sDiv).css("width",f+"px");if(j===false&&i.p.forceFit===true)i.p.forceFit=false;if(j===true){if(b.browser.safari)k=0;b.each(i.p.colModel,function(){if(this.hidden===false){e+=parseInt(this.width,10);if(this.fixed){Q+=this.width;G+=this.width+k}else a++;J++}});if(a!==0){i.p.tblwidth=e;B=f-k*a-G;if(!isNaN(i.p.height))if(b(i.grid.bDiv)[0].clientHeight<
b(i.grid.bDiv)[0].scrollHeight||i.rows.length===1){r=true;B-=u}e=0;var n=i.grid.cols.length>0;b.each(i.p.colModel,function(m){if(this.hidden===false&&!this.fixed){c=Math.round(B*this.width/(i.p.tblwidth-Q));if(!(c<0)){this.width=c;e+=c;i.grid.headers[m].width=c;i.grid.headers[m].el.style.width=c+"px";if(i.p.footerrow)i.grid.footers[m].style.width=c+"px";if(n)i.grid.cols[m].style.width=c+"px";l=m}}});M=0;if(r){if(f-G-(e+k*a)!==u)M=f-G-(e+k*a)-u}else if(Math.abs(f-G-(e+k*a))!==1)M=f-G-(e+k*a);i.p.colModel[l].width+=
M;i.p.tblwidth=e+M+Q+k*J;if(i.p.tblwidth>f){r=i.p.tblwidth-parseInt(f,10);i.p.tblwidth=f;c=i.p.colModel[l].width-=r}else c=i.p.colModel[l].width;i.grid.headers[l].width=c;i.grid.headers[l].el.style.width=c+"px";if(n)i.grid.cols[l].style.width=c+"px";b("table:first",i.grid.bDiv).css("width",i.p.tblwidth+"px");b("table:first",i.grid.hDiv).css("width",i.p.tblwidth+"px");i.grid.hDiv.scrollLeft=i.grid.bDiv.scrollLeft;if(i.p.footerrow){i.grid.footers[l].style.width=c+"px";b("table:first",i.grid.sDiv).css("width",
i.p.tblwidth+"px")}}}}}})},setGridHeight:function(f){return this.each(function(){var j=this;if(j.grid){b(j.grid.bDiv).css({height:f+(isNaN(f)?"":"px")});j.p.height=f;j.p.scroll&&j.grid.populateVisible()}})},setCaption:function(f){return this.each(function(){this.p.caption=f;b("span.ui-jqgrid-title",this.grid.cDiv).html(f);b(this.grid.cDiv).show()})},setLabel:function(f,j,i,c){return this.each(function(){var e=this,k=-1;if(e.grid){if(isNaN(f))b(e.p.colModel).each(function(r){if(this.name==f){k=r;return false}});
else k=parseInt(f,10);if(k>=0){var l=b("tr.ui-jqgrid-labels th:eq("+k+")",e.grid.hDiv);if(j){var a=b(".s-ico",l);b("[id^=jqgh_]",l).empty().html(j).append(a);e.p.colNames[k]=j}if(i)typeof i==="string"?b(l).addClass(i):b(l).css(i);typeof c==="object"&&b(l).attr(c)}}})},setCell:function(f,j,i,c,e,k){return this.each(function(){var l=this,a=-1,r,u;if(l.grid){if(isNaN(j))b(l.p.colModel).each(function(G){if(this.name==j){a=G;return false}});else a=parseInt(j,10);if(a>=0)if(r=l.rows.namedItem(f)){var B=
b("td:eq("+a+")",r);if(i!==""||k===true){r=l.formatter(f,i,a,r,"edit");u=l.p.colModel[a].title?{title:b.jgrid.stripHtml(r)}:{};l.p.treeGrid&&b(".tree-wrap",b(B)).length>0?b("span",b(B)).html(r).attr(u):b(B).html(r).attr(u);if(l.p.datatype=="local"){r=l.p.colModel[a];i=r.formatter&&typeof r.formatter==="string"&&r.formatter=="date"?b.unformat.date(i,r):i;if(u=l.p._index[f])l.p.data[u][r.name]=i}}if(typeof c==="string")b(B).addClass(c);else c&&b(B).css(c);typeof e==="object"&&b(B).attr(e)}}})},getCell:function(f,
j){var i=false;this.each(function(){var c=this,e=-1;if(c.grid){if(isNaN(j))b(c.p.colModel).each(function(a){if(this.name===j){e=a;return false}});else e=parseInt(j,10);if(e>=0){var k=c.rows.namedItem(f);if(k)try{i=b.unformat(b("td:eq("+e+")",k),{rowId:k.id,colModel:c.p.colModel[e]},e)}catch(l){i=b.jgrid.htmlDecode(b("td:eq("+e+")",k).html())}}}});return i},getCol:function(f,j,i){var c=[],e,k=0;j=typeof j!="boolean"?false:j;if(typeof i=="undefined")i=false;this.each(function(){var l=this,a=-1;if(l.grid){if(isNaN(f))b(l.p.colModel).each(function(G){if(this.name===
f){a=G;return false}});else a=parseInt(f,10);if(a>=0){var r=l.rows.length,u=0;if(r&&r>0){for(;u<r;){if(b(l.rows[u]).hasClass("jqgrow")){try{e=b.unformat(b(l.rows[u].cells[a]),{rowId:l.rows[u].id,colModel:l.p.colModel[a]},a)}catch(B){e=b.jgrid.htmlDecode(l.rows[u].cells[a].innerHTML)}if(i)k+=parseFloat(e);else if(j)c.push({id:l.rows[u].id,value:e});else c[u]=e}u++}if(i)switch(i.toLowerCase()){case "sum":c=k;break;case "avg":c=k/r;break;case "count":c=r;break}}}}});return c},clearGridData:function(f){return this.each(function(){var j=
this;if(j.grid){if(typeof f!="boolean")f=false;if(j.p.deepempty)b("#"+j.p.id+" tbody:first tr:gt(0)").remove();else{var i=b("#"+j.p.id+" tbody:first tr:first")[0];b("#"+j.p.id+" tbody:first").empty().append(i)}j.p.footerrow&&f&&b(".ui-jqgrid-ftable td",j.grid.sDiv).html("&#160;");j.p.selrow=null;j.p.selarrrow=[];j.p.savedRow=[];j.p.records=0;j.p.page=1;j.p.lastpage=0;j.p.reccount=0;j.p.data=[];j.p_index={};j.updatepager(true,false)}})},getInd:function(f,j){var i=false,c;this.each(function(){if(c=
this.rows.namedItem(f))i=j===true?c:c.rowIndex});return i}})})(jQuery);
(function(c){c.fmatter={};c.extend(c.fmatter,{isBoolean:function(a){return typeof a==="boolean"},isObject:function(a){return a&&(typeof a==="object"||c.isFunction(a))||false},isString:function(a){return typeof a==="string"},isNumber:function(a){return typeof a==="number"&&isFinite(a)},isNull:function(a){return a===null},isUndefined:function(a){return typeof a==="undefined"},isValue:function(a){return this.isObject(a)||this.isString(a)||this.isNumber(a)||this.isBoolean(a)},isEmpty:function(a){if(!this.isString(a)&&
this.isValue(a))return false;else if(!this.isValue(a))return true;a=c.trim(a).replace(/\&nbsp\;/ig,"").replace(/\&#160\;/ig,"");return a===""}});c.fn.fmatter=function(a,b,d,e,h){var g=b;d=c.extend({},c.jgrid.formatter,d);if(c.fn.fmatter[a])g=c.fn.fmatter[a](b,d,e,h);return g};c.fmatter.util={NumberFormat:function(a,b){c.fmatter.isNumber(a)||(a*=1);if(c.fmatter.isNumber(a)){var d=a<0,e=a+"",h=b.decimalSeparator?b.decimalSeparator:".";if(c.fmatter.isNumber(b.decimalPlaces)){var g=b.decimalPlaces;e=
Math.pow(10,g);e=Math.round(a*e)/e+"";a=e.lastIndexOf(".");if(g>0){if(a<0){e+=h;a=e.length-1}else if(h!==".")e=e.replace(".",h);for(;e.length-1-a<g;)e+="0"}}if(b.thousandsSeparator){g=b.thousandsSeparator;a=e.lastIndexOf(h);a=a>-1?a:e.length;h=e.substring(a);for(var f=-1,i=a;i>0;i--){f++;if(f%3===0&&i!==a&&(!d||i>1))h=g+h;h=e.charAt(i-1)+h}e=h}e=b.prefix?b.prefix+e:e;return e=b.suffix?e+b.suffix:e}else return a},DateFormat:function(a,b,d,e){var h=function(m,r){m=String(m);for(r=parseInt(r,10)||2;m.length<
r;)m="0"+m;return m},g={m:1,d:1,y:1970,h:0,i:0,s:0,u:0},f=0,i,j,k=["i18n"];k.i18n={dayNames:e.dayNames,monthNames:e.monthNames};if(a in e.masks)a=e.masks[a];if(b.constructor===Number)f=new Date(b);else if(b.constructor===Date)f=b;else{b=b.split(/[\\\/:_;.,\t\T\s-]/);a=a.split(/[\\\/:_;.,\t\T\s-]/);i=0;for(j=a.length;i<j;i++){if(a[i]=="M"){f=c.inArray(b[i],k.i18n.monthNames);if(f!==-1&&f<12)b[i]=f+1}if(a[i]=="F"){f=c.inArray(b[i],k.i18n.monthNames);if(f!==-1&&f>11)b[i]=f+1-12}if(b[i])g[a[i].toLowerCase()]=
parseInt(b[i],10)}if(g.f)g.m=g.f;if(g.m===0&&g.y===0&&g.d===0)return"&#160;";g.m=parseInt(g.m,10)-1;f=g.y;if(f>=70&&f<=99)g.y=1900+g.y;else if(f>=0&&f<=69)g.y=2E3+g.y;f=new Date(g.y,g.m,g.d,g.h,g.i,g.s,g.u)}if(d in e.masks)d=e.masks[d];else d||(d="Y-m-d");g=f.getHours();a=f.getMinutes();b=f.getDate();i=f.getMonth()+1;j=f.getTimezoneOffset();var l=f.getSeconds(),o=f.getMilliseconds(),n=f.getDay(),p=f.getFullYear(),q=(n+6)%7+1,s=(new Date(p,i-1,b)-new Date(p,0,1))/864E5,t={d:h(b),D:k.i18n.dayNames[n],
j:b,l:k.i18n.dayNames[n+7],N:q,S:e.S(b),w:n,z:s,W:q<5?Math.floor((s+q-1)/7)+1:Math.floor((s+q-1)/7)||(((new Date(p-1,0,1)).getDay()+6)%7<4?53:52),F:k.i18n.monthNames[i-1+12],m:h(i),M:k.i18n.monthNames[i-1],n:i,t:"?",L:"?",o:"?",Y:p,y:String(p).substring(2),a:g<12?e.AmPm[0]:e.AmPm[1],A:g<12?e.AmPm[2]:e.AmPm[3],B:"?",g:g%12||12,G:g,h:h(g%12||12),H:h(g),i:h(a),s:h(l),u:o,e:"?",I:"?",O:(j>0?"-":"+")+h(Math.floor(Math.abs(j)/60)*100+Math.abs(j)%60,4),P:"?",T:(String(f).match(/\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g)||
[""]).pop().replace(/[^-+\dA-Z]/g,""),Z:"?",c:"?",r:"?",U:Math.floor(f/1E3)};return d.replace(/\\.|[dDjlNSwzWFmMntLoYyaABgGhHisueIOPTZcrU]/g,function(m){return m in t?t[m]:m.substring(1)})}};c.fn.fmatter.defaultFormat=function(a,b){return c.fmatter.isValue(a)&&a!==""?a:b.defaultValue?b.defaultValue:"&#160;"};c.fn.fmatter.email=function(a,b){return c.fmatter.isEmpty(a)?c.fn.fmatter.defaultFormat(a,b):'<a href="mailto:'+a+'">'+a+"</a>"};c.fn.fmatter.checkbox=function(a,b){var d=c.extend({},b.checkbox);
c.fmatter.isUndefined(b.colModel.formatoptions)||(d=c.extend({},d,b.colModel.formatoptions));b=d.disabled===true?"disabled":"";if(c.fmatter.isEmpty(a)||c.fmatter.isUndefined(a))a=c.fn.fmatter.defaultFormat(a,d);a+="";a=a.toLowerCase();return'<input type="checkbox" '+(a.search(/(false|0|no|off)/i)<0?" checked='checked' ":"")+' value="'+a+'" offval="no" '+b+"/>"};c.fn.fmatter.link=function(a,b){var d={target:b.target},e="";c.fmatter.isUndefined(b.colModel.formatoptions)||(d=c.extend({},d,b.colModel.formatoptions));
if(d.target)e="target="+d.target;return c.fmatter.isEmpty(a)?c.fn.fmatter.defaultFormat(a,b):"<a "+e+' href="'+a+'">'+a+"</a>"};c.fn.fmatter.showlink=function(a,b){var d={baseLinkUrl:b.baseLinkUrl,showAction:b.showAction,addParam:b.addParam||"",target:b.target,idName:b.idName},e="";c.fmatter.isUndefined(b.colModel.formatoptions)||(d=c.extend({},d,b.colModel.formatoptions));if(d.target)e="target="+d.target;d=d.baseLinkUrl+d.showAction+"?"+d.idName+"="+b.rowId+d.addParam;return c.fmatter.isString(a)||
c.fmatter.isNumber(a)?"<a "+e+' href="'+d+'">'+a+"</a>":c.fn.fmatter.defaultFormat(a,b)};c.fn.fmatter.integer=function(a,b){var d=c.extend({},b.integer);c.fmatter.isUndefined(b.colModel.formatoptions)||(d=c.extend({},d,b.colModel.formatoptions));if(c.fmatter.isEmpty(a))return d.defaultValue;return c.fmatter.util.NumberFormat(a,d)};c.fn.fmatter.number=function(a,b){var d=c.extend({},b.number);c.fmatter.isUndefined(b.colModel.formatoptions)||(d=c.extend({},d,b.colModel.formatoptions));if(c.fmatter.isEmpty(a))return d.defaultValue;
return c.fmatter.util.NumberFormat(a,d)};c.fn.fmatter.currency=function(a,b){var d=c.extend({},b.currency);c.fmatter.isUndefined(b.colModel.formatoptions)||(d=c.extend({},d,b.colModel.formatoptions));if(c.fmatter.isEmpty(a))return d.defaultValue;return c.fmatter.util.NumberFormat(a,d)};c.fn.fmatter.date=function(a,b,d,e){d=c.extend({},b.date);c.fmatter.isUndefined(b.colModel.formatoptions)||(d=c.extend({},d,b.colModel.formatoptions));return!d.reformatAfterEdit&&e=="edit"?c.fn.fmatter.defaultFormat(a,
b):c.fmatter.isEmpty(a)?c.fn.fmatter.defaultFormat(a,b):c.fmatter.util.DateFormat(d.srcformat,a,d.newformat,d)};c.fn.fmatter.select=function(a,b){a+="";var d=false,e=[];if(c.fmatter.isUndefined(b.colModel.formatoptions)){if(!c.fmatter.isUndefined(b.colModel.editoptions))d=b.colModel.editoptions.value}else d=b.colModel.formatoptions.value;if(d){var h=b.colModel.editoptions.multiple===true?true:false,g=[],f;if(h){g=a.split(",");g=c.map(g,function(l){return c.trim(l)})}if(c.fmatter.isString(d))for(var i=
d.split(";"),j=0,k=0;k<i.length;k++){f=i[k].split(":");if(f.length>2)f[1]=jQuery.map(f,function(l,o){if(o>0)return l}).join(":");if(h){if(jQuery.inArray(f[0],g)>-1){e[j]=f[1];j++}}else if(c.trim(f[0])==c.trim(a)){e[0]=f[1];break}}else if(c.fmatter.isObject(d))if(h)e=jQuery.map(g,function(l){return d[l]});else e[0]=d[a]||""}a=e.join(", ");return a===""?c.fn.fmatter.defaultFormat(a,b):a};c.fn.fmatter.rowactions=function(a,b,d,e){switch(d){case "edit":d=function(){c("tr#"+a+" div.ui-inline-edit, tr#"+
a+" div.ui-inline-del","#"+b).show();c("tr#"+a+" div.ui-inline-save, tr#"+a+" div.ui-inline-cancel","#"+b).hide()};c("#"+b).jqGrid("editRow",a,e,null,null,null,{oper:"edit"},d,null,d);c("tr#"+a+" div.ui-inline-edit, tr#"+a+" div.ui-inline-del","#"+b).hide();c("tr#"+a+" div.ui-inline-save, tr#"+a+" div.ui-inline-cancel","#"+b).show();break;case "save":c("#"+b).jqGrid("saveRow",a,null,null);c("tr#"+a+" div.ui-inline-edit, tr#"+a+" div.ui-inline-del","#"+b).show();c("tr#"+a+" div.ui-inline-save, tr#"+
a+" div.ui-inline-cancel","#"+b).hide();break;case "cancel":c("#"+b).jqGrid("restoreRow",a);c("tr#"+a+" div.ui-inline-edit, tr#"+a+" div.ui-inline-del","#"+b).show();c("tr#"+a+" div.ui-inline-save, tr#"+a+" div.ui-inline-cancel","#"+b).hide();break}};c.fn.fmatter.actions=function(a,b){a={keys:false,editbutton:true,delbutton:true};c.fmatter.isUndefined(b.colModel.formatoptions)||(a=c.extend(a,b.colModel.formatoptions));var d=b.rowId,e="",h;if(typeof d=="undefined"||c.fmatter.isEmpty(d))return"";if(a.editbutton){h=
"onclick=$.fn.fmatter.rowactions('"+d+"','"+b.gid+"','edit',"+a.keys+");";e=e+"<div style='margin-left:8px;'><div title='"+c.jgrid.nav.edittitle+"' style='float:left;cursor:pointer;' class='ui-pg-div ui-inline-edit' "+h+"><span class='ui-icon ui-icon-pencil'></span></div>"}if(a.delbutton){h="onclick=jQuery('#"+b.gid+"').jqGrid('delGridRow','"+d+"');";e=e+"<div title='"+c.jgrid.nav.deltitle+"' style='float:left;margin-left:5px;' class='ui-pg-div ui-inline-del' "+h+"><span class='ui-icon ui-icon-trash'></span></div>"}h=
"onclick=$.fn.fmatter.rowactions('"+d+"','"+b.gid+"','save',false);";e=e+"<div title='"+c.jgrid.edit.bSubmit+"' style='float:left;display:none' class='ui-pg-div ui-inline-save'><span class='ui-icon ui-icon-disk' "+h+"></span></div>";h="onclick=$.fn.fmatter.rowactions('"+d+"','"+b.gid+"','cancel',false);";return e=e+"<div title='"+c.jgrid.edit.bCancel+"' style='float:left;display:none;margin-left:5px;' class='ui-pg-div ui-inline-cancel'><span class='ui-icon ui-icon-cancel' "+h+"></span></div></div>"};
c.unformat=function(a,b,d,e){var h,g=b.colModel.formatter,f=b.colModel.formatoptions||{},i=/([\.\*\_\'\(\)\{\}\+\?\\])/g,j=b.colModel.unformat||c.fn.fmatter[g]&&c.fn.fmatter[g].unformat;if(typeof j!=="undefined"&&c.isFunction(j))h=j(c(a).text(),b,a);else if(!c.fmatter.isUndefined(g)&&c.fmatter.isString(g)){h=c.jgrid.formatter||{};switch(g){case "integer":f=c.extend({},h.integer,f);b=f.thousandsSeparator.replace(i,"\\$1");b=new RegExp(b,"g");h=c(a).text().replace(b,"");break;case "number":f=c.extend({},
h.number,f);b=f.thousandsSeparator.replace(i,"\\$1");b=new RegExp(b,"g");h=c(a).text().replace(b,"").replace(f.decimalSeparator,".");break;case "currency":f=c.extend({},h.currency,f);b=f.thousandsSeparator.replace(i,"\\$1");b=new RegExp(b,"g");h=c(a).text().replace(b,"").replace(f.decimalSeparator,".").replace(f.prefix,"").replace(f.suffix,"");break;case "checkbox":f=b.colModel.editoptions?b.colModel.editoptions.value.split(":"):["Yes","No"];h=c("input",a).attr("checked")?f[0]:f[1];break;case "select":h=
c.unformat.select(a,b,d,e);break;case "actions":return"";default:h=c(a).text()}}return h?h:e===true?c(a).text():c.jgrid.htmlDecode(c(a).html())};c.unformat.select=function(a,b,d,e){d=[];a=c(a).text();if(e===true)return a;b=c.extend({},b.colModel.editoptions);if(b.value){var h=b.value;b=b.multiple===true?true:false;e=[];var g;if(b){e=a.split(",");e=c.map(e,function(k){return c.trim(k)})}if(c.fmatter.isString(h))for(var f=h.split(";"),i=0,j=0;j<f.length;j++){g=f[j].split(":");if(g.length>2)g[1]=jQuery.map(g,
function(k,l){if(l>0)return k}).join(":");if(b){if(jQuery.inArray(g[1],e)>-1){d[i]=g[0];i++}}else if(c.trim(g[1])==c.trim(a)){d[0]=g[0];break}}else if(c.fmatter.isObject(h)||c.isArray(h)){b||(e[0]=a);d=jQuery.map(e,function(k){var l;c.each(h,function(o,n){if(n==k){l=o;return false}});if(typeof l!="undefined")return l})}return d.join(", ")}else return a||""};c.unformat.date=function(a,b){var d=c.jgrid.formatter.date||{};c.fmatter.isUndefined(b.formatoptions)||(d=c.extend({},d,b.formatoptions));return c.fmatter.isEmpty(a)?
c.fn.fmatter.defaultFormat(a,b):c.fmatter.util.DateFormat(d.newformat,a,d.srcformat,d)}})(jQuery);
(function(a){a.jgrid.extend({getColProp:function(f){var d={},b=this[0];if(b.grid){b=b.p.colModel;for(var n=0;n<b.length;n++)if(b[n].name==f){d=b[n];break}return d}},setColProp:function(f,d){return this.each(function(){if(this.grid)if(d)for(var b=this.p.colModel,n=0;n<b.length;n++)if(b[n].name==f){a.extend(this.p.colModel[n],d);break}})},sortGrid:function(f,d,b){return this.each(function(){var n=this,s=-1;if(n.grid){if(!f)f=n.p.sortname;for(var q=0;q<n.p.colModel.length;q++)if(n.p.colModel[q].index==
f||n.p.colModel[q].name==f){s=q;break}if(s!=-1){q=n.p.colModel[s].sortable;if(typeof q!=="boolean")q=true;if(typeof d!=="boolean")d=false;q&&n.sortData("jqgh_"+f,s,d,b)}}})},GridDestroy:function(){return this.each(function(){if(this.grid){this.p.pager&&a(this.p.pager).remove();var f=this.id;try{a("#gbox_"+f).remove()}catch(d){}}})},GridUnload:function(){return this.each(function(){if(this.grid){var f={id:a(this).attr("id"),cl:a(this).attr("class")};this.p.pager&&a(this.p.pager).empty().removeClass("ui-state-default ui-jqgrid-pager corner-bottom");
var d=document.createElement("table");a(d).attr({id:f.id});d.className=f.cl;f=this.id;a(d).removeClass("ui-jqgrid-btable");if(a(this.p.pager).parents("#gbox_"+f).length===1){a(d).insertBefore("#gbox_"+f).show();a(this.p.pager).insertBefore("#gbox_"+f)}else a(d).insertBefore("#gbox_"+f).show();a("#gbox_"+f).remove()}})},setGridState:function(f){return this.each(function(){if(this.grid){var d=this;if(f=="hidden"){a(".ui-jqgrid-bdiv, .ui-jqgrid-hdiv","#gview_"+d.p.id).slideUp("fast");d.p.pager&&a(d.p.pager).slideUp("fast");
d.p.toppager&&a(d.p.toppager).slideUp("fast");if(d.p.toolbar[0]===true){d.p.toolbar[1]=="both"&&a(d.grid.ubDiv).slideUp("fast");a(d.grid.uDiv).slideUp("fast")}d.p.footerrow&&a(".ui-jqgrid-sdiv","#gbox_"+d.p.id).slideUp("fast");a(".ui-jqgrid-titlebar-close span",d.grid.cDiv).removeClass("ui-icon-circle-triangle-n").addClass("ui-icon-circle-triangle-s");d.p.gridstate="hidden"}else if(f=="visible"){a(".ui-jqgrid-hdiv, .ui-jqgrid-bdiv","#gview_"+d.p.id).slideDown("fast");d.p.pager&&a(d.p.pager).slideDown("fast");
d.p.toppager&&a(d.p.toppager).slideDown("fast");if(d.p.toolbar[0]===true){d.p.toolbar[1]=="both"&&a(d.grid.ubDiv).slideDown("fast");a(d.grid.uDiv).slideDown("fast")}d.p.footerrow&&a(".ui-jqgrid-sdiv","#gbox_"+d.p.id).slideDown("fast");a(".ui-jqgrid-titlebar-close span",d.grid.cDiv).removeClass("ui-icon-circle-triangle-s").addClass("ui-icon-circle-triangle-n");d.p.gridstate="visible"}}})},updateGridRows:function(f,d,b){var n,s=false,q;this.each(function(){var h=this,l,o,c,g;if(!h.grid)return false;
d||(d="id");f&&f.length>0&&a(f).each(function(){c=this;if(o=h.rows.namedItem(c[d])){g=c[d];if(b===true)if(h.p.jsonReader.repeatitems===true){if(h.p.jsonReader.cell)c=c[h.p.jsonReader.cell];for(var e=0;e<c.length;e++){l=h.formatter(g,c[e],e,c,"edit");q=h.p.colModel[e].title?{title:a.jgrid.stripHtml(l)}:{};h.p.treeGrid===true&&n==h.p.ExpandColumn?a("td:eq("+e+") > span:first",o).html(l).attr(q):a("td:eq("+e+")",o).html(l).attr(q)}return s=true}a(h.p.colModel).each(function(m){n=b===true?this.jsonmap||
this.name:this.name;if(c[n]!==undefined){l=h.formatter(g,c[n],m,c,"edit");q=this.title?{title:a.jgrid.stripHtml(l)}:{};h.p.treeGrid===true&&n==h.p.ExpandColumn?a("td:eq("+m+") > span:first",o).html(l).attr(q):a("td:eq("+m+")",o).html(l).attr(q);s=true}})}})});return s},filterGrid:function(f,d){d=a.extend({gridModel:false,gridNames:false,gridToolbar:false,filterModel:[],formtype:"horizontal",autosearch:true,formclass:"filterform",tableclass:"filtertable",buttonclass:"filterbutton",searchButton:"Search",
clearButton:"Clear",enableSearch:false,enableClear:false,beforeSearch:null,afterSearch:null,beforeClear:null,afterClear:null,url:"",marksearched:true},d||{});return this.each(function(){var b=this;this.p=d;if(this.p.filterModel.length===0&&this.p.gridModel===false)alert("No filter is set");else if(f){this.p.gridid=f.indexOf("#")!=-1?f:"#"+f;var n=a(this.p.gridid).jqGrid("getGridParam","colModel");if(n){if(this.p.gridModel===true){var s=a(this.p.gridid)[0],q;a.each(n,function(g){var e=[];this.search=
this.search===false?false:true;q=this.editrules&&this.editrules.searchhidden===true?true:this.hidden===true?false:true;if(this.search===true&&q===true){e.label=b.p.gridNames===true?s.p.colNames[g]:"";e.name=this.name;e.index=this.index||this.name;e.stype=this.edittype||"text";if(e.stype!="select")e.stype="text";e.defval=this.defval||"";e.surl=this.surl||"";e.sopt=this.editoptions||{};e.width=this.width;b.p.filterModel.push(e)}})}else a.each(b.p.filterModel,function(){for(var g=0;g<n.length;g++)if(this.name==
n[g].name){this.index=n[g].index||this.name;break}if(!this.index)this.index=this.name});var h=function(){var g={},e=0,m,i=a(b.p.gridid)[0],k;i.p.searchdata={};a.isFunction(b.p.beforeSearch)&&b.p.beforeSearch();a.each(b.p.filterModel,function(){k=this.index;switch(this.stype){case "select":if(m=a("select[name="+k+"]",b).val()){g[k]=m;b.p.marksearched&&a("#jqgh_"+this.name,i.grid.hDiv).addClass("dirty-cell");e++}else{b.p.marksearched&&a("#jqgh_"+this.name,i.grid.hDiv).removeClass("dirty-cell");try{delete i.p.postData[this.index]}catch(r){}}break;
default:if(m=a("input[name="+k+"]",b).val()){g[k]=m;b.p.marksearched&&a("#jqgh_"+this.name,i.grid.hDiv).addClass("dirty-cell");e++}else{b.p.marksearched&&a("#jqgh_"+this.name,i.grid.hDiv).removeClass("dirty-cell");try{delete i.p.postData[this.index]}catch(u){}}}});var p=e>0?true:false;a.extend(i.p.postData,g);var j;if(b.p.url){j=a(i).jqGrid("getGridParam","url");a(i).jqGrid("setGridParam",{url:b.p.url})}a(i).jqGrid("setGridParam",{search:p}).trigger("reloadGrid",[{page:1}]);j&&a(i).jqGrid("setGridParam",
{url:j});a.isFunction(b.p.afterSearch)&&b.p.afterSearch()},l=function(){var g={},e,m=0,i=a(b.p.gridid)[0],k;a.isFunction(b.p.beforeClear)&&b.p.beforeClear();a.each(b.p.filterModel,function(){k=this.index;e=this.defval?this.defval:"";if(!this.stype)this.stype="text";switch(this.stype){case "select":var r;a("select[name="+k+"] option",b).each(function(v){if(v===0)this.selected=true;if(a(this).text()==e){this.selected=true;r=a(this).val();return false}});if(r){g[k]=r;b.p.marksearched&&a("#jqgh_"+this.name,
i.grid.hDiv).addClass("dirty-cell");m++}else{b.p.marksearched&&a("#jqgh_"+this.name,i.grid.hDiv).removeClass("dirty-cell");try{delete i.p.postData[this.index]}catch(u){}}break;case "text":a("input[name="+k+"]",b).val(e);if(e){g[k]=e;b.p.marksearched&&a("#jqgh_"+this.name,i.grid.hDiv).addClass("dirty-cell");m++}else{b.p.marksearched&&a("#jqgh_"+this.name,i.grid.hDiv).removeClass("dirty-cell");try{delete i.p.postData[this.index]}catch(t){}}break}});var p=m>0?true:false;a.extend(i.p.postData,g);var j;
if(b.p.url){j=a(i).jqGrid("getGridParam","url");a(i).jqGrid("setGridParam",{url:b.p.url})}a(i).jqGrid("setGridParam",{search:p}).trigger("reloadGrid",[{page:1}]);j&&a(i).jqGrid("setGridParam",{url:j});a.isFunction(b.p.afterClear)&&b.p.afterClear()},o,c=a("<form name='SearchForm' style=display:inline;' class='"+this.p.formclass+"'></form>");o=a("<table class='"+this.p.tableclass+"' cellspacing='0' cellpading='0' border='0'><tbody></tbody></table>");a(c).append(o);(function(){var g=document.createElement("tr"),
e,m,i,k;b.p.formtype=="horizontal"&&a(o).append(g);a.each(b.p.filterModel,function(p){i=document.createElement("td");a(i).append("<label for='"+this.name+"'>"+this.label+"</label>");k=document.createElement("td");var j=this;if(!this.stype)this.stype="text";switch(this.stype){case "select":if(this.surl)a(k).load(this.surl,function(){j.defval&&a("select",this).val(j.defval);a("select",this).attr({name:j.index||j.name,id:"sg_"+j.name});j.sopt&&a("select",this).attr(j.sopt);b.p.gridToolbar===true&&j.width&&
a("select",this).width(j.width);b.p.autosearch===true&&a("select",this).change(function(){h();return false})});else if(j.sopt.value){var r=j.sopt.value,u=document.createElement("select");a(u).attr({name:j.index||j.name,id:"sg_"+j.name}).attr(j.sopt);var t;if(typeof r==="string"){p=r.split(";");for(var v=0;v<p.length;v++){r=p[v].split(":");t=document.createElement("option");t.value=r[0];t.innerHTML=r[1];if(r[1]==j.defval)t.selected="selected";u.appendChild(t)}}else if(typeof r==="object")for(v in r)if(r.hasOwnProperty(v)){p++;
t=document.createElement("option");t.value=v;t.innerHTML=r[v];if(r[v]==j.defval)t.selected="selected";u.appendChild(t)}b.p.gridToolbar===true&&j.width&&a(u).width(j.width);a(k).append(u);b.p.autosearch===true&&a(u).change(function(){h();return false})}break;case "text":u=this.defval?this.defval:"";a(k).append("<input type='text' name='"+(this.index||this.name)+"' id='sg_"+this.name+"' value='"+u+"'/>");j.sopt&&a("input",k).attr(j.sopt);if(b.p.gridToolbar===true&&j.width)a.browser.msie?a("input",k).width(j.width-
4):a("input",k).width(j.width-2);b.p.autosearch===true&&a("input",k).keypress(function(w){if((w.charCode?w.charCode:w.keyCode?w.keyCode:0)==13){h();return false}return this});break}if(b.p.formtype=="horizontal"){b.p.gridToolbar===true&&b.p.gridNames===false?a(g).append(k):a(g).append(i).append(k);a(g).append(k)}else{e=document.createElement("tr");a(e).append(i).append(k);a(o).append(e)}});k=document.createElement("td");if(b.p.enableSearch===true){m="<input type='button' id='sButton' class='"+b.p.buttonclass+
"' value='"+b.p.searchButton+"'/>";a(k).append(m);a("input#sButton",k).click(function(){h();return false})}if(b.p.enableClear===true){m="<input type='button' id='cButton' class='"+b.p.buttonclass+"' value='"+b.p.clearButton+"'/>";a(k).append(m);a("input#cButton",k).click(function(){l();return false})}if(b.p.enableClear===true||b.p.enableSearch===true)if(b.p.formtype=="horizontal")a(g).append(k);else{e=document.createElement("tr");a(e).append("<td>&#160;</td>").append(k);a(o).append(e)}})();a(this).append(c);
this.triggerSearch=h;this.clearSearch=l}else alert("Could not get grid colModel")}else alert("No target grid is set!")})},filterToolbar:function(f){f=a.extend({autosearch:true,searchOnEnter:true,beforeSearch:null,afterSearch:null,beforeClear:null,afterClear:null,searchurl:"",stringResult:false,groupOp:"AND",defaultSearch:"bw"},f||{});return this.each(function(){function d(h,l){var o=a(h);o[0]&&jQuery.each(l,function(){this.data!==undefined?o.bind(this.type,this.data,this.fn):o.bind(this.type,this.fn)})}
var b=this,n=function(){var h={},l=0,o,c,g={};a.each(b.p.colModel,function(){c=this.index||this.name;var j=this.searchoptions&&this.searchoptions.sopt?this.searchoptions.sopt[0]:f.defaultSearch;switch(this.stype){case "select":if(o=a("select[name="+c+"]",b.grid.hDiv).val()){h[c]=o;g[c]=j;l++}else try{delete b.p.postData[c]}catch(r){}break;case "text":if(o=a("input[name="+c+"]",b.grid.hDiv).val()){h[c]=o;g[c]=j;l++}else try{delete b.p.postData[c]}catch(u){}break}});var e=l>0?true:false;if(f.stringResult===
true||b.p.datatype=="local"){var m='{"groupOp":"'+f.groupOp+'","rules":[',i=0;a.each(h,function(j,r){if(i>0)m+=",";m+='{"field":"'+j+'",';m+='"op":"'+g[j]+'",';m+='"data":"'+r+'"}';i++});m+="]}";a.extend(b.p.postData,{filters:m})}else a.extend(b.p.postData,h);var k;if(b.p.searchurl){k=b.p.url;a(b).jqGrid("setGridParam",{url:b.p.searchurl})}var p=false;if(a.isFunction(f.beforeSearch))p=f.beforeSearch.call(b);p||a(b).jqGrid("setGridParam",{search:e}).trigger("reloadGrid",[{page:1}]);k&&a(b).jqGrid("setGridParam",
{url:k});a.isFunction(f.afterSearch)&&f.afterSearch()},s=a("<tr class='ui-search-toolbar' role='rowheader'></tr>"),q;a.each(b.p.colModel,function(){var h=this,l,o,c,g;o=a("<th role='columnheader' class='ui-state-default ui-th-column ui-th-"+b.p.direction+"'></th>");l=a("<div style='width:100%;position:relative;height:100%;padding-right:0.3em;'></div>");this.hidden===true&&a(o).css("display","none");this.search=this.search===false?false:true;if(typeof this.stype=="undefined")this.stype="text";c=a.extend({},
this.searchoptions||{});if(this.search)switch(this.stype){case "select":if(g=this.surl||c.dataUrl)a.ajax(a.extend({url:g,dataType:"html",complete:function(p){if(c.buildSelect!==undefined)(p=c.buildSelect(p))&&a(l).append(p);else a(l).append(p.responseText);c.defaultValue&&a("select",l).val(c.defaultValue);a("select",l).attr({name:h.index||h.name,id:"gs_"+h.name});c.attr&&a("select",l).attr(c.attr);a("select",l).css({width:"100%"});c.dataInit!==undefined&&c.dataInit(a("select",l)[0]);c.dataEvents!==
undefined&&d(a("select",l)[0],c.dataEvents);f.autosearch===true&&a("select",l).change(function(){n();return false});p=null}},a.jgrid.ajaxOptions,b.p.ajaxSelectOptions||{}));else{var e;if(h.searchoptions&&h.searchoptions.value)e=h.searchoptions.value;else if(h.editoptions&&h.editoptions.value)e=h.editoptions.value;if(e){g=document.createElement("select");g.style.width="100%";a(g).attr({name:h.index||h.name,id:"gs_"+h.name});var m,i;if(typeof e==="string"){e=e.split(";");for(var k=0;k<e.length;k++){m=
e[k].split(":");i=document.createElement("option");i.value=m[0];i.innerHTML=m[1];g.appendChild(i)}}else if(typeof e==="object")for(m in e)if(e.hasOwnProperty(m)){i=document.createElement("option");i.value=m;i.innerHTML=e[m];g.appendChild(i)}c.defaultValue&&a(g).val(c.defaultValue);c.attr&&a(g).attr(c.attr);c.dataInit!==undefined&&c.dataInit(g);c.dataEvents!==undefined&&d(g,c.dataEvents);a(l).append(g);f.autosearch===true&&a(g).change(function(){n();return false})}}break;case "text":g=c.defaultValue?
c.defaultValue:"";a(l).append("<input type='text' style='width:95%;padding:0px;' name='"+(h.index||h.name)+"' id='gs_"+h.name+"' value='"+g+"'/>");c.attr&&a("input",l).attr(c.attr);c.dataInit!==undefined&&c.dataInit(a("input",l)[0]);c.dataEvents!==undefined&&d(a("input",l)[0],c.dataEvents);if(f.autosearch===true)f.searchOnEnter?a("input",l).keypress(function(p){if((p.charCode?p.charCode:p.keyCode?p.keyCode:0)==13){n();return false}return this}):a("input",l).keydown(function(p){switch(p.which){case 13:return false;
case 9:case 16:case 37:case 38:case 39:case 40:case 27:break;default:q&&clearTimeout(q);q=setTimeout(function(){n()},500)}});break}a(o).append(l);a(s).append(o)});a("table thead",b.grid.hDiv).append(s);this.triggerToolbar=n;this.clearToolbar=function(h){var l={},o,c=0,g;h=typeof h!="boolean"?true:h;a.each(b.p.colModel,function(){o=this.searchoptions&&this.searchoptions.defaultValue?this.searchoptions.defaultValue:"";g=this.index||this.name;switch(this.stype){case "select":var j;a("select[name="+g+
"] option",b.grid.hDiv).each(function(t){if(t===0)this.selected=true;if(a(this).text()==o){this.selected=true;j=a(this).val();return false}});if(j){l[g]=j;c++}else try{delete b.p.postData[g]}catch(r){}break;case "text":a("input[name="+g+"]",b.grid.hDiv).val(o);if(o){l[g]=o;c++}else try{delete b.p.postData[g]}catch(u){}break}});var e=c>0?true:false;if(f.stringResult===true||b.p.datatype=="local"){var m='{"groupOp":"'+f.groupOp+'","rules":[',i=0;a.each(l,function(j,r){if(i>0)m+=",";m+='{"field":"'+
j+'",';m+='"op":"eq",';m+='"data":"'+r+'"}';i++});m+="]}";a.extend(b.p.postData,{filters:m})}else a.extend(b.p.postData,l);var k;if(b.p.searchurl){k=b.p.url;a(b).jqGrid("setGridParam",{url:b.p.searchurl})}var p=false;if(a.isFunction(f.beforeClear))p=f.beforeClear.call(b);p||h&&a(b).jqGrid("setGridParam",{search:e}).trigger("reloadGrid",[{page:1}]);k&&a(b).jqGrid("setGridParam",{url:k});a.isFunction(f.afterClear)&&f.afterClear()};this.toggleToolbar=function(){var h=a("tr.ui-search-toolbar",b.grid.hDiv);
h.css("display")=="none"?h.show():h.hide()}})}})})(jQuery);
var showModal=function(a){a.w.show()},closeModal=function(a){a.w.hide().attr("aria-hidden","true");a.o&&a.o.remove()},hideModal=function(a,b){b=jQuery.extend({jqm:true,gb:""},b||{});if(b.onClose){var c=b.onClose(a);if(typeof c=="boolean"&&!c)return}if(jQuery.fn.jqm&&b.jqm===true)jQuery(a).attr("aria-hidden","true").jqmHide();else{if(b.gb!=="")try{jQuery(".jqgrid-overlay:first",b.gb).hide()}catch(e){}jQuery(a).hide().attr("aria-hidden","true")}};
function findPos(a){var b=0,c=0;if(a.offsetParent){do{b+=a.offsetLeft;c+=a.offsetTop}while(a=a.offsetParent)}return[b,c]}
var createModal=function(a,b,c,e,f,g){var d=document.createElement("div"),h;h=jQuery(c.gbox).attr("dir")=="rtl"?true:false;d.className="ui-widget ui-widget-content ui-corner-all ui-jqdialog";d.id=a.themodal;var i=document.createElement("div");i.className="ui-jqdialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix";i.id=a.modalhead;jQuery(i).append("<span class='ui-jqdialog-title'>"+c.caption+"</span>");var j=jQuery("<a href='javascript:void(0)' class='ui-jqdialog-titlebar-close ui-corner-all'></a>").hover(function(){j.addClass("ui-state-hover")},
function(){j.removeClass("ui-state-hover")}).append("<span class='ui-icon ui-icon-closethick'></span>");jQuery(i).append(j);if(h){d.dir="rtl";jQuery(".ui-jqdialog-title",i).css("float","right");jQuery(".ui-jqdialog-titlebar-close",i).css("left","0.3em")}else{d.dir="ltr";jQuery(".ui-jqdialog-title",i).css("float","left");jQuery(".ui-jqdialog-titlebar-close",i).css("right","0.3em")}var l=document.createElement("div");jQuery(l).addClass("ui-jqdialog-content ui-widget-content").attr("id",a.modalcontent);
jQuery(l).append(b);d.appendChild(l);jQuery(d).prepend(i);g===true?jQuery("body").append(d):jQuery(d).insertBefore(e);if(typeof c.jqModal==="undefined")c.jqModal=true;b={};if(jQuery.fn.jqm&&c.jqModal===true){if(c.left===0&&c.top===0){e=[];e=findPos(f);c.left=e[0]+4;c.top=e[1]+4}b.top=c.top+"px";b.left=c.left}else if(c.left!==0||c.top!==0){b.left=c.left;b.top=c.top+"px"}jQuery("a.ui-jqdialog-titlebar-close",i).click(function(){var n=jQuery("#"+a.themodal).data("onClose")||c.onClose,k=jQuery("#"+a.themodal).data("gbox")||
c.gbox;hideModal("#"+a.themodal,{gb:k,jqm:c.jqModal,onClose:n});return false});if(c.width===0||!c.width)c.width=300;if(c.height===0||!c.height)c.height=200;if(!c.zIndex)c.zIndex=950;f=0;if(h&&b.left&&!g){f=jQuery(c.gbox).width()-(!isNaN(c.width)?parseInt(c.width,10):0)-8;b.left=parseInt(b.left,10)+parseInt(f,10)}if(b.left)b.left+="px";jQuery(d).css(jQuery.extend({width:isNaN(c.width)?"auto":c.width+"px",height:isNaN(c.height)?"auto":c.height+"px",zIndex:c.zIndex,overflow:"hidden"},b)).attr({tabIndex:"-1",
role:"dialog","aria-labelledby":a.modalhead,"aria-hidden":"true"});if(typeof c.drag=="undefined")c.drag=true;if(typeof c.resize=="undefined")c.resize=true;if(c.drag){jQuery(i).css("cursor","move");if(jQuery.fn.jqDrag)jQuery(d).jqDrag(i);else try{jQuery(d).draggable({handle:jQuery("#"+i.id)})}catch(q){}}if(c.resize)if(jQuery.fn.jqResize){jQuery(d).append("<div class='jqResize ui-resizable-handle ui-resizable-se ui-icon ui-icon-gripsmall-diagonal-se ui-icon-grip-diagonal-se'></div>");jQuery("#"+a.themodal).jqResize(".jqResize",
a.scrollelm?"#"+a.scrollelm:false)}else try{jQuery(d).resizable({handles:"se, sw",alsoResize:a.scrollelm?"#"+a.scrollelm:false})}catch(o){}c.closeOnEscape===true&&jQuery(d).keydown(function(n){if(n.which==27){n=jQuery("#"+a.themodal).data("onClose")||c.onClose;hideModal(this,{gb:c.gbox,jqm:c.jqModal,onClose:n})}})},viewModal=function(a,b){b=jQuery.extend({toTop:true,overlay:10,modal:false,onShow:showModal,onHide:closeModal,gbox:"",jqm:true,jqM:true},b||{});if(jQuery.fn.jqm&&b.jqm===true)b.jqM?jQuery(a).attr("aria-hidden",
"false").jqm(b).jqmShow():jQuery(a).attr("aria-hidden","false").jqmShow();else{if(b.gbox!==""){jQuery(".jqgrid-overlay:first",b.gbox).show();jQuery(a).data("gbox",b.gbox)}jQuery(a).show().attr("aria-hidden","false");try{jQuery(":input:visible",a)[0].focus()}catch(c){}}};
function info_dialog(a,b,c,e){var f={width:290,height:"auto",dataheight:"auto",drag:true,resize:false,caption:"<b>"+a+"</b>",left:250,top:170,zIndex:1E3,jqModal:true,modal:false,closeOnEscape:true,align:"center",buttonalign:"center",buttons:[]};jQuery.extend(f,e||{});var g=f.jqModal;if(jQuery.fn.jqm&&!g)g=false;a="";if(f.buttons.length>0)for(e=0;e<f.buttons.length;e++){if(typeof f.buttons[e].id=="undefined")f.buttons[e].id="info_button_"+e;a+="<a href='javascript:void(0)' id='"+f.buttons[e].id+"' class='fm-button ui-state-default ui-corner-all'>"+
f.buttons[e].text+"</a>"}e=isNaN(f.dataheight)?f.dataheight:f.dataheight+"px";var d="<div id='info_id'>";d+="<div id='infocnt' style='margin:0px;padding-bottom:1em;width:100%;overflow:auto;position:relative;height:"+e+";"+("text-align:"+f.align+";")+"'>"+b+"</div>";d+=c?"<div class='ui-widget-content ui-helper-clearfix' style='text-align:"+f.buttonalign+";padding-bottom:0.8em;padding-top:0.5em;background-image: none;border-width: 1px 0 0 0;'><a href='javascript:void(0)' id='closedialog' class='fm-button ui-state-default ui-corner-all'>"+
c+"</a>"+a+"</div>":a!==""?"<div class='ui-widget-content ui-helper-clearfix' style='text-align:"+f.buttonalign+";padding-bottom:0.8em;padding-top:0.5em;background-image: none;border-width: 1px 0 0 0;'>"+a+"</div>":"";d+="</div>";try{jQuery("#info_dialog").attr("aria-hidden")=="false"&&hideModal("#info_dialog",{jqm:g});jQuery("#info_dialog").remove()}catch(h){}createModal({themodal:"info_dialog",modalhead:"info_head",modalcontent:"info_content",scrollelm:"infocnt"},d,f,"","",true);a&&jQuery.each(f.buttons,
function(j){jQuery("#"+this.id,"#info_id").bind("click",function(){f.buttons[j].onClick.call(jQuery("#info_dialog"));return false})});jQuery("#closedialog","#info_id").click(function(j){hideModal("#info_dialog",{jqm:g});return false});jQuery(".fm-button","#info_dialog").hover(function(){jQuery(this).addClass("ui-state-hover")},function(){jQuery(this).removeClass("ui-state-hover")});jQuery.isFunction(f.beforeOpen)&&f.beforeOpen();viewModal("#info_dialog",{onHide:function(j){j.w.hide().remove();j.o&&
j.o.remove()},modal:f.modal,jqm:g});jQuery.isFunction(f.afterOpen)&&f.afterOpen();try{jQuery("#info_dialog").focus()}catch(i){}}
function createEl(a,b,c,e,f){function g(k,m){if(jQuery.isFunction(m.dataInit)){k.id=m.id;m.dataInit(k);delete m.id;delete m.dataInit}if(m.dataEvents){jQuery.each(m.dataEvents,function(){this.data!==undefined?jQuery(k).bind(this.type,this.data,this.fn):jQuery(k).bind(this.type,this.fn)});delete m.dataEvents}return m}var d="";b.defaultValue&&delete b.defaultValue;switch(a){case "textarea":d=document.createElement("textarea");if(e)b.cols||jQuery(d).css({width:"98%"});else if(!b.cols)b.cols=20;if(!b.rows)b.rows=
2;if(c=="&nbsp;"||c=="&#160;"||c.length==1&&c.charCodeAt(0)==160)c="";d.value=c;b=g(d,b);jQuery(d).attr(b).attr({role:"textbox",multiline:"true"});break;case "checkbox":d=document.createElement("input");d.type="checkbox";if(b.value){var h=b.value.split(":");if(c===h[0]){d.checked=true;d.defaultChecked=true}d.value=h[0];jQuery(d).attr("offval",h[1]);try{delete b.value}catch(i){}}else{h=c.toLowerCase();if(h.search(/(false|0|no|off|undefined)/i)<0&&h!==""){d.checked=true;d.defaultChecked=true;d.value=
c}else d.value="on";jQuery(d).attr("offval","off")}b=g(d,b);jQuery(d).attr(b).attr("role","checkbox");break;case "select":d=document.createElement("select");d.setAttribute("role","select");var j,l=[];if(b.multiple===true){j=true;d.multiple="multiple";jQuery(d).attr("aria-multiselectable","true")}else j=false;if(typeof b.dataUrl!="undefined")jQuery.ajax(jQuery.extend({url:b.dataUrl,type:"GET",dataType:"html",success:function(k,m){try{delete b.dataUrl;delete b.value}catch(r){}if(typeof b.buildSelect!=
"undefined"){k=b.buildSelect(k);k=jQuery(k).html();delete b.buildSelect}else k=jQuery(k).html();if(k){jQuery(d).append(k);b=g(d,b);if(typeof b.size==="undefined")b.size=j?3:1;if(j){l=c.split(",");l=jQuery.map(l,function(p){return jQuery.trim(p)})}else l[0]=jQuery.trim(c);jQuery(d).attr(b);setTimeout(function(){jQuery("option",d).each(function(p){if(p===0)this.selected="";jQuery(this).attr("role","option");if(jQuery.inArray(jQuery.trim(jQuery(this).text()),l)>-1||jQuery.inArray(jQuery.trim(jQuery(this).val()),
l)>-1){this.selected="selected";if(!j)return false}})},0)}}},f||{}));else if(b.value){if(j){l=c.split(",");l=jQuery.map(l,function(k){return jQuery.trim(k)});if(typeof b.size==="undefined")b.size=3}else b.size=1;if(typeof b.value==="function")b.value=b.value();if(typeof b.value==="string"){e=b.value.split(";");for(h=0;h<e.length;h++){f=e[h].split(":");if(f.length>2)f[1]=jQuery.map(f,function(k,m){if(m>0)return k}).join(":");a=document.createElement("option");a.setAttribute("role","option");a.value=
f[0];a.innerHTML=f[1];if(!j&&(jQuery.trim(f[0])==jQuery.trim(c)||jQuery.trim(f[1])==jQuery.trim(c)))a.selected="selected";if(j&&(jQuery.inArray(jQuery.trim(f[1]),l)>-1||jQuery.inArray(jQuery.trim(f[0]),l)>-1))a.selected="selected";d.appendChild(a)}}else if(typeof b.value==="object"){e=b.value;for(h in e)if(e.hasOwnProperty(h)){a=document.createElement("option");a.setAttribute("role","option");a.value=h;a.innerHTML=e[h];if(!j&&(jQuery.trim(h)==jQuery.trim(c)||jQuery.trim(e[h])==jQuery.trim(c)))a.selected=
"selected";if(j&&(jQuery.inArray(jQuery.trim(e[h]),l)>-1||jQuery.inArray(jQuery.trim(h),l)>-1))a.selected="selected";d.appendChild(a)}}b=g(d,b);try{delete b.value}catch(q){}jQuery(d).attr(b)}break;case "text":case "password":case "button":h=a=="button"?"button":"textbox";d=document.createElement("input");d.type=a;d.value=c;b=g(d,b);if(a!="button")if(e)b.size||jQuery(d).css({width:"98%"});else if(!b.size)b.size=20;jQuery(d).attr(b).attr("role",h);break;case "image":case "file":d=document.createElement("input");
d.type=a;b=g(d,b);jQuery(d).attr(b);break;case "custom":d=document.createElement("span");try{if(jQuery.isFunction(b.custom_element)){var o=b.custom_element.call(this,c,b);if(o){o=jQuery(o).addClass("customelement").attr({id:b.id,name:b.name});jQuery(d).empty().append(o)}else throw"e2";}else throw"e1";}catch(n){n=="e1"&&info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_element' "+jQuery.jgrid.edit.msg.nodefined,jQuery.jgrid.edit.bClose);n=="e2"?info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_element' "+
jQuery.jgrid.edit.msg.novalue,jQuery.jgrid.edit.bClose):info_dialog(jQuery.jgrid.errors.errcap,typeof n==="string"?n:n.message,jQuery.jgrid.edit.bClose)}break}return d}function daysInFebruary(a){return a%4===0&&(a%100!==0||a%400===0)?29:28}function DaysArray(a){for(var b=1;b<=a;b++){this[b]=31;if(b==4||b==6||b==9||b==11)this[b]=30;if(b==2)this[b]=29}return this}
function checkDate(a,b){var c={},e;a=a.toLowerCase();e=a.indexOf("/")!=-1?"/":a.indexOf("-")!=-1?"-":a.indexOf(".")!=-1?".":"/";a=a.split(e);b=b.split(e);if(b.length!=3)return false;e=-1;for(var f,g=-1,d=-1,h=0;h<a.length;h++){f=isNaN(b[h])?0:parseInt(b[h],10);c[a[h]]=f;f=a[h];if(f.indexOf("y")!=-1)e=h;if(f.indexOf("m")!=-1)d=h;if(f.indexOf("d")!=-1)g=h}f=a[e]=="y"||a[e]=="yyyy"?4:a[e]=="yy"?2:-1;h=DaysArray(12);var i;if(e===-1)return false;else{i=c[a[e]].toString();if(f==2&&i.length==1)f=1;if(i.length!=
f||c[a[e]]===0&&b[e]!="00")return false}if(d===-1)return false;else{i=c[a[d]].toString();if(i.length<1||c[a[d]]<1||c[a[d]]>12)return false}if(g===-1)return false;else{i=c[a[g]].toString();if(i.length<1||c[a[g]]<1||c[a[g]]>31||c[a[d]]==2&&c[a[g]]>daysInFebruary(c[a[e]])||c[a[g]]>h[c[a[d]]])return false}return true}function isEmpty(a){return a.match(/^\s+$/)||a===""?true:false}
function checkTime(a){var b=/^(\d{1,2}):(\d{2})([ap]m)?$/;if(!isEmpty(a))if(a=a.match(b)){if(a[3]){if(a[1]<1||a[1]>12)return false}else if(a[1]>23)return false;if(a[2]>59)return false}else return false;return true}
function checkValues(a,b,c){var e,f,g,d;if(typeof b=="string"){f=0;for(d=c.p.colModel.length;f<d;f++)if(c.p.colModel[f].name==b){e=c.p.colModel[f].editrules;b=f;try{g=c.p.colModel[f].formoptions.label}catch(h){}break}}else if(b>=0)e=c.p.colModel[b].editrules;if(e){g||(g=c.p.colNames[b]);if(e.required===true)if(isEmpty(a))return[false,g+": "+jQuery.jgrid.edit.msg.required,""];f=e.required===false?false:true;if(e.number===true)if(!(f===false&&isEmpty(a)))if(isNaN(a))return[false,g+": "+jQuery.jgrid.edit.msg.number,
""];if(typeof e.minValue!="undefined"&&!isNaN(e.minValue))if(parseFloat(a)<parseFloat(e.minValue))return[false,g+": "+jQuery.jgrid.edit.msg.minValue+" "+e.minValue,""];if(typeof e.maxValue!="undefined"&&!isNaN(e.maxValue))if(parseFloat(a)>parseFloat(e.maxValue))return[false,g+": "+jQuery.jgrid.edit.msg.maxValue+" "+e.maxValue,""];if(e.email===true)if(!(f===false&&isEmpty(a))){d=/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i;
if(!d.test(a))return[false,g+": "+jQuery.jgrid.edit.msg.email,""]}if(e.integer===true)if(!(f===false&&isEmpty(a))){if(isNaN(a))return[false,g+": "+jQuery.jgrid.edit.msg.integer,""];if(a%1!==0||a.indexOf(".")!=-1)return[false,g+": "+jQuery.jgrid.edit.msg.integer,""]}if(e.date===true)if(!(f===false&&isEmpty(a))){b=c.p.colModel[b].formatoptions&&c.p.colModel[b].formatoptions.newformat?c.p.colModel[b].formatoptions.newformat:c.p.colModel[b].datefmt||"Y-m-d";if(!checkDate(b,a))return[false,g+": "+jQuery.jgrid.edit.msg.date+
" - "+b,""]}if(e.time===true)if(!(f===false&&isEmpty(a)))if(!checkTime(a))return[false,g+": "+jQuery.jgrid.edit.msg.date+" - hh:mm (am/pm)",""];if(e.url===true)if(!(f===false&&isEmpty(a))){d=/^(((https?)|(ftp)):\/\/([\-\w]+\.)+\w{2,3}(\/[%\-\w]+(\.\w{2,})?)*(([\w\-\.\?\\\/+@&#;`~=%!]*)(\.\w{2,})?)*\/?)/i;if(!d.test(a))return[false,g+": "+jQuery.jgrid.edit.msg.url,""]}if(e.custom===true)if(!(f===false&&isEmpty(a)))if(jQuery.isFunction(e.custom_func)){a=e.custom_func.call(c,a,g);return jQuery.isArray(a)?
a:[false,jQuery.jgrid.edit.msg.customarray,""]}else return[false,jQuery.jgrid.edit.msg.customfcheck,""]}return[true,"",""]};
(function(a){var d=null;a.jgrid.extend({searchGrid:function(f){f=a.extend({recreateFilter:false,drag:true,sField:"searchField",sValue:"searchString",sOper:"searchOper",sFilter:"filters",loadDefaults:true,beforeShowSearch:null,afterShowSearch:null,onInitializeSearch:null,closeAfterSearch:false,closeAfterReset:false,closeOnEscape:false,multipleSearch:false,cloneSearchRowOnAdd:true,sopt:null,stringResult:undefined,onClose:null,useDataProxy:false,overlay:true},a.jgrid.search,f||{});return this.each(function(){function b(m,
q){q=m.p.postData[q.sFilter];if(typeof q=="string")q=a.jgrid.parse(q);if(q){q.groupOp&&m.SearchFilter.setGroupOp(q.groupOp);if(q.rules){var A,F=0,K=q.rules.length;for(A=false;F<K;F++){A=q.rules[F];if(A.field!==undefined&&A.op!==undefined&&A.data!==undefined)(A=m.SearchFilter.setFilter({sfref:m.SearchFilter.$.find(".sf:last"),filter:a.extend({},A)}))&&m.SearchFilter.add()}}}}function o(m){if(f.onClose){var q=f.onClose(m);if(typeof q=="boolean"&&!q)return}m.hide();f.overlay===true&&a(".jqgrid-overlay:first",
"#gbox_"+w.p.id).hide()}function C(){var m=a(".ui-searchFilter").length;if(m>1){var q=a("#"+i).css("zIndex");a("#"+i).css({zIndex:parseInt(q,10)+m})}a("#"+i).show();f.overlay===true&&a(".jqgrid-overlay:first","#gbox_"+w.p.id).show();try{a(":input:visible","#"+i)[0].focus()}catch(A){}}function v(m){var q=m!==undefined,A=a("#"+w.p.id),F={};if(f.multipleSearch===false){F[f.sField]=m.rules[0].field;F[f.sValue]=m.rules[0].data;F[f.sOper]=m.rules[0].op}else F[f.sFilter]=m;A[0].p.search=q;a.extend(A[0].p.postData,
F);A.trigger("reloadGrid",[{page:1}]);f.closeAfterSearch&&o(a("#"+i))}function B(m){m=m&&m.hasOwnProperty("reload")?m.reload:true;var q=a("#"+w.p.id),A={};q[0].p.search=false;if(f.multipleSearch===false)A[f.sField]=A[f.sValue]=A[f.sOper]="";else A[f.sFilter]="";a.extend(q[0].p.postData,A);m&&q.trigger("reloadGrid",[{page:1}]);f.closeAfterReset&&o(a("#"+i))}var w=this;if(w.grid){var i="fbox_"+w.p.id;if(a.fn.searchFilter){f.recreateFilter===true&&a("#"+i).remove();if(a("#"+i).html()!=null){a.isFunction(f.beforeShowSearch)&&
f.beforeShowSearch(a("#"+i));C();a.isFunction(f.afterShowSearch)&&f.afterShowSearch(a("#"+i))}else{var n=[],E=a("#"+w.p.id).jqGrid("getGridParam","colNames"),g=a("#"+w.p.id).jqGrid("getGridParam","colModel"),k=["eq","ne","lt","le","gt","ge","bw","bn","in","ni","ew","en","cn","nc"],h,j,l,s=[];if(f.sopt!==null)for(h=l=0;h<f.sopt.length;h++){if((j=a.inArray(f.sopt[h],k))!=-1){s[l]={op:f.sopt[h],text:f.odata[j]};l++}}else for(h=0;h<k.length;h++)s[h]={op:k[h],text:f.odata[h]};a.each(g,function(m,q){var A=
typeof q.search==="undefined"?true:q.search,F=q.hidden===true;m=a.extend({},{text:E[m],itemval:q.index||q.name},this.searchoptions);q=m.searchhidden===true;if(typeof m.sopt!=="undefined"){l=0;m.ops=[];if(m.sopt.length>0)for(h=0;h<m.sopt.length;h++)if((j=a.inArray(m.sopt[h],k))!=-1){m.ops[l]={op:m.sopt[h],text:f.odata[j]};l++}}if(typeof this.stype==="undefined")this.stype="text";if(this.stype=="select")if(m.dataUrl===undefined){var K;if(m.value)K=m.value;else if(this.editoptions)K=this.editoptions.value;
if(K){m.dataValues=[];if(typeof K==="string"){K=K.split(";");var c;for(h=0;h<K.length;h++){c=K[h].split(":");m.dataValues[h]={value:c[0],text:c[1]}}}else if(typeof K==="object"){h=0;for(c in K)if(K.hasOwnProperty(c)){m.dataValues[h]={value:c,text:K[c]};h++}}}}if(q&&A||A&&!F)n.push(m)});if(n.length>0){a("<div id='"+i+"' role='dialog' tabindex='-1'></div>").insertBefore("#gview_"+w.p.id);if(f.stringResult===undefined)f.stringResult=f.multipleSearch;w.SearchFilter=a("#"+i).searchFilter(n,{groupOps:f.groupOps,
operators:s,onClose:o,resetText:f.Reset,searchText:f.Find,windowTitle:f.caption,rulesText:f.rulesText,matchText:f.matchText,onSearch:v,onReset:B,stringResult:f.stringResult,ajaxSelectOptions:a.extend({},a.jgrid.ajaxOptions,w.p.ajaxSelectOptions||{}),clone:f.cloneSearchRowOnAdd});a(".ui-widget-overlay","#"+i).remove();w.p.direction=="rtl"&&a(".ui-closer","#"+i).css("float","left");if(f.drag===true){a("#"+i+" table thead tr:first td:first").css("cursor","move");if(jQuery.fn.jqDrag)a("#"+i).jqDrag(a("#"+
i+" table thead tr:first td:first"));else try{a("#"+i).draggable({handle:a("#"+i+" table thead tr:first td:first")})}catch(Q){}}if(f.multipleSearch===false){a(".ui-del, .ui-add, .ui-del, .ui-add-last, .matchText, .rulesText","#"+i).hide();a("select[name='groupOp']","#"+i).hide()}f.multipleSearch===true&&f.loadDefaults===true&&b(w,f);a.isFunction(f.onInitializeSearch)&&f.onInitializeSearch(a("#"+i));a.isFunction(f.beforeShowSearch)&&f.beforeShowSearch(a("#"+i));C();a.isFunction(f.afterShowSearch)&&
f.afterShowSearch(a("#"+i));f.closeOnEscape===true&&a("#"+i).keydown(function(m){m.which==27&&o(a("#"+i))})}}}}})},editGridRow:function(f,b){d=b=a.extend({top:0,left:0,width:300,height:"auto",dataheight:"auto",modal:false,drag:true,resize:true,url:null,mtype:"POST",clearAfterAdd:true,closeAfterEdit:false,reloadAfterSubmit:true,onInitializeForm:null,beforeInitData:null,beforeShowForm:null,afterShowForm:null,beforeSubmit:null,afterSubmit:null,onclickSubmit:null,afterComplete:null,onclickPgButtons:null,
afterclickPgButtons:null,editData:{},recreateForm:false,jqModal:true,closeOnEscape:false,addedrow:"first",topinfo:"",bottominfo:"",saveicon:[],closeicon:[],savekey:[false,13],navkeys:[false,38,40],checkOnSubmit:false,checkOnUpdate:false,_savedData:{},processing:false,onClose:null,ajaxEditOptions:{},serializeEditData:null,viewPagerButtons:true},a.jgrid.edit,b||{});return this.each(function(){function o(){a(".FormElement","#"+j).each(function(){var e=a(".customelement",this);if(e.length){var r=a(e[0]).attr("name");
a.each(g.p.colModel,function(){if(this.name==r&&this.editoptions&&a.isFunction(this.editoptions.custom_value)){try{c[r]=this.editoptions.custom_value(a("#"+r,"#"+j),"get");if(c[r]===undefined)throw"e1";}catch(p){p=="e1"?info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+a.jgrid.edit.msg.novalue,jQuery.jgrid.edit.bClose):info_dialog(jQuery.jgrid.errors.errcap,p.message,jQuery.jgrid.edit.bClose)}return true}})}else{switch(a(this).get(0).type){case "checkbox":if(a(this).attr("checked"))c[this.name]=
a(this).val();else{e=a(this).attr("offval");c[this.name]=e}break;case "select-one":c[this.name]=a("option:selected",this).val();u[this.name]=a("option:selected",this).text();break;case "select-multiple":c[this.name]=a(this).val();c[this.name]=c[this.name]?c[this.name].join(","):"";var t=[];a("option:selected",this).each(function(p,I){t[p]=a(I).text()});u[this.name]=t.join(",");break;case "password":case "text":case "textarea":case "button":c[this.name]=a(this).val();break}if(g.p.autoencode)c[this.name]=
a.jgrid.htmlEncode(c[this.name])}});return true}function C(e,r,t,p){for(var I,z,x,J=0,y,L,G,R=[],H=false,ca="",S=1;S<=p;S++)ca+="<td class='CaptionTD ui-widget-content'>&#160;</td><td class='DataTD ui-widget-content' style='white-space:pre'>&#160;</td>";if(e!="_empty")H=a(r).jqGrid("getInd",e);a(r.p.colModel).each(function(T){I=this.name;L=(z=this.editrules&&this.editrules.edithidden===true?false:this.hidden===true?true:false)?"style='display:none'":"";if(I!=="cb"&&I!=="subgrid"&&this.editable===
true&&I!=="rn"){if(H===false)y="";else if(I==r.p.ExpandColumn&&r.p.treeGrid===true)y=a("td:eq("+T+")",r.rows[H]).text();else try{y=a.unformat(a("td:eq("+T+")",r.rows[H]),{rowId:e,colModel:this},T)}catch(da){y=a("td:eq("+T+")",r.rows[H]).html()}var W=a.extend({},this.editoptions||{},{id:I,name:I}),X=a.extend({},{elmprefix:"",elmsuffix:"",rowabove:false,rowcontent:""},this.formoptions||{}),aa=parseInt(X.rowpos,10)||J+1,ea=parseInt((parseInt(X.colpos,10)||1)*2,10);if(e=="_empty"&&W.defaultValue)y=a.isFunction(W.defaultValue)?
W.defaultValue():W.defaultValue;if(!this.edittype)this.edittype="text";if(g.p.autoencode)y=a.jgrid.htmlDecode(y);G=createEl(this.edittype,W,y,false,a.extend({},a.jgrid.ajaxOptions,r.p.ajaxSelectOptions||{}));if(y===""&&this.edittype=="checkbox")y=a(G).attr("offval");if(y===""&&this.edittype=="select")y=a("option:eq(0)",G).text();if(d.checkOnSubmit||d.checkOnUpdate)d._savedData[I]=y;a(G).addClass("FormElement");x=a(t).find("tr[rowpos="+aa+"]");if(X.rowabove){W=a("<tr><td class='contentinfo' colspan='"+
p*2+"'>"+X.rowcontent+"</td></tr>");a(t).append(W);W[0].rp=aa}if(x.length===0){x=a("<tr "+L+" rowpos='"+aa+"'></tr>").addClass("FormData").attr("id","tr_"+I);a(x).append(ca);a(t).append(x);x[0].rp=aa}a("td:eq("+(ea-2)+")",x[0]).html(typeof X.label==="undefined"?r.p.colNames[T]:X.label);a("td:eq("+(ea-1)+")",x[0]).append(X.elmprefix).append(G).append(X.elmsuffix);R[J]=T;J++}});if(J>0){S=a("<tr class='FormData' style='display:none'><td class='CaptionTD'></td><td colspan='"+(p*2-1)+"' class='DataTD'><input class='FormElement' id='id_g' type='text' name='"+
r.p.id+"_id' value='"+e+"'/></td></tr>");S[0].rp=J+999;a(t).append(S);if(d.checkOnSubmit||d.checkOnUpdate)d._savedData[r.p.id+"_id"]=e}return R}function v(e,r,t){var p,I=0,z,x,J,y,L;if(d.checkOnSubmit||d.checkOnUpdate){d._savedData={};d._savedData[r.p.id+"_id"]=e}var G=r.p.colModel;if(e=="_empty"){a(G).each(function(){p=this.name;J=a.extend({},this.editoptions||{});x=a("#"+a.jgrid.jqID(p),"#"+t);if(x[0]!=null){y="";if(J.defaultValue){y=a.isFunction(J.defaultValue)?J.defaultValue():J.defaultValue;
if(x[0].type=="checkbox"){L=y.toLowerCase();if(L.search(/(false|0|no|off|undefined)/i)<0&&L!==""){x[0].checked=true;x[0].defaultChecked=true;x[0].value=y}else x.attr({checked:"",defaultChecked:""})}else x.val(y)}else if(x[0].type=="checkbox"){x[0].checked=false;x[0].defaultChecked=false;y=a(x).attr("offval")}else if(x[0].type&&x[0].type.substr(0,6)=="select")x[0].selectedIndex=0;else x.val(y);if(d.checkOnSubmit===true||d.checkOnUpdate)d._savedData[p]=y}});a("#id_g","#"+t).val(e)}else{var R=a(r).jqGrid("getInd",
e,true);if(R){a("td",R).each(function(H){p=G[H].name;if(p!=="cb"&&p!=="subgrid"&&p!=="rn"&&G[H].editable===true){if(p==r.p.ExpandColumn&&r.p.treeGrid===true)z=a(this).text();else try{z=a.unformat(a(this),{rowId:e,colModel:G[H]},H)}catch(ca){z=a(this).html()}if(g.p.autoencode)z=a.jgrid.htmlDecode(z);if(d.checkOnSubmit===true||d.checkOnUpdate)d._savedData[p]=z;p=a.jgrid.jqID(p);switch(G[H].edittype){case "password":case "text":case "button":case "image":a("#"+p,"#"+t).val(z);break;case "textarea":if(z==
"&nbsp;"||z=="&#160;"||z.length==1&&z.charCodeAt(0)==160)z="";a("#"+p,"#"+t).val(z);break;case "select":var S=z.split(",");S=a.map(S,function(da){return a.trim(da)});a("#"+p+" option","#"+t).each(function(){this.selected=!G[H].editoptions.multiple&&(S[0]==a.trim(a(this).text())||S[0]==a.trim(a(this).val()))?true:G[H].editoptions.multiple?a.inArray(a.trim(a(this).text()),S)>-1||a.inArray(a.trim(a(this).val()),S)>-1?true:false:false});break;case "checkbox":z+="";if(G[H].editoptions&&G[H].editoptions.value)if(G[H].editoptions.value.split(":")[0]==
z){a("#"+p,"#"+t).attr("checked",true);a("#"+p,"#"+t).attr("defaultChecked",true)}else{a("#"+p,"#"+t).attr("checked",false);a("#"+p,"#"+t).attr("defaultChecked","")}else{z=z.toLowerCase();if(z.search(/(false|0|no|off|undefined)/i)<0&&z!==""){a("#"+p,"#"+t).attr("checked",true);a("#"+p,"#"+t).attr("defaultChecked",true)}else{a("#"+p,"#"+t).attr("checked",false);a("#"+p,"#"+t).attr("defaultChecked","")}}break;case "custom":try{if(G[H].editoptions&&a.isFunction(G[H].editoptions.custom_value))G[H].editoptions.custom_value(a("#"+
p,"#"+t),"set",z);else throw"e1";}catch(T){T=="e1"?info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+a.jgrid.edit.msg.nodefined,jQuery.jgrid.edit.bClose):info_dialog(jQuery.jgrid.errors.errcap,T.message,jQuery.jgrid.edit.bClose)}break}I++}});I>0&&a("#id_g","#"+j).val(e)}}}function B(){var e=[true,"",""],r={},t=g.p.prmNames,p,I;if(a.isFunction(d.beforeCheckValues)){var z=d.beforeCheckValues(c,a("#"+h),c[g.p.id+"_id"]=="_empty"?t.addoper:t.editoper);if(z&&typeof z==="object")c=z}for(var x in c)if(c.hasOwnProperty(x)){e=
checkValues(c[x],x,g);if(e[0]===false)break}if(e[0]){if(a.isFunction(d.onclickSubmit))r=d.onclickSubmit(d,c)||{};if(a.isFunction(d.beforeSubmit))e=d.beforeSubmit(c,a("#"+h))}if(e[0]&&!d.processing){d.processing=true;a("#sData","#"+j+"_2").addClass("ui-state-active");I=t.oper;p=t.id;c[I]=a.trim(c[g.p.id+"_id"])=="_empty"?t.addoper:t.editoper;if(c[I]!=t.addoper)c[p]=c[g.p.id+"_id"];else if(c[p]===undefined)c[p]=c[g.p.id+"_id"];delete c[g.p.id+"_id"];c=a.extend(c,d.editData,r);r=a.extend({url:d.url?
d.url:a(g).jqGrid("getGridParam","editurl"),type:d.mtype,data:a.isFunction(d.serializeEditData)?d.serializeEditData(c):c,complete:function(J,y){if(y!="success"){e[0]=false;e[1]=a.isFunction(d.errorTextFormat)?d.errorTextFormat(J):y+" Status: '"+J.statusText+"'. Error code: "+J.status}else if(a.isFunction(d.afterSubmit))e=d.afterSubmit(J,c);if(e[0]===false){a("#FormError>td","#"+j).html(e[1]);a("#FormError","#"+j).show()}else{a.each(g.p.colModel,function(){if(u[this.name]&&this.formatter&&this.formatter==
"select")try{delete u[this.name]}catch(R){}});c=a.extend(c,u);g.p.autoencode&&a.each(c,function(R,H){c[R]=a.jgrid.htmlDecode(H)});d.reloadAfterSubmit=d.reloadAfterSubmit&&g.p.datatype!="local";if(c[I]==t.addoper){e[2]||(e[2]=parseInt(g.p.records,10)+1+"");c[p]=e[2];if(d.closeAfterAdd){if(d.reloadAfterSubmit)a(g).trigger("reloadGrid");else{a(g).jqGrid("addRowData",e[2],c,b.addedrow);a(g).jqGrid("setSelection",e[2])}hideModal("#"+l.themodal,{gb:"#gbox_"+k,jqm:b.jqModal,onClose:d.onClose})}else if(d.clearAfterAdd){d.reloadAfterSubmit?
a(g).trigger("reloadGrid"):a(g).jqGrid("addRowData",e[2],c,b.addedrow);v("_empty",g,h)}else d.reloadAfterSubmit?a(g).trigger("reloadGrid"):a(g).jqGrid("addRowData",e[2],c,b.addedrow)}else{if(d.reloadAfterSubmit){a(g).trigger("reloadGrid");d.closeAfterEdit||setTimeout(function(){a(g).jqGrid("setSelection",c[p])},1E3)}else g.p.treeGrid===true?a(g).jqGrid("setTreeRow",c[p],c):a(g).jqGrid("setRowData",c[p],c);d.closeAfterEdit&&hideModal("#"+l.themodal,{gb:"#gbox_"+k,jqm:b.jqModal,onClose:d.onClose})}if(a.isFunction(d.afterComplete)){A=
J;setTimeout(function(){d.afterComplete(A,c,a("#"+h));A=null},500)}}d.processing=false;if(d.checkOnSubmit||d.checkOnUpdate){a("#"+h).data("disabled",false);if(d._savedData[g.p.id+"_id"]!="_empty")for(var L in d._savedData)if(c[L])d._savedData[L]=c[L]}a("#sData","#"+j+"_2").removeClass("ui-state-active");try{a(":input:visible","#"+h)[0].focus()}catch(G){}},error:function(J,y,L){a("#FormError>td","#"+j).html(y+" : "+L);a("#FormError","#"+j).show();d.processing=false;a("#"+h).data("disabled",false);
a("#sData","#"+j+"_2").removeClass("ui-state-active")}},a.jgrid.ajaxOptions,d.ajaxEditOptions);if(!r.url&&!d.useDataProxy)if(a.isFunction(g.p.dataProxy))d.useDataProxy=true;else{e[0]=false;e[1]+=" "+a.jgrid.errors.nourl}if(e[0])d.useDataProxy?g.p.dataProxy.call(g,r,"set_"+g.p.id):a.ajax(r)}if(e[0]===false){a("#FormError>td","#"+j).html(e[1]);a("#FormError","#"+j).show()}}function w(e,r){var t=false,p;for(p in e)if(e[p]!=r[p]){t=true;break}return t}function i(){var e=true;a("#FormError","#"+j).hide();
if(d.checkOnUpdate){c={};u={};o();M=a.extend({},c,u);if(U=w(M,d._savedData)){a("#"+h).data("disabled",true);a(".confirm","#"+l.themodal).show();e=false}}return e}function n(e,r){e===0?a("#pData","#"+j+"_2").addClass("ui-state-disabled"):a("#pData","#"+j+"_2").removeClass("ui-state-disabled");e==r?a("#nData","#"+j+"_2").addClass("ui-state-disabled"):a("#nData","#"+j+"_2").removeClass("ui-state-disabled")}function E(){var e=a(g).jqGrid("getDataIDs"),r=a("#id_g","#"+j).val();return[a.inArray(r,e),e]}
var g=this;if(g.grid&&f){var k=g.p.id,h="FrmGrid_"+k,j="TblGrid_"+k,l={themodal:"editmod"+k,modalhead:"edithd"+k,modalcontent:"editcnt"+k,scrollelm:h},s=a.isFunction(d.beforeShowForm)?d.beforeShowForm:false,Q=a.isFunction(d.afterShowForm)?d.afterShowForm:false,m=a.isFunction(d.beforeInitData)?d.beforeInitData:false,q=a.isFunction(d.onInitializeForm)?d.onInitializeForm:false,A=null,F=1,K=0,c,u,M,U;if(f=="new"){f="_empty";b.caption=b.addCaption}else b.caption=b.editCaption;b.recreateForm===true&&a("#"+
l.themodal).html()!=null&&a("#"+l.themodal).remove();var O=true;if(b.checkOnUpdate&&b.jqModal&&!b.modal)O=false;if(a("#"+l.themodal).html()!=null){a(".ui-jqdialog-title","#"+l.modalhead).html(b.caption);a("#FormError","#"+j).hide();if(d.topinfo){a(".topinfo","#"+j+"_2").html(d.topinfo);a(".tinfo","#"+j+"_2").show()}else a(".tinfo","#"+j+"_2").hide();if(d.bottominfo){a(".bottominfo","#"+j+"_2").html(d.bottominfo);a(".binfo","#"+j+"_2").show()}else a(".binfo","#"+j+"_2").hide();m&&m(a("#"+h));v(f,g,
h);f=="_empty"||!d.viewPagerButtons?a("#pData, #nData","#"+j+"_2").hide():a("#pData, #nData","#"+j+"_2").show();if(d.processing===true){d.processing=false;a("#sData","#"+j+"_2").removeClass("ui-state-active")}if(a("#"+h).data("disabled")===true){a(".confirm","#"+l.themodal).hide();a("#"+h).data("disabled",false)}s&&s(a("#"+h));a("#"+l.themodal).data("onClose",d.onClose);viewModal("#"+l.themodal,{gbox:"#gbox_"+k,jqm:b.jqModal,jqM:false,closeoverlay:O,modal:b.modal});O||a(".jqmOverlay").click(function(){if(!i())return false;
hideModal("#"+l.themodal,{gb:"#gbox_"+k,jqm:b.jqModal,onClose:d.onClose});return false});Q&&Q(a("#"+h))}else{a(g.p.colModel).each(function(){var e=this.formoptions;F=Math.max(F,e?e.colpos||0:0);K=Math.max(K,e?e.rowpos||0:0)});var P=isNaN(b.dataheight)?b.dataheight:b.dataheight+"px";P=a("<form name='FormPost' id='"+h+"' class='FormGrid' onSubmit='return false;' style='width:100%;overflow:auto;position:relative;height:"+P+";'></form>").data("disabled",false);var N=a("<table id='"+j+"' class='EditTable' cellspacing='0' cellpading='0' border='0'><tbody></tbody></table>");
a(P).append(N);var D=a("<tr id='FormError' style='display:none'><td class='ui-state-error' colspan='"+F*2+"'></td></tr>");D[0].rp=0;a(N).append(D);D=a("<tr style='display:none' class='tinfo'><td class='topinfo' colspan='"+F*2+"'>"+d.topinfo+"</td></tr>");D[0].rp=0;a(N).append(D);m&&m(a("#"+h));D=(m=g.p.direction=="rtl"?true:false)?"nData":"pData";var V=m?"pData":"nData";C(f,g,N,F);D="<a href='javascript:void(0)' id='"+D+"' class='fm-button ui-state-default ui-corner-left'><span class='ui-icon ui-icon-triangle-1-w'></span></div>";
V="<a href='javascript:void(0)' id='"+V+"' class='fm-button ui-state-default ui-corner-right'><span class='ui-icon ui-icon-triangle-1-e'></span></div>";var Z="<a href='javascript:void(0)' id='sData' class='fm-button ui-state-default ui-corner-all'>"+b.bSubmit+"</a>",$="<a href='javascript:void(0)' id='cData' class='fm-button ui-state-default ui-corner-all'>"+b.bCancel+"</a>";D="<table border='0' class='EditTable' id='"+j+"_2'><tbody><tr id='Act_Buttons'><td class='navButton ui-widget-content'>"+(m?
V+D:D+V)+"</td><td class='EditButton ui-widget-content'>"+Z+$+"</td></tr>";D+="<tr style='display:none' class='binfo'><td class='bottominfo' colspan='2'>"+d.bottominfo+"</td></tr>";D+="</tbody></table>";if(K>0){var Y=[];a.each(a(N)[0].rows,function(e,r){Y[e]=r});Y.sort(function(e,r){if(e.rp>r.rp)return 1;if(e.rp<r.rp)return-1;return 0});a.each(Y,function(e,r){a("tbody",N).append(r)})}b.gbox="#gbox_"+k;var ba=false;if(b.closeOnEscape===true){b.closeOnEscape=false;ba=true}P=a("<span></span>").append(P).append(D);
createModal(l,P,b,"#gview_"+g.p.id,a("#gbox_"+g.p.id)[0]);if(m){a("#pData, #nData","#"+j+"_2").css("float","right");a(".EditButton","#"+j+"_2").css("text-align","left")}d.topinfo&&a(".tinfo","#"+j+"_2").show();d.bottominfo&&a(".binfo","#"+j+"_2").show();D=P=null;a("#"+l.themodal).keydown(function(e){var r=e.target;if(a("#"+h).data("disabled")===true)return false;if(d.savekey[0]===true&&e.which==d.savekey[1])if(r.tagName!="TEXTAREA"){a("#sData","#"+j+"_2").trigger("click");return false}if(e.which===
27){if(!i())return false;ba&&hideModal(this,{gb:b.gbox,jqm:b.jqModal,onClose:d.onClose});return false}if(d.navkeys[0]===true){if(a("#id_g","#"+j).val()=="_empty")return true;if(e.which==d.navkeys[1]){a("#pData","#"+j+"_2").trigger("click");return false}if(e.which==d.navkeys[2]){a("#nData","#"+j+"_2").trigger("click");return false}}});if(b.checkOnUpdate){a("a.ui-jqdialog-titlebar-close span","#"+l.themodal).removeClass("jqmClose");a("a.ui-jqdialog-titlebar-close","#"+l.themodal).unbind("click").click(function(){if(!i())return false;
hideModal("#"+l.themodal,{gb:"#gbox_"+k,jqm:b.jqModal,onClose:d.onClose});return false})}b.saveicon=a.extend([true,"left","ui-icon-disk"],b.saveicon);b.closeicon=a.extend([true,"left","ui-icon-close"],b.closeicon);if(b.saveicon[0]===true)a("#sData","#"+j+"_2").addClass(b.saveicon[1]=="right"?"fm-button-icon-right":"fm-button-icon-left").append("<span class='ui-icon "+b.saveicon[2]+"'></span>");if(b.closeicon[0]===true)a("#cData","#"+j+"_2").addClass(b.closeicon[1]=="right"?"fm-button-icon-right":
"fm-button-icon-left").append("<span class='ui-icon "+b.closeicon[2]+"'></span>");if(d.checkOnSubmit||d.checkOnUpdate){Z="<a href='javascript:void(0)' id='sNew' class='fm-button ui-state-default ui-corner-all' style='z-index:1002'>"+b.bYes+"</a>";V="<a href='javascript:void(0)' id='nNew' class='fm-button ui-state-default ui-corner-all' style='z-index:1002'>"+b.bNo+"</a>";$="<a href='javascript:void(0)' id='cNew' class='fm-button ui-state-default ui-corner-all' style='z-index:1002'>"+b.bExit+"</a>";
P=b.zIndex||999;P++;a("<div class='ui-widget-overlay jqgrid-overlay confirm' style='z-index:"+P+";display:none;'>&#160;"+(a.browser.msie&&a.browser.version==6?'<iframe style="display:block;position:absolute;z-index:-1;filter:Alpha(Opacity=\'0\');" src="javascript:false;"></iframe>':"")+"</div><div class='confirm ui-widget-content ui-jqconfirm' style='z-index:"+(P+1)+"'>"+b.saveData+"<br/><br/>"+Z+V+$+"</div>").insertAfter("#"+h);a("#sNew","#"+l.themodal).click(function(){B();a("#"+h).data("disabled",
false);a(".confirm","#"+l.themodal).hide();return false});a("#nNew","#"+l.themodal).click(function(){a(".confirm","#"+l.themodal).hide();a("#"+h).data("disabled",false);setTimeout(function(){a(":input","#"+h)[0].focus()},0);return false});a("#cNew","#"+l.themodal).click(function(){a(".confirm","#"+l.themodal).hide();a("#"+h).data("disabled",false);hideModal("#"+l.themodal,{gb:"#gbox_"+k,jqm:b.jqModal,onClose:d.onClose});return false})}q&&q(a("#"+h));f=="_empty"||!d.viewPagerButtons?a("#pData,#nData",
"#"+j+"_2").hide():a("#pData,#nData","#"+j+"_2").show();s&&s(a("#"+h));a("#"+l.themodal).data("onClose",d.onClose);viewModal("#"+l.themodal,{gbox:"#gbox_"+k,jqm:b.jqModal,closeoverlay:O,modal:b.modal});O||a(".jqmOverlay").click(function(){if(!i())return false;hideModal("#"+l.themodal,{gb:"#gbox_"+k,jqm:b.jqModal,onClose:d.onClose});return false});Q&&Q(a("#"+h));a(".fm-button","#"+l.themodal).hover(function(){a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});a("#sData",
"#"+j+"_2").click(function(){c={};u={};a("#FormError","#"+j).hide();o();if(c[g.p.id+"_id"]=="_empty")B();else if(b.checkOnSubmit===true){M=a.extend({},c,u);if(U=w(M,d._savedData)){a("#"+h).data("disabled",true);a(".confirm","#"+l.themodal).show()}else B()}else B();return false});a("#cData","#"+j+"_2").click(function(){if(!i())return false;hideModal("#"+l.themodal,{gb:"#gbox_"+k,jqm:b.jqModal,onClose:d.onClose});return false});a("#nData","#"+j+"_2").click(function(){if(!i())return false;a("#FormError",
"#"+j).hide();var e=E();e[0]=parseInt(e[0],10);if(e[0]!=-1&&e[1][e[0]+1]){a.isFunction(b.onclickPgButtons)&&b.onclickPgButtons("next",a("#"+h),e[1][e[0]]);v(e[1][e[0]+1],g,h);a(g).jqGrid("setSelection",e[1][e[0]+1]);a.isFunction(b.afterclickPgButtons)&&b.afterclickPgButtons("next",a("#"+h),e[1][e[0]+1]);n(e[0]+1,e[1].length-1)}return false});a("#pData","#"+j+"_2").click(function(){if(!i())return false;a("#FormError","#"+j).hide();var e=E();if(e[0]!=-1&&e[1][e[0]-1]){a.isFunction(b.onclickPgButtons)&&
b.onclickPgButtons("prev",a("#"+h),e[1][e[0]]);v(e[1][e[0]-1],g,h);a(g).jqGrid("setSelection",e[1][e[0]-1]);a.isFunction(b.afterclickPgButtons)&&b.afterclickPgButtons("prev",a("#"+h),e[1][e[0]-1]);n(e[0]-1,e[1].length-1)}return false})}s=E();n(s[0],s[1].length-1)}})},viewGridRow:function(f,b){b=a.extend({top:0,left:0,width:0,height:"auto",dataheight:"auto",modal:false,drag:true,resize:true,jqModal:true,closeOnEscape:false,labelswidth:"30%",closeicon:[],navkeys:[false,38,40],onClose:null,beforeShowForm:null,
viewPagerButtons:true},a.jgrid.view,b||{});return this.each(function(){function o(){if(b.closeOnEscape===true||b.navkeys[0]===true)setTimeout(function(){a(".ui-jqdialog-titlebar-close","#"+k.modalhead).focus()},0)}function C(c,u,M,U){for(var O,P,N,D=0,V,Z,$=[],Y=false,ba="<td class='CaptionTD form-view-label ui-widget-content' width='"+b.labelswidth+"'>&#160;</td><td class='DataTD form-view-data ui-helper-reset ui-widget-content'>&#160;</td>",e="",r=["integer","number","currency"],t=0,p=0,I,z,x,J=
1;J<=U;J++)e+=J==1?ba:"<td class='CaptionTD form-view-label ui-widget-content'>&#160;</td><td class='DataTD form-view-data ui-widget-content'>&#160;</td>";a(u.p.colModel).each(function(){P=this.editrules&&this.editrules.edithidden===true?false:this.hidden===true?true:false;if(!P&&this.align==="right")if(this.formatter&&a.inArray(this.formatter,r)!==-1)t=Math.max(t,parseInt(this.width,10));else p=Math.max(p,parseInt(this.width,10))});I=t!==0?t:p!==0?p:0;Y=a(u).jqGrid("getInd",c);a(u.p.colModel).each(function(y){O=
this.name;z=false;Z=(P=this.editrules&&this.editrules.edithidden===true?false:this.hidden===true?true:false)?"style='display:none'":"";x=typeof this.viewable!="boolean"?true:this.viewable;if(O!=="cb"&&O!=="subgrid"&&O!=="rn"&&x){V=Y===false?"":O==u.p.ExpandColumn&&u.p.treeGrid===true?a("td:eq("+y+")",u.rows[Y]).text():a("td:eq("+y+")",u.rows[Y]).html();z=this.align==="right"&&I!==0?true:false;a.extend({},this.editoptions||{},{id:O,name:O});var L=a.extend({},{rowabove:false,rowcontent:""},this.formoptions||
{}),G=parseInt(L.rowpos,10)||D+1,R=parseInt((parseInt(L.colpos,10)||1)*2,10);if(L.rowabove){var H=a("<tr><td class='contentinfo' colspan='"+U*2+"'>"+L.rowcontent+"</td></tr>");a(M).append(H);H[0].rp=G}N=a(M).find("tr[rowpos="+G+"]");if(N.length===0){N=a("<tr "+Z+" rowpos='"+G+"'></tr>").addClass("FormData").attr("id","trv_"+O);a(N).append(e);a(M).append(N);N[0].rp=G}a("td:eq("+(R-2)+")",N[0]).html("<b>"+(typeof L.label==="undefined"?u.p.colNames[y]:L.label)+"</b>");a("td:eq("+(R-1)+")",N[0]).append("<span>"+
V+"</span>").attr("id","v_"+O);z&&a("td:eq("+(R-1)+") span",N[0]).css({"text-align":"right",width:I+"px"});$[D]=y;D++}});if(D>0){c=a("<tr class='FormData' style='display:none'><td class='CaptionTD'></td><td colspan='"+(U*2-1)+"' class='DataTD'><input class='FormElement' id='id_g' type='text' name='id' value='"+c+"'/></td></tr>");c[0].rp=D+99;a(M).append(c)}return $}function v(c,u){var M,U,O=0,P,N;if(N=a(u).jqGrid("getInd",c,true)){a("td",N).each(function(D){M=u.p.colModel[D].name;U=u.p.colModel[D].editrules&&
u.p.colModel[D].editrules.edithidden===true?false:u.p.colModel[D].hidden===true?true:false;if(M!=="cb"&&M!=="subgrid"&&M!=="rn"){P=M==u.p.ExpandColumn&&u.p.treeGrid===true?a(this).text():a(this).html();a.extend({},u.p.colModel[D].editoptions||{});M=a.jgrid.jqID("v_"+M);a("#"+M+" span","#"+g).html(P);U&&a("#"+M,"#"+g).parents("tr:first").hide();O++}});O>0&&a("#id_g","#"+g).val(c)}}function B(c,u){c===0?a("#pData","#"+g+"_2").addClass("ui-state-disabled"):a("#pData","#"+g+"_2").removeClass("ui-state-disabled");
c==u?a("#nData","#"+g+"_2").addClass("ui-state-disabled"):a("#nData","#"+g+"_2").removeClass("ui-state-disabled")}function w(){var c=a(i).jqGrid("getDataIDs"),u=a("#id_g","#"+g).val();return[a.inArray(u,c),c]}var i=this;if(i.grid&&f){if(!b.imgpath)b.imgpath=i.p.imgpath;var n=i.p.id,E="ViewGrid_"+n,g="ViewTbl_"+n,k={themodal:"viewmod"+n,modalhead:"viewhd"+n,modalcontent:"viewcnt"+n,scrollelm:E},h=1,j=0;if(a("#"+k.themodal).html()!=null){a(".ui-jqdialog-title","#"+k.modalhead).html(b.caption);a("#FormError",
"#"+g).hide();v(f,i);a.isFunction(b.beforeShowForm)&&b.beforeShowForm(a("#"+E));viewModal("#"+k.themodal,{gbox:"#gbox_"+n,jqm:b.jqModal,jqM:false,modal:b.modal});o()}else{a(i.p.colModel).each(function(){var c=this.formoptions;h=Math.max(h,c?c.colpos||0:0);j=Math.max(j,c?c.rowpos||0:0)});var l=isNaN(b.dataheight)?b.dataheight:b.dataheight+"px",s=a("<form name='FormPost' id='"+E+"' class='FormGrid' style='width:100%;overflow:auto;position:relative;height:"+l+";'></form>"),Q=a("<table id='"+g+"' class='EditTable' cellspacing='1' cellpading='2' border='0' style='table-layout:fixed'><tbody></tbody></table>");
a(s).append(Q);C(f,i,Q,h);l=i.p.direction=="rtl"?true:false;var m="<a href='javascript:void(0)' id='"+(l?"nData":"pData")+"' class='fm-button ui-state-default ui-corner-left'><span class='ui-icon ui-icon-triangle-1-w'></span></div>",q="<a href='javascript:void(0)' id='"+(l?"pData":"nData")+"' class='fm-button ui-state-default ui-corner-right'><span class='ui-icon ui-icon-triangle-1-e'></span></div>",A="<a href='javascript:void(0)' id='cData' class='fm-button ui-state-default ui-corner-all'>"+b.bClose+
"</a>";if(j>0){var F=[];a.each(a(Q)[0].rows,function(c,u){F[c]=u});F.sort(function(c,u){if(c.rp>u.rp)return 1;if(c.rp<u.rp)return-1;return 0});a.each(F,function(c,u){a("tbody",Q).append(u)})}b.gbox="#gbox_"+n;var K=false;if(b.closeOnEscape===true){b.closeOnEscape=false;K=true}s=a("<span></span>").append(s).append("<table border='0' class='EditTable' id='"+g+"_2'><tbody><tr id='Act_Buttons'><td class='navButton ui-widget-content' width='"+b.labelswidth+"'>"+(l?q+m:m+q)+"</td><td class='EditButton ui-widget-content'>"+
A+"</td></tr></tbody></table>");createModal(k,s,b,"#gview_"+i.p.id,a("#gview_"+i.p.id)[0]);if(l){a("#pData, #nData","#"+g+"_2").css("float","right");a(".EditButton","#"+g+"_2").css("text-align","left")}b.viewPagerButtons||a("#pData, #nData","#"+g+"_2").hide();s=null;a("#"+k.themodal).keydown(function(c){if(c.which===27){K&&hideModal(this,{gb:b.gbox,jqm:b.jqModal,onClose:b.onClose});return false}if(b.navkeys[0]===true){if(c.which===b.navkeys[1]){a("#pData","#"+g+"_2").trigger("click");return false}if(c.which===
b.navkeys[2]){a("#nData","#"+g+"_2").trigger("click");return false}}});b.closeicon=a.extend([true,"left","ui-icon-close"],b.closeicon);if(b.closeicon[0]===true)a("#cData","#"+g+"_2").addClass(b.closeicon[1]=="right"?"fm-button-icon-right":"fm-button-icon-left").append("<span class='ui-icon "+b.closeicon[2]+"'></span>");a.isFunction(b.beforeShowForm)&&b.beforeShowForm(a("#"+E));viewModal("#"+k.themodal,{gbox:"#gbox_"+n,jqm:b.jqModal,modal:b.modal});a(".fm-button:not(.ui-state-disabled)","#"+g+"_2").hover(function(){a(this).addClass("ui-state-hover")},
function(){a(this).removeClass("ui-state-hover")});o();a("#cData","#"+g+"_2").click(function(){hideModal("#"+k.themodal,{gb:"#gbox_"+n,jqm:b.jqModal,onClose:b.onClose});return false});a("#nData","#"+g+"_2").click(function(){a("#FormError","#"+g).hide();var c=w();c[0]=parseInt(c[0],10);if(c[0]!=-1&&c[1][c[0]+1]){a.isFunction(b.onclickPgButtons)&&b.onclickPgButtons("next",a("#"+E),c[1][c[0]]);v(c[1][c[0]+1],i);a(i).jqGrid("setSelection",c[1][c[0]+1]);a.isFunction(b.afterclickPgButtons)&&b.afterclickPgButtons("next",
a("#"+E),c[1][c[0]+1]);B(c[0]+1,c[1].length-1)}o();return false});a("#pData","#"+g+"_2").click(function(){a("#FormError","#"+g).hide();var c=w();if(c[0]!=-1&&c[1][c[0]-1]){a.isFunction(b.onclickPgButtons)&&b.onclickPgButtons("prev",a("#"+E),c[1][c[0]]);v(c[1][c[0]-1],i);a(i).jqGrid("setSelection",c[1][c[0]-1]);a.isFunction(b.afterclickPgButtons)&&b.afterclickPgButtons("prev",a("#"+E),c[1][c[0]-1]);B(c[0]-1,c[1].length-1)}o();return false})}l=w();B(l[0],l[1].length-1)}})},delGridRow:function(f,b){d=
b=a.extend({top:0,left:0,width:240,height:"auto",dataheight:"auto",modal:false,drag:true,resize:true,url:"",mtype:"POST",reloadAfterSubmit:true,beforeShowForm:null,afterShowForm:null,beforeSubmit:null,onclickSubmit:null,afterSubmit:null,jqModal:true,closeOnEscape:false,delData:{},delicon:[],cancelicon:[],onClose:null,ajaxDelOptions:{},processing:false,serializeDelData:null,useDataProxy:false},a.jgrid.del,b||{});return this.each(function(){var o=this;if(o.grid)if(f){var C=typeof b.beforeShowForm===
"function"?true:false,v=typeof b.afterShowForm==="function"?true:false,B=o.p.id,w={},i="DelTbl_"+B,n,E,g,k,h={themodal:"delmod"+B,modalhead:"delhd"+B,modalcontent:"delcnt"+B,scrollelm:i};if(jQuery.isArray(f))f=f.join();if(a("#"+h.themodal).html()!=null){a("#DelData>td","#"+i).text(f);a("#DelError","#"+i).hide();if(d.processing===true){d.processing=false;a("#dData","#"+i).removeClass("ui-state-active")}C&&b.beforeShowForm(a("#"+i));viewModal("#"+h.themodal,{gbox:"#gbox_"+B,jqm:b.jqModal,jqM:false,
modal:b.modal})}else{var j=isNaN(b.dataheight)?b.dataheight:b.dataheight+"px";j="<div id='"+i+"' class='formdata' style='width:100%;overflow:auto;position:relative;height:"+j+";'>";j+="<table class='DelTable'><tbody>";j+="<tr id='DelError' style='display:none'><td class='ui-state-error'></td></tr>";j+="<tr id='DelData' style='display:none'><td >"+f+"</td></tr>";j+='<tr><td class="delmsg" style="white-space:pre;">'+b.msg+"</td></tr><tr><td >&#160;</td></tr>";j+="</tbody></table></div>";j+="<table cellspacing='0' cellpadding='0' border='0' class='EditTable' id='"+
i+"_2'><tbody><tr><td class='DataTD ui-widget-content'></td></tr><tr style='display:block;height:3px;'><td></td></tr><tr><td class='DelButton EditButton'>"+("<a href='javascript:void(0)' id='dData' class='fm-button ui-state-default ui-corner-all'>"+b.bSubmit+"</a>")+"&#160;"+("<a href='javascript:void(0)' id='eData' class='fm-button ui-state-default ui-corner-all'>"+b.bCancel+"</a>")+"</td></tr></tbody></table>";b.gbox="#gbox_"+B;createModal(h,j,b,"#gview_"+o.p.id,a("#gview_"+o.p.id)[0]);a(".fm-button",
"#"+i+"_2").hover(function(){a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});b.delicon=a.extend([true,"left","ui-icon-scissors"],b.delicon);b.cancelicon=a.extend([true,"left","ui-icon-cancel"],b.cancelicon);if(b.delicon[0]===true)a("#dData","#"+i+"_2").addClass(b.delicon[1]=="right"?"fm-button-icon-right":"fm-button-icon-left").append("<span class='ui-icon "+b.delicon[2]+"'></span>");if(b.cancelicon[0]===true)a("#eData","#"+i+"_2").addClass(b.cancelicon[1]==
"right"?"fm-button-icon-right":"fm-button-icon-left").append("<span class='ui-icon "+b.cancelicon[2]+"'></span>");a("#dData","#"+i+"_2").click(function(){var l=[true,""];w={};var s=a("#DelData>td","#"+i).text();if(typeof b.onclickSubmit==="function")w=b.onclickSubmit(d,s)||{};if(typeof b.beforeSubmit==="function")l=b.beforeSubmit(s);if(l[0]&&!d.processing){d.processing=true;a(this).addClass("ui-state-active");g=o.p.prmNames;n=a.extend({},d.delData,w);k=g.oper;n[k]=g.deloper;E=g.id;n[E]=s;var Q=a.extend({url:d.url?
d.url:a(o).jqGrid("getGridParam","editurl"),type:b.mtype,data:a.isFunction(b.serializeDelData)?b.serializeDelData(n):n,complete:function(m,q){if(q!="success"){l[0]=false;l[1]=a.isFunction(d.errorTextFormat)?d.errorTextFormat(m):q+" Status: '"+m.statusText+"'. Error code: "+m.status}else if(typeof d.afterSubmit==="function")l=d.afterSubmit(m,n);if(l[0]===false){a("#DelError>td","#"+i).html(l[1]);a("#DelError","#"+i).show()}else{if(d.reloadAfterSubmit&&o.p.datatype!="local")a(o).trigger("reloadGrid");
else{q=[];q=s.split(",");if(o.p.treeGrid===true)try{a(o).jqGrid("delTreeNode",q[0])}catch(A){}else for(var F=0;F<q.length;F++)a(o).jqGrid("delRowData",q[F]);o.p.selrow=null;o.p.selarrrow=[]}a.isFunction(d.afterComplete)&&setTimeout(function(){d.afterComplete(m,s)},500)}d.processing=false;a("#dData","#"+i+"_2").removeClass("ui-state-active");l[0]&&hideModal("#"+h.themodal,{gb:"#gbox_"+B,jqm:b.jqModal,onClose:d.onClose})},error:function(m,q,A){a("#DelError>td","#"+i).html(q+" : "+A);a("#DelError","#"+
i).show();d.processing=false;a("#dData","#"+i+"_2").removeClass("ui-state-active")}},a.jgrid.ajaxOptions,b.ajaxDelOptions);if(!Q.url&&!d.useDataProxy)if(a.isFunction(o.p.dataProxy))d.useDataProxy=true;else{l[0]=false;l[1]+=" "+a.jgrid.errors.nourl}if(l[0])d.useDataProxy?o.p.dataProxy.call(o,Q,"del_"+o.p.id):a.ajax(Q)}if(l[0]===false){a("#DelError>td","#"+i).html(l[1]);a("#DelError","#"+i).show()}return false});a("#eData","#"+i+"_2").click(function(){hideModal("#"+h.themodal,{gb:"#gbox_"+B,jqm:b.jqModal,
onClose:d.onClose});return false});C&&b.beforeShowForm(a("#"+i));viewModal("#"+h.themodal,{gbox:"#gbox_"+B,jqm:b.jqModal,modal:b.modal})}v&&b.afterShowForm(a("#"+i));b.closeOnEscape===true&&setTimeout(function(){a(".ui-jqdialog-titlebar-close","#"+h.modalhead).focus()},0)}})},navGrid:function(f,b,o,C,v,B,w){b=a.extend({edit:true,editicon:"ui-icon-pencil",add:true,addicon:"ui-icon-plus",del:true,delicon:"ui-icon-trash",search:true,searchicon:"ui-icon-search",refresh:true,refreshicon:"ui-icon-refresh",
refreshstate:"firstpage",view:false,viewicon:"ui-icon-document",position:"left",closeOnEscape:true,beforeRefresh:null,afterRefresh:null,cloneToTop:false},a.jgrid.nav,b||{});return this.each(function(){var i={themodal:"alertmod",modalhead:"alerthd",modalcontent:"alertcnt"},n=this,E,g,k;if(!(!n.grid||typeof f!="string")){if(a("#"+i.themodal).html()===null){if(typeof window.innerWidth!="undefined"){E=window.innerWidth;g=window.innerHeight}else if(typeof document.documentElement!="undefined"&&typeof document.documentElement.clientWidth!=
"undefined"&&document.documentElement.clientWidth!==0){E=document.documentElement.clientWidth;g=document.documentElement.clientHeight}else{E=1024;g=768}createModal(i,"<div>"+b.alerttext+"</div><span tabindex='0'><span tabindex='-1' id='jqg_alrt'></span></span>",{gbox:"#gbox_"+n.p.id,jqModal:true,drag:true,resize:true,caption:b.alertcap,top:g/2-25,left:E/2-100,width:200,height:"auto",closeOnEscape:b.closeOnEscape},"","",true)}E=1;if(b.cloneToTop&&n.p.toppager)E=2;for(g=0;g<E;g++){var h=a("<table cellspacing='0' cellpadding='0' border='0' class='ui-pg-table navtable' style='float:left;table-layout:auto;'><tbody><tr></tr></tbody></table>"),
j,l;if(g===0){j=f;l=n.p.id;if(j==n.p.toppager){l+="_top";E=1}}else{j=n.p.toppager;l=n.p.id+"_top"}n.p.direction=="rtl"&&a(h).attr("dir","rtl").css("float","right");if(b.add){C=C||{};k=a("<td class='ui-pg-button ui-corner-all'></td>");a(k).append("<div class='ui-pg-div'><span class='ui-icon "+b.addicon+"'></span>"+b.addtext+"</div>");a("tr",h).append(k);a(k,h).attr({title:b.addtitle||"",id:C.id||"add_"+l}).click(function(){a(this).hasClass("ui-state-disabled")||(typeof b.addfunc=="function"?b.addfunc():
a(n).jqGrid("editGridRow","new",C));return false}).hover(function(){a(this).hasClass("ui-state-disabled")||a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});k=null}if(b.edit){k=a("<td class='ui-pg-button ui-corner-all'></td>");o=o||{};a(k).append("<div class='ui-pg-div'><span class='ui-icon "+b.editicon+"'></span>"+b.edittext+"</div>");a("tr",h).append(k);a(k,h).attr({title:b.edittitle||"",id:o.id||"edit_"+l}).click(function(){if(!a(this).hasClass("ui-state-disabled")){var s=
n.p.selrow;if(s)typeof b.editfunc=="function"?b.editfunc(s):a(n).jqGrid("editGridRow",s,o);else{viewModal("#"+i.themodal,{gbox:"#gbox_"+n.p.id,jqm:true});a("#jqg_alrt").focus()}}return false}).hover(function(){a(this).hasClass("ui-state-disabled")||a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});k=null}if(b.view){k=a("<td class='ui-pg-button ui-corner-all'></td>");w=w||{};a(k).append("<div class='ui-pg-div'><span class='ui-icon "+b.viewicon+"'></span>"+b.viewtext+
"</div>");a("tr",h).append(k);a(k,h).attr({title:b.viewtitle||"",id:w.id||"view_"+l}).click(function(){if(!a(this).hasClass("ui-state-disabled")){var s=n.p.selrow;if(s)a(n).jqGrid("viewGridRow",s,w);else{viewModal("#"+i.themodal,{gbox:"#gbox_"+n.p.id,jqm:true});a("#jqg_alrt").focus()}}return false}).hover(function(){a(this).hasClass("ui-state-disabled")||a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});k=null}if(b.del){k=a("<td class='ui-pg-button ui-corner-all'></td>");
v=v||{};a(k).append("<div class='ui-pg-div'><span class='ui-icon "+b.delicon+"'></span>"+b.deltext+"</div>");a("tr",h).append(k);a(k,h).attr({title:b.deltitle||"",id:v.id||"del_"+l}).click(function(){if(!a(this).hasClass("ui-state-disabled")){var s;if(n.p.multiselect){s=n.p.selarrrow;if(s.length===0)s=null}else s=n.p.selrow;if(s)"function"==typeof b.delfunc?b.delfunc(s):a(n).jqGrid("delGridRow",s,v);else{viewModal("#"+i.themodal,{gbox:"#gbox_"+n.p.id,jqm:true});a("#jqg_alrt").focus()}}return false}).hover(function(){a(this).hasClass("ui-state-disabled")||
a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});k=null}if(b.add||b.edit||b.del||b.view)a("tr",h).append("<td class='ui-pg-button ui-state-disabled' style='width:4px;'><span class='ui-separator'></span></td>");if(b.search){k=a("<td class='ui-pg-button ui-corner-all'></td>");B=B||{};a(k).append("<div class='ui-pg-div'><span class='ui-icon "+b.searchicon+"'></span>"+b.searchtext+"</div>");a("tr",h).append(k);a(k,h).attr({title:b.searchtitle||"",id:B.id||"search_"+
l}).click(function(){a(this).hasClass("ui-state-disabled")||a(n).jqGrid("searchGrid",B);return false}).hover(function(){a(this).hasClass("ui-state-disabled")||a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});k=null}if(b.refresh){k=a("<td class='ui-pg-button ui-corner-all'></td>");a(k).append("<div class='ui-pg-div'><span class='ui-icon "+b.refreshicon+"'></span>"+b.refreshtext+"</div>");a("tr",h).append(k);a(k,h).attr({title:b.refreshtitle||"",id:"refresh_"+l}).click(function(){if(!a(this).hasClass("ui-state-disabled")){a.isFunction(b.beforeRefresh)&&
b.beforeRefresh();n.p.search=false;try{a("#fbox_"+n.p.id).searchFilter().reset({reload:false});a.isFunction(n.clearToolbar)&&n.clearToolbar(false)}catch(s){}switch(b.refreshstate){case "firstpage":a(n).trigger("reloadGrid",[{page:1}]);break;case "current":a(n).trigger("reloadGrid",[{current:true}]);break}a.isFunction(b.afterRefresh)&&b.afterRefresh()}return false}).hover(function(){a(this).hasClass("ui-state-disabled")||a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")});
k=null}k=a(".ui-jqgrid").css("font-size")||"11px";a("body").append("<div id='testpg2' class='ui-jqgrid ui-widget ui-widget-content' style='font-size:"+k+";visibility:hidden;' ></div>");k=a(h).clone().appendTo("#testpg2").width();a("#testpg2").remove();a(j+"_"+b.position,j).append(h);if(n.p._nvtd){if(k>n.p._nvtd[0]){a(j+"_"+b.position,j).width(k);n.p._nvtd[0]=k}n.p._nvtd[1]=k}h=k=k=null}}})},navButtonAdd:function(f,b){b=a.extend({caption:"newButton",title:"",buttonicon:"ui-icon-newwin",onClickButton:null,
position:"last",cursor:"pointer"},b||{});return this.each(function(){if(this.grid){if(f.indexOf("#")!==0)f="#"+f;var o=a(".navtable",f)[0],C=this;if(o){var v=a("<td></td>");b.buttonicon.toString().toUpperCase()=="NONE"?a(v).addClass("ui-pg-button ui-corner-all").append("<div class='ui-pg-div'>"+b.caption+"</div>"):a(v).addClass("ui-pg-button ui-corner-all").append("<div class='ui-pg-div'><span class='ui-icon "+b.buttonicon+"'></span>"+b.caption+"</div>");b.id&&a(v).attr("id",b.id);if(b.position==
"first")o.rows[0].cells.length===0?a("tr",o).append(v):a("tr td:eq(0)",o).before(v);else a("tr",o).append(v);a(v,o).attr("title",b.title||"").click(function(B){a(this).hasClass("ui-state-disabled")||a.isFunction(b.onClickButton)&&b.onClickButton.call(C,B);return false}).hover(function(){a(this).hasClass("ui-state-disabled")||a(this).addClass("ui-state-hover")},function(){a(this).removeClass("ui-state-hover")})}}})},navSeparatorAdd:function(f,b){b=a.extend({sepclass:"ui-separator",sepcontent:""},b||
{});return this.each(function(){if(this.grid){if(f.indexOf("#")!==0)f="#"+f;var o=a(".navtable",f)[0];if(o){var C="<td class='ui-pg-button ui-state-disabled' style='width:4px;'><span class='"+b.sepclass+"'></span>"+b.sepcontent+"</td>";a("tr",o).append(C)}}})},GridToForm:function(f,b){return this.each(function(){var o=this;if(o.grid){var C=a(o).jqGrid("getRowData",f);if(C)for(var v in C)a("[name="+v+"]",b).is("input:radio")||a("[name="+v+"]",b).is("input:checkbox")?a("[name="+v+"]",b).each(function(){a(this).val()==
C[v]?a(this).attr("checked","checked"):a(this).attr("checked","")}):a("[name="+v+"]",b).val(C[v])}})},FormToGrid:function(f,b,o,C){return this.each(function(){var v=this;if(v.grid){o||(o="set");C||(C="first");var B=a(b).serializeArray(),w={};a.each(B,function(i,n){w[n.name]=n.value});if(o=="add")a(v).jqGrid("addRowData",f,w,C);else o=="set"&&a(v).jqGrid("setRowData",f,w)}})}})})(jQuery);
jQuery.fn.searchFilter=function(k,H){function I(e,l,v){this.$=e;this.add=function(a){a==null?e.find(".ui-add-last").click():e.find(".sf:eq("+a+") .ui-add").click();return this};this.del=function(a){a==null?e.find(".sf:last .ui-del").click():e.find(".sf:eq("+a+") .ui-del").click();return this};this.search=function(){e.find(".ui-search").click();return this};this.reset=function(a){if(a===undefined)a=false;e.find(".ui-reset").trigger("click",[a]);return this};this.close=function(){e.find(".ui-closer").click();
return this};if(l!=null){function C(){jQuery(this).toggleClass("ui-state-hover");return false}function D(a){jQuery(this).toggleClass("ui-state-active",a.type=="mousedown");return false}function m(a,b){return"<option value='"+a+"'>"+b+"</option>"}function w(a,b,d){return"<select class='"+a+"'"+(d?" style='display:none;'":"")+">"+b+"</select>"}function E(a,b){a=e.find("tr.sf td.data "+a);a[0]!=null&&b(a)}function F(a,b){var d=e.find("tr.sf td.data "+a);d[0]!=null&&jQuery.each(b,function(){this.data!=
null?d.bind(this.type,this.data,this.fn):d.bind(this.type,this.fn)})}var f=jQuery.extend({},jQuery.fn.searchFilter.defaults,v),n=-1,r="";jQuery.each(f.groupOps,function(){r+=m(this.op,this.text)});r="<select name='groupOp'>"+r+"</select>";e.html("").addClass("ui-searchFilter").append("<div class='ui-widget-overlay' style='z-index: -1'>&#160;</div><table class='ui-widget-content ui-corner-all'><thead><tr><td colspan='5' class='ui-widget-header ui-corner-all' style='line-height: 18px;'><div class='ui-closer ui-state-default ui-corner-all ui-helper-clearfix' style='float: right;'><span class='ui-icon ui-icon-close'></span></div>"+
f.windowTitle+"</td></tr></thead><tbody><tr class='sf'><td class='fields'></td><td class='ops'></td><td class='data'></td><td><div class='ui-del ui-state-default ui-corner-all'><span class='ui-icon ui-icon-minus'></span></div></td><td><div class='ui-add ui-state-default ui-corner-all'><span class='ui-icon ui-icon-plus'></span></div></td></tr><tr><td colspan='5' class='divider'><div>&#160;</div></td></tr></tbody><tfoot><tr><td colspan='3'><span class='ui-reset ui-state-default ui-corner-all' style='display: inline-block; float: left;'><span class='ui-icon ui-icon-arrowreturnthick-1-w' style='float: left;'></span><span style='line-height: 18px; padding: 0 7px 0 3px;'>"+
f.resetText+"</span></span><span class='ui-search ui-state-default ui-corner-all' style='display: inline-block; float: right;'><span class='ui-icon ui-icon-search' style='float: left;'></span><span style='line-height: 18px; padding: 0 7px 0 3px;'>"+f.searchText+"</span></span><span class='matchText'>"+f.matchText+"</span> "+r+" <span class='rulesText'>"+f.rulesText+"</span></td><td>&#160;</td><td><div class='ui-add-last ui-state-default ui-corner-all'><span class='ui-icon ui-icon-plusthick'></span></div></td></tr></tfoot></table>");
var x=e.find("tr.sf"),G=x.find("td.fields"),y=x.find("td.ops"),o=x.find("td.data"),s="";jQuery.each(f.operators,function(){s+=m(this.op,this.text)});s=w("default",s,true);y.append(s);o.append("<input type='text' class='default' style='display:none;' />");var t="",z=false,p=false;jQuery.each(l,function(a){t+=m(this.itemval,this.text);if(this.ops!=null){z=true;var b="";jQuery.each(this.ops,function(){b+=m(this.op,this.text)});b=w("field"+a,b,true);y.append(b)}if(this.dataUrl!=null){if(a>n)n=a;p=true;
var d=this.dataEvents,c=this.dataInit,g=this.buildSelect;jQuery.ajax(jQuery.extend({url:this.dataUrl,complete:function(h){h=g!=null?jQuery("<div />").append(g(h)):jQuery("<div />").append(h.responseText);h.find("select").addClass("field"+a).hide();o.append(h.html());c&&E(".field"+a,c);d&&F(".field"+a,d);a==n&&e.find("tr.sf td.fields select[name='field']").change()}},f.ajaxSelectOptions))}else if(this.dataValues!=null){p=true;var i="";jQuery.each(this.dataValues,function(){i+=m(this.value,this.text)});
i=w("field"+a,i,true);o.append(i)}else if(this.dataEvents!=null||this.dataInit!=null){p=true;i="<input type='text' class='field"+a+"' />";o.append(i)}this.dataInit!=null&&a!=n&&E(".field"+a,this.dataInit);this.dataEvents!=null&&a!=n&&F(".field"+a,this.dataEvents)});t="<select name='field'>"+t+"</select>";G.append(t);l=G.find("select[name='field']");z?l.change(function(a){var b=a.target.selectedIndex;a=jQuery(a.target).parents("tr.sf").find("td.ops");a.find("select").removeAttr("name").hide();b=a.find(".field"+
b);if(b[0]==null)b=a.find(".default");b.attr("name","op").show();return false}):y.find(".default").attr("name","op").show();p?l.change(function(a){var b=a.target.selectedIndex;a=jQuery(a.target).parents("tr.sf").find("td.data");a.find("select,input").removeClass("vdata").hide();b=a.find(".field"+b);if(b[0]==null)b=a.find(".default");b.show().addClass("vdata");return false}):o.find(".default").show().addClass("vdata");if(z||p)l.change();e.find(".ui-state-default").hover(C,C).mousedown(D).mouseup(D);
e.find(".ui-closer").click(function(){f.onClose(jQuery(e.selector));return false});e.find(".ui-del").click(function(a){a=jQuery(a.target).parents(".sf");if(a.siblings(".sf").length>0){f.datepickerFix===true&&jQuery.fn.datepicker!==undefined&&a.find(".hasDatepicker").datepicker("destroy");a.remove()}else{a.find("select[name='field']")[0].selectedIndex=0;a.find("select[name='op']")[0].selectedIndex=0;a.find(".data input").val("");a.find(".data select").each(function(){this.selectedIndex=0});a.find("select[name='field']").change(function(b){b.stopPropagation()})}return false});
e.find(".ui-add").click(function(a){a=jQuery(a.target).parents(".sf");var b=a.clone(true).insertAfter(a);b.find(".ui-state-default").removeClass("ui-state-hover ui-state-active");if(f.clone){b.find("select[name='field']")[0].selectedIndex=a.find("select[name='field']")[0].selectedIndex;if(b.find("select[name='op']")[0]!=null)b.find("select[name='op']").focus()[0].selectedIndex=a.find("select[name='op']")[0].selectedIndex;var d=b.find("select.vdata");if(d[0]!=null)d[0].selectedIndex=a.find("select.vdata")[0].selectedIndex}else{b.find(".data input").val("");
b.find("select[name='field']").focus()}f.datepickerFix===true&&jQuery.fn.datepicker!==undefined&&a.find(".hasDatepicker").each(function(){var c=jQuery.data(this,"datepicker").settings;b.find("#"+this.id).unbind().removeAttr("id").removeClass("hasDatepicker").datepicker(c)});b.find("select[name='field']").change(function(c){c.stopPropagation()});return false});e.find(".ui-search").click(function(){var a=jQuery(e.selector),b,d=a.find("select[name='groupOp'] :selected").val();b=f.stringResult?'{"groupOp":"'+
d+'","rules":[':{groupOp:d,rules:[]};a.find(".sf").each(function(c){var g=jQuery(this).find("select[name='field'] :selected").val(),i=jQuery(this).find("select[name='op'] :selected").val(),h=jQuery(this).find("input.vdata,select.vdata :selected").val();h+="";h=h.replace(/\\/g,"\\\\").replace(/\"/g,'\\"');if(f.stringResult){if(c>0)b+=",";b+='{"field":"'+g+'",';b+='"op":"'+i+'",';b+='"data":"'+h+'"}'}else b.rules.push({field:g,op:i,data:h})});if(f.stringResult)b+="]}";f.onSearch(b);return false});e.find(".ui-reset").click(function(a,
b){a=jQuery(e.selector);a.find(".ui-del").click();a.find("select[name='groupOp']")[0].selectedIndex=0;f.onReset(b);return false});e.find(".ui-add-last").click(function(){var a=jQuery(e.selector+" .sf:last"),b=a.clone(true).insertAfter(a);b.find(".ui-state-default").removeClass("ui-state-hover ui-state-active");b.find(".data input").val("");b.find("select[name='field']").focus();f.datepickerFix===true&&jQuery.fn.datepicker!==undefined&&a.find(".hasDatepicker").each(function(){var d=jQuery.data(this,
"datepicker").settings;b.find("#"+this.id).unbind().removeAttr("id").removeClass("hasDatepicker").datepicker(d)});b.find("select[name='field']").change(function(d){d.stopPropagation()});return false});this.setGroupOp=function(a){selDOMobj=e.find("select[name='groupOp']")[0];var b={},d=selDOMobj.options.length,c;for(c=0;c<d;c++)b[selDOMobj.options[c].value]=c;selDOMobj.selectedIndex=b[a];jQuery(selDOMobj).change(function(g){g.stopPropagation()})};this.setFilter=function(a){var b=a.sfref;a=a.filter;
var d=[],c,g,i,h,j={};selDOMobj=b.find("select[name='field']")[0];c=0;for(i=selDOMobj.options.length;c<i;c++){j[selDOMobj.options[c].value]={index:c,ops:{}};d.push(selDOMobj.options[c].value)}c=0;for(i=d.length;c<i;c++){if(selDOMobj=b.find(".ops > select[class='field"+c+"']")[0]){g=0;for(h=selDOMobj.options.length;g<h;g++)j[d[c]].ops[selDOMobj.options[g].value]=g}if(selDOMobj=b.find(".data > select[class='field"+c+"']")[0]){j[d[c]].data={};g=0;for(h=selDOMobj.options.length;g<h;g++)j[d[c]].data[selDOMobj.options[g].value]=
g}}var u,q,A,B;d=a.field;if(j[d])u=j[d].index;if(u!=null){q=j[d].ops[a.op];if(q===undefined){c=0;for(i=v.operators.length;c<i;c++)if(v.operators[c].op==a.op){q=c;break}}A=a.data;B=j[d].data==null?-1:j[d].data[A]}if(u!=null&&q!=null&&B!=null){b.find("select[name='field']")[0].selectedIndex=u;b.find("select[name='field']").change();b.find("select[name='op']")[0].selectedIndex=q;b.find("input.vdata").val(A);if(b=b.find("select.vdata")[0])b.selectedIndex=B;return true}else return false}}}return new I(this,
k,H)};jQuery.fn.searchFilter.version="1.2.9";
jQuery.fn.searchFilter.defaults={clone:true,datepickerFix:true,onReset:function(k){alert("Reset Clicked. Data Returned: "+k)},onSearch:function(k){alert("Search Clicked. Data Returned: "+k)},onClose:function(k){k.hide()},groupOps:[{op:"AND",text:"all"},{op:"OR",text:"any"}],operators:[{op:"eq",text:"is equal to"},{op:"ne",text:"is not equal to"},{op:"lt",text:"is less than"},{op:"le",text:"is less or equal to"},{op:"gt",text:"is greater than"},{op:"ge",text:"is greater or equal to"},{op:"in",text:"is in"},
{op:"ni",text:"is not in"},{op:"bw",text:"begins with"},{op:"bn",text:"does not begin with"},{op:"ew",text:"ends with"},{op:"en",text:"does not end with"},{op:"cn",text:"contains"},{op:"nc",text:"does not contain"}],matchText:"match",rulesText:"rules",resetText:"Reset",searchText:"Search",stringResult:true,windowTitle:"Search Rules",ajaxSelectOptions:{}};
(function(a){a.jgrid.extend({editRow:function(d,u,i,n,o,r,s,c,f){return this.each(function(){var b=this,k,l,t=0,p=null,q={},h,g;if(b.grid){h=a(b).jqGrid("getInd",d,true);if(h!==false)if((a(h).attr("editable")||"0")=="0"&&!a(h).hasClass("not-editable-row")){g=b.p.colModel;a("td",h).each(function(j){k=g[j].name;var v=b.p.treeGrid===true&&k==b.p.ExpandColumn;if(v)l=a("span:first",this).html();else try{l=a.unformat(this,{rowId:d,colModel:g[j]},j)}catch(m){l=a(this).html()}if(k!="cb"&&k!="subgrid"&&k!=
"rn"){if(b.p.autoencode)l=a.jgrid.htmlDecode(l);q[k]=l;if(g[j].editable===true){if(p===null)p=j;v?a("span:first",this).html(""):a(this).html("");var e=a.extend({},g[j].editoptions||{},{id:d+"_"+k,name:k});if(!g[j].edittype)g[j].edittype="text";e=createEl(g[j].edittype,e,l,true,a.extend({},a.jgrid.ajaxOptions,b.p.ajaxSelectOptions||{}));a(e).addClass("editable");v?a("span:first",this).append(e):a(this).append(e);g[j].edittype=="select"&&g[j].editoptions.multiple===true&&a.browser.msie&&a(e).width(a(e).width());
t++}}});if(t>0){q.id=d;b.p.savedRow.push(q);a(h).attr("editable","1");a("td:eq("+p+") input",h).focus();u===true&&a(h).bind("keydown",function(j){j.keyCode===27&&a(b).jqGrid("restoreRow",d,f);if(j.keyCode===13){if(j.target.tagName=="TEXTAREA")return true;a(b).jqGrid("saveRow",d,n,o,r,s,c,f);return false}j.stopPropagation()});a.isFunction(i)&&i.call(b,d)}}}})},saveRow:function(d,u,i,n,o,r,s){return this.each(function(){var c=this,f,b={},k={},l,t,p,q;if(c.grid){q=a(c).jqGrid("getInd",d,true);if(q!==
false){l=a(q).attr("editable");i=i?i:c.p.editurl;if(l==="1"){var h;a("td",q).each(function(m){h=c.p.colModel[m];f=h.name;if(f!="cb"&&f!="subgrid"&&h.editable===true&&f!="rn"){switch(h.edittype){case "checkbox":var e=["Yes","No"];if(h.editoptions)e=h.editoptions.value.split(":");b[f]=a("input",this).attr("checked")?e[0]:e[1];break;case "text":case "password":case "textarea":case "button":b[f]=a("input, textarea",this).val();break;case "select":if(h.editoptions.multiple){e=a("select",this);var x=[];
b[f]=a(e).val();b[f]=b[f]?b[f].join(","):"";a("select > option:selected",this).each(function(y,z){x[y]=a(z).text()});k[f]=x.join(",")}else{b[f]=a("select>option:selected",this).val();k[f]=a("select>option:selected",this).text()}if(h.formatter&&h.formatter=="select")k={};break;case "custom":try{if(h.editoptions&&a.isFunction(h.editoptions.custom_value)){b[f]=h.editoptions.custom_value.call(c,a(".customelement",this),"get");if(b[f]===undefined)throw"e2";}else throw"e1";}catch(w){w=="e1"&&info_dialog(jQuery.jgrid.errors.errcap,
"function 'custom_value' "+a.jgrid.edit.msg.nodefined,jQuery.jgrid.edit.bClose);w=="e2"?info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+a.jgrid.edit.msg.novalue,jQuery.jgrid.edit.bClose):info_dialog(jQuery.jgrid.errors.errcap,w.message,jQuery.jgrid.edit.bClose)}break}p=checkValues(b[f],m,c);if(p[0]===false){p[1]=b[f]+" "+p[1];return false}if(c.p.autoencode)b[f]=a.jgrid.htmlEncode(b[f])}});if(p[0]===false)try{var g=findPos(a("#"+a.jgrid.jqID(d),c.grid.bDiv)[0]);info_dialog(a.jgrid.errors.errcap,
p[1],a.jgrid.edit.bClose,{left:g[0],top:g[1]})}catch(j){alert(p[1])}else{if(b){var v;g=c.p.prmNames;v=g.oper;l=g.id;b[v]=g.editoper;b[l]=d;if(typeof c.p.inlineData=="undefined")c.p.inlineData={};if(typeof n=="undefined")n={};b=a.extend({},b,c.p.inlineData,n)}if(i=="clientArray"){b=a.extend({},b,k);c.p.autoencode&&a.each(b,function(m,e){b[m]=a.jgrid.htmlDecode(e)});l=a(c).jqGrid("setRowData",d,b);a(q).attr("editable","0");for(g=0;g<c.p.savedRow.length;g++)if(c.p.savedRow[g].id==d){t=g;break}t>=0&&
c.p.savedRow.splice(t,1);a.isFunction(o)&&o.call(c,d,l)}else{a("#lui_"+c.p.id).show();a.ajax(a.extend({url:i,data:a.isFunction(c.p.serializeRowData)?c.p.serializeRowData.call(c,b):b,type:"POST",complete:function(m,e){a("#lui_"+c.p.id).hide();if(e==="success")if((a.isFunction(u)?u.call(c,m):true)===true){c.p.autoencode&&a.each(b,function(x,w){b[x]=a.jgrid.htmlDecode(w)});b=a.extend({},b,k);a(c).jqGrid("setRowData",d,b);a(q).attr("editable","0");for(e=0;e<c.p.savedRow.length;e++)if(c.p.savedRow[e].id==
d){t=e;break}t>=0&&c.p.savedRow.splice(t,1);a.isFunction(o)&&o.call(c,d,m)}else{a.isFunction(r)&&r.call(c,d,m,e);a(c).jqGrid("restoreRow",d,s)}},error:function(m,e){a("#lui_"+c.p.id).hide();a.isFunction(r)?r.call(c,d,m,e):alert("Error Row: "+d+" Result: "+m.status+":"+m.statusText+" Status: "+e);a(c).jqGrid("restoreRow",d,s)}},a.jgrid.ajaxOptions,c.p.ajaxRowOptions||{}))}a(q).unbind("keydown")}}}}})},restoreRow:function(d,u){return this.each(function(){var i=this,n,o,r={};if(i.grid){o=a(i).jqGrid("getInd",
d,true);if(o!==false){for(var s=0;s<i.p.savedRow.length;s++)if(i.p.savedRow[s].id==d){n=s;break}if(n>=0){if(a.isFunction(a.fn.datepicker))try{a("input.hasDatepicker","#"+a.jgrid.jqID(o.id)).datepicker("hide")}catch(c){}a.each(i.p.colModel,function(){if(this.editable===true&&this.name in i.p.savedRow[n])r[this.name]=i.p.savedRow[n][this.name]});a(i).jqGrid("setRowData",d,r);a(o).attr("editable","0").unbind("keydown");i.p.savedRow.splice(n,1)}a.isFunction(u)&&u.call(i,d)}}})}})})(jQuery);
(function(b){b.jgrid.extend({editCell:function(d,e,a){return this.each(function(){var c=this,h,f,g;if(!(!c.grid||c.p.cellEdit!==true)){e=parseInt(e,10);c.p.selrow=c.rows[d].id;c.p.knv||b(c).jqGrid("GridNav");if(c.p.savedRow.length>0){if(a===true)if(d==c.p.iRow&&e==c.p.iCol)return;b(c).jqGrid("saveCell",c.p.savedRow[0].id,c.p.savedRow[0].ic)}else window.setTimeout(function(){b("#"+c.p.knv).attr("tabindex","-1").focus()},0);h=c.p.colModel[e].name;if(!(h=="subgrid"||h=="cb"||h=="rn")){g=b("td:eq("+e+
")",c.rows[d]);if(c.p.colModel[e].editable===true&&a===true&&!g.hasClass("not-editable-cell")){if(parseInt(c.p.iCol,10)>=0&&parseInt(c.p.iRow,10)>=0){b("td:eq("+c.p.iCol+")",c.rows[c.p.iRow]).removeClass("edit-cell ui-state-highlight");b(c.rows[c.p.iRow]).removeClass("selected-row ui-state-hover")}b(g).addClass("edit-cell ui-state-highlight");b(c.rows[d]).addClass("selected-row ui-state-hover");try{f=b.unformat(g,{rowId:c.rows[d].id,colModel:c.p.colModel[e]},e)}catch(k){f=b(g).html()}if(c.p.autoencode)f=
b.jgrid.htmlDecode(f);if(!c.p.colModel[e].edittype)c.p.colModel[e].edittype="text";c.p.savedRow.push({id:d,ic:e,name:h,v:f});if(b.isFunction(c.p.formatCell)){var j=c.p.formatCell.call(c,c.rows[d].id,h,f,d,e);if(j!==undefined)f=j}j=b.extend({},c.p.colModel[e].editoptions||{},{id:d+"_"+h,name:h});var i=createEl(c.p.colModel[e].edittype,j,f,true,b.extend({},b.jgrid.ajaxOptions,c.p.ajaxSelectOptions||{}));b.isFunction(c.p.beforeEditCell)&&c.p.beforeEditCell.call(c,c.rows[d].id,h,f,d,e);b(g).html("").append(i).attr("tabindex",
"0");window.setTimeout(function(){b(i).focus()},0);b("input, select, textarea",g).bind("keydown",function(l){if(l.keyCode===27)if(b("input.hasDatepicker",g).length>0)b(".ui-datepicker").is(":hidden")?b(c).jqGrid("restoreCell",d,e):b("input.hasDatepicker",g).datepicker("hide");else b(c).jqGrid("restoreCell",d,e);l.keyCode===13&&b(c).jqGrid("saveCell",d,e);if(l.keyCode==9)if(c.grid.hDiv.loading)return false;else l.shiftKey?b(c).jqGrid("prevCell",d,e):b(c).jqGrid("nextCell",d,e);l.stopPropagation()});
b.isFunction(c.p.afterEditCell)&&c.p.afterEditCell.call(c,c.rows[d].id,h,f,d,e)}else{if(parseInt(c.p.iCol,10)>=0&&parseInt(c.p.iRow,10)>=0){b("td:eq("+c.p.iCol+")",c.rows[c.p.iRow]).removeClass("edit-cell ui-state-highlight");b(c.rows[c.p.iRow]).removeClass("selected-row ui-state-hover")}g.addClass("edit-cell ui-state-highlight");b(c.rows[d]).addClass("selected-row ui-state-hover");if(b.isFunction(c.p.onSelectCell)){f=g.html().replace(/\&#160\;/ig,"");c.p.onSelectCell.call(c,c.rows[d].id,h,f,d,e)}}c.p.iCol=
e;c.p.iRow=d}}})},saveCell:function(d,e){return this.each(function(){var a=this,c;if(!(!a.grid||a.p.cellEdit!==true)){c=a.p.savedRow.length>=1?0:null;if(c!==null){var h=b("td:eq("+e+")",a.rows[d]),f,g,k=a.p.colModel[e],j=k.name,i=b.jgrid.jqID(j);switch(k.edittype){case "select":if(k.editoptions.multiple){i=b("#"+d+"_"+i,a.rows[d]);var l=[];if(f=b(i).val())f.join(",");else f="";b("option:selected",i).each(function(m,p){l[m]=b(p).text()});g=l.join(",")}else{f=b("#"+d+"_"+i+">option:selected",a.rows[d]).val();
g=b("#"+d+"_"+i+">option:selected",a.rows[d]).text()}if(k.formatter)g=f;break;case "checkbox":var n=["Yes","No"];if(k.editoptions)n=k.editoptions.value.split(":");g=f=b("#"+d+"_"+i,a.rows[d]).attr("checked")?n[0]:n[1];break;case "password":case "text":case "textarea":case "button":g=f=b("#"+d+"_"+i,a.rows[d]).val();break;case "custom":try{if(k.editoptions&&b.isFunction(k.editoptions.custom_value)){f=k.editoptions.custom_value.call(a,b(".customelement",h),"get");if(f===undefined)throw"e2";else g=f}else throw"e1";
}catch(q){q=="e1"&&info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+b.jgrid.edit.msg.nodefined,jQuery.jgrid.edit.bClose);q=="e2"?info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+b.jgrid.edit.msg.novalue,jQuery.jgrid.edit.bClose):info_dialog(jQuery.jgrid.errors.errcap,q.message,jQuery.jgrid.edit.bClose)}break}if(g!=a.p.savedRow[c].v){if(b.isFunction(a.p.beforeSaveCell))if(c=a.p.beforeSaveCell.call(a,a.rows[d].id,j,f,d,e))f=c;var r=checkValues(f,e,a);if(r[0]===true){c=
{};if(b.isFunction(a.p.beforeSubmitCell))(c=a.p.beforeSubmitCell.call(a,a.rows[d].id,j,f,d,e))||(c={});b("input.hasDatepicker",h).length>0&&b("input.hasDatepicker",h).datepicker("hide");if(a.p.cellsubmit=="remote")if(a.p.cellurl){var o={};if(a.p.autoencode)f=b.jgrid.htmlEncode(f);o[j]=f;n=a.p.prmNames;k=n.id;i=n.oper;o[k]=a.rows[d].id;o[i]=n.editoper;o=b.extend(c,o);b("#lui_"+a.p.id).show();a.grid.hDiv.loading=true;b.ajax(b.extend({url:a.p.cellurl,data:b.isFunction(a.p.serializeCellData)?a.p.serializeCellData.call(a,
o):o,type:"POST",complete:function(m,p){b("#lui_"+a.p.id).hide();a.grid.hDiv.loading=false;if(p=="success")if(b.isFunction(a.p.afterSubmitCell)){m=a.p.afterSubmitCell.call(a,m,o.id,j,f,d,e);if(m[0]===true){b(h).empty();b(a).jqGrid("setCell",a.rows[d].id,e,g,false,false,true);b(h).addClass("dirty-cell");b(a.rows[d]).addClass("edited");b.isFunction(a.p.afterSaveCell)&&a.p.afterSaveCell.call(a,a.rows[d].id,j,f,d,e);a.p.savedRow.splice(0,1)}else{info_dialog(b.jgrid.errors.errcap,m[1],b.jgrid.edit.bClose);
b(a).jqGrid("restoreCell",d,e)}}else{b(h).empty();b(a).jqGrid("setCell",a.rows[d].id,e,g,false,false,true);b(h).addClass("dirty-cell");b(a.rows[d]).addClass("edited");b.isFunction(a.p.afterSaveCell)&&a.p.afterSaveCell.call(a,a.rows[d].id,j,f,d,e);a.p.savedRow.splice(0,1)}},error:function(m,p){b("#lui_"+a.p.id).hide();a.grid.hDiv.loading=false;b.isFunction(a.p.errorCell)?a.p.errorCell.call(a,m,p):info_dialog(b.jgrid.errors.errcap,m.status+" : "+m.statusText+"<br/>"+p,b.jgrid.edit.bClose);b(a).jqGrid("restoreCell",
d,e)}},b.jgrid.ajaxOptions,a.p.ajaxCellOptions||{}))}else try{info_dialog(b.jgrid.errors.errcap,b.jgrid.errors.nourl,b.jgrid.edit.bClose);b(a).jqGrid("restoreCell",d,e)}catch(s){}if(a.p.cellsubmit=="clientArray"){b(h).empty();b(a).jqGrid("setCell",a.rows[d].id,e,g,false,false,true);b(h).addClass("dirty-cell");b(a.rows[d]).addClass("edited");b.isFunction(a.p.afterSaveCell)&&a.p.afterSaveCell.call(a,a.rows[d].id,j,f,d,e);a.p.savedRow.splice(0,1)}}else try{window.setTimeout(function(){info_dialog(b.jgrid.errors.errcap,
f+" "+r[1],b.jgrid.edit.bClose)},100);b(a).jqGrid("restoreCell",d,e)}catch(t){}}else b(a).jqGrid("restoreCell",d,e)}b.browser.opera?b("#"+a.p.knv).attr("tabindex","-1").focus():window.setTimeout(function(){b("#"+a.p.knv).attr("tabindex","-1").focus()},0)}})},restoreCell:function(d,e){return this.each(function(){var a=this,c;if(!(!a.grid||a.p.cellEdit!==true)){c=a.p.savedRow.length>=1?0:null;if(c!==null){var h=b("td:eq("+e+")",a.rows[d]);if(b.isFunction(b.fn.datepicker))try{b("input.hasDatepicker",
h).datepicker("hide")}catch(f){}b(h).empty().attr("tabindex","-1");b(a).jqGrid("setCell",a.rows[d].id,e,a.p.savedRow[c].v,false,false,true);b.isFunction(a.p.afterRestoreCell)&&a.p.afterRestoreCell.call(a,a.rows[d].id,a.p.savedRow[c].v,d,e);a.p.savedRow.splice(0,1)}window.setTimeout(function(){b("#"+a.p.knv).attr("tabindex","-1").focus()},0)}})},nextCell:function(d,e){return this.each(function(){var a=this,c=false;if(!(!a.grid||a.p.cellEdit!==true)){for(var h=e+1;h<a.p.colModel.length;h++)if(a.p.colModel[h].editable===
true){c=h;break}if(c!==false)b(a).jqGrid("editCell",d,c,true);else a.p.savedRow.length>0&&b(a).jqGrid("saveCell",d,e)}})},prevCell:function(d,e){return this.each(function(){var a=this,c=false;if(!(!a.grid||a.p.cellEdit!==true)){for(var h=e-1;h>=0;h--)if(a.p.colModel[h].editable===true){c=h;break}if(c!==false)b(a).jqGrid("editCell",d,c,true);else a.p.savedRow.length>0&&b(a).jqGrid("saveCell",d,e)}})},GridNav:function(){return this.each(function(){function d(g,k,j){if(j.substr(0,1)=="v"){var i=b(a.grid.bDiv)[0].clientHeight,
l=b(a.grid.bDiv)[0].scrollTop,n=a.rows[g].offsetTop+a.rows[g].clientHeight,q=a.rows[g].offsetTop;if(j=="vd")if(n>=i)b(a.grid.bDiv)[0].scrollTop=b(a.grid.bDiv)[0].scrollTop+a.rows[g].clientHeight;if(j=="vu")if(q<l)b(a.grid.bDiv)[0].scrollTop=b(a.grid.bDiv)[0].scrollTop-a.rows[g].clientHeight}if(j=="h"){j=b(a.grid.bDiv)[0].clientWidth;i=b(a.grid.bDiv)[0].scrollLeft;l=a.rows[g].cells[k].offsetLeft;if(a.rows[g].cells[k].offsetLeft+a.rows[g].cells[k].clientWidth>=j+parseInt(i,10))b(a.grid.bDiv)[0].scrollLeft=
b(a.grid.bDiv)[0].scrollLeft+a.rows[g].cells[k].clientWidth;else if(l<i)b(a.grid.bDiv)[0].scrollLeft=b(a.grid.bDiv)[0].scrollLeft-a.rows[g].cells[k].clientWidth}}function e(g,k){var j,i;if(k=="lft"){j=g+1;for(i=g;i>=0;i--)if(a.p.colModel[i].hidden!==true){j=i;break}}if(k=="rgt"){j=g-1;for(i=g;i<a.p.colModel.length;i++)if(a.p.colModel[i].hidden!==true){j=i;break}}return j}var a=this;if(!(!a.grid||a.p.cellEdit!==true)){a.p.knv=a.p.id+"_kn";var c=b("<span style='width:0px;height:0px;background-color:black;' tabindex='0'><span tabindex='-1' style='width:0px;height:0px;background-color:grey' id='"+
a.p.knv+"'></span></span>"),h,f;b(c).insertBefore(a.grid.cDiv);b("#"+a.p.knv).focus().keydown(function(g){f=g.keyCode;if(a.p.direction=="rtl")if(f==37)f=39;else if(f==39)f=37;switch(f){case 38:if(a.p.iRow-1>0){d(a.p.iRow-1,a.p.iCol,"vu");b(a).jqGrid("editCell",a.p.iRow-1,a.p.iCol,false)}break;case 40:if(a.p.iRow+1<=a.rows.length-1){d(a.p.iRow+1,a.p.iCol,"vd");b(a).jqGrid("editCell",a.p.iRow+1,a.p.iCol,false)}break;case 37:if(a.p.iCol-1>=0){h=e(a.p.iCol-1,"lft");d(a.p.iRow,h,"h");b(a).jqGrid("editCell",
a.p.iRow,h,false)}break;case 39:if(a.p.iCol+1<=a.p.colModel.length-1){h=e(a.p.iCol+1,"rgt");d(a.p.iRow,h,"h");b(a).jqGrid("editCell",a.p.iRow,h,false)}break;case 13:parseInt(a.p.iCol,10)>=0&&parseInt(a.p.iRow,10)>=0&&b(a).jqGrid("editCell",a.p.iRow,a.p.iCol,true);break}return false})}})},getChangedCells:function(d){var e=[];d||(d="all");this.each(function(){var a=this,c;!a.grid||a.p.cellEdit!==true||b(a.rows).each(function(h){var f={};if(b(this).hasClass("edited")){b("td",this).each(function(g){c=
a.p.colModel[g].name;if(c!=="cb"&&c!=="subgrid")if(d=="dirty"){if(b(this).hasClass("dirty-cell"))try{f[c]=b.unformat(this,{rowId:a.rows[h].id,colModel:a.p.colModel[g]},g)}catch(k){f[c]=b.jgrid.htmlDecode(b(this).html())}}else try{f[c]=b.unformat(this,{rowId:a.rows[h].id,colModel:a.p.colModel[g]},g)}catch(j){f[c]=b.jgrid.htmlDecode(b(this).html())}});f.id=this.id;e.push(f)}})});return e}})})(jQuery);
(function(b){b.fn.jqm=function(a){var f={overlay:50,closeoverlay:true,overlayClass:"jqmOverlay",closeClass:"jqmClose",trigger:".jqModal",ajax:e,ajaxText:"",target:e,modal:e,toTop:e,onShow:e,onHide:e,onLoad:e};return this.each(function(){if(this._jqm)return i[this._jqm].c=b.extend({},i[this._jqm].c,a);l++;this._jqm=l;i[l]={c:b.extend(f,b.jqm.params,a),a:e,w:b(this).addClass("jqmID"+l),s:l};f.trigger&&b(this).jqmAddTrigger(f.trigger)})};b.fn.jqmAddClose=function(a){return o(this,a,"jqmHide")};b.fn.jqmAddTrigger=
function(a){return o(this,a,"jqmShow")};b.fn.jqmShow=function(a){return this.each(function(){b.jqm.open(this._jqm,a)})};b.fn.jqmHide=function(a){return this.each(function(){b.jqm.close(this._jqm,a)})};b.jqm={hash:{},open:function(a,f){var c=i[a],d=c.c,h="."+d.closeClass,g=parseInt(c.w.css("z-index"));g=g>0?g:3E3;var j=b("<div></div>").css({height:"100%",width:"100%",position:"fixed",left:0,top:0,"z-index":g-1,opacity:d.overlay/100});if(c.a)return e;c.t=f;c.a=true;c.w.css("z-index",g);if(d.modal){k[0]||
setTimeout(function(){p("bind")},1);k.push(a)}else if(d.overlay>0)d.closeoverlay&&c.w.jqmAddClose(j);else j=e;c.o=j?j.addClass(d.overlayClass).prependTo("body"):e;if(q){b("html,body").css({height:"100%",width:"100%"});if(j){j=j.css({position:"absolute"})[0];for(var m in{Top:1,Left:1})j.style.setExpression(m.toLowerCase(),"(_=(document.documentElement.scroll"+m+" || document.body.scroll"+m+"))+'px'")}}if(d.ajax){a=d.target||c.w;g=d.ajax;a=typeof a=="string"?b(a,c.w):b(a);g=g.substr(0,1)=="@"?b(f).attr(g.substring(1)):
g;a.html(d.ajaxText).load(g,function(){d.onLoad&&d.onLoad.call(this,c);h&&c.w.jqmAddClose(b(h,c.w));r(c)})}else h&&c.w.jqmAddClose(b(h,c.w));d.toTop&&c.o&&c.w.before('<span id="jqmP'+c.w[0]._jqm+'"></span>').insertAfter(c.o);d.onShow?d.onShow(c):c.w.show();r(c);return e},close:function(a){a=i[a];if(!a.a)return e;a.a=e;if(k[0]){k.pop();k[0]||p("unbind")}a.c.toTop&&a.o&&b("#jqmP"+a.w[0]._jqm).after(a.w).remove();if(a.c.onHide)a.c.onHide(a);else{a.w.hide();a.o&&a.o.remove()}return e},params:{}};var l=
0,i=b.jqm.hash,k=[],q=b.browser.msie&&b.browser.version=="6.0",e=false,r=function(a){var f=b('<iframe src="javascript:false;document.write(\'\');" class="jqm"></iframe>').css({opacity:0});if(q)if(a.o)a.o.html('<p style="width:100%;height:100%"/>').prepend(f);else b("iframe.jqm",a.w)[0]||a.w.prepend(f);s(a)},s=function(a){try{b(":input:visible",a.w)[0].focus()}catch(f){}},p=function(a){b(document)[a]("keypress",n)[a]("keydown",n)[a]("mousedown",n)},n=function(a){var f=i[k[k.length-1]];(a=!b(a.target).parents(".jqmID"+
f.s)[0])&&s(f);return!a},o=function(a,f,c){return a.each(function(){var d=this._jqm;b(f).each(function(){if(!this[c]){this[c]=[];b(this).click(function(){for(var h in{jqmShow:1,jqmHide:1})for(var g in this[h])i[this[h][g]]&&i[this[h][g]].w[h](this);return e})}this[c].push(d)})})}})(jQuery);
(function(b){b.fn.jqDrag=function(a){return l(this,a,"d")};b.fn.jqResize=function(a,e){return l(this,a,"r",e)};b.jqDnR={dnr:{},e:0,drag:function(a){if(c.k=="d")d.css({left:c.X+a.pageX-c.pX,top:c.Y+a.pageY-c.pY});else{d.css({width:Math.max(a.pageX-c.pX+c.W,0),height:Math.max(a.pageY-c.pY+c.H,0)});M1&&f.css({width:Math.max(a.pageX-M1.pX+M1.W,0),height:Math.max(a.pageY-M1.pY+M1.H,0)})}return false},stop:function(){b(document).unbind("mousemove",i.drag).unbind("mouseup",i.stop)}};var i=b.jqDnR,c=i.dnr,
d=i.e,f,l=function(a,e,n,m){return a.each(function(){e=e?b(e,a):a;e.bind("mousedown",{e:a,k:n},function(g){var j=g.data,h={};d=j.e;f=m?b(m):false;if(d.css("position")!="relative")try{d.position(h)}catch(o){}c={X:h.left||k("left")||0,Y:h.top||k("top")||0,W:k("width")||d[0].scrollWidth||0,H:k("height")||d[0].scrollHeight||0,pX:g.pageX,pY:g.pageY,k:j.k};M1=f&&j.k!="d"?{X:h.left||f1("left")||0,Y:h.top||f1("top")||0,W:f[0].offsetWidth||f1("width")||0,H:f[0].offsetHeight||f1("height")||0,pX:g.pageX,pY:g.pageY,
k:j.k}:false;try{b("input.hasDatepicker",d[0]).datepicker("hide")}catch(p){}b(document).mousemove(b.jqDnR.drag).mouseup(b.jqDnR.stop);return false})})},k=function(a){return parseInt(d.css(a))||false};f1=function(a){return parseInt(f.css(a))||false}})(jQuery);
(function(b){b.jgrid.extend({setSubGrid:function(){return this.each(function(){var e=this;e.p.colNames.unshift("");e.p.colModel.unshift({name:"subgrid",width:b.browser.safari?e.p.subGridWidth+e.p.cellLayout:e.p.subGridWidth,sortable:false,resizable:false,hidedlg:true,search:false,fixed:true});e=e.p.subGridModel;if(e[0]){e[0].align=b.extend([],e[0].align||[]);for(var c=0;c<e[0].name.length;c++)e[0].align[c]=e[0].align[c]||"left"}})},addSubGridCell:function(e,c){var a="",n,o;this.each(function(){a=
this.formatCol(e,c);n=this.p.gridview;o=this.p.id});return n===false?'<td role="grid" aria-describedby="'+o+'_subgrid" class="ui-sgcollapsed sgcollapsed" '+a+"><a href='javascript:void(0);'><span class='ui-icon ui-icon-plus'></span></a></td>":'<td role="grid" aria-describedby="'+o+'_subgrid" '+a+"></td>"},addSubGrid:function(e,c){return this.each(function(){var a=this;if(a.grid){var n=function(g,j,f){j=b("<td align='"+a.p.subGridModel[0].align[f]+"'></td>").html(j);b(g).append(j)},o=function(g,j){var f,
d,h,i=b("<table cellspacing='0' cellpadding='0' border='0'><tbody></tbody></table>"),k=b("<tr></tr>");for(d=0;d<a.p.subGridModel[0].name.length;d++){f=b("<th class='ui-state-default ui-th-subgrid ui-th-column ui-th-"+a.p.direction+"'></th>");b(f).html(a.p.subGridModel[0].name[d]);b(f).width(a.p.subGridModel[0].width[d]);b(k).append(f)}b(i).append(k);if(g){h=a.p.xmlReader.subgrid;b(h.root+" "+h.row,g).each(function(){k=b("<tr class='ui-widget-content ui-subtblcell'></tr>");if(h.repeatitems===true)b(h.cell,
this).each(function(q){n(k,b(this).text()||"&#160;",q)});else{var l=a.p.subGridModel[0].mapping||a.p.subGridModel[0].name;if(l)for(d=0;d<l.length;d++)n(k,b(l[d],this).text()||"&#160;",d)}b(i).append(k)})}g=b("table:first",a.grid.bDiv).attr("id")+"_";b("#"+g+j).append(i);a.grid.hDiv.loading=false;b("#load_"+a.p.id).hide();return false},u=function(g,j){var f,d,h,i,k=b("<table cellspacing='0' cellpadding='0' border='0'><tbody></tbody></table>"),l=b("<tr></tr>");for(d=0;d<a.p.subGridModel[0].name.length;d++){f=
b("<th class='ui-state-default ui-th-subgrid ui-th-column ui-th-"+a.p.direction+"'></th>");b(f).html(a.p.subGridModel[0].name[d]);b(f).width(a.p.subGridModel[0].width[d]);b(l).append(f)}b(k).append(l);if(g){f=a.p.jsonReader.subgrid;g=g[f.root];if(typeof g!=="undefined")for(d=0;d<g.length;d++){h=g[d];l=b("<tr class='ui-widget-content ui-subtblcell'></tr>");if(f.repeatitems===true){if(f.cell)h=h[f.cell];for(i=0;i<h.length;i++)n(l,h[i]||"&#160;",i)}else{var q=a.p.subGridModel[0].mapping||a.p.subGridModel[0].name;
if(q.length)for(i=0;i<q.length;i++)n(l,h[q[i]]||"&#160;",i)}b(k).append(l)}}d=b("table:first",a.grid.bDiv).attr("id")+"_";b("#"+d+j).append(k);a.grid.hDiv.loading=false;b("#load_"+a.p.id).hide();return false},x=function(g){var j,f,d,h;j=b(g).attr("id");f={nd_:(new Date).getTime()};f[a.p.prmNames.subgridid]=j;if(!a.p.subGridModel[0])return false;if(a.p.subGridModel[0].params)for(h=0;h<a.p.subGridModel[0].params.length;h++)for(d=0;d<a.p.colModel.length;d++)if(a.p.colModel[d].name==a.p.subGridModel[0].params[h])f[a.p.colModel[d].name]=
b("td:eq("+d+")",g).text().replace(/\&#160\;/ig,"");if(!a.grid.hDiv.loading){a.grid.hDiv.loading=true;b("#load_"+a.p.id).show();if(!a.p.subgridtype)a.p.subgridtype=a.p.datatype;if(b.isFunction(a.p.subgridtype))a.p.subgridtype.call(a,f);else a.p.subgridtype=a.p.subgridtype.toLowerCase();switch(a.p.subgridtype){case "xml":case "json":b.ajax(b.extend({type:a.p.mtype,url:a.p.subGridUrl,dataType:a.p.subgridtype,data:b.isFunction(a.p.serializeSubGridData)?a.p.serializeSubGridData(a,f):f,complete:function(i){a.p.subgridtype==
"xml"?o(i.responseXML,j):u(b.jgrid.parse(i.responseText),j)}},b.jgrid.ajaxOptions,a.p.ajaxSubgridOptions||{}));break}}return false},r,m,s,v,t,w,p;b("td:eq("+c+")",e).click(function(){if(b(this).hasClass("sgcollapsed")){s=a.p.id;r=b(this).parent();v=c>=1?"<td colspan='"+c+"'>&#160;</td>":"";m=b(r).attr("id");p=true;if(b.isFunction(a.p.subGridBeforeExpand))p=a.p.subGridBeforeExpand.call(a,s+"_"+m,m);if(p===false)return false;t=0;b.each(a.p.colModel,function(){if(this.hidden===true||this.name=="rn"||
this.name=="cb")t++});w="<tr role='row' class='ui-subgrid'>"+v+"<td class='ui-widget-content subgrid-cell'><span class='ui-icon ui-icon-carat-1-sw'/></td><td colspan='"+parseInt(a.p.colNames.length-1-t,10)+"' class='ui-widget-content subgrid-data'><div id="+s+"_"+m+" class='tablediv'>";b(this).parent().after(w+"</div></td></tr>");b.isFunction(a.p.subGridRowExpanded)?a.p.subGridRowExpanded.call(a,s+"_"+m,m):x(r);b(this).html("<a href='javascript:void(0);'><span class='ui-icon ui-icon-minus'></span></a>").removeClass("sgcollapsed").addClass("sgexpanded")}else if(b(this).hasClass("sgexpanded")){p=
true;if(b.isFunction(a.p.subGridRowColapsed)){r=b(this).parent();m=b(r).attr("id");p=a.p.subGridRowColapsed.call(a,s+"_"+m,m)}if(p===false)return false;b(this).parent().next().remove(".ui-subgrid");b(this).html("<a href='javascript:void(0);'><span class='ui-icon ui-icon-plus'></span></a>").removeClass("sgexpanded").addClass("sgcollapsed")}return false});a.subGridXml=function(g,j){o(g,j)};a.subGridJson=function(g,j){u(g,j)}}})},expandSubGridRow:function(e){return this.each(function(){var c=this;if(c.grid||
e)if(c.p.subGrid===true)if(c=b(this).jqGrid("getInd",e,true))(c=b("td.sgcollapsed",c)[0])&&b(c).trigger("click")})},collapseSubGridRow:function(e){return this.each(function(){var c=this;if(c.grid||e)if(c.p.subGrid===true)if(c=b(this).jqGrid("getInd",e,true))(c=b("td.sgexpanded",c)[0])&&b(c).trigger("click")})},toggleSubGridRow:function(e){return this.each(function(){var c=this;if(c.grid||e)if(c.p.subGrid===true)if(c=b(this).jqGrid("getInd",e,true)){var a=b("td.sgcollapsed",c)[0];if(a)b(a).trigger("click");
else(a=b("td.sgexpanded",c)[0])&&b(a).trigger("click")}})}})})(jQuery);
(function(f){f.jgrid.extend({groupingSetup:function(){return this.each(function(){var b=this,c=b.p.groupingView;if(c!==null&&(typeof c==="object"||f.isFunction(c)))if(c.groupField.length){for(var a=0;a<c.groupField.length;a++){c.groupOrder[a]||(c.groupOrder[a]="asc");c.groupText[a]||(c.groupText[a]="{0}");if(typeof c.groupColumnShow[a]!="boolean")c.groupColumnShow[a]=true;if(typeof c.groupSummary[a]!="boolean")c.groupSummary[a]=false;c.groupColumnShow[a]===true?f(b).jqGrid("showCol",c.groupField[a]):
f(b).jqGrid("hideCol",c.groupField[a]);c.sortitems[a]=[];c.sortnames[a]=[];c.summaryval[a]=[];if(c.groupSummary[a]){c.summary[a]=[];for(var d=b.p.colModel,e=0,g=d.length;e<g;e++)d[e].summaryType&&c.summary[a].push({nm:d[e].name,st:d[e].summaryType,v:""})}}b.p.scroll=false;b.p.rownumbers=false;b.p.subGrid=false;b.p.treeGrid=false;b.p.gridview=true}else b.p.grouping=false;else b.p.grouping=false})},groupingPrepare:function(b,c,a,d){this.each(function(){var e=c[0]?c[0].split(" ").join(""):"",g=this.p.groupingView,
j=this;if(a.hasOwnProperty(e))a[e].push(b);else{a[e]=[];a[e].push(b);g.sortitems[0].push(e);g.sortnames[0].push(f.trim(c[0]));g.summaryval[0][e]=f.extend(true,{},g.summary[0])}g.groupSummary[0]&&f.each(g.summaryval[0][e],function(){this.v=f.isFunction(this.st)?this.st.call(j,this.v,this.nm,d):f(j).jqGrid("groupingCalculations."+this.st,this.v,this.nm,d)})});return a},groupingToggle:function(b){this.each(function(){var c=this.p.groupingView,a=b.lastIndexOf("_"),d=b.substring(0,a+1);a=parseInt(b.substring(a+
1),10)+1;var e=c.minusicon,g=c.plusicon;if(f("#"+b+" span").hasClass(e)){c.showSummaryOnHide&&c.groupSummary[0]?f("#"+b).nextUntil(".jqfoot").hide():f("#"+b).nextUntil("#"+d+String(a)).hide();f("#"+b+" span").removeClass(e).addClass(g)}else{f("#"+b).nextUntil("#"+d+String(a)).show();f("#"+b+" span").removeClass(g).addClass(e)}});return false},groupingRender:function(b,c){return this.each(function(){var a=this,d=a.p.groupingView,e="",g="",j,l="";if(!d.groupDataSorted){d.sortitems[0].sort();d.sortnames[0].sort();
d.groupOrder[0].toLowerCase()=="desc"&&d.sortitems[0].reverse()}l=d.groupCollapse?d.plusicon:d.minusicon;l+=" tree-wrap-"+a.p.direction;f.each(d.sortitems[0],function(h,k){j=a.p.id+"ghead_"+h;g="<span style='cursor:pointer;' class='ui-icon "+l+"' onclick=\"jQuery('#"+a.p.id+"').jqGrid('groupingToggle','"+j+"');return false;\"></span>";e+='<tr id="'+j+'" role="row" class= "ui-widget-content jqgroup ui-row-'+a.p.direction+'"><td colspan="'+c+'">'+g+f.jgrid.format(d.groupText[0],d.sortnames[0][h],b[k].length)+
"</td></tr>";for(h=0;h<b[k].length;h++)e+=b[k][h].join("");if(d.groupSummary[0]){h="";if(d.groupCollapse&&!d.showSummaryOnHide)h=' style="display:none;"';e+="<tr"+h+' role="row" class="ui-widget-content jqfoot ui-row-'+a.p.direction+'">';h=d.summaryval[0][k];for(var m=a.p.colModel,n,o=b[k].length,i=0;i<c;i++){var p="<td "+a.formatCol(i,1,"")+">&#160;</td>",q="{0}";f.each(h,function(){if(this.nm==m[i].name){if(m[i].summaryTpl)q=m[i].summaryTpl;if(this.st=="avg")if(this.v&&o>0)this.v/=o;try{n=a.formatter("",
this.v,i,this)}catch(r){n=this.v}p="<td "+a.formatCol(i,1,"")+">"+f.jgrid.format(q,n)+"</td>";return false}});e+=p}e+="</tr>"}});f("#"+a.p.id+" tbody:first").append(e);e=null})},groupingGroupBy:function(b,c){return this.each(function(){var a=this;if(typeof b=="string")b=[b];var d=a.p.groupingView;a.p.grouping=true;for(var e=0;e<d.groupField.length;e++)d.groupColumnShow[e]||f(a).jqGrid("showCol",d.groupField[e]);a.p.groupingView=f.extend(a.p.groupingView,c||{});d.groupField=b;f(a).trigger("reloadGrid")})},
groupingRemove:function(b){return this.each(function(){var c=this;if(typeof b=="undefined")b=true;c.p.grouping=false;b===true?f("tr.jqgroup, tr.jqfoot","#"+c.p.id+" tbody:first").remove():f(c).trigger("reloadGrid")})},groupingCalculations:{sum:function(b,c,a){return parseFloat(b||0)+parseFloat(a[c]||0)},min:function(b,c,a){if(b==="")return parseFloat(a[c]||0);return Math.min(parseFloat(b),parseFloat(a[c]||0))},max:function(b,c,a){if(b==="")return parseFloat(a[c]||0);return Math.max(parseFloat(b),
parseFloat(a[c]||0))},count:function(b,c,a){if(b==="")b=0;return a.hasOwnProperty(c)?b+1:0},avg:function(b,c,a){return parseFloat(b||0)+parseFloat(a[c]||0)}}})})(jQuery);
(function(d){d.jgrid.extend({setTreeNode:function(b,c){return this.each(function(){var a=this;if(a.grid&&a.p.treeGrid){var f=a.p.expColInd,e=a.p.treeReader.expanded_field,k=a.p.treeReader.leaf_field,j=a.p.treeReader.level_field;c.level=b[j];if(a.p.treeGridModel=="nested"){var i=b[a.p.treeReader.left_field],h=b[a.p.treeReader.right_field];b[k]||(b[k]=parseInt(h,10)===parseInt(i,10)+1?"true":"false")}h=parseInt(b[j],10);if(a.p.tree_root_level===0){i=h+1;h=h}else{i=h;h=h-1}i="<div class='tree-wrap tree-wrap-"+
a.p.direction+"' style='width:"+i*18+"px;'>";i+="<div style='"+(a.p.direction=="rtl"?"right:":"left:")+h*18+"px;' class='ui-icon ";if(b[k]=="true"||b[k]===true){i+=a.p.treeIcons.leaf+" tree-leaf'";b[k]=true;b[e]=false}else{if(b[e]=="true"||b[e]===true){i+=a.p.treeIcons.minus+" tree-minus treeclick'";b[e]=true}else{i+=a.p.treeIcons.plus+" tree-plus treeclick'";b[e]=false}b[k]=false}i+="</div></div>";if(!a.p.loadonce){b[a.p.localReader.id]=c.id;a.p.data.push(b);a.p._index[c.id]=a.p.data.length-1}if(parseInt(b[j],
10)!==parseInt(a.p.tree_root_level,10))d(a).jqGrid("isVisibleNode",b)||d(c).css("display","none");d("td:eq("+f+")",c).wrapInner("<span></span>").prepend(i);d(".treeclick",c).bind("click",function(g){g=d(g.target||g.srcElement,a.rows).closest("tr.jqgrow")[0].id;g=a.p._index[g];var l=a.p.treeReader.expanded_field;if(!a.p.data[g][a.p.treeReader.leaf_field])if(a.p.data[g][l]){d(a).jqGrid("collapseRow",a.p.data[g]);d(a).jqGrid("collapseNode",a.p.data[g])}else{d(a).jqGrid("expandRow",a.p.data[g]);d(a).jqGrid("expandNode",
a.p.data[g])}return false});a.p.ExpandColClick===true&&d("span",c).css("cursor","pointer").bind("click",function(g){g=d(g.target||g.srcElement,a.rows).closest("tr.jqgrow")[0].id;var l=a.p._index[g],m=a.p.treeReader.expanded_field;if(!a.p.data[l][a.p.treeReader.leaf_field])if(a.p.data[l][m]){d(a).jqGrid("collapseRow",a.p.data[l]);d(a).jqGrid("collapseNode",a.p.data[l])}else{d(a).jqGrid("expandRow",a.p.data[l]);d(a).jqGrid("expandNode",a.p.data[l])}d(a).jqGrid("setSelection",g);return false})}})},setTreeGrid:function(){return this.each(function(){var b=
this,c=0;if(b.p.treeGrid){b.p.treedatatype||d.extend(b.p,{treedatatype:b.p.datatype});b.p.subGrid=false;b.p.altRows=false;b.p.pgbuttons=false;b.p.pginput=false;b.p.multiselect=false;b.p.rowList=[];b.p.treeIcons=d.extend({plus:"ui-icon-triangle-1-"+(b.p.direction=="rtl"?"w":"e"),minus:"ui-icon-triangle-1-s",leaf:"ui-icon-radio-off"},b.p.treeIcons||{});if(b.p.treeGridModel=="nested")b.p.treeReader=d.extend({level_field:"level",left_field:"lft",right_field:"rgt",leaf_field:"isLeaf",expanded_field:"expanded"},
b.p.treeReader);else if(b.p.treeGridModel=="adjacency")b.p.treeReader=d.extend({level_field:"level",parent_id_field:"parent",leaf_field:"isLeaf",expanded_field:"expanded"},b.p.treeReader);for(var a in b.p.colModel)if(b.p.colModel.hasOwnProperty(a)){if(b.p.colModel[a].name==b.p.ExpandColumn){b.p.expColInd=c;break}c++}if(!b.p.expColInd)b.p.expColInd=0;d.each(b.p.treeReader,function(f,e){if(e){b.p.colNames.push(e);b.p.colModel.push({name:e,width:1,hidden:true,sortable:false,resizable:false,hidedlg:true,
editable:true,search:false})}})}})},expandRow:function(b){this.each(function(){var c=this;if(c.grid&&c.p.treeGrid){var a=d(c).jqGrid("getNodeChildren",b),f=c.p.treeReader.expanded_field;d(a).each(function(){var e=d.jgrid.getAccessor(this,c.p.localReader.id);d("#"+e,c.grid.bDiv).css("display","");this[f]&&d(c).jqGrid("expandRow",this)})}})},collapseRow:function(b){this.each(function(){var c=this;if(c.grid&&c.p.treeGrid){var a=d(c).jqGrid("getNodeChildren",b),f=c.p.treeReader.expanded_field;d(a).each(function(){var e=
d.jgrid.getAccessor(this,c.p.localReader.id);d("#"+e,c.grid.bDiv).css("display","none");this[f]&&d(c).jqGrid("collapseRow",this)})}})},getRootNodes:function(){var b=[];this.each(function(){var c=this;if(c.grid&&c.p.treeGrid)switch(c.p.treeGridModel){case "nested":var a=c.p.treeReader.level_field;d(c.p.data).each(function(){parseInt(this[a],10)===parseInt(c.p.tree_root_level,10)&&b.push(this)});break;case "adjacency":var f=c.p.treeReader.parent_id_field;d(c.p.data).each(function(){if(this[f]===null||
String(this[f]).toLowerCase()=="null")b.push(this)});break}});return b},getNodeDepth:function(b){var c=null;this.each(function(){if(this.grid&&this.p.treeGrid){var a=this;switch(a.p.treeGridModel){case "nested":c=parseInt(b[a.p.treeReader.level_field],10)-parseInt(a.p.tree_root_level,10);break;case "adjacency":c=d(a).jqGrid("getNodeAncestors",b).length;break}}});return c},getNodeParent:function(b){var c=null;this.each(function(){var a=this;if(a.grid&&a.p.treeGrid)switch(a.p.treeGridModel){case "nested":var f=
a.p.treeReader.left_field,e=a.p.treeReader.right_field,k=a.p.treeReader.level_field,j=parseInt(b[f],10),i=parseInt(b[e],10),h=parseInt(b[k],10);d(this.p.data).each(function(){if(parseInt(this[k],10)===h-1&&parseInt(this[f],10)<j&&parseInt(this[e],10)>i){c=this;return false}});break;case "adjacency":var g=a.p.treeReader.parent_id_field,l=a.p.localReader.id;d(this.p.data).each(function(){if(this[l]==b[g]){c=this;return false}});break}});return c},getNodeChildren:function(b){var c=[];this.each(function(){var a=
this;if(a.grid&&a.p.treeGrid)switch(a.p.treeGridModel){case "nested":var f=a.p.treeReader.left_field,e=a.p.treeReader.right_field,k=a.p.treeReader.level_field,j=parseInt(b[f],10),i=parseInt(b[e],10),h=parseInt(b[k],10);d(this.p.data).each(function(){parseInt(this[k],10)===h+1&&parseInt(this[f],10)>j&&parseInt(this[e],10)<i&&c.push(this)});break;case "adjacency":var g=a.p.treeReader.parent_id_field,l=a.p.localReader.id;d(this.p.data).each(function(){this[g]==b[l]&&c.push(this)});break}});return c},
getFullTreeNode:function(b){var c=[];this.each(function(){var a=this,f;if(a.grid&&a.p.treeGrid)switch(a.p.treeGridModel){case "nested":var e=a.p.treeReader.left_field,k=a.p.treeReader.right_field,j=a.p.treeReader.level_field,i=parseInt(b[e],10),h=parseInt(b[k],10),g=parseInt(b[j],10);d(this.p.data).each(function(){parseInt(this[j],10)>=g&&parseInt(this[e],10)>=i&&parseInt(this[e],10)<=h&&c.push(this)});break;case "adjacency":c.push(b);var l=a.p.treeReader.parent_id_field,m=a.p.localReader.id;d(this.p.data).each(function(n){f=
c.length;for(n=0;n<f;n++)if(c[n][m]==this[l]){c.push(this);break}});break}});return c},getNodeAncestors:function(b){var c=[];this.each(function(){if(this.grid&&this.p.treeGrid)for(var a=d(this).jqGrid("getNodeParent",b);a;){c.push(a);a=d(this).jqGrid("getNodeParent",a)}});return c},isVisibleNode:function(b){var c=true;this.each(function(){var a=this;if(a.grid&&a.p.treeGrid){var f=d(a).jqGrid("getNodeAncestors",b),e=a.p.treeReader.expanded_field;d(f).each(function(){c=c&&this[e];if(!c)return false})}});
return c},isNodeLoaded:function(b){var c;this.each(function(){var a=this;if(a.grid&&a.p.treeGrid){var f=a.p.treeReader.leaf_field;c=b.loaded!==undefined?b.loaded:b[f]||d(a).jqGrid("getNodeChildren",b).length>0?true:false}});return c},expandNode:function(b){return this.each(function(){if(this.grid&&this.p.treeGrid){var c=this.p.treeReader.expanded_field;if(!b[c]){var a=d.jgrid.getAccessor(b,this.p.localReader.id),f=d("#"+a,this.grid.bDiv)[0],e=this.p._index[a];if(d(this).jqGrid("isNodeLoaded",this.p.data[e])){b[c]=
true;d("div.treeclick",f).removeClass(this.p.treeIcons.plus+" tree-plus").addClass(this.p.treeIcons.minus+" tree-minus")}else{b[c]=true;d("div.treeclick",f).removeClass(this.p.treeIcons.plus+" tree-plus").addClass(this.p.treeIcons.minus+" tree-minus");this.p.treeANode=f.rowIndex;this.p.datatype=this.p.treedatatype;this.p.treeGridModel=="nested"?d(this).jqGrid("setGridParam",{postData:{nodeid:a,n_left:b.lft,n_right:b.rgt,n_level:b.level}}):d(this).jqGrid("setGridParam",{postData:{nodeid:a,parentid:b.parent_id,
n_level:b.level}});d(this).trigger("reloadGrid");this.p.treeGridModel=="nested"?d(this).jqGrid("setGridParam",{postData:{nodeid:"",n_left:"",n_right:"",n_level:""}}):d(this).jqGrid("setGridParam",{postData:{nodeid:"",parentid:"",n_level:""}})}}}})},collapseNode:function(b){return this.each(function(){if(this.grid&&this.p.treeGrid)if(b.expanded){b.expanded=false;var c=d.jgrid.getAccessor(b,this.p.localReader.id);c=d("#"+c,this.grid.bDiv)[0];d("div.treeclick",c).removeClass(this.p.treeIcons.minus+" tree-minus").addClass(this.p.treeIcons.plus+
" tree-plus")}})},SortTree:function(b,c,a,f){return this.each(function(){if(this.grid&&this.p.treeGrid){var e,k,j,i=[],h=this,g;e=d(this).jqGrid("getRootNodes");e=d.jgrid.from(e);e.orderBy(b,c,a,f);g=e.select();e=0;for(k=g.length;e<k;e++){j=g[e];i.push(j);d(this).jqGrid("collectChildrenSortTree",i,j,b,c,a,f)}d.each(i,function(l){var m=d.jgrid.getAccessor(this,h.p.localReader.id);if(l===0){l=d("#"+m,h.grid.bDiv);d("td",l).each(function(n){d(this).css("width",h.grid.headers[n].width+"px")});h.grid.cols=
l[0].cells}d("tbody",h.grid.bDiv).append(d("#"+m,h.grid.bDiv))});i=g=e=null}})},collectChildrenSortTree:function(b,c,a,f,e,k){return this.each(function(){if(this.grid&&this.p.treeGrid){var j,i,h,g;j=d(this).jqGrid("getNodeChildren",c);j=d.jgrid.from(j);j.orderBy(a,f,e,k);g=j.select();j=0;for(i=g.length;j<i;j++){h=g[j];b.push(h);d(this).jqGrid("collectChildrenSortTree",b,h,a,f,e,k)}}})},setTreeRow:function(b,c){var a=false;this.each(function(){var f=this;if(f.grid&&f.p.treeGrid)a=d(f).jqGrid("setRowData",
b,c)});return a},delTreeNode:function(b){return this.each(function(){var c=this;if(c.grid&&c.p.treeGrid){var a=d(c).jqGrid("getInd",b,true);if(a){var f=d(c).jqGrid("getNodeChildren",a);if(f.length>0)for(var e=0;e<f.length;e++)d(c).jqGrid("delRowData",f[e].id);d(c).jqGrid("delRowData",a.id)}}})}})})(jQuery);
(function(b){b.jgrid.extend({jqGridImport:function(a){a=b.extend({imptype:"xml",impstring:"",impurl:"",mtype:"GET",impData:{},xmlGrid:{config:"roots>grid",data:"roots>rows"},jsonGrid:{config:"grid",data:"data"},ajaxOptions:{}},a||{});return this.each(function(){var d=this,c=function(e,g){var f=b(g.xmlGrid.config,e)[0];g=b(g.xmlGrid.data,e)[0];var k;if(xmlJsonClass.xml2json&&b.jgrid.parse){f=xmlJsonClass.xml2json(f," ");f=b.jgrid.parse(f);for(var h in f)if(f.hasOwnProperty(h))k=f[h];if(g){h=f.grid.datatype;
f.grid.datatype="xmlstring";f.grid.datastr=e;b(d).jqGrid(k).jqGrid("setGridParam",{datatype:h})}else b(d).jqGrid(k)}else alert("xml2json or parse are not present")},i=function(e,g){if(e&&typeof e=="string"){var f=b.jgrid.parse(e);e=f[g.jsonGrid.config];if(g=f[g.jsonGrid.data]){f=e.datatype;e.datatype="jsonstring";e.datastr=g;b(d).jqGrid(e).jqGrid("setGridParam",{datatype:f})}else b(d).jqGrid(e)}};switch(a.imptype){case "xml":b.ajax(b.extend({url:a.impurl,type:a.mtype,data:a.impData,dataType:"xml",
complete:function(e,g){if(g=="success"){c(e.responseXML,a);b.isFunction(a.importComplete)&&a.importComplete(e)}}},a.ajaxOptions));break;case "xmlstring":if(a.impstring&&typeof a.impstring=="string"){var j=b.jgrid.stringToDoc(a.impstring);if(j){c(j,a);b.isFunction(a.importComplete)&&a.importComplete(j);a.impstring=null}j=null}break;case "json":b.ajax(b.extend({url:a.impurl,type:a.mtype,data:a.impData,dataType:"json",complete:function(e,g){if(g=="success"){i(e.responseText,a);b.isFunction(a.importComplete)&&
a.importComplete(e)}}},a.ajaxOptions));break;case "jsonstring":if(a.impstring&&typeof a.impstring=="string"){i(a.impstring,a);b.isFunction(a.importComplete)&&a.importComplete(a.impstring);a.impstring=null}break}})},jqGridExport:function(a){a=b.extend({exptype:"xmlstring",root:"grid",ident:"\t"},a||{});var d=null;this.each(function(){if(this.grid){var c=b.extend({},b(this).jqGrid("getGridParam"));if(c.rownumbers){c.colNames.splice(0,1);c.colModel.splice(0,1)}if(c.multiselect){c.colNames.splice(0,1);
c.colModel.splice(0,1)}if(c.subGrid){c.colNames.splice(0,1);c.colModel.splice(0,1)}c.knv=null;if(c.treeGrid)for(var i in c.treeReader)if(c.treeReader.hasOwnProperty(i)){c.colNames.splice(c.colNames.length-1);c.colModel.splice(c.colModel.length-1)}switch(a.exptype){case "xmlstring":d="<"+a.root+">"+xmlJsonClass.json2xml(c,a.ident)+"</"+a.root+">";break;case "jsonstring":d="{"+xmlJsonClass.toJson(c,a.root,a.ident)+"}";if(c.postData.filters!==undefined){d=d.replace(/filters":"/,'filters":');d=d.replace(/}]}"/,
"}]}")}break}}});return d},excelExport:function(a){a=b.extend({exptype:"remote",url:null,oper:"oper",tag:"excel",exportOptions:{}},a||{});return this.each(function(){if(this.grid){var d;if(a.exptype=="remote"){d=b.extend({},this.p.postData);d[a.oper]=a.tag;d=jQuery.param(d);d=a.url.indexOf("?")!=-1?a.url+"&"+d:a.url+"?"+d;window.location=d}}})}})})(jQuery);
var xmlJsonClass={xml2json:function(a,b){if(a.nodeType===9)a=a.documentElement;a=this.toJson(this.toObj(this.removeWhite(a)),a.nodeName,"\t");return"{\n"+b+(b?a.replace(/\t/g,b):a.replace(/\t|\n/g,""))+"\n}"},json2xml:function(a,b){var g=function(d,c,j){var i="",k,h;if(d instanceof Array)if(d.length===0)i+=j+"<"+c+">__EMPTY_ARRAY_</"+c+">\n";else{k=0;for(h=d.length;k<h;k+=1){var l=j+g(d[k],c,j+"\t")+"\n";i+=l}}else if(typeof d==="object"){k=false;i+=j+"<"+c;for(h in d)if(d.hasOwnProperty(h))if(h.charAt(0)===
"@")i+=" "+h.substr(1)+'="'+d[h].toString()+'"';else k=true;i+=k?">":"/>";if(k){for(h in d)if(d.hasOwnProperty(h))if(h==="#text")i+=d[h];else if(h==="#cdata")i+="<![CDATA["+d[h]+"]]\>";else if(h.charAt(0)!=="@")i+=g(d[h],h,j+"\t");i+=(i.charAt(i.length-1)==="\n"?j:"")+"</"+c+">"}}else i+=typeof d==="function"?j+"<"+c+"><![CDATA["+d+"]]\></"+c+">":d.toString()==='""'||d.toString().length===0?j+"<"+c+">__EMPTY_STRING_</"+c+">":j+"<"+c+">"+d.toString()+"</"+c+">";return i},e="",f;for(f in a)if(a.hasOwnProperty(f))e+=
g(a[f],f,"");return b?e.replace(/\t/g,b):e.replace(/\t|\n/g,"")},toObj:function(a){var b={},g=/function/i;if(a.nodeType===1){if(a.attributes.length){var e;for(e=0;e<a.attributes.length;e+=1)b["@"+a.attributes[e].nodeName]=(a.attributes[e].nodeValue||"").toString()}if(a.firstChild){var f=e=0,d=false,c;for(c=a.firstChild;c;c=c.nextSibling)if(c.nodeType===1)d=true;else if(c.nodeType===3&&c.nodeValue.match(/[^ \f\n\r\t\v]/))e+=1;else if(c.nodeType===4)f+=1;if(d)if(e<2&&f<2){this.removeWhite(a);for(c=
a.firstChild;c;c=c.nextSibling)if(c.nodeType===3)b["#text"]=this.escape(c.nodeValue);else if(c.nodeType===4)if(g.test(c.nodeValue))b[c.nodeName]=[b[c.nodeName],c.nodeValue];else b["#cdata"]=this.escape(c.nodeValue);else if(b[c.nodeName])if(b[c.nodeName]instanceof Array)b[c.nodeName][b[c.nodeName].length]=this.toObj(c);else b[c.nodeName]=[b[c.nodeName],this.toObj(c)];else b[c.nodeName]=this.toObj(c)}else if(a.attributes.length)b["#text"]=this.escape(this.innerXml(a));else b=this.escape(this.innerXml(a));
else if(e)if(a.attributes.length)b["#text"]=this.escape(this.innerXml(a));else{b=this.escape(this.innerXml(a));if(b==="__EMPTY_ARRAY_")b="[]";else if(b==="__EMPTY_STRING_")b=""}else if(f)if(f>1)b=this.escape(this.innerXml(a));else for(c=a.firstChild;c;c=c.nextSibling)if(g.test(a.firstChild.nodeValue)){b=a.firstChild.nodeValue;break}else b["#cdata"]=this.escape(c.nodeValue)}if(!a.attributes.length&&!a.firstChild)b=null}else if(a.nodeType===9)b=this.toObj(a.documentElement);else alert("unhandled node type: "+
a.nodeType);return b},toJson:function(a,b,g){var e=b?'"'+b+'"':"";if(a==="[]")e+=b?":[]":"[]";else if(a instanceof Array){var f,d,c=[];d=0;for(f=a.length;d<f;d+=1)c[d]=this.toJson(a[d],"",g+"\t");e+=(b?":[":"[")+(c.length>1?"\n"+g+"\t"+c.join(",\n"+g+"\t")+"\n"+g:c.join(""))+"]"}else if(a===null)e+=(b&&":")+"null";else if(typeof a==="object"){f=[];for(d in a)if(a.hasOwnProperty(d))f[f.length]=this.toJson(a[d],d,g+"\t");e+=(b?":{":"{")+(f.length>1?"\n"+g+"\t"+f.join(",\n"+g+"\t")+"\n"+g:f.join(""))+
"}"}else if(typeof a==="string"){g=/function/i;f=a.toString();e+=/(^-?\d+\.?\d*$)/.test(f)||g.test(f)||f==="false"||f==="true"?(b&&":")+f:(b&&":")+'"'+a+'"'}else e+=(b&&":")+a.toString();return e},innerXml:function(a){var b="";if("innerHTML"in a)b=a.innerHTML;else{var g=function(e){var f="",d;if(e.nodeType===1){f+="<"+e.nodeName;for(d=0;d<e.attributes.length;d+=1)f+=" "+e.attributes[d].nodeName+'="'+(e.attributes[d].nodeValue||"").toString()+'"';if(e.firstChild){f+=">";for(d=e.firstChild;d;d=d.nextSibling)f+=
g(d);f+="</"+e.nodeName+">"}else f+="/>"}else if(e.nodeType===3)f+=e.nodeValue;else if(e.nodeType===4)f+="<![CDATA["+e.nodeValue+"]]\>";return f};for(a=a.firstChild;a;a=a.nextSibling)b+=g(a)}return b},escape:function(a){return a.replace(/[\\]/g,"\\\\").replace(/[\"]/g,'\\"').replace(/[\n]/g,"\\n").replace(/[\r]/g,"\\r")},removeWhite:function(a){a.normalize();var b;for(b=a.firstChild;b;)if(b.nodeType===3)if(b.nodeValue.match(/[^ \f\n\r\t\v]/))b=b.nextSibling;else{var g=b.nextSibling;a.removeChild(b);
b=g}else{b.nodeType===1&&this.removeWhite(b);b=b.nextSibling}return a}};
(function(b){b.jgrid.extend({setColumns:function(a){a=b.extend({top:0,left:0,width:200,height:"auto",dataheight:"auto",modal:false,drag:true,beforeShowForm:null,afterShowForm:null,afterSubmitForm:null,closeOnEscape:true,ShrinkToFit:false,jqModal:false,saveicon:[true,"left","ui-icon-disk"],closeicon:[true,"left","ui-icon-close"],onClose:null,colnameview:true,closeAfterSubmit:true,updateAfterCheck:false,recreateForm:false},b.jgrid.col,a||{});return this.each(function(){var c=this;if(c.grid){var j=typeof a.beforeShowForm===
"function"?true:false,k=typeof a.afterShowForm==="function"?true:false,l=typeof a.afterSubmitForm==="function"?true:false,e=c.p.id,d="ColTbl_"+e,f={themodal:"colmod"+e,modalhead:"colhd"+e,modalcontent:"colcnt"+e,scrollelm:d};a.recreateForm===true&&b("#"+f.themodal).html()!=null&&b("#"+f.themodal).remove();if(b("#"+f.themodal).html()!=null){j&&a.beforeShowForm(b("#"+d));viewModal("#"+f.themodal,{gbox:"#gbox_"+e,jqm:a.jqModal,jqM:false,modal:a.modal})}else{var g=isNaN(a.dataheight)?a.dataheight:a.dataheight+
"px";g="<div id='"+d+"' class='formdata' style='width:100%;overflow:auto;position:relative;height:"+g+";'>";g+="<table class='ColTable' cellspacing='1' cellpading='2' border='0'><tbody>";for(i=0;i<this.p.colNames.length;i++)c.p.colModel[i].hidedlg||(g+="<tr><td style='white-space: pre;'><input type='checkbox' style='margin-right:5px;' id='col_"+this.p.colModel[i].name+"' class='cbox' value='T' "+(this.p.colModel[i].hidden===false?"checked":"")+"/><label for='col_"+this.p.colModel[i].name+"'>"+this.p.colNames[i]+
(a.colnameview?" ("+this.p.colModel[i].name+")":"")+"</label></td></tr>");g+="</tbody></table></div>";g+="<table border='0' class='EditTable' id='"+d+"_2'><tbody><tr style='display:block;height:3px;'><td></td></tr><tr><td class='DataTD ui-widget-content'></td></tr><tr><td class='ColButton EditButton'>"+(!a.updateAfterCheck?"<a href='javascript:void(0)' id='dData' class='fm-button ui-state-default ui-corner-all'>"+a.bSubmit+"</a>":"")+"&#160;"+("<a href='javascript:void(0)' id='eData' class='fm-button ui-state-default ui-corner-all'>"+
a.bCancel+"</a>")+"</td></tr></tbody></table>";a.gbox="#gbox_"+e;createModal(f,g,a,"#gview_"+c.p.id,b("#gview_"+c.p.id)[0]);if(a.saveicon[0]==true)b("#dData","#"+d+"_2").addClass(a.saveicon[1]=="right"?"fm-button-icon-right":"fm-button-icon-left").append("<span class='ui-icon "+a.saveicon[2]+"'></span>");if(a.closeicon[0]==true)b("#eData","#"+d+"_2").addClass(a.closeicon[1]=="right"?"fm-button-icon-right":"fm-button-icon-left").append("<span class='ui-icon "+a.closeicon[2]+"'></span>");a.updateAfterCheck?
b(":input","#"+d).click(function(){var h=this.id.substr(4);if(h){this.checked?b(c).jqGrid("showCol",h):b(c).jqGrid("hideCol",h);a.ShrinkToFit===true&&b(c).jqGrid("setGridWidth",c.grid.width-0.0010,true)}return this}):b("#dData","#"+d+"_2").click(function(){for(i=0;i<c.p.colModel.length;i++)if(!c.p.colModel[i].hidedlg){var h=c.p.colModel[i].name.replace(/\./g,"\\.");if(b("#col_"+h,"#"+d).attr("checked")){b(c).jqGrid("showCol",c.p.colModel[i].name);b("#col_"+h,"#"+d).attr("defaultChecked",true)}else{b(c).jqGrid("hideCol",
c.p.colModel[i].name);b("#col_"+h,"#"+d).attr("defaultChecked","")}}a.ShrinkToFit===true&&b(c).jqGrid("setGridWidth",c.grid.width-0.0010,true);a.closeAfterSubmit&&hideModal("#"+f.themodal,{gb:"#gbox_"+e,jqm:a.jqModal,onClose:a.onClose});l&&a.afterSubmitForm(b("#"+d));return false});b("#eData","#"+d+"_2").click(function(){hideModal("#"+f.themodal,{gb:"#gbox_"+e,jqm:a.jqModal,onClose:a.onClose});return false});b("#dData, #eData","#"+d+"_2").hover(function(){b(this).addClass("ui-state-hover")},function(){b(this).removeClass("ui-state-hover")});
j&&a.beforeShowForm(b("#"+d));viewModal("#"+f.themodal,{gbox:"#gbox_"+e,jqm:a.jqModal,jqM:true,modal:a.modal})}k&&a.afterShowForm(b("#"+d))}})}})})(jQuery);
(function(c){c.jgrid.extend({getPostData:function(){var a=this[0];if(a.grid)return a.p.postData},setPostData:function(a){var b=this[0];if(b.grid)if(typeof a==="object")b.p.postData=a;else alert("Error: cannot add a non-object postData value. postData unchanged.")},appendPostData:function(a){var b=this[0];if(b.grid)typeof a==="object"?c.extend(b.p.postData,a):alert("Error: cannot append a non-object postData value. postData unchanged.")},setPostDataItem:function(a,b){var d=this[0];if(d.grid)d.p.postData[a]=
b},getPostDataItem:function(a){var b=this[0];if(b.grid)return b.p.postData[a]},removePostDataItem:function(a){var b=this[0];b.grid&&delete b.p.postData[a]},getUserData:function(){var a=this[0];if(a.grid)return a.p.userData},getUserDataItem:function(a){var b=this[0];if(b.grid)return b.p.userData[a]}})})(jQuery);
function tableToGrid(n,o){jQuery(n).each(function(){if(!this.grid){jQuery(this).width("99%");var a=jQuery(this).width(),d=jQuery("input[type=checkbox]:first",jQuery(this)),b=jQuery("input[type=radio]:first",jQuery(this));d=d.length>0;b=!d&&b.length>0;var l=d||b,c=[],g=[];jQuery("th",jQuery(this)).each(function(){if(c.length===0&&l){c.push({name:"__selection__",index:"__selection__",width:0,hidden:true});g.push("__selection__")}else{c.push({name:jQuery(this).attr("id")||jQuery.trim(jQuery.jgrid.stripHtml(jQuery(this).html())).split(" ").join("_"),
index:jQuery(this).attr("id")||jQuery.trim(jQuery.jgrid.stripHtml(jQuery(this).html())).split(" ").join("_"),width:jQuery(this).width()||150});g.push(jQuery(this).html())}});var f=[],h=[],i=[];jQuery("tbody > tr",jQuery(this)).each(function(){var j={},e=0;jQuery("td",jQuery(this)).each(function(){if(e===0&&l){var k=jQuery("input",jQuery(this)),m=k.attr("value");h.push(m||f.length);k.attr("checked")&&i.push(m);j[c[e].name]=k.attr("value")}else j[c[e].name]=jQuery(this).html();e++});e>0&&f.push(j)});
jQuery(this).empty();jQuery(this).addClass("scroll");jQuery(this).jqGrid(jQuery.extend({datatype:"local",width:a,colNames:g,colModel:c,multiselect:d},o||{}));for(a=0;a<f.length;a++){b=null;if(h.length>0)if((b=h[a])&&b.replace)b=encodeURIComponent(b).replace(/[.\-%]/g,"_");if(b===null)b=a+1;jQuery(this).jqGrid("addRowData",b,f[a])}for(a=0;a<i.length;a++)jQuery(this).jqGrid("setSelection",i[a])}})};
(function(a){if(a.browser.msie&&a.browser.version==8)a.expr[":"].hidden=function(b){return b.offsetWidth===0||b.offsetHeight===0||b.style.display=="none"};a.jgrid._multiselect=false;if(a.ui)if(a.ui.multiselect){if(a.ui.multiselect.prototype._setSelected){var q=a.ui.multiselect.prototype._setSelected;a.ui.multiselect.prototype._setSelected=function(b,j){b=q.call(this,b,j);if(j&&this.selectedList){var c=this.element;this.selectedList.find("li").each(function(){a(this).data("optionLink")&&a(this).data("optionLink").remove().appendTo(c)})}return b}}if(a.ui.multiselect.prototype.destroy)a.ui.multiselect.prototype.destroy=
function(){this.element.show();this.container.remove();a.Widget===undefined?a.widget.prototype.destroy.apply(this,arguments):a.Widget.prototype.destroy.apply(this,arguments)};a.jgrid._multiselect=true}a.jgrid.extend({sortableColumns:function(b){return this.each(function(){function j(){c.p.disableClick=true}var c=this,g={tolerance:"pointer",axis:"x",scrollSensitivity:"1",items:">th:not(:has(#jqgh_cb,#jqgh_rn,#jqgh_subgrid),:hidden)",placeholder:{element:function(e){return a(document.createElement(e[0].nodeName)).addClass(e[0].className+
" ui-sortable-placeholder ui-state-highlight").removeClass("ui-sortable-helper")[0]},update:function(e,h){h.height(e.currentItem.innerHeight()-parseInt(e.currentItem.css("paddingTop")||0,10)-parseInt(e.currentItem.css("paddingBottom")||0,10));h.width(e.currentItem.innerWidth()-parseInt(e.currentItem.css("paddingLeft")||0,10)-parseInt(e.currentItem.css("paddingRight")||0,10))}},update:function(e,h){e=a(h.item).parent();e=a(">th",e);var i={};a.each(c.p.colModel,function(m){i[this.name]=m});var l=[];
e.each(function(){var m=a(">div",this).get(0).id.replace(/^jqgh_/,"");m in i&&l.push(i[m])});a(c).jqGrid("remapColumns",l,true,true);a.isFunction(c.p.sortable.update)&&c.p.sortable.update(l);setTimeout(function(){c.p.disableClick=false},50)}};if(c.p.sortable.options)a.extend(g,c.p.sortable.options);else if(a.isFunction(c.p.sortable))c.p.sortable={update:c.p.sortable};if(g.start){var d=g.start;g.start=function(e,h){j();d.call(this,e,h)}}else g.start=j;if(c.p.sortable.exclude)g.items+=":not("+c.p.sortable.exclude+
")";b.sortable(g).data("sortable").floating=true})},columnChooser:function(b){function j(f,k,p){if(k>=0){var o=f.slice(),r=o.splice(k,Math.max(f.length-k,k));if(k>f.length)k=f.length;o[k]=p;return o.concat(r)}}function c(f,k){if(f)if(typeof f=="string")a.fn[f]&&a.fn[f].apply(k,a.makeArray(arguments).slice(2));else a.isFunction(f)&&f.apply(k,a.makeArray(arguments).slice(2))}var g=this;if(!a("#colchooser_"+g[0].p.id).length){var d=a('<div id="colchooser_'+g[0].p.id+'" style="position:relative;overflow:hidden"><div><select multiple="multiple"></select></div></div>'),
e=a("select",d);b=a.extend({width:420,height:240,classname:null,done:function(f){f&&g.jqGrid("remapColumns",f,true)},msel:"multiselect",dlog:"dialog",dlog_opts:function(f){var k={};k[f.bSubmit]=function(){f.apply_perm();f.cleanup(false)};k[f.bCancel]=function(){f.cleanup(true)};return{buttons:k,close:function(){f.cleanup(true)},modal:false,resizable:false,width:f.width+20}},apply_perm:function(){a("option",e).each(function(){this.selected?g.jqGrid("showCol",h[this.value].name):g.jqGrid("hideCol",
h[this.value].name)});var f=[];a("option[selected]",e).each(function(){f.push(parseInt(this.value,10))});a.each(f,function(){delete l[h[parseInt(this,10)].name]});a.each(l,function(){var k=parseInt(this,10);f=j(f,k,k)});b.done&&b.done.call(g,f)},cleanup:function(f){c(b.dlog,d,"destroy");c(b.msel,e,"destroy");d.remove();f&&b.done&&b.done.call(g)},msel_opts:{}},a.jgrid.col,b||{});if(a.ui)if(a.ui.multiselect)if(b.msel=="multiselect"){if(!a.jgrid._multiselect){alert("Multiselect plugin loaded after jqGrid. Please load the plugin before the jqGrid!");
return}b.msel_opts=a.extend(a.ui.multiselect.defaults,b.msel_opts)}b.caption&&d.attr("title",b.caption);if(b.classname){d.addClass(b.classname);e.addClass(b.classname)}if(b.width){a(">div",d).css({width:b.width,margin:"0 auto"});e.css("width",b.width)}if(b.height){a(">div",d).css("height",b.height);e.css("height",b.height-10)}var h=g.jqGrid("getGridParam","colModel"),i=g.jqGrid("getGridParam","colNames"),l={},m=[];e.empty();a.each(h,function(f){l[this.name]=f;if(this.hidedlg)this.hidden||m.push(f);
else e.append("<option value='"+f+"' "+(this.hidden?"":"selected='selected'")+">"+i[f]+"</option>")});var n=a.isFunction(b.dlog_opts)?b.dlog_opts.call(g,b):b.dlog_opts;c(b.dlog,d,n);n=a.isFunction(b.msel_opts)?b.msel_opts.call(g,b):b.msel_opts;c(b.msel,e,n)}},sortableRows:function(b){return this.each(function(){var j=this;if(j.grid)if(!j.p.treeGrid)if(a.fn.sortable){b=a.extend({cursor:"move",axis:"y",items:".jqgrow"},b||{});if(b.start&&a.isFunction(b.start)){b._start_=b.start;delete b.start}else b._start_=
false;if(b.update&&a.isFunction(b.update)){b._update_=b.update;delete b.update}else b._update_=false;b.start=function(c,g){a(g.item).css("border-width","0px");a("td",g.item).each(function(h){this.style.width=j.grid.cols[h].style.width});if(j.p.subGrid){var d=a(g.item).attr("id");try{a(j).jqGrid("collapseSubGridRow",d)}catch(e){}}b._start_&&b._start_.apply(this,[c,g])};b.update=function(c,g){a(g.item).css("border-width","");j.p.rownumbers===true&&a("td.jqgrid-rownum",j.rows).each(function(d){a(this).html(d+
1)});b._update_&&b._update_.apply(this,[c,g])};a("tbody:first",j).sortable(b);a("tbody:first",j).disableSelection()}})},gridDnD:function(b){return this.each(function(){function j(){var d=a.data(c,"dnd");a("tr.jqgrow:not(.ui-draggable)",c).draggable(a.isFunction(d.drag)?d.drag.call(a(c),d):d.drag)}var c=this;if(c.grid)if(!c.p.treeGrid)if(a.fn.draggable&&a.fn.droppable){a("#jqgrid_dnd").html()===null&&a("body").append("<table id='jqgrid_dnd' class='ui-jqgrid-dnd'></table>");if(typeof b=="string"&&b==
"updateDnD"&&c.p.jqgdnd===true)j();else{b=a.extend({drag:function(d){return a.extend({start:function(e,h){if(c.p.subGrid){var i=a(h.helper).attr("id");try{a(c).jqGrid("collapseSubGridRow",i)}catch(l){}}for(i=0;i<a.data(c,"dnd").connectWith.length;i++)a(a.data(c,"dnd").connectWith[i]).jqGrid("getGridParam","reccount")=="0"&&a(a.data(c,"dnd").connectWith[i]).jqGrid("addRowData","jqg_empty_row",{});h.helper.addClass("ui-state-highlight");a("td",h.helper).each(function(m){this.style.width=c.grid.headers[m].width+
"px"});d.onstart&&a.isFunction(d.onstart)&&d.onstart.call(a(c),e,h)},stop:function(e,h){if(h.helper.dropped){var i=a(h.helper).attr("id");a(c).jqGrid("delRowData",i)}for(i=0;i<a.data(c,"dnd").connectWith.length;i++)a(a.data(c,"dnd").connectWith[i]).jqGrid("delRowData","jqg_empty_row");d.onstop&&a.isFunction(d.onstop)&&d.onstop.call(a(c),e,h)}},d.drag_opts||{})},drop:function(d){return a.extend({accept:function(e){var h=a(e).closest("table.ui-jqgrid-btable");if(a.data(h[0],"dnd")!==undefined){e=a.data(h[0],
"dnd").connectWith;return a.inArray("#"+this.id,e)!=-1?true:false}return e},drop:function(e,h){var i=a(h.draggable).attr("id");i=a("#"+c.id).jqGrid("getRowData",i);if(!d.dropbyname){var l=0,m={},n,f=a("#"+this.id).jqGrid("getGridParam","colModel");try{for(var k in i){if(i.hasOwnProperty(k)&&f[l]){n=f[l].name;m[n]=i[k]}l++}i=m}catch(p){}}h.helper.dropped=true;if(d.beforedrop&&a.isFunction(d.beforedrop)){n=d.beforedrop.call(this,e,h,i,a("#"+c.id),a(this));if(typeof n!="undefined"&&n!==null&&typeof n==
"object")i=n}if(h.helper.dropped){var o;if(d.autoid)if(a.isFunction(d.autoid))o=d.autoid.call(this,i);else{o=Math.ceil(Math.random()*1E3);o=d.autoidprefix+o}a("#"+this.id).jqGrid("addRowData",o,i,d.droppos)}d.ondrop&&a.isFunction(d.ondrop)&&d.ondrop.call(this,e,h,i)}},d.drop_opts||{})},onstart:null,onstop:null,beforedrop:null,ondrop:null,drop_opts:{activeClass:"ui-state-active",hoverClass:"ui-state-hover"},drag_opts:{revert:"invalid",helper:"clone",cursor:"move",appendTo:"#jqgrid_dnd",zIndex:5E3},
dropbyname:false,droppos:"first",autoid:true,autoidprefix:"dnd_"},b||{});if(b.connectWith){b.connectWith=b.connectWith.split(",");b.connectWith=a.map(b.connectWith,function(d){return a.trim(d)});a.data(c,"dnd",b);c.p.reccount!="0"&&!c.p.jqgdnd&&j();c.p.jqgdnd=true;for(var g=0;g<b.connectWith.length;g++)a(b.connectWith[g]).droppable(a.isFunction(b.drop)?b.drop.call(a(c),b):b.drop)}}}})},gridResize:function(b){return this.each(function(){var j=this;if(j.grid&&a.fn.resizable){b=a.extend({},b||{});if(b.alsoResize){b._alsoResize_=
b.alsoResize;delete b.alsoResize}else b._alsoResize_=false;if(b.stop&&a.isFunction(b.stop)){b._stop_=b.stop;delete b.stop}else b._stop_=false;b.stop=function(c,g){a(j).jqGrid("setGridParam",{height:a("#gview_"+j.p.id+" .ui-jqgrid-bdiv").height()});a(j).jqGrid("setGridWidth",g.size.width,b.shrinkToFit);b._stop_&&b._stop_.call(j,c,g)};b.alsoResize=b._alsoResize_?eval("("+("{'#gview_"+j.p.id+" .ui-jqgrid-bdiv':true,'"+b._alsoResize_+"':true}")+")"):a(".ui-jqgrid-bdiv","#gview_"+j.p.id);delete b._alsoResize_;
a("#gbox_"+j.p.id).resizable(b)}})}})})(jQuery);


/*
 * jsTree 1.0-rc1
 * http://jstree.com/
 *
 * Copyright (c) 2010 Ivan Bozhanov (vakata.com)
 *
 * Dual licensed under the MIT and GPL licenses (same as jQuery):
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *
 * $Date: 2010-07-01 10:51:11 +0300 (четв, 01 юли 2010) $
 * $Revision: 191 $
 */

/*jslint browser: true, onevar: true, undef: true, bitwise: true, strict: true */
/*global window : false, clearInterval: false, clearTimeout: false, document: false, setInterval: false, setTimeout: false, jQuery: false, navigator: false, XSLTProcessor: false, DOMParser: false, XMLSerializer: false*/

"use strict";
// Common functions not related to jsTree 
// decided to move them to a `vakata` "namespace"
(function ($) {
	$.vakata = {};
	// CSS related functions
	$.vakata.css = {
		get_css : function(rule_name, delete_flag, sheet) {
			rule_name = rule_name.toLowerCase();
			var css_rules = sheet.cssRules || sheet.rules,
				j = 0;
			do {
				if(css_rules.length && j > css_rules.length + 5) { return false; }
				if(css_rules[j].selectorText && css_rules[j].selectorText.toLowerCase() == rule_name) {
					if(delete_flag === true) {
						if(sheet.removeRule) { sheet.removeRule(j); }
						if(sheet.deleteRule) { sheet.deleteRule(j); }
						return true;
					}
					else { return css_rules[j]; }
				}
			}
			while (css_rules[++j]);
			return false;
		},
		add_css : function(rule_name, sheet) {
			if($.jstree.css.get_css(rule_name, false, sheet)) { return false; }
			if(sheet.insertRule) { sheet.insertRule(rule_name + ' { }', 0); } else { sheet.addRule(rule_name, null, 0); }
			return $.vakata.css.get_css(rule_name);
		},
		remove_css : function(rule_name, sheet) { 
			return $.vakata.css.get_css(rule_name, true, sheet); 
		},
		add_sheet : function(opts) {
			var tmp;
			if(opts.str) {
				tmp = document.createElement("style");
				tmp.setAttribute('type',"text/css");
				if(tmp.styleSheet) {
					document.getElementsByTagName("head")[0].appendChild(tmp);
					tmp.styleSheet.cssText = opts.str;
				}
				else {
					tmp.appendChild(document.createTextNode(opts.str));
					document.getElementsByTagName("head")[0].appendChild(tmp);
				}
				return tmp.sheet || tmp.styleSheet;
			}
			if(opts.url) {
				if(document.createStyleSheet) {
					try { tmp = document.createStyleSheet(opts.url); } catch (e) { }
				}
				else {
					tmp			= document.createElement('link');
					tmp.rel		= 'stylesheet';
					tmp.type	= 'text/css';
					tmp.media	= "all";
					tmp.href	= opts.url;
					document.getElementsByTagName("head")[0].appendChild(tmp);
					return tmp.styleSheet;
				}
			}
		}
	};
})(jQuery);

/* 
 * jsTree core 1.0
 */
(function ($) {
	// private variables 
	var instances = [],			// instance array (used by $.jstree.reference/create/focused)
		focused_instance = -1,	// the index in the instance array of the currently focused instance
		plugins = {},			// list of included plugins
		prepared_move = {},		// for the move plugin
		is_ie6 = false;

	// jQuery plugin wrapper (thanks to jquery UI widget function)
	$.fn.jstree = function (settings) {
		var isMethodCall = (typeof settings == 'string'), // is this a method call like $().jstree("open_node")
			args = Array.prototype.slice.call(arguments, 1), 
			returnValue = this;

		// extend settings and allow for multiple hashes and metadata
		if(!isMethodCall && $.meta) { args.push($.metadata.get(this).jstree); }
		settings = !isMethodCall && args.length ? $.extend.apply(null, [true, settings].concat(args)) : settings;
		// block calls to "private" methods
		if(isMethodCall && settings.substring(0, 1) == '_') { return returnValue; }

		// if a method call execute the method on all selected instances
		if(isMethodCall) {
			this.each(function() {
				var instance = instances[$.data(this, "jstree-instance-id")],
					methodValue = (instance && $.isFunction(instance[settings])) ? instance[settings].apply(instance, args) : instance;
					if(typeof methodValue !== "undefined" && (settings.indexOf("is_" === 0) || (methodValue !== true && methodValue !== false))) { returnValue = methodValue; return false; }
			});
		}
		else {
			this.each(function() {
				var instance_id = $.data(this, "jstree-instance-id"),
					s = false;
				// if an instance already exists, destroy it first
				if(typeof instance_id !== "undefined" && instances[instance_id]) { instances[instance_id].destroy(); }
				// push a new empty object to the instances array
				instance_id = parseInt(instances.push({}),10) - 1;
				// store the jstree instance id to the container element
				$.data(this, "jstree-instance-id", instance_id);
				// clean up all plugins
				if(!settings) { settings = {}; }
				settings.plugins = $.isArray(settings.plugins) ? settings.plugins : $.jstree.defaults.plugins;
				if($.inArray("core", settings.plugins) === -1) { settings.plugins.unshift("core"); }
				
				// only unique plugins (NOT WORKING)
				// settings.plugins = settings.plugins.sort().join(",,").replace(/(,|^)([^,]+)(,,\2)+(,|$)/g,"$1$2$4").replace(/,,+/g,",").replace(/,$/,"").split(",");

				// extend defaults with passed data
				s = $.extend(true, {}, $.jstree.defaults, settings);
				s.plugins = settings.plugins;
				$.each(plugins, function (i, val) { if($.inArray(i, s.plugins) === -1) { s[i] = null; delete s[i]; } });
				// push the new object to the instances array (at the same time set the default classes to the container) and init
				instances[instance_id] = new $.jstree._instance(instance_id, $(this).addClass("jstree jstree-" + instance_id), s); 
				// init all activated plugins for this instance
				$.each(instances[instance_id]._get_settings().plugins, function (i, val) { instances[instance_id].data[val] = {}; });
				$.each(instances[instance_id]._get_settings().plugins, function (i, val) { if(plugins[val]) { plugins[val].__init.apply(instances[instance_id]); } });
				// initialize the instance
				instances[instance_id].init();
			});
		}
		// return the jquery selection (or if it was a method call that returned a value - the returned value)
		return returnValue;
	};
	// object to store exposed functions and objects
	$.jstree = {
		defaults : {
			plugins : []
		},
		_focused : function () { return instances[focused_instance] || null; },
		_reference : function (needle) { 
			// get by instance id
			if(instances[needle]) { return instances[needle]; }
			// get by DOM (if still no luck - return null
			var o = $(needle); 
			if(!o.length && typeof needle === "string") { o = $("#" + needle); }
			if(!o.length) { return null; }
			return instances[o.closest(".jstree").data("jstree-instance-id")] || null; 
		},
		_instance : function (index, container, settings) { 
			// for plugins to store data in
			this.data = { core : {} };
			this.get_settings	= function () { return $.extend(true, {}, settings); };
			this._get_settings	= function () { return settings; };
			this.get_index		= function () { return index; };
			this.get_container	= function () { return container; };
			this._set_settings	= function (s) { 
				settings = $.extend(true, {}, settings, s);
			};
		},
		_fn : { },
		plugin : function (pname, pdata) {
			pdata = $.extend({}, {
				__init		: $.noop, 
				__destroy	: $.noop,
				_fn			: {},
				defaults	: false
			}, pdata);
			plugins[pname] = pdata;

			$.jstree.defaults[pname] = pdata.defaults;
			$.each(pdata._fn, function (i, val) {
				val.plugin		= pname;
				val.old			= $.jstree._fn[i];
				$.jstree._fn[i] = function () {
					var rslt,
						func = val,
						args = Array.prototype.slice.call(arguments),
						evnt = new $.Event("before.jstree"),
						rlbk = false;

					// Check if function belongs to the included plugins of this instance
					do {
						if(func && func.plugin && $.inArray(func.plugin, this._get_settings().plugins) !== -1) { break; }
						func = func.old;
					} while(func);
					if(!func) { return; }

					// a chance to stop execution (or change arguments): 
					// * just bind to jstree.before
					// * check the additional data object (func property)
					// * call event.stopImmediatePropagation()
					// * return false (or an array of arguments)
					rslt = this.get_container().triggerHandler(evnt, { "func" : i, "inst" : this, "args" : args });
					if(rslt === false) { return; }
					if(typeof rslt !== "undefined") { args = rslt; }

					// context and function to trigger events, then finally call the function
					if(i.indexOf("_") === 0) {
						rslt = func.apply(this, args);
					}
					else {
						rslt = func.apply(
							$.extend({}, this, { 
								__callback : function (data) { 
									this.get_container().triggerHandler( i + '.jstree', { "inst" : this, "args" : args, "rslt" : data, "rlbk" : rlbk });
								},
								__rollback : function () { 
									rlbk = this.get_rollback();
									return rlbk;
								},
								__call_old : function (replace_arguments) {
									return func.old.apply(this, (replace_arguments ? Array.prototype.slice.call(arguments, 1) : args ) );
								}
							}), args);
					}

					// return the result
					return rslt;
				};
				$.jstree._fn[i].old = val.old;
				$.jstree._fn[i].plugin = pname;
			});
		},
		rollback : function (rb) {
			if(rb) {
				if(!$.isArray(rb)) { rb = [ rb ]; }
				$.each(rb, function (i, val) {
					instances[val.i].set_rollback(val.h, val.d);
				});
			}
		}
	};
	// set the prototype for all instances
	$.jstree._fn = $.jstree._instance.prototype = {};

	// css functions - used internally

	// load the css when DOM is ready
	$(function() {
		// code is copied form jQuery ($.browser is deprecated + there is a bug in IE)
		var u = navigator.userAgent.toLowerCase(),
			v = (u.match( /.+?(?:rv|it|ra|ie)[\/: ]([\d.]+)/ ) || [0,'0'])[1],
			css_string = '' + 
				'.jstree ul, .jstree li { display:block; margin:0 0 0 0; padding:0 0 0 0; list-style-type:none; } ' + 
				'.jstree li { display:block; min-height:18px; line-height:18px; white-space:nowrap; margin-left:18px; } ' + 
				'.jstree-rtl li { margin-left:0; margin-right:18px; } ' + 
				'.jstree > ul > li { margin-left:0px; } ' + 
				'.jstree-rtl > ul > li { margin-right:0px; } ' + 
				'.jstree ins { display:inline-block; text-decoration:none; width:18px; height:18px; margin:0 0 0 0; padding:0; } ' + 
				'.jstree a { display:inline-block; line-height:16px; height:16px; color:black; white-space:nowrap; text-decoration:none; padding:1px 2px; margin:0; } ' + 
				'.jstree a:focus { outline: none; } ' + 
				'.jstree a > ins { height:16px; width:16px; } ' + 
				'.jstree a > .jstree-icon { margin-right:3px; } ' + 
				'.jstree-rtl a > .jstree-icon { margin-left:3px; margin-right:0; } ' + 
				'li.jstree-open > ul { display:block; } ' + 
				'li.jstree-closed > ul { display:none; } ';
		// Correct IE 6 (does not support the > CSS selector)
		if(/msie/.test(u) && parseInt(v, 10) == 6) { 
			is_ie6 = true;
			css_string += '' + 
				'.jstree li { height:18px; margin-left:0; margin-right:0; } ' + 
				'.jstree li li { margin-left:18px; } ' + 
				'.jstree-rtl li li { margin-left:0px; margin-right:18px; } ' + 
				'li.jstree-open ul { display:block; } ' + 
				'li.jstree-closed ul { display:none !important; } ' + 
				'.jstree li a { display:inline; border-width:0 !important; padding:0px 2px !important; } ' + 
				'.jstree li a ins { height:16px; width:16px; margin-right:3px; } ' + 
				'.jstree-rtl li a ins { margin-right:0px; margin-left:3px; } ';
		}
		// Correct IE 7 (shifts anchor nodes onhover)
		if(/msie/.test(u) && parseInt(v, 10) == 7) { 
			css_string += '.jstree li a { border-width:0 !important; padding:0px 2px !important; } ';
		}
		$.vakata.css.add_sheet({ str : css_string });
	});

	// core functions (open, close, create, update, delete)
	$.jstree.plugin("core", {
		__init : function () {
			this.data.core.to_open = $.map($.makeArray(this.get_settings().core.initially_open), function (n) { return "#" + n.toString().replace(/^#/,"").replace('\\/','/').replace('/','\\/'); });
		},
		defaults : { 
			html_titles	: false,
			animation	: 500,
			initially_open : [],
			rtl			: false,
			strings		: {
				loading		: "Loading ...",
				new_node	: "New node"
			}
		},
		_fn : { 
			init	: function () { 
				this.set_focus(); 
				if(this._get_settings().core.rtl) {
					this.get_container().addClass("jstree-rtl").css("direction", "rtl");
				}
				this.get_container().html("<ul><li class='jstree-last jstree-leaf'><ins>&#160;</ins><a class='jstree-loading' href='#'><ins class='jstree-icon'>&#160;</ins>" + this._get_settings().core.strings.loading + "</a></li></ul>");
				this.data.core.li_height = this.get_container().find("ul li.jstree-closed, ul li.jstree-leaf").eq(0).height() || 18;

				this.get_container()
					.delegate("li > ins", "click.jstree", $.proxy(function (event) {
							var trgt = $(event.target);
							if(trgt.is("ins") && event.pageY - trgt.offset().top < this.data.core.li_height) { this.toggle_node(trgt); }
						}, this))
					.bind("mousedown.jstree", $.proxy(function () { 
							this.set_focus(); // This used to be setTimeout(set_focus,0) - why?
						}, this))
					.bind("dblclick.jstree", function (event) { 
						var sel;
						if(document.selection && document.selection.empty) { document.selection.empty(); }
						else {
							if(window.getSelection) {
								sel = window.getSelection();
								try { 
									sel.removeAllRanges();
									sel.collapse();
								} catch (err) { }
							}
						}
					});
				this.__callback();
				this.load_node(-1, function () { this.loaded(); this.reopen(); });
			},
			destroy	: function () { 
				var i,
					n = this.get_index(),
					s = this._get_settings(),
					_this = this;

				$.each(s.plugins, function (i, val) {
					try { plugins[val].__destroy.apply(_this); } catch(err) { }
				});
				this.__callback();
				// set focus to another instance if this one is focused
				if(this.is_focused()) { 
					for(i in instances) { 
						if(instances.hasOwnProperty(i) && i != n) { 
							instances[i].set_focus(); 
							break; 
						} 
					}
				}
				// if no other instance found
				if(n === focused_instance) { focused_instance = -1; }
				// remove all traces of jstree in the DOM (only the ones set using jstree*) and cleans all events
				this.get_container()
					.unbind(".jstree")
					.undelegate(".jstree")
					.removeData("jstree-instance-id")
					.find("[class^='jstree']")
						.andSelf()
						.attr("class", function () { return this.className.replace(/jstree[^ ]*|$/ig,''); });
				// remove the actual data
				instances[n] = null;
				delete instances[n];
			},
			save_opened : function () {
				var _this = this;
				this.data.core.to_open = [];
				this.get_container().find(".jstree-open").each(function () { 
					_this.data.core.to_open.push("#" + this.id.toString().replace(/^#/,"").replace('\\/','/').replace('/','\\/')); 
				});
				this.__callback(_this.data.core.to_open);
			},
			reopen : function (is_callback) {
				var _this = this,
					done = true,
					current = [],
					remaining = [];
				if(!is_callback) { this.data.core.reopen = false; this.data.core.refreshing = true; }
				if(this.data.core.to_open.length) {
					$.each(this.data.core.to_open, function (i, val) {
						if(val == "#") { return true; }
						if($(val).length && $(val).is(".jstree-closed")) { current.push(val); }
						else { remaining.push(val); }
					});
					if(current.length) {
						this.data.core.to_open = remaining;
						$.each(current, function (i, val) { 
							_this.open_node(val, function () { _this.reopen(true); }, true); 
						});
						done = false;
					}
				}
				if(done) { 
					// TODO: find a more elegant approach to syncronizing returning requests
					if(this.data.core.reopen) { clearTimeout(this.data.core.reopen); }
					this.data.core.reopen = setTimeout(function () { _this.__callback({}, _this); }, 50);
					this.data.core.refreshing = false;
				}
			},
			refresh : function (obj) {
				var _this = this;
				this.save_opened();
				if(!obj) { obj = -1; }
				obj = this._get_node(obj);
				if(!obj) { obj = -1; }
				if(obj !== -1) { obj.children("UL").remove(); }
				this.load_node(obj, function () { _this.__callback({ "obj" : obj}); _this.reopen(); });
			},
			// Dummy function to fire after the first load (so that there is a jstree.loaded event)
			loaded	: function () { 
				this.__callback(); 
			},
			// deal with focus
			set_focus	: function () { 
				var f = $.jstree._focused();
				if(f && f !== this) {
					f.get_container().removeClass("jstree-focused"); 
				}
				if(f !== this) {
					this.get_container().addClass("jstree-focused"); 
					focused_instance = this.get_index(); 
				}
				this.__callback();
			},
			is_focused	: function () { 
				return focused_instance == this.get_index(); 
			},

			// traverse
			_get_node		: function (obj) { 
				var $obj = $(obj, this.get_container()); 
				if($obj.is(".jstree") || obj == -1) { return -1; } 
				$obj = $obj.closest("li", this.get_container()); 
				return $obj.length ? $obj : false; 
			},
			_get_next		: function (obj, strict) {
				obj = this._get_node(obj);
				if(obj === -1) { return this.get_container().find("> ul > li:first-child"); }
				if(!obj.length) { return false; }
				if(strict) { return (obj.nextAll("li").size() > 0) ? obj.nextAll("li:eq(0)") : false; }

				if(obj.hasClass("jstree-open")) { return obj.find("li:eq(0)"); }
				else if(obj.nextAll("li").size() > 0) { return obj.nextAll("li:eq(0)"); }
				else { return obj.parentsUntil(".jstree","li").next("li").eq(0); }
			},
			_get_prev		: function (obj, strict) {
				obj = this._get_node(obj);
				if(obj === -1) { return this.get_container().find("> ul > li:last-child"); }
				if(!obj.length) { return false; }
				if(strict) { return (obj.prevAll("li").length > 0) ? obj.prevAll("li:eq(0)") : false; }

				if(obj.prev("li").length) {
					obj = obj.prev("li").eq(0);
					while(obj.hasClass("jstree-open")) { obj = obj.children("ul:eq(0)").children("li:last"); }
					return obj;
				}
				else { var o = obj.parentsUntil(".jstree","li:eq(0)"); return o.length ? o : false; }
			},
			_get_parent		: function (obj) {
				obj = this._get_node(obj);
				if(obj == -1 || !obj.length) { return false; }
				var o = obj.parentsUntil(".jstree", "li:eq(0)");
				return o.length ? o : -1;
			},
			_get_children	: function (obj) {
				obj = this._get_node(obj);
				if(obj === -1) { return this.get_container().children("ul:eq(0)").children("li"); }
				if(!obj.length) { return false; }
				return obj.children("ul:eq(0)").children("li");
			},
			get_path		: function (obj, id_mode) {
				var p = [],
					_this = this;
				obj = this._get_node(obj);
				if(obj === -1 || !obj || !obj.length) { return false; }
				obj.parentsUntil(".jstree", "li").each(function () {
					p.push( id_mode ? this.id : _this.get_text(this) );
				});
				p.reverse();
				p.push( id_mode ? obj.attr("id") : this.get_text(obj) );
				return p;
			},

			is_open		: function (obj) { obj = this._get_node(obj); return obj && obj !== -1 && obj.hasClass("jstree-open"); },
			is_closed	: function (obj) { obj = this._get_node(obj); return obj && obj !== -1 && obj.hasClass("jstree-closed"); },
			is_leaf		: function (obj) { obj = this._get_node(obj); return obj && obj !== -1 && obj.hasClass("jstree-leaf"); },
			// open/close
			open_node	: function (obj, callback, skip_animation) {
				obj = this._get_node(obj);
				if(!obj.length) { return false; }
				if(!obj.hasClass("jstree-closed")) { if(callback) { callback.call(); } return false; }
				var s = skip_animation || is_ie6 ? 0 : this._get_settings().core.animation,
					t = this;
				if(!this._is_loaded(obj)) {
					obj.children("a").addClass("jstree-loading");
					this.load_node(obj, function () { t.open_node(obj, callback, skip_animation); }, callback);
				}
				else {
					if(s) { obj.children("ul").css("display","none"); }
					obj.removeClass("jstree-closed").addClass("jstree-open").children("a").removeClass("jstree-loading");
					if(s) { obj.children("ul").stop(true).slideDown(s, function () { this.style.display = ""; }); }
					this.__callback({ "obj" : obj });
					if(callback) { callback.call(); }
				}
			},
			close_node	: function (obj, skip_animation) {
				obj = this._get_node(obj);
				var s = skip_animation || is_ie6 ? 0 : this._get_settings().core.animation;
				if(!obj.length || !obj.hasClass("jstree-open")) { return false; }
				if(s) { obj.children("ul").attr("style","display:block !important"); }
				obj.removeClass("jstree-open").addClass("jstree-closed");
				if(s) { obj.children("ul").stop(true).slideUp(s, function () { this.style.display = ""; }); }
				this.__callback({ "obj" : obj });
			},
			toggle_node	: function (obj) {
				obj = this._get_node(obj);
				if(obj.hasClass("jstree-closed")) { return this.open_node(obj); }
				if(obj.hasClass("jstree-open")) { return this.close_node(obj); }
			},
			open_all	: function (obj, original_obj) {
				obj = obj ? this._get_node(obj) : this.get_container();
				if(!obj || obj === -1) { obj = this.get_container(); }
				if(original_obj) { 
					obj = obj.find("li.jstree-closed");
				}
				else {
					original_obj = obj;
					if(obj.is(".jstree-closed")) { obj = obj.find("li.jstree-closed").andSelf(); }
					else { obj = obj.find("li.jstree-closed"); }
				}
				var _this = this;
				obj.each(function () { 
					var __this = this; 
					if(!_this._is_loaded(this)) { _this.open_node(this, function() { _this.open_all(__this, original_obj); }, true); }
					else { _this.open_node(this, false, true); }
				});
				// so that callback is fired AFTER all nodes are open
				if(original_obj.find('li.jstree-closed').length === 0) { this.__callback({ "obj" : original_obj }); }
			},
			close_all	: function (obj, skipAnimation) {
				var _this = this;
				obj = obj ? this._get_node(obj) : this.get_container();
				if(!obj || obj === -1) { obj = this.get_container(); }
				obj.find("li.jstree-open").andSelf().each(function () { _this.close_node(this, skipAnimation ? skipAnimation : false); });
				this.__callback({ "obj" : obj });
			},
			clean_node	: function (obj) {
				obj = obj && obj != -1 ? $(obj) : this.get_container();
				obj = obj.is("li") ? obj.find("li").andSelf() : obj.find("li");
				obj.removeClass("jstree-last")
					.filter("li:last-child").addClass("jstree-last").end()
					.filter(":has(li)")
						.not(".jstree-open").removeClass("jstree-leaf").addClass("jstree-closed");
				obj.not(".jstree-open, .jstree-closed").addClass("jstree-leaf").children("ul").remove();
				this.__callback({ "obj" : obj });
			},
			// rollback
			get_rollback : function () { 
				this.__callback();
				return { i : this.get_index(), h : this.get_container().children("ul").clone(true), d : this.data }; 
			},
			set_rollback : function (html, data) {
				this.get_container().empty().append(html);
				this.data = data;
				this.__callback();
			},
			// Dummy functions to be overwritten by any datastore plugin included
			load_node	: function (obj, s_call, e_call) { this.__callback({ "obj" : obj }); },
			_is_loaded	: function (obj) { return true; },

			// Basic operations: create
			create_node	: function (obj, position, js, callback, is_loaded) {
				obj = this._get_node(obj);
				position = typeof position === "undefined" ? "last" : position;
				var d = $("<li>"),
					s = this._get_settings().core,
					tmp;

				if(obj !== -1 && !obj.length) { return false; }
				if(!is_loaded && !this._is_loaded(obj)) { this.load_node(obj, function () { this.create_node(obj, position, js, callback, true); }); return false; }

				this.__rollback();

				if(typeof js === "string") { js = { "data" : js }; }
				if(!js) { js = {}; }
				if(js.attr) { d.attr(js.attr); }
				if(js.state) { d.addClass("jstree-" + js.state); }
				if(!js.data) { js.data = s.strings.new_node; }
				if(!$.isArray(js.data)) { tmp = js.data; js.data = []; js.data.push(tmp); }
				$.each(js.data, function (i, m) {
					tmp = $("<a>");
					if($.isFunction(m)) { m = m.call(this, js); }
					if(typeof m == "string") { tmp.attr('href','#')[ s.html_titles ? "html" : "text" ](m); }
					else {
						if(!m.attr) { m.attr = {}; }
						if(!m.attr.href) { m.attr.href = '#'; }
						tmp.attr(m.attr)[ s.html_titles ? "html" : "text" ](m.title);
						if(m.language) { tmp.addClass(m.language); }
					}
					tmp.prepend("<ins class='jstree-icon'>&#160;</ins>");
					if(m.icon) { 
						if(m.icon.indexOf("/") === -1) { tmp.children("ins").addClass(m.icon); }
						else { tmp.children("ins").css("background","url('" + m.icon + "') center center no-repeat"); }
					}
					d.append(tmp);
				});
				d.prepend("<ins class='jstree-icon'>&#160;</ins>");
				if(obj === -1) {
					obj = this.get_container();
					if(position === "before") { position = "first"; }
					if(position === "after") { position = "last"; }
				}
				switch(position) {
					case "before": obj.before(d); tmp = this._get_parent(obj); break;
					case "after" : obj.after(d);  tmp = this._get_parent(obj); break;
					case "inside":
					case "first" :
						if(!obj.children("ul").length) { obj.append("<ul>"); }
						obj.children("ul").prepend(d);
						tmp = obj;
						break;
					case "last":
						if(!obj.children("ul").length) { obj.append("<ul>"); }
						obj.children("ul").append(d);
						tmp = obj;
						break;
					default:
						if(!obj.children("ul").length) { obj.append("<ul>"); }
						if(!position) { position = 0; }
						tmp = obj.children("ul").children("li").eq(position);
						if(tmp.length) { tmp.before(d); }
						else { obj.children("ul").append(d); }
						tmp = obj;
						break;
				}
				if(tmp === -1 || tmp.get(0) === this.get_container().get(0)) { tmp = -1; }
				this.clean_node(tmp);
				this.__callback({ "obj" : d, "parent" : tmp });
				if(callback) { callback.call(this, d); }
				return d;
			},
			// Basic operations: rename (deal with text)
			get_text	: function (obj) {
				obj = this._get_node(obj);
				if(!obj.length) { return false; }
				var s = this._get_settings().core.html_titles;
				obj = obj.children("a:eq(0)");
				if(s) {
					obj = obj.clone();
					obj.children("INS").remove();
					return obj.html();
				}
				else {
					obj = obj.contents().filter(function() { return this.nodeType == 3; })[0];
					return obj.nodeValue;
				}
			},
			set_text	: function (obj, val) {
				obj = this._get_node(obj);
				if(!obj.length) { return false; }
				obj = obj.children("a:eq(0)");
				if(this._get_settings().core.html_titles) {
					var tmp = obj.children("INS").clone();
					obj.html(val).prepend(tmp);
					this.__callback({ "obj" : obj, "name" : val });
					return true;
				}
				else {
					obj = obj.contents().filter(function() { return this.nodeType == 3; })[0];
					this.__callback({ "obj" : obj, "name" : val });
					return (obj.nodeValue = val);
				}
			},
			rename_node : function (obj, val) {
				obj = this._get_node(obj);
				this.__rollback();
				if(obj && obj.length && this.set_text.apply(this, Array.prototype.slice.call(arguments))) { this.__callback({ "obj" : obj, "name" : val }); }
			},
			// Basic operations: deleting nodes
			delete_node : function (obj) {
				obj = this._get_node(obj);
				if(!obj.length) { return false; }
				this.__rollback();
				var p = this._get_parent(obj), prev = this._get_prev(obj);
				obj = obj.remove();
				if(p !== -1 && p.find("> ul > li").length === 0) {
					p.removeClass("jstree-open jstree-closed").addClass("jstree-leaf");
				}
				this.clean_node(p);
				this.__callback({ "obj" : obj, "prev" : prev });
				return obj;
			},
			prepare_move : function (o, r, pos, cb, is_cb) {
				var p = {};

				p.ot = $.jstree._reference(p.o) || this;
				p.o = p.ot._get_node(o);
				p.r = r === - 1 ? -1 : this._get_node(r);
				p.p = (typeof p === "undefined") ? "last" : pos; // TODO: move to a setting
				if(!is_cb && prepared_move.o && prepared_move.o[0] === p.o[0] && prepared_move.r[0] === p.r[0] && prepared_move.p === p.p) {
					this.__callback(prepared_move);
					if(cb) { cb.call(this, prepared_move); }
					return;
				}
				p.ot = $.jstree._reference(p.o) || this;
				p.rt = r === -1 ? p.ot : $.jstree._reference(p.r) || this;
				if(p.r === -1) {
					p.cr = -1;
					switch(p.p) {
						case "first":
						case "before":
						case "inside":
							p.cp = 0; 
							break;
						case "after":
						case "last":
							p.cp = p.rt.get_container().find(" > ul > li").length; 
							break;
						default:
							p.cp = p.p;
							break;
					}
				}
				else {
					if(!/^(before|after)$/.test(p.p) && !this._is_loaded(p.r)) {
						return this.load_node(p.r, function () { this.prepare_move(o, r, pos, cb, true); });
					}
					switch(p.p) {
						case "before":
							p.cp = p.r.index();
							p.cr = p.rt._get_parent(p.r);
							break;
						case "after":
							p.cp = p.r.index() + 1;
							p.cr = p.rt._get_parent(p.r);
							break;
						case "inside":
						case "first":
							p.cp = 0;
							p.cr = p.r;
							break;
						case "last":
							p.cp = p.r.find(" > ul > li").length; 
							p.cr = p.r;
							break;
						default: 
							p.cp = p.p;
							p.cr = p.r;
							break;
					}
				}
				p.np = p.cr == -1 ? p.rt.get_container() : p.cr;
				p.op = p.ot._get_parent(p.o);
				p.or = p.np.find(" > ul > li:nth-child(" + (p.cp + 1) + ")");

				prepared_move = p;
				this.__callback(prepared_move);
				if(cb) { cb.call(this, prepared_move); }
			},
			check_move : function () {
				var obj = prepared_move, ret = true;
				if(obj.or[0] === obj.o[0]) { return false; }
				obj.o.each(function () { 
					if(obj.r.parentsUntil(".jstree").andSelf().filter("li").index(this) !== -1) { ret = false; return false; }
				});
				return ret;
			},
			move_node : function (obj, ref, position, is_copy, is_prepared, skip_check) {
				if(!is_prepared) { 
					return this.prepare_move(obj, ref, position, function (p) {
						this.move_node(p, false, false, is_copy, true, skip_check);
					});
				}
				if(!skip_check && !this.check_move()) { return false; }

				this.__rollback();
				var o = false;
				if(is_copy) {
					o = obj.o.clone();
					o.find("*[id]").andSelf().each(function () {
						if(this.id) { this.id = "copy_" + this.id; }
					});
				}
				else { o = obj.o; }

				if(obj.or.length) { obj.or.before(o); }
				else { 
					if(!obj.np.children("ul").length) { $("<ul>").appendTo(obj.np); }
					obj.np.children("ul:eq(0)").append(o); 
				}

				try { 
					obj.ot.clean_node(obj.op);
					obj.rt.clean_node(obj.np);
					if(!obj.op.find("> ul > li").length) {
						obj.op.removeClass("jstree-open jstree-closed").addClass("jstree-leaf").children("ul").remove();
					}
				} catch (e) { }

				if(is_copy) { 
					prepared_move.cy = true;
					prepared_move.oc = o; 
				}
				this.__callback(prepared_move);
				return prepared_move;
			},
			_get_move : function () { return prepared_move; }
		}
	});
})(jQuery);
//*/

/* 
 * jsTree ui plugin 1.0
 * This plugins handles selecting/deselecting/hovering/dehovering nodes
 */
(function ($) {
	$.jstree.plugin("ui", {
		__init : function () { 
			this.data.ui.selected = $(); 
			this.data.ui.last_selected = false; 
			this.data.ui.hovered = null;
			this.data.ui.to_select = this.get_settings().ui.initially_select;

			this.get_container()
				.delegate("a", "click.jstree", $.proxy(function (event) {
						event.preventDefault();
						this.select_node(event.currentTarget, true, event);
					}, this))
				.delegate("a", "mouseenter.jstree", $.proxy(function (event) {
						this.hover_node(event.target);
					}, this))
				.delegate("a", "mouseleave.jstree", $.proxy(function (event) {
						this.dehover_node(event.target);
					}, this))
				.bind("reopen.jstree", $.proxy(function () { 
						this.reselect();
					}, this))
				.bind("get_rollback.jstree", $.proxy(function () { 
						this.dehover_node();
						this.save_selected();
					}, this))
				.bind("set_rollback.jstree", $.proxy(function () { 
						this.reselect();
					}, this))
				.bind("close_node.jstree", $.proxy(function (event, data) { 
						var s = this._get_settings().ui,
							obj = this._get_node(data.rslt.obj),
							clk = (obj && obj.length) ? obj.children("ul").find(".jstree-clicked") : $(),
							_this = this;
						if(s.selected_parent_close === false || !clk.length) { return; }
						clk.each(function () { 
							_this.deselect_node(this);
							if(s.selected_parent_close === "select_parent") { _this.select_node(obj); }
						});
					}, this))
				.bind("delete_node.jstree", $.proxy(function (event, data) { 
						var s = this._get_settings().ui.select_prev_on_delete,
							obj = this._get_node(data.rslt.obj),
							clk = (obj && obj.length) ? obj.find(".jstree-clicked") : [],
							_this = this;
						clk.each(function () { _this.deselect_node(this); });
						if(s && clk.length) { this.select_node(data.rslt.prev); }
					}, this))
				.bind("move_node.jstree", $.proxy(function (event, data) { 
						if(data.rslt.cy) { 
							data.rslt.oc.find(".jstree-clicked").removeClass("jstree-clicked");
						}
					}, this));
		},
		defaults : {
			select_limit : -1, // 0, 1, 2 ... or -1 for unlimited
			select_multiple_modifier : "ctrl", // on, or ctrl, shift, alt
			selected_parent_close : "select_parent", // false, "deselect", "select_parent"
			select_prev_on_delete : true,
			disable_selecting_children : false,
			initially_select : []
		},
		_fn : { 
			_get_node : function (obj, allow_multiple) {
				if(typeof obj === "undefined" || obj === null) { return allow_multiple ? this.data.ui.selected : this.data.ui.last_selected; }
				var $obj = $(obj, this.get_container()); 
				if($obj.is(".jstree") || obj == -1) { return -1; } 
				$obj = $obj.closest("li", this.get_container()); 
				return $obj.length ? $obj : false; 
			},
			save_selected : function () {
				var _this = this;
				this.data.ui.to_select = [];
				this.data.ui.selected.each(function () { _this.data.ui.to_select.push("#" + this.id.toString().replace(/^#/,"").replace('\\/','/').replace('/','\\/')); });
				this.__callback(this.data.ui.to_select);
			},
			reselect : function () {
				var _this = this,
					s = this.data.ui.to_select;
				s = $.map($.makeArray(s), function (n) { return "#" + n.toString().replace(/^#/,"").replace('\\/','/').replace('/','\\/'); });
				this.deselect_all();
				$.each(s, function (i, val) { if(val && val !== "#") { _this.select_node(val); } });
				this.__callback();
			},
			refresh : function (obj) {
				this.save_selected();
				return this.__call_old();
			},
			hover_node : function (obj) {
				obj = this._get_node(obj);
				if(!obj.length) { return false; }
				//if(this.data.ui.hovered && obj.get(0) === this.data.ui.hovered.get(0)) { return; }
				if(!obj.hasClass("jstree-hovered")) { this.dehover_node(); }
				this.data.ui.hovered = obj.children("a").addClass("jstree-hovered").parent();
				this.__callback({ "obj" : obj });
			},
			dehover_node : function () {
				var obj = this.data.ui.hovered, p;
				if(!obj || !obj.length) { return false; }
				p = obj.children("a").removeClass("jstree-hovered").parent();
				if(this.data.ui.hovered[0] === p[0]) { this.data.ui.hovered = null; }
				this.__callback({ "obj" : obj });
			},
			select_node : function (obj, check, e) {
				obj = this._get_node(obj);
				if(obj == -1 || !obj || !obj.length) { return false; }
				var s = this._get_settings().ui,
					is_multiple = (s.select_multiple_modifier == "on" || (s.select_multiple_modifier !== false && e && e[s.select_multiple_modifier + "Key"])),
					is_selected = this.is_selected(obj),
					proceed = true;
				if(check) {
					if(s.disable_selecting_children && is_multiple && obj.parents("li", this.get_container()).children(".jstree-clicked").length) {
						return false;
					}
					proceed = false;
					switch(!0) {
						case (is_selected && !is_multiple): 
							this.deselect_all();
							is_selected = false;
							proceed = true;
							break;
						case (!is_selected && !is_multiple): 
							if(s.select_limit == -1 || s.select_limit > 0) {
								this.deselect_all();
								proceed = true;
							}
							break;
						case (is_selected && is_multiple): 
							this.deselect_node(obj);
							break;
						case (!is_selected && is_multiple): 
							if(s.select_limit == -1 || this.data.ui.selected.length + 1 <= s.select_limit) { 
								proceed = true;
							}
							break;
					}
				}
				if(proceed && !is_selected) {
					obj.children("a").addClass("jstree-clicked");
					this.data.ui.selected = this.data.ui.selected.add(obj);
					this.data.ui.last_selected = obj;
					this.__callback({ "obj" : obj });
				}
			},
			deselect_node : function (obj) {
				obj = this._get_node(obj);
				if(!obj.length) { return false; }
				if(this.is_selected(obj)) {
					obj.children("a").removeClass("jstree-clicked");
					this.data.ui.selected = this.data.ui.selected.not(obj);
					if(this.data.ui.last_selected.get(0) === obj.get(0)) { this.data.ui.last_selected = this.data.ui.selected.eq(0); }
					this.__callback({ "obj" : obj });
				}
			},
			toggle_select : function (obj) {
				obj = this._get_node(obj);
				if(!obj.length) { return false; }
				if(this.is_selected(obj)) { this.deselect_node(obj); }
				else { this.select_node(obj); }
			},
			is_selected : function (obj) { return this.data.ui.selected.index(this._get_node(obj)) >= 0; },
			get_selected : function (context) { 
				return context ? $(context).find(".jstree-clicked").parent() : this.data.ui.selected; 
			},
			deselect_all : function (context) {
				if(context) { $(context).find(".jstree-clicked").removeClass("jstree-clicked"); } 
				else { this.get_container().find(".jstree-clicked").removeClass("jstree-clicked"); }
				this.data.ui.selected = $([]);
				this.data.ui.last_selected = false;
				this.__callback();
			}
		}
	});
	// include the selection plugin by default
	$.jstree.defaults.plugins.push("ui");
})(jQuery);
//*/

/* 
 * jsTree CRRM plugin 1.0
 * Handles creating/renaming/removing/moving nodes by user interaction.
 */
(function ($) {
	$.jstree.plugin("crrm", { 
		__init : function () {
			this.get_container()
				.bind("move_node.jstree", $.proxy(function (e, data) {
					if(this._get_settings().crrm.move.open_onmove) {
						var t = this;
						data.rslt.np.parentsUntil(".jstree").andSelf().filter(".jstree-closed").each(function () {
							t.open_node(this, false, true);
						});
					}
				}, this));
		},
		defaults : {
			input_width_limit : 200,
			move : {
				always_copy			: false, // false, true or "multitree"
				open_onmove			: true,
				default_position	: "last",
				check_move			: function (m) { return true; }
			}
		},
		_fn : {
			_show_input : function (obj, callback) {
				obj = this._get_node(obj);
				var rtl = this._get_settings().core.rtl,
					w = this._get_settings().crrm.input_width_limit,
					w1 = obj.children("ins").width(),
					w2 = obj.find("> a:visible > ins").width() * obj.find("> a:visible > ins").length,
					t = this.get_text(obj),
					h1 = $("<div>", { css : { "position" : "absolute", "top" : "-200px", "left" : (rtl ? "0px" : "-1000px"), "visibility" : "hidden" } }).appendTo("body"),
					h2 = obj.css("position","relative").append(
					$("<input>", { 
						"value" : t,
						// "size" : t.length,
						"css" : {
							"padding" : "0",
							"border" : "1px solid silver",
							"position" : "absolute",
							"left"  : (rtl ? "auto" : (w1 + w2 + 4) + "px"),
							"right" : (rtl ? (w1 + w2 + 4) + "px" : "auto"),
							"top" : "0px",
							"height" : (this.data.core.li_height - 2) + "px",
							"lineHeight" : (this.data.core.li_height - 2) + "px",
							"width" : "150px" // will be set a bit further down
						},
						"blur" : $.proxy(function () {
							var i = obj.children("input"),
								v = i.val();
							if(v === "") { v = t; }
							i.remove(); // rollback purposes
							this.set_text(obj,t); // rollback purposes
							this.rename_node(obj, v);
							callback.call(this, obj, v, t);
							obj.css("position","");
						}, this),
						"keyup" : function (event) {
							var key = event.keyCode || event.which;
							if(key == 27) { this.value = t; this.blur(); return; }
							else if(key == 13) { this.blur(); return; }
							else {
								h2.width(Math.min(h1.text("pW" + this.value).width(),w));
							}
						}
					})
				).children("input"); 
				this.set_text(obj, "");
				h1.css({
						fontFamily		: h2.css('fontFamily')		|| '',
						fontSize		: h2.css('fontSize')		|| '',
						fontWeight		: h2.css('fontWeight')		|| '',
						fontStyle		: h2.css('fontStyle')		|| '',
						fontStretch		: h2.css('fontStretch')		|| '',
						fontVariant		: h2.css('fontVariant')		|| '',
						letterSpacing	: h2.css('letterSpacing')	|| '',
						wordSpacing		: h2.css('wordSpacing')		|| ''
				});
				h2.width(Math.min(h1.text("pW" + h2[0].value).width(),w))[0].select();
			},
			rename : function (obj) {
				obj = this._get_node(obj);
				this.__rollback();
				var f = this.__callback;
				this._show_input(obj, function (obj, new_name, old_name) { 
					f.call(this, { "obj" : obj, "new_name" : new_name, "old_name" : old_name });
				});
			},
			create : function (obj, position, js, callback, skip_rename) {
				var t, _this = this;
				obj = this._get_node(obj);
				if(!obj) { obj = -1; }
				this.__rollback();
				t = this.create_node(obj, position, js, function (t) {
					var p = this._get_parent(t),
						pos = $(t).index();
					if(callback) { callback.call(this, t); }
					if(p.length && p.hasClass("jstree-closed")) { this.open_node(p, false, true); }
					if(!skip_rename) { 
						this._show_input(t, function (obj, new_name, old_name) { 
							_this.__callback({ "obj" : obj, "name" : new_name, "parent" : p, "position" : pos });
						});
					}
					else { _this.__callback({ "obj" : t, "name" : this.get_text(t), "parent" : p, "position" : pos }); }
				});
				return t;
			},
			remove : function (obj) {
				obj = this._get_node(obj, true);
				this.__rollback();
				this.delete_node(obj);
				this.__callback({ "obj" : obj });
			},
			check_move : function () {
				if(!this.__call_old()) { return false; }
				var s = this._get_settings().crrm.move;
				if(!s.check_move.call(this, this._get_move())) { return false; }
				return true;
			},
			move_node : function (obj, ref, position, is_copy, is_prepared, skip_check) {
				var s = this._get_settings().crrm.move;
				if(!is_prepared) { 
					if(!position) { position = s.default_position; }
					if(position === "inside" && !s.default_position.match(/^(before|after)$/)) { position = s.default_position; }
					return this.__call_old(true, obj, ref, position, is_copy, false, skip_check);
				}
				// if the move is already prepared
				if(s.always_copy === true || (s.always_copy === "multitree" && obj.rt.get_index() !== obj.ot.get_index() )) {
					is_copy = true;
				}
				this.__call_old(true, obj, ref, position, is_copy, true, skip_check);
			},

			cut : function (obj) {
				obj = this._get_node(obj);
				this.data.crrm.cp_nodes = false;
				this.data.crrm.ct_nodes = false;
				if(!obj || !obj.length) { return false; }
				this.data.crrm.ct_nodes = obj;
			},
			copy : function (obj) {
				obj = this._get_node(obj);
				this.data.crrm.cp_nodes = false;
				this.data.crrm.ct_nodes = false;
				if(!obj || !obj.length) { return false; }
				this.data.crrm.cp_nodes = obj;
			},
			paste : function (obj) { 
				obj = this._get_node(obj);
				if(!obj || !obj.length) { return false; }
				if(!this.data.crrm.ct_nodes && !this.data.crrm.cp_nodes) { return false; }
				if(this.data.crrm.ct_nodes) { this.move_node(this.data.crrm.ct_nodes, obj); }
				if(this.data.crrm.cp_nodes) { this.move_node(this.data.crrm.cp_nodes, obj, false, true); }
				this.data.crrm.cp_nodes = false;
				this.data.crrm.ct_nodes = false;
			}
		}
	});
	// include the crr plugin by default
	$.jstree.defaults.plugins.push("crrm");
})(jQuery);

/* 
 * jsTree themes plugin 1.0
 * Handles loading and setting themes, as well as detecting path to themes, etc.
 */
(function ($) {
	var themes_loaded = [];
	// this variable stores the path to the themes folder - if left as false - it will be autodetected
	$.jstree._themes = false;
	$.jstree.plugin("themes", {
		__init : function () { 
			this.get_container()
				.bind("init.jstree", $.proxy(function () {
						var s = this._get_settings().themes;
						this.data.themes.dots = s.dots; 
						this.data.themes.icons = s.icons; 
						//alert(s.dots);
						this.set_theme(s.theme, s.url);
					}, this))
				.bind("loaded.jstree", $.proxy(function () {
						// bound here too, as simple HTML tree's won't honor dots & icons otherwise
						if(!this.data.themes.dots) { this.hide_dots(); }
						else { this.show_dots(); }
						if(!this.data.themes.icons) { this.hide_icons(); }
						else { this.show_icons(); }
					}, this));
		},
		defaults : { 
			theme : "default", 
			url : false,
			dots : true,
			icons : true
		},
		_fn : {
			set_theme : function (theme_name, theme_url) {
				if(!theme_name) { return false; }
				if(!theme_url) { theme_url = $.jstree._themes + theme_name + '/style.css'; }
				if($.inArray(theme_url, themes_loaded) == -1) {
					$.vakata.css.add_sheet({ "url" : theme_url, "rel" : "jstree" });
					themes_loaded.push(theme_url);
				}
				if(this.data.themes.theme != theme_name) {
					this.get_container().removeClass('jstree-' + this.data.themes.theme);
					this.data.themes.theme = theme_name;
				}
				this.get_container().addClass('jstree-' + theme_name);
				if(!this.data.themes.dots) { this.hide_dots(); }
				else { this.show_dots(); }
				if(!this.data.themes.icons) { this.hide_icons(); }
				else { this.show_icons(); }
				this.__callback();
			},
			get_theme	: function () { return this.data.themes.theme; },

			show_dots	: function () { this.data.themes.dots = true; this.get_container().children("ul").removeClass("jstree-no-dots"); },
			hide_dots	: function () { this.data.themes.dots = false; this.get_container().children("ul").addClass("jstree-no-dots"); },
			toggle_dots	: function () { if(this.data.themes.dots) { this.hide_dots(); } else { this.show_dots(); } },

			show_icons	: function () { this.data.themes.icons = true; this.get_container().children("ul").removeClass("jstree-no-icons"); },
			hide_icons	: function () { this.data.themes.icons = false; this.get_container().children("ul").addClass("jstree-no-icons"); },
			toggle_icons: function () { if(this.data.themes.icons) { this.hide_icons(); } else { this.show_icons(); } }
		}
	});
	// autodetect themes path
	$(function () {
		if($.jstree._themes === false) {
			$("script").each(function () { 
				if(this.src.toString().match(/jquery\.jstree[^\/]*?\.js(\?.*)?$/)) { 
					$.jstree._themes = this.src.toString().replace(/jquery\.jstree[^\/]*?\.js(\?.*)?$/, "") + 'themes/'; 
					return false; 
				}
			});
		}
		if($.jstree._themes === false) { $.jstree._themes = "themes/"; }
	});
	// include the themes plugin by default
	$.jstree.defaults.plugins.push("themes");
})(jQuery);
//*/

/*
 * jsTree hotkeys plugin 1.0
 * Enables keyboard navigation for all tree instances
 * Depends on the jstree ui & jquery hotkeys plugins
 */
(function ($) {
	var bound = [];
	function exec(i, event) {
		var f = $.jstree._focused(), tmp;
		if(f && f.data && f.data.hotkeys && f.data.hotkeys.enabled) { 
			tmp = f._get_settings().hotkeys[i];
			if(tmp) { return tmp.call(f, event); }
		}
	}
	$.jstree.plugin("hotkeys", {
		__init : function () {
			if(typeof $.hotkeys === "undefined") { throw "jsTree hotkeys: jQuery hotkeys plugin not included."; }
			if(!this.data.ui) { throw "jsTree hotkeys: jsTree UI plugin not included."; }
			$.each(this._get_settings().hotkeys, function (i, val) {
				if($.inArray(i, bound) == -1) {
					$(document).bind("keydown", i, function (event) { return exec(i, event); });
					bound.push(i);
				}
			});
			this.enable_hotkeys();
		},
		defaults : {
			"up" : function () { 
				var o = this.data.ui.hovered || this.data.ui.last_selected || -1;
				this.hover_node(this._get_prev(o));
				return false; 
			},
			"down" : function () { 
				var o = this.data.ui.hovered || this.data.ui.last_selected || -1;
				this.hover_node(this._get_next(o));
				return false;
			},
			"left" : function () { 
				var o = this.data.ui.hovered || this.data.ui.last_selected;
				if(o) {
					if(o.hasClass("jstree-open")) { this.close_node(o); }
					else { this.hover_node(this._get_prev(o)); }
				}
				return false;
			},
			"right" : function () { 
				var o = this.data.ui.hovered || this.data.ui.last_selected;
				if(o && o.length) {
					if(o.hasClass("jstree-closed")) { this.open_node(o); }
					else { this.hover_node(this._get_next(o)); }
				}
				return false;
			},
			"space" : function () { 
				if(this.data.ui.hovered) { this.data.ui.hovered.children("a:eq(0)").click(); } 
				return false; 
			},
			"ctrl+space" : function (event) { 
				event.type = "click";
				if(this.data.ui.hovered) { this.data.ui.hovered.children("a:eq(0)").trigger(event); } 
				return false; 
			},
			"f2" : function () { this.rename(this.data.ui.hovered || this.data.ui.last_selected); },
			"del" : function () { this.remove(this.data.ui.hovered || this._get_node(null)); }
		},
		_fn : {
			enable_hotkeys : function () {
				this.data.hotkeys.enabled = true;
			},
			disable_hotkeys : function () {
				this.data.hotkeys.enabled = false;
			}
		}
	});
})(jQuery);
//*/

/* 
 * jsTree JSON 1.0
 * The JSON data store. Datastores are build by overriding the `load_node` and `_is_loaded` functions.
 */
(function ($) {
	$.jstree.plugin("json_data", {
		defaults : { 
			data : false,
			ajax : false,
			correct_state : true,
			progressive_render : false
		},
		_fn : {
			load_node : function (obj, s_call, e_call) { var _this = this; this.load_node_json(obj, function () { _this.__callback({ "obj" : obj }); s_call.call(this); }, e_call); },
			_is_loaded : function (obj) { 
				var s = this._get_settings().json_data, d;
				obj = this._get_node(obj); 
				if(obj && obj !== -1 && s.progressive_render && !obj.is(".jstree-open, .jstree-leaf") && obj.children("ul").children("li").length === 0 && obj.data("jstree-children")) {
					d = this._parse_json(obj.data("jstree-children"));
					if(d) {
						obj.append(d);
						$.removeData(obj, "jstree-children");
					}
					this.clean_node(obj);
					return true;
				}
				return obj == -1 || !obj || !s.ajax || obj.is(".jstree-open, .jstree-leaf") || obj.children("ul").children("li").size() > 0;
			},
			load_node_json : function (obj, s_call, e_call) {
				var s = this.get_settings().json_data, d,
					error_func = function () {},
					success_func = function () {};
				obj = this._get_node(obj);
				if(obj && obj !== -1) {
					if(obj.data("jstree-is-loading")) { return; }
					else { obj.data("jstree-is-loading",true); }
				}
				switch(!0) {
					case (!s.data && !s.ajax): throw "Neither data nor ajax settings supplied.";
					case (!!s.data && !s.ajax) || (!!s.data && !!s.ajax && (!obj || obj === -1)):
						if(!obj || obj == -1) {
							d = this._parse_json(s.data);
							if(d) {
								this.get_container().children("ul").empty().append(d.children());
								this.clean_node();
							}
							else { 
								if(s.correct_state) { this.get_container().children("ul").empty(); }
							}
						}
						if(s_call) { s_call.call(this); }
						break;
					case (!s.data && !!s.ajax) || (!!s.data && !!s.ajax && obj && obj !== -1):
						error_func = function (x, t, e) {
							var ef = this.get_settings().json_data.ajax.error; 
							if(ef) { ef.call(this, x, t, e); }
							if(obj != -1 && obj.length) {
								obj.children(".jstree-loading").removeClass("jstree-loading");
								obj.data("jstree-is-loading",false);
								if(t === "success" && s.correct_state) { obj.removeClass("jstree-open jstree-closed").addClass("jstree-leaf"); }
							}
							else {
								if(t === "success" && s.correct_state) { this.get_container().children("ul").empty(); }
							}
							if(e_call) { e_call.call(this); }
						};
						success_func = function (d, t, x) {
							var sf = this.get_settings().json_data.ajax.success; 
							if(sf) { d = sf.call(this,d,t,x) || d; }
							if(d === "" || (!$.isArray(d) && !$.isPlainObject(d))) {
								return error_func.call(this, x, t, "");
							}
							d = this._parse_json(d);
							if(d) {
								if(obj === -1 || !obj) { this.get_container().children("ul").empty().append(d.children()); }
								else { obj.append(d).children(".jstree-loading").removeClass("jstree-loading"); obj.data("jstree-is-loading",false); }
								this.clean_node(obj);
								if(s_call) { s_call.call(this); }
							}
							else {
								if(obj === -1 || !obj) {
									if(s.correct_state) { 
										this.get_container().children("ul").empty(); 
										if(s_call) { s_call.call(this); }
									}
								}
								else {
									obj.children(".jstree-loading").removeClass("jstree-loading");
									obj.data("jstree-is-loading",false);
									if(s.correct_state) { 
										obj.removeClass("jstree-open jstree-closed").addClass("jstree-leaf"); 
										if(s_call) { s_call.call(this); } 
									}
								}
							}
						};
						s.ajax.context = this;
						s.ajax.error = error_func;
						s.ajax.success = success_func;
						if(!s.ajax.dataType) { s.ajax.dataType = "json"; }
						if($.isFunction(s.ajax.url)) { s.ajax.url = s.ajax.url.call(this, obj); }
						if($.isFunction(s.ajax.data)) { s.ajax.data = s.ajax.data.call(this, obj); }
						$.ajax(s.ajax);
						break;
				}
			},
			_parse_json : function (js, is_callback) {
				var d = false, 
					p = this._get_settings(),
					s = p.json_data,
					t = p.core.html_titles,
					tmp, i, j, ul1, ul2;

				if(!js) { return d; }
				if($.isFunction(js)) { 
					js = js.call(this);
				}
				if($.isArray(js)) {
					d = $();
					if(!js.length) { return false; }
					for(i = 0, j = js.length; i < j; i++) {
						tmp = this._parse_json(js[i], true);
						if(tmp.length) { d = d.add(tmp); }
					}
				}
				else {
					if(typeof js == "string") { js = { data : js }; }
					if(!js.data && js.data !== "") { return d; }
					d = $("<li>");
					if(js.attr) { d.attr(js.attr); }
					if(js.metadata) { d.data("jstree", js.metadata); }
					if(js.state) { d.addClass("jstree-" + js.state); }
					if(!$.isArray(js.data)) { tmp = js.data; js.data = []; js.data.push(tmp); }
					$.each(js.data, function (i, m) {
						tmp = $("<a>");
						if($.isFunction(m)) { m = m.call(this, js); }
						if(typeof m == "string") { tmp.attr('href','#')[ t ? "html" : "text" ](m); }
						else {
							if(!m.attr) { m.attr = {}; }
							if(!m.attr.href) { m.attr.href = '#'; }
							tmp.attr(m.attr)[ t ? "html" : "text" ](m.title);
							if(m.language) { tmp.addClass(m.language); }
						}
						tmp.prepend("<ins class='jstree-icon'>&#160;</ins>");
						if(!m.icon && js.icon) { m.icon = js.icon; }
						if(m.icon) { 
							if(m.icon.indexOf("/") === -1) { tmp.children("ins").addClass(m.icon); }
							else { tmp.children("ins").css("background","url('" + m.icon + "') center center no-repeat"); }
						}
						d.append(tmp);
					});
					d.prepend("<ins class='jstree-icon'>&#160;</ins>");
					if(js.children) { 
						if(s.progressive_render && js.state !== "open") {
							d.addClass("jstree-closed").data("jstree-children", js.children);
						}
						else {
							if($.isFunction(js.children)) {
								js.children = js.children.call(this, js);
							}
							if($.isArray(js.children) && js.children.length) {
								tmp = this._parse_json(js.children, true);
								if(tmp.length) {
									ul2 = $("<ul>");
									ul2.append(tmp);
									d.append(ul2);
								}
							}
						}
					}
				}
				if(!is_callback) {
					ul1 = $("<ul>");
					ul1.append(d);
					d = ul1;
				}
				return d;
			},
			get_json : function (obj, li_attr, a_attr, is_callback) {
				var result = [], 
					s = this._get_settings(), 
					_this = this,
					tmp1, tmp2, li, a, t, lang;
				obj = this._get_node(obj);
				if(!obj || obj === -1) { obj = this.get_container().find("> ul > li"); }
				li_attr = $.isArray(li_attr) ? li_attr : [ "id", "class" ];
				if(!is_callback && this.data.types) { li_attr.push(s.types.type_attr); }
				a_attr = $.isArray(a_attr) ? a_attr : [ ];

				obj.each(function () {
					li = $(this);
					tmp1 = { data : [] };
					if(li_attr.length) { tmp1.attr = { }; }
					$.each(li_attr, function (i, v) { 
						tmp2 = li.attr(v); 
						if(tmp2 && tmp2.length && tmp2.replace(/jstree[^ ]*|$/ig,'').length) {
							tmp1.attr[v] = tmp2.replace(/jstree[^ ]*|$/ig,''); 
						}
					});
					if(li.hasClass("jstree-open")) { tmp1.state = "open"; }
					if(li.hasClass("jstree-closed")) { tmp1.state = "closed"; }
					a = li.children("a");
					a.each(function () {
						t = $(this);
						if(
							a_attr.length || 
							$.inArray("languages", s.plugins) !== -1 || 
							t.children("ins").get(0).style.backgroundImage.length || 
							(t.children("ins").get(0).className && t.children("ins").get(0).className.replace(/jstree[^ ]*|$/ig,'').length)
						) { 
							lang = false;
							if($.inArray("languages", s.plugins) !== -1 && $.isArray(s.languages) && s.languages.length) {
								$.each(s.languages, function (l, lv) {
									if(t.hasClass(lv)) {
										lang = lv;
										return false;
									}
								});
							}
							tmp2 = { attr : { }, title : _this.get_text(t, lang) }; 
							$.each(a_attr, function (k, z) {
								tmp1.attr[z] = (t.attr(z) || "").replace(/jstree[^ ]*|$/ig,'');
							});
							$.each(s.languages, function (k, z) {
								if(t.hasClass(z)) { tmp2.language = z; return true; }
							});
							if(t.children("ins").get(0).className.replace(/jstree[^ ]*|$/ig,'').replace(/^\s+$/ig,"").length) {
								tmp2.icon = t.children("ins").get(0).className.replace(/jstree[^ ]*|$/ig,'').replace(/^\s+$/ig,"");
							}
							if(t.children("ins").get(0).style.backgroundImage.length) {
								tmp2.icon = t.children("ins").get(0).style.backgroundImage.replace("url(","").replace(")","");
							}
						}
						else {
							tmp2 = _this.get_text(t);
						}
						if(a.length > 1) { tmp1.data.push(tmp2); }
						else { tmp1.data = tmp2; }
					});
					li = li.find("> ul > li");
					if(li.length) { tmp1.children = _this.get_json(li, li_attr, a_attr, true); }
					result.push(tmp1);
				});
				return result;
			}
		}
	});
})(jQuery);
//*/

/* 
 * jsTree languages plugin 1.0
 * Adds support for multiple language versions in one tree
 * This basically allows for many titles coexisting in one node, but only one of them being visible at any given time
 * This is useful for maintaining the same structure in many languages (hence the name of the plugin)
 */
(function ($) {
	$.jstree.plugin("languages", {
		__init : function () { this._load_css();  },
		defaults : [],
		_fn : {
			set_lang : function (i) { 
				var langs = this._get_settings().languages,
					st = false,
					selector = ".jstree-" + this.get_index() + ' a';
				if(!$.isArray(langs) || langs.length === 0) { return false; }
				if($.inArray(i,langs) == -1) {
					if(!!langs[i]) { i = langs[i]; }
					else { return false; }
				}
				if(i == this.data.languages.current_language) { return true; }
				st = $.vakata.css.get_css(selector + "." + this.data.languages.current_language, false, this.data.languages.language_css);
				if(st !== false) { st.style.display = "none"; }
				st = $.vakata.css.get_css(selector + "." + i, false, this.data.languages.language_css);
				if(st !== false) { st.style.display = ""; }
				this.data.languages.current_language = i;
				this.__callback(i);
				return true;
			},
			get_lang : function () {
				return this.data.languages.current_language;
			},
			get_text : function (obj, lang) {
				obj = this._get_node(obj) || this.data.ui.last_selected;
				if(!obj.size()) { return false; }
				var langs = this._get_settings().languages,
					s = this._get_settings().core.html_titles;
				if($.isArray(langs) && langs.length) {
					lang = (lang && $.inArray(lang,langs) != -1) ? lang : this.data.languages.current_language;
					obj = obj.children("a." + lang);
				}
				else { obj = obj.children("a:eq(0)"); }
				if(s) {
					obj = obj.clone();
					obj.children("INS").remove();
					return obj.html();
				}
				else {
					obj = obj.contents().filter(function() { return this.nodeType == 3; })[0];
					return obj.nodeValue;
				}
			},
			set_text : function (obj, val, lang) {
				obj = this._get_node(obj) || this.data.ui.last_selected;
				if(!obj.size()) { return false; }
				var langs = this._get_settings().languages,
					s = this._get_settings().core.html_titles,
					tmp;
				if($.isArray(langs) && langs.length) {
					lang = (lang && $.inArray(lang,langs) != -1) ? lang : this.data.languages.current_language;
					obj = obj.children("a." + lang);
				}
				else { obj = obj.children("a:eq(0)"); }
				if(s) {
					tmp = obj.children("INS").clone();
					obj.html(val).prepend(tmp);
					this.__callback({ "obj" : obj, "name" : val, "lang" : lang });
					return true;
				}
				else {
					obj = obj.contents().filter(function() { return this.nodeType == 3; })[0];
					this.__callback({ "obj" : obj, "name" : val, "lang" : lang });
					return (obj.nodeValue = val);
				}
			},
			_load_css : function () {
				var langs = this._get_settings().languages,
					str = "/* languages css */",
					selector = ".jstree-" + this.get_index() + ' a',
					ln;
				if($.isArray(langs) && langs.length) {
					this.data.languages.current_language = langs[0];
					for(ln = 0; ln < langs.length; ln++) {
						str += selector + "." + langs[ln] + " {";
						if(langs[ln] != this.data.languages.current_language) { str += " display:none; "; }
						str += " } ";
					}
					this.data.languages.language_css = $.vakata.css.add_sheet({ 'str' : str });
				}
			},
			create_node : function (obj, position, js, callback) {
				var t = this.__call_old(true, obj, position, js, function (t) {
					var langs = this._get_settings().languages,
						a = t.children("a"),
						ln;
					if($.isArray(langs) && langs.length) {
						for(ln = 0; ln < langs.length; ln++) {
							if(!a.is("." + langs[ln])) {
								t.append(a.eq(0).clone().removeClass(langs.join(" ")).addClass(langs[ln]));
							}
						}
						a.not("." + langs.join(", .")).remove();
					}
					if(callback) { callback.call(this, t); }
				});
				return t;
			}
		}
	});
})(jQuery);
//*/

/*
 * jsTree cookies plugin 1.0
 * Stores the currently opened/selected nodes in a cookie and then restores them
 * Depends on the jquery.cookie plugin
 */
(function ($) {
	$.jstree.plugin("cookies", {
		__init : function () {
			if(typeof $.cookie === "undefined") { throw "jsTree cookie: jQuery cookie plugin not included."; }

			var s = this._get_settings().cookies,
				tmp;
			if(!!s.save_opened) {
				tmp = $.cookie(s.save_opened);
				if(tmp && tmp.length) { this.data.core.to_open = tmp.split(","); }
			}
			if(!!s.save_selected) {
				tmp = $.cookie(s.save_selected);
				if(tmp && tmp.length && this.data.ui) { this.data.ui.to_select = tmp.split(","); }
			}
			this.get_container()
				.one( ( this.data.ui ? "reselect" : "reopen" ) + ".jstree", $.proxy(function () {
					this.get_container()
						.bind("open_node.jstree close_node.jstree select_node.jstree deselect_node.jstree", $.proxy(function (e) { 
								if(this._get_settings().cookies.auto_save) { this.save_cookie((e.handleObj.namespace + e.handleObj.type).replace("jstree","")); }
							}, this));
				}, this));
		},
		defaults : {
			save_opened		: "jstree_open",
			save_selected	: "jstree_select",
			auto_save		: true,
			cookie_options	: {}
		},
		_fn : {
			save_cookie : function (c) {
				if(this.data.core.refreshing) { return; }
				var s = this._get_settings().cookies;
				if(!c) { // if called manually and not by event
					if(s.save_opened) {
						this.save_opened();
						$.cookie(s.save_opened, this.data.core.to_open.join(","), s.cookie_options);
					}
					if(s.save_selected && this.data.ui) {
						this.save_selected();
						$.cookie(s.save_selected, this.data.ui.to_select.join(","), s.cookie_options);
					}
					return;
				}
				switch(c) {
					case "open_node":
					case "close_node":
						if(!!s.save_opened) { 
							this.save_opened(); 
							$.cookie(s.save_opened, this.data.core.to_open.join(","), s.cookie_options); 
						}
						break;
					case "select_node":
					case "deselect_node":
						if(!!s.save_selected && this.data.ui) { 
							this.save_selected(); 
							$.cookie(s.save_selected, this.data.ui.to_select.join(","), s.cookie_options); 
						}
						break;
				}
			}
		}
	});
	// include cookies by default
	$.jstree.defaults.plugins.push("cookies");
})(jQuery);
//*/

/*
 * jsTree sort plugin 1.0
 * Sorts items alphabetically (or using any other function)
 */
(function ($) {
	$.jstree.plugin("sort", {
		__init : function () {
			this.get_container()
				.bind("load_node.jstree", $.proxy(function (e, data) {
						var obj = this._get_node(data.rslt.obj);
						obj = obj === -1 ? this.get_container().children("ul") : obj.children("ul");
						this.sort(obj);
					}, this))
				.bind("rename_node.jstree", $.proxy(function (e, data) {
						this.sort(data.rslt.obj.parent());
					}, this))
				.bind("move_node.jstree", $.proxy(function (e, data) {
						var m = data.rslt.np == -1 ? this.get_container() : data.rslt.np;
						this.sort(m.children("ul"));
					}, this));
		},
		defaults : function (a, b) { return this.get_text(a) > this.get_text(b) ? 1 : -1; },
		_fn : {
			sort : function (obj) {
				var s = this._get_settings().sort,
					t = this;
				obj.append($.makeArray(obj.children("li")).sort($.proxy(s, t)));
				obj.find("> li > ul").each(function() { t.sort($(this)); });
				this.clean_node(obj);
			}
		}
	});
})(jQuery);
//*/

/*
 * jsTree DND plugin 1.0
 * Drag and drop plugin for moving/copying nodes
 */
(function ($) {
	var o = false,
		r = false,
		m = false,
		sli = false,
		sti = false,
		dir1 = false,
		dir2 = false;
	$.vakata.dnd = {
		is_down : false,
		is_drag : false,
		helper : false,
		scroll_spd : 10,
		init_x : 0,
		init_y : 0,
		threshold : 5,
		user_data : {},

		drag_start : function (e, data, html) { 
			if($.vakata.dnd.is_drag) { $.vakata.drag_stop({}); }
			try {
				e.currentTarget.unselectable = "on";
				e.currentTarget.onselectstart = function() { return false; };
				if(e.currentTarget.style) { e.currentTarget.style.MozUserSelect = "none"; }
			} catch(err) { }
			$.vakata.dnd.init_x = e.pageX;
			$.vakata.dnd.init_y = e.pageY;
			$.vakata.dnd.user_data = data;
			$.vakata.dnd.is_down = true;
			$.vakata.dnd.helper = $("<div id='vakata-dragged'>").html(html).css("opacity", "0.75");
			$(document).bind("mousemove", $.vakata.dnd.drag);
			$(document).bind("mouseup", $.vakata.dnd.drag_stop);
			return false;
		},
		drag : function (e) { 
			if(!$.vakata.dnd.is_down) { return; }
			if(!$.vakata.dnd.is_drag) {
				if(Math.abs(e.pageX - $.vakata.dnd.init_x) > 5 || Math.abs(e.pageY - $.vakata.dnd.init_y) > 5) { 
					$.vakata.dnd.helper.appendTo("body");
					$.vakata.dnd.is_drag = true;
					$(document).triggerHandler("drag_start.vakata", { "event" : e, "data" : $.vakata.dnd.user_data });
				}
				else { return; }
			}

			// maybe use a scrolling parent element instead of document?
			if(e.type === "mousemove") { // thought of adding scroll in order to move the helper, but mouse poisition is n/a
				var d = $(document), t = d.scrollTop(), l = d.scrollLeft();
				if(e.pageY - t < 20) { 
					if(sti && dir1 === "down") { clearInterval(sti); sti = false; }
					if(!sti) { dir1 = "up"; sti = setInterval(function () { $(document).scrollTop($(document).scrollTop() - $.vakata.dnd.scroll_spd); }, 150); }
				}
				else { 
					if(sti && dir1 === "up") { clearInterval(sti); sti = false; }
				}
				if($(window).height() - (e.pageY - t) < 20) {
					if(sti && dir1 === "up") { clearInterval(sti); sti = false; }
					if(!sti) { dir1 = "down"; sti = setInterval(function () { $(document).scrollTop($(document).scrollTop() + $.vakata.dnd.scroll_spd); }, 150); }
				}
				else { 
					if(sti && dir1 === "down") { clearInterval(sti); sti = false; }
				}

				if(e.pageX - l < 20) {
					if(sli && dir2 === "right") { clearInterval(sli); sli = false; }
					if(!sli) { dir2 = "left"; sli = setInterval(function () { $(document).scrollLeft($(document).scrollLeft() - $.vakata.dnd.scroll_spd); }, 150); }
				}
				else { 
					if(sli && dir2 === "left") { clearInterval(sli); sli = false; }
				}
				if($(window).width() - (e.pageX - l) < 20) {
					if(sli && dir2 === "left") { clearInterval(sli); sli = false; }
					if(!sli) { dir2 = "right"; sli = setInterval(function () { $(document).scrollLeft($(document).scrollLeft() + $.vakata.dnd.scroll_spd); }, 150); }
				}
				else { 
					if(sli && dir2 === "right") { clearInterval(sli); sli = false; }
				}
			}

			$.vakata.dnd.helper.css({ left : (e.pageX + 5) + "px", top : (e.pageY + 10) + "px" });
			$(document).triggerHandler("drag.vakata", { "event" : e, "data" : $.vakata.dnd.user_data });
		},
		drag_stop : function (e) {
			$(document).unbind("mousemove", $.vakata.dnd.drag);
			$(document).unbind("mouseup", $.vakata.dnd.drag_stop);
			$(document).triggerHandler("drag_stop.vakata", { "event" : e, "data" : $.vakata.dnd.user_data });
			$.vakata.dnd.helper.remove();
			$.vakata.dnd.init_x = 0;
			$.vakata.dnd.init_y = 0;
			$.vakata.dnd.user_data = {};
			$.vakata.dnd.is_down = false;
			$.vakata.dnd.is_drag = false;
		}
	};
	$(function() {
		var css_string = '#vakata-dragged { display:block; margin:0 0 0 0; padding:4px 4px 4px 24px; position:absolute; top:-2000px; line-height:16px; z-index:10000; } ';
		$.vakata.css.add_sheet({ str : css_string });
	});

	$.jstree.plugin("dnd", {
		__init : function () {
			this.data.dnd = {
				active : false,
				after : false,
				inside : false,
				before : false,
				off : false,
				prepared : false,
				w : 0,
				to1 : false,
				to2 : false,
				cof : false,
				cw : false,
				ch : false,
				i1 : false,
				i2 : false
			};
			this.get_container()
				.bind("mouseenter.jstree", $.proxy(function () {
						if($.vakata.dnd.is_drag && $.vakata.dnd.user_data.jstree && this.data.themes) {
							m.attr("class", "jstree-" + this.data.themes.theme); 
							$.vakata.dnd.helper.attr("class", "jstree-dnd-helper jstree-" + this.data.themes.theme);
						}
					}, this))
				.bind("mouseleave.jstree", $.proxy(function () {
						if($.vakata.dnd.is_drag && $.vakata.dnd.user_data.jstree) {
							if(this.data.dnd.i1) { clearInterval(this.data.dnd.i1); }
							if(this.data.dnd.i2) { clearInterval(this.data.dnd.i2); }
						}
					}, this))
				.bind("mousemove.jstree", $.proxy(function (e) {
						if($.vakata.dnd.is_drag && $.vakata.dnd.user_data.jstree) {
							var cnt = this.get_container()[0];

							// Horizontal scroll
							if(e.pageX + 24 > this.data.dnd.cof.left + this.data.dnd.cw) {
								if(this.data.dnd.i1) { clearInterval(this.data.dnd.i1); }
								this.data.dnd.i1 = setInterval($.proxy(function () { this.scrollLeft += $.vakata.dnd.scroll_spd; }, cnt), 100);
							}
							else if(e.pageX - 24 < this.data.dnd.cof.left) {
								if(this.data.dnd.i1) { clearInterval(this.data.dnd.i1); }
								this.data.dnd.i1 = setInterval($.proxy(function () { this.scrollLeft -= $.vakata.dnd.scroll_spd; }, cnt), 100);
							}
							else {
								if(this.data.dnd.i1) { clearInterval(this.data.dnd.i1); }
							}

							// Vertical scroll
							if(e.pageY + 24 > this.data.dnd.cof.top + this.data.dnd.ch) {
								if(this.data.dnd.i2) { clearInterval(this.data.dnd.i2); }
								this.data.dnd.i2 = setInterval($.proxy(function () { this.scrollTop += $.vakata.dnd.scroll_spd; }, cnt), 100);
							}
							else if(e.pageY - 24 < this.data.dnd.cof.top) {
								if(this.data.dnd.i2) { clearInterval(this.data.dnd.i2); }
								this.data.dnd.i2 = setInterval($.proxy(function () { this.scrollTop -= $.vakata.dnd.scroll_spd; }, cnt), 100);
							}
							else {
								if(this.data.dnd.i2) { clearInterval(this.data.dnd.i2); }
							}

						}
					}, this))
				.delegate("a", "mousedown.jstree", $.proxy(function (e) { 
						if(e.which === 1) {
							this.start_drag(e.currentTarget, e);
							return false;
						}
					}, this))
				.delegate("a", "mouseenter.jstree", $.proxy(function (e) { 
						if($.vakata.dnd.is_drag && $.vakata.dnd.user_data.jstree) {
							this.dnd_enter(e.currentTarget);
						}
					}, this))
				.delegate("a", "mousemove.jstree", $.proxy(function (e) { 
						if($.vakata.dnd.is_drag && $.vakata.dnd.user_data.jstree) {
							if(typeof this.data.dnd.off.top === "undefined") { this.data.dnd.off = $(e.target).offset(); }
							this.data.dnd.w = (e.pageY - (this.data.dnd.off.top || 0)) % this.data.core.li_height;
							if(this.data.dnd.w < 0) { this.data.dnd.w += this.data.core.li_height; }
							this.dnd_show();
						}
					}, this))
				.delegate("a", "mouseleave.jstree", $.proxy(function (e) { 
						if($.vakata.dnd.is_drag && $.vakata.dnd.user_data.jstree) {
							this.data.dnd.after		= false;
							this.data.dnd.before	= false;
							this.data.dnd.inside	= false;
							$.vakata.dnd.helper.children("ins").attr("class","jstree-invalid");
							m.hide();
							if(r && r[0] === e.target.parentNode) {
								if(this.data.dnd.to1) {
									clearTimeout(this.data.dnd.to1);
									this.data.dnd.to1 = false;
								}
								if(this.data.dnd.to2) {
									clearTimeout(this.data.dnd.to2);
									this.data.dnd.to2 = false;
								}
							}
						}
					}, this))
				.delegate("a", "mouseup.jstree", $.proxy(function (e) { 
						if($.vakata.dnd.is_drag && $.vakata.dnd.user_data.jstree) {
							this.dnd_finish(e);
						}
					}, this));

			$(document)
				.bind("drag_stop.vakata", $.proxy(function () {
						this.data.dnd.after		= false;
						this.data.dnd.before	= false;
						this.data.dnd.inside	= false;
						this.data.dnd.off		= false;
						this.data.dnd.prepared	= false;
						this.data.dnd.w			= false;
						this.data.dnd.to1		= false;
						this.data.dnd.to2		= false;
						this.data.dnd.active	= false;
						this.data.dnd.foreign	= false;
						if(m) { m.css({ "top" : "-2000px" }); }
					}, this))
				.bind("drag_start.vakata", $.proxy(function (e, data) {
						if(data.data.jstree) { 
							var et = $(data.event.target);
							if(et.closest(".jstree").hasClass("jstree-" + this.get_index())) {
								this.dnd_enter(et);
							}
						}
					}, this));

			var s = this._get_settings().dnd;
			if(s.drag_target) {
				$(document)
					.delegate(s.drag_target, "mousedown.jstree", $.proxy(function (e) {
						o = e.target;
						$.vakata.dnd.drag_start(e, { jstree : true, obj : e.target }, "<ins class='jstree-icon'></ins>" + $(e.target).text() );
						if(this.data.themes) { 
							m.attr("class", "jstree-" + this.data.themes.theme); 
							$.vakata.dnd.helper.attr("class", "jstree-dnd-helper jstree-" + this.data.themes.theme); 
						}
						$.vakata.dnd.helper.children("ins").attr("class","jstree-invalid");
						var cnt = this.get_container();
						this.data.dnd.cof = cnt.offset();
						this.data.dnd.cw = parseInt(cnt.width(),10);
						this.data.dnd.ch = parseInt(cnt.height(),10);
						this.data.dnd.foreign = true;
						return false;
					}, this));
			}
			if(s.drop_target) {
				$(document)
					.delegate(s.drop_target, "mouseenter.jstree", $.proxy(function (e) {
							if(this.data.dnd.active && this._get_settings().dnd.drop_check.call(this, { "o" : o, "r" : $(e.target) })) {
								$.vakata.dnd.helper.children("ins").attr("class","jstree-ok");
							}
						}, this))
					.delegate(s.drop_target, "mouseleave.jstree", $.proxy(function (e) {
							if(this.data.dnd.active) {
								$.vakata.dnd.helper.children("ins").attr("class","jstree-invalid");
							}
						}, this))
					.delegate(s.drop_target, "mouseup.jstree", $.proxy(function (e) {
							if(this.data.dnd.active && $.vakata.dnd.helper.children("ins").hasClass("jstree-ok")) {
								this._get_settings().dnd.drop_finish.call(this, { "o" : o, "r" : $(e.target) });
							}
						}, this));
			}
		},
		defaults : {
			copy_modifier	: "ctrl",
			check_timeout	: 200,
			open_timeout	: 500,
			drop_target		: ".jstree-drop",
			drop_check		: function (data) { return true; },
			drop_finish		: $.noop,
			drag_target		: ".jstree-draggable",
			drag_finish		: $.noop,
			drag_check		: function (data) { return { after : false, before : false, inside : true }; }
		},
		_fn : {
			dnd_prepare : function () {
				if(!r || !r.length) { return; }
				this.data.dnd.off = r.offset();
				if(this._get_settings().core.rtl) {
					this.data.dnd.off.right = this.data.dnd.off.left + r.width();
				}
				if(this.data.dnd.foreign) {
					var a = this._get_settings().dnd.drag_check.call(this, { "o" : o, "r" : r });
					this.data.dnd.after = a.after;
					this.data.dnd.before = a.before;
					this.data.dnd.inside = a.inside;
					this.data.dnd.prepared = true;
					return this.dnd_show();
				}
				this.prepare_move(o, r, "before");
				this.data.dnd.before = this.check_move();
				this.prepare_move(o, r, "after");
				this.data.dnd.after = this.check_move();
				if(this._is_loaded(r)) {
					this.prepare_move(o, r, "inside");
					this.data.dnd.inside = this.check_move();
				}
				else {
					this.data.dnd.inside = false;
				}
				this.data.dnd.prepared = true;
				return this.dnd_show();
			},
			dnd_show : function () {
				if(!this.data.dnd.prepared) { return; }
				var o = ["before","inside","after"],
					r = false,
					rtl = this._get_settings().core.rtl,
					pos;
				if(this.data.dnd.w < this.data.core.li_height/3) { o = ["before","inside","after"]; }
				else if(this.data.dnd.w <= this.data.core.li_height*2/3) {
					o = this.data.dnd.w < this.data.core.li_height/2 ? ["inside","before","after"] : ["inside","after","before"];
				}
				else { o = ["after","inside","before"]; }
				$.each(o, $.proxy(function (i, val) { 
					if(this.data.dnd[val]) {
						$.vakata.dnd.helper.children("ins").attr("class","jstree-ok");
						r = val;
						return false;
					}
				}, this));
				if(r === false) { $.vakata.dnd.helper.children("ins").attr("class","jstree-invalid"); }
				
				pos = rtl ? (this.data.dnd.off.right - 18) : (this.data.dnd.off.left + 10);
				switch(r) {
					case "before":
						m.css({ "left" : pos + "px", "top" : (this.data.dnd.off.top - 6) + "px" }).show();
						break;
					case "after":
						m.css({ "left" : pos + "px", "top" : (this.data.dnd.off.top + this.data.core.li_height - 7) + "px" }).show();
						break;
					case "inside":
						m.css({ "left" : pos + ( rtl ? -4 : 4) + "px", "top" : (this.data.dnd.off.top + this.data.core.li_height/2 - 5) + "px" }).show();
						break;
					default:
						m.hide();
						break;
				}
				return r;
			},
			dnd_open : function () {
				this.data.dnd.to2 = false;
				this.open_node(r, $.proxy(this.dnd_prepare,this), true);
			},
			dnd_finish : function (e) {
				if(this.data.dnd.foreign) {
					if(this.data.dnd.after || this.data.dnd.before || this.data.dnd.inside) {
						this._get_settings().dnd.drag_finish.call(this, { "o" : o, "r" : r });
					}
				}
				else {
					this.dnd_prepare();
					this.move_node(o, r, this.dnd_show(), e[this._get_settings().dnd.copy_modifier + "Key"]);
				}
				o = false;
				r = false;
				m.hide();
			},
			dnd_enter : function (obj) {
				var s = this._get_settings().dnd;
				this.data.dnd.prepared = false;
				r = this._get_node(obj);
				if(s.check_timeout) { 
					// do the calculations after a minimal timeout (users tend to drag quickly to the desired location)
					if(this.data.dnd.to1) { clearTimeout(this.data.dnd.to1); }
					this.data.dnd.to1 = setTimeout($.proxy(this.dnd_prepare, this), s.check_timeout); 
				}
				else { 
					this.dnd_prepare(); 
				}
				if(s.open_timeout) { 
					if(this.data.dnd.to2) { clearTimeout(this.data.dnd.to2); }
					if(r && r.length && r.hasClass("jstree-closed")) { 
						// if the node is closed - open it, then recalculate
						this.data.dnd.to2 = setTimeout($.proxy(this.dnd_open, this), s.open_timeout);
					}
				}
				else {
					if(r && r.length && r.hasClass("jstree-closed")) { 
						this.dnd_open();
					}
				}
			},
			start_drag : function (obj, e) {
				o = this._get_node(obj);
				if(this.data.ui && this.is_selected(o)) { o = this._get_node(null, true); }
				$.vakata.dnd.drag_start(e, { jstree : true, obj : o }, "<ins class='jstree-icon'></ins>" + (o.length > 1 ? "Multiple selection" : this.get_text(o)) );
				if(this.data.themes) { 
					m.attr("class", "jstree-" + this.data.themes.theme); 
					$.vakata.dnd.helper.attr("class", "jstree-dnd-helper jstree-" + this.data.themes.theme); 
				}
				var cnt = this.get_container();
				this.data.dnd.cof = cnt.children("ul").offset();
				this.data.dnd.cw = parseInt(cnt.width(),10);
				this.data.dnd.ch = parseInt(cnt.height(),10);
				this.data.dnd.active = true;
			}
		}
	});
	$(function() {
		var css_string = '' + 
			'#vakata-dragged ins { display:block; text-decoration:none; width:16px; height:16px; margin:0 0 0 0; padding:0; position:absolute; top:4px; left:4px; } ' + 
			'#vakata-dragged .jstree-ok { background:green; } ' + 
			'#vakata-dragged .jstree-invalid { background:red; } ' + 
			'#jstree-marker { padding:0; margin:0; line-height:12px; font-size:1px; overflow:hidden; height:12px; width:8px; position:absolute; top:-30px; z-index:10000; background-repeat:no-repeat; display:none; background-color:silver; } ';
		$.vakata.css.add_sheet({ str : css_string });
		m = $("<div>").attr({ id : "jstree-marker" }).hide().appendTo("body");
		$(document).bind("drag_start.vakata", function (e, data) {
			if(data.data.jstree) { 
				m.show(); 
			}
		});
		$(document).bind("drag_stop.vakata", function (e, data) {
			if(data.data.jstree) { m.hide(); }
		});
	});
})(jQuery);
//*/

/*
 * jsTree checkbox plugin 1.0
 * Inserts checkboxes in front of every node
 * Depends on the ui plugin
 * DOES NOT WORK NICELY WITH MULTITREE DRAG'N'DROP
 */
(function ($) {
	$.jstree.plugin("checkbox", {
		__init : function () {
			this.select_node = this.deselect_node = this.deselect_all = $.noop;
			this.get_selected = this.get_checked;

			this.get_container()
				.bind("open_node.jstree create_node.jstree clean_node.jstree", $.proxy(function (e, data) { 
						this._prepare_checkboxes(data.rslt.obj);
					}, this))
				.bind("loaded.jstree", $.proxy(function (e) {
						this._prepare_checkboxes();
					}, this))
				.delegate("a", "click.jstree", $.proxy(function (e) {
						if(this._get_node(e.target).hasClass("jstree-checked")) { this.uncheck_node(e.target); }
						else { this.check_node(e.target); }
						if(this.data.ui) { this.save_selected(); }
						if(this.data.cookies) { this.save_cookie("select_node"); }
						e.preventDefault();
					}, this));
		},
		__destroy : function () {
			this.get_container().find(".jstree-checkbox").remove();
		},
		_fn : {
			_prepare_checkboxes : function (obj) {
				obj = !obj || obj == -1 ? this.get_container() : this._get_node(obj);
				var c, _this = this, t;
				obj.each(function () {
					t = $(this);
					c = t.is("li") && t.hasClass("jstree-checked") ? "jstree-checked" : "jstree-unchecked";
					t.find("a").not(":has(.jstree-checkbox)").prepend("<ins class='jstree-checkbox'>&#160;</ins>").parent().not(".jstree-checked, .jstree-unchecked").addClass(c);
				});
				if(obj.is("li")) { this._repair_state(obj); }
				else { obj.find("> ul > li").each(function () { _this._repair_state(this); }); }
			},
			change_state : function (obj, state) {
				obj = this._get_node(obj);
				state = (state === false || state === true) ? state : obj.hasClass("jstree-checked");
				if(state) { obj.find("li").andSelf().removeClass("jstree-checked jstree-undetermined").addClass("jstree-unchecked"); }
				else { 
					obj.find("li").andSelf().removeClass("jstree-unchecked jstree-undetermined").addClass("jstree-checked"); 
					if(this.data.ui) { this.data.ui.last_selected = obj; }
					this.data.checkbox.last_selected = obj;
				}
				obj.parentsUntil(".jstree", "li").each(function () {
					var $this = $(this);
					if(state) {
						if($this.children("ul").children(".jstree-checked, .jstree-undetermined").length) {
							$this.parentsUntil(".jstree", "li").andSelf().removeClass("jstree-checked jstree-unchecked").addClass("jstree-undetermined");
							return false;
						}
						else {
							$this.removeClass("jstree-checked jstree-undetermined").addClass("jstree-unchecked");
						}
					}
					else {
						if($this.children("ul").children(".jstree-unchecked, .jstree-undetermined").length) {
							$this.parentsUntil(".jstree", "li").andSelf().removeClass("jstree-checked jstree-unchecked").addClass("jstree-undetermined");
							return false;
						}
						else {
							$this.removeClass("jstree-unchecked jstree-undetermined").addClass("jstree-checked");
						}
					}
				});
				if(this.data.ui) { this.data.ui.selected = this.get_checked(); }
				this.__callback(obj);
			},
			check_node : function (obj) {
				this.change_state(obj, false);
			},
			uncheck_node : function (obj) {
				this.change_state(obj, true);
			},
			check_all : function () {
				var _this = this;
				this.get_container().children("ul").children("li").each(function () {
					_this.check_node(this, false);
				});
			},
			uncheck_all : function () {
				var _this = this;
				this.get_container().children("ul").children("li").each(function () {
					_this.change_state(this, true);
				});
			},

			is_checked : function(obj) {
				obj = this._get_node(obj);
				return obj.length ? obj.is(".jstree-checked") : false;
			},
			get_checked : function (obj) {
				obj = !obj || obj === -1 ? this.get_container() : this._get_node(obj);
				return obj.find("> ul > .jstree-checked, .jstree-undetermined > ul > .jstree-checked");
			},
			get_unchecked : function (obj) { 
				obj = !obj || obj === -1 ? this.get_container() : this._get_node(obj);
				return obj.find("> ul > .jstree-unchecked, .jstree-undetermined > ul > .jstree-unchecked");
			},

			show_checkboxes : function () { this.get_container().children("ul").removeClass("jstree-no-checkboxes"); },
			hide_checkboxes : function () { this.get_container().children("ul").addClass("jstree-no-checkboxes"); },

			_repair_state : function (obj) {
				obj = this._get_node(obj);
				if(!obj.length) { return; }
				var a = obj.find("> ul > .jstree-checked").length,
					b = obj.find("> ul > .jstree-undetermined").length,
					c = obj.find("> ul > li").length;

				if(c === 0) { if(obj.hasClass("jstree-undetermined")) { this.check_node(obj); } }
				else if(a === 0 && b === 0) { this.uncheck_node(obj); }
				else if(a === c) { this.check_node(obj); }
				else { 
					obj.parentsUntil(".jstree","li").removeClass("jstree-checked jstree-unchecked").addClass("jstree-undetermined");
				}
			},
			reselect : function () {
				if(this.data.ui) { 
					var _this = this,
						s = this.data.ui.to_select;
					s = $.map($.makeArray(s), function (n) { return "#" + n.toString().replace(/^#/,"").replace('\\/','/').replace('/','\\/'); });
					this.deselect_all();
					$.each(s, function (i, val) { _this.check_node(val); });
					this.__callback();
				}
			}
		}
	});
})(jQuery);
//*/

/* 
 * jsTree XML 1.0
 * The XML data store. Datastores are build by overriding the `load_node` and `_is_loaded` functions.
 */
(function ($) {
	$.vakata.xslt = function (xml, xsl, callback) {
		var rs = "", xm, xs, processor, support;
		if(document.recalc) {
			xm = document.createElement('xml');
			xs = document.createElement('xml');
			xm.innerHTML = xml;
			xs.innerHTML = xsl;
			$("body").append(xm).append(xs);
			setTimeout( (function (xm, xs, callback) {
				return function () {
					callback.call(null, xm.transformNode(xs.XMLDocument));
					setTimeout( (function (xm, xs) { return function () { jQuery("body").remove(xm).remove(xs); }; })(xm, xs), 200);
				};
			}) (xm, xs, callback), 100);
			return true;
		}
		if(typeof window.DOMParser !== "undefined" && typeof window.XMLHttpRequest !== "undefined" && typeof window.XSLTProcessor !== "undefined") {
			processor = new XSLTProcessor();
			support = $.isFunction(processor.transformDocument) ? (typeof window.XMLSerializer !== "undefined") : true;
			if(!support) { return false; }
			xml = new DOMParser().parseFromString(xml, "text/xml");
			xsl = new DOMParser().parseFromString(xsl, "text/xml");
			if($.isFunction(processor.transformDocument)) {
				rs = document.implementation.createDocument("", "", null);
				processor.transformDocument(xml, xsl, rs, null);
				callback.call(null, XMLSerializer().serializeToString(rs));
				return true;
			}
			else {
				processor.importStylesheet(xsl);
				rs = processor.transformToFragment(xml, document);
				callback.call(null, $("<div>").append(rs).html());
				return true;
			}
		}
		return false;
	};
	var xsl = {
		'nest' : '<?xml version="1.0" encoding="utf-8" ?>' + 
			'<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >' + 
			'<xsl:output method="html" encoding="utf-8" omit-xml-declaration="yes" standalone="no" indent="no" media-type="text/html" />' + 
			'<xsl:template match="/">' + 
			'	<xsl:call-template name="nodes">' + 
			'		<xsl:with-param name="node" select="/root" />' + 
			'	</xsl:call-template>' + 
			'</xsl:template>' + 
			'<xsl:template name="nodes">' + 
			'	<xsl:param name="node" />' + 
			'	<ul>' + 
			'	<xsl:for-each select="$node/item">' + 
			'		<xsl:variable name="children" select="count(./item) &gt; 0" />' + 
			'		<li>' + 
			'			<xsl:attribute name="class">' + 
			'				<xsl:if test="position() = last()">jstree-last </xsl:if>' + 
			'				<xsl:choose>' + 
			'					<xsl:when test="@state = \'open\'">jstree-open </xsl:when>' + 
			'					<xsl:when test="$children or @hasChildren or @state = \'closed\'">jstree-closed </xsl:when>' + 
			'					<xsl:otherwise>jstree-leaf </xsl:otherwise>' + 
			'				</xsl:choose>' + 
			'				<xsl:value-of select="@class" />' + 
			'			</xsl:attribute>' + 
			'			<xsl:for-each select="@*">' + 
			'				<xsl:if test="name() != \'class\' and name() != \'state\' and name() != \'hasChildren\'">' + 
			'					<xsl:attribute name="{name()}"><xsl:value-of select="." /></xsl:attribute>' + 
			'				</xsl:if>' + 
			'			</xsl:for-each>' + 
			'	<ins class="jstree-icon"><xsl:text>&#xa0;</xsl:text></ins>' + 
			'			<xsl:for-each select="content/name">' + 
			'				<a>' + 
			'				<xsl:attribute name="href">' + 
			'					<xsl:choose>' + 
			'					<xsl:when test="@href"><xsl:value-of select="@href" /></xsl:when>' + 
			'					<xsl:otherwise>#</xsl:otherwise>' + 
			'					</xsl:choose>' + 
			'				</xsl:attribute>' + 
			'				<xsl:attribute name="class"><xsl:value-of select="@lang" /> <xsl:value-of select="@class" /></xsl:attribute>' + 
			'				<xsl:attribute name="style"><xsl:value-of select="@style" /></xsl:attribute>' + 
			'				<xsl:for-each select="@*">' + 
			'					<xsl:if test="name() != \'style\' and name() != \'class\' and name() != \'href\'">' + 
			'						<xsl:attribute name="{name()}"><xsl:value-of select="." /></xsl:attribute>' + 
			'					</xsl:if>' + 
			'				</xsl:for-each>' + 
			'					<ins>' + 
			'						<xsl:attribute name="class">jstree-icon ' + 
			'							<xsl:if test="string-length(attribute::icon) > 0 and not(contains(@icon,\'/\'))"><xsl:value-of select="@icon" /></xsl:if>' + 
			'						</xsl:attribute>' + 
			'						<xsl:if test="string-length(attribute::icon) > 0 and contains(@icon,\'/\')"><xsl:attribute name="style">background:url(<xsl:value-of select="@icon" />) center center no-repeat;</xsl:attribute></xsl:if>' + 
			'						<xsl:text>&#xa0;</xsl:text>' + 
			'					</ins>' + 
			'					<xsl:value-of select="current()" />' + 
			'				</a>' + 
			'			</xsl:for-each>' + 
			'			<xsl:if test="$children or @hasChildren"><xsl:call-template name="nodes"><xsl:with-param name="node" select="current()" /></xsl:call-template></xsl:if>' + 
			'		</li>' + 
			'	</xsl:for-each>' + 
			'	</ul>' + 
			'</xsl:template>' + 
			'</xsl:stylesheet>',

		'flat' : '<?xml version="1.0" encoding="utf-8" ?>' + 
			'<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >' + 
			'<xsl:output method="html" encoding="utf-8" omit-xml-declaration="yes" standalone="no" indent="no" media-type="text/xml" />' + 
			'<xsl:template match="/">' + 
			'	<ul>' + 
			'	<xsl:for-each select="//item[not(@parent_id) or @parent_id=0 or not(@parent_id = //item/@id)]">' + /* the last `or` may be removed */
			'		<xsl:call-template name="nodes">' + 
			'			<xsl:with-param name="node" select="." />' + 
			'			<xsl:with-param name="is_last" select="number(position() = last())" />' + 
			'		</xsl:call-template>' + 
			'	</xsl:for-each>' + 
			'	</ul>' + 
			'</xsl:template>' + 
			'<xsl:template name="nodes">' + 
			'	<xsl:param name="node" />' + 
			'	<xsl:param name="is_last" />' + 
			'	<xsl:variable name="children" select="count(//item[@parent_id=$node/attribute::id]) &gt; 0" />' + 
			'	<li>' + 
			'	<xsl:attribute name="class">' + 
			'		<xsl:if test="$is_last = true()">jstree-last </xsl:if>' + 
			'		<xsl:choose>' + 
			'			<xsl:when test="@state = \'open\'">jstree-open </xsl:when>' + 
			'			<xsl:when test="$children or @hasChildren or @state = \'closed\'">jstree-closed </xsl:when>' + 
			'			<xsl:otherwise>jstree-leaf </xsl:otherwise>' + 
			'		</xsl:choose>' + 
			'		<xsl:value-of select="@class" />' + 
			'	</xsl:attribute>' + 
			'	<xsl:for-each select="@*">' + 
			'		<xsl:if test="name() != \'parent_id\' and name() != \'hasChildren\' and name() != \'class\' and name() != \'state\'">' + 
			'		<xsl:attribute name="{name()}"><xsl:value-of select="." /></xsl:attribute>' + 
			'		</xsl:if>' + 
			'	</xsl:for-each>' + 
			'	<ins class="jstree-icon"><xsl:text>&#xa0;</xsl:text></ins>' + 
			'	<xsl:for-each select="content/name">' + 
			'		<a>' + 
			'		<xsl:attribute name="href">' + 
			'			<xsl:choose>' + 
			'			<xsl:when test="@href"><xsl:value-of select="@href" /></xsl:when>' + 
			'			<xsl:otherwise>#</xsl:otherwise>' + 
			'			</xsl:choose>' + 
			'		</xsl:attribute>' + 
			'		<xsl:attribute name="class"><xsl:value-of select="@lang" /> <xsl:value-of select="@class" /></xsl:attribute>' + 
			'		<xsl:attribute name="style"><xsl:value-of select="@style" /></xsl:attribute>' + 
			'		<xsl:for-each select="@*">' + 
			'			<xsl:if test="name() != \'style\' and name() != \'class\' and name() != \'href\'">' + 
			'				<xsl:attribute name="{name()}"><xsl:value-of select="." /></xsl:attribute>' + 
			'			</xsl:if>' + 
			'		</xsl:for-each>' + 
			'			<ins>' + 
			'				<xsl:attribute name="class">jstree-icon ' + 
			'					<xsl:if test="string-length(attribute::icon) > 0 and not(contains(@icon,\'/\'))"><xsl:value-of select="@icon" /></xsl:if>' + 
			'				</xsl:attribute>' + 
			'				<xsl:if test="string-length(attribute::icon) > 0 and contains(@icon,\'/\')"><xsl:attribute name="style">background:url(<xsl:value-of select="@icon" />) center center no-repeat;</xsl:attribute></xsl:if>' + 
			'				<xsl:text>&#xa0;</xsl:text>' + 
			'			</ins>' + 
			'			<xsl:value-of select="current()" />' + 
			'		</a>' + 
			'	</xsl:for-each>' + 
			'	<xsl:if test="$children">' + 
			'		<ul>' + 
			'		<xsl:for-each select="//item[@parent_id=$node/attribute::id]">' + 
			'			<xsl:call-template name="nodes">' + 
			'				<xsl:with-param name="node" select="." />' + 
			'				<xsl:with-param name="is_last" select="number(position() = last())" />' + 
			'			</xsl:call-template>' + 
			'		</xsl:for-each>' + 
			'		</ul>' + 
			'	</xsl:if>' + 
			'	</li>' + 
			'</xsl:template>' + 
			'</xsl:stylesheet>'
	};
	$.jstree.plugin("xml_data", {
		defaults : { 
			data : false,
			ajax : false,
			xsl : "flat",
			clean_node : false,
			correct_state : true
		},
		_fn : {
			load_node : function (obj, s_call, e_call) { var _this = this; this.load_node_xml(obj, function () { _this.__callback({ "obj" : obj }); s_call.call(this); }, e_call); },
			_is_loaded : function (obj) { 
				var s = this._get_settings().xml_data;
				obj = this._get_node(obj);
				return obj == -1 || !obj || !s.ajax || obj.is(".jstree-open, .jstree-leaf") || obj.children("ul").children("li").size() > 0;
			},
			load_node_xml : function (obj, s_call, e_call) {
				var s = this.get_settings().xml_data,
					error_func = function () {},
					success_func = function () {};

				obj = this._get_node(obj);
				if(obj && obj !== -1) {
					if(obj.data("jstree-is-loading")) { return; }
					else { obj.data("jstree-is-loading",true); }
				}
				switch(!0) {
					case (!s.data && !s.ajax): throw "Neither data nor ajax settings supplied.";
					case (!!s.data && !s.ajax) || (!!s.data && !!s.ajax && (!obj || obj === -1)):
						if(!obj || obj == -1) {
							this.parse_xml(s.data, $.proxy(function (d) {
								if(d) {
									d = d.replace(/ ?xmlns="[^"]*"/ig, "");
									if(d.length > 10) {
										d = $(d);
										this.get_container().children("ul").empty().append(d.children());
										if(s.clean_node) { this.clean_node(obj); }
										if(s_call) { s_call.call(this); }
									}
								}
								else { 
									if(s.correct_state) { 
										this.get_container().children("ul").empty(); 
										if(s_call) { s_call.call(this); }
									}
								}
							}, this));
						}
						break;
					case (!s.data && !!s.ajax) || (!!s.data && !!s.ajax && obj && obj !== -1):
						error_func = function (x, t, e) {
							var ef = this.get_settings().xml_data.ajax.error; 
							if(ef) { ef.call(this, x, t, e); }
							if(obj !== -1 && obj.length) {
								obj.children(".jstree-loading").removeClass("jstree-loading");
								obj.data("jstree-is-loading",false);
								if(t === "success" && s.correct_state) { obj.removeClass("jstree-open jstree-closed").addClass("jstree-leaf"); }
							}
							else {
								if(t === "success" && s.correct_state) { this.get_container().children("ul").empty(); }
							}
							if(e_call) { e_call.call(this); }
						};
						success_func = function (d, t, x) {
							d = x.responseText;
							var sf = this.get_settings().xml_data.ajax.success; 
							if(sf) { d = sf.call(this,d,t,x) || d; }
							if(d == "") {
								return error_func.call(this, x, t, "");
							}
							this.parse_xml(d, $.proxy(function (d) {
								if(d) {
									d = d.replace(/ ?xmlns="[^"]*"/ig, "");
									if(d.length > 10) {
										d = $(d);
										if(obj === -1 || !obj) { this.get_container().children("ul").empty().append(d.children()); }
										else { obj.children(".jstree-loading").removeClass("jstree-loading"); obj.append(d); obj.data("jstree-is-loading",false); }
										if(s.clean_node) { this.clean_node(obj); }
										if(s_call) { s_call.call(this); }
									}
									else {
										if(obj && obj !== -1) { 
											obj.children(".jstree-loading").removeClass("jstree-loading");
											obj.data("jstree-is-loading",false);
											if(s.correct_state) { 
												obj.removeClass("jstree-open jstree-closed").addClass("jstree-leaf"); 
												if(s_call) { s_call.call(this); } 
											}
										}
										else {
											if(s.correct_state) { 
												this.get_container().children("ul").empty();
												if(s_call) { s_call.call(this); } 
											}
										}
									}
								}
							}, this));
						};
						s.ajax.context = this;
						s.ajax.error = error_func;
						s.ajax.success = success_func;
						if(!s.ajax.dataType) { s.ajax.dataType = "xml"; }
						if($.isFunction(s.ajax.url)) { s.ajax.url = s.ajax.url.call(this, obj); }
						if($.isFunction(s.ajax.data)) { s.ajax.data = s.ajax.data.call(this, obj); }
						$.ajax(s.ajax);
						break;
				}
			},
			parse_xml : function (xml, callback) {
				var s = this._get_settings().xml_data;
				$.vakata.xslt(xml, xsl[s.xsl], callback);
			},
			get_xml : function (tp, obj, li_attr, a_attr, is_callback) {
				var result = "", 
					s = this._get_settings(), 
					_this = this,
					tmp1, tmp2, li, a, lang;
				if(!tp) { tp = "flat"; }
				if(!is_callback) { is_callback = 0; }
				obj = this._get_node(obj);
				if(!obj || obj === -1) { obj = this.get_container().find("> ul > li"); }
				li_attr = $.isArray(li_attr) ? li_attr : [ "id", "class" ];
				if(!is_callback && this.data.types && $.inArray(s.types.type_attr, li_attr) === -1) { li_attr.push(s.types.type_attr); }

				a_attr = $.isArray(a_attr) ? a_attr : [ ];

				if(!is_callback) { result += "<root>"; }
				obj.each(function () {
					result += "<item";
					li = $(this);
					$.each(li_attr, function (i, v) { result += " " + v + "=\"" + (li.attr(v) || "").replace(/jstree[^ ]*|$/ig,'').replace(/^\s+$/ig,"") + "\""; });
					if(li.hasClass("jstree-open")) { result += " state=\"open\""; }
					if(li.hasClass("jstree-closed")) { result += " state=\"closed\""; }
					if(tp === "flat") { result += " parent_id=\"" + is_callback + "\""; }
					result += ">";
					result += "<content>";
					a = li.children("a");
					a.each(function () {
						tmp1 = $(this);
						lang = false;
						result += "<name";
						if($.inArray("languages", s.plugins) !== -1) {
							$.each(s.languages, function (k, z) {
								if(tmp1.hasClass(z)) { result += " lang=\"" + z + "\""; lang = z; return false; }
							});
						}
						if(a_attr.length) { 
							$.each(a_attr, function (k, z) {
								result += " " + z + "=\"" + (tmp1.attr(z) || "").replace(/jstree[^ ]*|$/ig,'') + "\"";
							});
						}
						if(tmp1.children("ins").get(0).className.replace(/jstree[^ ]*|$/ig,'').replace(/^\s+$/ig,"").length) {
							result += ' icon="' + tmp1.children("ins").get(0).className.replace(/jstree[^ ]*|$/ig,'').replace(/^\s+$/ig,"") + '"';
						}
						if(tmp1.children("ins").get(0).style.backgroundImage.length) {
							result += ' icon="' + tmp1.children("ins").get(0).style.backgroundImage.replace("url(","").replace(")","") + '"';
						}
						result += ">";
						result += "<![CDATA[" + _this.get_text(tmp1, lang) + "]]>";
						result += "</name>";
					});
					result += "</content>";
					tmp2 = li[0].id;
					li = li.find("> ul > li");
					if(li.length) { tmp2 = _this.get_xml(tp, li, li_attr, a_attr, tmp2); }
					else { tmp2 = ""; }
					if(tp == "nest") { result += tmp2; }
					result += "</item>";
					if(tp == "flat") { result += tmp2; }
				});
				if(!is_callback) { result += "</root>"; }
				return result;
			}
		}
	});
})(jQuery);
//*/

/*
 * jsTree search plugin 1.0
 * Enables both sync and async search on the tree
 * DOES NOT WORK WITH JSON PROGRESSIVE RENDER
 */
(function ($) {
	$.expr[':'].jstree_contains = function(a,i,m){
		return (a.textContent || a.innerText || "").toLowerCase().indexOf(m[3].toLowerCase())>=0;
	};
	$.jstree.plugin("search", {
		__init : function () {
			this.data.search.str = "";
			this.data.search.result = $();
		},
		defaults : {
			ajax : false, // OR ajax object
			case_insensitive : false
		},
		_fn : {
			search : function (str, skip_async) {
				if(str === "") { return; }
				var s = this.get_settings().search, 
					t = this,
					error_func = function () { },
					success_func = function () { };
				this.data.search.str = str;

				if(!skip_async && s.ajax !== false && this.get_container().find(".jstree-closed:eq(0)").length > 0) {
					this.search.supress_callback = true;
					error_func = function () { };
					success_func = function (d, t, x) {
						var sf = this.get_settings().search.ajax.success; 
						if(sf) { d = sf.call(this,d,t,x) || d; }
						this.data.search.to_open = d;
						this._search_open();
					};
					s.ajax.context = this;
					s.ajax.error = error_func;
					s.ajax.success = success_func;
					if($.isFunction(s.ajax.url)) { s.ajax.url = s.ajax.url.call(this, str); }
					if($.isFunction(s.ajax.data)) { s.ajax.data = s.ajax.data.call(this, str); }
					if(!s.ajax.data) { s.ajax.data = { "search_string" : str }; }
					if(!s.ajax.dataType || /^json/.exec(s.ajax.dataType)) { s.ajax.dataType = "json"; }
					$.ajax(s.ajax);
					return;
				}
				if(this.data.search.result.length) { this.clear_search(); }
				this.data.search.result = this.get_container().find("a" + (this.data.languages ? "." + this.get_lang() : "" ) + ":" + (s.case_insensitive ? "jstree_contains" : "contains") + "(" + this.data.search.str + ")");
				this.data.search.result.addClass("jstree-search").parents(".jstree-closed").each(function () {
					t.open_node(this, false, true);
				});
				this.__callback({ nodes : this.data.search.result, str : str });
			},
			clear_search : function (str) {
				this.data.search.result.removeClass("jstree-search");
				this.__callback(this.data.search.result);
				this.data.search.result = $();
			},
			_search_open : function (is_callback) {
				var _this = this,
					done = true,
					current = [],
					remaining = [];
				if(this.data.search.to_open.length) {
					$.each(this.data.search.to_open, function (i, val) {
						if(val == "#") { return true; }
						if($(val).length && $(val).is(".jstree-closed")) { current.push(val); }
						else { remaining.push(val); }
					});
					if(current.length) {
						this.data.search.to_open = remaining;
						$.each(current, function (i, val) { 
							_this.open_node(val, function () { _this._search_open(true); }); 
						});
						done = false;
					}
				}
				if(done) { this.search(this.data.search.str, true); }
			}
		}
	});
})(jQuery);
//*/

/*
 * jsTree contextmenu plugin 1.0
 */
(function ($) {
	$.vakata.context = {
		cnt		: $("<div id='vakata-contextmenu'>"),
		vis		: false,
		tgt		: false,
		par		: false,
		func	: false,
		data	: false,
		show	: function (s, t, x, y, d, p) {
			var html = $.vakata.context.parse(s), h, w;
			if(!html) { return; }
			$.vakata.context.vis = true;
			$.vakata.context.tgt = t;
			$.vakata.context.par = p || t || null;
			$.vakata.context.data = d || null;
			$.vakata.context.cnt
				.html(html)
				.css({ "visibility" : "hidden", "display" : "block", "left" : 0, "top" : 0 });
			h = $.vakata.context.cnt.height();
			w = $.vakata.context.cnt.width();
			if(x + w > $(document).width()) { 
				x = $(document).width() - (w + 5); 
				$.vakata.context.cnt.find("li > ul").addClass("right"); 
			}
			if(y + h > $(document).height()) { 
				y = y - (h + t[0].offsetHeight); 
				$.vakata.context.cnt.find("li > ul").addClass("bottom"); 
			}

			$.vakata.context.cnt
				.css({ "left" : x, "top" : y })
				.find("li:has(ul)")
					.bind("mouseenter", function (e) { 
						var w = $(document).width(),
							h = $(document).height(),
							ul = $(this).children("ul").show(); 
						if(w !== $(document).width()) { ul.toggleClass("right"); }
						if(h !== $(document).height()) { ul.toggleClass("bottom"); }
					})
					.bind("mouseleave", function (e) { 
						$(this).children("ul").hide(); 
					})
					.end()
				.css({ "visibility" : "visible" })
				.show();
			$(document).triggerHandler("context_show.vakata");
		},
		hide	: function () {
			$.vakata.context.vis = false;
			$.vakata.context.cnt.attr("class","").hide();
			$(document).triggerHandler("context_hide.vakata");
		},
		parse	: function (s, is_callback) {
			if(!s) { return false; }
			var str = "",
				tmp = false,
				was_sep = true;
			if(!is_callback) { $.vakata.context.func = {}; }
			str += "<ul>";
			$.each(s, function (i, val) {
				if(!val) { return true; }
				$.vakata.context.func[i] = val.action;
				if(!was_sep && val.separator_before) {
					str += "<li class='vakata-separator vakata-separator-before'></li>";
				}
				was_sep = false;
				str += "<li class='" + (val._class || "") + (val._disabled ? " jstree-contextmenu-disabled " : "") + "'><ins ";
				if(val.icon && val.icon.indexOf("/") === -1) { str += " class='" + val.icon + "' "; }
				if(val.icon && val.icon.indexOf("/") !== -1) { str += " style='background:url(" + val.icon + ") center center no-repeat;' "; }
				str += ">&#160;</ins><a href='#' rel='" + i + "'>";
				if(val.submenu) {
					str += "<span style='float:right;'>&raquo;</span>";
				}
				str += val.label + "</a>";
				if(val.submenu) {
					tmp = $.vakata.context.parse(val.submenu, true);
					if(tmp) { str += tmp; }
				}
				str += "</li>";
				if(val.separator_after) {
					str += "<li class='vakata-separator vakata-separator-after'></li>";
					was_sep = true;
				}
			});
			str = str.replace(/<li class\='vakata-separator vakata-separator-after'\><\/li\>$/,"");
			str += "</ul>";
			return str.length > 10 ? str : false;
		},
		exec	: function (i) {
			if($.isFunction($.vakata.context.func[i])) {
				$.vakata.context.func[i].call($.vakata.context.data, $.vakata.context.par);
				return true;
			}
			else { return false; }
		}
	};
	$(function () {
		var css_string = '' + 
			'#vakata-contextmenu { display:none; position:absolute; margin:0; padding:0; min-width:180px; background:#ebebeb; border:1px solid silver; z-index:10000; *width:180px; } ' + 
			'#vakata-contextmenu ul { min-width:180px; *width:180px; } ' + 
			'#vakata-contextmenu ul, #vakata-contextmenu li { margin:0; padding:0; list-style-type:none; display:block; } ' + 
			'#vakata-contextmenu li { line-height:20px; min-height:20px; position:relative; padding:0px; } ' + 
			'#vakata-contextmenu li a { padding:1px 6px; line-height:17px; display:block; text-decoration:none; margin:1px 1px 0 1px; } ' + 
			'#vakata-contextmenu li ins { float:left; width:16px; height:16px; text-decoration:none; margin-right:2px; } ' + 
			'#vakata-contextmenu li a:hover, #vakata-contextmenu li.vakata-hover > a { background:gray; color:white; } ' + 
			'#vakata-contextmenu li ul { display:none; position:absolute; top:-2px; left:100%; background:#ebebeb; border:1px solid gray; } ' + 
			'#vakata-contextmenu .right { right:100%; left:auto; } ' + 
			'#vakata-contextmenu .bottom { bottom:-1px; top:auto; } ' + 
			'#vakata-contextmenu li.vakata-separator { min-height:0; height:1px; line-height:1px; font-size:1px; overflow:hidden; margin:0 2px; background:silver; /* border-top:1px solid #fefefe; */ padding:0; } ';
		$.vakata.css.add_sheet({ str : css_string });
		$.vakata.context.cnt
			.delegate("a","click", function (e) { e.preventDefault(); })
			.delegate("a","mouseup", function (e) {
				if(!$(this).parent().hasClass("jstree-contextmenu-disabled") && $.vakata.context.exec($(this).attr("rel"))) {
					$.vakata.context.hide();
				}
				else { $(this).blur(); }
			})
			.delegate("a","mouseover", function () {
				$.vakata.context.cnt.find(".vakata-hover").removeClass("vakata-hover");
			})
			.appendTo("body");
		$(document).bind("mousedown", function (e) { if($.vakata.context.vis && !$.contains($.vakata.context.cnt[0], e.target)) { $.vakata.context.hide(); } });
		if(typeof $.hotkeys !== "undefined") {
			$(document)
				.bind("keydown", "up", function (e) { 
					if($.vakata.context.vis) { 
						var o = $.vakata.context.cnt.find("ul:visible").last().children(".vakata-hover").removeClass("vakata-hover").prevAll("li:not(.vakata-separator)").first();
						if(!o.length) { o = $.vakata.context.cnt.find("ul:visible").last().children("li:not(.vakata-separator)").last(); }
						o.addClass("vakata-hover");
						e.stopImmediatePropagation(); 
						e.preventDefault();
					} 
				})
				.bind("keydown", "down", function (e) { 
					if($.vakata.context.vis) { 
						var o = $.vakata.context.cnt.find("ul:visible").last().children(".vakata-hover").removeClass("vakata-hover").nextAll("li:not(.vakata-separator)").first();
						if(!o.length) { o = $.vakata.context.cnt.find("ul:visible").last().children("li:not(.vakata-separator)").first(); }
						o.addClass("vakata-hover");
						e.stopImmediatePropagation(); 
						e.preventDefault();
					} 
				})
				.bind("keydown", "right", function (e) { 
					if($.vakata.context.vis) { 
						$.vakata.context.cnt.find(".vakata-hover").children("ul").show().children("li:not(.vakata-separator)").removeClass("vakata-hover").first().addClass("vakata-hover");
						e.stopImmediatePropagation(); 
						e.preventDefault();
					} 
				})
				.bind("keydown", "left", function (e) { 
					if($.vakata.context.vis) { 
						$.vakata.context.cnt.find(".vakata-hover").children("ul").hide().children(".vakata-separator").removeClass("vakata-hover");
						e.stopImmediatePropagation(); 
						e.preventDefault();
					} 
				})
				.bind("keydown", "esc", function (e) { 
					$.vakata.context.hide(); 
					e.preventDefault();
				})
				.bind("keydown", "space", function (e) { 
					$.vakata.context.cnt.find(".vakata-hover").last().children("a").click();
					e.preventDefault();
				});
		}
	});

	$.jstree.plugin("contextmenu", {
		__init : function () {
			this.get_container()
				.delegate("a", "contextmenu.jstree", $.proxy(function (e) {
						e.preventDefault();
						this.show_contextmenu(e.currentTarget, e.pageX, e.pageY);
					}, this))
				.bind("destroy.jstree", $.proxy(function () {
						if(this.data.contextmenu) {
							$.vakata.context.hide();
						}
					}, this));
			$(document).bind("context_hide.vakata", $.proxy(function () { this.data.contextmenu = false; }, this));
		},
		defaults : { 
			select_node : false, // requires UI plugin
			show_at_node : true,
			items : { // Could be a function that should return an object like this one
				"create" : {
					"separator_before"	: false,
					"separator_after"	: true,
					"label"				: "Create",
					"action"			: function (obj) { this.create(obj); }
				},
				"rename" : {
					"separator_before"	: false,
					"separator_after"	: false,
					"label"				: "Rename",
					"action"			: function (obj) { this.rename(obj); }
				},
				"remove" : {
					"separator_before"	: false,
					"icon"				: false,
					"separator_after"	: false,
					"label"				: "Delete",
					"action"			: function (obj) { this.remove(obj); }
				},
				"ccp" : {
					"separator_before"	: true,
					"icon"				: false,
					"separator_after"	: false,
					"label"				: "Edit",
					"action"			: false,
					"submenu" : { 
						"cut" : {
							"separator_before"	: false,
							"separator_after"	: false,
							"label"				: "Cut",
							"action"			: function (obj) { this.cut(obj); }
						},
						"copy" : {
							"separator_before"	: false,
							"icon"				: false,
							"separator_after"	: false,
							"label"				: "Copy",
							"action"			: function (obj) { this.copy(obj); }
						},
						"paste" : {
							"separator_before"	: false,
							"icon"				: false,
							"separator_after"	: false,
							"label"				: "Paste",
							"action"			: function (obj) { this.paste(obj); }
						}
					}
				}
			}
		},
		_fn : {
			show_contextmenu : function (obj, x, y) {
				obj = this._get_node(obj);
				var s = this.get_settings().contextmenu,
					a = obj.children("a:visible:eq(0)"),
					o = false;
				if(s.select_node && this.data.ui && !this.is_selected(obj)) {
					this.deselect_all();
					this.select_node(obj, true);
				}
				if(s.show_at_node || typeof x === "undefined" || typeof y === "undefined") {
					o = a.offset();
					x = o.left;
					y = o.top + this.data.core.li_height;
				}
				if($.isFunction(s.items)) { s.items = s.items.call(this, obj); }
				this.data.contextmenu = true;
				$.vakata.context.show(s.items, a, x, y, this, obj);
				if(this.data.themes) { $.vakata.context.cnt.attr("class", "jstree-" + this.data.themes.theme + "-context"); }
			}
		}
	});
})(jQuery);
//*/

/* 
 * jsTree types plugin 1.0
 * Adds support types of nodes
 * You can set an attribute on each li node, that represents its type.
 * According to the type setting the node may get custom icon/validation rules
 */
(function ($) {
	$.jstree.plugin("types", {
		__init : function () {
			var s = this._get_settings().types;
			this.data.types.attach_to = [];
			this.get_container()
				.bind("init.jstree", $.proxy(function () { 
						var types = s.types, 
							attr  = s.type_attr, 
							icons_css = "", 
							_this = this;

						$.each(types, function (i, tp) {
							$.each(tp, function (k, v) { 
								if(!/^(max_depth|max_children|icon|valid_children)$/.test(k)) { _this.data.types.attach_to.push(k); }
							});
							if(!tp.icon) { return true; }
							if( tp.icon.image || tp.icon.position) {
								if(i == "default")	{ icons_css += '.jstree-' + _this.get_index() + ' a > .jstree-icon { '; }
								else				{ icons_css += '.jstree-' + _this.get_index() + ' li[' + attr + '=' + i + '] > a > .jstree-icon { '; }
								if(tp.icon.image)	{ icons_css += ' background-image:url(' + tp.icon.image + '); '; }
								if(tp.icon.position){ icons_css += ' background-position:' + tp.icon.position + '; '; }
								else				{ icons_css += ' background-position:0 0; '; }
								icons_css += '} ';
							}
						});
						if(icons_css != "") { $.vakata.css.add_sheet({ 'str' : icons_css }); }
					}, this))
				.bind("before.jstree", $.proxy(function (e, data) { 
						if($.inArray(data.func, this.data.types.attach_to) !== -1) {
							var s = this._get_settings().types.types,
								t = this._get_type(data.args[0]);
							if(
								( 
									(s[t] && typeof s[t][data.func] !== "undefined") || 
									(s["default"] && typeof s["default"][data.func] !== "undefined")
								) && !this._check(data.func, data.args[0])
							) {
								e.stopImmediatePropagation();
								return false;
							}
						}
					}, this));
		},
		defaults : {
			// defines maximum number of root nodes (-1 means unlimited, -2 means disable max_children checking)
			max_children		: -1,
			// defines the maximum depth of the tree (-1 means unlimited, -2 means disable max_depth checking)
			max_depth			: -1,
			// defines valid node types for the root nodes
			valid_children		: "all",

			// where is the type stores (the rel attribute of the LI element)
			type_attr : "rel",
			// a list of types
			types : {
				// the default type
				"default" : {
					"max_children"	: -1,
					"max_depth"		: -1,
					"valid_children": "all"

					// Bound functions - you can bind any other function here (using boolean or function)
					//"select_node"	: true,
					//"open_node"	: true,
					//"close_node"	: true,
					//"create_node"	: true,
					//"delete_node"	: true
				}
			}
		},
		_fn : {
			_get_type : function (obj) {
				obj = this._get_node(obj);
				return (!obj || !obj.length) ? false : obj.attr(this._get_settings().types.type_attr) || "default";
			},
			set_type : function (str, obj) {
				obj = this._get_node(obj);
				return (!obj.length || !str) ? false : obj.attr(this._get_settings().types.type_attr, str);
			},
			_check : function (rule, obj, opts) {
				var v = false, t = this._get_type(obj), d = 0, _this = this, s = this._get_settings().types;
				if(obj === -1) { 
					if(!!s[rule]) { v = s[rule]; }
					else { return; }
				}
				else {
					if(t === false) { return; }
					if(!!s.types[t] && !!s.types[t][rule]) { v = s.types[t][rule]; }
					else if(!!s.types["default"] && !!s.types["default"][rule]) { v = s.types["default"][rule]; }
				}
				if($.isFunction(v)) { v = v.call(this, obj); }
				if(rule === "max_depth" && obj !== -1 && opts !== false && s.max_depth !== -2 && v !== 0) {
					// also include the node itself - otherwise if root node it is not checked
					this._get_node(obj).children("a:eq(0)").parentsUntil(".jstree","li").each(function (i) {
						// check if current depth already exceeds global tree depth
						if(s.max_depth !== -1 && s.max_depth - (i + 1) <= 0) { v = 0; return false; }
						d = (i === 0) ? v : _this._check(rule, this, false);
						// check if current node max depth is already matched or exceeded
						if(d !== -1 && d - (i + 1) <= 0) { v = 0; return false; }
						// otherwise - set the max depth to the current value minus current depth
						if(d >= 0 && (d - (i + 1) < v || v < 0) ) { v = d - (i + 1); }
						// if the global tree depth exists and it minus the nodes calculated so far is less than `v` or `v` is unlimited
						if(s.max_depth >= 0 && (s.max_depth - (i + 1) < v || v < 0) ) { v = s.max_depth - (i + 1); }
					});
				}
				return v;
			},
			check_move : function () {
				if(!this.__call_old()) { return false; }
				var m  = this._get_move(),
					s  = m.rt._get_settings().types,
					mc = m.rt._check("max_children", m.cr),
					md = m.rt._check("max_depth", m.cr),
					vc = m.rt._check("valid_children", m.cr),
					ch = 0, d = 1, t;

				if(vc === "none") { return false; } 
				if($.isArray(vc) && m.ot && m.ot._get_type) {
					m.o.each(function () {
						if($.inArray(m.ot._get_type(this), vc) === -1) { d = false; return false; }
					});
					if(d === false) { return false; }
				}
				if(s.max_children !== -2 && mc !== -1) {
					ch = m.cr === -1 ? this.get_container().children("> ul > li").not(m.o).length : m.cr.children("> ul > li").not(m.o).length;
					if(ch + m.o.length > mc) { return false; }
				}
				if(s.max_depth !== -2 && md !== -1) {
					d = 0;
					if(md === 0) { return false; }
					if(typeof m.o.d === "undefined") {
						// TODO: deal with progressive rendering and async when checking max_depth (how to know the depth of the moved node)
						t = m.o;
						while(t.length > 0) {
							t = t.find("> ul > li");
							d ++;
						}
						m.o.d = d;
					}
					if(md - m.o.d < 0) { return false; }
				}
				return true;
			},
			create_node : function (obj, position, js, callback, is_loaded, skip_check) {
				if(!skip_check && (is_loaded || this._is_loaded(obj))) {
					var p  = (position && position.match(/^before|after$/i) && obj !== -1) ? this._get_parent(obj) : this._get_node(obj),
						s  = this._get_settings().types,
						mc = this._check("max_children", p),
						md = this._check("max_depth", p),
						vc = this._check("valid_children", p),
						ch;
					if(!js) { js = {}; }
					if(vc === "none") { return false; } 
					if($.isArray(vc)) {
						if(!js.attr || !js.attr[s.type_attr]) { 
							if(!js.attr) { js.attr = {}; }
							js.attr[s.type_attr] = vc[0]; 
						}
						else {
							if($.inArray(js.attr[s.type_attr], vc) === -1) { return false; }
						}
					}
					if(s.max_children !== -2 && mc !== -1) {
						ch = p === -1 ? this.get_container().children("> ul > li").length : p.children("> ul > li").length;
						if(ch + 1 > mc) { return false; }
					}
					if(s.max_depth !== -2 && md !== -1 && (md - 1) < 0) { return false; }
				}
				return this.__call_old(true, obj, position, js, callback, is_loaded, skip_check);
			}
		}
	});
})(jQuery);
//*/

/* 
 * jsTree HTML data 1.0
 * The HTML data store. Datastores are build by replacing the `load_node` and `_is_loaded` functions.
 */
(function ($) {
	$.jstree.plugin("html_data", {
		__init : function () { 
			// this used to use html() and clean the whitespace, but this way any attached data was lost
			this.data.html_data.original_container_html = this.get_container().find(" > ul > li").clone(true);
			// remove white space from LI node - otherwise nodes appear a bit to the right
			this.data.html_data.original_container_html.find("li").andSelf().contents().filter(function() { return this.nodeType == 3; }).remove();
		},
		defaults : { 
			data : false,
			ajax : false,
			correct_state : true
		},
		_fn : {
			load_node : function (obj, s_call, e_call) { var _this = this; this.load_node_html(obj, function () { _this.__callback({ "obj" : obj }); s_call.call(this); }, e_call); },
			_is_loaded : function (obj) { 
				obj = this._get_node(obj); 
				return obj == -1 || !obj || !this._get_settings().html_data.ajax || obj.is(".jstree-open, .jstree-leaf") || obj.children("ul").children("li").size() > 0;
			},
			load_node_html : function (obj, s_call, e_call) {
				var d,
					s = this.get_settings().html_data,
					error_func = function () {},
					success_func = function () {};
				obj = this._get_node(obj);
				if(obj && obj !== -1) {
					if(obj.data("jstree-is-loading")) { return; }
					else { obj.data("jstree-is-loading",true); }
				}
				switch(!0) {
					case (!s.data && !s.ajax):
						if(!obj || obj == -1) {
							this.get_container()
								.children("ul").empty()
								.append(this.data.html_data.original_container_html)
								.find("li, a").filter(function () { return this.firstChild.tagName !== "INS"; }).prepend("<ins class='jstree-icon'>&#160;</ins>").end()
								.filter("a").children("ins:first-child").not(".jstree-icon").addClass("jstree-icon");
							this.clean_node();
						}
						if(s_call) { s_call.call(this); }
						break;
					case (!!s.data && !s.ajax) || (!!s.data && !!s.ajax && (!obj || obj === -1)):
						if(!obj || obj == -1) {
							d = $(s.data);
							if(!d.is("ul")) { d = $("<ul>").append(d); }
							this.get_container()
								.children("ul").empty().append(d.children())
								.find("li, a").filter(function () { return this.firstChild.tagName !== "INS"; }).prepend("<ins class='jstree-icon'>&#160;</ins>").end()
								.filter("a").children("ins:first-child").not(".jstree-icon").addClass("jstree-icon");
							this.clean_node();
						}
						if(s_call) { s_call.call(this); }
						break;
					case (!s.data && !!s.ajax) || (!!s.data && !!s.ajax && obj && obj !== -1):
						obj = this._get_node(obj);
						error_func = function (x, t, e) {
							var ef = this.get_settings().html_data.ajax.error; 
							if(ef) { ef.call(this, x, t, e); }
							if(obj != -1 && obj.length) {
								obj.children(".jstree-loading").removeClass("jstree-loading");
								obj.data("jstree-is-loading",false);
								if(t === "success" && s.correct_state) { obj.removeClass("jstree-open jstree-closed").addClass("jstree-leaf"); }
							}
							else {
								if(t === "success" && s.correct_state) { this.get_container().children("ul").empty(); }
							}
							if(e_call) { e_call.call(this); }
						};
						success_func = function (d, t, x) {
							var sf = this.get_settings().html_data.ajax.success; 
							if(sf) { d = sf.call(this,d,t,x) || d; }
							if(d == "") {
								return error_func.call(this, x, t, "");
							}
							if(d) {
								d = $(d);
								if(!d.is("ul")) { d = $("<ul>").append(d); }
								if(obj == -1 || !obj) { this.get_container().children("ul").empty().append(d.children()).find("li, a").filter(function () { return this.firstChild.tagName !== "INS"; }).prepend("<ins class='jstree-icon'>&#160;</ins>").end().filter("a").children("ins:first-child").not(".jstree-icon").addClass("jstree-icon"); }
								else { obj.children(".jstree-loading").removeClass("jstree-loading"); obj.append(d).find("li, a").filter(function () { return this.firstChild.tagName !== "INS"; }).prepend("<ins class='jstree-icon'>&#160;</ins>").end().filter("a").children("ins:first-child").not(".jstree-icon").addClass("jstree-icon"); obj.data("jstree-is-loading",false); }
								this.clean_node(obj);
								if(s_call) { s_call.call(this); }
							}
							else {
								if(obj && obj !== -1) {
									obj.children(".jstree-loading").removeClass("jstree-loading");
									obj.data("jstree-is-loading",false);
									if(s.correct_state) { 
										obj.removeClass("jstree-open jstree-closed").addClass("jstree-leaf"); 
										if(s_call) { s_call.call(this); } 
									}
								}
								else {
									if(s.correct_state) { 
										this.get_container().children("ul").empty();
										if(s_call) { s_call.call(this); } 
									}
								}
							}
						};
						s.ajax.context = this;
						s.ajax.error = error_func;
						s.ajax.success = success_func;
						if(!s.ajax.dataType) { s.ajax.dataType = "html"; }
						if($.isFunction(s.ajax.url)) { s.ajax.url = s.ajax.url.call(this, obj); }
						if($.isFunction(s.ajax.data)) { s.ajax.data = s.ajax.data.call(this, obj); }
						$.ajax(s.ajax);
						break;
				}
			}
		}
	});
	// include the HTML data plugin by default
	$.jstree.defaults.plugins.push("html_data");
})(jQuery);
//*/

/* 
 * jsTree themeroller plugin 1.0
 * Adds support for jQuery UI themes. Include this at the end of your plugins list, also make sure "themes" is not included.
 */
(function ($) {
	$.jstree.plugin("themeroller", {
		__init : function () {
			var s = this._get_settings().themeroller;
			this.get_container()
				.addClass("ui-widget-content")
				.delegate("a","mouseenter.jstree", function () {
					$(this).addClass(s.item_h);
				})
				.delegate("a","mouseleave.jstree", function () {
					$(this).removeClass(s.item_h);
				})
				.bind("open_node.jstree create_node.jstree", $.proxy(function (e, data) { 
						this._themeroller(data.rslt.obj);
					}, this))
				.bind("loaded.jstree refresh.jstree", $.proxy(function (e) {
						this._themeroller();
					}, this))
				.bind("close_node.jstree", $.proxy(function (e, data) {
						data.rslt.obj.children("ins").removeClass(s.opened).addClass(s.closed);
					}, this))
				.bind("select_node.jstree", $.proxy(function (e, data) {
						data.rslt.obj.children("a").addClass(s.item_a);
					}, this))
				.bind("deselect_node.jstree deselect_all.jstree", $.proxy(function (e, data) {
						this.get_container()
							.find("." + s.item_a).removeClass(s.item_a).end()
							.find(".jstree-clicked").addClass(s.item_a);
					}, this))
				.bind("move_node.jstree", $.proxy(function (e, data) {
						this._themeroller(data.rslt.o);
					}, this));
		},
		__destroy : function () {
			var s = this._get_settings().themeroller,
				c = [ "ui-icon" ];
			$.each(s, function (i, v) {
				v = v.split(" ");
				if(v.length) { c = c.concat(v); }
			});
			this.get_container()
				.removeClass("ui-widget-content")
				.find("." + c.join(", .")).removeClass(c.join(" "));
		},
		_fn : {
			_themeroller : function (obj) {
				var s = this._get_settings().themeroller;
				obj = !obj || obj == -1 ? this.get_container() : this._get_node(obj).parent();
				obj
					.find("li.jstree-closed > ins.jstree-icon").removeClass(s.opened).addClass("ui-icon " + s.closed).end()
					.find("li.jstree-open > ins.jstree-icon").removeClass(s.closed).addClass("ui-icon " + s.opened).end()
					.find("a").addClass(s.item)
						.children("ins.jstree-icon").addClass("ui-icon " + s.item_icon);
			}
		},
		defaults : {
			"opened" : "ui-icon-triangle-1-se",
			"closed" : "ui-icon-triangle-1-e",
			"item" : "ui-state-default",
			"item_h" : "ui-state-hover",
			"item_a" : "ui-state-active",
			"item_icon" : "ui-icon-folder-collapsed"
		}
	});
	$(function() {
		var css_string = '.jstree .ui-icon { overflow:visible; } .jstree a { padding:0 2px; }';
		$.vakata.css.add_sheet({ str : css_string });
	});
})(jQuery);
//*/

/* 
 * jsTree unique plugin 1.0
 * Forces different names amongst siblings (still a bit experimental)
 * NOTE: does not check language versions (it will not be possible to have nodes with the same title, even in different languages)
 */
(function ($) {
	$.jstree.plugin("unique", {
		__init : function () {
			this.get_container()
				.bind("before.jstree", $.proxy(function (e, data) { 
						var nms = [], res = true, p, t;
						if(data.func == "move_node") {
							// obj, ref, position, is_copy, is_prepared, skip_check
							if(data.args[4] === true) {
								if(data.args[0].o && data.args[0].o.length) {
									data.args[0].o.children("a").each(function () { nms.push($(this).text().replace(/^\s+/g,"")); });
									res = this._check_unique(nms, data.args[0].np.find("> ul > li").not(data.args[0].o));
								}
							}
						}
						if(data.func == "create_node") {
							// obj, position, js, callback, is_loaded
							if(data.args[4] || this._is_loaded(data.args[0])) {
								p = this._get_node(data.args[0]);
								if(data.args[1] && (data.args[1] === "before" || data.args[1] === "after")) {
									p = this._get_parent(data.args[0]);
									if(!p || p === -1) { p = this.get_container(); }
								}
								if(typeof data.args[2] === "string") { nms.push(data.args[2]); }
								else if(!data.args[2] || !data.args[2].data) { nms.push(this._get_settings().core.strings.new_node); }
								else { nms.push(data.args[2].data); }
								res = this._check_unique(nms, p.find("> ul > li"));
							}
						}
						if(data.func == "rename_node") {
							// obj, val
							nms.push(data.args[1]);
							t = this._get_node(data.args[0]);
							p = this._get_parent(t);
							if(!p || p === -1) { p = this.get_container(); }
							res = this._check_unique(nms, p.find("> ul > li").not(t));
						}
						if(!res) {
							e.stopPropagation();
							return false;
						}
					}, this));
		},
		_fn : { 
			_check_unique : function (nms, p) {
				var cnms = [];
				p.children("a").each(function () { cnms.push($(this).text().replace(/^\s+/g,"")); });
				if(!cnms.length || !nms.length) { return true; }
				cnms = cnms.sort().join(",,").replace(/(,|^)([^,]+)(,,\2)+(,|$)/g,"$1$2$4").replace(/,,+/g,",").replace(/,$/,"").split(",");
				if((cnms.length + nms.length) != cnms.concat(nms).sort().join(",,").replace(/(,|^)([^,]+)(,,\2)+(,|$)/g,"$1$2$4").replace(/,,+/g,",").replace(/,$/,"").split(",").length) {
					return false;
				}
				return true;
			},
			check_move : function () {
				if(!this.__call_old()) { return false; }
				var p = this._get_move(), nms = [];
				if(p.o && p.o.length) {
					p.o.children("a").each(function () { nms.push($(this).text().replace(/^\s+/g,"")); });
					return this._check_unique(nms, p.np.find("> ul > li").not(p.o));
				}
				return true;
			}
		}
	});
})(jQuery);
//*/

/*
 * jQuery Pines Notify (pnotify) Plugin 1.0.1
 *
 * Copyright (c) 2009 Hunter Perrin
 *
 * Licensed (along with all of Pines) under the GNU Affero GPL:
 *	  http://www.gnu.org/licenses/agpl.html
 */
(function(e){var q,m,k,n;e.extend({pnotify_remove_all:function(){var g=k.data("pnotify");g&&g.length&&e.each(g,function(){this.pnotify_remove&&this.pnotify_remove()})},pnotify_position_all:function(){m&&clearTimeout(m);m=null;var g=k.data("pnotify");if(g&&g.length){e.each(g,function(){var c=this.opts.pnotify_stack;if(c){if(!c.nextpos1)c.nextpos1=c.firstpos1;if(!c.nextpos2)c.nextpos2=c.firstpos2;if(!c.addpos2)c.addpos2=0;if(this.css("display")!="none"){var a,j,i={},b;switch(c.dir1){case "down":b="top";
break;case "up":b="bottom";break;case "left":b="right";break;case "right":b="left";break}a=parseInt(this.css(b));if(isNaN(a))a=0;if(typeof c.firstpos1=="undefined"){c.firstpos1=a;c.nextpos1=c.firstpos1}var h;switch(c.dir2){case "down":h="top";break;case "up":h="bottom";break;case "left":h="right";break;case "right":h="left";break}j=parseInt(this.css(h));if(isNaN(j))j=0;if(typeof c.firstpos2=="undefined"){c.firstpos2=j;c.nextpos2=c.firstpos2}if(c.dir1=="down"&&c.nextpos1+this.height()>n.height()||
c.dir1=="up"&&c.nextpos1+this.height()>n.height()||c.dir1=="left"&&c.nextpos1+this.width()>n.width()||c.dir1=="right"&&c.nextpos1+this.width()>n.width()){c.nextpos1=c.firstpos1;c.nextpos2+=c.addpos2+10;c.addpos2=0}if(c.animation&&c.nextpos2<j)switch(c.dir2){case "down":i.top=c.nextpos2+"px";break;case "up":i.bottom=c.nextpos2+"px";break;case "left":i.right=c.nextpos2+"px";break;case "right":i.left=c.nextpos2+"px";break}else this.css(h,c.nextpos2+"px");switch(c.dir2){case "down":case "up":if(this.outerHeight(true)>
c.addpos2)c.addpos2=this.height();break;case "left":case "right":if(this.outerWidth(true)>c.addpos2)c.addpos2=this.width();break}if(c.nextpos1)if(c.animation&&(a>c.nextpos1||i.top||i.bottom||i.right||i.left))switch(c.dir1){case "down":i.top=c.nextpos1+"px";break;case "up":i.bottom=c.nextpos1+"px";break;case "left":i.right=c.nextpos1+"px";break;case "right":i.left=c.nextpos1+"px";break}else this.css(b,c.nextpos1+"px");if(i.top||i.bottom||i.right||i.left)this.animate(i,{duration:500,queue:false});switch(c.dir1){case "down":case "up":c.nextpos1+=
this.height()+10;break;case "left":case "right":c.nextpos1+=this.width()+10;break}}}});e.each(g,function(){var c=this.opts.pnotify_stack;if(c){c.nextpos1=c.firstpos1;c.nextpos2=c.firstpos2;c.addpos2=0;c.animation=true}})}},pnotify:function(g){k||(k=e("body"));n||(n=e(window));var c,a;if(typeof g!="object"){a=e.extend({},e.pnotify.defaults);a.pnotify_text=g}else a=e.extend({},e.pnotify.defaults,g);if(a.pnotify_before_init)if(a.pnotify_before_init(a)===false)return null;var j,i=function(d,f){b.css("display",
"none");var o=document.elementFromPoint(d.clientX,d.clientY);b.css("display","block");var r=e(o),s=r.css("cursor");b.css("cursor",s!="auto"?s:"default");if(!j||j.get(0)!=o){if(j){p.call(j.get(0),"mouseleave",d.originalEvent);p.call(j.get(0),"mouseout",d.originalEvent)}p.call(o,"mouseenter",d.originalEvent);p.call(o,"mouseover",d.originalEvent)}p.call(o,f,d.originalEvent);j=r},b=e("<div />",{"class":"ui-pnotify "+a.pnotify_addclass,css:{display:"none"},mouseenter:function(d){a.pnotify_nonblock&&d.stopPropagation();
if(a.pnotify_mouse_reset&&c=="out"){b.stop(true);c="in";b.css("height","auto").animate({width:a.pnotify_width,opacity:a.pnotify_nonblock?a.pnotify_nonblock_opacity:a.pnotify_opacity},"fast")}a.pnotify_nonblock&&b.animate({opacity:a.pnotify_nonblock_opacity},"fast");a.pnotify_hide&&a.pnotify_mouse_reset&&b.pnotify_cancel_remove();a.pnotify_closer&&!a.pnotify_nonblock&&b.closer.show()},mouseleave:function(d){a.pnotify_nonblock&&d.stopPropagation();j=null;b.css("cursor","auto");a.pnotify_nonblock&&c!=
"out"&&b.animate({opacity:a.pnotify_opacity},"fast");a.pnotify_hide&&a.pnotify_mouse_reset&&b.pnotify_queue_remove();b.closer.hide();e.pnotify_position_all()},mouseover:function(d){a.pnotify_nonblock&&d.stopPropagation()},mouseout:function(d){a.pnotify_nonblock&&d.stopPropagation()},mousemove:function(d){if(a.pnotify_nonblock){d.stopPropagation();i(d,"onmousemove")}},mousedown:function(d){if(a.pnotify_nonblock){d.stopPropagation();d.preventDefault();i(d,"onmousedown")}},mouseup:function(d){if(a.pnotify_nonblock){d.stopPropagation();
d.preventDefault();i(d,"onmouseup")}},click:function(d){if(a.pnotify_nonblock){d.stopPropagation();i(d,"onclick")}},dblclick:function(d){if(a.pnotify_nonblock){d.stopPropagation();i(d,"ondblclick")}}});b.opts=a;if(a.pnotify_shadow&&!e.browser.msie)b.shadow_container=e("<div />",{"class":"ui-widget-shadow ui-corner-all ui-pnotify-shadow"}).prependTo(b);b.container=e("<div />",{"class":"ui-widget ui-widget-content ui-corner-all ui-pnotify-container "+(a.pnotify_type=="error"?"ui-state-error":"ui-state-highlight")}).appendTo(b);
b.pnotify_version="1.0.1";b.pnotify=function(d){var f=a;if(typeof d=="string")a.pnotify_text=d;else a=e.extend({},a,d);b.opts=a;if(a.pnotify_shadow!=f.pnotify_shadow)if(a.pnotify_shadow&&!e.browser.msie)b.shadow_container=e("<div />",{"class":"ui-widget-shadow ui-pnotify-shadow"}).prependTo(b);else b.children(".ui-pnotify-shadow").remove();if(a.pnotify_addclass===false)b.removeClass(f.pnotify_addclass);else a.pnotify_addclass!==f.pnotify_addclass&&b.removeClass(f.pnotify_addclass).addClass(a.pnotify_addclass);
if(a.pnotify_title===false)b.title_container.hide("fast");else a.pnotify_title!==f.pnotify_title&&b.title_container.html(a.pnotify_title).show(200);if(a.pnotify_text===false)b.text_container.hide("fast");else if(a.pnotify_text!==f.pnotify_text){if(a.pnotify_insert_brs)a.pnotify_text=a.pnotify_text.replace(/\n/g,"<br />");b.text_container.html(a.pnotify_text).show(200)}b.pnotify_history=a.pnotify_history;a.pnotify_type!=f.pnotify_type&&b.container.toggleClass("ui-state-error ui-state-highlight");if(a.pnotify_notice_icon!=
f.pnotify_notice_icon&&a.pnotify_type=="notice"||a.pnotify_error_icon!=f.pnotify_error_icon&&a.pnotify_type=="error"||a.pnotify_type!=f.pnotify_type){b.container.find("div.ui-pnotify-icon").remove();if(a.pnotify_error_icon&&a.pnotify_type=="error"||a.pnotify_notice_icon)e("<div />",{"class":"ui-pnotify-icon"}).append(e("<span />",{"class":a.pnotify_type=="error"?a.pnotify_error_icon:a.pnotify_notice_icon})).prependTo(b.container)}a.pnotify_width!==f.pnotify_width&&b.animate({width:a.pnotify_width});
a.pnotify_min_height!==f.pnotify_min_height&&b.container.animate({minHeight:a.pnotify_min_height});a.pnotify_opacity!==f.pnotify_opacity&&b.fadeTo(a.pnotify_animate_speed,a.pnotify_opacity);if(a.pnotify_hide)f.pnotify_hide||b.pnotify_queue_remove();else b.pnotify_cancel_remove();b.pnotify_queue_position();return b};b.pnotify_queue_position=function(){m&&clearTimeout(m);m=setTimeout(e.pnotify_position_all,10)};b.pnotify_display=function(){b.parent().length||b.appendTo(k);if(a.pnotify_before_open)if(a.pnotify_before_open(b)===
false)return;b.pnotify_queue_position();if(a.pnotify_animation=="fade"||a.pnotify_animation.effect_in=="fade")b.show().fadeTo(0,0).hide();else a.pnotify_opacity!=1&&b.show().fadeTo(0,a.pnotify_opacity).hide();b.animate_in(function(){a.pnotify_after_open&&a.pnotify_after_open(b);b.pnotify_queue_position();a.pnotify_hide&&b.pnotify_queue_remove()})};b.pnotify_remove=function(){if(b.timer){window.clearTimeout(b.timer);b.timer=null}if(a.pnotify_before_close)if(a.pnotify_before_close(b)===false)return;
b.animate_out(function(){if(a.pnotify_after_close)if(a.pnotify_after_close(b)===false)return;b.pnotify_queue_position();a.pnotify_remove&&b.detach()})};b.animate_in=function(d){c="in";var f;f=typeof a.pnotify_animation.effect_in!="undefined"?a.pnotify_animation.effect_in:a.pnotify_animation;if(f=="none"){b.show();d()}else if(f=="show")b.show(a.pnotify_animate_speed,d);else if(f=="fade")b.show().fadeTo(a.pnotify_animate_speed,a.pnotify_opacity,d);else if(f=="slide")b.slideDown(a.pnotify_animate_speed,
d);else if(typeof f=="function")f("in",d,b);else b.effect&&b.effect(f,{},a.pnotify_animate_speed,d)};b.animate_out=function(d){c="out";var f;f=typeof a.pnotify_animation.effect_out!="undefined"?a.pnotify_animation.effect_out:a.pnotify_animation;if(f=="none"){b.hide();d()}else if(f=="show")b.hide(a.pnotify_animate_speed,d);else if(f=="fade")b.fadeOut(a.pnotify_animate_speed,d);else if(f=="slide")b.slideUp(a.pnotify_animate_speed,d);else if(typeof f=="function")f("out",d,b);else b.effect&&b.effect(f,
{},a.pnotify_animate_speed,d)};b.pnotify_cancel_remove=function(){b.timer&&window.clearTimeout(b.timer)};b.pnotify_queue_remove=function(){b.pnotify_cancel_remove();b.timer=window.setTimeout(function(){b.pnotify_remove()},isNaN(a.pnotify_delay)?0:a.pnotify_delay)};b.closer=e("<div />",{"class":"ui-pnotify-closer",css:{cursor:"pointer",display:"none"},click:function(){b.pnotify_remove();b.closer.hide()}}).append(e("<span />",{"class":"ui-icon ui-icon-circle-close"})).appendTo(b.container);if(a.pnotify_error_icon&&
a.pnotify_type=="error"||a.pnotify_notice_icon)e("<div />",{"class":"ui-pnotify-icon"}).append(e("<span />",{"class":a.pnotify_type=="error"?a.pnotify_error_icon:a.pnotify_notice_icon})).appendTo(b.container);b.title_container=e("<div />",{"class":"ui-pnotify-title",html:a.pnotify_title}).appendTo(b.container);a.pnotify_title===false&&b.title_container.hide();if(a.pnotify_insert_brs&&typeof a.pnotify_text=="string")a.pnotify_text=a.pnotify_text.replace(/\n/g,"<br />");b.text_container=e("<div />",
{"class":"ui-pnotify-text",html:a.pnotify_text}).appendTo(b.container);a.pnotify_text===false&&b.text_container.hide();typeof a.pnotify_width=="string"&&b.css("width",a.pnotify_width);typeof a.pnotify_min_height=="string"&&b.container.css("min-height",a.pnotify_min_height);b.pnotify_history=a.pnotify_history;var h=k.data("pnotify");if(h==null||typeof h!="object")h=[];h=a.pnotify_stack.push=="top"?e.merge([b],h):e.merge(h,[b]);k.data("pnotify",h);a.pnotify_after_init&&a.pnotify_after_init(b);if(a.pnotify_history){var l=
k.data("pnotify_history");if(typeof l=="undefined"){l=e("<div />",{"class":"ui-pnotify-history-container ui-state-default ui-corner-bottom",mouseleave:function(){l.animate({top:"-"+q+"px"},{duration:100,queue:false})}}).append(e("<div />",{"class":"ui-pnotify-history-header",text:"Redisplay"})).append(e("<button />",{"class":"ui-pnotify-history-all ui-state-default ui-corner-all",text:"All",mouseenter:function(){e(this).addClass("ui-state-hover")},mouseleave:function(){e(this).removeClass("ui-state-hover")},
click:function(){e.each(h,function(){this.pnotify_history&&this.pnotify_display&&this.pnotify_display()});return false}})).append(e("<button />",{"class":"ui-pnotify-history-last ui-state-default ui-corner-all",text:"Last",mouseenter:function(){e(this).addClass("ui-state-hover")},mouseleave:function(){e(this).removeClass("ui-state-hover")},click:function(){for(var d=1;!h[h.length-d]||!h[h.length-d].pnotify_history||h[h.length-d].is(":visible");){if(h.length-d===0)return false;d++}d=h[h.length-d];
d.pnotify_display&&d.pnotify_display();return false}})).appendTo(k);q=e("<span />",{"class":"ui-pnotify-history-pulldown ui-icon ui-icon-grip-dotted-horizontal",mouseenter:function(){l.animate({top:"0"},{duration:100,queue:false})}}).appendTo(l).offset().top+2;l.css({top:"-"+q+"px"});k.data("pnotify_history",l)}}a.pnotify_stack.animation=false;b.pnotify_display();return b}});var t=/^on/,u=/^(dbl)?click$|^mouse(move|down|up|over|out|enter|leave)$|^contextmenu$/,v=/^(focus|blur|select|change|reset)$|^key(press|down|up)$/,
w=/^(scroll|resize|(un)?load|abort|error)$/,p=function(g,c){var a;g=g.toLowerCase();if(document.createEvent&&this.dispatchEvent){g=g.replace(t,"");if(g.match(u)){e(this).offset();a=document.createEvent("MouseEvents");a.initMouseEvent(g,c.bubbles,c.cancelable,c.view,c.detail,c.screenX,c.screenY,c.clientX,c.clientY,c.ctrlKey,c.altKey,c.shiftKey,c.metaKey,c.button,c.relatedTarget)}else if(g.match(v)){a=document.createEvent("UIEvents");a.initUIEvent(g,c.bubbles,c.cancelable,c.view,c.detail)}else if(g.match(w)){a=
document.createEvent("HTMLEvents");a.initEvent(g,c.bubbles,c.cancelable)}a&&this.dispatchEvent(a)}else{g.match(t)||(g="on"+g);a=document.createEventObject(c);this.fireEvent(g,a)}};e.pnotify.defaults={pnotify_title:false,pnotify_text:false,pnotify_addclass:"",pnotify_nonblock:false,pnotify_nonblock_opacity:0.2,pnotify_history:true,pnotify_width:"300px",pnotify_min_height:"16px",pnotify_type:"notice",pnotify_notice_icon:"ui-icon ui-icon-info",pnotify_error_icon:"ui-icon ui-icon-alert",pnotify_animation:"fade",
pnotify_animate_speed:"slow",pnotify_opacity:1,pnotify_shadow:false,pnotify_closer:true,pnotify_hide:true,pnotify_delay:8E3,pnotify_mouse_reset:true,pnotify_remove:true,pnotify_insert_brs:true,pnotify_stack:{dir1:"down",dir2:"left",push:"bottom"}}})(jQuery);


/*
    http://www.JSON.org/json2.js
    2010-08-25

    Public Domain.

    NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.

    See http://www.JSON.org/js.html


    This code should be minified before deployment.
    See http://javascript.crockford.com/jsmin.html

    USE YOUR OWN COPY. IT IS EXTREMELY UNWISE TO LOAD CODE FROM SERVERS YOU DO
    NOT CONTROL.


    This file creates a global JSON object containing two methods: stringify
    and parse.

        JSON.stringify(value, replacer, space)
            value       any JavaScript value, usually an object or array.

            replacer    an optional parameter that determines how object
                        values are stringified for objects. It can be a
                        function or an array of strings.

            space       an optional parameter that specifies the indentation
                        of nested structures. If it is omitted, the text will
                        be packed without extra whitespace. If it is a number,
                        it will specify the number of spaces to indent at each
                        level. If it is a string (such as '\t' or '&nbsp;'),
                        it contains the characters used to indent at each level.

            This method produces a JSON text from a JavaScript value.

            When an object value is found, if the object contains a toJSON
            method, its toJSON method will be called and the result will be
            stringified. A toJSON method does not serialize: it returns the
            value represented by the name/value pair that should be serialized,
            or undefined if nothing should be serialized. The toJSON method
            will be passed the key associated with the value, and this will be
            bound to the value

            For example, this would serialize Dates as ISO strings.

                Date.prototype.toJSON = function (key) {
                    function f(n) {
                        // Format integers to have at least two digits.
                        return n < 10 ? '0' + n : n;
                    }

                    return this.getUTCFullYear()   + '-' +
                         f(this.getUTCMonth() + 1) + '-' +
                         f(this.getUTCDate())      + 'T' +
                         f(this.getUTCHours())     + ':' +
                         f(this.getUTCMinutes())   + ':' +
                         f(this.getUTCSeconds())   + 'Z';
                };

            You can provide an optional replacer method. It will be passed the
            key and value of each member, with this bound to the containing
            object. The value that is returned from your method will be
            serialized. If your method returns undefined, then the member will
            be excluded from the serialization.

            If the replacer parameter is an array of strings, then it will be
            used to select the members to be serialized. It filters the results
            such that only members with keys listed in the replacer array are
            stringified.

            Values that do not have JSON representations, such as undefined or
            functions, will not be serialized. Such values in objects will be
            dropped; in arrays they will be replaced with null. You can use
            a replacer function to replace those with JSON values.
            JSON.stringify(undefined) returns undefined.

            The optional space parameter produces a stringification of the
            value that is filled with line breaks and indentation to make it
            easier to read.

            If the space parameter is a non-empty string, then that string will
            be used for indentation. If the space parameter is a number, then
            the indentation will be that many spaces.

            Example:

            text = JSON.stringify(['e', {pluribus: 'unum'}]);
            // text is '["e",{"pluribus":"unum"}]'


            text = JSON.stringify(['e', {pluribus: 'unum'}], null, '\t');
            // text is '[\n\t"e",\n\t{\n\t\t"pluribus": "unum"\n\t}\n]'

            text = JSON.stringify([new Date()], function (key, value) {
                return this[key] instanceof Date ?
                    'Date(' + this[key] + ')' : value;
            });
            // text is '["Date(---current time---)"]'


        JSON.parse(text, reviver)
            This method parses a JSON text to produce an object or array.
            It can throw a SyntaxError exception.

            The optional reviver parameter is a function that can filter and
            transform the results. It receives each of the keys and values,
            and its return value is used instead of the original value.
            If it returns what it received, then the structure is not modified.
            If it returns undefined then the member is deleted.

            Example:

            // Parse the text. Values that look like ISO date strings will
            // be converted to Date objects.

            myData = JSON.parse(text, function (key, value) {
                var a;
                if (typeof value === 'string') {
                    a =
/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/.exec(value);
                    if (a) {
                        return new Date(Date.UTC(+a[1], +a[2] - 1, +a[3], +a[4],
                            +a[5], +a[6]));
                    }
                }
                return value;
            });

            myData = JSON.parse('["Date(09/09/2001)"]', function (key, value) {
                var d;
                if (typeof value === 'string' &&
                        value.slice(0, 5) === 'Date(' &&
                        value.slice(-1) === ')') {
                    d = new Date(value.slice(5, -1));
                    if (d) {
                        return d;
                    }
                }
                return value;
            });


    This is a reference implementation. You are free to copy, modify, or
    redistribute.
*/

/*jslint evil: true, strict: false */

/*members "", "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", apply,
    call, charCodeAt, getUTCDate, getUTCFullYear, getUTCHours,
    getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join,
    lastIndex, length, parse, prototype, push, replace, slice, stringify,
    test, toJSON, toString, valueOf
*/


// Create a JSON object only if one does not already exist. We create the
// methods in a closure to avoid creating global variables.

if (!this.JSON) {
    this.JSON = {};
}

(function () {

    function f(n) {
        // Format integers to have at least two digits.
        return n < 10 ? '0' + n : n;
    }

    if (typeof Date.prototype.toJSON !== 'function') {

        Date.prototype.toJSON = function (key) {

            return isFinite(this.valueOf()) ?
                   this.getUTCFullYear()   + '-' +
                 f(this.getUTCMonth() + 1) + '-' +
                 f(this.getUTCDate())      + 'T' +
                 f(this.getUTCHours())     + ':' +
                 f(this.getUTCMinutes())   + ':' +
                 f(this.getUTCSeconds())   + 'Z' : null;
        };

        String.prototype.toJSON =
        Number.prototype.toJSON =
        Boolean.prototype.toJSON = function (key) {
            return this.valueOf();
        };
    }

    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        gap,
        indent,
        meta = {    // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        },
        rep;


    function quote(string) {

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe escape
// sequences.

        escapable.lastIndex = 0;
        return escapable.test(string) ?
            '"' + string.replace(escapable, function (a) {
                var c = meta[a];
                return typeof c === 'string' ? c :
                    '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
            }) + '"' :
            '"' + string + '"';
    }


    function str(key, holder) {

// Produce a string from holder[key].

        var i,          // The loop counter.
            k,          // The member key.
            v,          // The member value.
            length,
            mind = gap,
            partial,
            value = holder[key];

// If the value has a toJSON method, call it to obtain a replacement value.

        if (value && typeof value === 'object' &&
                typeof value.toJSON === 'function') {
            value = value.toJSON(key);
        }

// If we were called with a replacer function, then call the replacer to
// obtain a replacement value.

        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }

// What happens next depends on the value's type.

        switch (typeof value) {
        case 'string':
            return quote(value);

        case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

            return isFinite(value) ? String(value) : 'null';

        case 'boolean':
        case 'null':

// If the value is a boolean or null, convert it to a string. Note:
// typeof null does not produce 'null'. The case is included here in
// the remote chance that this gets fixed someday.

            return String(value);

// If the type is 'object', we might be dealing with an object or an array or
// null.

        case 'object':

// Due to a specification blunder in ECMAScript, typeof null is 'object',
// so watch out for that case.

            if (!value) {
                return 'null';
            }

// Make an array to hold the partial results of stringifying this object value.

            gap += indent;
            partial = [];

// Is the value an array?

            if (Object.prototype.toString.apply(value) === '[object Array]') {

// The value is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                length = value.length;
                for (i = 0; i < length; i += 1) {
                    partial[i] = str(i, value) || 'null';
                }

// Join all of the elements together, separated with commas, and wrap them in
// brackets.

                v = partial.length === 0 ? '[]' :
                    gap ? '[\n' + gap +
                            partial.join(',\n' + gap) + '\n' +
                                mind + ']' :
                          '[' + partial.join(',') + ']';
                gap = mind;
                return v;
            }

// If the replacer is an array, use it to select the members to be stringified.

            if (rep && typeof rep === 'object') {
                length = rep.length;
                for (i = 0; i < length; i += 1) {
                    k = rep[i];
                    if (typeof k === 'string') {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            } else {

// Otherwise, iterate through all of the keys in the object.

                for (k in value) {
                    if (Object.hasOwnProperty.call(value, k)) {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            }

// Join all of the member texts together, separated with commas,
// and wrap them in braces.

            v = partial.length === 0 ? '{}' :
                gap ? '{\n' + gap + partial.join(',\n' + gap) + '\n' +
                        mind + '}' : '{' + partial.join(',') + '}';
            gap = mind;
            return v;
        }
    }

// If the JSON object does not yet have a stringify method, give it one.

    if (typeof JSON.stringify !== 'function') {
        JSON.stringify = function (value, replacer, space) {

// The stringify method takes a value and an optional replacer, and an optional
// space parameter, and returns a JSON text. The replacer can be a function
// that can replace values, or an array of strings that will select the keys.
// A default replacer method can be provided. Use of the space parameter can
// produce text that is more easily readable.

            var i;
            gap = '';
            indent = '';

// If the space parameter is a number, make an indent string containing that
// many spaces.

            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    indent += ' ';
                }

// If the space parameter is a string, it will be used as the indent string.

            } else if (typeof space === 'string') {
                indent = space;
            }

// If there is a replacer, it must be a function or an array.
// Otherwise, throw an error.

            rep = replacer;
            if (replacer && typeof replacer !== 'function' &&
                    (typeof replacer !== 'object' ||
                     typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }

// Make a fake root object containing our value under the key of ''.
// Return the result of stringifying the value.

            return str('', {'': value});
        };
    }


// If the JSON object does not yet have a parse method, give it one.

    if (typeof JSON.parse !== 'function') {
        JSON.parse = function (text, reviver) {

// The parse method takes a text and an optional reviver function, and returns
// a JavaScript value if the text is a valid JSON text.

            var j;

            function walk(holder, key) {

// The walk method is used to recursively walk the resulting structure so
// that modifications can be made.

                var k, v, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }


// Parsing happens in four stages. In the first stage, we replace certain
// Unicode characters with escape sequences. JavaScript handles many characters
// incorrectly, either silently deleting them, or treating them as line endings.

            text = String(text);
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return '\\u' +
                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }

// In the second stage, we run the text against regular expressions that look
// for non-JSON patterns. We are especially concerned with '()' and 'new'
// because they can cause invocation, and '=' because it can cause mutation.
// But just to be safe, we want to reject all unexpected forms.

// We split the second stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

            if (/^[\],:{}\s]*$/
.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
.replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']')
.replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the third stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                j = eval('(' + text + ')');

// In the optional fourth stage, we recursively walk the new structure, passing
// each name/value pair to a reviver function for possible transformation.

                return typeof reviver === 'function' ?
                    walk({'': j}, '') : j;
            }

// If the text is not JSON parseable, then a SyntaxError is thrown.

            throw new SyntaxError('JSON.parse');
        };
    }
}());


// This source code is free for use in the public domain.
// NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.

// http://code.google.com/p/json-sans-eval/

/**
 * Parses a string of well-formed JSON text.
 *
 * If the input is not well-formed, then behavior is undefined, but it is
 * deterministic and is guaranteed not to modify any object other than its
 * return value.
 *
 * This does not use `eval` so is less likely to have obscure security bugs than
 * json2.js.
 * It is optimized for speed, so is much faster than json_parse.js.
 *
 * This library should be used whenever security is a concern (when JSON may
 * come from an untrusted source), speed is a concern, and erroring on malformed
 * JSON is *not* a concern.
 *
 *                      Pros                   Cons
 *                    +-----------------------+-----------------------+
 * json_sans_eval.js  | Fast, secure          | Not validating        |
 *                    +-----------------------+-----------------------+
 * json_parse.js      | Validating, secure    | Slow                  |
 *                    +-----------------------+-----------------------+
 * json2.js           | Fast, some validation | Potentially insecure  |
 *                    +-----------------------+-----------------------+
 *
 * json2.js is very fast, but potentially insecure since it calls `eval` to
 * parse JSON data, so an attacker might be able to supply strange JS that
 * looks like JSON, but that executes arbitrary javascript.
 * If you do have to use json2.js with untrusted data, make sure you keep
 * your version of json2.js up to date so that you get patches as they're
 * released.
 *
 * @param {string} json per RFC 4627
 * @param {function (this:Object, string, *):*} opt_reviver optional function
 *     that reworks JSON objects post-parse per Chapter 15.12 of EcmaScript3.1.
 *     If supplied, the function is called with a string key, and a value.
 *     The value is the property of 'this'.  The reviver should return
 *     the value to use in its place.  So if dates were serialized as
 *     {@code { "type": "Date", "time": 1234 }}, then a reviver might look like
 *     {@code
 *     function (key, value) {
 *       if (value && typeof value === 'object' && 'Date' === value.type) {
 *         return new Date(value.time);
 *       } else {
 *         return value;
 *       }
 *     }}.
 *     If the reviver returns {@code undefined} then the property named by key
 *     will be deleted from its container.
 *     {@code this} is bound to the object containing the specified property.
 * @return {Object|Array}
 * @author Mike Samuel <mikesamuel@gmail.com>
 */
var jsonParse = (function () {
  var number
      = '(?:-?\\b(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?\\b)';
  var oneChar = '(?:[^\\0-\\x08\\x0a-\\x1f\"\\\\]'
      + '|\\\\(?:[\"/\\\\bfnrt]|u[0-9A-Fa-f]{4}))';
  var string = '(?:\"' + oneChar + '*\")';

  // Will match a value in a well-formed JSON file.
  // If the input is not well-formed, may match strangely, but not in an unsafe
  // way.
  // Since this only matches value tokens, it does not match whitespace, colons,
  // or commas.
  var jsonToken = new RegExp(
      '(?:false|true|null|[\\{\\}\\[\\]]'
      + '|' + number
      + '|' + string
      + ')', 'g');

  // Matches escape sequences in a string literal
  var escapeSequence = new RegExp('\\\\(?:([^u])|u(.{4}))', 'g');

  // Decodes escape sequences in object literals
  var escapes = {
    '"': '"',
    '/': '/',
    '\\': '\\',
    'b': '\b',
    'f': '\f',
    'n': '\n',
    'r': '\r',
    't': '\t'
  };
  function unescapeOne(_, ch, hex) {
    return ch ? escapes[ch] : String.fromCharCode(parseInt(hex, 16));
  }

  // A non-falsy value that coerces to the empty string when used as a key.
  var EMPTY_STRING = new String('');
  var SLASH = '\\';

  // Constructor to use based on an open token.
  var firstTokenCtors = { '{': Object, '[': Array };

  var hop = Object.hasOwnProperty;

  return function (json, opt_reviver) {
    // Split into tokens
    var toks = json.match(jsonToken);
    // Construct the object to return
    var result;
    var tok = toks[0];
    var topLevelPrimitive = false;
    if ('{' === tok) {
      result = {};
    } else if ('[' === tok) {
      result = [];
    } else {
      // The RFC only allows arrays or objects at the top level, but the JSON.parse
      // defined by the EcmaScript 5 draft does allow strings, booleans, numbers, and null
      // at the top level.
      result = [];
      topLevelPrimitive = true;
    }

    // If undefined, the key in an object key/value record to use for the next
    // value parsed.
    var key;
    // Loop over remaining tokens maintaining a stack of uncompleted objects and
    // arrays.
    var stack = [result];
    for (var i = 1 - topLevelPrimitive, n = toks.length; i < n; ++i) {
      tok = toks[i];

      var cont;
      switch (tok.charCodeAt(0)) {
        default:  // sign or digit
          cont = stack[0];
          cont[key || cont.length] = +(tok);
          key = void 0;
          break;
        case 0x22:  // '"'
          tok = tok.substring(1, tok.length - 1);
          if (tok.indexOf(SLASH) !== -1) {
            tok = tok.replace(escapeSequence, unescapeOne);
          }
          cont = stack[0];
          if (!key) {
            if (cont instanceof Array) {
              key = cont.length;
            } else {
              key = tok || EMPTY_STRING;  // Use as key for next value seen.
              break;
            }
          }
          cont[key] = tok;
          key = void 0;
          break;
        case 0x5b:  // '['
          cont = stack[0];
          stack.unshift(cont[key || cont.length] = []);
          key = void 0;
          break;
        case 0x5d:  // ']'
          stack.shift();
          break;
        case 0x66:  // 'f'
          cont = stack[0];
          cont[key || cont.length] = false;
          key = void 0;
          break;
        case 0x6e:  // 'n'
          cont = stack[0];
          cont[key || cont.length] = null;
          key = void 0;
          break;
        case 0x74:  // 't'
          cont = stack[0];
          cont[key || cont.length] = true;
          key = void 0;
          break;
        case 0x7b:  // '{'
          cont = stack[0];
          stack.unshift(cont[key || cont.length] = {});
          key = void 0;
          break;
        case 0x7d:  // '}'
          stack.shift();
          break;
      }
    }
    // Fail if we've got an uncompleted object.
    if (topLevelPrimitive) {
      if (stack.length !== 1) { throw new Error(); }
      result = result[0];
    } else {
      if (stack.length) { throw new Error(); }
    }

    if (opt_reviver) {
      // Based on walk as implemented in http://www.json.org/json2.js
      var walk = function (holder, key) {
        var value = holder[key];
        if (value && typeof value === 'object') {
          var toDelete = null;
          for (var k in value) {
            if (hop.call(value, k) && value !== holder) {
              // Recurse to properties first.  This has the effect of causing
              // the reviver to be called on the object graph depth-first.

              // Since 'this' is bound to the holder of the property, the
              // reviver can access sibling properties of k including ones
              // that have not yet been revived.

              // The value returned by the reviver is used in place of the
              // current value of property k.
              // If it returns undefined then the property is deleted.
              var v = walk(value, k);
              if (v !== void 0) {
                value[k] = v;
              } else {
                // Deleting properties inside the loop has vaguely defined
                // semantics in ES3 and ES3.1.
                if (!toDelete) { toDelete = []; }
                toDelete.push(k);
              }
            }
          }
          if (toDelete) {
            for (var i = toDelete.length; --i >= 0;) {
              delete value[toDelete[i]];
            }
          }
        }
        return opt_reviver.call(holder, key, value);
      };
      result = walk({ '': result }, '');
    }

    return result;
  };
})();


/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};

var pnotify_stack = {"dir1": "up", "dir2": "left", "firstpos1": 15, "firstpos2": 30};

QCD.MessagesController = function() {

	this.clearMessager = function() {
		$.pnotify_remove_all()
	}
	
	this.addMessage = function(message) { // type = [info|error|success]
		
		type = message.type.toLowerCase();
		if (type == "failure") {
			type = "error";
		}
		
		$.pnotify({
			pnotify_title: message.title,
			pnotify_text: message.content,
			pnotify_stack: pnotify_stack,
			pnotify_history: false,
			pnotify_width: "300px",
			pnotify_type: type,
			pnotify_addclass: type == 'success' ? 'ui-state-success' : '',
			pnotify_notice_icon: type == 'success' ? 'ui-icon ui-icon-success' : 'ui-icon ui-icon-notify',
			pnotify_error_icon: 'ui-icon ui-icon-error',
			pnotify_opacity: .9,
			pnotify_delay: 4000,
			pnotify_hide: true // type == 'error' ? false : true
		});
		
	}

}



/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};

QCD.WindowController = function(_menuStructure) {
	
	var iframe = null;
	
	var loadingIndicator;
	
	var statesStack = new Array();
	
	var serializationObjectToInsert = null;
	
	var currentPage = null;
	
	var messagesController = new QCD.MessagesController();
	
	var menuStructure = _menuStructure;
	var menuController
	
	function constructor(_this) {
		iframe = $("#mainPageIframe");
		loadingIndicator = $("#loadingIndicator");
		loadingIndicator.hide();
		iframe.load(function() {
			onIframeLoad(this);
		});
		$(window).bind('resize', updateSize);
		
		menuController = new QCD.menu.MenuController(menuStructure, _this);
		
		updateSize();
	}
	
	this.addMessage = function(type, content) {
		messagesController.addMessage(type, content);
	}
	
	this.performLogout = function() {
		QCD.info("logout");
		window.location = "j_spring_security_logout";
	}
	
	this.goToPage = function(url, serializationObject, isPage) {
		var stateObject = {
			url: iframe.attr('src'),
			serializationObject: serializationObject
		};
		statesStack.push(stateObject);
		if (isPage) {
			currentPage = "page/"+url;	
		} else {
			currentPage = url;
		}
		performGoToPage(currentPage);
	}
	
	this.goBack = function() {
		var stateObject = statesStack.pop();
		serializationObjectToInsert = stateObject.serializationObject;
		currentPage = stateObject.url;
		performGoToPage(currentPage);
	}
	
	this.goToLastPage = function() {
		performGoToPage(currentPage);
	}
	
	this.goToMenuPosition = function(position) {
		menuController.goToMenuPosition(position);
	}
	
	this.hasMenuPosition = function(position) {
		return menuController.hasMenuPosition(position);
	}
	
	this.onSessionExpired = function(serializationObject) {
		serializationObjectToInsert = serializationObject;
		performGoToPage("login.html");
	}
	
	this.restoreMenuState = function() {
		menuController.restoreState();
	}
	
	this.canChangePage = function() {
		try {
			if (iframe[0].contentWindow.canClose) {
				return iframe[0].contentWindow.canClose();
			}
		} catch (e) {
		}
		return true;
	}
	
	this.onMenuClicked = function(pageName) {
		currentPage = pageName;
		statesStack = new Array();
		performGoToPage(currentPage);
	}
	
	function performGoToPage(url) {
		loadingIndicator.show();
		if (url.search("://") <= 0) {
			if (url.indexOf("?") == -1) {
				url += "?iframe=true";
			} else {
				if (url.charAt(url.length - 1) == '?') {
					url += "iframe=true";
				} else {
					url += "&iframe=true";
				}
			}
		}
		iframe.attr('src', url);
	}
	
	function onIframeLoad() {
		try {
			if (iframe[0].contentWindow.init) {
				iframe[0].contentWindow.init(serializationObjectToInsert);
				serializationObjectToInsert = null;
			}
		} catch (e) {
		}
		loadingIndicator.hide();
	}
	
	function updateSize() {
		var width = $(document).width();
		var margin = Math.round(width * 0.02);
		var innerWidth = Math.round(width - 2 * margin);
		$("#q_menu_row1").width(innerWidth);
		$("#secondLevelMenu").width(innerWidth);
	}
	this.updateSize = updateSize;
	
	constructor(this);
	
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.menu = QCD.menu || {};

QCD.menu.MenuController = function(menuStructure, _windowController) {
	
	var windowController = _windowController;
	
	var firstLevelElement = $("#firstLevelMenu");
	var secondLevelElement = $("#secondLevelMenu");
	
	var previousActive = new Object();
	previousActive.first = null;
	previousActive.second = null;
	
	var currentActive = new Object();
	currentActive.first = null;
	currentActive.second = null;
	
	function constructor(menuStructure) {
		model = new QCD.menu.MenuModel(menuStructure.menuItems);
		
		var menuContentElement = $("<ul>").addClass("q_row1");
		var q_menu_row1 = $("<div>").attr("id", "q_menu_row1");
			q_menu_row1.append(menuContentElement);
		var q_row1 = $("<div>").attr("id", "q_row1");
			q_row1.append(q_menu_row1);
		var q_row1_out = $("<div>").attr("id", "q_row1_out");
			q_row1_out.append(q_row1);
		firstLevelElement.append(q_row1_out);
		
		for (var i in model.items) {
			var item = model.items[i];
			
			var firstLevelButton = $("<li>").html("<a href='#'><span>"+item.label+"</span></a>").attr("id", "firstLevelButton_"+item.name);
			menuContentElement.append(firstLevelButton);
			item.element = firstLevelButton;
			
			firstLevelButton.click(function(e) {
				onTopItemClick($(this), e);
			});
		}
		previousActive.first = model.selectedItem;
		model.selectedItem.element.addClass("path");
		previousActive.second = model.selectedItem.selectedItem;
		
		updateState();
		
		changePage(model.selectedItem.selectedItem.page);
	}
	
	
	
	function onTopItemClick(itemElement, e) {
		itemElement.children().blur();
		
		var buttonName = itemElement.attr("id").substring(17);
		var topItem = model.itemsMap[buttonName];
		
		model.selectedItem = topItem;
		if (model.selectedItem.selectedItem) {
			currentActive.second = model.selectedItem.selectedItem
		}
		
		updateState();
	}
	
	function onBottomItemClick(itemElement) {
		itemElement.children().blur();
		
		if (! canChangePage()) {
			return;
		}
		
		var buttonName = itemElement.attr("id").substring(18);
		var selectedItem = model.selectedItem.itemsMap[buttonName];
		
		model.selectedItem.selectedItem = selectedItem;
		
		previousActive.first.element.removeClass("path");
		previousActive.first = model.selectedItem;
		previousActive.second = model.selectedItem.selectedItem;
		previousActive.first.element.addClass("path");
		
		updateState();
		
		changePage(model.selectedItem.selectedItem.page);
	}
	
	this.goToMenuPosition = function(position) {
		var menuParts = position.split(".");
		
		var topItem = model.itemsMap[menuParts[0]];
		var bottomItem = topItem.itemsMap[menuParts[0]+"_"+menuParts[1]];
		
		model.selectedItem.element.removeClass("path");
		
		topItem.selectedItem = bottomItem;
		previousActive.first = topItem;
		previousActive.second = bottomItem;
		model.selectedItem = topItem;
		
		updateState();
		changePage(model.selectedItem.selectedItem.page);
	}
	
	this.hasMenuPosition = function(position) {
		var menuParts = position.split(".");
		var topItem = model.itemsMap[menuParts[0]];
		if (topItem == null) {
			return false;
		}
		var bottomItem = topItem.itemsMap[menuParts[0]+"_"+menuParts[1]];
		if (bottomItem == null) {
			return false;
		}
		return true;
	}
	
	this.restoreState = function() {
		model.selectedItem = previousActive.first;
		if (previousActive.second) {
			model.selectedItem.selectedItem = previousActive.second;
		}
		updateState();
	}
	
	function updateState() {
		if (currentActive.first != model.selectedItem) {
			if (currentActive.first) {
				currentActive.first.element.removeClass("activ");
				currentActive.first.selectedItem = null;
			}
			currentActive.first = model.selectedItem;
			currentActive.first.element.addClass("activ");
			
			updateSecondLevel();
			
		} else {
			if (currentActive.second != model.selectedItem.selectedItem) {
				if (currentActive.second) {
					currentActive.second.element.removeClass("activ");
				}
				currentActive.second = model.selectedItem.selectedItem;
				if (currentActive.second) {
					currentActive.second.element.addClass("activ");
				}
			}
		}
	}
	
	function updateSecondLevel() {
		secondLevelElement.children().remove();
		
		var menuContentElement = $("<ul>").addClass("q_row2");
		var q_menu_row2 = $("<div>").attr("id", "q_menu_row2");
			q_menu_row2.append(menuContentElement);
		var q_row2_out = $("<div>").attr("id", "q_row2_out");
			q_row2_out.append(q_menu_row2);
		secondLevelElement.append(q_row2_out);
		
		for (var i in model.selectedItem.items) {
			var secondLevelItem = model.selectedItem.items[i];
			var secondLevelButton = $("<li>").html("<a href='#'><span>"+secondLevelItem.label+"</span></a>").attr("id", "secondLevelButton_"+secondLevelItem.name);
			menuContentElement.append(secondLevelButton);
			secondLevelItem.element = secondLevelButton;

			secondLevelButton.click(function() {
				onBottomItemClick($(this));
			});
			
			if (previousActive.second && previousActive.second.name == secondLevelItem.name) {
				secondLevelItem.element.addClass("activ");
				currentActive.second = secondLevelItem;
			}
			
		}
	} 
	
	function changePage(page) {
		windowController.onMenuClicked(page);
	}
	
	function canChangePage() {
		return windowController.canChangePage();
	}
	
	constructor(menuStructure);
}


/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.menu = QCD.menu || {};

QCD.menu.MenuModel = function(menuItems) {
	
	this.selectedItem = null;
	
	this.items = new Array();
	this.itemsMap = new Object();
	for (var i in menuItems) {
		var button = new QCD.menu.FirstButton(menuItems[i]);
		this.items.push(button);
		this.itemsMap[button.name] = button;
		if (! this.selectedItem) {
			this.selectedItem = button;
			button.selectedItem = button.items[0]; 
		}
	}
}

QCD.menu.FirstButton = function(menuItem) {
	this.name = menuItem.name;
	this.label = menuItem.label;
	
	this.element = null;
	
	this.selectedItem = null;
	
	this.itemsMap = new Object();
	this.items = new Array();
	for (var i in menuItem.items) {
		var secondButton = new QCD.menu.SecondButton(menuItem.items[i], this);
		this.itemsMap[secondButton.name] = secondButton;
		this.items.push(secondButton);
	}
}

QCD.menu.SecondButton = function(menuItem, firstButton) {
	this.name = firstButton.name+"_"+menuItem.name;
	this.label = menuItem.label;
	
	this.page = menuItem.page;
	
	this.element = null;
	
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCDConnector = {};

QCDConnector.windowName = null;
QCDConnector.mainController = null;

QCDConnector.sendGet = function(type, parameters, responseFunction, errorFunction) {
	if (!QCDConnector.windowName) {
		throw("no window name defined in conector");
	}
	var url = QCDConnector.windowName+"/"+type+".html";
	
	if (parameters) {
		var first = true;
		for (var i in parameters) {
			if (first) {
				url+="?";
			} else {
				url += "&"
			}
			url += i+"="+parameters[i];
			first = false;
		}
	}
	
	$.ajax({
		url: url,
		type: 'GET',
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		complete: function(XMLHttpRequest, textStatus) {
			if (XMLHttpRequest.status == 200) {
				var responseText = $.trim(XMLHttpRequest.responseText); 
				if (responseText == "sessionExpired") {
					QCDConnector.mainController.onSessionExpired();
					return;
				}
				if (responseText.substring(0, 20) == "<![CDATA[ERROR PAGE:") {
					var message = responseText.substring(20, responseText.search("]]>"));
					QCDConnector.showErrorMessage(message);
					if (errorFunction) {
						errorFunction(message);
					}
					return;
				}
				if (responseFunction) {
					if (responseText != "") {
						var response = JSON.parse(responseText);
						responseFunction(response);
					} else {
						responseFunction(null);
					}
				}
			} else {
				QCDConnector.showErrorMessage("connection error: "+XMLHttpRequest.statusText);
				if (errorFunction) {
					errorFunction(XMLHttpRequest.statusText);
				}
			}
		}
	});
}

QCDConnector.sendPost = function(parameters, responseFunction, errorFunction) {
	if (!QCDConnector.windowName) {
		throw("no window name defined in conector");
	}
	var url = QCDConnector.windowName+".html";
	
	$.ajax({
		url: url,
		type: 'POST',
		data: parameters,
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		complete: function(XMLHttpRequest, textStatus) {
			if (XMLHttpRequest.status == 200) {
				var responseText = $.trim(XMLHttpRequest.responseText); 
				if (responseText == "sessionExpired") {
					QCDConnector.mainController.onSessionExpired();
					return;
				}
				//alert(responseText);
				if (responseText.substring(0, 20) == "<![CDATA[ERROR PAGE:") {
					var message = responseText.substring(20, responseText.search("]]>"));
					QCDConnector.showErrorMessage(message);
					if (errorFunction) {
						errorFunction(message);
					}
					return;
				}
				if (responseFunction) {
					if (responseText != "") {
						var response = JSON.parse(responseText);
						responseFunction(response);
					} else {
						responseFunction(null);
					}
				}
			} else {
				QCDConnector.showErrorMessage("connection error: "+XMLHttpRequest.statusText);
				if (errorFunction) {
					errorFunction(XMLHttpRequest.statusText);
				}
			}
		}
	});
}

QCDConnector.showErrorMessage = function(messageContent) {
	QCDConnector.mainController.showMessage({
		type: "failure",
		content: messageContent
	});
}


/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};

QCD.info = function(msg) {
	if (window.console && window.console.info) {
		window.console.info(msg);
	}
};

QCD.debug = function(msg) {
	if (window.console && window.console.debug) {
		window.console.debug(msg);
	}
};

QCD.error = function(msg) {
	if (window.console && window.console.error) {
		window.console.error(msg);
	}
};

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCDOptions = {};

QCDOptions.getElementOptions = function(elementName) {
	var optionsElement = $($("#"+elementName+" .element_options")[0]);
	if (!optionsElement.html() || $.trim(optionsElement.html()) == "") {
		var options = new Object();
	} else {
		var options = jsonParse(optionsElement.html());
	}
	optionsElement.remove();
	return options;
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCDPageConstructor = {};

QCDPageConstructor.getChildrenComponents = function(elements, mainController) {
	var components = new Object();
	elements.each(function(i,e) {
		var element = $(e);
		if (element.hasClass("component")) {
			var component = null;
			
			
			var elementFullName = element.attr('id');
			var elementSearchName = elementFullName.replace(/\./g,"\\.");
			var elementName = elementFullName.split(".")[elementFullName.split(".").length - 1];
			
			var jsObjectElement = $("#"+elementSearchName+" > .element_js_object");
			var jsObjectClassName = $.trim(jsObjectElement.html());
			jsObjectElement.remove();
			
			component = eval("new "+jsObjectClassName+"(element, mainController);");
			
//			var elementName = elementFullName.split("-")[elementFullName.split("-").length - 1];
//			if (element.hasClass("component_container_window")) {
//				component = new QCD.components.containers.Window(element, mainController);
//			} if (element.hasClass("component_container_form")) {
//				component = new QCD.components.containers.Form(element, mainController);
//			} else if (element.hasClass("component_element_grid")) {
//				component = new QCD.components.elements.Grid(element, mainController);
//			} else if (element.hasClass("component_element_textInput")) {
//				component = new QCD.components.elements.TextInput(element, mainController);
//			} else if (element.hasClass("component_element_textArea")) {
//				component = new QCD.components.elements.TextArea(element, mainController);
//			} else if (element.hasClass("component_element_passwordInput")) {
//				component = new QCD.components.elements.PasswordInput(element, mainController);
//			} else if (element.hasClass("component_element_dynamicComboBox")) {
//				component = new QCD.components.elements.DynamicComboBox(element, mainController);
//			} else if (element.hasClass("component_element_entityComboBox")) {
//				component = new QCD.components.elements.EntityComboBox(element, mainController);
//			} else if (element.hasClass("component_element_lookup")) {
//				component = new QCD.components.elements.Lookup(element, mainController);
//			} else if (element.hasClass("component_element_checkbox")) {
//				component = new QCD.components.elements.CheckBox(element, mainController);
//			} else if (element.hasClass("component_element_linkButton")) {
//				component = new QCD.components.elements.LinkButton(element, mainController);
//			} else if (element.hasClass("component_element_tree")) {
//				component = new QCD.components.elements.Tree(element, mainController);
//			} else if (element.hasClass("component_element_calendar")) {
//				component = new QCD.components.elements.Calendar(element, mainController);
//			}
//			
//			if (! component) {
//				component = new QCD.components.elements.StaticComponent(element, mainController);
//			}
			
			components[elementName] = component;
		}
	});
	return components;
}


/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCDSerializator = {};

QCDSerializator.serializeForm = function(form) {
	var a = form.serializeArray();
	var o = {};
	$.each(a, function() {
		if (/.*\[.*\]/.test(this.name)) {
			var objectName = this.name.substring(0, this.name.search(/\[/));
			var fieldName = this.name.substring(this.name.search(/\[/)+1, this.name.search(/\]/));
			if (! o[objectName]) {
				o[objectName] = new Object();
			}
			o[objectName][fieldName] = this.value || '';
		} else {
			o[this.name] = this.value || '';
		}
    });
	return o;

};

QCDSerializator.equals = function(u, v) {
	if (u == null && v == null) {
		return true;
	}
	
	if (u == null || v == null) {
		return false;
	}
	
    if (typeof(u) != typeof(v)) {
        return false;
    }

    var allkeys = {};
    for (var i in u) {
        allkeys[i] = 1;
    }
    for (var i in v) {
        allkeys[i] = 1;
    }
    for (var i in allkeys) {
        if (u.hasOwnProperty(i) != v.hasOwnProperty(i)) {
            if ((u.hasOwnProperty(i) && typeof(u[i]) == 'function') ||
                (v.hasOwnProperty(i) && typeof(v[i]) == 'function')) {
                continue;
            } else {
                return false;
            }
        }
        if (typeof(u[i]) != typeof(v[i])) {
            return false;
        }
        if (typeof(u[i]) == 'object') {
            if (!QCDSerializator.equals(u[i], v[i])) {
                return false;
            }
        } else {
            if (u[i] !== v[i]) {
                return false;
            }
        }
    }

    return true;
};



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

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Component = function(_element, _mainController) {
	
	var mainController = _mainController;
	var element = _element;
	
	var elementPath = element.attr('id');
	var elementSearchName = elementPath.replace(/\./g,"\\.");
	var elementName = elementPath.split(".")[elementPath.split(".").length - 1];
	
	this.element = element;
	this.elementPath = elementPath;
	this.elementSearchName = elementSearchName;
	this.elementName = elementName;
	
	var isVisible = true;
	var isEnabled = true;
	
	this.contextObject = null;
	
	
	function constructor(_this) {
		var optionsElement = $("#"+elementSearchName+" > .element_options");
		if (!optionsElement.html() || $.trim(optionsElement.html()) == "") {
			_this.options = new Object();
		} else {
			_this.options = jsonParse(optionsElement.html());
		}
		optionsElement.remove();
		isVisible = _this.options.defaultVisible;
		isEnabled = _this.options.defaultEnabled;
	}
	
	this.getValue = function() {
		var valueObject = new Object();
		
		valueObject.enabled = isEnabled;
		valueObject.visible = isVisible;
		
		if (this.getComponentValue) {
			valueObject.content = this.getComponentValue();
		} else {
			valueObject.content = null;
		}
		if (this.contextObject) {
			valueObject.context = this.contextObject;
		}
		if (this.getComponentsValue) {
			valueObject.components = this.getComponentsValue();
		}
		return valueObject;
	}
	
	this.setValue = function(value) {
		this.setEnabled(value.enabled);
		this.setVisible(value.visible);
		this.setMessages(value.messages);
		if (value.components) {
			this.setComponentsValue(value);
		}
		
		if (value.content != null) {
			this.setComponentValue(value.content);
		}
		if (value.updateState) {
			this.performUpdateState();
		}
	}
	
	this.performUpdateState = function() {
	}
	
	this.addContext = function(contextField, contextValue) {
		if (! this.contextObject) {
			this.contextObject = new Object;
		}
		this.contextObject[contextField] = contextValue;
	}
	
	this.fireEvent = function(eventName, args) {
		this.beforeEventFunction();
		mainController.callEvent(eventName, elementPath, null, args);
	}
	
	this.setState = function(state) {
		this.setEnabled(state.enabled);
		this.setVisible(state.visible);
		if (this.setComponentState) {
			this.setComponentState(state.content);
		} else {
			QCD.error(this.elementPath+".setComponentState() no implemented");
		}
		if (state.components) {
			this.setComponentsState(state);
		}
	}
	
//	this.setLoading = function(isLoadingVisible) {
//		var listeners = options.listeners;
//		if (listeners) {
//			for (var i in listeners) {
//				mainController.getComponent(listeners[i]).setLoading(isLoadingVisible);
//			}
//		}
//		if (this.setComponentLoading) {
//			this.setComponentLoading(isLoadingVisible);
//		} else {
//			QCD.error(this.elementPath+".setLoading() no implemented");
//		}
//	}
	
	this.updateSize = function(width, height) {
	}

	this.setMessages = function(messages) {
		for ( var i in messages) {
			mainController.showMessage(messages[i]);
		}
	}

	this.setEnabled = function(_isEnabled) {
		isEnabled = _isEnabled;
		this.setComponentEnabled(isEnabled);
	}
	
	this.isEnabled = function() {
		return isEnabled;
	}
	
	this.setVisible = function(_isVisible) {
		isVisible = _isVisible;
		if (this.setComponentVisible) {
			this.setComponentVisible(isVisible);
		} else {
			if (isVisible) {
				element.show();
			} else {
				element.hide();
			}
		}
	}
	
	this.isVisible = function() {
		return isVisible;
	}

	this.isChanged = function() {
		return this.isComponentChanged();
	}
	
	this.isComponentChanged = function() {
		return false;
	}
	
	constructor(this);

}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Container = function(_element, _mainController, childrenElements) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	var mainController = _mainController;

	var components;
	
	this.constructChildren = function(childrenElements) {
		components = QCDPageConstructor.getChildrenComponents(childrenElements, mainController);
		this.components = components;
	}
	
	this.getComponentsValue = function() {
		var values = new Object();
		for (var i in components) {
			values[i] = components[i].getValue();
		}
		return values;
	}
	
	this.setComponentsValue = function(value) {
		for (var i in value.components) {
			var componentValue = value.components[i];
			components[i].setValue(componentValue);
		}
	}
	
	this.setComponentsState = function(state) {
		for (var i in state.components) {
			var componentState = state.components[i];
			components[i].setState(componentState);
		}
	}

	
	this.isChanged = function() {
		changed = this.isComponentChanged();
		if (changed == true) {
			return true;
		}
		for (var i in components) {
			if(components[i].isChanged()) {
				changed = true;
				break;
			}
		}
		return changed;
	}
	
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Form = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	
	var elementPath = this.elementPath;
	
	var formValue = null;
	
	var baseValue = null; 
	
	var headerEntityIdentifier = null;
	
	var header = null;
	
	translations = this.options.translations;
	
	function constructor(_this) {
		var childrenElement = $("#"+_this.elementSearchName+"_formComponents");
		_this.constructChildren(childrenElement.children());
	}

	this.getComponentValue = function() {
		return {
			entityId: formValue,
			baseValue: baseValue,
			headerEntityIdentifier: headerEntityIdentifier,
			header : header
		};
	}
	
	this.setComponentValue = function(value) {
		if(value.valid) {
			if(value.headerEntityIdentifier) {
				mainController.setWindowHeader(value.header + ' <span>' + value.headerEntityIdentifier + '</span>');
			} else {
				mainController.setWindowHeader(value.header);
			}
		}
		headerEntityIdentifier = value.headerEntityIdentifier;
		header = value.header;
		formValue = value.entityId;
		unblock();
	}
	
	this.setComponentState = function(state) {
		if(state.headerEntityIdentifier) {
			mainController.setWindowHeader(state.header + ' <span>' + state.headerEntityIdentifier + '</span>');
		} else {
			mainController.setWindowHeader(state.header);
		}
		headerEntityIdentifier = state.headerEntityIdentifier;
		header = state.header;
		formValue = state.entityId;
		if (state.baseValue) {
			baseValue = state.baseValue;
		}
		unblock();
	}
	
	this.setComponentEnabled = function(isEnabled) {
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			block();
		} else {
			unblock();
		}
	}
	
	this.performUpdateState = function() {
		baseValue = formValue;
	}
	
	this.isComponentChanged = function() {
		return ! (baseValue == formValue);
	}
	
	this.performSave = function(actionsPerformer) {
		callEvent("save", actionsPerformer);
	}
	
	this.performDelete = function(actionsPerformer) {
		if (window.confirm(translations.confirmDeleteMessage)) {
			callEvent("delete", actionsPerformer);
		}
	}
	
	this.performCancel = function(actionsPerformer) {
		if (window.confirm(translations.confirmCancelMessage)) {
			callEvent("reset", actionsPerformer);
		}
	}
	
	this.fireEvent = function(actionsPerformer, eventName, args) {
		callEvent(eventName, actionsPerformer, args);
	}
	
	function callEvent(eventName, actionsPerformer, args) {
		block();
		mainController.callEvent(eventName, elementPath, function() {
			unblock();
		}, args, actionsPerformer);
	}
	
	this.updateSize = function(_width, _height) {
		for (var i in this.components) {
			this.components[i].updateSize(_width, _height);
		}
	}
	
	function block() {
		QCD.components.elements.utils.LoadingIndicator.blockElement(element);
	}
	
	function unblock() {
		QCD.components.elements.utils.LoadingIndicator.unblockElement(element);
	}
	
	constructor(this);
}

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.BorderLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));

	function constructor(_this) {
		_this.constructChildren(_this.getLayoutChildren());
	}
	
	this.getLayoutChildren = function() {
		return $("#"+this.elementSearchName+"_layoutComponents").children();
	}
	
	this.updateSize = function(_width, _height) {
		for (var i in this.components) {
			this.components[i].updateSize(_width, _height);
		}
	}
	
	constructor(this);
}

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.GridLayout = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));
	
	var elementSearchName = this.elementSearchName;
	var rootElement = $("#"+elementSearchName+"_layoutComponents > table > tbody");
	
	var colsNumber = this.options.colsNumber;
	
	function constructor(_this) {
		_this.constructChildren(getLayoutChildren());
	}
	
	function getLayoutChildren() {
		var components = rootElement.children().children().children();
		return components;
	}
	
	this.updateSize = function(_width, _height) {
		var baseWidth = _width/colsNumber;
		var baseHeight = 50;
		
		var tdElements = rootElement.children().children();
		
		for (var i=0; i<tdElements.length; i++) {
			var tdElement = $(tdElements[i]);
			var colspan = tdElement.attr("colspan") ? tdElement.attr("colspan") : 1;
			var elementWidth = baseWidth * colspan;
			tdElement.width(elementWidth);
		}
		
		for (var i in this.components) {
			var tdElement = this.components[i].element.parent();
			var rowspan = tdElement.attr("rowspan") ? tdElement.attr("rowspan") : 1;
			var colspan = tdElement.attr("colspan") ? tdElement.attr("colspan") : 1;
			
			var elementHeight = baseHeight * rowspan;
			var elementWidth = baseWidth * colspan;
			
			this.components[i].updateSize(elementWidth, elementHeight);
		}
	}
	
	constructor(this);
}

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};
QCD.components.containers.layout = QCD.components.containers.layout || {};

QCD.components.containers.layout.Layout = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	this.getComponentValue = function() {
		return {};
	}
	this.setComponentValue = function(value) {
	}
	this.setComponentState = function(state) {
	}
	
	this.setMessages = function(messages) {
	}
	
	this.setComponentEnabled = function(isEnabled) {
	}
	
	this.setComponentLoading = function() {
	}
	
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.containers = QCD.components.containers || {};

QCD.components.containers.Window = function(_element, _mainController) {
	$.extend(this, new QCD.components.Container(_element, _mainController));
	
	var mainController = _mainController;
	
	this.element.css("height","100%");
	
	function constructor(_this) {
		
		var childrenElement = $("#"+_this.elementSearchName+"_windowComponents");
		_this.constructChildren(childrenElement.children());
		
		mainController.setWindowHeaderComponent(_this);
		
		if (_this.options.ribbon) {
			ribbon = new QCD.components.Ribbon(_this.options.ribbon, _this.elementName, mainController);
			var ribbonElement = ribbon.constructElement();
			var ribbonDiv = $("#"+_this.elementPath+"_windowContainerRibbon");
			ribbonDiv.append(ribbonElement);
		}
	}
	
	this.getComponentValue = function() {
		return {};
	}
	this.setComponentValue = function(value) {
	}
	this.setComponentState = function(state) {
	}
	
	this.setMessages = function(messages) {
	}
	
	this.setComponentEnabled = function(isEnabled) {
		
	}
	
	this.setComponentLoading = function() {
		
	}
	
	this.updateSize = function(_width, _height) {
		
		var isMinWidth = ! mainController.isPopup();
		
		var childrenElement = $("#"+this.elementSearchName+"_windowContent");
		
		var margin = Math.round(_width * 0.02);
		if (margin < 20 && isMinWidth) {
			margin = 20;
		}
		width = Math.round(_width - 2 * margin);
		
		if (width < 960 && isMinWidth) {
			width = 960;
			childrenElement.css("marginLeft", margin+"px");
			childrenElement.css("marginRight", margin+"px");
		} else {
			childrenElement.css("marginLeft", "auto");
			childrenElement.css("marginRight", "auto");
		}
		childrenElement.width(width);
		childrenElement.css("marginTop", margin+"px");
		if (! this.options.fixedHeight) {
			childrenElement.css("marginBottom", margin+"px");
		}
		
		height = null;
		if (this.options.fixedHeight) {
			var containerHeight = Math.round(_height - 2 * margin - 70);
			height = containerHeight;
			if (this.options.header) {
				height -= 34;
			}
			childrenElement.height(containerHeight);
		}
		
		for (var i in this.components) {
			var componentsHeight = height ? height-20 : null;
			this.components[i].updateSize(width-20, componentsHeight);
		}
		
		var innerWidth = $("#"+this.elementSearchName+"_windowContainerContentBodyWidthMarker").innerWidth();
		if (ribbon) {
			ribbon.updateSize(margin, innerWidth);
		}
	}
	
	this.setHeader = function(header) {
		var headerElement = $("#"+this.elementPath+"_windowHeader");
		if (headerElement) {
			headerElement.html(header);
		}
	}
	
	this.performBack = function(actionsPerformer) {
		mainController.goBack();
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	this.performCloseWindow = function(actionsPerformer) {
		mainController.closeWindow();
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	constructor(this);
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Calendar = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var ANIMATION_LENGTH = 200;
	
	var containerElement = _element;
	
	var calendar = $("#"+this.elementSearchName+"_calendar");
	
	var input = this.input;
	
	var datepicker;
	var datepickerElement;
	
	var opened = false;
	
	var skipButtonClick = false;
	
	var isTriggerBootonHovered = false;
	
	var constructor = function(_this) {
		options = $.datepicker.regional[locale];
		
		if(!options) {
			options = $.datepicker.regional[''];
		}
		
		options.changeMonth = true;
		options.changeYear = true;
		options.showOn = 'button';
		options.dateFormat = 'yy-mm-dd';
		options.showAnim = 'show';
		options.altField = input;
		options.onClose = function(dateText, inst) {
			opened = false;
			if (isTriggerBootonHovered) {
				skipButtonClick = true;
			}
		}
		options.onSelect = function(dateText, inst) {
			datepickerElement.slideUp(ANIMATION_LENGTH);
			opened = false;
		}
		
		datepickerElement = $("<div>").css("position", "absolute").css("zIndex", 100).css("right", "15px");
		containerElement.css("position", "relative");
		datepickerElement.hide();
		
		$("#ui-datepicker-div").hide();
		
		containerElement.append(datepickerElement);
		
		datepickerElement.datepicker(options);
		
		input.val("");
		
		$(document).mousedown(function(event) {
			if(!opened) {
				return;
			}
			var target = $(event.target);
			if (target.attr("id") != input.attr("id") && target.attr("id") != calendar.attr("id")
					&& target.parents('.ui-datepicker').length == 0) {
				datepickerElement.slideUp(ANIMATION_LENGTH);
				opened = false;
			}
		});
		
		calendar.hover(function() {isTriggerBootonHovered = true;}, function() {isTriggerBootonHovered = false;})
		calendar.click(function() {
			if(calendar.hasClass("enabled")) {
				if (skipButtonClick) {
					skipButtonClick = false;
					return;
				}
				if(!opened) {
					
					if (input.val()) {
						try {
							$.datepicker.parseDate( "yy-mm-dd", input.val());
							datepickerElement.datepicker("setDate", input.val());
						} catch (e) {
							// do nothing
						}
					}
					
					var top = input.offset().top;
					var calendarHeight = datepickerElement.outerHeight();
					var inputHeight = input.outerHeight() + 10;
					var viewHeight = document.documentElement.clientHeight + $(document).scrollTop();
					
					if ((top+calendarHeight+inputHeight) > viewHeight) {
						datepickerElement.css("top", "");
						datepickerElement.css("bottom", inputHeight +"px");
						isOnTop = true;
					} else {
						datepickerElement.css("top", inputHeight +"px");
						datepickerElement.css("bottom", "");
						isOnTop = false;
					}
					
					datepickerElement.slideDown(ANIMATION_LENGTH).show();
					opened = true;
				} else {
					datepickerElement.slideUp(ANIMATION_LENGTH)
					opened = false;
				}
			}
		});
		
		input.focus(function() {
			calendar.addClass("lightHover");
		}).blur(function() {
			calendar.removeClass("lightHover");
		});
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			calendar.addClass("enabled");
			input.datepicker("enable");
		} else {
			calendar.removeClass("enabled");
			input.datepicker("disable")
		}
	}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().parent().parent().height(height);
	}
	
	constructor(this);
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.CheckBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var mainController = _mainController;
	var textRepresentation = $("#" + this.elementSearchName + "_text");
	var currentValue;
	
	var translations = this.options.translations; 
	
	this.getComponentData = function() {
		if (this.input.attr('checked')) {
			return { value: "1" };
		}
		return { value: "0" };
	}
	
	this.setComponentData = function(data) {
		if (data != null && data.value == 1) {
			this.input.attr('checked', true);
			textRepresentation.html(translations["true"]);
		} else {
			this.input.attr('checked', false);
			textRepresentation.html(translations["false"]);
		}
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if(this.options.textRepresentationOnDisabled) {
			if(isEnabled) {
				this.input.show();
				textRepresentation.hide();
			} else {
				this.input.hide();
				textRepresentation.show();
			}
		}
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			textRepresentation.removeClass("disabled");
			this.input.removeAttr('disabled');
		} else {
			textRepresentation.addClass("disabled");
			this.input.attr('disabled', 'true');
		}
		if (this.setFormComponentEnabled) {
			this.setFormComponentEnabled(isEnabled);
		}
	}
	
	this.setSelected = function(actionsPerformer, isSelected) {
		this.input.attr('checked', isSelected);
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	
	this.setCurrentValue = function(data) {
		currentValue = this.input.attr('checked');
	} 
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().height(height);
	}
	
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.DynamicComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var element = _element;
	var mainController = _mainController;
	var elementPath = this.elementPath;
	var stateSelectedValue = null;
	
	var input = this.input;
	
	var constructor = function(_this) {
		input.change(function() {
			setTitle();
		});
	}
	
	function setTitle() {
		title = input.find(':selected').text();
		value = input.val();
		
		if(title && value) {		
			input.attr('title', title);
		} else {
			input.removeAttr('title');
		}
	}
	
	this.getComponentData = function() {
		var selected = this.input.val();
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		setData(data);
	}
	
	function setData(data) {
		if (data == null) {
			return;
		}
		if (data.value && ! data.values) { // is setState
			stateSelectedValue = data.value;
			return;
		}
		var previousSelected = input.val();
		input.children().remove();
		for (var i in data.values) {
			var value = data.values[i];
			input.append("<option value='"+value.key+"'>"+value.value+"</option>");
		}
		
		if (stateSelectedValue) {
			selected = stateSelectedValue;
		} else {
			selected = data.value;
		}
		
		if (selected == undefined) {
			input.val(previousSelected);
		} else {
			input.val(selected);
		}
		
		setTitle();
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			element.removeClass("disabled");
			this.input.removeAttr('disabled');
		} else {
			element.addClass("disabled");
			this.input.attr('disabled', 'true');
		}
		if (this.setFormComponentEnabled) {
			this.setFormComponentEnabled(isEnabled);
		}
	}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().height(height);
	}
		
	constructor(this);
	
}


/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.EntityComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));

	var mainController = _mainController;
	var options = this.options;
	var elementPath = this.elementPath;
	var input = this.input;
	
	function constructor(_this) {
		_this.input.change(onChange);
	}

	function onChange() {
		if (options.listeners.length > 0) {
			mainController.getUpdate(elementPath, input.val(), options.listeners);
		}
	}
	
	this.getComponentData = function() {
		var selected = this.input.val();
		if (!selected || $.trim(selected) == "") {
			selected = null;
		}
		return {
			value: selected
		}
	}
	
	this.setComponentData = function(data) {
		var previousSelected = this.input.val();
		
		if(data.values != null) {
			this.input.children().remove();
			var blankValue = mainController.getPluginIdentifier()+"."+mainController.getViewName()+"."+elementPath.replace(/-/g,".")+".blankValue";
			this.input.append("<option value=''>"+mainController.getTranslation(blankValue)+"</option>");
			for (var i in data.values) {
				var value = data.values[i];
				this.input.append("<option value='"+i+"'>"+value+"</option>");
			}
		}
		
		selected = data.value;
		
		if (selected != null) {
			this.input.val(selected);
		} else {
			this.input.val(previousSelected);
		}
	}
	
	constructor(this);
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.FormComponent = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;

	var element = _element;
	
	var errorIcon = $("#" + this.elementSearchName + "_error_icon");
	var errorMessages = $("#" + this.elementSearchName + "_error_messages");

	var descriptionIcon = $("#" + this.elementSearchName + "_description_icon");
	var descriptionMessage = $("#" + this.elementSearchName + "_description_message");
	
	var baseValue;
	
	this.input = $("#" + this.elementSearchName + "_input");

	function constructor(_this) {
		_this.registerCallbacks();
	}
	
	this.registerCallbacks = function() {
		descriptionIcon.hover(function() {
			descriptionMessage.show();
		}, function() {
			descriptionMessage.hide();
		});

		errorIcon.hover(function() {
			errorMessages.show();
		}, function() {
			errorMessages.hide();
		});
	}
	
	this.getComponentData = function() {
		return {
			value : this.input.val(),
		}
	}

	this.setComponentData = function(data) {
		if (data.value) {
			this.input.val(data.value);
		} else {
			this.input.val("");
		}
	}

	this.getComponentValue = function() {
		var value = this.getComponentData();
		value.required = element.hasClass("required");		
		value.baseValue = baseValue;
		return value;
	}

	this.setComponentValue = function(value) {
		this.setComponentData(value);
		setComponentRequired(value.required);
	}

	this.setComponentState = function(state) {
		this.setComponentData(state);
		setComponentRequired(state.required);
		if (state.baseValue != undefined) {
			baseValue = state.baseValue;
		}
	}
	
	this.performUpdateState = function() {
		baseValue = this.getComponentData().value;
	}
	this.isComponentChanged = function() {
		if (! (baseValue == this.getComponentData().value)) {
			baseValue
		}
		return ! (baseValue == this.getComponentData().value);
	}

	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			element.removeClass("disabled");
			this.input.removeAttr("readonly");
		} else {
			element.addClass("disabled");
			this.input.attr("readonly", "readonly");
		}
		if (this.setFormComponentEnabled) {
			this.setFormComponentEnabled(isEnabled);
		}
	}
	
	function setComponentRequired(isRequired) {
		if (isRequired) {
			element.addClass("required");
		} else {
			element.removeClass("required");
		}
	}
	
	this.setMessages = function(messages) {
		errorMessages.html("");
		for ( var i in messages) {
			this.addMessage(messages[i]);
		}
		if (messages) {
			setComponentError(messages.length != 0);
		}
	}
	
	this.addMessage = function(message) {
		messageDiv = $('<div>');
		
		messageDiv.append('<span>' + message.title + '</span>');
		messageDiv.append('<p>' + message.content + '</p>');

		errorMessages.append(messageDiv);

		var top = this.input.offset().top;
		var errorIconHeight = errorMessages.height();
		var inputHeight = this.input.outerHeight() - 1;
		var viewHeight = document.documentElement.clientHeight + $(document).scrollTop();

		if ((top+errorIconHeight+inputHeight) > viewHeight) {
			errorMessages.css("top", "");
			errorMessages.css("bottom", errorIcon.outerHeight()+"px");
		} else {
			errorMessages.css("top", errorIcon.outerHeight()+"px");
			errorMessages.css("bottom", "");
		}
	}
	
	function setComponentError(isError) {
		if (isError) {
			element.addClass("error");
		} else {
			element.removeClass("error");
		}
	}

	this.setComponentLoading = function(isLoadingVisible) {}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().height(height);
	}
	
	constructor(this);

}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.grid = QCD.components.elements.grid || {};

QCD.components.elements.grid.GridHeaderController = function(_gridController, _mainController, _gridParameters, _translations) {
	
	var gridController = _gridController;
	var mainController = _mainController;
	var gridParameters = _gridParameters;
	var translations = _translations;
	
	
	var pagingVars = new Object();
	pagingVars.first = null;
	pagingVars.max = null;
	pagingVars.totalNumberOfEntities = null;
	
	var headerElement;
	var footerElement;
	
	var headerElements = new Object();
	headerElements.filterButton = null;
	headerElements.predefiniedFiltersCombo = null;
	headerElements.predefiniedFiltersCustomOption_line1 = $("<option>").attr("value",-1).html("--------------").css("display", "none");
	headerElements.predefiniedFiltersCustomOption_line2 = $("<option>").attr("value",-1).html(translations.customPredefinedFilter).css("display", "none");
	headerElements.newButton = null;
	headerElements.deleteButton = null;
	headerElements.upButton = null;
	headerElements.downButton = null;
	
	var headerPagingController = null;
	var footerPagingController = null;
	
	var entitiesNumberSpan;
	
	var enabled = false;
	var rowIndex = null;
	
	function constructor(_this) {
		pagingVars.first = 0;
		pagingVars.max = 30;
		pagingVars.totalNumberOfEntities = 0;
	}
	
	function paging_refresh() {
		if (gridParameters.paging && enabled) {
			var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
			if (pagesNo == 0) {
				pagesNo = 1;
			}
			var currPage = Math.ceil(pagingVars.first / pagingVars.max);
			if (pagingVars.first % pagingVars.max == 0) {
				currPage += 1;
			}
			headerPagingController.setPageData(currPage, pagesNo, pagingVars.max);
			footerPagingController.setPageData(currPage, pagesNo, pagingVars.max);
			if (currPage > 1) {
				headerPagingController.enablePreviousButtons();
				footerPagingController.enablePreviousButtons();
			} else {
				headerPagingController.disablePreviousButtons();
				footerPagingController.disablePreviousButtons();
			}
			if (pagingVars.first + pagingVars.max < pagingVars.totalNumberOfEntities) {
				headerPagingController.enableNextButtons();
				footerPagingController.enableNextButtons();
			} else {
				headerPagingController.disableNextButtons();
				footerPagingController.disableNextButtons();
			}
			headerPagingController.enableRecordsNoSelect();
			footerPagingController.enableRecordsNoSelect();
			headerPagingController.enableInput();
			footerPagingController.enableInput();
		}
	}
		
	this.paging_prev = function() {
		pagingVars.first -= pagingVars.max;
		if (pagingVars.first < 0) {
			pagingVars.first = 0;
		}
		onPagingEvent();
	}

	this.paging_next = function() {
		pagingVars.first += pagingVars.max;
		onPagingEvent();
	}
	
	this.paging_first = function() {
		pagingVars.first = 0;
		onPagingEvent();
	}

	this.paging_last = function() {
		if (pagingVars.totalNumberOfEntities % pagingVars.max > 0) {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.totalNumberOfEntities % pagingVars.max;
		} else {
			pagingVars.first = pagingVars.totalNumberOfEntities - pagingVars.max;
		}
		onPagingEvent();
	}

	this.paging_onRecordsNoSelectChange = function(recordsNoSelectElement) {
		var recordsNoSelectValue = recordsNoSelectElement.val();
		pagingVars.max = parseInt(recordsNoSelectValue);
		pagingVars.first = 0;
		onPagingEvent();
	}
	
	this.paging_setPageNo = function(pageNoElement) {
		var pageNoValue = pageNoElement.val();
		if (! pageNoValue || $.trim(pageNoValue) == "") {
			pageNoElement.addClass("inputError");
			return;
		}
		if (! /^\d*$/.test(pageNoValue)) {
			pageNoElement.addClass("inputError");
			return;
		}
		var intValue = parseInt(pageNoValue);
		if (intValue <= 0) {
			pageNoElement.addClass("inputError");
			return;
		}
		if (intValue > Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max)) {
			pageNoElement.addClass("inputError");
			return;
		}
		pagingVars.first = pagingVars.max * (intValue - 1);
		onPagingEvent();
	}
	
	function onPagingEvent() {
		headerPagingController.hideInputError();
		footerPagingController.hideInputError();
		gridController.onPagingParametersChange();
	}
	
	this.getPagingParameters = function() {
		return [pagingVars.first, pagingVars.max];
	}
	
	this.updatePagingParameters = function(_first, _max, _totalNumberOfEntities) {
		if (_first > _totalNumberOfEntities) {
			pagingVars.first = 0;
			gridController.onPagingParametersChange();
		} else {
			pagingVars.first = _first;
		}
		pagingVars.max = _max;
		pagingVars.totalNumberOfEntities = _totalNumberOfEntities;
		entitiesNumberSpan.html("("+pagingVars.totalNumberOfEntities+")");
		paging_refresh();
	}
	
	this.getHeaderElement = function() {
		headerElement = $("<div>").addClass('grid_header').addClass("elementHeader").addClass("elementHeaderDisabled");
		headerElement.append($("<span>").html(translations.header).addClass('grid_header_gridName').addClass('elementHeaderTitle'));
		entitiesNumberSpan = $("<span>").html("(0)").addClass('grid_header_totalNumberOfEntities').addClass('elementHeaderTitle');
		headerElement.append(entitiesNumberSpan);
		
		if (gridParameters.hasPredefinedFilters) { // TODO mina add option
			var options = new Array();
			for (var i in gridParameters.predefinedFilters) {
				options[i] = {
					value: i,
					label: translations["filter."+gridParameters.predefinedFilters[i].label]
				};
			}
			headerElements.predefiniedFiltersCombo = QCD.components.elements.utils.HeaderUtils.createHeaderComboBox(options, function(selectedItem) {
				if (selectedItem < 0) {
					return;
				}
				headerElements.predefiniedFiltersCustomOption_line1.css("display","none");
				headerElements.predefiniedFiltersCustomOption_line2.css("display","none");
				var filterObj = gridParameters.predefinedFilters[selectedItem];
				gridController.setFilterObject(filterObj);	
			});
			headerElements.predefiniedFiltersCombo.append(headerElements.predefiniedFiltersCustomOption_line1);
			headerElements.predefiniedFiltersCombo.append(headerElements.predefiniedFiltersCustomOption_line2);
			headerElement.append(headerElements.predefiniedFiltersCombo);
		}
		if (gridParameters.filter) {
			headerElements.filterButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.addFilterButton, function(e) {
				if (headerElements.filterButton.hasClass("headerButtonEnabled")) {
					filterClicked();
				}
			}, "filterIcon16_dis.png");
			headerElement.append(headerElements.filterButton);
			setEnabledButton(headerElements.filterButton, false);
		}
		if (gridParameters.canNew) {
			headerElements.newButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.newButton,function(e) {
				if (headerElements.newButton.hasClass("headerButtonEnabled")) {
					gridController.onNewButtonClicked();
				}
			}, "newIcon16_dis.png");
			headerElement.append(headerElements.newButton);
			setEnabledButton(headerElements.newButton, false);
		}
		if (gridParameters.canDelete) {
			headerElements.deleteButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.deleteButton, function(e) {
				if (headerElements.deleteButton.hasClass("headerButtonEnabled")) {
					gridController.onDeleteButtonClicked();
				}
			}, "deleteIcon16_dis.png");
			headerElement.append(headerElements.deleteButton);
			setEnabledButton(headerElements.deleteButton, false);
		}
		if (gridParameters.orderable) {
			headerElements.upButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.upButton,function(e) {
				if (headerElements.upButton.hasClass("headerButtonEnabled")) {
					gridController.onUpButtonClicked();
				}
			}, "upIcon16_dis.png");
			headerElement.append(headerElements.upButton);
			setEnabledButton(headerElements.upButton, false);
			headerElements.downButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.downButton, function(e) {
				if (headerElements.downButton.hasClass("headerButtonEnabled")) {
					gridController.onDownButtonClicked();
				}
			}, "downIcon16_dis.png");
			headerElement.append(headerElements.downButton);
			setEnabledButton(headerElements.downButton, false);
		}
		if (gridParameters.paging) {
			headerPagingController = new QCD.components.elements.grid.GridPagingElement(this, mainController, translations);
			headerElement.append(headerPagingController.getPagingElement(pagingVars));
		}
		return headerElement;
	}
	
	this.getFooterElement = function() {
		if (!gridParameters.paging) {
			return null;
		}
		footerPagingController = new QCD.components.elements.grid.GridPagingElement(this, mainController, translations);
		footerElement = $("<div>").addClass('grid_footer').append(footerPagingController.getPagingElement(pagingVars)); 
		return footerElement;
	}
	
	this.setEnabled = function(_enabled) {
		enabled = _enabled;
		if (enabled) {
			headerElement.removeClass("elementHeaderDisabled");
			if (footerElement) {
				footerElement.removeClass("elementHeaderDisabled");
			}
		} else {
			headerElement.addClass("elementHeaderDisabled");
			if (footerElement) {
				footerElement.addClass("elementHeaderDisabled");
			}
		}
		refreshButtons();
	}
	
	this.onRowClicked = function(_rowIndex) {
		rowIndex = _rowIndex;
		refreshButtons();
	}
	
	function refreshButtons() {
		if (!enabled) {
			if (headerElements.filterButton != null) {
				setEnabledButton(headerElements.filterButton, false);
			}
			if (headerElements.predefiniedFiltersCombo != null) {
				headerElements.predefiniedFiltersCombo.disable();
			}
			if (headerElements.newButton != null) {
				setEnabledButton(headerElements.newButton, false);
			}
			if (headerElements.deleteButton != null) {
				setEnabledButton(headerElements.deleteButton, false);
			} 
			if (headerElements.upButton != null) {
				setEnabledButton(headerElements.upButton, false);
			}
			if (headerElements.downButton != null) {
				setEnabledButton(headerElements.downButton, false);
			}
			if (gridParameters.paging) {
				headerPagingController.disablePreviousButtons();
				footerPagingController.disablePreviousButtons();
				headerPagingController.disableNextButtons();
				footerPagingController.disableNextButtons();
				headerPagingController.disableRecordsNoSelect();
				footerPagingController.disableRecordsNoSelect();
				headerPagingController.disableInput();
				footerPagingController.disableInput();
			}
			
		} else {
			if (headerElements.filterButton != null) {
				setEnabledButton(headerElements.filterButton, true);
			}
			if (headerElements.predefiniedFiltersCombo != null) {
				headerElements.predefiniedFiltersCombo.enable();
			}
			if (headerElements.newButton != null) {
				setEnabledButton(headerElements.newButton, true);
			}
			if (headerElements.deleteButton != null) {
				if (rowIndex != null) {
					setEnabledButton(headerElements.deleteButton, true);
				} else {
					setEnabledButton(headerElements.deleteButton, false);
				}
			}
			if (gridParameters.paging) {
				var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
				var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
				if (pagesNo == 0) {
					pagesNo = 1;
				}
			} 
			if (headerElements.upButton != null) {
				if (rowIndex == 1 || rowIndex == null) {
					setEnabledButton(headerElements.upButton, false);
				} else {
					setEnabledButton(headerElements.upButton, true);
				}
			}
			if (headerElements.downButton != null) {
				if (rowIndex == pagingVars.totalNumberOfEntities || rowIndex == null) {	
					setEnabledButton(headerElements.downButton, false);
				} else {
					setEnabledButton(headerElements.downButton, true);
				}
			}
		}
		
	}
	
	function filterClicked() {
		if (headerElements.filterButton.hasClass("headerButtonActive")) {
			headerElements.filterButton.removeClass("headerButtonActive");
			headerElements.filterButton.label.html(translations.addFilterButton);
		} else {
			headerElements.filterButton.addClass("headerButtonActive");
			headerElements.filterButton.label.html(translations.removeFilterButton);
		}
		gridController.onFilterButtonClicked();
	}
	
	this.setFilterActive = function() {
		headerElements.filterButton.addClass("headerButtonActive");
		headerElements.filterButton.label.html(translations.removeFilterButton);
	}
	
	this.setFilterNotActive = function() {
		headerElements.filterButton.removeClass("headerButtonActive");
		headerElements.filterButton.label.html(translations.addFilterButton);
	}
	
	this.setPredefinedFilter = function(predefinedFilter) {
		if (predefinedFilter == null) {
			headerElements.predefiniedFiltersCustomOption_line1.css("display","");
			headerElements.predefiniedFiltersCustomOption_line2.css("display","");
			headerElements.predefiniedFiltersCombo.val(-1);
		} else {
			headerElements.predefiniedFiltersCustomOption_line1.css("display","none");
			headerElements.predefiniedFiltersCustomOption_line2.css("display","none");
			headerElements.predefiniedFiltersCombo.val(predefinedFilter);
		}
	}

	this.setEnabledButton = function(button, enabled) {
		if (enabled) {
			button.addClass("headerButtonEnabled");
		} else {
			button.removeClass("headerButtonEnabled");
		}		
	} 
	var setEnabledButton = this.setEnabledButton;
	
	constructor(this);
}


QCD.components.elements.grid.GridPagingElement = function(_gridHeaderController, _mainController, _translations) {
	
	var gridHeaderController = _gridHeaderController;
	var mainController = _mainController;
	var translations = _translations;
	
	var pagingElements = new Object();
	pagingElements.prevButton = null;
	pagingElements.nextButton = null;
	pagingElements.firstButton = null;
	pagingElements.lastButton = null;
	pagingElements.recordsNoSelect = null;
	pagingElements.pageNo = null;
	pagingElements.allPagesNoSpan = null;
	
	function constructor() {
	}
	
	this.getPagingElement = function(pagingVars) {
		var pagingDiv = $("<div>").addClass('grid_paging');
		var onPageSpan = $("<span>").html(translations.perPage).addClass('onPageSpan');
		pagingDiv.append(onPageSpan);
		pagingElements.recordsNoSelect = $("<select>").addClass('recordsNoSelect');
			pagingElements.recordsNoSelect.append("<option value=10>10</option>");
			pagingElements.recordsNoSelect.append("<option value=20>20</option>");
			pagingElements.recordsNoSelect.append("<option value=30>30</option>");
			pagingElements.recordsNoSelect.append("<option value=50>50</option>");
			pagingElements.recordsNoSelect.append("<option value=100>100</option>");
			pagingElements.recordsNoSelect.val(pagingVars.max);
		pagingDiv.append(pagingElements.recordsNoSelect);
		
		pagingElements.firstButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_first");
		pagingDiv.append(pagingElements.firstButton);
		
		pagingElements.prevButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_left");
		pagingDiv.append(pagingElements.prevButton);

		var pagesNo = Math.ceil(pagingVars.totalNumberOfEntities / pagingVars.max);
		if (pagesNo == 0) {
			pagesNo = 1;
		}
		var currPage = Math.ceil(pagingVars.first / pagingVars.max) + 1;
		
		var pageInfoSpan = $("<span>").addClass('grid_paging_pageInfo');
		
//		<div class="component_container_form_w">
//			<div class="component_container_form_inner">
//				<div class="component_container_form_x"></div>
//				<div class="component_container_form_y"></div>
//				<input type='text' id="usernameInput" name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/>
//			</div>
//		</div>
		
			pagingElements.pageNo = $("<input type='text'></input>").addClass('pageInput');
				var component_container_form_inner = $("<div>").addClass("component_container_form_inner");
				component_container_form_inner.append('<div class="component_container_form_x"></div>');
				component_container_form_inner.append('<div class="component_container_form_y"></div>');
				component_container_form_inner.append(pagingElements.pageNo.val(currPage));
				var component_container_form_w = $("<div>").addClass('component_container_form_w').append(component_container_form_inner);
			pageInfoSpan.append(component_container_form_w);
			var ofPagesInfoSpan = $("<span>").addClass("ofPagesSpan");
			ofPagesInfoSpan.append('<span>').html(' ' + translations.outOfPages + ' ');
			pagingElements.allPagesNoSpan = $("<span>");
			ofPagesInfoSpan.append(pagingElements.allPagesNoSpan.html(pagesNo));
			pageInfoSpan.append(ofPagesInfoSpan);
		pagingDiv.append(pageInfoSpan);
	
		pagingElements.nextButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_right");
		pagingDiv.append(pagingElements.nextButton);
		pagingElements.lastButton =  $("<div>").addClass("headerPagingButton").addClass("headerButton_last");;
		pagingDiv.append(pagingElements.lastButton);
		
		pagingElements.firstButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_first();
			}
		});
		pagingElements.prevButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_prev();
			}
		});

		pagingElements.recordsNoSelect.change(function(e) {
			gridHeaderController.paging_onRecordsNoSelectChange($(this));
		});
		pagingElements.pageNo.change(function(e) {
			gridHeaderController.paging_setPageNo($(this));
		});
		
		pagingElements.nextButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_next();
			}
		});
		pagingElements.lastButton.click(function(e) {
			if ($(e.target).hasClass("headerButtonEnabled")) {
				gridHeaderController.paging_last();
			}
		});
		
		gridHeaderController.setEnabledButton(pagingElements.prevButton, false);
		gridHeaderController.setEnabledButton(pagingElements.firstButton, false);
		gridHeaderController.setEnabledButton(pagingElements.nextButton, false);
		gridHeaderController.setEnabledButton(pagingElements.lastButton, false);
		
		return pagingDiv;
	}
	
	this.setPageData = function(currPage, pagesNo, max) {
		pagingElements.allPagesNoSpan.html(pagesNo);
		pagingElements.pageNo.val(currPage);
		pagingElements.recordsNoSelect.val(max);
	}
	
	this.enablePreviousButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.prevButton, true);
		gridHeaderController.setEnabledButton(pagingElements.firstButton, true);
	}
	this.disablePreviousButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.prevButton, false);
		gridHeaderController.setEnabledButton(pagingElements.firstButton, false);
	}
	this.enableNextButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.nextButton, true);
		gridHeaderController.setEnabledButton(pagingElements.lastButton, true);
	}
	this.disableNextButtons = function() {
		gridHeaderController.setEnabledButton(pagingElements.nextButton, false);
		gridHeaderController.setEnabledButton(pagingElements.lastButton, false);
	}
	this.enableRecordsNoSelect = function() {
		pagingElements.recordsNoSelect.attr("disabled", false);
	}
	this.disableRecordsNoSelect = function() {
		pagingElements.recordsNoSelect.attr("disabled", true);
	}
	this.enableInput = function() {
		pagingElements.pageNo.attr("disabled", false);
	}
	this.disableInput = function() {
		pagingElements.pageNo.attr("disabled", true);
	}
	
	this.showInputError = function() {
		pagingElements.pageNo.addClass("inputError");
	}
	this.hideInputError = function() {
		pagingElements.pageNo.removeClass("inputError");
	}
	
	constructor();
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Grid = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	var mainController = _mainController;
	var element = _element;
	
	var headerController;
	
	var elementPath = this.elementPath;
	var elementName = this.elementName;
	var elementSearchName = this.elementSearchName;
	
	var gridParameters;
	var grid;
	var belongsToFieldName;
	var currentOrder;

	var translations;
	
	var componentEnabled = false;
	
	var currentGridHeight;
	
	var linkListener;
	
	var currentState = {
		selectedEntityId: null,
		filtersEnabled: false
	}
	
	var RESIZE_COLUMNS_ON_UPDATE_SIZE = true;
	
	var columnModel = new Object();
	
	var hiddenColumnValues = new Object();
	
	var defaultOptions = {
		paging: true,
		fullScreen: false,
		shrinkToFit: false
	};
	
	var noRecordsDiv;
	
	var FILTER_TIMEOUT = 200;
	var filterRefreshTimeout = null;
	
	function parseOptions(options) {
		gridParameters = new Object();

		var colNames = new Array();
		var colModel = new Array();
		var isfiltersEnabled = false;
		
		for (var i in options.columns) {
			var column = options.columns[i];
			columnModel[column.name] = column;
			var isSortable = false;
			var isSerchable = false;
			for (var sortColIter in options.orderableColumns) {
				if (options.orderableColumns[sortColIter] == column.name) {
					isSortable = true;
					break;
				}
			}
			for (var sortColIter in options.searchableColumns) {
				if (options.searchableColumns[sortColIter] == column.name) {
					isSerchable = true;
					isfiltersEnabled = true;
					break;
				}
			}
			
			column.isSerchable = isSerchable;
			
			if (!column.hidden) {
				colNames.push(column.label+"<div class='sortArrow' id='"+elementPath+"_sortArrow_"+column.name+"'></div>");
				
				var stype = 'text';
				var searchoptions = {};
				if (column.filterValues) {
					var possibleValues = new Object();
					possibleValues[""] = "";
					for (var i in column.filterValues) {
						possibleValues[i] = column.filterValues[i];
					}
					stype = 'select';
					searchoptions.value = possibleValues;
					searchoptions.defaultValue = "";
				}
				
				colModel.push({name:column.name, index:column.name, width:column.width, sortable: isSortable, resizable: true, 
					align: column.align, stype: stype, searchoptions: searchoptions
					});
			} else {
				hiddenColumnValues[column.name] = new Object();
			}
		}
		
		gridParameters.hasPredefinedFilters = options.hasPredefinedFilters;
		gridParameters.predefinedFilters = options.predefinedFilters;
		
		gridParameters.sortColumns = options.orderableColumns;
		
		gridParameters.colNames = colNames;
		gridParameters.colModel = colModel;
		gridParameters.datatype = function(postdata) {
			//onPostDataChange(postdata);
		}
		gridParameters.multiselect = true;
		gridParameters.shrinkToFit = true;
		
		gridParameters.listeners = options.listeners;
		gridParameters.canNew = options.creatable;
		gridParameters.canDelete = options.deletable;
		gridParameters.paging = options.paginable;
		gridParameters.filter = isfiltersEnabled;
		gridParameters.orderable = options.prioritizable;
		
		gridParameters.fullScreen = options.fullscreen;
		if (options.height) { 
			gridParameters.height = parseInt(options.height);
			if (gridParameters.height <= 0) {
				gridParameters.height = null;
			}
		}
		if (options.width) { gridParameters.width = parseInt(options.width); }
		if (! gridParameters.width && ! gridParameters.fullScreen) {
			gridParameters.width = 300;
		}
		gridParameters.correspondingViewName = options.correspondingView;
		gridParameters.correspondingComponent = options.correspondingComponent;
		
		for (var opName in defaultOptions) {
			if (gridParameters[opName] == undefined) {
				gridParameters[opName] = defaultOptions[opName];
			}
		}
	};
	function rowClicked(rowId) {
		if (currentState.selectedEntityId == rowId) {
			currentState.selectedEntityId = null;
		} else {
			if (currentState.selectedEntityId) {
				grid.setSelection(currentState.selectedEntityId, false);
			}
			currentState.selectedEntityId = rowId;
		}
		
		var rowIndex = grid.jqGrid('getInd', currentState.selectedEntityId);
		if (rowIndex == false) {
			rowIndex = null;
		}
		headerController.onRowClicked(rowIndex);
		
		if (gridParameters.listeners.length > 0) {
			onSelectChange();
		}
	}
	
	this.setLinkListener = function(_linkListener) {
		linkListener = _linkListener;
	}
	
	function linkClicked(entityId) {
		if (linkListener) {
			linkListener.onGridLinkClicked(entityId);
		} else {
			var params = new Object();
			params[gridParameters.correspondingComponent+".id"] = entityId;
			redirectToCorrespondingPage(params);	
		}
	}
	
	function redirectToCorrespondingPage(params) {
		if (gridParameters.correspondingViewName && gridParameters.correspondingViewName != '') {
			var url = gridParameters.correspondingViewName + ".html";
			if (params) {
				url += "?context="+JSON.stringify(params);
			}
			mainController.goToPage(url);
		}
	}
	
	this.getComponentValue = function() {
		return currentState;
	}
	
	this.setComponentState = function(state) {
		if (state.selectedEntityId) {
			currentState.selectedEntityId = state.selectedEntityId;
		}
		if (state.belongsToEntityId) {
			currentState.belongsToEntityId = state.belongsToEntityId;
		}
		if (state.firstEntity) {
			currentState.firstEntity = state.firstEntity;
		}
		if (state.maxEntities) {
			currentState.maxEntities = state.maxEntities;
		}
		if (state.filtersEnabled) {
			currentState.filtersEnabled = state.filtersEnabled;
			headerController.setFilterActive();
			grid[0].toggleToolbar();
			updateSearchFields();
			if (currentState.filtersEnabled) {
				currentGridHeight -= 21;
			} else {
				currentGridHeight += 21;
			}
			grid.setGridHeight(currentGridHeight);
		}
		if (state.filters) {
			currentState.filters = state.filters;
			for (var filterIndex in currentState.filters) {
				$("#gs_"+filterIndex).val(currentState.filters[filterIndex]);
			}
			findMatchingPredefiniedFilter();
		}
		if (state.order) {
			setSortColumnAndDirection(state.order);
		}
	}
	
	this.setComponentValue = function(value) {
		
		if (value.belongsToEntityId) {
			currentState.belongsToEntityId = value.belongsToEntityId;
		}
		if (value.firstEntity) {
			currentState.firstEntity = value.firstEntity;
		}
		if (value.maxEntities) {
			currentState.maxEntities = value.maxEntities;
		}
		
		if (value.entities == null) {
			return;
		}
		grid.jqGrid('clearGridData');
		var rowCounter = 1;
		for (var entityNo in value.entities) {
			var entity = value.entities[entityNo];
			var fields = new Object();
			for (var fieldName in entity.fields) {
				if (hiddenColumnValues[fieldName]) {
					hiddenColumnValues[fieldName][entity.id] = entity.fields[fieldName];
				} else {
					if (columnModel[fieldName].link && entity.fields[fieldName] && entity.fields[fieldName] != "") {
						fields[fieldName] = "<a href=# id='"+elementPath+"_"+fieldName+"_"+entity.id+"' class='"+elementPath+"_link gridLink'>" + entity.fields[fieldName] + "</a>";
						
					} else {
						fields[fieldName] = entity.fields[fieldName];
					}
				}
			}			
			grid.jqGrid('addRowData', entity.id, fields);
			if (rowCounter % 2 == 0) {
				grid.jqGrid('setRowData', entity.id, false, "darkRow");
			} else {
				grid.jqGrid('setRowData', entity.id, false, "lightRow");
			}
			rowCounter++;
		}
		
		if (rowCounter == 1) {
			noRecordsDiv.show();
		} else {
			noRecordsDiv.hide();
		}
		
		$("."+elementSearchName+"_link").click(function(e) {
			var idArr = e.target.id.split("_");
			var entityId = idArr[idArr.length-1];
			linkClicked(entityId);
		});
		
		headerController.updatePagingParameters(currentState.firstEntity, currentState.maxEntities, value.totalEntities);
		
		grid.setSelection(currentState.selectedEntityId, false);
		var rowIndex = grid.jqGrid('getInd', currentState.selectedEntityId);
		if (rowIndex == false) {
			currentState.selectedEntityId = null;
			rowIndex = null;
		}
		headerController.onRowClicked(rowIndex);
		
		if (value.order) {			
			setSortColumnAndDirection(value.order);			
		}
		
		unblockGrid();
	}
	
	this.setComponentEnabled = function(isEnabled) {
		componentEnabled = isEnabled;
		headerController.setEnabled(isEnabled);
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			blockGrid();
		} else {
			unblockGrid();
		}
	}

	
	function blockGrid() {
		QCD.components.elements.utils.LoadingIndicator.blockElement(element);
	}
	
	function unblockGrid() {
		QCD.components.elements.utils.LoadingIndicator.unblockElement(element);
	}

	function constructor(_this) {
		
		parseOptions(_this.options, _this);
		
		gridParameters.modifiedPath = elementPath.replace(/\./g,"_");
		gridParameters.element = gridParameters.modifiedPath+"_grid";
		
		$("#"+elementSearchName+"_grid").attr('id', gridParameters.element);
		
		translations = _this.options.translations;
		belongsToFieldName = _this.options.belongsToFieldName;	
		
		headerController = new QCD.components.elements.grid.GridHeaderController(_this, mainController, gridParameters, _this.options.translations);
		
		$("#"+elementSearchName+"_gridHeader").append(headerController.getHeaderElement());
		$("#"+elementSearchName+"_gridFooter").append(headerController.getFooterElement());
		
		currentState.firstEntity = headerController.getPagingParameters()[0];
		currentState.maxEntities = headerController.getPagingParameters()[1];

		gridParameters.onSelectRow = function(id){
			rowClicked(id);
        }
		gridParameters.ondblClickRow = function(id){
			linkClicked(id);
        }
		gridParameters.onSortCol = onSortColumnChange;
		
		grid = $("#"+gridParameters.element).jqGrid(gridParameters);
		
		$("#cb_"+gridParameters.element).hide(); // hide 'select add' checkbox
		$("#jqgh_cb").hide();
		
		for (var i in gridParameters.sortColumns) {
			$("#"+gridParameters.modifiedPath+"_grid_"+gridParameters.sortColumns[i]).addClass("sortableColumn");
		}
		
		element.width("100%");
		
		grid.jqGrid('filterToolbar',{
			stringResult: true
		});
		if (gridParameters.isLookup) {
			headerController.setFilterActive();
			currentState.filtersEnabled = true;
		} else {
			grid[0].toggleToolbar();
			currentState.filtersEnabled = false;
		}
		
		noRecordsDiv = $("<div>").html(translations.noResults).addClass("noRecordsBox");
		noRecordsDiv.hide();
		$("#window_orders_grid").parent().append(noRecordsDiv);
	}
	
	this.onPagingParametersChange = function() {
		blockGrid();
		currentState.firstEntity = headerController.getPagingParameters()[0];
		currentState.maxEntities = headerController.getPagingParameters()[1];
		onCurrentStateChange();
	}
	
	function setSortColumnAndDirection(order) {
		if(currentOrder && currentOrder.column == order.column) {
			if (order.direction == "asc") {
				$("#"+elementSearchName+"_sortArrow_"+order.column).removeClass("downArrow");
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("upArrow");
				currentState.order.direction = "asc";
			} else {
				$("#"+elementSearchName+"_sortArrow_"+order.column).removeClass("upArrow");
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("downArrow");
				currentState.order.direction = "desc";
			}
		} else {
			if(currentOrder) {
				$("#"+gridParameters.modifiedPath+"_grid_"+currentOrder.column).removeClass("sortColumn");
			}
			
			$("#"+gridParameters.modifiedPath+"_grid_"+order.column).addClass("sortColumn");
			
			currentState.order = { column: order.column }
			
			if (order.direction == "asc") {
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("upArrow");
				currentState.order.direction = "asc";
			} else {
				$("#"+elementSearchName+"_sortArrow_"+order.column).addClass("downArrow");
				currentState.order.direction = "desc";
			}
		}
		currentOrder = { column: order.column, direction: order.direction };
	}
	
	function onSortColumnChange(index,iCol,sortorder) {		
		blockGrid();
		currentState.order.column = index;
		if (currentState.order.direction == "asc") {
			currentState.order.direction = "desc";
		} else {
			currentState.order.direction = "asc";
		}
		onCurrentStateChange();
		return 'stop';
	}
	
	function onFilterChange() {
		if (filterRefreshTimeout) {
			window.clearTimeout(filterRefreshTimeout);
			filterRefreshTimeout = null;
		}
		filterRefreshTimeout = window.setTimeout(function() {
			filterRefreshTimeout = null;
			performFilter();
		}, FILTER_TIMEOUT);	
	}
	
	function performFilter() {
		blockGrid();
		if (currentState.filtersEnabled) {
			currentState.filters = new Object();
			for (var i in columnModel) {
				var column = columnModel[i];
				if (column.isSerchable) {
					var filterValue = $("#gs_"+column.name).val();
					filterValue = $.trim(filterValue);
					if (filterValue && filterValue != "") {
						currentState.filters[column.name] = filterValue;
					}
				}
			}
		} else {
			currentState.filters = null;
		}
		onCurrentStateChange();
	}
	
	this.onFilterButtonClicked = function() {
		grid[0].toggleToolbar();
		currentState.filtersEnabled = ! currentState.filtersEnabled;
		if (currentState.filtersEnabled) {
			currentGridHeight -= 23;
			updateSearchFields();
		} else {
			currentGridHeight += 23;
		}
		grid.setGridHeight(currentGridHeight);
		onCurrentStateChange();
	}
	
	function updateSearchFields() {
		for (var i in columnModel) {
			var column = columnModel[i];
			if (column.isSerchable) {
				var columnElement = $("#gs_"+column.name);
				columnElement.unbind('change keyup');
				if (column.filterValues) {
					columnElement.change(onFilterChange);
				} else {
					columnElement.keyup(function(e) {
						var val = $(this).val();
						var columnName = $(this).attr("id").substring(3);
						var currentFilter = "";
						if (currentState.filters && currentState.filters[columnName]) {
							currentFilter = currentState.filters[columnName];
						}
						if (currentState.filters && val == currentFilter) {
							return;
						}
						onFilterChange();
					});
				}
			} else {
				$("#gs_"+column.name).hide();
			}
		}
	}
	
	this.setFilterState = function(column, filterText) {
		if (! currentState.filtersEnabled) {
			grid[0].toggleToolbar();
			currentState.filtersEnabled = true;
			headerController.setFilterActive();
			currentGridHeight -= 21;
			grid.setGridHeight(currentGridHeight);
		}
		currentState.filters = new Object();
		currentState.filters[column] = filterText;
		$("#gs_"+column).val(filterText);
	}
	
	this.setFilterObject = function(filter) {
		blockGrid();
		
		var filterObject = filter.filter
		for (var i in columnModel) {
			var column = columnModel[i];
			$("#gs_"+column.name).val("");
		}
		var fieldsNo = 0;
		for (var col in filterObject) {
			filterObject[col] = Encoder.htmlDecode(filterObject[col]);
			$("#gs_"+col).val(filterObject[col]);
			fieldsNo++;
		}
		currentState.filters = filterObject;
		
		if (fieldsNo == 0) {
			if (currentState.filtersEnabled) {
				currentGridHeight += 23;
				grid.setGridHeight(currentGridHeight);
				grid[0].toggleToolbar();
			}
			headerController.setFilterNotActive();
			currentState.filtersEnabled = false;
		} else {
			if (! currentState.filtersEnabled) {
				currentGridHeight -= 23	;
				grid.setGridHeight(currentGridHeight);
				grid[0].toggleToolbar();
			}
			headerController.setFilterActive();
			currentState.filtersEnabled = true;
		}
		
		setSortColumnAndDirection({column: filter.orderColumn, direction: filter.orderDirection});
		
		updateSearchFields();
		onCurrentStateChange(true);
	}
	
	this.onNewButtonClicked = function() {
		performNew();
	}
	
	this.onDeleteButtonClicked = function() {
		 performDelete();
	}
	
	this.onUpButtonClicked = function() {
		blockGrid();
		mainController.callEvent("moveUp", elementPath, function() {
			unblockGrid();
		});
	}
	
	this.onDownButtonClicked = function() {
		blockGrid();
		mainController.callEvent("moveDown", elementPath, function() {
			unblockGrid();
		});
	}
	
	this.updateSize = function(_width, _height) {
		if (! _width) {
			_width = 300;
		}
		if (! _height) {
			_height = 300;
		}
		
		element.css("height",_height+"px")
		
		var HEIGHT_DIFF = 120;
		currentGridHeight = _height - HEIGHT_DIFF;
		if (currentState.filtersEnabled) {
			currentGridHeight -= 21;
		}
		if (! gridParameters.paging) {
			currentGridHeight += 35;
		}
		grid.setGridHeight(currentGridHeight);
		
		grid.setGridWidth(_width-24, RESIZE_COLUMNS_ON_UPDATE_SIZE);
	}
	
	function onCurrentStateChange(forceUpdate) {
		if (!forceUpdate) {
			findMatchingPredefiniedFilter();
		}
		if (componentEnabled) {
			mainController.callEvent("refresh", elementPath, function() {
				unblockGrid();
			});
		}
	}
	
	function findMatchingPredefiniedFilter() {
		var filterToSearch = {};
		if (currentState.filtersEnabled && currentState.filters) {
			filterToSearch = currentState.filters;
		}
		var isIdentical = true;
		for (var i in gridParameters.predefinedFilters) {
			var predefiniedFilter = gridParameters.predefinedFilters[i].filter;
			isIdentical = true;
			
			if (gridParameters.predefinedFilters[i].orderColumn) {
				if (currentState.order.column != gridParameters.predefinedFilters[i].orderColumn) {
					isIdentical = false;
					continue;
				}
				if (currentState.order.direction != gridParameters.predefinedFilters[i].orderDirection) {
					isIdentical = false;
					continue;
				}
			}
			
			for (var col in columnModel) {
				var column = columnModel[col];
				if (predefiniedFilter[column.name] != filterToSearch[column.name]) {
					isIdentical = false;
					break;
				}
			}
			if (isIdentical) {
				headerController.setPredefinedFilter(i);
				break;
			}
		}
		if (! isIdentical) {
			headerController.setPredefinedFilter(null);
		}
	}
	
	function onSelectChange() {
		if (componentEnabled) {
			mainController.callEvent("select", elementPath, null);
		}
	}
	
	this.performNew = function(actionsPerformer) {
		var params = new Object();
		params[gridParameters.correspondingComponent+"."+belongsToFieldName] = currentState.belongsToEntityId;
		redirectToCorrespondingPage(params);	
		if (actionsPerformer) {
			actionsPerformer.performNext();
		}
	}
	var performNew = this.performNew;
	
	
	this.performDelete = function(actionsPerformer) {
		if (currentState.selectedEntityId) {
			if (window.confirm(translations.confirmDeleteMessage)) {
				blockGrid();
				mainController.callEvent("remove", elementPath, function() {
					unblockGrid();
				}, null, actionsPerformer);
			}
		} else {
			mainController.showMessage({type: "error", content: translations.noRowSelectedError});
		}	
	}
	var performDelete = this.performDelete;
	
	this.fireEvent = function(actionsPerformer, eventName, args) {
		blockGrid();
		mainController.callEvent(eventName, elementPath, function() {
			unblockGrid();
		}, args, actionsPerformer);
	}
	
	this.performLinkClicked = function(actionsPerformer) {
		if (currentState.selectedEntityId) {
			
			linkClicked(currentState.selectedEntityId);
			
			if (actionsPerformer) {
				actionsPerformer.performNext();
			}
		} else {
			mainController.showMessage({type: "error", content: translations.noRowSelectedError});
		}	
	}
	
	this.getLookupData = function(entityId) {
		var result = Object();
		result.entityId = entityId;
		result.lookupValue = hiddenColumnValues["lookupValue"][entityId];
		var lookupCodeLink = grid.getRowData(entityId).lookupCode;
		lookupCodeLink = lookupCodeLink.replace(/^<a[^>]*>/,"");
		lookupCodeLink = lookupCodeLink.replace(/<\/a>$/,"");
		result.lookupCode = lookupCodeLink;
		return result;
	}

	constructor(this);
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.LinkButton = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;
	
	var element = _element;
	
	var elementPath = this.elementPath;
	var elementName = this.elementName;
	
	var pageUrl;
	
	var button = $("#"+this.elementSearchName+"_buttonDiv");
	var buttonLink = $("#"+this.elementSearchName+"_buttonLink");
	
	this.getComponentValue = function() {
		return { value: {}};
	}
	
	this.setComponentValue = function(value) {
		insertValue(value);
	}
	
	this.setComponentState = function(state) {
		insertValue(state);
	}
	
	function insertValue(value) {
		pageUrl = value.value;
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			button.addClass('activeButton');
		} else {
			button.removeClass('activeButton');
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {

	}
	
	function onButtonClick(e) {
		buttonLink.blur();
		if (button.hasClass('activeButton')) {
			mainController.goToPage(pageUrl);
		}
	}
	
	function constructor(_this) {		
		buttonLink.click(onButtonClick);
	}
	
	constructor(this);
}


var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.lookup = QCD.components.elements.lookup || {};

QCD.components.elements.lookup.Dropdown = function(_lookupDropdownElement, _controller, _translations) {
	
	var MAX_RESULTS = 25;
	var PAGE_RESULT = 4;
	var RESULT_HEIGHT = 25;
	
	var lookupDropdownElement = _lookupDropdownElement;
	
	var controller = _controller;
	
	var translations = _translations;
	
	var selectedElement;
	
	var mouseSelectedElement;
	
	var autocompleteMatches;
	
	function constructor() {
		lookupDropdownElement.css("top", "21px");
	}
	
	this.updateAutocomplete = function(_autocompleteMatches, _autocompleteEntitiesNumber) {
		
		autocompleteMatches = _autocompleteMatches;
		
		selectedElement = null;
		
		lookupDropdownElement.children().remove();
		lookupDropdownElement.scrollTop(0);
		
		if (_autocompleteEntitiesNumber > MAX_RESULTS) {
			var noRecordsElement = $("<div>").addClass("lookupMatch_noRecords").html(translations.tooManyResultsInfo+" ("+_autocompleteEntitiesNumber+")");
			lookupDropdownElement.append(noRecordsElement);
			lookupDropdownElement.css("height", (RESULT_HEIGHT-1)+"px");
		} else if (autocompleteMatches.length == 0) {
			var noRecordsElement = $("<div>").addClass("lookupMatch_noRecords").html(translations.noResultsInfo);
			lookupDropdownElement.append(noRecordsElement);
			lookupDropdownElement.css("height", (RESULT_HEIGHT-1)+"px");
		} else {
			if (autocompleteMatches.length > PAGE_RESULT) {
				lookupDropdownElement.css("height", (RESULT_HEIGHT*PAGE_RESULT-1)+"px");
				lookupDropdownElement.css("overflow", "auto");
			} else {
				lookupDropdownElement.css("height", (autocompleteMatches.length*RESULT_HEIGHT-1)+"px");
				lookupDropdownElement.css("overflow", "hidden");
			}
			for (var i in autocompleteMatches) {
				var entity = autocompleteMatches[i];
				var matchElement = $("<div>").addClass("lookupMatch").html(entity.value).attr("id", controller.elementPath+"_autocompleteOption_"+i);
				
				matchElement.mouseover(function() {
					$(this).addClass("lookupMatchHover");
					mouseSelectedElement = $(this);
				});
				matchElement.mouseout(function() {
					$(this).removeClass("lookupMatchHover");
					mouseSelectedElement = null;
				});
				matchElement.click(function() {
					// do nothing, blur will perform action 
				});
				
				lookupDropdownElement.append(matchElement);
			}
		}
	}
	
	this.selectNext = function() {
		if (! selectedElement) {
			nextElement = $(lookupDropdownElement.children()[0]);
		} else {
			var nextElement = selectedElement.next();
		}
		if (! nextElement || nextElement.length == 0) {
			return;
		}
		if (selectedElement) {
			selectedElement.removeClass("lookupMatchHover");	
		}
		selectedElement = nextElement;
		selectedElement.addClass("lookupMatchHover");
		if (selectedElement.position().top < 0) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top);
		}
		if (selectedElement.position().top >= 100) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top - 75);
		}
	}
	
	this.selectPrevious = function() {
		if (! selectedElement) {
			nextElement = $(lookupDropdownElement.children()[lookupDropdownElement.children().length - 1]);
		} else {
			var nextElement = selectedElement.prev();
		}
		if (! nextElement || nextElement.length == 0) {
			return;
		}
		if (selectedElement) {
			selectedElement.removeClass("lookupMatchHover");	
		}
		selectedElement = nextElement;
		selectedElement.addClass("lookupMatchHover");
		if (selectedElement.position().top < 0) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top);
		}
		if (selectedElement.position().top >= 100) {
			lookupDropdownElement.scrollTop(lookupDropdownElement.scrollTop() + selectedElement.position().top - 75);
		}
	}
	
	this.getSelected = function() {
		if (selectedElement) {
			var id = selectedElement.attr("id").substring((controller.elementPath+"_autocompleteOption_").length);
			return autocompleteMatches[id];
		}
		return null;
	}
	
	this.getMouseSelected = function() {
		if (mouseSelectedElement) {
			var id = mouseSelectedElement.attr("id").substring((controller.elementPath+"_autocompleteOption_").length);
			return autocompleteMatches[id];
		}
		return null;
	}
	
	this.hide = function() {
		//lookupDropdownElement.hide();
		lookupDropdownElement.slideUp(400);
	}
	
	this.show = function() {
		//lookupDropdownElement.show();
		lookupDropdownElement.slideDown(400);
	}
	
	this.isOpen = function() {
		return lookupDropdownElement.is(':visible');
	}
	
	constructor();
}

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Lookup = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var element = _element;
	var elementPath = this.elementPath;
	
	var translations = this.options.translations;
	
	var AUTOCOMPLETE_TIMEOUT = 200;
	
	var keyboard = {
		UP: 38,
		DOWN: 40,
		ENTER: 13,
		ESCAPE: 27
	};
	
	var elements = {
		input: this.input,
		loading: $("#"+this.elementSearchName+"_loadingDiv"),
		label: $("#"+this.elementSearchName+"_labelDiv"),
		openLookupButton: $("#"+this.elementSearchName+"_openLookupButton"),
		lookupDropdown: $("#"+this.elementSearchName+"_lookupDropdown")
	};
	
	var labels = {
		normal: elements.label.html(),
		focus: "<span class='focusedLabel'>"+this.options.translations.labelOnFocus+"</span>"
	};
	
	var viewState = {
		isFocused: false,
		error: null
	};
	
	var dataState = {
		currentCode: null,
		selectedEntity: {
			id: null,
			value: null,
			code: null
		},
		autocomplete: {
			matches: null,
			code: null,
			entitiesNumber: null
		},
		contextEntityId: null
	}
	
	var autocompleteRefreshTimeout = null;
	
	var blurAfterLoad = false;
	
	var lookupDropdown = new QCD.components.elements.lookup.Dropdown(elements.lookupDropdown, this, translations);
	
	var hasListeners = (this.options.listeners.length > 0) ? true : false;
	
	var _this = this;
	
	var lookupWindow;
	
	var baseValue;
	
	function constructor(_this) {
		
		elements.openLookupButton.click(openLookup);
		
		elements.input.focus(function() {
			viewState.isFocused = true;
			onViewStateChange();
		}).blur(function() {
			viewState.isFocused = false;
			onViewStateChange();
		});
		
		elements.input.keyup(function(e) {
			var key = getKey(e);
			if (key == keyboard.UP) {
				if (! lookupDropdown.isOpen()) {
					onInputValueChange(true);
				}
				lookupDropdown.selectPrevious();
				
			} else if (key == keyboard.DOWN) {
				if (! lookupDropdown.isOpen()) {
					onInputValueChange(true);
				}
				lookupDropdown.selectNext();
				
			} else if (key == keyboard.ENTER) {
				if (! lookupDropdown.isOpen()) {
					return;
				}
				var entity = lookupDropdown.getSelected();
				if (entity == null) {
					return;
				}
				performSelectEntity(entity);
				dataState.currentCode = dataState.selectedEntity.code;
				elements.input.val(dataState.currentCode);
				lookupDropdown.hide();
				
			} else if (key == keyboard.ESCAPE) {
				preventEvent(e);
				elements.input.val(dataState.currentCode);
				lookupDropdown.hide();
			} else {
				var inputVal = elements.input.val();
				if (dataState.currentCode != inputVal) {
					dataState.currentCode = inputVal;
					performSelectEntity(null);
					onInputValueChange();
				} 
			}
		});
		
		// prevent event propagation
		elements.input.keydown(function(e) {
			var key = getKey(e);
			if (key == keyboard.UP || key == keyboard.ESCAPE) {
				preventEvent(e);
				return false;
			}
		}).keypress(function(e) {
			var key = getKey(e);
			if (key == keyboard.UP || key == keyboard.ESCAPE) {
				preventEvent(e);
				return false;
			}
		});
	}
	
	
	
	this.getComponentData = function() {
		return {
			value: dataState.selectedEntity.id,
			selectedEntityValue: dataState.selectedEntity.value,
			selectedEntityCode: dataState.selectedEntity.code,
			currentCode: dataState.currentCode,
			autocompleteCode: dataState.autocomplete.code,
			contextEntityId: dataState.contextEntityId
		};
	}
	
	this.setComponentData = function(data) {
		dataState.currentCode = data.currentCode ? data.currentCode : dataState.currentCode;
		dataState.selectedEntity.id = data.value ? data.value : null;
		dataState.selectedEntity.value = data.selectedEntityValue;
		dataState.selectedEntity.code = data.selectedEntityCode;
		dataState.autocomplete.matches = data.autocompleteMatches ? data.autocompleteMatches : [];
		dataState.autocomplete.code = data.autocompleteCode ? data.autocompleteCode : "";
		dataState.autocomplete.entitiesNumber = data.autocompleteEntitiesNumber;
		if (dataState.contextEntityId != data.contextEntityId) {
			dataState.contextEntityId = data.contextEntityId;
			dataState.currentCode = "";
		}
		
		// initialaize current code on first load
		if (! dataState.currentCode) {
			dataState.currentCode = dataState.selectedEntity.id ? dataState.selectedEntity.code : "";
		}
		
		onDataStateChange();
	}
	
	this.performUpdateState = function() {
		baseValue = {
			currentCode: dataState.currentCode
		};
	}
	
	this.isComponentChanged = function() {
		return ! (dataState.currentCode == baseValue.currentCode);
	}
	
	function onViewStateChange() {
		if (viewState.isFocused && !elements.input.attr("readonly")) {
			elements.openLookupButton.addClass("lightHover");
			elements.label.html(labels.focus);
			elements.input.val(dataState.currentCode);
		} else {
			elements.openLookupButton.removeClass("lightHover");
			lookupDropdown.hide();
			
			if (autocompleteRefreshTimeout || elements.loading.is(':visible')) {
				blurAfterLoad = true;
				return;
			}
			
			viewState.error = null;
			if (! dataState.selectedEntity.id && ! lookupDropdown.getSelected() && ! lookupDropdown.getMouseSelected() 
					&& dataState.autocomplete.matches && dataState.currentCode != "") {
				if (dataState.autocomplete.matches.length == 0) {
					viewState.error = translations.noMatchError;
				} else if (dataState.autocomplete.matches.length > 1) {
					viewState.error = translations.moreTahnOneMatchError;
				} else {
					performSelectEntity(dataState.autocomplete.matches[0]);
				}
			}
			
			if (viewState.error == null) {
				elements.label.html(labels.normal);
				if (dataState.selectedEntity.id) {
					
				} else if (lookupDropdown.getMouseSelected()) {
					performSelectEntity(lookupDropdown.getMouseSelected());
					dataState.currentCode = lookupDropdown.getMouseSelected().code;
				} else if (lookupDropdown.getSelected()) {
					performSelectEntity(lookupDropdown.getSelected());
					dataState.currentCode = lookupDropdown.getSelected().code;
				}
				elements.input.val(stripHTML(dataState.selectedEntity.value));
			} else {
				_this.addMessage({
					title: "",
					content: viewState.error
				});
				element.addClass("error");
			}
		}
	}
	
	function onDataStateChange() {
		if (dataState.autocomplete.code == dataState.currentCode) {
			elements.loading.hide();	
		}
		if (dataState.selectedEntity.id) {
			element.removeClass("error");
		}
		if (blurAfterLoad) {
			blurAfterLoad = false;
			viewState.isFocused = false;
			lookupDropdown.updateAutocomplete(dataState.autocomplete.matches, dataState.autocomplete.entitiesNumber);
			onViewStateChange();
			return;
		}
		if (viewState.isFocused) {
			lookupDropdown.updateAutocomplete(dataState.autocomplete.matches, dataState.autocomplete.entitiesNumber);
			lookupDropdown.show();
		} else {
			elements.input.val(stripHTML(dataState.selectedEntity.value));
		}
	}
	
	function onInputValueChange(immidiateRefresh) {
		if (autocompleteRefreshTimeout) {
			window.clearTimeout(autocompleteRefreshTimeout);
			autocompleteRefreshTimeout = null;
		}
		if (immidiateRefresh) {
			elements.loading.show();
			mainController.callEvent("autompleteSearch", elementPath, null, null, null);	
		} else {
			autocompleteRefreshTimeout = window.setTimeout(function() {
				autocompleteRefreshTimeout = null;
				elements.loading.show();
				mainController.callEvent("autompleteSearch", elementPath, null, null, null);
			}, AUTOCOMPLETE_TIMEOUT);	
		}
	}
	
	function performSelectEntity(entity, callEvent) {
		if (callEvent == undefined) {
			callEvent = true;
		}
		if (entity) {
			dataState.selectedEntity.id = entity.id;
			dataState.selectedEntity.code = entity.code;
			dataState.selectedEntity.value = entity.value;	
		} else {
			dataState.selectedEntity.id = null;
			dataState.selectedEntity.code = null;
			dataState.selectedEntity.value = null;
		}
		if (hasListeners && callEvent) {
			mainController.callEvent("onSelectedEntityChange", elementPath, null, null, null);
		}
	}
	
	function stripHTML(text){
		if (!text || text == "") {
			return "";
		}
		var re= /<\S[^><]*>/g
		return text.replace(re, "");
	}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().parent().parent().height(height);
	}
	
	function preventEvent(e) {
		e.preventDefault();
		e.stopImmediatePropagation();
		e.stopPropagation();
		e.keyCode = 0;
		e.which = 0;
		e.returnValue = false;
	}
	
	function getKey(e) {
		return e.keyCode || e.which;
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			elements.openLookupButton.addClass("enabled")
		} else {
			elements.openLookupButton.removeClass("enabled")
		}
	}
	
	function openLookup() {
		if (! elements.openLookupButton.hasClass("enabled")) {
			return;
		}
		var url = _this.options.viewName+".html";
		if (dataState.contextEntityId) {
			var params = new Object();
			params["window.grid.belongsToEntityId"] = dataState.contextEntityId;
			url += "?context="+JSON.stringify(params);
	}		
		lookupWindow = mainController.openPopup(url, _this, "lookup");
	}
	
	this.onPopupInit = function() {
		var grid = lookupWindow.getComponent("window.grid");
		grid.setLinkListener(this);
		if (dataState.currentCode) {
			grid.setFilterState("lookupCode", dataState.currentCode);	
		}
		lookupWindow.init();
	}
	
	this.onPopupClose = function() {
		lookupWindow = null;
	}
	
	this.onGridLinkClicked = function(entityId) {
		var grid = lookupWindow.getComponent("window.grid");
		var lookupData = grid.getLookupData(entityId);
		performSelectEntity({
			id: lookupData.entityId,
			code: lookupData.lookupCode,
			value: lookupData.lookupValue
		});
		dataState.currentCode = lookupData.lookupCode;
		onDataStateChange();
		onViewStateChange();
		if (hasListeners) {
			mainController.callEvent("onSelectedEntityChange", elementPath, null, null, null);
		}
		mainController.closePopup();
	}
	
	constructor(this);
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.PasswordInput = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	this.setComponentData = function(data) {
		this.input.val("");
	}
	
}

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.containers.layout.SeperatorLine = function(_element, _mainController) {
	$.extend(this, new QCD.components.containers.layout.Layout(_element, _mainController));

}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.StaticComponent = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	

	this.setComponentState = function(state) {
	}
	this.getComponentValue = function() {
		return null;
	}
	this.setComponentValue = function(value) {
	}
	this.setComponentEnabled = function(_isEnabled) {
	}
	this.setComponentLoading = function(isLoadingVisible) {
	}
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.TextArea = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 90;
		if (height < 50) {
			this.input.height(22); // same as input['text']
		} else {
			this.input.height(height-23);
		}
		this.input.parent().parent().parent().height(height);
	}
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.TextInput = function(_element, _mainController) {
	$.extend(this, new QCD.components.elements.FormComponent(_element, _mainController));
	
	var textRepresentation = $("#" + this.elementSearchName + "_text");
	
	var input = this.input;
	
	this.getComponentData = function() {
		return {
			value : input.val()
		}
	}
	
	this.setComponentData = function(data) {
		if (data.value != undefined && data.value != null) {
			input.val(data.value);
			textRepresentation.html(data.value);
		}
	}
	
	this.setFormComponentEnabled = function(isEnabled) {
		if(this.options.textRepresentationOnDisabled) {
			if(isEnabled) {
				input.show();
				textRepresentation.hide();
			} else {
				input.hide();
				textRepresentation.show();
			}
		}
	}
	
	this.updateSize = function(_width, _height) {
		var height = _height ? _height-10 : 40;
		this.input.parent().parent().parent().height(height);
		this.input.parent().parent().height(height);
	}
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.Tree = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));
	
	var mainController = _mainController;
	
	var element = _element;
	
	var tree;
	
	var header;
	var buttons = new Object();
	
	var contentElement;
	
	var belongsToEntityId;
	var belongsToFieldName = this.options.belongsToFieldName;
	
	var correspondingView = this.options.correspondingView;
	var correspondingComponent = this.options.correspondingComponent;

	var elementPath = this.elementPath;
	var elementSearchName = this.elementSearchName;

	var root;
	
	var isEnabled = false;
	
	var listeners = this.options.listeners;
	
	var openedNodesArrayToInsert;
	var selectedNodeToInstert;
	
	var fireSelectEvent = true;
	
	var translations = this.options.translations;
	
	function constructor(_this) {
		header = $("<div>").addClass('tree_header').addClass('elementHeader').addClass("elementHeaderDisabled");
			
			var title = $("<div>").addClass('tree_title').addClass('elementHeaderTitle').html(translations.header);
			header.append(title);
			
			buttons.newButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.newButton, function(e) {
				newClicked();
			}, "newIcon16_dis.png");
			buttons.editButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.editButton, function(e) {
				editClicked();
			}, "editIcon16_dis.png");
			buttons.deleteButton = QCD.components.elements.utils.HeaderUtils.createHeaderButton(translations.deleteButton, function(e) {
				deleteClicked();
			}, "deleteIcon16_dis.png");
			
			header.append(buttons.newButton);
			header.append(buttons.editButton);
			header.append(buttons.deleteButton);
		
		contentElement = $("<div>").addClass('tree_content');
		
		var container = $("<div>").addClass('tree_wrapper');
		
		container.append(header);
		container.append(contentElement);
		
		element.append(container);
		element.css("padding", "10px");
		
		tree = contentElement.jstree({ plugins : ["json_data", "themes", "crrm", "ui", /*"hotkeys"*/ ],
			"themes" : {
				"theme": "classic",
				"dots" : false,
				"icons" : false
			},
			"json_data" : {
				"data" : [ ]
			},
//			"hotkeys" : {
//				"f2" : function () { },
//				"del" : function () { }
//				"up": function(){
//					var o = this.data.ui.last_selected || -1;
//					this.deselect_node(o);
//					this.select_node(this._get_prev(o));
//					return false; 
//				},
//				"down" : function () { 
//					var o = this.data.ui.last_selected || -1;
//					this.deselect_node(o);
//					this.select_node(this._get_next(o));
//					//tree.jstree("select_node", this._get_next(o), false);
//					return false;
//				}
//			},
			"ui": {
				"select_limit": 1
			},
			core : {
				html_titles: true,
				animation: 100
			},
		    cookies: false
		}).bind("before.jstree", function (e, data) {
			if (!isEnabled && (data.func == 'select_node' || data.func == 'hover_node')) { 
				e.stopImmediatePropagation();
		    	return false;
			}
		}).bind("select_node.jstree", function (e, data) {
			if (fireSelectEvent) {
				updateButtons();
				if (listeners.length > 0) {
					onSelectChange();
				}
			}
		});
	}
	
	this.setComponentState = function(state) {
		openedNodesArrayToInsert = state.openedNodes;
		selectedNodeToInstert = state.selectedEntityId;
		belongsToEntityId = state.belongsToEntityId;
	}
	
	this.getComponentValue = function() {
		var openedNodesArray;
		if (openedNodesArrayToInsert) {
			openedNodesArray = openedNodesArrayToInsert;
			openedNodesArrayToInsert = null;
		} else {
			openedNodesArray = new Array();
			tree.find(".jstree-open").each(function () { 
				openedNodesArray.push(getEntityId(this.id));
			});
		}
		var selectedNode;
		if (selectedNodeToInstert) {
			selectedNode = selectedNodeToInstert;
			selectedNodeToInstert = null;
		} else {
			selectedNode = getSelectedEntityId();
		}
		return {
			openedNodes: openedNodesArray,
			selectedEntityId: selectedNode,
			belongsToEntityId: belongsToEntityId
		}
	}
	
	this.setComponentValue = function(value) {
		
		if (value.belongsToEntityId) {
			belongsToEntityId = value.belongsToEntityId;
		}
		
		if (value.root) {
			if (root) {
				tree.jstree("remove", root); 
			}
			root = addNode(value.root, -1);
		}
		
		tree.jstree("close_all", root, true);
		for (var i in value.openedNodes) {
			tree.jstree("open_node", $("#"+elementSearchName+"_node_"+value.openedNodes[i]), false, true);
		}
		
		if (value.selectedEntityId != null) {
			fireSelectEvent = false;
			tree.jstree("select_node", $("#"+elementSearchName+"_node_"+value.selectedEntityId), false);
			fireSelectEvent = true;
		}
		
		updateButtons();
		unblock();
	}
	
	function addNode(data, node) {
		var nodeId = data.id ? data.id : "0";
		var newNode = tree.jstree("create", node, "last", {data: {title: data.label}, attr : { id: elementPath+"_node_"+nodeId }}, false, true);
		for (var i in data.children) {
			addNode(data.children[i], newNode, false);
		}
		tree.jstree("close_node", newNode, true);
		return newNode;
	}
	
	function updateButtons() {
		var selected = getSelectedEntityId();
		if (!selected) {
			buttons.newButton.removeClass("headerButtonEnabled");
			buttons.editButton.removeClass("headerButtonEnabled");
			buttons.deleteButton.removeClass("headerButtonEnabled");
		} else {
			buttons.newButton.addClass("headerButtonEnabled");
			if (selected != "0") {
				buttons.editButton.addClass("headerButtonEnabled");
				buttons.deleteButton.addClass("headerButtonEnabled");
			} else {
				buttons.editButton.removeClass("headerButtonEnabled");
				buttons.deleteButton.removeClass("headerButtonEnabled");
			}
		}
	}
	
	this.setComponentEnabled = function(_isEnabled) {
		isEnabled = _isEnabled;
		if (isEnabled) {
			tree.removeClass("treeDisabled");
			header.removeClass("elementHeaderDisabled");
		} else {
			tree.addClass("treeDisabled");
			header.addClass("elementHeaderDisabled");
			buttons.newButton.removeClass("headerButtonEnabled");
			buttons.editButton.removeClass("headerButtonEnabled");
			buttons.deleteButton.removeClass("headerButtonEnabled");
		}
	}
	
	function newClicked() {
		if (buttons.newButton.hasClass("headerButtonEnabled")) {
			var params = new Object();
			if (belongsToFieldName) {
				params[correspondingComponent+"."+belongsToFieldName] = belongsToEntityId;
			}
			var entityId = getSelectedEntityId();
			entityId = entityId=="0" ? null : entityId;
			params[correspondingComponent+".parent"] = entityId;
			redirectToCorrespondingPage(params);
		}
	}
	
	function editClicked() {
		if (buttons.editButton.hasClass("headerButtonEnabled")) {
			var params = new Object();
			params[correspondingComponent+".id"] = getSelectedEntityId();
			redirectToCorrespondingPage(params);
		}
	}
	
	function deleteClicked() {
		var confirmDeleteMessage = translations.confirmDeleteMessage;
		if (buttons.deleteButton.hasClass("headerButtonEnabled")) {
			if (window.confirm(confirmDeleteMessage)) {
				block();
				mainController.callEvent("remove", elementPath, function() {
					unblock();
				}, null, null);
			}
		}
	}	
	
	function onSelectChange() {
		if (isEnabled) {
			mainController.callEvent("select", elementPath, null);
		}
	}
	
	function getSelectedEntityId() {
		var selected = tree.jstree("get_selected");
		if (selected && selected.length > 0) {
			return getEntityId(selected.attr("id"));
		}
		return null;
	}
	function getEntityId(nodeId) {
		return nodeId.substring(elementPath.length + 6);
	}
	
	function redirectToCorrespondingPage(params) {
		if (correspondingView && correspondingView != '') {
			var url = correspondingView + ".html";
			if (params) {
				url += "?context="+JSON.stringify(params);
			}
			mainController.goToPage(url);
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
		if (isLoadingVisible) {
			block();
		} else {
			unblock();
		}
	}
	
	this.updateSize = function(_width, _height) {
		if (! _width) {
			_width = 300;
		}
		if (! _height) {
			_height = 300;
		}
		//element.css("height",_height+"px")
		contentElement.height(_height - 52);
	}
	
	function block() {
		isEnabled = false;
		QCD.components.elements.utils.LoadingIndicator.blockElement(element);
	}
	
	function unblock() {
		QCD.components.elements.utils.LoadingIndicator.unblockElement(element);
		isEnabled = true;
	}
	
	constructor(this);
}

/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.utils = QCD.components.elements.utils || {};

QCD.components.elements.utils.HeaderUtils = {};

QCD.components.elements.utils.HeaderUtils.createHeaderButton = function(label, clickAction, icon) {
	var elementIcon = (icon && $.trim(icon) != "") ? $.trim(icon) : null;
	
	var itemElementLabel = $('<div>');
	itemElementLabel.html(label);
	
	var itemElementSpan = $('<span>');
	
	var itemElementButton = $("<a>").attr('href','#').append(itemElementSpan);
	
	if (icon && $.trim(icon) != "") {
		itemElementLabel.addClass('hasIcon');
		itemElementSpan.append($('<div>').addClass('icon').css('backgroundImage', 'url(\'/img/core/icons/'+elementIcon+'\')'));
	}

	itemElementSpan.append(itemElementLabel);
	if (label == "") {
		itemElementLabel.css("paddingLeft", "0px");
		itemElementLabel.css("paddingRight", "3px");
	}
	itemElementButton.click(function() {
		itemElementButton.blur();
		clickAction.call();
	});
	
	var itemElementButtonWrapper = $("<div>").addClass("headerActionButton").append(itemElementButton);
	itemElementButtonWrapper.label = itemElementLabel;
	
	return itemElementButtonWrapper;
}

QCD.components.elements.utils.HeaderUtils.createHeaderComboBox = function(options, selectAction) {
	
	var select = $("<select>").addClass("headerSelect");
	select.change(function() {
		selectAction(select.val());
	});
	
	for (var i in options) {
		select.append($("<option>").attr("value",options[i].value).html(options[i].label));
	}
	
	select.enable = function() {
		$(this).attr("disabled", "");
	}
	select.disable = function() {
		$(this).attr("disabled", "true");
	}
	
	select.disable();
	
	return select;
}

var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.utils = QCD.components.elements.utils || {};

QCD.components.elements.utils.LoadingIndicator = {};

QCD.components.elements.utils.LoadingIndicator.blockElement = function(element) {
	element.block({ message: '<div class="loading_div">'+""+'</div>', showOverlay: true,  fadeOut: 0, fadeIn: 0,
		css: { 
	        border: 'none', 
	        padding: '15px', 
	        backgroundColor: '#000', 
	        '-webkit-border-radius': '10px', 
	        '-moz-border-radius': '10px', 
	        opacity: .5, 
	        color: '#fff',
	        width: '50px'
        },
        overlayCSS:  { 
            backgroundColor: '#000', 
            opacity:         0.1 
        }, 
	});
};

QCD.components.elements.utils.LoadingIndicator.unblockElement = function(element) {
	element.unblock();
};



/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

var QCD = QCD || {};
QCD.components = QCD.components || {};

QCD.components.Ribbon = function(_model, _elementName, _mainController) {
	
	QCD.info(_model);
	
	var ribbonModel = _model;
	var mainController = _mainController;
	var elementName = _elementName;
	
	var element;
	
	this.constructElement = function() {
		
		element = $("<div>");
		
		var contentWrapper = $("<div>").attr("id", "q_row3_out");
		element.append(contentWrapper);
		element.append($("<div>").attr("id", "q_row4_out"));
		
		var content = $("<div>").attr("id", "q_menu_row3");
		contentWrapper.append(content);
		
		if (ribbonModel.groups) {
			for (var groupIter in ribbonModel.groups) {
				var groupModel = ribbonModel.groups[groupIter];
				var groupContent = $("<div>").addClass("ribbon_content");
				var groupTitle = $("<div>").addClass("ribbon_title").html(groupModel.label);
				
				var ribbonMenu_right = $("<div>").addClass("ribbonMenu_right").append(groupTitle).append(groupContent);
				var ribbonMenu_left = $("<div>").addClass("ribbonMenu_left").append(ribbonMenu_right);
				var groupElement = $("<div>").addClass("ribbonMenu").append(ribbonMenu_left);
				
				var smallElementsGroupElement = null;
				for (var itemsIter in groupModel.items) {
					var itemModel = groupModel.items[itemsIter];
					
					var itemElement = null;
					var isSmall = false;
					
					if (itemModel.type == "BIG_BUTTON") {
						if (itemModel.items) {
							itemElement = createBigButtonWithDropdown(groupModel.name, itemModel);
						} else {
							itemElement = createBigButton(groupModel.name, itemModel);
						}
					} else if (itemModel.type == "SMALL_BUTTON") {
						if (itemModel.items) {
							itemElement = createSmallButtonWithDropdown(groupModel.name, itemModel);
						} else {
							itemElement = createSmallButton(groupModel.name, itemModel);
						}
						isSmall = true;
					} else if (itemModel.type == "COMBOBOX") {
						itemElement = createComboBox(groupModel.name, itemModel);
						isSmall = true;
					}
					
					if (itemElement) {
						if (isSmall) {
							if (smallElementsGroupElement) {
								smallElementsGroupElement.append(itemElement);
								smallElementsGroupElement = null;
							} else {
								smallElementsGroupElement = $("<ul>").addClass("ribbon_list");
								smallElementsGroupElement.append(itemElement);
								groupContent.append(smallElementsGroupElement);
							}
						} else {
							groupContent.append(itemElement);
							smallElementsGroupElement = null;
						}
					}
				}
				content.append(groupElement);
			}
		}
		return element;
	}
	
	function createBigButton(path, itemModel) {
		var aElement = $("<a>").attr('href','#').html("<span><div"+getItemIconStyle(itemModel)+"><label>"+itemModel.label+"</label><div></div></div></span>");
		var liElement = $("<li>").append(aElement);
		var ribbonListElement = $("<ul>").addClass("ribbonListElement").append(liElement);
		var itemElement = $("<div>").addClass("ribbonBigElement").append(ribbonListElement);
		aElement.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		return itemElement;
	}
	
	function createBigButtonWithDropdown(path, itemModel) {
		var icon = (itemModel.icon && $.trim(itemModel.icon) != "") ? $.trim(itemModel.icon) : null;
		var style = "";
		var className = "";
		if (icon) {
			style = " style=\"background-image:url(\'../../images/icons/"+icon+"\')\"";
			className = " hasIcon";
		}
		var itemElementButton = $("<a>").attr('href','#').html("<span><div class='"+className+" bigDropdownButtonDiv' "+style+"><label>"+itemModel.label+"</label><div></div></div></span>");
		var buttonLi = $("<li>").append(itemElementButton);
		var itemElementDropdownButton = $("<a>").attr('href','#').html("<span><div class='icon_btn_addB'></div></span>");
		var buttonDropdownLi = $("<li>").addClass("addB").append(itemElementDropdownButton);
		var ulElement = $("<ul>").append(buttonLi).append(buttonDropdownLi);
		var divElement = $("<div>").append(ulElement);
		var spanElement = $("<span>").append(divElement);
		var liElement = $("<li>").append(spanElement);
		var ribbonAddElement = $("<ul>").addClass("ribbonAddElement").append(liElement);
		var itemElement = $("<div>").addClass("ribbonBigElement").addClass("ribbonDropdownContainer").append(ribbonAddElement);
		
		var dropdownMenu = createDropdownMenu(path + "." + (itemModel.label ? itemModel.label : itemModel.name), itemModel).addClass("bigButtonDropdownMenu");
		addDropdownAction(itemElementDropdownButton);
		itemElement.append(dropdownMenu);
		
		itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);

		return itemElement;
	}
	
	function createSmallButton(path, itemModel) {
		var itemElementButton = $("<a>").attr('href','#').html("<span><div"+getItemIconStyle(itemModel)+">"+itemModel.label+"</div></span>");
		var itemElement = $("<li>").addClass("btnOne").append(itemElementButton);
		itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		return itemElement;
	}
	
	function createSmallButtonWithDropdown(path, itemModel) {
		var itemElementButton = $("<a>").attr('href','#').html("<span><div "+getItemIconStyle(itemModel)+">"+itemModel.label+"</div></span>");
		var buttonLi = $("<li>").append(itemElementButton);
		var itemElementDropdownButton = $("<a>").attr('href','#').addClass("twoB_down");
		var buttonDropdownLi = $("<li>").append(itemElementDropdownButton);
		var ulElement = $("<ul>").append(buttonLi).append(buttonDropdownLi);
		var divElement = $("<div>").append(ulElement);
		var spanElement = $("<span>").append(divElement);
		var itemElement = $("<li>").addClass("twoB").addClass("ribbonDropdownContainer").append(spanElement);
		
		itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		
		var dropdownMenu = createDropdownMenu(path + "." + (itemModel.label ? itemModel.label : itemModel.name), itemModel).addClass("smallButtonDropdownMenu");
		addDropdownAction(itemElementDropdownButton);
		itemElement.append(dropdownMenu);
			
		return itemElement;
	}
	
	function createDropdownMenu(path, itemModel) {
		var dropdownMenuContent = $("<ul>");
		for (var menuIter in itemModel.items) {
			var menuItemName = itemModel.items[menuIter].name;
			var icon = (itemModel.items[menuIter].icon && $.trim(itemModel.items[menuIter].icon) != "") ? $.trim(itemModel.items[menuIter].icon) : null;
			var style = "";
			if (icon) {
				style = " style=\"background-image:url(\'/img/core/icons/"+icon+"\')\"";
			}
			var menuItemButton = $("<a>").attr('href','#').html("<span "+style+">"+itemModel.items[menuIter].label+"</span>").addClass("icon");
			menuItemButton.bind('click', {itemName: itemModel.name+"."+menuItemName, clickAction: itemModel.items[menuIter].clickAction}, buttonClicked);
			var menuItem = $("<li>").append(menuItemButton);
			dropdownMenuContent.append(menuItem);
		}
		var dropdownMenu = $("<div>").addClass("dropdownMenu").addClass("m_module").append(dropdownMenuContent);
		return dropdownMenu;
	}
	
	function addDropdownAction(dropdownTriggerButton) {
		dropdownTriggerButton.addClass("dropdownTrigger");
		dropdownTriggerButton.click(function() {
			var parent = $(this);
			parent.blur();
			while(! parent.hasClass("ribbonDropdownContainer")) {
				parent = parent.parent();
			}
			if (parent.find(".dropdownMenu").is(":visible")) {
				parent.find(".dropdownMenu").slideUp(100);
			} else {
				parent.find(".dropdownMenu").slideDown(100).show();
				parent.hover(function() {}, function(){  
					parent.find(".dropdownMenu").slideUp(100);
				});
			}
		});
	}
	
	function createComboBox(path, itemModel) {
		var itemElementButton = $("<a>").attr('href','#').html("<span><div "+getItemIconStyle(itemModel)+">"+itemModel.label+"</div></span>");
		var buttonLi = $("<li>").append(itemElementButton);
		var itemElementDropdownButton = $("<a>").attr('href','#').addClass("twoB_down");
		var buttonDropdownLi = $("<li>").append(itemElementDropdownButton);
		var ulElement = $("<ul>").append(buttonLi).append(buttonDropdownLi);
		var divElement = $("<div>").append(ulElement);
		var spanElement = $("<span>").append(divElement);
		var itemElement = $("<li>").addClass("twoB").addClass("ribbonDropdownContainer").append(spanElement);
		
		//itemElementButton.bind('click', {itemName: itemModel.name, clickAction: itemModel.clickAction}, buttonClicked);
		
		//var dropdownMenu = createDropdownMenu(path + "." + (itemModel.label ? itemModel.label : itemModel.name), itemModel).addClass("smallButtonDropdownMenu");
		//addDropdownAction(itemElementDropdownButton);
		//itemElement.append(dropdownMenu);
			
		return itemElement;
	}
	
	function getItemIconStyle(itemModel) {
		var icon = (itemModel.icon && $.trim(itemModel.icon) != "") ? $.trim(itemModel.icon) : null;
		var style = "";
		if (icon) {
			style = " class='hasIcon' style=\"background-image:url(\'/img/core/icons/"+icon+"\')\"";
		}
		return style;
	}
	
	function buttonClicked(e) {
		$(this).blur();
		var action = e.data.clickAction;
		var name = e.data.itemName;
		mainController.performRibbonAction(action);
	}

	this.updateSize = function(margin, innerWidth) {
		$("#q_menu_row3").css("margin-left", (margin)+"px");
		$("#q_row4_out").width(innerWidth);
	}
	
}

var QCD = QCD || {};

QCD.PageController = function(_viewName, _pluginIdentifier, _hasDataDefinition, _isPopup) {
	
	var viewName = _viewName;
	var pluginIdentifier = _pluginIdentifier;
	var hasDataDefinition = _hasDataDefinition;
	var isPopup = _isPopup;
	
	var pageComponents;
	
	var headerComponent = null;
	
	var pageOptions;
	
	var messagesController;
	
	var popup;
	
	function constructor(_this) {
		
		QCDConnector.windowName = "/page/"+pluginIdentifier+"/"+viewName;
		QCDConnector.mainController = _this;
		
		var pageOptionsElement = $("#pageOptions");
		pageOptions = JSON.parse($.trim(pageOptionsElement.html()));
		pageOptionsElement.remove();
		
		var contentElement = $("body");
		pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), _this);
		QCD.debug(pageComponents);
		
		$(window).bind('resize', updateSize);
		updateSize();
		
		if (window.parent) {
			$(window.parent).focus(onWindowClick);
		} else {
			$(window).focus(onWindowClick);
		}
		
		QCD.components.elements.utils.LoadingIndicator.blockElement($("body"));
	}
	
	this.init = function(serializationObject) {
		if (serializationObject) {
			setComponentState(serializationObject);
			if (hasDataDefinition) {
				this.callEvent("initializeAfterBack", null, function() {QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"))});
			} else {
				QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"));
			}
		} else {
			if (hasDataDefinition) {
				this.callEvent("initialize", null, function() {QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"))});
			} else {
				QCD.components.elements.utils.LoadingIndicator.unblockElement($("body"));
			}
		}
	}
	
	this.setContext = function(contextStr) {
		var context = JSON.parse(contextStr);
		for (var i in context) {
			var dotPos = i.lastIndexOf(".");
			var contextComponentPath = i.substring(0, dotPos);
			var contextField = i.substring(dotPos+1);
			var contextComponent = this.getComponent(contextComponentPath);
			contextComponent.addContext(contextField, context[i]);
		}
	}
	
	
	this.callEvent = function(eventName, component, completeFunction, args, actionsPerformer) {
		var initParameters = new Object();
		var eventCompleteFunction = completeFunction;
		initParameters.event = {
			name: eventName
		}
		if (component) {
			initParameters.event.component = component;
			var componentObject = getComponent(component);
			var componentListeners = componentObject.options.listeners;
			if (componentListeners) {
				for (var i = 0; i<componentListeners.length; i++) {
					var listenerElement = getComponent(componentListeners[i]);
					listenerElement.setComponentLoading(true);
				}
				eventCompleteFunction = function() {
					if (completeFunction) {
						completeFunction();
					}
					for (var i = 0; i<componentListeners.length; i++) {
						var listenerElement = getComponent(componentListeners[i]);
						listenerElement.setComponentLoading(false);
					}
				}
			}
		}
		if (args) {
			initParameters.event.args = args;
		}
		initParameters.components = getValueData();
		performEvent(initParameters, eventCompleteFunction, actionsPerformer);
	}
	
	function performEvent(parameters, completeFunction, actionsPerformer) {
		var parametersJson = JSON.stringify(parameters);
		QCDConnector.sendPost(parametersJson, function(response) {
			if (completeFunction) {
				completeFunction();
			}
			if (response.redirect) {
				if (response.redirect.openInNewWindow) {
					window.open(response.redirect.url);
				} else {
					goToPage(response.redirect.url, false);
					return;
				}
			} else {
				setValueData(response);
			}
			if (actionsPerformer && response.content.status == "ok") {
				actionsPerformer.performNext();
			}
		}, function() {
			if (completeFunction) {
				completeFunction();
			}
		});
	}
	
	// TODO mina
	
//	this.performLookupSelect = function(entityId, entityString, entityCode, actionsPerformer) {
//		window.opener[lookupComponentName+"_onSelectFunction"].call(null, entityId, entityString, entityCode);
//		if (actionsPerformer) {
//			actionsPerformer.performNext();
//		}
//	}
	
	this.performRibbonAction = function(ribbonAction) {
		var actionParts = ribbonAction.split(";");
		var actions = new Array();
		for (var actionIter in actionParts) {
			var action = $.trim(actionParts[actionIter]);
			if (action) {
				var elementBegin = action.search("{");
				var elementEnd = action.search("}");
				if (elementBegin<0 || elementEnd<0 || elementEnd<elementBegin) {
					QCD.error("action parse error in: "+action);
					return;
				}
				var elementPath = action.substring(elementBegin+1, elementEnd);
				var component = this.getComponent(elementPath);
				
				var elementAction = action.substring(elementEnd+1);
				if (elementAction[0] != ".") {
					QCD.error("action parse error in: "+action);
					return;
				}
				elementAction = elementAction.substring(1);

				var argumentsBegin = elementAction.indexOf("(");
				var argumentsEnd = elementAction.indexOf(")");
				var argumentsList = new Array();
				
				//(argumentsBegin < argumentsEnd-1) because it then means that there are no arguments
				//and only empty parenthesis ()
				if(argumentsBegin > 0 && argumentsEnd > 0 && argumentsBegin < argumentsEnd-1) {
					var args = elementAction.substring(argumentsBegin+1, argumentsEnd);
					argumentsList = args.split(",");
					elementAction = elementAction.substring(0, argumentsBegin);
				} else if(argumentsBegin == argumentsEnd-1) {
					//we need to get rid of the empty parenthesis
					elementAction = elementAction.substring(0, argumentsBegin);
				}

				var actionObject = {
					component: component,
					action: elementAction,
					arguments: argumentsList
				}
				
				actions.push(actionObject);
			}
		}
		var actionsPerformer = {
			actions: actions,
			actionIter: 0,
			performNext: function() {
				var actionObject = this.actions[this.actionIter];
				if (actionObject) {
					var func = actionObject.component[actionObject.action];
					if (!func) {
						QCD.error("no function in "+actionObject.component.elementPath+": "+actionObject.action);
						return;
					}
					this.actionIter++;
					
					var fullArgumentList = new Array(this);
					fullArgumentList = fullArgumentList.concat(actionObject.arguments[0]);
					fullArgumentList.push(actionObject.arguments.slice(1));
					
					func.apply(actionObject.component, fullArgumentList);
				}
			}
		}
		actionsPerformer.performNext();
	}
	
	function getValueData() {
		var values = new Object();
		for (var i in pageComponents) {
			var value = pageComponents[i].getValue();
			if (value) {
				values[i] = value;
			}
		}
		return values;
	}
	
	function setComponentState(state) {
		for (var i in state.components) {
			var component = pageComponents[i];
			component.setState(state.components[i]);
		}
	}
	
	this.showMessage = function(message) {
		if (window.parent && window.parent.addMessage) {
			window.parent.addMessage(message);
		} else {
			if (!messagesController) {
				messagesController = new QCD.MessagesController();
			}
			messagesController.addMessage(message);
		}
	}
	
	this.setWindowHeaderComponent = function(component) {
		headerComponent = component;
	}
	this.setWindowHeader = function(header) {
		if (headerComponent) {
			headerComponent.setHeader(header);
		}
	}
	
	function setValueData(data) {
		QCD.debug(data);
		if (data.messages) {
			for (var i in data.messages) {
				var message = data.messages[i];
				window.parent.addMessage(message.type, message.content);
			}
		}
		for (var i in data.components) {
			var component = pageComponents[i];
			component.setValue(data.components[i]);
		}
	}
	
	this.getComponent = function(componentPath) {
		var pathParts = componentPath.split(".");
		var component = pageComponents[pathParts[0]];
		if (! component) {
			return null;
		}
		for (var i = 1; i<pathParts.length; i++) {
			component = component.components[pathParts[i]];
			if (! component) {
				return null;
			}
		}
		return component;
	}
	var getComponent = this.getComponent;
	
	function onWindowClick() {
		if (popup) {
			popup.parentComponent.onPopupClose();
			popup.window.close();
			popup = null;
		}
	}
	
	this.closePopup = function() {
		if (popup) {
			popup.parentComponent.onPopupClose();
			try {
				popup.window.close();
			} catch (e) {
			}
			popup = null;
		}
	}
	
	this.openPopup = function(url, parentComponent, title) {
		if (popup) {
			
		}
		
		if (url.indexOf("?") != -1) {
			url+="&";
		} else {
			url+="?";
		}
		url+="popup=true";
		
		popup = new Object();
		popup.parentComponent = parentComponent;
		var left = (screen.width/2)-(400);
		var top = (screen.height/2)-(350);
		popup.window = window.open(url, title, 'status=0,toolbar=0,width=800,height=700,left='+left+',top='+top);
		return popup.window;
	}
	
	this.onPopupInit = function() {
		popup.parentComponent.onPopupInit();
	}
	
	this.isPopup = function() {
		return isPopup;
	}
	
	this.goToPage = function(url, isPage) {
		if (isPage == undefined || isPage == null) {
			isPage = true;
		}
		//if(canClose()) {
			var serializationObject = {
				components: getValueData()
			}
			window.parent.goToPage(url, serializationObject, isPage);
		//}
	}
	var goToPage = this.goToPage;
	
	this.goBack = function() {
		if(canClose()) {
			window.parent.goBack();
		}
	}
	this.canClose = canClose;
	
	function canClose() {
		changed = false;
		for (var i in pageComponents) {
			if(pageComponents[i].isChanged()) {				
				changed = true;
			}
		}
		if(changed) {
			return window.confirm(pageOptions.translations.backWithChangesConfirmation);
		} else {
			return true;
		}
	}
	
	this.closeWindow = function() {
		window.close();
	}
	
	this.onSessionExpired = function() {
		var serializationObject = {
			components: getValueData()
		}
		window.parent.onSessionExpired(serializationObject);
	}
	
	function updateSize() {
		var width = $(document).width();
		var height = $(document).height();
		for (var i in pageComponents) {
			pageComponents[i].updateSize(width, height);
		}
	}
	
	constructor(this);
}

