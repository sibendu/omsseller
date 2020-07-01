function Node(id, data) {
    this.id = id; 
    this.data = data;
    this.parent = null;
    this.children = [];
}

function Tree(data) {
    var node = new Node(0,data);
    this._root = node;
}

 
Tree.prototype.traverseDF = function(callback) {
 
    // this is a recurse and immediately-invoking function
    (function recurse(currentNode) {
        //alert('traversing '+currentNode.id+'  '+currentNode.children); 
        // step 2
        for (var i = 0, length = currentNode.children.length; i < length; i++) {
            // step 3
            recurse(currentNode.children[i]);
        }
 
        // step 4
        callback(currentNode);
 
        // step 1
    })(this._root);
 
};

Tree.prototype.traverseBF = function(callback) {
    var queue = new Queue();
     
    queue.enqueue(this._root);
 
    currentTree = queue.dequeue();
 
    while(currentTree){
        for (var i = 0, length = currentTree.children.length; i < length; i++) {
            queue.enqueue(currentTree.children[i]);
        }
 
        callback(currentTree);
        currentTree = queue.dequeue();
    }
};

Tree.prototype.contains = function(callback, traversal) {
    traversal.call(this, callback);
};

Tree.prototype.add = function(data, toData, traversal) {
    var child = new Node(data),
        parent = null,
        callback = function(node) {
            if (node.data === toData) {
                parent = node;
            }
        };
 
    this.contains(callback, traversal);
 
    if (parent) {
        parent.children.push(child);
        child.parent = parent;
    } else {
        throw new Error('Cannot add node to a non-existent parent.');
    }
};

Tree.prototype.remove = function(data, fromData, traversal) {
    var tree = this,
        parent = null,
        childToRemove = null,
        index;
 
    var callback = function(node) {
        if (node.data === fromData) {
            parent = node;
        }
    };
 
    this.contains(callback, traversal);
 
    if (parent) {
        index = findIndex(parent.children, data);
 
        if (index === undefined) {
            throw new Error('Node to remove does not exist.');
        } else {
            childToRemove = parent.children.splice(index, 1);
        }
    } else {
        throw new Error('Parent does not exist.');
    }
 
    return childToRemove;
};

function findIndex(arr, data) {
    var index;
 
    for (var i = 0; i < arr.length; i++) {
        if (arr[i].data === data) {
            index = i;
        }
    }
 
    return index;
}

function searchNode(id){
    var result; 	
    tree.contains(function(node){    		
    	if (node.id === id) {
    		//alert('Looking: '+node.id+ ' &&  '+node.data.name);	
    		result = node;
    	}
   }, tree.traverseDF);	
   return result;	
}

//populate products Tree from JSON saved in session

//** IMP:: variable productjson MUST BE POPULATED by the page using this js file
//with JSON of the product catalog saved in Http Session during login **

var products = JSON.parse(productjson);

var tree = new Tree({"name":"products"});

products.forEach(display);

function display(value, index, array){
	handleCategory(value, 0);
}

function handleCategory(cat, parentId){

	//alert(parentId+ ' found '+searchNode(tree, parentId));

	if(cat.subcategories){
				
		searchNode(parentId).children.push(new Node(cat.id, {"name":cat.name, "edgeCategory":false}));
		for(var m = 0 ; m < cat.subcategories.length; m++){
			subcat = cat.subcategories[m];
			handleCategory(subcat, cat.id);
		}
	}else if(cat.items){
		searchNode(parentId).children.push(new Node(cat.id, {"name":cat.name, "edgeCategory":true}));
		for(var p = 0 ; p < cat.items.length; p++){
			item = cat.items[p];
			searchNode(cat.id).children.push(new Node(item.id, {"code":item.code,"name":item.name , "description":item.description, "price":item.price, "unit":item.unit,"min":item.min,"max":item.max,"step":item.step,"defaultValue":item.defaultValue})); 
		}			
	}	
}