<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['id'])){
    $id = $_GET['id'];
    
    require'db_connect.php';
    if($update = $conn->query("DELETE FROM contacts WHERE id = $id")){
        //success
        $response["success"]=1;
        $response["message"]="Contact deleted";
        
        //echo JSON
        echo json_encode($response);
    } else {
        //no contact found 
        $response["success"]=0;
        $response["message"]="No contact found";
        //echo JSON 
        echo json_encode($response);
    }
}else {
    //required field missing
    $response["success"]=0;
    $response["message"]="required field missing";
    //echo JSON
    echo json_encode($response);
}
?>