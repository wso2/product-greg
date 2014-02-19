// Custom Arc Attribute, position x&y, value portion of total, total value, Radius
var states,paper;
//========================================
//========================================
//          Look and feel
//========================================
//========================================
//    Node positioning params
var NODE_SHIFT = 210;
var NODE_WIDTH = 120;
var NODE_HEIGHT = 30;
var NODE_STROKE_WIDTH = 1;
var NODE_INT_X = -50;
var NODE_INT_Y = 150;
var NODE_FAN_OUT = 8;
var ARROW_LENGTH = 70;
var ARROW_LENGTH_ENDS = 60;
var ARROW_HEAD_SIZE = 5;
var CIRCLE_INSIDE_RADIUS = NODE_HEIGHT / 2 - 4;
var CIRCLE_RADIUS = NODE_HEIGHT / 2;

//    Color params
var LINE_COLOR = "#000000";
var STATE_LABEL_FILL = '#ffffff';
var STATE_LABEL_STROKE = 'transparent';
var STATE_TEXT = '#000000';
var CIRCLE_FILL = "#000000";
var CIRCLE_STROKE = "#ffffff";
var CIRCLE_INSIDE_STROKE = "#ffffff";
var RECTANGLE_FILL_COMPLETED = "#51a351";
var RECTANGLE_FILL_COMPLETE_OVER = "#408140";
var RECTANGLE_FILL_NEW = "#ffffff";
var RECTANGLE_FILL_NEW_OVER = "#f3f3f3";
var RECTANGLE_FILL_CURRENT = "#999999";
var RECTANGLE_FILL_CURRENT_OVER = "#888888";
var RECTANGLE_STROKE = '#000000';
var FONT_FILL = "#f5f5f5";
var FONT_FILL_NEW = "#000000";

var FONT_SIZE = 9;
var LINE_TEXT_SHIFT = 10;
var TEXT_DY = 2;
//========================================
//========================================
//          Raphael additions
//========================================
//========================================
Raphael.fn.arrow = function (x1, y1, x2, y2, size) {
    var angle = Math.atan2(x1-x2,y2-y1);
    angle = (angle / (2 * Math.PI)) * 360;
    var arrowPath = this.path("M" + x2 + " " + y2 + " L" + (x2 - size*2) + " " + (y2 - size) + " C" + (x2 - 3*size/2) + " " + (y2 - size) + " " + (x2 - 3*size/2) + " " + (y2 + size) + " " + (x2 - size*2) + " " + (y2 + size) + " L" + x2 + " " + y2 ).attr("fill","black").rotate((90+angle),x2,y2);
    var linePath = this.path("M" + x1 + " " + y1 + " L" + x2 + " " + y2).attr('stroke',LINE_COLOR).attr("fill","white");
    return [linePath,arrowPath];
};
var arrows = new Array();
Raphael.fn.fourPointArrow = function (x1, y1, x2, y2,x3,y3,x4,y4,size,type) {
    var angle = (type == "Q" ? Math.atan2((x3 - (x3 - x2)/2 - x4),(y2 + (y2 - y1)) - y4) : Math.atan2(x3 - x4,y3 - y4));
    angle = (angle / (2 * Math.PI)) * 360;
    var arrowPath = this.path("M" + x4 + " " + y4 + " L" + (x4 - size*2) + " " + (y4 - size) + " C" + (x4 - 3*size/2) + " " + (y4 - size) + " " + (x4 - 3*size/2) + " " + (y4 + size) + " " + (x4 - size*2) + " " + (y4 + size) + " L" + x4 + " " + y4 ).attr("fill","black").rotate((-90-angle),x4,y4);
    var linePath = (type == "Q" ? this.path("M" + x1 + " " + y1 + " Q" + (x3 - (x3 - x2)/2) + " " + (y2 + (y2 - y1)) + " " + x4 + " " + y4).attr('stroke',LINE_COLOR) : this.path("M" + x1 + " " + y1 + " L" + x2 + " " + y2 + " L" + x3 + " " + y3 + " L" + x4 + " " + y4).attr('stroke',LINE_COLOR));
    return [linePath,arrowPath];
};
//========================================
//========================================
//          Utility methods
//========================================
//========================================

var getFirstNode = function() {
    var firstNode;
    for (var i = 0; i < states.length; i++) {
        if (states[i].type == "initial") {
            firstNode = states[i];
        }
    }
    return firstNode;
};
var getLastNodes = function() {
    var lastNodes = new Array();
    for (var i = 0; i < states.length; i++) {
        if (states[i].transition.length == 0) {
            lastNodes.push(states[i]);
        }
    }
    return lastNodes;
};
var getNode = function(input) {
    var node,i;
    if (typeof(input) == "number") {
        for (i = 0; i < states.length; i++) {
            if (states[i].order == input) {
                node = states[i];
            }
        }
    } else {
        for (i = 0; i < states.length; i++) {
            if (states[i].id == input) {
                node = states[i];
            }
        }
    }

    return node;
};
var swapOrder = function(node1, node2) {
    var tmpOrder;
    tmpOrder = node1.order;
    node1.order = node2.order;
    node2.order = tmpOrder;
};
var getAllCons = function(node) {
    if (typeof(node) == "string") {
        node = getNode(node);
    }

};


var labelMouseOver = function(line) {
    var tmpLine = line;
    return function() {
        //tmpLine[0].attr('stroke-width', '3');
    };
};

var labelMouseOut = function(line) {
    var tmpLine = line;
    return function() {
        //tmpLine[0].attr('stroke-width', '1');
    };
};

var drawArrow = function(t, dif) {
    var a_order = getNode(t.from).order;
    var a_x = NODE_INT_X + a_order * NODE_SHIFT;
    var a_y = NODE_INT_Y;

    var b_order,line,text,text_width,text_height,rectangle,rectSet;

    if (dif == 1) {
        b_order = a_order + 1;

        line = paper.arrow(a_x + NODE_WIDTH, a_y+NODE_HEIGHT/4, a_x + NODE_SHIFT, a_y+NODE_HEIGHT/4, ARROW_HEAD_SIZE);
        text = paper.text((2 * a_x + NODE_WIDTH + NODE_SHIFT) / 2, a_y-LINE_TEXT_SHIFT+NODE_HEIGHT/4, t.event);

        text_width = text.getBBox().width;
        text_height = text.getBBox().height;
        text_width += 5;
        text_height += 5;

        rectangle = paper.rect((2 * a_x + NODE_WIDTH + NODE_SHIFT) / 2 - text_width / 2-LINE_TEXT_SHIFT, a_y - text_height / 2, text_width, text_height, 0);
        rectangle.attr('fill', STATE_LABEL_FILL).attr('fill-opacity', 0).attr('stroke', STATE_LABEL_STROKE);
        text.toFront();

        rectSet = paper.set();
        rectSet.push(rectangle, text);

        //Handling the label mouse over and out events

        rectSet.mouseover(labelMouseOver(line));
        rectSet.mouseout(labelMouseOut(line));

    } else {
        b_order = a_order - 1;
        a_y = a_y + NODE_HEIGHT;
        line = paper.arrow(a_x, a_y-NODE_HEIGHT/4, a_x - (NODE_SHIFT - NODE_WIDTH), a_y-NODE_HEIGHT/4, ARROW_HEAD_SIZE);
        text = paper.text((2 * a_x - (NODE_SHIFT - NODE_WIDTH)) / 2, a_y+LINE_TEXT_SHIFT-NODE_HEIGHT/4, t.event);


        text_width = text.getBBox().width;
        text_height = text.getBBox().height;
        text_width += 5;
        text_height += 5;

        rectangle = paper.rect((2 * a_x - (NODE_SHIFT - NODE_WIDTH)) / 2 - text_width / 2, a_y - text_height / 2+LINE_TEXT_SHIFT, text_width, text_height, 0);
        rectangle.attr('fill', STATE_LABEL_FILL).attr('fill-opacity', 0).attr('stroke', STATE_LABEL_STROKE);
        text.toFront();

        rectSet = paper.set();
        rectSet.push(rectangle, text);



        //Handling the label mouse over and out events

        rectSet.mouseover(labelMouseOver(line));
        rectSet.mouseout(labelMouseOut(line));
    }
};
var height_top = 1;
var height_bottom = 1;
var draw3LineCon = function(t, dif, cons_from_AtoB) {
    var fromNode = getNode(t.from);
    var targetNode = getNode(t.target.name);
    var a_order = fromNode.order;
    var b_order = targetNode.order;


    var a_x,b_x,c_x,d_x,a_y,b_y,line_up_down_y,line,text,text_width,text_height,rectangle,rectSet;
    var moveFromEdge = NODE_WIDTH/NODE_FAN_OUT;
    var heightShift = 20;
    var additionalHeight = 3 * NODE_HEIGHT / 2;


    if (dif >= 1) {
        if (fromNode.cons_tr == undefined) {
            fromNode.cons_tr = 0;
        }
        if (targetNode.cons_tl == undefined) {
            targetNode.cons_tl = 0;
        }
        a_x = NODE_INT_X + a_order * NODE_SHIFT + NODE_WIDTH - fromNode.cons_tr * NODE_WIDTH/NODE_FAN_OUT - NODE_WIDTH/NODE_FAN_OUT;
        a_y = NODE_INT_Y;

        d_x = NODE_INT_X + b_order * NODE_SHIFT + targetNode.cons_tl * NODE_WIDTH/NODE_FAN_OUT + NODE_WIDTH/NODE_FAN_OUT;
        b_y = NODE_INT_Y;

        line_up_down_y = a_y - additionalHeight - height_top * heightShift;
        line = paper.fourPointArrow(a_x, a_y, a_x, line_up_down_y, d_x, line_up_down_y, d_x, b_y, ARROW_HEAD_SIZE, (dif > states.maxTopDistance ? "L" : "Q"));
        text = paper.text((a_x + d_x) / 2, line_up_down_y -LINE_TEXT_SHIFT, t.event).attr('fill', STATE_TEXT);
        text_width = text.getBBox().width;
        text_height = text.getBBox().height;
        text_width += 5;
        text_height += 5;

        rectangle = paper.rect((a_x + d_x) / 2 - text_width / 2, line_up_down_y - text_height / 2-LINE_TEXT_SHIFT, text_width, text_height, 0);
        rectangle.attr('fill', STATE_LABEL_FILL).attr('fill-opacity', 0).attr('stroke', STATE_LABEL_STROKE);
        text.toFront();

        rectSet = paper.set();
        rectSet.push(rectangle, text);


        //Handling the label mouse over and out events

        rectSet.mouseover(labelMouseOver(line));
        rectSet.mouseout(labelMouseOut(line));

        //draw with height
        height_top++;
        fromNode.cons_tr = fromNode.cons_tr + 1;
        targetNode.cons_tl = targetNode.cons_tl + 1;
    } else if (dif <= -1) {
        if (fromNode.cons_bl == undefined) {
            fromNode.cons_bl = 0;
        }
        if (targetNode.cons_br == undefined) {
            targetNode.cons_br = 0;
        }

        a_x = NODE_INT_X + a_order * NODE_SHIFT + fromNode.cons_bl * NODE_WIDTH/NODE_FAN_OUT + NODE_WIDTH/NODE_FAN_OUT;
        a_y = NODE_INT_Y + NODE_HEIGHT;

        d_x = NODE_INT_X + b_order * NODE_SHIFT + NODE_WIDTH - targetNode.cons_br * NODE_WIDTH/NODE_FAN_OUT - NODE_WIDTH/NODE_FAN_OUT;
        b_y = a_y;

        line_up_down_y = a_y + additionalHeight + height_bottom * heightShift;
        line = paper.fourPointArrow(a_x, a_y, a_x, line_up_down_y, d_x, line_up_down_y, d_x, b_y, ARROW_HEAD_SIZE,  (dif < states.maxBottomDistance ? "L" : "Q"));
        text = paper.text((a_x + d_x) / 2, line_up_down_y+LINE_TEXT_SHIFT, t.event).attr('fill', STATE_TEXT);
        text_width = text.getBBox().width;
        text_height = text.getBBox().height;
        text_width += 5;
        text_height += 5;

        rectangle = paper.rect((a_x + d_x) / 2 - text_width / 2, line_up_down_y - text_height / 2+LINE_TEXT_SHIFT, text_width, text_height, 0);
        rectangle.attr('fill', STATE_LABEL_FILL).attr('fill-opacity', 0).attr('stroke', STATE_LABEL_STROKE);
        text.toFront();

        rectSet = paper.set();
        rectSet.push(rectangle, text);


        //Handling the label mouse over and out events

        rectSet.mouseover(labelMouseOver(line));
        rectSet.mouseout(labelMouseOut(line));

        height_bottom++;
        fromNode.cons_bl = fromNode.cons_bl + 1;
        targetNode.cons_br = targetNode.cons_br + 1;
    }
};
//========================================
//========================================
//          Getting the json and processing
//========================================
//========================================

function drawGraph(json) {
    states = json.state;
    var n = states.length;


    //Finding the cordinates of the points and show them
    var x,y,new_node;

    var total = 60,
            bg, bar, st;

    //assign order number to all the nodes

    for(var i=0;i<states.length;i++){
        states[i].order = i;
    }
    //Sorting nodes


    //Check if the initial nodes number is 1
    var firstNode = getFirstNode();
    if (firstNode.order != 0) {
        var nodeWithOrder_0 = getNode(0);
        swapOrder(firstNode, nodeWithOrder_0);
    }



    //Finding which node has the max number of transitions from a given node
    // And make it the next node
    // Fist we have to get all the connections from one node to the other.
    var allTransitions = new Array();
    for (var i = 0; i < n; i++) {
        var transitions = states[i].transition;
        for (var j = 0; j < transitions.length; j++) {
            transitions[j].from = states[i].id;
            allTransitions.push(transitions[j]);
        }
    }


    //calculate the canves size and create it
    var maxNodeCount=0;
    for (var i = 0; i < n; i++) {
        var findId = states[i].id;
        var tmpMaxNodeCount = 0;
        for(var j=0;j<allTransitions.length;j++){
            if(findId == allTransitions[j].target.name){
                tmpMaxNodeCount++;
            }
        }
        if(tmpMaxNodeCount>maxNodeCount){
            maxNodeCount = tmpMaxNodeCount;
        }
    }

    var paperHeight = maxNodeCount*2*20 + NODE_HEIGHT + 80 + 3 * NODE_HEIGHT / 2;
    var paperWidth = n*(NODE_SHIFT)+NODE_INT_X;

    /*if(paperHeight < 200){
     paperHeight = 200;
     }*/

    paper = new Raphael(document.getElementById('canvas_container'), paperWidth, paperHeight);

    NODE_INT_Y = paperHeight/2;


    // We set the currentNode to the firstNode.
    //Then we find the next node which has the max number of connections from the other one..
    //In order to avoid the back-word traversing of the loop.. we keep another array (nodesInOrder) and remove the elements in that from the calculation

    var currentNode = firstNode;
    var nodesInOrder = new Array(); //To store nodes already ordered

    while (currentNode != null) {
        var currentStateId = currentNode.id;
        var nodesConnectedTo_currentState = new Array();
        for (var j = 0; j < allTransitions.length; j++) {
            if (allTransitions[j].from == currentStateId || allTransitions[j].target.name == currentStateId) {
                var isLinkTo_nodesInOrder = false;
                for(var k=0;k< nodesInOrder.length;k++){
                    if(allTransitions[j].from == nodesInOrder[k].id || allTransitions[j].target.name == nodesInOrder[k].id){
                        isLinkTo_nodesInOrder = true;
                    }
                }
                if(!isLinkTo_nodesInOrder){
                    nodesConnectedTo_currentState.push(allTransitions[j]);
                }
            }
        }


        var maxTransitionId,transitionMaxCount = 0;
        for (var j = 0; j < nodesConnectedTo_currentState.length; j++) {
            var seekingTarget = nodesConnectedTo_currentState[j].from;     //Check in which side is the currentState id is and get the other side of the connection
            if(currentStateId == nodesConnectedTo_currentState[j].from){
                seekingTarget = nodesConnectedTo_currentState[j].target.name;
            }

            var count = 0;
            for (var k = 0; k < nodesConnectedTo_currentState.length; k++) {
                if(nodesConnectedTo_currentState[k].from == seekingTarget || nodesConnectedTo_currentState[k].target.name == seekingTarget) {
                    count++;
                    if(count > transitionMaxCount){
                        transitionMaxCount = count;
                        maxTransitionId = seekingTarget;
                    }
                }
            }
        }
        nodesInOrder.push(currentNode);
        var init_order =  currentNode.order;

        //Re-assign the newly found node to the currentNode variable.
        currentNode =  getNode(maxTransitionId);
        //currentNode.order = (init_order+1);

        var nodeWithOrder_x = getNode(init_order+1);
        swapOrder(currentNode, nodeWithOrder_x);


        if(currentNode.order  == states.length-1){
            currentNode = null;
        }
    }

    //Set the last node to the length of the state nodes
    var lastNodes = getLastNodes();
    var lastNode = lastNodes[0];
    if (lastNode && lastNode.order != states.length-1) {
        var nodeWithOrder_last = getNode(states.length-1);
        swapOrder(lastNode, nodeWithOrder_last);
    }

    var adjacentTop = new Array(), adjacentBottom = new Array();

    for (var i = 0; i < allTransitions.length; i++) {
        var order_a = getNode(allTransitions[i].target.name).order, order_b = getNode(allTransitions[i].from).order;
        var dif = order_a - order_b;
        if (dif > 0) {
            if (dif == 1 && !adjacentTop[order_a]) {
                adjacentTop[order_a] = 1;
            } else if (!states.maxTopDistance) {
                states.maxTopDistance = dif;
            } else if (states.maxTopDistance > dif) {
                states.maxTopDistance = dif;
            }
        } else if (dif < 0) {
            if (dif == -1 && !adjacentBottom[order_a]) {
                adjacentBottom[order_a] = 1;
            } else if (!states.maxBottomDistance) {
                states.maxBottomDistance = dif;
            } else if (states.maxBottomDistance < dif) {
                states.maxBottomDistance = dif;
            }
        }
    }

    //===========================================
    //          Pooh Ordering is done
    //===========================================
    var pre_x,pre_y;

    //Draw the starting circle.
    var c2 = paper.circle(NODE_INT_X+70 + NODE_WIDTH/NODE_FAN_OUT, NODE_INT_Y-ARROW_LENGTH_ENDS, CIRCLE_RADIUS).attr({fill: CIRCLE_FILL,stroke:CIRCLE_STROKE});
    var arrow1 = paper.arrow(NODE_INT_X+70 + NODE_WIDTH/NODE_FAN_OUT, NODE_INT_Y-ARROW_LENGTH_ENDS+CIRCLE_RADIUS, NODE_INT_X+70 + NODE_WIDTH/NODE_FAN_OUT, NODE_INT_Y-CIRCLE_RADIUS, ARROW_HEAD_SIZE);
    NODE_INT_X = NODE_INT_X+ ARROW_LENGTH;
    NODE_INT_Y-=NODE_HEIGHT/2;

    var drawnTransitions = new Array();
    for (i = 0; i < n; i++) {
        x = NODE_INT_X + i * NODE_SHIFT;
        y = NODE_INT_Y;
        var currentNode = getNode(i); //Say A
        var node_transitions = currentNode.transition;
        for(var j=0; j < node_transitions.length; j++){
            var evaluating_target = node_transitions[j].target.name;
            var evaluating_target_node = getNode(evaluating_target);//Say B


            //Is there any other node from A to B drawn
            var cons_from_AtoB = 0;
            for(var k=0;k<drawnTransitions.length;k++){
                if(drawnTransitions[k].target.name == evaluating_target &&
                        drawnTransitions[k].from == currentNode.id){
                    cons_from_AtoB++;
                }
            }
            var order_difference = evaluating_target_node.order-currentNode.order;
            if (cons_from_AtoB == 0) {
                if(order_difference == 1 || order_difference == -1){
                    // draw an arrow from A to B
                    drawArrow(node_transitions[j],order_difference);
                }else{
                    draw3LineCon(node_transitions[j],order_difference,cons_from_AtoB);
                }
            } else if (cons_from_AtoB > 0 && cons_from_AtoB <= 4) {
                // Draw the 4 point line
                draw3LineCon(node_transitions[j],order_difference,cons_from_AtoB);
            } else if (cons_from_AtoB < 0 && cons_from_AtoB >= -4) {
                draw3LineCon(node_transitions[j],order_difference,cons_from_AtoB);
                // Draw two point line with a CV
            }


            drawnTransitions.push(node_transitions[j]);
        }

        pre_x = x;
        pre_y = y;
        
        var pageColor = (currentNode.mode == "completed" ? RECTANGLE_FILL_COMPLETED : (currentNode.mode == "new" ? RECTANGLE_FILL_NEW : RECTANGLE_FILL_CURRENT));
        var pageOverColor = (currentNode.mode == "completed" ? RECTANGLE_FILL_COMPLETE_OVER : (currentNode.mode == "new" ? RECTANGLE_FILL_NEW_OVER : RECTANGLE_FILL_CURRENT_OVER));
        var fontColor = (currentNode.mode == "new" ? FONT_FILL_NEW : FONT_FILL);

        //Store the node position
        var rectangle = paper.rect(x, y, NODE_WIDTH, NODE_HEIGHT, 4);
        rectangle.attr({fill:pageColor,stroke:RECTANGLE_STROKE,'stroke-width':NODE_STROKE_WIDTH, 'stroke-linejoin':'round',cursor:"hand",cursor:"pointer"});
        var word = paper.text(rectangle.getBBox().x + NODE_WIDTH/2, rectangle.getBBox().y + NODE_HEIGHT/2 + TEXT_DY, currentNode.id).attr({"font-size":FONT_SIZE,"font-family":"'Helvetica Neue',Helvetica,Arial,sans-serif","font-weight":"bold",fill:fontColor,stroke:"none",cursor:"hand",cursor:"pointer"});
        var rectangleSet = paper.set();
        rectangleSet.push(rectangle, word);
        rectangleSet.mouseup(loadLc(currentNode.id));
        rectangleSet.mouseover(rectMouseOverHandler(rectangleSet, pageOverColor, fontColor));
        rectangleSet.mouseout(rectMouseOutHandler(rectangleSet, pageColor, fontColor));


    }

    //Draw the End circle.
    if (getLastNodes()[0]) {
        var finalNode_x = NODE_INT_X + NODE_SHIFT*(n-1) + NODE_WIDTH - NODE_WIDTH/8;

        var c3 = paper.circle(finalNode_x, NODE_INT_Y+CIRCLE_RADIUS+ARROW_LENGTH_ENDS, CIRCLE_RADIUS).attr({fill: CIRCLE_STROKE,stroke:CIRCLE_FILL});
        var c = paper.circle(finalNode_x, NODE_INT_Y+CIRCLE_RADIUS+ARROW_LENGTH_ENDS, CIRCLE_INSIDE_RADIUS).attr({fill: CIRCLE_FILL,stroke:CIRCLE_INSIDE_STROKE});

        paper.arrow(finalNode_x, NODE_INT_Y+NODE_HEIGHT, finalNode_x, NODE_INT_Y+ARROW_LENGTH_ENDS, ARROW_HEAD_SIZE);
    }

    //Drawing other final node arrows
    for (var j = 1; j < lastNodes.length; j++) {//We are not taking the 0 the element, since it's been already drown
        var nodeInProcess = lastNodes[j];
        var nodeInProcess_x = NODE_INT_X + NODE_SHIFT * nodeInProcess.order;
        var nodeInProcessCircle_x = nodeInProcess_x + NODE_WIDTH/2;
        var nodeInProcessCircle_y = NODE_INT_Y + ARROW_LENGTH_ENDS;

        paper.circle(nodeInProcessCircle_x, nodeInProcessCircle_y+CIRCLE_RADIUS, CIRCLE_RADIUS).attr({fill: CIRCLE_STROKE,stroke:CIRCLE_FILL});
        paper.circle(nodeInProcessCircle_x, nodeInProcessCircle_y+CIRCLE_RADIUS, CIRCLE_INSIDE_RADIUS).attr({fill: CIRCLE_FILL,stroke:CIRCLE_INSIDE_STROKE});

        paper.arrow(nodeInProcessCircle_x, NODE_INT_Y+NODE_HEIGHT, nodeInProcessCircle_x, nodeInProcessCircle_y, ARROW_HEAD_SIZE);
    }
}

var rectMouseOverHandler = function (rect, color, fontColor) {
    return function() {
        rect[0].animate({ 'fill': color }, 100, 'easeIn');
        if (rect.length > 1) {
            rect[1].attr({ 'fill': fontColor });
        }
    }
};

var rectMouseOutHandler = function (rect, color, fontColor) {
    return function() {
        if (rect.length > 1) {
            rect[1].attr({ 'fill': fontColor });
        }
        rect[0].animate({ fill: color }, 100, 'easeOut');
    }
};
function loadLc(newState){
    return function(){
        var index = window.location.href.indexOf("&");
        window.location = (index > 0 ? window.location.href.substring(0, index) : window.location.href) + "&tab=2&state=" + newState;
    }
}