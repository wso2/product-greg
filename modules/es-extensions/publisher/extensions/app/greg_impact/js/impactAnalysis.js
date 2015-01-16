/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

$(document).ready(function() {

    $("#search").select2({
        placeholder: "Locate a resource",
        data: root.nodes,
        maximumSelectionSize: 1
    }).on("select2-close", function() {
        searchNode();
        $(".reset-locate").show();
    });

    for (i = 0; i < root.nodes.length; i++) {
        linkedByIndex[i + "," + i] = 1;
    };

    root.edges.forEach(function(d) {
        linkedByIndex[d.source.index + "," + d.target.index] = 1;
    });

    var linknodes = $('.linkNode');

    for (var i = 0; i < linknodes.length; i++) {
        setClickableTooltip('#' + linknodes[i].id , alertLinkRelations(linknodes[i].__data__));
    };

    var nodeCircles = $('.nodeCircle');

    for (var i = 0; i < nodeCircles.length; i++) {
        setClickableTooltip('#' + nodeCircles[i].id , alertNodeRelations(nodeCircles[i].__data__));
    };

});

$(window).resize(function() {
    $("svg").height($(window).height());
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

function update() {


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
        .attr("class", "linkg");

    link = linkg.append("line")
        .attr("class", "link");

    linkNode = linkg.append("circle")
        .attr("id", getLinkID)
        .attr("class", "linkNode")
        .attr("title", '<p>&nbsp;</p>')
        .on("click", showRelations)
        .attr("r", 3);

    // Update nodes.
    node = node.data(nodes, function(d) {
        return d.id;
    });

    node.exit().remove();

    var nodeEnter = node.enter().append("g")
        .attr("id", getNodeID)
        .attr("class", function (d) {
            return "node" + ('image' in d ? ' imagenode' : '')  + " nodeCircle";
        })
        .on("click", click);
        //.call(force.drag); //enable node dragging

    var circle = nodeEnter.append("circle")
        .attr("cx", 0)
        .attr("cy", 0)
        .attr("r", 30);

    nodeEnter.attr("nodetype", nodeType);

    node.select("[nodetype=parent] circle")
        .attr("r", 50);

    svg.selectAll(".imagenode")
        .attr("class", "node")
        .append("image")
        .attr("x", -12)
        .attr("y", -12)
        .attr("xlink:href", function (d) {
            var url = "../extensions/app/greg_impact/images/icons/" + d['image'] ;
            var simg = this;
            var img = new Image();
            img.onload = function () {
                d.width = this.width * imageScale;
                d.height = this.height * imageScale;
                simg.setAttribute("width", d.width);
                simg.setAttribute("height", d.height);
            }
            return img.src = url;
        });

    nodeEnter.append("image")
        .attr("x", -24)
        .attr("y", -24)
        .attr("xlink:href", function (d) {
            if( d['alertimage'] != undefined)  {
                var url = "../extensions/app/greg_impact/images/" + d['alertimage'] ;
                var simg = this;
                var img = new Image();
                img.onload = function () {
                    d.width = this.width * imageScale;
                    d.height = this.height * imageScale;
                    simg.setAttribute("width", d.width);
                    simg.setAttribute("height", d.height);
                }
                return img.src = url;
            }
        });

    nodeEnter.append("svg:foreignObject")
        .attr("width", 62)
        .attr("height", 62)
        .attr("y", -60/4)
        .attr("x", -60/4)
        .append("xhtml:span")
        .attr("class", iconClass);

    node.select("[nodetype=parent] foreignObject")
        .attr("width", 106)
        .attr("height", 106)
        .attr("y", -108/4)
        .attr("x", -108/4);

    nodeEnter.append("text")
        .attr("class", "resource-name")
        .attr("dy", 50)
        .attr("dx", -30)
        .text(function(d) { return d.name; });

    nodeEnter.append("text")
        .attr("class", "media-type")
        .attr("dy", 62)
        .attr("dx", -30)
        .text(function(d) { return d.mediaType; });

    nodeEnter.select("[nodetype=parent] text.resource-name")
        .attr("dy", 75)
        .attr("dx", -50);

    nodeEnter.select("[nodetype=parent] text.media-type")
        .attr("dy", 87)
        .attr("dx", -50);
    

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

function iconClass(d) {
    switch(d.mediaType) {
        case "application/wsdl+xml":
            return "fa fa-cube";
            break;
        case "application/wadl+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-uri+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-site+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-servicex+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-service+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-sequence+xml":
            return "fa fa-cube";
            break;
        case "application/x-xsd+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-proxy+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-provider+xml":
            return "fa fa-cube";
            break;
        case "application/policy+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-gadget+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-endpoint+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-ebook+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-document+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-api+xml":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2.endpoint":
            return "fa fa-cube";
            break;
        case "application/vnd.wso2-application+xml":
            return "fa fa-cube";
            break;
        default:
            return "fa fa-cube";
    }
}

//This function looks up whether a pair are neighbours  
function neighboring(a, b) {
    return linkedByIndex[a.index + "," + b.index];
}

var isSame = null,
    delay = 250,
    clicks = 0,
    timer = null;

/*
on double click expand and retract node's children
 */
function doubleclick(d) {

    /*if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        d.children = d._children;
        d._children = null;
    }
    update();*/
}

function closeSidebar() {
    $("#wrapper").removeClass("toggled");
    $("#sidebar-wrapper").removeClass("toggled");
}

function outClick() {
    //if($(this).parents('svg').length > 0) {
    //d3.event.stopPropagation();
    //}
}

function click(d) {

    var self = this;

    d3.selectAll("g").select("circle").classed("active", false);
    d3.select(self).select("circle").classed("active", true);

    // if single click
    if (timer == null) {
        timer = setTimeout(function() {
            clicks = 0;
            timer = null;

            // single click function
            if ((self === isSame) && ($("#wrapper").hasClass("toggled"))) {
                $("#wrapper").removeClass("toggled");
                $("#sidebar-wrapper").removeClass("toggled");
                d3.selectAll("g").select("circle").classed("active", false);
            }
            else {
                $("#wrapper").addClass("toggled");
                $("#sidebar-wrapper").addClass("toggled");
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


    //if (d.relations.length > 0){
    //    var notify = [];
    //    for (i = 0; i < d.relations.length; i++){
    //        notify.push(root.relations[d.relations[i]])
    //    }
    //    //alert(JSON.stringify(notify));
    //    console.log("node " + d.name + " = " + JSON.stringify(notify));
    //}
}

// Returns a list of all nodes under the root.
function flatten(root) {
    var nodes = [], i = 0;

    function recurse(node) {
        if (node.children)
            node.children.forEach(recurse);
        if (!node.id)
            node.id = ++i;
        nodes.push(node);
    }

    recurse(root);
    return nodes;
}

function imageZoom(img, scale) {

    d3.select(img)
        .transition()
        .attr("width", function (d) {
            return scale * d.width;
        })
        .attr("height", function (d) {
            return scale * d.height;
        });
}

function displayInfo(resource){
    $('#name span').text(resource.name);
    $('#mediaType span').text(resource.mediaType);
    var linkString = '<a href = "../carbon/resources/resource.jsp?region=region3&item=resource_browser_menu&path=' +
        encodeURIComponent(resource.path) + '">' + resource.path + '</a>';
    $('#path span').html(linkString);
    if(resource.lcState==null){
        $('#lcState span').text("Not defined");
    } else{
        $('#lcState span').text(resource.lcState) ;
    }
}

function tick() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    linkNode.attr("cx", function(d) { return midPoint(d.source.x,d.target.x) })
            .attr("cy", function(d) { return midPoint(d.source.y,d.target.y) });

    linkg.attr("object", function(d) {
        return JSON.stringify(d)
    });

    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
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

    $("#wrapper").removeClass("toggled");
    $("#sidebar-wrapper").removeClass("toggled");
}

function clearSearchedNode() {

    d3.selectAll("g").select("circle").classed("active", false);
    node.attr("class", "");
    linkg.attr("class", "");

    //if (searchedNode){

        //searchedNode.select("circle").attr("class", nodeType);

        //var rootNode = root.nodes[searchedNode[0][0].__data__.id];
        //rootNode.fixed = false;
        //tick();
        //force.resume();
    //}
}

function searchNode() {

    clearSearchedNode();

    var selectedVal = $('#search').select2('data').name;

    //document.getElementById('search').value;
    var node = svg.selectAll("g");

    if (selectedVal == "none") {
        node.style("stroke", "black").style("stroke-width", "10");
    } else {

        searchedNode = node.filter(function (d, i) {
            return d.name == selectedVal;
        });

        var rootNode = root.nodes[searchedNode[0][0].__data__.id];
        // root.nodes.detect(function(d) {
        //     return d.name == selectedVal;
        // });

        searchedNode.select("circle").classed("active", true);
        displayInfo(searchedNode[0][0].__data__);

        //searchedNode.select("circle")
            //.attr("class", "active");
            //.attr('transform', 'translate('+ $(window).width()/2 +','+ $(window).height()/2 +')');

        //d3.select("#mainG").attr("transform", "translate(200,0)");

        if (rootNode) {
            // rootNode.x = rootNode.px = getCenter().x;
            // rootNode.y = rootNode.py = getCenter().y;
            //rootNode.fixed = true;
            //some other stuff...
        }
        // svg.attr("transform", "translate(" + "0,0" + ")" + " scale(" + "1" + ")");

        zoom.scale(1);
        var coor = zoom.translate();

        zoom.translate( [/*coor[0] + */getCenter().x - rootNode.x, /*coor [1] + */getCenter().y - rootNode.y] );
        zoom.event(svg);

        //tick();
        //force.resume();

        //transform="translate(152.47678677550027,93.15541752921163) scale(0.6434946236506358)
        /*var selected = node.filter(function (d, i) {
            return d.name == selectedVal;
        });
        selected.style("fill", "#000");*/
        //var link = svg.selectAll(".link")
        //link.style("opacity", "0");
        /*d3.selectAll(".node, .link").transition()
            .duration(5000)
            .style("opacity", 1);*/
    }
}

function zoomIn(){
    var coor = zoom.translate();
    var x = (coor[0] - getCenter().x) * 1.1 + getCenter().x;
    var y = (coor[1] - getCenter().y) * 1.1 + getCenter().y;

    zoom.scale(zoom.scale() * 1.1);
    zoom.translate([x, y]);

    zoom.event(svg);
}

function zoomOut(){
    var coor = zoom.translate();
    var x = (coor[0] - getCenter().x) * 0.9 + getCenter().x;
    var y = (coor[1] - getCenter().y) * 0.9 + getCenter().y;

    zoom.scale(zoom.scale() * 0.9);
    zoom.translate([x, y]);

    zoom.event(svg);
}

function zoomFit(){
    //var coor = zoom.translate();
    //var x = (coor[0] - getCenter().x) * 1.1 + getCenter().x;
    //var y = (coor[1] - getCenter().y) * 1.1 + getCenter().y;
    //
    //zoom.scale(zoom.scale(1));
    //zoom.translate([x, y]);
    //
    //zoom.event(svg);
}

function showRelations(d){
    var id = '#' + getLinkID(d);
    console.log("link " + $(id).parent().get(0));
    $(id).css({ opacity: 1 });
    //var tooltip = $('#tooltipG');
    //tooltip.attr("type", "hidden");
    //var input = '<text x="10" y="20" style="fill:red;">' + "edge1";
    //tooltip.append('<ul>');
    /*for (i = 0; i < d.relations.length; i++){
        input = input + '<tspan x="10" y="45">' + JSON.stringify(root.relations[d.relations[i]]) + '</tspan>';
    }*/
    //input = input + '</text>';
    //tooltip.append(input);
    //console.log("link " + $('#search').parent().get(0).tagName);
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
                        //$(this).remove();
                    })
                }
            );
        }  
    });
}
