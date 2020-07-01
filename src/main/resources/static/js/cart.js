var cartitems = [];
var json = "";
if(localStorage) {
	json = localStorage.getItem("cartitems");
	if(json){
		//alert(json);
		cartitems = JSON.parse(json);
    	//alert("Got cart from local storage :: "+ cartitems);
	}	
} else if(sessionStorage) {
	json = sessionStorage.getItem("cartitems", item);
	if(json){
		//alert(json);
		cartitems = JSON.parse(json);
    	//alert("Got cart from session storage :: "+ cartitems);
	}
} else {
    alert("Cart facility may not work on your device; please contact admin!");
}

function isItemInCart(code) {
	//alert(cartitems);
	var itemExistsInCart = false;
	for( i = 0; i < cartitems.length; i++){
		thisItem = cartitems[i];
		if(thisItem.code == code){
			itemExistsInCart = true;
		}
	}
	return itemExistsInCart;
}

function getItemFromCart(code) {
	for( i = 0; i < cartitems.length; i++){
		thisItem = cartitems[i];
		if(thisItem.code == code){
			return thisItem;
		}
	}
	return null;
}

function getCart(){
	var items;
	if(localStorage) {
		json = localStorage.getItem("cartitems");
		items = JSON.parse(json);
	    //alert("From local storage :: "+ json);
	} else if(sessionStorage) {
		json = sessionStorage.getItem("cartitems", item);
		items = JSON.parse(json);
	    //alert("From session storage :: "+ json + ' '+json.name);
	} else {
	    alert("Cart may not work on your device; please contact admin!");
	}
	//alert('Cart length = '+items.length);
	return items;
}

function saveCart() {
	//alert('Cart length = '+cartitems.length);
	if(localStorage) {
	    localStorage.setItem("cartitems", JSON.stringify(cartitems));
	    //alert("Stored in local storage");
	} else if(sessionStorage) {
		sessionStorage.setItem("cartitems", JSON.stringify(cartitems));
	    //alert("Stored in session storage");
	} else {
	    alert("Cart may not work on your device; please contact admin!");
	}
}

function addItemToCart(id, code, name, description, price, unit, fieldName) {
	//alert(id+' '+name+' '+code+' '+name+' '+description+' '+price+' '+unit+' '+quantity);
	var input = $("input[name='"+fieldName+"']");
    var quantity = parseFloat(input.val());
	//alert(input+ ' '+quantity);
	var item = {"id": id,"code":code,"name": name,"description": description,"price":price,"unit":unit,"quantity":quantity};
	
	if(isItemInCart(item.code)){
		for( i = 0; i < cartitems.length; i++){
			thisItem = cartitems[i];
			if(thisItem.code == item.code){ //Already in cart; replace
				cartitems[i] = item;
			}
		}
	}else{
		cartitems.push(item);		
	}
	
	saveCart(); // Time to save the cart

	$('#btnRemoveFromCart-'+code).prop('disabled', false);			
	$('#btnAddToCart-'+code).text('Update Cart');

	alert("Cart updated: Items = "+cartitems.length);
}


function removeItemFromCart(id, code, name, description, price, unit, fieldName) {
	
	for( i = 0; i < cartitems.length; i++){
		thisItem = cartitems[i];
		if(thisItem.code == code){
			cartitems.splice(i, 1);
		}
	}
	
	saveCart(); // Time to save the cart

	var input = $("input[name='"+fieldName+"']");
    
	$('#btnRemoveFromCart-'+code).prop('disabled', true);
	$('#btnAddToCart-'+code).text('Add To Cart');
	
	//$('#itemRow-'+code).remove();
	
	alert("Cart updated: Items = "+cartitems.length);
}