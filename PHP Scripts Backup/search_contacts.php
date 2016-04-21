<?php

require 'db_connect.php';


if (isset($_GET['Name'])){
	$name = trim($_GET['Name']);
	$myArray = array();
if($result = $conn->query("SELECT * FROM contacts WHERE CONTAINS (Name, '$name'")){
    
    while($row=$result->fetch_array(MYSQL_ASSOC)){
        $myArray[]=$row;     
    }
    
    echo json_encode($myArray);
    $myArray["success"]=1;
    echo json_encode($myArray);
	print(json_encode($myArray));
} else {
    $myArray["success"]=0;
    $myArray["message"]= "an error occured";
    echo json_encode($myArray);
}
//$result->close();
}



?>