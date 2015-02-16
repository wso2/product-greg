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

/**
 * @set default global variables
 */
var svgIconsFolder = "../extensions/app/greg_impact/images/svg/",
    footerHeight = 50;

/**
 * Impact analysis: Dependency Graph UI functions
 * @method functions to run on DOM ready
 */
$(document).ready(function() {

    $('#search').select2({
        placeholder: 'Find Resource',
        data: root.nodes,
        multiple: false,
        //width: "copy",
        width: "100%",
        formatResult: function (object, container, d){
            return  '<div class="item">' +
                        '<div class="text">' +
                            '<div class="resource-name">' + object.text + '</div>' +
                            '<div class="media-type">' + object.mediaType + '</div>' +
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
        setClickableTooltip('#' + linknodes[i].id , alertLinkRelations(linknodes[i].__data__));
    }

    var nodeCircles = $('.nodeCircle');
    for (var i = 0; i < nodeCircles.length; i++) {
        setClickableTooltip('#' + nodeCircles[i].id , alertNodeRelations(nodeCircles[i].__data__));
    }

});

$(window).resize(function() {
    d3.select("svg").attr("width", $(window).width());
    d3.select("svg").attr("height", ($(window).height()-footerHeight));
    d3.select("svg").attr("viewBox", "0 0 " + $(window).width() + " " + ($(window).height()-footerHeight));
});

$(window).load(function() {
    clearSearchOperation();
    $(".searchfield").css("visibility", "visible");
});

function redraw() {
    svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")");
}

function getCenter() {
    var center = {
        x : this.width / 2,
        y : this.height / 2
    };
    return center;
}

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
        .enter().append("g");

    link = linkg.append("line")
        .attr("class", "link");

    linkNode = linkg.append("circle")
        .attr("id", getLinkID)
        .attr("class", "linkNode")
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
        .on("click", click)
        .attr("group", "node");
        //.call(force.drag); //enable node dragging

    var circle = nodeEnter.append("circle")
        .attr("cx", 0)
        .attr("cy", 0)
        .attr("r", 35)
        .attr("active-status", "");

    nodeEnter.attr("nodetype", nodeType);

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
        .attr("class", "media-type")
        .attr("dy", 78)
        .attr("dx", -40)
        .text(function(d) { return d.mediaType; });

    nodeEnter.select("[nodetype=parent] text.resource-name")
        .attr("dy", 100)
        .attr("dx", -70);

    nodeEnter.select("[nodetype=parent] text.media-type")
        .attr("dy", 120)
        .attr("dx", -70);

}

/*function isPointInPoly(poly, pt){
    for(var c = false, i = -1, l = poly.length, j = l - 1; ++i < l; j = i)
        ((poly[i].y <= pt.y && pt.y < poly[j].y) || (poly[j].y <= pt.y && pt.y < poly[i].y))
        && (pt.x < (poly[j].x - poly[i].x) * (pt.y - poly[i].y) / (poly[j].y - poly[i].y) + poly[i].x)
        && (c = !c);
    return c;
}*/

function drawArrows() {
    var markerWidth = 10,
        markerHeight = 18,
        cRadius = 29,
        refX = cRadius + (markerWidth * 2),
        refY = 0,
        drSub = cRadius + refY;


    svg.append("svg:defs").selectAll("marker")
        .data(["arrow"])
        .enter().append("svg:marker")
        .attr("id", "arrow")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", refX)
        .attr("refY", refY)
        .attr("markerWidth", markerWidth)
        .attr("markerHeight", markerHeight)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");

    path = svg.append("svg:g").selectAll("line")
        .attr("class", function (d) {
            return "link";
        })
        .attr("marker-end", function (d) {
            return "url(#arrow)"})
        .data(force.links())
        .enter();
}

// Get node types ex:- Parent > Child
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

// Defining icons for resource type
function nodeIcon(getType) {

    switch(getType) {
        case "application/wsdl+xml":
            return "xml6.svg";
            break;
        case "application/wadl+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-uri+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-site+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-servicex+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-service+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-sequence+xml":
            return "xml6.svg";
            break;
        case "application/x-xsd+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-proxy+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-provider+xml":
            return "xml6.svg";
            break;
        case "application/policy+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-gadget+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-endpoint+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-ebook+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-document+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2-api+xml":
            return "xml6.svg";
            break;
        case "application/vnd.wso2.endpoint":
            return "meeting3.svg";
            break;
        case "application/vnd.wso2-application+xml":
            return "xml6.svg";
            break;
        default:
            return "rdf.svg";
    }
}

function dragstart(d){
    d3.select(this).classed("fixed", d.fixed = true);
}

//This function looks up whether a pair are neighbours  
function neighboring(a, b) {
    return linkedByIndex[a.index + "," + b.index];
}

var isSame = null,
    delay = 300,
    clicks = 0,
    timer = null;

/*
on double click expand and retract node's children
 */
function showHideChildren(d) {
    if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        d.children = d._children;
        d._children = null;
    }
    update();
}

function closeSidebar() {
    //$("#wrapper").removeClass("toggled");
    //$("#sidebar-wrapper").removeClass("toggled");
    $('#urlView #path').html("<i>Select a node to view path</i>");
}

function openSidebar() {
    //$("#wrapper").addClass("toggled");
    //$("#sidebar-wrapper").addClass("toggled");
}

function outClick() {

    closeSidebar();
    clearSearchOperation();
    d3.selectAll("g").select("circle").classed("active", false);
    d3.selectAll("g").attr("active-status", "");

    node.attr("class", "");
    linkg.attr("class", "");
    isSame = self;
    selectedNode = -1;
}

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

    // if single click
    if (timer == null) {
        timer = setTimeout(function() {
            clicks = 0;
            timer = null;

            // single click function
            if ((self === isSame) && ($("#wrapper").hasClass("toggled"))) {
                closeSidebar();
                d3.selectAll("g").select("circle").classed("active", false);
            }
            else {
                openSidebar();
            }
            isSame = self;
        }, delay);

        // Reset relation highlight
        node.attr("class", "");
        linkg.attr("class", "");
    }

    // if double click
    if(clicks === 1) {
        clearTimeout(timer);
        timer = null;
        clicks = -1;

        // double click function
        if(selectedNode == -1 || selectedNode != d.index){
            // highlight nodes if they are connected to source
            node.attr("class", function(o) {
                return neighboring(o, d) || neighboring(d, o) ? "active" : "inactive";
            });
            // highlight links if they are connected to source
            linkg.attr("class", function(o) {
                return (d.index == o.target.index) || (d.index == o.source.index) ? "active" : "inactive";
            });

            //var order = {red: -1, green: 0, orange: 1};
            //
            //node.sort(function(a, b) {
            //    return order[a.d3.selectAll('.active')] < order[b.d3.selectAll('.inactive')] ? -1 :
            // order[b.d3.selectAll('.inactive')] < order[a.d3.selectAll('.active')] ? 1 : 0;
            //});

            d3.select(self).attr("active-status", "groupselect");
            selectedNode = d.index;
        }
        else{
            // Reset relation highlight
            node.attr("class", "");
            linkg.attr("class", "");

            isSame = self;
            selectedNode = -1;
        }
    }
    clicks++;

    displayInfo(d);
    return false;
}

//function imageZoom(img, scale) {
//    d3.select(img)
//        .transition()
//        .attr("width", function (d) {
//            return scale * d.width;
//        })
//        .attr("height", function (d) {
//            return scale * d.height;
//        });
//}

function displayInfo(resource){

    $('#name').text(resource.name);
    $('#mediaType').text(resource.mediaType);
    var linkString = '<a href = "../../carbon/resources/resource.jsp?region=region3&item=resource_browser_menu&path=' +
        encodeURIComponent(resource.path) + '">' + resource.path + '</a>';
    $('#path').html(linkString);
    if(resource.lcState==null){
        $('#lcState').text("Not defined");
    } else{
        $('#lcState').text(resource.lcState);
    }
}

var screenOrientation = $(window).width() > $(window).height() ? .5625 : .9625;

function tick(){
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return screenOrientation * d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return screenOrientation * d.target.y; });

    linkNode.attr("cx", function(d) { return midPoint(d.source.x,d.target.x) })
            .attr("cy", function(d) { return screenOrientation * midPoint(d.source.y,d.target.y) });

    linkg.attr("object", function(d) {
        return JSON.stringify(d)
    });

    //console.log(nodeEnter.html());

    nodeEnter.attr("transform", function(d) { return "translate(" + d.x + "," + screenOrientation * d.y + ")"; });

    zoomFit(); //Zoom out graph to fit screen on page load
}

function tickCallback(){
    $("#preLoader").hide();
    $(".zoom").attr("disabled", false);
}

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

function setupSearch() {
    for (var i = 0; i < root.nodes.length; i++) {
        searchArray.push(root.nodes[i].name);
    }

    searchArray = searchArray.sort();
}

function clearSearchOperation() {
    $("#search").select2('data', null);
    $(".reset-locate").hide();

    clearSearchedNode();
    closeSidebar();
    //zoomFit();
}

function clearSearchedNode() {
    d3.selectAll("g").select("circle").classed("active", false);
    node.attr("class", "");
    linkg.attr("class", "");
    d3.selectAll("g").attr("active-status", "");
}

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
        var coor = zoom.translate();

        zoom.translate( [/*coor[0] + */getCenter().x - rootNode.x, /*coor [1] + */getCenter().y - rootNode.y] );
        zoom.event(svg);
    }
}

/* Function to zoom in graph */
function zoomIn(){
    var coor = zoom.translate();
    var x = (coor[0] - getCenter().x) * 1.1 + getCenter().x;
    var y = (coor[1] - getCenter().y) * 1.1 + getCenter().y;

    zoom.scale(zoom.scale() * 1.1);
    zoom.translate([x, y]);

    zoom.event(svg);
}

/* Function to zoom out graph */
function zoomOut(){
    var coor = zoom.translate();
    var x = (coor[0] - getCenter().x) * 0.9 + getCenter().x;
    var y = (coor[1] - getCenter().y) * 0.9 + getCenter().y;

    zoom.scale(zoom.scale() * 0.9);
    zoom.translate([x, y]);

    zoom.event(svg);
}

/* Function to zoom out/in graph to fit current screen */
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

/* Function to show resource relationships */
function showRelations(d){
    var id = '#' + getLinkID(d);
    //console.log("link " + $(id).parent().get(0));
    $(id).css({ opacity: 1 });
}

/* Function to save current screen as a .png image file */
function svgDownload(){

    $("#graph svg").clone().appendTo("#graph-capture");
    $("#graph-capture svg").attr("id","cloned");
    var cssRules = {
        'propertyGroups' : {
            'block' : ['fill'],
            'inline' : ['fill', 'stroke', 'stroke-width'],
            'object' : ['fill'],
            'headings' : ['font', 'font-size', 'font-family', 'font-weight', 'fill', 'display']
        },
        'elementGroups' : {
            'block' : ['g'],
            'inline' : ['circle', 'line'],
            'object' : ['svg', 'path'],
            'headings' : ['text']
        }
    };
    $("#graph-capture svg g").inlineStyler(cssRules);
    svgenie.save(document.getElementById("cloned"), { name:"graph.png" });
    $("#graph-capture svg").remove();

}

function alertLinkRelations(d){

    if (d.relations.length > 0){
        var result = "";
        for (i = 0; i < d.relations.length; i++){
            var relation = root.relations[d.relations[i]];
            result = result + '<span class="relation-type">' + relation.relation + '</span><br />'
                + root.nodes[relation.source].name + '<i class="fa fa-chevron-right"></i>'
                + root.nodes[relation.target].name + '<br>';
        }
        return result;
    }
}

function alertNodeRelations(nodeData){
    if (nodeData.relations.length > 0){
        var result = "";
        for (i = 0; i < nodeData.relations.length; i++){
            var relation = root.relations[nodeData.relations[i]];
            result = result + relation.relation + '<br>' + root.nodes[relation.source].name + ' > '
                + root.nodes[relation.target].name + '<br>';
        }
        return result;
    }
}

function getLinkID(d){
    return d.source.id + "_" + d.target.id;
}

function getNodeID(d){
    return "node_" + d.id;
}

function setClickableTooltip(target, content){
    $(target).tooltip({
        show: null, // show immediately 
        content: content,
        hide: { effect: "" }, //fadeOut
        tooltipClass: 'top',
        position: { my: 'left bottom-10', at: 'right-78 bottom', collision: "none" },
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