/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * @set  default global variables
 */
var svgIconsFolder = "../extensions/app/greg_impact/images/svg/",
    footerHeight = 50,
    isSame = null,
    delay = 300,
    clicks = 0,
    timer = null,
    screenOrientation = $(window).width() > $(window).height() ? .5625 : .9625,
    onload = true,
    graphSVG = "#graph svg",
    graphCaptureSVG = "#graph-capture svg";

/*
 * Impact analysis: Dependency Graph UI functions (functions to run on DOM ready).
 *
 * @method  Initializing search element
 *          - On search element drop list open function
 *          - On search element drop list close function
 */
$(document).ready(function() {

    // Initializing search element
    $('#search').select2({
        placeholder: 'Find Resource',
        data: root.nodes,
        multiple: false,
        width: "100%",
        formatResult: function (object, container, d){
            return  '<div class="item" id="search_' + object.id + '">' +
                        '<div class="text">' +
                            '<div class="resource-name">' + object.text + '</div>' +
                            '<div class="uniqueAttributes">' + getuniqueattributestring(object) + '</div>' +
                            '<div class="media-type">' + object.shortName + '</div>' +
                        '</div>' +
                        '<div class="icon">' +
                            '<img class="svg" src="' + svgIconsFolder + nodeIcon(object.mediaType) + '" />' +
                        '</div>' +
                    '</div>';
        }
    }).on('select2-close', function() {
        if (this.value !== ""){
            searchNode();
            $('.reset-locate').css("display", "inline-block");
        }
    }).on('select2-open', function(){
        $('ul.select2-results li').each(function(){
            var itemId = $('.item', this).attr('id');
                itemId = itemId.replace('search_', 'node_');

            if($("#"+itemId+"").css('display') == 'none'){
                $(this).hide();
            }
        });

        // Convert all svg images to xml paths
        $('img.svg').each(function(){
            var $img = $(this);
            var imgID = $img.attr('id');
            var imgClass = $img.attr('class');
            var imgURL = $img.attr('src');

            $.get(imgURL, function(data) {
                // Get the SVG tag, ignore the rest
                var $svg = $(data).find('svg');

                // Add replaced image's ID to the new SVG
                if(typeof imgID !== 'undefined') {
                    $svg = $svg.attr('id', imgID);
                }
                // Add replaced image's classes to the new SVG
                if(typeof imgClass !== 'undefined') {
                    $svg = $svg.attr('class', imgClass+' replaced-svg');
                }

                // Remove any invalid XML tags as per http://validator.w3.org
                $svg = $svg.removeAttr('xmlns:a');

                // Replace image with new SVG
                $img.replaceWith($svg);

            }, 'xml');
        });

    });

    for (i = 0; i < root.nodes.length; i++) {
        linkedByIndex[i + "," + i] = 1;
    }

    root.edges.forEach(function(d) {
        linkedByIndex[d.source.index + "," + d.target.index] = 1;
    });

    var linknodes = $('.linkNode');
    for (var i = 0; i < linknodes.length; i++) {
        setClickableTooltip('#' + linknodes[i].id , linkRelations(linknodes[i].__data__));
    }

    var nodeCircles = $('.nodeCircle');
    for (var i = 0; i < nodeCircles.length; i++) {
        setClickableTooltip('#' + nodeCircles[i].id , linkRelations(nodeCircles[i].__data__));
    }

});

/*
 * Function to run on window resize (To keep graph screen responsiveness).
 */
$(window).resize(function() {
    d3.select("svg").attr("width", $(window).width());
    d3.select("svg").attr("height", $(window).height());
    d3.select("svg").attr("viewBox", "0 0 " + $(window).width() + " " + $(window).height());
});

/*
 * Function to run on window load (After page load functions)
 */
$(window).load(function() {
    clearSearchOperation();
    $(".searchfield").css("visibility", "visible");
});

/*
 * Graph redrawing function
 */
function redraw() {
    svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")");
}

/*
 * Function to get center of an element.
 * @return center: Element center
 */
function getCenter() {
    var center = {
        x : this.width / 2,
        y : this.height / 2
    };
    return center;
}

/*
 * Function to update graph UI.
 * @param d: Data
 */
function update(d) {

    var nodes = root.nodes,
        links = root.edges;

    // Restart the force layout.
    force.nodes(nodes)
        .links(links)
        .start();

    link = link.data(links);
    linkNode = linkNode.data(links);

    link.exit().remove();

    linkg = svg.selectAll(".linkg")
        .data(links)
        .enter().append("g")
        .attr("group", "link")
        .attr("mediatypes", function (d, i) {
            return (d.source.shortName + ";" + d.target.shortName);
        })
        .attr("id", function(d,i){
            return "link_"+[i];
        })
        .attr("associations", function(d,i){
            var result = "";
            for (loop = 0; loop < root.edges[i].relations.length; loop++){
                result += (loop !== 0 ? ";" : "") + root.relations[root.edges[i].relations[loop]].relation;
            }
            return result;
        })
        .attr("nodes", function(d,i){
            return ("node_"+ root.edges[i].source.id +";node_"+ root.edges[i].target.id);
        });

    link = linkg.append("line")
        .attr("class", "linkLine");

    linkNode = linkg.append("circle")
        .attr("class", "linkNode")
        .attr("id", getLinkID)
        .attr("title", '')
        .on("click", showRelations)
        .attr("r", 5);

    // Update nodes.
    node = node.data(nodes, function(d) {
        return d.id;
    });

    node.exit().remove();

    nodeEnter = node.enter().append("g")
        .attr("id", getNodeID)
        .attr("class", function (d) {
            return "node" + ('image' in d ? ' imagenode' : '')  + " nodeCircle";
        })
        .on("dblclick", doubleclick)
        .on("click", click)
        .attr("group", "node")
        .attr("edges", function(d, i){
            var result = "";
            for (loop = 0; loop < root.nodes[i].relations.length; loop++){

                var relationTarget = root.relations[root.nodes[i].relations[loop]].target,
                    relationSource = root.relations[root.nodes[i].relations[loop]].source;

                for (innerloop = 0; innerloop < root.edges.length; innerloop++) {
                    if( ((relationTarget == root.edges[innerloop].target.index) && (relationSource == root.edges[innerloop].source.index)) ||
                        ((relationTarget == root.edges[innerloop].source.index) && (relationSource == root.edges[innerloop].target.index)) ){
                        result += (loop !== 0 ? ";" : "") + "link_"+[innerloop];
                    }
                }

            }
            return result;
        });
        //.call(force.drag); //enable node dragging

    var circle = nodeEnter.append("circle")
        .attr("cx", 0)
        .attr("cy", 0)
        .attr("r", 35)
        .attr("active-status", "");

    nodeEnter.attr("nodetype", nodeType);
    nodeEnter.attr("isuniqueattributespresent", isuniqueattributespresent);

    node.select("[nodetype=parent] circle")
        .attr("r", 65);

    nodeEnter.each(function(d, i){
        d3.xml(svgIconsFolder+nodeIcon(d.mediaType), "image/svg+xml", function(xml){
            document.getElementById("node_"+i).appendChild(xml.documentElement.cloneNode(true));

            nodeEnter.select("svg")
                .attr("width", 40)
                .attr("height", 40)
                .attr("y", -40/2)
                .attr("x", -40/2)
                .selectAll("svg path")
                .attr("style","");
            nodeEnter.select("[nodetype=parent] svg")
                .attr("width", 80)
                .attr("height", 80)
                .attr("y", -80/2)
                .attr("x", -80/2);
        });
    });

    nodeEnter.append("text")
        .attr("class", "resource-name")
        .attr("dy", 60)
        .attr("dx", -40)
        .text(function(d) { return d.name; });

    nodeEnter.append("text")
        .attr("class", "uniqueAttributes")
        .attr("dy", 78)
        .attr("dx", -40)
        .text(function(d) {
            if (isuniqueattributespresent(d)) {
                var content = "";
                for (key in d.uniqueAttributes) {
                    content = content + ", " + d.uniqueAttributes[key];
                }
                return content.substring(2);
            }
            return null;
        });

    nodeEnter.append("text")
        .attr("class", "media-type")
        .attr("dy", 78)
        .attr("dx", -40)
        .text(function(d) { return d.shortName; });

    nodeEnter.selectAll("[isuniqueattributespresent=true] text.media-type")
        .attr("dy", 95)
        .attr("dx", -40);

    nodeEnter.select("[nodetype=parent] text.resource-name")
        .attr("dy", 100)
        .attr("dx", -70);

    nodeEnter.select("[nodetype=parent] text.media-type")
        .attr("dy", function(d) { return (isuniqueattributespresent(d) == "true") ? 140 : 120; })
        .attr("dx", -70);

    nodeEnter.select("[nodetype=parent] text.uniqueAttributes")
        .attr("dy", 120)
        .attr("dx", -70);

}

/*
 * Get node types ex:- Parent > Child.
 * @param d: Data
 */
function nodeType(d) {
    if(d.children){
        if(d.children.length > 0){
            return "parent";
        }
    }

    if(d.children){
        if(d.children.length > 0){
            return d.nodeType;
        }
    }

    return d.nodeType;
}

/*
 * Returns wheather any associated unique attributes are present
 * @param d: Data
 */
function isuniqueattributespresent(d) {
    return (jQuery.isEmptyObject(d.uniqueAttributes)) ? "false" : "true";
}

function getuniqueattributestring(d) {
    if (isuniqueattributespresent(d)) {
        var content = "";
        for (key in d.uniqueAttributes) {
            content = content + ", " + d.uniqueAttributes[key];
        }
        return content.substring(2);
    }
    return null;
}

/* Defining icons for resource type.
 * @param getType: Selected resource media type
 */
function nodeIcon(getType) {

    switch(getType) {
        case "application/wsdl+xml":
            return "wsdl.svg";
            break;
        case "application/wadl+xml":
            return "wadl.svg";
            break;
        case "application/vnd.wso2-uri+xml":
            return "uri.svg";
            break;
        case "application/vnd.wso2-site+xml":
            return "website.svg";
            break;
        case "application/vnd.wso2-servicex+xml":
            return "service.svg";
            break;
        case "application/vnd.wso2-service+xml":
            return "service.svg";
            break;
        case "application/vnd.wso2-soap-service+xml":
            return "soap.svg";
            break;
        case "application/vnd.wso2-sequence+xml":
            return "sequence.svg";
            break;
        case "application/x-xsd+xml":
            return "xsd.svg";
            break;
        case "application/vnd.wso2-proxy+xml":
            return "proxy.svg";
            break;
        case "application/vnd.wso2-provider+xml":
            return "service-provider.svg";
            break;
        case "application/policy+xml":
            return "policy.svg";
            break;
        case "application/vnd.wso2-gadget+xml":
            return "gadget.svg";
            break;
        case "application/vnd.wso2-endpoint+xml":
            return "endpoint.svg";
            break;
        case "application/vnd.wso2.endpoint":
            return "endpoint.svg";
            break;
        case "application/vnd.wso2-ebook+xml":
            return "ebook.svg";
            break;
        case "application/vnd.wso2-document+xml":
            return "document.svg";
            break;
        case "application/vnd.wso2-api+xml":
            return "api.svg";
            break;   
        case "application/vnd.wso2-application+xml":
            return "application.svg";
            break;
        default:
            return "blank-document.svg";
    }
}

/* Function to run on node drag.
 * @param d: data
 */
function dragstart(d){
    d3.select(this).classed("fixed", d.fixed = true);
}

/*
 * Initializing filtering function.
 */
function setupFilters(){
    var filters = [];

    d3.selectAll("[group=link]").each(function(){
        var associations = $(this).attr("associations"),
            association = associations.split(';');

        for(var i = 0; i < association.length; i++) {
            var newFilter = association[i];
            if(filters.indexOf(newFilter) === -1) {
                filters.push(newFilter)
            }
        }
    });

    for(var i = 0; i < filters.length; i++) {
        $('#filters').append('<div class="tag" onclick="filter(this)"><span>' +filters[i]+ '</span> <i class="fa fa-check" aria-hidden="true"></i></div><br />');
    }

    var mediaTypeFilters = [];

    d3.selectAll("[group=node] text.media-type").each(function(){
        var mediaType = $(this).html();

        var newFilter = mediaType;
        if(mediaTypeFilters.indexOf(newFilter) === -1) {
            mediaTypeFilters.push(newFilter)
        }
    });

    for(var j = 0; j < mediaTypeFilters.length; j++) {
        $('#filters').append('<div class="tag mediaType" onclick="filter(this)"><span>' +mediaTypeFilters[j]+ '</span> <i class="fa fa-check" aria-hidden="true"></i></div><br />');
    }
}

/* Function to filter resources by it's connected relationships.
 * @param elem: Selected filter tag
 */
function filter(elem){

    $(elem).toggleClass('hidden');

    $('i', elem).toggleClass('fa fa-check', function(){

        $("svg").find("[group='link']").show();
        $('.tag').each(function () {
            if ($(this).hasClass('hidden')) {

                var filter = $('span', this).html();
                d3.selectAll("[group=link]").each(function () {
                    var mediaTypes = $(this).attr("mediatypes"),
                        mediaType = mediaTypes.split(';');

                    var associations = $(this).attr("associations"),
                        association = associations.split(';');

                    for (var i = 0; i < association.length; i++) {
                        if (association[i] == filter) {
                            $(this).hide();
                        }
                    }

                    if ((mediaType.indexOf(filter) > -1)) {
                        $(this).hide();
                    }
                });
            }
        });

        $("svg").find("[group='node']").hide();
        d3.selectAll("[group=node]").each(function(){
            var edges = $(this).attr("edges"),
                edge = edges.split(';');

            for(var i = 0; i < edge.length; i++) {
                if($("#"+edge[i]+"").css('display') !== 'none'){
                    $(this).show();
                }
            }
            zoomFit();
        });
        
        d3.selectAll("[group=link]").each(function(){
            if(!($(this).css('display') == 'none')) {
                var nodes = $(this).attr("nodes"),
                    node = nodes.split(';');
                
                for(var i = 0; i < node.length; i++) {
                    if($("svg").find("#"+node[i]).css('display') == 'none'){
                        $("#"+node[i]).show();
                    }
                }
            }
        });

    });
    
    clearSearchOperation();

    $("#search").select2("enable", false);
    $('#filters .tag').each(function(){
        if(!$(this).hasClass('hidden')){
            $("#search").select2("enable", true);
        }
    });

}

/*
 * Function to looks up whether a pair are neighbours.
 */
function neighboring(a, b) {
    return linkedByIndex[a.index + "," + b.index];
}

/*
 * Reset resource path on page bottom.
 */
function resetPath() {
    $("#urlView #path").html("<i>Select a resource to view path</i>");
}

/*
 * Function to run on graph outside click.
 */
function outClick() {
    resetPath();
    clearSearchOperation();
    d3.selectAll("g").select("circle").classed("active", false);
    d3.selectAll("g").attr("active-status", "");

    node.attr("class", "");
    linkg.attr("class", "");
    isSame = self;
    selectedNode = -1;
}

/*
 * Function to run on resource node double click and single click.
 * @param d: data
 */
function click(d) {
    d3.event.stopPropagation();

    var self = this;

    d3.selectAll("g").select("circle").classed("active", false);
    d3.selectAll("g").attr("active-status", "");

    d3.select(self).select("circle").classed("active", true);

    $('#search').select2("val", function(){
        $('.reset-locate').css("display", "inline-block");
        return d.id;
    });

    if(selectedNode == -1 || selectedNode != d.index){
        selectedNode = d.index;

        // highlight links if they are connected to source
        linkg.attr("class", function(o) {
            return (d.index == o.target.index) || (d.index == o.source.index) ? "active" : "inactive";
        });

        // highlight nodes if they are connected to source
        node.attr("class", function(o) {
            return neighboring(o, d) || neighboring(d, o) ? "active" : "inactive";
        });

    }
    else{
        // Reset relation highlight
        node.attr("class", "");
        linkg.attr("class", "");
        selectedNode = -1;
    }
}

/*
 * Function to run on resource node double click.
 * @param d: data
 */
function doubleclick(d) {
//     if (d3.event.defaultPrevented) return; // ignore drag

    var linkString;
    if (d.isActivatedAssetsType){
        linkString = '../assets/' + d.shortName + '/details/' +
            encodeURIComponent(d.uuid);
    } else {
        // Following link string redirects user from publisher to management console
        // As this was regarded as bad practice following code line was removed from execution
        // https://wso2.org/jira/browse/REGISTRY-2978
        // linkString = '../../carbon/resources/resource.jsp?region=region3&item=resource_browser_menu&path=' +
        //     encodeURIComponent(d.path);
        messages.alertInfo("Asset type \"" + d.shortName + "\" is not activated for publisher view!");
        return;
    }

    var win = window.open(linkString, '_blank');
    if(win){
        //Browser has allowed it to be opened
        win.focus();
    }else{
        //Broswer has blocked it
        alert('Please allow popups for this site');
    }
}

/*
 * Function to display resource info.
 * @param  resource: Selected resource
 */
function displayInfo(resource){
    $('#name').text(resource.name);
    $('#mediaType').text(resource.mediaType);
    var linkString;
    if (resource.isActivatedAssetsType){
        linkString = '<a href = "../assets/' + resource.shortName + '/details/' +
            encodeURIComponent(resource.uuid) + '">' + resource.path + '</a>';
    } else {
        linkString = '<a href = "../../carbon/resources/resource.jsp?region=region3&item=resource_browser_menu&path=' +
            encodeURIComponent(resource.path) + '">' + resource.path + '</a>';
    }
    $('#path').html(linkString);
    if(resource.lcState==null){
        $('#lcState').text("Not defined");
    } else{
        $('#lcState').text(resource.lcState);
    }
}

/*
 * On load node spreading function.
 */
function tick(){
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return screenOrientation * d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return screenOrientation * d.target.y; });

    linkNode.attr("cx", function(d) { return midPoint(d.source.x,d.target.x) })
            .attr("cy", function(d) { return screenOrientation * midPoint(d.source.y,d.target.y) });

    nodeEnter.attr("transform", function(d) { return "translate(" + d.x + "," + screenOrientation * d.y + ")"; });
}

/*
 * Function to run after nodes spreading done.
 */
function tickCallback(){
    zoomFit(); //Zoom out graph to fit screen on page load
    $("#preLoader").hide();
    $(".zoom").attr("disabled", false);
    $(".searchbox").attr("disabled", false);
    $(".download").attr("disabled", false);
    $(".filters").css("display", "inline-block");
    onload = false;
}

/*
 * Function to find graph middle point.
 */
function midPoint(val1, val2) {
    if (val1 < val2){
        return val1 + ((val2 - val1) / 2);
    }
    else if (val1 > val2){
        return val2 + ((val1 - val2) / 2);
    }
    else{
        return val1;
    }
}

/*
 * Initializing search function.
 */
function setupSearch() {
    for (var i = 0; i < root.nodes.length; i++) {
        searchArray.push(root.nodes[i].name);
    }

    searchArray = searchArray.sort();
}

/*
 * Function to clear search operation.
 */
function clearSearchOperation() {
    $("#search").select2('data', null);
    $(".reset-locate").hide();

    clearSearchedNode();
    resetPath();
}

/*
 * Function to clear search.
 */
function clearSearchedNode() {
    d3.selectAll("g").select("circle").classed("active", false);
    node.attr("class", "");
    linkg.attr("class", "");
    d3.selectAll("g").attr("active-status", "");
}

/*
 * Function to search resource.
 */
function searchNode() {
    clearSearchedNode();

    var selectedVal = $('#search').select2('data').name,
        node = svg.selectAll("[group=node]");

    if (selectedVal == "none") {
        node.style("stroke", "black").style("stroke-width", "10");
    } else {

        searchedNode = node.filter(function (d, i) {
            return d.name == selectedVal;
        });

        var rootNode = root.nodes[searchedNode[0][0].__data__.id];

        searchedNode.select("circle").classed("active", true);
        displayInfo(searchedNode[0][0].__data__);

        zoom.scale(1);

        screenOrientation = 1;
        tick();

        zoom.translate( [getCenter().x - rootNode.x, getCenter().y - rootNode.y] );
        zoom.event(svg);
    }
}

/*
 * Function to zoom in graph.
 */
function zoomIn(){
    var coor = zoom.translate();
    var x = (coor[0] - getCenter().x) * 1.1 + getCenter().x;
    var y = (coor[1] - getCenter().y) * 1.1 + getCenter().y;

    zoom.scale(zoom.scale() * 1.1);
    zoom.translate([x, y]);

    zoom.event(svg);
}

/*
 * Function to zoom out graph.
 */
function zoomOut(){
    var coor = zoom.translate();
    var x = (coor[0] - getCenter().x) * 0.9 + getCenter().x;
    var y = (coor[1] - getCenter().y) * 0.9 + getCenter().y;

    zoom.scale(zoom.scale() * 0.9);
    zoom.translate([x, y]);

    zoom.event(svg);
}

/*
 * Function to zoom out/in graph to fit current screen.
 */
function zoomFit(){
    var graphBBox = d3.select("svg g#mainG").node().getBBox(),
        graphScreen = $("svg");

    var scaleAmount = Math.min(graphScreen.width()/(graphBBox.width+100), graphScreen.height()/(graphBBox.height+100));

    var xOffSet = graphBBox.x > 0 ? -Math.abs(graphBBox.x) : Math.abs(graphBBox.x),
        yOffSet = graphBBox.y > 0 ? -Math.abs(graphBBox.y) : Math.abs(graphBBox.y);

    var tx = (xOffSet*scaleAmount + (graphScreen.width()/scaleAmount - graphBBox.width) * scaleAmount/2),
        ty = (yOffSet*scaleAmount + (graphScreen.height()/scaleAmount - graphBBox.height) * scaleAmount/2);

    zoom.translate([tx, ty]).scale(scaleAmount);
    zoom.event(svg);
}

/*
 * Function to show resource relationships.
 */
function showRelations(d){
    var id = '#' + getLinkID(d);
    $(id).css({ opacity: 1 });
}

/*
 * Function to save current screen as a .png image file.
 */
function svgDownload(){
    var minX = Number.POSITIVE_INFINITY,
        minY = Number.POSITIVE_INFINITY,
        currentScale = zoom.scale();

    d3.selectAll("[group=node]").each(function () {
        var xforms = this.getAttribute('transform');
        var parts = /translate\(\s*([^\s,)]+)[ ,]([^\s,)]+)/.exec(xforms);
        var firstX = parseInt(parts[1], 10),
            firstY = parseInt(parts[2], 10);
        if (firstX < minX) {
            minX = firstX;
        }
        if (firstY < minY) {
            minY = firstY;
        }
    });
    minX = -minX + 100;
    minY = -minY + 100;

    var graphBBox = d3.select("svg g#mainG").node().getBBox();

    var clone = $(graphSVG).clone();
    clone.appendTo("#graph-capture");
    clone.attr("id", "cloned");
    clone.attr("width", (graphBBox.width + 200) * currentScale);
    clone.attr("height", (graphBBox.height + 100) * currentScale);
    clone.attr("viewBox", [
        0,
        0,
        (graphBBox.width + 200) * currentScale,
        (graphBBox.height + 100) * currentScale
    ].join(" "));
    var cssRules = {
        'propertyGroups': {
            'block': ['fill'],
            'inline': ['fill', 'stroke', 'stroke-width'],
            'object': ['fill'],
            'headings': ['font', 'font-size', 'font-family', 'font-weight', 'fill', 'display']
        },
        'elementGroups': {
            'block': ['g'],
            'inline': ['circle', 'line'],
            'object': ['svg', 'path'],
            'headings': ['text']
        }
    };
    var g = clone.children("g");
    g.attr('transform', 'translate(' + minX * currentScale + ',' + minY * currentScale + ')' + ' scale(' + currentScale + ')');
    g.inlineStyler(cssRules);

    svgenie.save(document.getElementById("cloned"), { name: DOWNLOAD_FILENAME + '.png' });
    $(graphCaptureSVG).remove();
}

/*
 * Function to get link relations.
 * @param d: data
 */
function linkRelations(d){
    if (d.relations.length > 0){
        var result = "";
        for (i = 0; i < d.relations.length; i++){
            var relation = root.relations[d.relations[i]];
            result = result + '<div class="'+relation.relation+'"><span class="relation-type">' + relation.relation + '</span><br />'
                + root.nodes[relation.source].name + '<i class="fa fa-chevron-right"></i>'
                + root.nodes[relation.target].name + '</div>';
        }
        return result;
    }
}

/*
 * Function to get link id.
 * @param d: data
 */
function getLinkID(d){
    return d.source.id + "_" + d.target.id;
}

/*
 * Function to get resource id.
 * @param d: data
 */
function getNodeID(d){
    return "node_" + d.id;
}

/*
 * Initializing mouse over tooltip to resource links to show relationships.
 */
function setClickableTooltip(target, content){
    $(target).tooltip({
        show: null, // show immediately
        content: content,
        hide: { effect: "" }, //fadeOut
        tooltipClass: 'top',
        position: { my: 'left bottom-10', at: 'right-80 bottom', collision: "none" },
        open: function(event, ui){
            $(ui.tooltip[0]).find('.ui-tooltip-content div').each(function(){
                var self = this;
                $('.filters .tag.hidden span').each(function(){
                    if($(this).html() == $(self).attr('class')){
                        $(self).remove();
                    }
                });
            });
        },
        close: function(event, ui){
            ui.tooltip.hover(
                function () {
                    $(this).stop(true).fadeTo(500, 1);
                },
                function () {
                    $(this).fadeOut(500, function(){
                    })
                }
            );
        }
    });
}