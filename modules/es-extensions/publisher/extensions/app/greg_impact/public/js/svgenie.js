/* global canvg window document */
/*
 * svgenie
 * https://github.com/Causata/svgenie
 *
 * Copyright (c) 2013 Causata Ltd
 * Licensed under the MIT license.
 */

var svgenie = (function(){
    "use strict";

    var _serializeXmlNode = function (xmlNode) {
        if (typeof window.XMLSerializer != "undefined") {
            return (new window.XMLSerializer()).serializeToString(xmlNode);
        } else if (typeof xmlNode.xml != "undefined") {
            return xmlNode.xml;
        }
        return "";
    };

    var _toCanvas = function( svg, options, callback ){
        if ( typeof svg == "string" ){
            if ( svg.substr(0,1) == "#" ) { svg = svg.substr(1); }
            svg = document.getElementById(svg);
        }

        // Hopefully don't need to attach anything to the DOM
        var canvas = document.createElement("canvas");
        canvas.setAttribute("height",svg.offsetHeight);
        canvas.setAttribute("width",svg.offsetWidth);
        canvg( canvas, _serializeXmlNode(svg), {
            ignoreMouse : true,
            ignoreAnimation : true,
            renderCallback : function(){ callback( canvas ); }
        });
    };

    var _toDataURL = function( id, options, callback ){
        _toCanvas( id, options, function( canvas ){
            callback( canvas.toDataURL("image/png"), canvas );
        });
    };

    var _save = function( id, options ){

        _toDataURL( id, options, function(data, canvas){
            _saveToFile({
                data : data,
                canvas : canvas,
                name : options.name || "image.png"
            });
        });
    };

    var _saveToFile = function( conf ){
        var a = document.createElement( "a" );

        // Can we use the "download" attribute? (Chrome && FF20)
        if( a.download != null ){
            a.href = conf.data;
            a.download = conf.name;
            _pretendClick(a);
            return;
        };

        // IE10
        if (window.navigator.msSaveOrOpenBlob) {
            navigator.msSaveOrOpenBlob(conf.canvas.msToBlob(), conf.name);
            return;
        }

        //if above options does not work (Ex - Safari) the image will be opened as a png image in a new tab
        //user has to manually download the image from the browser.
        a.href = conf.data;
        a.download = conf.name;
        a.target = "_blank";
        a.addEventListener("click", function (e) {
            a.parentNode.removeChild(a);
        });

        document.body.appendChild(a);
        _pretendClick(a);
        return;

    };

    function _pretendClick(eElement) {
        var oEvent = document.createEvent("MouseEvents");
        oEvent.initMouseEvent( "click", true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null );
        return eElement.dispatchEvent(oEvent);
    };

    return {
        save : _save,
        toCanvas : _toCanvas,
        toDataURL : _toDataURL
    };
})();