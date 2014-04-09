function initialize(svg,force,node,link){
var imageScale = 0.9;

var width =950,
    height = 600,
    root;

var path;

 force = d3.layout.force()
    .linkDistance(120)
    .charge(-400)
    .gravity(.01)
    .size([width, height])
    .on("tick", tick);

 svg = d3.select("#graph").append("svg")
    .attr("width", width)
    .attr("height", height)
    .attr("pointer-events", "all")
    .call(d3.behavior.zoom().on("zoom", redraw)).on("dblclick.zoom", null)
    .append('g');

/* svg.append('rect')
 .attr('width', 100)
 .attr('height', height)
 .attr('fill', 'white');*/

    link = svg.selectAll(".link");
    node = svg.selectAll(".node");

 //   svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")");


}

